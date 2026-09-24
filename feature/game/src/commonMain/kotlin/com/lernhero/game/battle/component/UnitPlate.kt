package com.lernhero.game.battle.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.GenericShape
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.shared.theme.FontFirst
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Tabliczka walczacego nad glowa, w jednym rzedzie: `[poziom] [zywiol] [pasek HP] (ATK) (DEF)`.
 *
 * - **Poziom** - szesciokatna odznaka z lewej.
 * - **Zywiol** - emblemat ([elementBadge]); u naszych pulsuje, gdy kafel tematu go wzmocnil.
 * - **Pasek HP** z liczba w srodku i "cieniem obrazen" (jasny kawalek znika chwile po ciosie).
 * - **ATK NA TE TURE** - po odpowiedzi mnoznik salwy wchodzi od razu: liczba dolicza sie, kolko
 *   "pompuje" i zmienia kolor (jasne zloto = dobrze dla gracza, czerwien = zle) do nastepnego pytania.
 * - **DEF** - pancerz w procentach, czyli ile kazdego ciosu ta postac odcina
 *   ([com.lernhero.domain.combat.Armor]).
 *
 * Tla odznak mozna podmienic na grafiki - patrz [PlateArt].
 *
 * Tabliczka stoi CALA nad glowa - jej dolna krawedz jest na wysokosci czubka glowy (patrz
 * BattleField.HealthBarsOverlay), wiec nie przykrywa postaci.
 */
@Composable
internal fun UnitPlate(
    character: CharacterUiModel,
    barWidth: Dp,
    attackMultiplier: Float,
    modifier: Modifier = Modifier,
    elementBadge: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        LevelBadge(level = character.level, isPlayer = character.isPlayer)
        elementBadge?.invoke()
        HpBar(hp = character.hp, maxHp = character.maxHp, width = barWidth)
        AttackBadge(base = character.attack, multiplier = attackMultiplier, isPlayer = character.isPlayer)
        DefenseBadge(armor = character.armor)
    }
}

/**
 * Grafiki pod odznakami tabliczki. `null` = odznaka rysowana w kodzie (kolo/szesciokat z gradientem).
 * Po wgraniu PNG wystarczy tu podpiac zasob - liczba zostaje rysowana na wierzchu.
 */
internal object PlateArt {
    val level: DrawableResource? = null
    val attack: DrawableResource? = null
    val defense: DrawableResource? = null
}

@Composable
private fun LevelBadge(level: Int, isPlayer: Boolean) {
    val rim = if (isPlayer) Color(0xFFFFD66B) else Color(0xFFFF8A7A)
    BadgeFrame(
        art = PlateArt.level,
        shape = HexShape,
        fill = Brush.verticalGradient(listOf(Color(0xFF3A4458), Color(0xFF151A24))),
        rim = rim
    ) {
        BadgeNumber(text = "$level", color = Color.White)
    }
}

@Composable
private fun AttackBadge(base: Int, multiplier: Float, isPlayer: Boolean) {
    val target = (base * multiplier).roundToInt()
    val value by animateFloatAsState(target.toFloat(), tween(650, easing = FastOutSlowInEasing), label = "atk")
    val up = multiplier > 1.001f
    val down = multiplier < 0.999f
    val goodForPlayer = if (isPlayer) up else down
    // Kolor = korzysc dla GRACZA: nasz atak w gore albo wroga w dol to jasne zloto, odwrotnie czerwien.
    val top by animateColorAsState(
        when {
            !up && !down -> Color(0xFFFFB25C)
            goodForPlayer -> Color(0xFFFFF09A)
            else -> Color(0xFFFF6B5E)
        }, label = "atkTop"
    )
    val bottom by animateColorAsState(
        when {
            !up && !down -> Color(0xFFC2410C)
            goodForPlayer -> Color(0xFFE39B00)
            else -> Color(0xFF8E1410)
        }, label = "atkBottom"
    )
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(target) {
        if (!up && !down && target == base) return@LaunchedEffect
        pulse.snapTo(1f)
        pulse.animateTo(1.55f, tween(140, easing = FastOutSlowInEasing))
        pulse.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 420f))
    }
    BadgeFrame(
        art = PlateArt.attack,
        shape = CircleShape,
        fill = Brush.radialGradient(listOf(top, bottom)),
        rim = Color(0xFF2A1606),
        modifier = Modifier.graphicsLayer {
            scaleX = pulse.value
            scaleY = pulse.value
        }
    ) {
        BadgeNumber(text = "${value.roundToInt()}", color = Color.White)
    }
}

@Composable
private fun DefenseBadge(armor: Float) {
    val percent = (armor * 100).roundToInt()
    BadgeFrame(
        art = PlateArt.defense,
        shape = CircleShape,
        fill = Brush.radialGradient(
            if (percent > 0) listOf(Color(0xFFD6E6F5), Color(0xFF4D6A8A))
            else listOf(Color(0xFF8A8F96), Color(0xFF3F444C))
        ),
        rim = Color(0xFF121821)
    ) {
        BadgeNumber(text = "$percent", color = Color.White)
    }
}

/** Wspolna rama odznaki: grafika z [PlateArt], a bez niej ksztalt z gradientem i obwodka. */
@Composable
private fun BadgeFrame(
    art: DrawableResource?,
    shape: Shape,
    fill: Brush,
    rim: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.size(STAT_BADGE),
        contentAlignment = Alignment.Center
    ) {
        if (art != null) {
            Image(
                painter = painterResource(art),
                contentDescription = null,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(fill)
                    .border(1.5.dp, rim, shape)
            )
        }
        content()
    }
}

/** Liczba na odznace - im wiecej cyfr, tym mniejsza, zeby zmiescila sie w kole. */
@Composable
private fun BadgeNumber(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Black,
        fontSize = when {
            text.length <= 2 -> 8.sp
            text.length == 3 -> 7.sp
            else -> 5.5.sp
        },
        lineHeight = 8.sp,
        style = HeavyOutline
    )
}

/** Szesciokat "na plasko" - odznaka poziomu, zeby nie mylila sie z okraglymi statystykami. */
private val HexShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.25f, 0f)
    lineTo(w * 0.75f, 0f)
    lineTo(w, h * 0.5f)
    lineTo(w * 0.75f, h)
    lineTo(w * 0.25f, h)
    lineTo(0f, h * 0.5f)
    close()
}

private val HeavyOutline = TextStyle(shadow = Shadow(color = Color(0xFF1A0E04), blurRadius = 4f))

/** Srednica odznak poziomu/ataku/pancerza na tabliczce. */
internal val STAT_BADGE = 17.dp

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

private val Outline = TextStyle(
    shadow = Shadow(color = Color(0xFF0A0604), blurRadius = 2.5f),
    drawStyle = null
)

