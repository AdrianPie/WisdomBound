package com.lernhero.domain.run

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BossChestTest {

    @Test
    fun `jackpoty zostaja rzadkie nawet na najglebszym poziomie`() {
        val legendary = ChestWheel.chanceOf(ChestSlot.ItemLegendary, dungeonLevel = 10)
        val heroic = ChestWheel.chanceOf(ChestSlot.HeroicTicket, dungeonLevel = 10)
        assertTrue(legendary < 0.05f, "legendarny: $legendary")
        assertTrue(heroic < 0.08f, "heroiczny los: $heroic")
    }

    @Test
    fun `glebszy loch podbija szanse na najlepsze pola`() {
        val shallow = ChestWheel.chanceOf(ChestSlot.ItemLegendary, dungeonLevel = 1)
        val deep = ChestWheel.chanceOf(ChestSlot.ItemLegendary, dungeonLevel = 10)
        assertTrue(deep > shallow, "1. poziom: $shallow, 10. poziom: $deep")
    }

    @Test
    fun `szanse wszystkich pol sumuja sie do stu procent`() {
        val total = ChestSlot.entries.sumOf { ChestWheel.chanceOf(it, dungeonLevel = 4).toDouble() }
        assertTrue(kotlin.math.abs(total - 1.0) < 1e-4, "suma: $total")
    }

    @Test
    fun `losowanie trzyma sie wag`() {
        val random = Random(2024)
        val draws = (1..20_000).map { ChestWheel.roll(random, dungeonLevel = 1) }
        val goldShare = draws.count { it == ChestSlot.GoldSmall } / draws.size.toFloat()
        val expected = ChestWheel.chanceOf(ChestSlot.GoldSmall, dungeonLevel = 1)
        assertTrue(kotlin.math.abs(goldShare - expected) < 0.02f, "wypadlo $goldShare, oczekiwane $expected")
        assertEquals(ChestSlot.entries.size, draws.toSet().size, "nie kazde pole kiedykolwiek wypadlo")
    }
}
