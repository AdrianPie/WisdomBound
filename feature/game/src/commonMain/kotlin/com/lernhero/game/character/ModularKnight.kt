package com.lernhero.game.character

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.lernhero.shared.Resources.KnightPivots
import com.lernhero.shared.Resources.Player
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ModularKnight(
    modifier: Modifier = Modifier,
    attackTrigger: Int = 0
) {
    val wandAttack = remember { Animatable(0f) }
    var localAttackTrigger by remember { mutableStateOf(0) }

    suspend fun playWandAttack() {
        wandAttack.stop()
        wandAttack.snapTo(0f)
        wandAttack.animateTo(0.42f, tween(260, easing = FastOutSlowInEasing))
        wandAttack.animateTo(0.62f, tween(110, easing = FastOutLinearInEasing))
        wandAttack.animateTo(0.78f, tween(2000, easing = LinearOutSlowInEasing))
        wandAttack.animateTo(1f, tween(300, easing = LinearOutSlowInEasing))
        wandAttack.snapTo(0f)
    }

    LaunchedEffect(attackTrigger) {
        if (attackTrigger > 0) {
            playWandAttack()
        }
    }

    LaunchedEffect(localAttackTrigger) {
        if (localAttackTrigger > 0) {
            playWandAttack()
        }
    }

    val idle = rememberInfiniteTransition()
    val breath by idle.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )
    val counterBreath by idle.animateFloat(
        initialValue = 1f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, delayMillis = 180, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    val attack = wandAttack.value
    val bodyRotation = breath * 1.25f + wandCurve(attack, gather = -3.2f, cast = 4.2f, hold = 2.2f)
    val headRotation = counterBreath * 0.45f + wandCurve(attack, gather = 1.8f, cast = -2.8f, hold = -1.4f)

    val backArmShoulderRotation = counterBreath * 0.7f +
            wandCurve(attack, gather = -4f, cast = 8f, hold = 3f)
    val backArmElbowRotation = breath * 0.45f +
            wandCurve(attack, gather = 6f, cast = 10f, hold = 4f)

    val wandShoulderRotation = breath * 0.8f +
            wandCurve(attack, gather = -118f, cast = -178f, hold = -178f)
    val wandElbowRotation = counterBreath * 0.35f +
            wandCurve(attack, gather = 68f, cast = 86f, hold = 86f)

    val rearHipRotation = breath * 0.18f + wandCurve(attack, gather = -2.5f, cast = -1.2f, hold = -0.7f)
    val rearKneeRotation = wandCurve(attack, gather = 4.5f, cast = 2.5f, hold = 1f)
    val frontHipRotation = counterBreath * 0.18f + wandCurve(attack, gather = 2.5f, cast = 1.4f, hold = 0.7f)
    val frontKneeRotation = wandCurve(attack, gather = -4.5f, cast = -2.8f, hold = -1f)

    Box(
        modifier = modifier
            .size(300.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                localAttackTrigger += 1
            }
    ) {
        KnightLeg(
            upperPart = Player.rightUpLeg,
            lowerPart = Player.rightDownLeg,
            hipPivot = KnightPivots.rightUpLeg,
            kneePivot = KnightPivots.rightDownLeg,
            hipRotation = rearHipRotation,
            kneeRotation = rearKneeRotation
        )
        KnightLeg(
            upperPart = Player.leftUpLeg,
            lowerPart = Player.leftDownLeg,
            hipPivot = KnightPivots.leftUpLeg,
            kneePivot = KnightPivots.leftDownLeg,
            hipRotation = frontHipRotation,
            kneeRotation = frontKneeRotation,
            lowerBehindUpper = true
        )

        KnightUpperBodyLayer(bodyRotation = bodyRotation) {
            KnightArm(
                upperPart = Player.leftUpHand,
                lowerPart = Player.leftDownHand,
                shoulderPivot = KnightPivots.leftUpHand,
                elbowPivot = KnightPivots.leftDownHand,
                shoulderRotation = backArmShoulderRotation,
                elbowRotation = backArmElbowRotation
            )
            Image(
                painter = painterResource(Player.body),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            Image(
                painter = painterResource(Player.head),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        transformOrigin = KnightPivots.head
                        rotationZ = headRotation
                    }
            )
        }

        KnightUpperBodyLayer(bodyRotation = bodyRotation) {
            KnightArm(
                upperPart = Player.rightUpHand,
                lowerPart = Player.rightDownHand,
                shoulderPivot = KnightPivots.rightUpHand,
                elbowPivot = KnightPivots.rightDownHand,
                shoulderRotation = wandShoulderRotation,
                elbowRotation = wandElbowRotation
            )
        }
    }
}

@Composable
private fun KnightUpperBodyLayer(
    bodyRotation: Float,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                transformOrigin = KnightPivots.body
                rotationZ = bodyRotation
            }
    ) {
        content()
    }
}

@Composable
private fun KnightArm(
    upperPart: DrawableResource,
    lowerPart: DrawableResource,
    shoulderPivot: TransformOrigin,
    elbowPivot: TransformOrigin,
    shoulderRotation: Float,
    elbowRotation: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                transformOrigin = shoulderPivot
                rotationZ = shoulderRotation
            }
    ) {
        Image(
            painter = painterResource(upperPart),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(lowerPart),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    transformOrigin = elbowPivot
                    rotationZ = elbowRotation
                }
        )
    }
}

@Composable
private fun KnightLeg(
    upperPart: DrawableResource,
    lowerPart: DrawableResource,
    hipPivot: TransformOrigin,
    kneePivot: TransformOrigin,
    hipRotation: Float,
    kneeRotation: Float,
    lowerBehindUpper: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                transformOrigin = hipPivot
                rotationZ = hipRotation
            }
    ) {
        if (lowerBehindUpper) {
            LegLowerPart(
                lowerPart = lowerPart,
                kneePivot = kneePivot,
                kneeRotation = kneeRotation
            )
            Image(
                painter = painterResource(upperPart),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(upperPart),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            LegLowerPart(
                lowerPart = lowerPart,
                kneePivot = kneePivot,
                kneeRotation = kneeRotation
            )
        }
    }
}

@Composable
private fun LegLowerPart(
    lowerPart: DrawableResource,
    kneePivot: TransformOrigin,
    kneeRotation: Float
) {
    Image(
        painter = painterResource(lowerPart),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                transformOrigin = kneePivot
                rotationZ = kneeRotation
            }
    )
}

private fun wandCurve(
    progress: Float,
    gather: Float,
    cast: Float,
    hold: Float
): Float {
    return when {
        progress <= 0.42f -> {
            val t = progress.segment(0f, 0.42f).smoothStep()
            lerp(0f, gather, t)
        }
        progress <= 0.62f -> {
            val t = progress.segment(0.42f, 0.62f)
            lerp(gather, cast, t)
        }
        progress <= 0.78f -> {
            val t = progress.segment(0.62f, 0.78f).smoothStep()
            lerp(cast, hold, t)
        }
        else -> {
            val t = progress.segment(0.78f, 1f).smoothStep()
            lerp(hold, 0f, t)
        }
    }
}

private fun Float.segment(
    start: Float,
    end: Float
): Float {
    return ((this - start) / (end - start)).coerceIn(0f, 1f)
}

private fun Float.smoothStep(): Float {
    return this * this * (3f - 2f * this)
}

private fun lerp(
    start: Float,
    end: Float,
    amount: Float
): Float {
    return start + (end - start) * amount
}
