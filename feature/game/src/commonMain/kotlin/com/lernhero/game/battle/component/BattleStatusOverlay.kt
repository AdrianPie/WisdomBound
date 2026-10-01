package com.lernhero.game.battle.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.live.CycleOutcome
import com.lernhero.domain.quiz.CategoryOffer
import com.lernhero.game.battle.BattleUiState
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

private val Good = Color(0xFF22A55E)
private val Bad = Color(0xFFD1453D)
private val Accent = Color(0xFFE0A93A)
private val Ink = Color(0xFF120E0A)

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
    val boosted = state.boostedElements

    // Nic aktywnego - nie zasmiecamy pola bitwy pustym paskiem.
    if (cycleBuff == null && streak <= 0 && !stunned && boosted.isEmpty()) return

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
            val toUlti = if (state.ultimateQueued) 0 else (state.ultimate.streak - state.streak.ultiCharge).coerceAtLeast(0)
            StatusChip(
                icon = "🔥",
                label = "STREAK $streak",
                detail = if (toUlti > 0) "$toUlti to ${state.ultimate.name}" else "ULTI!",
                accent = Accent
            )
        }

        if (stunned) {
            StatusChip("💫", "STUNNED", "${state.stunTurnsRemaining} turns", Bad)
        }

        // Bonus frakcyjny z kafla wybranego przed tura - bez tego znacznika gracz zapomina, ktora
        // frakcje wlasnie wzmocnil, zanim salwa dojdzie do skutku.
        boosted.forEach { element ->
            StatusChip(
                icon = element.icon,
                label = element.label.uppercase(),
                detail = "+${(CategoryOffer.ELEMENT_BOOST * 100).toInt()}% ATK",
                accent = Color(element.colorHex)
            )
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

