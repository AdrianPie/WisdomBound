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
    /* --------------------------------- Bron bohatera --------------------------------- */
    "knife", "dagger", "daggers" -> "\uD83D\uDD2A"
    "sword", "sabre", "greatsword" -> "\u2694\uFE0F"
    "axe" -> "\uD83E\uDE93"
    "hammer", "club" -> "\uD83D\uDD28"
    "spear", "trident", "glaive" -> "\uD83D\uDD31"
    "sickle" -> "\u26CF\uFE0F"
    "staff" -> "\uD83E\uDE84"
    "bark" -> "\uD83E\uDEB5"

    /* -------------------------------- Pancerz bohatera ------------------------------- */
    "plate_head", "helm", "cap" -> "\uD83E\uDE96"
    "crown" -> "\uD83D\uDC51"
    "plate_hands", "pauldrons" -> "\uD83E\uDDE4"
    "plate_legs", "greaves" -> "\uD83D\uDC56"
    "plate_feet", "boots" -> "\uD83E\uDD7E"
    "plate", "chainmail", "mail", "jerkin", "carapace" -> "\uD83E\uDDBA"
    "cloak", "shroud" -> "\uD83E\uDDE5"
    "shield", "buckler" -> "\uD83D\uDEE1\uFE0F"

    /* ---------------------------------- Bizuteria ----------------------------------- */
    "ring", "band" -> "\uD83D\uDC8D"
    "amulet_frost", "pendant", "charm" -> "\uD83D\uDCFF"
    "torc" -> "\uD83D\uDCFF"
    "sigil" -> "\uD83D\uDD2F"
    "heart" -> "\uD83D\uDC9A"
    "censer" -> "\uD83D\uDD6F\uFE0F"
    "banner" -> "\uD83D\uDEA9"
    "pearl" -> "\uD83D\uDD2E"
    "totem" -> "\uD83D\uDDFF"
    "seed" -> "\uD83C\uDF31"

    /* ----- Sprzet kompanow - patrz [com.lernhero.domain.item.catalog.MonsterGear] ---- */
    "claw", "claws", "talon", "hook" -> "\uD83E\uDE9D"
    "fang", "maw" -> "\uD83E\uDDB7"
    "spine" -> "\uD83D\uDD3A"
    "lash" -> "\uD83C\uDF3F"
    "hide" -> "\uD83D\uDFEB"
    "chitin" -> "\uD83E\uDEB2"
    "horns", "hooves" -> "\uD83D\uDC0F"
    "mask" -> "\uD83C\uDFAD"
    "paws" -> "\uD83D\uDC3E"
    "fins" -> "\uD83D\uDC1F"

    // Znak zapytania to BLAD DANYCH, nie ozdobnik: znaczy, ze przedmiot ma iconKey, ktorego
    // nikt tu nie dopisal. Pilnuje tego [ItemArtTest], zeby nie wrocila sytuacja, w ktorej
    // pol katalogu wyglada na ekranie jak rzad pytajnikow.
    else -> "\u2754"
}

/** Wszystkie klucze ikon, ktore ta mapa obsluguje - do testu pokrycia katalogu. */
internal val MAPPED_ICON_KEYS: Set<String> = setOf(
    "knife", "dagger", "daggers", "sword", "sabre", "greatsword", "axe", "hammer", "club",
    "spear", "trident", "glaive", "sickle", "staff", "bark",
    "plate_head", "helm", "cap", "crown", "plate_hands", "pauldrons", "plate_legs", "greaves",
    "plate_feet", "boots", "plate", "chainmail", "mail", "jerkin", "carapace", "cloak", "shroud",
    "shield", "buckler",
    "ring", "band", "amulet_frost", "pendant", "charm", "torc", "sigil", "heart", "censer",
    "banner", "pearl", "totem", "seed",
    "claw", "claws", "talon", "hook", "fang", "maw", "spine", "lash", "hide", "chitin",
    "horns", "hooves", "mask", "paws", "fins"
)

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
