package com.lernhero.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.KnightEquipmentSlot
import com.lernhero.game.character.ModularKnight
import com.lernhero.shared.FontFirst
import com.lernhero.shared.FontPixel
import com.lernhero.shared.Resources
import com.lernhero.shared.Surface as AppSurface
import com.lernhero.shared.TextPrimary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val WowPanelDark = Color(0xFF1A1410)
private val WowPanelMid = Color(0xFF2A2218)
private val WowGold = Color(0xFFC8A45A)
private val WowGoldDim = Color(0xFF6B5A38)
private val WowSlotEmpty = Color(0xFF120E0A)
private val WowSlotEquipped = Color(0xFF3D3224)
private val WowTextGold = Color(0xFFFFD100)
private val WowTextMuted = Color(0xFF9D8B6A)

@Composable
fun EquipmentScreen() {
    var equipment by remember { mutableStateOf(KnightEquipment.Empty) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CharacterPaperDollHeader(equipment = equipment)

            WowCharacterPanel(
                equipment = equipment,
                onSlotClick = { slot ->
                    if (slot.isEquippable) {
                        equipment = slot.toggle(equipment)
                    }
                }
            )

            EquipmentStatsPanel(equipment = equipment)
        }
    }
}

@Composable
private fun CharacterPaperDollHeader(equipment: KnightEquipment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(WowPanelDark)
            .border(1.dp, WowGoldDim, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, WowGold, RoundedCornerShape(4.dp))
                .background(WowPanelMid),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Resources.Image.knightAvatar),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(3.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Sir Lernhero",
                color = WowTextGold,
                fontFamily = FontPixel(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Level 4 Knight",
                color = WowTextMuted,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Item Level",
                color = WowTextMuted,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = equippedItemCount(equipment).toString(),
                color = WowTextGold,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WowCharacterPanel(
    equipment: KnightEquipment,
    onSlotClick: (KnightEquipmentSlot) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(WowPanelDark)
            .border(2.dp, WowGoldDim, RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EquipmentSlotColumn(
                slots = listOf(
                    KnightEquipmentSlot.Head,
                    KnightEquipmentSlot.Neck,
                    KnightEquipmentSlot.Shoulder,
                    KnightEquipmentSlot.Back,
                    KnightEquipmentSlot.Chest,
                    KnightEquipmentSlot.Hands,
                    KnightEquipmentSlot.Waist,
                    KnightEquipmentSlot.Legs,
                    KnightEquipmentSlot.Feet,
                ),
                equipment = equipment,
                onSlotClick = onSlotClick
            )

            Box(modifier = Modifier.weight(1f)) {
                CharacterModelPane(equipment = equipment)
            }

            EquipmentSlotColumn(
                slots = listOf(
                    KnightEquipmentSlot.MainHand,
                    KnightEquipmentSlot.OffHand,
                ),
                equipment = equipment,
                onSlotClick = onSlotClick
            )
        }
    }
}

@Composable
private fun EquipmentSlotColumn(
    slots: List<KnightEquipmentSlot>,
    equipment: KnightEquipment,
    onSlotClick: (KnightEquipmentSlot) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        slots.forEach { slot ->
            EquipmentSlot(
                slot = slot,
                equipped = slot.isEquipped(equipment),
                icon = slotIcon(slot),
                onClick = { onSlotClick(slot) }
            )
        }
    }
}

@Composable
private fun CharacterModelPane(equipment: KnightEquipment) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = "Character",
            color = WowTextMuted,
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp
        )
        Box(
            modifier = Modifier
                .width(200.dp)
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3A3028),
                            Color(0xFF1E1812),
                            Color(0xFF0E0A08)
                        )
                    )
                )
                .border(1.dp, WowGoldDim, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(1f)
                    .align(Alignment.BottomCenter),
                contentAlignment = Alignment.BottomCenter
            ) {
                ModularKnight(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    equipment = equipment
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x66000000)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun EquipmentSlot(
    slot: KnightEquipmentSlot,
    equipped: Boolean,
    icon: DrawableResource?,
    onClick: () -> Unit,
) {
    val borderColor = when {
        equipped -> WowGold
        slot.isEquippable -> WowGoldDim
        else -> Color(0xFF3A3028)
    }
    val backgroundColor = when {
        equipped -> WowSlotEquipped
        slot.isEquippable -> WowSlotEmpty
        else -> Color(0xFF0E0C0A)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor)
                .border(1.5.dp, borderColor, RoundedCornerShape(4.dp))
                .then(
                    if (slot.isEquippable) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClick
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (equipped && icon != null) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = slot.label,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            } else if (!slot.isEquippable) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(1.dp, Color(0xFF2A2218), RoundedCornerShape(2.dp))
                )
            }
        }
        Text(
            text = slot.shortLabel,
            color = if (equipped) WowTextGold else WowTextMuted,
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp)
        )
    }
}

@Composable
private fun EquipmentStatsPanel(equipment: KnightEquipment) {
    val attackBonus = listOf(equipment.mainHand, equipment.hands).count { it } * 3
    val defenseBonus = listOf(
        equipment.head,
        equipment.hands,
        equipment.legs,
        equipment.feet
    ).count { it } * 2

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(WowPanelDark)
            .border(1.dp, WowGoldDim, RoundedCornerShape(4.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Character Stats",
            color = WowTextGold,
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        StatRow(label = "Attack", base = 12, bonus = attackBonus, color = Color(0xFFF97316))
        StatRow(label = "Defense", base = 8, bonus = defenseBonus, color = Color(0xFF14B8A6))
        StatRow(label = "HP", base = 50, bonus = defenseBonus * 5, color = Color(0xFFEF4444))
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Tap equippable slots to toggle plate armor pieces.",
            color = WowTextMuted,
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    base: Int,
    bonus: Int,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = WowTextMuted,
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.bodySmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = base.toString(),
                color = TextPrimary,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            if (bonus > 0) {
                Text(
                    text = "+$bonus",
                    color = color,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun slotIcon(slot: KnightEquipmentSlot): DrawableResource? = when (slot) {
    KnightEquipmentSlot.Head -> Resources.PlateArmor.head
    KnightEquipmentSlot.Hands -> Resources.PlateArmor.leftDownHand
    KnightEquipmentSlot.Legs -> Resources.PlateArmor.leftUpLeg
    KnightEquipmentSlot.Feet -> Resources.PlateArmor.leftFoot
    KnightEquipmentSlot.MainHand -> Resources.PlateArmor.swordFront
    else -> null
}

private fun equippedItemCount(equipment: KnightEquipment): Int = listOf(
    equipment.head,
    equipment.hands,
    equipment.legs,
    equipment.feet,
    equipment.mainHand,
).count { it }
