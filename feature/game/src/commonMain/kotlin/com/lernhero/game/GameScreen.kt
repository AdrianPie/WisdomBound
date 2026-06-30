package com.lernhero.game

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
fun GameScreen (
    navigateToFightScreen: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(Resources.Image.battlegroundFirst),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xD3151515)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Arena",
                        color = Color.White,
                        fontFamily = FontPixel(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fala 1 - testowy run",
                        color = Color(0xFFEFEFEF),
                        fontFamily = FontFirst(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = navigateToFightScreen,
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
                        Text("Start")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FightInfoTile(
                    title = "Enemy",
                    value = "Skeleton",
                    modifier = Modifier.weight(1f)
                )
                FightInfoTile(
                    title = "Reward",
                    value = "+3 pkt",
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Team",
                    color = TextPrimary,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TeamRow(name = "Knight", role = "Front", stat = "HP 50")
                TeamRow(name = "Sorcerer", role = "Back", stat = "MP 50")
            }
        }
    }
}

@Composable
private fun FightInfoTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
            .padding(14.dp)
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TeamRow(
    name: String,
    role: String,
    stat: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = name,
                color = TextPrimary,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = role,
                color = Color(0xFF6D6D6D),
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = stat,
            color = Color(0xFFF24C00),
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
