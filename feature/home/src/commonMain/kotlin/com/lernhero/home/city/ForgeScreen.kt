package com.lernhero.home.city

import com.lernhero.shared.theme.GlyphText
import androidx.compose.animation.core.FastOutSlowInEasing
import com.lernhero.home.ui.ItemArtImage
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.craft.CraftCost
import com.lernhero.domain.craft.ForgeTrack
import com.lernhero.domain.craft.ItemUpgrades
import com.lernhero.domain.item.Inventory
import com.lernhero.domain.item.ItemCategory
import com.lernhero.domain.item.ItemInstance
import com.lernhero.domain.item.base
import com.lernhero.domain.item.Item
import com.lernhero.domain.town.TownResources
import com.lernhero.domain.item.catalog.MonsterGear
import com.lernhero.domain.monster.Element
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.ui.ItemInfoBlock
import com.lernhero.home.ui.ItemIcon
import androidx.compose.foundation.layout.BoxWithConstraints
import com.lernhero.home.ui.OutlinedText
import com.lernhero.home.ui.RarityPalette
import com.lernhero.home.ui.badgeColor
import com.lernhero.home.ui.format
import com.lernhero.home.ui.icon
import com.lernhero.home.ui.label
import com.lernhero.shared.theme.FontFirst

private val Outline = Color(0xFF120B05)
private val Bad = Color(0xFFFF6B5B)
private val Good = Color(0xFF7BE06A)

/**
 * Kuznia - tylko ULEPSZANIE. Przedmiotow sie tu nie wykuwa: wypadaja ze skrzyn i bossow, a kuznia
 * podnosi poziom konkretnych sztuk. Na gorze przelacznik toru ([ForgeTrack] - bohater/kompani).
 *
 * Uklad: warsztat wybranej rzeczy u gory (staly rozmiar), pod nim PRZEWIJANA siatka duzych
 * kafelkow. Wczesniej siatka byla stronicowana i sciskana do resztki ekranu, przez co kafelki
 * mialy ~24dp, a nazwy byly nieczytelne - teraz kafelek ma zawsze czytelny rozmiar, a to, co sie
 * nie miesci, po prostu sie przewija.
 */
@Composable
fun ForgeScreen(
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val state by playerViewModel.uiState.collectAsState()

    var track by remember { mutableStateOf(ForgeTrack.Hero) }

    val bag = if (track == ForgeTrack.Monster) state.monsterBag else state.heroBag

    // Kuznia juz nie wykuwa - przedmioty wypadaja ze skrzyn i bossow, a tu ulepsza sie konkretne
    // SZTUKI z plecaka (dwie kopie tego samego miecza to dwa kafelki, kazdy z wlasnym poziomem).
    val entries = remember(bag) {
        bag.mapNotNull { instance ->
            val item = instance.base() ?: return@mapNotNull null
            // Kule mocy (ulti) sie nie ulepsza - ich sila to rzadkosc, wiec w kuzni ich nie ma.
            if (item.slot == com.lernhero.domain.item.ItemSlot.Ultimate) return@mapNotNull null
            ForgeEntry(
                key = instance.uid,
                item = item,
                cost = ItemUpgrades.nextCost(item, instance.level) ?: CraftCost(),
                instance = instance,
                level = instance.level,
                ownedCount = Inventory.countOf(bag, item.id)
            )
        }
    }
    var category by remember(track) { mutableStateOf<ItemCategory?>(null) }
    val categories = remember(entries) {
        ItemCategory.entries.filter { cat -> entries.any { it.category == cat } }
    }
    val visible = remember(entries, category) {
        category?.let { cat -> entries.filter { it.category == cat } } ?: entries
    }

    var selectedKey by remember(track, category) { mutableStateOf(visible.firstOrNull()?.key) }
    val entry = visible.firstOrNull { it.key == selectedKey } ?: visible.firstOrNull()

    TownScreenScaffold(
        title = "Forge",
        resources = state.resources,
        money = state.money,
        toasts = emptyList(),
        onBack = onBack,
        compact = true,
        showAllResources = true
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedText(text = "Upgrade gear", size = 18.sp, color = Gold, strokeWidth = 5f)
                        GlyphText(
                            text = "New gear drops from chests and bosses.",
                            color = Muted,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    TrackToggle(selected = track, onSelect = { track = it })
                }

                if (entry == null) {
                    TownPanel(modifier = Modifier.fillMaxWidth()) {
                        GlyphText(
                            text = "No ${track.label.lowercase()} gear yet - it drops from chests and bosses.",
                            color = Muted,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(24.dp)
                        )
                    }
                } else {
                    UpgradeBench(
                        item = entry.item,
                        level = entry.level,
                        resources = state.resources,
                        money = state.money,
                        onUpgrade = { entry.instance?.let(playerViewModel::upgradeItem) }
                    )
                }

                CatalogBoard(
                    entries = visible,
                    selectedKey = entry?.key,
                    affordable = { it.cost.canAfford(state.resources, state.money) },
                    categories = categories,
                    category = category,
                    onCategory = { category = it },
                    onSelect = { selectedKey = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // Komunikat jako nakladka - element ukladu przesuwalby zawartosc po kazdym wykuciu.
            state.message?.let { message ->
                GlyphText(
                    text = message,
                    color = Gold,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    maxLines = 1,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xF01A1008))
                        .border(2.dp, Outline, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * Jedna pozycja na tablicy kuzni - KONKRETNA sztuka z plecaka. [key] to uid sztuki, dzieki temu
 * dwie kopie tego samego pazura sa dwoma osobnymi kafelkami, kazdy z wlasnym poziomem.
 * [cost] to koszt NASTEPNEGO poziomu (pusty na maksie).
 */
private data class ForgeEntry(
    val key: String,
    val item: Item,
    val cost: CraftCost,
    val instance: ItemInstance?,
    val level: Int,
    /** Ile sztuk tego typu gracz juz ma. */
    val ownedCount: Int
) {
    val category: ItemCategory get() = ItemCategory.of(item)
}

/* ------------------------------------------- Gorny rzad ------------------------------------------ */

@Composable
private fun TrackToggle(selected: ForgeTrack, onSelect: (ForgeTrack) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xE60C0806))
            .border(2.dp, Outline, RoundedCornerShape(50))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        ForgeTrack.entries.forEach { track ->
            val active = track == selected
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) Brush.verticalGradient(listOf(Color(0xFF5BB8FF), Color(0xFF2C6FC4)))
                        else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable { onSelect(track) },
                contentAlignment = Alignment.Center
            ) {
                GlyphText(text = track.icon, fontSize = 18.sp, modifier = Modifier.alpha(if (active) 1f else 0.5f))
            }
        }
    }
}

/* ------------------------------------------ Warsztat ------------------------------------------ */

@Composable
private fun ItemHeader(item: Item, active: Boolean, badge: String?, levelLine: String? = null) {
    val frame = RarityPalette.frame(item.rarity)
    val element = MonsterGear.elementFor(item.id)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ForgeRing(frame = frame, active = active) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RarityPalette.cardBackground(item.rarity))
                    .border(3.dp, frame, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                ItemArtImage(item = item, size = 54.dp)
                badge?.let {
                    GlyphText(
                        text = it,
                        color = Color.White,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xCC000000))
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedText(text = item.name, size = 19.sp, color = frame.brighten(), maxLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Chip(text = RarityPalette.label(item.rarity), color = frame)
                Chip(text = item.slot.label(), color = Color(0xFF8C7A5C))
                element?.let { ElementBadge(it) }
            }
            levelLine?.let { OutlinedText(text = it, size = 15.sp, color = Gold, strokeWidth = 4f) }
        }
    }
}

private fun Color.brighten(): Color = Color(
    red = red + (1f - red) * 0.3f,
    green = green + (1f - green) * 0.3f,
    blue = blue + (1f - blue) * 0.3f,
    alpha = 1f
)

@Composable
private fun Chip(text: String, color: Color) {
    GlyphText(
        text = text,
        color = Color.White,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Black,
        fontSize = 10.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.35f))
            .border(1.5.dp, color, RoundedCornerShape(50))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    )
}

/** Obracajacy sie pierscien swiatla wokol przedmiotu - gasnie, gdy nie da sie wykuc. */
@Composable
private fun ForgeRing(
    frame: Color,
    active: Boolean,
    content: @Composable () -> Unit
) {
    val spin = rememberInfiniteTransition(label = "forge-ring")
    val angle by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 9_000, easing = LinearEasing)),
        label = "forge-ring-angle"
    )
    val pulse by spin.animateFloat(0.6f, 1f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "forge-pulse")

    Box(modifier = Modifier.size(92.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(frame.copy(alpha = if (active) 0.45f * pulse else 0.12f), Color(0xFF0C0806))))
                .border(2.5.dp, Outline, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(86.dp)
                .alpha(if (active) 1f else 0.25f)
                .rotate(angle)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(frame.copy(alpha = 0f), frame.copy(alpha = 0.8f), frame.copy(alpha = 0f), frame.copy(alpha = 0.4f), frame.copy(alpha = 0f))
                    )
                )
        )
        content()
    }
}

@Composable
private fun ElementBadge(element: Element) {
    val color = element.badgeColor()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.35f))
            .border(1.5.dp, color, RoundedCornerShape(50))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.lernhero.game.character.ElementEmblem(element = element, size = 14.dp)
        GlyphText(text = element.label, color = Color.White, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 10.sp)
    }
}

/**
 * Koszt jako kafelki "mam / trzeba": brakujacy surowiec swieci na czerwono, wiec od razu widac,
 * CZEGO brakuje, zamiast zgadywac po wygaszonym przycisku.
 */
@Composable
private fun CostRow(cost: CraftCost, resources: TownResources, money: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        cost.entries().forEach { (icon, amount) ->
            val have = when (icon) {
                "🪨" -> resources.stone
                "🔩" -> resources.iron
                "✨" -> resources.essence
                "🎟" -> resources.summonTickets
                else -> money
            }
            val enough = have >= amount
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xE60C0806))
                    .border(2.dp, if (enough) Outline else Bad.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlyphText(text = icon, fontSize = 15.sp)
                GlyphText(
                    text = " ${amount.compact()}",
                    color = if (enough) Color.White else Bad,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
        }
    }
}

/* ---------------------------------------- Ulepszanie ---------------------------------------- */

/**
 * Warsztat ulepszania: ten sam przedmiot, tylko mocniejszy. Pokazuje PRZED -> PO na kazdej
 * statystyce, bo bez tego "+1" nie znaczy nic; koszt kolejnego progu liczy [ItemUpgrades].
 */
@Composable
private fun UpgradeBench(
    item: Item,
    level: Int,
    resources: TownResources,
    money: Int,
    onUpgrade: () -> Unit
) {
    val maxed = ItemUpgrades.isMaxed(level)
    val cost = ItemUpgrades.nextCost(item, level)
    val affordable = cost?.canAfford(resources, money) == true

    val current = ItemUpgrades.upgraded(item, level).stats
    val next = ItemUpgrades.upgraded(item, level + 1).stats

    TownPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ItemHeader(
                item = item,
                active = affordable,
                badge = if (level > ItemUpgrades.MIN_LEVEL) "+$level" else null,
                levelLine = if (maxed) "+$level · MAX" else "+$level  →  +${level + 1}"
            )
            LevelPips(level = level)

            // Tabelka PRZED -> PO i zdolnosci. Blok ma MINIMALNA wysokosc (typowy przedmiot miesci
            // sie w niej, wiec panel nie skacze przy przegladaniu), ale rosnie, gdy tresci jest wiecej.
            // Wczesniej byl sztywny, a zdolnosci siedzialy w drugim, 40-dp okienku z wlasnym
            // przewijaniem - drugi opis konczyl sie w polowie linijki i nikt nie wiedzial, ze da sie
            // go przewinac. Roznice wysokosci zabiera siatka pod spodem (weight), nie ekran.
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = UPGRADE_INFO_HEIGHT)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val statTypes = current.asMap().keys.toList()
                    if (statTypes.isEmpty()) {
                        GlyphText(
                            text = "This item has no stats to scale - only its effect.",
                            color = Muted,
                            fontFamily = FontFirst(),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        statTypes.forEach { stat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x66000000))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GlyphText(stat.label(), color = Muted, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    GlyphText(stat.format(current[stat]), color = Color.White, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 13.sp)
                                    if (!maxed) {
                                        GlyphText("→ ${stat.format(next[stat])}", color = Good, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                    item.effects.forEach { effect ->
                        GlyphText(
                            text = "✦ ${effect.description}",
                            color = Gold,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                        )
                    }
                }
            }

            cost?.let { CostRow(cost = it, resources = resources, money = money) }

            TownActionButton(
                text = if (maxed) "MAX LEVEL" else "UPGRADE",
                subtitle = if (!maxed && !affordable) "Not enough materials" else null,
                primary = true,
                enabled = !maxed && affordable,
                modifier = Modifier.fillMaxWidth(),
                onClick = onUpgrade
            )
        }
    }
}

/** Pasek segmentow 0..MAX - jeden rzut oka mowi, jak daleko do konca toru. */
@Composable
private fun LevelPips(level: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(ItemUpgrades.MAX_LEVEL) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (index < level) Brush.verticalGradient(listOf(Color(0xFFFFE27A), Color(0xFFE08A1E))) else Brush.verticalGradient(listOf(Color(0xFF0C0806), Color(0xFF0C0806))))
                    .border(1.dp, Outline, RoundedCornerShape(50))
            )
        }
    }
}

/* ------------------------------------------- Katalog ------------------------------------------- */

/** Katalog: polki (filtr) + przewijana siatka duzych kafelkow z nazwa pod ikona. */
@Composable
private fun CatalogBoard(
    entries: List<ForgeEntry>,
    selectedKey: String?,
    affordable: (ForgeEntry) -> Boolean,
    categories: List<ItemCategory>,
    category: ItemCategory?,
    onCategory: (ItemCategory?) -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TownPanel(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryBar(categories = categories, selected = category, onSelect = onCategory)
            LazyVerticalGrid(
                columns = GridCells.Fixed(COLUMNS),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 36.dp)
            ) {
                items(entries, key = { it.key }) { entry ->
                    RecipeTile(
                        entry = entry,
                        selected = entry.key == selectedKey,
                        affordable = affordable(entry),
                        onClick = { onSelect(entry.key) }
                    )
                }
            }
        }
    }
}

/** Pasek polek nad siatka: WSZYSTKO / bron / pancerz / bizuteria. */
@Composable
private fun CategoryBar(
    categories: List<ItemCategory>,
    selected: ItemCategory?,
    onSelect: (ItemCategory?) -> Unit
) {
    if (categories.size <= 1) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CategoryChip(label = "ALL", active = selected == null, modifier = Modifier.weight(1f), onClick = { onSelect(null) })
        categories.forEach { cat ->
            CategoryChip(
                label = cat.icon,
                active = selected == cat,
                modifier = Modifier.weight(1f),
                // Ponowne stukniecie w aktywna polke zdejmuje filtr.
                onClick = { onSelect(if (selected == cat) null else cat) }
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (active) Brush.verticalGradient(listOf(Color(0xFF5BB8FF), Color(0xFF2C6FC4)))
                else Brush.verticalGradient(listOf(Color(0xFF2A1E14), Color(0xFF17100A)))
            )
            .border(2.dp, Outline, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (label == "ALL") {
            OutlinedText(text = label, size = 13.sp, color = if (active) Color.White else Muted, strokeWidth = 4f)
        } else {
            GlyphText(text = label, fontSize = 16.sp, modifier = Modifier.alpha(if (active) 1f else 0.6f))
        }
    }
}

@Composable
private fun RecipeTile(
    entry: ForgeEntry,
    selected: Boolean,
    affordable: Boolean,
    onClick: () -> Unit
) {
    val item = entry.item
    val level = entry.level
    val frame = RarityPalette.frame(item.rarity)
    val element = MonsterGear.elementFor(item.id)
    val pulse by rememberInfiniteTransition(label = "tile").animateFloat(
        0.6f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "tile-pulse"
    )

    Column(
        modifier = Modifier
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    val s = if (selected) 1f + 0.04f * pulse else 1f
                    scaleX = s
                    scaleY = s
                },
            contentAlignment = Alignment.Center
        ) {
            // Wybrany kafel: zlota poswiata za karta.
            if (selected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(maxWidth * 0.18f))
                        .background(Gold.copy(alpha = 0.35f * pulse))
                )
            }
            ItemIcon(
                item = item,
                level = level,
                size = maxWidth * 0.9f,
                canUpgrade = affordable && !ItemUpgrades.isMaxed(level)
            )
            element?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(it.badgeColor())
                        .border(1.dp, Outline, CircleShape)
                )
            }
        }
        GlyphText(
            text = item.name,
            color = if (selected) Gold else Color(0xFFEADFC8),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 11.sp
        )
    }
}

/**
 * Minimalna wysokosc tabelki PRZED -> PO i zdolnosci - miesci typowy przedmiot (jedna-dwie
 * statystyki i zdolnosc), wiec przy przegladaniu panel stoi w miejscu. Bogatszy przedmiot go wydluza.
 */
private val UPGRADE_INFO_HEIGHT = 96.dp

private const val COLUMNS = 4
