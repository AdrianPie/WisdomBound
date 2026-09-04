package com.lernhero.game.battle.component


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp

fun Modifier.shake(trigger: Any): Modifier = composed {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        offsetX.animateTo(
            0f,
            keyframes {
                durationMillis = 300
                -12f at 50
                12f at 100
                -8f at 150
                8f at 200
                -4f at 250
                0f at 300
            }
        )
    }

    this.offset(x = offsetX.value.dp)
}