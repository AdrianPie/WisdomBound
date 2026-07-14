package com.lernhero.domain.item.catalog

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.combat.effects.AllyAuraEffect
import com.lernhero.domain.combat.effects.BattleCryEffect
import com.lernhero.domain.combat.effects.CooldownReductionEffect
import com.lernhero.domain.combat.effects.CriticalChanceEffect
import com.lernhero.domain.combat.effects.CriticalDamageEffect
import com.lernhero.domain.combat.effects.DodgeChanceEffect
import com.lernhero.domain.combat.effects.FreezeOnCriticalEffect
import com.lernhero.domain.combat.effects.FrostThornsEffect
import com.lernhero.domain.combat.effects.SpeedEffect
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.preset.Rarity

/**
 * Dodatki. Tu widac sile skladania: "pierscien krytyka" i "amulet mrozu" nie znaja sie nawzajem,
 * ale zalozone razem daja kombinacje - krytyk czesciej trafia, a kazdy krytyk zamraza.
 */
object Accessories {

    val RING_OF_FURY = Item(
        id = "ring_of_fury",
        name = "Pierscien furii",
        description = "Czesciej trafiasz krytycznie, a krytyki bola bardziej.",
        slot = ItemSlot.Ring,
        rarity = Rarity.Rare,
        iconKey = "ring",
        effects = listOf(
            CriticalChanceEffect(percent = 0.15f),
            CriticalDamageEffect(multiplier = 0.3f)
        ),
        cost = 240
    )

    val BAND_OF_HASTE = Item(
        id = "band_of_haste",
        name = "Obraczka pospiechu",
        description = "Szybsze tury i krotsze przerwy miedzy zakleciami.",
        slot = ItemSlot.Ring,
        rarity = Rarity.Uncommon,
        iconKey = "band",
        effects = listOf(
            SpeedEffect(amount = 3f),
            CooldownReductionEffect(turns = 1)
        ),
        cost = 180
    )

    val AMULET_OF_FROST = Item(
        id = "amulet_of_frost",
        name = "Amulet szronu",
        description = "Twoje krytyki zamrazaja, a napastnicy ryzykuja przymarzniecie do ziemi.",
        slot = ItemSlot.Amulet,
        rarity = Rarity.Epic,
        iconKey = "amulet_frost",
        stats = Stats.of(StatType.Dodge to 0.05f),
        effects = listOf(
            FreezeOnCriticalEffect(turns = 1),
            FrostThornsEffect(chance = 0.2f, turns = 1)
        ),
        cost = 400
    )

    val BANNER_OF_VALOR = Item(
        id = "banner_of_valor",
        name = "Sztandar mestwa",
        description = "Cala druzyna bije mocniej, a walka zaczyna sie od okrzyku bojowego.",
        slot = ItemSlot.Amulet,
        rarity = Rarity.Legendary,
        iconKey = "banner",
        effects = listOf(
            AllyAuraEffect(
                id = "aura_valor",
                description = "Sojusznicy zyskuja +4 ataku.",
                bonus = Stats.of(StatType.Attack to 4f)
            ),
            BattleCryEffect(stat = StatType.Attack, potency = 6f, turns = 3)
        ),
        cost = 650
    )

    val CLOAK_OF_SHADOWS = Item(
        id = "cloak_of_shadows",
        name = "Plaszcz cieni",
        description = "Cios, ktory cie nie trafi, nie zrobi ci krzywdy.",
        slot = ItemSlot.Chest,
        rarity = Rarity.Rare,
        iconKey = "cloak",
        stats = Stats.of(StatType.Speed to 2f),
        effects = listOf(
            DodgeChanceEffect(percent = 0.20f)
        ),
        cost = 300
    )

    val all: List<Item> = listOf(
        RING_OF_FURY,
        BAND_OF_HASTE,
        AMULET_OF_FROST,
        BANNER_OF_VALOR,
        CLOAK_OF_SHADOWS
    )
}
