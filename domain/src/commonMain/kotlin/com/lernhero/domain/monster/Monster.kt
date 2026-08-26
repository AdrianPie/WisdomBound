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
        name = "Szkielet wojownik",
        hp = 70,
        attack = 14,
        rarity = Rarity.Common,
        cost = 120,
        spriteKey = "skeleton_warrior",
        spellIds = listOf("bone_shard"),
        skills = listOf(
            MonsterSkill("Kosciany cios", "Zadaje obrazenia w zwarciu wybranemu wrogowi.")
        )
    )

    val SKELETON_ARCHER = MonsterCard(
        id = "skeleton_archer",
        name = "Szkielet lucznik",
        hp = 60,
        attack = 16,
        rarity = Rarity.Common,
        cost = 140,
        spriteKey = "skeleton_archer",
        spellIds = listOf("arrow_volley"),
        skills = listOf(
            MonsterSkill("Strzal z dystansu", "Trafia wroga strzala, zanim ten zdazy sie zblizyc.")
        )
    )

    val SKELETON_MAGE = MonsterCard(
        id = "skeleton_mage",
        name = "Szkielet mag",
        hp = 55,
        attack = 22,
        rarity = Rarity.Rare,
        cost = 320,
        spriteKey = "skeleton_mage",
        spellIds = listOf("dark_bolt", "mend"),
        skills = listOf(
            MonsterSkill("Pocisk arkany", "Magiczny pocisk zadajacy obrazenia z dystansu."),
            MonsterSkill("Mrozna nowa", "Spowalnia i rani wroga lodem.")
        )
    )

    val ORC_WARRIOR = MonsterCard(
        id = "orc_warrior",
        name = "Ork wojownik",
        hp = 100,
        attack = 18,
        rarity = Rarity.Uncommon,
        cost = 200,
        spriteKey = "orc_warrior",
        spellIds = listOf("war_cry"),
        skills = listOf(
            MonsterSkill("Rozszarpanie toporem", "Szybki atak zadajacy podwyzszone obrazenia.")
        )
    )

    val ORC_ARCHER = MonsterCard(
        id = "orc_archer",
        name = "Ork lucznik",
        hp = 80,
        attack = 20,
        rarity = Rarity.Uncommon,
        cost = 220,
        spriteKey = "orc_archer",
        spellIds = listOf("flame_arrow"),
        skills = listOf(
            MonsterSkill("Grad strzal", "Seria strzalow zadajaca obrazenia z dystansu.")
        )
    )

    val HUMAN_ARCHER = MonsterCard(
        id = "human_archer",
        name = "Ludzki lucznik",
        hp = 75,
        attack = 19,
        rarity = Rarity.Uncommon,
        cost = 210,
        spriteKey = "human_archer",
        spellIds = listOf("arrow_volley", "mend"),
        skills = listOf(
            MonsterSkill("Celny strzal", "Precyzyjny strzal z podwyzszona szansa na trafienie krytyczne.")
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
