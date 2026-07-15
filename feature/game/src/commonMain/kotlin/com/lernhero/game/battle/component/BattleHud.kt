package com.lernhero.game.battle.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.game.battle.BattleUiState
import com.lernhero.game.battle.GamePhase

@Composable
internal fun BattleHud(
    state: BattleUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xCC21150F))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Fala ${state.wave}",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = state.phase.hudLabel(),
            color = Color(0xFFE7D1B6),
            fontSize = 13.sp
        )
        state.playback?.let { playback ->
            Text(
                text = "Playback: ${playback.progressLabel}",
                color = Color(0xFFE7D1B6),
                fontSize = 12.sp
            )
        }
        state.message?.let { message ->
            Text(
                text = message,
                color = Color(0xFFFFF1D6),
                fontSize = 12.sp
            )
        }

        SpeedSwitch()
    }
}

/**
 * Sterowanie tempem walki: [-] wolniej, [+] szybciej. Walka startuje na 1x, wiec animacje widac
 * od razu; przyspieszasz dopiero, gdy chcesz przewinac fale.
 */
@Composable
private fun SpeedSwitch() {
    val multiplier = BattleSpeed.multiplier
    Row(
        modifier = Modifier.padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpeedButton(symbol = "−", enabled = BattleSpeed.canSlowDown, onClick = { BattleSpeed.slower() })
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x99000000))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = BattleSpeed.label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        SpeedButton(symbol = "+", enabled = BattleSpeed.canSpeedUp, onClick = { BattleSpeed.faster() })
    }
}

@Composable
private fun SpeedButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) Color(0xFFB4451F) else Color(0x55B4451F))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = Color.White.copy(alpha = if (enabled) 1f else 0.5f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun GamePhase.hudLabel(): String =
    when (this) {
        GamePhase.Battle -> "Odtwarzanie walki"
        GamePhase.Quiz -> "Quiz po fali"
        GamePhase.Upgrade -> "Ulepszenia po fali"
        GamePhase.Defeat -> "Koniec runu"
    }
