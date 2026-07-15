package com.lernhero.domain.spell

import com.lernhero.domain.combat.SpellChoice

/**
 * Pasek zdolnosci: podzial 100% tury miedzy zwykly atak a przypisane zaklecia.
 *
 * [capacity] to gorny limit procentow, ktore wolno oddac zakleciom (baza 30%, rosnie od przedmiotow
 * i perkow). [weights] mapuje id zaklecia na jego procent. Reszta do 100% to zwykly atak.
 *
 * Model jest niezmienny i pilnuje niezmiennika sam: nie da sie przypisac wiecej niz [capacity].
 */
data class AbilityLoadout(
    val capacity: Int = BASE_CAPACITY,
    val weights: Map<String, Int> = emptyMap()
) {
    /** Ile procent tury zajmuja zaklecia. */
    val spentPercent: Int get() = weights.values.sum()

    /** Ile procent tury to zwykly atak. */
    val attackPercent: Int get() = (100 - spentPercent).coerceIn(0, 100)

    /** Ile jeszcze mozna rozdac zakleciom. */
    val freePercent: Int get() = (capacity - spentPercent).coerceAtLeast(0)

    fun percentOf(spellId: String): Int = weights[spellId] ?: 0

    /**
     * Ustawia procent zaklecia, docinajac go tak, by suma nie przekroczyla [capacity].
     * Zero usuwa zaklecie z paska.
     */
    fun withPercent(spellId: String, percent: Int): AbilityLoadout {
        val others = weights - spellId
        val room = (capacity - others.values.sum()).coerceAtLeast(0)
        val clamped = percent.coerceIn(0, room)
        val next = if (clamped <= 0) others else others + (spellId to clamped)
        return copy(weights = next)
    }

    /** Zmienia pojemnosc (np. perk z obozowiska) i docina przypisania, gdy nie mieszcza sie juz w nowej. */
    fun withCapacity(newCapacity: Int): AbilityLoadout {
        val cap = newCapacity.coerceIn(0, 100)
        if (weights.values.sum() <= cap) return copy(capacity = cap)
        // Za duzo przypisane - obcinamy od najmniejszych wag, az zmiescimy sie w limicie.
        val trimmed = weights.toList().sortedByDescending { it.second }.toMutableList()
        var total = trimmed.sumOf { it.second }
        var i = trimmed.lastIndex
        while (total > cap && i >= 0) {
            val cut = minOf(trimmed[i].second, total - cap)
            trimmed[i] = trimmed[i].first to (trimmed[i].second - cut)
            total -= cut
            if (trimmed[i].second == 0) trimmed.removeAt(i)
            i--
        }
        return copy(capacity = cap, weights = trimmed.toMap())
    }

    /**
     * Zamienia pasek na liste opcji dla silnika: kazde zaklecie z jego szansa, w kolejnosci malejacej.
     * Nieznane id (usuniete z katalogu) sa pomijane. [resolve] dostarcza logike rzutu z katalogu.
     */
    fun toChoices(resolve: (String) -> SpellChoice?): List<SpellChoice> =
        weights.entries
            .sortedByDescending { it.value }
            .mapNotNull { (id, percent) -> if (percent > 0) resolve(id)?.copy(chancePercent = percent) else null }

    companion object {
        /** Bazowa pojemnosc paska bez zadnych bonusow. */
        const val BASE_CAPACITY = 30
    }
}

/** Most z katalogu do silnika: zaklecie jako opcja tury (procent dolozy pasek). */
fun Spell.toChoice(): SpellChoice = SpellChoice(spellId = id, chancePercent = 0, cast = cast)
