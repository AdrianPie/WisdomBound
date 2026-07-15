package com.lernhero.home.ui

import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.DrawableResource

/**
 * Jedyne miejsce, w ktorym przedmiot spotyka sie z grafika.
 *
 * Domena zna tylko [Item.iconKey] - zwykly tekst - bo nie moze zalezec od Compose. Tutaj klucz
 * zamienia sie w obrazek. Gdy narysujesz wlasne ikony, podmieniasz je wylacznie w tej tabeli.
 */
fun Item.icon(): DrawableResource = when (iconKey) {
    "plate_head" -> Resources.PlateArmor.head
    "plate_hands" -> Resources.PlateArmor.leftDownHand
    "plate_legs" -> Resources.PlateArmor.leftUpLeg
    "plate_feet" -> Resources.PlateArmor.leftFoot

    // TODO: wlasne grafiki. Na razie kazda bron pozycza miecz plytowy, a dodatki - ikonki z UI.
    "sword", "glaive", "dagger" -> Resources.PlateArmor.swordFront
    "staff" -> Resources.Icon.Book
    "shield" -> Resources.Icon.Weight
    "cloak" -> Resources.Icon.Person
    "ring", "band" -> Resources.Icon.Unlock
    "amulet_frost" -> Resources.Icon.MapPin
    "banner" -> Resources.Icon.swords

    else -> Resources.Icon.Categories
}

fun ItemSlot.label(): String = when (this) {
    ItemSlot.Head -> "Glowa"
    ItemSlot.Chest -> "Tors"
    ItemSlot.Hands -> "Rece"
    ItemSlot.Legs -> "Nogi"
    ItemSlot.Feet -> "Stopy"
    ItemSlot.MainHand -> "Glowna reka"
    ItemSlot.OffHand -> "Druga reka"
    ItemSlot.Ring -> "Pierscien"
    ItemSlot.Amulet -> "Amulet"
}

/** Krotka etykieta pod slotem na postaci - dluzsze nazwy nie mieszcza sie w kafelku. */
fun ItemSlot.shortLabel(): String = when (this) {
    ItemSlot.Head -> "Glowa"
    ItemSlot.Chest -> "Tors"
    ItemSlot.Hands -> "Rece"
    ItemSlot.Legs -> "Nogi"
    ItemSlot.Feet -> "Stopy"
    ItemSlot.MainHand -> "Bron"
    ItemSlot.OffHand -> "Tarcza"
    ItemSlot.Ring -> "Pierscien"
    ItemSlot.Amulet -> "Amulet"
}

/**
 * Ikony zaklec. Osobna tabela od przedmiotow, bo spell ma wlasny [Spell.iconKey]. Na razie
 * pozyczaja ikonki z UI - podmien tutaj, gdy narysujesz wlasne.
 */
fun com.lernhero.domain.spell.Spell.icon(): org.jetbrains.compose.resources.DrawableResource = when (iconKey) {
    "fireball", "firebomb" -> com.lernhero.shared.resources.Resources.Icon.swords
    "heal" -> com.lernhero.shared.resources.Resources.Icon.Home
    "poison_dart" -> com.lernhero.shared.resources.Resources.Icon.MapPin
    "frost_shard" -> com.lernhero.shared.resources.Resources.Icon.Unlock
    else -> com.lernhero.shared.resources.Resources.Icon.Categories
}
