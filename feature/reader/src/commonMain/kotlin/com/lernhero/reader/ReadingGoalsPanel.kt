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
fun ReadingGoalsPanel(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<LibraryViewModel>()
    val state by viewModel.uiState.collectAsState()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Daily Goals - Reading",
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "dzis ${state.readingToday.minutes} min, ${state.readingToday.pages} stron",
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
        }

        ReadingGoalCatalog.DAILY.forEach { goal ->
            val progress = goal.progress(state.readingToday).coerceAtMost(goal.threshold)
            val complete = goal.isComplete(state.readingToday)
            val claimed = state.claimedToday(goal)
            val ratio = if (goal.threshold <= 0) 0f else progress.toFloat() / goal.threshold.toFloat()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Ink)
                    .border(1.5.dp, if (complete && !claimed) Gold else GoldDim, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            color = Gold,
                            fontFamily = FontPixel(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(text = goal.description, color = Muted, fontFamily = FontFirst(), fontSize = 10.sp)
                    }
                    Text(
                        text = "+${goal.reward} zl",
                        color = GoldFrame,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoldDim.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio.coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (complete) Green else GoldFrame)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val unit = if (goal.kind == ReadingGoalKind.Minutes) " min" else " str."
                    Text(
                        text = "$progress / ${goal.threshold}$unit",
                        color = Muted,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp
                    )

                    val enabled = complete && !claimed
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (enabled) Color(0xFFB4451F) else Ink)
                            .border(1.dp, if (enabled) Gold else GoldDim, RoundedCornerShape(8.dp))
                            .clickable(enabled = enabled) { viewModel.claim(goal) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when {
                                claimed -> "Claimed"
                                complete -> "Claim"
                                else -> "Too Early"
                            },
                            color = when {
                                claimed -> Green
                                enabled -> Color.White
                                else -> Muted
                            },
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        ReadingTotals(
            minutes = state.readingStats.totalMinutes,
            pages = state.readingStats.totalPages,
            words = state.readingStats.totalWords,
            wpm = state.readingStats.averageWpm
        )
    }
}

/** Sumaryczne liczby z czytania - w Czytelni zostal juz tylko ich skrot w naglowku. */
@Composable
private fun ReadingTotals(minutes: Int, pages: Int, words: Int, wpm: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Ink)
            .border(1.dp, GoldDim, RoundedCornerShape(10.dp))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Total(label = "Minutes", value = "$minutes")
        Total(label = "Pages", value = "$pages")
        Total(label = "Words", value = "$words")
        Total(label = "Pace", value = "$wpm/min")
    }
}

@Composable
private fun Total(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Gold, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = label, color = Muted, fontFamily = FontFirst(), fontSize = 9.sp)
    }
}
