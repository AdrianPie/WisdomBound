package com.lernhero.domain.item.catalog

import com.lernhero.domain.combat.Effect
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.combat.effects.CleanseAlliesEffect
import com.lernhero.domain.combat.effects.DamageReductionEffect
import com.lernhero.domain.combat.effects.DodgeChanceEffect
import com.lernhero.domain.combat.effects.FrostThornsEffect
import com.lernhero.domain.combat.effects.IgnoreFirstHitEffect
import com.lernhero.domain.combat.effects.LastStandEffect
import com.lernhero.domain.combat.effects.LifestealEffect
import com.lernhero.domain.combat.effects.ReflectDamageEffect
import com.lernhero.domain.combat.effects.SpeedEffect
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot

/**
 * Druga partia pancerza. Powstala glownie po to, zeby ZAPELNIC SLOTY - stary katalog mial po
 * jednym przedmiocie na glowe, korpus, nogi, stopy i tarcze, wiec "wybor pancerza" nie istnial:
 * albo mialo sie ten jeden, albo nic.
 *
 * Pancerz w tej grze nie ma byc biernym workiem HP, wiec kazdy prog dostaje trzy rozne odpowiedzi
 * na to samo pytanie "jak nie umrzec":
 *
 * - **twardosc** - najwiecej HP i obrony, zero sztuczek,
 * - **unik** - malo HP, za to szansa, ze cios w ogole nie trafi,
 * - **odwet** - srednio wszystkiego, ale atakujacy placi za kazdy cios.
 *
 * Te trzy drogi celowo skaluja sie inaczej z reszta buildu: unik lubi szybkosc, odwet lubi duze HP.
 */
object ArmorsExtra {

    private fun armor(
        id: String,
        name: String,
        description: String,
        slot: ItemSlot,
        rarity: Rarity,
        iconKey: String,
        cost: Int,
        hp: Float = 0f,
        defense: Float = 0f,
        extraStats: Stats = Stats.Empty,
        effects: List<Effect> = emptyList()
    ) = Item(
        id = id,
        name = name,
        description = description,
        slot = slot,
        rarity = rarity,
        iconKey = iconKey,
        stats = Stats.of(StatType.MaxHp to hp, StatType.Defense to defense) + extraStats,
        effects = effects,
        cost = cost
    )

    /* ------------------------------- Rough (Common) ------------------------------- */

    val LEATHER_JERKIN = armor(
        id = "leather_jerkin",
        name = "Leather Jerkin",
        description = "Cheap, light, and better than the shirt you were wearing.",
        slot = ItemSlot.Chest,
        rarity = Rarity.Common,
        iconKey = "jerkin",
        cost = 95,
        hp = 12f,
        defense = 1f
    )

    val WORN_BUCKLER = armor(
        id = "worn_buckler",
        name = "Worn Buckler",
        description = "Small shield, small comfort - but it does turn a blade aside now and then.",
        slot = ItemSlot.OffHand,
        rarity = Rarity.Common,
        iconKey = "buckler",
        cost = 100,
        hp = 6f,
        defense = 2f,
        effects = listOf(DodgeChanceEffect(percent = 0.05f))
    )

    val LEATHER_BOOTS = armor(
        id = "leather_boots",
        name = "Leather Boots",
        description = "Soft soles, quick feet. Nobody hears you coming.",
        slot = ItemSlot.Feet,
        rarity = Rarity.Common,
        iconKey = "boots",
        cost = 85,
        hp = 5f,
        defense = 1f,
        effects = listOf(SpeedEffect(amount = 3f))
    )

    /* ------------------------------ Sturdy (Uncommon) ----------------------------- */

    val IRON_GAUNTLETS = armor(
        id = "iron_gauntlets",
        name = "Iron Gauntlets",
        description = "Heavy knuckles. Every block turns into a small punch back.",
        slot = ItemSlot.Hands,
        rarity = Rarity.Uncommon,
        iconKey = "plate_hands",
        cost = 170,
        hp = 10f,
        defense = 2f,
        effects = listOf(ReflectDamageEffect(percent = 0.1f))
    )

    val CHAIN_HAUBERK = armor(
        id = "chain_hauberk",
        name = "Chain Hauberk",
        description = "Rings on rings. Slows you down, keeps you alive.",
        slot = ItemSlot.Chest,
        rarity = Rarity.Uncommon,
        iconKey = "chainmail",
        cost = 180,
        hp = 20f,
        defense = 3f,
        extraStats = Stats.of(StatType.Speed to -2f)
    )

    val SCOUTS_BOOTS = armor(
        id = "scouts_boots",
        name = "Scout's Boots",
        description = "Made for moving first and being somewhere else when it lands.",
        slot = ItemSlot.Feet,
        rarity = Rarity.Uncommon,
        iconKey = "boots",
        cost = 165,
        hp = 6f,
        defense = 1f,
        effects = listOf(SpeedEffect(amount = 6f), DodgeChanceEffect(percent = 0.08f))
    )

    val SPIKED_PAULDRONS = armor(
        id = "spiked_pauldrons",
        name = "Spiked Pauldrons",
        description = "Hitting you is a decision with consequences.",
        slot = ItemSlot.Hands,
        rarity = Rarity.Uncommon,
        iconKey = "pauldrons",
        cost = 190,
        hp = 14f,
        defense = 2f,
        effects = listOf(ReflectDamageEffect(percent = 0.2f))
    )

    /* -------------------------------- Fine (Rare) --------------------------------- */

    val WARDENS_PLATE = armor(
        id = "wardens_plate",
        name = "Warden's Plate",
        description = "The straightforward answer: be too thick to break.",
        slot = ItemSlot.Chest,
        rarity = Rarity.Rare,
        iconKey = "plate",
        cost = 320,
        hp = 30f,
        defense = 4f,
        effects = listOf(DamageReductionEffect(percent = 0.1f))
    )

    val BLESSED_SHIELD = armor(
        id = "blessed_shield",
        name = "Blessed Shield",
        description = "A temple blessing in steel. Blows land softer behind it.",
        slot = ItemSlot.OffHand,
        rarity = Rarity.Rare,
        iconKey = "shield",
        cost = 310,
        hp = 24f,
        defense = 4f,
        effects = listOf(DamageReductionEffect(percent = 0.08f))
    )

    /* ------------------------------- Master (Epic) -------------------------------- */

    val DARKSTEEL_ARMOR = armor(
        id = "darksteel_armor",
        name = "Darksteel Armor",
        description = "Forged in the dark and sharp at every edge. Hit it and bleed.",
        slot = ItemSlot.Chest,
        rarity = Rarity.Epic,
        iconKey = "plate",
        cost = 500,
        hp = 32f,
        defense = 5f,
        effects = listOf(ReflectDamageEffect(percent = 0.15f), DamageReductionEffect(percent = 0.08f))
    )

    val BULWARK_OF_DAWN = armor(
        id = "bulwark_of_dawn",
        name = "Bulwark of Dawn",
        description = "Absorbs the opening blow, then holds the line through the rest.",
        slot = ItemSlot.OffHand,
        rarity = Rarity.Epic,
        iconKey = "shield",
        cost = 480,
        hp = 34f,
        defense = 5f,
        effects = listOf(IgnoreFirstHitEffect(), DamageReductionEffect(percent = 0.12f))
    )

    val REVENANT_MAIL = armor(
        id = "revenant_mail",
        name = "Revenant Mail",
        description = "Feeds on the fight. The longer it lasts, the better you do.",
        slot = ItemSlot.Chest,
        rarity = Rarity.Epic,
        iconKey = "mail",
        cost = 495,
        hp = 26f,
        defense = 4f,
        effects = listOf(LifestealEffect(percent = 0.15f), LastStandEffect(hpFraction = 0.25f, shield = 35f))
    )

    val PHANTOM_TREADS = armor(
        id = "phantom_treads",
        name = "Phantom Treads",
        description = "You are always a half-step ahead of where the blade expected you.",
        slot = ItemSlot.Feet,
        rarity = Rarity.Epic,
        iconKey = "boots",
        cost = 440,
        hp = 12f,
        defense = 2f,
        effects = listOf(DodgeChanceEffect(percent = 0.22f), SpeedEffect(amount = 8f))
    )

    /* ----------------------------- Mythic (Legendary) ----------------------------- */

    val TITANS_CARAPACE = armor(
        id = "titans_carapace",
        name = "Titan's Carapace",
        description = "Nothing subtle. You simply stop being something that can be killed quickly.",
        slot = ItemSlot.Chest,
        rarity = Rarity.Legendary,
        iconKey = "carapace",
        cost = 760,
        hp = 48f,
        defense = 7f,
        extraStats = Stats.of(StatType.Speed to -4f),
        effects = listOf(DamageReductionEffect(percent = 0.18f), IgnoreFirstHitEffect())
    )

    val all: List<Item> = listOf(
        LEATHER_JERKIN, WORN_BUCKLER, LEATHER_BOOTS,
        CHAIN_HAUBERK, SCOUTS_BOOTS, SPIKED_PAULDRONS, IRON_GAUNTLETS,
        WARDENS_PLATE, BLESSED_SHIELD,
        BULWARK_OF_DAWN, REVENANT_MAIL, PHANTOM_TREADS, DARKSTEEL_ARMOR,
        TITANS_CARAPACE
    )
}
