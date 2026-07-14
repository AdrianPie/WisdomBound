package com.lernhero.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.data.PlayerRepository
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.shared.FontFirst
import com.lernhero.shared.FontPixel
import com.lernhero.shared.Resources
import com.lernhero.shared.Surface as AppSurface
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

/* ---------------------------------------------------------------------------------------------
 *  Fresh RPG palette - warm obsidian + bright gold, high contrast so slots/items pop.
 * ------------------------------------------------------------------------------------------- */
private val Obsidian = Color(0xFF0F0C09)
private val Panel = Color(0xFF211A12)
private val PanelHi = Color(0xFF31261A)
private val SlotBg = Color(0xFF0B0806)
private val Gold = Color(0xFFE8C15A)
private val GoldDim = Color(0xFF6E5730)
private val Bronze = Color(0xFF463A24)
private val Parchment = Color(0xFFEDDCB6)
private val Muted = Color(0xFF9C8968)
private val Emerald = Color(0xFF62D98A)

private val SLOT = 56.dp
private val BACKPACK_ICON = 60.dp

/** Sloty po lewej i prawej stronie postaci - uklad jak w klasycznym oknie postaci z MMO. */
private val LEFT_SLOTS = listOf(ItemSlot.Head, ItemSlot.Chest, ItemSlot.Amulet)
private val RIGHT_SLOTS = listOf(ItemSlot.Hands, ItemSlot.Legs, ItemSlot.Feet)
private val BOTTOM_SLOTS = listOf(ItemSlot.MainHand, ItemSlot.OffHand, ItemSlot.Ring)

@Composable
fun EquipmentScreen() {
    val playerRepository = koinInject<PlayerRepository>()
    val viewModel = remember(playerRepository) { PlayerViewModel(playerRepository) }
    val state by viewModel.uiState.collectAsState()

    /** Slot, dla ktorego otwarto liste posiadanych przedmiotow. */
    var pickerSlot by remember { mutableStateOf<ItemSlot?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF070505), Obsidian)))
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TitleBar(
                name = state.name,
                money = state.money,
                equippedCount = state.equipment.equipped.size
            )

            PaperDoll(
                knight = state.knightEquipment,
                itemAt = state::itemIn,
                // Klikniecie zdejmuje to, co stoi w slocie; pusty slot otwiera liste do wyboru.
                onSlotClick = { slot ->
                    if (state.itemIn(slot) != null) viewModel.unequipSlot(slot) else pickerSlot = slot
                }
            )

            Backpack(
                items = state.backpack,
                onEquip = viewModel::equipItem
            )

            StatsPanel(equipped = state.equipment.equipped.keys.size)
            state.message?.let { HintBar(it) }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }

    pickerSlot?.let { slot ->
        ItemPickerDialog(
            slot = slot,
            items = state.ownedFor(slot),
            onDismiss = { pickerSlot = null },
            onPick = { item ->
                viewModel.equipItem(item)
                pickerSlot = null
            }
        )
    }
}

/* ------------------------------------------ Title ------------------------------------------ */

@Composable
private fun TitleBar(name: String, money: Int, equippedCount: Int) {
    OrnateFrame(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SlotBg)
                    .border(2.dp, Gold, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Resources.Image.knightAvatar),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(2.dp).clip(RoundedCornerShape(50)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.ifBlank { "Sir Lernhero" },
                    color = Gold,
                    fontFamily = FontPixel(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Poziom 60  -  Rycerz",
                    color = Parchment,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Pill(text = "iLvl ${equippedCount * 12}")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Image(painterResource(Resources.Icon.Dollar), null, modifier = Modifier.size(13.dp))
                    Text("$money", color = Gold, fontFamily = FontFirst(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/* ---------------------------------------- Paper doll --------------------------------------- */

@Composable
private fun PaperDoll(
    knight: KnightEquipment,
    itemAt: (ItemSlot) -> Item?,
    onSlotClick: (ItemSlot) -> Unit
) {
    OrnateFrame(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SlotColumn(LEFT_SLOTS, itemAt, onSlotClick)
                Box(modifier = Modifier.weight(1f)) { Viewport(knight) }
                SlotColumn(RIGHT_SLOTS, itemAt, onSlotClick)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                BOTTOM_SLOTS.forEach { slot ->
                    GearSlot(slot = slot, item = itemAt(slot), onClick = { onSlotClick(slot) })
                }
            }
        }
    }
}

@Composable
private fun SlotColumn(
    slots: List<ItemSlot>,
    itemAt: (ItemSlot) -> Item?,
    onSlotClick: (ItemSlot) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        slots.forEach { slot ->
            GearSlot(slot = slot, item = itemAt(slot), onClick = { onSlotClick(slot) })
        }
    }
}

@Composable
private fun Viewport(knight: KnightEquipment) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(214.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF52411F), Color(0xFF1C140C), Color(0xFF080605)),
                    radius = 480f
                )
            )
            .border(1.5.dp, GoldDim, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            ModularKnight(
                modifier = Modifier.align(Alignment.BottomCenter),
                equipment = knight,
                interactive = true
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x99000000))))
        )
    }
}

/**
 * Pojedynczy slot. Ramka bierze kolor z rzadkosci zalozonego przedmiotu, wiec legendarny miecz
 * widac z drugiego konca ekranu - i nie trzeba do tego zadnej wiedzy o samym mieczu.
 */
@Composable
private fun GearSlot(
    slot: ItemSlot,
    item: Item?,
    onClick: () -> Unit
) {
    val equipped = item != null
    val border = if (item != null) RarityPalette.frame(item.rarity) else GoldDim

    SlotShell(label = item?.name ?: slot.shortLabel(), labelStrong = equipped) {
        Box(
            modifier = Modifier
                .size(SLOT)
                .then(if (equipped) Modifier.shadow(6.dp, RoundedCornerShape(8.dp)) else Modifier)
                .clip(RoundedCornerShape(8.dp))
                .background(if (equipped) Color(0xFF3A2C18) else SlotBg)
                .border(if (equipped) 2.dp else 1.dp, border, RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (item != null) {
                Image(
                    painter = painterResource(item.icon()),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(text = "+", color = GoldDim, fontFamily = FontPixel(), fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun SlotShell(label: String, labelStrong: Boolean, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        content()
        Text(
            text = label,
            color = if (labelStrong) Gold else Muted,
            fontFamily = FontFirst(),
            fontSize = 8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(SLOT + 8.dp)
        )
    }
}

@Composable
private fun GearIcon(icon: DrawableResource, frame: Color, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF3A2C18))
            .border(2.dp, frame, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().padding(6.dp),
            contentScale = ContentScale.Fit
        )
    }
}

/* ------------------------------------------ Backpack --------------------------------------- */

@Composable
private fun Backpack(
    items: List<Item>,
    onEquip: (Item) -> Unit
) {
    OrnateFrame(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plecak",
                    color = Gold,
                    fontFamily = FontPixel(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("kliknij, zeby zalozyc", color = Muted, fontFamily = FontFirst(), fontSize = 10.sp)
            }

            if (items.isEmpty()) {
                Text(
                    "Plecak pusty. Kup ekwipunek w sklepie.",
                    color = Muted,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items.forEach { item ->
                        BackpackItem(item = item, onClick = { onEquip(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun BackpackItem(item: Item, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(BACKPACK_ICON + 8.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        GearIcon(icon = item.icon(), frame = RarityPalette.frame(item.rarity), size = BACKPACK_ICON)
        Text(
            text = item.name,
            color = Parchment,
            fontFamily = FontFirst(),
            fontSize = 8.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/* ------------------------------------------ Picker ----------------------------------------- */

/**
 * Lista posiadanych przedmiotow pasujacych do slotu. Pokazuje efekty kazdego z nich, bo to one -
 * a nie suche liczby - decyduja, co warto zalozyc.
 */
@Composable
private fun ItemPickerDialog(
    slot: ItemSlot,
    items: List<Item>,
    onDismiss: () -> Unit,
    onPick: (Item) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = {
            Text(
                text = slot.label(),
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (items.isEmpty()) {
                Text(
                    text = "Nie masz nic na to miejsce. Zajrzyj do sklepu.",
                    color = Muted,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlotBg)
                                .border(1.dp, RarityPalette.frame(item.rarity), RoundedCornerShape(8.dp))
                                .clickable { onPick(item) }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GearIcon(
                                icon = item.icon(),
                                frame = RarityPalette.frame(item.rarity),
                                size = 40.dp
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = item.name,
                                    color = RarityPalette.frame(item.rarity),
                                    fontFamily = FontFirst(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                item.effects.forEach { effect ->
                                    Text(
                                        text = "- ${effect.description}",
                                        color = Emerald,
                                        fontFamily = FontFirst(),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Zamknij", color = Gold, fontFamily = FontFirst())
            }
        }
    )
}

/* ------------------------------------------- Stats ----------------------------------------- */

@Composable
private fun StatsPanel(equipped: Int) {
    OrnateFrame(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                header = "Magia",
                modifier = Modifier.weight(1f),
                rows = listOf(
                    StatRow("Moc zaklec", "${172 + equipped * 8}", false),
                    StatRow("Leczenie", "${172 + equipped * 6}", false),
                    StatRow("Regen many", "${11 + equipped}", false)
                )
            )
            StatCard(
                header = "Walka",
                modifier = Modifier.weight(1f),
                rows = listOf(
                    StatRow("Obrazenia", "${120 + equipped * 8}-${180 + equipped * 12}", false),
                    StatRow("Moc ataku", "${100 + equipped * 12}", true),
                    StatRow("Szybkosc", "3.6", false),
                    StatRow("Kryt", "${7 + equipped}%", false)
                )
            )
        }
    }
}

private data class StatRow(val label: String, val value: String, val highlight: Boolean)

@Composable
private fun StatCard(header: String, rows: List<StatRow>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(PanelHi)
                .border(1.dp, GoldDim, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Text(header, color = Gold, fontFamily = FontFirst(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.label, color = Muted, fontFamily = FontFirst(), fontSize = 11.sp)
                Text(
                    text = row.value,
                    color = if (row.highlight) Emerald else Parchment,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/* ------------------------------------------ Shared ----------------------------------------- */

@Composable
private fun OrnateFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(Panel, Obsidian)))
            .border(2.dp, GoldDim, RoundedCornerShape(12.dp))
            .padding(2.dp)
            .border(1.dp, Bronze, RoundedCornerShape(10.dp))
    ) {
        content()
    }
}

@Composable
private fun Pill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SlotBg)
            .border(1.dp, Gold, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = Gold, fontFamily = FontFirst(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HintBar(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PanelHi)
            .border(1.dp, GoldDim, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(message, color = Gold, fontFamily = FontFirst(), style = MaterialTheme.typography.labelMedium)
    }
}
