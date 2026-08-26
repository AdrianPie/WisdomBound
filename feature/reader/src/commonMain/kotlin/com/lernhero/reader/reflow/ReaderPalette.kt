package com.lernhero.reader.reflow

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.lernhero.domain.reading.ReaderTheme

/**
 * Kolory samej kartki. Czytnik celowo NIE uzywa zlotej palety reszty aplikacji - strona ma
 * wygladac jak papier, a nie jak panel ekwipunku. Chrome (paski, dialogi) trzyma sie palety gry.
 */
@Immutable
data class ReaderPalette(
    val background: Color,
    val text: Color,
    val muted: Color,
    /** Podswietlenie dotknietego slowa. */
    val selection: Color
) {
    companion object {
        fun of(theme: ReaderTheme): ReaderPalette = when (theme) {
            ReaderTheme.Light -> ReaderPalette(
                background = Color(0xFFFFFFFF),
                text = Color(0xFF14110E),
                muted = Color(0xFF8A8078),
                selection = Color(0xFFFFE08A)
            )

            ReaderTheme.Sepia -> ReaderPalette(
                background = Color(0xFFF6ECD9),
                text = Color(0xFF3B2F21),
                muted = Color(0xFF9B8B72),
                selection = Color(0xFFE8C879)
            )

            ReaderTheme.Dark -> ReaderPalette(
                background = Color(0xFF121110),
                text = Color(0xFFE4DED3),
                muted = Color(0xFF7A736A),
                selection = Color(0xFF5C4A1E)
            )
        }
    }
}
