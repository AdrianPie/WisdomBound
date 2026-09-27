package com.lernhero.domain.reading

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EpubParserTest {

    /** Zip bez kompresji (metoda 0) - wystarczy do testu, a dekompresor nie jest wtedy wolany. */
    private fun storedZip(files: List<Pair<String, String>>): ByteArray {
        val out = ArrayList<Byte>()
        val central = ArrayList<Byte>()
        fun MutableList<Byte>.u16(v: Int) { add(v.toByte()); add((v shr 8).toByte()) }
        fun MutableList<Byte>.u32(v: Int) { u16(v and 0xFFFF); u16(v ushr 16) }
        files.forEach { (name, content) ->
            val nameBytes = name.encodeToByteArray()
            val data = content.encodeToByteArray()
            val offset = out.size
            out.u32(0x04034b50); out.u16(20); out.u16(0); out.u16(0); out.u16(0); out.u16(0)
            out.u32(0); out.u32(data.size); out.u32(data.size); out.u16(nameBytes.size); out.u16(0)
            out.addAll(nameBytes.toList()); out.addAll(data.toList())
            central.u32(0x02014b50); central.u16(20); central.u16(20); central.u16(0); central.u16(0)
            central.u16(0); central.u16(0); central.u32(0); central.u32(data.size); central.u32(data.size)
            central.u16(nameBytes.size); central.u16(0); central.u16(0); central.u16(0); central.u16(0)
            central.u32(0); central.u32(offset); central.addAll(nameBytes.toList())
        }
        val centralOffset = out.size
        out.addAll(central)
        out.u32(0x06054b50); out.u16(0); out.u16(0); out.u16(files.size); out.u16(files.size)
        out.u32(central.size); out.u32(centralOffset); out.u16(0)
        return out.toByteArray()
    }

    private fun epub(vararg chapters: Pair<String, String>) = storedZip(
        listOf(
            "mimetype" to "application/epub+zip",
            "META-INF/container.xml" to """<container><rootfiles><rootfile full-path="OPS/content.opf"/></rootfiles></container>""",
            "OPS/content.opf" to buildString {
                append("<package><manifest>")
                chapters.forEachIndexed { i, (href, _) -> append("""<item id="c$i" href="$href" media-type="application/xhtml+xml"/>""") }
                append("""<item id="nav" href="nav.xhtml" properties="nav"/>""")
                append("</manifest><spine><itemref idref=\"nav\"/>")
                // Odwrotna kolejnosc w manifescie niz w spine - liczy sie spine.
                chapters.indices.forEach { append("""<itemref idref="c$it"/>""") }
                append("</spine></package>")
            },
            "OPS/nav.xhtml" to "<html><body><p>Spis tresci</p></body></html>"
        ) + chapters.map { (href, body) -> "OPS/${href.replace("%20", " ")}" to "<html><head><title>x</title></head><body>$body</body></html>" }
    )

    private val noInflate: (ByteArray, Int) -> ByteArray? = { _, _ -> error("stored entries only") }

    @Test
    fun chaptersFollowSpineAndHeadings() {
        val content = EpubParser.parse(
            epub(
                "a.xhtml" to "<h2>Rozdzial 1</h2><p>Pierwszy   akapit\n z lamaniem.</p><p>Drugi&nbsp;akapit &mdash; koniec.</p>",
                "b%20c.xhtml" to "<h2>Rozdzial 2</h2><p>Tekst<br/>po br.</p>"
            ),
            noInflate
        )!!

        assertEquals(listOf("Rozdzial 1", "Rozdzial 2"), content.chapters.map { it.title })
        assertEquals(listOf("Pierwszy akapit z lamaniem.", "Drugi akapit — koniec."), content.chapters[0].paragraphs)
        assertEquals(listOf("Tekst", "po br."), content.chapters[1].paragraphs)
    }

    @Test
    fun skipsFootnoteAnchorsAndBoilerplate() {
        val content = EpubParser.parse(
            epub(
                "a.xhtml" to """<section id="pg-header"><p>Project Gutenberg license</p></section>
                    <h2>Tytul</h2><p>na Litwie<a class="anchor" href="n.xhtml#1"><sup>1</sup></a>.</p>
                    <div class="fundraising"><p>Wesprzyj nas</p></div><style>p{}</style>"""
            ),
            noInflate
        )!!

        assertEquals(listOf(Chapter("Tytul", listOf("na Litwie."))), content.chapters)
    }

    @Test
    fun titleOnlyFileMergesWithNextHeading() {
        val content = EpubParser.parse(
            epub("a.xhtml" to "<h2>Ksiega pierwsza</h2>", "b.xhtml" to "<h3>Gospodarstwo</h3><p>Litwo!</p>"),
            noInflate
        )!!

        assertEquals(listOf(Chapter("Ksiega pierwsza · Gospodarstwo", listOf("Litwo!"))), content.chapters)
    }

    @Test
    fun fileWithoutHeadingContinuesChapterAndCaptionsAreDropped() {
        val content = EpubParser.parse(
            epub(
                "a.xhtml" to "<h2>CHAPTER I.</h2><p>Poczatek.</p>",
                "b.xhtml" to """<div class="figcenter"><div class="caption"><p>Mr. Bennet</p></div></div><p>Dalszy ciag.</p>
                    <h2><span class="caption">I hope Mr. Bingley will like it.</span><br/>CHAPTER II.</h2><p>Nowy.</p>"""
            ),
            noInflate
        )!!

        assertEquals(
            listOf(Chapter("CHAPTER I.", listOf("Poczatek.", "Dalszy ciag.")), Chapter("CHAPTER II.", listOf("Nowy."))),
            content.chapters
        )
    }

    @Test
    fun garbageIsNotAnEpub() {
        assertNull(EpubParser.parse("to nie jest zip".encodeToByteArray(), noInflate))
    }
}
