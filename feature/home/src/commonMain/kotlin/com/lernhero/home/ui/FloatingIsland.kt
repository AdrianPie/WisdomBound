package com.lernhero.home.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.painterResource

/**
 * Latajaca wyspa z kompanem: duzy kamien wiszacy w powietrzu, sylwetka STOI na jego plaskim
 * wierzchu (stopy dotykaja kamienia), a wokol dolnej krawedzi lataja male kamyki - kazdy z INNYM
 * okresem i przesunieta faza, wiec ruch nie jest zsynchronizowany. Caly zestaw (kamien + sylwetka)
 * buja sie razem, wolniej; kamyki maja wlasny, szybszy rytm.
 *
 * Uklad liczy sie w Dp z [BoxWithConstraints], wiec "stopy na kamieniu" trzyma sie na kazdej
 * wielkosci sceny. Komponent chce zajac cala scene - podaj mu `Modifier.fillMaxSize()`.
 */
@Composable
fun FloatingIsland(
    spritePainter: Painter,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "floating-island")

    BoxWithConstraints(modifier = modifier) {
        // JEDNA jednostka skali dla kamienia, sylwetki i kamykow. Wczesniej kamien bral 70%
        // szerokosci niezaleznie od wysokosci sceny - na niskiej scenie obraz sie sciskal (Fit),
        // a kamyki, liczone od NIESCISNIETEJ szerokosci, rozlatywaly sie po calym ekranie.
        val unit = minOf(maxWidth * SLAB_WIDTH_FRACTION, maxHeight * HEIGHT_BUDGET)
        val slabW = unit
        val spriteH = unit * SPRITE_TO_SLAB

        // Srodek grupy (widoczna sylwetka + kamien) = srodek sceny.
        val spriteReach = spriteH * (1f - SPRITE_EDGE_PADDING)
        val slabReach = slabW * (SLAB_VISUAL_BOTTOM - SLAB_TOP_INSET)
        val feetY = maxHeight / 2 + (spriteReach - slabReach) / 2
        val slabTop = feetY - slabW * SLAB_TOP_INSET
        val sink = spriteH * SPRITE_EDGE_PADDING

        val bobPx = with(LocalDensity.current) { 5.dp.toPx() }
        val bob by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "slab-bob",
        )

        // Miekka poswiata pod stopami - osadza postac w scenie.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = feetY - slabW * 0.35f)
                .requiredSize(slabW * 1.3f, slabW * 0.7f)
                .graphicsLayer { translationY = -bobPx * bob }
                .background(
                    Brush.radialGradient(listOf(Color(0x55FFD9A0), Color.Transparent)),
                    RoundedCornerShape(50)
                )
        )

        PEBBLES.forEach { pebble ->
            val ampPx = with(LocalDensity.current) { pebble.amplitude.toPx() }
            val t by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(pebble.periodMs, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(pebble.startMs),
                ),
                label = "pebble-${pebble.resIndex}",
            )
            Image(
                painter = painterResource(Resources.Image.floatingIslandPebbles[pebble.resIndex]),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = slabW * pebble.xFactor, y = feetY + slabW * pebble.yFactor)
                    .requiredSize(slabW * pebble.sizeFactor)
                    .graphicsLayer { translationY = -ampPx * t - bobPx * bob * 0.5f },
            )
        }

        Image(
            painter = painterResource(Resources.Image.floatingIslandSlab),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = slabTop)
                .requiredSize(slabW)
                .graphicsLayer { translationY = -bobPx * bob },
        )

        Image(
            painter = spritePainter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopCenter)
                // +sink: kadr sprite'a ma pusty margines pod stopami.
                .offset(y = feetY + sink - spriteH)
                .requiredSize(spriteH)
                .graphicsLayer { translationY = -bobPx * bob },
        )
    }
}

/** Ile szerokosci sceny zajmuje glowny kamien. */
private const val SLAB_WIDTH_FRACTION = 0.62f

/** Jaka czesc wysokosci sceny moze zajac jednostka skali - grupa ma ~1.35 jednostki wysokosci. */
private const val HEIGHT_BUDGET = 0.68f

/** Kadr sylwetki wzgledem szerokosci kamienia. */
private const val SPRITE_TO_SLAB = 1.15f


/** Przezroczysty margines w kadrze sprite'a, u gory i u dolu - zmierzone na plikach potworow. */
private const val SPRITE_EDGE_PADDING = 0.13f

/** Gdzie w kadrze kamienia (0 = gora grafiki) lezy jego plaski wierzch. */
private const val SLAB_TOP_INSET = 0.30f

/** Gdzie w kadrze kamienia konczy sie jego widoczny dol. */
private const val SLAB_VISUAL_BOTTOM = 0.80f

/**
 * Jeden lewitujacy kamyk. [xFactor]/[yFactor] to pozycja wzgledem szerokosci kamienia liczona od
 * jego plaskiego wierzchu (xFactor 0 = srodek, yFactor 0 = wierzch, ~0.7 = dol grafiki kamienia).
 */
private data class Pebble(
    val resIndex: Int,
    val xFactor: Float,
    val yFactor: Float,
    val sizeFactor: Float,
    val periodMs: Int,
    val amplitude: Dp,
    val startMs: Int,
)

/** Kamyki trzymaja sie SPODU kamienia (yFactor od wierzchu: 0.3-0.6), w obrysie jego szerokosci. */
private val PEBBLES = listOf(
    Pebble(resIndex = 0, xFactor = -0.34f, yFactor = 0.42f, sizeFactor = 0.075f, periodMs = 2600, amplitude = 5.dp, startMs = 0),
    Pebble(resIndex = 1, xFactor = 0.36f, yFactor = 0.36f, sizeFactor = 0.06f, periodMs = 3400, amplitude = 6.dp, startMs = 520),
    Pebble(resIndex = 2, xFactor = -0.08f, yFactor = 0.62f, sizeFactor = 0.08f, periodMs = 2100, amplitude = 4.dp, startMs = 960),
    Pebble(resIndex = 3, xFactor = 0.2f, yFactor = 0.56f, sizeFactor = 0.05f, periodMs = 3900, amplitude = 7.dp, startMs = 240),
)
