package com.lernhero.game.battle.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.live.CycleOutcome
import com.lernhero.game.battle.BattleUiState
import com.lernhero.game.battle.TurnOutcome
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val Good = Color(0xFF22A55E)
private val Bad = Color(0xFFD1453D)
private val Accent = Color(0xFFE0A93A)
private val Ink = Color(0xFF120E0A)

/**
 * Duzy, nie do przeoczenia baner z wynikiem ostatniej odpowiedzi - wjezdza na srodek pola bitwy
 * dokladnie w momencie, w ktorym druzyna rusza do ataku, i pokazuje KONKRETNIE, co ta odpowiedz dala
 * (mnoznik salwy, punkty, ulti). Wczesniej jedynym sladem byla jedna linijka tekstu w pasku na dole,
 * ktorej w praniu nie dalo sie zauwazyc.
 */
@Composable
internal fun AnswerOutcomeBanner(
    outcome: TurnOutcome?,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    var shown by remember { mutableStateOf<TurnOutcome?>(null) }

    LaunchedEffect(outcome?.id) {
        val current = outcome ?: return@LaunchedEffect
        shown = current
        visible = true
        delay(BANNER_VISIBLE_MILLIS)
        visible = false
    }

    val entry = shown ?: return
    val accent = if (entry.correct) Good else Bad

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(160)) +
                scaleIn(initialScale = 0.82f, animationSpec = tween(220)) +
                slideInVertically(tween(220)) { it / 3 },
            exit = fadeOut(tween(260))
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Ink.copy(alpha = 0.92f))
                    .border(2.5.dp, accent, RoundedCornerShape(14.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = if (entry.correct) "✅ DOBRZE!" else "❌ ZLE!",
                    color = accent,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "SALWA x${entry.damageMultiplier.oneDecimal()}",
                    color = Color.White,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (entry.reward > 0) {
                    Text(
                        text = "+${entry.reward} pkt",
                        color = Accent,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                if (entry.ultiTriggered) {
                    Text(
                        text = "⚡ ULTI!",
                        color = Accent,
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                entry.cycleLabel?.let {
                    Text(
                        text = it,
                        color = Color.White.copy(alpha = 0.85f),
                        fontFamily = FontFirst(),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Staly zestaw ikon stanu druzyny - widoczny PRZEZ CALA walke, nie tylko w chwili odpowiedzi.
 * Odpowiada na "co ja wlasciwie mam wlaczone": buff/debuff z cyklu, passa do ulti, oszolomienie.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TeamStatusBar(
    state: BattleUiState,
    modifier: Modifier = Modifier
) {
    val cycleBuff = state.activeCycleBuff?.takeIf { state.elapsedMs < it.expiresAtMs }
    val streak = state.streak.consecutiveCorrect
    val stunned = state.stunTurnsRemaining > 0

    // Nic aktywnego - nie zasmiecamy pola bitwy pustym paskiem.
    if (cycleBuff == null && streak <= 0 && !stunned) return

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        cycleBuff?.let { buff ->
            val secondsLeft = ((buff.expiresAtMs - state.elapsedMs) / 1000).coerceAtLeast(0)
            when (buff.outcome) {
                CycleOutcome.Empowered -> StatusChip("⚔️", "+35% ATK", "${secondsLeft}s", Good)
                CycleOutcome.Weakened -> StatusChip("🔻", "-25% ATK", "${secondsLeft}s", Bad)
                CycleOutcome.Neutral -> Unit
            }
        }

        if (streak > 0) {
            val toUlti = (ULTI_STREAK - streak).coerceAtLeast(0)
            StatusChip(
                icon = "🔥",
                label = "PASSA $streak",
                detail = if (toUlti > 0) "$toUlti do ULTI" else "ULTI!",
                accent = Accent
            )
        }

        if (stunned) {
            StatusChip("💫", "OGLUSZENIE", "${state.stunTurnsRemaining} tur", Bad)
        }
    }
}

@Composable
private fun StatusChip(
    icon: String,
    label: String,
    detail: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Ink.copy(alpha = 0.85f))
            .border(1.5.dp, accent, RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 11.sp)
        Text(
            text = label,
            color = accent,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
        Text(
            text = detail,
            color = Color.White.copy(alpha = 0.75f),
            fontFamily = FontFirst(),
            fontSize = 8.sp
        )
    }
}

/** 1.35f -> "1.4" - bez kotlin.String.format, ktorego nie ma w common. */
private fun Float.oneDecimal(): String {
    val scaled = (this * 10).roundToInt()
    return "${scaled / 10}.${scaled % 10}"
}

private const val BANNER_VISIBLE_MILLIS = 1500L

/** Tyle poprawnych z rzedu odpala ulti - zgodne z ULTI_STREAK w domain.combat.live. */
private const val ULTI_STREAK = 5
