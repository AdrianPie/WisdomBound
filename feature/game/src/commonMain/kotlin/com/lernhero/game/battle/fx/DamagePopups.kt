package com.lernhero.game.battle.fx

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.playback.HitKind
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.shared.theme.StrokedText
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/** Jak wyglada wyskakujaca liczba - kolor, wielkosc i ruch zaleza od rodzaju trafienia. */
data class PopupLook(
    val color: Color,
    val outline: Color,
    val sizeSp: Float,
    val label: String? = null,
    val rise: Boolean = false,
    val durationMs: Int = 950
)

fun HitKind.popupLook(): PopupLook = when (this) {
    HitKind.Hit -> PopupLook(Color.White, Color(0xFF1A0E08), 17f)
    HitKind.Crit -> PopupLook(Color(0xFFFFD84A), Color(0xFF5A1A04), 25f, label = "CRIT!", durationMs = 1150)
    HitKind.Burn -> PopupLook(Color(0xFFFF9A3C), Color(0xFF4A1204), 18f)
    HitKind.Poison -> PopupLook(Color(0xFFB6F04A), Color(0xFF1E3A06), 17f)
    HitKind.Reflect -> PopupLook(Color(0xFFFF7A6B), Color(0xFF3A0A06), 15f)
    HitKind.Skill -> PopupLook(Color(0xFF9FDCFF), Color(0xFF0A2340), 19f)
    HitKind.Heal -> PopupLook(Color(0xFF7CF08A), Color(0xFF0A3A14), 17f, rise = true)
    HitKind.Block -> PopupLook(Color(0xFFD6ECFF), Color(0xFF0E2238), 15f)
    HitKind.Dodge -> PopupLook(Color.White, Color(0xFF1A1A24), 15f)
    HitKind.Shield -> PopupLook(Color(0xFF9FD8FF), Color(0xFF0A2238), 14f, rise = true)
}

internal data class ActivePopup(
    val id: Long,
    val text: String,
    val look: PopupLook,
    val at: Offset,
    /** Strona, w ktora liczba odskakuje - na przemian, zeby seria trafien sie nie nakladala. */
    val side: Float,
    /** Ktora z rzedu w krotkim czasie na tym samym celu - kazda kolejna startuje wyzej. */
    val stack: Int
)

/**
 * Wyskakujace liczby obrazen/leczenia. Osobny kontroler, bo tekst najlepiej wyglada jako
 * komponent (obrys z [StrokedText]), a nie rysowany na Canvasie efektow.
 */
@Stable
class DamagePopupController {
    private var nextId = 0L
    internal val active = mutableStateListOf<ActivePopup>()
    private val lastOnTarget = mutableMapOf<String, Pair<Long, Int>>()

    fun show(targetId: String, at: Offset, text: String, look: PopupLook) {
        val now = VfxEngine.monoNow() / 1_000_000L
        val previous = lastOnTarget[targetId]
        val stack = if (previous != null && now - previous.first < 450L) (previous.second + 1).coerceAtMost(4) else 0
        lastOnTarget[targetId] = now to stack
        val id = nextId++
        active += ActivePopup(id, text, look, at, side = if (id % 2L == 0L) 1f else -1f, stack = stack)
    }

    internal fun remove(id: Long) {
        active.removeAll { it.id == id }
    }
}

@Composable
fun DamagePopupsLayer(controller: DamagePopupController, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        controller.active.forEach { popup ->
            key(popup.id) { PopupView(popup, onDone = { controller.remove(popup.id) }) }
        }
    }
}

@Composable
private fun PopupView(popup: ActivePopup, onDone: () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(BattleSpeed.scale(popup.look.durationMs), easing = LinearEasing))
        onDone()
    }
    val look = popup.look
    val crit = look.label != null

    // Punkt zaczepienia o zerowym rozmiarze - tekst centruje sie na nim sam.
    Box(
        modifier = Modifier
            .offset { IntOffset(popup.at.x.roundToInt(), (popup.at.y - popup.stack * 16.dp.toPx()).roundToInt()) }
            .size(0.dp)
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize(Alignment.Center, unbounded = true)
                .align(Alignment.Center)
                .graphicsLayer {
                    val p = progress.value
                    // Wyskok: szybkie powiekszenie z przestrzeleniem, potem osiada.
                    val pop = when {
                        p < 0.1f -> 0.3f + (p / 0.1f) * (if (crit) 1.4f else 1.05f)
                        p < 0.24f -> (if (crit) 1.7f else 1.35f) - ((p - 0.1f) / 0.14f) * (if (crit) 0.55f else 0.35f)
                        else -> if (crit) 1.15f else 1f
                    }
                    scaleX = pop
                    scaleY = pop
                    val up = 1f - (1f - p).pow(2)
                    translationY = -(if (look.rise) 46f else 30f) * density * up
                    translationX = if (look.rise) 0f else popup.side * 16f * density * p
                    rotationZ = if (crit && p < 0.3f) sin(p * 60f) * 6f * (1f - p / 0.3f) else 0f
                    alpha = if (p > 0.7f) (1f - (p - 0.7f) / 0.3f).coerceIn(0f, 1f) else 1f
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((-4).dp)
        ) {
            look.label?.let {
                StrokedText(text = it, size = 11.sp, color = Color(0xFFFF8A2A), outline = look.outline, strokeWidth = 5f, maxLines = 1)
            }
            StrokedText(text = popup.text, size = look.sizeSp.sp, color = look.color, outline = look.outline, strokeWidth = 7f, maxLines = 1)
        }
    }
}

/** Uzywane przez sinusowe kolysanie - zeby nie importowac PI w kilku miejscach. */
internal const val TAU = (2.0 * PI).toFloat()
