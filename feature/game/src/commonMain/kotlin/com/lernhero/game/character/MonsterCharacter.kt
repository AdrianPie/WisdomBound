package com.lernhero.game.character

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import org.jetbrains.compose.resources.imageResource

/**
 * Pojedynczy PNG animowany matematycznie (bez sprite-sheeta) - odpowiednik [ModularKnight] dla
 * potworow, tylko bez rigu na osobne czesci ciala (jeden plik = cala postac).
 *
 * Animacja ataku SAMA zglasza kiedy skonczyla (przez [onAttackAnimationFinished]) zamiast
 * wywolujacy musial zgadywac czas trwania ze sprite'a - to jedyny punkt, w ktorym ten komponent
 * "rozmawia" z warstwa sekwencjonowania w BattleField.
 *
 * Art wszystkich gatunkow jest narysowany patrzac w lewo - [isEnemySide] steruje odbiciem
 * (`scaleX`), zeby ten sam preset dzialal zarowno jako wrog (bez odbicia) jak i sojusznik/companion
 * (odbity, patrzy w prawo w strone wrogow).
 */
@Composable
fun MonsterCharacter(
    preset: MonsterVisualPreset,
    state: CharacterState,
    attackTrigger: Int,
    isEnemySide: Boolean,
    modifier: Modifier = Modifier,
    sizeDp: Dp = preset.baseSizeDp,
    attackSpeed: Float = 1f,
    /**
     * Gdy true, sylwetka moze wyjsc poza kwadrat karty w formacji ([requiredSize] zamiast [size],
     * ktory jest przycinany do constraintow rodzica). Uzywane dla bossa - patrz [CharacterUiModel.battleScale].
     */
    unbounded: Boolean = false,
    onAttackAnimationFinished: () -> Unit = {}
) {
    fun faster(millis: Int): Int = (millis / attackSpeed).toInt().coerceAtLeast(1)

    val attack = remember { Animatable(0f) }
    LaunchedEffect(attackTrigger) {
        if (attackTrigger > 0) {
            attack.stop()
            attack.snapTo(0f)
            // Cofniecie (anticipation), potem szybki wypad do przodu z lekkim przestrzeleniem, powrot.
            attack.animateTo(-0.25f, tween(faster(120), easing = FastOutSlowInEasing))
            attack.animateTo(1.1f, tween(faster(140), easing = FastOutSlowInEasing))
            attack.animateTo(1f, tween(faster(70), easing = LinearOutSlowInEasing))
            attack.animateTo(0f, tween(faster(160), easing = FastOutSlowInEasing))
            onAttackAnimationFinished()
        }
    }

    val hitFlash = remember { Animatable(0f) }
    LaunchedEffect(state) {
        if (state == CharacterState.HIT) {
            hitFlash.snapTo(1f)
            hitFlash.animateTo(0f, tween(220, easing = LinearOutSlowInEasing))
        }
    }

    val enterPop = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) {
        enterPop.animateTo(1.08f, tween(220, easing = FastOutSlowInEasing))
        enterPop.animateTo(1f, tween(120, easing = LinearOutSlowInEasing))
    }

    val idleInfinite = rememberInfiniteTransition()
    val breath by idleInfinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse)
    )
    // Zegar animacji spoczynku w ms. Kazdy potwor dostaje wlasne przesuniecie, zeby dwa takie same
    // gatunki obok siebie nie oddychaly i nie sypaly iskrami w idealnym unisonie.
    val clock = idleInfinite.animateFloat(
        initialValue = 0f,
        targetValue = IDLE_CLOCK_MS.toFloat(),
        animationSpec = infiniteRepeatable(tween(IDLE_CLOCK_MS, easing = LinearEasing))
    )
    val phaseOffset = remember { kotlin.random.Random.nextFloat() * 10_000f }

    val deadAlpha by animateFloatAsState(
        targetValue = if (state == CharacterState.DEAD) 0f else 1f,
        animationSpec = tween(420)
    )
    val deadScale by animateFloatAsState(
        targetValue = if (state == CharacterState.DEAD) 0.7f else 1f,
        animationSpec = tween(420)
    )

    // Art patrzy w lewo z natury - po stronie wroga zostaje jak jest, po stronie sojusznika odbijamy.
    val facing = preset.facing(isEnemySide)
    // Wypad zawsze w strone przeciwnej formacji: wrog (prawa strona) bije w lewo, sojusznik w prawo.
    val lungeDirection = if (isEnemySide) -1f else 1f
    val attackTranslatePx = attack.value * 14f
    // Stary podskok zostaje tylko dla artu bez wlasnego oddechu (breathe = 0).
    val idleBobPx = if (preset.idle.breathe > 0f) 0f else breath * 2.5f

    Box(
        // preset.visualScale kompensuje rozne puste marginesy w kazdym PNG, zeby wszystkie
        // gatunki wygladaly na ta sama wielkosc obok siebie mimo tego samego sizeDp.
        modifier = modifier.let {
            if (unbounded) it.requiredSize(sizeDp * preset.visualScale) else it.size(sizeDp * preset.visualScale)
        },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Jeden Canvas zamiast dwoch Image: sprite, fala szat, czasteczki i blysk trafienia maja
        // ten sam czas i te same transformacje (patrz [drawIdleMonster]).
        val image = imageResource(preset.drawable)
        val pivotY = preset.anchors.root.pivotFractionY
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = facing * enterPop.value * deadScale
                    scaleY = enterPop.value * deadScale
                    translationY = idleBobPx
                    translationX = attackTranslatePx * lungeDirection
                    alpha = deadAlpha
                }
        ) {
            // Czytane TUTAJ, w fazie rysowania - zegar co klatke odswieza tylko rysunek, nie kompozycje.
            val t = clock.value + phaseOffset
            drawIdleMonster(image = image, style = preset.idle, t = t, pivotY = pivotY)
            val flash = hitFlash.value
            if (flash > 0f) {
                drawIdleMonster(
                    image = image,
                    style = preset.idle,
                    t = t,
                    pivotY = pivotY,
                    alpha = flash * 0.65f,
                    colorFilter = ColorFilter.tint(Color(0xFFFF3B30), BlendMode.SrcIn),
                    particles = false
                )
            }
        }
    }
}

/** Dlugosc petli zegara spoczynku - dluga, zeby przeskok na poczatek zdarzal sie rzadko. */
private const val IDLE_CLOCK_MS = 120_000
