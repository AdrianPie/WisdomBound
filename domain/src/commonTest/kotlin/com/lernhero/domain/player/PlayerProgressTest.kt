package com.lernhero.domain.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerProgressTest {

    @Test
    fun `awans daje punkty i zostawia nadwyzke XP`() {
        val start = PlayerProgress()
        val needed = LevelCurve.xpForLevel(2)

        val result = LevelCurve.award(start, needed + 10)

        assertEquals(2, result.progress.level)
        assertEquals(10, result.progress.xp)
        assertEquals(1, result.levelsGained)
        assertEquals(LevelCurve.POINTS_PER_LEVEL, result.progress.unspentPoints)
    }

    @Test
    fun `jedna nagroda moze dac kilka poziomow naraz`() {
        val huge = (2..6).sumOf { LevelCurve.xpForLevel(it) }
        val result = LevelCurve.award(PlayerProgress(), huge)

        assertEquals(6, result.progress.level)
        assertEquals(5, result.levelsGained)
        assertEquals(5 * LevelCurve.POINTS_PER_LEVEL, result.pointsGained)
    }

    @Test
    fun `kolejne poziomy sa coraz drozsze`() {
        val second = LevelCurve.xpForLevel(2)
        val tenth = LevelCurve.xpForLevel(10)
        assertTrue(tenth > second * 3, "poziom 2: $second, poziom 10: $tenth")
    }

    @Test
    fun `zerowe i ujemne XP niczego nie zmienia`() {
        val start = PlayerProgress(level = 3, xp = 40, unspentPoints = 2)
        assertEquals(start, LevelCurve.award(start, 0).progress)
        assertEquals(start, LevelCurve.award(start, -100).progress)
    }

    @Test
    fun `punkt da sie wydac tylko gdy jakis zostal`() {
        val withPoint = PlayerProgress(unspentPoints = 1)
        val spent = LevelCurve.spendPoint(withPoint, HeroStat.Strength)

        assertEquals(1, spent?.stats?.strength)
        assertEquals(0, spent?.unspentPoints)
        assertNull(LevelCurve.spendPoint(PlayerProgress(unspentPoints = 0), HeroStat.Strength))
    }

    @Test
    fun `statystyki przekladaja sie na konkretne bonusy w walce`() {
        val stats = HeroStats(strength = 5, vitality = 4, focus = 3, luck = 10)

        assertEquals(5 * HeroStats.ATTACK_PER_POINT, stats.attackBonus)
        assertEquals(4 * HeroStats.MAX_HP_PER_POINT, stats.maxHpBonus)
        assertEquals(1f + 3 * HeroStats.QUIZ_DAMAGE_PER_POINT, stats.quizDamageMultiplier)
        assertEquals(10 * HeroStats.CRIT_PER_POINT, stats.critChanceBonus)
    }
}
