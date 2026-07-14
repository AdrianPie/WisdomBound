package com.lernhero.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.lernhero.domain.shop.ShopCatalog
import com.lernhero.domain.shop.ShopItem
import com.lernhero.domain.shop.ShopItemSlot
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.KnightEquipmentSlot
import com.lernhero.game.character.ModularKnight
import com.lernhero.home.dnd.DragAndDropContainer
import com.lernhero.home.dnd.DragAndDropState
import com.lernhero.home.dnd.dragSource
import com.lernhero.home.dnd.dropTarget
import com.lernhero.home.dnd.rememberDragAndDropState
import com.lernhero.shared.FontFirst
import com.lernhero.shared.FontPixel
import com.lernhero.shared.Resources
import com.lernhero.shared.Surface as AppSurface
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

/* ---------------------------------------------------------------------------------------------
 *  Fresh RPG palette – warm obsidian + bright gold, high contrast so slots/items pop.
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
private val Accept = Color(0xFF6FE07A)
private val Emerald = Color(0xFF62D98A)
private val Ruby = Color(0xFFE0654F)

private const val EQUIP_INVENTORY_KEY = "equip_inventory"
private val SLOT = 56.dp
private val BACKPACK_ICON = 60.dp

/* A slot on the paper doll: a real gear slot or a cosmetic placeholder. */
private sealed interface PaperSlot {
    data class Gear(val slot: KnightEquipmentSlot) : PaperSlot
    data class Deco(val label: String) : PaperSlot
}

private val LEFT_SLOTS = listOf(
    PaperSlot.Gear(KnightEquipmentSlot.Head),
    PaperSlot.Deco("Napiersnik"),
    PaperSlot.Deco("Ramie"),
)

private val RIGHT_SLOTS = listOf(
    PaperSlot.Gear(KnightEquipmentSlot.Hands),
    PaperSlot.Gear(KnightEquipmentSlot.Legs),
    PaperSlot.Gear(KnightEquipmentSlot.Feet),
)

private val WEAPON_SLOTS = listOf(
    PaperSlot.Gear(KnightEquipmentSlot.MainHand),
    PaperSlot.Deco("Tarcza"),
)

private sealed interface EquipDrag {
    val item: ShopItem
    data class FromInventory(override val item: ShopItem) : EquipDrag
    data class FromSlot(val slot: KnightEquipmentSlot, override val item: ShopItem) : EquipDrag
}

@Composable
fun EquipmentScreen() {
    val playerRepository = koinInject<PlayerRepository>()
    val viewModel = remember(playerRepository) { PlayerViewModel(playerRepository) }
    val state by viewModel.uiState.collectAsState()
    val equipment = state.knightEquipment
    val dragState = rememberDragAndDropState<EquipDrag>()

    Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {
        DragAndDropContainer(state = dragState, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF070505), Obsidian)))
                    .verticalScroll(rememberScrollState())
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TitleBar(equipment = equipment, name = state.name, money = state.money)
                PaperDoll(equipment = equipment, dragState = dragState, onEquip = viewModel::equipSlot)
                Backpack(
                    ownedEquipment = state.ownedEquipment,
                    equipment = equipment,
                    dragState = dragState,
                    onUnequip = viewModel::unequipSlot
                )
                StatsPanel(equipment = equipment)
                state.message?.let { HintBar(it) }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/* ------------------------------------------ Title ------------------------------------------ */

@Composable
private fun TitleBar(equipment: KnightEquipment, name: String, money: Int) {
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
                    text = "Poziom 60  •  Rycerz",
                    color = Parchment,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Pill(text = "iLvl ${equippedCount(equipment) * 12}", strong = true)
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
    equipment: KnightEquipment,
    dragState: DragAndDropState<EquipDrag>,
    onEquip: (KnightEquipmentSlot) -> Unit
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
                SlotColumn(LEFT_SLOTS, equipment, dragState, onEquip)
                Box(modifier = Modifier.weight(1f)) { Viewport(equipment) }
                SlotColumn(RIGHT_SLOTS, equipment, dragState, onEquip)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                WEAPON_SLOTS.forEach { Slot(it, equipment, dragState, onEquip) }
            }
        }
    }
}

@Composable
private fun SlotColumn(
    slots: List<PaperSlot>,
    equipment: KnightEquipment,
    dragState: DragAndDropState<EquipDrag>,
    onEquip: (KnightEquipmentSlot) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        slots.forEach { Slot(it, equipment, dragState, onEquip) }
    }
}

@Composable
private fun Viewport(equipment: KnightEquipment) {
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
                equipment = equipment,
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

@Composable
private fun Slot(
    paperSlot: PaperSlot,
    equipment: KnightEquipment,
    dragState: DragAndDropState<EquipDrag>,
    onEquip: (KnightEquipmentSlot) -> Unit
) {
    when (paperSlot) {
        is PaperSlot.Deco -> DecoSlot(paperSlot.label)
        is PaperSlot.Gear -> GearSlot(paperSlot.slot, paperSlot.slot.isEquipped(equipment), dragState, onEquip)
    }
}

@Composable
private fun DecoSlot(label: String) {
    SlotShell(label = label, labelStrong = false) {
        Box(
            modifier = Modifier
                .size(SLOT)
                .clip(RoundedCornerShape(8.dp))
                .background(SlotBg)
                .border(1.dp, GoldDim.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E1710))
                    .border(1.dp, GoldDim.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun GearSlot(
    slot: KnightEquipmentSlot,
    equipped: Boolean,
    dragState: DragAndDropState<EquipDrag>,
    onEquip: (KnightEquipmentSlot) -> Unit
) {
    val hovered = dragState.hoveredTargetKey == slot
    val shopItem = slot.toShopSlot()?.let { ShopCatalog.itemForSlot(it) }
    val icon = slotIcon(slot)

    val border = when {
        hovered -> Accept
        equipped -> Gold
        else -> GoldDim
    }
    val bg = when {
        hovered -> Accept.copy(alpha = 0.18f)
        equipped -> Color(0xFF3A2C18)
        else -> SlotBg
    }

    SlotShell(label = slot.shortLabel, labelStrong = equipped || hovered) {
        Box(
            modifier = Modifier
                .size(SLOT)
                .then(if (equipped) Modifier.shadow(6.dp, RoundedCornerShape(8.dp)) else Modifier)
                .clip(RoundedCornerShape(8.dp))
                .background(bg)
                .border(if (hovered || equipped) 2.dp else 1.dp, border, RoundedCornerShape(8.dp))
                .dropTarget(
                    state = dragState,
                    key = slot,
                    canAccept = { it != null && it.item.slot.toKnightSlot() == slot },
                    onDrop = { onEquip(slot) }
                )
                .then(
                    if (equipped && shopItem != null) {
                        Modifier.dragSource(
                            state = dragState,
                            dataProvider = { EquipDrag.FromSlot(slot, shopItem) },
                            preview = { GearIcon(icon, SLOT) }
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = slot.label,
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                    contentScale = ContentScale.Fit,
                    alpha = if (equipped) 1f else 0.15f
                )
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
private fun GearIcon(icon: DrawableResource?, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF3A2C18))
            .border(2.dp, Gold, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(6.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/* ------------------------------------------ Backpack --------------------------------------- */

@Composable
private fun Backpack(
    ownedEquipment: List<ShopItem>,
    equipment: KnightEquipment,
    dragState: DragAndDropState<EquipDrag>,
    onUnequip: (KnightEquipmentSlot) -> Unit
) {
    val available = ownedEquipment.filterNot { it.slot.toKnightSlot().isEquipped(equipment) }
    val hovered = dragState.hoveredTargetKey == EQUIP_INVENTORY_KEY

    OrnateFrame(
        modifier = Modifier.fillMaxWidth(),
        glow = hovered
    ) {
        Column(
            modifier = Modifier
                .dropTarget(
                    state = dragState,
                    key = EQUIP_INVENTORY_KEY,
                    canAccept = { it is EquipDrag.FromSlot },
                    onDrop = { drag -> if (drag is EquipDrag.FromSlot) onUnequip(drag.slot) }
                )
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Plecak", color = Gold, fontFamily = FontPixel(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("przeciagnij na postac →", color = Muted, fontFamily = FontFirst(), fontSize = 10.sp)
            }

            if (available.isEmpty()) {
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
                    available.forEach { item -> BackpackItem(item, dragState) }
                }
            }
        }
    }
}

@Composable
private fun BackpackItem(item: ShopItem, dragState: DragAndDropState<EquipDrag>) {
    Column(
        modifier = Modifier
            .width(BACKPACK_ICON + 8.dp)
            .dragSource(
                state = dragState,
                dataProvider = { EquipDrag.FromInventory(item) },
                preview = { GearIcon(itemIcon(item), BACKPACK_ICON) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        GearIcon(itemIcon(item), BACKPACK_ICON)
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

/* ------------------------------------------- Stats ----------------------------------------- */

@Composable
private fun StatsPanel(equipment: KnightEquipment) {
    val atk = listOf(equipment.mainHand, equipment.hands).count { it } * 3
    val def = listOf(equipment.head, equipment.hands, equipment.legs, equipment.feet).count { it } * 2

    OrnateFrame(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                header = "Magia",
                modifier = Modifier.weight(1f),
                rows = listOf(
                    StatRow("Moc zaklec", "${172 + def * 4}", false),
                    StatRow("Leczenie", "${172 + def * 3}", false),
                    StatRow("Regen many", "${11 + def}", false)
                )
            )
            StatCard(
                header = "Walka",
                modifier = Modifier.weight(1f),
                rows = listOf(
                    StatRow("Obrazenia", "${120 + atk * 8}-${180 + atk * 12}", false),
                    StatRow("Moc ataku", "${100 + atk * 12}", true),
                    StatRow("Szybkosc", "3.6", false),
                    StatRow("Kryt", "${7 + atk}%", false),
                    StatRow("Trafienie", "6%", false)
                )
            )
        }
    }
}

private data class StatRow(val label: String, val value: String, val highlight: Boolean)

@Composable
private fun StatCard(header: String, rows: List<StatRow>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        // WoW-style header "dropdown"
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
    glow: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(Panel, Obsidian)))
            .border(2.dp, if (glow) Gold else GoldDim, RoundedCornerShape(12.dp))
            .padding(2.dp)
            .border(1.dp, Bronze, RoundedCornerShape(10.dp))
    ) {
        content()
    }
}

@Composable
private fun Pill(text: String, strong: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SlotBg)
            .border(1.dp, if (strong) Gold else GoldDim, RoundedCornerShape(6.dp))
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

/* ------------------------------------------ Helpers ---------------------------------------- */

private fun slotIcon(slot: KnightEquipmentSlot): DrawableResource? = when (slot) {
    KnightEquipmentSlot.Head -> Resources.PlateArmor.head
    KnightEquipmentSlot.Hands -> Resources.PlateArmor.leftDownHand
    KnightEquipmentSlot.Legs -> Resources.PlateArmor.leftUpLeg
    KnightEquipmentSlot.Feet -> Resources.PlateArmor.leftFoot
    KnightEquipmentSlot.MainHand -> Resources.PlateArmor.swordFront
    else -> null
}

private fun itemIcon(item: ShopItem): DrawableResource = when (item.slot) {
    ShopItemSlot.Head -> Resources.PlateArmor.head
    ShopItemSlot.Hands -> Resources.PlateArmor.leftDownHand
    ShopItemSlot.Legs -> Resources.PlateArmor.leftUpLeg
    ShopItemSlot.Feet -> Resources.PlateArmor.leftFoot
    ShopItemSlot.MainHand -> Resources.PlateArmor.swordFront
}

private fun equippedCount(equipment: KnightEquipment): Int = listOf(
    equipment.head, equipment.hands, equipment.legs, equipment.feet, equipment.mainHand
).count { it }
