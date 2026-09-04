package com.lernhero.domain.monster

import kotlinx.serialization.Serializable

/**
 * Zywiol kompana i wroga. Cztery typy ustawione w KOLO, nie w pary:
 *
 * ```
 * Ogien -> Natura -> Woda -> Smierc -> Ogien
 * ```
 *
 * Kazdy bije dokladnie jeden i przegrywa z dokladnie jednym, dwa pozostale sa neutralne. To wersja
 * "papier-kamien-nozyce" dla czterech elementow: zawsze istnieje dobry wybor przeciw konkretnemu
 * przeciwnikowi, ale nie ma typu, ktory jest po prostu najlepszy.
 *
 * [icon] i [colorHex] sa placeholderami pod przyszla grafike - domena nie zna zasobow, wiec warstwa
 * UI zamienia je na ikonki i ramki tak samo, jak robi to juz ze spriteKey.
 */
@Serializable
enum class Element(
    val label: String,
    val icon: String,
    /** Kolor odznaki - warstwa UI robi z tego Color(0xFF...). Placeholder do podmiany razem z grafika. */
    val colorHex: Long
) {
    Fire("Fire", "🔥", 0xFFE0654F),
    Nature("Nature", "🌿", 0xFF4ADE80),
    Water("Water", "💧", 0xFF3B9DFF),
    Death("Death", "💀", 0xFF9B6BD6);

    /** Zywiol, ktory ten bije (dostaje [ADVANTAGE_BONUS] obrazen wiecej). */
    val beats: Element
        get() = when (this) {
            Fire -> Nature
            Nature -> Water
            Water -> Death
            Death -> Fire
        }

    /** Zywiol, ktory bije ten (stad [DISADVANTAGE_PENALTY] obrazen mniej). */
    val losesTo: Element
        get() = entries.first { it.beats == this }

    companion object {
        /** O ile mocniej bije zywiol z przewaga. */
        const val ADVANTAGE_BONUS = 0.25f

        /** O ile slabiej bije zywiol w niekorzystnym starciu. Mniej niz bonus - kara ma bolec, nie parazlizowac. */
        const val DISADVANTAGE_PENALTY = 0.15f

        /**
         * Mnoznik obrazen, gdy [attacker] uderza w [defender].
         *
         * Zwraca 1.0 dla starc neutralnych i dla brakujacego zywiolu (null) - bohater i stare zapisy
         * bez przypisanego typu maja dzialac normalnie, a nie dostawac losowej kary.
         */
        fun multiplier(attacker: Element?, defender: Element?): Float {
            if (attacker == null || defender == null) return 1f
            return when (defender) {
                attacker.beats -> 1f + ADVANTAGE_BONUS
                attacker.losesTo -> 1f - DISADVANTAGE_PENALTY
                else -> 1f
            }
        }

        /** Jak nazwac starcie w UI - podpowiedz nad paskiem HP, zeby gracz wiedzial, czemu bije mocniej. */
        fun relationLabel(attacker: Element?, defender: Element?): String? = when {
            attacker == null || defender == null -> null
            defender == attacker.beats -> "Przewaga"
            defender == attacker.losesTo -> "Slabosc"
            else -> null
        }
    }
}
