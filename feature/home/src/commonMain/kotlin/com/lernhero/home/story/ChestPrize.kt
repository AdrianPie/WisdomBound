package com.lernhero.home.story

import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.run.ChestSlot
import com.lernhero.home.city.TownViewModel
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.ui.icon
import kotlin.random.Random

/**
 * Konkretna nagroda za wylosowane pole. Pole mowi TYLKO "przedmiot rzadki" - dopiero tutaj losuje
 * sie, ktory to przedmiot (z prawdziwego [ItemCatalog]) albo ktory kompan, i tutaj wiadomo, jak go
 * przyznac. Gdy w danej klasie rzadkosci nie ma jeszcze zadnego przedmiotu, nagroda spada do zlota -
 * skrzynia nigdy nie jest pusta.
 *
 * [icon] i [name] to to, co wyskakuje ze skrzyni przy otwarciu - [label] to pelny podpis.
 */
class ChestPrize(
    val icon: String,
    val name: String,
    val label: String,
    val grant: (PlayerViewModel, TownViewModel) -> Unit,
    /** Wylosowany przedmiot - skrzynia pokazuje wtedy jego grafike zamiast [icon]. */
    val item: Item? = null
)

fun resolveChestPrize(slot: ChestSlot, random: Random): ChestPrize {
    slot.itemRarities?.let { rarities ->
        val pool = ItemCatalog.all.filter { it.rarity in rarities }
        if (pool.isNotEmpty()) {
            val item = pool[random.nextInt(pool.size)]
            val fallbackGold = (item.cost / 2).coerceAtLeast(80)
            return ChestPrize(
                icon = item.icon(),
                name = item.name,
                label = "${item.name} (${item.rarity.name})",
                grant = { player, _ -> player.grantItem(item, fallbackGold) },
                item = item
            )
        }
        val consolation = 120
        return ChestPrize(
            icon = "💰",
            name = "$consolation coins",
            label = "The chest held no item - $consolation coins instead",
            grant = { player, _ -> player.grantGold(consolation, "+$consolation coins from the chest") }
        )
    }

    if (slot.ticketCount > 0) {
        val count = slot.ticketCount
        return ChestPrize(
            icon = slot.emoji,
            name = if (count == 1) "1 summon ticket" else "$count summon tickets",
            label = slot.label,
            grant = { _, town -> town.grantSummonTickets(count, "+$count 🎟️ from the chest") }
        )
    }

    val gold = slot.goldAmount
    return ChestPrize(
        icon = slot.emoji,
        name = "$gold coins",
        label = slot.label,
        grant = { player, _ -> player.grantGold(gold, "+$gold coins from the chest") }
    )
}
