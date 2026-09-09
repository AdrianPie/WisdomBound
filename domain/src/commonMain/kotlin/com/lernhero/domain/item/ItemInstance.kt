package com.lernhero.domain.item

import com.lernhero.domain.craft.ItemUpgrades
import com.lernhero.domain.item.catalog.MonsterGear
import kotlinx.serialization.Serializable

/**
 * JEDNA SZTUKA przedmiotu w plecaku gracza.
 *
 * Do tej pory plecak byl lista ID z katalogu ([com.lernhero.domain.player.Player.ownedItems]), wiec
 * "nozyk" istnial w calej grze w jednym egzemplarzu: nie dalo sie uzbroic dziesieciu kompanow tym
 * samym typem broni bez dopisywania dziesieciu roznych wpisow do katalogu, a ulepszenie jednego
 * nozyka ulepszalo kazdy nozyk naraz (poziomy siedzialy w mapie `itemUpgrades` kluczowanej ID
 * przedmiotu). [MonsterGear] wprost tlumaczy sie w swoim KDoc z istnienia osobnego katalogu
 * wlasnie tym brakiem pojecia egzemplarza.
 *
 * [uid] to tozsamosc SZTUKI i to on ladu je w slotach ekwipunku - dzieki temu dwie kopie tego
 * samego [itemId] sa dla gry dwoma roznymi przedmiotami, kazda z wlasnym [level].
 *
 * Poziom ulepszenia jest CZESCIA egzemplarza, a nie osobna mapa obok: nie da sie wtedy zapisac
 * poziomu przedmiotu, ktorego juz nie ma, ani zgubic poziomu przy przekladaniu miedzy kompanami.
 */
@Serializable
data class ItemInstance(
    /** Unikalne w calym plecaku - patrz [Inventory.uidFor]. */
    val uid: String,
    /** Wpis w katalogu ([ItemCatalog] albo [com.lernhero.domain.item.catalog.MonsterGear]). */
    val itemId: String,
    /** Poziom ulepszenia TEJ sztuki, 0..[com.lernhero.domain.craft.ItemUpgrades.MAX_LEVEL]. */
    val level: Int = 0
)

/**
 * Operacje na plecaku. Trzymane jako funkcje na liscie, a nie metody klasy, bo plecak jest
 * zwyklym polem zapisu i przechodzi przez Firestore - im mniej typow po drodze, tym mniej
 * powodow, zeby zapis sie rozjechal.
 */
object Inventory {

    /**
     * Separator w [ItemInstance.uid]. Musi byc znakiem, ktorego NIE MA w id z katalogu - inaczej
     * "miecz#2" i "miecz#2" z dwoch roznych zrodel mogly by sie zderzyc.
     */
    const val UID_SEPARATOR = "#"

    /**
     * Kolejny wolny identyfikator sztuki danego przedmiotu: `miecz#0`, `miecz#1`, ...
     *
     * Numerujemy deterministycznie od stanu plecaka, a nie losowo ani od zegara, bo ten sam
     * plecak musi dawac ten sam wynik przy odtwarzaniu zapisu i w testach.
     */
    fun uidFor(itemId: String, existing: List<ItemInstance>): String {
        val taken = existing.mapTo(mutableSetOf()) { it.uid }
        var index = existing.count { it.itemId == itemId }
        while ("$itemId$UID_SEPARATOR$index" in taken) index++
        return "$itemId$UID_SEPARATOR$index"
    }

    /** Id przedmiotu z katalogu zaszyte w uid - do czytania starych zapisow i logow. */
    fun itemIdOf(uid: String): String = uid.substringBeforeLast(UID_SEPARATOR)

    fun byUid(inventory: List<ItemInstance>, uid: String): ItemInstance? =
        inventory.firstOrNull { it.uid == uid }

    /** Wszystkie sztuki danego typu - tego szuka ekran, gdy pyta "ile mam nozykow". */
    fun countOf(inventory: List<ItemInstance>, itemId: String): Int =
        inventory.count { it.itemId == itemId }

    /** Dokłada [count] nowych sztuk. Kazda dostaje wlasny uid, wiec kazda ulepsza sie osobno. */
    fun add(inventory: List<ItemInstance>, itemId: String, count: Int = 1): List<ItemInstance> {
        var result = inventory
        repeat(count.coerceAtLeast(0)) {
            result = result + ItemInstance(uid = uidFor(itemId, result), itemId = itemId)
        }
        return result
    }

    fun remove(inventory: List<ItemInstance>, uid: String): List<ItemInstance> =
        inventory.filterNot { it.uid == uid }

    /** Podnosi poziom JEDNEJ sztuki. Brak sztuki = plecak bez zmian, zamiast wyjatku. */
    fun withLevel(inventory: List<ItemInstance>, uid: String, level: Int): List<ItemInstance> =
        inventory.map { if (it.uid == uid) it.copy(level = level) else it }
}

/**
 * Sztuka zamieniona na gotowy [Item] - z katalogu bohatera ALBO kompanow, juz z naliczonym
 * poziomem ulepszenia TEJ sztuki.
 *
 * To jedyne miejsce, w ktorym uid zamienia sie w liczby do walki. Dzieki temu ekrany i silnik nie
 * musza wiedziec, z ktorego katalogu pochodzi przedmiot ani jak liczy sie poziom - pytaja o Item
 * i dostaja Item.
 */
fun ItemInstance.resolve(): Item? {
    val base = ItemCatalog.byId(itemId) ?: MonsterGear.byId(itemId) ?: return null
    return ItemUpgrades.upgraded(base, level.coerceIn(ItemUpgrades.MIN_LEVEL, ItemUpgrades.MAX_LEVEL))
}

/** Przedmiot BEZ naliczonego poziomu - do pokazania "czym to jest" w katalogu i sklepie. */
fun ItemInstance.base(): Item? = ItemCatalog.byId(itemId) ?: MonsterGear.byId(itemId)
