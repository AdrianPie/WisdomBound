package com.lernhero.game.battle.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.ultimate.UltimateSpec
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.game.battle.UltimateCast
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import com.lernhero.shared.theme.StrokedText

/** Kolor kuli mocy - domena trzyma go jako liczbe ARGB (patrz [UltimateSpec.colorHex]). */
val UltimateSpec.color: Color get() = Color(colorHex)

internal fun Color.mix(other: Color, t: Float) = Color(
    red = red + (other.red - red) * t,
    green = green + (other.green - green) * t,
    blue = blue + (other.blue - blue) * t,
    alpha = alpha
)

/**
 * Kula mocy rysowana w kodzie - poswiata, szklana kula z wirem swiatla w srodku i odblaskiem.
 *
 * To jest grafika ZASTEPCZA: gdy dojda PNG kul (item_orb_*.png), plecak i karta przedmiotu wezma
 * obrazek, a ta kula zostanie w miejscach, gdzie liczy sie kolor, nie detal (licznik ulti w walce).
 * [size] obejmuje tez poswiate - sama kula ma okolo 70% tej szerokosci.
 */
@Composable
fun PowerOrb(color: Color, size: Dp, modifier: Modifier = Modifier, glow: Float = 1f) {
    Canvas(modifier = modifier.size(size)) {
        drawPowerOrb(center = center, radius = this.size.minDimension * 0.36f, color = color, glow = glow)
    }
}

/**
 * Sama kula na dowolnym Canvasie - [radius] to promien SZKLA; poswiata siega okolo 1.4 raza dalej.
 * Wspolna dla ikony ([PowerOrb]), licznika i sceny wystrzalu, zeby kula wszedzie wygladala tak samo.
 */
fun DrawScope.drawPowerOrb(center: Offset, radius: Float, color: Color, glow: Float = 1f) {
    val c = center
    val r = radius
    val glowRadius = r / 0.72f
    // Poswiata wokol kuli.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.55f * glow), color.copy(alpha = 0.18f * glow), Color.Transparent),
            center = c,
            radius = glowRadius
        ),
        radius = glowRadius,
        center = c
    )
    // Szklana kula: jasno u gory z lewej, ciemno przy krawedzi.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.mix(Color.White, 0.65f), color, color.mix(Color.Black, 0.6f)),
            center = Offset(c.x - r * 0.3f, c.y - r * 0.35f),
            radius = r * 1.35f
        ),
        radius = r,
        center = c
    )
    // Wir swiatla w srodku - dwa luki, zeby kula nie wygladala jak plaska kropka.
    val swirl = Size(r * 1.3f, r * 1.3f)
    val swirlTopLeft = Offset(c.x - swirl.width / 2f, c.y - swirl.height / 2f)
    drawArc(
        color = color.mix(Color.White, 0.8f).copy(alpha = 0.55f),
        startAngle = 200f, sweepAngle = 120f, useCenter = false,
        topLeft = swirlTopLeft, size = swirl, style = Stroke(width = r * 0.12f)
    )
    drawArc(
        color = color.mix(Color.White, 0.5f).copy(alpha = 0.45f),
        startAngle = 20f, sweepAngle = 110f, useCenter = false,
        topLeft = Offset(swirlTopLeft.x + r * 0.15f, swirlTopLeft.y + r * 0.15f),
        size = Size(swirl.width - r * 0.3f, swirl.height - r * 0.3f), style = Stroke(width = r * 0.09f)
    )
    // Rdzen.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.85f), color.copy(alpha = 0f)),
            center = c,
            radius = r * 0.45f
        ),
        radius = r * 0.45f,
        center = c
    )
    // Odblask szkla.
    drawOval(
        color = Color.White.copy(alpha = 0.75f),
        topLeft = Offset(c.x - r * 0.62f, c.y - r * 0.72f),
        size = Size(r * 0.55f, r * 0.32f)
    )
    drawCircle(color = color.mix(Color.Black, 0.7f).copy(alpha = 0.8f), radius = r, center = c, style = Stroke(width = r * 0.07f))
}

/**
 * Licznik ladowania ulti nad quizem: kula, [UltimateSpec.streak] rombow i podpis "ile jeszcze".
 *
 * Wczesniej jedyna informacja o ulti byla w dziesieciu kratkach cyklu, ktore licza cos INNEGO
 * (buff/debuff co dziesiec odpowiedzi) - nie dalo sie z nich odczytac, ile brakuje do ulti ani kiedy
 * ono padlo. Tu kazda dobra odpowiedz zapala romb w kolorze kuli, zla gasi wszystkie, a ostatni
 * brakujacy krok pulsuje ("1 MORE!").
 */
@Composable
fun UltimateMeter(
    spec: UltimateSpec,
    charge: Int,
    /** Id ostatniego wystrzalu - zmiana odpala krotki rozblysk licznika. */
    castId: Long,
    modifier: Modifier = Modifier
) {
    val color = spec.color
    val left = (spec.streak - charge).coerceAtLeast(0)
    val almost = left == 1
    val pulse by rememberInfiniteTransition(label = "ultiMeter").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ultiMeterPulse"
    )
    val flash = remember { Animatable(0f) }
    LaunchedEffect(castId) {
        if (castId == 0L) return@LaunchedEffect
        flash.snapTo(1f)
        flash.animateTo(0f, tween(BattleSpeed.scale(700), easing = LinearEasing))
    }

    Row(
        modifier = modifier
            .graphicsLayer {
                val s = 1f + flash.value * 0.18f
                scaleX = s
                scaleY = s
            }
            .clip(RoundedCornerShape(50))
            .background(Color(0xE0140D08).mix(color, 0.35f * flash.value))
            .border(1.dp, (if (almost) color else PanelBorder).copy(alpha = if (almost) pulse else 0.7f), RoundedCornerShape(50))
            .padding(start = 2.dp, end = 7.dp, top = 1.dp, bottom = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PowerOrb(color = color, size = 18.dp, glow = if (almost) pulse else 0.55f + 0.45f * flash.value)
        repeat(spec.streak) { index ->
            val lit = index < charge
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .rotate(45f)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(
                        if (lit) Brush.linearGradient(listOf(color.mix(Color.White, 0.6f), color))
                        else Brush.linearGradient(listOf(Color(0xFF0A0604), Color(0xFF2A1D12)))
                    )
                    .border(0.8.dp, if (lit) color.mix(Color.White, 0.4f) else PanelBorder.copy(alpha = 0.6f), RoundedCornerShape(1.5.dp))
            )
        }
        Text(
            text = when (left) {
                0 -> "ULTI!"
                1 -> "1 MORE!"
                else -> "$left to ULTI"
            },
            color = if (almost) color.mix(Color.White, 0.35f).copy(alpha = 0.6f + 0.4f * pulse) else Color.White.copy(alpha = 0.8f),
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 8.sp,
            maxLines = 1,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}
