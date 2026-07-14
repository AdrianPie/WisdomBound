package com.lernhero.game.battle.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lernhero.game.battle.BattleUiState
import com.lernhero.shared.theme.ButtonPrimary
import com.lernhero.shared.theme.TextPrimary

@Composable
fun DefeatDialog(
    state: BattleUiState,
    onRestart: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF171717)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DialogHeader(
                    title = "Defeat",
                    subtitle = "Dotarles do fali ${state.wave}. Wynik: ${state.totalScore}. +${state.rewardCoins} monet"
                )

                Button(
                    onClick = onRestart,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonPrimary,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Restart")
                }
            }
        }
    }
}
