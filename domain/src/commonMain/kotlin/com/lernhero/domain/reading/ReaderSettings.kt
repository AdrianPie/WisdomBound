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
    val lineHeightMultiplier: Float = DEFAULT_LINE_HEIGHT,
    val horizontalMarginDp: Int = 24,
    /** Justowanie do obu krawedzi - tak wyglada sklad ksiazkowy. */
    val justify: Boolean = true,
    val theme: ReaderTheme = ReaderTheme.Light,
    val typeface: ReaderTypeface = ReaderTypeface.Serif
) {
    fun withFontSize(sp: Int): ReaderSettings =
        copy(fontSizeSp = sp.coerceIn(MIN_FONT_SIZE_SP, MAX_FONT_SIZE_SP))

    fun withLineHeight(multiplier: Float): ReaderSettings =
        copy(lineHeightMultiplier = multiplier.coerceIn(MIN_LINE_HEIGHT, MAX_LINE_HEIGHT))

    /**
     * Interlinia gotowa do uzycia w stylu tekstu.
     *
     * Czytana PRZEZ TE FUNKCJE, a nie prosto z pola, bo w zapisach graczy siedzi jeszcze stara
     * wartosc 1.55 - przy niej ogonek "g" z jednej linii niemal dotykal "B" z nastepnej. Docinanie
     * do [MIN_LINE_HEIGHT] naprawia te zapisy bez osobnej migracji.
     */
    val effectiveLineHeight: Float
        get() = lineHeightMultiplier.coerceIn(MIN_LINE_HEIGHT, MAX_LINE_HEIGHT)

    companion object {
        const val MIN_FONT_SIZE_SP = 13
        const val MAX_FONT_SIZE_SP = 32
        const val DEFAULT_FONT_SIZE_SP = 19

        /**
         * Dolny sufit interlinii. Ponizej ~1.7 wydluzone znaki kroju szeryfowego (ogonki g/y/p
         * kontra wysokie b/l/B) zaczynaja sie stykac miedzy liniami.
         */
        const val MIN_LINE_HEIGHT = 1.7f
        const val MAX_LINE_HEIGHT = 2.4f
        const val DEFAULT_LINE_HEIGHT = 1.85f
        const val LINE_HEIGHT_STEP = 0.1f
    }
}
