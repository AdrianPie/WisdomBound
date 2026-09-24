package com.lernhero.game.battle.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.shared.theme.FontFirst
import kotlin.math.roundToInt

/**
 * Tabliczka walczacego nad glowa, jednym wierszem: `7 🔥[pasek HP] ⚔42`.
 *
 * - **Poziom** - sama liczba z obrysem, bez ramki (tak czytelnie jak w grach, na ktorych sie
 *   wzorujemy).
 * - **Zywiol** - emblemat ([elementBadge]); u naszych pulsuje, gdy kafel tematu go wzmocnil.
 * - **Pasek HP** z liczba w srodku i "cieniem obrazen" (jasny kawalek znika chwile po ciosie).
 * - **Atak NA TE TURE** z prawej. Po odpowiedzi mnoznik salwy wchodzi od razu: liczba dolicza sie,
 *   "pompuje" i zostaje zabarwiona (zloto = mocniej, czerwien = slabiej) do nastepnego pytania.
 *
 * Tabliczka stoi CALA nad glowa - jej dolna krawedz jest na wysokosci czubka glowy (patrz
 * BattleField.HealthBarsOverlay), wiec nie przykrywa postaci.
 */
@Composable
internal fun UnitPlate(
    character: CharacterUiModel,
    attackMultiplier: Float,
    barWidth: Dp,
    modifier: Modifier = Modifier,
    elementBadge: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "${character.level}",
            color = if (character.isPlayer) Color.White else Color(0xFFFFB4AC),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            style = Outline
        )
        elementBadge?.invoke()
        HpBar(hp = character.hp, maxHp = character.maxHp, width = barWidth)
        AttackStat(base = character.attack, multiplier = attackMultiplier)
    }
}

@Composable
private fun HpBar(hp: Int, maxHp: Int, width: Dp) {
    val fraction = if (maxHp > 0) (hp.toFloat() / maxHp).coerceIn(0f, 1f) else 0f
    val shown by animateFloatAsState(fraction, tween(180), label = "hp")
    // Cien obrazen: dogania pasek z opoznieniem, wiec przez chwile widac zabrany kawalek.
    val ghost by animateFloatAsState(fraction, tween(520, delayMillis = 260, easing = FastOutSlowInEasing), label = "hpGhost")
    val fill = when {
        fraction > 0.5f -> listOf(Color(0xFF7CF25A), Color(0xFF2FB344))
        fraction > 0.25f -> listOf(Color(0xFFFFE066), Color(0xFFE0A21C))
        else -> listOf(Color(0xFFFF7A6B), Color(0xFFD42A1E))
    }
    Box(
        modifier = Modifier
            .size(width = width, height = 8.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xE6120C08))
            .border(0.75.dp, Color.Black.copy(alpha = 0.7f), RoundedCornerShape(50))
            .padding(1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(ghost.coerceAtLeast(shown))
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFFFF1D6).copy(alpha = 0.85f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(shown)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(Brush.verticalGradient(fill))
        )
        // Blik na gorze paska.
        Box(
            modifier = Modifier
                .fillMaxWidth(shown)
                .height(1.5.dp)
                .padding(horizontal = 2.dp)
                .background(Color.White.copy(alpha = 0.4f))
        )
        Text(
            text = "${hp.coerceAtLeast(0)}",
            color = Color.White,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 6.sp,
            lineHeight = 6.sp,
            style = Outline,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * Atak z animacja zmiany: gdy mnoznik rosnie, liczba dolicza sie w gore, "pompuje" (szybkie
 * powiekszenie i powrot ze sprezynowaniem) i zlocieje; gdy maleje - to samo w dol, na czerwono.
 */
@Composable
private fun AttackStat(base: Int, multiplier: Float, modifier: Modifier = Modifier) {
    val target = (base * multiplier).roundToInt()
    val value by animateFloatAsState(target.toFloat(), tween(650, easing = FastOutSlowInEasing), label = "atk")
    val up = multiplier > 1.001f
    val down = multiplier < 0.999f
    val color by animateColorAsState(
        when {
            up -> Color(0xFFFFD34D)
            down -> Color(0xFFFF6B5E)
            else -> Color.White.copy(alpha = 0.9f)
        },
        label = "atkColor"
    )

    val pulse = remember { Animatable(1f) }
    LaunchedEffect(target) {
        if (target == base && !up && !down) return@LaunchedEffect
        pulse.snapTo(1f)
        pulse.animateTo(1.55f, tween(140, easing = FastOutSlowInEasing))
        pulse.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 420f))
    }

    Row(
        modifier = modifier.graphicsLayer {
            scaleX = pulse.value
            scaleY = pulse.value
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(text = "⚔", color = color, fontSize = 7.sp, style = Outline)
        Text(
            text = "${value.roundToInt()}",
            color = color,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            style = Outline
        )
        if (up || down) {
            Text(
                text = if (up) "▲" else "▼",
                color = color,
                fontSize = 6.sp,
                style = Outline
            )
        }
    }
}

private val Outline = TextStyle(
    shadow = Shadow(color = Color(0xFF0A0604), blurRadius = 2.5f),
    drawStyle = null
)

