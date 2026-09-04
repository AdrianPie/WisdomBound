package com.lernhero.domain.combat.effects

import com.lernhero.domain.combat.AuraEffect
import com.lernhero.domain.combat.AuraScope
import com.lernhero.domain.combat.StatEffect
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats

/**
 * Efekty pasywne: sam bonus do liczb, zadnej reakcji na zdarzenia.
 *
 * Sa tak cienkie, bo cala robote wykonuje silnik, czytajac statystyki. "Miecz bijacy dwoch
 * wrogow" nie ma wlasnej logiki ataku - podnosi [StatType.AttackTargets] o jeden.
 */

/** Uniwersalny bonus. Uzywaj, gdy przedmiot po prostu dodaje liczby i nie ma nazwy mechaniki. */
class StatBonusEffect(
    override val id: String,
    override val description: String,
    override val bonus: Stats
) : StatEffect

/**
 * Atak obejmuje [targets] wrogow naraz. Baza kazdego walczacego to 1 cel, wiec dokladamy roznice -
 * dzieki temu miecz (2 cele) i pierscien (+1 cel) sumuja sie do trzech bez zadnej specjalnej obslugi.
 */
class MultiTargetAttackEffect(private val targets: Int) : StatEffect {
    override val id = "multi_target_$targets"
    override val description = "Atakuje $targets przeciwnikow naraz."
    override val bonus = Stats.of(StatType.AttackTargets to (targets - 1).toFloat())
}

/** Szansa na trafienie krytyczne, 0.15 = +15%. */
class CriticalChanceEffect(private val percent: Float) : StatEffect {
    override val id = "crit_chance_${(percent * 100).toInt()}"
    override val description = "+${(percent * 100).toInt()}% szansy na trafienie krytyczne."
    override val bonus = Stats.of(StatType.CritChance to percent)
}

class CriticalDamageEffect(private val multiplier: Float) : StatEffect {
    override val id = "crit_damage_${(multiplier * 100).toInt()}"
    override val description = "+${(multiplier * 100).toInt()}% obrazen krytycznych."
    override val bonus = Stats.of(StatType.CritDamage to multiplier)
}

/** Kradziez zycia - ulamek zadanych obrazen wraca jako leczenie. Silnik dolicza to sam. */
class LifestealEffect(private val percent: Float) : StatEffect {
    override val id = "lifesteal_${(percent * 100).toInt()}"
    override val description = "${(percent * 100).toInt()}% zadanych obrazen wraca jako zdrowie."
    override val bonus = Stats.of(StatType.Lifesteal to percent)
}

class DodgeChanceEffect(private val percent: Float) : StatEffect {
    override val id = "dodge_${(percent * 100).toInt()}"
    override val description = "+${(percent * 100).toInt()}% szansy na unik."
    override val bonus = Stats.of(StatType.Dodge to percent)
}

class SpeedEffect(private val amount: Float) : StatEffect {
    override val id = "speed_${amount.toInt()}"
    override val description = "+${amount.toInt()} szybkosci - czesciej zaczynasz ture pierwszy."
    override val bonus = Stats.of(StatType.Speed to amount)
}

class DamageReductionEffect(private val percent: Float) : StatEffect {
    override val id = "damage_reduction_${(percent * 100).toInt()}"
    override val description = "Otrzymujesz o ${(percent * 100).toInt()}% mniej obrazen."
    override val bonus = Stats.of(StatType.DamageReduction to percent)
}

/**
 * Skraca odstepy wszystkich efektow cyklicznych ("co X tur") o [turns].
 * Nie zna zadnego z nich - warunek [com.lernhero.domain.combat.EveryNTurns] sam czyta te statystyke.
 */
class CooldownReductionEffect(private val turns: Int) : StatEffect {
    override val id = "cooldown_reduction_$turns"
    override val description = "Efekty cykliczne odpalaja sie o $turns ture szybciej."
    override val bonus = Stats.of(StatType.CooldownReduction to turns.toFloat())
}

/** Aura: bonus dla wszystkich sojusznikow na polu bitwy. */
class AllyAuraEffect(
    override val id: String,
    override val description: String,
    override val bonus: Stats
) : AuraEffect {
    override val scope = AuraScope.Allies
}

/** Aura odwrotna: oslabia wszystkich wrogow. */
class EnemyAuraEffect(
    override val id: String,
    override val description: String,
    override val bonus: Stats
) : AuraEffect {
    override val scope = AuraScope.Enemies
}
