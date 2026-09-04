package com.lernhero.domain.reading

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlainTextParserSpacingTest {

    @Test
    fun podwojna_spacja_po_kropce_zwija_sie_do_jednej() {
        // Maszynopisowy zapis z Gutenberga: dwie spacje po kropce. Przy justowaniu silnik tekstu
        // rozciagal je osobno i po kazdym zdaniu robila sie wyrwa.
        val content = PlainTextParser.parse("Zdanie pierwsze.  Zdanie drugie.  I trzecie.")
        val text = content.chapters.first().paragraphs.first()

        assertEquals("Zdanie pierwsze. Zdanie drugie. I trzecie.", text)
        assertTrue(!text.contains("  "), "W tekscie nie moze zostac zadna podwojna spacja")
    }

    @Test
    fun sklejanie_linii_nie_zostawia_podwojnych_spacji() {
        val content = PlainTextParser.parse("Linia konczaca zdanie.   \n   Nastepna linia.")
        assertEquals("Linia konczaca zdanie. Nastepna linia.", content.chapters.first().paragraphs.first())
    }

    @Test
    fun taby_i_dlugie_ciagi_spacji_tez_sie_zwijaja() {
        val content = PlainTextParser.parse("Slowo\t\tdrugie     trzecie.")
        assertEquals("Slowo drugie trzecie.", content.chapters.first().paragraphs.first())
    }
}
