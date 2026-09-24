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
 * `MonsterVisualPresets.forSpecies` (the key equals the card id). "boss_skeleton_dragon" is wave-boss only and
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

    // ---- Druga fala kompanow (wrzesien 2026): 35 gatunkow na czterech zywiolach, od Common do
    // Legendary. Statystyki = baza rzadkosci x archetyp (tank / zbalansowany / dps / mag / healer /
    // lucznik), zeby karty jednej rzadkosci roznily sie ROLA, a nie tylko liczbami.

    val EMBER_IMP = MonsterCard(
        id = "ember_imp",
        name = "Ember Imp",
        hp = 58,
        attack = 18,
        rarity = Rarity.Common,
        cost = 120,
        spriteKey = "ember_imp",
        element = Element.Fire,
        spellIds = listOf("bone_shard"),
        skills = listOf(MonsterSkill("Cinder Flick", "Flicks a burning ember at an enemy."))
    )
    val FIRE_BAT = MonsterCard(
        id = "fire_bat",
        name = "Fire Bat",
        hp = 58,
        attack = 18,
        rarity = Rarity.Common,
        cost = 120,
        spriteKey = "fire_bat",
        element = Element.Fire,
        spellIds = listOf("bone_shard"),
        skills = listOf(MonsterSkill("Scorching Dive", "Dives at an enemy, leaving it smoldering."))
    )
    val LAVA_SLIME = MonsterCard(
        id = "lava_slime",
        name = "Lava Slime",
        hp = 92,
        attack = 12,
        rarity = Rarity.Common,
        cost = 120,
        spriteKey = "lava_slime",
        element = Element.Fire,
        spellIds = listOf("war_cry"),
        skills = listOf(MonsterSkill("Molten Body", "Soaks hits with its bubbling bulk."))
    )
    val GOBLIN_SPEARMAN = MonsterCard(
        id = "goblin_spearman",
        name = "Goblin Spearman",
        hp = 71,
        attack = 15,
        rarity = Rarity.Common,
        cost = 120,
        spriteKey = "goblin_spearman",
        element = Element.Nature,
        spellIds = listOf("war_cry"),
        skills = listOf(MonsterSkill("Spear Jab", "A quick thrust with a crude spear."))
    )
    val MUSHROOM_SPRITE = MonsterCard(
        id = "mushroom_sprite",
        name = "Mushroom Sprite",
        hp = 65,
        attack = 14,
        rarity = Rarity.Common,
        cost = 120,
        spriteKey = "mushroom_sprite",
        element = Element.Nature,
        spellIds = listOf("mend"),
        skills = listOf(MonsterSkill("Spore Cloud", "Soothing spores patch up allies."))
    )
    val TIDECRAWLER = MonsterCard(
        id = "tidecrawler",
        name = "Tidecrawler",
        hp = 92,
        attack = 12,
        rarity = Rarity.Common,
        cost = 120,
        spriteKey = "tidecrawler",
        element = Element.Water,
        spellIds = listOf("war_cry"),
        skills = listOf(MonsterSkill("Shell Guard", "Hides behind a barnacled shell."))
    )
    val BUBBLE_SLIME = MonsterCard(
        id = "bubble_slime",
        name = "Bubble Slime",
        hp = 92,
        attack = 12,
        rarity = Rarity.Common,
        cost = 120,
        spriteKey = "bubble_slime",
        element = Element.Water,
        spellIds = listOf("war_cry"),
        skills = listOf(MonsterSkill("Bubble Wrap", "Absorbs blows in a wobbling bubble."))
    )
    val MURLOC = MonsterCard(
        id = "murloc",
        name = "Murloc",
        hp = 58,
        attack = 18,
        rarity = Rarity.Common,
        cost = 120,
        spriteKey = "murloc",
        element = Element.Water,
        spellIds = listOf("bone_shard"),
        skills = listOf(MonsterSkill("Fish Spear", "Pokes enemies with a tiny, angry spear."))
    )
    val ZOMBIE = MonsterCard(
        id = "zombie",
        name = "Zombie",
        hp = 92,
        attack = 12,
        rarity = Rarity.Common,
        cost = 120,
        spriteKey = "zombie",
        element = Element.Death,
        spellIds = listOf("war_cry"),
        skills = listOf(MonsterSkill("Shamble", "Keeps coming no matter what."))
    )
    val GHOST = MonsterCard(
        id = "ghost",
        name = "Ghost",
        hp = 54,
        attack = 19,
        rarity = Rarity.Common,
        cost = 120,
        spriteKey = "ghost",
        element = Element.Death,
        spellIds = listOf("dark_bolt"),
        skills = listOf(MonsterSkill("Wail", "A chilling wail that saps an enemy."))
    )
    val HELLHOUND = MonsterCard(
        id = "hellhound",
        name = "Hellhound",
        hp = 75,
        attack = 23,
        rarity = Rarity.Uncommon,
        cost = 210,
        spriteKey = "hellhound",
        element = Element.Fire,
        spellIds = listOf("bone_shard"),
        skills = listOf(MonsterSkill("Lava Bite", "A burning bite that tears through armor."))
    )
    val FLAME_CULTIST = MonsterCard(
        id = "flame_cultist",
        name = "Flame Cultist",
        hp = 70,
        attack = 24,
        rarity = Rarity.Uncommon,
        cost = 210,
        spriteKey = "flame_cultist",
        element = Element.Fire,
        spellIds = listOf("flame_arrow"),
        skills = listOf(MonsterSkill("Fireball", "Hurls a ball of fire at an enemy."))
    )
    val FOREST_FOX = MonsterCard(
        id = "forest_fox",
        name = "Forest Fox",
        hp = 75,
        attack = 23,
        rarity = Rarity.Uncommon,
        cost = 210,
        spriteKey = "forest_fox",
        element = Element.Nature,
        spellIds = listOf("bone_shard"),
        skills = listOf(MonsterSkill("Leaf Dash", "Darts in and out with a flurry of leaves."))
    )
    val TREANT_SAPLING = MonsterCard(
        id = "treant_sapling",
        name = "Treant Sapling",
        hp = 119,
        attack = 15,
        rarity = Rarity.Uncommon,
        cost = 210,
        spriteKey = "treant_sapling",
        element = Element.Nature,
        spellIds = listOf("war_cry"),
        skills = listOf(MonsterSkill("Bark Skin", "Hardens its bark against blows."))
    )
    val ELF_RANGER = MonsterCard(
        id = "elf_ranger",
        name = "Elf Ranger",
        hp = 79,
        attack = 21,
        rarity = Rarity.Uncommon,
        cost = 210,
        spriteKey = "elf_ranger",
        element = Element.Nature,
        spellIds = listOf("arrow_volley"),
        skills = listOf(MonsterSkill("Vine Arrow", "An arrow that never misses its mark."))
    )
    val BOAR_RIDER = MonsterCard(
        id = "boar_rider",
        name = "Boar Rider",
        hp = 92,
        attack = 19,
        rarity = Rarity.Uncommon,
        cost = 210,
        spriteKey = "boar_rider",
        element = Element.Nature,
        spellIds = listOf("war_cry"),
        skills = listOf(MonsterSkill("Tusk Charge", "Charges into the enemy line."))
    )
    val FROST_WOLF = MonsterCard(
        id = "frost_wolf",
        name = "Frost Wolf",
        hp = 75,
        attack = 23,
        rarity = Rarity.Uncommon,
        cost = 210,
        spriteKey = "frost_wolf",
        element = Element.Water,
        spellIds = listOf("bone_shard"),
        skills = listOf(MonsterSkill("Frost Fang", "An icy bite that slows its prey."))
    )
    val ICE_ARCHER = MonsterCard(
        id = "ice_archer",
        name = "Ice Archer",
        hp = 79,
        attack = 21,
        rarity = Rarity.Uncommon,
        cost = 210,
        spriteKey = "ice_archer",
        element = Element.Water,
        spellIds = listOf("arrow_volley"),
        skills = listOf(MonsterSkill("Ice Arrow", "A frozen arrow shot from afar."))
    )
    val GHOUL = MonsterCard(
        id = "ghoul",
        name = "Ghoul",
        hp = 75,
        attack = 23,
        rarity = Rarity.Uncommon,
        cost = 210,
        spriteKey = "ghoul",
        element = Element.Death,
        spellIds = listOf("bone_shard"),
        skills = listOf(MonsterSkill("Rending Claws", "Rips into an enemy with filthy claws."))
    )
    val BANSHEE = MonsterCard(
        id = "banshee",
        name = "Banshee",
        hp = 70,
        attack = 24,
        rarity = Rarity.Uncommon,
        cost = 210,
        spriteKey = "banshee",
        element = Element.Death,
        spellIds = listOf("dark_bolt"),
        skills = listOf(MonsterSkill("Soul Scream", "A scream that hurts every enemy."))
    )
    val SALAMANDER = MonsterCard(
        id = "salamander",
        name = "Salamander",
        hp = 89,
        attack = 28,
        rarity = Rarity.Rare,
        cost = 320,
        spriteKey = "salamander",
        element = Element.Fire,
        spellIds = listOf("bone_shard"),
        skills = listOf(MonsterSkill("Blazing Slash", "A curved blade wreathed in fire."))
    )
    val EFREET = MonsterCard(
        id = "efreet",
        name = "Efreet",
        hp = 84,
        attack = 29,
        rarity = Rarity.Rare,
        cost = 320,
        spriteKey = "efreet",
        element = Element.Fire,
        spellIds = listOf("flame_arrow", "dark_bolt"),
        skills = listOf(MonsterSkill("Firestorm", "Engulfs all enemies in flame."))
    )
    val MAGMA_GOLEM = MonsterCard(
        id = "magma_golem",
        name = "Magma Golem",
        hp = 142,
        attack = 18,
        rarity = Rarity.Rare,
        cost = 320,
        spriteKey = "magma_golem",
        element = Element.Fire,
        spellIds = listOf("war_cry"),
        skills = listOf(MonsterSkill("Molten Fists", "Crushing blows of living magma."))
    )
    val CENTAUR_HUNTRESS = MonsterCard(
        id = "centaur_huntress",
        name = "Centaur Huntress",
        hp = 94,
        attack = 26,
        rarity = Rarity.Rare,
        cost = 320,
        spriteKey = "centaur_huntress",
        element = Element.Nature,
        spellIds = listOf("arrow_volley"),
        skills = listOf(MonsterSkill("Hunter's Volley", "Rains arrows on the enemy line."))
    )
    val THORN_BEAST = MonsterCard(
        id = "thorn_beast",
        name = "Thorn Beast",
        hp = 142,
        attack = 18,
        rarity = Rarity.Rare,
        cost = 320,
        spriteKey = "thorn_beast",
        element = Element.Nature,
        spellIds = listOf("war_cry"),
        skills = listOf(MonsterSkill("Thorn Hide", "Enemies that strike it get pricked."))
    )
    val NAGA_SIREN = MonsterCard(
        id = "naga_siren",
        name = "Naga Siren",
        hp = 100,
        attack = 21,
        rarity = Rarity.Rare,
        cost = 320,
        spriteKey = "naga_siren",
        element = Element.Water,
        spellIds = listOf("mend"),
        skills = listOf(MonsterSkill("Tidal Song", "A song of the sea that heals allies."))
    )
    val YETI = MonsterCard(
        id = "yeti",
        name = "Yeti",
        hp = 142,
        attack = 18,
        rarity = Rarity.Rare,
        cost = 320,
        spriteKey = "yeti",
        element = Element.Water,
        spellIds = listOf("war_cry"),
        skills = listOf(MonsterSkill("Avalanche Roar", "A roar that shakes the battlefield."))
    )
    val VAMPIRE = MonsterCard(
        id = "vampire",
        name = "Vampire",
        hp = 89,
        attack = 28,
        rarity = Rarity.Rare,
        cost = 320,
        spriteKey = "vampire",
        element = Element.Death,
        spellIds = listOf("bone_shard"),
        skills = listOf(MonsterSkill("Blood Drain", "Drains an enemy's life."))
    )
    val MINOTAUR = MonsterCard(
        id = "minotaur",
        name = "Minotaur",
        hp = 142,
        attack = 28,
        rarity = Rarity.Epic,
        cost = 520,
        spriteKey = "minotaur",
        element = Element.Fire,
        spellIds = listOf("war_cry"),
        skills = listOf(MonsterSkill("Labrys Cleave", "A massive axe swing through enemies."))
    )
    val CENTAUR = MonsterCard(
        id = "centaur",
        name = "Centaur Warden",
        hp = 122,
        attack = 31,
        rarity = Rarity.Epic,
        cost = 520,
        spriteKey = "centaur",
        element = Element.Nature,
        spellIds = listOf("arrow_volley"),
        skills = listOf(MonsterSkill("Storm of Arrows", "A storm of arrows on all enemies."))
    )
    val ANCIENT_TREANT = MonsterCard(
        id = "ancient_treant",
        name = "Ancient Treant",
        hp = 182,
        attack = 22,
        rarity = Rarity.Epic,
        cost = 520,
        spriteKey = "ancient_treant",
        element = Element.Nature,
        spellIds = listOf("war_cry", "mend"),
        skills = listOf(MonsterSkill("Forest's Embrace", "Roots shield and heal the party."))
    )
    val FROST_GIANT = MonsterCard(
        id = "frost_giant",
        name = "Frost Giant",
        hp = 182,
        attack = 22,
        rarity = Rarity.Epic,
        cost = 520,
        spriteKey = "frost_giant",
        element = Element.Water,
        spellIds = listOf("war_cry", "mend"),
        skills = listOf(MonsterSkill("Glacier Club", "A crushing blow of solid ice."))
    )
    val DEATH_KNIGHT = MonsterCard(
        id = "death_knight",
        name = "Death Knight",
        hp = 142,
        attack = 28,
        rarity = Rarity.Epic,
        cost = 520,
        spriteKey = "death_knight",
        element = Element.Death,
        spellIds = listOf("war_cry"),
        skills = listOf(MonsterSkill("Runeblade", "A cursed blade that cuts deep."))
    )
    val LICH = MonsterCard(
        id = "lich",
        name = "Lich",
        hp = 108,
        attack = 35,
        rarity = Rarity.Epic,
        cost = 520,
        spriteKey = "lich",
        element = Element.Death,
        spellIds = listOf("arrow_volley", "dark_bolt"),
        skills = listOf(MonsterSkill("Soul Fire", "Ghostly flames burn all enemies."))
    )
    val PHOENIX = MonsterCard(
        id = "phoenix",
        name = "Phoenix",
        hp = 132,
        attack = 42,
        rarity = Rarity.Legendary,
        cost = 850,
        spriteKey = "phoenix",
        element = Element.Fire,
        spellIds = listOf("flame_arrow", "mend"),
        skills = listOf(MonsterSkill("Rebirth Flames", "Flames that burn foes and mend allies."))
    )

    /** Kompani drugiej fali - osobno, bo [EnemySpecies] bierze z nich wrogow do Rare wlacznie. */
    val secondWave: List<MonsterCard> = listOf(
        EMBER_IMP,
        FIRE_BAT,
        LAVA_SLIME,
        GOBLIN_SPEARMAN,
        MUSHROOM_SPRITE,
        TIDECRAWLER,
        BUBBLE_SLIME,
        MURLOC,
        ZOMBIE,
        GHOST,
        HELLHOUND,
        FLAME_CULTIST,
        FOREST_FOX,
        TREANT_SAPLING,
        ELF_RANGER,
        BOAR_RIDER,
        FROST_WOLF,
        ICE_ARCHER,
        GHOUL,
        BANSHEE,
        SALAMANDER,
        EFREET,
        MAGMA_GOLEM,
        CENTAUR_HUNTRESS,
        THORN_BEAST,
        NAGA_SIREN,
        YETI,
        VAMPIRE,
        MINOTAUR,
        CENTAUR,
        ANCIENT_TREANT,
        FROST_GIANT,
        DEATH_KNIGHT,
        LICH,
        PHOENIX
    )

    val all: List<MonsterCard> = listOf(
        SKELETON_WARRIOR,
        SKELETON_ARCHER,
        SKELETON_MAGE,
        ORC_WARRIOR,
        ORC_ARCHER,
        HUMAN_ARCHER
    ) + secondWave

    fun byId(id: String): MonsterCard? = all.firstOrNull { it.id == id }
}
