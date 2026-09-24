package com.lernhero.home.fight

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.custom.CustomDeck
import com.lernhero.domain.quiz.custom.CustomQuestion
import com.lernhero.domain.quiz.custom.CustomQuestionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * [RunMaterial.key] laduje w zapisanym stanie ekranu, wiec kazdy wybor musi przezyc zapis i odczyt.
 * Bez tego gracz po powrocie z walki gralby z czegos innego, niz wybral - i nie mialby jak sie
 * domyslic, dlaczego.
 */
class RunMaterialTest {

    @Test
    fun `every choice survives a round trip`() {
        val choices = listOf(
            RunMaterial.Mixed,
            RunMaterial.Words,
            RunMaterial.Topic(QuizCategory.Biology),
            RunMaterial.Deck("deck-17")
        )
        choices.forEach { choice ->
            assertEquals(choice, RunMaterial.parse(choice.key), "round trip of $choice")
        }
    }

    @Test
    fun `unknown state falls back to mixed instead of crashing`() {
        // Zapis moze pochodzic ze starszej wersji albo z kategorii, ktorej juz nie ma w banku.
        listOf(null, "", "topic:Atlantis", "deck:", "nonsense").forEach { key ->
            assertEquals(RunMaterial.Mixed, RunMaterial.parse(key), "fallback for $key")
        }
    }

    @Test
    fun `a deck that lost its questions stops being playable material`() {
        val thin = deck("d1", ready = CustomDeck.MIN_QUESTIONS - 1)
        val full = deck("d2", ready = CustomDeck.MIN_QUESTIONS)

        // Talia bez kompletu pytan nie moze wystartowac wyprawy - quiz zaczalby powtarzac to samo
        // pytanie w kolko.
        assertNull(RunMaterial.Deck("d1").deckIn(listOf(thin, full)))
        // Ani talia, ktorej gracz w miedzyczasie w ogole skasowal.
        assertNull(RunMaterial.Deck("gone").deckIn(listOf(full)))
        assertEquals(full, RunMaterial.Deck("d2").deckIn(listOf(thin, full)))
    }

    @Test
    fun `label never promises a deck the run will not use`() {
        val decks = listOf(deck("d1", ready = 1))
        // Pigulka na scenie ma mowic to samo, co wystartuje po nacisnieciu START.
        assertEquals("All topics", RunMaterial.Deck("d1").label(decks))
        assertEquals("Biology", RunMaterial.Topic(QuizCategory.Biology).label(decks))
    }

    private fun deck(id: String, ready: Int) = CustomDeck(
        id = id,
        name = id,
        questions = List(ready) {
            CustomQuestion(
                id = "$id-$it",
                type = CustomQuestionType.Choice,
                prompt = "Question $it?",
                correct = listOf("a"),
                wrong = listOf("b", "c", "d")
            )
        }
    )
}
