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
import com.lernhero.game.battle.GamePhase
import com.lernhero.shared.theme.ButtonPrimary
import com.lernhero.shared.theme.TextPrimary

/**
 * Koniec walki - albo wygrana (ostatnia fala z [com.lernhero.domain.run.BattleRequest.waves]
 * oczyszczona), albo porazka/kapitulacja. Jedyna droga dalej prowadzi z powrotem na mape/korytarz.
 */
@Composable
fun BattleResultDialog(
    state: BattleUiState,
    onLeave: () -> Unit
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
                    title = if (state.phase == GamePhase.Victory) "Walka wygrana" else "Koniec biegu",
                    // Dokladny rozklad "sakwa vs konto" siedzi juz w message - liczony raz, w viewmodelu.
                    subtitle = state.message ?: "Padles na fali ${state.wave}."
                )

                Button(
                    onClick = onLeave,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonPrimary,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Wroc na mape")
                }
            }
        }
    }
}
