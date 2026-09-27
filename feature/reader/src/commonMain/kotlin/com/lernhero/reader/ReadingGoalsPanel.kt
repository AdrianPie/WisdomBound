package com.lernhero.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.reading.ReadingGoalCatalog
import com.lernhero.domain.reading.ReadingGoalKind
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.koin.compose.viewmodel.koinViewModel

private val Ink = Color(0xFF0F0B08)
private val Gold = Color(0xFFFFD100)
private val GoldFrame = Color(0xFFC8A45A)
private val GoldDim = Color(0xFF6B5A38)
private val Muted = Color(0xFF9D8B6A)
private val Green = Color(0xFF4ADE80)

/**
 * Cele dzienne za czytanie - **zakladka Postep**, nie Czytelnia. Byly przez chwile w Czytelni i to
 * bylo zle miejsce: gracz wchodzil po ksiazke, a dostawal liste zadan przed polka. Nagrody i
 * osiagniecia maja jedno wspolne miejsce, a Czytelnia ma byc czytelnia.
 *
 * Panel siega po [LibraryViewModel] sam, zamiast dostawac stan z gory - zrodlem prawdy o czytaniu
 * jest ten sam ViewModel, ktory obsluguje biblioteke, wiec odebranie nagrody tutaj od razu widac
 * w Czytelni i odwrotnie.
 */
@Composable
fun ReadingGoalsPanel(modifier: Modifier = Modifier, onRewardClaimed: () -> Unit = {}) {
    val viewModel = koinViewModel<LibraryViewModel>()
    val state by viewModel.uiState.collectAsState()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DailyReadingCard(
            today = state.readingToday,
            claimed = state::claimedToday,
            onClaim = { goal -> viewModel.claim(goal, onRewardClaimed) }
        )
        state.message?.let {
            Text(text = it, color = Color(0xFF6FD04A), fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        ReadingTotals(
            minutes = state.readingStats.activeSeconds / 60,
            pages = state.readingStats.totalPages,
            words = state.readingStats.totalWords,
            wpm = state.readingStats.averageWpm
        )
    }
}

/** Sumaryczne liczby z czytania - w Czytelni zostal juz tylko ich skrot w naglowku. */
@Composable
private fun ReadingTotals(minutes: Int, pages: Int, words: Int, wpm: Int) {
    PaperPanel {
        Text("Your Reading", color = PaperInkColor, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 16.sp, lineHeight = 18.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Total(label = "Minutes", value = "$minutes", modifier = Modifier.weight(1f))
            Total(label = "Pages", value = "$pages", modifier = Modifier.weight(1f))
            Total(label = "Words", value = "$words", modifier = Modifier.weight(1f))
            Total(label = "Pace/min", value = "$wpm", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun Total(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PaperCardFill)
            .border(1.dp, PaperCardEdge, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        PaperCaption(label)
        Text(text = value, color = PaperInkColor, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 17.sp, lineHeight = 19.sp, maxLines = 1)
    }
}
