package com.lernhero.domain.energy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EnergyRulesTest {

    private val start = 1_000_000L

    @Test
    fun oldSaveStartsFull() {
        // Zapis sprzed energii nie ma znacznika czasu - gracz nie moze przez to obudzic sie z zerem.
        val migrated = EnergyRules.regenerated(EnergyState(amount = EnergyRules.MAX, updatedAt = 0L), start)
        assertEquals(EnergyRules.MAX, migrated.amount)
        assertEquals(start, migrated.updatedAt)
    }

    @Test
    fun regenAddsOnePointPerInterval() {
        val empty = EnergyState(amount = 0, updatedAt = start)
        val after = EnergyRules.regenerated(empty, start + EnergyRules.REFILL_MILLIS * 3)
        assertEquals(3, after.amount)
    }

    @Test
    fun partialProgressIsNotLost() {
        // Zagladanie do gry co pol interwalu nie moze kasowac naliczonego czasu - inaczej gracz,
        // ktory czesto wraca, nie dostaje energii NIGDY.
        val empty = EnergyState(amount = 0, updatedAt = start)
        val half = EnergyRules.regenerated(empty, start + EnergyRules.REFILL_MILLIS / 2)
        assertEquals(0, half.amount)
        val full = EnergyRules.regenerated(half, start + EnergyRules.REFILL_MILLIS)
        assertEquals(1, full.amount)
    }

    @Test
    fun regenStopsAtMax() {
        val nearFull = EnergyState(amount = EnergyRules.MAX - 1, updatedAt = start)
        val after = EnergyRules.regenerated(nearFull, start + EnergyRules.REFILL_MILLIS * 50)
        assertEquals(EnergyRules.MAX, after.amount)
    }

    @Test
    fun spendingFromFullStartsTheClock() {
        val full = EnergyState(amount = EnergyRules.MAX, updatedAt = start)
        val paid = EnergyRules.spend(full, start + 5000L)!!
        assertEquals(EnergyRules.MAX - EnergyRules.BATTLE_COST, paid.amount)
        assertEquals(start + 5000L, paid.updatedAt)
        // Pierwszy punkt po wydaniu ma przyjsc po PELNYM interwale, a nie natychmiast.
        assertEquals(paid.amount, EnergyRules.regenerated(paid, start + 5000L + EnergyRules.REFILL_MILLIS - 1).amount)
    }

    @Test
    fun cannotSpendWhatIsNotThere() {
        val low = EnergyState(amount = EnergyRules.BATTLE_COST - 1, updatedAt = start)
        assertNull(EnergyRules.spend(low, start))
    }

    @Test
    fun fullBarIsAboutFiveHoursAndEightBattles() {
        // Balans: pelny pasek to jedna dluzsza sesja, a odbudowa - jedna noc.
        assertEquals(8, EnergyRules.MAX / EnergyRules.BATTLE_COST)
        val hoursToFull = EnergyRules.MAX * EnergyRules.REFILL_MILLIS / 3_600_000.0
        assertTrue(hoursToFull in 4.0..7.0, "pelny pasek odbudowuje sie $hoursToFull h")
    }

    @Test
    fun countdownRunsDownToNextPoint() {
        val empty = EnergyState(amount = 0, updatedAt = start)
        assertEquals(EnergyRules.REFILL_MILLIS, EnergyRules.millisToNext(empty, start))
        assertEquals(EnergyRules.REFILL_MILLIS / 2, EnergyRules.millisToNext(empty, start + EnergyRules.REFILL_MILLIS / 2))
        val full = EnergyState(amount = EnergyRules.MAX, updatedAt = start)
        assertEquals(0L, EnergyRules.millisToNext(full, start))
    }
}
