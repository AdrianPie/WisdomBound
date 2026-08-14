package com.lernhero.home.ui

import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.spell.Spell

/**
 * Jedyne miejsce, w ktorym przedmiot/zaklecie spotyka sie z grafika.
 *
 * Domena zna tylko [Item.iconKey]/[Spell.iconKey] - zwykly tekst - bo nie moze zalezec od
 * Compose. Ikony to emoji: bez tego kazda bron i tak dzielila jedna ikonke miecza plytowego,
 * a "kula ognia" pokazywala mieczyki, "leczenie" - domek. Emoji przynajmniej trafnie pokazuja,
 * co dany przedmiot robi - podmien na wlasna grafike, gdy tylko ja narysujesz.
 */
fun Item.icon(): String = when (iconKey) {
    "plate_head" -> "🪖"
    "plate_hands" -> "🧤"
    "plate_legs" -> "👖"
    "plate_feet" -> "🥾"
    "sword", "dagger" -> "🗡️"
    "glaive" -> "🔱"
    "staff" -> "🪄"
    "shield" -> "🛡️"
    "cloak" -> "🧥"
    "ring", "band" -> "💍"
    "amulet_frost" -> "📿"
    "banner" -> "🚩"
    else -> "❔"
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

fun Spell.icon(): String = when (iconKey) {
    "fireball" -> "🔥"
    "firebomb" -> "💥"
    "heal" -> "💚"
    "poison_dart" -> "☠️"
    "frost_shard" -> "❄️"
    else -> "✨"
}
