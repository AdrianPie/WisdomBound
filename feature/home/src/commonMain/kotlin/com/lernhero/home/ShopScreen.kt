package com.lernhero.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.data.PlayerRepository
import com.lernhero.domain.preset.MonsterCard
import com.lernhero.domain.preset.MonsterCatalog
import com.lernhero.domain.shop.ShopCatalog
import com.lernhero.domain.shop.ShopItem
import com.lernhero.domain.shop.ShopItemSlot
import com.lernhero.home.component.MonsterCardView
import com.lernhero.shared.FontFirst
import com.lernhero.shared.FontPixel
import com.lernhero.shared.Resources
import com.lernhero.shared.Surface as AppSurface
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

private val ShopBg = Color(0xFF17120D)
private val ShopPanel = Color(0xFF1A1410)
private val ShopPanelMid = Color(0xFF2A2218)
private val ShopBorder = Color(0xFF6B5A38)
private val ShopGoldFrame = Color(0xFFC8A45A)
private val ShopGold = Color(0xFFFFD100)
private val ShopMuted = Color(0xFF9D8B6A)
private val ShopBuy = Color(0xFFB4451F)

private enum class ShopTab(val label: String) {
    Equipment("Ekwipunek"),
    Monsters("Potwory")
}

@Composable
fun ShopScreen() {
    val playerRepository = koinInject<PlayerRepository>()
    val viewModel = remember(playerRepository) { PlayerViewModel(playerRepository) }
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(ShopTab.Equipment) }

    Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShopHeader(money = state.money, onAddGold = { viewModel.addGold(5000) })

            ShopTabs(selected = tab, onSelect = { tab = it })

            state.message?.let { message ->
                Text(
                    text = message,
                    color = ShopGold,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            when (tab) {
                ShopTab.Equipment -> EquipmentShop(
                    ownedItems = state.ownedItems,
                    money = state.money,
                    onBuy = viewModel::buyItem
                )

                ShopTab.Monsters -> MonsterShop(
                    money = state.money,
                    onBuy = viewModel::buyMonster
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ShopHeader(money: Int, onAddGold: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ShopPanel)
            .border(1.dp, ShopGoldFrame, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Sklep",
                color = ShopGold,
                fontFamily = FontPixel(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Wyposaz bohatera i zbierz kompanow",
                color = ShopMuted,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(ShopBg)
                    .border(1.dp, ShopGoldFrame, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter = painterResource(Resources.Icon.Dollar),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "$money",
                    color = ShopGold,
                    fontFamily = FontPixel(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Dev/testing: grant coins so the shop and party can be exercised.
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(ShopBuy)
                    .border(1.dp, ShopGold, RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAddGold
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+5000",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ShopTabs(selected: ShopTab, onSelect: (ShopTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShopTab.entries.forEach { entry ->
            val active = entry == selected
            val bg by animateColorAsState(if (active) ShopBuy else ShopPanel)
            val border by animateColorAsState(if (active) ShopGold else ShopBorder)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(entry) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.label,
                    color = if (active) Color.White else ShopMuted,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EquipmentShop(
    ownedItems: List<String>,
    money: Int,
    onBuy: (ShopItem) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ShopCatalog.items.forEach { item ->
            EquipmentShopCard(
                item = item,
                owned = item.id in ownedItems,
                canBuy = money >= item.cost && item.id !in ownedItems,
                onBuy = { onBuy(item) },
                modifier = Modifier.width(164.dp)
            )
        }
    }
}

@Composable
private fun EquipmentShopCard(
    item: ShopItem,
    owned: Boolean,
    canBuy: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ShopPanel)
            .border(1.5.dp, ShopGoldFrame, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ShopBg)
                .border(1.dp, ShopGoldFrame.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(item.icon()),
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
        }
        Text(
            text = item.name,
            color = ShopGold,
            fontFamily = FontPixel(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = item.slot.label(),
            color = ShopMuted,
            fontFamily = FontFirst(),
            fontSize = 10.sp
        )
        Text(
            text = item.description,
            color = Color(0xFFCDBB99),
            fontFamily = FontFirst(),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        BuyButton(
            cost = item.cost,
            owned = owned,
            canBuy = canBuy,
            onBuy = onBuy,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonsterShop(
    money: Int,
    onBuy: (MonsterCard) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MonsterCatalog.all.forEach { card ->
            MonsterCardView(
                card = card,
                portraitHeight = 96,
                showSkills = true,
                modifier = Modifier.width(164.dp)
            ) {
                BuyButton(
                    cost = card.cost,
                    owned = false,
                    canBuy = money >= card.cost,
                    onBuy = { onBuy(card) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BuyButton(
    cost: Int,
    owned: Boolean,
    canBuy: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pulse by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pulse) 0.92f else 1f)

    val bg = when {
        owned -> ShopPanelMid
        canBuy -> ShopBuy
        else -> ShopPanelMid
    }
    val content = when {
        owned -> ShopMuted
        canBuy -> Color.White
        else -> ShopMuted
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, if (canBuy) ShopGold else ShopBorder, RoundedCornerShape(8.dp))
            .clickable(
                enabled = canBuy && !owned,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                pulse = true
                onBuy()
            }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (owned) {
                Text(
                    text = "Posiadane",
                    color = content,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            } else {
                Image(
                    painter = painterResource(Resources.Icon.Dollar),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "$cost",
                    color = content,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }

    // Reset the press pulse shortly after it triggers.
    if (pulse) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(120)
            pulse = false
        }
    }
}

private fun ShopItem.icon(): DrawableResource = when (slot) {
    ShopItemSlot.Head -> Resources.PlateArmor.head
    ShopItemSlot.Hands -> Resources.PlateArmor.leftDownHand
    ShopItemSlot.Legs -> Resources.PlateArmor.leftUpLeg
    ShopItemSlot.Feet -> Resources.PlateArmor.leftFoot
    ShopItemSlot.MainHand -> Resources.PlateArmor.swordFront
}

private fun ShopItemSlot.label(): String = when (this) {
    ShopItemSlot.Head -> "Glowa"
    ShopItemSlot.Hands -> "Rece"
    ShopItemSlot.Legs -> "Nogi"
    ShopItemSlot.Feet -> "Stopy"
    ShopItemSlot.MainHand -> "Glowna reka"
}
