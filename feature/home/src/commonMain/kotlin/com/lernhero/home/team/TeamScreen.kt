package com.lernhero.home.team

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.home.map.DungeonMapPanel
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.ui.MonsterCardView
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.koin.compose.viewmodel.koinViewModel

private val PanelDark = Color(0xFF1A1410)
private val Gold = Color(0xFFC8A45A)
private val GoldDim = Color(0xFF6B5A38)
private val TextGold = Color(0xFFFFD100)
private val TextMuted = Color(0xFF9D8B6A)
private val SlotEmpty = Color(0xFF120E0A)
private val HeroGlow = Color(0xFF3B9DFF)

/**
 * Ekran zakladki "Fight": mapa krainy jest pelnoekranowym tlem (odpowiednik "Estate Map" z
 * Darkest Dungeon), a sklad druzyny plywa nad nia jako karta u gory. Trudnosc i kategoria pytan
 * nie sa juz wybierane osobno tutaj - zalezy od lokacji wybranej na mapie (patrz
 * [com.lernhero.home.map.DungeonMapPanel]), ktora sama odpala [onStartBattle] po potwierdzeniu wyboru.
 *
 * Bohater siedzi w pierwszym, zablokowanym slocie. Obok niego stoja potwory-kompani,
 * wybierane kliknieciem (plus w rogu panelu albo pusty slot) - bez przeciagania.
 */
@Composable
fun TeamScreen(
    onStartBattle: () -> Unit
) {
    val viewModel = koinViewModel<PlayerViewModel>()
    val state by viewModel.uiState.collectAsState()

    var monsterPickerOpen by remember { mutableStateOf(false) }

    val hasFreeSlot = state.partyMonsters.any { it == null }

    Box(modifier = Modifier.fillMaxSize()) {
        DungeonMapPanel(
            onStartBattle = onStartBattle,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TeamPanel(
                equipment = state.knightEquipment,
                companions = state.partyMonsters,
                onAddCompanion = { monsterPickerOpen = true },
                onRemoveCompanion = viewModel::removeFromParty
            )

            state.message?.let { message ->
                Text(
                    text = message,
                    color = TextGold,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PanelDark.copy(alpha = 0.9f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }

    if (monsterPickerOpen) {
        MonsterPickerDialog(
            available = state.companionInventory,
            hasFreeSlot = hasFreeSlot,
            onDismiss = { monsterPickerOpen = false },
            onPick = { card ->
                val free = state.partyMonsters.indexOfFirst { it == null }
                if (free >= 0) viewModel.placeInParty(card.id, free)
                monsterPickerOpen = false
            }
        )
    }
}

@Composable
private fun TeamPanel(
    equipment: KnightEquipment,
    companions: List<MonsterCard?>,
    onAddCompanion: () -> Unit,
    onRemoveCompanion: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelDark)
            .border(2.dp, GoldDim, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YOUR TEAM",
                color = TextGold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f).padding(start = 10.dp, top = 8.dp, bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(36.dp)
                    .background(SlotEmpty)
                    .border(1.5.dp, GoldDim)
                    .clickable(onClick = onAddCompanion),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = TextGold, fontFamily = FontPixel(), fontSize = 18.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HeroSlotCard(equipment = equipment, modifier = Modifier.weight(1f).height(78.dp))
            companions.forEachIndexed { index, card ->
                CompanionSlotCard(
                    card = card,
                    onClick = {
                        if (card == null) onAddCompanion() else onRemoveCompanion(index)
                    },
                    modifier = Modifier.weight(1f).height(78.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroSlotCard(
    equipment: KnightEquipment,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    listOf(HeroGlow.copy(alpha = 0.22f), Color(0xFF0E0A08))
                )
            )
            .border(2.dp, HeroGlow, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(modifier = Modifier.fillMaxWidth(0.94f)) {
            ModularKnight(
                modifier = Modifier.align(Alignment.BottomCenter),
                equipment = equipment
            )
        }
        Text(
            text = "🔒",
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.TopEnd).padding(5.dp)
        )
    }
}

@Composable
private fun CompanionSlotCard(
    card: MonsterCard?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (card == null) {
                    Modifier
                        .background(SlotEmpty)
                        .border(1.5.dp, GoldDim.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (card == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(text = "＋", color = GoldDim, fontSize = 26.sp)
                Text(
                    text = "Wolne\nmiejsce",
                    color = TextMuted,
                    fontFamily = FontFirst(),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            MonsterCardView(
                card = card,
                portraitHeight = 72,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Wybor kompana kliknieciem. Zastapil przeciaganie, ktore na telefonie bylo meczace. */
@Composable
private fun MonsterPickerDialog(
    available: List<MonsterCard>,
    hasFreeSlot: Boolean,
    onDismiss: () -> Unit,
    onPick: (MonsterCard) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PanelDark,
        title = {
            Text(
                text = "Wybierz kompana",
                color = TextGold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            when {
                !hasFreeSlot -> Text(
                    text = "Wszystkie miejsca zajete. Kliknij kompana w druzynie, zeby go odeslac.",
                    color = TextMuted,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.bodyMedium
                )

                available.isEmpty() -> Text(
                    text = "Nie masz wolnych potworow. Kup ich w sklepie.",
                    color = TextMuted,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.bodyMedium
                )

                else -> MonsterPickerGrid(available = available, onPick = onPick)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Zamknij", color = TextGold, fontFamily = FontFirst())
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonsterPickerGrid(
    available: List<MonsterCard>,
    onPick: (MonsterCard) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        available.forEach { card ->
            MonsterCardView(
                card = card,
                portraitHeight = 84,
                modifier = Modifier
                    .width(108.dp)
                    .clickable { onPick(card) }
            )
        }
    }
}

