package com.lernhero.home.story

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.story.StageLoot
import com.lernhero.domain.story.StoryStage
import com.lernhero.home.ui.RarityPalette
import com.lernhero.home.ui.icon
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlin.math.roundToInt

private val Panel = Color(0xFF19120C)
private val Ink = Color(0xFF120E0A)
private val Frame = Color(0xFFC8A45A)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val GoldText = Color(0xFFFFD34D)

/**
 * Podglad lupu planszy (przycisk LOOT). Jawna lista "co moze wypasc" - ta sama zasada, co jawne
 * szanse na kole fortuny: gracz ma wiedziec, PO CO bije akurat te plansze, zamiast zgadywac.
 *
 * Rzadsze przedmioty wchodza do puli dopiero glebiej i na wyzszych tierach, wiec ten ekran jest
 * jednoczesnie odpowiedzia na pytanie "czy oplaca sie tu wrocic na Koszmarze".
 */
@Composable
fun StageLootDialog(
    stage: StoryStage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember(stage.globalIndex, stage.tier) {
        StageLoot.itemPool(stage.globalIndex, stage.tier)
    }
    val maxRarity = remember(stage.globalIndex, stage.tier) {
        StageLoot.maxRarity(stage.globalIndex, stage.tier)
    }
    val dropChance = (StageLoot.dropChance(stage.isBoss) * 100).roundToInt()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC07050B))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Panel)
                .border(2.dp, Frame, RoundedCornerShape(16.dp))
                .clickable(enabled = false) {}
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LUP Z PLANSZY ${stage.label}",
                color = GoldText,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LootStat("💰", "${stage.rewardGold}", "zloto")
                LootStat("⭐", "${stage.rewardXp}", "XP")
                LootStat("🎲", "$dropChance%", "na przedmiot")
            }

            Text(
                text = "Best possible drop here: ${RarityPalette.label(maxRarity)}",
                color = RarityPalette.frame(maxRarity),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Ink)
                        .border(1.dp, RarityPalette.frame(item.rarity), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RarityPalette.cardBackground(item.rarity)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.icon(), fontSize = 17.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            color = Cream,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = RarityPalette.label(item.rarity),
                            color = RarityPalette.frame(item.rarity),
                            fontFamily = FontFirst(),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (stage.isBoss) {
                Text(
                    text = "The boss also opens a chest wheel of fortune.",
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "ZAMKNIJ",
                color = GoldText,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF3A2A18))
                    .border(1.5.dp, Frame, RoundedCornerShape(8.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 20.dp, vertical = 9.dp)
            )
        }
    }
}

@Composable
private fun LootStat(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 15.sp)
        Text(
            text = value,
            color = Cream,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Text(text = label, color = Muted, fontFamily = FontFirst(), fontSize = 9.sp)
    }
}
