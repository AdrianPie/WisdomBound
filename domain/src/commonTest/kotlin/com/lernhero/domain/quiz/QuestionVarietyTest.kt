package com.lernhero.domain.quiz

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuestionVarietyTest {

    @Test
    fun `historia zapomina najstarsze po przekroczeniu pojemnosci`() {
        val history = QuestionHistory(capacity = 2)
        history.remember("a")
        history.remember("b")
        history.remember("c")
        assertFalse("a" in history)
        assertTrue("b" in history && "c" in history)
    }

    @Test
    fun `ponowne zapamietanie odswieza pozycje`() {
        val history = QuestionHistory(capacity = 2)
        history.remember("a")
        history.remember("b")
        history.remember("a")
        history.remember("c")
        assertTrue("a" in history)
        assertFalse("b" in history)
    }

    @Test
    fun `dwadziescia fal jednego tematu bez powtorki pytania z wyborem`() {
        val history = QuestionHistory()
        val random = Random(7)
        repeat(80) {
            val entry = QuizBank.draw(listOf(QuizDifficulty.Hard), QuizCategory.Mythology, random, history::contains).single()
            assertFalse(entry.id in history, "Powtorzone pytanie ${entry.id} w ${it + 1}. turze")
            history.remember(entry.id)
        }
    }

    @Test
    fun `specjalne typy nigdy nie wracaja, tylko sie koncza`() {
        val history = QuestionHistory()
        val random = Random(3)
        var drawn = 0
        while (true) {
            val entry = TrueFalseBank.draw(QuizDifficulty.Medium, QuizCategory.Animals, random, history::contains) ?: break
            assertFalse(entry.id in history)
            history.remember(entry.id)
            drawn++
        }
        assertTrue(drawn > 0)
        assertNull(TrueFalseBank.draw(QuizDifficulty.Medium, QuizCategory.Animals, random, history::contains))
    }

    @Test
    fun `prawda falsz ma unikalne id i tresci oraz pokrywa kazdy temat`() {
        val all = TrueFalseBank.all
        assertEquals(all.size, all.map { it.id }.toSet().size)
        assertEquals(all.size, all.map { it.statement.lowercase() }.toSet().size)
        // Prawda/falsz to co piata tura, a bierze WYLACZNIE pytania jeszcze nie widziane (patrz
        // drawFresh) - przy dziesieciu na temat wyczerpywaly sie w polowie jednego biegu.
        QuizCategory.builtin.filter { it != QuizCategory.Flags }.forEach { category ->
            assertTrue(all.count { it.category == category } >= 20, "Za malo prawda/falsz w ${category.label}")
        }
    }

    @Test
    fun `prawda falsz nie jest jednostronny`() {
        val trueShare = TrueFalseBank.all.count { it.isTrue }.toFloat() / TrueFalseBank.all.size
        assertTrue(trueShare in 0.4f..0.7f, "Udzial prawdziwych: $trueShare")
    }
}
