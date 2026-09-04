package com.lernhero.game.character

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import com.lernhero.domain.player.HeroAppearance
import com.lernhero.domain.player.HeroPalette

/**
 * Zamienia zapisany [HeroAppearance] na filtry, ktorymi rysuje sie sprite'y bohatera.
 *
 * Kolorowanie idzie przez [BlendMode.Modulate], czyli MNOZENIE pikseli przez kolor - a nie przez
 * zwykly `tint`, ktory zamalowalby sprite na plasko i skasowal cieniowanie oraz czarny kontur.
 * Sprite'y ciala sa kremowe (prawie biale), wiec pomnozone przez odcien skory daja dokladnie ten
 * odcien, a czarny kontur (0 x cokolwiek = 0) zostaje czarny.
 *
 * Dlatego tez wlosy maja wlasna warstwe [com.lernhero.shared.resources.Resources.Player.hair]:
 * w oryginalnej glowie sa niemal czarne, a czarnego mnozeniem nie da sie rozjasnic.
 */
data class HeroLook(
    val skin: Color,
    val hair: Color
) {
    val skinFilter: ColorFilter get() = ColorFilter.tint(skin, BlendMode.Modulate)
    val hairFilter: ColorFilter get() = ColorFilter.tint(hair, BlendMode.Modulate)

    companion object {
        /** Wyglad "jak w sprite'ach", bez zadnego barwienia - uzywany, gdy gracz jeszcze nie wczytany. */
        val Default = HeroLook(skin = Color.White, hair = Color.White)

        fun of(appearance: HeroAppearance): HeroLook = HeroLook(
            skin = Color(HeroPalette.parseHex(appearance.skinTone)),
            hair = Color(HeroPalette.parseHex(appearance.hairColor))
        )
    }
}
