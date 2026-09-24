package com.lernhero.home.city

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.lernhero.home.flicker
import com.lernhero.home.rememberAmbienceClock
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.imageResource
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Ryte znaki oltarza zapalaja sie w kolorze rzadkosci summona: same symbole ZMIENIAJA BARWE, a
 * kamien dookola dostaje odblask.
 *
 * ## Dlaczego maska, a nie samo swiatlo
 *
 * Pierwsze podejscie tylko dokladalo swiatla ([BlendMode.Plus]) tam, gdzie malarz umiescil znaki -
 * tak jak robi to miasto w [com.lernhero.home.TownAmbience]. Na mapie miasta to dziala, bo tam
 * swiatlo pada na kolorowa juz farbe. Tutaj znaki sa SZARE, a dodawanie swiatla do szarosci daje
 * jasniejsza szarosc z ledwie wyczuwalnym odcieniem - runa robila sie brudnobiala zamiast zielona.
 *
 * Dlatego znaki maja wlasna maske ([Resources.CityIsland.altarRunes]): bialy obraz, w ktorego
 * alfie siedzi glebokosc rytu. Rysowana NA oltarzu i zabarwiona [ColorFilter.tint] podmienia sam
 * symbol na kolor rzadkosci - dokladnie po ksztalcie, bez ruszania kamienia. Dopiero na to ida
 * dwie warstwy swiatla: ten sam symbol w trybie dodawania (rozzarzenie znaku) i miekkie klady
 * dookola (odblask na kamieniu).
 *
 * Kolor nie przeskakuje, tylko przechodzi ([COLOR_SHIFT_MS]) - po kliknieciu summona znaki
 * "przebarwiaja sie" na oczach gracza, zamiast zmienic sie miedzy klatkami.
 */
@Composable
internal fun AltarRuneLights(
    accent: Color,
    /** 0 = oltarz tylko tli sie w tle, 1 = pelnia rytualu. Patrz [ALTAR_RUNE_IDLE]. */
    intensity: Float,
    /** Ladowanie rytualu 0..1 - podkreca tempo migotania, zeby znaki "przyspieszyly" po kliknieciu. */
    charge: Float,
    modifier: Modifier = Modifier
) {
    if (intensity <= 0.01f) return
    val time by rememberAmbienceClock()
    val runes = imageResource(Resources.CityIsland.altarRunes)
    val color by animateColorAsState(accent, tween(COLOR_SHIFT_MS), label = "runeColor")

    // Po kliknieciu summona znaki maja ozywac szybko - w spoczynku oddychaja, w trakcie rytualu
    // pulsuja kilka razy predzej. To ta zmiana tempa, a nie sama jasnosc, czyta sie jako "rusza".
    val speed = 1f + ALTAR_RUNE_SPEEDUP * charge

    Canvas(modifier = modifier) {
        val breath = (sin(time * speed * (2f * PI.toFloat() / BREATH_PERIOD)) + 1f) / 2f

        // 1. Odblask na kamieniu - miekkie klady pod znakami. Slabe, bo maja tylko sugerowac, ze
        //    znak oswietla otoczenie; caly kolor niesie warstwa 2.
        ALTAR_RUNES.forEach { light ->
            val phase = when (light.kind) {
                // Plomien drga nierowno (te same trzy nieskorelowane sinusoidy co ognie w miescie),
                // znak oddycha rownym rytmem - inaczej caly oltarz migalby jak zepsuta swietlowka.
                RuneKind.Flame -> flicker(time * speed, light.seed)
                else -> (sin(time * speed * (2f * PI.toFloat() / light.period) + light.seed) + 1f) / 2f
            }
            // Plomien swiecy ma wlasna, ciepla barwe i dopiero rytual przeciaga go na kolor
            // rzadkosci - swieca nie moze zrobic sie zielona, zanim cokolwiek sie zacznie.
            val spill = when (light.kind) {
                RuneKind.Flame -> lerp(CandleGold, color, intensity)
                else -> color
            }
            drawSpill(
                center = Offset(size.width * light.x, size.height * light.y),
                radius = size.minDimension * light.radius * (0.92f + 0.14f * phase),
                // Plyta jest w rzucie izometrycznym - okragla plama swiatla wystawalaby nad krag
                // i wygladala jak kula nad oltarzem, nie jak swiecacy nadruk.
                flatten = light.flatten,
                color = spill,
                alpha = light.strength * intensity * (0.55f + 0.45f * phase)
            )
        }

        val whole = IntSize(size.width.roundToInt(), size.height.roundToInt())
        if (whole.width <= 0 || whole.height <= 0) return@Canvas

        // 2. Przebarwienie samych znakow. To ta warstwa robi z szarej runy zielona/zlota/fioletowa.
        //    Krycie nie schodzi do zera nawet w spoczynku - inaczej znaki gasly do szarosci i kolor
        //    bannera w ogole nie bylby widoczny, dopoki ktos nie kliknie summona.
        drawImage(
            image = runes,
            dstOffset = IntOffset.Zero,
            dstSize = whole,
            alpha = ((TINT_BASE + (1f - TINT_BASE) * intensity) * (0.88f + 0.12f * breath))
                .coerceIn(0f, 1f),
            colorFilter = ColorFilter.tint(color)
        )

        // 3. Rozzarzenie znaku - ten sam ksztalt jeszcze raz, w trybie dodawania. Dopiero to sprawia,
        //    ze w szczycie rytualu runa swieci, zamiast byc pomalowanym rowkiem.
        drawImage(
            image = runes,
            dstOffset = IntOffset.Zero,
            dstSize = whole,
            alpha = (intensity * (0.30f + 0.35f * breath)).coerceIn(0f, 1f),
            colorFilter = ColorFilter.tint(color),
            blendMode = BlendMode.Plus
        )
    }
}

/**
 * Miekka klaza swiatla na kamieniu dookola znaku, dodawana do tego, co juz namalowane.
 *
 * Srodek jest slabszy niz obrzeza: sam znak barwi warstwa maski, a tutaj chodzi tylko o swiatlo
 * rozlane na kamieniu. Dolozenie mocy w srodku wypalaloby runie do bieli - dokladnie ten blad,
 * przez ktory pierwsza wersja wygladala na sprana.
 */
private fun DrawScope.drawSpill(
    center: Offset,
    radius: Float,
    flatten: Float,
    color: Color,
    alpha: Float
) {
    if (radius <= 0f || alpha <= 0f) return
    scale(scaleX = 1f, scaleY = flatten, pivot = center) {
        drawCircle(
            brush = Brush.radialGradient(
                0.0f to color.copy(alpha = alpha * 0.55f),
                0.40f to color.copy(alpha = alpha),
                1.0f to color.copy(alpha = 0f),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center,
            blendMode = BlendMode.Plus
        )
    }
}

/** Co dokladnie swieci - od tego zalezy rytm i barwa, patrz [AltarRuneLights]. */
private enum class RuneKind {
    /** Znaki na plycie kregu - gwiazda i obiegajacy ja okrag; najwieksze zrodla na scenie. */
    Sigil,

    /** Znaki wyryte w stojacych kamieniach. */
    Rune,

    /** Godla na swiecznikach - swieca slabiej, to drobny detal, nie glowny znak. */
    Banner,

    /** Plomienie swiec - jedyne, ktore migocza nierowno i maja wlasna cieple barwe. */
    Flame
}

/**
 * Zrodlo odblasku: [x]/[y] to srodek, [radius] zasieg - wszystko jako ulamek kwadratowego kadru
 * oltarza. [flatten] sciska plame w pionie (1 = kolo). [period] to dlugosc pelnego oddechu w
 * sekundach; celowo rozne na kazdym znaku, zeby oltarz nie pulsowal jak jedno wielkie serce.
 */
private data class RuneLight(
    val x: Float,
    val y: Float,
    val radius: Float,
    val strength: Float,
    val kind: RuneKind,
    val period: Float,
    val seed: Int,
    val flatten: Float = 1f
)

private val CandleGold = Color(0xFFFFD08A)

/**
 * Gdzie znaki rozlewaja swiatlo na kamien. Ksztalt znaku niesie maska, wiec te wspolrzedne nie
 * musza byc co do piksela - to srodki ciezkosci skupisk wykrytych w `summon_center.webp` (494x494)
 * tym samym rachunkiem, ktorym `tools/altar_rune_mask.py` wycina maske.
 */
private val ALTAR_RUNES = listOf(
    // Gwiazda na plycie - zmierzony prostokat x[0.350,0.601] y[0.502,0.688].
    RuneLight(x = 0.478f, y = 0.590f, radius = 0.165f, strength = 0.20f, kind = RuneKind.Sigil, period = 3.4f, seed = 0, flatten = 0.72f),
    // Ryty okrag obiegajacy plyte - szerszy i slabszy od gwiazdy, to on "zamyka" krag swiatlem.
    RuneLight(x = 0.490f, y = 0.612f, radius = 0.275f, strength = 0.13f, kind = RuneKind.Sigil, period = 5.2f, seed = 1, flatten = 0.50f),
    // Kamien centralny: rogaty symbol z okiem i mniejszy znak pod nim.
    RuneLight(x = 0.527f, y = 0.198f, radius = 0.075f, strength = 0.18f, kind = RuneKind.Rune, period = 3.1f, seed = 2),
    RuneLight(x = 0.533f, y = 0.325f, radius = 0.052f, strength = 0.16f, kind = RuneKind.Rune, period = 4.2f, seed = 3),
    // Kamien lewy.
    RuneLight(x = 0.179f, y = 0.393f, radius = 0.062f, strength = 0.17f, kind = RuneKind.Rune, period = 3.7f, seed = 4),
    RuneLight(x = 0.189f, y = 0.488f, radius = 0.045f, strength = 0.15f, kind = RuneKind.Rune, period = 2.9f, seed = 5),
    // Kamien prawy.
    RuneLight(x = 0.812f, y = 0.409f, radius = 0.055f, strength = 0.17f, kind = RuneKind.Rune, period = 4.6f, seed = 6),
    RuneLight(x = 0.802f, y = 0.488f, radius = 0.042f, strength = 0.15f, kind = RuneKind.Rune, period = 3.3f, seed = 7),
    // Godla na swiecznikach.
    RuneLight(x = 0.345f, y = 0.458f, radius = 0.042f, strength = 0.11f, kind = RuneKind.Banner, period = 5.1f, seed = 8, flatten = 1.4f),
    RuneLight(x = 0.238f, y = 0.766f, radius = 0.042f, strength = 0.11f, kind = RuneKind.Banner, period = 4.4f, seed = 9, flatten = 1.4f),
    RuneLight(x = 0.843f, y = 0.649f, radius = 0.042f, strength = 0.11f, kind = RuneKind.Banner, period = 5.8f, seed = 10, flatten = 1.4f),
    RuneLight(x = 0.717f, y = 0.805f, radius = 0.042f, strength = 0.11f, kind = RuneKind.Banner, period = 4.9f, seed = 11, flatten = 1.4f),
    // Plomienie swiec - jedyne, ktore naprawde swieca same z siebie, wiec zostaja mocniejsze.
    RuneLight(x = 0.322f, y = 0.322f, radius = 0.052f, strength = 0.30f, kind = RuneKind.Flame, period = 1f, seed = 12),
    RuneLight(x = 0.867f, y = 0.515f, radius = 0.052f, strength = 0.30f, kind = RuneKind.Flame, period = 1f, seed = 13),
    RuneLight(x = 0.216f, y = 0.626f, radius = 0.056f, strength = 0.32f, kind = RuneKind.Flame, period = 1f, seed = 14),
    RuneLight(x = 0.743f, y = 0.669f, radius = 0.056f, strength = 0.32f, kind = RuneKind.Flame, period = 1f, seed = 15)
)

/**
 * Ile swiatla maja znaki, gdy nic sie nie dzieje. Zero zrobiloby z oltarza martwa naklejke, ktora
 * dopiero po kliknieciu "wlacza sie" - a ma tlic sie caly czas i dopiero rozpalac przy rytuale.
 */
internal const val ALTAR_RUNE_IDLE = 0.24f

/** O ile szybciej migocza znaki na szczycie ladowania rytualu. */
private const val ALTAR_RUNE_SPEEDUP = 3.5f

/** Ile koloru maja znaki w spoczynku - reszte doklada rytual. Patrz [AltarRuneLights]. */
private const val TINT_BASE = 0.55f

/** Wspolny oddech calej maski - to jedna warstwa, wiec jeden rytm na wszystkie znaki. */
private const val BREATH_PERIOD = 3.2f

/** Ile trwa przejscie barwy znakow na kolor nowego summona. */
private const val COLOR_SHIFT_MS = 420
