package com.lernhero.domain.reading

/**
 * Zamienia czysty tekst na [BookContent].
 *
 * Zasady sa celowo proste i przewidywalne, bo TXT nie niesie zadnej struktury poza ukladem
 * pustych linii:
 * - pusta linia rozdziela akapity,
 * - pojedyncze zlamanie linii w srodku akapitu jest sklejane spacja (tak zapisana jest
 *   wiekszosc plikow z Gutenberga - twarde lamanie na ~70 znakow, ktore w reflow musi zniknac),
 * - krotka linia bez konczacej interpunkcji, stojaca samotnie, to naglowek rozdzialu.
 */
object PlainTextParser {

    private const val MAX_HEADING_LENGTH = 70

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
            val joined = chunk.trim().replace(Regex("[ \t]*\n[ \t]*"), " ").trim()
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
