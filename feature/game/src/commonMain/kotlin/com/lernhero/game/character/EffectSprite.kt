package com.lernhero.game.character

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.lernhero.shared.SpriteAsset
import com.stevdza_san.sprite.component.SpriteView
import com.stevdza_san.sprite.domain.SpriteSheet
import com.stevdza_san.sprite.domain.SpriteSpec
import com.stevdza_san.sprite.domain.rememberSpriteState
import com.stevdza_san.sprite.util.getScreenWidth
import kotlinx.coroutines.delay

@Composable
fun EffectSprite(
    effectId: EffectId,
    modifier: Modifier = Modifier,
) {

    val sprite: SpriteAsset = remember(effectId) {
        effectSpriteFor(effectId.name)
    }


    val spriteState =
        rememberSpriteState(
            totalFrames = sprite.totalFrames,
            framesPerRow = sprite.framesPerRow,
            animationSpeed = sprite.animationSpeed
        )


    LaunchedEffect(sprite) {
        spriteState.start()
    }
    LaunchedEffect(sprite) {
        spriteState.start()

        val durationMs =
            sprite.totalFrames * sprite.animationSpeed

        delay(durationMs.toLong())

        spriteState.stop()
        spriteState.cleanup()
        println("EffectSprite FINISHED $effectId")
    }


    SpriteView(
        modifier = modifier,
        spriteState = spriteState,
        spriteSpec = SpriteSpec(
            screenWidth = getScreenWidth().value,
            default = SpriteSheet(
                frameWidth = sprite.frameWidth,
                frameHeight = sprite.frameHeight,
                image = sprite.drawable
            )
        )
    )
}