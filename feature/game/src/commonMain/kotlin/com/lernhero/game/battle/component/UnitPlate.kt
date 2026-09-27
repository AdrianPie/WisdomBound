package com.lernhero.game.battle.component

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.game.character.ElementEmblem
import com.lernhero.shared.theme.StrokedText

/**
 * Tabliczka nad glowa - tylko to, co trzeba widziec W TRAKCIE walki: poziom, pasek HP i zywiol.
 *
 * Wczesniej byl tu rzad czterech pigulek (poziom, HP, atak, pancerz) z pieciopunktowym pismem -
 * przy szesciu potworach po stronie zlewaly sie w jeden szum, nad ktorym nie bylo widac ani
 * pasekow, ani postaci. Liczby sa teraz pod tapnieciem w postac ([InspectCard]).
 *
 * Mnoznik ataku tury (dobra/zla odpowiedz) zostaje jako mala strzalka przy pasku: w gore na zloto,
 * w dol na czerwono - kolor mowi o KORZYSCI gracza, tak jak wczesniej.
 */
@Composable
internal fun UnitPlate(
    character: CharacterUiModel,
    attackMultiplier: Float,
    boosted: Boolean,
    modifier: Modifier = Modifier
) {
    val giant = character.battleScale > 1.9f
    val barWidth = if (giant) 78.dp else if (character.knightEquipment != null) 50.dp else 42.dp
    val barHeight = if (giant) 7.dp else 5.dp
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        LevelBadge(level = character.level, isPlayer = character.isPlayer, giant = giant)
        HpBar(
            hp = character.hp,
            maxHp = character.maxHp,
            isPlayer = character.isPlayer,
            modifier = Modifier.width(barWidth).height(barHeight)
        )
        character.element?.let { element -> ElementDot(element = element, boosted = boosted) }
        AttackArrow(multiplier = attackMultiplier, isPlayer = character.isPlayer)
    }
}

@Composable
private fun LevelBadge(level: Int, isPlayer: Boolean, giant: Boolean) {
    val rim = if (isPlayer) Color(0xFFFFD66B) else Color(0xFFFF8A7A)
    Box(
        modifier = Modifier
            .size(if (giant) 18.dp else 14.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFF3A4458), Color(0xFF12161F))))
            .border(1.2.dp, rim, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        StrokedText(
            text = "$level",
            size = if (level >= 100) 5.5.sp else if (giant) 8.sp else 7.sp,
            color = Color.White,
            outline = Color(0xFF0A0A10),
            strokeWidth = 3f,
            tight = true
        )
    }
}

/** Zywiol przy pasku; u naszych pulsuje, gdy kafel tematu go wzmocnil. */
@Composable
private fun ElementDot(element: com.lernhero.domain.monster.Element, boosted: Boolean) {
    val color = Color(element.colorHex)
    val pulse = rememberInfiniteTransition(label = "elementPulse")
    val glow by pulse.animateFloat(
        initialValue = if (boosted) 0.35f else 0f,
        targetValue = if (boosted) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(760, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "elementGlow"
    )
    Box(modifier = Modifier.size(12.dp), contentAlignment = Alignment.Center) {
        if (boosted) {
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .graphicsLayer {
                        alpha = glow
                        scaleX = 1f + 0.25f * glow
                        scaleY = 1f + 0.25f * glow
                    }
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.6f))
            )
        }
        ElementEmblem(element = element, size = 12.dp)
    }
}

@Composable
private fun AttackArrow(multiplier: Float, isPlayer: Boolean) {
    val up = multiplier > 1.001f
    val down = multiplier < 0.999f
    if (!up && !down) return
    val good = if (isPlayer) up else down
    StrokedText(
        text = if (up) "▲" else "▼",
        size = 8.sp,
        color = if (good) Color(0xFFFFE066) else Color(0xFFFF6B5E),
        outline = Color(0xFF1A0E04),
        strokeWidth = 3f,
        tight = true
    )
}

/** Pasek HP z ciemna oprawa i "cieniem obrazen" - jasny kawalek znika chwile po ciosie. */
@Composable
private fun HpBar(hp: Int, maxHp: Int, isPlayer: Boolean, modifier: Modifier = Modifier) {
    val fraction = if (maxHp > 0) (hp.toFloat() / maxHp).coerceIn(0f, 1f) else 0f
    val shown by animateFloatAsState(fraction, tween(160), label = "hp")
    val ghost by animateFloatAsState(fraction, tween(520, delayMillis = 280, easing = FastOutSlowInEasing), label = "hpGhost")
    // Nasi sa zieloni, wrogowie czerwoni - od razu widac, czyj pasek sie kurczy.
    val fill = when {
        !isPlayer -> listOf(Color(0xFFFF7A6B), Color(0xFFC8261C))
        fraction > 0.5f -> listOf(Color(0xFF8CF76A), Color(0xFF2FB344))
        fraction > 0.25f -> listOf(Color(0xFFFFE066), Color(0xFFE0A21C))
        else -> listOf(Color(0xFFFF7A6B), Color(0xFFD42A1E))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xF0100A08))
            .border(0.8.dp, Color(0xFF000000), RoundedCornerShape(50))
            .padding(0.8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(ghost.coerceAtLeast(shown))
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFFFF1D6).copy(alpha = 0.9f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(shown)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(Brush.verticalGradient(fill))
        )
        // Gorny blik - pasek wyglada na szklany, a nie plaski.
        Box(
            modifier = Modifier
                .fillMaxWidth(shown)
                .fillMaxHeight(0.4f)
                .padding(horizontal = 1.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.35f))
        )
    }
}
