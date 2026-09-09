package com.lernhero.domain.player

import kotlinx.serialization.Serializable

/**
 * Wyglad bohatera wybrany przy tworzeniu postaci. Same ODNOSNIKI (id presetu) i KOLORY, zero
 * grafik - dzieki temu ten sam zapis rysuje sie tak samo na Domu, w ekwipunku i w walce, a
 * podmiana sprite'ow nie wymaga ruszania zapisow graczy.
 *
 * Kolory trzymamy jako "RRGGBB" (bez alfy), bo Firestore nie ma typu koloru, a string czyta sie
 * w konsoli. Warstwa UI zamienia je na Color przez [HeroPalette.parseHex].
 */
@Serializable
data class HeroAppearance(
    /** Id z [HeroPresets] - decyduje o zestawie sprite'ow i o domyslnych kolorach. */
    val presetId: String = HeroPresets.Default.id,
    val skinTone: String = HeroPalette.SkinTones[1],
    val hairColor: String = HeroPalette.HairColors[0]
) {
    val preset: HeroPreset get() = HeroPresets.byId(presetId)
}

/**
 * Jeden "szablon" postaci do wyboru na ekranie tworzenia. Nazwa widoczna dla gracza NIE siedzi
 * tutaj, tylko w AppStrings (modul :shared) - domena nie zna jezyka UI. Na razie dwa (kobieta/mezczyzna) i oba
 * korzystaja z tego samego zestawu sprite'ow - osobnych grafik dla kobiety jeszcze nie ma.
 * Dolozenie kolejnej wersji postaci to dopisanie wpisu tutaj plus zestawu sprite'ow po stronie UI
 * (patrz mapowanie [spriteSet] w feature:game).
 */
@Serializable
data class HeroPreset(
    val id: String,
    /**
     * Id zestawu grafik. Kilka presetow moze wskazywac ten sam zestaw - tak jest teraz, dopoki
     * kobieta nie dostanie wlasnych sprite'ow.
     */
    val spriteSet: String,
    val defaultSkinTone: String,
    val defaultHairColor: String
)

object HeroPresets {

    val Male = HeroPreset(
        id = "male",
        spriteSet = SPRITE_SET_CLASSIC,
        defaultSkinTone = HeroPalette.SkinTones[1],
        defaultHairColor = HeroPalette.HairColors[0]
    )

    val Female = HeroPreset(
        id = "female",
        spriteSet = SPRITE_SET_CLASSIC,
        defaultSkinTone = HeroPalette.SkinTones[0],
        defaultHairColor = HeroPalette.HairColors[3]
    )

    val all: List<HeroPreset> = listOf(Male, Female)

    val Default: HeroPreset = Male

    fun byId(id: String): HeroPreset = all.firstOrNull { it.id == id } ?: Default

    /** Domyslny wyglad presetu - to dostaje gracz, zanim ruszy suwakami kolorow. */
    fun appearanceOf(preset: HeroPreset): HeroAppearance = HeroAppearance(
        presetId = preset.id,
        skinTone = preset.defaultSkinTone,
        hairColor = preset.defaultHairColor
    )
}

/** Jedyny na razie zestaw grafik bohatera (head_new/body_new/...). */
const val SPRITE_SET_CLASSIC = "classic"

/**
 * Palety do wyboru. Krotkie i skonczone - to ma byc kilka kafelkow do klikniecia, a nie color
 * picker; sprite'y sa kolorowane mnozeniem, wiec skrajnie ciemne odcienie i tak by sie zlaly z
 * konturem.
 */
object HeroPalette {

    val SkinTones: List<String> = listOf(
        "FFE0C4", // jasna
        "F2C79B", // domyslna
        "D9A272",
        "B57A4C",
        "8A5433",
        "5E3620"
    )

    val HairColors: List<String> = listOf(
        "2B2320", // ciemny braz (jak oryginalny sprite)
        "6B4A2F",
        "A9703C",
        "D9A441", // blond
        "B44B2B", // rudy
        "E8E2D6", // siwy
        "5B6BC0", // niebieski
        "8E4FB5"  // fioletowy
    )

    /** "RRGGBB" -> 0xFFRRGGBB. Zle wpisy nie wywalaja gry, tylko wracaja jako biel (brak barwienia). */
    fun parseHex(hex: String): Long {
        val cleaned = hex.removePrefix("#").trim()
        val value = cleaned.toLongOrNull(16) ?: return 0xFFFFFFFF
        return 0xFF000000L or (value and 0xFFFFFF)
    }
}
