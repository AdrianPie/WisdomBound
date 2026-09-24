package com.lernhero.home.story

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.progress.CategoryLevels
import com.lernhero.domain.progress.CategoryMastery
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlin.math.roundToInt

/**
 * Lista tematow do wyboru przed wejsciem w endless - kazdy z wlasnym poziomem opanowania.
 *
 * Kafel pokazuje nie sam poziom, tylko to, CO on zmienia: od jakiej trudnosci ruszy quiz i o ile
 * wiecej placi. Bez tych dwoch liczb poziom bylby odznaka, a ma byc powodem, dla ktorego gracz
 * wybiera akurat ten temat.
 *
 * Kolor odznaki idzie za progiem trudnosci ([CategoryMastery.startingDifficulty]), a nie za samym
 * numerem - dzieki temu jednym rzutem oka widac, ktore tematy sa juz "rozpedzone", nawet gdy
 * poziomy roznia sie o jeden.
 */
private val Ink = Color(0xFF120E0A)
private val FrameDim = Color(0xFF6B5A38)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val GoldText = Color(0xFFFFD34D)

/** Barwa odznaki wedlug progu, na ktorym startuja pytania - zielony rookie, czerwony ekspert. */
private val TierColors = listOf(
    Color(0xFF4FA96B), // Easy
    Color(0xFF2F8FE0), // Medium
    Color(0xFF9B6BE0), // Hard
    Color(0xFFE0654F) // Expert
)

@Composable
fun CategoryMasteryList(
    categories: List<QuizCategory>,
    levels: CategoryLevels,
    selected: QuizCategory?,
    onSelect: (QuizCategory?) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Czy podswietlic wiersz "wszystkie tematy". Domyslnie tak, gdy zaden temat nie jest wybrany -
     * ale lista bywa teraz czescia WIEKSZEGO wyboru ([com.lernhero.home.fight.MaterialSheet]),
     * w ktorym brak tematu moze znaczyc "gramy z wlasnej talii". Wtedy nic tutaj nie jest wybrane
     * i mieszanka nie moze tego udawac.
     */
    mixedSelected: Boolean = selected == null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // "Wszystkie tematy" nie ma poziomu i to jest uczciwe: skoro pytania lecą z calego banku,
        // nie ma czego awansowac. Zostaje jako szybkie wejscie bez zobowiazan.
        MixedRow(selected = mixedSelected, onClick = { onSelect(null) })

        categories.forEach { category ->
            val level = levels.levelOf(category)
            CategoryRow(
                category = category,
                level = level,
                selected = selected == category,
                onClick = { onSelect(category) }
            )
        }
    }
}

@Composable
private fun MixedRow(selected: Boolean, onClick: () -> Unit) {
    RowShell(selected = selected, onClick = onClick) {
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFF3A2A18))
                .border(2.dp, FrameDim, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "?", color = Muted, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(
                text = "All topics",
                color = Cream,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "Mixed questions - no topic level",
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun CategoryRow(
    category: QuizCategory,
    level: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val difficulty = CategoryMastery.startingDifficulty(level)
    val badge = TierColors[difficulty.ordinal.coerceIn(0, TierColors.lastIndex)]
    val bonusPercent = ((CategoryMastery.rewardMultiplier(level) - 1f) * 100).roundToInt()

    RowShell(selected = selected, onClick = onClick) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(badge, badge.copy(alpha = 0.65f))))
                .border(2.dp, if (selected) GoldText else badge, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$level",
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(
                text = category.label,
                color = Cream,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Starts at ${difficulty.label}",
                    color = badge,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
                if (bonusPercent > 0) {
                    Text(
                        text = "+$bonusPercent% rewards",
                        color = GoldText,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/** Wspolna oprawa wiersza - zaznaczony dostaje zlota ramke, reszta stoi wygaszona. */
@Composable
private fun RowShell(selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFF3A2A18) else Ink)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) GoldText else FrameDim,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}
