package com.lernhero.domain.story

import com.lernhero.domain.common.Rarity
import com.lernhero.domain.run.RunDifficulty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StageLootTest {

    @Test
    fun `pierwsze plansze daja tylko zwykle przedmioty`() {
        assertEquals(Rarity.Common, StageLoot.maxRarity(1, RunDifficulty.Normal))
    }

    @Test
    fun `glebsza plansza odblokowuje lepsza pule`() {
        val early = StageLoot.maxRarity(5, RunDifficulty.Normal)
        val deep = StageLoot.maxRarity(60, RunDifficulty.Normal)
        assertTrue(deep.ordinal > early.ordinal)
    }

    @Test
    fun `wyzszy tier podbija pule na tej samej planszy`() {
        val normal = StageLoot.maxRarity(30, RunDifficulty.Normal)
        val death = StageLoot.maxRarity(30, RunDifficulty.Death)
        assertTrue(death.ordinal > normal.ordinal, "$normal vs $death")
    }

    @Test
    fun `pula nie zawiera nic ponad sufit rzadkosci`() {
        val cap = StageLoot.maxRarity(20, RunDifficulty.Normal)
        StageLoot.itemPool(20, RunDifficulty.Normal).forEach { item ->
            assertTrue(item.rarity.ordinal <= cap.ordinal, "${item.id} ponad sufit $cap")
        }
    }

    @Test
    fun `pula jest deterministyczna - podglad zgadza sie z tym co wypada`() {
        val first = StageLoot.itemPool(33, RunDifficulty.Nightmare).map { it.id }
        val second = StageLoot.itemPool(33, RunDifficulty.Nightmare).map { it.id }
        assertEquals(first, second)
    }

    @Test
    fun `boss ma wieksza szanse na przedmiot`() {
        assertTrue(StageLoot.dropChance(isBoss = true) > StageLoot.dropChance(isBoss = false))
    }
}
