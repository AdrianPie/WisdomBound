package com.lernhero.domain.item.catalog

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.combat.effects.BurnOnHitEffect
import com.lernhero.domain.combat.effects.CriticalChanceEffect
import com.lernhero.domain.combat.effects.ExtraAttackOnKillEffect
import com.lernhero.domain.combat.effects.HealAlliesEffect
import com.lernhero.domain.combat.effects.MultiTargetAttackEffect
import com.lernhero.domain.combat.effects.PoisonOnHitEffect
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.common.Rarity

/**
 * Bron. Kazdy wpis to czysty przepis: statystyki + efekty. Zero logiki.
 *
 * Zwroc uwage, ze "miecz bijacy dwoch" i "kosa bijaca trzech" uzywaja tego samego efektu
 * z innym parametrem - to jest cala roznica miedzy nimi.
 */
object Weapons {

    val PLATE_SWORD = Item(
        id = "plate_sword",
        name = "Plate Sword",
        description = "A sturdy double-edged sword. Reaches two enemies at once.",
        slot = ItemSlot.MainHand,
        rarity = Rarity.Common,
        iconKey = "sword",
        stats = Stats.of(StatType.Attack to 5f),
        effects = listOf(
            MultiTargetAttackEffect(targets = 2)
        ),
        cost = 100
    )

    val REAPER_GLAIVE = Item(
        id = "reaper_glaive",
        name = "Reaper's Glaive",
        description = "Cuts three enemies at once, and lets you strike again after a kill.",
        slot = ItemSlot.MainHand,
        rarity = Rarity.Epic,
        iconKey = "glaive",
        stats = Stats.of(StatType.Attack to 9f),
        effects = listOf(
            MultiTargetAttackEffect(targets = 3),
            ExtraAttackOnKillEffect()
        ),
        cost = 480
    )

    val VENOM_DAGGER = Item(
        id = "venom_dagger",
        name = "Venom Dagger",
        description = "Fast and treacherous. Poisons the wound, and the venom shows armor no mercy.",
        slot = ItemSlot.MainHand,
        rarity = Rarity.Rare,
        iconKey = "dagger",
        stats = Stats.of(
            StatType.Attack to 3f,
            StatType.Speed to 4f
        ),
        effects = listOf(
            CriticalChanceEffect(percent = 0.10f),
            PoisonOnHitEffect(potency = 6f, turns = 5, chance = 0.5f)
        ),
        cost = 260
    )

    val EMBER_STAFF = Item(
        id = "ember_staff",
        name = "Ember Staff",
        description = "Sets enemies alight and heals the three most wounded allies every other turn.",
        slot = ItemSlot.MainHand,
        rarity = Rarity.Rare,
        iconKey = "staff",
        stats = Stats.of(
            StatType.Attack to 2f,
            StatType.HealPower to 0.25f
        ),
        effects = listOf(
            BurnOnHitEffect(potency = 5f, turns = 3),
            HealAlliesEffect(targets = 3, amount = 14f, everyTurns = 2)
        ),
        cost = 320
    )

    val all: List<Item> = listOf(PLATE_SWORD, REAPER_GLAIVE, VENOM_DAGGER, EMBER_STAFF)
}
