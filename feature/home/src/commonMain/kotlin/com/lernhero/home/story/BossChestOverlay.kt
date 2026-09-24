package com.lernhero.home.story

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.run.ChestSlot
import com.lernhero.domain.run.ChestWheel
import com.lernhero.game.spritesheet.SpriteSheetFrame
import com.lernhero.game.spritesheet.SpriteSheetSpec
import com.lernhero.game.spritesheet.frameForProgress
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private enum class ChestPhase { Dropping, Ready, Opening, Revealed }

/**
 * Skrzynia z nagroda - po pierwszym pokonaniu bossa i za dzienny cel krokow.
 *
 * Cztery takty, kazdy z wlasnym efektem, zeby nagroda byla wydarzeniem, a nie linijka tekstu:
 *  1. skrzynia SPADA z gory i odbija sie od ziemi (wstrzas),
 *  2. czeka na tapniecie - pulsuje poswiata pod spodem, dookola migaja iskry,
 *  3. otwiera sie animacja ze spritesheeta ([Resources.Image.treasureChestSheet], 40 klatek),
 *  4. blysk - nagroda wyskakuje ze skrzyni na tle obracajacych sie promieni w kolorze rzadkosci,
 *     z iskrami dookola, a przy zlocie tryska fontanna monet.
 *
 * Nagroda jest wylosowana ZANIM cokolwiek ruszy ([prize]) - animacja jej tylko nie zdradza.
 */
@Composable
fun BossChestOverlay(
    dungeonLevel: Int,
    slot: ChestSlot,
    prize: ChestPrize,
    onClaim: () -> Unit,
    title: String = "BOSS TREASURE",
    chanceOf: ((ChestSlot) -> Float)? = null,
    /** Ile dolu sceny zaslania cos z wierzchu (dolny pasek) - tlo idzie pod spod, tresc sie odsuwa. */
    bottomInset: androidx.compose.ui.unit.Dp = 0.dp,
    /**
     * Pewna czesc zawartosci (skrzynie ze sklepu): chipy "+1 250 🪙" pod glowna nagroda. Miejsce na
     * nie jest zarezerwowane od poczatku, zeby po otwarciu nic nie podskoczylo.
     */
    extras: List<Pair<String, Int>> = emptyList(),
    modifier: Modifier = Modifier
) {
    val sheet = remember { SpriteSheetSpec(drawable = Resources.Image.treasureChestSheet, frameCount = CHEST_FRAMES, columns = 4) }
    val accent = slot.accent()

    var phase by remember { mutableStateOf(ChestPhase.Dropping) }
    val drop = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }
    val open = remember { Animatable(0f) }
    val reveal = remember { Animatable(0f) }
    val flash = remember { Animatable(0f) }
    val fountain = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        delay(DROP_DELAY_MS)
        drop.animateTo(1f, tween(DROP_MS, easing = EaseOutBounce))
        launch {
            shake.snapTo(1f)
            shake.animateTo(0f, tween(SHAKE_MS))
        }
        phase = ChestPhase.Ready
    }

    // Klucz to faza, wiec zmiana fazy anuluje ten efekt - dlatego kazda faza ma tu WLASNA galaz,
    // a przejscie do nastepnej jest zawsze ostatnia instrukcja galezi.
    LaunchedEffect(phase) {
        when (phase) {
            ChestPhase.Opening -> {
                open.snapTo(0f)
                open.animateTo(1f, tween(OPEN_MS, easing = LinearEasing))
                flash.snapTo(1f)
                phase = ChestPhase.Revealed
            }

            ChestPhase.Revealed -> {
                launch { flash.animateTo(0f, tween(FLASH_MS)) }
                if (slot.goldAmount > 0 || prize.icon == "💰") {
                    launch {
                        fountain.snapTo(0f)
                        fountain.animateTo(1f, tween(FOUNTAIN_MS, easing = LinearEasing))
                    }
                }
                reveal.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 160f))
            }

            else -> Unit
        }
    }

    val infinite = rememberInfiniteTransition()
    val bob by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = EaseInOutSine), RepeatMode.Reverse)
    )
    val spin by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing))
    )
    val twinkle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing))
    )

    val startOpening = { if (phase == ChestPhase.Ready) phase = ChestPhase.Opening }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF1E1426), Color(0xFF07050B))))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Scena ma pusta gore (tam wyskakuje nagroda), wiec bez podniesienia skrzynia siedzi pod srodkiem.
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp + bottomInset),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = title, color = Color(0xFFFFD34D), fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Text(
                text = when (phase) {
                    ChestPhase.Dropping -> " "
                    ChestPhase.Ready -> "Tap the chest to open it"
                    ChestPhase.Opening -> "..."
                    ChestPhase.Revealed -> if (slot.isJackpot) "JACKPOT!" else "You found"
                },
                color = if (phase == ChestPhase.Revealed && slot.isJackpot) accent else Color(0xFF9D8B6A),
                fontFamily = if (phase == ChestPhase.Revealed && slot.isJackpot) FontPixel() else FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = if (phase == ChestPhase.Revealed && slot.isJackpot) 16.sp else 13.sp,
                modifier = Modifier.graphicsLayer {
                    if (phase == ChestPhase.Revealed && slot.isJackpot) {
                        val s = 1f + 0.08f * bob
                        scaleX = s
                        scaleY = s
                    }
                }
            )

            Box(
                modifier = Modifier.fillMaxWidth().height(STAGE_HEIGHT),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Promienie za nagroda - dwie warstwy w przeciwnych kierunkach, przy jackpocie gestsze.
                if (phase == ChestPhase.Revealed) {
                    LightRays(accent = accent, rotation = spin, strength = reveal.value.coerceIn(0f, 1f), dense = slot.isJackpot)
                }

                // Poswiata pod skrzynia: mocniejsza, gdy czeka na tapniecie.
                Box(
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .width(220.dp)
                        .height(60.dp)
                        .graphicsLayer {
                            alpha = when (phase) {
                                ChestPhase.Dropping -> drop.value * 0.6f
                                ChestPhase.Ready -> 0.55f + 0.3f * (bob + 1f) / 2f
                                else -> 0.9f
                            }
                        }
                        .background(
                            Brush.radialGradient(listOf(accentGlow(phase, accent).copy(alpha = 0.7f), Color.Transparent)),
                            RoundedCornerShape(50)
                        )
                )

                if (phase == ChestPhase.Ready) {
                    Sparkles(twinkle = twinkle, color = Color(0xFFFFF1C2), radius = 120f, count = 7, seed = 3)
                }

                // Sama skrzynia ze spritesheeta. Klatka jest prostokatna (16:9) - aspectRatio trzyma
                // proporcje, inaczej obraz sie splaszcza.
                val frame = when (phase) {
                    ChestPhase.Opening -> sheet.frameForProgress(open.value)
                    ChestPhase.Revealed -> sheet.frameCount - 1
                    else -> 0
                }
                Box(
                    modifier = Modifier
                        .width(CHEST_WIDTH)
                        .aspectRatio(CHEST_FRAME_RATIO)
                        .graphicsLayer {
                            translationY = -DROP_HEIGHT_PX * (1f - drop.value) * density +
                                if (phase == ChestPhase.Ready) bob * 3f * density else 0f
                            translationX = sin(shake.value * 40f) * 8f * shake.value * density
                            alpha = if (phase == ChestPhase.Revealed) 0.85f else 1f
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = phase == ChestPhase.Ready,
                            onClick = startOpening
                        )
                ) {
                    SpriteSheetFrame(spec = sheet, frame = frame, modifier = Modifier.fillMaxSize())
                }

                if (phase == ChestPhase.Revealed) {
                    CoinFountain(progress = fountain.value)
                    PrizeBurst(prize = prize, accent = accent, reveal = reveal.value, twinkle = twinkle, bob = bob)
                }
            }

            // Miejsce na podpis nagrody jest ZAREZERWOWANE od poczatku (stala wysokosc) - inaczej po
            // otwarciu kolumna rosla, a wysrodkowana scena podskakiwala w gore ("teleport" skrzyni).
            Column(
                modifier = Modifier.height(REVEAL_TEXT_HEIGHT),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
            if (phase == ChestPhase.Revealed) {
                Text(
                    text = prize.name,
                    color = accent,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { alpha = reveal.value.coerceIn(0f, 1f) }
                )
                if (prize.label != prize.name) {
                    Text(text = prize.label, color = Color(0xFFCDBB99), fontFamily = FontFirst(), fontSize = 12.sp, textAlign = TextAlign.Center)
                }
                Text(
                    text = "${slot.label} - ${((chanceOf?.invoke(slot) ?: ChestWheel.chanceOf(slot, dungeonLevel)) * 100).roundToInt()}% chance",
                    color = Color(0xFF6E6250),
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
            }
            }

            if (extras.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .height(EXTRAS_HEIGHT)
                        .graphicsLayer { alpha = if (phase == ChestPhase.Revealed) reveal.value.coerceIn(0f, 1f) else 0f },
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "+", color = Color(0xFF9D8B6A), fontFamily = FontPixel(), fontSize = 14.sp)
                    extras.forEach { (icon, amount) ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF2A1F14))
                                .border(1.5.dp, Color(0xFF6B4A2A), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = icon, fontSize = 12.sp)
                            Text(
                                text = amount.toString().reversed().chunked(3).joinToString(",").reversed(),
                                color = Color(0xFFFFE9A8),
                                fontFamily = FontFirst(),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            val buttonVisible = phase == ChestPhase.Ready || (phase == ChestPhase.Revealed && reveal.value > 0.6f)
            Text(
                text = if (phase == ChestPhase.Revealed) "CLAIM" else "OPEN",
                color = Color(0xFF1C1408),
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .graphicsLayer { alpha = if (buttonVisible) 1f else 0f }
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFE9A8), Color(0xFFD99A1E))))
                    .border(1.5.dp, Color(0xFFFFF1C2), RoundedCornerShape(10.dp))
                    .clickable(enabled = buttonVisible) {
                        if (phase == ChestPhase.Revealed) onClaim() else startOpening()
                    }
                    .padding(horizontal = 32.dp, vertical = 11.dp)
            )
        }

        // Blysk w chwili otwarcia - na caly ekran, w kolorze nagrody przechodzacym w biel.
        if (flash.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = flash.value * if (slot.isJackpot) 0.95f else 0.75f }
                    .background(Brush.radialGradient(listOf(Color.White, accent.copy(alpha = 0.6f), Color.Transparent)))
            )
        }
    }
}

/** Nagroda unoszaca sie ze skrzyni: aureola w kolorze rzadkosci, ikona, iskry dookola. */
@Composable
private fun PrizeBurst(prize: ChestPrize, accent: Color, reveal: Float, twinkle: Float, bob: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = -(PRIZE_RISE_PX * reveal + bob * 4f) * density
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .graphicsLayer {
                    scaleX = reveal
                    scaleY = reveal
                    alpha = reveal.coerceIn(0f, 1f)
                }
                .background(
                    Brush.radialGradient(listOf(Color.White.copy(alpha = 0.9f), accent.copy(alpha = 0.55f), Color.Transparent)),
                    CircleShape
                )
        )
        Sparkles(twinkle = twinkle, color = accent, radius = 78f, count = 8, seed = 11)
        Text(
            text = prize.icon,
            fontSize = 60.sp,
            modifier = Modifier.graphicsLayer {
                scaleX = reveal
                scaleY = reveal
                rotationZ = (1f - reveal.coerceIn(0f, 1f)) * -30f
            }
        )
    }
}

/**
 * Promienie za nagroda: waskie, zwezajace sie smugi w kolorze rzadkosci, gasnace ku brzegom
 * (radialny gradient), plus miekka poswiata w srodku. Bez twardych krawedzi kola - wczesniejszy
 * sweepGradient na CircleShape dawal "tarcze" z ostrym obrysem.
 */
@Composable
private fun LightRays(accent: Color, rotation: Float, strength: Float, dense: Boolean) {
    val rays = if (dense) 14 else 10
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = -PRIZE_RISE_PX * density
                alpha = strength
            }
    ) {
        val c = center
        val radius = RAYS_SIZE.toPx() * (0.75f + 0.25f * strength)
        val rayBrush = Brush.radialGradient(
            0f to accent.copy(alpha = 0.55f),
            0.35f to accent.copy(alpha = 0.22f),
            1f to Color.Transparent,
            center = c,
            radius = radius
        )
        for (layer in 0..1) {
            val turn = if (layer == 0) rotation else -rotation * 0.6f + 180f / rays
            val spread = if (layer == 0) 0.11f else 0.06f
            rotate(turn, c) {
                for (i in 0 until rays) {
                    val angle = (i * 2f * PI / rays).toFloat()
                    val left = angle - spread
                    val right = angle + spread
                    val path = Path().apply {
                        moveTo(c.x, c.y)
                        lineTo(c.x + cos(left) * radius, c.y + sin(left) * radius)
                        lineTo(c.x + cos(right) * radius, c.y + sin(right) * radius)
                        close()
                    }
                    drawPath(path, rayBrush)
                }
            }
        }
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.55f), accent.copy(alpha = 0.25f), Color.Transparent),
                center = c,
                radius = radius * 0.45f
            ),
            radius = radius * 0.45f,
            center = c
        )
    }
}

/** Iskry migajace na okregu - kazda w innej fazie, wiec nie gasna wszystkie naraz. */
@Composable
private fun Sparkles(twinkle: Float, color: Color, radius: Float, count: Int, seed: Int) {
    val points = remember(seed, count) {
        val random = Random(seed)
        List(count) { index ->
            Triple(
                (index.toFloat() / count) * 2f * PI.toFloat() + random.nextFloat() * 0.5f,
                radius * (0.75f + random.nextFloat() * 0.5f),
                random.nextFloat()
            )
        }
    }
    points.forEach { (angle, distance, phase) ->
        val t = (twinkle + phase) % 1f
        val glow = sin(t * PI.toFloat()).coerceAtLeast(0f)
        Text(
            text = "✦",
            color = color,
            fontSize = 14.sp,
            modifier = Modifier.graphicsLayer {
                translationX = cos(angle) * distance * density
                translationY = (sin(angle) * distance - PRIZE_RISE_PX / 2f) * density
                alpha = glow
                scaleX = 0.5f + glow
                scaleY = scaleX
            }
        )
    }
}

/** Monety tryskajace ze skrzyni i spadajace z grawitacja - tylko przy nagrodzie w zlocie. */
@Composable
private fun CoinFountain(progress: Float) {
    if (progress >= 1f) return
    val coins = remember {
        val random = Random(7)
        List(FOUNTAIN_COINS) {
            val angle = (-90f + (random.nextFloat() - 0.5f) * 80f) * PI.toFloat() / 180f
            val speed = 220f + random.nextFloat() * 140f
            Triple(cos(angle) * speed, sin(angle) * speed, random.nextFloat() * 0.25f)
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        coins.forEach { (vx, vy, delayFraction) ->
            val t = ((progress - delayFraction) / (1f - delayFraction)).coerceIn(0f, 1f)
            if (t <= 0f) return@forEach
            Text(
                text = "🪙",
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .graphicsLayer {
                        translationX = vx * t * density
                        translationY = (vy * t + GRAVITY * t * t) * density
                        rotationZ = 540f * t
                        alpha = 1f - t * t
                    }
            )
        }
    }
}

private fun ChestSlot.accent(): Color = when (this) {
    ChestSlot.GoldSmall, ChestSlot.GoldMedium, ChestSlot.GoldLarge -> Color(0xFFFFD34D)
    ChestSlot.SummonTicket -> Color(0xFF6FB7FF)
    ChestSlot.ItemCommon -> Color(0xFFD7D7D7)
    ChestSlot.ItemRare -> Color(0xFFC77DFF)
    ChestSlot.HeroicTicket -> Color(0xFFFF7A45)
    ChestSlot.ItemLegendary -> Color(0xFFFFB02E)
}

/** Przed otwarciem poswiata jest neutralna (zlota) - kolor rzadkosci zdradzilby nagrode. */
private fun accentGlow(phase: ChestPhase, accent: Color): Color =
    if (phase == ChestPhase.Revealed) accent else Color(0xFFFFD34D)

private const val CHEST_FRAMES = 40
private const val CHEST_FRAME_RATIO = 768f / 432f
private val CHEST_WIDTH = 380.dp
private val REVEAL_TEXT_HEIGHT = 64.dp
private val EXTRAS_HEIGHT = 30.dp
private val STAGE_HEIGHT = 380.dp
private val RAYS_SIZE = 270.dp
private const val DROP_HEIGHT_PX = 520f
private const val PRIZE_RISE_PX = 130f
private const val GRAVITY = 520f
private const val FOUNTAIN_COINS = 18

private const val DROP_DELAY_MS = 150L
private const val DROP_MS = 750
private const val SHAKE_MS = 380
private const val OPEN_MS = 2000
private const val FLASH_MS = 550
private const val FOUNTAIN_MS = 1500
