package com.lernhero.reader

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.reading.ReadingDayProgress
import com.lernhero.domain.reading.ReadingGoal
import com.lernhero.domain.reading.ReadingGoalCatalog
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.StrokedText

/* Wspolny "growy" jezyk czytelni - ten sam co sklep i budynki Miasta (modul czytnika nie widzi
 * komponentow z :feature:home, wiec ma wlasne, identyczne w wygladzie kopie). */

internal val KitOutline = Color(0xFF120B05)
internal val KitGold = Color(0xFFFFD34D)
internal val KitMuted = Color(0xFFB8A888)
internal val KitGreen = Color(0xFF6FD04A)

@Composable
internal fun KitText(
    text: String,
    size: TextUnit,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 6f,
    maxLines: Int = Int.MAX_VALUE
) {
    StrokedText(text, size, color, KitOutline, modifier, strokeWidth, maxLines)
}

/** Drewniana plyta: cieply gradient, gruby obrys, cienka zlota linia w srodku. */
@Composable
internal fun KitPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xF0352519), Color(0xF01C130C))))
            .border(2.5.dp, KitOutline, RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(2.5.dp)
                .border(1.dp, KitGold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
        )
        content()
    }
}

@Composable
internal fun KitButton(
    label: String,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: TextUnit = 15.sp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(if (enabled) colors else listOf(Color(0xFF7D7468), Color(0xFF4F483F))))
            .border(2.5.dp, KitOutline, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        KitText(text = label, size = size, color = if (enabled) Color.White else Color(0xFFD8D0C4), strokeWidth = 5f, maxLines = 1)
    }
}

internal val GreenButton = listOf(Color(0xFF8BE05A), Color(0xFF3F9E2B))

/** "1:05:30" / "12:04" - licznik dnia z sekundami, zeby bylo widac, ze naprawde leci. */
internal fun Int.asTimer(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    return if (h > 0) "$h:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    else "$m:${s.toString().padStart(2, '0')}"
}

/** Laczny czas w "3h 20m" - do statystyk, gdzie sekundy tylko przeszkadzaja. */
internal fun Int.asHoursMinutes(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

/**
 * Pierscien celu: wewnetrzny luk do 15 minut, zewnetrzny do godziny. Oba cele widac naraz, wiec
 * po domknieciu pierwszego od razu widac, ile brakuje do drugiego.
 */
@Composable
internal fun GoalRings(seconds: Int, modifier: Modifier = Modifier, stroke: Float = 10f, track: Color = Color(0xFF0B0714)) {
    val goals = ReadingGoalCatalog.DAILY
    val first = goals.first().threshold * 60f
    val second = goals.last().threshold * 60f
    val inner by animateFloatAsState((seconds / first).coerceIn(0f, 1f), tween(600))
    val outer by animateFloatAsState((seconds / second).coerceIn(0f, 1f), tween(600))
    Canvas(modifier = modifier) {
        val w = stroke * density
        fun ring(inset: Float, fraction: Float, color: Color) {
            val topLeft = Offset(inset + w / 2, inset + w / 2)
            val arc = Size(size.width - 2 * inset - w, size.height - 2 * inset - w)
            drawArc(track, -90f, 360f, false, topLeft, arc, style = Stroke(w))
            drawArc(color, -90f, 360f * fraction, false, topLeft, arc, style = Stroke(w, cap = StrokeCap.Round))
        }
        ring(0f, outer, Color(0xFFB48BFF))
        ring(w * 1.35f, inner, if (inner >= 1f) KitGreen else KitGold)
    }
}

@Composable
internal fun RewardChips(goal: ReadingGoal, size: TextUnit = 11.sp) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Chip("💰 ${goal.reward}", size)
        if (goal.gems > 0) Chip("💎 ${goal.gems}", size)
        if (goal.tickets > 0) Chip("🎟 ${goal.tickets}", size)
    }
}

@Composable
private fun Chip(text: String, size: TextUnit) {
    Text(
        text = text,
        color = Color.White,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Black,
        fontSize = size,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xCC0C0806))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    )
}

/**
 * Karta dziennego czytania: pierscienie celu, dzisiejszy czas i dwie nagrody (15 min / 1 h),
 * kazda z wlasnym przyciskiem odbioru. To ona ma byc powodem, zeby codziennie siasc do ksiazki.
 */
@Composable
internal fun DailyReadingCard(
    today: ReadingDayProgress,
    claimed: (ReadingGoal) -> Boolean,
    onClaim: (ReadingGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    val seconds = today.activeSeconds
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF4A2A6E), Color(0xFF24133A), Color(0xFF160C22))))
            .border(3.dp, Brush.verticalGradient(listOf(KitGold, Color(0xFFE08A1E))), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(104.dp), contentAlignment = Alignment.Center) {
                GoalRings(seconds = seconds, modifier = Modifier.fillMaxSize())
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    KitText(text = "${seconds / 60}", size = 26.sp, color = Color.White)
                    Text("min today", color = Color(0xFFBFA9DE), fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                KitText(text = "Daily Reading", size = 20.sp, color = KitGold)
                val next = ReadingGoalCatalog.DAILY.firstOrNull { seconds < it.threshold * 60 }
                Text(
                    text = if (next == null) "Both rewards unlocked today - legendary!"
                    else "${((next.threshold * 60 - seconds + 59) / 60)} min to the ${if (next == ReadingGoalCatalog.DAILY.first()) "first" else "big"} reward",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "Only active reading counts - turn pages to keep the clock running.",
                    color = Color(0xFFBFA9DE),
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
            }
        }
        ReadingGoalCatalog.DAILY.forEachIndexed { index, goal ->
            GoalRow(goal = goal, seconds = seconds, big = index == 1, claimed = claimed(goal), onClaim = { onClaim(goal) })
        }
    }
}

@Composable
private fun GoalRow(goal: ReadingGoal, seconds: Int, big: Boolean, claimed: Boolean, onClaim: () -> Unit) {
    val complete = seconds >= goal.threshold * 60
    val claimable = complete && !claimed
    val pulse by rememberInfiniteTransition().animateFloat(0.94f, 1.06f, infiniteRepeatable(tween(600), RepeatMode.Reverse))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (claimable) Color(0x553F9E2B) else Color(0x66000000))
            .border(2.dp, if (claimable) KitGreen else KitOutline, RoundedCornerShape(12.dp))
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
                        if (big) listOf(Color(0xFFB48BFF), Color(0xFF5A2DA8)) else listOf(Color(0xFFFFC94A), Color(0xFFB8701E))
                    )
                )
                .border(2.dp, KitOutline, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (claimed) "✅" else if (big) "👑" else "🎁", fontSize = 22.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                KitText(text = goal.title, size = 14.sp, color = Color.White, strokeWidth = 4f)
                Text(
                    text = if (goal.threshold >= 60) "${goal.threshold / 60} h" else "${goal.threshold} min",
                    color = KitGold,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
            RewardChips(goal)
        }
        when {
            claimed -> Text("Claimed", color = KitGreen, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 12.sp)
            claimable -> KitButton(label = "CLAIM", colors = GreenButton, size = 13.sp, onClick = onClaim)
            else -> Text(
                text = "${((goal.threshold * 60 - seconds + 59) / 60)} min\nleft",
                color = KitMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Licznik celu w czytniku - maly, zawsze widoczny pierscien z dzisiejszym czasem. Bez niego
 * zegar byl schowany w pasku, ktory pojawia sie dopiero po tapnieciu, i gracz nie wiedzial,
 * czy czytanie w ogole sie liczy.
 */
@Composable
internal fun ReadingGoalPill(todaySeconds: Int, paused: Boolean, ink: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(ink.copy(alpha = 0.08f))
            .padding(start = 3.dp, end = 9.dp, top = 3.dp, bottom = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GoalRings(seconds = todaySeconds, modifier = Modifier.size(22.dp), stroke = 2.5f, track = ink.copy(alpha = 0.15f))
        Text(
            text = if (paused) "paused" else todaySeconds.asTimer(),
            color = if (paused) Color(0xFFE0913A) else ink.copy(alpha = 0.7f),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

/**
 * Baner "cel osiagniety" - odpala sie w chwili, gdy dzisiejszy czas PRZEKRACZA prog celu w trakcie
 * czytania (a nie przy otwarciu, gdy prog byl juz za nami), i znika po kilku sekundach.
 */
@Composable
internal fun GoalReachedBanner(todaySeconds: Int, modifier: Modifier = Modifier) {
    val startSeconds = androidx.compose.runtime.remember { todaySeconds }
    var shown by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<ReadingGoal?>(null) }
    val reached = ReadingGoalCatalog.DAILY.lastOrNull { todaySeconds >= it.threshold * 60 && startSeconds < it.threshold * 60 }
    androidx.compose.runtime.LaunchedEffect(reached?.id) {
        if (reached != null) {
            shown = reached
            kotlinx.coroutines.delay(4500)
            shown = null
        }
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = shown != null,
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.7f),
        exit = androidx.compose.animation.fadeOut(),
        modifier = modifier
    ) {
        val goal = shown ?: return@AnimatedVisibility
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF4A2A6E), Color(0xFF24133A))))
                .border(2.5.dp, KitGold, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🎁", fontSize = 26.sp)
            Column {
                KitText(text = "${goal.title} unlocked!", size = 15.sp, color = KitGold, strokeWidth = 4f)
                Text("Claim it in the Library", color = Color.White, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}
