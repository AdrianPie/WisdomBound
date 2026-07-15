package com.lernhero.domain.spell

import com.lernhero.domain.combat.SpellCast
import com.lernhero.domain.common.Rarity

/**
 * Zaklecie kupowane w sklepie i przypisywane do paska zdolnosci. Tak jak przedmiot, jest to
 * **dane** ([id], koszt, ikona) plus [cast] - logika opisujaca, co robi. Zadnej wiedzy o silniku
 * ani o UI: [cast] zwraca intencje, ktore wykona silnik walki.
 *
 * Dolozenie setnego spella to jeden wpis w katalogu, zlozony z istniejacych akcji bojowych.
 */
data class Spell(
    val id: String,
    val name: String,
    val description: String,
    val rarity: Rarity,
    val iconKey: String,
    val cost: Int,
    val cast: SpellCast
)
