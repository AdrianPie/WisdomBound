package com.lernhero.domain.story

import com.lernhero.domain.common.Rarity
import com.lernhero.domain.run.RunDifficulty
import kotlin.test.Test
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNull
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

    /* -------------------------------- roll() ---------------------------------
     * Te testy istnieja, bo przez dluzszy czas byla TYLKO polowa tego systemu: dialog LOOT
     * pokazywal pule i procenty, a po wygranej nie wypadalo nigdy nic - roll() nie istnial.
     * Pilnuja wiec nie tyle matematyki, co samego faktu, ze druga polowa jest podpieta.
     */

    @Test
    fun `losowanie oddaje przedmiot z tej samej puli, ktora widzi gracz`() {
        val stage = 30
        val tier = RunDifficulty.Nightmare
        val pool = StageLoot.itemPool(stage, tier).map { it.id }.toSet()

        // Boss + wymuszone trafienie: 200 prob z roznych ziaren, kazde trafienie MUSI byc z puli.
        var drops = 0
        repeat(200) { seed ->
            val item = StageLoot.roll(stage, tier, isBoss = true, random = Random(seed.toLong()))
            if (item != null) {
                drops += 1
                assertTrue(item.id in pool, "${item.id} spoza puli podgladu LOOT")
            }
        }
        assertTrue(drops > 0, "przy 200 probach na bossie nie wypadlo NIC - roll() nie dziala")
    }

    @Test
    fun `czestotliwosc dropow trzyma sie deklarowanej szansy`() {
        val runs = 4000
        val hits = (0 until runs).count {
            StageLoot.roll(30, RunDifficulty.Normal, isBoss = false, random = Random(it.toLong())) != null
        }
        val rate = hits.toFloat() / runs
        val expected = StageLoot.dropChance(isBoss = false)
        // Szeroki margines - to test "podpiete i mniej wiecej tyle", nie test generatora losowego.
        assertTrue(rate > expected / 2 && rate < expected * 2, "rate=$rate, oczekiwane ~$expected")
    }

    @Test
    fun `zerowa szansa nie daje nic`() {
        // Random zwracajacy zawsze 1.0 z nextFloat nigdy nie przejdzie progu dropChance.
        val never = object : Random() {
            override fun nextBits(bitCount: Int): Int = Int.MAX_VALUE
        }
        assertNull(StageLoot.roll(30, RunDifficulty.Normal, isBoss = true, random = never))
    }
}
