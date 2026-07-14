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
import com.lernhero.domain.preset.Rarity

/**
 * Pancerz. Identyfikatory zostaly te same co w starym sklepie ("plate_helm", "plate_boots"...),
 * wiec zapisane stany graczy - lista posiadanych przedmiotow - dzialaja dalej bez migracji.
 */
object Armors {

    val PLATE_HELM = Item(
        id = "plate_helm",
        name = "Helm plytowy",
        description = "Ciezki helm. Pierwszy cios w walce odbija sie od niego bez szkody.",
        slot = ItemSlot.Head,
        rarity = Rarity.Uncommon,
        iconKey = "plate_head",
        stats = Stats.of(
            StatType.MaxHp to 12f,
            StatType.Defense to 2f
        ),
        effects = listOf(
            IgnoreFirstHitEffect()
        ),
        cost = 90
    )

    val PLATE_GAUNTLETS = Item(
        id = "plate_gauntlets",
        name = "Rekawice plytowe",
        description = "Stalowe rekawice, ktore odbieraja wrogowi zycie i oddaja je tobie.",
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

    val PLATE_LEGS = Item(
        id = "plate_legs",
        name = "Nagolenniki plytowe",
        description = "Solidne ochraniacze nog. Wiecej zdrowia, mniej ryzyka.",
        slot = ItemSlot.Legs,
        rarity = Rarity.Common,
        iconKey = "plate_legs",
        stats = Stats.of(
            StatType.MaxHp to 10f,
            StatType.Defense to 2f
        ),
        cost = 90
    )

    val PLATE_BOOTS = Item(
        id = "plate_boots",
        name = "Buty plytowe",
        description = "Wzmocnione buty bojowe. Ruszasz do walki pierwszy.",
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
        name = "Tarcza Egidy",
        description = "Odbija polowe otrzymanych obrazen, a gdy jest zle - stawia bariere.",
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

    val all: List<Item> = listOf(PLATE_HELM, PLATE_GAUNTLETS, PLATE_LEGS, PLATE_BOOTS, AEGIS_SHIELD)
}
