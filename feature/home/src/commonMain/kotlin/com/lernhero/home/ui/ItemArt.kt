package com.lernhero.home.ui

import androidx.compose.ui.graphics.Color
import com.lernhero.domain.item.Item
import com.lernhero.domain.monster.Element
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
    // Sprzet kompanow - patrz [com.lernhero.domain.item.catalog.MonsterGear]. Placeholdery
    // do podmiany razem z reszta grafiki.
    "claw" -> "🪝"
    "fang" -> "🦷"
    "spine" -> "🔺"
    "lash" -> "🌿"
    "hide" -> "🟫"
    "chitin" -> "🪲"
    "shroud" -> "🕸️"
    "horns" -> "🐏"
    "mask" -> "🎭"
    "paws" -> "🐾"
    "fins" -> "🐟"
    "totem" -> "🗿"
    "pearl" -> "🔮"
    "seed" -> "🌱"
    else -> "❔"
}

/** Odznaka zywiolu - kolor bierze sie z [Element.colorHex], zeby domena zostala bez Compose. */
fun Element.badgeColor(): Color = Color(colorHex)

fun ItemSlot.label(): String = when (this) {
    ItemSlot.Head -> "Head"
    ItemSlot.Chest -> "Chest"
    ItemSlot.Hands -> "Hands"
    ItemSlot.Legs -> "Legs"
    ItemSlot.Feet -> "Feet"
    ItemSlot.MainHand -> "Main Hand"
    ItemSlot.OffHand -> "Off Hand"
    ItemSlot.Ring -> "Ring"
    ItemSlot.Amulet -> "Amulet"
}

/** Krotka etykieta pod slotem na postaci - dluzsze nazwy nie mieszcza sie w kafelku. */
fun ItemSlot.shortLabel(): String = when (this) {
    ItemSlot.Head -> "Head"
    ItemSlot.Chest -> "Chest"
    ItemSlot.Hands -> "Hands"
    ItemSlot.Legs -> "Legs"
    ItemSlot.Feet -> "Feet"
    ItemSlot.MainHand -> "Weapon"
    ItemSlot.OffHand -> "Shield"
    ItemSlot.Ring -> "Ring"
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
