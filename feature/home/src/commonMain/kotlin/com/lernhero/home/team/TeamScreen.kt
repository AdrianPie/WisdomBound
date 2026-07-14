package com.lernhero.home.team

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.ui.MonsterCardView
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import com.lernhero.shared.theme.Surface as AppSurface
import org.koin.compose.viewmodel.koinViewModel

private val PanelDark = Color(0xFF1A1410)
private val Gold = Color(0xFFC8A45A)
private val GoldDim = Color(0xFF6B5A38)
private val TextGold = Color(0xFFFFD100)
private val TextMuted = Color(0xFF9D8B6A)
private val SlotEmpty = Color(0xFF120E0A)
private val HeroGlow = Color(0xFF3B9DFF)

/**
 * Inny gracz w druzynie. Na razie bierzemy ich z atrapy listy [FAKE_ONLINE_PLAYERS] -
 * gdy pojawi sie backend, ta sama struktura przyjdzie z serwera i ekran sie nie zmieni.
 */
private data class TeamMate(
    val id: String,
    val name: String,
    val level: Int,
    val heroClass: String
)

private val FAKE_ONLINE_PLAYERS = listOf(
    TeamMate("p1", "Zeratul", 12, "Lowca"),
    TeamMate("p2", "Mirea", 9, "Kaplanka"),
    TeamMate("p3", "Borg", 15, "Berserk"),
    TeamMate("p4", "Ashe", 11, "Mag")
)

/**
 * Ekran zakladki "Fight": tu skladasz druzyne i stad ruszasz do lochu.
 *
 * Bohater siedzi w pierwszym, zablokowanym slocie. Obok niego stoja potwory-kompani,
 * wybierane kliknieciem (plus w rogu panelu albo pusty slot) - bez przeciagania.
 */
@Composable
fun TeamScreen(
    onStartDungeon: () -> Unit
) {
    val viewModel = koinViewModel<PlayerViewModel>()
    val state by viewModel.uiState.collectAsState()

    val mates = remember { mutableStateListOf<TeamMate>() }
    var monsterPickerOpen by remember { mutableStateOf(false) }
    var playerPickerOpen by remember { mutableStateOf(false) }

    val hasFreeSlot = state.partyMonsters.any { it == null }

    Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TeamPanel(
                equipment = state.knightEquipment,
                companions = state.partyMonsters,
                onAddCompanion = { monsterPickerOpen = true },
                onRemoveCompanion = viewModel::removeFromParty
            )

            Button(
                onClick = { playerPickerOpen = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PanelDark,
                    contentColor = TextGold
                ),
                border = BorderStroke(2.dp, GoldDim)
            ) {
                Text(
                    text = "ADD PLAYER",
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (mates.isNotEmpty()) {
                MateRow(mates = mates, onRemove = { mate -> mates.remove(mate) })
            }

            state.message?.let { message ->
                Text(
                    text = message,
                    color = TextGold,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Szukanie losowej druzyny ruszy, gdy bedzie kogo szukac.",
                color = TextMuted,
                fontFamily = FontFirst(),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onStartDungeon,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB4451F),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "START DUNGEON",
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
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

    if (playerPickerOpen) {
        PlayerPickerDialog(
            taken = mates.map { it.id }.toSet(),
            onDismiss = { playerPickerOpen = false },
            onPick = { mate ->
                mates += mate
                playerPickerOpen = false
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
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f).padding(14.dp)
            )
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(SlotEmpty)
                    .border(2.dp, GoldDim)
                    .clickable(onClick = onAddCompanion),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = TextGold, fontFamily = FontPixel(), fontSize = 26.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HeroSlotCard(equipment = equipment, modifier = Modifier.weight(1f))
            companions.forEachIndexed { index, card ->
                CompanionSlotCard(
                    card = card,
                    onClick = {
                        if (card == null) onAddCompanion() else onRemoveCompanion(index)
                    },
                    modifier = Modifier.weight(1f)
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
            .aspectRatio(0.7f)
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
            .aspectRatio(0.7f)
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

/** Gracze dobrani do druzyny. Docelowo pojada tu znajomi i gildia z social area. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MateRow(
    mates: List<TeamMate>,
    onRemove: (TeamMate) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        mates.forEach { mate ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlotEmpty)
                    .border(1.dp, Gold, RoundedCornerShape(8.dp))
                    .clickable { onRemove(mate) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mate.name,
                    color = TextGold,
                    fontFamily = FontFirst(),
                    fontSize = 12.sp
                )
                Text(
                    text = "lvl ${mate.level}  ✕",
                    color = TextMuted,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Atrapa listy znajomych/online. Tu wladuje sie pozniej prawdziwa lista z social area:
 * znajomi, gildia, a docelowo kolejka losowego dobierania graczy.
 */
@Composable
private fun PlayerPickerDialog(
    taken: Set<String>,
    onDismiss: () -> Unit,
    onPick: (TeamMate) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PanelDark,
        title = {
            Text(
                text = "Dodaj gracza",
                color = TextGold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Gracze online (na razie atrapa - czeka na backend).",
                    color = TextMuted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
                FAKE_ONLINE_PLAYERS.forEach { mate ->
                    val used = mate.id in taken
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlotEmpty)
                            .border(
                                width = 1.dp,
                                color = if (used) GoldDim.copy(alpha = 0.4f) else GoldDim,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = !used) { onPick(mate) }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = mate.name,
                            color = if (used) TextMuted else TextGold,
                            fontFamily = FontFirst()
                        )
                        Text(
                            text = "${mate.heroClass} - lvl ${mate.level}",
                            color = TextMuted,
                            fontFamily = FontFirst(),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Zamknij", color = TextGold, fontFamily = FontFirst())
            }
        }
    )
}
