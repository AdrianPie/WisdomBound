package com.lernhero.domain.monster

import kotlin.math.roundToInt

/**
 * Moc bierna potwora - dziala ZAWSZE, bez losowania i bez esencji, po obu stronach pola bitwy
 * (kompan gracza i wrog tego samego gatunku maja te sama moc).
 *
 * Zaklecia ([CompanionSpell]) mowia, co potwor czasem ROBI; moc bierna mowi, JAKI jest. Dwie karty
 * z tym samym HP i atakiem graja sie inaczej, bo jedna oddaje ciosy ([Thorns]), a druga raz na fale
 * wstaje z martwych ([Undying]) - i to jest cala roznica miedzy "skorka" a potworem.
 *
 * [value] to jedna liczba na moc - jej znaczenie opisuje [describe] (i to samo czyta walka).
 */
enum class CompanionPassive(
    val label: String,
    val icon: String,
    val value: Float
) {
    /** Oddaje czesc obrazen, ktore przyjal, prosto w napastnika. */
    Thorns("Thorns", "🌵", 0.25f),

    /** Leczy sie o czesc zadanych obrazen. */
    Lifesteal("Lifesteal", "🩸", 0.20f),

    /** Szansa, ze cios wroga w ogole go nie trafi. */
    Evasion("Evasion", "💨", 0.12f),

    /** Raz na fale zamiast pasc wstaje z czescia maks. HP. */
    Undying("Undying", "💀", 0.30f),

    /** Bije mocniej, gdy sam ma mniej niz polowe HP. */
    Berserk("Berserk", "😡", 0.40f),

    /** Na poczatku kazdej tury odzyskuje czesc maks. HP. */
    Regeneration("Regeneration", "🌿", 0.05f),

    /** Bije mocniej w cele, ktorym zostalo malo HP. */
    Predator("Predator", "🎯", 0.35f),

    /** Wzmacnia atak kazdego sojusznika TEGO SAMEGO zywiolu (siebie tez) - nagradza skladanie druzyny. */
    Warband("Warband", "🚩", 0.10f),

    /** Dodatkowa szansa na trafienie krytyczne. */
    KeenEye("Keen Eye", "👁️", 0.15f);

    /** Opis dla gracza - te same liczby, ktore liczy walka. */
    fun describe(): String {
        val pct = (value * 100).roundToInt()
        return when (this) {
            Thorns -> "Reflects $pct% of damage taken back at the attacker."
            Lifesteal -> "Heals for $pct% of the damage it deals."
            Evasion -> "$pct% chance to dodge an attack completely."
            Undying -> "Once per wave, survives a killing blow with $pct% HP."
            Berserk -> "+$pct% attack while below half HP."
            Regeneration -> "Recovers $pct% max HP at the start of every turn."
            Predator -> "+$pct% damage to targets below ${(PREDATOR_THRESHOLD * 100).roundToInt()}% HP."
            Warband -> "+$pct% attack to every ally of the same element."
            KeenEye -> "+$pct% critical hit chance."
        }
    }

    companion object {
        /** Ponizej tego ulamka HP cel jest "ranny" dla [Predator] i zaklec typu Execute. */
        const val PREDATOR_THRESHOLD = 0.40f

        /** Ponizej tego ulamka wlasnego HP wlacza sie [Berserk]. */
        const val BERSERK_THRESHOLD = 0.50f
    }
}
