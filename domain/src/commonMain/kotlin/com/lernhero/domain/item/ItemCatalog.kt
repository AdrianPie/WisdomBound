package com.lernhero.domain.item

import com.lernhero.domain.craft.ItemUpgrades
import com.lernhero.domain.item.catalog.Accessories
import com.lernhero.domain.item.catalog.Armors
import com.lernhero.domain.item.catalog.Weapons
import com.lernhero.domain.player.PlayerEquipment

/**
 * Rejestr wszystkich przedmiotow w grze.
 *
 * Dolozenie setnego miecza sprowadza sie do dopisania go w [Weapons] - katalog zbiera pliki,
 * nie odwrotnie. Gdy przedmioty zaczna przychodzic z serwera, [register] przyjmie je tak samo,
 * a reszta systemu nie zauwazy zmiany zrodla.
 */
object ItemCatalog {

    private val registry = LinkedHashMap<String, Item>()

    init {
        register(Weapons.all)
        register(Armors.all)
        register(Accessories.all)
    }

    fun register(items: List<Item>) {
        items.forEach { item ->
            require(registry[item.id] == null || registry[item.id] == item) {
                "Przedmiot o id '${item.id}' jest juz zarejestrowany"
            }
            registry[item.id] = item
        }
    }

    val all: List<Item> get() = registry.values.toList()

    fun byId(id: String): Item? = registry[id]

    fun bySlot(slot: ItemSlot): List<Item> = registry.values.filter { it.slot == slot }

    /**
     * [upgrades] to poziomy ulepszen (patrz [com.lernhero.domain.craft.ItemUpgrades]) - wchodza
     * TUTAJ, w jednym miejscu, w ktorym zalozony ekwipunek zamienia sie w liczby dla walki.
     * Dzieki temu ulepszony miecz bije mocniej wszedzie, a nie tylko na ekranie kuzni.
     */
    fun loadoutOf(ids: List<String>, upgrades: Map<String, Int> = emptyMap()): Loadout =
        Loadout(
            ids.mapNotNull { id ->
                byId(id)?.let { ItemUpgrades.upgraded(it, ItemUpgrades.levelOf(upgrades, id)) }
            }.associateBy { it.slot }
        )
}

/* ------------------------------- Ekwipunek gracza -------------------------------- */

/**
 * Przerabia zapis sprzed zmiany formatu (piec flag) na mape slot -> id przedmiotu.
 *
 * Gracz, ktory mial "head = true", dostaje helm plytowy - czyli dokladnie ten przedmiot,
 * ktory wtedy oznaczala ta flaga. Nowe zapisy wchodza tu i wychodza nietkniete.
 */
fun PlayerEquipment.migrated(): PlayerEquipment {
    if (equipped.isNotEmpty()) return this

    val legacy = buildMap {
        if (head) put(ItemSlot.Head.name, Armors.PLATE_HELM.id)
        if (hands) put(ItemSlot.Hands.name, Armors.PLATE_GAUNTLETS.id)
        if (legs) put(ItemSlot.Legs.name, Armors.PLATE_LEGS.id)
        if (feet) put(ItemSlot.Feet.name, Armors.PLATE_BOOTS.id)
        if (mainHand) put(ItemSlot.MainHand.name, Weapons.PLATE_SWORD.id)
    }
    return PlayerEquipment(equipped = legacy)
}

/** Przedmiot zalozony w danym slocie, albo null. */
fun PlayerEquipment.itemIn(slot: ItemSlot): Item? =
    migrated().equipped[slot.name]?.let { ItemCatalog.byId(it) }

fun PlayerEquipment.equip(item: Item): PlayerEquipment =
    PlayerEquipment(equipped = migrated().equipped + (item.slot.name to item.id))

fun PlayerEquipment.unequip(slot: ItemSlot): PlayerEquipment =
    PlayerEquipment(equipped = migrated().equipped - slot.name)

/** Zalozony ekwipunek widziany przez system walki: suma statystyk i lista efektow. */
fun PlayerEquipment.toLoadout(upgrades: Map<String, Int> = emptyMap()): Loadout =
    ItemCatalog.loadoutOf(migrated().equipped.values.toList(), upgrades)
