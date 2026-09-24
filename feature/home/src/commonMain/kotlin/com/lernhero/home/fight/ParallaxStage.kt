package com.lernhero.home.fight

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Przewijane tlo sceny - kilka warstw jadacych w roznym tempie, zapetlonych bez konca.
 *
 * ## Okno, nie ekran
 *
 * Caly efekt zamyka sie w [ParallaxStage] i jest PRZYCIETY do jego rozmiaru ([clipToBounds]).
 * Warstwy sa szersze i wyzsze niz to okno i wystaja poza nie ze wszystkich stron - to dlatego nie
 * widac, ze cokolwiek sie konczy: ekran pokazuje wycinek swiata, ktory istnieje takze obok.
 *
 * ## Zapetlenie
 *
 * Grafika NIE musi byc zapetlona sama w sobie - o to dba tryb kafelkowania:
 *
 * - [TileMode.Repeat] - kafel za kaflem. Wymaga, zeby prawa krawedz grafiki przechodzila w lewa.
 * - [TileMode.Mirror] - kafel, jego lustrzane odbicie, kafel, odbicie... Szew znika Z DEFINICJI,
 *   bo odbity kafel zaczyna sie dokladnie tym, czym poprzedni sie skonczyl. To jest odpowiedz na
 *   most: nie trzeba go przerysowywac, zeby sie laczyl - wystarczy co drugi raz odbic.
 *   Cena: wzor powtarza sie co DWIE szerokosci i jest symetryczny, wiec nadaje sie do rzeczy
 *   regularnych (most, mur, sciezka), a nie do pejzazu z rozpoznawalnym drzewem.
 * - [TileMode.Scatter] - nie kafel, tylko POJEDYNCZE obiekty (skalka, krzak) stawiane co jakis
 *   czas. Miejsca sa losowe, ale WYLICZONE Z POZYCJI W SWIECIE, a nie wylosowane w trakcie: ta
 *   sama skalka stoi zawsze w tym samym miejscu trasy, wiec nic nie miga przy zmianie orientacji
 *   ekranu ani po powrocie na ekran.
 *
 * ## Tempo
 *
 * [ParallaxLayer.speed] jest wzgledem warstwy ziemi (1.0). Dawaj wartosci NIEWYMIERNE wzgledem
 * siebie (0.17, 0.41, 1.0, 1.63) i rozne szerokosci grafik - inaczej warstwy zgraja sie w jeden
 * wzor i calosc zacznie pulsowac zamiast plynac.
 */
@Composable
fun ParallaxStage(
    layers: List<ParallaxLayer>,
    modifier: Modifier = Modifier,
    /** Ile pikseli swiata przejezdza warstwa ziemi w ciagu sekundy. */
    groundSpeedPxPerSecond: Float = 90f,
    /** Zatrzymanie sceny - np. gdy ekran schodzi z widoku albo trwa dialog. */
    running: Boolean = true
) {
    // Przebyty dystans, a nie czas: predkosc wolno zmienic w trakcie, a scena nie skoczy.
    var travel by remember { mutableStateOf(0f) }
    LaunchedEffect(running, groundSpeedPxPerSecond) {
        if (!running) return@LaunchedEffect
        var previous = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val seconds = (now - previous) / 1_000_000_000f
            previous = now
            travel += seconds * groundSpeedPxPerSecond
        }
    }

    val images = layers.map { imageResource(it.image) }

    Canvas(modifier = modifier.clipToBounds()) {
        layers.forEachIndexed { index, layer ->
            drawLayer(layer = layer, image = images[index], travel = travel * layer.speed)
        }
    }
}

/**
 * Jedna warstwa tla.
 *
 * [heightFraction] to ulamek WYSOKOSCI OKNA, ktory zajmuje grafika - szerokosc wychodzi z
 * proporcji, wiec grafiki nie trzeba dopasowywac do zadnego ekranu. [bottom] mowi, czy warstwa
 * wisi u dolu okna (ziemia, trawa, most), czy u gory (chmury).
 */
data class ParallaxLayer(
    val image: DrawableResource,
    /** Tempo wzgledem ziemi: 1.0 = ziemia, 0.2 = daleki plan, 1.6 = pierwszy plan. */
    val speed: Float,
    val mode: TileMode = TileMode.Repeat,
    val heightFraction: Float = 1f,
    val bottom: Boolean = true,
    /** Przesuniecie w pionie w ulamku wysokosci okna - do wsuwania warstwy pod krawedz. */
    val verticalBias: Float = 0f,
    /** Dla [TileMode.Scatter]: co ile pikselow swiata WYPADA miejsce na obiekt. */
    val scatterSpacing: Float = 900f,
    /** Dla [TileMode.Scatter]: z jakim prawdopodobienstwem miejsce jest zajete. */
    val scatterChance: Float = 0.45f,
    /** Ziarno rozstawienia - zmiana przestawia cala trase, ta sama daje zawsze ten sam teren. */
    val scatterSeed: Int = 1
)

enum class TileMode {
    /** Kafel za kaflem - grafika musi laczyc sie sama ze soba. */
    Repeat,

    /** Kafel, lustro, kafel, lustro - laczy sie zawsze, kosztem symetrii wzoru. */
    Mirror,

    /** Pojedyncze obiekty rozstawione po trasie, nie ciagly pas. */
    Scatter
}

private fun DrawScope.drawLayer(layer: ParallaxLayer, image: ImageBitmap, travel: Float) {
    val height = size.height * layer.heightFraction
    if (height <= 0f || image.height == 0) return
    val width = image.width * (height / image.height)
    if (width <= 1f) return

    val top = if (layer.bottom) size.height - height else 0f
    val y = top + size.height * layer.verticalBias

    when (layer.mode) {
        TileMode.Scatter -> drawScatter(layer, image, travel, width, height, y)
        else -> drawTiles(layer.mode, image, travel, width, height, y)
    }
}

/**
 * Kafelkowanie. Numer kafla liczy sie w SWIECIE ([first]), a nie na ekranie - dzieki temu parzystosc
 * (czyli to, ktory kafel jest odbity) nie zmienia sie przy przewijaniu, a most nie zaczyna nagle
 * migotac miedzy wersja normalna a odbita.
 */
private fun DrawScope.drawTiles(
    mode: TileMode,
    image: ImageBitmap,
    travel: Float,
    width: Float,
    height: Float,
    y: Float
) {
    val first = floor(travel / width).toInt()
    val offset = travel - first * width
    val count = ceil((size.width + offset) / width).toInt() + 2

    repeat(count) { step ->
        val index = first + step - 1
        val x = (step - 1) * width - offset
        val mirrored = mode == TileMode.Mirror && index % 2 != 0
        if (mirrored) {
            // Odbicie wzgledem SRODKA tego kafla - inaczej odbity kafel wyladowalby po drugiej
            // stronie okna zamiast na swoim miejscu.
            scale(scaleX = -1f, scaleY = 1f, pivot = Offset(x + width / 2f, y + height / 2f)) {
                drawTile(image, x, y, width, height)
            }
        } else {
            drawTile(image, x, y, width, height)
        }
    }
}

/**
 * Rozstawione obiekty. Kazde "miejsce" na trasie ma swoj numer i wlasny [Random] - stad ta sama
 * skalka zawsze w tym samym miejscu, bez trzymania czegokolwiek w pamieci.
 */
private fun DrawScope.drawScatter(
    layer: ParallaxLayer,
    image: ImageBitmap,
    travel: Float,
    width: Float,
    height: Float,
    y: Float
) {
    val spacing = layer.scatterSpacing.coerceAtLeast(width)
    val first = floor(travel / spacing).toInt()
    val count = ceil(size.width / spacing).toInt() + 2

    repeat(count) { step ->
        val slot = first + step
        val random = Random(layer.scatterSeed * 92_821L + slot)
        if (random.nextFloat() > layer.scatterChance) return@repeat

        // Rozrzut W OBREBIE miejsca - bez niego obiekty staly by w rowniutkim rytmie i cala
        // sztuczka z losowoscia bylaby widoczna na pierwszy rzut oka.
        val jitter = random.nextFloat() * (spacing - width)
        val x = slot * spacing + jitter - travel
        val flip = random.nextBoolean()
        if (flip) {
            scale(scaleX = -1f, scaleY = 1f, pivot = Offset(x + width / 2f, y + height / 2f)) {
                drawTile(image, x, y, width, height)
            }
        } else {
            drawTile(image, x, y, width, height)
        }
    }
}

/**
 * Jeden kafel. Krawedzie licza sie przez zaokraglenie POZYCJI, a nie rozmiaru: prawy brzeg kafla
 * to zaokraglone `x + szerokosc`, czyli dokladnie to samo, co lewy brzeg nastepnego.
 *
 * To jest cala przyczyna "klejenia", ktore bylo widac: gdy kazdy kafel zaokraglal swoja szerokosc
 * osobno, sasiedzi rozjezdzali sie o jeden piksel i przez scene biegla pionowa szpara (albo
 * zdublowana kreska) - co jedna szerokosc kafla, czyli dokladnie tam, gdzie oko szuka szwu.
 */
private fun DrawScope.drawTile(image: ImageBitmap, x: Float, y: Float, width: Float, height: Float) {
    val left = x.roundToInt()
    val right = (x + width).roundToInt()
    val top = y.roundToInt()
    val bottom = (y + height).roundToInt()
    drawImage(
        image = image,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(image.width, image.height),
        dstOffset = IntOffset(left, top),
        dstSize = IntSize((right - left).coerceAtLeast(1), (bottom - top).coerceAtLeast(1)),
        filterQuality = FilterQuality.High
    )
}
