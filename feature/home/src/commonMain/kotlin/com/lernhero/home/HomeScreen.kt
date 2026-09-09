package com.lernhero.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.player.PlayerProgress
import com.lernhero.domain.town.TownResources
import com.lernhero.game.character.HeroLook
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import androidx.compose.material3.Text
import org.jetbrains.compose.resources.painterResource
import com.lernhero.shared.isDebugBuild

private val Muted = AppPalette.TextMuted

/**
 * Przelacznik calego rzedu linkow deweloperskich na Domu (podglady modeli + przycisk dosypujacy
 * zasoby). MUSI byc false przed kazdym wydaniem do sklepu - przelacz recznie na true lokalnie,
 * gdy testujesz, i wroc do false przed buildem release. Docelowo warto to zastapic prawdziwa
 * flaga per-platform (BuildConfig.DEBUG na Androidzie i jej odpowiednik na iOS/desktop), ale to
 * jedna linijka wystarcza, zeby nie wypuscic przycisku "darmowe zloto" do sklepu.
 */
private val SHOW_DEV_TOOLS: Boolean get() = isDebugBuild

/**
 * Jak szeroka (wzgledem szerokosci ekranu) jest jedna wyspa - praktycznie caly ekran.
 *
 * Kazdy PNG ma wokol wyspy przezroczysty margines (tresc zajmuje ~78% szerokosci i ~72% wysokosci
 * kadru), wiec kontenery moga na siebie nachodzic o ~20% i grafiki i tak sie nie zetkna. To wlasnie
 * ten margines pozwala trzymac wyspy tak duze przy czterech sztukach bez scrolla.
 */
private const val ISLAND_WIDTH_FRACTION = 0.48f

/** Zmierzone na PNG-ach: gdzie w kadrze zaczyna sie i jak wysoka jest sama wyspa (reszta to alpha). */
private const val ISLAND_ART_TOP_FRACTION = 0.17f
private const val ISLAND_ART_HEIGHT_FRACTION = 0.72f

/**
 * Jedna wyspa-budynek na mapie Miasta. Pozycja pionowa liczy sie z kolejnosci na liscie (patrz
 * CityMap - kazda wyspa dostaje wlasny "pas" wysokosci), tu trzymamy tylko poziomy ulamek
 * (lekki zygzak lewo/prawo) i etykiete.
 */
private data class CityBuildingSpot(
    val icon: Painter,
    val label: String,
    val xFraction: Float,
    val onClick: () -> Unit
)

/**
 * Ekran Miasta - tlo (niebo/wyspy) rysowane wyzej w HomeNavGraph, wspolne dla calej apki.
 * Cztery wyspy-budynki (tartak, kamieniolom, kuznia, summon center) rozlozone tak, zeby caly
 * uklad miescil sie na ekranie bez scrolla - kazda to osobna, klikalna "instancja".
 */
@Composable
fun HomeScreen(
    viewModel: PlayerViewModel,
    onOpenCharacter: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenSawmill: () -> Unit = {},
    onOpenQuarry: () -> Unit = {},
    onOpenForge: () -> Unit = {},
    onOpenSummonCenter: () -> Unit = {},
    onOpenKnightPreview: () -> Unit,
    onOpenModularKnightPreview: () -> Unit,
    onOpenMonsterAnchorLab: () -> Unit,
    onOpenMonsterAnimationLab: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Karta postaci nie ma juz wlasnej zakladki - jej skrot (kto, jaki poziom, ile zasobow)
        // siedzi u gory Domu i jest wejsciem na pelny ekran ekwipunku.
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeHeroBar(
                name = state.name,
                progress = state.progress,
                money = state.money,
                resources = state.resources,
                knight = state.knightEquipment,
                look = state.heroLook,
                onClick = onOpenCharacter,
                modifier = Modifier.weight(1f)
            )
            SettingsButton(onClick = onOpenSettings)
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CityMap(
                onOpenSawmill = onOpenSawmill,
                onOpenQuarry = onOpenQuarry,
                onOpenForge = onOpenForge,
                onOpenSummonCenter = onOpenSummonCenter
            )

            // Za flaga: "dev: +zasoby" doklada pelen worek surowcow jednym tapnieciem, wiec w
            // buildzie sklepowym ten caly rzad musi byc wylaczony - inaczej kazdy gracz ma darmowy
            // cheat bez zadnego hackowania, samym interfejsem. Firestore Security Rules i tak by to
            // przycielo (limit delty na zapis), ale nie ma po co w ogole pokazywac przycisku.
            //
            // Warunek czyta TYP BUILDU (isDebugBuild), a nie reczna stala - to bylo jedyne, co
            // stalo miedzy release'em a widocznym cheatem, i opieralo sie na pamieci czlowieka.
            if (SHOW_DEV_TOOLS) {
                DevLinksRow(
                    onOpenKnightPreview = onOpenKnightPreview,
                    onOpenModularKnightPreview = onOpenModularKnightPreview,
                    onOpenMonsterAnchorLab = onOpenMonsterAnchorLab,
                    onOpenMonsterAnimationLab = onOpenMonsterAnimationLab,
                    onGrantResources = viewModel::grantTestResources,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 2.dp)
                )
            }
        }
    }
}

/** Srednica kola-awatara. Wieksza niz wysokosc paska info - kolo ma nad pasek wystawac. */
private val HERO_AVATAR = 56.dp

/** Szerokosc paska poziomu w pasku info - staly, wyrazny, nie przyciety krawedzia. */
private val HERO_XP_BAR_W = 132.dp

/** Przycina pusta przestrzen nad/pod linia tekstu - pixelowa czcionka niesie jej sporo. */
private val TrimmedLine = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.Both
)

/**
 * Skrot karty postaci na gorze Domu. Dwie OSOBNE rzeczy w jednym rzedzie:
 *  - lewa: kolo-awatar + przylegly pasek info (imie, XP, pasek poziomu). Pasek jest zaokraglony
 *    od strony kola, a scienty na prosto od prawej. Cala lewa strona jest klikalna.
 *  - prawa: samodzielne, przezroczyste pastylki zasobow (zloto, esencja).
 * Wszystkie tla sa polprzezroczyste, zeby niebo Miasta przebijalo pod spodem.
 */
@Composable
private fun SettingsButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x992A2018))
            .border(1.dp, Color(0x996B5A38), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(text = "⚙", fontSize = 18.sp)
    }
}

@Composable
private fun HomeHeroBar(
    name: String,
    progress: PlayerProgress,
    money: Int,
    resources: TownResources,
    knight: KnightEquipment,
    look: HeroLook,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fill by animateFloatAsState(targetValue = progress.xpProgress, label = "homeXp")

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Grupa lewa: kolo + pasek. Owija sie wokol tresci (nie rozpycha pustym panelem);
        // Spacer nizej odpycha zasoby do prawej krawedzi.
        Box(
            modifier = Modifier.clickable(onClick = onClick),
            contentAlignment = Alignment.CenterStart
        ) {
            HeroInfoPanel(
                name = name,
                xpText = "Knight · ${progress.xp} / ${progress.xpForNextLevel} XP",
                fill = fill
            )
            HeroAvatar(knight = knight, look = look, level = progress.level)
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ResourceChip("🪙", money)
            ResourceChip("💎", resources.essence)
        }
    }
}

@Composable
private fun HeroInfoPanel(
    name: String,
    xpText: String,
    fill: Float
) {
    Row(
        modifier = Modifier
            .padding(start = HERO_AVATAR / 2)
            // Lewa strona zaokraglona (chowa sie pod kolem), prawa scienta na prosto - jak prostokat.
            .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
            .background(AppPalette.Panel.copy(alpha = 0.5f))
            .padding(start = HERO_AVATAR / 2 + 10.dp, end = 12.dp, top = 6.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = name.ifBlank { "Sir Lernhero" },
                color = AppPalette.Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                style = TextStyle(lineHeight = 11.sp, lineHeightStyle = TrimmedLine),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = xpText,
                color = AppPalette.TextMuted,
                fontFamily = FontFirst(),
                fontSize = 8.sp,
                style = TextStyle(lineHeight = 8.sp, lineHeightStyle = TrimmedLine),
                maxLines = 1
            )
            Box(
                modifier = Modifier
                    .width(HERO_XP_BAR_W)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AppPalette.Ink.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fill)
                        .clip(RoundedCornerShape(50))
                        .background(AppPalette.Gold)
                )
            }
        }
    }
}

@Composable
private fun HeroAvatar(knight: KnightEquipment, look: HeroLook, level: Int) {
    // Zewnetrzny Box NIE jest przyciety - dzieki temu ramka z poziomem moze SIEDZIEC NA kole,
    // w jego prawym dolnym rogu, zamiast zostac scieta razem z rycerzem.
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(HERO_AVATAR)
                .clip(RoundedCornerShape(50))
                .background(AppPalette.Ink)
                .border(2.dp, AppPalette.Gold, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            // Duzo wieksza klatka rycerza, docisnieta w dol - w kolku widac tors i glowe, nie
            // calego ludzika z mnostwem pustej przestrzeni. requiredSize omija ciasne wiazania.
            ModularKnight(
                equipment = knight,
                look = look,
                sizeDp = 92.dp,
                interactive = false,
                modifier = Modifier.requiredSize(92.dp).offset(y = 21.dp)
            )
        }
        Box(
            modifier = Modifier
                .offset(x = 3.dp, y = 3.dp)
                .clip(RoundedCornerShape(50))
                .background(AppPalette.Ink)
                .border(1.5.dp, AppPalette.Gold, RoundedCornerShape(50))
                .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
            Text(
                text = "$level",
                color = AppPalette.Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                style = TextStyle(lineHeight = 9.sp, lineHeightStyle = TrimmedLine)
            )
        }
    }
}

@Composable
private fun ResourceChip(icon: String, value: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AppPalette.Ink.copy(alpha = 0.5f))
            .border(1.dp, AppPalette.GoldDim.copy(alpha = 0.7f), RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 11.sp)
        Text(
            text = "$value",
            color = AppPalette.Gold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun CityMap(
    onOpenSawmill: () -> Unit,
    onOpenQuarry: () -> Unit,
    onOpenForge: () -> Unit,
    onOpenSummonCenter: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // xFraction = pozycja w WOLNEJ szerokosci (0 = do lewej, 1 = do prawej) - stad zygzak.
        val spots = listOf(
            CityBuildingSpot(painterResource(Resources.CityIsland.sawmill), "Sawmill", 0.02f, onOpenSawmill),
            CityBuildingSpot(painterResource(Resources.CityIsland.quarry), "Quarry", 0.98f, onOpenQuarry),
            CityBuildingSpot(painterResource(Resources.CityIsland.forge), "Forge", 0.02f, onOpenForge),
            CityBuildingSpot(painterResource(Resources.CityIsland.summon), "Summon Center", 0.98f, onOpenSummonCenter)
        )

        // Wyspy sa CELOWO wieksze niz 1/4 wysokosci ekranu - inaczej (przy czterech osobnych,
        // rozlacznych pasach) kazda musialaby zmiescic sie w ~1/4 wysokosci i robila sie malutka,
        // z mnostwem pustego nieba dookola. Zamiast tego rozmiar bierze sie z SZEROKOSCI ekranu,
        // a wyspy ida zygzakiem lewo-prawo i lekko na siebie nachodza w pionie. Nachodzenie nie
        // przeszkadza, bo kazdy PNG ma wokol siebie przezroczysty margines, a zygzak sprawia, ze
        // sasiednie wyspy i tak nie stykaja sie graficznie.
        val islandSize = maxWidth * ISLAND_WIDTH_FRACTION
        val freeWidth = (maxWidth - islandSize).coerceAtLeast(0.dp)
        val lastIndex = (spots.size - 1).coerceAtLeast(1)

        // Widoczna grafika zajmuje tylko srodek kadru - liczymy pozycje wzgledem NIEJ, a nie
        // wzgledem calego (w duzej czesci przezroczystego) PNG, zeby wyspy byly rozlozone rowno
        // optycznie i zeby skrajne nie wychodzily poza ekran.
        val artHeight = islandSize * ISLAND_ART_HEIGHT_FRACTION
        val artTopInset = islandSize * ISLAND_ART_TOP_FRACTION
        val firstCenter = artHeight / 2
        val lastCenter = maxHeight - artHeight / 2
        val centerStep = (lastCenter - firstCenter) / lastIndex

        spots.forEachIndexed { index, spot ->
            val artCenterY = firstCenter + centerStep * index
            CityIslandButton(
                icon = spot.icon,
                label = spot.label,
                size = islandSize,
                floatSeed = index,
                modifier = Modifier.offset(
                    x = freeWidth * spot.xFraction,
                    y = artCenterY - artHeight / 2 - artTopInset
                )
            ) {
                spot.onClick()
            }
        }
    }
}

/**
 * Wyspa jako klikalny przycisk - lewitujaca (bardzo delikatny, powolny ruch gora-dol, spring/tween
 * na sinusoidzie), z podpisem NAD grafika, bez zadnej "pigulki"/obwodki - sama czcionka pixelowa
 * (ta sama co tytuly na gorze ekranu, np. "Dom") z lekkim cieniem, zeby dalo sie ja przeczytac na
 * kazdym tle nieba.
 */
@Composable
private fun CityIslandButton(
    icon: Painter,
    label: String,
    size: Dp,
    floatSeed: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Kazda wyspa ma inny czas/amplitude lewitacji, zeby nie unosily sie idealnie w takt -
    // wyglada bardziej naturalnie/zywo, jak niezalezne obiekty, a nie jeden mechanizm.
    val infiniteTransition = rememberInfiniteTransition(label = "islandFloat$floatSeed")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2600 + floatSeed * 340,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "islandFloatValue$floatSeed"
    )
    val bobAmplitude = (6 + floatSeed % 2 * 2).dp

    // Podpis lezy NA kadrze wyspy (w przezroczystym pasie tuz nad grafika), a nie nad nim - dzieki
    // temu nie zabiera wysokosci, ktora jest tu najcenniejsza, i jedzie razem z lewitujaca wyspa.
    Box(
        modifier = modifier
            .size(size)
            .clickable(onClick = onClick)
            .offset(y = bobAmplitude * floatOffset)
    ) {
        // .size() (nie .width()) na OBU wymiarach - kwadratowe PNG (256x256), a samo .width()
        // zdawalo sie honorowac jakis wewnetrzny preferowany rozmiar malarza zamiast wymuszonego
        // rozmiaru (rozmiar wysp sie nie zmienial mimo zmiany wartosci, mimo ze pozycja - zwykly
        // offset() - reagowala poprawnie).
        Image(
            painter = icon,
            contentDescription = label,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(size)
        )
        Text(
            text = label,
            color = AppPalette.Gold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            style = TextStyle(
                shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 12f)
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = size * (ISLAND_ART_TOP_FRACTION - 0.09f))
                .fillMaxWidth()
        )
    }
}

/** Ekrany deweloperskie - male, dyskretne linki, zeby nie mylily sie z prawdziwymi funkcjami gry. */
@Composable
private fun DevLinksRow(
    onOpenKnightPreview: () -> Unit,
    onOpenModularKnightPreview: () -> Unit,
    onOpenMonsterAnchorLab: () -> Unit,
    onOpenMonsterAnimationLab: () -> Unit,
    onGrantResources: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        // Jedno tapniecie = pelen worek wszystkiego, czym da sie cokolwiek przetestowac:
        // zloto, drewno, kamien, ruda, esencja, losy i XP. Wyroznione kolorem, bo w odroznieniu
        // od pozostalych linkow dev ta rzecz ZMIENIA ZAPIS gracza.
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
    }
}
