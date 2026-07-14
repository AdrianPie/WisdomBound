package com.lernhero.domain.item

import com.lernhero.domain.combat.Effect
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.preset.Rarity

/** Miejsca na postaci. Nowy slot dopisuje sie tutaj - reszta systemu nie zauwazy roznicy. */
enum class ItemSlot {
    Head,
    Chest,
    Hands,
    Legs,
    Feet,
    MainHand,
    OffHand,
    Ring,
    Amulet
}

/**
 * Przedmiot to **dane**, nie zachowanie. Nie ma metody "uderz" ani "obron" - ma liste efektow,
 * ktore silnik potrafi wykonac.
 *
 * Dodanie setnego miecza nie wymaga ani jednej linijki w silniku: skladasz go z istniejacych
 * efektow, jak z klockow.
 *
 * [iconKey] jest tekstem, a nie zasobem graficznym, bo domena nie moze zalezec od Compose.
 * Warstwa UI mapuje klucz na obrazek - tak samo jak robi to juz spriteKey przy potworach.
 */
data class Item(
    val id: String,
    val name: String,
    val description: String,
    val slot: ItemSlot,
    val rarity: Rarity,
    val iconKey: String,
    val stats: Stats = Stats.Empty,
    val effects: List<Effect> = emptyList(),
    val cost: Int = 0
)

/**
 * Zalozony ekwipunek. Splaszcza sie do dwoch rzeczy, ktorych potrzebuje walka:
 * sumy statystyk i listy efektow.
 */
data class Loadout(
    val items: Map<ItemSlot, Item> = emptyMap()
) {
    val stats: Stats
        get() = items.values.fold(Stats.Empty) { total, item -> total + item.stats }

    val effects: List<Effect>
        get() = items.values.flatMap { it.effects }

    fun with(item: Item): Loadout = Loadout(items + (item.slot to item))

    fun without(slot: ItemSlot): Loadout = Loadout(items - slot)

    operator fun get(slot: ItemSlot): Item? = items[slot]

    companion object {
        val Empty = Loadout()

        fun of(vararg items: Item): Loadout = Loadout(items.associateBy { it.slot })
    }
}
