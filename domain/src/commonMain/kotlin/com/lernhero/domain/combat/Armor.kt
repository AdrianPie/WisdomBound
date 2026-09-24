package com.lernhero.domain.combat

/**
 * Pancerz w walce - JEDNA liczba: ulamek obrazen, ktory postac odcina (0.15 = bierze 15% mniej).
 *
 * Przedmioty maja dwie rzeczy, ktore do tej pory w walce nic nie robily: plaska [StatType.Defense]
 * (zbroje, buty, tarcze) i procentowa [StatType.DamageReduction] (Szafiry, niektore legendy, sprzet
 * kompanow). Obie skladamy tutaj w jeden procent, bo plaskie odejmowanie od ciosu przestaje miec
 * znaczenie, gdy ataki wrogow rosna do setek - a procent dziala tak samo na kazdej fali.
 *
 * Obrona przechodzi przez krzywa z malejacym zyskiem (`def / (def + K)`), wiec kazdy punkt cos daje,
 * ale nie da sie zbudowac postaci nietykalnej. Calosc jest przycieta do [MAX].
 */
object Armor {
    /** Obrona, przy ktorej odcina polowe obrazen (przed dodaniem redukcji procentowej). */
    const val DEFENSE_HALF = 40f

    /** Twardy sufit - nawet najlepszy tank dostaje co najmniej 40% ciosu. */
    const val MAX = 0.6f

    fun of(defense: Float, damageReduction: Float): Float {
        val fromDefense = if (defense > 0f) defense / (defense + DEFENSE_HALF) else 0f
        return (fromDefense + damageReduction.coerceAtLeast(0f)).coerceIn(0f, MAX)
    }

    fun of(stats: Stats): Float = of(stats[StatType.Defense], stats[StatType.DamageReduction])
}
