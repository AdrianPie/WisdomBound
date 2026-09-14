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
        // Prog byl kiedys "dziesiaty x3 wzgledem drugiego" i opisywal krzywa 1.18. Ta krzywa zostala
        // zlagodzona do 1.12, bo uciekala LINIOWEJ sile, ktora poziom rozdaje (+2 ataku i +8 HP za
        // punkt) - ostatnia plansza Normalnego wymagala poziomu 23, a cala kampania wyplacala 14.
        // Test pilnuje wiec INTENCJI (krzywa ma byc wykladnicza i rosnaca), a nie tamtej liczby.
        assertTrue(
            (2..40).all { LevelCurve.xpForLevel(it) > LevelCurve.xpForLevel(it - 1) },
            "Kazdy kolejny poziom ma kosztowac wiecej od poprzedniego"
        )
        val early = LevelCurve.xpForLevel(10) - LevelCurve.xpForLevel(9)
        val late = LevelCurve.xpForLevel(30) - LevelCurve.xpForLevel(29)
        assertTrue(late > early * 5, "Krzywa ma zostac wykladnicza: skok $early wczesnie, $late pozno")
    }

    @Test
    fun `pierwsze poziomy leca po jednej walce, a dalsze wyraznie zwalniaja`() {
        // To jest KSZTALT krzywej, o ktory chodzi w calej grze: pierwsza godzina ma dac
        // kilkanascie awansow (miejsca na kompanow, punkty talentow - widac, ze rozwoj istnieje),
        // a druga juz tylko kilka. Wczesniej BASE_XP 120 kazalo zagrac trzy plansze na poziom 2:
        // przez pierwsze piec minut nowy gracz nie widzial ANI JEDNEGO awansu.
        //
        // Odniesienie: jedna plansza kampanii wyplaca mniej wiecej sto XP.
        val stagePayout = 100
        assertTrue(
            LevelCurve.xpForLevel(2) < stagePayout,
            "pierwszy awans ma zmiescic sie w jednej walce, a kosztuje ${LevelCurve.xpForLevel(2)}"
        )

        val toLevel10 = (2..10).sumOf { LevelCurve.xpForLevel(it) }
        val toLevel25 = (2..25).sumOf { LevelCurve.xpForLevel(it) }
        assertTrue(
            toLevel25 > toLevel10 * 8,
            "druga polowa toru ma byc nieporownanie drozsza: $toLevel10 do 10, $toLevel25 do 25"
        )
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
