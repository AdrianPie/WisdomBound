package com.lernhero.reader.reflow

import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Constraints
import com.lernhero.domain.reading.ContentBlock
import com.lernhero.domain.reading.ReadingAnchor
import com.lernhero.domain.reading.TextStats

/**
 * Kawalek jednego bloku tekstu lezacy na jednej stronie. Dlugi akapit rozlewa sie na kilka
 * stron, wiec jeden blok moze dac kilka fragmentow - stad [isContinuation], ktory mowi
 * renderowi, ze to dalszy ciag, a nie poczatek akapitu (czytaj: bez wciecia pierwszej linii).
 */
data class PageFragment(
    val blockIndex: Int,
    val charStart: Int,
    val text: String,
    val isHeading: Boolean,
    val isContinuation: Boolean,
    /**
     * Czy pierwsza linia tego fragmentu dostaje wciecie akapitowe.
     *
     * Fałsz nie tylko dla dalszego ciagu akapitu, ale tez dla PIERWSZEGO akapitu po naglowku -
     * w skladzie ksiazkowym wciecie mowi "to nowy akapit po poprzednim", a po tytule rozdzialu
     * nie ma poprzedniego, wiec wciecie jest tam bledem, nie ozdoba.
     */
    val indentFirstLine: Boolean,
    /**
     * Odstep nad fragmentem, w pikselach. Wyliczony przy paginacji i podany renderowi gotowy,
     * zeby obie strony liczyly tak samo - gdy paginator rezerwuje miejsce, ktorego render nie
     * rysuje (albo odwrotnie), tekst przestaje sie zgadzac z wysokoscia strony.
     */
    val spacingBeforePx: Int
)

data class ReaderPage(
    val fragments: List<PageFragment>,
    val words: Int
) {
    /** Pozycja poczatku strony - to ja zapisujemy jako postep w ksiazce. */
    val anchor: ReadingAnchor =
        fragments.firstOrNull()
            ?.let { ReadingAnchor(it.blockIndex, it.charStart) }
            ?: ReadingAnchor.START
}

/**
 * Zamienia bloki tekstu na strony pod konkretny rozmiar ekranu i konkretna typografie.
 *
 * Mierzy **kazdy blok osobno**, a nie cala ksiazke jako jeden wielki tekst. Zmierzenie calej
 * powiesci (~100 tys. slow) jednym `measure` zjadaloby sekundy i mnostwo pamieci, a i tak
 * trzeba by ja przeliczac po kazdej zmianie rozmiaru czcionki. Blok po bloku jest tanie,
 * przerywalne i pozwala paginowac tylko to, co gracz zaraz zobaczy.
 *
 * Lamanie strony wypada zawsze na granicy linii ([TextLayoutResult.getLineStart]), wiec
 * fragment przepuszczony ponownie przez `Text` o tej samej szerokosci i stylu zlamie sie
 * identycznie - strona wyglada tak, jak zostala zmierzona.
 */
class TextPaginator(
    private val measurer: TextMeasurer,
    private val paragraphStyle: TextStyle,
    private val headingStyle: TextStyle,
    private val contentWidthPx: Int,
    private val contentHeightPx: Int,
    /** Odstep miedzy blokami, w pikselach - naglowki dostaja go podwojnie. */
    private val blockSpacingPx: Int
) {

    fun paginate(blocks: List<ContentBlock>, blockIndexOffset: Int = 0): List<ReaderPage> {
        if (contentWidthPx <= 0 || contentHeightPx <= 0 || blocks.isEmpty()) return emptyList()

        val pages = mutableListOf<ReaderPage>()
        var current = mutableListOf<PageFragment>()
        var remaining = contentHeightPx

        fun flush() {
            if (current.isNotEmpty()) {
                pages += ReaderPage(
                    fragments = current.toList(),
                    words = current.sumOf { TextStats.countWords(it.text) }
                )
                current = mutableListOf()
            }
            remaining = contentHeightPx
        }

        blocks.forEachIndexed { localIndex, block ->
            val isHeading = block is ContentBlock.Heading
            // Akapity nie dostaja odstepu - rozdziela je wciecie pierwszej linii, tak jak w
            // skladzie ksiazkowym. Oddycha tylko naglowek: nad soba i pod soba.
            val previousWasHeading = blocks.getOrNull(localIndex - 1) is ContentBlock.Heading
            // Pierwszy akapit po naglowku idzie BEZ wciecia - i musi byc tak juz na etapie
            // mierzenia, inaczej paginator policzylby inna liczbe linii niz render narysuje.
            val indentFirstLine = !isHeading && !previousWasHeading
            val style = when {
                isHeading -> headingStyle
                indentFirstLine -> paragraphStyle
                else -> paragraphStyle.copy(textIndent = TextIndent())
            }
            val layout = measure(block.text, style)
            if (layout.lineCount == 0) return@forEachIndexed

            var line = 0
            var spacing = when {
                current.isEmpty() -> 0
                isHeading -> blockSpacingPx * 2
                previousWasHeading -> blockSpacingPx
                else -> 0
            }

            // Naglowek sam na dole strony wyglada jak blad skladu - jesli nie zmiesci sie razem
            // z pierwszymi dwiema liniami tekstu pod soba, przenosimy go w calosci na nastepna.
            if (isHeading && current.isNotEmpty()) {
                val headingHeight = layout.getLineBottom(layout.lineCount - 1)
                val lineHeight = layout.getLineBottom(0)
                if (remaining - spacing - headingHeight < lineHeight * 2) {
                    flush()
                    spacing = 0
                }
            }

            while (line < layout.lineCount) {
                val top = layout.getLineTop(line)
                val available = remaining - spacing

                val lastFitting = lastLineFitting(layout, fromLine = line, top = top, available = available)
                if (lastFitting < line) {
                    // Nawet jedna linia sie nie miesci - zaczynamy od czystej strony.
                    if (current.isEmpty()) return@forEachIndexed // strona mniejsza niz linia: nie zapetlaj sie
                    flush()
                    spacing = 0
                    continue
                }

                val start = layout.getLineStart(line)
                val end = layout.getLineEnd(lastFitting, visibleEnd = true)
                current += PageFragment(
                    blockIndex = blockIndexOffset + localIndex,
                    charStart = start,
                    text = block.text.substring(start, end),
                    isHeading = isHeading,
                    isContinuation = line > 0,
                    indentFirstLine = indentFirstLine && line == 0,
                    spacingBeforePx = spacing
                )

                remaining -= spacing + (layout.getLineBottom(lastFitting) - top).toInt()
                spacing = 0
                line = lastFitting + 1

                if (line < layout.lineCount) flush()
            }
        }

        flush()
        return pages
    }

    private fun measure(text: String, style: TextStyle): TextLayoutResult =
        measurer.measure(
            text = text,
            style = style,
            constraints = Constraints(maxWidth = contentWidthPx)
        )

    /** Ostatnia linia, ktora jeszcze miesci sie w [available] pikselach liczac od [top]. */
    private fun lastLineFitting(
        layout: TextLayoutResult,
        fromLine: Int,
        top: Float,
        available: Int
    ): Int {
        var last = fromLine - 1
        var line = fromLine
        while (line < layout.lineCount && layout.getLineBottom(line) - top <= available) {
            last = line
            line++
        }
        return last
    }
}

/** Numer strony, na ktorej lezy [anchor] - do odtworzenia pozycji po zmianie ustawien. */
fun List<ReaderPage>.pageIndexOf(anchor: ReadingAnchor): Int {
    val index = indexOfLast { page ->
        val a = page.anchor
        a.blockIndex < anchor.blockIndex ||
            (a.blockIndex == anchor.blockIndex && a.charOffset <= anchor.charOffset)
    }
    return if (index < 0) 0 else index
}
