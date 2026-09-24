package com.lernhero.home

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.sin

/**
 * Gdzie na EKRANIE wylądowała mapa. Wszystko, co ma stanąć na konkretnym budynku, przelicza sie
 * przez to jedno miejsce - dzieki temu ogien w kuzni i klikalny obszar kuzni nie moga sie
 * rozjechac, bo licza z tej samej ramki.
 */
internal data class MapFrame(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
) {
    fun x(u: Float): Float = left + u * width
    fun y(v: Float): Float = top + v * height
    /** Dlugosc podana jako ulamek SZEROKOSCI mapy - promienie swiatel skaluja sie z nia. */
    fun span(u: Float): Float = u * width
}

/**
 * Warstwa "zycia" miasta: migoczacy ogien, unoszacy sie dym, pulsujace runy, iskry.
 *
 * ## Dlaczego proceduralnie, a nie GIF-em/sprite'ami
 *
 * Wszystko tutaj jest liczone z jednego zegara i kilku sinusoid - nie ma ani jednego nowego pliku
 * graficznego. Powody sa trzy:
 *
 * 1. GIF to prostokatna lata z wlasna paleta wklejona na 24-bitowy obraz; przy cieplym swietle
 *    ognia szew widac. Tutaj swiatlo jest rysowane W TRYBIE DODAWANIA na istniejacych pikselach,
 *    wiec nie ma czego skleic - ono po prostu rozjasnia to, co juz jest namalowane.
 * 2. Petla sie nie zdradza. Kazde zrodlo miesza trzy czestotliwosci, ktore nie sa swoimi
 *    wielokrotnosciami, wiec wzor nie powtarza sie w zadnym sensownym czasie - a pętla dymu w
 *    GIF-ie "przeskakuje" co kilka sekund i oko to lapie.
 * 3. Zero kosztu w pamieci i w APK.
 *
 * Pozycje zrodel nie sa dobrane na oko - wykryte z samej grafiki (skupiska jasnych, cieplych
 * pikseli dla ognia, zimnych dla run), wiec swiatlo pada dokladnie tam, gdzie malarz je namalowal.
 */
@Composable
internal fun TownAmbience(frame: MapFrame, modifier: Modifier = Modifier) {
    val time by rememberAmbienceClock()

    Canvas(modifier = modifier) {
        // Kolejnosc ma znaczenie: najpierw poswiaty (pod dymem), potem dym, na koncu iskry.
        FIRES.forEach { drawFire(frame, it, time) }
        PULSES.forEach { drawPulse(frame, it, time) }
        drawSmoke(frame, time)
        EMBER_SOURCES.forEach { drawEmbers(frame, it, time) }
    }
}

/**
 * Jeden zegar na cala warstwe, w sekundach od wejscia na ekran.
 *
 * [withFrameNanos] zamiast animacji na kazde zrodlo osobno: dwadziescia niezaleznych
 * `animateFloat` to dwadziescia animacji do zarzadzania, a i tak wszystkie potrzebuja tej samej
 * jednej liczby - czasu. Gdy ekran schodzi z kompozycji, petla konczy sie sama.
 */
@Composable
internal fun rememberAmbienceClock(): State<Float> {
    val seconds = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now -> seconds.floatValue = (now - start) / 1_000_000_000f }
        }
    }
    return seconds
}

/* ---- ogien ------------------------------------------------------------------------------------ */

/**
 * Zrodlo ognia. [radius] i pozycje to ulamki mapy (patrz [MapFrame]), [strength] to szczytowa
 * jasnosc dodawanego swiatla.
 */
private data class Fire(
    val x: Float,
    val y: Float,
    val radius: Float,
    val strength: Float,
    val color: Color,
    val seed: Int
)

private val FireOrange = Color(0xFFFF9B3D)
private val CandleGold = Color(0xFFFFD08A)

/**
 * Wykryte z grafiki - skupiska pikseli jasnych i mocno cieplych (R znacznie wyzsze od B).
 * Kolejnosc od najwiekszego zrodla.
 */
private val FIRES = listOf(
    /** Brama swiatyni - najwiekszy ogien na mapie. */
    Fire(x = 0.779f, y = 0.397f, radius = 0.150f, strength = 0.30f, color = FireOrange, seed = 0),
    /** Palenisko kuzni. */
    Fire(x = 0.763f, y = 0.672f, radius = 0.115f, strength = 0.34f, color = FireOrange, seed = 1),
    /** Kosze ogniowe przy bramie - lewy i prawy. */
    Fire(x = 0.360f, y = 0.844f, radius = 0.085f, strength = 0.30f, color = FireOrange, seed = 2),
    Fire(x = 0.639f, y = 0.844f, radius = 0.085f, strength = 0.30f, color = FireOrange, seed = 3),
    /** Kosz ogniowy przy kopalni. */
    Fire(x = 0.316f, y = 0.370f, radius = 0.075f, strength = 0.26f, color = FireOrange, seed = 4),
    /** Okno kuzni. */
    Fire(x = 0.854f, y = 0.655f, radius = 0.045f, strength = 0.18f, color = FireOrange, seed = 5),
    /** Krysztaly na wiezy swiatyni - nie ogien, ale tez "zywe" swiatlo. */
    Fire(x = 0.817f, y = 0.264f, radius = 0.040f, strength = 0.20f, color = FireOrange, seed = 6),
    Fire(x = 0.828f, y = 0.223f, radius = 0.036f, strength = 0.18f, color = FireOrange, seed = 7),
    /** Swiece wokol kregu - dwa skupiska, cieplejsze i spokojniejsze niz ogien. */
    Fire(x = 0.432f, y = 0.614f, radius = 0.070f, strength = 0.20f, color = CandleGold, seed = 8),
    Fire(x = 0.198f, y = 0.625f, radius = 0.062f, strength = 0.18f, color = CandleGold, seed = 9)
)

/**
 * Nieregularne migotanie: trzy sinusoidy o czestotliwosciach, ktore nie sa swoimi wielokrotnosciami.
 * Jedna dalaby rowne pulsowanie ("oddychajaca zarowka"), trzy zlozone nie powtarzaja sie w zadnym
 * czasie, ktory oko zdazy zapamietac - i dopiero to czyta sie jako ogien.
 */
internal fun flicker(time: Float, seed: Int): Float {
    val a = sin(time * 5.3f + seed * 1.7f)
    val b = sin(time * 8.9f + seed * 3.1f)
    val c = sin(time * 14.3f + seed * 0.7f)
    return (((a * 0.5f + b * 0.32f + c * 0.18f) + 1f) / 2f).coerceIn(0f, 1f)
}

private fun DrawScope.drawFire(frame: MapFrame, fire: Fire, time: Float) {
    val f = flicker(time, fire.seed)
    // Nigdy nie gasnie do zera - ogien ma drgac, a nie mrugac jak zepsuta swietlowka.
    val alpha = fire.strength * (0.62f + 0.38f * f)
    val radius = frame.span(fire.radius) * (0.92f + 0.12f * f)
    drawGlow(
        center = Offset(frame.x(fire.x), frame.y(fire.y)),
        radius = radius,
        color = fire.color,
        alpha = alpha
    )
}

/* ---- zimne swiatla ---------------------------------------------------------------------------- */

/** Swiatlo, ktore nie migocze, tylko powoli oddycha - runy, ksiezyc, portal. */
private data class Pulse(
    val x: Float,
    val y: Float,
    val radius: Float,
    val strength: Float,
    val color: Color,
    /** Dlugosc pelnego oddechu w sekundach. */
    val period: Float,
    val seed: Int
)

private val RuneCyan = Color(0xFF8FE8FF)
private val MoonPink = Color(0xFFF7C8E8)
private val PortalPink = Color(0xFFFF63C8)

private val PULSES = listOf(
    /** Krag przyzywania - pentagram i kamienne runy. Wykryte jako skupiska zimnych pikseli. */
    Pulse(x = 0.293f, y = 0.600f, radius = 0.150f, strength = 0.22f, color = RuneCyan, period = 4.4f, seed = 0),
    Pulse(x = 0.330f, y = 0.545f, radius = 0.055f, strength = 0.20f, color = RuneCyan, period = 3.1f, seed = 1),
    Pulse(x = 0.418f, y = 0.577f, radius = 0.048f, strength = 0.18f, color = RuneCyan, period = 3.7f, seed = 2),
    /** Ksiezyc - ledwo widoczny oddech, zeby niebo nie bylo martwe. */
    Pulse(x = 0.569f, y = 0.213f, radius = 0.190f, strength = 0.10f, color = MoonPink, period = 9.0f, seed = 3),
    /** Portal pod zamkiem w oddali - jedyna rzecz na horyzoncie, ktora ma "tetnic". */
    Pulse(x = 0.528f, y = 0.263f, radius = 0.075f, strength = 0.20f, color = PortalPink, period = 2.6f, seed = 4)
)

private fun DrawScope.drawPulse(frame: MapFrame, pulse: Pulse, time: Float) {
    val phase = (sin(time * (2f * PI.toFloat() / pulse.period) + pulse.seed) + 1f) / 2f
    val alpha = pulse.strength * (0.55f + 0.45f * phase)
    val radius = frame.span(pulse.radius) * (0.94f + 0.10f * phase)
    drawGlow(
        center = Offset(frame.x(pulse.x), frame.y(pulse.y)),
        radius = radius,
        color = pulse.color,
        alpha = alpha
    )
}

/**
 * Miekka kalza swiatla dodawana DO tego, co juz namalowane ([BlendMode.Plus]).
 *
 * Srodek jest slabszy niz polowa promienia - w srodku farba i tak jest juz najjasniejsza, a to,
 * co czyta sie jako zywy ogien, to swiatlo tanczace na kamieniu DOOKOLA niego. Dolozenie mocy
 * w samym srodku tylko spalaloby kolor do bialego.
 */
private fun DrawScope.drawGlow(center: Offset, radius: Float, color: Color, alpha: Float) {
    if (radius <= 0f || alpha <= 0f) return
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to color.copy(alpha = alpha * 0.55f),
            0.35f to color.copy(alpha = alpha),
            1.0f to color.copy(alpha = 0f),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center,
        blendMode = BlendMode.Plus
    )
}

/* ---- dym -------------------------------------------------------------------------------------- */

/** Wylot komina kuzni - zmierzony na grafice. */
private const val CHIMNEY_X = 0.930f
private const val CHIMNEY_Y = 0.555f

/**
 * Ile klebow leci naraz i jak dlugo trwa droga jednego.
 *
 * Gestosc jest dobrana pod grafike BEZ namalowanego dymu: pierwsza wersja tylko dokladala ruchu
 * do smugi, ktora juz byla na obrazku, wiec mogla byc ledwie widoczna. Teraz caly dym to te
 * kleby - musza wiec byc na tyle gesto i mocno, zeby czytaly sie jako smuga same z siebie.
 */
private const val SMOKE_PUFFS = 8
private const val SMOKE_TRAVEL_SECONDS = 6.0f

private val SmokeGrey = Color(0xFFD9D4E4)

/**
 * Dym z komina: kilka miekkich klebow na jednej drodze, kazdy przesuniety w fazie.
 *
 * Kleby wychodza Z wylotu i przechodza PRZEZ namalowana smuge - nie zamiast niej. Namalowany dym
 * zostaje w grafice (nie retuszuje cudzego obrazu), a ruch nakladany na niego wystarcza, zeby cala
 * smuga czytala sie jako plynaca. Nad nia kleby leca juz same i gasna.
 */
private fun DrawScope.drawSmoke(frame: MapFrame, time: Float) {
    repeat(SMOKE_PUFFS) { index ->
        val progress = ((time / SMOKE_TRAVEL_SECONDS) + index.toFloat() / SMOKE_PUFFS) % 1f
        // Dym zwalnia, im wyzej jest - stad krzywa gasnaca zamiast liniowej.
        val rise = progress * (2f - progress) * 0.115f
        val drift = sin(progress * 3.4f + index) * 0.022f * progress
        val x = CHIMNEY_X + drift
        val y = CHIMNEY_Y - rise
        val size = 0.016f + progress * 0.042f
        // Pojawia sie szybko tuz nad wylotem i gasnie dlugo - tak zachowuje sie prawdziwy dym.
        val fade = (progress * 7f).coerceAtMost(1f) * (1f - progress) * (1f - progress)
        val alpha = 0.62f * fade
        if (alpha <= 0.004f) return@repeat

        // Kazdy kleb to TRZY zachodzace na siebie platy, a nie jedno kolo. Idealne kolo zdradza
        // sie natychmiast - oko wylapuje je jako ksztalt geometryczny, nie jako dym. Przesuniecia
        // platow wychodza z sinusow o roznych okresach, wiec kleb zmienia sylwetke, plynac.
        repeat(3) { lobe ->
            val phase = index * 1.7f + lobe * 2.3f
            val lobeX = x + sin(time * 0.6f + phase) * size * 0.34f
            val lobeY = y + sin(time * 0.5f + phase * 1.6f) * size * 0.22f
            val lobeRadius = frame.span(size * (0.80f + 0.22f * lobe))
            val center = Offset(frame.x(lobeX), frame.y(lobeY))
            // Rdzen platu jest prawie jednolity, a miekka jest dopiero krawedz. Rozmyty od
            // srodka gradient dawal mgliste smugi - a ten obrazek jest rysowany plaskimi,
            // wyraznymi klebami i dym musi byc z tej samej bajki.
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to SmokeGrey.copy(alpha = alpha),
                    0.62f to SmokeGrey.copy(alpha = alpha * 0.88f),
                    1.0f to SmokeGrey.copy(alpha = 0f),
                    center = center,
                    radius = lobeRadius
                ),
                radius = lobeRadius,
                center = center
            )
        }
    }
}

/* ---- iskry ------------------------------------------------------------------------------------ */

/** Skad leca iskry - te same ogniska, ktore realnie plona. */
private data class EmberSource(val x: Float, val y: Float, val count: Int, val seed: Int)

private val EMBER_SOURCES = listOf(
    EmberSource(x = 0.360f, y = 0.838f, count = 4, seed = 0),
    EmberSource(x = 0.639f, y = 0.838f, count = 4, seed = 1),
    EmberSource(x = 0.763f, y = 0.665f, count = 5, seed = 2),
    EmberSource(x = 0.316f, y = 0.365f, count = 3, seed = 3)
)

private val EmberWarm = Color(0xFFFFC46A)

private const val EMBER_TRAVEL_SECONDS = 3.6f

/**
 * Iskry - male, ciepłe punkty unoszace sie nad ogniem i gasnace po drodze.
 *
 * To one, bardziej niz sama poswiata, mowia oku "to naprawde plonie": poswiata moze byc
 * namalowana, ale nic namalowanego nie odlatuje do gory.
 */
private fun DrawScope.drawEmbers(frame: MapFrame, source: EmberSource, time: Float) {
    repeat(source.count) { index ->
        val offset = index.toFloat() / source.count + source.seed * 0.37f
        val progress = ((time / EMBER_TRAVEL_SECONDS) + offset) % 1f
        val wobble = sin(progress * 7.5f + index + source.seed) * 0.012f
        val center = Offset(
            x = frame.x(source.x + wobble),
            y = frame.y(source.y - progress * 0.075f)
        )
        // Zapala sie od razu, gasnie im wyzej - iskra stygnie, a nie znika nagle.
        val alpha = 0.75f * (1f - progress) * (1f - progress)
        val radius = frame.span(0.0045f) * (1f - progress * 0.45f)
        if (alpha <= 0.01f || radius <= 0f) return@repeat
        drawCircle(
            color = EmberWarm.copy(alpha = alpha),
            radius = radius,
            center = center,
            blendMode = BlendMode.Plus
        )
    }
}
