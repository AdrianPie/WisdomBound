package com.lernhero.game.battle.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.live.AnswerTimer
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.game.battle.TurnOutcome
import com.lernhero.game.battle.WRONG_ANSWER_ENEMY_BONUS
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private val Good = Color(0xFF3BE07F)
private val GoodDeep = Color(0xFF148A45)
private val Gold = Color(0xFFFFD34D)
private val Bad = Color(0xFFFF5446)
private val BadDeep = Color(0xFF8E1C15)
private val Outline = Color(0xFF140C06)

/**
 * Efekt odpowiedzi - to, co gra sie na polu bitwy w takcie [com.lernhero.game.battle.TurnPhase.Impact],
 * ZANIM ktokolwiek uderzy.
 *
 * Wczesniej byla tu karta z tabelka (werdykt, zetony, rownanie, pasek) - poprawna, ale czytalo sie
 * ja jak paragon, a nie jak cios. Teraz ma to jedna pointe i trzy uderzenia, jak w bijatyce:
 *
 *  1. **Blysk + fala uderzeniowa + iskry** - caly ekran reaguje na odpowiedz w kolorze werdyktu.
 *  2. **Pieczec** ("PERFECT!", "GREAT!", "CORRECT!" albo "WRONG!") wbija sie w ekran z duzej skali;
 *     zla odpowiedz dodatkowo trzesie pieczecia.
 *  3. **Mnoznik salwy** ("×1.40 DMG") - najwieksza liczba na ekranie, bo to ona mowi, co dala
 *     odpowiedz. Pod nia maly licznik ataku druzyny, ktory dolicza sie od bazy do wyniku.
 *
 * Wszystkie czasy ida przez [BattleSpeed], wiec na 4x/8x efekt jest odpowiednio krotszy.
 */
@Composable
internal fun TurnImpactOverlay(
    outcome: TurnOutcome?,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    // Ostatni wynik trzymamy osobno, zeby efekt mogl spokojnie zgasnac juz po tym, jak stan tury
    // poszedl dalej - inaczej znikalby w jednej klatce, w polowie animacji.
    var shown by remember { mutableStateOf<TurnOutcome?>(null) }
    LaunchedEffect(outcome?.id) { outcome?.let { shown = it } }
    val entry = shown ?: return

    val fade by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(BattleSpeed.scale(if (visible) 120 else 260)),
        label = "impactFade"
    )
    if (fade <= 0.01f) return

    val accent = if (entry.correct) Good else Bad
    val tier = verdictOf(entry)

    // Jedna os czasu na caly efekt - kazda warstwa czyta z niej swoj fragment.
    val flash = remember(entry.id) { Animatable(0f) }
    val burst = remember(entry.id) { Animatable(0f) }
    val stamp = remember(entry.id) { Animatable(0f) }
    val jolt = remember(entry.id) { Animatable(0f) }
    val multi = remember(entry.id) { Animatable(0f) }
    val counter = remember(entry.id) { Animatable(0f) }

    LaunchedEffect(entry.id) {
        launch {
            flash.snapTo(1f)
            flash.animateTo(0f, tween(BattleSpeed.scale(420), easing = EaseOutCubic))
        }
        launch { burst.animateTo(1f, tween(BattleSpeed.scale(900), easing = EaseOutCubic)) }
        launch {
            stamp.animateTo(1f, tween(BattleSpeed.scale(260), easing = EaseOutBack))
            if (!entry.correct) {
                jolt.animateTo(
                    0f,
                    keyframes {
                        durationMillis = BattleSpeed.scale(420)
                        -16f at BattleSpeed.scale(50)
                        14f at BattleSpeed.scale(110)
                        -10f at BattleSpeed.scale(180)
                        8f at BattleSpeed.scale(250)
                        -4f at BattleSpeed.scale(330)
                    }
                )
            }
        }
        delay(BattleSpeed.scale(MULTIPLIER_DELAY_MILLIS))
        launch { multi.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = 380f)) }
        delay(BattleSpeed.scale(COUNTER_DELAY_MILLIS))
        counter.animateTo(1f, tween(BattleSpeed.scale(COUNTER_MILLIS), easing = EaseOutCubic))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = fade },
        contentAlignment = Alignment.Center
    ) {
        // Arena gasnie pod efektem - to jedyna chwila tury, w ktorej gracz ma patrzec tylko tutaj.
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = SCRIM_ALPHA)))

        // Blysk calego ekranu w kolorze werdyktu.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(accent.copy(alpha = 0.38f * flash.value))
        )

        BurstLayer(
            seed = entry.id,
            progress = burst.value,
            accent = accent,
            correct = entry.correct,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // Pieczec werdyktu - wbija sie z duzej skali i lekko przekrzywiona.
            OutlinedText(
                text = tier.title,
                color = accent,
                size = 46.sp,
                modifier = Modifier.graphicsLayer {
                    val s = 2.6f - 1.6f * stamp.value
                    scaleX = s
                    scaleY = s
                    alpha = stamp.value.coerceIn(0f, 1f)
                    rotationZ = -6f * stamp.value
                    translationX = jolt.value * density
                }
            )

            Text(
                text = subtitleOf(entry, tier),
                color = Color.White.copy(alpha = 0.85f),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(shadow = Shadow(Outline, blurRadius = 6f)),
                modifier = Modifier.graphicsLayer { alpha = stamp.value.coerceIn(0f, 1f) }
            )

            // Pointa: mnoznik salwy. Najwiekszy element po pieczeci.
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .padding(top = 6.dp)
                    .graphicsLayer {
                        val s = 0.4f + 0.6f * multi.value
                        scaleX = s
                        scaleY = s
                        alpha = multi.value.coerceIn(0f, 1f)
                    }
            ) {
                OutlinedText(
                    text = entry.damageMultiplier.asMultiplier(),
                    color = if (entry.correct) Gold else Bad,
                    size = 40.sp
                )
                Text(
                    text = "DMG",
                    color = Color.White.copy(alpha = 0.8f),
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Drobne skladniki - tylko te, ktore cos zmienily.
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.graphicsLayer { alpha = counter.value.coerceIn(0f, 1f) * 1.4f }
            ) {
                if (!entry.correct) {
                    Chip("🛡", "Enemies +${enemyBonusPercent()}%", Bad)
                }
                if (entry.correct && entry.cycleMultiplier != 1f) {
                    val buffed = entry.cycleMultiplier > 1f
                    Chip(
                        if (buffed) "🔥" else "🔻",
                        (if (buffed) "Streak " else "Slump ") + entry.cycleMultiplier.asPercent(),
                        if (buffed) Good else Bad
                    )
                }
                if (entry.ultiTriggered) Chip("⚡", "ULTI!", Gold)
            }

            AttackCounter(entry = entry, progress = counter.value, accent = accent)

            entry.cycleLabel?.let { label ->
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.85f * counter.value),
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Fala uderzeniowa i iskry - wszystko na jednym Canvasie, liczone z [progress] (0..1),
 * wiec nie ma tu zadnej petli klatek ani stanu per iskra. Iskry maja kierunki z [seed], zeby kazda
 * odpowiedz wybuchala troche inaczej, ale ta sama nie migotala przy rekompozycji.
 */
@Composable
private fun BurstLayer(
    seed: Long,
    progress: Float,
    accent: Color,
    correct: Boolean,
    modifier: Modifier = Modifier
) {
    val sparks = remember(seed) {
        val random = Random(seed)
        List(if (correct) 22 else 14) {
            Spark(
                angle = random.nextFloat() * 2f * PI.toFloat(),
                speed = 0.55f + random.nextFloat() * 0.6f,
                size = 3f + random.nextFloat() * 5f,
                gold = random.nextFloat() < 0.45f
            )
        }
    }

    Canvas(modifier = modifier) {
        val c = Offset(size.width / 2f, size.height * 0.42f)
        val reach = size.minDimension * 0.62f

        // Bez promieni i poswiaty za pieczecia - obracajace sie swiatla sa juz w wielu miejscach
        // aplikacji i tutaj tylko zaglusaly werdykt. Zostaje sam wybuch: fale i iskry.

        // Dwie fale uderzeniowe - druga pozniej i szersza.
        for (k in 0..1) {
            val p = ((progress - k * 0.18f) / 0.82f).coerceIn(0f, 1f)
            if (p <= 0f || p >= 1f) continue
            drawCircle(
                color = (if (k == 0) Color.White else accent).copy(alpha = (1f - p) * 0.8f),
                radius = reach * (0.15f + 0.95f * p),
                center = c,
                style = Stroke(width = (14f - 10f * p) * density)
            )
        }

        // Iskry - leca na zewnatrz, zwalniaja i gasna. Zla odpowiedz sypie mniej i "w dol".
        sparks.forEach { spark ->
            val travel = reach * spark.speed * EaseOutCubic.transform(progress)
            val drop = if (correct) 0f else reach * 0.35f * progress * progress
            val x = c.x + cos(spark.angle) * travel
            val y = c.y + sin(spark.angle) * travel + drop
            val alpha = (1f - progress).coerceIn(0f, 1f)
            val color = if (spark.gold && correct) Gold else accent
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = spark.size * density * (1f - 0.5f * progress),
                center = Offset(x, y)
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.8f),
                radius = spark.size * density * 0.4f * (1f - 0.5f * progress),
                center = Offset(x, y)
            )
        }
    }
}

private data class Spark(val angle: Float, val speed: Float, val size: Float, val gold: Boolean)

/** Atak druzyny doliczajacy sie od bazy do wyniku - maly, bo mnoznik nad nim mowi juz wszystko. */
@Composable
private fun AttackCounter(entry: TurnOutcome, progress: Float, accent: Color) {
    val current = (entry.baseAttack + (entry.boostedAttack - entry.baseAttack) * progress).roundToInt()
    Row(
        modifier = Modifier
            .graphicsLayer { alpha = (progress * 3f).coerceIn(0f, 1f) }
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.55f))
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "⚔", fontSize = 13.sp)
        Text(
            text = "TEAM ATK",
            color = Color.White.copy(alpha = 0.6f),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
        Text(
            text = "${entry.baseAttack}",
            color = Color.White.copy(alpha = 0.5f),
            fontFamily = FontPixel(),
            fontSize = 13.sp
        )
        Text(text = "→", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        Text(
            text = "$current",
            color = if (entry.correct) Gold else Bad,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun Chip(icon: String, label: String, accent: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.2f))
            .border(1.dp, accent.copy(alpha = 0.8f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 11.sp)
        Text(
            text = label,
            color = Color.White,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

/** Gruby napis z ciemnym obrysem - czytelny na kazdym tle krainy. */
@Composable
private fun OutlinedText(text: String, color: Color, size: TextUnit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color = Outline,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = size,
            style = TextStyle(drawStyle = Stroke(width = 14f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        )
        Text(
            text = text,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = size,
            style = TextStyle(
                brush = Brush.verticalGradient(listOf(Color.White, color, color)),
                shadow = Shadow(color.copy(alpha = 0.8f), blurRadius = 18f)
            )
        )
    }
}

/** Stopien werdyktu - im szybsza dobra odpowiedz, tym mocniejsza pieczec. */
private enum class Verdict(val title: String) {
    Perfect("PERFECT!"),
    Great("GREAT!"),
    Correct("CORRECT"),
    Wrong("WRONG!")
}

private fun verdictOf(entry: TurnOutcome): Verdict = when {
    !entry.correct -> Verdict.Wrong
    entry.speedMultiplier >= AnswerTimer.CORRECT_PEAK - 0.001f -> Verdict.Perfect
    entry.speedMultiplier >= 1.2f -> Verdict.Great
    else -> Verdict.Correct
}

private fun subtitleOf(entry: TurnOutcome, verdict: Verdict): String = when (verdict) {
    Verdict.Wrong -> "Weaker volley this turn"
    Verdict.Perfect -> "Full power · ${entry.answerSeconds.oneDecimal()}s"
    else -> "Answered in ${entry.answerSeconds.oneDecimal()}s"
}

private fun enemyBonusPercent(): Int = ((WRONG_ANSWER_ENEMY_BONUS - 1f) * 100).roundToInt()

/** 1.4f -> "×1.40". */
private fun Float.asMultiplier(): String {
    val h = (this * 100).roundToInt()
    return "×${h / 100}.${(h % 100).toString().padStart(2, '0')}"
}

/** 1.35f -> "+35%", 0.75f -> "-25%". */
private fun Float.asPercent(): String {
    val delta = ((this - 1f) * 100).roundToInt()
    return if (delta >= 0) "+$delta%" else "$delta%"
}

/** 1.35f -> "1.4" - bez kotlin.String.format, ktorego nie ma w common. */
private fun Float.oneDecimal(): String {
    val scaled = (this * 10).roundToInt()
    return "${scaled / 10}.${scaled % 10}"
}

/** Po ilu ms od pieczeci wskakuje mnoznik - chwila na przeczytanie werdyktu. */
private const val MULTIPLIER_DELAY_MILLIS = 380L

/** Po ilu ms od mnoznika rusza licznik ataku. */
private const val COUNTER_DELAY_MILLIS = 420L

/** Ile trwa doliczanie ataku druzyny. */
private const val COUNTER_MILLIS = 800

/** Jak mocno arena gasnie pod efektem. */
private const val SCRIM_ALPHA = 0.55f
