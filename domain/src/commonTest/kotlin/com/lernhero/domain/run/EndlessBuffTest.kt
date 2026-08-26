package com.lernhero.domain.run

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EndlessBuffTest {

    @Test
    fun `oferta ma trzy rozne karty i pomija wymaksowane`() {
        val owned = EndlessBuff.entries.associateWith { it.maxStacks } - EndlessBuff.Might - EndlessBuff.Cleave - EndlessBuff.Avatar
        val offer = EndlessBuff.offer(owned, quality = 0.5f, random = Random(7))

        assertEquals(3, offer.size)
        assertEquals(offer.toSet().size, offer.size)
        assertEquals(setOf(EndlessBuff.Might, EndlessBuff.Cleave, EndlessBuff.Avatar), offer.toSet())
    }

    @Test
    fun `lepszy quiz czesciej daje mocniejsze karty`() {
        fun rareOrBetterShare(quality: Float): Float {
            val random = Random(1234)
            val draws = (1..400).flatMap { EndlessBuff.offer(emptyMap(), quality, random) }
            return draws.count { it.rarity != UpgradeRarity.Common }.toFloat() / draws.size
        }

        val weak = rareOrBetterShare(0f)
        val strong = rareOrBetterShare(1f)
        assertTrue(strong > weak + 0.2f, "slaby quiz: $weak, mocny quiz: $strong")
    }

    @Test
    fun `celnosc ponizej progu zgadywania to zerowa jakosc`() {
        assertEquals(0f, EndlessBuff.qualityFor(correctAnswers = 3, answeredQuestions = 12))
        assertEquals(1f, EndlessBuff.qualityFor(correctAnswers = 10, answeredQuestions = 10))
        assertEquals(0f, EndlessBuff.qualityFor(correctAnswers = 0, answeredQuestions = 0))
    }
}
