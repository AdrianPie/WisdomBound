package com.lernhero.reader.reflow

import androidx.compose.ui.geometry.Offset

/**
 * Wyciaganie slowa i zdania spod palca.
 *
 * Nie robimy osobnego klikalnego elementu na kazde slowo - przy stronie tekstu to setki
 * komponentow i i tak nie trafiloby sie w nie palcem. Zamiast tego `Text` oddaje offset znaku
 * pod dotknieciem, a granice slowa i zdania wyznaczamy tutaj, na czystym tekscie.
 */
object WordPicker {

    /** Znaki, ktore traktujemy jako czesc slowa - apostrof i lacznik sa w srodku slow. */
    private fun isWordChar(c: Char): Boolean = c.isLetterOrDigit() || c == '\'' || c == '’' || c == '-'

    private val SENTENCE_ENDINGS = charArrayOf('.', '!', '?', '…')

    /** Zakres slowa zawierajacego [offset], albo null gdy palec trafil w spacje/interpunkcje. */
    fun wordRangeAt(text: String, offset: Int): IntRange? {
        if (text.isEmpty()) return null
        // Dotkniecie tuz za slowem (offset == dlugosc slowa) ma nadal wybrac to slowo.
        val probe = when {
            offset < text.length && isWordChar(text[offset]) -> offset
            offset > 0 && isWordChar(text[offset - 1]) -> offset - 1
            else -> return null
        }

        var start = probe
        while (start > 0 && isWordChar(text[start - 1])) start--
        var end = probe
        while (end + 1 < text.length && isWordChar(text[end + 1])) end++

        // Lacznik/apostrof na samym brzegu nie jest czescia slowa (np. myslnik na koncu zdania).
        while (start < end && !text[start].isLetterOrDigit()) start++
        while (end > start && !text[end].isLetterOrDigit()) end--

        return if (text[start].isLetterOrDigit()) start..end else null
    }

    /**
     * Zakres zdania otaczajacego [offset], WLACZNIE z biala spacja przed nim (nieprzycieta) -
     * przycinanie zmienia offsety, a te maja sie zgadzac z pozycjami w [text] do podswietlania.
     * Kropka konczaca zdanie jest wliczona w zakres.
     */
    fun sentenceRangeAt(text: String, offset: Int): IntRange {
        if (text.isEmpty()) return 0 until 0
        val probe = offset.coerceIn(0, text.length - 1)

        var start = probe
        while (start > 0 && text[start - 1] !in SENTENCE_ENDINGS) start--
        var end = probe
        while (end < text.length && text[end] !in SENTENCE_ENDINGS) end++
        if (end < text.length) end++ // domknij zdanie razem z kropka

        return start until end
    }

    /**
     * Zdanie otaczajace [offset] - kontekst wysylany razem ze slowem do tlumaczenia. Bez niego
     * slownik nie ma jak wybrac wlasciwego znaczenia (klasyczne "bark" psa kontra drzewa).
     */
    fun sentenceAt(text: String, offset: Int): String =
        text.substring(sentenceRangeAt(text, offset)).trim()
}

/** Czy popup pokazuje tlumaczenie samego slowa, czy calego zdania wokol niego. */
enum class SelectionScope { Word, Sentence }

/**
 * Co gracz zaznaczyl w tekscie - slowo dotkniete palcem plus od razu policzony zakres zdania
 * wokol niego, zeby przycisk "rozszerz na zdanie" w popupie nie musial nic przeliczac na nowo,
 * tylko przelaczyc [scope].
 *
 * [anchor] to GORNA krawedz dotknietego slowa w ukladzie okna (px, srodek w poziomie) - liczona
 * raz, w miejscu dotkniecia, i NIE zmienia sie przy rozszerzeniu na zdanie (popup zostaje tam,
 * gdzie gracz dotknal, zamiast skakac po ekranie razem z rosnacym zaznaczeniem).
 *
 * [wordBottomPx] to dolna krawedz tego samego slowa. Popup domyslnie staje NAD slowem, ale gdy
 * slowo jest przy samej gorze ekranu (brak miejsca nad nim), przeskakuje POD nie - i wtedy
 * potrzebuje wiedziec, gdzie slowo sie konczy.
 */
data class WordSelection(
    val scope: SelectionScope,
    val word: String,
    val sentence: String,
    val blockIndex: Int,
    val wordCharStart: Int,
    val sentenceCharStart: Int,
    val sentenceCharEnd: Int,
    val anchor: Offset,
    val wordBottomPx: Float
) {
    val selectedText: String
        get() = if (scope == SelectionScope.Word) word else sentence

    val highlightStart: Int
        get() = if (scope == SelectionScope.Word) wordCharStart else sentenceCharStart

    /** Wylaczna gorna granica podswietlenia. */
    val highlightEnd: Int
        get() = if (scope == SelectionScope.Word) wordCharStart + word.length else sentenceCharEnd
}
