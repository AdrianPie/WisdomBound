package com.lernhero.domain.quiz

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Dodatek czasu za dlugie pytania i pytania do przemyslenia - patrz [QuizPace]. */
class QuizPaceTest {

    private val all = QuizBank.all()

    @Test
    fun `typowe krotkie pytanie nie dostaje dodatku`() {
        val extra = QuizPace.extraMs("Who wrote 'Hamlet'?", listOf("Shakespeare", "Marlowe", "Jonson", "Milton"), thinking = false)
        assertEquals(0L, extra)
    }

    @Test
    fun `dlugie pytanie dostaje czas na czytanie, ale z sufitem`() {
        val long = "x".repeat(QuizPace.BASE_CHARS + 40)
        assertEquals(40 * QuizPace.MS_PER_CHAR, QuizPace.extraMs(long, emptyList(), thinking = false))
        val huge = "x".repeat(2000)
        assertEquals(QuizPace.MAX_READING_MS, QuizPace.extraMs(huge, emptyList(), thinking = false))
    }

    @Test
    fun `pytanie do przemyslenia dostaje staly dodatek`() {
        val plain = QuizPace.extraMs("Short?", listOf("A", "B", "C", "D"), thinking = false)
        val thinking = QuizPace.extraMs("Short?", listOf("A", "B", "C", "D"), thinking = true)
        assertEquals(QuizPace.THINKING_MS, thinking - plain)
    }

    @Test
    fun `porownania i matematyka to pytania do przemyslenia, reszta nie`() {
        val comparisons = all.filter { it.section?.startsWith("geography/comparisons/") == true }
        assertTrue(comparisons.size >= 500, "dzial porownan ma tylko ${comparisons.size} pytan")
        assertTrue(comparisons.all(QuizPace::isThinking))
        assertTrue(all.filter { it.category == QuizCategory.Math }.all(QuizPace::isThinking))

        val history = all.filter { it.category == QuizCategory.History }
        assertFalse(history.any(QuizPace::isThinking), "historia to wiedza, nie rachunek")

        // Dodatek ma byc wyjatkiem, nie norma - inaczej to po prostu dluzsze okno dla wszystkich.
        val share = all.count(QuizPace::isThinking).toDouble() / all.size
        assertTrue(share < 0.2, "za duzo pytan do przemyslenia: $share")
    }

    @Test
    fun `wiekszosc banku miesci sie w bazowym czasie czytania`() {
        val withReading = all.count { QuizPace.extraMs(it.prompt, it.answers, thinking = false) > 0 }
        val share = withReading.toDouble() / all.size
        assertTrue(share < 0.5, "prog czytania jest za nisko: $share pytan dostaje dodatek")
    }
}
