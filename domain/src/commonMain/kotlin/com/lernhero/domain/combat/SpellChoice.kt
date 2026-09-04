package com.lernhero.domain.combat

/**
 * Co zaklecie robi, gdy padnie na nie los. Czysta funkcja (kontekst) -> intencje, tak samo jak
 * [TriggeredEffect] - efekt nigdy nie dotyka stanu sam, tylko zwraca akcje do wykonania.
 */
fun interface SpellCast {
    fun cast(context: EffectContext): List<CombatAction>
}

/**
 * Jedna opcja tury: zaklecie [spellId] z szansa [chancePercent] na aktywacje. Silnik trzyma liste
 * takich opcji na walczacym i losuje z niej co ture - reszta prawdopodobienstwa to zwykly atak.
 *
 * To jest widok silnika na "pasek zdolnosci": sam procent i logika rzutu, bez wiedzy o katalogu
 * zaklec ([com.lernhero.domain.spell.Spell]) ani o UI.
 */
data class SpellChoice(
    val spellId: String,
    val chancePercent: Int,
    val cast: SpellCast
)
