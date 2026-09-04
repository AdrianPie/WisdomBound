package com.lernhero.domain.reading

/**
 * Sprowadza surowy tekst wyciagniety z PDF-a do postaci, ktora [PlainTextParser] rozumie.
 *
 * Bez tego kroku PDF w czytniku wyglada jak jedna wielka scianka tekstu. Powod: ekstraktory
 * PDF (PDFBox, PDFKit) oddaja tekst **linia po linii tak, jak byl narysowany na stronie** -
 * kazda linia wizualna konczy sie znakiem nowej linii, a akapitow nie rozdziela NIC. Dla
 * [PlainTextParser], ktory szuka pustych linii, cala strona jest wtedy jednym akapitem.
 *
 * PDF nie niesie informacji "tu zaczyna sie akapit", wiec trzeba ja odgadnac. Uzywamy trzech
 * sygnalow, w kolejnosci pewnosci:
 *  1. pusta linia - jawny odstep miedzy akapitami,
 *  2. wciecie pierwszej linii - klasyczny sklad ksiazkowy,
 *  3. **krotka linia poprzednia** - w tekscie lamanym do stalej szerokosci tylko OSTATNIA linia
 *     akapitu jest wyraznie krotsza od pozostalych. To najsilniejszy sygnal i dziala nawet, gdy
 *     PDF nie ma ani pustych linii, ani wciec.
 */
object PdfTextNormalizer {

    /** Ponizej tylu znakow linia nie nadaje sie do oszacowania szerokosci kolumny tekstu. */
    private const val MIN_MEANINGFUL_LINE = 20

    /** Linia krotsza niz tyle procent szerokosci kolumny konczy akapit. */
    private const val SHORT_LINE_RATIO = 0.80

    /** Ile procent stron musi miec te sama linie na brzegu, zeby uznac ja za zywa pagine. */
    private const val RUNNING_HEAD_SHARE = 0.5

    /**
     * [pages] to tekst kolejnych stron. Strony sklejamy w JEDEN strumien linii, a nie w osobne
     * akapity - zdanie przechodzace przez lamanie strony ma zostac jednym zdaniem, nie zostac
     * przeciete na pol tylko dlatego, ze skonczyla sie kartka.
     */
    fun normalize(pages: List<String>): String {
        val pageLines = pages.map { page ->
            page.replace("\r\n", "\n").replace('\r', '\n').split('\n')
        }

        val runningHeads = detectRunningHeads(pageLines)
        val lines = pageLines.flatMap { page -> cleanPage(page, runningHeads) }
        val bodyWidth = estimateBodyWidth(lines)

        return buildParagraphs(lines, bodyWidth).joinToString("\n\n")
    }

    /**
     * Zywe paginy i stopki - ten sam tytul rozdzialu powtorzony na kazdej stronie. Wykrywamy je
     * po tym, ze IDENTYCZNA tresc stoi na brzegu wielu stron; pojedyncze wystapienie to zwykly
     * tekst i nie ruszamy go.
     */
    private fun detectRunningHeads(pages: List<List<String>>): Set<String> {
        if (pages.size < 3) return emptySet()

        val counts = mutableMapOf<String, Int>()
        pages.forEach { page ->
            val meaningful = page.map { it.trim() }.filter { it.isNotEmpty() }
            if (meaningful.isEmpty()) return@forEach
            // Tylko pierwsza i ostatnia linia strony - w srodku strony powtorzenie to przypadek.
            setOf(meaningful.first(), meaningful.last())
                .filter { it.length in 1..80 }
                .forEach { counts[it] = (counts[it] ?: 0) + 1 }
        }

        val threshold = (pages.size * RUNNING_HEAD_SHARE).toInt().coerceAtLeast(2)
        return counts.filterValues { it >= threshold }.keys
    }

    private fun cleanPage(page: List<String>, runningHeads: Set<String>): List<String> {
        val kept = page.filterNot { line ->
            val trimmed = line.trim()
            trimmed in runningHeads || isPageNumber(trimmed)
        }
        // Puste linie na styku stron nie niosa informacji o akapicie - to tylko marginesy kartki.
        return kept.dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }
    }

    /** Sama liczba (ewentualnie w ozdobnikach typu "- 12 -") to numer strony, nie tresc. */
    private fun isPageNumber(line: String): Boolean {
        if (line.isEmpty()) return false
        val digits = line.filter { it.isDigit() }
        if (digits.isEmpty()) return false
        return line.all { it.isDigit() || it.isWhitespace() || it in "-–—[]()." }
    }

    /**
     * Szerokosc kolumny tekstu, liczona jako 90. percentyl dlugosci linii. Nie maksimum, bo
     * pojedyncza nietypowo dluga linia (tabela, adres URL) zawyzylaby prog dla calej ksiazki.
     */
    private fun estimateBodyWidth(lines: List<String>): Int {
        val lengths = lines.map { it.trim().length }.filter { it >= MIN_MEANINGFUL_LINE }.sorted()
        if (lengths.isEmpty()) return 0
        return lengths[(lengths.size * 0.9).toInt().coerceAtMost(lengths.lastIndex)]
    }

    private fun buildParagraphs(lines: List<String>, bodyWidth: Int): List<String> {
        val paragraphs = mutableListOf<String>()
        val current = StringBuilder()

        fun flush() {
            val text = current.toString().trim()
            if (text.isNotEmpty()) paragraphs += text
            current.clear()
        }

        var previousWasShort = false
        var previousWasBlank = false

        lines.forEach { raw ->
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) {
                previousWasBlank = true
                return@forEach
            }

            val indented = raw.startsWith("  ") || raw.startsWith("\t")
            if (previousWasBlank || previousWasShort || (indented && current.isNotEmpty())) {
                flush()
            }

            appendLine(current, trimmed)

            // Gdy nie znamy szerokosci kolumny (bardzo krotki tekst), nie zgadujemy - lepiej
            // zostawic akapit w calosci, niz posiekac go na przypadkowe kawalki.
            previousWasShort = bodyWidth > 0 && trimmed.length < bodyWidth * SHORT_LINE_RATIO
            previousWasBlank = false
        }
        flush()

        return paragraphs
    }

    /**
     * Doklejanie linii do akapitu. Slowo przeniesione lacznikiem na kolejna linie ("wrec-\nkage")
     * skleja sie z powrotem w jedno slowo - inaczej w reflow zostalby lacznik w srodku zdania,
     * w miejscu, ktore juz nie jest koncem linii.
     */
    private fun appendLine(builder: StringBuilder, line: String) {
        if (builder.isEmpty()) {
            builder.append(line)
            return
        }

        val endsWithHyphen = builder.length >= 2 &&
            builder.last() == '-' &&
            builder[builder.length - 2].isLetter() &&
            line.firstOrNull()?.isLowerCase() == true

        if (endsWithHyphen) {
            builder.deleteAt(builder.length - 1)
            builder.append(line)
        } else {
            builder.append(' ').append(line)
        }
    }
}
