package com.lernhero.home.shop

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
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.spell.SpellCatalog
import com.lernhero.domain.spell.Spell
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.domain.monster.MonsterCatalog
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.ui.MonsterCardView
import com.lernhero.home.ui.RarityPalette
import com.lernhero.home.ui.icon
import com.lernhero.home.ui.label
import com.lernhero.home.ui.shortLabel
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

private val ShopBg = AppPalette.Ink
private val ShopPanel = AppPalette.Panel
private val ShopPanelMid = AppPalette.PanelRaised
private val ShopBorder = AppPalette.GoldDim
private val ShopGoldFrame = AppPalette.GoldFrame
private val ShopGold = AppPalette.Gold
private val ShopMuted = AppPalette.TextMuted
private val ShopBuy = AppPalette.Action

private enum class ShopTab(val label: String) {
    Equipment("Ekwipunek"),
    Spells("Zaklecia"),
    Monsters("Potwory")
}

@Composable
fun ShopScreen() {
    val viewModel = koinViewModel<PlayerViewModel>()
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(ShopTab.Equipment) }

    Surface(modifier = Modifier.fillMaxSize(), color = AppPalette.Background) {
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

                ShopTab.Spells -> SpellShop(
                    ownedSpells = state.ownedSpells,
                    money = state.money,
                    onBuy = viewModel::buySpell
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

            // Dev/testing: dyskretny link, nie prawdziwy przycisk zakupu - inaczej wyglada
            // jak zepsuta platnosc w grze zamiast narzedzia deweloperskiego.
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAddGold
                    )
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "dev: +5000",
                    color = ShopMuted.copy(alpha = 0.5f),
                    fontFamily = FontFirst(),
                    fontSize = 9.sp
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
private fun SpellShop(
    ownedSpells: List<String>,
    money: Int,
    onBuy: (Spell) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Zaklecia przypiszesz do paska zdolnosci w ekwipunku.",
            color = ShopMuted,
            fontFamily = FontFirst(),
            fontSize = 11.sp
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SpellCatalog.all.forEach { spell ->
                SpellShopCard(
                    spell = spell,
                    owned = spell.id in ownedSpells,
                    canBuy = money >= spell.cost && spell.id !in ownedSpells,
                    onBuy = { onBuy(spell) },
                    modifier = Modifier.width(164.dp)
                )
            }
        }
    }
}

@Composable
private fun SpellShopCard(
    spell: Spell,
    owned: Boolean,
    canBuy: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ShopPanel)
            .border(1.5.dp, RarityPalette.frame(spell.rarity), RoundedCornerShape(10.dp))
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
            Text(text = spell.icon(), fontSize = 28.sp)
        }
        Text(
            text = spell.name,
            color = ShopGold,
            fontFamily = FontPixel(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = "Zaklecie  -  ${RarityPalette.label(spell.rarity)}",
            color = RarityPalette.frame(spell.rarity),
            fontFamily = FontFirst(),
            fontSize = 10.sp
        )
        Text(
            text = spell.description,
            color = Color(0xFFCDBB99),
            fontFamily = FontFirst(),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 4
        )
        BuyButton(
            cost = spell.cost,
            owned = owned,
            canBuy = canBuy,
            onBuy = onBuy,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EquipmentShop(
    ownedItems: List<String>,
    money: Int,
    onBuy: (Item) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ItemCatalog.all.forEach { item ->
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
    item: Item,
    owned: Boolean,
    canBuy: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ShopPanel)
            .border(1.5.dp, RarityPalette.frame(item.rarity), RoundedCornerShape(10.dp))
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
            Text(text = item.icon(), fontSize = 28.sp)
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
            text = "${item.slot.label()}  -  ${RarityPalette.label(item.rarity)}",
            color = RarityPalette.frame(item.rarity),
            fontFamily = FontFirst(),
            fontSize = 10.sp
        )
        Text(
            text = item.description,
            color = Color(0xFFCDBB99),
            fontFamily = FontFirst(),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 3
        )
        item.effects.forEach { effect ->
            Text(
                text = "- ${effect.description}",
                color = Color(0xFF7FD1A0),
                fontFamily = FontFirst(),
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
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
