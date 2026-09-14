package com.lernhero.domain.talent

import com.lernhero.domain.run.EndlessBuff

/**
 * Registry of every talent. Like the item or spell catalog: a new talent is one entry, not a
 * line in the engine - because every effect is built from mechanics the battle already handles.
 *
 * Each tree's layout is five rows of 2-3 nodes. A row opens after
 * [TalentRules.POINTS_PER_TIER] points spent in THAT tree, so reaching the very bottom costs
 * [TalentRules.POINTS_PER_TIER] * 4 = 12 points, roughly twelve hero levels.
 * The numbers are deliberately conservative - this is the first balance pass, not the last.
 */
object TalentCatalog {

    /* ============================== DESTRUCTION (DPS) ============================== */

    private val dps: List<Talent> = listOf(
        Talent(
            id = "dps_might",
            tree = TalentTree.Dps,
            tier = 0,
            column = 0,
            name = "Strength of Blows",
            description = "+8% party attack per rank.",
            icon = "💥",
            maxRank = 5,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Might))
        ),
        Talent(
            id = "dps_edge",
            tree = TalentTree.Dps,
            tier = 0,
            column = 2,
            name = "Honed Blade",
            description = "+6 party attack per rank.",
            icon = "🗡️",
            maxRank = 5,
            effects = listOf(TalentEffect.Attack(perRank = 6))
        ),

        Talent(
            id = "dps_precision",
            tree = TalentTree.Dps,
            tier = 1,
            column = 0,
            name = "Precision",
            description = "+8% critical hit chance per rank.",
            icon = "🎯",
            maxRank = 4,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Precision))
        ),
        Talent(
            id = "dps_battle_focus",
            tree = TalentTree.Dps,
            tier = 1,
            column = 1,
            name = "Battle Focus",
            description = "+3% damage from correct answers per rank.",
            icon = "🧠",
            maxRank = 3,
            effects = listOf(TalentEffect.QuizDamage(perRank = 0.03f))
        ),
        Talent(
            id = "dps_momentum",
            tree = TalentTree.Dps,
            tier = 1,
            column = 2,
            name = "Momentum",
            description = "+6% attack per correct answer in a row (up to +30%) per rank.",
            icon = "🌪️",
            maxRank = 2,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Momentum))
        ),

        Talent(
            id = "dps_executioner",
            tree = TalentTree.Dps,
            tier = 2,
            column = 0,
            name = "Executioner",
            description = "+35% damage to targets below 35% HP per rank.",
            icon = "🪓",
            maxRank = 2,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Executioner))
        ),
        Talent(
            id = "dps_cleave",
            tree = TalentTree.Dps,
            tier = 2,
            column = 1,
            name = "Cleave",
            description = "Every attack also hits an extra enemy for 60% damage.",
            icon = "🌊",
            maxRank = 2,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Cleave))
        ),
        Talent(
            id = "dps_brutality",
            tree = TalentTree.Dps,
            tier = 2,
            column = 2,
            name = "Brutality",
            description = "+40% damage from critical hits per rank.",
            icon = "💀",
            maxRank = 2,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Brutality)),
            requires = "dps_precision"
        ),

        Talent(
            id = "dps_multishot",
            tree = TalentTree.Dps,
            tier = 3,
            column = 0,
            name = "Double Shot",
            description = "The hero fires +1 extra projectile per attack per rank. Extra projectiles hit for 60%.",
            icon = "🏹",
            maxRank = 3,
            effects = listOf(TalentEffect.Buff(EndlessBuff.MultiShot))
        ),
        Talent(
            id = "dps_fireball",
            tree = TalentTree.Dps,
            tier = 3,
            column = 1,
            name = "Fireball",
            description = "15% of the hero's hits turn into a Fireball that sets the enemy alight.",
            icon = "🔥",
            maxRank = 1,
            effects = listOf(TalentEffect.UnlockSpell("fireball", castChancePercent = 15))
        ),
        Talent(
            id = "dps_frenzy",
            tree = TalentTree.Dps,
            tier = 3,
            column = 2,
            name = "Frenzy",
            description = "+1 extra exchange of blows per turn per rank.",
            icon = "😤",
            maxRank = 2,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Frenzy))
        ),

        Talent(
            id = "dps_volley",
            tree = TalentTree.Dps,
            tier = 4,
            column = 0,
            name = "Volley",
            description = "The WHOLE party fires +1 extra projectile per attack per rank. Extra projectiles hit for 60%.",
            icon = "☄️",
            maxRank = 2,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Volley))
        ),
        Talent(
            id = "dps_firebomb",
            tree = TalentTree.Dps,
            tier = 4,
            column = 2,
            name = "Firebomb",
            description = "12% of hits are a Firebomb that damages every enemy. Adds +10 attack.",
            icon = "🧨",
            maxRank = 1,
            effects = listOf(
                TalentEffect.UnlockSpell("firebomb", castChancePercent = 12),
                TalentEffect.Attack(perRank = 10)
            ),
            requires = "dps_fireball"
        )
    )

    /* ================================= BULWARK (TANK) ================================= */

    private val tank: List<Talent> = listOf(
        Talent(
            id = "tank_hide",
            tree = TalentTree.Tank,
            tier = 0,
            column = 0,
            name = "Thick Hide",
            description = "+25 max party HP per rank.",
            icon = "🧱",
            maxRank = 5,
            effects = listOf(TalentEffect.MaxHp(perRank = 25))
        ),
        Talent(
            id = "tank_fortress",
            tree = TalentTree.Tank,
            tier = 0,
            column = 2,
            name = "Fortress",
            description = "-7% damage taken per rank (max 60%). Stacks with Sanctuary.",
            icon = "🏰",
            maxRank = 5,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Fortress))
        ),

        Talent(
            id = "tank_vitality",
            tree = TalentTree.Tank,
            tier = 1,
            column = 0,
            name = "Vitality",
            description = "+10% max party HP per rank - and heals for the same amount instantly. Stacks with Breath of Life.",
            icon = "❤️",
            maxRank = 4,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Vitality))
        ),
        Talent(
            id = "tank_guard",
            tree = TalentTree.Tank,
            tier = 1,
            column = 1,
            name = "Guard",
            description = "Every hero blocks the first hit of the turn - no damage taken.",
            icon = "🛡️",
            maxRank = 2,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Guard))
        ),
        Talent(
            id = "tank_thorns",
            tree = TalentTree.Tank,
            tier = 1,
            column = 2,
            name = "Thorns",
            description = "The attacker takes 25% of the damage it dealt to the party. Stacks with Iron Shell.",
            icon = "🌵",
            maxRank = 3,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Thorns))
        ),

        Talent(
            id = "tank_stone",
            tree = TalentTree.Tank,
            tier = 2,
            column = 0,
            name = "Stone Physique",
            description = "+40 max party HP per rank.",
            icon = "🗿",
            maxRank = 3,
            effects = listOf(TalentEffect.MaxHp(perRank = 40))
        ),
        Talent(
            id = "tank_second_wind",
            tree = TalentTree.Tank,
            tier = 2,
            column = 1,
            name = "Second Wind",
            description = "Once per wave the hero survives a fatal hit with 25% HP. Stacks with Mercy.",
            icon = "🫀",
            maxRank = 2,
            effects = listOf(TalentEffect.Buff(EndlessBuff.SecondWind))
        ),
        Talent(
            id = "tank_frost",
            tree = TalentTree.Tank,
            tier = 2,
            column = 2,
            name = "Frost Shard",
            description = "12% of hits are a Frost Shard - damages and freezes the enemy for a turn.",
            icon = "❄️",
            maxRank = 1,
            effects = listOf(TalentEffect.UnlockSpell("frost_shard", castChancePercent = 12))
        ),

        Talent(
            id = "tank_riposte",
            tree = TalentTree.Tank,
            tier = 3,
            column = 0,
            name = "Riposte",
            description = "A blocked hit bounces back at the attacker for 150% of its damage.",
            icon = "⚡",
            maxRank = 1,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Riposte)),
            requires = "tank_guard"
        ),
        Talent(
            id = "tank_bulwark",
            tree = TalentTree.Tank,
            tier = 3,
            column = 1,
            name = "Bastion",
            description = "+30 max HP and +4 party attack per rank.",
            icon = "⛰️",
            maxRank = 3,
            effects = listOf(
                TalentEffect.MaxHp(perRank = 30),
                TalentEffect.Attack(perRank = 4)
            )
        ),
        Talent(
            id = "tank_spikes",
            tree = TalentTree.Tank,
            tier = 3,
            column = 2,
            name = "Iron Shell",
            description = "An extra stack of Thorns and +3% critical chance.",
            icon = "🦔",
            maxRank = 2,
            effects = listOf(
                TalentEffect.Buff(EndlessBuff.Thorns),
                TalentEffect.Crit(perRank = 0.03f)
            )
        ),

        Talent(
            id = "tank_aegis",
            tree = TalentTree.Tank,
            tier = 4,
            column = 1,
            name = "Aegis",
            description = "Every hero blocks the first hit of EVERY round, not just the turn.",
            icon = "🌟",
            maxRank = 1,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Aegis)),
            requires = "tank_riposte"
        ),
        Talent(
            id = "tank_colossus",
            tree = TalentTree.Tank,
            tier = 4,
            column = 2,
            name = "Colossus",
            description = "+20% attack AND +20% max HP for the whole party per rank. Stacks with Avatar of Light.",
            icon = "👑",
            maxRank = 2,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Avatar))
        )
    )

    /* =============================== LIGHT (HEALER) =============================== */

    private val healer: List<Talent> = listOf(
        Talent(
            id = "heal_vampirism",
            tree = TalentTree.Healer,
            tier = 0,
            column = 0,
            name = "Vampirism",
            description = "Heal for 5% of damage dealt per rank. Stacks with Transfusion.",
            icon = "🩸",
            maxRank = 4,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Vampirism))
        ),
        Talent(
            id = "heal_regeneration",
            tree = TalentTree.Healer,
            tier = 0,
            column = 2,
            name = "Regeneration",
            description = "After every wave cleared, the party heals 7% of max HP per rank. Stacks with Renewal.",
            icon = "🌿",
            maxRank = 4,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Regeneration))
        ),

        Talent(
            id = "heal_spell",
            tree = TalentTree.Healer,
            tier = 1,
            column = 0,
            name = "Healing Spell",
            description = "18% of hits become Healing - heals the most wounded ally.",
            icon = "💚",
            maxRank = 1,
            effects = listOf(TalentEffect.UnlockSpell("heal", castChancePercent = 18))
        ),
        Talent(
            id = "heal_blessing",
            tree = TalentTree.Healer,
            tier = 1,
            column = 1,
            name = "Blessing",
            description = "+30% to EVERY heal per rank.",
            icon = "🙏",
            maxRank = 2,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Blessing))
        ),
        Talent(
            id = "heal_vitality",
            tree = TalentTree.Healer,
            tier = 1,
            column = 2,
            name = "Breath of Life",
            description = "+10% max party HP per rank - and heals for the same amount instantly. Stacks with Vitality.",
            icon = "🌸",
            maxRank = 4,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Vitality))
        ),

        Talent(
            id = "heal_prayer",
            tree = TalentTree.Healer,
            tier = 2,
            column = 0,
            name = "Prayer of Focus",
            description = "+3% damage from correct answers per rank.",
            icon = "📿",
            maxRank = 3,
            effects = listOf(TalentEffect.QuizDamage(perRank = 0.03f))
        ),
        Talent(
            id = "heal_poison",
            tree = TalentTree.Healer,
            tier = 2,
            column = 1,
            name = "Poison Dart",
            description = "15% of hits are a Poison Dart - the poison ignores armor.",
            icon = "🧪",
            maxRank = 1,
            effects = listOf(TalentEffect.UnlockSpell("poison_dart", castChancePercent = 15))
        ),
        Talent(
            id = "heal_sanctuary",
            tree = TalentTree.Healer,
            tier = 2,
            column = 2,
            name = "Sanctuary",
            description = "+25 max party HP and -7% damage taken per rank. The reduction stacks with Fortress.",
            icon = "⛪",
            maxRank = 3,
            effects = listOf(
                TalentEffect.MaxHp(perRank = 25),
                TalentEffect.Buff(EndlessBuff.Fortress)
            )
        ),

        Talent(
            id = "heal_transfusion",
            tree = TalentTree.Healer,
            tier = 3,
            column = 0,
            name = "Transfusion",
            description = "An extra stack of Vampirism and +5 attack per rank.",
            icon = "🔮",
            maxRank = 2,
            effects = listOf(
                TalentEffect.Buff(EndlessBuff.Vampirism),
                TalentEffect.Attack(perRank = 5)
            ),
            requires = "heal_vampirism"
        ),
        Talent(
            id = "heal_second_wind",
            tree = TalentTree.Healer,
            tier = 3,
            column = 1,
            name = "Mercy",
            description = "Once per wave the hero survives a fatal hit with 25% HP. Stacks with Second Wind.",
            icon = "🕊️",
            maxRank = 2,
            effects = listOf(TalentEffect.Buff(EndlessBuff.SecondWind))
        ),
        Talent(
            id = "heal_renewal",
            tree = TalentTree.Healer,
            tier = 3,
            column = 2,
            name = "Renewal",
            description = "An extra stack of Regeneration and +20 max HP per rank.",
            icon = "🌱",
            maxRank = 2,
            effects = listOf(
                TalentEffect.Buff(EndlessBuff.Regeneration),
                TalentEffect.MaxHp(perRank = 20)
            )
        ),

        Talent(
            id = "heal_avatar",
            tree = TalentTree.Healer,
            tier = 4,
            column = 1,
            name = "Avatar of Light",
            description = "+20% attack AND +20% max HP for the whole party per rank. Stacks with Colossus.",
            icon = "😇",
            maxRank = 2,
            effects = listOf(TalentEffect.Buff(EndlessBuff.Avatar)),
            requires = "heal_blessing"
        )
    )

    /** All talents in one list - same order as the trees, row by row. */
    val all: List<Talent> = dps + tank + healer

    private val byId: Map<String, Talent> = all.associateBy { it.id }

    fun byId(id: String): Talent? = byId[id]

    fun forTree(tree: TalentTree): List<Talent> = all.filter { it.tree == tree }

    /**
     * One tree's talents grouped into rows - ready to draw as a grid.
     *
     * Sorting by key is done explicitly (`entries.sortedBy`), not via `toSortedMap` - the latter
     * is from java.util and doesn't exist on iOS, so it broke the Kotlin/Native build.
     */
    fun rowsOf(tree: TalentTree): List<List<Talent>> =
        forTree(tree)
            .groupBy { it.tier }
            .entries
            .sortedBy { it.key }
            .map { (_, row) -> row.sortedBy { it.column } }

    /** The deepest row in the tree - how many "floors" the grid has. */
    fun tierCount(tree: TalentTree): Int = (forTree(tree).maxOfOrNull { it.tier } ?: -1) + 1
}
