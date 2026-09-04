package com.lernhero.domain.reading

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PdfTextNormalizerTest {

    @Test
    fun `linie lamane w PDF sklejaja sie w akapity, a nie w jedna scianke`() {
        // Tak wyglada tekst z PDFBox: kazda linia wizualna osobno, ZERO pustych linii.
        // Krotka linia ("...the city.") konczy akapit - to jedyny sygnal, jaki mamy.
        val page = """
            The war at Troy had ended ten years earlier and the
            Greek leaders had sailed away after they had finally
            destroyed the city.
            Odysseus, king of the small island of Ithaca, was one
            of the missing men who had left his home twenty years
            before all of this.
        """.trimIndent()

        val content = PlainTextParser.parse(PdfTextNormalizer.normalize(listOf(page)))
        val paragraphs = content.blocks.map { it.text }

        assertEquals(2, paragraphs.size, "powinny powstac dwa akapity, powstalo: $paragraphs")
        assertTrue(paragraphs[0].startsWith("The war at Troy"))
        assertTrue(paragraphs[0].endsWith("destroyed the city."))
        assertTrue(paragraphs[1].startsWith("Odysseus, king"))
    }

    @Test
    fun `zdanie przechodzace przez lamanie strony zostaje jednym akapitem`() {
        val page1 = """
            The storm had passed before dawn and the road along
            the cliff was littered with branches that had fallen
            during the night, so Mira walked it slowly and she was
        """.trimIndent()
        val page2 = """
            counting every broken one of them along the whole way.
            She had kept the lantern burning for nine long years.
        """.trimIndent()

        val content = PlainTextParser.parse(PdfTextNormalizer.normalize(listOf(page1, page2)))
        val paragraphs = content.blocks.map { it.text }

        assertTrue(
            paragraphs.any { it.contains("she was counting every broken one") },
            "zdanie ucieto na granicy stron: $paragraphs"
        )
    }

    @Test
    fun `slowo przeniesione lacznikiem skleja sie z powrotem`() {
        val page = """
            Her father had taught her how to read the weather in wrec-
            kage, because thin twigs always meant nothing but wind.
        """.trimIndent()

        val text = PdfTextNormalizer.normalize(listOf(page))

        assertTrue(text.contains("wreckage"), "lacznik nie zostal sklejony: $text")
        assertTrue(!text.contains("wrec- kage"))
    }

    @Test
    fun `numery stron i zywe paginy znikaja`() {
        val pages = (1..6).map { number ->
            """
                The Lantern Keeper
                This is the body of page $number and it carries the real
                content that the reader actually came here to read now.
                $number
            """.trimIndent()
        }

        val text = PdfTextNormalizer.normalize(pages)

        assertTrue(!text.contains("The Lantern Keeper"), "zywa pagina zostala: $text")
        assertTrue(text.contains("the real content"), "tresc zginela: $text")
    }

    @Test
    fun `pusta linia nadal rozdziela akapity`() {
        val page = """
            First paragraph line one of the text that runs on for a while.

            Second paragraph starts here after a blank line separator.
        """.trimIndent()

        val content = PlainTextParser.parse(PdfTextNormalizer.normalize(listOf(page)))
        assertEquals(2, content.blocks.size)
    }

    @Test
    fun `pusty pdf nie wywraca normalizacji`() {
        assertEquals("", PdfTextNormalizer.normalize(emptyList()))
        assertEquals("", PdfTextNormalizer.normalize(listOf("", "   ")))
    }
}
