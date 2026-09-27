package com.lernhero.game.battle.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.lerp
import com.lernhero.game.battle.fx.VfxEngine
import kotlin.math.sin

/**
 * Pasek czasu na odpowiedz jako PLONACY LONT - jak lina w Hearthstone: lont pali sie od prawej,
 * na jego koncu tanczy plomien, sypia sie iskry i unosi dym, a za plomieniem zostaje zweglony
 * slad z zarzacymi sie wegielkami.
 *
 * - Strefa MAX (poczatek okna, [graceFraction] od prawej) - lont jest zaczarowany: zloty, z iskierkami.
 * - Ostatnie sekundy ([urgent]) - plomien rosnie, czerwienieje i pulsuje.
 * - Po odpowiedzi ([burning] = false) lont gasnie i zamiera dokladnie tam, gdzie padla odpowiedz.
 *
 * Plomien jest rysowany na OSOBNYM, nieprzycietym Canvasie - wystaje nad pasek.
 */
@Composable
internal fun BurningFuse(
    remaining: Float,
    graceFraction: Float,
    burning: Boolean,
    urgent: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = VfxEngine.monoNow() / 1_000_000_000f
            drawFuse(remaining.coerceIn(0f, 1f), graceFraction, burning, t)
        }
        if (burning && remaining > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val t = VfxEngine.monoNow() / 1_000_000_000f
                drawFlame(size.width * remaining.coerceIn(0f, 1f), urgent, t)
            }
        }
    }
}

private val RopeLight = Color(0xFFE8C48A)
private val RopeDark = Color(0xFF9A6A34)
private val RopeGoldLight = Color(0xFFFFE9A0)
private val RopeGoldDark = Color(0xFFD99A1C)

private fun DrawScope.drawFuse(remaining: Float, grace: Float, burning: Boolean, t: Float) {
    val w = size.width
    val h = size.height
    val u = density
    val head = w * remaining
    val graceStart = w * (1f - grace)
    val inGrace = remaining > 1f - grace

    // Rowek pod lontem.
    drawRoundRect(Color(0xFF140E09), cornerRadius = CornerRadius(h / 2f))
    drawRoundRect(Color(0xFF000000), cornerRadius = CornerRadius(h / 2f), style = androidx.compose.ui.graphics.drawscope.Stroke(1f * u))

    // Zweglony slad za plomieniem - wegielki tla sie najmocniej tuz przy nim.
    if (head < w) {
        drawRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF3A2C22), Color(0xFF15100B)), startY = h * 0.25f, endY = h * 0.75f),
            topLeft = Offset(head, h * 0.25f),
            size = Size(w - head, h * 0.5f)
        )
        for (i in 0 until 9) {
            val x = head + (i + 0.5f) * 5.5f * u
            if (x > w) break
            val near = 1f - (x - head) / (50f * u)
            if (near <= 0f) break
            val flicker = (sin(t * 9f + i * 1.9f) + 1f) / 2f
            drawCircle(Color(0xFFFF7A2A).copy(alpha = (0.25f + 0.6f * flicker) * near), 1.3f * u, Offset(x, h * (0.4f + 0.2f * ((i % 3) / 2f))))
        }
    }

    if (head <= 0.5f) return
    // Sam lont: skrecona lina (ukosne prazki), w strefie MAX zlota i zaczarowana.
    clipRect(0f, 0f, head, h) {
        val light = if (inGrace) RopeGoldLight else RopeLight
        val dark = if (inGrace) RopeGoldDark else RopeDark
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(light, dark, lerp(dark, Color.Black, 0.3f)), startY = h * 0.12f, endY = h * 0.88f),
            topLeft = Offset(0f, h * 0.12f),
            size = Size(head + h, h * 0.76f),
            cornerRadius = CornerRadius(h / 2f)
        )
        var x = -h
        val step = 6f * u
        while (x < head + h) {
            drawLine(
                color = lerp(dark, Color.Black, 0.45f).copy(alpha = 0.55f),
                start = Offset(x, h * 0.86f),
                end = Offset(x + h * 0.7f, h * 0.14f),
                strokeWidth = 1.8f * u
            )
            x += step
        }
        drawRect(Color.White.copy(alpha = 0.28f), Offset(0f, h * 0.2f), Size(head, h * 0.14f))

        if (inGrace && burning) {
            // Iskierki przebiegajace po zaczarowanym lontcie.
            for (i in 0 until 4) {
                val phase = (t * 0.55f + i / 4f) % 1f
                val sx = phase * head
                val a = sin(phase * kotlin.math.PI.toFloat())
                drawCircle(Color.White.copy(alpha = 0.8f * a), 1.8f * u, Offset(sx, h * (0.35f + 0.3f * (i % 2))))
            }
        }
    }

    // Znacznik granicy strefy MAX - zlote naciecie na rowku.
    if (grace > 0f) {
        drawLine(Color(0xFFFFD34D).copy(alpha = 0.8f), Offset(graceStart, 0f), Offset(graceStart, h * 0.22f), 2f * u)
        drawLine(Color(0xFFFFD34D).copy(alpha = 0.8f), Offset(graceStart, h * 0.78f), Offset(graceStart, h), 2f * u)
    }
}

/** Tanczacy plomien na koncu lontu, z poswiata, iskrami i dymem. */
private fun DrawScope.drawFlame(headX: Float, urgent: Boolean, t: Float) {
    val h = size.height
    val u = density
    val cy = h * 0.55f
    val pulse = if (urgent) 1f + 0.15f * sin(t * 18f) else 1f
    val base = h * (if (urgent) 1.55f else 1.1f) * pulse

    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFFF8A2A).copy(alpha = if (urgent) 0.65f else 0.45f), Color.Transparent),
            Offset(headX, cy),
            h * 2f
        ),
        radius = h * 2f,
        center = Offset(headX, cy)
    )

    // Dym - szare klebki odplywajace w gore i w prawo, nad zweglony slad.
    for (i in 0 until 3) {
        val phase = (t / 1.3f + i / 3f) % 1f
        val pos = Offset(headX + phase * 12f * u, cy - h * 0.6f - phase * 22f * u)
        drawCircle(Color(0xFF6A5E58).copy(alpha = 0.32f * (1f - phase)), (3f + 6f * phase) * u, pos)
    }

    val outer = if (urgent) Color(0xFFD8261A) else Color(0xFFE8521C)
    val layers = listOf(
        Triple(outer, 1f, 0f),
        Triple(Color(0xFFFF9A2A), 0.72f, 1.3f),
        Triple(Color(0xFFFFF0A8), 0.42f, 2.6f)
    )
    layers.forEach { (color, scale, phase) ->
        val flick = 1f + 0.18f * sin(t * 23f + phase) + 0.1f * sin(t * 37f + phase * 2f)
        val fh = base * scale * flick
        val fw = h * 0.5f * scale * (1f + 0.12f * sin(t * 29f + phase))
        val sway = sin(t * 11f + phase) * fw * 0.35f
        val tip = Offset(headX + sway, cy - fh * 1.35f)
        val bottom = cy + fw * 0.45f
        val flame = Path().apply {
            moveTo(tip.x, tip.y)
            cubicTo(headX + fw * 0.85f, cy - fh * 0.55f, headX + fw, cy, headX, bottom)
            cubicTo(headX - fw, cy, headX - fw * 0.85f, cy - fh * 0.55f, tip.x, tip.y)
            close()
        }
        drawPath(flame, color)
    }

    // Iskry pryskajace z plomienia.
    for (i in 0 until 8) {
        val period = 0.55f + 0.2f * ((i * 37) % 10) / 10f
        val phase = (t / period + i * 0.13f) % 1f
        val vx = (((i * 53) % 21) - 6) / 10f * 26f * u
        val vy = (34f + ((i * 29) % 17)) * u
        val pos = Offset(headX + vx * phase, cy - vy * phase + 60f * u * phase * phase)
        drawCircle(
            color = (if (i % 2 == 0) Color(0xFFFFE08A) else Color(0xFFFF9A2A)).copy(alpha = 1f - phase),
            radius = 1.3f * u * (1f - 0.5f * phase),
            center = pos
        )
    }
}
