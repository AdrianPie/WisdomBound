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
    /** Odpalane gdy pelna sekwencja ataku ([playSwordAttack]) faktycznie sie skonczy - tak walka wie, kiedy naprawde odegrala sie animacja, bez zgadywania czasu z zewnatrz. */
    onAttackAnimationFinished: () -> Unit = {},
) {
    val gear = if (armored) KnightEquipment.FullPlate else equipment
    val wandAttack = remember { Animatable(0f) }
    var localAttackTrigger by remember { mutableStateOf(0) }

    fun faster(millis: Int): Int = (millis / attackSpeed).toInt().coerceAtLeast(1)

    // Szybkie ciecie mieczem - cofniecie, zamach z lekkim przestrzeleniem, powrot. Cala sekwencja
    // ~360ms przy attackSpeed=1x (dawniej ~3s "kanalowania" jak przy zaklęciu - za wolno na zwykly cios).
    suspend fun playSwordAttack() {
        wandAttack.stop()
        wandAttack.snapTo(0f)
        wandAttack.animateTo(-0.18f, tween(faster(70), easing = FastOutSlowInEasing))
        wandAttack.animateTo(1.08f, tween(faster(90), easing = FastOutSlowInEasing))
        wandAttack.animateTo(1f, tween(faster(60), easing = LinearOutSlowInEasing))
        wandAttack.animateTo(0f, tween(faster(140), easing = FastOutSlowInEasing))
    }

    LaunchedEffect(attackTrigger) {
        if (attackTrigger > 0) {
            playSwordAttack()
            onAttackAnimationFinished()
        }
    }

    LaunchedEffect(localAttackTrigger) {
        if (localAttackTrigger > 0) {
            playSwordAttack()
            onAttackAnimationFinished()
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
    // Szybkie ciecie mieczem = ruch samej gornej polowy ciala i miecza. Nogi stoja w miejscu
    // (tylko lekki oddech idle) - zadnego przysiadu/wykroku, ktory wygladal jak przygotowanie
    // do zapasow, nie do zamachu mieczem.
    val wholeBodySway = breath * 0.35f + attack * 1f
    val upperBodySway = breath * 0.6f + attack * 3f
    val headRotation = counterBreath * 0.45f + attack * -2f

    // Limbs are single, rigid pieces (no elbow/knee art) - each rotates as one unit
    // around its shoulder/hip pivot only.
    val backArmRotation = counterBreath * 0.7f + attack * 6f
    // Naturalny luk ciecia mieczem (nie uniesienie jak przy rozdzce) - okolo -70 stopni w szczycie zamachu.
    val wandArmRotation = breath * 0.8f + attack * -70f

    // Nogi zostaja w idle - zero wykroku/przysiadu przy ataku.
    val rearLegRotation = breath * 0.18f
    val frontLegRotation = counterBreath * 0.18f

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
            }
    ) {
        // Rear leg = character's LEFT leg (screen-right)
        KnightLeg(
            part = Player.leftLeg,
            pivot = KnightPivots.leftLeg,
            rotation = rearLegRotation,
            footOverlay = if (gear.feet) PlateArmor.leftFoot else null
        )
        // Front leg = character's RIGHT leg (screen-left)
        KnightLeg(
            part = Player.rightLeg,
            pivot = KnightPivots.rightLeg,
            rotation = frontLegRotation,
            footOverlay = if (gear.feet) PlateArmor.rightFoot else null
        )

        KnightUpperBodyLayer(
            rotation = upperBodySway,
            lift = 0f
        ) {
            // Idle/back arm = character's RIGHT arm (screen-left), drawn behind body/head
            KnightArm(
                part = Player.rightHand,
                pivot = KnightPivots.rightHand,
                rotation = backArmRotation,
                handOverlay = if (gear.hands) PlateArmor.rightHand else null
            )
        }

        KnightUpperBodyLayer(
            rotation = upperBodySway,
            lift = 0f
        ) {
            // Wand/sword arm = character's LEFT arm (screen-right), drawn in front
            KnightArm(
                part = Player.leftHand,
                pivot = KnightPivots.leftHand,
                rotation = wandArmRotation,
                handOverlay = if (gear.hands) PlateArmor.leftHand else null,
                heldBehind = if (gear.mainHand) PlateArmor.swordBehind else null,
                heldFront = if (gear.mainHand) PlateArmor.swordFront else null
            )
            Image(
                painter = painterResource(Player.body),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            if (gear.chest) {
                Image(
                    painter = painterResource(PlateArmor.chest),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
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
            }
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
    part: DrawableResource,
    pivot: TransformOrigin,
    rotation: Float,
    handOverlay: DrawableResource? = null,
    heldBehind: DrawableResource? = null,
    heldFront: DrawableResource? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                transformOrigin = pivot
                rotationZ = rotation
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
            painter = painterResource(part),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        handOverlay?.let { overlay ->
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

@Composable
private fun KnightLeg(
    part: DrawableResource,
    pivot: TransformOrigin,
    rotation: Float,
    footOverlay: DrawableResource? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                transformOrigin = pivot
                rotationZ = rotation
            }
    ) {
        Image(
            painter = painterResource(part),
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
