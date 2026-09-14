package com.lernhero.domain.story

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdleRewardsTest {

    private val hour = 3_600_000L

    @Test
    fun `bez zdobytej planszy nic nie kapie`() {
        val reward = IdleRewards.pendingFor(highestClearedStage = 0, lastCollectedAt = 1L, now = 1L + 5 * hour)
        assertTrue(reward.isEmpty)
    }

    @Test
    fun `pierwszy raz nie nalicza od epoki Uniksa`() {
        val reward = IdleRewards.pendingFor(highestClearedStage = 10, lastCollectedAt = 0L, now = 9_999_999_999L)
        assertTrue(reward.isEmpty, "znacznik 0 = nigdy nie odbierano, wiec nie ma czego naliczac")
    }

    @Test
    fun `dalszy postep daje wieksza stawke na godzine`() {
        val early = IdleRewards.ratePerHour(5)
        val late = IdleRewards.ratePerHour(60)
        assertTrue(late.gold > early.gold)
        assertTrue(late.essence > early.essence)
    }

    @Test
    fun `magazyn ma sufit`() {
        val atCap = IdleRewards.pendingFor(20, 1L, 1L + (IdleRewards.CAP_HOURS.toLong() * hour))
        val wayOver = IdleRewards.pendingFor(20, 1L, 1L + 100 * hour)
        assertEquals(atCap.gold, wayOver.gold, "po suficie nic wiecej sie nie zbiera")
    }

    @Test
    fun `pasek zapelnienia dochodzi do jedynki dokladnie na suficie`() {
        assertEquals(0f, IdleRewards.storageFraction(0L, 5 * hour))
        assertEquals(0.5f, IdleRewards.storageFraction(1L, 1L + (IdleRewards.CAP_HOURS / 2).toLong() * hour), 0.01f)
        assertEquals(1f, IdleRewards.storageFraction(1L, 1L + 50 * hour))
    }

    @Test
    fun `kapaczka placi materialami, a nie awansem`() {
        // Regula calej gry brzmi "XP plynie z NAUKI" (patrz RunLoot) - a kapaczka idle dawala do
        // 1440 XP za dwanascie godzin nieobecnosci, czyli poltora poziomu ZA NIC. Gorzej na
        // starcie: po pierwszej krainie 360 XP przy awansie kosztujacym niecale 240. Teraz idle
        // odklada wylacznie zloto i surowce - powod, zeby zajrzec rano, a nie darmowy poziom.
        val rate = IdleRewards.ratePerHour(36)
        assertTrue(rate.gold > 0f && rate.iron > 0f && rate.essence > 0f)

        val reward = IdleRewards.pendingFor(36, 1L, 1L + 12 * hour)
        assertTrue(reward.gold > 0 && reward.iron > 0 && reward.essence > 0)
    }

    @Test
    fun `dluzsza nieobecnosc daje wiecej, dopoki nie przekroczy sufitu`() {
        val short = IdleRewards.pendingFor(20, 1L, 1L + 2 * hour)
        val long = IdleRewards.pendingFor(20, 1L, 1L + 6 * hour)
        assertTrue(long.gold > short.gold)
    }
}
