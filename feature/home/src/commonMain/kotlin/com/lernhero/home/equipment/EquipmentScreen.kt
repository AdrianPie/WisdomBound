package com.lernhero.home.equipment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.toLoadout
import com.lernhero.domain.player.PlayerEquipment
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.ui.AbilityBar
import com.lernhero.home.ui.RarityPalette
import com.lernhero.home.ui.icon
import com.lernhero.home.ui.label
import com.lernhero.home.ui.shortLabel
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.koin.compose.viewmodel.koinViewModel

/* ---------------------------------------------------------------------------------------------
 *  Ta sama paleta co reszta gry (AppPalette) - lokalne aliasy, zeby nazwy zostaly krotkie.
 * ------------------------------------------------------------------------------------------- */
private val Bg = AppPalette.Background
private val Panel = AppPalette.Panel
private val PanelRaised = AppPalette.PanelRaised
private val SlotBg = AppPalette.Ink
private val Gold = AppPalette.Gold
private val GoldDim = AppPalette.GoldDim
private val GoldFrame = AppPalette.GoldFrame
private val TextBody = AppPalette.TextBody
private val Muted = AppPalette.TextMuted
private val Success = AppPalette.Success

private val SLOT = 60.dp
private val BACKPACK_ICON = 64.dp

/** Rozmiar rycerza w podgladzie - dopasowany do wysokosci [Viewport], zeby nic sie nie obcinalo. */
private val KNIGHT_SIZE = 232.dp
private val VIEWPORT_HEIGHT = 250.dp

private val LEFT_SLOTS = listOf(ItemSlot.Head, ItemSlot.Chest, ItemSlot.Amulet)
private val RIGHT_SLOTS = listOf(ItemSlot.Hands, ItemSlot.Legs, ItemSlot.Feet)
private val BOTTOM_SLOTS = listOf(ItemSlot.MainHand, ItemSlot.OffHand, ItemSlot.Ring)

@Composable
fun EquipmentScreen() {
    val viewModel = koinViewModel<PlayerViewModel>()
    val state by viewModel.uiState.collectAsState()

    /** Slot, dla ktorego otwarto liste posiadanych przedmiotow. */
    var pickerSlot by remember { mutableStateOf<ItemSlot?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppPalette.ScreenGradient)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TitleBar(name = state.name, money = state.money)

            PaperDoll(
                knight = state.knightEquipment,
                itemAt = state::itemIn,
                onSlotClick = { slot ->
                    if (state.itemIn(slot) != null) viewModel.unequipSlot(slot) else pickerSlot = slot
                }
            )

            Backpack(items = state.backpack, onEquip = viewModel::equipItem)

            AbilityBar(
                ability = state.ability,
                ownedSpells = state.spells,
                onSet = viewModel::setSpellPercent
            )

            StatsPanel(equipment = state.equipment)
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
private fun TitleBar(name: String, money: Int) {
    OrnateFrame(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SlotBg)
                    .border(2.dp, Gold, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚔️", fontSize = 20.sp)
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
                    text = "Rycerz",
                    color = TextBody.copy(alpha = 0.7f),
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
            }
            Pill(text = "💰 $money")
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
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SlotColumn(LEFT_SLOTS, itemAt, onSlotClick)
                Box(modifier = Modifier.weight(1f)) { Viewport(knight) }
                SlotColumn(RIGHT_SLOTS, itemAt, onSlotClick)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally)
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        slots.forEach { slot ->
            GearSlot(slot = slot, item = itemAt(slot), onClick = { onSlotClick(slot) })
        }
    }
}

/**
 * Rycerz renderuje sie na stalym [KNIGHT_SIZE] (kwadrat), a viewport ma [VIEWPORT_HEIGHT] wieksza
 * niz ten rozmiar - wczesniej bylo odwrotnie (rycerz 300dp w ramce 214dp) i glowa znikala za
 * gorna krawedzia karty. Teraz nic nie ma prawa sie obciac.
 */
@Composable
private fun Viewport(knight: KnightEquipment) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(VIEWPORT_HEIGHT)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF3D2E15), Color(0xFF1A130B), Color(0xFF0A0705))
                )
            )
            .border(1.5.dp, GoldDim, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        ModularKnight(
            modifier = Modifier.align(Alignment.BottomCenter),
            equipment = knight,
            sizeDp = KNIGHT_SIZE,
            interactive = true
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xAA000000))))
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
                .clip(RoundedCornerShape(10.dp))
                .then(
                    if (item != null) {
                        Modifier.background(RarityPalette.cardBackground(item.rarity))
                    } else {
                        Modifier.background(SlotBg)
                    }
                )
                .border(if (equipped) 2.dp else 1.dp, border, RoundedCornerShape(10.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (item != null) {
                Text(text = item.icon(), fontSize = 24.sp)
            } else {
                Text(text = "+", color = GoldDim, fontFamily = FontPixel(), fontSize = 22.sp)
            }
        }
    }
}

@Composable
private fun SlotShell(label: String, labelStrong: Boolean, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        content()
        Text(
            text = label,
            color = if (labelStrong) Gold else Muted,
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(SLOT + 14.dp)
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
            modifier = Modifier.padding(12.dp),
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
                Text("dotknij, zeby zalozyc", color = Muted, fontFamily = FontFirst(), fontSize = 10.sp)
            }

            if (items.isEmpty()) {
                Text(
                    "Plecak pusty. Zajrzyj do sklepu po nowy sprzet.",
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
            .width(BACKPACK_ICON + 10.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(BACKPACK_ICON)
                .clip(RoundedCornerShape(10.dp))
                .background(RarityPalette.cardBackground(item.rarity))
                .border(2.dp, RarityPalette.frame(item.rarity), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = item.icon(), fontSize = 26.sp)
        }
        Text(
            text = item.name,
            color = TextBody,
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/* ------------------------------------------- Picker ----------------------------------------- */

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
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Panel)
                                    .border(2.dp, RarityPalette.frame(item.rarity), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item.icon(), fontSize = 20.sp)
                            }
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
                                        color = Success,
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

/** Etykieta po polsku dla kazdej statystyki - w kolejnosci, w jakiej maja sie pokazywac. */
private val STAT_ORDER = listOf(
    StatType.MaxHp, StatType.Attack, StatType.Defense, StatType.Speed,
    StatType.CritChance, StatType.CritDamage, StatType.Lifesteal, StatType.Dodge,
    StatType.DamageReduction, StatType.SpellPower, StatType.HealPower,
    StatType.CooldownReduction, StatType.AttackTargets
)

private fun StatType.label(): String = when (this) {
    StatType.MaxHp -> "Zdrowie"
    StatType.Attack -> "Atak"
    StatType.Defense -> "Obrona"
    StatType.Speed -> "Szybkosc"
    StatType.CritChance -> "Szansa na kryta"
    StatType.CritDamage -> "Sila kryta"
    StatType.Dodge -> "Unik"
    StatType.Lifesteal -> "Kradziez zycia"
    StatType.DamageReduction -> "Redukcja obrazen"
    StatType.HealPower -> "Sila leczenia"
    StatType.CooldownReduction -> "Skrocenie odnowienia"
    StatType.AttackTargets -> "Dodatkowe cele"
    StatType.SpellPower -> "Sila zaklec"
}

private fun StatType.isPercent(): Boolean = this in setOf(
    StatType.CritChance, StatType.CritDamage, StatType.Dodge, StatType.Lifesteal,
    StatType.DamageReduction, StatType.HealPower, StatType.CooldownReduction, StatType.SpellPower
)

/** Prawdziwe bonusy z zalozonego ekwipunku - zadnych zmyslonych liczb, tylko to, co faktycznie dziala. */
@Composable
private fun StatsPanel(equipment: PlayerEquipment) {
    val stats = remember(equipment) { equipment.toLoadout().stats }
    val nonZero = STAT_ORDER.mapNotNull { type ->
        val value = stats[type]
        if (value != 0f) type to value else null
    }

    OrnateFrame(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Bonusy z ekwipunku",
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            if (nonZero.isEmpty()) {
                Text(
                    text = "Nic jeszcze nie zalozyles - sloty czekaja.",
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
            } else {
                StatGrid(nonZero)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatGrid(entries: List<Pair<StatType, Float>>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        entries.forEach { (type, value) ->
            val display = if (type.isPercent()) "+${(value * 100).toInt()}%" else "+${value.toInt()}"
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlotBg)
                    .border(1.dp, GoldDim, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = display, color = Success, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = type.label(), color = Muted, fontFamily = FontFirst(), fontSize = 10.sp)
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
            .clip(RoundedCornerShape(14.dp))
            .background(AppPalette.CardGradient)
            .border(2.dp, GoldDim, RoundedCornerShape(14.dp))
    ) {
        content()
    }
}

@Composable
private fun Pill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SlotBg)
            .border(1.dp, GoldFrame, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = Gold, fontFamily = FontFirst(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HintBar(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PanelRaised)
            .border(1.dp, GoldDim, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(message, color = Gold, fontFamily = FontFirst(), style = MaterialTheme.typography.labelMedium)
    }
}
