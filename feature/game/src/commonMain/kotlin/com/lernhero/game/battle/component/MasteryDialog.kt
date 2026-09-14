package com.lernhero.game.battle.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.progress.CategoryMastery
import com.lernhero.game.battle.MasteryOffer
import com.lernhero.shared.theme.FontFirst
import kotlin.math.roundToInt

private val Scrim = Color(0xCC0B0805)
private val CardTop = Color(0xFF2E2318)
private val CardBottom = Color(0xFF1A1310)
private val Gold = Color(0xFFFFD34D)
private val GoldDim = Color(0xFFC8A45A)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val Green = Color(0xFF22A55E)
private val Risk = Color(0xFFE0654F)

/**
 * Moment awansu tematu: komplet fal zebrany, poziom zdobyty na pewno - do rozstrzygniecia zostaje
 * tylko sakwa.
 *
 * Ekran celowo nie udaje wyboru "nagroda albo nic": poziom jest juz przyznany i tak to nazywamy
 * ("Level secured"). Decyzja dotyczy wylacznie zlota - wyjscie ksieguje cale, dalsza gra stawia je
 * pod zastaw. Obie opcje pokazuja wprost, ile jest w grze, bo bez tej liczby wybor bylby zgadywaniem.
 */
@Composable
fun MasteryDialog(
    offer: MasteryOffer,
    onClaim: () -> Unit,
    onPushOn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextDifficulty = CategoryMastery.startingDifficulty(offer.newLevel)
    val rewardPercent = ((CategoryMastery.rewardMultiplier(offer.newLevel) - 1f) * 100).roundToInt()
    val atRisk = (offer.bagGold * 0.75f).roundToInt()

    Box(
        modifier = modifier.fillMaxSize().background(Scrim),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .widthIn(max = 380.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.verticalGradient(listOf(CardTop, CardBottom)))
                .border(2.dp, Brush.verticalGradient(listOf(Gold, GoldDim)), RoundedCornerShape(22.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "LEVEL SECURED",
                color = Gold,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(46.dp).clip(CircleShape).background(Green),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${offer.newLevel}",
                        color = Color.White,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Column {
                    Text(
                        text = offer.category.label,
                        color = Cream,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                    Text(
                        text = "${CategoryMastery.WAVES_PER_LEVEL} waves cleared",
                        color = Muted,
                        fontFamily = FontFirst(),
                        fontSize = 12.sp
                    )
                }
            }

            // Co ten poziom realnie zmieni przy nastepnym wejsciu - bez tego awans bylby sama liczba.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x2AFFD34D))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MasteryPerk("Questions now start at", nextDifficulty.label)
                MasteryPerk("Gold and XP from now on", "+$rewardPercent%")
            }

            Text(
                text = "Your bag holds ${offer.bagGold} gold.",
                color = Cream,
                fontFamily = FontFirst(),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            MasteryButton(
                title = "CLAIM AND LEAVE",
                subtitle = "Bank all ${offer.bagGold} gold",
                accent = Green,
                onClick = onClaim
            )
            MasteryButton(
                title = "PUSH ON",
                subtitle = if (offer.bagGold > 0) "Falling now costs $atRisk gold" else "Keep the waves coming",
                accent = Risk,
                onClick = onPushOn
            )
        }
    }
}

@Composable
private fun MasteryPerk(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Muted, fontFamily = FontFirst(), fontSize = 12.sp)
        Text(
            text = value,
            color = Gold,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun MasteryButton(title: String, subtitle: String, accent: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.18f))
            .border(2.dp, accent, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = accent,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(text = subtitle, color = Muted, fontFamily = FontFirst(), fontSize = 11.sp)
    }
}
