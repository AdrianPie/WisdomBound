package com.lernhero.domain.item.catalog

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.combat.effects.IgnoreFirstHitEffect
import com.lernhero.domain.combat.effects.LastStandEffect
import com.lernhero.domain.combat.effects.LifestealEffect
import com.lernhero.domain.combat.effects.ReflectDamageEffect
import com.lernhero.domain.combat.effects.SpeedEffect
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.common.Rarity

/**
 * Pancerz. Identyfikatory zostaly te same co w starym sklepie ("plate_helm", "plate_boots"...),
 * wiec zapisane stany graczy - lista posiadanych przedmiotow - dzialaja dalej bez migracji.
 */
object Armors {

    val PLATE_GAUNTLETS = Item(
        id = "plate_gauntlets",
        name = "Plate Gauntlets",
        description = "Steel gauntlets that drain the enemy's life and give it to you.",
        slot = ItemSlot.Hands,
        rarity = Rarity.Rare,
        iconKey = "plate_hands",
        stats = Stats.of(
            StatType.Attack to 3f,
            StatType.Defense to 2f
        ),
        effects = listOf(
            LifestealEffect(percent = 0.15f)
        ),
        cost = 110
    )

    val PLATE_BOOTS = Item(
        id = "plate_boots",
        name = "Plate Boots",
        description = "Reinforced combat boots. You strike first in battle.",
        slot = ItemSlot.Feet,
        rarity = Rarity.Common,
        iconKey = "plate_feet",
        stats = Stats.of(StatType.Defense to 2f),
        effects = listOf(
            SpeedEffect(amount = 5f)
        ),
        cost = 80
    )

    val AEGIS_SHIELD = Item(
        id = "aegis_shield",
        name = "Aegis Shield",
        description = "Reflects half of damage taken, and raises a barrier when things get dire.",
        slot = ItemSlot.OffHand,
        rarity = Rarity.Epic,
        iconKey = "shield",
        stats = Stats.of(
            StatType.Defense to 4f,
            StatType.MaxHp to 20f
        ),
        effects = listOf(
            ReflectDamageEffect(percent = 0.5f),
            LastStandEffect(hpFraction = 0.3f, shield = 30f)
        ),
        cost = 420
    )

    val all: List<Item> = listOf(PLATE_GAUNTLETS, PLATE_BOOTS, AEGIS_SHIELD)
}
