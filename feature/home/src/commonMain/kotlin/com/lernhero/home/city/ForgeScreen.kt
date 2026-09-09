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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import com.lernhero.home.ui.FramedPanel
import com.lernhero.home.ui.ItemInfoBlock
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

    val bag = if (track == ForgeTrack.Monster) state.monsterBag else state.heroBag
    val allRecipes = remember(track) { Forge.recipesFor(track) }

    // Dwa tryby, dwie ROZNE listy - i to jest sedno przejscia na egzemplarze.
    //
    // Forge pokazuje RECEPTURY (co da sie wykuc; kazde wykucie daje kolejna sztuke), a Upgrade
    // pokazuje SZTUKI (co juz mam; ulepsza sie konkretny egzemplarz, nie typ). Wczesniej obie
    // zakladki chodzily po recepturach, wiec ulepszenie "nozyka" dotykalo wszystkich nozykow naraz.
    val entries = remember(mode, allRecipes, bag, state.storyProgress) {
        when (mode) {
            // Kolejnosc wg progu, a nie wg pliku z recepturami: gracz przeglada katalog od tego,
            // co moze wykuc teraz, do tego, na co jeszcze pracuje. Dzieki temu zamkniete rzeczy
            // skupiaja sie na koncu, zamiast byc rozsiane po wszystkich stronach.
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
                    // Inaczej cofniecie sie w kampanii zamrazaloby graczowi wlasny sprzet.
                    tier = null,
                    locked = false
                )
            }
        }
    }
    // Polka: bron / pancerz / bizuteria. null = wszystko. Przy kilkudziesieciu recepturach
    // "przewin i szukaj miecza" przestalo dzialac - filtr jest tansza odpowiedzia niz osobne
    // zakladki, bo nie zabiera trzeciej osi wyboru obok trybu i toru.
    var category by remember(track, mode) { mutableStateOf<ItemCategory?>(null) }

    // Kategorie liczymy z PELNEJ listy, nie z przefiltrowanej - inaczej wybranie broni ukryloby
    // przyciski pozostalych polek i nie bylo jak wrocic.
    val categories = remember(entries) {
        ItemCategory.entries.filter { cat -> entries.any { it.category == cat } }
    }
    val visible = remember(entries, category) {
        category?.let { cat -> entries.filter { it.category == cat } } ?: entries
    }
    val pages = remember(visible) { visible.chunked(PAGE_SIZE) }

    // Wybor i strona sa per WIDOK (tor + tryb + polka): przejscie na "Summony" nie moze zostawic
    // w warsztacie miecza bohatera, bo przycisk dzialalby na cos, czego nie widac ponizej.
    var selectedKey by remember(track, mode, category) { mutableStateOf(visible.firstOrNull()?.key) }
    var page by remember(track, mode, category) { mutableStateOf(0) }

    val entry = visible.firstOrNull { it.key == selectedKey } ?: visible.firstOrNull()

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

                // Rail "dla kogo" nie stoi OBOK panelu (wtedy panel ucieka w lewo, a rail dynda
                // przy krawedzi ekranu), tylko OKRAKIEM na jego prawym boku: polowa w obrysie
                // pergaminu, polowa poza nim. Panel przez to jest wezszy i wysrodkowany, a rail
                // wyglada jak przypiety do niego, nie zawieszony w powietrzu.
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                  Box(modifier = Modifier.fillMaxHeight().widthIn(max = 360.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = RAIL_STRADDLE),
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                    ) {
                        if (entry == null) {
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
                                item = entry.item,
                                cost = entry.cost,
                                ownedCount = entry.ownedCount,
                                affordable = entry.cost.canAfford(state.resources, state.money),
                                lockedTier = entry.tier?.takeUnless { it.isUnlocked(state.storyProgress) },
                                ironHave = state.resources.iron,
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
                            page = pages.getOrNull(page).orEmpty(),
                            pageCount = pages.size,
                            pageIndex = page,
                            selectedKey = selectedKey,
                            affordable = { it.cost.canAfford(state.resources, state.money) },
                            categories = categories,
                            category = category,
                            onCategory = { category = it },
                            // Strona jest "zamknieta", gdy ZADNEJ pozycji z niej nie da sie
                            // jeszcze wykuc - wtedy klodka na kropce mowi prawde o calej stronie.
                            pageLocked = { index -> pages.getOrNull(index).orEmpty().all { it.locked } },
                            onSelect = { selectedKey = it },
                            onPage = { page = it },
                            modifier = Modifier.heightIn(max = 340.dp)
                        )
                    }

                    TrackRail(
                        selected = track,
                        onSelect = { track = it },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = RAIL_STRADDLE)
                    )
                  }
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

/* ---------------------------------------- Rail torow ---------------------------------------- */

/**
 * Pionowy pasek przy prawej krawedzi: dla kogo kujemy. Stoi z boku, a nie na gorze, bo to wybor
 * niezalezny od trybu - przelaczenie go ma zmienic pule, a nie to, co gracz wlasnie robi.
 */
/** O ile kafelek railu wystaje poza prawy bok panelu (i o tyle samo w niego wchodzi). */
private val RAIL_STRADDLE = 16.dp

@Composable
private fun TrackRail(
    selected: ForgeTrack,
    onSelect: (ForgeTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
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
    item: Item,
    cost: CraftCost,
    /** Ile sztuk juz mam. Zastapilo "owned: Boolean" - teraz wykuwa sie w nieskonczonosc. */
    ownedCount: Int,
    affordable: Boolean,
    /** Niepusty = prog kuzni jeszcze zamkniety, wiec przycisk nie dziala mimo surowcow. */
    lockedTier: ItemTier?,
    ironHave: Int,
    onForge: () -> Unit
) {
    val frame = RarityPalette.frame(item.rarity)
    val element = MonsterGear.elementFor(item.id)

    FramedPanel(modifier = Modifier.fillMaxWidth(), title = "Forge: ${item.name}") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MaterialSlot(icon = "🪵")
            Spacer(modifier = Modifier.size(6.dp))
            ForgeRing(frame = frame, active = affordable) {
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

        // Opis + statystyki + ZDOLNOSCI w jednym bloku o stalej wysokosci - patrz [ItemInfoBlock]
        // po powod, dla ktorego wysokosc jest sztywna (menu skakalo przy zmianie przedmiotu).
        ItemInfoBlock(item = item)

        CostRow(cost = cost)

        // Licznik zamiast blokady: wykucie kolejnej sztuki jest normalnym ruchem, bo kazda
        // z nich mozna zalozyc innemu kompanowi.
        if (ownedCount > 0) {
            Text(
                text = "You already have $ownedCount",
                color = ParchmentPalette.InkMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }

        ParchmentButton(
            text = if (lockedTier != null) "LOCKED" else "FORGE",
            enabled = affordable && lockedTier == null,
            modifier = Modifier.fillMaxWidth(),
            onClick = onForge
        )

        // Zamkniety prog mowi WPROST, co go otwiera - "LOCKED" bez powodu wyglada jak blad gry.
        lockedTier?.let { tier ->
            Text(
                text = "${tier.label} gear unlocks after clearing ${tier.requiredStages()} stages.",
                color = Color(0xFF9E2B20),
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }

        // Wygaszony przycisk musi powiedziec, DLACZEGO jest wygaszony - inaczej ekran wyglada
        // na zepsuty. Ruda jest tu kluczowa: to jedyny material, ktorego nie produkuje miasto.
        if (lockedTier == null && !affordable) {
            Text(
                text = if (cost.iron > ironHave) {
                    "Not enough ore ($ironHave of ${cost.iron}). Ore only drops in dungeons."
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

    FramedPanel(
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

        // Tabelka PRZED -> PO plus zdolnosci, w bloku o STALEJ wysokosci. Wysokosc jest sztywna
        // z tego samego powodu co w warsztacie kucia: liczba statystyk rozni sie miedzy
        // przedmiotami, wiec bez tego panel kurczyl sie i rozciagal przy kazdej zmianie sztuki.
        Box(modifier = Modifier.fillMaxWidth().height(UPGRADE_INFO_HEIGHT)) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Same statystyki, ktore przedmiot NAPRAWDE ma - pusta tabelka na przedmiocie bez
                // statystyk mowilaby, ze ulepszanie nic nie da, i mialaby racje.
                val statTypes = current.asMap().keys.toList()
                if (statTypes.isEmpty()) {
                    Text(
                        text = "This item has no stats to scale - only its effect.",
                        color = ParchmentPalette.InkMuted,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
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

                // Zdolnosci NIE zmieniaja sie z poziomem (ulepszenie podbija liczby, nie reguly) -
                // ale gracz musi je widziec tutaj, bo to na ich podstawie decyduje, ktora sztuke
                // warto pchac dalej.
                if (item.effects.isNotEmpty()) {
                    ItemInfoBlock(
                        item = item,
                        height = UPGRADE_EFFECTS_HEIGHT,
                        showDescription = false,
                        showStats = false
                    )
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
    page: List<ForgeEntry>,
    pageCount: Int,
    pageIndex: Int,
    selectedKey: String?,
    affordable: (ForgeEntry) -> Boolean,
    categories: List<ItemCategory>,
    category: ItemCategory?,
    onCategory: (ItemCategory?) -> Unit,
    pageLocked: (Int) -> Boolean,
    onSelect: (String) -> Unit,
    onPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gap = 7.dp
    FramedPanel(modifier = modifier.fillMaxWidth()) {
        CategoryBar(categories = categories, selected = category, onSelect = onCategory)

        // Kafelek dostaje STALY bok liczony z tego, co realnie zostalo na ekranie - i z szerokosci,
        // i z wysokosci. Wczesniej kafelki byly kwadratami "po szerokosci" (weight + aspectRatio),
        // wiec cztery rzedy zawsze mialy ~380dp i na nizszych telefonach siatka wychodzila pod
        // dolna krawedz. Teraz nigdy nie przekracza przydzielonego miejsca.
        ParchmentInset(modifier = Modifier.fillMaxWidth().weight(1f)) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val byWidth = (maxWidth - gap * (COLUMNS - 1)) / COLUMNS
                val byHeight = (maxHeight - gap * (ROWS - 1)) / ROWS
                val tile = minOf(byWidth, byHeight).coerceAtLeast(28.dp)
                Column(
                    verticalArrangement = Arrangement.spacedBy(gap, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    page.chunked(COLUMNS).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                            row.forEach { entry ->
                                RecipeTile(
                                    entry = entry,
                                    selected = entry.key == selectedKey,
                                    affordable = affordable(entry),
                                    modifier = Modifier.size(tile),
                                    onClick = { onSelect(entry.key) }
                                )
                            }
                            // Niepelny ostatni rzad: puste kafelki tej samej wielkosci trzymaja
                            // siatke wyrownana do lewej zamiast rozjezdzac ja na srodku.
                            repeat(COLUMNS - row.size) {
                                Box(modifier = Modifier.size(tile))
                            }
                        }
                    }
                }
            }
        }

        PageDots(count = pageCount, selected = pageIndex, locked = pageLocked, onSelect = onPage)
    }
}

/**
 * Pasek polek nad siatka: WSZYSTKO / bron / pancerz / bizuteria.
 *
 * Ikony zamiast nazw, bo cztery slowa po angielsku nie mieszcza sie w szerokosci panelu na
 * mniejszym telefonie, a symbol miecza i tarczy czyta sie bez tlumaczenia.
 */
@Composable
private fun CategoryBar(
    categories: List<ItemCategory>,
    selected: ItemCategory?,
    onSelect: (ItemCategory?) -> Unit
) {
    if (categories.size <= 1) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
    ) {
        CategoryChip(label = "ALL", active = selected == null, onClick = { onSelect(null) })
        categories.forEach { cat ->
            CategoryChip(
                label = cat.icon,
                active = selected == cat,
                // Ponowne stukniecie w aktywna polke zdejmuje filtr - inaczej powrot do calosci
                // wymagalby trafienia w maly przycisk "ALL" po drugiej stronie paska.
                onClick = { onSelect(if (selected == cat) null else cat) }
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(24.dp)
            .widthIn(min = 40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) ParchmentPalette.Button else ParchmentPalette.Inset)
            .border(
                width = 2.dp,
                color = if (active) ParchmentPalette.ButtonEdge else ParchmentPalette.FrameLight,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) ParchmentPalette.Ink else ParchmentPalette.InkMuted,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun RecipeTile(
    entry: ForgeEntry,
    selected: Boolean,
    affordable: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val item = entry.item
    val level = entry.level
    val owned = entry.ownedCount > 0
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
            .alpha(if (entry.locked) 0.5f else if (owned || affordable) 1f else 0.45f),
        contentAlignment = Alignment.Center
    ) {
        Text(text = item.icon(), fontSize = 30.sp)

        // Zamkniety prog: klodka NA ikonie, ale przedmiot nadal widoczny i klikalny. Ukrycie go
        // odbieraloby graczowi jedyny powod, dla ktorego ma isc dalej w kampanie - zeby chciec
        // Titan's Carapace, trzeba najpierw wiedziec, ze istnieje.
        if (entry.locked) {
            Text(text = "🔒", fontSize = 22.sp)
        }

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

/** Staly budzet miejsca na tabelke PRZED -> PO i zdolnosci w warsztacie ulepszania. */
private val UPGRADE_INFO_HEIGHT = 118.dp

/** Ile z tego przypada na liste zdolnosci. */
private val UPGRADE_EFFECTS_HEIGHT = 44.dp

private const val COLUMNS = 4

/** Rzedy na jednej "tablicy". Trzy zamiast czterech - siatka mniej sciska warsztat i cala
 *  tresc miesci sie na jednym ekranie bez scrolla; reszta receptur idzie na kolejna strone. */
private const val ROWS = 3

/** Ile kafelkow miesci sie na jednej "tablicy" - [ROWS] rzedow po [COLUMNS]. */
private const val PAGE_SIZE = ROWS * COLUMNS
