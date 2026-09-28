package com.lernhero.home

import com.lernhero.home.tutorial.TutorialTarget
import com.lernhero.home.tutorial.tutorialTarget
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.monetization.AdRewards
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.reader.todayDateKey
import com.lernhero.shared.isDebugBuild
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.jetbrains.compose.resources.imageResource
import kotlin.math.max
import kotlin.math.roundToInt

private val Muted = AppPalette.TextMuted

/** Ciemnosc, w ktorej tonie scena poza kadrem mapy - patrz [TownMap]. */
private val VoidDark = Color(0xFF120E18)

/**
 * Przelacznik calego rzedu linkow deweloperskich na Domu (podglady modeli + przycisk dosypujacy
 * zasoby). Czyta TYP BUILDU, wiec do sklepu nie ma prawa pojsc widoczny cheat.
 */
private val SHOW_DEV_TOOLS: Boolean get() = isDebugBuild

/**
 * Wymiary grafiki mapy w pikselach. Sa tu na sztywno, bo to wzgledem NICH liczone sa wszystkie
 * pozycje budynkow - [Painter.intrinsicSize] dalby te sama liczbe, ale dopiero po wczytaniu
 * obrazu, wiec uklad przeskakiwalby przy pierwszej klatce.
 *
 * Przy podmianie grafiki trzeba zmienic te dwie liczby (i przemierzyc ulamki w [townSpots]).
 */
private const val MAP_WIDTH = 768f
private const val MAP_HEIGHT = 1512f

/**
 * Ile wierszy pikseli z krawedzi grafiki rozciagamy na pas poza mapa. Kilka, a nie jeden:
 * pojedynczy wiersz lapie szum kompresji i potrafi wyjsc w paskach.
 */
private const val EDGE_BLEED_PX = 4

/**
 * O ile podnosimy cala mape ponad dolna krawedz ekranu - miasto stalo zbyt nisko, przyklejone
 * dokladnie do dolu. Zapas, ktory ten offset otwiera pod mapa, wypelnia juz istniejacy pas
 * "most schodzi w otchlan" (patrz nizej, `bottomStart < screenHeight`), wiec podniesienie mapy
 * nie wymaga zadnej dodatkowej logiki - most po prostu wisi troche wyzej nad ciemnoscia.
 */
private val MAP_LIFT = 16.dp


/**
 * Jedno klikalne miejsce na mapie Domu.
 *
 * Wszystkie cztery liczby to UŁAMKI GRAFIKI (0..1), a nie ekranu - i to jest tu cala sztuczka.
 * Mapa rysuje sie przez [ContentScale.Crop], wiec na kazdym telefonie ma inna skale i inne
 * przyciecie bokow; gdyby pozycje byly ulamkami EKRANU, kopalnia zjezdzalaby z wejscia do jaskini
 * przy kazdej innej proporcji ekranu. Liczone wzgledem grafiki i przepuszczone przez to samo
 * przeksztalcenie, co ona ([TownMap]), zostaja przyklejone do budynku na zawsze.
 *
 * [x]/[y] to SRODEK budynku, [width]/[height] jego bryla - czyli obszar, ktory realnie da sie
 * tapnac. Podpis wisi pod ta bryla.
 */
private data class TownSpot(
    val label: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val onClick: () -> Unit,
    val target: TutorialTarget? = null
)

/**
 * Ekran Domu - jedna statyczna mapa osady, po ktorej sie klika.
 *
 * Zastapila cztery lewitujace wyspy. Tamte byly czterema osobnymi grafikami bez wspolnej
 * perspektywy, swiatla ani gruntu - kazda ladna z osobna, razem nie skladaly sie w zadne miejsce,
 * tylko w liste przyciskow udajaca mape. Tutaj budynki stoja w jednym kadrze, wiec Dom wyglada jak
 * miejsce, do ktorego sie wraca - a klikalnosc jest doklejona do tego kadru, nie odwrotnie.
 */
@Composable
fun HomeScreen(
    viewModel: PlayerViewModel,
    /** Marginesy pod paski gry - dotycza PODPISOW i linkow dev, nie samej mapy (ta idzie pod paski). */
    contentPadding: PaddingValues = PaddingValues(0.dp),
    /**
     * Ile paski REALNIE zakrywaja farba. Mniej niz [contentPadding], bo tamto rezerwuje miejsce
     * takze na portret zwieszony pod plyta gornego paska - a przez ten pas widac tlo. Mapa musi
     * siegac dokladnie tam, dokad siega farba paskow, ani mniej (zostaje szpara), ani wiecej
     * (niepotrzebnie przycina boki).
     */
    coverPadding: PaddingValues = contentPadding,
    onOpenQuarry: () -> Unit = {},
    onOpenForge: () -> Unit = {},
    onOpenSummonCenter: () -> Unit = {},
    /** Swiatynia - osiagniecia i statystyki nauki. */
    onOpenTemple: () -> Unit = {},
    onOpenKnightPreview: () -> Unit,
    onOpenModularKnightPreview: () -> Unit,
    onOpenMonsterAnchorLab: () -> Unit,
    onOpenMonsterAnimationLab: () -> Unit,
    onOpenChestPreview: () -> Unit = {}
) {
    // Ulamki zmierzone na samej grafice (siatka procentowa nalozona na plik), a nie dobrane na oko:
    // srodek kazdego wpisu lezy na bryle budynku, a nie na jego podpisie czy cieniu.
    val spots = listOf(
        TownSpot(
            label = "Mine",
            x = 0.185f, y = 0.375f, width = 0.30f, height = 0.138f,
            onClick = onOpenQuarry
        ),
        TownSpot(
            label = "Temple",
            x = 0.790f, y = 0.340f, width = 0.32f, height = 0.190f,
            onClick = onOpenTemple
        ),
        TownSpot(
            label = "Summon",
            x = 0.280f, y = 0.612f, width = 0.40f, height = 0.164f,
            onClick = onOpenSummonCenter,
            target = TutorialTarget.SummonAltar
        ),
        TownSpot(
            label = "Forge",
            x = 0.850f, y = 0.625f, width = 0.33f, height = 0.172f,
            onClick = onOpenForge
        )
    )

    val state by viewModel.uiState.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        TownMap(spots = spots, contentPadding = contentPadding, coverPadding = coverPadding)

        AdGoldButton(
            remaining = state.adGold.remainingOn(todayDateKey(), AdRewards.GOLD_ADS_PER_DAY),
            perDay = AdRewards.GOLD_ADS_PER_DAY,
            reward = AdRewards.goldFor(state.progress.level),
            busy = state.watchingAd,
            isPremium = isPremium,
            pulse = state.adRewardPulse,
            lastReward = state.lastAdReward,
            note = state.adNote,
            onClick = viewModel::watchAdForGold,
            onNoteShown = viewModel::clearAdNote,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = contentPadding.calculateTopPadding() + 6.dp, end = 8.dp)
        )

        // Za flaga: "dev: +zasoby" doklada pelen worek surowcow jednym tapnieciem, wiec w
        // buildzie sklepowym ten caly rzad musi byc wylaczony.
        if (SHOW_DEV_TOOLS) {
            var bestiaryOpen by remember { mutableStateOf(false) }
            DevLinksRow(
                onOpenBestiary = { bestiaryOpen = true },
                onOpenKnightPreview = onOpenKnightPreview,
                onOpenModularKnightPreview = onOpenModularKnightPreview,
                onOpenMonsterAnchorLab = onOpenMonsterAnchorLab,
                onOpenMonsterAnimationLab = onOpenMonsterAnimationLab,
                onOpenChestPreview = onOpenChestPreview,
                onGrantResources = viewModel::grantTestResources,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(contentPadding)
            )
            if (bestiaryOpen) {
                com.lernhero.home.dev.BestiaryDevOverlay(onClose = { bestiaryOpen = false })
            }
        }
    }
}

/**
 * Mapa i przypiete do niej punkty.
 *
 * Grafika idzie POD paski gry (pelny kadr), bo obraz urwany rowno pod paskiem zdradza, ze to
 * obrazek w ramce, a nie miejsce. Punkty licza sie z tego samego przeksztalcenia, ktorym rysuje
 * sie obraz - stad recznie liczona skala zamiast zwyklego [Modifier.fillMaxSize] na kazdym z nich.
 */
@Composable
private fun TownMap(
    spots: List<TownSpot>,
    contentPadding: PaddingValues,
    coverPadding: PaddingValues = contentPadding
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val boxWidth = with(density) { maxWidth.toPx() }
        val boxHeight = with(density) { maxHeight.toPx() }

        // Mapa musi pokryc PAS MIEDZY PASKAMI, a nie caly ekran - i to jest tu cala sztuczka.
        //
        // Grafika jest w proporcjach 0.59, telefon ma ~0.45, wiec czegos trzeba sie pozbyc.
        // Probowalem obu skrajnosci i obie byly zle:
        //  - dopasowanie do szerokosci zostawialo pas u gory i u dolu; domalowywana rozciagnieta
        //    krawedz wychodzila spod paska zakladek i bylo widac smugi;
        //  - wypelnienie calego ekranu ucinalo po ~12% z boku, czyli komin kuzni (a z nim dym)
        //    i choragiew kopalni.
        //
        // Pasek gry i pasek zakladek sa nieprzezroczyste, wiec mapa nie musi siegac pod nie
        // trescia - wystarczy, ze pokryje to, co realnie widac. Skala bierze sie wiec z WIEKSZEJ
        // z dwoch potrzeb: wypelnic szerokosc albo wypelnic wysokosc widocznego pasa. Na zwyklym
        // telefonie wygrywa szerokosc (nie ginie ani piksel w poziomie), a im wyzszy ekran, tym
        // bardziej dokladamy skali - ale zawsze mniej, niz zadalby pelny ekran.
        val safeTop = with(density) { coverPadding.calculateTopPadding().toPx() }
        val safeBottom = with(density) { coverPadding.calculateBottomPadding().toPx() }
        val bandHeight = (boxHeight - safeTop - safeBottom).coerceAtLeast(1f)

        val scale = max(boxWidth / MAP_WIDTH, bandHeight / MAP_HEIGHT)
        val drawnWidth = MAP_WIDTH * scale
        val drawnHeight = MAP_HEIGHT * scale
        val left = (boxWidth - drawnWidth) / 2f
        // Mapa jest przyklejona DOLEM DO DOLU EKRANU, a nie wysrodkowana - i to nie jest detal.
        //
        // Zapas musi gdzies pojsc i obie krawedzie znosza to zupelnie inaczej: u gory jest gladkie
        // niebo (odchylenie kolorow w poziomie to 5 na 255), wiec jego przedluzenie jest
        // niewidoczne; u dolu jest most rysowany w perspektywie, ktorego nie da sie przedluzyc bez
        // rozmazania go w pionowe pasy. Wysrodkowanie dzielilo zapas po rowno, czyli zawsze
        // oddawalo czesc tej gorszej krawedzi. Teraz dolu nie przedluzamy w ogole - most konczy
        // sie dokladnie tam, gdzie konczy sie ekran - a caly zapas bierze na siebie niebo.
        val top = boxHeight - drawnHeight - with(density) { MAP_LIFT.toPx() }

        val map = imageResource(Resources.Image.townHome)
        Canvas(modifier = Modifier.matchParentSize()) {
            val mapTop = top.roundToInt()
            val mapLeft = left.roundToInt()
            val mapWidth = drawnWidth.roundToInt()
            val mapHeight = drawnHeight.roundToInt()
            val screenWidth = size.width.roundToInt()
            val screenHeight = size.height.roundToInt()

            // NAJPIERW nieprzezroczysty spod. Bez niego w pasie nad i pod mapa przeswitywalo
            // globalne tlo aplikacji - zupelnie inne niebo - i to ono wygladalo jak "grafika nie
            // siega do konca ekranu". Pas nie byl pusty; byl wypelniony cudzym obrazkiem.
            drawRect(color = VoidDark)

            drawImage(
                image = map,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(map.width, map.height),
                dstOffset = IntOffset(mapLeft, mapTop),
                dstSize = IntSize(mapWidth, mapHeight),
                filterQuality = FilterQuality.High
            )

            // Gdy pas jednak zostaje (bardzo wysoki ekran), przedluzamy mape JEJ WLASNA krawedzia
            // i dopiero to przedluzenie wygaszamy w ciemnosc. Samo rozciagniecie dawalo widoczne
            // smugi kamienia, a sam ciemny prostokat - twarda linie w poprzek ekranu. Zlozone
            // razem daja szew, ktorego nie widac: przy krawedzi kolory zgadzaja sie co do piksela,
            // a zanim rozciagniecie zdazy sie zdradzic, jest juz ciemno.
            if (mapTop > 0) {
                drawImage(
                    image = map,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(map.width, EDGE_BLEED_PX),
                    dstOffset = IntOffset(mapLeft, 0),
                    dstSize = IntSize(mapWidth, mapTop),
                    filterQuality = FilterQuality.High
                )
                // To jedyne przedluzenie, ktore zostalo - i celowo tylko ono. Niebo ciemnieje ku
                // gorze, wiec wygaszenie nie jest zaklejaniem bledu, tylko ciagiem tego, co
                // grafika juz robi: im wyzej, tym glebsza noc.
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to VoidDark.copy(alpha = 0.80f),
                        1f to VoidDark.copy(alpha = 0f),
                        startY = 0f,
                        endY = mapTop.toFloat()
                    ),
                    size = Size(size.width, mapTop.toFloat())
                )
            }

            // Przy przyklejeniu dolem ten pas ma zerowa wysokosc - zostaje wylacznie jako
            // zabezpieczenie przed zaokragleniem do pelnych pikseli.
            val bottomStart = mapTop + mapHeight
            if (bottomStart < screenHeight) {
                val gap = (screenHeight - bottomStart).toFloat()
                drawImage(
                    image = map,
                    srcOffset = IntOffset(0, map.height - EDGE_BLEED_PX),
                    srcSize = IntSize(map.width, EDGE_BLEED_PX),
                    dstOffset = IntOffset(mapLeft, bottomStart),
                    dstSize = IntSize(mapWidth, gap.roundToInt()),
                    filterQuality = FilterQuality.High
                )
                // Dol gasimy SZYBKO - na dole grafiki jest most z perspektywa, wiec jego
                // rozciagniecie rozmazuje sie w pasy. Pelna ciemnosc wchodzi juz w polowie pasa:
                // przy samej krawedzi kolory zgadzaja sie co do piksela, a zanim rozciagniecie
                // zdazy sie zdradzic, nie ma juz czego ogladac. Most po prostu schodzi w otchlan,
                // nad ktora cala ta wyspa wisi.
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to VoidDark.copy(alpha = 0f),
                        0.5f to VoidDark.copy(alpha = 0.92f),
                        1.0f to VoidDark,
                        startY = bottomStart.toFloat(),
                        endY = size.height
                    ),
                    topLeft = Offset(0f, bottomStart.toFloat()),
                    size = Size(size.width, gap)
                )
            }

            // Ekran szerszy niz mapa (tablet) - boki juz sa ciemne od podkladu, wiec nie ma tu
            // nic do dorysowania.
            if (screenWidth > mapWidth) Unit
        }

        // Zycie miasta - ogien, dym, runy, iskry. Rysowane NA mapie, ale POD podpisami: swiatlo
        // ma padac na kamien, a nie na napisy.
        TownAmbience(
            frame = MapFrame(left = left, top = top, width = drawnWidth, height = drawnHeight),
            modifier = Modifier.matchParentSize()
        )

        spots.forEach { spot ->
            val spotWidth = spot.width * drawnWidth
            val spotHeight = spot.height * drawnHeight
            val centerX = left + spot.x * drawnWidth
            // Jedno zrodlo dotyku dla bryly i podpisu - tapniecie budynku ma animowac JEGO
            // podpis, mimo ze to dwa osobne elementy ukladu.
            val interaction = remember { MutableInteractionSource() }

            // Bryla budynku: przyklejona do grafiki co do piksela. Moze czesciowo wyjsc poza
            // ekran przy ciasnym kadrze i to jest w porzadku - klika sie to, co widac.
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { (centerX - spotWidth / 2f).toDp() },
                        y = with(density) { (top + spot.y * drawnHeight - spotHeight / 2f).toDp() }
                    )
                    .size(
                        width = with(density) { spotWidth.toDp() },
                        height = with(density) { spotHeight.toDp() }
                    )
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = spot.onClick
                    )
                    .then(spot.target?.let { Modifier.tutorialTarget(it) } ?: Modifier)
            )

            // Podpis NIE jest dzieckiem bryly, tylko osobno przypietym elementem - i to celowo.
            // Przy ciasnym kadrze srodek kuzni wypada kilkadziesiat pikseli od krawedzi, wiec
            // podpis wysrodkowany na niej wychodzil poza ekran ("FORG"). [BiasAlignment] ustawia
            // go proporcjonalnie do pozycji budynku, ale NIGDY poza rodzica - czyli sam sie
            // docisknie do krawedzi, zamiast sie o nia uciac.
            TownSpotLabel(
                label = spot.label,
                interactionSource = interaction,
                modifier = Modifier
                    .align(
                        BiasAlignment(
                            horizontalBias = ((centerX / boxWidth) * 2f - 1f).coerceIn(-1f, 1f),
                            verticalBias = -1f
                        )
                    )
                    .offset(
                        y = with(density) {
                            (top + (spot.y + spot.height / 2f) * drawnHeight).toDp()
                        }
                    )
            )
        }
    }
}

/**
 * Podpis budynku.
 *
 * Ma wlasne, ciemne tlo, a nie sam cien - mapa jest gesta i roznokolorowa (rozowe niebo,
 * pomaranczowy ogien, szary kamien), wiec tekst z samym cieniem gubil sie dokladnie tam, gdzie
 * bylo najciekawiej. Pigulka jest przezroczysta na tyle, zeby nie zaklejac grafiki.
 *
 * Nie ma tu ramki wokol budynku ani stalej "poswiaty": mapa ma wygladac jak obraz, a nie jak
 * plansza z zaznaczonymi polami. Ze cos jest klikalne, mowi podpis - i reakcja na dotyk, ktora
 * przychodzi z bryly budynku przez wspolny [interactionSource].
 */
@Composable
private fun TownSpotLabel(
    label: String,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    val pressed by interactionSource.collectIsPressedAsState()
    val press by animateFloatAsState(
        targetValue = if (pressed) 1.10f else 1f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "townPress"
    )

    Text(
        text = label.uppercase(),
        color = AppPalette.Gold,
        fontFamily = FontPixel(),
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        style = TextStyle(
            shadow = Shadow(color = Color.Black.copy(alpha = 0.9f), blurRadius = 8f)
        ),
        modifier = modifier
            .scale(press)
            .clip(RoundedCornerShape(50))
            .background(Color(0xCC120E0A))
            .border(1.5.dp, Color(0x99C8A45A), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

/** Ekrany deweloperskie - male, dyskretne linki, zeby nie mylily sie z prawdziwymi funkcjami gry. */
@Composable
private fun DevLinksRow(
    onOpenKnightPreview: () -> Unit,
    onOpenModularKnightPreview: () -> Unit,
    onOpenMonsterAnchorLab: () -> Unit,
    onOpenMonsterAnimationLab: () -> Unit,
    onOpenChestPreview: () -> Unit,
    onGrantResources: () -> Unit,
    onOpenBestiary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        // Jedno tapniecie = pelen worek wszystkiego, czym da sie cokolwiek przetestowac.
        // Wyroznione kolorem, bo w odroznieniu od pozostalych linkow dev ta rzecz ZMIENIA ZAPIS.
        Text(
            text = "zoo",
            color = Muted.copy(alpha = 0.5f),
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            modifier = Modifier.clickable(onClick = onOpenBestiary).padding(horizontal = 4.dp, vertical = 8.dp)
        )
        Text(
            text = "dev: +zasoby",
            color = Color(0xFF5FD08A),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.clickable(onClick = onGrantResources).padding(8.dp)
        )
        Text(
            text = "dev: pivots",
            color = Muted.copy(alpha = 0.5f),
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            modifier = Modifier.clickable(onClick = onOpenKnightPreview).padding(8.dp)
        )
        Text(
            text = "dev: preview",
            color = Muted.copy(alpha = 0.5f),
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            modifier = Modifier.clickable(onClick = onOpenModularKnightPreview).padding(8.dp)
        )
        Text(
            text = "dev: anchors",
            color = Muted.copy(alpha = 0.5f),
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            modifier = Modifier.clickable(onClick = onOpenMonsterAnchorLab).padding(8.dp)
        )
        Text(
            text = "dev: anim",
            color = Muted.copy(alpha = 0.5f),
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            modifier = Modifier.clickable(onClick = onOpenMonsterAnimationLab).padding(8.dp)
        )
        Text(
            text = "dev: chest",
            color = Muted.copy(alpha = 0.5f),
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            modifier = Modifier.clickable(onClick = onOpenChestPreview).padding(8.dp)
        )
    }
}
