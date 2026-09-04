package com.lernhero.domain.reading

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VocabularyDeckTest {

    private fun word(id: String = "w1") = SavedWord(
        id = id,
        text = "lantern",
        translation = "latarnia",
        sentence = "He raised the lantern.",
        bookId = "b1",
        bookTitle = "Ksiazka",
        savedAt = 1000L
    )

    /** Kilka poprawnych odpowiedzi z rzedu - dokladnie tyle, ile trzeba do awansu. */
    private fun answerCorrectly(word: SavedWord, times: Int): SavedWord =
        (1..times).fold(word) { current, index ->
            VocabularyDeck.recordAnswer(current, correct = true, now = 5000L + index)
        }

    @Test
    fun `nowa fiszka lezy w archiwum do nauki`() {
        val fresh = word()
        assertEquals(VocabularyBox.Learning, fresh.box)
        assertNull(fresh.masteredAt)
        assertEquals(VocabularyDeck.MASTERY_STREAK, VocabularyDeck.stepsToMastery(fresh))
    }

    @Test
    fun `poprawna odpowiedz wydluza serie i liczniki`() {
        val after = VocabularyDeck.recordAnswer(word(), correct = true, now = 5000L)

        assertEquals(1, after.correctStreak)
        assertEquals(1, after.correctTotal)
        assertEquals(0, after.wrongTotal)
        assertNull(after.masteredAt, "jedno trafienie to jeszcze nie opanowanie")
    }

    @Test
    fun `piec trafien z rzedu przenosi slowo do opanowanych`() {
        val almost = answerCorrectly(word(), VocabularyDeck.MASTERY_STREAK - 1)
        assertNull(almost.masteredAt, "o jedno trafienie za wczesnie")
        assertEquals(1, VocabularyDeck.stepsToMastery(almost))

        val mastered = VocabularyDeck.recordAnswer(almost, correct = true, now = 9000L)
        assertNotNull(mastered.masteredAt)
        assertEquals(VocabularyBox.Mastered, mastered.box)
        assertEquals(0, VocabularyDeck.stepsToMastery(mastered))
    }

    @Test
    fun `bledna odpowiedz zeruje serie ale nie kasuje historii trafien`() {
        val threeInRow = answerCorrectly(word(), 3)
        val missed = VocabularyDeck.recordAnswer(threeInRow, correct = false, now = 9000L)

        assertEquals(0, missed.correctStreak, "seria musi zaczac sie od nowa")
        assertEquals(3, missed.correctTotal, "sumaryczne trafienia zostaja")
        assertEquals(1, missed.wrongTotal)
    }

    @Test
    fun `pomylka cofa slowo z archiwum z powrotem do nauki`() {
        val mastered = answerCorrectly(word(), VocabularyDeck.MASTERY_STREAK)
        assertEquals(VocabularyBox.Mastered, mastered.box)

        val forgotten = VocabularyDeck.recordAnswer(mastered, correct = false, now = 9999L)
        assertEquals(VocabularyBox.Learning, forgotten.box)
        assertNull(forgotten.masteredAt)
    }

    @Test
    fun `raz zdobyte archiwum nie przestawia sie na kolejnym trafieniu`() {
        val mastered = answerCorrectly(word(), VocabularyDeck.MASTERY_STREAK)
        val stamp = mastered.masteredAt

        val later = VocabularyDeck.recordAnswer(mastered, correct = true, now = 123_456L)
        assertEquals(stamp, later.masteredAt, "moment awansu ma zostac ten pierwszy")
    }

    @Test
    fun `podzial na archiwa zgadza sie z licznikami`() {
        val learning = word("a")
        val mastered = answerCorrectly(word("b"), VocabularyDeck.MASTERY_STREAK)
        val deck = listOf(learning, mastered)

        assertEquals(listOf(learning), VocabularyDeck.learning(deck))
        assertEquals(listOf(mastered), VocabularyDeck.mastered(deck))
        assertEquals(listOf(learning), VocabularyDeck.inBox(deck, VocabularyBox.Learning))
        assertEquals(listOf(mastered), VocabularyDeck.inBox(deck, VocabularyBox.Mastered))
    }

    @Test
    fun `skutecznosc liczy sie z obu licznikow`() {
        val threeGoodOneBad = VocabularyDeck.recordAnswer(answerCorrectly(word(), 3), correct = false, now = 1L)
        assertEquals(4, threeGoodOneBad.answersTotal)
        assertTrue(threeGoodOneBad.accuracy in 0.74f..0.76f)
        assertEquals(0f, word().accuracy, "bez odpowiedzi nie dzielimy przez zero")
    }
}
