package com.lernhero.domain.town

import com.lernhero.domain.quiz.QuizBank
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductionQuizTest {

    @Test
    fun `drabinka ma tyle pytan, ile podejscie`() {
        assertEquals(ProductionQuiz.QUESTION_COUNT, ProductionQuiz.LADDER.size)
    }

    @Test
    fun `bank potrafi wylosowac pelne podejscie bez powtorek`() {
        repeat(20) { seed ->
            val drawn = QuizBank.draw(ProductionQuiz.LADDER, category = null, random = Random(seed))
            assertEquals(ProductionQuiz.QUESTION_COUNT, drawn.size, "seed $seed")
            assertEquals(drawn.size, drawn.map { it.id }.toSet().size, "powtorzone pytanie, seed $seed")
            drawn.forEach { assertTrue(it.wrong.size >= 3, "za malo blednych odpowiedzi: ${it.id}") }
        }
    }

    @Test
    fun `bonus rosnie z liczba trafien i zeruje sie przy zerze`() {
        assertEquals(0, ProductionQuiz.boostPercentFor(0))
        val values = (0..ProductionQuiz.QUESTION_COUNT).map { ProductionQuiz.boostPercentFor(it) }
        assertEquals(values.sorted(), values, "skala musi byc niemalejaca")
        assertTrue(ProductionQuiz.boostPercentFor(4) > 25, "4/5 ma przebic dawny darmowy +25%")
    }

    @Test
    fun `wynik spoza zakresu nie wywala skali`() {
        assertEquals(0, ProductionQuiz.boostPercentFor(-3))
        assertEquals(ProductionQuiz.MAX_BOOST_PERCENT, ProductionQuiz.boostPercentFor(99))
    }
}
