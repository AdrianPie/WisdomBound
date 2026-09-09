package com.lernhero.domain.item

/**
 * Rodzaj sprzetu: bron / pancerz / bizuteria.
 *
 * Nie jest polem [Item], tylko wynika ze [ItemSlot] - z tego samego powodu, dla ktorego
 * [com.lernhero.domain.craft.ItemTier] wynika z rzadkosci: dwie skale opisujace to samo zawsze
 * sie w koncu rozjezdzaja. Slot juz mowi, gdzie rzecz sie zaklada, wiec mowi tez, czym jest.
 *
 * Kategoria istnieje dla JEDNEGO odbiorcy - katalogu kuzni. Przy 57 przedmiotach bohatera
 * przewijanie stron w poszukiwaniu "jakiejkolwiek broni" bylo bezcelowe; teraz gracz wybiera
 * ikone i widzi tylko swoja polke.
 *
 * [Trinket] laczy pierscien i amulet, bo w kuzni to jedna polka: przedmioty prawie bez statystyk,
 * za to z regula walki (patrz [com.lernhero.domain.item.catalog.AccessoriesExtra]).
 */
enum class ItemCategory(val label: String, val icon: String) {
    Weapon("Weapons", "⚔"),
    Armor("Armor", "🛡"),
    Trinket("Trinkets", "💍");

    companion object {
        fun of(slot: ItemSlot): ItemCategory = when (slot) {
            ItemSlot.MainHand -> Weapon
            // Reka pomocnicza to w tym katalogu wylacznie tarcze i puklerze, wiec idzie do
            // pancerza - gracz szukajacy "czym bic" nie ma po co ogladac Bulwark of Dawn.
            ItemSlot.OffHand,
            ItemSlot.Head, ItemSlot.Chest, ItemSlot.Hands, ItemSlot.Legs, ItemSlot.Feet -> Armor
            ItemSlot.Ring, ItemSlot.Amulet -> Trinket
        }

        fun of(item: Item): ItemCategory = of(item.slot)
    }
}
