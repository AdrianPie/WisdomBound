package com.lernhero.game.battle.component

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.run.EndlessBuff
import com.lernhero.domain.run.UpgradeRarity
import com.lernhero.game.battle.UpgradeOffer
import com.lernhero.shared.theme.FontFirst
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val PanelInk = Color(0xFF19120C)
private val PanelFrame = Color(0xFFC8A45A)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)

/** Kolor ramki/etykiety karty - jedyne, po czym na pierwszy rzut oka poznajesz klase ulepszenia. */
private fun UpgradeRarity.color(): Color = when (this) {
    UpgradeRarity.Common -> Color(0xFFB9B9B9)
    UpgradeRarity.Rare -> Color(0xFF6FB7FF)
    UpgradeRarity.Epic -> Color(0xFFC77DFF)
    UpgradeRarity.Legendary -> Color(0xFFFFC04D)
}

/**
 * Roguelike'owy wybor ulepszenia po oczyszczonej fali (endless mode). Walka stoi, dopoki gracz nie
 * wezmie jednej z kart - stad brak jakiegokolwiek "zamknij": [com.lernhero.game.battle.BattleViewModel.chooseUpgrade]
 * jest jedynym wyjsciem z tego ekranu.
 *
 * Karty wjezdzaja po kolei (jak odpowiedzi w quizie), a pasek u gory pokazuje, ze to celnosc
 * odpowiedzi wylosowala te konkretna oferte - inaczej gracz nie mialby jak zauwazyc, ze dobry quiz
 * realnie podnosi klase kart.
 */
@Composable
fun UpgradeChoiceDialog(
    offer: UpgradeOffer,
    onPick: (EndlessBuff) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC0B0704)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OfferHeader(offer)

            offer.choices.forEachIndexed { index, buff ->
                var visible by remember(offer.wave, buff) { mutableStateOf(false) }
                LaunchedEffect(offer.wave, buff) {
                    delay(CARD_STAGGER_MILLIS * index)
                    visible = true
                }
                UpgradeCard(
                    buff = buff,
                    ownedStacks = offer.ownedStacks[buff] ?: 0,
                    visible = visible,
                    onPick = { onPick(buff) }
                )
            }
        }
    }
}

@Composable
private fun OfferHeader(offer: UpgradeOffer) {
    val pulse = rememberInfiniteTransition()
    val glow by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PanelInk)
            .border(2.dp, PanelFrame.copy(alpha = glow), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "WAVE ${offer.wave} - PICK AN UPGRADE",
            color = Color(0xFFFFD34D),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            text = "Lasts until the end of the run. Card quality: ${(offer.quality * 100).roundToInt()}% " +
                "(the more correct answers, the better the cards).",
            color = Muted,
            fontFamily = FontFirst(),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
        QualityBar(offer.quality)
    }
}

/** Pasek "poziomu kart" - ta sama informacja co procent w naglowku, tylko od razu widoczna. */
@Composable
private fun QualityBar(quality: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF2B2018))
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(quality.coerceIn(0.02f, 1f))
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        listOf(UpgradeRarity.Common.color(), UpgradeRarity.Epic.color(), UpgradeRarity.Legendary.color())
                    )
                )
                .padding(vertical = 2.5.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun UpgradeCard(
    buff: EndlessBuff,
    ownedStacks: Int,
    visible: Boolean,
    onPick: () -> Unit
) {
    val accent = buff.rarity.color()
    val entrance by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = EaseOutBack)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = entrance
                scaleX = 0.9f + 0.1f * entrance
                scaleY = 0.9f + 0.1f * entrance
                translationX = (1f - entrance) * 60f
            }
            .clip(RoundedCornerShape(12.dp))
            .background(PanelInk)
            .border(2.dp, accent, RoundedCornerShape(12.dp))
            .clickable(enabled = visible, onClick = onPick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buff.title,
                    color = Cream,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = buff.rarity.label,
                    color = accent,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            }
            Text(
                text = buff.description,
                color = Color(0xFFCBBEA6),
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
            Text(
                text = if (ownedStacks > 0) {
                    "You have x$ownedStacks of ${buff.maxStacks} - picking adds another stack."
                } else {
                    "Nowe (max x${buff.maxStacks})"
                },
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 9.sp
            )
        }
    }
}

private const val CARD_STAGGER_MILLIS = 110L
