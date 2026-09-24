package com.lernhero.game.battle.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.playback.HitKind
import kotlinx.coroutines.delay

/** Kolor liczby obrazen wg rodzaju trafienia - to samo skojarzenie co dawniej w CombatFx. */
fun HitKind.damageNumberColor(): Color = when (this) {
    HitKind.Hit -> Color(0xFFFFFFFF)
    HitKind.Crit -> Color(0xFFFFDE59)
    HitKind.Burn -> Color(0xFFFF8A3C)
    HitKind.Poison -> Color(0xFF9CE24B)
    HitKind.Reflect -> Color(0xFFFF7A6B)
    HitKind.Skill -> Color(0xFF8FD0FF)
    HitKind.Heal -> Color(0xFF7BE88A)
    HitKind.Block -> Color(0xFFBFD4E8)
    HitKind.Dodge -> Color(0xFFE6F0FF)
    HitKind.Shield -> Color(0xFF9FD8FF)
}

internal data class ActiveDamageNumber(
    val instanceId: Long,
    val text: String,
    val color: Color,
    val position: Offset
)

/**
 * Osobna, prosta warstwa "-12"/"+8" nad postacia - nic wspolnego z PNG-owymi efektami
 * ([EffectsController]). Liczba unosi sie w gore i gasnie sama, kontroler tylko trzyma liste
 * zywych liczb i usuwa kazda po uplywie [durationMs].
 */
class DamageNumberController {
    private var nextInstanceId = 0L
    internal val active = mutableStateListOf<ActiveDamageNumber>()

    suspend fun show(position: Offset, text: String, color: Color, durationMs: Int = 950) {
        val id = nextInstanceId++
        active.add(ActiveDamageNumber(id, text, color, position))
        delay(durationMs.toLong())
        active.removeAll { it.instanceId == id }
    }
}

@Composable
fun DamageNumbersOverlay(controller: DamageNumberController, modifier: Modifier = Modifier) {
    var layerOrigin by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { layerOrigin = it.positionInRoot() }
    ) {
        controller.active.forEach { number ->
            key(number.instanceId) {
                DamageNumberVisual(number = number, layerOrigin = layerOrigin)
            }
        }
    }
}

@Composable
private fun DamageNumberVisual(number: ActiveDamageNumber, layerOrigin: Offset) {
    val progress = remember(number.instanceId) { Animatable(0f) }
    LaunchedEffect(number.instanceId) {
        progress.animateTo(1f, tween(950, easing = LinearOutSlowInEasing))
    }

    val local = number.position - layerOrigin
    val riseOffsetPx = progress.value * 70f
    // Krotki "pop" na starcie (0.5 -> 1.2 -> 1.0), potem stala skala do konca zycia liczby.
    val scale = when {
        progress.value < 0.12f -> 0.5f + (progress.value / 0.12f) * 0.7f
        progress.value < 0.22f -> 1.2f - ((progress.value - 0.12f) / 0.10f) * 0.2f
        else -> 1f
    }
    val alpha = when {
        progress.value < 0.1f -> progress.value / 0.1f
        progress.value > 0.55f -> (1f - (progress.value - 0.55f) / 0.45f).coerceIn(0f, 1f)
        else -> 1f
    }

    Text(
        text = number.text,
        color = number.color,
        fontSize = 15.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .offset { IntOffset((local.x - 18f).toInt(), (local.y - 12f).toInt()) }
            .graphicsLayer {
                this.alpha = alpha
                translationY = -riseOffsetPx
                scaleX = scale
                scaleY = scale
            }
    )
}
