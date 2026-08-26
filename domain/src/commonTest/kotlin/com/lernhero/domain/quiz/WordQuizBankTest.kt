package com.lernhero.domain.quiz

import com.lernhero.domain.reading.SavedWord
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WordQuizBankTest {

    private fun word(text: String, translation: String, id: String = text) = SavedWord(
        id = id,
        text = text,
        translation = translation,
        sentence = "The $text was there.",
        bookId = "book",
        bookTitle = "Book",
        savedAt = 0L
    )

    @Test
    fun `za malo fiszek nie daje pytania`() {
        val words = listOf(word("cat", "kot"), word("dog", "pies"), word("sun", "słońce"))
        assertNull(WordQuizBank.draw(words, Random(1)))
        assertTrue(!WordQuizBank.hasEnoughWords(words))
    }

    @Test
    fun `cztery rozne slowa daja pytanie z czterema odpowiedziami`() {
        val words = listOf(
            word("cat", "kot"),
            word("dog", "pies"),
            word("sun", "słońce"),
            word("moon", "księżyc")
        )
        assertTrue(WordQuizBank.hasEnoughWords(words))

        val entry = WordQuizBank.draw(words, Random(1))
        assertTrue(entry != null)
        assertEquals(4, entry.answers.size)
        assertEquals(4, entry.answers.distinct().size, "odpowiedzi nie moga sie powtarzac")
        assertTrue(entry.correct in words.map { it.translation })
        assertEquals(QuizCategory.Vocabulary, entry.category)
    }

    @Test
    fun `fiszki bez tlumaczenia sa pomijane`() {
        val words = listOf(
            word("cat", "kot"),
            word("dog", "pies"),
            word("sun", "słońce"),
            word("moon", "") // nie przetlumaczone jeszcze
        )
        assertTrue(!WordQuizBank.hasEnoughWords(words))
        assertNull(WordQuizBank.draw(words, Random(1)))
    }

    @Test
    fun `duplikaty tego samego slowa licza sie raz`() {
        val words = listOf(
            word("cat", "kot", id = "1"),
            word("cat", "kot", id = "2"),
            word("dog", "pies"),
            word("sun", "słońce"),
            word("moon", "księżyc")
        )
        assertEquals(4, WordQuizBank.eligible(words).size)
    }
}
