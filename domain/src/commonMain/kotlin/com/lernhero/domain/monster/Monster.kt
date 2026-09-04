package com.lernhero.domain.monster

import com.lernhero.domain.common.Rarity

import kotlinx.serialization.Serializable


@Serializable
data class MonsterSkill(
    val name: String,
    val description: String
)

/**
 * Catalog definition of a companion monster. Owned monsters are stored on the player as a
 * multiset of [id]s (see [Player.ownedMonsters]); the party references these ids by slot.
 *
 * [spriteKey] maps to a single-PNG visual preset in the feature layer, see
 * `MonsterVisualPresets.forSpecies` ("skeleton_warrior" | "skeleton_archer" | "skeleton_mage" |
 * "orc_warrior" | "orc_archer" | "human_archer"). "boss_skeleton_dragon" is wave-boss only and
 * intentionally has no [MonsterCard] - it is never purchasable as a companion.
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
    /**
     * Zywiol karty - decyduje o przewagach w walce (patrz [Element]). Domyslnie [Element.Fire],
     * zeby kazda karta jakis miala; katalog i tak przypisuje go kazdej z osobna.
     */
    val element: Element = Element.Fire,
    val skills: List<MonsterSkill> = emptyList(),
    /**
     * Id zaklec kompana z [CompanionSpells]. To one odrozniaja karty od siebie bardziej niz same
     * HP/atak - i to na nich gracz wydaje esencje z rozpuszczonych duplikatow.
     */
    val spellIds: List<String> = emptyList()
) {
    /** Zaklecia rozwiazane z katalogu - nieznane id po prostu wypada, nie wysadza karty. */
    val spells: List<CompanionSpell> get() = spellIds.mapNotNull { CompanionSpells.byId(it) }
}

object MonsterCatalog {
    val SKELETON_WARRIOR = MonsterCard(
        id = "skeleton_warrior",
        name = "Skeleton Warrior",
        hp = 70,
        attack = 14,
        rarity = Rarity.Common,
        cost = 120,
        spriteKey = "skeleton_warrior",
        element = Element.Death,
        spellIds = listOf("bone_shard"),
        skills = listOf(
            MonsterSkill("Bone Strike", "Deals melee damage to a chosen enemy.")
        )
    )

    val SKELETON_ARCHER = MonsterCard(
        id = "skeleton_archer",
        name = "Skeleton Archer",
        hp = 60,
        attack = 16,
        rarity = Rarity.Common,
        cost = 140,
        spriteKey = "skeleton_archer",
        element = Element.Death,
        spellIds = listOf("arrow_volley"),
        skills = listOf(
            MonsterSkill("Ranged Shot", "Hits an enemy with an arrow before it can close the distance.")
        )
    )

    val SKELETON_MAGE = MonsterCard(
        id = "skeleton_mage",
        name = "Skeleton Mage",
        hp = 55,
        attack = 22,
        rarity = Rarity.Rare,
        cost = 320,
        spriteKey = "skeleton_mage",
        element = Element.Water,
        spellIds = listOf("dark_bolt", "mend"),
        skills = listOf(
            MonsterSkill("Arcane Bolt", "A magic missile that deals damage at range."),
            MonsterSkill("Frost Nova", "Slows and damages an enemy with ice.")
        )
    )

    val ORC_WARRIOR = MonsterCard(
        id = "orc_warrior",
        name = "Orc Warrior",
        hp = 100,
        attack = 18,
        rarity = Rarity.Uncommon,
        cost = 200,
        spriteKey = "orc_warrior",
        element = Element.Fire,
        spellIds = listOf("war_cry"),
        skills = listOf(
            MonsterSkill("Axe Rend", "A quick attack that deals increased damage.")
        )
    )

    val ORC_ARCHER = MonsterCard(
        id = "orc_archer",
        name = "Orc Archer",
        hp = 80,
        attack = 20,
        rarity = Rarity.Uncommon,
        cost = 220,
        spriteKey = "orc_archer",
        element = Element.Fire,
        spellIds = listOf("flame_arrow"),
        skills = listOf(
            MonsterSkill("Arrow Hail", "A volley of arrows dealing damage at range.")
        )
    )

    val HUMAN_ARCHER = MonsterCard(
        id = "human_archer",
        name = "Human Archer",
        hp = 75,
        attack = 19,
        rarity = Rarity.Uncommon,
        cost = 210,
        spriteKey = "human_archer",
        element = Element.Nature,
        spellIds = listOf("arrow_volley", "mend"),
        skills = listOf(
            MonsterSkill("Precise Shot", "A precise shot with a higher chance to critically hit.")
        )
    )

    val all: List<MonsterCard> = listOf(
        SKELETON_WARRIOR,
        SKELETON_ARCHER,
        SKELETON_MAGE,
        ORC_WARRIOR,
        ORC_ARCHER,
        HUMAN_ARCHER
    )

    fun byId(id: String): MonsterCard? = all.firstOrNull { it.id == id }
}
