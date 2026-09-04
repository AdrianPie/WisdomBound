package com.lernhero.home.dev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lernhero.game.character.ModularKnight
import com.lernhero.shared.theme.ButtonPrimary
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.Surface
import com.lernhero.shared.theme.TextPrimary

@Composable
fun ModularKnightPreviewScreen(
    onBack: () -> Unit
) {
    var attackTrigger by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    text = "Wroc",
                    fontFamily = FontFirst()
                )
            }
            Text(
                text = "ModularKnight",
                color = TextPrimary,
                fontFamily = FontFirst()
            )
        }

        Button(
            onClick = {
                attackTrigger += 1
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonPrimary,
                contentColor = TextPrimary
            )
        ) {
            Text(
                text = "Wand attack",
                fontFamily = FontFirst()
            )
        }

        KnightPreviewCard(
            title = "Bez zbroi",
            attackTrigger = attackTrigger,
            armored = false
        )
        KnightPreviewCard(
            title = "Zbroja plytowa",
            attackTrigger = attackTrigger,
            armored = true
        )
    }
}

@Composable
private fun KnightPreviewCard(
    title: String,
    attackTrigger: Int,
    armored: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontFamily = FontFirst()
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E1E)),
            contentAlignment = Alignment.Center
        ) {
            ModularKnight(
                attackTrigger = attackTrigger,
                armored = armored
            )
        }
    }
}
