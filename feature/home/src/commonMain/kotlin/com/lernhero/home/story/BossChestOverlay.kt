package com.lernhero.home.story

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.run.ChestSlot
import com.lernhero.domain.run.ChestWheel
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val SlotColors: Map<ChestSlot, Color> = mapOf(
    ChestSlot.GoldSmall to Color(0xFF8A7A4E),
    ChestSlot.GoldMedium to Color(0xFFB89A46),
    ChestSlot.GoldLarge to Color(0xFFD9B44A),
    ChestSlot.SummonTicket to Color(0xFF4E8AB8),
    ChestSlot.ItemCommon to Color(0xFF7C8A94),
    ChestSlot.ItemRare to Color(0xFF6F4FB0),
    ChestSlot.HeroicTicket to Color(0xFFE05A2F),
    ChestSlot.ItemLegendary to Color(0xFFFFC04D)
)

private enum class WheelPhase { Closed, Spinning, Revealed }

/**
 * Skrzynia po bossie: kolo fortuny, ktore kreci sie SAMO. Gracz nie ma tu zadnej decyzji - i o to
 * chodzi. To nagroda za pokonanie bossa planszy, wiec jedyne, co ma robic, to budowac napiecie:
 * kolo zwalnia przez ostatnie obroty i staje na wylosowanym polu.
 *
 * Losowanie ([ChestWheel.roll]) dzieje sie ZANIM ruszy animacja - kat koncowy jest z niego policzony,
 * a nie odwrotnie. Dzieki temu wynik nie zalezy od tego, jak animacja sie zaokragli.
 */
@Composable
fun BossChestOverlay(
    dungeonLevel: Int,
    prize: ChestSlot,
    prizeLabel: String,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(WheelPhase.Closed) }
    val rotation = remember { Animatable(0f) }

    val slots = ChestSlot.entries
    val sliceAngle = 360f / slots.size
    val targetAngle = remember(prize) {
        val slotCenter = slots.indexOf(prize) * sliceAngle + sliceAngle / 2f
        SPIN_TURNS * 360f - slotCenter
    }

    LaunchedEffect(phase) {
        if (phase != WheelPhase.Spinning) return@LaunchedEffect
        rotation.snapTo(0f)
        rotation.animateTo(targetAngle, tween(SPIN_MILLIS, easing = SpinEasing))
        phase = WheelPhase.Revealed
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07050B).copy(alpha = 0.95f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SKARBIEC BOSSA",
                color = Color(0xFFFFD34D),
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Text(
                text = when (phase) {
                    WheelPhase.Closed -> "Boss defeated. The chest will open on its own - the wheel decides what drops."
                    WheelPhase.Spinning -> "The wheel is spinning..."
                    WheelPhase.Revealed -> prizeLabel
                },
                color = if (phase == WheelPhase.Revealed) SlotColors.getValue(prize) else Color(0xFF9D8B6A),
                fontFamily = FontFirst(),
                fontWeight = if (phase == WheelPhase.Revealed) FontWeight.Bold else FontWeight.Normal,
                fontSize = if (phase == WheelPhase.Revealed) 15.sp else 12.sp,
                textAlign = TextAlign.Center
            )

            when (phase) {
                WheelPhase.Closed -> ClosedChest(onOpen = { phase = WheelPhase.Spinning })
                else -> PrizeWheel(
                    rotation = rotation.value,
                    highlighted = if (phase == WheelPhase.Revealed) prize else null,
                    dungeonLevel = dungeonLevel
                )
            }

            if (phase == WheelPhase.Revealed) {
                Text(
                    text = "CLAIM",
                    color = Color(0xFFFFD34D),
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF3A2A18))
                        .border(1.5.dp, Color(0xFFC8A45A), RoundedCornerShape(8.dp))
                        .clickable(onClick = onClaim)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun ClosedChest(onOpen: () -> Unit) {
    val infinite = rememberInfiniteTransition()
    val bob by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            tween(1100, easing = EaseInOutSine), RepeatMode.Reverse
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(Resources.Image.treasureChest),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(150.dp)
                .graphicsLayer { translationY = bob * 8f }
                .clickable(onClick = onOpen)
        )
        Text(
            text = "OTWORZ SKRZYNIE",
            color = Color(0xFFFFD34D),
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF3A2A18))
                .border(1.5.dp, Color(0xFFC8A45A), RoundedCornerShape(8.dp))
                .clickable(onClick = onOpen)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun PrizeWheel(rotation: Float, highlighted: ChestSlot?, dungeonLevel: Int) {
    val slots = ChestSlot.entries
    val sliceAngle = 360f / slots.size

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(0.85f).aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rotation }) {
                val diameter = size.minDimension
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                slots.forEachIndexed { index, slot ->
                    val base = SlotColors.getValue(slot)
                    drawArc(
                        color = if (highlighted == null || highlighted == slot) base else base.copy(alpha = 0.35f),
                        startAngle = index * sliceAngle - 90f,
                        sweepAngle = sliceAngle,
                        useCenter = true,
                        topLeft = topLeft,
                        size = Size(diameter, diameter)
                    )
                    drawArc(
                        color = Color(0xFF0B0805),
                        startAngle = index * sliceAngle - 90f,
                        sweepAngle = sliceAngle,
                        useCenter = true,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                    )
                }
            }

            slots.forEachIndexed { index, slot ->
                val angleDeg = index * sliceAngle + sliceAngle / 2f - 90f + rotation
                val angleRad = angleDeg * PI.toFloat() / 180f
                val radius = maxWidth.value * 0.31f
                Text(
                    text = slot.emoji,
                    fontSize = 20.sp,
                    modifier = Modifier.graphicsLayer {
                        translationX = cos(angleRad) * radius * density
                        translationY = sin(angleRad) * radius * density
                    }
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(width = 16.dp, height = 26.dp)
                    .background(Color(0xFFFFD34D), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            )

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.radialGradient(listOf(Color(0xFF2B2018), Color(0xFF0B0805))))
                    .border(2.dp, Color(0xFFC8A45A), RoundedCornerShape(50))
            )
        }

        if (highlighted != null) {
            Text(
                text = "${highlighted.label} - ${(ChestWheel.chanceOf(highlighted, dungeonLevel) * 100).roundToInt()}% chance",
                color = Color(0xFF9D8B6A),
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
        }
    }
}

private const val SPIN_TURNS = 6
private const val SPIN_MILLIS = 4200
private val SpinEasing = androidx.compose.animation.core.CubicBezierEasing(0.12f, 0.85f, 0.05f, 1f)
