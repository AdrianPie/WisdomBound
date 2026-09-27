package com.lernhero.home.city

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.monster.Element
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.game.character.ElementEmblem
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.shared.theme.StrokedText
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Pirotechnika losowania. Wszystko tutaj czyta kanaly [SummonShow] WEWNATRZ rysowania (Canvas,
 * graphicsLayer), a nie w kompozycji - dzieki temu 60 klatek na sekunde przerysowuje tylko
 * efekty, a nie caly ekran Summon Center.
 */

/** Kolor swiatla danej rzadkosci - jasniejszy niz ramka karty, bo ma swiecic, nie zdobic. */
internal fun Rarity.fx(): Color = when (this) {
    Rarity.Common -> Color(0xFFDCE6F2)
    Rarity.Uncommon -> Color(0xFF5CF08F)
    Rarity.Rare -> Color(0xFF4FA8FF)
    Rarity.Epic -> Color(0xFFC06BFF)
    Rarity.Legendary -> Color(0xFFFFC23A)
}

internal fun Rarity.shoutText(): String = when (this) {
    Rarity.Legendary -> "LEGENDARY!"
    Rarity.Epic -> "EPIC!"
    Rarity.Rare -> "RARE!"
    Rarity.Uncommon -> "UNCOMMON"
    Rarity.Common -> "COMMON"
}

/**
 * Punkty sceny oltarza w pikselach (uklad samej sceny). Liczone raz w [AltarStage] z tych samych
 * stalych, ktore ustawiaja grafike oltarza, wiec efekty zawsze trafiaja w plyte kregu.
 */
internal data class AltarGeometry(
    val size: Size,
    /** Srodek wyrytego kregu na plycie oltarza. */
    val circleCenter: Offset,
    /** Promien kregu w poziomie; w pionie jest [CIRCLE_TILT] razy mniejszy (perspektywa). */
    val circleRx: Float,
    /** Gdzie stoja stopy kompana. */
    val feet: Offset,
    /** Gdzie wisi kula energii - troche nad kregiem. */
    val orb: Offset,
    /** Srodek sylwetki kompana - stad strzelaja promienie i stad wylatuje karta. */
    val chest: Offset,
    /** Skala wszystkich efektow. */
    val base: Float
)

/** Splaszczenie kregu w perspektywie (pion / poziom) - zmierzone na grafice oltarza. */
internal const val CIRCLE_TILT = 0.5f

private val MoteColor = Color(0xFFCFE9FF)

/** Deterministyczny "los" 0..1 - czasteczki maja stale tory, a nie nowe w kazdej klatce. */
private fun rnd(i: Int, salt: Int): Float {
    var x = i * 374761393 + salt * 668265263
    x = (x xor (x ushr 13)) * 1274126177
    x = x xor (x ushr 16)
    return (x and 0x7FFFFFFF) / 2147483647f
}

private fun polar(center: Offset, angle: Float, radius: Float) =
    Offset(center.x + cos(angle) * radius, center.y + sin(angle) * radius)

private fun easeOut(x: Float) = EaseOutCubic.transform(x.coerceIn(0f, 1f))

/* ------------------------------------------------------------------ warstwa POD kompanem */

/**
 * Swiatlo na oltarzu, magiczny krag, fala na plycie, promienie za kompanem, slup swiatla,
 * splywajace czasteczki i kula energii. Rysowane nad grafika oltarza, ale pod kompanem.
 */
@Composable
internal fun RitualUnderFx(
    show: SummonShow,
    geo: AltarGeometry,
    color: State<Color>,
    time: State<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val t = time.value
        val c = color.value
        val gather = show.gather.value
        val circle = show.circle.value
        val burst = show.burst.value
        val rays = show.rays.value
        val pulse = show.tierPulse.value
        val beam = show.beam.value
        val inhale = show.inhale.value

        // 1. Oltarz oswietlony od kuli / kregu - bez tego efekty wisialyby "obok" kamienia.
        val light = (maxOf(gather * 0.8f, circle * 0.75f, rays * 0.55f) + pulse * 0.35f).coerceAtMost(1f)
        if (light > 0.01f) {
            val r = geo.circleRx * 2.6f
            drawCircle(
                brush = Brush.radialGradient(listOf(c.copy(alpha = 0.32f * light), Color.Transparent), geo.circleCenter, r),
                radius = r,
                center = geo.circleCenter
            )
        }

        magicCircle(geo, c, (circle + pulse * 0.6f).coerceAtMost(1f), t)
        if (burst > 0f && burst < 1f) floorShock(geo, c, burst)
        if (rays > 0.01f) godRays(geo, c, rays, t)
        if (beam > 0.01f) beamOfLight(geo, c, beam, pulse, t)
        if (gather > 0.001f && burst <= 0f) {
            gatherMotes(geo, gather, inhale)
            orb(geo, c, gather, inhale, pulse, beam, t)
        }
    }
}

/** Swiecacy krag na plycie: dwa pierscienie, obracajace sie znaczniki, heksagram i punkty. */
private fun DrawScope.magicCircle(geo: AltarGeometry, c: Color, strength: Float, t: Float) {
    if (strength <= 0.01f) return
    val a = strength.coerceIn(0f, 1f)
    val center = geo.circleCenter
    val r = geo.circleRx
    val stroke = 2.2.dp.toPx()
    // Rysujemy okrag i "sciskamy" go w pionie - obroty wewnatrz tez dostaja perspektywe.
    scale(scaleX = 1f, scaleY = CIRCLE_TILT, pivot = center) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(c.copy(alpha = 0.42f * a), c.copy(alpha = 0.14f * a), Color.Transparent),
                center,
                r * 1.2f
            ),
            radius = r * 1.2f,
            center = center
        )
        drawCircle(c.copy(alpha = 0.95f * a), r, center, style = Stroke(stroke * 1.5f))
        drawCircle(c.copy(alpha = 0.7f * a), r * 0.8f, center, style = Stroke(stroke))
        drawCircle(c.copy(alpha = 0.55f * a), r * 0.34f, center, style = Stroke(stroke))

        rotate(degrees = t * 26f, pivot = center) {
            for (i in 0 until 36) {
                val angle = (i * 10f) * (PI.toFloat() / 180f)
                val inner = if (i % 3 == 0) 0.82f else 0.87f
                drawLine(
                    color = c.copy(alpha = 0.85f * a),
                    start = polar(center, angle, r * inner),
                    end = polar(center, angle, r * 0.96f),
                    strokeWidth = stroke * 0.8f
                )
            }
        }
        rotate(degrees = -t * 38f, pivot = center) {
            val hex = Path()
            for (triangle in 0..1) {
                for (k in 0..2) {
                    val angle = (-90f + triangle * 60f + k * 120f) * (PI.toFloat() / 180f)
                    val p = polar(center, angle, r * 0.76f)
                    if (k == 0) hex.moveTo(p.x, p.y) else hex.lineTo(p.x, p.y)
                }
                hex.close()
            }
            drawPath(hex, c.copy(alpha = 0.8f * a), style = Stroke(stroke))
        }
        rotate(degrees = t * 64f, pivot = center) {
            for (i in 0 until 8) {
                val angle = i * (PI.toFloat() / 4f)
                drawCircle(c.copy(alpha = 0.95f * a), stroke * 1.4f, polar(center, angle, r * 0.57f))
            }
        }
    }
}

/** Fala uderzeniowa rozchodzaca sie po plycie oltarza. */
private fun DrawScope.floorShock(geo: AltarGeometry, c: Color, burst: Float) {
    val center = geo.circleCenter
    val r = geo.circleRx
    scale(scaleX = 1f, scaleY = CIRCLE_TILT, pivot = center) {
        val e = easeOut(burst)
        drawCircle(
            color = c.copy(alpha = (1f - burst).pow(1.4f) * 0.9f),
            radius = r * (0.45f + 2.8f * e),
            center = center,
            style = Stroke((16.dp * (1f - e) + 1.dp).toPx())
        )
        val late = ((burst - 0.14f) / 0.86f).coerceIn(0f, 1f)
        if (late > 0f) {
            val e2 = easeOut(late)
            drawCircle(
                color = Color.White.copy(alpha = (1f - late).pow(1.6f) * 0.7f),
                radius = r * (0.3f + 2.1f * e2),
                center = center,
                style = Stroke((8.dp * (1f - e2) + 1.dp).toPx())
            )
        }
        val flashA = (1f - burst).pow(3f) * 0.8f
        drawCircle(
            brush = Brush.radialGradient(listOf(Color.White.copy(alpha = flashA), Color.Transparent), center, r * 1.4f),
            radius = r * 1.4f,
            center = center
        )
    }
}

/** Obracajacy sie wachlarz promieni i poswiata za kompanem. */
private fun DrawScope.godRays(geo: AltarGeometry, c: Color, strength: Float, t: Float) {
    val center = geo.chest
    val length = geo.base * 1.3f
    drawCircle(
        brush = Brush.radialGradient(listOf(c.copy(alpha = 0.42f * strength), Color.Transparent), center, geo.base * 0.62f),
        radius = geo.base * 0.62f,
        center = center
    )
    rotate(degrees = t * 11f, pivot = center) {
        val count = 14
        for (i in 0 until count) {
            val angle = i * (2f * PI.toFloat() / count)
            val half = if (i % 2 == 0) 0.12f else 0.065f
            val ray = Path().apply {
                moveTo(center.x, center.y)
                val a = polar(center, angle - half, length)
                val b = polar(center, angle + half, length)
                lineTo(a.x, a.y)
                lineTo(b.x, b.y)
                close()
            }
            val alpha = strength * if (i % 2 == 0) 0.38f else 0.24f
            drawPath(ray, Brush.radialGradient(listOf(c.copy(alpha = alpha), Color.Transparent), center, length))
        }
    }
}

/**
 * Slup swiatla z kuli w niebo - celowo wychodzi poza scene, az do gornej krawedzi ekranu.
 * Rdzen jest bialy, obrzeze w kolorze rzadkosci; w gore plyna iskierki.
 */
private fun DrawScope.beamOfLight(geo: AltarGeometry, c: Color, beam: Float, pulse: Float, t: Float) {
    val orb = geo.orb
    val reach = (beam * 3f).coerceAtMost(1f)
    val alpha = beam.coerceAtMost(1f)
    val top = orb.y - reach * (orb.y + 2400f)
    val height = orb.y - top
    val width = geo.base * 0.34f * (1f + 0.45f * pulse) * (0.95f + 0.05f * sin(t * 9f))
    drawRect(
        brush = Brush.horizontalGradient(
            listOf(
                Color.Transparent,
                c.copy(alpha = 0.35f * alpha),
                c.copy(alpha = 0.75f * alpha),
                c.copy(alpha = 0.35f * alpha),
                Color.Transparent
            ),
            startX = orb.x - width / 2f,
            endX = orb.x + width / 2f
        ),
        topLeft = Offset(orb.x - width / 2f, top),
        size = Size(width, height)
    )
    val core = width * 0.3f
    drawRect(
        brush = Brush.horizontalGradient(
            listOf(Color.Transparent, Color.White.copy(alpha = 0.95f * alpha), Color.Transparent),
            startX = orb.x - core / 2f,
            endX = orb.x + core / 2f
        ),
        topLeft = Offset(orb.x - core / 2f, top),
        size = Size(core, height)
    )
    // Podstawa slupa rozlewa sie po kregu.
    scale(scaleX = 1f, scaleY = CIRCLE_TILT, pivot = geo.circleCenter) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.7f * alpha), c.copy(alpha = 0.4f * alpha), Color.Transparent),
                geo.circleCenter,
                width * 1.3f
            ),
            radius = width * 1.3f,
            center = geo.circleCenter
        )
    }
    repeat(18) { i ->
        val speed = 0.35f + 0.55f * rnd(i, 1)
        val phase = (t * speed + rnd(i, 2)) % 1f
        val y = orb.y - phase * (orb.y + 120f)
        val x = orb.x + (rnd(i, 3) - 0.5f) * width * 0.7f + sin(t * 3f + i) * 4.dp.toPx()
        drawCircle(
            color = Color.White.copy(alpha = alpha * (1f - phase) * 0.9f),
            radius = (1.4f + 2.2f * rnd(i, 4)).dp.toPx(),
            center = Offset(x, y)
        )
    }
}

/**
 * Czasteczki splywajace spiralnie do kuli - energia zbierana z calej sceny. Kazda to mala kometa:
 * jasna glowka z poswiata i zakrzywiony, zwezajacy sie ogon (kilka odcinkow po torze spirali),
 * a nie prosta kreska - proste kreski wygladaly jak deszcz.
 */
private fun DrawScope.gatherMotes(geo: AltarGeometry, gather: Float, inhale: Float) {
    val count = 40
    val tailSteps = 5
    for (i in 0 until count) {
        val start = rnd(i, 11) * 0.45f
        val p = ((gather - start) / (1f - start)).coerceIn(0f, 1f)
        if (p <= 0f || p >= 1f) continue
        val theta = rnd(i, 12) * 2f * PI.toFloat()
        val r0 = geo.base * (0.7f + 0.85f * rnd(i, 13))
        val swirl = 2.2f + 1.2f * rnd(i, 15)
        fun at(q: Float): Offset {
            val k = 1f - q
            val angle = theta + k * swirl
            val radius = r0 * k.pow(1.35f)
            return Offset(geo.orb.x + cos(angle) * radius, geo.orb.y + sin(angle) * radius * 0.72f)
        }
        val alpha = sin(p * PI.toFloat()).coerceIn(0f, 1f) * (1f - inhale)
        val width = (1.4f + 1.4f * rnd(i, 14)).dp.toPx()
        val tint = if (rnd(i, 16) < 0.35f) Color.White else MoteColor
        var prev = at(p)
        for (k in 1..tailSteps) {
            val next = at((p - k * 0.022f).coerceAtLeast(0f))
            val fade = 1f - k / (tailSteps + 1f)
            drawLine(
                color = tint.copy(alpha = 0.75f * alpha * fade),
                start = prev,
                end = next,
                strokeWidth = width * fade,
                cap = StrokeCap.Round
            )
            prev = next
        }
        val head = at(p)
        drawCircle(
            brush = Brush.radialGradient(listOf(tint.copy(alpha = 0.55f * alpha), Color.Transparent), head, width * 4f),
            radius = width * 4f,
            center = head
        )
        drawCircle(Color.White.copy(alpha = alpha), width * 0.9f, head)
    }
}

/**
 * Kula energii. Dopoki slup nie wystrzeli, jest bladoniebieska - kolor rzadkosci pokazuje sie
 * dopiero razem ze slupem, zeby nie zdradzac wyniku za wczesnie.
 */
private fun DrawScope.orb(geo: AltarGeometry, c: Color, gather: Float, inhale: Float, pulse: Float, beam: Float, t: Float) {
    val tint = androidx.compose.ui.graphics.lerp(MoteColor, c, beam.coerceIn(0f, 1f))
    val flicker = 1f + 0.08f * sin(t * 31f) + 0.05f * sin(t * 17f)
    val r = geo.base * (0.025f + 0.1f * gather) * flicker * (1f - 0.72f * inhale) * (1f + 0.35f * pulse)
    drawCircle(
        brush = Brush.radialGradient(listOf(tint.copy(alpha = 0.6f * gather), Color.Transparent), geo.orb, r * 3.4f),
        radius = r * 3.4f,
        center = geo.orb
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.White, Color.White.copy(alpha = 0.92f), tint.copy(alpha = 0f)),
            geo.orb,
            r * 1.35f
        ),
        radius = r * 1.35f,
        center = geo.orb
    )
}

/* ------------------------------------------------------------------ warstwa NAD kompanem */

/** Iskry i fala w powietrzu po wybuchu, pierscien przy przeskoku koloru, kurz, zar. */
@Composable
internal fun RitualOverFx(
    show: SummonShow,
    geo: AltarGeometry,
    color: State<Color>,
    time: State<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val t = time.value
        val c = color.value
        val burst = show.burst.value
        val pulse = show.tierPulse.value
        val land = show.land.value
        val rays = show.rays.value

        if (burst > 0f && burst < 1f) {
            val e = easeOut(burst)
            drawCircle(
                color = Color.White.copy(alpha = (1f - e) * 0.65f),
                radius = geo.base * (0.15f + 2.3f * e),
                center = geo.orb,
                style = Stroke((10.dp * (1f - e) + 1.dp).toPx())
            )
            sparks(geo, c, burst, show.best.ordinal)
        }
        if (pulse > 0.01f) {
            drawCircle(
                color = c.copy(alpha = pulse * 0.9f),
                radius = geo.base * (0.12f + 0.9f * (1f - pulse)),
                center = geo.orb,
                style = Stroke((5.dp * pulse + 1.dp).toPx())
            )
        }
        if (land > 0f && land < 1f) dust(geo, land)
        if (rays > 0.01f) embers(geo, c, rays, t)
    }
}

private fun DrawScope.sparks(geo: AltarGeometry, c: Color, burst: Float, tier: Int) {
    val count = 18 + 8 * tier
    val seconds = burst * 1.15f
    val gravity = geo.base * 2.4f
    val alpha = (1f - burst).pow(1.1f)
    for (i in 0 until count) {
        val angle = rnd(i, 21) * 2f * PI.toFloat()
        val speed = geo.base * (1.0f + 1.7f * rnd(i, 22))
        val vx = cos(angle) * speed
        val vy = sin(angle) * speed * 0.85f - geo.base * 0.7f
        val x = geo.orb.x + vx * seconds
        val y = geo.orb.y + vy * seconds + 0.5f * gravity * seconds * seconds
        val dx = vx
        val dy = vy + gravity * seconds
        val len = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val streak = (10f + 14f * rnd(i, 24)).dp.toPx()
        drawLine(
            color = (if (rnd(i, 23) < 0.5f) Color.White else c).copy(alpha = alpha),
            start = Offset(x, y),
            end = Offset(x - dx / len * streak, y - dy / len * streak),
            strokeWidth = (2f + 1.6f * rnd(i, 25)).dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.dust(geo: AltarGeometry, land: Float) {
    val e = easeOut(land)
    for (i in 0 until 14) {
        val side = if (i % 2 == 0) -1f else 1f
        val spread = geo.circleRx * (0.2f + 0.9f * e * (0.55f + 0.45f * rnd(i, 31)))
        val x = geo.feet.x + side * spread * (0.45f + 0.55f * rnd(i, 32))
        val y = geo.feet.y - (4.dp + 16.dp * e * rnd(i, 33)).toPx()
        val r = (5.dp + 11.dp * e * (0.5f + rnd(i, 34))).toPx()
        drawCircle(Color(0xFFE2D8C8).copy(alpha = (1f - land) * 0.5f), r, Offset(x, y))
    }
}

private fun DrawScope.embers(geo: AltarGeometry, c: Color, strength: Float, t: Float) {
    for (i in 0 until 20) {
        val speed = 0.1f + 0.18f * rnd(i, 41)
        val phase = (t * speed + rnd(i, 42)) % 1f
        val x = geo.feet.x + (rnd(i, 43) - 0.5f) * geo.circleRx * 2.2f + sin(t * 2f + i) * 6.dp.toPx()
        val y = geo.feet.y - phase * geo.base * 1.3f
        drawCircle(c.copy(alpha = strength * (1f - phase) * 0.85f), (1.4f + 1.8f * rnd(i, 44)).dp.toPx(), Offset(x, y))
    }
}

/* ------------------------------------------------------------------ kompan */

private val RimOffsets = listOf(
    -1f to 0f, 1f to 0f, 0f to -1f, 0f to 1f,
    -0.7f to -0.7f, 0.7f to -0.7f, -0.7f to 0.7f, 0.7f to 0.7f
)

/**
 * Kompan wychodzacy z kregu. Najpierw czarna sylweta z obwodka w kolorze rzadkosci (jak zawodnik
 * w tunelu), potem zalewa go swiatlo i nabiera koloru. Po wszystkim delikatnie "oddycha".
 *
 * [feetPivot] - gdzie w kadrze sprite'a sa stopy (skalowanie i oddech trzymaja je w miejscu).
 */
@Composable
internal fun RevealedMonster(
    card: MonsterCard,
    show: SummonShow,
    glow: State<Color>,
    time: State<Float>,
    size: Dp,
    feetPivot: Float,
    silhouette: Boolean,
    modifier: Modifier = Modifier
) {
    val preset = MonsterVisualPresets.forSpecies(card.spriteKey)
    val painter = painterResource(preset.drawable)
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                val rise = show.rise.value
                val fill = show.fill.value
                transformOrigin = TransformOrigin(0.5f, feetPivot)
                val grow = (0.55f + 0.45f * rise) * preset.visualScale
                val breathe = 1f + 0.018f * fill * sin(time.value * 2.3f)
                scaleX = grow
                scaleY = grow * breathe
                translationY = (1f - rise) * 70.dp.toPx()
                alpha = (rise * 3f).coerceIn(0f, 1f)
            }
    ) {
        if (silhouette) {
            // Obwodka: ta sama grafika zabarwiona na kolor rzadkosci, przesunieta w 8 stron.
            RimOffsets.forEach { (dx, dy) ->
                Image(
                    painter = painter,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(glow.value),
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = 2.6.dp * dx, y = 2.6.dp * dy)
                        .graphicsLayer { alpha = (1f - show.fill.value) * 0.95f }
                )
            }
            Image(
                painter = painter,
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color(0xFF0A0612)),
                modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 1f - show.fill.value }
            )
        }
        Image(
            painter = painter,
            contentDescription = card.name,
            modifier = Modifier.fillMaxSize().graphicsLayer { alpha = if (silhouette) show.fill.value else 1f }
        )
        if (silhouette) {
            // Bialy rozblysk w polowie przejscia - "zalewa go swiatlo".
            Image(
                painter = painter,
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    val f = show.fill.value
                    alpha = (4f * f * (1f - f) * 0.9f).coerceIn(0f, 1f)
                }
            )
        }
    }
}

/**
 * Pierwsza wskazowka jak flaga przy walkoucie w FIFA: emblemat zywiolu wbija sie obok sylwetki,
 * zanim gracz zobaczy, kto to jest.
 */
@Composable
internal fun ElementClue(element: Element, show: SummonShow, modifier: Modifier = Modifier) {
    val tint = Color(element.colorHex)
    Column(
        modifier = modifier.graphicsLayer {
            val p = show.clue.value
            val s = 2.3f - 1.3f * p
            scaleX = s
            scaleY = s
            alpha = (p * 2f).coerceIn(0f, 1f) * (1f - show.card.value).coerceIn(0f, 1f)
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(86.dp)) {
                drawCircle(Brush.radialGradient(listOf(tint.copy(alpha = 0.6f), Color.Transparent)))
            }
            ElementEmblem(element = element, size = 58.dp)
        }
        StrokedText(
            text = element.label.uppercase(),
            size = 13.sp,
            color = Color.White,
            outline = Color(0xFF0E0A14),
            strokeWidth = 6f
        )
    }
}

/* ------------------------------------------------------------------ nakladki na caly ekran */

/** Bialy blysk wybuchu i kolorowy przy przeskoku slupa - na calym ekranie. */
@Composable
internal fun ScreenFlash(show: SummonShow, color: State<Color>, focus: () -> Offset, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val flash = show.flash.value
        val pulse = show.tierPulse.value
        val c = color.value
        val center = focus()
        if (flash > 0.001f) {
            drawRect(
                brush = Brush.radialGradient(
                    listOf(Color.White.copy(alpha = flash), c.copy(alpha = flash * 0.85f), c.copy(alpha = flash * 0.5f)),
                    center,
                    size.maxDimension
                )
            )
        }
        if (pulse > 0.001f) {
            val r = size.minDimension * 0.9f
            drawCircle(Brush.radialGradient(listOf(c.copy(alpha = pulse * 0.4f), Color.Transparent), center, r), r, center)
        }
    }
}

/** "EPIC!" / "LEGENDARY!" wbijane na srodek, z flara za napisem. */
@Composable
internal fun RarityShout(rarity: Rarity, show: SummonShow, centerY: () -> Float, modifier: Modifier = Modifier) {
    val c = rarity.fx()
    Box(modifier = modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .offset { IntOffset(0, (centerY() - 60.dp.toPx()).roundToInt()) }
            .graphicsLayer {
                val s = show.shout.value
                alpha = if (s <= 0f || s >= 1f) 0f else shoutAlpha(s)
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .graphicsLayer { scaleX = easeOut(show.shout.value / 0.22f) }
        ) {
            drawRect(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, c.copy(alpha = 0.55f), Color.White.copy(alpha = 0.85f), c.copy(alpha = 0.55f), Color.Transparent)
                ),
                topLeft = Offset(0f, size.height * 0.42f),
                size = Size(size.width, size.height * 0.16f)
            )
            drawRect(
                Brush.radialGradient(listOf(c.copy(alpha = 0.45f), Color.Transparent), center, size.width * 0.45f)
            )
        }
        StrokedText(
            text = rarity.shoutText(),
            size = 46.sp,
            color = c,
            outline = Color(0xFF12081C),
            strokeWidth = 12f,
            modifier = Modifier.graphicsLayer {
                val s = show.shout.value
                val inP = (s / 0.22f).coerceIn(0f, 1f)
                val outP = ((s - 0.78f) / 0.22f).coerceIn(0f, 1f)
                val scale = (2.8f - 1.8f * EaseOutBack.transform(inP)) * (1f + 0.12f * outP)
                scaleX = scale
                scaleY = scale
                translationY = -30.dp.toPx() * outP
            }
        )
    }
    }
}

private fun shoutAlpha(s: Float): Float {
    val inP = (s / 0.22f).coerceIn(0f, 1f)
    val outP = ((s - 0.78f) / 0.22f).coerceIn(0f, 1f)
    return inP * (1f - outP)
}

private val ConfettiColors = listOf(
    Color(0xFFFFD54A), Color.White, Color(0xFFFF9F1C), Color(0xFFFFF3B0), Color(0xFFE8B84A), Color(0xFFFFC23A)
)

/** Zloty deszcz konfetti przy legendzie - spada przez caly ekran i wiruje. */
@Composable
internal fun ConfettiRain(show: SummonShow, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val v = show.confetti.value
        if (v <= 0f || v >= 1f) return@Canvas
        val seconds = v * 4.2f
        val fade = if (v > 0.85f) (1f - v) / 0.15f else 1f
        val w = 5.dp.toPx()
        val h = 9.dp.toPx()
        for (i in 0 until 90) {
            val delayS = rnd(i, 51) * 1.3f
            val tt = seconds - delayS
            if (tt <= 0f) continue
            val fall = size.height * (0.26f + 0.2f * rnd(i, 52))
            val y = -20.dp.toPx() + fall * tt
            if (y > size.height + h) continue
            val x = rnd(i, 53) * size.width + sin(tt * 3f + i) * 18.dp.toPx()
            val spin = tt * (160f + 220f * rnd(i, 54))
            val color = ConfettiColors[i % ConfettiColors.size].copy(alpha = fade)
            val center = Offset(x, y)
            rotate(degrees = spin, pivot = center) {
                // "Wirowanie" w 3D - kartka raz jest szeroka, raz widac tylko jej krawedz.
                scale(scaleX = cos(tt * 7f + i).coerceIn(-1f, 1f), scaleY = 1f, pivot = center) {
                    drawRect(color, topLeft = Offset(x - w / 2f, y - h / 2f), size = Size(w, h))
                }
            }
        }
    }
}
