package com.lernhero.domain.combat

/**
 * Wszystko, co da sie wyrazic liczba, jest statystyka - lacznie z rzeczami, ktore w innych grach
 * bywaja zaszyte w kodzie przedmiotu (liczba celow ataku, szansa na uniki, kradziez zycia).
 *
 * Dzieki temu "miecz atakujacy 2 wrogow" i "pierscien +1 cel" skladaja sie same z siebie:
 * silnik czyta jedna liczbe, nie zna zadnego z tych przedmiotow.
 */
enum class StatType {
    MaxHp,
    Attack,
    Defense,

    /** Kolejnosc tur - szybsi bija pierwsi. */
    Speed,

    /** 0.0 - 1.0 */
    CritChance,

    /** Mnoznik obrazen przy krytyku (1.5 = +50%). */
    CritDamage,

    /** 0.0 - 1.0 */
    Dodge,

    /** Ulamek zadanych obrazen wracajacy jako leczenie. */
    Lifesteal,

    /** 0.0 - 1.0, plaska redukcja obrazen przychodzacych. */
    DamageReduction,

    /** Sila leczenia - mnoznik dla efektow leczacych. */
    HealPower,

    /** Skraca odstepy efektow cyklicznych ("co X tur"). */
    CooldownReduction,

    /** Ilu wrogow obejmuje zwykly atak. Kazdy startuje z 1. */
    AttackTargets
}

/**
 * Worek statystyk. Niezmienny; [plus] skleja wklady z wielu zrodel (baza + przedmioty + aury).
 */
data class Stats(private val values: Map<StatType, Float> = emptyMap()) {

    operator fun get(stat: StatType): Float = values[stat] ?: 0f

    operator fun plus(other: Stats): Stats {
        if (values.isEmpty()) return other
        if (other.values.isEmpty()) return this
        val merged = values.toMutableMap()
        other.values.forEach { (stat, value) -> merged[stat] = (merged[stat] ?: 0f) + value }
        return Stats(merged)
    }

    fun with(stat: StatType, value: Float): Stats = Stats(values + (stat to value))

    /** Odsiewa wybrane statystyki - przydatne, gdy warstwa wyzej wchlonela je juz do swoich liczb. */
    fun without(vararg stats: StatType): Stats = Stats(values - stats.toSet())

    fun isEmpty(): Boolean = values.isEmpty()

    fun asMap(): Map<StatType, Float> = values

    companion object {
        val Empty = Stats()

        fun of(vararg pairs: Pair<StatType, Float>): Stats = Stats(pairs.toMap())

        /** Statystyki bazowe kazdego walczacego - stad biora sie domysle wartosci. */
        fun baseline(maxHp: Int, attack: Int, defense: Int = 0, speed: Float = 10f): Stats = of(
            StatType.MaxHp to maxHp.toFloat(),
            StatType.Attack to attack.toFloat(),
            StatType.Defense to defense.toFloat(),
            StatType.Speed to speed,
            StatType.CritChance to 0.05f,
            StatType.CritDamage to 1.5f,
            StatType.AttackTargets to 1f
        )
    }
}
