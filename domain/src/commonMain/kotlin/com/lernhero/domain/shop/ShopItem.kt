package com.lernhero.domain.shop

import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.item.ItemSlot

/**
 * Widok przedmiotu dla starego sklepu i ekranu ekwipunku.
 *
 * Nie trzyma juz wlasnych danych - jest rzutem [ItemCatalog] na te piec slotow, ktore obsluguje
 * dzisiejszy zapis gracza ([com.lernhero.domain.preset.PlayerEquipment] to wciaz piec flag).
 * Jedno zrodlo prawdy o przedmiotach siedzi w katalogu; tutaj tylko je przycinamy.
 */
data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val slot: ShopItemSlot,
    val cost: Int
)

enum class ShopItemSlot {
    Head,
    Hands,
    Legs,
    Feet,
    MainHand
}

object ShopCatalog {

    /**
     * Sklep pokazuje po jednym przedmiocie na slot, bo tyle unosi dzisiejszy model zapisu.
     * Gdy [com.lernhero.domain.preset.PlayerEquipment] przejdzie z flag na mape slot -> id,
     * wystarczy tu puscic caly [ItemCatalog] i sklep od razu zobaczy wszystkie bronie.
     */
    private val legacyIds = listOf(
        "plate_helm",
        "plate_gauntlets",
        "plate_legs",
        "plate_boots",
        "plate_sword"
    )

    val items: List<ShopItem> = legacyIds
        .mapNotNull { id -> ItemCatalog.byId(id) }
        .mapNotNull { item -> item.toShopItem() }

    fun itemForSlot(slot: ShopItemSlot): ShopItem? = items.firstOrNull { it.slot == slot }

    fun byId(id: String): ShopItem? = items.firstOrNull { it.id == id }
}

private fun Item.toShopItem(): ShopItem? {
    val shopSlot = slot.toShopSlot() ?: return null
    return ShopItem(
        id = id,
        name = name,
        description = description,
        slot = shopSlot,
        cost = cost
    )
}

private fun ItemSlot.toShopSlot(): ShopItemSlot? = when (this) {
    ItemSlot.Head -> ShopItemSlot.Head
    ItemSlot.Hands -> ShopItemSlot.Hands
    ItemSlot.Legs -> ShopItemSlot.Legs
    ItemSlot.Feet -> ShopItemSlot.Feet
    ItemSlot.MainHand -> ShopItemSlot.MainHand
    ItemSlot.Chest, ItemSlot.OffHand, ItemSlot.Ring, ItemSlot.Amulet -> null
}
