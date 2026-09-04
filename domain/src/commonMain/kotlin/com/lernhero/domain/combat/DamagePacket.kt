package com.lernhero.domain.combat

/**
 * Obrazenia w locie - miedzy chwila, gdy ktos zamachnal sie mieczem, a chwila, gdy komus spadlo HP.
 *
 * Kazdy [DamageModifierEffect] dostaje ten pakiet i zwraca jego nowa wersje. To jedyne miejsce,
 * w ktorym cios da sie jeszcze osłabic, odbic albo wyminac.
 */
data class DamagePacket(
    val sourceId: CombatantId,
    val targetId: CombatantId,
    val amount: Float,
    val tag: DamageTag,
    val canCrit: Boolean = true,
    val isCritical: Boolean = false,
    val dodged: Boolean = false,
    /** Obrona celu nie zdejmie juz nic z tego pakietu (trucizna, obrazenia czyste). */
    val ignoresDefense: Boolean = false
) {
    fun scaled(factor: Float): DamagePacket = copy(amount = amount * factor)

    fun nullified(): DamagePacket = copy(amount = 0f)
}
