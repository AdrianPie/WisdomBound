package com.lernhero.home.story

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

private val Ink = Color(0xFF120E0A)
private val Panel = Color(0xFF19120C)
private val Frame = Color(0xFFC8A45A)
private val FrameDim = Color(0xFF6B5A38)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val GoldText = Color(0xFFFFD34D)
private val EndlessAccent = Color(0xFFE0654F)

/** Zrodlo pytan do endless: wbudowany bank albo (wkrotce) wlasna talia gracza. */
enum class QuestionSource(val label: String) {
    Builtin("Bank pytan"),
    Custom("Wlasne")
}

/**
 * Konfiguracja walki endless: z czego i jak trudne maja byc pytania. To jest wlasciwy powod, dla
 * ktorego endless istnieje jako osobny tryb - nie chodzi o "fale bez konca", tylko o to, ze gracz
 * moze przyjsc TRENOWAC konkretny temat na konkretnym poziomie.
 *
 * Zakladka [QuestionSource.Custom] jest na razie pusta i mowi to wprost - to miejsce pod wgrywanie
 * wlasnych pytan i generowanie talii przez AI.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EndlessSetupPanel(
    source: QuestionSource,
    difficulty: QuizDifficulty?,
    category: QuizCategory?,
    onSourceChange: (QuestionSource) -> Unit,
    onDifficultyChange: (QuizDifficulty?) -> Unit,
    onCategoryChange: (QuizCategory?) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Panel)
            .border(2.dp, Frame.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SourceTabs(selected = source, onSelect = onSourceChange)

        when (source) {
            QuestionSource.Builtin -> {
                SectionLabel("Poziom pytan")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Chip(
                        label = "Rosnacy",
                        selected = difficulty == null,
                        onClick = { onDifficultyChange(null) }
                    )
                    QuizDifficulty.entries.forEach { entry ->
                        Chip(
                            label = entry.label,
                            selected = difficulty == entry,
                            onClick = { onDifficultyChange(entry) }
                        )
                    }
                }
                Text(
                    text = if (difficulty == null) {
                        "Trudnosc rosnie z kazda fala - klasyczny sprawdzian wytrzymalosci."
                    } else {
                        "Wszystkie pytania na poziomie \"${difficulty.label}\" - trening jednego progu."
                    },
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )

                SectionLabel("Kategoria")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Chip(
                        label = "Losowa",
                        selected = category == null,
                        onClick = { onCategoryChange(null) }
                    )
                    QuizCategory.entries.forEach { entry ->
                        Chip(
                            label = entry.label,
                            selected = category == entry,
                            onClick = { onCategoryChange(entry) }
                        )
                    }
                }

                StartButton(onStart = onStart)
            }

            QuestionSource.Custom -> CustomComingSoon()
        }
    }
}

@Composable
private fun SourceTabs(selected: QuestionSource, onSelect: (QuestionSource) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        QuestionSource.entries.forEach { entry ->
            val isSelected = entry == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) Color(0xFF3A2A18) else Ink)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) GoldText else FrameDim,
                        shape = RoundedCornerShape(9.dp)
                    )
                    .clickable { onSelect(entry) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.label,
                    color = if (isSelected) GoldText else Muted,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = GoldText,
        fontFamily = FontPixel(),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp
    )
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) Color(0xFF0B0805) else Cream,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) GoldText else Ink)
            .border(
                width = 1.dp,
                color = if (selected) GoldText else FrameDim,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun StartButton(onStart: () -> Unit) {
    Text(
        text = "ROZPOCZNIJ",
        color = Color.White,
        fontFamily = FontPixel(),
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EndlessAccent)
            .border(2.dp, Color(0xFFFF8A6B), RoundedCornerShape(10.dp))
            .clickable(onClick = onStart)
            .padding(vertical = 14.dp)
    )
}

/**
 * Placeholder wlasnych talii. Mowi wprost, czego tu jeszcze nie ma - pusty ekran bez slowa
 * wyjasnienia gracz odczytalby jako zepsuty, a nie jako "w budowie".
 */
@Composable
private fun CustomComingSoon() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Ink)
            .border(1.dp, FrameDim.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "📝", fontSize = 30.sp)
        Text(
            text = "WLASNE PYTANIA",
            color = GoldText,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Text(
            text = "Tu wgrasz wlasna talie - notatki, kartkowke, material na egzamin - albo dasz " +
                "temat i AI ulozy z niego pytania. Potem trenujesz to w endless jak kazdy inny bank.",
            color = Muted,
            fontFamily = FontFirst(),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "WKROTCE",
            color = Muted,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF241E18))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}
