package com.lernhero.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

private val Bg = AppPalette.Background
private val Panel = AppPalette.Panel
private val PanelRaised = AppPalette.PanelRaised
private val Ink = AppPalette.Ink
private val Gold = AppPalette.Gold
private val GoldDim = AppPalette.GoldDim
private val GoldFrame = AppPalette.GoldFrame
private val TextBody = AppPalette.TextBody
private val Muted = AppPalette.TextMuted
private val Action = AppPalette.Action

private data class QuickAction(
    val icon: String,
    val title: String,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    onOpenFight: () -> Unit,
    onOpenKnightPreview: () -> Unit,
    onOpenModularKnightPreview: () -> Unit,
    onOpenEquipment: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenFitness: () -> Unit = {},
    onOpenBooks: () -> Unit = {}
) {
    val viewModel = koinViewModel<PlayerViewModel>()
    val state by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppPalette.ScreenGradient)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HeroCard(name = state.name, money = state.money)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryTile(icon = "💰", title = "Zloto", value = "${state.money}", modifier = Modifier.weight(1f))
                SummaryTile(
                    icon = "🛡️",
                    title = "Ekwipunek",
                    value = "${state.equipment.equipped.size}/9",
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(icon = "✨", title = "Zaklecia", value = "${state.ownedSpells.size}", modifier = Modifier.weight(1f))
            }

            SectionLabel("Ruszaj w droge")
            QuickActionsGrid(
                actions = listOf(
                    QuickAction("⚔️", "Walcz", onOpenFight),
                    QuickAction("🛡️", "Postac", onOpenEquipment),
                    QuickAction("🛒", "Sklep", onOpenShop),
                    QuickAction("🏋️", "Trening", onOpenFitness),
                    QuickAction("📚", "Ksiazki", onOpenBooks)
                )
            )

            SectionLabel("Dzisiaj")
            TodayCard()

            DevLinksRow(onOpenKnightPreview, onOpenModularKnightPreview)
        }
    }
}

@Composable
private fun HeroCard(name: String, money: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppPalette.CardGradient)
            .border(2.dp, GoldDim, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Ink)
                .border(2.dp, Gold, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Resources.Image.knightAvatar),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = name.ifBlank { "Sir Lernhero" },
                color = Gold,
                fontFamily = FontPixel(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Rycerz gotowy do runu",
                color = TextBody.copy(alpha = 0.75f),
                fontFamily = FontFirst(),
                fontSize = 12.sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Ink)
                    .border(1.dp, GoldFrame, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(text = "💰 $money", color = Gold, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SummaryTile(icon: String, title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Panel)
            .border(1.5.dp, GoldDim, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 18.sp)
        Text(text = value, color = Gold, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(text = title, color = Muted, fontFamily = FontFirst(), fontSize = 10.sp)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = Gold,
        fontFamily = FontPixel(),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickActionsGrid(actions: List<QuickAction>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        actions.forEach { action ->
            Column(
                modifier = Modifier
                    .width(96.dp)
                    .aspectRatio(0.95f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.verticalGradient(listOf(PanelRaised, Panel)))
                    .border(1.5.dp, GoldDim, RoundedCornerShape(14.dp))
                    .clickable(onClick = action.onClick)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = action.icon, fontSize = 26.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = action.title,
                    color = TextBody,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun TodayCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppPalette.CardGradient)
            .border(2.dp, GoldDim, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TaskRow(icon = "⚔️", text = "Wygraj 1 walke", reward = "+zloto")
        TaskRow(icon = "🏋️", text = "15 minut aktywnosci", reward = "+zloto")
        TaskRow(icon = "📖", text = "15 minut czytania", reward = "+zloto")
    }
}

@Composable
private fun TaskRow(icon: String, text: String, reward: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = TextBody, fontFamily = FontFirst(), fontSize = 13.sp)
        }
        Text(text = reward, color = Action, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

/** Ekrany deweloperskie - male, dyskretne linki, zeby nie mylily sie z prawdziwymi funkcjami gry. */
@Composable
private fun DevLinksRow(onOpenKnightPreview: () -> Unit, onOpenModularKnightPreview: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "dev: pivots",
            color = Muted.copy(alpha = 0.5f),
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            modifier = Modifier.clickable(onClick = onOpenKnightPreview).padding(8.dp)
        )
        Text(
            text = "dev: preview",
            color = Muted.copy(alpha = 0.5f),
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            modifier = Modifier.clickable(onClick = onOpenModularKnightPreview).padding(8.dp)
        )
    }
}
