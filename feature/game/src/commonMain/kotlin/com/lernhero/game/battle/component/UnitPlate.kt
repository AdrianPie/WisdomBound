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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.FontFirst
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Tabliczka walczacego nad glowa: u gory rzad `[poziom] (serce HP) (miecz ATK) (tarcza DEF)`,
 * a pod nim `[zywiol]` i cienki pasek HP na reszte szerokosci.
 *
 * - **Poziom** - okragla odznaka ze zlota (nasi) albo czerwona (wrogowie) obwodka.
 * - **Zywiol** - emblemat ([elementBadge]); u naszych pulsuje, gdy kafel tematu go wzmocnil.
 * - **HP / ATK / DEF** - ciemne "pigulki" z ikona i liczba.
 * - **ATK NA TE TURE** - po odpowiedzi mnoznik salwy wchodzi od razu: liczba dolicza sie, pigulka
 *   "pompuje", a liczba zmienia kolor (jasne zloto = dobrze dla gracza, czerwien = zle).
 * - **DEF** - pancerz w procentach ([com.lernhero.domain.combat.Armor]).
 * - **Pasek HP** z "cieniem obrazen" (jasny kawalek znika chwile po ciosie).
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
    Column(
        modifier = modifier.width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            LevelBadge(level = character.level, isPlayer = character.isPlayer)
            StatPill(icon = Resources.Icon.PlateHeart, text = "${character.hp.coerceAtLeast(0)}")
            AttackPill(base = character.attack, multiplier = attackMultiplier, isPlayer = character.isPlayer)
            StatPill(icon = Resources.Icon.PlateShield, text = "${(character.armor * 100).roundToInt()}")
        }
        // Zywiol z lewej przy pasku zycia, pasek zajmuje reszte szerokosci rzedu.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            elementBadge?.invoke()
            HpBar(
                hp = character.hp,
                maxHp = character.maxHp,
                modifier = Modifier.weight(1f).widthIn(min = barWidth)
            )
        }
    }
}

/**
 * Grafiki pod odznakami tabliczki. `null` = odznaka rysowana w kodzie (kolo/szesciokat z gradientem).
 * Po wgraniu PNG wystarczy tu podpiac zasob - liczba zostaje rysowana na wierzchu.
 */
internal object PlateArt {
    val level: DrawableResource? = null
}

@Composable
private fun LevelBadge(level: Int, isPlayer: Boolean) {
    val rim = if (isPlayer) Color(0xFFFFD66B) else Color(0xFFFF8A7A)
    BadgeFrame(
        art = PlateArt.level,
        shape = CircleShape,
        fill = Brush.verticalGradient(listOf(Color(0xFF3A4458), Color(0xFF151A24))),
        rim = rim
    ) {
        BadgeNumber(text = "$level", color = Color.White)
    }
}

@Composable
private fun AttackPill(base: Int, multiplier: Float, isPlayer: Boolean) {
    val target = (base * multiplier).roundToInt()
    val value by animateFloatAsState(target.toFloat(), tween(650, easing = FastOutSlowInEasing), label = "atk")
    val up = multiplier > 1.001f
    val down = multiplier < 0.999f
    val goodForPlayer = if (isPlayer) up else down
    // Kolor = korzysc dla GRACZA: nasz atak w gore albo wroga w dol to jasne zloto, odwrotnie czerwien.
    val textColor by animateColorAsState(
        when {
            !up && !down -> Color.White
            goodForPlayer -> Color(0xFFFFE066)
            else -> Color(0xFFFF6B5E)
        }, label = "atkText"
    )
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(target) {
        if (!up && !down && target == base) return@LaunchedEffect
        pulse.snapTo(1f)
        pulse.animateTo(1.35f, tween(140, easing = FastOutSlowInEasing))
        pulse.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 420f))
    }
    StatPill(
        icon = Resources.Icon.PlateSword,
        text = "${value.roundToInt()}",
        textColor = textColor,
        modifier = Modifier.graphicsLayer {
            scaleX = pulse.value
            scaleY = pulse.value
        }
    )
}

/** Ciemna pigulka: ikona z lewej, liczba z prawej. */
@Composable
private fun StatPill(
    icon: DrawableResource,
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    Row(
        modifier = modifier
            .height(STAT_BADGE)
            .clip(RoundedCornerShape(50))
            .background(Color(0xCC0E1118))
            .padding(start = 1.5.dp, end = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(7.5.dp)
        )
        Text(
            text = text,
            color = textColor,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 5.sp,
            lineHeight = 5.sp,
            style = HeavyOutline
        )
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
                    .border(1.dp, rim, shape)
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
            text.length <= 2 -> 5.5.sp
            text.length == 3 -> 5.sp
            else -> 4.sp
        },
        lineHeight = 5.5.sp,
        style = HeavyOutline
    )
}

private val HeavyOutline = TextStyle(shadow = Shadow(color = Color(0xFF1A0E04), blurRadius = 2f))

/** Wysokosc rzedu statystyk (odznaka poziomu i pigulki) na tabliczce. */
internal val STAT_BADGE = 12.dp

@Composable
private fun HpBar(hp: Int, maxHp: Int, modifier: Modifier = Modifier) {
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
        modifier = modifier
            .height(3.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xE6120C08))
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
    }
}
