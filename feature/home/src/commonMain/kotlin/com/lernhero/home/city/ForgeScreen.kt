package com.lernhero.home.city

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.craft.CraftCost
import com.lernhero.domain.craft.Forge
import com.lernhero.domain.craft.ForgeRecipe
import com.lernhero.domain.craft.ForgeTrack
import com.lernhero.domain.craft.ItemUpgrades
import com.lernhero.domain.item.Item
import com.lernhero.domain.town.TownResources
import com.lernhero.domain.item.catalog.MonsterGear
import com.lernhero.domain.monster.Element
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.ui.PageDots
import com.lernhero.home.ui.ParchmentButton
import com.lernhero.home.ui.ParchmentInset
import com.lernhero.home.ui.ParchmentPalette
import com.lernhero.home.ui.ParchmentPanel
import com.lernhero.home.ui.ParchmentTabs
import com.lernhero.home.ui.RarityPalette
import com.lernhero.home.ui.ResourcePill
import com.lernhero.home.ui.badgeColor
import com.lernhero.home.ui.format
import com.lernhero.home.ui.icon
import com.lernhero.home.ui.label
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/**
 * Kuznia. Dwie NIEZALEZNE osie wyboru, kazda na wlasnej krawedzi ekranu:
 *
 * ```
 * [ Forge | Upgrade ]                <- co robimy (gora)
 * +-------------------------+ [B]    <- dla kogo (prawa krawedz):
 * |  warsztat wybranej rzeczy| [S]       B = bohater, S = summony
 * +-------------------------+
 * +-------------------------+
 * |  siatka przedmiotow     |
 * |        (1)(2)(3)        |
 * +-------------------------+
 * ```
 *
 * Wczesniej gorne zakladki mielily tor (bohater/summony) i nie bylo gdzie dolozyc ulepszania.
 * Rozbicie na dwie osie daje cztery widoki z dwoch krotkich list, zamiast jednej listy czterech
 * pozycji - i pozwala przelaczyc "dla kogo" bez gubienia tego, co sie wlasnie robi.
 *
 * [ForgeTrack] to dwie ROZNE pule sprzetu (patrz [MonsterGear] po powod, dla ktorego nie dziela
 * jednej), a [ForgeMode] to dwie rozne czynnosci na tej samej puli.
 */
@Composable
fun ForgeScreen(
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val state by playerViewModel.uiState.collectAsState()

    var mode by remember { mutableStateOf(ForgeMode.Forge) }
    var track by remember { mutableStateOf(ForgeTrack.Hero) }

    val ownedPool = if (track == ForgeTrack.Monster) state.ownedMonsterItems else state.ownedItems
    val allRecipes = remember(track) { Forge.recipesFor(track) }

    // Ulepszac mozna WYLACZNIE to, co sie ma. Pokazywanie tu calego katalogu robiloby z tej
    // zakladki druga liste zyczen zamiast warsztatu.
    val recipes = remember(mode, allRecipes, ownedPool) {
        if (mode == ForgeMode.Upgrade) allRecipes.filter { it.item.id in ownedPool } else allRecipes
    }
    val pages = remember(recipes) { recipes.chunked(PAGE_SIZE) }

    // Wybor i strona sa per WIDOK (tor + tryb): przejscie na "Summony" nie moze zostawic
    // w warsztacie miecza bohatera, bo przycisk dzialalby na cos, czego nie widac ponizej.
    var selectedId by remember(track, mode) { mutableStateOf(recipes.firstOrNull()?.id) }
    var page by remember(track, mode) { mutableStateOf(0) }

    val recipe = recipes.firstOrNull { it.id == selectedId } ?: recipes.firstOrNull()

    // Ten sam szkielet co reszta budynkow Miasta (tartak, kamieniolom, summon center): powrot,
    // tytul i pasek surowcow z jednego miejsca.
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
            // BEZ scrolla: wszystko ma sie zmiescic na jednym ekranie. Warsztat zajmuje tyle,
            // ile potrzebuje, a katalog bierze CALA reszte (weight) - dzieki temu na kazdym
            // telefonie uklad konczy sie dokladnie na dolnej krawedzi, zamiast uciekac pod nia.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ParchmentTabs(
                    tabs = ForgeMode.entries.map { it.label },
                    selectedIndex = ForgeMode.entries.indexOf(mode),
                    onSelect = { mode = ForgeMode.entries[it] }
                )

                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (recipe == null) {
                            ParchmentPanel(modifier = Modifier.fillMaxWidth(), title = mode.label) {
                                Text(
                                    text = mode.emptyHint(track),
                                    color = ParchmentPalette.InkMuted,
                                    fontFamily = FontFirst(),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else when (mode) {
                            ForgeMode.Forge -> Workbench(
                                recipe = recipe,
                                owned = recipe.item.id in ownedPool,
                                affordable = recipe.cost.canAfford(state.resources, state.money),
                                ironHave = state.resources.iron,
                                onForge = { playerViewModel.craftItem(recipe.item, recipe.cost) }
                            )

                            ForgeMode.Upgrade -> UpgradeBench(
                                item = recipe.item,
                                level = ItemUpgrades.levelOf(state.itemUpgrades, recipe.item.id),
                                resources = state.resources,
                                money = state.money,
                                onUpgrade = { playerViewModel.upgradeItem(recipe.item) }
                            )
                        }

                        CatalogBoard(
                            page = pages.getOrNull(page).orEmpty(),
                            pageCount = pages.size,
                            pageIndex = page,
                            selectedId = selectedId,
                            ownedIds = ownedPool,
                            upgrades = state.itemUpgrades,
                            affordable = { it.cost.canAfford(state.resources, state.money) },
                            onSelect = { selectedId = it },
                            onPage = { page = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    TrackRail(
                        selected = track,
                        onSelect = { track = it }
                    )
                }
            }

            // Komunikat jako nakladka - element ukladu przesuwalby cala zawartosc i ekran
            // skakalby po kazdym wykuciu.
            state.message?.let { message ->
                Text(
                    text = message,
                    color = ParchmentPalette.Button,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF1A1008).copy(alpha = 0.85f))
                        .padding(horizontal = 12.dp, vertical = 3.dp)
                )
            }
        }
    }
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

/* ---------------------------------------- Rail torow ---------------------------------------- */

/**
 * Pionowy pasek przy prawej krawedzi: dla kogo kujemy. Stoi z boku, a nie na gorze, bo to wybor
 * niezalezny od trybu - przelaczenie go ma zmienic pule, a nie to, co gracz wlasnie robi.
 */
@Composable
private fun TrackRail(
    selected: ForgeTrack,
    onSelect: (ForgeTrack) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ForgeTrack.entries.forEach { track ->
            val active = track == selected
            Column(
                modifier = Modifier
                    .size(width = 46.dp, height = 54.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (active) ParchmentPalette.Button else ParchmentPalette.Inset)
                    .border(
                        width = if (active) 3.dp else 2.dp,
                        color = if (active) ParchmentPalette.ButtonEdge else ParchmentPalette.FrameLight,
                        shape = RoundedCornerShape(11.dp)
                    )
                    .clickable { onSelect(track) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = track.icon, fontSize = 20.sp)
                Text(
                    text = track.label,
                    color = if (active) ParchmentPalette.Ink else ParchmentPalette.InkMuted,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    maxLines = 1
                )
            }
        }
    }
}

/* ------------------------------------------- Naglowek ------------------------------------------ */

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(ParchmentPalette.Button)
                .border(3.dp, ParchmentPalette.ButtonEdge, RoundedCornerShape(11.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "←", color = ParchmentPalette.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Forge",
            color = ParchmentPalette.Button,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun ResourceStrip(wood: Int, stone: Int, iron: Int, gold: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ResourcePill(icon = "🪵", amount = short(wood), modifier = Modifier.weight(1f))
        ResourcePill(icon = "🪨", amount = short(stone), modifier = Modifier.weight(1f))
        ResourcePill(icon = "⚙", amount = short(iron), modifier = Modifier.weight(1f))
        ResourcePill(icon = "💰", amount = short(gold), modifier = Modifier.weight(1f))
    }
}

/** Skracanie duzych liczb - pigulka ma stala szerokosc, a saldo potrafi miec siedem cyfr. */
private fun short(value: Int): String = when {
    value >= 1_000_000 -> "${value / 100_000 / 10f}M"
    value >= 1_000 -> "${value / 100 / 10f}K"
    else -> "$value"
}

/* ------------------------------------------ Warsztat ------------------------------------------ */

/**
 * Gorna tablica: wybrany przedmiot w obracajacym sie kregu run, po bokach sloty materialow,
 * pod spodem koszt i przycisk. Krag jest jedynym ruchomym elementem - kuznia ma "pracowac",
 * ale nie odciagac uwagi od liczb.
 */
@Composable
private fun Workbench(
    recipe: ForgeRecipe,
    owned: Boolean,
    affordable: Boolean,
    ironHave: Int,
    onForge: () -> Unit
) {
    val item = recipe.item
    val frame = RarityPalette.frame(item.rarity)
    val element = MonsterGear.elementFor(item.id)

    ParchmentPanel(modifier = Modifier.fillMaxWidth(), title = "Forge: ${item.name}") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MaterialSlot(icon = "🪵")
            Spacer(modifier = Modifier.size(6.dp))
            ForgeRing(frame = frame, active = affordable && !owned) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(RarityPalette.cardBackground(item.rarity))
                        .border(3.dp, frame, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item.icon(), fontSize = 34.sp)
                }
            }
            Spacer(modifier = Modifier.size(6.dp))
            MaterialSlot(icon = "⚙")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${item.slot.label()} · ${RarityPalette.label(item.rarity)}",
                color = ParchmentPalette.InkMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            element?.let { ElementBadge(it) }
        }

        Text(
            text = item.description,
            color = ParchmentPalette.Ink,
            fontFamily = FontFirst(),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )

        CostRow(cost = recipe.cost)

        ParchmentButton(
            text = if (owned) "ALREADY OWNED" else "FORGE",
            enabled = affordable && !owned,
            modifier = Modifier.fillMaxWidth(),
            onClick = onForge
        )

        // Wygaszony przycisk musi powiedziec, DLACZEGO jest wygaszony - inaczej ekran wyglada
        // na zepsuty. Ruda jest tu kluczowa: to jedyny material, ktorego nie produkuje miasto.
        if (!owned && !affordable) {
            Text(
                text = if (recipe.cost.iron > ironHave) {
                    "Not enough ore ($ironHave of ${recipe.cost.iron}). Ore only drops in dungeons."
                } else {
                    "Not enough materials - check the cost above."
                },
                color = Color(0xFF9E2B20),
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Pusty slot materialu po bokach kregu - placeholder pod przyszle "wsady" do wykuwania. */
@Composable
private fun MaterialSlot(icon: String) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ParchmentPalette.Inset)
            .border(2.dp, ParchmentPalette.FrameLight, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 18.sp, modifier = Modifier.alpha(0.65f))
    }
}

/** Obracajacy sie pierscien run wokol przedmiotu - gasnie, gdy nie da sie wykuc. */
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
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "forge-ring-angle"
    )

    Box(modifier = Modifier.size(126.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(126.dp)
                .clip(CircleShape)
                .background(ParchmentPalette.Inset)
                .border(3.dp, ParchmentPalette.FrameLight, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(112.dp)
                .alpha(if (active) 1f else 0.3f)
                .rotate(angle)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(
                            frame.copy(alpha = 0f),
                            frame.copy(alpha = 0.65f),
                            frame.copy(alpha = 0f),
                            frame.copy(alpha = 0.3f),
                            frame.copy(alpha = 0f)
                        )
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
            .clip(RoundedCornerShape(7.dp))
            .background(color.copy(alpha = 0.25f))
            .border(1.dp, color, RoundedCornerShape(7.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = element.icon, fontSize = 10.sp)
        Text(
            text = element.label,
            color = ParchmentPalette.Ink,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
    }
}

/** Koszt w jednym wglebionym wierszu - [CraftCost.entries] pomija zera, wiec nie ma pustych ikon. */
@Composable
private fun CostRow(cost: CraftCost) {
    ParchmentInset(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            cost.entries().forEach { (icon, amount) ->
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = icon, fontSize = 13.sp)
                    Text(
                        text = "$amount",
                        color = ParchmentPalette.Ink,
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/* ---------------------------------------- Ulepszanie ---------------------------------------- */

/**
 * Warsztat ulepszania: ten sam przedmiot, tylko mocniejszy. Pokazuje PRZED -> PO na kazdej
 * statystyce, bo bez tego "+1" nie znaczy nic; koszt kolejnego progu liczy [ItemUpgrades],
 * a nie ekran - tak samo jak przy zapisie, wiec liczby nie potrafia sie rozjechac.
 */
@Composable
private fun UpgradeBench(
    item: Item,
    level: Int,
    resources: TownResources,
    money: Int,
    onUpgrade: () -> Unit
) {
    val frame = RarityPalette.frame(item.rarity)
    val maxed = ItemUpgrades.isMaxed(level)
    val cost = ItemUpgrades.nextCost(item, level)
    val affordable = cost?.canAfford(resources, money) == true

    val current = ItemUpgrades.upgraded(item, level).stats
    val next = ItemUpgrades.upgraded(item, level + 1).stats

    ParchmentPanel(
        modifier = Modifier.fillMaxWidth(),
        title = "Upgrade: ${item.name}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(RarityPalette.cardBackground(item.rarity))
                    .border(3.dp, frame, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon(), fontSize = 30.sp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = if (maxed) "+$level · MAX" else "+$level  →  +${level + 1}",
                    color = ParchmentPalette.Button,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                LevelPips(level = level)
            }
        }

        // Same statystyki, ktore przedmiot NAPRAWDE ma - pusta tabelka na przedmiocie bez
        // statystyk mowilaby, ze ulepszanie nic nie da, i mialaby racje.
        val statTypes = current.asMap().keys.toList()
        if (statTypes.isEmpty()) {
            Text(
                text = "This item has no stats to scale - only its effect.",
                color = ParchmentPalette.InkMuted,
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        } else {
            ParchmentInset(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    statTypes.forEach { stat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stat.label(),
                                color = ParchmentPalette.InkMuted,
                                fontFamily = FontFirst(),
                                fontSize = 10.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = stat.format(current[stat]),
                                    color = ParchmentPalette.Ink,
                                    fontFamily = FontPixel(),
                                    fontSize = 11.sp
                                )
                                if (!maxed) {
                                    Text(
                                        text = "→ ${stat.format(next[stat])}",
                                        color = Color(0xFF2F7D32),
                                        fontFamily = FontPixel(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        cost?.let { CostRow(cost = it) }

        ParchmentButton(
            text = if (maxed) "MAX LEVEL" else "UPGRADE",
            enabled = !maxed && affordable,
            modifier = Modifier.fillMaxWidth(),
            onClick = onUpgrade
        )

        if (!maxed && !affordable) {
            Text(
                text = "Not enough materials - check the cost above.",
                color = Color(0xFF9E2B20),
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Pasek kropek 0..MAX - jeden rzut oka mowi, jak daleko do konca toru. */
@Composable
private fun LevelPips(level: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(ItemUpgrades.MAX_LEVEL) { index ->
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < level) ParchmentPalette.Button else ParchmentPalette.Inset
                    )
                    .border(1.dp, ParchmentPalette.FrameLight, CircleShape)
            )
        }
    }
}

/* ------------------------------------------- Katalog ------------------------------------------- */

/**
 * Dolna tablica: cala oferta jako siatka kwadratowych ikon ze stronicowaniem.
 *
 * Siatke skladamy z [Row] po cztery, a nie z LazyVerticalGrid, bo caly ekran siedzi juz
 * w [verticalScroll]; zagniezdzenie leniwej siatki w scrollu wysadza pomiar wysokosci.
 */
@Composable
private fun CatalogBoard(
    page: List<ForgeRecipe>,
    pageCount: Int,
    pageIndex: Int,
    selectedId: String?,
    ownedIds: List<String>,
    upgrades: Map<String, Int>,
    affordable: (ForgeRecipe) -> Boolean,
    onSelect: (String) -> Unit,
    onPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ParchmentPanel(modifier = modifier.fillMaxWidth()) {
        ParchmentInset(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                page.chunked(COLUMNS).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        row.forEach { entry ->
                            RecipeTile(
                                recipe = entry,
                                selected = entry.id == selectedId,
                                owned = entry.item.id in ownedIds,
                                level = ItemUpgrades.levelOf(upgrades, entry.item.id),
                                affordable = affordable(entry),
                                modifier = Modifier.weight(1f),
                                onClick = { onSelect(entry.id) }
                            )
                        }
                        // Ostatni rzad bywa niepelny - puste wagi trzymaja kafelki w tej samej
                        // szerokosci, zamiast rozciagac dwa ostatnie na caly wiersz.
                        repeat(COLUMNS - row.size) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }

        PageDots(count = pageCount, selected = pageIndex, onSelect = onPage)
    }
}

@Composable
private fun RecipeTile(
    recipe: ForgeRecipe,
    selected: Boolean,
    owned: Boolean,
    level: Int,
    affordable: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val item = recipe.item
    val frame = RarityPalette.frame(item.rarity)
    val element = MonsterGear.elementFor(item.id)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(RarityPalette.cardBackground(item.rarity))
            .border(
                width = if (selected) 3.dp else 2.dp,
                color = if (selected) ParchmentPalette.Button else frame.copy(alpha = 0.85f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            // Nieosiagalne recepty gasna, ale zostaja klikalne - gracz ma prawo sprawdzic,
            // ile brakuje, zanim uzbiera surowce.
            .alpha(if (owned || affordable) 1f else 0.45f),
        contentAlignment = Alignment.Center
    ) {
        Text(text = item.icon(), fontSize = 30.sp)

        // Znaczniki w rogach: zywiol i poziom lewo-gora, "masz juz" prawo-gora - tak jak
        // gwiazdki rzadkosci na kafelkach artefaktow. Zywiol i poziom siedza w JEDNYM rzedzie,
        // bo sprzet kompana potrafi miec obie rzeczy naraz i w osobnych Boxach nachodzilyby
        // na siebie.
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
                        .border(1.dp, ParchmentPalette.Frame, CircleShape)
                )
            }
            if (level > ItemUpgrades.MIN_LEVEL) {
                Text(
                    text = "+$level",
                    color = ParchmentPalette.Ink,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(ParchmentPalette.Button)
                        .padding(horizontal = 3.dp)
                )
            }
        }
        if (owned) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4ADE80))
                    .border(1.dp, ParchmentPalette.Frame, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✓", color = ParchmentPalette.Ink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = item.name,
            color = Color(0xFFF4E9D7),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 7.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0x99000000))
                .padding(horizontal = 2.dp, vertical = 2.dp)
        )
    }
}

private const val COLUMNS = 4

/** Ile kafelkow miesci sie na jednej "tablicy" - cztery rzedy po cztery, jak na wzorze. */
private const val PAGE_SIZE = 16
