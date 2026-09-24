package com.lernhero.home.city

import androidx.compose.animation.core.FastOutSlowInEasing
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
import com.lernhero.domain.craft.Forge
import com.lernhero.domain.craft.ForgeTrack
import com.lernhero.domain.craft.ItemTier
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
 * Kuznia. Dwie NIEZALEZNE osie wyboru: tryb ([ForgeMode] - kuj/ulepszaj) i tor ([ForgeTrack] -
 * dla bohatera/dla kompanow). Obie stoja w jednym rzedzie na gorze.
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

    var mode by remember { mutableStateOf(ForgeMode.Forge) }
    var track by remember { mutableStateOf(ForgeTrack.Hero) }

    val bag = if (track == ForgeTrack.Monster) state.monsterBag else state.heroBag
    val allRecipes = remember(track) { Forge.recipesFor(track) }

    // Forge pokazuje RECEPTURY (co da sie wykuc), a Upgrade - SZTUKI (co juz mam; ulepsza sie
    // konkretny egzemplarz, nie typ).
    val entries = remember(mode, allRecipes, bag, state.storyProgress) {
        when (mode) {
            ForgeMode.Forge -> allRecipes.sortedBy { ItemTier.of(it.item).ordinal }.map { recipe ->
                val tier = ItemTier.of(recipe.item)
                ForgeEntry(
                    key = recipe.id,
                    item = recipe.item,
                    cost = recipe.cost,
                    instance = null,
                    level = ItemUpgrades.MIN_LEVEL,
                    ownedCount = Inventory.countOf(bag, recipe.item.id),
                    tier = tier,
                    locked = !tier.isUnlocked(state.storyProgress)
                )
            }

            ForgeMode.Upgrade -> bag.mapNotNull { instance ->
                val item = instance.base() ?: return@mapNotNull null
                ForgeEntry(
                    key = instance.uid,
                    item = item,
                    cost = Forge.costFor(item),
                    instance = instance,
                    level = instance.level,
                    ownedCount = Inventory.countOf(bag, item.id),
                    // Ulepszac wolno zawsze - prog bramkuje WYKUWANIE, a nie to, co juz masz.
                    tier = null,
                    locked = false
                )
            }
        }
    }
    var category by remember(track, mode) { mutableStateOf<ItemCategory?>(null) }
    val categories = remember(entries) {
        ItemCategory.entries.filter { cat -> entries.any { it.category == cat } }
    }
    val visible = remember(entries, category) {
        category?.let { cat -> entries.filter { it.category == cat } } ?: entries
    }

    var selectedKey by remember(track, mode, category) { mutableStateOf(visible.firstOrNull()?.key) }
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
                    ForgeMode.entries.forEach { m ->
                        ChunkyTab(label = m.label, active = m == mode, modifier = Modifier.weight(1f)) { mode = m }
                    }
                    TrackToggle(selected = track, onSelect = { track = it })
                }

                if (entry == null) {
                    TownPanel(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = mode.emptyHint(track),
                            color = Muted,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(24.dp)
                        )
                    }
                } else when (mode) {
                    ForgeMode.Forge -> Workbench(
                        item = entry.item,
                        cost = entry.cost,
                        ownedCount = entry.ownedCount,
                        resources = state.resources,
                        money = state.money,
                        lockedTier = entry.tier?.takeUnless { it.isUnlocked(state.storyProgress) },
                        onForge = { playerViewModel.craftItem(entry.item, entry.cost) }
                    )

                    ForgeMode.Upgrade -> UpgradeBench(
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
                Text(
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
 * Jedna pozycja na tablicy kuzni - wspolny ksztalt dla obu trybow, zeby siatka i warsztat nie
 * musialy wiedziec, ktory tryb jest wlaczony.
 *
 * [instance] jest null w trybie Forge (jeszcze nie ma czego ulepszac - dopiero wykuwamy) i
 * niepuste w trybie Upgrade, gdzie kazdy kafelek to KONKRETNA sztuka z plecaka.
 * [key] to wtedy uid sztuki, a nie id receptury - dzieki temu dwie kopie tego samego pazura sa
 * dwoma osobnymi kafelkami, kazdy z wlasnym poziomem.
 */
private data class ForgeEntry(
    val key: String,
    val item: Item,
    val cost: CraftCost,
    val instance: ItemInstance?,
    val level: Int,
    /** Ile sztuk tego typu gracz juz ma - warsztat pokazuje to zamiast "juz masz / nie masz". */
    val ownedCount: Int,
    /** Prog kuzni - null w trybie Upgrade, gdzie bramka nie obowiazuje. */
    val tier: ItemTier?,
    /** Czy prog jest jeszcze zamkniety. Zamkniete pozycje ZOSTAJA w katalogu - patrz [RecipeTile]. */
    val locked: Boolean
) {
    val category: ItemCategory get() = ItemCategory.of(item)
}

/**
 * Dwie czynnosci kuzni. Osobno od [ForgeTrack], bo to niezalezne pytania: "co robie" i "dla kogo".
 */
private enum class ForgeMode(val label: String) {
    Forge("Forge"),
    Upgrade("Upgrade");

    fun emptyHint(track: ForgeTrack): String = when (this) {
        Forge -> "This tab has no recipes yet."
        Upgrade -> "Nothing to upgrade yet - forge ${track.label.lowercase()} gear first."
    }
}

/* ------------------------------------------- Gorny rzad ------------------------------------------ */

@Composable
private fun ChunkyTab(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (active) Brush.verticalGradient(listOf(Color(0xFFFFC94A), Color(0xFFE0801C)))
                else Brush.verticalGradient(listOf(Color(0xFF3A2A1C), Color(0xFF221810)))
            )
            .border(2.5.dp, Outline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (active) OutlinedText(text = label, size = 16.sp, color = Color.White, strokeWidth = 5f)
        else Text(label, color = Muted, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 15.sp)
    }
}

/** Dla kogo kujemy: bohater / kompani - dwa okragle przyciski w jednej kapsule. */
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
                Text(text = track.icon, fontSize = 18.sp, modifier = Modifier.alpha(if (active) 1f else 0.5f))
            }
        }
    }
}

/* ------------------------------------------ Warsztat ------------------------------------------ */

/**
 * Warsztat: po lewej wybrany przedmiot w obracajacym sie kregu, po prawej nazwa, rzadkosc,
 * opis/statystyki/zdolnosci; pod spodem koszt (ile mam / ile trzeba) i przycisk.
 */
@Composable
private fun Workbench(
    item: Item,
    cost: CraftCost,
    ownedCount: Int,
    resources: TownResources,
    money: Int,
    lockedTier: ItemTier?,
    onForge: () -> Unit
) {
    val affordable = cost.canAfford(resources, money)
    TownPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ItemHeader(item = item, active = affordable && lockedTier == null, badge = if (ownedCount > 0) "x$ownedCount" else null)
            ItemInfoBlock(item = item, height = 52.dp)
            CostRow(cost = cost, resources = resources, money = money)
            TownActionButton(
                text = if (lockedTier != null) "🔒 LOCKED" else "FORGE",
                subtitle = when {
                    lockedTier != null -> "${lockedTier.label} gear unlocks after ${lockedTier.requiredStages()} stages"
                    !affordable && cost.iron > resources.iron -> "Not enough ore - it drops in dungeons"
                    !affordable -> "Not enough materials"
                    else -> null
                },
                primary = true,
                enabled = affordable && lockedTier == null,
                modifier = Modifier.fillMaxWidth(),
                onClick = onForge
            )
        }
    }
}

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
                Text(text = item.icon(), fontSize = 34.sp)
                badge?.let {
                    Text(
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
    Text(
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
        Text(text = element.icon, fontSize = 10.sp)
        Text(text = element.label, color = Color.White, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 10.sp)
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
                Text(text = icon, fontSize = 15.sp)
                Text(
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

            // Tabelka PRZED -> PO w bloku o STALEJ wysokosci - liczba statystyk rozni sie miedzy
            // przedmiotami, a panel nie moze skakac przy zmianie sztuki.
            Box(modifier = Modifier.fillMaxWidth().height(UPGRADE_INFO_HEIGHT)) {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val statTypes = current.asMap().keys.toList()
                    if (statTypes.isEmpty()) {
                        Text(
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
                                Text(stat.label(), color = Muted, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(stat.format(current[stat]), color = Color.White, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 13.sp)
                                    if (!maxed) {
                                        Text("→ ${stat.format(next[stat])}", color = Good, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                    if (item.effects.isNotEmpty()) {
                        ItemInfoBlock(item = item, height = UPGRADE_EFFECTS_HEIGHT, showDescription = false, showStats = false)
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
            Text(text = label, fontSize = 16.sp, modifier = Modifier.alpha(if (active) 1f else 0.6f))
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
    val owned = entry.ownedCount > 0
    val frame = RarityPalette.frame(item.rarity)
    val element = MonsterGear.elementFor(item.id)
    val pulse by rememberInfiniteTransition(label = "tile").animateFloat(
        0.6f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "tile-pulse"
    )

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            // Nieosiagalne recepty gasna, ale zostaja klikalne - gracz ma prawo sprawdzic, ile brakuje.
            .alpha(if (entry.locked) 0.5f else if (owned || affordable) 1f else 0.6f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    val s = if (selected) 1f + 0.03f * pulse else 1f
                    scaleX = s
                    scaleY = s
                }
                .clip(RoundedCornerShape(12.dp))
                .background(RarityPalette.cardBackground(item.rarity))
                .border(if (selected) 3.dp else 2.dp, if (selected) Gold else frame, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = item.icon(), fontSize = 30.sp)
            if (entry.locked) Text(text = "🔒", fontSize = 20.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp))

            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                element?.let {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(it.badgeColor())
                            .border(1.dp, Outline, CircleShape)
                    )
                }
                if (level > ItemUpgrades.MIN_LEVEL) {
                    OutlinedText(text = "+$level", size = 11.sp, color = Gold, strokeWidth = 4f)
                }
            }
            if (owned) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4ADE80))
                        .border(1.5.dp, Outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✓", color = Outline, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Text(
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

/** Staly budzet miejsca na tabelke PRZED -> PO i zdolnosci w warsztacie ulepszania. */
private val UPGRADE_INFO_HEIGHT = 96.dp

/** Ile z tego przypada na liste zdolnosci. */
private val UPGRADE_EFFECTS_HEIGHT = 40.dp

private const val COLUMNS = 4
