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
        val slabW = maxWidth * SLAB_WIDTH_FRACTION
        val spriteH = maxHeight * SPRITE_HEIGHT_FRACTION

        // Sprite'y potworow maja wlasny przezroczysty margines (zmierzone na plikach: ~13%
        // wysokosci kadru u gory i u dolu). Stala w dp tego nie nadganiala - im wieksza sylwetka,
        // tym wiekszy margines, wiec stopy zaczynaly wisiec nad kamieniem. Liczymy go wiec jako
        // UDZIAL wysokosci sprite'a.
        val sink = spriteH * SPRITE_EDGE_PADDING

        // Gdzie postawic stopy, zeby CALA grupa (widoczna sylwetka + kamien) miala srodek dokladnie
        // w srodku sceny - czyli na wysokosci strzalek przewijania kompanow. Wczesniej feetY bylo
        // stalym ulamkiem wysokosci i grupa wychodzila wyraznie nad strzalki.
        //
        //   zasieg sylwetki w gore = (1 - 2 * SPRITE_EDGE_PADDING) * spriteH
        //   zasieg kamienia w dol  = (SLAB_VISUAL_BOTTOM - SLAB_TOP_INSET) * slabW
        val spriteReach = spriteH * (1f - 2f * SPRITE_EDGE_PADDING)
        val slabReach = slabW * (SLAB_VISUAL_BOTTOM - SLAB_TOP_INSET)
        val feetY = maxHeight / 2 + spriteReach / 2 - slabReach / 2

        // Plaski wierzch kamienia siedzi [SLAB_TOP_INSET] wysokosci jego (kwadratowej) grafiki
        // od gory - o tyle podnosimy kamien, zeby jego wierzch wypadl dokladnie pod stopami.
        val slabTop = (feetY - slabW * SLAB_TOP_INSET).coerceAtLeast(0.dp)

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
                    .padding(top = (feetY + slabW * pebble.yFactor).coerceAtLeast(0.dp))
                    .offset(x = slabW * pebble.xFactor)
                    .size(pebble.size)
                    .graphicsLayer { translationY = -ampPx * t },
            )
        }

        Image(
            painter = painterResource(Resources.Image.floatingIslandSlab),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = slabTop)
                .width(slabW)
                .graphicsLayer { translationY = -bobPx * bob },
        )

        Image(
            painter = spritePainter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopCenter)
                // +sink: kadr sprite'a ma pusty margines pod stopami, wiec opuszczamy go o tyle,
                // ile ten margines realnie zajmuje - inaczej sylwetka wisi nad kamieniem.
                .padding(top = (feetY + sink - spriteH).coerceAtLeast(0.dp))
                .height(spriteH)
                .graphicsLayer { translationY = -bobPx * bob },
        )
    }
}

/** Ile szerokosci sceny zajmuje glowny kamien. */
private const val SLAB_WIDTH_FRACTION = 0.70f

/** Ile wysokosci sceny zajmuje KADR sylwetki - widoczna czesc jest o [SPRITE_EDGE_PADDING] mniejsza. */
private const val SPRITE_HEIGHT_FRACTION = 0.50f

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
    val size: Dp,
    val periodMs: Int,
    val amplitude: Dp,
    val startMs: Int,
)

private val PEBBLES = listOf(
    Pebble(resIndex = 0, xFactor = -0.60f, yFactor = 0.28f, size = 20.dp, periodMs = 2600, amplitude = 7.dp, startMs = 0),
    Pebble(resIndex = 1, xFactor = 0.62f, yFactor = 0.16f, size = 15.dp, periodMs = 3400, amplitude = 9.dp, startMs = 520),
    Pebble(resIndex = 2, xFactor = -0.16f, yFactor = 0.74f, size = 24.dp, periodMs = 2100, amplitude = 6.dp, startMs = 960),
    Pebble(resIndex = 3, xFactor = 0.38f, yFactor = 0.66f, size = 13.dp, periodMs = 3900, amplitude = 11.dp, startMs = 240),
    Pebble(resIndex = 4, xFactor = 0.06f, yFactor = 0.52f, size = 22.dp, periodMs = 2900, amplitude = 8.dp, startMs = 1320),
)
