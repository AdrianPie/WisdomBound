package com.lernhero.game.battle.effects

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Efekt zlozony z KILKU nakladajacych sie warstw-PNG, z ktorych kazda kreci sie z wlasna
 * predkoscia. Tak zbudowany jest np. pocisk magiczny: trzy pierscienie na tym samym canvasie,
 * obracajace sie w roznym tempie i w rozne strony - z trzech statycznych obrazkow robi sie zywy,
 * wirujacy obiekt bez ani jednej klatki animacji.
 *
 * System jest CELOWO niezalezny od liczby warstw: [layers] moze miec ich dwie albo dziesiec,
 * a i renderer ([LayeredEffectView]), i lab animacji generuja sterowanie automatycznie z tej listy.
 * Dodanie nowego efektu warstwowego = jeden wpis w [LayeredEffectCatalog], zero nowego kodu UI.
 *
 * Warunek na grafike jest jeden: wszystkie warstwy jednego efektu musza miec TEN SAM canvas
 * (np. 256x256, reszta przezroczysta) - wtedy nakladaja sie same z siebie, bez pozycjonowania.
 */
data class LayeredEffectSpec(
    val id: String,
    val label: String,
    val layers: List<EffectLayer>
) {
    fun layer(id: String): EffectLayer? = layers.firstOrNull { it.id == id }
}

/**
 * Jedna warstwa efektu. [spinPerSecond] to PELNE OBROTY na sekunde: 0.5 = polowa obrotu na sekunde,
 * wartosc ujemna = obrot w druga strone (kontra-rotacja dwoch pierscieni wyglada duzo lepiej niz
 * krecenie wszystkiego w te sama strone).
 */
data class EffectLayer(
    val id: String,
    val label: String,
    val drawable: DrawableResource,
    val spinPerSecond: Float = 0f,
    /** Wlasna skala warstwy wzgledem calego efektu - zewnetrzny pierscien bywa szerszy niz rdzen. */
    val scale: Float = 1f,
    val alpha: Float = 1f
)

/**
 * Katalog efektow warstwowych. Nowy efekt dorzuca sie tutaj - lab i walka same go podchwyca.
 */
object LayeredEffectCatalog {

    /**
     * Pocisk magiczny: trzy pierscienie 256x256. Predkosci sa celowo NIEWSPOLMIERNE (0.35 / -0.6 / 1.1),
     * zeby uklad nigdy nie wracal do tej samej pozy - gdyby byly wielokrotnosciami siebie, efekt
     * "zapetlalby sie" widocznie co kilka sekund. Srodkowy kreci sie w przeciwna strone.
     */
    val magicMissile = LayeredEffectSpec(
        id = "magic_missile_rings",
        label = "Pocisk magiczny",
        layers = listOf(
            // Kolejnosc = kolejnosc rysowania: pierwszy na spodzie, ostatni na wierzchu.
            EffectLayer("ring3", "Pierscien 3", Resources.EffectArt.magicMissileRing3, spinPerSecond = 1.1f),
            EffectLayer("ring2", "Pierscien 2", Resources.EffectArt.magicMissileRing2, spinPerSecond = -0.6f),
            EffectLayer("ring1", "Pierscien 1", Resources.EffectArt.magicMissileRing1, spinPerSecond = 0.35f)
        )
    )

    val all: List<LayeredEffectSpec> = listOf(magicMissile)

    fun byId(id: String): LayeredEffectSpec? = all.firstOrNull { it.id == id }
}

/**
 * Rysuje efekt warstwowy w danej chwili. [elapsedSeconds] to czas, jaki uplynal od poczatku efektu -
 * z niego liczy sie kat obrotu kazdej warstwy, wiec przewijanie osi czasu w labie pokazuje
 * DOKLADNIE te sama poze co odtwarzanie (obrot jest funkcja czasu, nie stanem narastajacym).
 *
 * [spinOverrides] pozwala podmienic predkosci z katalogu na te ustawione w labie, bez dotykania
 * samego katalogu - dzieki temu jedna grafika moze grac raz jako leniwa aura, raz jako szybki pocisk.
 */
@Composable
fun LayeredEffectView(
    spec: LayeredEffectSpec,
    elapsedSeconds: Float,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    spinOverrides: Map<String, Float> = emptyMap(),
    /**
     * Przemalowanie calego efektu na jeden odcien (np. zielony przy leczeniu, zolty przy
     * ogluszeniu). null = oryginalne kolory PNG. Patrz [recolorFilter] - dziala i na bialej,
     * i na kolorowej grafice, bo najpierw sprowadza pixel do jasnosci, a potem mnozy przez [tint].
     */
    tint: Color? = null
) {
    val filter = tint?.let { recolorFilter(it) }
    Box(modifier = modifier) {
        spec.layers.forEach { layer ->
            val spin = spinOverrides[layer.id] ?: layer.spinPerSecond
            Image(
                painter = painterResource(layer.drawable),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = filter,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = spin * elapsedSeconds * 360f
                        scaleX = layer.scale
                        scaleY = layer.scale
                        this.alpha = layer.alpha * alpha
                    }
            )
        }
    }
}

/**
 * Filtr "potraktuj grafike jak skale szarosci i przemaluj na [tint]". Jeden [ColorMatrix], ktory
 * kazdy pixel zamienia na `jasnosc * tint`, zachowujac alfe - czyli jasne miejsca daja jasny
 * odcien koloru, ciemne ciemny, a ksztalt/poswiata pierscieni zostaje.
 *
 * Dziala na DOWOLNYM zrodle. Na PNG bialym/szarym trafia w kolor idealnie. Na PNG mocno
 * fioletowym (jak teraz) tez zadziala, ale zielony/zolty wyjdzie lekko przybrudzony - jesli
 * kolory maja byc czyste, wystarczy wyeksportowac pierscienie w bieli.
 */
private fun recolorFilter(tint: Color): ColorFilter {
    val r = tint.red
    val g = tint.green
    val b = tint.blue
    // Wspolczynniki jasnosci wg Rec. 709 - to samo, czego uzywa desaturacja.
    val lr = 0.2126f
    val lg = 0.7152f
    val lb = 0.0722f
    return ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                lr * r, lg * r, lb * r, 0f, 0f,
                lr * g, lg * g, lb * g, 0f, 0f,
                lr * b, lg * b, lb * b, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )
}
