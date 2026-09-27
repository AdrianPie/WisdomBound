package com.lernhero.home

import com.lernhero.shared.theme.GlyphText
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Darmowe zloto za reklame - medalion w prawym gornym rogu mapy Domu, jak w wiekszosci gier
 * mobilnych. Dopoki sa dzis proby, pulsuje i swieci; po wyczerpaniu gasnie i czeka do jutra.
 * Kazda wyplata ([pulse] rosnie) wyrzuca garsc monet i unoszaca sie kwote.
 */
@Composable
internal fun AdGoldButton(
    remaining: Int,
    perDay: Int,
    reward: Int,
    busy: Boolean,
    isPremium: Boolean,
    pulse: Int,
    lastReward: Int,
    note: String?,
    onClick: () -> Unit,
    onNoteShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val available = remaining > 0
    val infinite = rememberInfiniteTransition()
    // Lekkie unoszenie + co kilka sekund blysk przejezdzajacy po kafelku i krotkie "zawolanie"
    // (przechyl), zeby przycisk zwracal uwage, ale nie migal bez przerwy.
    val bob by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )
    val cycle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing))
    )
    val shine = ((cycle - 0.55f) / 0.25f).coerceIn(0f, 1f)
    val wiggle = if (cycle in 0.85f..1f) sin((cycle - 0.85f) / 0.15f * 3f * PI.toFloat()) * (1f - (cycle - 0.85f) / 0.15f) * 8f else 0f

    val burst = remember { Animatable(1f) }
    // Wyplata sprzed wejscia na ekran NIE odpala efektu ponownie - pulse zyje w ViewModelu, wiec
    // po powrocie na Dom LaunchedEffect widzialby "nowa" wartosc i pokazywal +690 jeszcze raz.
    val seenPulse = remember { pulse }
    LaunchedEffect(pulse) {
        if (pulse == seenPulse) return@LaunchedEffect
        burst.snapTo(0f)
        burst.animateTo(1f, tween(BURST_MS, easing = LinearEasing))
    }
    val press = remember { Animatable(1f) }
    LaunchedEffect(pulse) {
        if (pulse == seenPulse) return@LaunchedEffect
        press.snapTo(0.8f)
        press.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 500f))
    }

    LaunchedEffect(note) {
        if (note == null) return@LaunchedEffect
        delay(NOTE_MS)
        onNoteShown()
    }

    val tileTop = if (available) Color(0xFFFFC94A) else Color(0xFF7B7266)
    val tileBottom = if (available) Color(0xFFE0801C) else Color(0xFF4C453C)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(TILE + 14.dp), contentAlignment = Alignment.Center) {
            // Miekka poswiata pod kafelkiem, gdy jest co odebrac.
            if (available) {
                Box(
                    modifier = Modifier
                        .size(TILE + 14.dp)
                        .alpha(0.35f + bob * 0.3f)
                        .background(Brush.radialGradient(listOf(Color(0xFFFFD34D), Color.Transparent)), CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .size(TILE)
                    .graphicsLayer {
                        translationY = if (available && !busy) -bob * 3f * density else 0f
                        rotationZ = if (available && !busy) wiggle else 0f
                        scaleX = press.value
                        scaleY = press.value
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(listOf(tileTop, tileBottom)))
                    .border(3.dp, Color(0xFF2A1A08), RoundedCornerShape(16.dp))
                    .clickable(enabled = !busy, onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                // Jasny "garb" u gory kafelka - daje mu wypuklosc.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                        .size(width = TILE - 14.dp, height = 12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = if (available) 0.35f else 0.12f))
                )
                GlyphText(text = if (busy) "⏳" else "💰", fontSize = 30.sp, modifier = Modifier.offset(y = 2.dp))
                // Blysk przejezdzajacy po skosie.
                if (available && shine in 0.001f..0.999f) {
                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = TILE * 1.6f)
                            .graphicsLayer {
                                translationX = (-TILE.toPx()) + shine * TILE.toPx() * 2f
                                rotationZ = 25f
                            }
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.6f), Color.Transparent)))
                    )
                }
            }

            // Plakietka AD / korona w lewym gornym rogu.
            GlyphText(
                text = if (isPremium) "👑" else "AD",
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (available) Color(0xFF7A4BD6) else Color(0xFF5A4F42))
                    .border(1.5.dp, Color(0xFF2A1A08), RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            )
            // Licznik prob na dzis.
            GlyphText(
                text = "$remaining/$perDay",
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(50))
                    .background(if (available) Color(0xFFD1453D) else Color(0xFF5A4F42))
                    .border(1.5.dp, Color(0xFF2A1A08), RoundedCornerShape(50))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            )

            CoinBurst(progress = burst.value, amount = lastReward)
        }

        // Kwota na ciemnej pastylce pod kafelkiem.
        Row(
            modifier = Modifier
                .offset(y = (-2).dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xE6120E0A))
                .padding(horizontal = 7.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (available) GlyphText(text = "🪙", fontSize = 10.sp)
            GlyphText(
                text = if (available) "+$reward" else "Tomorrow",
                color = if (available) Color(0xFFFFD34D) else Color(0xFF9D8B6A),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }

        note?.let {
            GlyphText(
                text = it,
                color = Color(0xFFF4E9D7),
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .widthIn(max = 150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xE6120E0A))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            )
        }
    }
}

/**
 * Wyplata: monety wyskakuja z kafelka fontanna (kazda z opoznieniem, po lukach z grawitacja),
 * a kwota wyrasta nad nim i odplywa w gore.
 */
@Composable
private fun CoinBurst(progress: Float, amount: Int) {
    if (progress >= 1f) return
    Box(contentAlignment = Alignment.Center) {
        repeat(COINS) { index ->
            val delayFrac = index * 0.035f
            val t = ((progress - delayFrac) / (1f - delayFrac)).coerceIn(0f, 1f)
            if (t > 0f && t < 1f) {
                val spread = (index - (COINS - 1) / 2f) / ((COINS - 1) / 2f)
                val vx = spread * 55f
                val vy = -130f - (index % 3) * 18f
                GlyphText(
                    text = "🪙",
                    fontSize = 15.sp,
                    modifier = Modifier.graphicsLayer {
                        translationX = vx * t * density
                        translationY = (vy * t + 260f * t * t) * density
                        rotationY = 540f * t
                        alpha = if (t < 0.75f) 1f else (1f - t) / 0.25f
                    }
                )
            }
        }
        val pop = (progress / 0.25f).coerceIn(0f, 1f)
        GlyphText(
            text = "+$amount",
            color = Color(0xFFFFE27A),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(Color(0xFF3A2200), androidx.compose.ui.geometry.Offset(0f, 3f), 4f)
            ),
            modifier = Modifier.graphicsLayer {
                translationY = (-30f - 50f * progress) * density
                val s = 0.4f + 0.9f * pop - 0.2f * pop * pop
                scaleX = s
                scaleY = s
                alpha = if (progress < 0.7f) 1f else (1f - progress) / 0.3f
            }
        )
    }
}

private val TILE = 58.dp
private const val COINS = 12
private const val BURST_MS = 1500
private const val NOTE_MS = 2600L
