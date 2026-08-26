package com.lernhero.game.battle.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.lernhero.game.spritesheet.SpriteSheetFrame
import com.lernhero.game.spritesheet.frameForProgress
import org.jetbrains.compose.resources.painterResource

/**
 * Renderuje wszystkie zywe efekty z [controller] - jedna nakladka na cale pole bitwy, calkowicie
 * niezalezna od tego, ktora postac je odpalila. Pozycje w [ActiveEffect] sa w globalnych (root)
 * wspolrzednych (patrz [CharacterRect.resolve]), wiec musimy je tylko przesunac wzgledem wlasnego
 * pochodzenia tej nakladki - dokladnie ten sam trik co dawny `ProjectileOverlay`.
 */
@Composable
fun EffectsOverlay(controller: EffectsController, modifier: Modifier = Modifier) {
    var layerOrigin by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { layerOrigin = it.positionInRoot() }
    ) {
        controller.active.forEach { effect ->
            key(effect.instanceId) {
                EffectVisual(effect = effect, layerOrigin = layerOrigin)
            }
        }
    }
}

@Composable
private fun EffectVisual(effect: ActiveEffect, layerOrigin: Offset) {
    val spec = effect.spec
    val progress = remember(effect.instanceId) { Animatable(0f) }
    LaunchedEffect(effect.instanceId) {
        progress.animateTo(1f, tween(spec.durationMs, easing = LinearEasing))
    }

    val world = when (spec.motion) {
        EffectMotion.TRAVEL -> Offset(
            x = effect.from.x + (effect.to.x - effect.from.x) * progress.value,
            y = effect.from.y + (effect.to.y - effect.from.y) * progress.value
        )
        EffectMotion.STATIC_FADE, EffectMotion.LOOP_STATUS -> effect.from
    }
    val local = world - layerOrigin
    val density = LocalDensity.current
    val widthPx = with(density) { spec.sizeDp.toPx() }
    val heightPx = with(density) { spec.heightDp.toPx() }
    val halfWidthPx = widthPx / 2
    val halfHeightPx = heightPx / 2
    // Kompensuje asymetryczne dopelnienie w samym PNG (patrz EffectSpec.contentOffset) - bez tego
    // centrujemy niewidoczny, geometryczny box klatki na anchorze, a nie faktyczna tresc w niej.
    val contentOffsetXPx = spec.contentOffset.x * widthPx
    val contentOffsetYPx = spec.contentOffset.y * heightPx

    // Prosty pop-in / fade-out - konkretny ksztalt "zycia" efektu zalezy od tego, co narysuje
    // sam PNG (spec docelowo moze dostac wlasna krzywa, na razie jedna wspolna wystarcza).
    val alpha = when {
        progress.value < 0.15f -> progress.value / 0.15f
        progress.value > 0.8f -> (1f - progress.value) / 0.2f
        else -> 1f
    }

    // Pocisk lecacy w lewo (wrog -> gracz) ma byc odbity - art jest narysowany leczac w prawo.
    val flipped = spec.motion == EffectMotion.TRAVEL && effect.to.x < effect.from.x
    val positionModifier = Modifier
        .size(width = spec.sizeDp, height = spec.heightDp)
        .offset {
            IntOffset(
                (local.x - halfWidthPx - contentOffsetXPx).toInt(),
                (local.y - halfHeightPx - contentOffsetYPx).toInt()
            )
        }
        .graphicsLayer {
            this.alpha = alpha
            scaleX = if (flipped) -1f else 1f
        }

    val spriteSheet = spec.spriteSheet
    if (spriteSheet != null) {
        SpriteSheetFrame(
            spec = spriteSheet,
            frame = spriteSheet.frameForProgress(progress.value),
            modifier = positionModifier
        )
    } else {
        Image(
            painter = painterResource(spec.drawable),
            contentDescription = null,
            modifier = positionModifier
        )
    }
}
