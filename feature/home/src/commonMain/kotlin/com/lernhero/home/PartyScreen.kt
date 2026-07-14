package com.lernhero.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.lernhero.domain.preset.MonsterCard
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.home.component.MonsterCardView
import com.lernhero.home.dnd.DragAndDropContainer
import com.lernhero.home.dnd.DragAndDropState
import com.lernhero.home.dnd.dragSource
import com.lernhero.home.dnd.dropTarget
import com.lernhero.home.dnd.rememberDragAndDropState
import com.lernhero.shared.FontFirst
import com.lernhero.shared.FontPixel
import com.lernhero.shared.Surface as AppSurface
import org.koin.compose.koinInject

private val PanelDark = Color(0xFF1A1410)
private val PanelMid = Color(0xFF2A2218)
private val Gold = Color(0xFFC8A45A)
private val GoldDim = Color(0xFF6B5A38)
private val TextGold = Color(0xFFFFD100)
private val TextMuted = Color(0xFF9D8B6A)
private val SlotEmpty = Color(0xFF120E0A)
private val HeroGlow = Color(0xFF3B9DFF)

/** Payload carried during a party drag. */
private sealed interface PartyDrag {
    data class FromInventory(val monsterId: String) : PartyDrag
    data class FromSlot(val slotIndex: Int, val monsterId: String) : PartyDrag
}

private const val INVENTORY_KEY = "party_inventory"
private fun slotKey(index: Int) = "party_slot_$index"

@Composable
fun PartyScreen(
    onStartBattle: () -> Unit
) {
    val playerRepository = koinInject<PlayerRepository>()
    val viewModel = remember(playerRepository) { PlayerViewModel(playerRepository) }
    val state by viewModel.uiState.collectAsState()
    val dragState = rememberDragAndDropState<PartyDrag>()

    Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {
        DragAndDropContainer(state = dragState, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PartyHeader(name = state.name)

                PartyFormationRow(
                    equipment = state.knightEquipment,
                    partyMonsters = state.partyMonsters,
                    dragState = dragState,
                    onPlace = viewModel::placeInParty,
                    onSwap = viewModel::swapPartySlots
                )

                state.message?.let { message ->
                    Text(
                        text = message,
                        color = TextGold,
                        fontFamily = FontFirst(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = onStartBattle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB4451F),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Do walki",
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                CompanionInventory(
                    monsters = state.companionInventory,
                    dragState = dragState,
                    onRemove = viewModel::removeFromParty
                )
            }
        }
    }
}

@Composable
private fun PartyHeader(name: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "Druzyna",
            color = TextGold,
            fontFamily = FontPixel(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ustaw ${name.ifBlank { "bohatera" }} i dwoch kompanow, potem rusz do walki.",
            color = TextMuted,
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun PartyFormationRow(
    equipment: KnightEquipment,
    partyMonsters: List<MonsterCard?>,
    dragState: DragAndDropState<PartyDrag>,
    onPlace: (String, Int) -> Unit,
    onSwap: (Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelDark)
            .border(2.dp, GoldDim, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HeroSlot(equipment = equipment, modifier = Modifier.weight(1f))
            repeat(partyMonsters.size) { index ->
                CompanionSlot(
                    index = index,
                    monster = partyMonsters[index],
                    dragState = dragState,
                    onPlace = onPlace,
                    onSwap = onSwap,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeroSlot(
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
                        colors = listOf(HeroGlow.copy(alpha = 0.22f), Color(0xFF0E0A08))
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
            LockBadge(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
        }
        Text(
            text = "Bohater",
            color = HeroGlow,
            fontFamily = FontFirst(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LockBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xCC0E0A08))
            .border(1.dp, HeroGlow.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(text = "🔒", fontSize = 9.sp)
    }
}

@Composable
private fun CompanionSlot(
    index: Int,
    monster: MonsterCard?,
    dragState: DragAndDropState<PartyDrag>,
    onPlace: (String, Int) -> Unit,
    onSwap: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val key = slotKey(index)
    val hovered = dragState.hoveredTargetKey == key

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
                .dropTarget(
                    state = dragState,
                    key = key,
                    canAccept = { it != null },
                    onDrop = { drag ->
                        when (drag) {
                            is PartyDrag.FromInventory -> onPlace(drag.monsterId, index)
                            is PartyDrag.FromSlot -> onSwap(drag.slotIndex, index)
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (monster == null) {
                EmptyCompanionSlot(highlighted = hovered)
            } else {
                MonsterCardView(
                    card = monster,
                    portraitHeight = 72,
                    modifier = Modifier
                        .fillMaxWidth()
                        .dragSource(
                            state = dragState,
                            dataProvider = { PartyDrag.FromSlot(index, monster.id) },
                            preview = {
                                MonsterCardView(card = monster, portraitHeight = 72)
                            }
                        )
                )
            }
        }
        Text(
            text = if (monster == null) "Kompan ${index + 1}" else monster.name,
            color = if (monster == null) TextMuted else TextGold,
            fontFamily = FontFirst(),
            fontSize = 11.sp,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyCompanionSlot(highlighted: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(if (highlighted) Gold.copy(alpha = 0.15f) else SlotEmpty)
            .border(
                width = if (highlighted) 2.dp else 1.5.dp,
                color = if (highlighted) TextGold else GoldDim.copy(alpha = 0.7f),
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "＋", color = if (highlighted) TextGold else GoldDim, fontSize = 26.sp)
            Text(
                text = "Upusc\npotwora",
                color = if (highlighted) TextGold else TextMuted,
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompanionInventory(
    monsters: List<MonsterCard>,
    dragState: DragAndDropState<PartyDrag>,
    onRemove: (Int) -> Unit
) {
    val hovered = dragState.hoveredTargetKey == INVENTORY_KEY

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (hovered) PanelMid else PanelDark)
            .border(
                width = if (hovered) 2.dp else 1.dp,
                color = if (hovered) TextGold else GoldDim,
                shape = RoundedCornerShape(12.dp)
            )
            .dropTarget(
                state = dragState,
                key = INVENTORY_KEY,
                canAccept = { it is PartyDrag.FromSlot },
                onDrop = { drag ->
                    if (drag is PartyDrag.FromSlot) {
                        onRemove(drag.slotIndex)
                    }
                }
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kompani",
                color = TextGold,
                fontFamily = FontPixel(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${monsters.size} dostepnych",
                color = TextMuted,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }

        if (monsters.isEmpty()) {
            Text(
                text = "Brak potworow. Kup ich w sklepie, aby wzmocnic druzyne.",
                color = TextMuted,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                monsters.forEach { card ->
                    MonsterCardView(
                        card = card,
                        portraitHeight = 84,
                        modifier = Modifier
                            .width(108.dp)
                            .dragSource(
                                state = dragState,
                                dataProvider = { PartyDrag.FromInventory(card.id) },
                                preview = { MonsterCardView(card = card, portraitHeight = 84, modifier = Modifier.width(108.dp)) }
                            )
                    )
                }
            }
        }
    }
}
