package com.lernhero.home.story

import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.run.ChestSlot
import com.lernhero.home.city.TownViewModel
import com.lernhero.home.player.PlayerViewModel
import kotlin.random.Random

/**
 * Konkretna nagroda za wylosowane pole kola. Pole mowi TYLKO "przedmiot rzadki" - dopiero tutaj
 * losuje sie, ktory to przedmiot (z prawdziwego [ItemCatalog]) albo ktory kompan, i tutaj wiadomo,
 * jak go przyznac. Gdy w danej klasie rzadkosci nie ma jeszcze zadnego przedmiotu, nagroda spada
 * do zlota - kolo nigdy nie oddaje pustego pola.
 */
class ChestPrize(
    val label: String,
    val grant: (PlayerViewModel, TownViewModel) -> Unit
)

fun resolveChestPrize(slot: ChestSlot, random: Random): ChestPrize {
    slot.itemRarities?.let { rarities ->
        val pool = ItemCatalog.all.filter { it.rarity in rarities }
        if (pool.isNotEmpty()) {
            val item = pool[random.nextInt(pool.size)]
            val fallbackGold = (item.cost / 2).coerceAtLeast(80)
            return ChestPrize(
                label = "${slot.emoji} ${item.name} (${item.rarity.name})",
                grant = { player, _ -> player.grantItem(item, fallbackGold) }
            )
        }
        val consolation = 120
        return ChestPrize(
            label = "${slot.emoji} The chest was empty - $consolation coins",
            grant = { player, _ -> player.grantGold(consolation, "+$consolation coins from the chest") }
        )
    }

    if (slot.ticketCount > 0) {
        val count = slot.ticketCount
        return ChestPrize(
            label = "${slot.emoji} ${slot.label}",
            grant = { _, town -> town.grantSummonTickets(count, "+$count 🎟️ from the boss chest") }
        )
    }

    val gold = slot.goldAmount
    return ChestPrize(
        label = "${slot.emoji} ${slot.label}",
        grant = { player, _ -> player.grantGold(gold, "+$gold coins from the boss chest") }
    )
}
