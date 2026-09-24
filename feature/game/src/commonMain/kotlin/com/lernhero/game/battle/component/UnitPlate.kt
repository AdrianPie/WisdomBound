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
 * Tabliczka walczacego nad glowa: `7 🔥[pasek HP]` - atak i pancerz stoja przy stopach ([FeetStats]).
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
        HpBar(hp = character.hp, maxHp = character.maxHp, width = barWidth, armor = 0f)
    }
}

/**
 * Znaczki przy stopach, jak w Hearthstone: z lewej ATAK (zlota gema z liczba), z prawej PANCERZ
 * (stalowa tarcza z procentem). Nad glowa zostaje tylko poziom, zywiol i HP - dwie najwazniejsze
 * liczby walki stoja tam, gdzie oko i tak patrzy na postac, a nie w tlumie nad glowami.
 */
@Composable
internal fun FeetStats(
    character: CharacterUiModel,
    attackMultiplier: Float,
    spread: Dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spread)
    ) {
        AttackGem(base = character.attack, multiplier = attackMultiplier, isPlayer = character.isPlayer)
        ArmorShield(armor = character.armor)
    }
}

@Composable
private fun AttackGem(base: Int, multiplier: Float, isPlayer: Boolean) {
    val target = (base * multiplier).roundToInt()
    val value by animateFloatAsState(target.toFloat(), tween(650, easing = FastOutSlowInEasing), label = "atk")
    val up = multiplier > 1.001f
    val down = multiplier < 0.999f
    val goodForPlayer = if (isPlayer) up else down
    // Gema zmienia kolor razem z mnoznikiem tury: zloto normalnie, jasniejsze zloto na plus dla
    // gracza, czerwien na minus - i "pompuje" przy kazdej zmianie.
    val top by animateColorAsState(
        when {
            !up && !down -> Color(0xFFFFD66B)
            goodForPlayer -> Color(0xFFFFF09A)
            else -> Color(0xFFFF8A7A)
        }, label = "gemTop"
    )
    val bottom by animateColorAsState(
        when {
            !up && !down -> Color(0xFFE08A12)
            goodForPlayer -> Color(0xFFFFB300)
            else -> Color(0xFFC0281C)
        }, label = "gemBottom"
    )
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(target) {
        if (!up && !down && target == base) return@LaunchedEffect
        pulse.snapTo(1f)
        pulse.animateTo(1.5f, tween(140, easing = FastOutSlowInEasing))
        pulse.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 420f))
    }
    Box(
        modifier = Modifier
            .size(STAT_BADGE)
            .graphicsLayer {
                scaleX = pulse.value
                scaleY = pulse.value
            }
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(top, bottom)))
            .border(1.5.dp, Color(0xFF2A1606), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${value.roundToInt()}",
            color = Color.White,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = if (value >= 1000f) 7.sp else 9.sp,
            style = HeavyOutline
        )
    }
}

@Composable
private fun ArmorShield(armor: Float) {
    val percent = (armor * 100).roundToInt()
    Box(
        modifier = Modifier
            .size(width = STAT_BADGE, height = STAT_BADGE + 2.dp)
            .clip(ShieldShape)
            .background(
                Brush.verticalGradient(
                    if (percent > 0) listOf(Color(0xFFE3ECF5), Color(0xFF7D8FA3))
                    else listOf(Color(0xFF8A8F96), Color(0xFF4A4F57))
                )
            )
            .border(1.5.dp, Color(0xFF151A20), ShieldShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$percent",
            color = Color.White,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            style = HeavyOutline,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

/** Ksztalt tarczy: plaska gora, boki schodza w szpic na dole. */
private val ShieldShape = androidx.compose.foundation.shape.GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(0f, h * 0.08f)
    quadraticTo(w * 0.5f, -h * 0.06f, w, h * 0.08f)
    lineTo(w, h * 0.5f)
    quadraticTo(w * 0.95f, h * 0.85f, w * 0.5f, h)
    quadraticTo(w * 0.05f, h * 0.85f, 0f, h * 0.5f)
    close()
}

private val HeavyOutline = TextStyle(shadow = Shadow(color = Color(0xFF1A0E04), blurRadius = 4f))

/** Srednica znaczka ataku/pancerza przy stopach. */
internal val STAT_BADGE = 18.dp

@Composable
private fun HpBar(hp: Int, maxHp: Int, width: Dp, armor: Float) {
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
            // Pancerz widac na samym pasku: stalowa, jasna ramka zamiast czarnej. Im wiecej, tym grubsza.
            .border(
                width = if (armor > 0f) (1f + armor * 2.5f).dp else 0.75.dp,
                color = if (armor > 0f) Color(0xFFB9C7D6) else Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(50)
            )
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

private val Outline = TextStyle(
    shadow = Shadow(color = Color(0xFF0A0604), blurRadius = 2.5f),
    drawStyle = null
)

