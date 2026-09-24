package com.lernhero.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.reading.SavedWord
import com.lernhero.reader.reflow.SelectionScope
import com.lernhero.reader.reflow.WordSelection
import com.lernhero.reader.translate.TranslationResult
import com.lernhero.reader.translate.TranslationSource
import com.lernhero.domain.monetization.PremiumLimits
import com.lernhero.shared.monetization.PremiumPaywall
import com.lernhero.shared.monetization.TranslationQuota
import com.lernhero.shared.theme.FontFirst
import org.koin.compose.koinInject
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
 * [WordSelection.anchor], ktory jest juz we wspolrzednych okna. Domyslnie staje NAD slowem;
 * gdy nad slowem nie ma miejsca (slowo przy gorze ekranu / pod wcieciem aparatu), przeskakuje
 * POD slowo - inaczej karta wjezdzala na notch i na samo slowo.
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
    val quota = koinInject<TranslationQuota>()
    var paywallOpen by remember { mutableStateOf(false) }
    var freeLeft by remember { mutableStateOf<Int?>(null) }

    var result by remember(selection.scope, selection.selectedText) {
        mutableStateOf<TranslationResult?>(null)
    }
    LaunchedEffect(selection.scope, selection.selectedText) {
        val today = todayDateKey()
        if (!quota.canTranslate(today)) {
            result = TranslationResult.LimitReached
            return@LaunchedEffect
        }
        val translated = translationSource.translate(selection.selectedText)
        if (translated is TranslationResult.Translated) {
            quota.consume(today)
            freeLeft = if (quota.isUnlimited) null else quota.remaining(today)
        }
        result = translated
    }

    if (paywallOpen) {
        PremiumPaywall(
            subscription = koinInject(),
            reason = "You've used today's ${PremiumLimits.FREE_TRANSLATIONS_PER_DAY} free translations.",
            onDismiss = { paywallOpen = false }
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val popupWidthDp = 240.dp
        val maxWidthPx = with(density) { maxWidth.roundToPx() }
        val gapPx = with(density) { 14.dp.roundToPx() }
        val marginPx = with(density) { 8.dp.roundToPx() }
        val maxHeightPx = with(density) { maxHeight.roundToPx() }
        // Bezpieczny margines u gory: pasek statusu + wciecie aparatu. W trybie czytania pasek
        // jest schowany, ale notch fizycznie zostaje - stad safeDrawing, a nie systemBars.
        val safeTopPx = WindowInsets.safeDrawing.getTop(density).coerceAtLeast(marginPx)
        val anchorX = selection.anchor.x
        val anchorY = selection.anchor.y
        val wordBottomPx = selection.wordBottomPx

        Column(
            modifier = Modifier
                .widthIn(max = popupWidthDp)
                // Wysokosc karty zalezy od dlugosci tlumaczenia, a pion popupa (nad slowem) od tej
                // wysokosci. Liczymy go z FAKTYCZNEGO pomiaru w tym samym przebiegu ukladu: wariant
                // "zmierz w stanie, przesun w recompozycji" ladowal pierwsza klatke pod slowem
                // (wysokosc == 0) i dopiero nastepna przeskakiwal nad nie - rzadko, ale widocznie.
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val x = (anchorX - placeable.width / 2f).roundToInt()
                        .coerceIn(marginPx, (maxWidthPx - placeable.width - marginPx).coerceAtLeast(marginPx))

                    // Preferujemy NAD slowem. Gdy tam nie ma miejsca (karta wjechalaby pod notch),
                    // stajemy POD slowem i docinamy do dolnej krawedzi ekranu.
                    val yAbove = (anchorY - placeable.height - gapPx).roundToInt()
                    val y = if (yAbove >= safeTopPx) {
                        yAbove
                    } else {
                        (wordBottomPx + gapPx).roundToInt()
                            .coerceIn(safeTopPx, (maxHeightPx - placeable.height - marginPx).coerceAtLeast(safeTopPx))
                    }

                    layout(placeable.width, placeable.height) {
                        placeable.place(x, y)
                    }
                }
                .clip(RoundedCornerShape(14.dp))
                .background(PopupBg)
                .border(1.dp, PopupBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = when (val r = result) {
                        is TranslationResult.Translated -> r.text
                        TranslationResult.Unavailable -> "Translator isn't connected yet"
                        TranslationResult.Failed -> "Translation failed"
                        TranslationResult.LimitReached ->
                            "That's today's ${PremiumLimits.FREE_TRANSLATIONS_PER_DAY} free translations. " +
                                "Premium translates without limits."
                        null -> "..."
                    },
                    color = if (result is TranslationResult.Unavailable || result is TranslationResult.LimitReached) {
                        Color(0xFF8A93A0)
                    } else {
                        Color.White
                    },
                    fontFamily = FontFirst(),
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                PopupIconButton(symbol = "×", onClick = onDismiss)
            }

            if (result is TranslationResult.LimitReached) {
                Text(
                    text = "👑 GO PREMIUM",
                    color = Color(0xFF1C1408),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFD34D))
                        .clickable { paywallOpen = true }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                )
                return@Column
            }

            freeLeft?.let { left ->
                Text(
                    text = "$left free translations left today",
                    color = Color(0xFF8A93A0),
                    fontFamily = FontFirst(),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
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
