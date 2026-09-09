package com.lernhero.domain.item

import com.lernhero.domain.player.Player
import com.lernhero.domain.player.PlayerEquipment

/**
 * Czytanie plecaka i slotow niezaleznie od tego, w ktorym formacie zapisal sie gracz.
 *
 * Ten sam wzorzec co [PlayerEquipment.migrated]: NIE ma osobnego przebiegu migracyjnego po
 * wszystkich dokumentach ani flagi wersji. Stary ksztalt po prostu CZYTA SIE jak nowy, a po
 * pierwszym zapisie gracza stare pola przestaja byc wypelniane i same znikaja z dokumentu.
 * Dzieki temu wydanie tej zmiany nie wymaga zadnej akcji na produkcji i nie da sie "zapomniec"
 * zmigrowac czyjegos konta.
 *
 * Stary zapis mial po JEDNEJ sztuce kazdego przedmiotu, wiec jego egzemplarz to zawsze `id#0`.
 */

/** Plecak bohatera jako sztuki - z nowego pola albo przerobiony ze starej listy ID. */
fun Player.heroBag(): List<ItemInstance> =
    heroInventory.ifEmpty { ownedItems.toInstances(itemUpgrades) }

/** Plecak kompanow jako sztuki - jak wyzej. */
fun Player.monsterBag(): List<ItemInstance> =
    monsterInventory.ifEmpty { ownedMonsterItems.toInstances(itemUpgrades) }

/**
 * Sloty bohatera przestawione na uid sztuk. Stary zapis trzymal tu ID z katalogu - poznajemy go
 * po braku [Inventory.UID_SEPARATOR] i doklejamy `#0`.
 */
fun Player.migratedHeroEquipment(): PlayerEquipment {
    val current = equipment.migrated()
    return PlayerEquipment(equipped = current.equipped.mapValues { (_, value) -> value.asUid() })
}

/** To samo dla kazdego kompana z osobna. */
fun Player.migratedMonsterEquipment(): Map<String, Map<String, String>> =
    monsterEquipment.mapValues { (_, slots) -> slots.mapValues { (_, value) -> value.asUid() } }

/**
 * Stary zapis trzymal w slocie ID z katalogu, nowy trzyma uid sztuki. Rozroznia je separator:
 * id z katalogu nigdy go nie zawiera (pilnuje tego [InventoryMigrationInvariant]).
 */
private fun String.asUid(): String =
    if (contains(Inventory.UID_SEPARATOR)) this else "$this${Inventory.UID_SEPARATOR}0"

private fun List<String>.toInstances(upgrades: Map<String, Int>): List<ItemInstance> =
    // distinct(): stary format byl zbiorem ID, wiec powtorzone id to zdublowany zapis, a nie
    // druga sztuka - inaczej migracja rozmnozylaby komus przedmioty.
    distinct().map { itemId ->
        ItemInstance(
            uid = "$itemId${Inventory.UID_SEPARATOR}0",
            itemId = itemId,
            level = upgrades[itemId] ?: 0
        )
    }

/**
 * Warunek, na ktorym stoi cale rozroznianie starego i nowego formatu: zaden identyfikator
 * z katalogu nie moze zawierac [Inventory.UID_SEPARATOR]. Pilnuje go test katalogu - gdyby ktos
 * dopisal przedmiot z "#" w id, stare zapisy zaczelyby sie czytac jako nowe i sloty by opustoszaly.
 */
object InventoryMigrationInvariant {
    fun holdsFor(itemIds: Collection<String>): Boolean =
        itemIds.none { it.contains(Inventory.UID_SEPARATOR) }
}

/**
 * Gracz sprowadzony do NOWEGO formatu: oba plecaki jako sztuki, oba zestawy slotow na uid,
 * stare pola wyczyszczone.
 *
 * Wolaj to przed KAZDYM zapisem dotykajacym przedmiotow. Migracja jest leniwa (czytamy stary
 * ksztalt jak nowy), ale zapisywac trzeba juz tylko nowy - inaczej dokument w nieskonczonosc
 * niesie oba formaty i pierwszy blad w ktorymkolwiek z nich cofa gracza do stanu sprzed zmiany.
 *
 * Czysci WSZYSTKIE trzy stare pola naraz, bo [Player.itemUpgrades] byla wspolna dla obu katalogow:
 * wyzerowanie jej po zmigrowaniu tylko jednego plecaka skasowaloby poziomy z drugiego.
 */
fun Player.withMigratedItems(): Player = copy(
    heroInventory = heroBag(),
    monsterInventory = monsterBag(),
    equipment = migratedHeroEquipment(),
    monsterEquipment = migratedMonsterEquipment(),
    ownedItems = emptyList(),
    ownedMonsterItems = emptyList(),
    itemUpgrades = emptyMap()
)
