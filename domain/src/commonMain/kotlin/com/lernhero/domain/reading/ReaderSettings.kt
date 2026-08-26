package com.lernhero.domain.reading

import kotlinx.serialization.Serializable

/**
 * Jak czytamy tekst.
 *
 * [Reflow] to tryb domyslny i jedyny, ktory potrafi tlumaczyc slowa - tekst jest prawdziwym
 * tekstem, wiec da sie w niego trafic palcem i policzyc slowa. [Original] pokazuje plik tak,
 * jak wyglada u zrodla (PDF renderowany na bitmapie), za cene braku tlumaczen i statystyk slow.
 */
@Serializable
enum class ReadingMode {
    Reflow,
    Original
}

@Serializable
enum class ReaderTheme {
    Light,
    Sepia,
    Dark
}

@Serializable
enum class ReaderTypeface {
    Serif,
    Sans
}

/**
 * Ustawienia czytnika - wspolne dla calej biblioteki, nie per ksiazka. Zmiana czegokolwiek tutaj
 * uniewaznia paginacje, dlatego pozycja w ksiazce trzymana jest jako [ReadingAnchor].
 */
@Serializable
data class ReaderSettings(
    val mode: ReadingMode = ReadingMode.Reflow,
    val fontSizeSp: Int = DEFAULT_FONT_SIZE_SP,
    val lineHeightMultiplier: Float = 1.55f,
    val horizontalMarginDp: Int = 24,
    /** Justowanie do obu krawedzi - tak wyglada sklad ksiazkowy. */
    val justify: Boolean = true,
    val theme: ReaderTheme = ReaderTheme.Light,
    val typeface: ReaderTypeface = ReaderTypeface.Serif
) {
    fun withFontSize(sp: Int): ReaderSettings =
        copy(fontSizeSp = sp.coerceIn(MIN_FONT_SIZE_SP, MAX_FONT_SIZE_SP))

    companion object {
        const val MIN_FONT_SIZE_SP = 13
        const val MAX_FONT_SIZE_SP = 32
        const val DEFAULT_FONT_SIZE_SP = 19
    }
}
