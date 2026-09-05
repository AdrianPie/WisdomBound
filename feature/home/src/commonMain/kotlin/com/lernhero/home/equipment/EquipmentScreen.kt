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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.toLoadout
import com.lernhero.domain.player.PlayerEquipment
import com.lernhero.game.character.HeroLook
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.home.player.PlayerViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.lernhero.domain.player.HeroCombatStats
import com.lernhero.domain.player.HeroStat
import com.lernhero.domain.player.PlayerProgress
import com.lernhero.domain.talent.TalentRules
import com.lernhero.home.ui.ItemDetailSheet
import com.lernhero.home.ui.format
import com.lernhero.home.ui.RarityPalette
import com.lernhero.home.ui.icon
import com.lernhero.home.ui.label
import com.lernhero.home.ui.shortLabel
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/* ---------------------------------------------------------------------------------------------
 *  Ta sama paleta co reszta gry (AppPalette) - lokalne aliasy, zeby nazwy zostaly krotkie.
 * ------------------------------------------------------------------------------------------- */
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

/**
 * Rozmiar rycerza w podgladzie - dopasowany do wysokosci [Viewport], zeby nic sie nie obcinalo.
 * Zmniejszony, gdy na ekran weszly poziom i statystyki bohatera: podglad byl najwiekszym elementem
 * ekranu, a niosl najmniej informacji, wiec to on oddal miejsce, a nie ekwipunek.
 */
private val KNIGHT_SIZE = 176.dp
private val VIEWPORT_HEIGHT = 190.dp

private val LEFT_SLOTS = listOf(ItemSlot.Head, ItemSlot.Chest, ItemSlot.Amulet)
private val RIGHT_SLOTS = listOf(ItemSlot.Hands, ItemSlot.Legs, ItemSlot.Feet)
private val BOTTOM_SLOTS = listOf(ItemSlot.MainHand, ItemSlot.OffHand, ItemSlot.Ring)

@Composable
fun EquipmentScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    onOpenTalents: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    /** Slot, dla ktorego otwarto liste posiadanych przedmiotow. */
    var pickerSlot by remember { mutableStateOf<ItemSlot?>(null) }

    /** Przedmiot ogladany w karcie szczegolow - patrz [ItemDetailSheet]. */
    var inspected by remember { mutableStateOf<Item?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Karta postaci nie jest juz zakladka - wchodzi sie na nia z naglowka na Dom, wiec
            // potrzebuje wlasnego powrotu.
            BackRow(onBack = onBack)

            // Gora ekranu: kim jestes, na jakim poziomie i co masz do rozdania. Wczesniej stal tu
            // sam pasek z imieniem i zlotem, a poziom mieszkal w OSOBNEJ zakladce - czyli karta
            // postaci byla rozbita na dwa ekrany pokazujace te sama postac.
            HeroHeader(
                name = state.name,
                money = state.money,
                progress = state.progress
            )

            HeroStatsRow(
                progress = state.progress,
                onSpend = viewModel::spendStatPoint
            )

            PaperDoll(
                knight = state.knightEquipment,
                look = state.heroLook,
                itemAt = state::itemIn,
                combatStats = state.combatStats,
                onSlotClick = { slot ->
                    // Tapniecie zajetego slotu POKAZUJE przedmiot, a nie zdejmuje go po cichu -
                    // to jedyny moment, w ktorym da sie zobaczyc, co ta rzecz w ogole robi.
                    val equipped = state.itemIn(slot)
                    if (equipped != null) inspected = equipped else pickerSlot = slot
                }
            )

            Backpack(items = state.backpack, onInspect = { inspected = it })

            // Zaklecia bierze sie teraz wylacznie z drzewka - stad wejscie do niego zamiast paska.
            TalentEntryTile(
                points = state.talentPoints,
                spent = TalentRules.totalSpent(state.talentRanks),
                onClick = onOpenTalents
            )

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
                pickerSlot = null
                inspected = item
            }
        )
    }

    // Karta przedmiotu - jedno miejsce, w ktorym widac statystyki, opis i roznice wzgledem tego,
    // co masz zalozone. Zakladanie/zdejmowanie idzie stad, a nie z samego tapniecia w ikone.
    inspected?.let { item ->
        val current = state.itemIn(item.slot)
        ItemDetailSheet(
            item = item,
            comparedTo = current?.takeIf { it.id != item.id },
            equipped = current?.id == item.id,
            onEquip = {
                viewModel.equipItem(item)
                inspected = null
            },
            onUnequip = {
                viewModel.unequipSlot(item.slot)
                inspected = null
            },
            onDismiss = { inspected = null }
        )
    }
}

/* ------------------------------------------ Talenty ----------------------------------------- */

/**
 * Wejscie do drzewka talentow. Swieci i pokazuje licznik, dopoki sa nierozdane punkty - awans
 * daje je po cichu w tle walki, wiec bez tego badge'a gracz nie mialby skad wiedziec, ze cos
 * na niego czeka.
 */
@Composable
private fun TalentEntryTile(
    points: Int,
    spent: Int,
    onClick: () -> Unit
) {
    val pending = points > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (pending) AppPalette.Gold.copy(alpha = 0.12f) else AppPalette.Panel)
            .border(
                width = if (pending) 2.dp else 1.dp,
                color = if (pending) AppPalette.Gold else AppPalette.GoldFrame.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "\uD83C\uDF33", fontSize = 24.sp)

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Talent Tree",
                color = AppPalette.Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = when {
                    pending -> "You have $points pts to spend"
                    spent > 0 -> "$spent pts spent - review or respec"
                    else -> "Destruction, Bulwark, Light - choose your path"
                },
                color = if (pending) AppPalette.TextBody else AppPalette.TextMuted,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }

        if (pending) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(AppPalette.Gold)
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$points",
                    color = AppPalette.Ink,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        } else {
            Text(text = "›", color = AppPalette.TextMuted, fontFamily = FontPixel(), fontSize = 20.sp)
        }
    }
}

/* ------------------------------------------ Title ------------------------------------------ */

@Composable
private fun BackRow(onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(SlotBg.copy(alpha = 0.8f))
                .border(1.5.dp, GoldDim, RoundedCornerShape(10.dp))
                .clickable(onClick = onBack)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = "←", color = Gold, fontFamily = FontPixel(), fontSize = 15.sp)
        }
    }
}

@Composable
private fun HeroHeader(name: String, money: Int, progress: PlayerProgress) {
    val fill by animateFloatAsState(targetValue = progress.xpProgress, animationSpec = tween(600))

    OrnateFrame(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Awatar niesie poziom - jedna rzecz zamiast dwoch, i od razu widac, gdzie sie patrzy.
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SlotBg)
                        .border(2.dp, Gold, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${progress.level}",
                        color = Gold,
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
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
                        text = "Knight · ${progress.xp}/${progress.xpForNextLevel} XP",
                        color = TextBody.copy(alpha = 0.7f),
                        fontFamily = FontFirst(),
                        fontSize = 11.sp
                    )
                }
                Pill(text = "💰 $money")
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SlotBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fill)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(50))
                        .background(Gold)
                )
            }
        }
    }
}

/**
 * Cztery statystyki w JEDNYM rzedzie zamiast czterech wierszy na osobnym ekranie. Opisy ("+2 ataku
 * za punkt") zniknely stad celowo - przy pierwszym rozdaniu sa potrzebne, przy setnym to juz tylko
 * cztery linijki do przescrollowania. Zostaje skrot, wartosc i "+", ktory zapala sie tylko wtedy,
 * gdy naprawde jest co rozdac.
 */
@Composable
private fun HeroStatsRow(progress: PlayerProgress, onSpend: (HeroStat) -> Unit) {
    val canSpend = progress.unspentPoints > 0

    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        if (canSpend) {
            Text(
                text = "You have ${progress.unspentPoints} pts to spend",
                color = Success,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HeroStat.entries.forEach { stat ->
                StatChip(
                    stat = stat,
                    value = progress.stats[stat],
                    canSpend = canSpend,
                    onSpend = { onSpend(stat) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    stat: HeroStat,
    value: Int,
    canSpend: Boolean,
    onSpend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Panel)
            .border(
                width = if (canSpend) 1.5.dp else 1.dp,
                color = if (canSpend) Gold else GoldDim.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = canSpend, onClick = onSpend)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            text = stat.short,
            color = Muted,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
        Text(
            text = "$value",
            color = TextBody,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        if (canSpend) {
            Text(
                text = "+",
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

/* ---------------------------------------- Paper doll --------------------------------------- */

@Composable
private fun PaperDoll(
    knight: KnightEquipment,
    look: HeroLook,
    itemAt: (ItemSlot) -> Item?,
    combatStats: HeroCombatStats,
    onSlotClick: (ItemSlot) -> Unit
) {
    OrnateFrame(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SlotColumn(LEFT_SLOTS, itemAt, onSlotClick)
                Box(modifier = Modifier.weight(1f)) { Viewport(knight, look) }
                SlotColumn(RIGHT_SLOTS, itemAt, onSlotClick)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)
            ) {
                BOTTOM_SLOTS.forEach { slot ->
                    GearSlot(slot = slot, item = itemAt(slot), onClick = { onSlotClick(slot) })
                }
            }

            // Liczby walki SIEDZA POD POSTACIA, w tej samej ramce. Wczesniej byly na samym dole
            // ekranu jako "bonusy z ekwipunku" - czyli gracz nie widzial, ile w ogole zadaje
            // obrazen, tylko ile DOKLADA mu sprzet.
            CombatStatsStrip(stats = combatStats)
        }
    }
}

/**
 * Cztery liczby, ktore realnie decyduja o walce - atak, HP, krytyk i bonus z quizu. Jeden zwarty
 * rzad zamiast siatki "bonusow z ekwipunku": na telefonie liczy sie to, co widac bez scrollowania.
 */
@Composable
private fun CombatStatsStrip(stats: HeroCombatStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SlotBg)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CombatStat("⚔", "${stats.attack}", "atak")
        CombatStat("❤", "${stats.maxHp}", "zdrowie")
        CombatStat("🎯", "${stats.critPercent}%", "kryt")
        CombatStat("📚", "+${stats.quizBonusPercent}%", "z quizu")
    }
}

@Composable
private fun CombatStat(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = icon, fontSize = 12.sp)
            Text(
                text = value,
                color = TextBody,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Text(text = label, color = Muted, fontFamily = FontFirst(), fontSize = 9.sp)
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
private fun Viewport(knight: KnightEquipment, look: HeroLook) {
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
            look = look,
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

    // Nazwa przedmiotu ZNIKNELA spod ikony - dwie linijki tekstu przy kazdym z dziewieciu slotow
    // zjadaly pol ekranu, a i tak byly przyciete. Pelna nazwe (i wszystko inne) pokazuje karta
    // przedmiotu po tapnieciu.
    SlotShell(label = if (equipped) null else slot.shortLabel(), labelStrong = equipped) {
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
private fun SlotShell(label: String?, labelStrong: Boolean, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        content()
        // Zajety slot nie ma podpisu - o tym, co w nim stoi, mowi ikona i kolor rzadkosci ramki.
        if (label != null) {
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
}

/* ------------------------------------------ Backpack --------------------------------------- */

@Composable
private fun Backpack(
    items: List<Item>,
    onInspect: (Item) -> Unit
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
                    text = "Backpack",
                    color = Gold,
                    fontFamily = FontPixel(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("tap to view", color = Muted, fontFamily = FontFirst(), fontSize = 10.sp)
            }

            if (items.isEmpty()) {
                Text(
                    "Backpack is empty. Forge gear at the Forge.",
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
                        BackpackItem(item = item, onClick = { onInspect(item) })
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
                    text = "You have nothing for this slot. Forge gear at the Forge.",
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
                Text(text = "Close", color = Gold, fontFamily = FontFirst())
            }
        }
    )
}

/* ------------------------------------------- Stats ----------------------------------------- */

/** Etykieta po polsku dla kazdej statystyki - w kolejnosci, w jakiej maja sie pokazywac. */
@Composable
private fun OrnateFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Przeswitujaca ramka - tlo apki (HomeNavGraph) ma byc widoczne w calym obszarze
    // wokol slotow/kart, tylko one maja wlasne, solidne tla.
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, GoldDim.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
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
