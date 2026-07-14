package com.lernhero.game.character

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lernhero.shared.resources.Resources.KnightPivots
import com.lernhero.shared.resources.Resources.PlateArmor
import com.lernhero.shared.resources.Resources.Player
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ModularKnight(
    modifier: Modifier = Modifier,
    attackTrigger: Int = 0,
    armored: Boolean = false,
    equipment: KnightEquipment = if (armored) KnightEquipment.FullPlate else KnightEquipment.Empty,
    sizeDp: Dp = 300.dp,
    interactive: Boolean = true,
    /**
     * Mnoznik tempa zamachu. W walce podaje go BattleSpeed; poza nia (ekwipunek, podglad)
     * rycerz macha w normalnym tempie.
     */
    attackSpeed: Float = 1f,
) {
    val gear = if (armored) KnightEquipment.FullPlate else equipment
    val wandAttack = remember { Animatable(0f) }
    var localAttackTrigger by remember { mutableStateOf(0) }

    fun faster(millis: Int): Int = (millis / attackSpeed).toInt().coerceAtLeast(1)

    suspend fun playWandAttack() {
        wandAttack.stop()
        wandAttack.snapTo(0f)
        // Anticipation: slight wind-up in the opposite direction
        wandAttack.animateTo(-0.12f, tween(faster(140), easing = FastOutSlowInEasing))
        // Rise with a small overshoot, then settle into the pose
        wandAttack.animateTo(1.06f, tween(faster(380), easing = FastOutSlowInEasing))
        wandAttack.animateTo(1f, tween(faster(150), easing = LinearOutSlowInEasing))
        // ~2s channel hold with a gentle pulse instead of a frozen pose
        repeat(2) {
            wandAttack.animateTo(0.975f, tween(faster(480), easing = EaseInOutSine))
            wandAttack.animateTo(1f, tween(faster(480), easing = EaseInOutSine))
        }
        // Return with follow-through past neutral, then settle
        wandAttack.animateTo(-0.05f, tween(faster(340), easing = FastOutSlowInEasing))
        wandAttack.animateTo(0f, tween(faster(160), easing = LinearOutSlowInEasing))
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
    // Whole figure sways together around a pivot near the feet, so legs and torso move
    // as one body: the head/shoulders swing more (far from the pivot), feet barely move
    // (close to it).
    val wholeBodySway = breath * 0.35f + attack * 2.5f
    // Torso adds a little extra lean at the waist. Rotation only - a vertical breathing
    // shift was detaching the torso from the hips, rotation keeps them connected.
    val upperBodySway = breath * 0.6f + attack * 1.7f
    // Battle-stance drop: the WHOLE figure sinks while the legs bend. The leg angles
    // below shorten each leg by the same ~11 canvas px this drop adds back, so the
    // feet land exactly on the ground line instead of hovering or sinking.
    val stanceDrop = attack.coerceAtLeast(0f) * 6.5f
    // Extra torso compression onto the hips (down only, waist overlaps the pelvis).
    val crouch = attack.coerceAtLeast(0f) * 5f
    val headRotation = counterBreath * 0.45f + attack * -2.8f

    val backArmShoulderRotation = counterBreath * 0.7f + attack * 8f
    val backArmElbowRotation = breath * 0.45f + attack * 10f

    val wandShoulderRotation = breath * 0.8f + attack * -150f
    val wandElbowRotation = counterBreath * 0.35f + attack * 10f

    // Attack = asymmetric battle stance. Rear leg (his LEFT) takes the weight: thigh
    // swings out, knee bends ~35deg and the shin tucks back under the body - a half-kneel.
    // Front leg (his RIGHT) extends sideways like a lunge, nearly straight. Both bends
    // shorten the legs by the same amount stanceDrop lowers the figure, so the feet stay
    // on the ground line, and the ankles counter-rotate to keep them flat.
    val rearHipRotation = breath * 0.18f + attack * -30f
    val rearKneeRotation = attack * 35f - wholeBodySway
    val rearFootRotation = -(wholeBodySway + rearHipRotation + rearKneeRotation)
    val frontHipRotation = counterBreath * 0.18f + attack * 24f
    val frontKneeRotation = attack * -12f - wholeBodySway
    val frontFootRotation = -(wholeBodySway + frontHipRotation + frontKneeRotation)

    Box(
        modifier = modifier
            .size(sizeDp)
            .then(
                if (interactive) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        localAttackTrigger += 1
                    }
                } else {
                    Modifier
                }
            )
            .graphicsLayer {
                transformOrigin = KnightPivots.ground
                rotationZ = wholeBodySway
                translationY = stanceDrop.dp.toPx()
            }
    ) {
        // Rear leg = character's LEFT leg (screen-right)
        KnightLeg(
            upperPart = Player.leftUpLeg,
            lowerPart = Player.leftDownLeg,
            footPart = Player.leftFoot,
            hipPivot = KnightPivots.leftUpLeg,
            kneePivot = KnightPivots.leftDownLeg,
            anklePivot = KnightPivots.leftFoot,
            hipRotation = rearHipRotation,
            kneeRotation = rearKneeRotation,
            footRotation = rearFootRotation,
            upperOverlay = if (gear.legs) PlateArmor.leftUpLeg else null,
            lowerOverlay = if (gear.legs) PlateArmor.leftDownLeg else null,
            footOverlay = if (gear.feet) PlateArmor.leftFoot else null
        )
        // Front leg = character's RIGHT leg (screen-left)
        KnightLeg(
            upperPart = Player.rightUpLeg,
            lowerPart = Player.rightDownLeg,
            footPart = Player.rightFoot,
            hipPivot = KnightPivots.rightUpLeg,
            kneePivot = KnightPivots.rightDownLeg,
            anklePivot = KnightPivots.rightFoot,
            hipRotation = frontHipRotation,
            kneeRotation = frontKneeRotation,
            footRotation = frontFootRotation,
            upperOverlay = if (gear.legs) PlateArmor.rightUpLeg else null,
            lowerOverlay = if (gear.legs) PlateArmor.rightDownLeg else null,
            footOverlay = if (gear.feet) PlateArmor.rightFoot else null,
            lowerBehindUpper = true
        )

        KnightUpperBodyLayer(
            rotation = upperBodySway,
            lift = crouch
        ) {
            // Wand/sword arm = character's LEFT arm (screen-right), drawn in front
            KnightArm(
                upperPart = Player.leftUpHand,
                lowerPart = Player.leftDownHand,
                shoulderPivot = KnightPivots.leftUpHand,
                elbowPivot = KnightPivots.leftDownHand,
                shoulderRotation = wandShoulderRotation,
                elbowRotation = wandElbowRotation,
                lowerOverlay = if (gear.hands) PlateArmor.leftDownHand else null,
                heldBehind = if (gear.mainHand) PlateArmor.swordBehind else null,
                heldFront = if (gear.mainHand) PlateArmor.swordFront else null
            )
            Image(
                painter = painterResource(Player.body),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        transformOrigin = KnightPivots.head
                        rotationZ = headRotation
                    }
            ) {
                Image(
                    painter = painterResource(Player.head),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                if (gear.head) {
                    Image(
                        painter = painterResource(PlateArmor.head),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        KnightUpperBodyLayer(
            rotation = upperBodySway,
            lift = crouch
        ) {
            // Idle/back arm = character's RIGHT arm (screen-left), drawn behind
            KnightArm(
                upperPart = Player.rightUpHand,
                lowerPart = Player.rightDownHand,
                shoulderPivot = KnightPivots.rightUpHand,
                elbowPivot = KnightPivots.rightDownHand,
                shoulderRotation = backArmShoulderRotation,
                elbowRotation = backArmElbowRotation,
                lowerOverlay = if (gear.hands) PlateArmor.rightDownHand else null
            )
        }
    }
}

@Composable
private fun KnightUpperBodyLayer(
    rotation: Float,
    lift: Float,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                transformOrigin = KnightPivots.body
                rotationZ = rotation
                translationY = lift.dp.toPx()
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
    elbowRotation: Float,
    upperOverlay: DrawableResource? = null,
    lowerOverlay: DrawableResource? = null,
    heldBehind: DrawableResource? = null,
    heldFront: DrawableResource? = null
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
        upperOverlay?.let { overlay ->
            Image(
                painter = painterResource(overlay),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    transformOrigin = elbowPivot
                    rotationZ = elbowRotation
                }
        ) {
            // Held object (sword handle) sticking out behind the fist
            heldBehind?.let { held ->
                Image(
                    painter = painterResource(held),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Image(
                painter = painterResource(lowerPart),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            lowerOverlay?.let { overlay ->
                Image(
                    painter = painterResource(overlay),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Held object (sword blade) in front of the fist
            heldFront?.let { held ->
                Image(
                    painter = painterResource(held),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun KnightLeg(
    upperPart: DrawableResource,
    lowerPart: DrawableResource,
    footPart: DrawableResource,
    hipPivot: TransformOrigin,
    kneePivot: TransformOrigin,
    anklePivot: TransformOrigin,
    hipRotation: Float,
    kneeRotation: Float,
    footRotation: Float,
    upperOverlay: DrawableResource? = null,
    lowerOverlay: DrawableResource? = null,
    footOverlay: DrawableResource? = null,
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
                footPart = footPart,
                kneePivot = kneePivot,
                anklePivot = anklePivot,
                kneeRotation = kneeRotation,
                footRotation = footRotation,
                lowerOverlay = lowerOverlay,
                footOverlay = footOverlay
            )
            Image(
                painter = painterResource(upperPart),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            upperOverlay?.let { overlay ->
                Image(
                    painter = painterResource(overlay),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Image(
                painter = painterResource(upperPart),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            upperOverlay?.let { overlay ->
                Image(
                    painter = painterResource(overlay),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            LegLowerPart(
                lowerPart = lowerPart,
                footPart = footPart,
                kneePivot = kneePivot,
                anklePivot = anklePivot,
                kneeRotation = kneeRotation,
                footRotation = footRotation,
                lowerOverlay = lowerOverlay,
                footOverlay = footOverlay
            )
        }
    }
}

@Composable
private fun LegLowerPart(
    lowerPart: DrawableResource,
    footPart: DrawableResource,
    kneePivot: TransformOrigin,
    anklePivot: TransformOrigin,
    kneeRotation: Float,
    footRotation: Float,
    lowerOverlay: DrawableResource? = null,
    footOverlay: DrawableResource? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                transformOrigin = kneePivot
                rotationZ = kneeRotation
            }
    ) {
        Image(
            painter = painterResource(lowerPart),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        lowerOverlay?.let { overlay ->
            Image(
                painter = painterResource(overlay),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    transformOrigin = anklePivot
                    rotationZ = footRotation
                }
        ) {
            Image(
                painter = painterResource(footPart),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            footOverlay?.let { overlay ->
                Image(
                    painter = painterResource(overlay),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
