package com.lernhero.domain.preset

import kotlinx.serialization.Serializable

/** Rarity tiers shared by monsters and equipment. UI maps these to frame colors. */
enum class Rarity {
    Common,
    Uncommon,
    Rare,
    Epic,
    Legendary
}

@Serializable
data class MonsterSkill(
    val name: String,
    val description: String
)

/**
 * Catalog definition of a companion monster. Owned monsters are stored on the player as a
 * multiset of [id]s (see [Player.ownedMonsters]); the party references these ids by slot.
 *
 * [spriteKey] maps to the animated sprite sets in the feature layer ("skeleton" | "wolf" | "mag").
 */
@Serializable
data class MonsterCard(
    val id: String,
    val name: String,
    val hp: Int,
    val attack: Int,
    val rarity: Rarity,
    val cost: Int,
    val spriteKey: String,
    val skills: List<MonsterSkill> = emptyList()
)

object MonsterCatalog {
    val SKELETON = MonsterCard(
        id = "skeleton",
        name = "Szkielet",
        hp = 70,
        attack = 12,
        rarity = Rarity.Common,
        cost = 120,
        spriteKey = "skeleton",
        skills = listOf(
            MonsterSkill("Kosciany cios", "Zadaje obrazenia w zwarciu wybranemu wrogowi.")
        )
    )

    val WOLF = MonsterCard(
        id = "wolf",
        name = "Wilk",
        hp = 90,
        attack = 16,
        rarity = Rarity.Uncommon,
        cost = 200,
        spriteKey = "wolf",
        skills = listOf(
            MonsterSkill("Rozszarpanie", "Szybki atak zadajacy podwyzszone obrazenia.")
        )
    )

    val SKELETON_MAGE = MonsterCard(
        id = "skeleton_mage",
        name = "Szkielet mag",
        hp = 60,
        attack = 22,
        rarity = Rarity.Rare,
        cost = 320,
        spriteKey = "mag",
        skills = listOf(
            MonsterSkill("Pocisk arkany", "Magiczny pocisk zadajacy obrazenia z dystansu."),
            MonsterSkill("Mrozna nowa", "Spowalnia i rani wroga lodem.")
        )
    )

    val all: List<MonsterCard> = listOf(SKELETON, WOLF, SKELETON_MAGE)

    fun byId(id: String): MonsterCard? = all.firstOrNull { it.id == id }
}
