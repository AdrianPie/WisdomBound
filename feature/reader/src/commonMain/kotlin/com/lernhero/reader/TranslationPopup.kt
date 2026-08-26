package com.lernhero.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.reading.SavedWord
import com.lernhero.reader.reflow.SelectionScope
import com.lernhero.reader.reflow.WordSelection
import com.lernhero.reader.translate.TranslationResult
import com.lernhero.reader.translate.TranslationSource
import com.lernhero.shared.theme.FontFirst
import kotlin.math.roundToInt

private val PopupBg = Color(0xFF1C2430)
private val PopupBorder = Color(0xFF34404F)
private val AccentBlue = Color(0xFF6AA9FF)

/**
 * Karta tlumaczenia zakotwiczona nad dotknietym slowem - tak jak w Dicto: dotkniecie slowa
 * pokazuje jego tlumaczenie z przyciskiem "rozszerz na cale zdanie", a rozszerzone zaznaczenie
 * pokazuje tlumaczenie calego zdania bez tego przycisku (juz jest rozszerzone).
 *
 * [modifier] powinien wypelniac caly ekran czytnika - popup pozycjonuje sie sam wzgledem
 * [WordSelection.anchor], ktory jest juz we wspolrzednych okna.
 */
@Composable
fun TranslationPopup(
    selection: WordSelection,
    translationSource: TranslationSource,
    onDismiss: () -> Unit,
    onExpandToSentence: () -> Unit,
    onSave: (SavedWord) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    var result by remember(selection.scope, selection.selectedText) {
        mutableStateOf<TranslationResult?>(null)
    }
    LaunchedEffect(selection.scope, selection.selectedText) {
        result = translationSource.translate(selection.selectedText)
    }

    // Wysokosc karty poznajemy dopiero po zlozeniu (tekst tlumaczenia ma zmienna dlugosc), wiec
    // pion liczymy w dwoch krokach: najpierw zakladamy typowa wysokosc, potem korygujemy po
    // faktycznym pomiarze - bez tego dluzsze tlumaczenie ucieka czescia karty poza gore ekranu.
    var measuredHeightPx by remember { mutableStateOf(0) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val popupWidthDp = 240.dp
        val popupWidthPx = with(density) { popupWidthDp.roundToPx() }
        val maxXPx = (with(density) { maxWidth.roundToPx() } - popupWidthPx - 16).coerceAtLeast(16)

        val xPx = (selection.anchor.x - popupWidthPx / 2f).roundToInt().coerceIn(16, maxXPx)
        val gapPx = with(density) { 14.dp.roundToPx() }
        val yPx = (selection.anchor.y - measuredHeightPx - gapPx).roundToInt().coerceAtLeast(with(density) { 8.dp.roundToPx() })

        Column(
            modifier = Modifier
                .offset { IntOffset(xPx, yPx) }
                .widthIn(max = popupWidthDp)
                .onGloballyPositioned { measuredHeightPx = it.size.height }
                .clip(RoundedCornerShape(14.dp))
                .background(PopupBg)
                .border(1.dp, PopupBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = when (val r = result) {
                        is TranslationResult.Translated -> r.text
                        TranslationResult.Unavailable -> "Tlumacz nie jest jeszcze podlaczony"
                        TranslationResult.Failed -> "Nie udalo sie przetlumaczyc"
                        null -> "..."
                    },
                    color = if (result is TranslationResult.Unavailable) Color(0xFF8A93A0) else Color.White,
                    fontFamily = FontFirst(),
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                PopupIconButton(symbol = "×", onClick = onDismiss)
            }

            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PopupIconButton(symbol = "🔖") {
                    onSave(
                        SavedWord(
                            id = "${selection.blockIndex}_${selection.highlightStart}_${selection.scope}",
                            text = selection.selectedText,
                            translation = (result as? TranslationResult.Translated)?.text.orEmpty(),
                            sentence = selection.sentence,
                            bookId = "",
                            bookTitle = "",
                            savedAt = 0L,
                            isSentence = selection.scope == SelectionScope.Sentence
                        )
                    )
                }
                if (selection.scope == SelectionScope.Word) {
                    PopupIconButton(symbol = "≡", onClick = onExpandToSentence)
                }
            }
        }
    }
}

@Composable
private fun PopupIconButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color(0xFF2A3444))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
