package com.lernhero.domain.item

import com.lernhero.domain.craft.ItemUpgrades
import com.lernhero.domain.item.catalog.Accessories
import com.lernhero.domain.item.catalog.AccessoriesExtra
import com.lernhero.domain.item.catalog.Armors
import com.lernhero.domain.item.catalog.ArmorsExtra
import com.lernhero.domain.item.catalog.Weapons
import com.lernhero.domain.item.catalog.WeaponsExtra
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
        register(WeaponsExtra.all)
        register(Armors.all)
        register(ArmorsExtra.all)
        register(Accessories.all)
        register(AccessoriesExtra.all)
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
     * Zalozone SZTUKI zamienione w liczby dla walki. Poziom ulepszenia przychodzi z egzemplarza
     * ([ItemInstance.level]), a nie z osobnej mapy obok - dzieki temu dwie kopie tego samego
     * miecza moga miec rozne poziomy i kazda bije tak, jak faktycznie zostala wykuta.
     */
    fun loadoutOf(instances: List<ItemInstance>): Loadout =
        Loadout(instances.mapNotNull { it.resolve() }.associateBy { it.slot })
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

/**
 * Sloty trzymaja uid SZTUK, wiec zeby powiedziec, co w nich lezy, trzeba miec plecak. Stad
 * [bag] w kazdej z tych funkcji - to on jest zrodlem prawdy o przedmiotach, a mapa slotow
 * tylko wskazuje palcem.
 */
fun PlayerEquipment.instanceIn(slot: ItemSlot, bag: List<ItemInstance>): ItemInstance? =
    migrated().equipped[slot.name]?.let { uid -> Inventory.byUid(bag, uid) }

/** Przedmiot zalozony w danym slocie (juz z poziomem ulepszenia tej sztuki), albo null. */
fun PlayerEquipment.itemIn(slot: ItemSlot, bag: List<ItemInstance>): Item? =
    instanceIn(slot, bag)?.resolve()

/** Zaklada KONKRETNA sztuke - slot zapamietuje jej uid, a nie typ przedmiotu. */
fun PlayerEquipment.equip(instance: ItemInstance, slot: ItemSlot): PlayerEquipment =
    PlayerEquipment(equipped = migrated().equipped + (slot.name to instance.uid))

fun PlayerEquipment.unequip(slot: ItemSlot): PlayerEquipment =
    PlayerEquipment(equipped = migrated().equipped - slot.name)

/** Uid sztuk zalozonych przez bohatera - do odfiltrowania ich z listy "wolne w plecaku". */
fun PlayerEquipment.equippedUids(): Set<String> = migrated().equipped.values.toSet()

/** Zalozony ekwipunek widziany przez system walki: suma statystyk i lista efektow. */
fun PlayerEquipment.toLoadout(bag: List<ItemInstance>): Loadout =
    ItemCatalog.loadoutOf(migrated().equipped.values.mapNotNull { Inventory.byUid(bag, it) })
