package com.lernhero.domain.quiz.custom

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CustomDeckTest {

    private val random = Random(7)

    private fun choice(id: String, difficulty: QuizDifficulty = QuizDifficulty.Medium) = CustomQuestion(
        id = id,
        type = CustomQuestionType.Choice,
        prompt = "Co oznacza znak B-2?",
        difficulty = difficulty,
        correct = listOf("Zakaz wjazdu"),
        wrong = listOf("Zakaz zatrzymywania", "Nakaz jazdy w prawo", "Koniec zakazu")
    )

    @Test
    fun `pytanie z jedna poprawna i trzema blednymi jest gotowe`() {
        assertTrue(choice("q1").isValid, choice("q1").issues().toString())
    }

    @Test
    fun `pytanie z dwiema poprawnymi nie jest wyborem jednej odpowiedzi`() {
        val broken = choice("q1").copy(correct = listOf("Zakaz wjazdu", "Zakaz ruchu"))
        assertFalse(broken.isValid)
    }

    @Test
    fun `powtorzona odpowiedz to blad`() {
        val broken = choice("q1").copy(wrong = listOf("Zakaz wjazdu", "Inna", "Jeszcze inna"))
        assertTrue(broken.issues().any { it.contains("powtarzaja") }, broken.issues().toString())
    }

    @Test
    fun `puste odpowiedzi nie licza sie do minimum`() {
        val broken = choice("q1").copy(wrong = listOf("Zla", "", "  "))
        assertTrue(broken.issues().any { it.contains("Za malo") }, broken.issues().toString())
    }

    @Test
    fun `ukladanka potrzebuje trzech roznych elementow`() {
        val question = CustomQuestion(
            id = "o1",
            type = CustomQuestionType.Ordering,
            prompt = "Ulóz czynnosci ruszania",
            items = listOf("Sprzeglo", "Bieg")
        )
        assertFalse(question.isValid)
        assertTrue(question.copy(items = listOf("Sprzeglo", "Bieg", "Gaz")).isValid)
    }

    @Test
    fun `szacowanie pilnuje zakresu`() {
        val question = CustomQuestion(
            id = "e1",
            type = CustomQuestionType.Estimation,
            prompt = "Ile wynosi droga hamowania przy 100 km/h?",
            correctValue = 80,
            minValue = 100,
            maxValue = 200,
            unit = "m"
        )
        assertTrue(question.issues().any { it.contains("poza zakresem") }, question.issues().toString())
        assertTrue(question.copy(minValue = 10, maxValue = 200).isValid)
    }

    @Test
    fun `szacowanie bez tolerancji dostaje ja z epoki`() {
        val question = CustomQuestion(
            id = "e1",
            type = CustomQuestionType.Estimation,
            prompt = "Ktory rok?",
            correctValue = 1990,
            minValue = 1900,
            maxValue = 2020,
            unit = "rok"
        )
        val entry = assertNotNull(question.toEstimationEntry("d1"))
        assertTrue(entry.tolerance > 0)
    }

    @Test
    fun `konwersja nadaje kategorie Wlasne i id z talia w srodku`() {
        val entry = assertNotNull(choice("q1").toQuizEntry("deck7"))
        assertEquals(QuizCategory.Custom, entry.category)
        assertEquals("deck7" to "q1", CustomDeckBank.sourceOf(entry.id))
    }

    @Test
    fun `id spoza talii nie daje sie rozlozyc`() {
        assertNull(CustomDeckBank.sourceOf("word_abc"))
        assertNull(CustomDeckBank.sourceOf("bio_12"))
    }

    @Test
    fun `talia gra dopiero od czterech gotowych pytan`() {
        val deck = CustomDeck(id = "d1", name = "Prawo jazdy")
        assertFalse(deck.isPlayable)
        val filled = deck.copy(questions = (1..4).map { choice("q$it") })
        assertTrue(filled.isPlayable)
    }

    @Test
    fun `pytania z bledem nie licza sie do gotowosci talii`() {
        val deck = CustomDeck(
            id = "d1",
            questions = (1..3).map { choice("q$it") } + choice("q4").copy(correct = emptyList())
        )
        assertEquals(1, deck.brokenCount)
        assertFalse(deck.isPlayable)
    }

    @Test
    fun `pytania na oszacowanie nie wchodza do zwyklej tury`() {
        val estimation = CustomQuestion(
            id = "e1",
            type = CustomQuestionType.Estimation,
            prompt = "Ktory rok?",
            correctValue = 1990,
            minValue = 1900,
            maxValue = 2020
        )
        val deck = CustomDeck(id = "d1", questions = (1..4).map { choice("q$it") } + estimation)

        repeat(30) {
            val drawn = assertNotNull(CustomDeckBank.draw(deck, random = random))
            assertTrue(drawn.type != CustomQuestionType.Estimation)
        }
        assertNotNull(CustomDeckBank.drawEstimation(deck, random))
    }

    @Test
    fun `losowanie trzyma sie wybranego poziomu, gdy talia go ma`() {
        val deck = CustomDeck(
            id = "d1",
            questions = listOf(
                choice("q1", QuizDifficulty.Easy),
                choice("q2", QuizDifficulty.Easy),
                choice("q3", QuizDifficulty.Expert),
                choice("q4", QuizDifficulty.Expert)
            )
        )
        repeat(20) {
            assertEquals(
                QuizDifficulty.Expert,
                assertNotNull(CustomDeckBank.draw(deck, QuizDifficulty.Expert, random)).difficulty
            )
        }
    }

    @Test
    fun `brak pytan na danym poziomie schodzi na najblizszy zamiast blokowac ture`() {
        val deck = CustomDeck(id = "d1", questions = (1..4).map { choice("q$it", QuizDifficulty.Easy) })
        val drawn = assertNotNull(CustomDeckBank.draw(deck, QuizDifficulty.Expert, random))
        assertEquals(QuizDifficulty.Easy, drawn.difficulty)
    }

    @Test
    fun `pusta talia nie oddaje pytania`() {
        assertNull(CustomDeckBank.draw(CustomDeck(id = "d1"), QuizDifficulty.Easy, random))
        assertNull(CustomDeckBank.drawEstimation(CustomDeck(id = "d1"), random))
    }
}
