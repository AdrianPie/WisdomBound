package com.lernhero.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.reading.SavedWord
import com.lernhero.domain.reading.VocabularyBox
import com.lernhero.domain.reading.VocabularyDeck
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.koin.compose.viewmodel.koinViewModel

private val Bg = Color(0xFF120E0A)
private val Panel = Color(0xFF1A1410)
private val Ink = Color(0xFF0F0B08)
private val Gold = Color(0xFFFFD100)
private val GoldFrame = Color(0xFFC8A45A)
private val GoldDim = Color(0xFF6B5A38)
private val Muted = Color(0xFF9D8B6A)
private val Cream = Color(0xFFEADFC8)
private val Green = Color(0xFF4ADE80)
private val Amber = Color(0xFFE0913A)
private val Red = Color(0xFFE56A5D)

/** Ile kafelkow miesci sie w rzedzie. Kwadraty, wiec siatka, a nie lista - patrz [WordTile]. */
private const val GRID_COLUMNS = 4

/**
 * "Slowka" - osobna instancja obok Czytelni, bo to zupelnie inna robota niz czytanie: tu sie
 * powtarza, a nie czyta. Czytelnia tylko podaje link i licznik.
 *
 * Fiszki leza w dwoch archiwach ([VocabularyBox]) i kazda ma na kafelku wlasne liczniki: ile razy
 * z rzedu poszlo dobrze, ile razy w sumie dobrze i ile zle. Po
 * [VocabularyDeck.MASTERY_STREAK] trafieniach z rzedu slowo przenosi sie samo do "Opanowane" -
 * gracz nie musi (i nie moze) przesuwac go recznie, bo to licznik ma decydowac, a nie humor.
 */
@Composable
fun VocabularyScreen(
    onBack: () -> Unit,
    insets: PaddingValues = PaddingValues(0.dp)
) {
    val viewModel = koinViewModel<LibraryViewModel>()
    val state by viewModel.uiState.collectAsState()

    var box by remember { mutableStateOf(VocabularyBox.Learning) }
    var selected by remember { mutableStateOf<SavedWord?>(null) }

    val learning = remember(state.savedWords) { VocabularyDeck.learning(state.savedWords) }
    val mastered = remember(state.savedWords) { VocabularyDeck.mastered(state.savedWords) }

    // W "Do nauki" na przod ida slowa najslabiej idace (najwiecej pomylek), bo to one wymagaja
    // powtorki - sortowanie po dacie zapisu spychaloby je na koniec wraz z kazda nowa fiszka.
    val words = remember(box, learning, mastered) {
        when (box) {
            VocabularyBox.Learning -> learning.sortedWith(
                compareByDescending<SavedWord> { it.wrongTotal }.thenByDescending { it.savedAt }
            )
            VocabularyBox.Mastered -> mastered.sortedByDescending { it.masteredAt ?: 0L }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Header(
                learning = learning.size,
                mastered = mastered.size,
                onBack = onBack
            )

            BoxTabs(
                selected = box,
                learningCount = learning.size,
                masteredCount = mastered.size,
                onSelect = { box = it }
            )

            if (words.isEmpty()) {
                EmptyHint(box)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(GRID_COLUMNS),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(words, key = { it.id }) { word ->
                        WordTile(word = word, onClick = { selected = word })
                    }
                }
            }
        }
    }

    selected?.let { word ->
        WordDetailDialog(
            word = word,
            onDismiss = { selected = null },
            onRemove = {
                viewModel.removeWord(word)
                selected = null
            }
        )
    }
}

@Composable
private fun Header(learning: Int, mastered: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "‹",
            color = Gold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onBack)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Words",
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "$learning do nauki  ·  $mastered opanowanych",
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun BoxTabs(
    selected: VocabularyBox,
    learningCount: Int,
    masteredCount: Int,
    onSelect: (VocabularyBox) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, GoldDim, RoundedCornerShape(10.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        VocabularyBox.entries.forEach { entry ->
            val active = entry == selected
            val count = if (entry == VocabularyBox.Learning) learningCount else masteredCount
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) Color(0xFF3A2A14) else Color.Transparent)
                    .border(
                        width = if (active) 1.5.dp else 0.dp,
                        color = if (active) GoldFrame else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(entry) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${entry.label} ($count)",
                    color = if (active) Gold else Muted,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Kwadratowy kafelek jednej fiszki. Miesci slowo, tlumaczenie i - najwazniejsze - pasek serii:
 * [VocabularyDeck.MASTERY_STREAK] kropek, z ktorych zapalone to aktualna seria trafien. Gracz od
 * razu widzi, ile mu brakuje do przeniesienia slowa do archiwum, bez czytania zadnej liczby.
 */
@Composable
private fun WordTile(word: SavedWord, onClick: () -> Unit) {
    val mastered = word.masteredAt != null

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(Panel)
            .border(1.5.dp, if (mastered) Green.copy(alpha = 0.7f) else GoldDim, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = word.text,
                color = Cream,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = word.translation.ifBlank { "—" },
                color = if (word.translation.isBlank()) Amber else Muted,
                fontFamily = FontFirst(),
                fontSize = 9.sp,
                lineHeight = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (mastered) {
            Text(
                text = "✓ umiem",
                color = Green,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp
            )
        } else {
            StreakDots(streak = word.correctStreak)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "✓${word.correctTotal}", color = Green, fontFamily = FontFirst(), fontSize = 8.sp)
            Text(text = "✗${word.wrongTotal}", color = Red, fontFamily = FontFirst(), fontSize = 8.sp)
        }
    }
}

@Composable
private fun StreakDots(streak: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(VocabularyDeck.MASTERY_STREAK) { index ->
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (index < streak) Gold else GoldDim.copy(alpha = 0.45f))
            )
        }
    }
}

@Composable
private fun EmptyHint(box: VocabularyBox) {
    Text(
        text = when (box) {
            VocabularyBox.Learning ->
                "No words to learn yet. Tap a word in a book, translate it, and tap the tab - " +
                    "it will land here and you'll be able to train it in Endless mode."
            VocabularyBox.Mastered ->
                "Nothing here yet. A word lands in this archive after " +
                    "${VocabularyDeck.MASTERY_STREAK} correct answers in a row in the quiz."
        },
        color = Muted,
        fontFamily = FontFirst(),
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 20.dp)
    )
}

/** Zdanie-kontekst i pelne liczniki nie mieszcza sie na kafelku - stad podglad po dotknieciu. */
@Composable
private fun WordDetailDialog(word: SavedWord, onDismiss: () -> Unit, onRemove: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Panel)
                .border(2.dp, GoldFrame, RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = word.text,
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = word.translation.ifBlank { "bez tlumaczenia" },
                color = if (word.translation.isBlank()) Amber else Cream,
                fontFamily = FontFirst(),
                fontSize = 13.sp
            )

            if (word.sentence.isNotBlank()) {
                Text(
                    text = "\"${word.sentence}\"",
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Ink)
                        .padding(10.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailStat(label = "Correct", value = "${word.correctTotal}", color = Green)
                DetailStat(label = "Wrong", value = "${word.wrongTotal}", color = Red)
                DetailStat(label = "Streak", value = "${word.correctStreak}", color = Gold)
                DetailStat(
                    label = "To Archive",
                    value = if (word.masteredAt != null) "—" else "${VocabularyDeck.stepsToMastery(word)}",
                    color = GoldFrame
                )
            }

            if (word.bookTitle.isNotBlank()) {
                Text(text = "from: ${word.bookTitle}", color = GoldDim, fontFamily = FontFirst(), fontSize = 10.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogButton(text = "Delete", accent = Red, modifier = Modifier.weight(1f), onClick = onRemove)
                DialogButton(text = "Close", accent = GoldFrame, modifier = Modifier.weight(1f), onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun DetailStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(text = label, color = Muted, fontFamily = FontFirst(), fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun DialogButton(text: String, accent: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Ink)
            .border(1.5.dp, accent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = accent, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
