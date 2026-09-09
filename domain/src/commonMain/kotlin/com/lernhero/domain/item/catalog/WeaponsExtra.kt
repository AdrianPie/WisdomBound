package com.lernhero.domain.item.catalog

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.combat.effects.BurnOnHitEffect
import com.lernhero.domain.combat.effects.CriticalChanceEffect
import com.lernhero.domain.combat.effects.CriticalDamageEffect
import com.lernhero.domain.combat.effects.DeathExplosionEffect
import com.lernhero.domain.combat.effects.DoubleAttackChanceEffect
import com.lernhero.domain.combat.effects.ElementBaneEffect
import com.lernhero.domain.combat.effects.ExtraAttackOnKillEffect
import com.lernhero.domain.combat.effects.FreezeOnCriticalEffect
import com.lernhero.domain.combat.effects.LifestealEffect
import com.lernhero.domain.combat.effects.MultiTargetAttackEffect
import com.lernhero.domain.combat.effects.PoisonOnHitEffect
import com.lernhero.domain.combat.effects.SpeedEffect
import com.lernhero.domain.combat.effects.WarHornEffect
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.monster.Element

/**
 * Druga partia broni bohatera - to ona zamienia kuznie z listy czterech pozycji w realny wybor.
 *
 * Trzy zasady, wedlug ktorych to jest ulozone:
 *
 * 1. **Kazda bron ma ROLE, nie tylko liczbe.** "Wiecej ataku niz poprzednia" to nie jest wybor.
 *    Dlatego kazdy prog ma komplet archetypow: jednocelowa (najwyzszy atak), wielocelowa (mniej
 *    ataku, wiecej celow), krytyczna (niski atak, wysoki sufit), obrazenia w czasie (trucizna/ogien)
 *    i wsparcie (lifesteal, oslabianie wroga).
 * 2. **Efekt kosztuje statystyki.** Bron z mocnym efektem ma wyraznie nizszy atak od "czystej"
 *    broni z tego samego progu - inaczej efekty bylyby darmowe i wybor znowu znikalby.
 * 3. **Bronie na zywiol ([ElementBaneEffect]) sa TANSZE i slabsze bazowo**, bo ich premia dziala
 *    tylko czesc walki. To sprzet, ktory sie podmienia pod krainy, a nie nosi caly czas.
 */
object WeaponsExtra {

    private fun weapon(
        id: String,
        name: String,
        description: String,
        rarity: Rarity,
        iconKey: String,
        attack: Float,
        cost: Int,
        extraStats: Stats = Stats.Empty,
        effects: List<com.lernhero.domain.combat.Effect> = emptyList(),
        slot: ItemSlot = ItemSlot.MainHand
    ) = Item(
        id = id,
        name = name,
        description = description,
        slot = slot,
        rarity = rarity,
        iconKey = iconKey,
        stats = Stats.of(StatType.Attack to attack) + extraStats,
        effects = effects,
        cost = cost
    )

    /* ------------------------------- Rough (Common) ------------------------------- */

    val RUSTY_AXE = weapon(
        id = "rusty_axe",
        name = "Rusty Axe",
        description = "Heavy and blunt. Hits one target, but hits it properly.",
        rarity = Rarity.Common,
        iconKey = "axe",
        attack = 7f,
        cost = 110
    )

    val HUNTING_KNIFE = weapon(
        id = "hunting_knife",
        name = "Hunting Knife",
        description = "Light enough to strike before anyone else does.",
        rarity = Rarity.Common,
        iconKey = "knife",
        attack = 4f,
        cost = 95,
        extraStats = Stats.of(StatType.Speed to 3f),
        effects = listOf(CriticalChanceEffect(percent = 0.08f))
    )

    val OAK_CLUB = weapon(
        id = "oak_club",
        name = "Oak Club",
        description = "No edge, no finesse. Drains a little life with every bruise.",
        rarity = Rarity.Common,
        iconKey = "club",
        attack = 5f,
        cost = 100,
        effects = listOf(LifestealEffect(percent = 0.08f))
    )

    /* ------------------------------ Sturdy (Uncommon) ----------------------------- */

    val SOLDIERS_SPEAR = weapon(
        id = "soldiers_spear",
        name = "Soldier's Spear",
        description = "Reach beats strength: catches two enemies in one thrust.",
        rarity = Rarity.Uncommon,
        iconKey = "spear",
        attack = 6f,
        cost = 175,
        effects = listOf(MultiTargetAttackEffect(targets = 2))
    )

    val BRONZE_WARHAMMER = weapon(
        id = "bronze_warhammer",
        name = "Bronze Warhammer",
        description = "Slow, but every hit lands like a verdict.",
        rarity = Rarity.Uncommon,
        iconKey = "hammer",
        attack = 9f,
        cost = 190,
        extraStats = Stats.of(StatType.Speed to -2f)
    )

    val TORCHBLADE = weapon(
        id = "torchblade",
        name = "Torchblade",
        description = "A blade wrapped in pitch. Sets the wound alight.",
        rarity = Rarity.Uncommon,
        iconKey = "sword",
        attack = 5f,
        cost = 185,
        effects = listOf(BurnOnHitEffect(potency = 4f, turns = 3, chance = 0.6f))
    )

    val SILVER_SABRE = weapon(
        id = "silver_sabre",
        name = "Silver Sabre",
        description = "Silvered edge. The restless dead fear it more than anyone living.",
        rarity = Rarity.Uncommon,
        iconKey = "sabre",
        attack = 5f,
        cost = 165,
        effects = listOf(ElementBaneEffect(against = Element.Death, bonus = 0.35f))
    )

    /* -------------------------------- Fine (Rare) --------------------------------- */

    val TWIN_FANGS = weapon(
        id = "twin_fangs",
        name = "Twin Fangs",
        description = "Paired blades. Often the second one arrives before the enemy recovers.",
        rarity = Rarity.Rare,
        iconKey = "daggers",
        attack = 6f,
        cost = 310,
        extraStats = Stats.of(StatType.Speed to 3f),
        effects = listOf(DoubleAttackChanceEffect(chance = 0.25f))
    )

    val EXECUTIONERS_AXE = weapon(
        id = "executioners_axe",
        name = "Executioner's Axe",
        description = "Made for finishing. A kill leaves it swinging for the next one.",
        rarity = Rarity.Rare,
        iconKey = "axe",
        attack = 10f,
        cost = 330,
        effects = listOf(ExtraAttackOnKillEffect(chance = 0.6f))
    )

    val FROSTBRAND = weapon(
        id = "frostbrand",
        name = "Frostbrand",
        description = "A critical hit locks the target in ice for a turn.",
        rarity = Rarity.Rare,
        iconKey = "sword",
        attack = 7f,
        cost = 300,
        effects = listOf(
            CriticalChanceEffect(percent = 0.12f),
            FreezeOnCriticalEffect(turns = 1)
        )
    )

    val TIDE_TRIDENT = weapon(
        id = "tide_trident",
        name = "Tide Trident",
        description = "Forged against things that swim. Poor against everything else.",
        rarity = Rarity.Rare,
        iconKey = "trident",
        attack = 7f,
        cost = 275,
        effects = listOf(ElementBaneEffect(against = Element.Water, bonus = 0.45f))
    )

    val BLIGHT_SICKLE = weapon(
        id = "blight_sickle",
        name = "Blight Sickle",
        description = "The cut is shallow. The rot that follows is not.",
        rarity = Rarity.Rare,
        iconKey = "sickle",
        attack = 4f,
        cost = 290,
        effects = listOf(PoisonOnHitEffect(potency = 9f, turns = 4, chance = 0.7f))
    )

    /* ------------------------------- Master (Epic) -------------------------------- */

    val STORMPIERCER = weapon(
        id = "stormpiercer",
        name = "Stormpiercer",
        description = "Strikes twice as often as it should, and faster than it should.",
        rarity = Rarity.Epic,
        iconKey = "spear",
        attack = 9f,
        cost = 470,
        effects = listOf(
            DoubleAttackChanceEffect(chance = 0.35f),
            SpeedEffect(amount = 4f)
        )
    )

    val SUNDERING_MAUL = weapon(
        id = "sundering_maul",
        name = "Sundering Maul",
        description = "Nothing clever. The single hardest hit in the Forge.",
        rarity = Rarity.Epic,
        iconKey = "hammer",
        attack = 14f,
        cost = 500,
        extraStats = Stats.of(StatType.Speed to -3f),
        effects = listOf(CriticalDamageEffect(multiplier = 0.4f))
    )

    val PLAGUE_CENSER = weapon(
        id = "plague_censer",
        name = "Plague Censer",
        description = "Poisons on contact, and the dying spread it to whoever stood too close.",
        rarity = Rarity.Epic,
        iconKey = "censer",
        attack = 6f,
        cost = 460,
        effects = listOf(
            PoisonOnHitEffect(potency = 11f, turns = 4),
            DeathExplosionEffect(damage = 18f)
        )
    )

    val WITHERING_CROOK = weapon(
        id = "withering_crook",
        name = "Withering Crook",
        description = "Saps the enemy line before the first blow lands.",
        rarity = Rarity.Epic,
        iconKey = "staff",
        attack = 7f,
        cost = 440,
        extraStats = Stats.of(StatType.SpellPower to 0.2f),
        effects = listOf(
            WarHornEffect(stat = StatType.Attack, potency = 5f, turns = 4),
            LifestealEffect(percent = 0.18f)
        )
    )

    /* ----------------------------- Mythic (Legendary) ----------------------------- */

    val DAWNBREAKER = weapon(
        id = "dawnbreaker",
        name = "Dawnbreaker",
        description = "Cuts three at once and burns whatever survives the swing.",
        rarity = Rarity.Legendary,
        iconKey = "greatsword",
        attack = 13f,
        cost = 720,
        effects = listOf(
            MultiTargetAttackEffect(targets = 3),
            BurnOnHitEffect(potency = 10f, turns = 3)
        )
    )

    val THOUSAND_CUTS = weapon(
        id = "thousand_cuts",
        name = "Blade of a Thousand Cuts",
        description = "Half your strikes come in pairs, and every second one bleeds you back to health.",
        rarity = Rarity.Legendary,
        iconKey = "daggers",
        attack = 10f,
        cost = 780,
        extraStats = Stats.of(StatType.Speed to 5f),
        effects = listOf(
            DoubleAttackChanceEffect(chance = 0.5f),
            LifestealEffect(percent = 0.2f),
            CriticalChanceEffect(percent = 0.1f)
        )
    )

    val WORLDROOT_STAFF = weapon(
        id = "worldroot_staff",
        name = "Worldroot Staff",
        description = "Old wood, older grudge. Nothing that grows stands well against it.",
        rarity = Rarity.Legendary,
        iconKey = "staff",
        attack = 9f,
        cost = 690,
        extraStats = Stats.of(StatType.SpellPower to 0.35f, StatType.HealPower to 0.2f),
        effects = listOf(
            ElementBaneEffect(against = Element.Nature, bonus = 0.6f),
            ElementBaneEffect(against = Element.Fire, bonus = 0.3f)
        )
    )

    val all: List<Item> = listOf(
        RUSTY_AXE, HUNTING_KNIFE, OAK_CLUB,
        SOLDIERS_SPEAR, BRONZE_WARHAMMER, TORCHBLADE, SILVER_SABRE,
        TWIN_FANGS, EXECUTIONERS_AXE, FROSTBRAND, TIDE_TRIDENT, BLIGHT_SICKLE,
        STORMPIERCER, SUNDERING_MAUL, PLAGUE_CENSER, WITHERING_CROOK,
        DAWNBREAKER, THOUSAND_CUTS, WORLDROOT_STAFF
    )
}
