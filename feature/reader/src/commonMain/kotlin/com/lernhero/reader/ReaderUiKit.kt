package com.lernhero.reader

import com.lernhero.shared.theme.GlyphText
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

/*
 * Pergamin jak na karcie postaci (kopia [com.lernhero.home.ui.PaperCard] - modul czytnika nie widzi
 * :feature:home). Uzywany przez karty z trescia: dzienne czytanie, "czytaj dalej", statystyki.
 */
internal val PaperFrame = Color(0xFF6B4A2B)
internal val PaperFrameLight = Color(0xFF9A7043)
internal val PaperTop = Color(0xFFE8D5B0)
internal val PaperBottom = Color(0xFFD8BF93)
internal val PaperCardFill = Color(0xFFF3E6C8)
internal val PaperCardEdge = Color(0xFFB9945E)
internal val PaperInkColor = Color(0xFF4A3620)
internal val PaperInkMuted = Color(0xFF7A6244)
internal val PaperGood = Color(0xFF3E8A2E)
internal val PaperAmber = Color(0xFFB7791F)

@Composable
internal fun PaperPanel(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PaperFrame)
            .padding(3.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.verticalGradient(listOf(PaperTop, PaperBottom)))
            .border(1.5.dp, PaperFrameLight.copy(alpha = 0.6f), RoundedCornerShape(15.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

/** Mala etykieta nad liczba: drukowane litery, przygaszony tusz. */
@Composable
internal fun PaperCaption(text: String, color: Color = PaperInkMuted) {
    GlyphText(
        text = text.uppercase(),
        color = color,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        lineHeight = 10.sp,
        letterSpacing = 0.6.sp,
        maxLines = 1
    )
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
    // Gruby przycisk z ciemniejsza "warga" pod spodem - wyglada na wciskany, jak w grach.
    val face = if (enabled) colors else listOf(Color(0xFFB8AC96), Color(0xFF9A8E78))
    val lip = face.last().copy(red = face.last().red * 0.62f, green = face.last().green * 0.62f, blue = face.last().blue * 0.62f)
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(lip)
            .padding(bottom = 3.dp)
            .clip(shape)
            .background(Brush.verticalGradient(face))
            .border(1.5.dp, lip, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        StrokedText(label, size, Color.White, lip.copy(red = lip.red * 0.6f, green = lip.green * 0.6f, blue = lip.blue * 0.6f), strokeWidth = 5f, maxLines = 1)
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
        ring(0f, outer, Color(0xFF8B5CD6))
        ring(w * 1.35f, inner, if (inner >= 1f) KitGreen else Color(0xFFE5A823))
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
    GlyphText(
        text = text,
        color = PaperInkColor,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Black,
        fontSize = size,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0x264A3620))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    )
}

/**
 * Karta dziennego czytania: pierscienie celu, dzisiejszy czas i dwie nagrody (15 min / 1 h),
 * kazda z wlasnym przyciskiem odbioru. To ona ma byc powodem, zeby codziennie siasc do ksiazki.
 * Pergamin jak karta postaci - bez fioletowego gradientu, bialego tekstu z obrysem i emoji.
 */
@Composable
internal fun DailyReadingCard(
    today: ReadingDayProgress,
    claimed: (ReadingGoal) -> Boolean,
    onClaim: (ReadingGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    val seconds = today.activeSeconds
    PaperPanel(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(90.dp), contentAlignment = Alignment.Center) {
                GoalRings(seconds = seconds, modifier = Modifier.fillMaxSize(), stroke = 9f, track = Color(0x334A3620))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GlyphText(
                        text = "${seconds / 60}",
                        color = PaperInkColor,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        lineHeight = 26.sp
                    )
                    PaperCaption("min today")
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                GlyphText(
                    text = "Daily Reading",
                    color = PaperInkColor,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    lineHeight = 20.sp
                )
                val next = ReadingGoalCatalog.DAILY.firstOrNull { seconds < it.threshold * 60 }
                GlyphText(
                    text = if (next == null) "Both rewards unlocked today!"
                    else "${((next.threshold * 60 - seconds + 59) / 60)} min to the ${if (next == ReadingGoalCatalog.DAILY.first()) "first" else "big"} reward",
                    color = PaperAmber,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    lineHeight = 14.sp
                )
                GlyphText(
                    text = "Only active reading counts - turn pages to keep the clock running.",
                    color = PaperInkMuted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
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
    val shape = RoundedCornerShape(12.dp)
    val badge = if (big) listOf(Color(0xFF9C74E0), Color(0xFF5E36A8)) else listOf(Color(0xFFF2B845), Color(0xFFB8701E))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (claimable) Color(0xFFE3EFCB) else PaperCardFill)
            .border(if (claimable) 1.5.dp else 1.dp, if (claimable) PaperGood else PaperCardEdge, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Znaczek z progiem celu ("15m" / "1h") zamiast emoji prezentu i korony.
        Box(
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer { if (claimable) { scaleX = pulse; scaleY = pulse } }
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(badge))
                .border(1.5.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            StrokedText(
                text = if (goal.threshold >= 60) "${goal.threshold / 60}h" else "${goal.threshold}m",
                size = 14.sp,
                color = Color.White,
                outline = badge.last().copy(red = badge.last().red * 0.5f, green = badge.last().green * 0.5f, blue = badge.last().blue * 0.5f),
                strokeWidth = 5f,
                tight = true
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            GlyphText(
                text = goal.title,
                color = PaperInkColor,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                lineHeight = 16.sp
            )
            RewardChips(goal)
        }
        when {
            claimed -> GlyphText("✓ Claimed", color = PaperGood, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 12.sp)
            claimable -> KitButton(label = "CLAIM", colors = GreenButton, size = 13.sp, onClick = onClaim)
            else -> Column(horizontalAlignment = Alignment.End) {
                GlyphText(
                    text = "${((goal.threshold * 60 - seconds + 59) / 60)} min",
                    color = PaperInkColor,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    lineHeight = 15.sp
                )
                PaperCaption("left")
            }
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
        GlyphText(
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
            GlyphText("🎁", fontSize = 26.sp)
            Column {
                KitText(text = "${goal.title} unlocked!", size = 15.sp, color = KitGold, strokeWidth = 4f)
                GlyphText("Claim it in the Library", color = Color.White, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}
