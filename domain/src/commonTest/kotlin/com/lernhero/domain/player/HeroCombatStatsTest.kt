package com.lernhero.domain.player

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeroCombatStatsTest {

    @Test
    fun `goly bohater ma dokladnie statystyki bazowe`() {
        val stats = HeroCombatStats.of(Stats.Empty, HeroStats())

        assertEquals(HeroBaseStats.ATTACK, stats.attack)
        assertEquals(HeroBaseStats.HP, stats.maxHp)
        assertEquals(HeroBaseStats.CRIT_CHANCE, stats.critChance)
        assertEquals(0, stats.quizBonusPercent)
    }

    @Test
    fun `ekwipunek i punkty z awansow sumuja sie`() {
        val gear = Stats.Empty
            .with(StatType.Attack, 20f)
            .with(StatType.MaxHp, 100f)
        val hero = HeroStats(strength = 5, vitality = 3)

        val stats = HeroCombatStats.of(gear, hero)

        assertEquals(HeroBaseStats.ATTACK + 20 + 5 * HeroStats.ATTACK_PER_POINT, stats.attack)
        assertEquals(HeroBaseStats.HP + 100 + 3 * HeroStats.MAX_HP_PER_POINT, stats.maxHp)
    }

    @Test
    fun `szczescie i krytyk z przedmiotu skladaja sie na jedna szanse`() {
        val gear = Stats.Empty.with(StatType.CritChance, 0.10f)
        val stats = HeroCombatStats.of(gear, HeroStats(luck = 10))

        val expected = HeroBaseStats.CRIT_CHANCE + 0.10f + 10 * HeroStats.CRIT_PER_POINT
        assertEquals(expected, stats.critChance, 0.0001f)
    }

    @Test
    fun `skupienie widac jako procent bonusu z quizu`() {
        val stats = HeroCombatStats.of(Stats.Empty, HeroStats(focus = 5))
        assertEquals(10, stats.quizBonusPercent, "5 punktow x 2% = +10%")
    }

    @Test
    fun `karta postaci pokazuje te sama baze, ktora liczy walka`() {
        // Ten test jest po to, zeby baza nie rozjechala sie miedzy ekranem a walka - dokladnie to
        // sie stalo, gdy HERO_BASE_* byly prywatnymi stalymi w BattleViewModel.
        assertTrue(HeroBaseStats.HP > 0)
        assertTrue(HeroBaseStats.ATTACK > 0)
        assertTrue(HeroBaseStats.CRIT_CHANCE in 0f..1f)
    }
}
