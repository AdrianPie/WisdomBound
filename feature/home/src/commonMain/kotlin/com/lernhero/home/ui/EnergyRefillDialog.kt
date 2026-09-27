package com.lernhero.home.ui

import com.lernhero.shared.theme.GlyphText
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.energy.EnergyRules
import com.lernhero.domain.energy.EnergyState
import com.lernhero.domain.monetization.EnergyRefill
import com.lernhero.shared.theme.FontFirst
import kotlinx.coroutines.delay
import kotlin.time.Clock

/**
 * Okno doladowania energii - wyskakuje, gdy gracz chce walczyc bez energii, albo po "+" przy
 * energii w gornym pasku.
 *
 * Mowi DWIE rzeczy naraz, zeby nie bylo to tylko okno "zaplac": ile trzeba poczekac na darmowa
 * energie i ile kosztuje doladowanie teraz (cena rosnie z kazdym zakupem tego dnia, patrz
 * [EnergyRefill]). Gdy brakuje diamentow, przycisk prowadzi do sklepu zamiast udawac, ze dziala.
 */
@Composable
fun EnergyRefillDialog(
    energy: EnergyState,
    gems: Int,
    refillsToday: Int,
    onRefill: () -> Unit,
    onGetGems: () -> Unit,
    onClose: () -> Unit
) {
    var now by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = Clock.System.now().toEpochMilliseconds()
        }
    }
    val current = EnergyRules.regenerated(energy, now)
    val price = EnergyRefill.priceFor(refillsToday)
    val canPay = price != null && gems >= price
    val left = EnergyRefill.MAX_PER_DAY - refillsToday

    val pop = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 320f)) }
    val infinite = rememberInfiniteTransition()
    val spin by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(14_000, easing = LinearEasing)))
    val pulse by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val sweep by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF2B4A8C), Color(0xFF16264E))))
                .border(3.dp, Color(0xFF0B1330), RoundedCornerShape(18.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
                OutlinedText(text = if (current.amount < EnergyRules.BATTLE_COST) "Out of Energy!" else "Energy", size = 22.sp, color = Color.White)
                GlyphText(
                    text = "✕",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0B1330))
                        .clickable(onClick = onClose)
                        .padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Bolt w promieniach: aktualny stan i to, ile wpadnie po doladowaniu.
            Box(modifier = Modifier.fillMaxWidth().height(150.dp).clipToBounds(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .rotate(spin)
                        .alpha(0.5f)
                        .background(Brush.sweepGradient(List(18) { if (it % 2 == 0) Color(0x66FFE27A) else Color.Transparent }), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Brush.radialGradient(listOf(Color(0x99FFE27A), Color.Transparent)), CircleShape)
                )
                GlyphText(
                    text = "⚡",
                    fontSize = 70.sp,
                    modifier = Modifier.graphicsLayer {
                        val s = 1f + 0.06f * pulse
                        scaleX = s
                        scaleY = s
                    }
                )
                OutlinedText(
                    text = "+${EnergyRefill.AMOUNT}",
                    size = 26.sp,
                    color = Color(0xFFFFE27A),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 70.dp, bottom = 18.dp)
                )
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x55000000))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    GlyphText("You have", color = Color(0xFF8FA3C4), fontFamily = FontFirst(), fontSize = 11.sp)
                    OutlinedText(text = "⚡ ${current.amount}/${EnergyRules.MAX}", size = 16.sp, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    GlyphText("Full for free in", color = Color(0xFF8FA3C4), fontFamily = FontFirst(), fontSize = 11.sp)
                    OutlinedText(
                        text = EnergyRules.millisToFull(current, now).let { if (it == 0L) "Full" else waitLabel(it) },
                        size = 16.sp,
                        color = Color(0xFF8BE05A)
                    )
                }
            }

            GlyphText(
                text = "One refill = ${EnergyRefill.AMOUNT / EnergyRules.BATTLE_COST} more battles. Bought energy can go above the bar.",
                color = Color(0xFFBFD3F0),
                fontFamily = FontFirst(),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            when {
                                price == null -> listOf(Color(0xFFB8B8B8), Color(0xFF7C7C7C))
                                canPay -> listOf(Color(0xFF8BE05A), Color(0xFF3F9E2B))
                                else -> listOf(Color(0xFF5BB8FF), Color(0xFF2C7BD6))
                            }
                        )
                    )
                    .border(2.5.dp, Color(0xFF0B1330), RoundedCornerShape(14.dp))
                    .clickable(enabled = price != null) { if (canPay) onRefill() else onGetGems() }
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                if (canPay) {
                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = 90.dp)
                            .graphicsLayer {
                                translationX = (sweep * 2f - 1f) * 180f * density
                                rotationZ = 20f
                            }
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.5f), Color.Transparent)))
                    )
                }
                OutlinedText(
                    text = when {
                        price == null -> "No refills left today"
                        canPay -> "REFILL  💎 $price"
                        else -> "Need 💎 $price - get gems"
                    },
                    size = 18.sp,
                    color = Color.White
                )
            }

            GlyphText(
                text = if (price == null) "Refills reset tomorrow." else "$left of ${EnergyRefill.MAX_PER_DAY} refills left today · you have 💎 $gems",
                color = Color(0xFF8FA3C4),
                fontFamily = FontFirst(),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }
    }
}

private fun waitLabel(ms: Long): String {
    val minutes = ms / 60_000
    val hours = minutes / 60
    return if (hours > 0) "${hours}h ${minutes % 60}m" else "${minutes.coerceAtLeast(1)}m"
}
