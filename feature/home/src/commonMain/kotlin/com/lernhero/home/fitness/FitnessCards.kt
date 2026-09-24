package com.lernhero.home.fitness

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.fitness.FitnessGoal
import com.lernhero.domain.fitness.FitnessGoalCatalog
import com.lernhero.domain.fitness.FitnessSnapshot
import com.lernhero.domain.fitness.StepChest
import com.lernhero.home.city.TownPanel
import com.lernhero.home.city.TownProgressBar
import com.lernhero.home.ui.OutlinedText
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.FontFirst
import org.jetbrains.compose.resources.painterResource

private val Outline = Color(0xFF120B05)
private val KGold = Color(0xFFFFD34D)
private val KGreen = Color(0xFF6FD04A)
private val KMuted = Color(0xFFB8A888)

private fun Int.grouped(): String = toString().reversed().chunked(3).joinToString(",").reversed()

/**
 * Karta dziennych krokow - blizniak karty czytania: pierscienie (5 000 wewnatrz, 10 000 na
 * zewnatrz), wielka liczba krokow i dwie nagrody z przyciskami odbioru.
 */
@Composable
internal fun DailyStepsCard(
    snapshot: FitnessSnapshot,
    claimed: (FitnessGoal) -> Boolean,
    onClaim: (FitnessGoal) -> Unit
) {
    val goals = FitnessGoalCatalog.DAILY
    val steps = snapshot.steps
    val inner by animateFloatAsState((steps / goals.first().threshold.toFloat()).coerceIn(0f, 1f), tween(700))
    val outer by animateFloatAsState((steps / goals.last().threshold.toFloat()).coerceIn(0f, 1f), tween(700))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1E6B4A), Color(0xFF12402C), Color(0xFF0A2418))))
            .border(3.dp, Brush.verticalGradient(listOf(KGold, Color(0xFFE08A1E))), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = 10f * density
                    fun ring(inset: Float, fraction: Float, color: Color) {
                        val topLeft = Offset(inset + w / 2, inset + w / 2)
                        val arc = Size(size.width - 2 * inset - w, size.height - 2 * inset - w)
                        drawArc(Color(0xFF06140D), -90f, 360f, false, topLeft, arc, style = Stroke(w))
                        drawArc(color, -90f, 360f * fraction, false, topLeft, arc, style = Stroke(w, cap = StrokeCap.Round))
                    }
                    ring(0f, outer, Color(0xFF5BB8FF))
                    ring(w * 1.35f, inner, if (inner >= 1f) KGreen else KGold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👟", fontSize = 16.sp)
                    OutlinedText(text = steps.grouped(), size = 19.sp, color = Color.White, maxLines = 1)
                    Text("steps", color = Color(0xFFA8D8BE), fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedText(text = "Daily Steps", size = 20.sp, color = KGold)
                val next = goals.firstOrNull { steps < it.threshold }
                Text(
                    text = if (next == null) "Both rewards unlocked - legendary legs!"
                    else "${(next.threshold - steps).grouped()} steps to the ${if (next == goals.first()) "first" else "big"} reward",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "${snapshot.activeMinutes} active min  ·  ${snapshot.workouts.size} workouts",
                    color = Color(0xFFA8D8BE),
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
            }
        }
        goals.forEachIndexed { index, goal ->
            StepGoalRow(goal = goal, steps = steps, big = index == 1, claimed = claimed(goal), onClaim = { onClaim(goal) })
        }
    }
}

@Composable
private fun StepGoalRow(goal: FitnessGoal, steps: Int, big: Boolean, claimed: Boolean, onClaim: () -> Unit) {
    val complete = steps >= goal.threshold
    val claimable = complete && !claimed
    val pulse by rememberInfiniteTransition().animateFloat(0.94f, 1.06f, infiniteRepeatable(tween(600), RepeatMode.Reverse))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (claimable) Color(0x553F9E2B) else Color(0x66000000))
            .border(2.dp, if (claimable) KGreen else Outline, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer { if (claimable) { scaleX = pulse; scaleY = pulse } }
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        if (big) listOf(Color(0xFF5BB8FF), Color(0xFF2C6FC4)) else listOf(Color(0xFFFFC94A), Color(0xFFB8701E))
                    )
                )
                .border(2.dp, Outline, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (claimed) "✅" else if (big) "🏆" else "🎁", fontSize = 22.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedText(text = goal.title, size = 14.sp, color = Color.White, strokeWidth = 4f)
                Text("${goal.threshold.grouped()}", color = KGold, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                RewardChip("💰 ${goal.reward}")
                if (goal.gems > 0) RewardChip("💎 ${goal.gems}")
                if (goal.tickets > 0) RewardChip("🎟 ${goal.tickets}")
            }
        }
        when {
            claimed -> Text("Claimed", color = KGreen, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 12.sp)
            claimable -> Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF8BE05A), Color(0xFF3F9E2B))))
                    .border(2.5.dp, Outline, RoundedCornerShape(12.dp))
                    .clickable(onClick = onClaim)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) { OutlinedText(text = "CLAIM", size = 13.sp, color = Color.White, strokeWidth = 4f) }
            else -> Text(
                text = "${(goal.threshold - steps).grouped()}\nto go",
                color = KMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RewardChip(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Black,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xCC0C0806))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    )
}

/**
 * Skrzynia za kroki w stylu kart sklepu: skrzynia w obracajacych sie promieniach, pasek do progu
 * i wielki przycisk. Gdy gotowa - pulsuje i swieci; po otwarciu gasnie do jutra.
 */
@Composable
internal fun StepChestBanner(snapshot: FitnessSnapshot, openedToday: Boolean, onOpen: () -> Unit) {
    val unlocked = StepChest.isUnlocked(snapshot)
    val ready = unlocked && !openedToday
    val infinite = rememberInfiniteTransition()
    val spin by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(14_000, easing = LinearEasing)))
    val bob by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse))

    TownPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.radialGradient(listOf(Color(0xFFBFE3FF), Color(0xFF2361A8))))
                    .border(2.5.dp, Outline, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (ready) {
                    Canvas(modifier = Modifier.fillMaxSize().rotate(spin)) {
                        val r = size.maxDimension
                        for (i in 0 until 10) {
                            rotate(i * 36f) {
                                val path = Path().apply {
                                    moveTo(center.x, center.y)
                                    lineTo(center.x - r * 0.12f, center.y - r)
                                    lineTo(center.x + r * 0.12f, center.y - r)
                                    close()
                                }
                                drawPath(path, Color.White.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
                Image(
                    painter = painterResource(Resources.Image.shopChestBasic),
                    contentDescription = null,
                    alpha = if (openedToday) 0.35f else 1f,
                    modifier = Modifier
                        .size(78.dp)
                        .graphicsLayer { if (ready) translationY = -bob * 5f * density }
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedText(text = "Step Chest", size = 18.sp, color = KGold, strokeWidth = 5f)
                Text(
                    text = when {
                        openedToday -> "Opened today - a new one tomorrow."
                        ready -> "Unlocked! Open it for random loot."
                        else -> "Walk ${StepChest.STEPS_REQUIRED.grouped()} steps to unlock a loot chest."
                    },
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                if (ready) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFFFFE27A), Color(0xFFE0801C))))
                            .border(2.5.dp, Outline, RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpen)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) { OutlinedText(text = "OPEN CHEST", size = 15.sp, color = Color.White, strokeWidth = 5f) }
                } else {
                    TownProgressBar(
                        progress = StepChest.progress(snapshot),
                        fill = if (openedToday) Color(0xFF7D7468) else Color(0xFF5BB8FF),
                        height = 20.dp,
                        label = "${snapshot.steps.coerceAtMost(StepChest.STEPS_REQUIRED).grouped()} / ${StepChest.STEPS_REQUIRED.grouped()}"
                    )
                }
            }
        }
    }
}
