package com.lernhero.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lernhero.shared.ButtonPrimary
import com.lernhero.shared.FontFirst
import com.lernhero.shared.FontPixel
import com.lernhero.shared.Resources
import com.lernhero.shared.Surface as AppSurface
import com.lernhero.shared.TextPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun EquipmentScreen() {
    var points by remember { mutableStateOf(8) }
    var strength by remember { mutableStateOf(4) }
    var hp by remember { mutableStateOf(5) }
    var mana by remember { mutableStateOf(3) }
    var speed by remember { mutableStateOf(2) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CharacterHeader(points = points)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Stats",
                    color = TextPrimary,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                UpgradeRow(
                    icon = Resources.Icon.swords,
                    label = "Attack",
                    value = strength,
                    points = points,
                    onUpgrade = {
                        strength += 1
                        points -= 1
                    }
                )
                UpgradeRow(
                    icon = Resources.Icon.Weight,
                    label = "HP",
                    value = hp,
                    points = points,
                    onUpgrade = {
                        hp += 1
                        points -= 1
                    }
                )
                UpgradeRow(
                    icon = Resources.Icon.Book,
                    label = "Mana",
                    value = mana,
                    points = points,
                    onUpgrade = {
                        mana += 1
                        points -= 1
                    }
                )
                UpgradeRow(
                    icon = Resources.Icon.RightArrow,
                    label = "Speed",
                    value = speed,
                    points = points,
                    onUpgrade = {
                        speed += 1
                        points -= 1
                    }
                )
            }
        }
    }
}

@Composable
private fun CharacterHeader(points: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF171717))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A2A2A)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Resources.Image.knightAvatar),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Knight",
                color = Color.White,
                fontFamily = FontPixel(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Level 4",
                color = Color(0xFFE7E7E7),
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Punkty rozwoju: $points",
                color = Color(0xFFFFC738),
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun UpgradeRow(
    icon: org.jetbrains.compose.resources.DrawableResource,
    label: String,
    value: Int,
    points: Int,
    onUpgrade: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = TextPrimary
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    color = TextPrimary,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value.toString(),
                    color = Color(0xFFF24C00),
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            StatMeter(progress = value / 10f)
        }
        Button(
            enabled = points > 0,
            onClick = onUpgrade,
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonPrimary,
                contentColor = TextPrimary,
                disabledContainerColor = Color(0xFFE2E2E2),
                disabledContentColor = Color(0xFF777777)
            )
        ) {
            Icon(
                painter = painterResource(Resources.Icon.Plus),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun StatMeter(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE2E2E2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF24C00))
        )
    }
}
