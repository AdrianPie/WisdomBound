package com.lernhero.home.ui

import androidx.compose.ui.graphics.Color
import com.lernhero.domain.item.Item
import com.lernhero.domain.monster.Element
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.ultimate.Ultimates
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
    //
    // Kule mocy (ulti) maja iconKey = wlasne id (orb_ember...), zeby kazda mogla dostac osobny
    // PNG (item_orb_ember.png). Do tego czasu [ItemArtImage] rysuje je kodem - emoji to ostatnia deska.
    else -> if (iconKey in ORB_ICON_KEYS) "\uD83D\uDD2E" else "\u2754"
}

/**
 * Nazwa grafiki przedmiotu (`drawable/item_<nazwa>.png`, patrz
 * [com.lernhero.shared.resources.Resources.itemArt]).
 *
 * Jedna grafika sluzy kilku przedmiotom - jak w grach, gdzie ten sam miecz wystepuje w wersji D
 * i S: rozni je kolor tla i ranga na kafelku ([ItemIcon]), a nie osobny rysunek. Przedmiot
 * spoza tej mapy szuka grafiki po swoim [Item.iconKey], a gdy i jej nie ma - zostaje emoji.
 */
fun Item.artKey(): String = ITEM_ART[id] ?: iconKey

private val ITEM_ART: Map<String, String> = mapOf(
    // Miecze
    "plate_sword" to "sword",
    "torchblade" to "sword",
    "frostbrand" to "sword",
    "silver_sabre" to "sabre",
    "dawnbreaker" to "greatsword",
    // Sztylety
    "hunting_knife" to "venomdagger",
    "venom_dagger" to "venomdagger",
    "twin_fangs" to "daggers",
    "thousand_cuts" to "daggers",
    // Topory i obuchy
    "rusty_axe" to "axe",
    "executioners_axe" to "waraxe",
    "oak_club" to "club",
    "bronze_warhammer" to "woodenmace",
    "sundering_maul" to "woodenmace",
    // Drzewcowe
    "soldiers_spear" to "spear",
    "stormpiercer" to "spear",
    "tide_trident" to "trident",
    "reaper_glaive" to "glaive",
    "blight_sickle" to "sickle",
    // Kostury - kazdy w swoim zywiole
    "ember_staff" to "firestaff",
    "withering_crook" to "deathstaff",
    "worldroot_staff" to "naturestaff",
    // Zbroje (korpus)
    "leather_jerkin" to "leatherarmor",
    "hide_wrap" to "leatherarmor",
    "chain_hauberk" to "chainarmor",
    "chitin_plate" to "chainarmor",
    "wardens_plate" to "platearmor",
    "titans_carapace" to "platearmor",
    "aeonplate" to "platearmor",
    "revenant_mail" to "skullarmor",
    "grave_shroud" to "skullarmor",
    "darksteel_armor" to "darkarmor",
    "cloak_of_shadows" to "darkarmor",
    // Buty
    "leather_boots" to "leatherboots",
    "phantom_treads" to "leatherboots",
    "scouts_boots" to "scoutboots",
    "plate_boots" to "plateboots",
    // Rekawice
    "plate_gauntlets" to "plategloves",
    "iron_gauntlets" to "plategloves",
    "spiked_pauldrons" to "plategloves",
    // Tarcze
    "worn_buckler" to "woodenshield",
    "blessed_shield" to "blessedshield",
    "bulwark_of_dawn" to "blessedshield",
    "aegis_shield" to "blessedshield",
    // Pierscienie
    "copper_band" to "copperring",
    "blood_band" to "copperring",
    "band_of_haste" to "copperring",
    "ironwood_ring" to "copperring",
    "ring_of_the_duellist" to "gemring",
    "ring_of_fury" to "gemring",
    "ring_of_tempo" to "gemring",
    "leech_signet" to "signetring",
    "signet_of_the_first_blow" to "signetring",
    "soulbinder_band" to "soulring",
    "seed_ring" to "soulring",
    // Amulety
    "bone_charm" to "bonecharm",
    "emberheart_charm" to "bonecharm",
    "pendant_of_the_medic" to "pendant",
    "tide_pearl" to "pendant",
    "amulet_of_frost" to "frostamulet",
    "torc_of_command" to "torc",
    "banner_of_valor" to "torc",
    "sigil_of_dread" to "sigil",
    "heart_of_the_grove" to "naturecharm",
    "spirit_totem" to "totem",
    "totem_of_ages" to "totem"
)

/** Wszystkie klucze ikon, ktore ta mapa obsluguje - do testu pokrycia katalogu. */
/** Klucze ikon kul mocy - wszystkie z [Ultimates], wiec nowa kula nie wymaga dopisywania tutaj. */
private val ORB_ICON_KEYS: Set<String> by lazy { Ultimates.items.mapTo(mutableSetOf()) { it.iconKey } }

internal val MAPPED_ICON_KEYS: Set<String> get() = BASE_ICON_KEYS + ORB_ICON_KEYS

private val BASE_ICON_KEYS: Set<String> = setOf(
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
    ItemSlot.Ultimate -> "Ultimate"
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
    ItemSlot.Ultimate -> "Ulti"
}

fun Spell.icon(): String = when (iconKey) {
    "fireball" -> "🔥"
    "firebomb" -> "💥"
    "heal" -> "💚"
    "poison_dart" -> "☠️"
    "frost_shard" -> "❄️"
    else -> "✨"
}
