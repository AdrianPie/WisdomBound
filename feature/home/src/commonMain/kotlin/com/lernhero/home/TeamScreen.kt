package com.lernhero.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.data.PlayerRepository
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.shared.FontFirst
import com.lernhero.shared.FontPixel
import com.lernhero.shared.Surface as AppSurface
import org.koin.compose.koinInject

private val PanelDark = Color(0xFF1A1410)
private val Gold = Color(0xFFC8A45A)
private val GoldDim = Color(0xFF6B5A38)
private val TextGold = Color(0xFFFFD100)
private val TextMuted = Color(0xFF9D8B6A)
private val SlotEmpty = Color(0xFF120E0A)
private val HeroGlow = Color(0xFF3B9DFF)

/** Ile obcych postaci moze dolaczyc do bohatera. Bohater ma wlasny, staly slot obok nich. */
private const val PLAYER_SLOTS = 2

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
 * Bohater jest zablokowany w pierwszym slocie, pozostale sa na innych graczy (MMO).
 */
@Composable
fun TeamScreen(
    onStartDungeon: () -> Unit,
    onOpenCompanions: () -> Unit
) {
    val playerRepository = koinInject<PlayerRepository>()
    val viewModel = remember(playerRepository) { PlayerViewModel(playerRepository) }
    val state by viewModel.uiState.collectAsState()

    val party = remember { List<TeamMate?>(PLAYER_SLOTS) { null }.toMutableStateList() }
    var pickerOpen by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TeamPanel(
                heroName = state.name,
                equipment = state.knightEquipment,
                party = party,
                onAdd = { pickerOpen = true },
                onRemove = { index -> party[index] = null }
            )

            Button(
                onClick = { pickerOpen = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PanelDark,
                    contentColor = TextGold
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, GoldDim)
            ) {
                Text(
                    text = "ADD PLAYER",
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            OutlinedButton(
                onClick = onOpenCompanions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Kompani (potwory)",
                    color = TextMuted,
                    fontFamily = FontFirst()
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

    if (pickerOpen) {
        PlayerPickerDialog(
            taken = party.filterNotNull().map { it.id }.toSet(),
            onDismiss = { pickerOpen = false },
            onPick = { mate ->
                val free = party.indexOfFirst { it == null }
                if (free >= 0) party[free] = mate
                pickerOpen = false
            }
        )
    }
}

@Composable
private fun TeamPanel(
    heroName: String,
    equipment: KnightEquipment,
    party: List<TeamMate?>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit
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
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = TextGold, fontFamily = FontPixel(), fontSize = 26.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HeroSlotCard(
                name = heroName.ifBlank { "Bohater" },
                equipment = equipment,
                modifier = Modifier.weight(1f)
            )
            party.forEachIndexed { index, mate ->
                PlayerSlotCard(
                    mate = mate,
                    onClick = { if (mate == null) onAdd() else onRemove(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeroSlotCard(
    name: String,
    equipment: KnightEquipment,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
        Text(
            text = name,
            color = HeroGlow,
            fontFamily = FontFirst(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun PlayerSlotCard(
    mate: TeamMate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(10.dp))
                .background(SlotEmpty)
                .border(
                    width = if (mate == null) 1.5.dp else 2.dp,
                    color = if (mate == null) GoldDim.copy(alpha = 0.7f) else Gold,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (mate == null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(text = "＋", color = GoldDim, fontSize = 24.sp)
                    Text(
                        text = "Wolne\nmiejsce",
                        color = TextMuted,
                        fontFamily = FontFirst(),
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(text = "🛡", fontSize = 24.sp)
                    Text(
                        text = "lvl ${mate.level}",
                        color = TextGold,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "usun",
                        color = TextMuted,
                        fontFamily = FontFirst(),
                        fontSize = 9.sp
                    )
                }
            }
        }
        Text(
            text = mate?.name ?: "Gracz",
            color = if (mate == null) TextMuted else TextGold,
            fontFamily = FontFirst(),
            fontSize = 11.sp,
            maxLines = 1
        )
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
                            .border(1.dp, if (used) GoldDim.copy(alpha = 0.4f) else GoldDim, RoundedCornerShape(8.dp))
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
