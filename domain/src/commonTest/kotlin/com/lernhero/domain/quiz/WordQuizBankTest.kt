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

    private fun sentence(text: String, translation: String, id: String = text) = SavedWord(
        id = id,
        text = text,
        translation = translation,
        sentence = text,
        bookId = "book",
        bookTitle = "Book",
        savedAt = 0L,
        isSentence = true
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
    fun `zapisane zdania nie wchodza do puli quizu`() {
        val words = listOf(
            word("cat", "kot"),
            word("dog", "pies"),
            word("sun", "słońce"),
            word("moon", "księżyc"),
            sentence("The cat sat on the mat.", "Kot usiadl na macie.")
        )
        val eligible = WordQuizBank.eligible(words)
        assertEquals(4, eligible.size)
        assertTrue(eligible.none { it.isSentence })

        val entry = WordQuizBank.draw(words, Random(1))
        assertTrue(entry != null)
        assertTrue("Kot usiadl na macie." !in entry.answers, "zdanie nie moze byc odpowiedzia")
    }

    @Test
    fun `wielowyrazowy tekst bez flagi tez jest odsiewany`() {
        // Stare fiszki sprzed dodania isSentence maja te flage domyslnie false - spacja w
        // tekscie jest druga siatka bezpieczenstwa, zeby i tak nie trafily do quizu.
        val words = listOf(
            word("cat", "kot"),
            word("dog", "pies"),
            word("sun", "słońce"),
            word("moon", "księżyc"),
            SavedWord(
                id = "legacy",
                text = "read weather in wreckage",
                translation = "czytac pogode we wraku",
                sentence = "Her father had taught her to read weather in wreckage.",
                bookId = "book",
                bookTitle = "Book",
                savedAt = 0L
            )
        )
        assertEquals(4, WordQuizBank.eligible(words).size)
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
