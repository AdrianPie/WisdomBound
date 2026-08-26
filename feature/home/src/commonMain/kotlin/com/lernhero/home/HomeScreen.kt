package com.lernhero.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import androidx.compose.material3.Text
import org.jetbrains.compose.resources.painterResource

private val Muted = AppPalette.TextMuted

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
    onOpenSawmill: () -> Unit = {},
    onOpenQuarry: () -> Unit = {},
    onOpenForge: () -> Unit = {},
    onOpenSummonCenter: () -> Unit = {},
    onOpenKnightPreview: () -> Unit,
    onOpenModularKnightPreview: () -> Unit,
    onOpenMonsterAnchorLab: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CityMap(
            onOpenSawmill = onOpenSawmill,
            onOpenQuarry = onOpenQuarry,
            onOpenForge = onOpenForge,
            onOpenSummonCenter = onOpenSummonCenter
        )

        DevLinksRow(
            onOpenKnightPreview = onOpenKnightPreview,
            onOpenModularKnightPreview = onOpenModularKnightPreview,
            onOpenMonsterAnchorLab = onOpenMonsterAnchorLab,
            onGrantResources = viewModel::grantTestResources,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
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
            CityBuildingSpot(painterResource(Resources.CityIsland.sawmill), "Tartak", 0.02f, onOpenSawmill),
            CityBuildingSpot(painterResource(Resources.CityIsland.quarry), "Kamieniolom", 0.98f, onOpenQuarry),
            CityBuildingSpot(painterResource(Resources.CityIsland.forge), "Kuznia", 0.02f, onOpenForge),
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
    }
}
