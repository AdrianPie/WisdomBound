package com.lernhero.domain.monetization

import kotlin.test.Test
import kotlin.test.assertEquals

class MonetizationTest {

    @Test
    fun counterFromAnotherDayReadsAsZero() {
        val yesterday = DailyCounter("2026-09-21", 5)
        assertEquals(0, yesterday.countOn("2026-09-22"))
        assertEquals(5, yesterday.remainingOn("2026-09-22", limit = 5))
    }

    @Test
    fun incrementOnNewDayStartsFromOne() {
        val next = DailyCounter("2026-09-21", 4).increment("2026-09-22")
        assertEquals(DailyCounter("2026-09-22", 1), next)
    }

    @Test
    fun incrementSameDayAccumulates() {
        val counter = DailyCounter().increment("d").increment("d").increment("d")
        assertEquals(3, counter.countOn("d"))
        assertEquals(2, counter.remainingOn("d", limit = 5))
    }

    @Test
    fun remainingNeverNegative() {
        assertEquals(0, DailyCounter("d", 9).remainingOn("d", limit = 5))
    }

    @Test
    fun goldRewardGrowsWithLevel() {
        assertEquals(100, AdRewards.goldFor(1))
        assertEquals(100, AdRewards.goldFor(0))
        assertEquals(127, AdRewards.goldFor(10))
    }
}
