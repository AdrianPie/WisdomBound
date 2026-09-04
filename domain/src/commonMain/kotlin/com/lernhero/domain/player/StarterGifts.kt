package com.lernhero.domain.player

import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.catalog.Armors
import com.lernhero.domain.item.catalog.Weapons

/**
 * Przedmiot na start, wybierany raz przy tworzeniu postaci - jak "dar" w Dark Souls. Kazdy wpis
 * wskazuje GOTOWY przedmiot z [ItemCatalog], zeby nie powstala druga, rownolegla lista statystyk,
 * ktora zaraz rozjedzie sie ze sklepem i kuznia.
 *
 * Nazwa i opis widoczne dla gracza biora sie z samego przedmiotu, a wiec tlumaczy sie je tam,
 * gdzie tlumaczy sie caly katalog - nie tutaj.
 */
data class StarterGift(
    val id: String,
    val itemId: String
) {
    val item get() = ItemCatalog.byId(itemId)

    /** Slot, w ktorym przedmiot wyladuje od razu po stworzeniu postaci. */
    val slot: ItemSlot? get() = item?.slot
}

object StarterGifts {

    /** Miecz - najprostszy start, bije dwoch przeciwnikow naraz. */
    val Sword = StarterGift(id = "sword", itemId = Weapons.PLATE_SWORD.id)

    /** Helm - pierwszy cios w walce odbija sie od niego bez szkody. */
    val Helm = StarterGift(id = "helm", itemId = Armors.PLATE_HELM.id)

    /** Buty - inicjatywa: uderzasz pierwszy. Dla tych, ktorzy wola wyprzedzic niz przetrzymac. */
    val Boots = StarterGift(id = "boots", itemId = Armors.PLATE_BOOTS.id)

    /**
     * Trzy dary o zblizonej cenie sklepowej (80-100 zlota) - wybor ma byc o STYLU gry, a nie o
     * tym, ktora opcja jest po prostu mocniejsza.
     */
    val all: List<StarterGift> = listOf(Sword, Helm, Boots)

    val Default: StarterGift = Sword

    fun byId(id: String?): StarterGift = all.firstOrNull { it.id == id } ?: Default
}
