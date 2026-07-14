package com.lernhero.game.battle.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lernhero.game.battle.BattleUiState
import com.lernhero.game.battle.UpgradeRarity
import com.lernhero.shared.theme.ButtonPrimary
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.TextPrimary

@Composable
fun UpgradeDialog(
    state: BattleUiState,
    onBuyUpgrade: (String) -> Unit,
    onFinish: () -> Unit
) {
    val upgrade = state.upgrade ?: return

    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF171717)
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = "Nagrody",
                    subtitle = "Punkty: ${state.rewardPoints}   Wynik: ${state.totalScore}"
                )

                upgrade.choices.forEach { choice ->
                    val isSelected = choice.id in upgrade.selectedChoiceIds
                    val canBuy = !isSelected && state.rewardPoints >= choice.cost

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = choice.title,
                                    color = choice.rarity.color(),
                                    fontFamily = FontFirst(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = choice.rarity.label,
                                    color = choice.rarity.color(),
                                    fontFamily = FontFirst(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = choice.description,
                                    color = Color(0xFF666666),
                                    fontFamily = FontFirst(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            OutlinedButton(
                                enabled = canBuy,
                                onClick = { onBuyUpgrade(choice.id) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isSelected) "OK" else "${choice.cost}")
                            }
                        }
                    }
                }

                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonPrimary,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Kolejna fala")
                }
            }
        }
    }
}

private fun UpgradeRarity.color(): Color =
    when (this) {
        UpgradeRarity.Common -> Color(0xFF555555)
        UpgradeRarity.Rare -> Color(0xFF236DFF)
        UpgradeRarity.Epic -> Color(0xFF8B3DFF)
        UpgradeRarity.Legendary -> Color(0xFFF24C00)
    }
