package com.lernhero.domain.preset

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val character: Character? = null,
    val money: Int = 0,
    val equipment: PlayerEquipment = PlayerEquipment(),
    val ownedItems: List<String> = emptyList(),
    /** Multiset of [MonsterCard] ids the player owns (duplicates allowed). */
    val ownedMonsters: List<String> = emptyList(),
    /** Companion slots for the pre-battle party. Hero is implicit; entries are [MonsterCard] ids or null. */
    val party: List<String?> = emptyList()
)

