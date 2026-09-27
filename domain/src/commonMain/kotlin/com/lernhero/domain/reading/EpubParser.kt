package com.lernhero.domain.reading

/**
 * Zamienia EPUB na [BookContent].
 *
 * EPUB to zip z plikami XHTML. Kolejnosc czytania nie wynika z nazw plikow, tylko z pliku `.opf`:
 * `META-INF/container.xml` wskazuje `.opf`, a w nim `spine` wylicza rozdzialy po kolei, odsylajac
 * do `manifest` po sciezki. Z kazdego XHTML-a bierzemy tylko tekst: naglowki `h1`-`h6` zaczynaja
 * nowy rozdzial, elementy blokowe (`p`, `div`, `li`...) to akapity, reszta (style, obrazki,
 * skrypty) odpada.
 *
 * W odroznieniu od [PlainTextParser] niczego tu nie zgadujemy - naglowki sa oznaczone w pliku,
 * wiec spis tresci jest dokladny.
 */
object EpubParser {

    /** Null, gdy plik nie jest poprawnym EPUB-em albo nie ma w nim zadnego tekstu. */
    fun parse(
        bytes: ByteArray,
        inflate: (compressed: ByteArray, uncompressedSize: Int) -> ByteArray?
    ): BookContent? {
        val zip = ZipArchive(bytes, inflate)
        val container = zip.readText("META-INF/container.xml") ?: return null
        val opfPath = FULL_PATH.find(container)?.groupValues?.get(1)?.let(::decodeEntities) ?: return null
        val opf = zip.readText(opfPath) ?: return null
        val opfDir = opfPath.substringBeforeLast('/', missingDelimiterValue = "")

        val manifest = ITEM_TAG.findAll(opf).mapNotNull { match ->
            val attrs = match.value
            val id = attr(attrs, "id") ?: return@mapNotNull null
            val href = attr(attrs, "href") ?: return@mapNotNull null
            id to ManifestItem(href, attr(attrs, "properties").orEmpty())
        }.toMap()

        // linear="no" to strony poza glownym tokiem czytania (np. sama okladka).
        val spine = ITEMREF_TAG.findAll(opf)
            .filter { attr(it.value, "linear") != "no" }
            .mapNotNull { attr(it.value, "idref") }
            .toList()

        val builder = ChapterBuilder()
        spine.forEach { idref ->
            val item = manifest[idref] ?: return@forEach
            // Spis tresci EPUB 3 bywa wpiety w spine - to lista linkow, nie tresc ksiazki.
            if ("nav" in item.properties.split(' ')) return@forEach
            val path = resolve(opfDir, item.href)
            val xhtml = zip.readText(path) ?: return@forEach
            extractBlocks(xhtml, builder)
        }

        val chapters = builder.finish()
        return chapters.takeIf { list -> list.any { it.paragraphs.isNotEmpty() } }?.let(::BookContent)
    }

    private class ManifestItem(val href: String, val properties: String)

    /**
     * Sklada rozdzialy. Nowy zaczyna WYLACZNIE naglowek, nie granica pliku: Gutenberg tnie dlugie
     * rozdzialy na kilka plikow w przypadkowych miejscach, a plik bez naglowka to dalszy ciag.
     */
    private class ChapterBuilder {
        private val chapters = mutableListOf<Chapter>()
        private var title: String? = null
        private var paragraphs = mutableListOf<String>()

        fun heading(text: String) {
            // Dwa naglowki pod rzad ("Czesc I" + "Rozdzial 1") skladamy w jeden tytul, zamiast
            // produkowac pusty rozdzial z samym tytulem.
            if (title != null && paragraphs.isEmpty()) {
                title = "$title · $text"
            } else {
                close()
                title = text
            }
        }

        fun paragraph(text: String) {
            paragraphs += text
        }

        private fun close() {
            if (title != null || paragraphs.isNotEmpty()) chapters += Chapter(title, paragraphs.toList())
            title = null
            paragraphs = mutableListOf()
        }

        fun finish(): List<Chapter> {
            close()
            return chapters
        }
    }

    private fun extractBlocks(xhtml: String, out: ChapterBuilder) {
        val bodyStart = BODY_OPEN.find(xhtml)?.range?.last?.plus(1) ?: 0
        val text = StringBuilder()
        var headingDepth = 0
        // Poziom zagniezdzenia, na ktorym zaczal sie pomijany element, i jego nazwa.
        var skipTag: String? = null
        var skipDepth = 0

        fun flush() {
            val value = decodeEntities(text.toString()).replace(WHITESPACE, " ").trim()
            text.clear()
            if (value.isEmpty()) return
            if (headingDepth > 0) out.heading(value) else out.paragraph(value)
        }

        var pos = bodyStart
        while (pos < xhtml.length) {
            val lt = xhtml.indexOf('<', pos)
            if (lt < 0) {
                if (skipTag == null) text.append(xhtml, pos, xhtml.length)
                break
            }
            if (skipTag == null) text.append(xhtml, pos, lt)

            if (xhtml.startsWith("<!--", lt)) {
                val end = xhtml.indexOf("-->", lt)
                pos = if (end < 0) xhtml.length else end + 3
                continue
            }
            val gt = xhtml.indexOf('>', lt)
            if (gt < 0) break
            val tag = xhtml.substring(lt + 1, gt)
            pos = gt + 1

            val closing = tag.startsWith("/")
            val selfClosing = tag.endsWith("/")
            val name = tag.removePrefix("/").takeWhile { !it.isWhitespace() && it != '/' }
                .substringAfter(':').lowercase()
            if (name.isEmpty() || name.startsWith("!") || name.startsWith("?")) continue

            if (skipTag != null) {
                if (name == skipTag) {
                    if (closing) skipDepth-- else if (!selfClosing) skipDepth++
                    if (skipDepth == 0) skipTag = null
                }
                continue
            }

            if (!closing && !selfClosing && (name in SKIPPED_TAGS || isBoilerplate(tag))) {
                // Odsylacz do przypisu siedzi w srodku zdania - nie wolno go tam przecinac.
                if (headingDepth == 0 && name != "a" && name != "sup" && name != "span") flush()
                skipTag = name
                skipDepth = 1
                continue
            }

            when {
                name == "br" -> if (headingDepth > 0) text.append(' ') else flush()
                name in HEADING_TAGS -> {
                    flush()
                    if (closing) headingDepth = (headingDepth - 1).coerceAtLeast(0)
                    else if (!selfClosing) headingDepth++
                }
                name in BLOCK_TAGS -> if (headingDepth == 0) flush()
            }
        }
        flush()
    }

    /**
     * To, co nie jest trescia ksiazki: naglowek i stopka licencyjna Project Gutenberga, wstawki
     * zbiorkowe Wolnych Lektur i odsylacze do przypisow (numerek przyklejony do slowa - "Litwie1"
     * - psulby i tekst, i tlumaczenie slowa).
     */
    private fun isBoilerplate(tag: String): Boolean {
        val id = attr(tag, "id")
        val classes = attr(tag, "class").orEmpty().split(' ')
        val epubType = attr(tag, "epub:type").orEmpty()
        return id == "pg-header" || id == "pg-footer" ||
            classes.any { it in SKIPPED_CLASSES } ||
            "noteref" in epubType
    }

    private fun attr(tag: String, name: String): String? =
        Regex("""(?:^|\s)$name\s*=\s*(["'])(.*?)\1""", RegexOption.DOT_MATCHES_ALL)
            .find(tag)?.groupValues?.get(2)

    /** Sciezka z manifestu jest wzgledna do katalogu `.opf` i moze miec %20 i kotwice. */
    private fun resolve(baseDir: String, href: String): String {
        val clean = percentDecode(decodeEntities(href.substringBefore('#')))
        val parts = (if (baseDir.isEmpty()) emptyList() else baseDir.split('/')).toMutableList()
        clean.split('/').forEach { part ->
            when (part) {
                "", "." -> Unit
                ".." -> if (parts.isNotEmpty()) parts.removeAt(parts.lastIndex)
                else -> parts += part
            }
        }
        return parts.joinToString("/")
    }

    private fun percentDecode(value: String): String {
        if ('%' !in value) return value
        val out = ArrayList<Byte>(value.length)
        var i = 0
        while (i < value.length) {
            val c = value[i]
            if (c == '%' && i + 2 <= value.lastIndex) {
                val byte = value.substring(i + 1, i + 3).toIntOrNull(16)
                if (byte != null) {
                    out += byte.toByte()
                    i += 3
                    continue
                }
            }
            c.toString().encodeToByteArray().forEach { out += it }
            i++
        }
        return out.toByteArray().decodeToString()
    }

    internal fun decodeEntities(text: String): String {
        if ('&' !in text) return text
        return ENTITY.replace(text) { match ->
            val body = match.groupValues[1]
            val codePoint = when {
                body.startsWith("#x") || body.startsWith("#X") -> body.substring(2).toIntOrNull(16)
                body.startsWith("#") -> body.substring(1).toIntOrNull()
                else -> NAMED_ENTITIES[body]
            } ?: return@replace match.value
            codePointToString(codePoint)
        }
    }

    private fun codePointToString(codePoint: Int): String = when {
        codePoint == 0xAD -> "" // miekki lacznik - w reflow tylko przeszkadza
        codePoint == 0xA0 -> " "
        codePoint < 0x10000 -> codePoint.toChar().toString()
        else -> {
            val offset = codePoint - 0x10000
            charArrayOf((0xD800 + (offset shr 10)).toChar(), (0xDC00 + (offset and 0x3FF)).toChar()).concatToString()
        }
    }

    private val FULL_PATH = Regex("""full-path\s*=\s*["']([^"']+)["']""")
    private val ITEM_TAG = Regex("""<(?:\w+:)?item\s[^>]*>""")
    private val ITEMREF_TAG = Regex("""<(?:\w+:)?itemref\s[^>]*>""")
    private val BODY_OPEN = Regex("""<(?:\w+:)?body[^>]*>""", RegexOption.IGNORE_CASE)
    private val WHITESPACE = Regex("""\s+""")
    private val ENTITY = Regex("""&(#[xX]?[0-9a-fA-F]+|[a-zA-Z]+);""")

    private val HEADING_TAGS = setOf("h1", "h2", "h3", "h4", "h5", "h6")
    private val BLOCK_TAGS = setOf(
        "p", "div", "li", "blockquote", "section", "article", "tr", "dd", "dt",
        "pre", "hr", "table", "ul", "ol", "figure", "figcaption", "aside", "header", "footer"
    )
    /** Podpisy ilustracji wpadaja u Gutenberga nawet do srodka <h2> - bez obrazka nic nie znacza. */
    private val SKIPPED_CLASSES = setOf(
        "pg-boilerplate", "fundraising", "anchor", "noteref", "caption", "x-ebookmaker-pageno", "pageno"
    )
    private val SKIPPED_TAGS = setOf("script", "style", "head", "svg", "math", "title")

    private val NAMED_ENTITIES = mapOf(
        "amp" to '&'.code, "lt" to '<'.code, "gt" to '>'.code, "quot" to '"'.code, "apos" to '\''.code,
        "nbsp" to 0xA0, "shy" to 0xAD, "mdash" to 0x2014, "ndash" to 0x2013, "hellip" to 0x2026,
        "lsquo" to 0x2018, "rsquo" to 0x2019, "ldquo" to 0x201C, "rdquo" to 0x201D,
        "bdquo" to 0x201E, "sbquo" to 0x201A, "laquo" to 0xAB, "raquo" to 0xBB,
        "copy" to 0xA9, "reg" to 0xAE, "trade" to 0x2122, "middot" to 0xB7, "bull" to 0x2022,
        "eacute" to 0xE9, "egrave" to 0xE8, "agrave" to 0xE0, "aacute" to 0xE1, "ccedil" to 0xE7,
        "uuml" to 0xFC, "ouml" to 0xF6, "auml" to 0xE4, "szlig" to 0xDF, "oacute" to 0xF3,
        "iacute" to 0xED, "uacute" to 0xFA, "ntilde" to 0xF1, "deg" to 0xB0, "times" to 0xD7
    )
}
