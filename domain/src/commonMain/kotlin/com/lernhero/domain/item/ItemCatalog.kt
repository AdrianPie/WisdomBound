package com.lernhero.domain.item

import com.lernhero.domain.item.catalog.Accessories
import com.lernhero.domain.item.catalog.Armors
import com.lernhero.domain.item.catalog.Weapons
import com.lernhero.domain.preset.PlayerEquipment

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

    fun loadoutOf(ids: List<String>): Loadout =
        Loadout(ids.mapNotNull { byId(it) }.associateBy { it.slot })
}

/**
 * Most miedzy starym zapisem gracza a nowym systemem.
 *
 * [PlayerEquipment] to piec flag zapisanych jeszcze przez poprzedni ekwipunek. Mapujemy je na
 * przedmioty z katalogu, wiec stare stany dzialaja bez migracji bazy - a caly system walki
 * widzi juz tylko [Loadout].
 */
fun PlayerEquipment.toLoadout(): Loadout {
    val ids = buildList {
        if (head) add(Armors.PLATE_HELM.id)
        if (hands) add(Armors.PLATE_GAUNTLETS.id)
        if (legs) add(Armors.PLATE_LEGS.id)
        if (feet) add(Armors.PLATE_BOOTS.id)
        if (mainHand) add(Weapons.PLATE_SWORD.id)
    }
    return ItemCatalog.loadoutOf(ids)
}
