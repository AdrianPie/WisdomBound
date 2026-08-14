package com.lernhero.shared.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Jedna wspolna paleta dla calej gry - ciemny pergamin i zloto, ta sama estetyka co w lochu
 * i walce. Wczesniej kazdy ekran definiowal wlasne hexy lokalnie (trzy rozne "ciemne motywy"
 * plus jeszcze jeden jasny w Color.kt) i przez to apka wygladala jak sklejona z kawalkow.
 *
 * Nowe ekrany powinny siegac po te stale zamiast pisac Color(0xFF...) na nowo.
 */
object AppPalette {
    /** Tlo pod calym ekranem - prawie czarny brąz. */
    val Background = Color(0xFF120E0A)

    /** Panele/karty - troche jasniejszy odcien tla. */
    val Panel = Color(0xFF1C1610)
    val PanelRaised = Color(0xFF261D14)

    /** Wnetrza slotow, pol tekstowych - najciemniejszy odcien. */
    val Ink = Color(0xFF0D0A07)

    /** Zloto - akcent numer jeden: tytuly, wartosci, aktywne stany. */
    val Gold = Color(0xFFFFD34D)
    val GoldBright = Color(0xFFFFE28A)
    val GoldFrame = Color(0xFFC8A45A)
    val GoldDim = Color(0xFF6B5A38)

    /** Tekst podstawowy na ciemnym tle - cieplawa biel, nie czysta biel (mniej razi). */
    val TextBody = Color(0xFFEADFC8)
    val TextMuted = Color(0xFF9D8B6A)
    val TextFaint = Color(0xFF6B5F4C)

    /** Stan powodzenia/niebezpieczenstwa - te same odcienie wszedzie. */
    val Success = Color(0xFF4ADE80)
    val Danger = Color(0xFFE0654F)
    val Warning = Color(0xFFE0913A)
    val Info = Color(0xFF3B9DFF)

    /** Przycisk akcji - cieply rdzawy pomarancz, ciepły kontrast wzgledem zlota. */
    val Action = Color(0xFFB4451F)
    val ActionBright = Color(0xFFD65A2E)

    val CardGradient = Brush.verticalGradient(listOf(PanelRaised, Panel))
    val ScreenGradient = Brush.verticalGradient(listOf(Color(0xFF1B140D), Background))
}
