package com.lernhero.reader.reflow

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.lernhero.domain.reading.BookContent
import com.lernhero.domain.reading.ReaderSettings
import com.lernhero.domain.reading.ReaderTypeface
import com.lernhero.domain.reading.ReadingAnchor

/**
 * Czytnik reflow - tekst jest prawdziwym tekstem, wiec da sie go przeskalowac, zlamac pod
 * dowolny ekran i trafic w konkretne slowo palcem.
 *
 * Paginacja liczy sie tutaj, a nie w modelu: strona nie istnieje, dopoki nie znamy rozmiaru
 * ekranu i ustawien typografii. Dlatego pozycje w ksiazce oddajemy na zewnatrz jako
 * [ReadingAnchor] (blok + znak), nie jako numer strony.
 */
@Composable
fun ReflowReader(
    content: BookContent,
    settings: ReaderSettings,
    startAnchor: ReadingAnchor,
    palette: ReaderPalette,
    /** Aktualne zaznaczenie - stan nalezy do wywolujacego (popup tlumaczenia zyje poza tym Composable). */
    selection: WordSelection?,
    onAnchorChanged: (ReadingAnchor) -> Unit,
    /** Wolane przy przejsciu na NOWA strone do przodu - stad biora sie slowa do statystyk i WPM. */
    onPageAdvanced: (words: Int) -> Unit,
    onWordTapped: (WordSelection) -> Unit,
    onToggleChrome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val paragraphStyle = rememberParagraphStyle(settings, palette)
    val headingStyle = rememberHeadingStyle(settings, palette)

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(palette.background)) {
        val horizontalPadding = settings.horizontalMarginDp.dp
        val verticalPadding = 56.dp

        val contentWidthPx = with(density) { (maxWidth - horizontalPadding * 2).roundToPx() }
        val contentHeightPx = with(density) { (maxHeight - verticalPadding * 2).roundToPx() }
        val blockSpacingPx = with(density) { 10.dp.roundToPx() }

        // Repaginacja przy kazdej zmianie tekstu, rozmiaru ekranu albo typografii. Pozycja
        // przezywa to bez szwanku, bo wracamy do kotwicy znakowej, nie do numeru strony.
        val pages = remember(content, contentWidthPx, contentHeightPx, settings) {
            TextPaginator(
                measurer = measurer,
                paragraphStyle = paragraphStyle,
                headingStyle = headingStyle,
                contentWidthPx = contentWidthPx,
                contentHeightPx = contentHeightPx,
                blockSpacingPx = blockSpacingPx
            ).paginate(content.blocks)
        }

        if (pages.isEmpty()) {
            CircularProgressIndicator(
                color = palette.muted,
                modifier = Modifier.align(Alignment.Center)
            )
            return@BoxWithConstraints
        }

        val pagerState = rememberPagerState(
            initialPage = pages.pageIndexOf(startAnchor),
            pageCount = { pages.size }
        )

        // Ustawienia sie zmienily -> nowa paginacja -> wroc na te sama kotwice, nie na ten
        // sam numer strony (przy innej czcionce numer wskazywalby inny fragment ksiazki).
        LaunchedEffect(pages) {
            pagerState.scrollToPage(pages.pageIndexOf(startAnchor).coerceAtMost(pages.lastIndex))
        }

        var furthestPage by remember(pages) { mutableStateOf(pagerState.currentPage) }
        LaunchedEffect(pagerState, pages) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                onAnchorChanged(pages[page].anchor)
                // Liczymy tylko ruch do przodu - kartkowanie w tyl nie ma dublowac slow w statystykach.
                if (page > furthestPage) {
                    for (i in (furthestPage + 1)..page) onPageAdvanced(pages[i].words)
                    furthestPage = page
                }
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pageIndex ->
            ReaderPageView(
                page = pages[pageIndex],
                content = content,
                settings = settings,
                palette = palette,
                paragraphStyle = paragraphStyle,
                headingStyle = headingStyle,
                selection = selection,
                horizontalPadding = horizontalPadding,
                verticalPadding = verticalPadding,
                onWordTapped = onWordTapped,
                onEmptyTap = onToggleChrome
            )
        }
    }
}

@Composable
private fun ReaderPageView(
    page: ReaderPage,
    content: BookContent,
    settings: ReaderSettings,
    palette: ReaderPalette,
    paragraphStyle: TextStyle,
    headingStyle: TextStyle,
    selection: WordSelection?,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    verticalPadding: androidx.compose.ui.unit.Dp,
    onWordTapped: (WordSelection) -> Unit,
    onEmptyTap: () -> Unit
) {
    // Dotkniecie gdziekolwiek POZA tekstem (marginesy, wolne miejsce pod ostatnim akapitem)
    // pokazuje chrome. Bez tego panel dalby sie otworzyc tylko przez trafienie w spacje.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(page) { detectTapGestures { onEmptyTap() } }
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        val density = LocalDensity.current
        page.fragments.forEach { fragment ->
            // Odstep bierzemy z paginacji, nie ze stalej w renderze - inaczej strona rysuje sie
            // wyzsza (albo nizsza) niz ta, ktora zostala zmierzona, i tekst ucieka poza ekran.
            if (fragment.spacingBeforePx > 0) {
                Spacer(Modifier.height(with(density) { fragment.spacingBeforePx.toDp() }))
            }
            if (fragment.isHeading) {
                Text(
                    text = fragment.text,
                    style = headingStyle,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                FragmentText(
                    fragment = fragment,
                    content = content,
                    settings = settings,
                    palette = palette,
                    baseStyle = paragraphStyle,
                    selection = selection,
                    onWordTapped = onWordTapped,
                    onEmptyTap = onEmptyTap
                )
            }
        }
    }
    }
}

/**
 * Jeden fragment akapitu na stronie. Trzyma wlasny [TextLayoutResult], bo bez niego nie da sie
 * zamienic wspolrzednych dotkniecia na offset znaku - a to jest caly mechanizm tap-na-slowo.
 */
@Composable
private fun FragmentText(
    fragment: PageFragment,
    content: BookContent,
    settings: ReaderSettings,
    palette: ReaderPalette,
    baseStyle: TextStyle,
    selection: WordSelection?,
    onWordTapped: (WordSelection) -> Unit,
    onEmptyTap: () -> Unit
) {
    var layout by remember(fragment) { mutableStateOf<TextLayoutResult?>(null) }
    // Pozycja tego fragmentu w oknie - potrzebna, zeby zamienic dotkniecie (lokalne dla tego
    // Text) na wspolrzedne, pod ktorymi popup tlumaczenia moze sie zakotwiczyc.
    var rootOffset by remember(fragment) { mutableStateOf(Offset.Zero) }

    // Wciecie pierwszej linii tylko wtedy, gdy akapit naprawde tu sie zaczyna. Dalszy ciag
    // akapitu przeniesiony na kolejna strone dostalby inaczej wciecie w srodku zdania.
    val style = if (fragment.isContinuation) {
        baseStyle.copy(textIndent = TextIndent())
    } else {
        baseStyle
    }

    val highlighted = remember(fragment, selection) {
        buildHighlighted(fragment, selection, palette)
    }

    Text(
        text = highlighted,
        style = style,
        onTextLayout = { layout = it },
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { rootOffset = it.positionInRoot() }
            .pointerInput(fragment) {
                detectTapGestures { position ->
                    val result = layout ?: return@detectTapGestures
                    val offset = result.getOffsetForPosition(position)
                    val range = WordPicker.wordRangeAt(fragment.text, offset)
                    if (range == null) {
                        onEmptyTap()
                        return@detectTapGestures
                    }
                    // Zdanie bierzemy z CALEGO bloku, nie z fragmentu - zdanie potrafi przejsc
                    // przez lamanie strony, a kontekst uciety w polowie jest bezwartosciowy.
                    val blockText = content.blocks.getOrNull(fragment.blockIndex)?.text.orEmpty()
                    val globalWordStart = fragment.charStart + range.first
                    val sentenceRange = WordPicker.sentenceRangeAt(blockText, globalWordStart)
                    val wordBox = result.getBoundingBox(range.first)
                    onWordTapped(
                        WordSelection(
                            scope = SelectionScope.Word,
                            word = fragment.text.substring(range.first, range.last + 1),
                            sentence = blockText.substring(sentenceRange).trim(),
                            blockIndex = fragment.blockIndex,
                            wordCharStart = globalWordStart,
                            sentenceCharStart = sentenceRange.first,
                            sentenceCharEnd = sentenceRange.last + 1,
                            anchor = rootOffset + Offset((wordBox.left + wordBox.right) / 2f, wordBox.top)
                        )
                    )
                }
            }
    )
}

private fun buildHighlighted(
    fragment: PageFragment,
    selection: WordSelection?,
    palette: ReaderPalette
): AnnotatedString {
    if (selection == null || selection.blockIndex != fragment.blockIndex) {
        return AnnotatedString(fragment.text)
    }
    val start = selection.highlightStart - fragment.charStart
    val end = selection.highlightEnd - fragment.charStart
    // Zdanie moze przekroczyc granice fragmentu (lamanie strony w jego srodku) - wtedy nie
    // podswietlamy niczego zamiast rysowac obcięty, mylacy fragment podswietlenia.
    if (start < 0 || end > fragment.text.length || start >= end) {
        return AnnotatedString(fragment.text)
    }

    return buildAnnotatedString {
        append(fragment.text)
        addStyle(SpanStyle(background = palette.selection), start, end)
    }
}

@Composable
private fun rememberParagraphStyle(settings: ReaderSettings, palette: ReaderPalette): TextStyle =
    TextStyle(
        color = palette.text,
        fontSize = settings.fontSizeSp.sp,
        lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
        fontFamily = settings.typeface.toFontFamily(),
        textAlign = if (settings.justify) TextAlign.Justify else TextAlign.Start,
        textIndent = TextIndent(firstLine = 1.6.em)
    )

@Composable
private fun rememberHeadingStyle(settings: ReaderSettings, palette: ReaderPalette): TextStyle =
    TextStyle(
        color = palette.text,
        fontSize = (settings.fontSizeSp * 1.35f).sp,
        lineHeight = (settings.fontSizeSp * 1.35f * 1.3f).sp,
        fontFamily = settings.typeface.toFontFamily(),
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

private fun ReaderTypeface.toFontFamily(): FontFamily = when (this) {
    // Rodziny systemowe, nie wlasne pliki - serif z systemu wyglada naturalnie na obu
    // platformach i nie dokłada wagi do zasobow aplikacji.
    ReaderTypeface.Serif -> FontFamily.Serif
    ReaderTypeface.Sans -> FontFamily.SansSerif
}
