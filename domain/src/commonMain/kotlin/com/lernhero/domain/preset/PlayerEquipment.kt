package com.lernhero.domain.preset

import kotlinx.serialization.Serializable

@Serializable
data class PlayerEquipment(
    val head: Boolean = false,
    val hands: Boolean = false,
    val legs: Boolean = false,
    val feet: Boolean = false,
    val mainHand: Boolean = false,
) {
    val attackBonus: Int
        get() = listOf(mainHand, hands).count { it } * 3

    val defenseBonus: Int
        get() = listOf(head, hands, legs, feet).count { it } * 2

    val hpBonus: Int
        get() = defenseBonus * 5
}
