package com.lernhero.domain.player

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.talent.TalentBonuses
import kotlin.math.roundToInt

/**
 * Bazowe liczby bohatera i skladanie ich z ekwipunkiem oraz punktami z awansow.
 *
 * Wyciagniete z BattleViewModel do domeny, bo tych samych liczb potrzebuje TEZ karta postaci -
 * a dopoki siedzialy jako prywatne stale w walce, ekran nie mial jak pokazac, ile gracz naprawde
 * zadaje obrazen. Jedno zrodlo prawdy zamiast dwoch, ktore i tak by sie rozjechaly.
 */
object HeroBaseStats {
    const val HP = 420
    const val ATTACK = 78

    /** Bazowa szansa na cios krytyczny, zanim doliczy sie [HeroStat.Luck] i ekwipunek. */
    const val CRIT_CHANCE = 0.12f

    /** Mnoznik obrazen krytycznych. */
    const val CRIT_MULTIPLIER = 1.5f
}

/**
 * Gotowe liczby "kim jestem w walce" - to, co karta postaci pokazuje pod sylwetka. Kazda wartosc
 * jest juz zlozona z bazy, ekwipunku i statystyk z awansow, wiec ekran niczego nie dolicza sam.
 */
data class HeroCombatStats(
    val attack: Int,
    val maxHp: Int,
    val critChance: Float,
    val critMultiplier: Float,
    val quizDamageMultiplier: Float,
    val spellPower: Float,
    val damageReduction: Float
) {
    val critPercent: Int get() = (critChance * 100).roundToInt()
    val quizBonusPercent: Int get() = ((quizDamageMultiplier - 1f) * 100).roundToInt()
    val spellPowerPercent: Int get() = (spellPower * 100).roundToInt()
    val damageReductionPercent: Int get() = (damageReduction * 100).roundToInt()

    companion object {
        /**
         * [gearStats] to suma statystyk z zalozonego ekwipunku
         * ([com.lernhero.domain.item.Loadout.stats]), [heroStats] to punkty rozdane po awansach,
         * a [talents] to plaskie bonusy z drzewka ([com.lernhero.domain.talent.TalentRules.bonusesOf]).
         *
         * Talenty dorzucaja tu WYLACZNIE to, co da sie pokazac jedna liczba. Ich druga, wieksza
         * czesc - staki mechanik bojowych (garda, kolce, rozped...) - nie ma sensu na karcie
         * postaci, bo dziala dopiero w walce; te wchodza do niej osobno, patrz BattleViewModel.
         */
        fun of(
            gearStats: Stats,
            heroStats: HeroStats,
            talents: TalentBonuses = TalentBonuses.EMPTY
        ): HeroCombatStats = HeroCombatStats(
            attack = HeroBaseStats.ATTACK + gearStats[StatType.Attack].toInt() + heroStats.attackBonus + talents.attack,
            maxHp = HeroBaseStats.HP + gearStats[StatType.MaxHp].toInt() + heroStats.maxHpBonus + talents.maxHp,
            critChance = HeroBaseStats.CRIT_CHANCE + gearStats[StatType.CritChance] +
                heroStats.critChanceBonus + talents.critChance,
            critMultiplier = HeroBaseStats.CRIT_MULTIPLIER,
            quizDamageMultiplier = heroStats.quizDamageMultiplier * talents.quizDamageMultiplier,
            spellPower = gearStats[StatType.SpellPower],
            damageReduction = gearStats[StatType.DamageReduction]
        )
    }
}
