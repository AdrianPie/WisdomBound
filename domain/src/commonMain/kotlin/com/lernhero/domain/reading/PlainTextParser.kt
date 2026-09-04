package com.lernhero.domain.reading

/**
 * Zamienia czysty tekst na [BookContent].
 *
 * Zasady sa celowo proste i przewidywalne, bo TXT nie niesie zadnej struktury poza ukladem
 * pustych linii:
 * - pusta linia rozdziela akapity,
 * - pojedyncze zlamanie linii w srodku akapitu jest sklejane spacja (tak zapisana jest
 *   wiekszosc plikow z Gutenberga - twarde lamanie na ~70 znakow, ktore w reflow musi zniknac),
 * - ciagi bialych znakow zwijaja sie do JEDNEJ spacji,
 * - krotka linia bez konczacej interpunkcji, stojaca samotnie, to naglowek rozdzialu.
 */
object PlainTextParser {

    private const val MAX_HEADING_LENGTH = 70

    /**
     * Ciag spacji/tabow -> jedna spacja.
     *
     * Klasyczny maszynopis (i wiekszosc plikow z Gutenberga) stawia DWIE spacje po kropce.
     * Przy wyrownaniu do obu krawedzi silnik tekstu rozciaga kazda spacje z osobna, wiec ta
     * podwojna robi sie dwa razy szersza od reszty - stad wyrwa po kazdym zdaniu. Zwijamy je
     * juz przy parsowaniu, bo to wada ZRODLA, a nie sposobu rysowania.
     */
    private val SPACE_RUN = Regex("[ \t ]{2,}")

    fun parse(raw: String): BookContent {
        val normalized = raw.replace("\r\n", "\n").replace('\r', '\n')
        val chunks = normalized.split(Regex("\n[ \t]*\n+"))

        val chapters = mutableListOf<Chapter>()
        var title: String? = null
        var paragraphs = mutableListOf<String>()

        fun closeChapter() {
            if (title != null || paragraphs.isNotEmpty()) {
                chapters += Chapter(title, paragraphs.toList())
            }
            title = null
            paragraphs = mutableListOf()
        }

        chunks.forEach { chunk ->
            val joined = chunk.trim()
                .replace(Regex("[ \t]*\n[ \t]*"), " ")
                .replace(SPACE_RUN, " ")
                .trim()
            if (joined.isEmpty()) return@forEach

            if (isHeading(joined)) {
                closeChapter()
                title = joined
            } else {
                paragraphs += joined
            }
        }
        closeChapter()

        return BookContent(chapters)
    }

    private fun isHeading(text: String): Boolean {
        if (text.length > MAX_HEADING_LENGTH) return false
        if (text.contains(' ') && text.last() in charArrayOf('.', ',', ';', ':')) return false
        // Naglowek nie konczy sie kropka ani przecinkiem i jest krotki - reszta to akapit.
        return text.last().isLetterOrDigit() || text.last() in charArrayOf('!', '?')
    }
}
