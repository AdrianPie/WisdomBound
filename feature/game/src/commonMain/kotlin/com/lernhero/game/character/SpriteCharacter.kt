package com.lernhero.game.character

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.shared.resources.SpriteAsset
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.imageResource

@Composable
fun SpriteCharacter(
    modifier: Modifier = Modifier,
    sprite: SpriteAsset,
    isLooping: Boolean = true
) {
    val spriteSheet = imageResource(sprite.drawable)
    val totalFrames = sprite.totalFrames.coerceAtLeast(1)
    val animationFrames = if (sprite.pingPong && totalFrames > 1) {
        totalFrames * 2 - 2
    } else {
        totalFrames
    }
    val framesPerRow = sprite.framesPerRow.coerceAtLeast(1)
    val frameAspectRatio = sprite.frameWidth.toFloat() / sprite.frameHeight.toFloat()
    var frameIndex by remember(sprite, isLooping) { mutableIntStateOf(0) }

    // Animacje jednorazowe (atak, smierc) musza zmiescic sie w skroconym kroku walki, wiec ida
    // w jej tempie. Idle i trafienie zostaja w swoim rytmie - inaczej przy 8x cale pole bitwy
    // wibruje, choc zadna z tych animacji niczego nie opoznia.
    val frameDelay = if (isLooping) {
        sprite.animationSpeed
    } else {
        BattleSpeed.scale(sprite.animationSpeed)
    }

    LaunchedEffect(sprite, isLooping, frameDelay) {
        frameIndex = 0

        if (isLooping) {
            while (true) {
                delay(frameDelay)
                frameIndex = (frameIndex + 1) % animationFrames
            }
        } else {
            for (frame in 0 until animationFrames) {
                frameIndex = frame
                if (frame < animationFrames - 1) {
                    delay(frameDelay)
                }
            }
        }
    }

    val pingPongFrame = if (sprite.pingPong && totalFrames > 1 && frameIndex >= totalFrames) {
        animationFrames - frameIndex
    } else {
        frameIndex
    }

    val spriteFrame = if (sprite.playBackwards) {
        totalFrames - 1 - pingPongFrame
    } else {
        pingPongFrame
    }
    val col = spriteFrame % framesPerRow
    val row = spriteFrame / framesPerRow

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(frameAspectRatio)
    ) {
        drawImage(
            image = spriteSheet,
            srcOffset = IntOffset(col * sprite.frameWidth, row * sprite.frameHeight),
            srcSize = IntSize(sprite.frameWidth, sprite.frameHeight),
            dstSize = IntSize(size.width.toInt(), size.height.toInt())
        )
    }
}
