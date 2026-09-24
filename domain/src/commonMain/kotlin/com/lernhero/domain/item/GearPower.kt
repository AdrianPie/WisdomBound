package com.lernhero.domain.item

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.player.HeroCombatStats
import kotlin.math.roundToInt

/**
 * JEDNA liczba mowiaca "ile ten przedmiot/ta postac jest warta".
 *
 * Bez niej karta przedmiotu kazala graczowi porownywac dwie kolumny liczb w roznych jednostkach
 * (+20 ataku kontra +5% uniku) i samemu zgadywac, co z tego wychodzi. Mobilne RPG rozwiazuja to
 * od lat tak samo: kazda statystyka ma swoja cene w punktach mocy, a gracz widzi sume.
 *
 * Wagi sa w skali ATAKU: 1 pkt mocy = 1 pkt ataku. Statystyki procentowe przeliczaja sie przez
 * to, ile ataku musialby dac przedmiot, zeby dac tyle samo w walce - dlatego 5% krytyka wazy
 * podobnie co +15 ataku, a nie co +0.05.
 *
 * To jest WSKAZNIK, a nie regula walki: silnik go nie czyta, wiec przestrojenie wag nie rusza
 * ani jednej liczby obrazen.
 */
object GearPower {

    private val WEIGHTS: Map<StatType, Float> = mapOf(
        StatType.Attack to 1f,
        StatType.MaxHp to 0.2f,
        StatType.Defense to 1.5f,
        StatType.Speed to 2f,
        StatType.CritChance to 300f,
        StatType.CritDamage to 120f,
        StatType.Dodge to 320f,
        StatType.Lifesteal to 240f,
        StatType.DamageReduction to 400f,
        StatType.SpellPower to 200f,
        StatType.HealPower to 140f,
        StatType.CooldownReduction to 200f,
        StatType.AttackTargets to 150f
    )

    /** Moc samych statystyk - tyle pokazuje odznaka na karcie przedmiotu. */
    fun of(stats: Stats): Int =
        stats.asMap().entries.sumOf { (stat, value) ->
            ((WEIGHTS[stat] ?: 0f) * value).toDouble()
        }.roundToInt()

    /**
     * Moc przedmiotu razem z jego zdolnosciami. Efekt nie ma statystyk, wiec bez tej doplaty
     * legendarny pierscien "odbija 30% obrazen" wygladalby na slabszy od pospolitego z +10 HP -
     * a to on wygrywa walki.
     */
    fun of(item: Item): Int = of(item.stats) + item.effects.size * EFFECT_POWER

    /**
     * Moc BOHATERA - ta sama skala co przedmioty, bo liczy sie z jego gotowych liczb bojowych.
     * Dzieki temu "+140 mocy" z karty miecza widac potem w tej samej liczbie nad postacia.
     */
    fun of(hero: HeroCombatStats): Int = of(
        Stats.of(
            StatType.Attack to hero.attack.toFloat(),
            StatType.MaxHp to hero.maxHp.toFloat(),
            StatType.CritChance to hero.critChance,
            StatType.SpellPower to hero.spellPower,
            StatType.DamageReduction to hero.damageReduction
        )
    )

    /** Ile "warta" jest jedna zdolnosc. Grubo przyblizone - patrz KDoc klasy. */
    private const val EFFECT_POWER = 40
}
