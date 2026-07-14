package com.lernhero.domain.combat

/**
 * Skad wzialy sie obrazenia. Slowo klucz dla dwoch rzeczy:
 * - odbicie ignoruje pakiety oznaczone [Reflect], inaczej dwie tarcze odbijalyby sie w nieskonczonosc,
 * - trucizna z definicji przebija obrone.
 */
enum class DamageTag {
    Attack,
    Reflect,
    Burn,
    Poison,
    Skill
}

/**
 * Slownik intencji, ktorymi efekty rozmawiaja z silnikiem. Efekt nigdy nie zmienia stanu sam -
 * zwraca akcje, a silnik je wykonuje i emituje z nich zdarzenia.
 *
 * Nowa akcja = nowa gałąź w [com.lernhero.domain.combat.engine.BattleEngine]; nowy przedmiot
 * uzywajacy istniejacych akcji nie wymaga zmian nigdzie.
 */
sealed interface CombatAction {

    data class DealDamage(
        val sourceId: CombatantId,
        val targetIds: List<CombatantId>,
        val amount: Float,
        val canCrit: Boolean = true,
        val tag: DamageTag = DamageTag.Skill
    ) : CombatAction

    data class Heal(
        val sourceId: CombatantId,
        val targetIds: List<CombatantId>,
        val amount: Float
    ) : CombatAction

    data class ApplyStatus(
        val sourceId: CombatantId,
        val targetIds: List<CombatantId>,
        val kind: StatusKind,
        val potency: Float,
        val turns: Int,
        val stat: StatType? = null
    ) : CombatAction

    data class GrantShield(
        val targetIds: List<CombatantId>,
        val amount: Float
    ) : CombatAction

    /** Dodatkowy atak poza kolejnoscia - np. po zabiciu wroga. Silnik pilnuje glebokosci lancucha. */
    data class ExtraAttack(
        val attackerId: CombatantId,
        val targetId: CombatantId? = null
    ) : CombatAction

    data class Cleanse(
        val targetIds: List<CombatantId>,
        val kinds: Set<StatusKind>
    ) : CombatAction
}
