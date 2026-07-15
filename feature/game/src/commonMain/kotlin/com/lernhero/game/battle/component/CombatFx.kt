package com.lernhero.game.battle.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.playback.HitKind
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.game.battle.FloatingEffect
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

/*
 * Efekty bojowe przeniesione 1:1 z animacji zrobionych w Claude Design (procedularne sceny
 * wektorowe). Zamiast spritesheetow rysujemy je na [Canvas], wiec dowolna liczba trafien odpala
 * sie naraz bez jednego dodatkowego pliku PNG.
 *
 * Kazdy efekt to jedna funkcja postepu p: 0..1 - dokladnie jak w oryginale (rise, pop, fade).
 */

/** Kolor liczby obrazen wg rodzaju trafienia. Wprost z palety oryginalnych scen. */
private fun HitKind.color(): Color = when (this) {
    HitKind.Hit -> Color(0xFFFFFFFF)
    HitKind.Crit -> Color(0xFFFFDE59)
    HitKind.Burn -> Color(0xFFFF8A3C)
    HitKind.Poison -> Color(0xFF9CE24B)
    HitKind.Reflect -> Color(0xFFFF7A6B)
    HitKind.Skill -> Color(0xFF8FD0FF)
    HitKind.Heal -> Color(0xFF7BE88A)
}

/* ---------------------------------- helpery czasu ----------------------------------- */

private fun interp(p: Float, a: Float, b: Float, from: Float, to: Float): Float {
    if (p <= a) return from
    if (p >= b) return to
    return from + (to - from) * ((p - a) / (b - a))
}

private fun easeOutQuad(t: Float): Float = 1f - (1f - t) * (1f - t)
private fun easeOutCubic(t: Float): Float = 1f - (1f - t).pow(3)
private fun easeOutBack(t: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    return 1f + c3 * (t - 1f).pow(3) + c1 * (t - 1f).pow(2)
}

/** Trojkatny impuls: 0 na krancach, 1 w srodku [a,b]. Uzywany do bunt/flinch. */
private fun pulse(p: Float, a: Float, b: Float): Float {
    if (p < a || p > b) return 0f
    val t = (p - a) / (b - a)
    return sin(t * PI).toFloat()
}

/* --------------------------------- warstwa efektow ---------------------------------- */

/**
 * Nakladka rysujaca wszystkie aktywne efekty. Kazdy jest przypiety do srodka swojego celu
 * ([slotPositions] trzyma pozycje w ukladzie korzenia, ten sam, w ktorym lezy ta nakladka).
 *
 * Efekt sam siebie zdejmuje: po dobiegnieciu animacji wola [onConsumed].
 */
@Composable
internal fun CombatFxLayer(
    effects: List<FloatingEffect>,
    slotPositions: Map<String, Offset>,
    cardSize: Dp,
    onConsumed: (Long) -> Unit
) {
    // Pozycje celow sa w ukladzie korzenia. Odejmujemy wlasne polozenie warstwy, wiec efekt
    // trafia w cel niezaleznie od tego, gdzie ta nakladka wyladowala w hierarchii.
    var layerOrigin by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { layerOrigin = it.positionInRoot() }
    ) {
        effects.forEach { fx ->
            val center = slotPositions[fx.targetId] ?: return@forEach
            key(fx.id) {
                FloatingEffectView(
                    effect = fx,
                    centerInLayer = center - layerOrigin,
                    cardSize = cardSize,
                    onDone = { onConsumed(fx.id) }
                )
            }
        }
    }
}

@Composable
private fun FloatingEffectView(
    effect: FloatingEffect,
    centerInLayer: Offset,
    cardSize: Dp,
    onDone: () -> Unit
) {
    val density = LocalDensity.current
    val progress = remember(effect.id) { Animatable(0f) }

    LaunchedEffect(effect.id) {
        // Tempo goni predkosc walki, ale efekt nie moze mrugnac szybciej niz da sie zobaczyc.
        val duration = BattleSpeed.scale(900L).coerceAtLeast(320L)
        progress.animateTo(1f, androidx.compose.animation.core.tween(duration.toInt()))
        onDone()
    }

    // Plotno efektu jest wieksze od karty, bo wybuch i embersy wychodza poza obrys postaci.
    val fxSize = cardSize * 2.6f
    val fxPx = with(density) { fxSize.toPx() }
    // Sprite jest wyrownany do dolu karty, wiec srodek karty wypada na glowie. Schodzimy na tors,
    // zeby wybuch trafial w postac, a nie unosil sie nad nia.
    val bodyShiftPx = with(density) { (cardSize * 0.68f).toPx() }
    val measurer = rememberTextMeasurer()
    val p = progress.value

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (centerInLayer.x - fxPx / 2f).toInt(),
                    y = (centerInLayer.y - fxPx / 2f + bodyShiftPx).toInt()
                )
            }
            .size(fxSize),
        contentAlignment = Alignment.Center
    ) {
        val numberColor = effect.kind.color()
        val label = if (effect.kind == HitKind.Heal) "+${effect.amount}" else "-${effect.amount}"
        val crit = effect.kind == HitKind.Crit

        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            // Skala: sceny w oryginale operowaly w ~1000 px, tutaj mieszcza sie w plotnie efektu.
            val k = size.minDimension / 620f

            when (effect.kind) {
                HitKind.Burn -> drawFireBurst(c, k, p)
                HitKind.Poison -> drawPoisonCloud(c, k, p)
                HitKind.Heal -> drawHealAura(c, k, p)
                HitKind.Crit -> drawImpactStar(c, k, p, numberColor, big = true)
                else -> drawImpactStar(c, k, p, numberColor, big = false)
            }

            drawDamageNumber(measurer, c, k, p, label, numberColor, crit)
        }
    }
}

/* ------------------------------- pływajaca liczba ---------------------------------- */

/**
 * Liczba obrazen: wystrzeliwuje w gore, robi "pop" skala, po czym gasnie. Timing przepisany
 * wprost z komponentu Damage w oryginale.
 */
private fun DrawScope.drawDamageNumber(
    measurer: androidx.compose.ui.text.TextMeasurer,
    center: Offset,
    k: Float,
    p: Float,
    text: String,
    color: Color,
    crit: Boolean
) {
    if (p <= 0f) return

    val rise = interp(easeOutQuad(p), 0f, 1f, 0f, -140f) * k
    val scale = if (p < 0.16f) {
        interp(easeOutBack(p / 0.16f), 0f, 1f, 0.2f, 1.28f)
    } else {
        interp(easeOutQuad(((p - 0.16f) / 0.18f).coerceIn(0f, 1f)), 0f, 1f, 1.28f, 1.02f)
    }
    val alpha = when {
        p < 0.1f -> p / 0.1f
        p > 0.72f -> interp(p, 0.72f, 1f, 1f, 0f)
        else -> 1f
    }

    val fontPx = (if (crit) 30f else 24f) * k * scale
    val style = TextStyle(
        color = color.copy(alpha = alpha),
        fontSize = (fontPx / density).sp,
        fontWeight = FontWeight.Black,
        textAlign = TextAlign.Center
    )
    val outline = style.copy(color = Color(0xFF141420).copy(alpha = alpha))

    val layout = measurer.measure(text, style)
    val ox = center.x - layout.size.width / 2f
    // Liczba startuje tuz nad glowa (blizej niz w oryginale) i stad wystrzeliwuje w gore.
    val oy = center.y - 70f * k + rise - layout.size.height / 2f

    // Obrys: cztery przesuniete kopie w ciemnym kolorze, potem wypelnienie - tanie "paintOrder=stroke".
    val d = 3f * k
    listOf(Offset(-d, 0f), Offset(d, 0f), Offset(0f, -d), Offset(0f, d)).forEach { off ->
        drawText(measurer, text, topLeft = Offset(ox + off.x, oy + off.y), style = outline)
    }
    drawText(measurer, text, topLeft = Offset(ox, oy), style = style)
}

/* ------------------------------------ fireball ------------------------------------- */

/**
 * Wybuch ognia: rdzen, plomienie w pierscieniu, fala uderzeniowa i rozlatujace sie embersy.
 * Przeniesione ze sceny Fireball - te same kolory i faza wybuchu.
 */
private fun DrawScope.drawFireBurst(center: Offset, k: Float, p: Float) {
    val burst = pulse(p, 0f, 0.5f)
    val ring = interp(p, 0f, 0.34f, 0f, 1f)

    // fala uderzeniowa
    if (ring in 0.001f..0.999f) {
        drawCircle(
            color = Color(0xFFFFBB55).copy(alpha = 1f - ring),
            radius = (40f + ring * 220f) * k,
            center = center,
            style = Stroke(width = 14f * (1f - ring) * k)
        )
    }
    // plomienie w pierscieniu
    if (burst > 0f) {
        for (i in 0 until 7) {
            val a = (i / 7f) * 2f * PI.toFloat()
            val d = (60f + burst * 90f) * k
            val fx = center.x + cos(a) * d
            val fy = center.y + sin(a) * d * 0.7f
            drawCircle(
                color = if (i % 2 == 0) Color(0xFFF5451F) else Color(0xFFFF9E2C),
                radius = (34f + sin(i.toFloat()) * 12f) * burst * k,
                center = Offset(fx, fy),
                alpha = 0.7f * burst
            )
        }
        // rozzarzony rdzen
        drawCircle(
            color = Color(0xFFFFE07A),
            radius = (40f + burst * 60f) * k,
            center = center,
            alpha = 0.85f * burst
        )
    }
    // embersy - rozlatuja sie i opadaja
    val et = interp(p, 0f, 0.6f, 0f, 1f)
    if (et in 0.001f..0.999f) {
        for (i in 0 until 12) {
            val a = (i / 12f) * 2f * PI.toFloat()
            val spd = (120f + (i % 4) * 60f) * k
            val ex = center.x + cos(a) * spd * et
            val ey = center.y + sin(a) * spd * 0.6f * et + et * et * 180f * k
            drawCircle(
                color = if (i % 2 == 0) Color(0xFFFFC24A) else Color(0xFFFF7A2A),
                radius = 9f * (1f - et) * k,
                center = Offset(ex, ey),
                alpha = 1f - et
            )
        }
    }
}

/* ------------------------------------- poison -------------------------------------- */

/**
 * Chmura trucizny: zielone kleby gazu, unoszace sie banki i czaszka. Ze sceny Poison.
 */
private fun DrawScope.drawPoisonCloud(center: Offset, k: Float, p: Float) {
    val cloud = pulse(p, 0.06f, 0.96f)

    // kleby gazu
    val puffs = listOf(
        Triple(-90f, -60f, 90f), Triple(90f, -60f, 96f), Triple(0f, -120f, 104f),
        Triple(-120f, 30f, 78f), Triple(120f, 30f, 82f), Triple(0f, 20f, 120f)
    )
    puffs.forEach { (dx, dy, r) ->
        drawCircle(
            color = Color(0xFF8FE04B),
            radius = r * (0.7f + 0.4f * cloud) * k,
            center = Offset(center.x + dx * k, center.y + dy * k),
            alpha = 0.12f * cloud
        )
    }
    // unoszace sie banki
    for (i in 0 until 7) {
        val ph = ((p * 1.4f + i / 7f) % 1f)
        val bx = center.x + (sin(i * 2.1f + ph * 4f) * 70f + (i - 3) * 14f) * k
        val by = center.y + (30f - ph * 250f) * k
        val r = max((10f - ph * 6f) * (0.7f + (i % 3) * 0.2f), 1f) * k
        drawCircle(
            color = Color(0xFFB6EF5A),
            radius = r,
            center = Offset(bx, by),
            alpha = sin(ph * PI).toFloat() * 0.85f
        )
    }
    // czaszka wyskakuje razem z liczba
    val skullP = interp(p, 0.36f, 1f, 0f, 1f)
    if (skullP > 0f) {
        val op = when {
            skullP < 0.15f -> skullP / 0.15f
            skullP > 0.7f -> interp(skullP, 0.7f, 1f, 1f, 0f)
            else -> 1f
        }
        drawSkull(Offset(center.x + 120f * k, center.y - 100f * k - skullP * 120f * k), 0.9f * k, op)
    }
}

/** Mala czaszka - motyw trucizny. Przepisana z komponentu Skull. */
private fun DrawScope.drawSkull(at: Offset, s: Float, op: Float) {
    val bone = Color(0xFFE9F6D6).copy(alpha = op)
    val socket = Color(0xFF3A4D1F).copy(alpha = op)
    drawCircle(bone, radius = 26f * s, center = at)
    drawCircle(bone, radius = 18f * s, center = Offset(at.x, at.y + 22f * s))
    drawCircle(socket, radius = 7f * s, center = Offset(at.x - 11f * s, at.y + 4f * s))
    drawCircle(socket, radius = 7f * s, center = Offset(at.x + 11f * s, at.y + 4f * s))
}

/* -------------------------------------- heal --------------------------------------- */

/** Leczenie: zielona aura, rozchodzacy sie pierscien i unoszace sie plusiki. Ze sceny Heal. */
private fun DrawScope.drawHealAura(center: Offset, k: Float, p: Float) {
    val aura = pulse(p, 0.06f, 0.92f)
    drawCircle(
        color = Color(0xFF5FD07A),
        radius = 200f * (0.7f + 0.4f * aura) * k,
        center = Offset(center.x, center.y + 30f * k),
        alpha = 0.15f * aura
    )
    val ring = interp(p, 0.24f, 0.7f, 0f, 1f)
    if (ring in 0.001f..0.999f) {
        drawCircle(
            color = Color(0xFF9FF0A8).copy(alpha = 1f - ring),
            radius = (40f + ring * 180f) * k,
            center = Offset(center.x, center.y + 60f * k),
            style = Stroke(width = 10f * (1f - ring) * k)
        )
    }
    for (i in 0 until 9) {
        val ph = ((p * 1.2f + i / 9f) % 1f)
        val px = center.x + (sin(i * 1.7f + ph * 3f) * 120f + (i - 4) * 10f) * k
        val py = center.y + (110f - ph * 300f) * k
        val op = sin(ph * PI).toFloat() * 0.9f
        val s = (0.5f + (i % 3) * 0.25f) * (1f - ph * 0.3f) * k
        drawPlus(Offset(px, py), s, if (i % 2 == 0) Color(0xFF7BE88A) else Color(0xFFFFE07A), op)
    }
}

private fun DrawScope.drawPlus(at: Offset, s: Float, color: Color, op: Float) {
    val col = color.copy(alpha = op)
    drawRoundRectCentered(col, at, 8f * s, 28f * s)
    drawRoundRectCentered(col, at, 28f * s, 8f * s)
}

private fun DrawScope.drawRoundRectCentered(color: Color, at: Offset, w: Float, h: Float) {
    drawRect(color = color, topLeft = Offset(at.x - w / 2f, at.y - h / 2f), size = androidx.compose.ui.geometry.Size(w, h))
}

/* ----------------------------- zwykle trafienie / krytyk ---------------------------- */

/**
 * Blysk dla ciosu, krytyka, odbicia i efektu bez wlasnej sceny: kilka promieni + rozblysk.
 * Krotki i mocny, zeby bylo widac "cios wszedl", bez udawania spritea, ktorego nie mamy.
 */
private fun DrawScope.drawImpactStar(center: Offset, k: Float, p: Float, color: Color, big: Boolean) {
    val flash = pulse(p, 0f, 0.42f)
    if (flash <= 0f) return

    val spokes = if (big) 8 else 6
    val len = (if (big) 150f else 100f) * k * flash
    val width = (if (big) 12f else 8f) * k * flash
    rotate(degrees = if (big) 22f else 0f, pivot = center) {
        for (i in 0 until spokes) {
            val a = (i / spokes.toFloat()) * 2f * PI.toFloat()
            val end = Offset(center.x + cos(a) * len, center.y + sin(a) * len)
            drawLine(
                color = color.copy(alpha = 0.85f * flash),
                start = center,
                end = end,
                strokeWidth = width
            )
        }
    }
    drawCircle(
        color = Color(0xFFFFFFFF).copy(alpha = 0.9f * flash),
        radius = (if (big) 34f else 22f) * k * flash,
        center = center
    )
    drawCircle(
        color = color.copy(alpha = 0.5f * flash),
        radius = (if (big) 60f else 40f) * k * flash,
        center = center
    )
}
