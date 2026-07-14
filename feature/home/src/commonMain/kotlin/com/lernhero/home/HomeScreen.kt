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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lernhero.shared.theme.ButtonPrimary
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.Surface as AppSurface
import com.lernhero.shared.theme.TextPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeScreen(
    onOpenFight: () -> Unit,
    onOpenKnightPreview: () -> Unit,
    onOpenModularKnightPreview: () -> Unit,
    onOpenEquipment: () -> Unit
) {
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
            HeroStatusCard(
                onOpenFight = onOpenFight,
                onOpenKnightPreview = onOpenKnightPreview,
                onOpenModularKnightPreview = onOpenModularKnightPreview,
                onOpenEquipment = onOpenEquipment
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryTile(
                    title = "Level",
                    value = "4",
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(
                    title = "Gold",
                    value = "120",
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(
                    title = "Wave",
                    value = "1",
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Dzisiaj",
                    color = TextPrimary,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TaskRow(text = "Wygraj 1 walke", reward = "+2 pkt")
                TaskRow(text = "Ulepsz postac", reward = "+1 pkt")
                TaskRow(text = "Odpowiedz na quiz", reward = "+3 gold")
            }
        }
    }
}

@Composable
private fun HeroStatusCard(
    onOpenFight: () -> Unit,
    onOpenKnightPreview: () -> Unit,
    onOpenModularKnightPreview: () -> Unit,
    onOpenEquipment: () -> Unit
) {
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
                .size(92.dp)
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
                text = "LernHero",
                color = Color.White,
                fontFamily = FontPixel(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Knight - gotowy do runu",
                color = Color(0xFFE7E7E7),
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodyMedium
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpenFight,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonPrimary,
                            contentColor = TextPrimary
                        )
                    ) {
                        Icon(
                            painter = painterResource(Resources.Icon.swords),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fight")
                    }
                    OutlinedButton(onClick = onOpenEquipment) {
                        Icon(
                            painter = painterResource(Resources.Icon.Person),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EQ")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onOpenKnightPreview) {
                        Icon(
                            painter = painterResource(Resources.Icon.Person),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pivots")
                    }
                    OutlinedButton(onClick = onOpenModularKnightPreview) {
                        Icon(
                            painter = painterResource(Resources.Icon.Person),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Preview")
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(92.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Color(0xFF6D6D6D),
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = value,
            color = TextPrimary,
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TaskRow(
    text: String,
    reward: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = TextPrimary,
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = reward,
            color = Color(0xFFF24C00),
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
