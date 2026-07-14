package com.lernhero.domain.shop

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
    val PLATE_HELM = ShopItem(
        id = "plate_helm",
        name = "Helm plytowy",
        description = "Ciezki helm chroniacy glowe. +2 do obrony.",
        slot = ShopItemSlot.Head,
        cost = 90
    )

    val PLATE_GAUNTLETS = ShopItem(
        id = "plate_gauntlets",
        name = "Rekawice plytowe",
        description = "Stalowe rekawice. +3 do ataku i +2 do obrony.",
        slot = ShopItemSlot.Hands,
        cost = 110
    )

    val PLATE_LEGS = ShopItem(
        id = "plate_legs",
        name = "Nagolenniki plytowe",
        description = "Solidne ochraniacze nog. +2 do obrony.",
        slot = ShopItemSlot.Legs,
        cost = 90
    )

    val PLATE_BOOTS = ShopItem(
        id = "plate_boots",
        name = "Buty plytowe",
        description = "Wzmocnione buty bojowe. +2 do obrony.",
        slot = ShopItemSlot.Feet,
        cost = 80
    )

    val PLATE_SWORD = ShopItem(
        id = "plate_sword",
        name = "Miecz plytowy",
        description = "Solidny miecz do reki glownej. +3 do ataku.",
        slot = ShopItemSlot.MainHand,
        cost = 100
    )

    val items: List<ShopItem> = listOf(
        PLATE_HELM,
        PLATE_GAUNTLETS,
        PLATE_LEGS,
        PLATE_BOOTS,
        PLATE_SWORD
    )

    fun itemForSlot(slot: ShopItemSlot): ShopItem? = items.firstOrNull { it.slot == slot }

    fun byId(id: String): ShopItem? = items.firstOrNull { it.id == id }
}
