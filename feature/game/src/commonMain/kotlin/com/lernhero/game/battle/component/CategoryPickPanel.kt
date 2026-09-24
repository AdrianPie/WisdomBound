package com.lernhero.game.battle.component

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.monster.Element
import com.lernhero.domain.quiz.CategoryOffer
import com.lernhero.domain.quiz.CategoryOption
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/**
 * Wybor tematu przed tura - cztery kafle 2x2 (mechanika z Quiz RPG / Mystic Wiz).
 *
 * Kafel niesie DWIE decyzje naraz i obie musza byc czytelne bez zgadywania:
 *  - **temat** pytania - duza ikona i nazwa,
 *  - **zywiol**, ktory ta tura wzmocni (+[CategoryOffer.ELEMENT_BOOST] ataku) - kula zywiolu
 *    i podpis. Gdy w druzynie SA kompani tego zywiolu, kafel swieci w jego kolorze i mowi wprost,
 *    ilu z nich skorzysta ("2 allies +20%"). Wczesniej sama kolorowa kula nic nie mowila - gracz
 *    nie wiedzial, ze wybor tematu w ogole cos zmienia w walce.
 *
 * Kafle wypelniaja cale miejsce panelu (dol ekranu ma w tej fazie te sama wysokosc co przy quizie),
 * zamiast stac malym paskiem u gory nad pusta plama.
 */
@Composable
fun CategoryPickPanel(
    offer: CategoryOffer,
    onPick: (CategoryOption) -> Unit,
    modifier: Modifier = Modifier,
    /** Zywioly zywych kompanow - ile kafel realnie wzmocni. Pusta mapa = bez podpowiedzi. */
    teamElements: Map<Element, Int> = emptyMap()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(PanelShape)
            .background(PanelGradient)
            .border(3.dp, FrameGradient, PanelShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose your next question",
                color = InkStrong,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Topic decides the question · element boosts your team",
                color = InkMuted,
                fontFamily = FontFirst(),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }

        // Kafle wjezdzaja po kolei - krotko, ale widac, ze to nowa decyzja, a nie ten sam ekran.
        var shown by remember(offer) { mutableStateOf(0) }
        LaunchedEffect(offer) {
            shown = 0
            repeat(offer.options.size) {
                delay(70)
                shown += 1
            }
        }

        var index = 0
        offer.options.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { option ->
                    val order = index++
                    CategoryTile(
                        option = option,
                        allies = option.elements.sumOf { teamElements[it] ?: 0 },
                        visible = shown > order,
                        onClick = { onPick(option) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                repeat(2 - row.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    option: CategoryOption,
    allies: Int,
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = option.elements.map { Color(it.colorHex) }
    val main = colors.first()
    val relevant = allies > 0

    val appear by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 420f),
        label = "tileAppear"
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 0.95f else 1f, label = "tilePress")

    // Kafel, ktory wzmocni kogos z druzyny, oddycha poswiata - to jest ten "dobry" wybor.
    val glow = rememberInfiniteTransition(label = "tileGlow")
    val glowAlpha by glow.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1100, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "tileGlowAlpha"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                val s = (0.85f + 0.15f * appear) * press
                scaleX = s
                scaleY = s
                alpha = appear.coerceIn(0f, 1f)
                translationY = (1f - appear) * 24f * density
            }
            .clip(TileShape)
            .background(Brush.verticalGradient(listOf(Color(0xFF4A3524), Color(0xFF2A1D12))))
            .background(
                Brush.radialGradient(
                    listOf(main.copy(alpha = if (relevant) 0.45f else 0.22f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = 420f
                )
            )
            .border(
                width = if (relevant) 2.5.dp else 1.5.dp,
                brush = if (colors.size > 1) {
                    Brush.linearGradient(colors.map { it.copy(alpha = if (relevant) glowAlpha else 0.6f) })
                } else {
                    Brush.linearGradient(
                        listOf(
                            main.copy(alpha = if (relevant) glowAlpha else 0.45f),
                            Color(0xFF8A6B3A).copy(alpha = 0.6f)
                        )
                    )
                },
                shape = TileShape
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(10.dp)
    ) {
        // Ikona tematu na gorze, podpis zywiolu na dole - osobne pasy, zeby duza ikona nie
        // wchodzila na tekst bonusu.
        Text(
            text = option.category.icon,
            fontSize = 34.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .graphicsLayer { rotationZ = 8f }
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = option.category.label,
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                modifier = Modifier.padding(end = 40.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ElementOrb(elements = option.elements, modifier = Modifier.size(24.dp))
                Column {
                    Text(
                        text = option.elements.joinToString(" + ") { it.label } +
                            " +${(CategoryOffer.ELEMENT_BOOST * 100).toInt()}%",
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        color = if (relevant) main else Color.White.copy(alpha = 0.75f),
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    Text(
                        text = when (allies) {
                            0 -> "no allies boosted"
                            1 -> "1 ally boosted"
                            else -> "$allies allies boosted"
                        },
                        color = Color.White.copy(alpha = if (relevant) 0.85f else 0.45f),
                        fontFamily = FontFirst(),
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private val TileShape = RoundedCornerShape(16.dp)

/**
 * Kula zywiolu - jeden kolor, albo przy dwoch zywiolach polowa na polowe (pion), z pulsujaca
 * poswiata. PLACEHOLDER: docelowo w to miejsce wchodzi PNG/spritesheet ikony frakcji, uklad
 * kafla sie nie zmienia.
 */
@Composable
private fun ElementOrb(elements: List<Element>, modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition()
    val glow by pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = EaseInOutSine), RepeatMode.Reverse)
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = glow }) {
            val r = size.minDimension / 2f
            val c = Offset(size.width / 2f, size.height / 2f)

            if (elements.size == 1) {
                drawCircle(color = Color(elements[0].colorHex), radius = r, center = c)
            } else {
                // Dwa zywioly: lewa polowa pierwszy, prawa drugi. clipRect na polowie kola daje
                // czysty podzial bez rysowania wlasnej sciezki.
                clipRect(right = c.x) {
                    drawCircle(color = Color(elements[0].colorHex), radius = r, center = c)
                }
                clipRect(left = c.x) {
                    drawCircle(color = Color(elements[1].colorHex), radius = r, center = c)
                }
            }

            // Blik u gory - tanie "szklo", zeby kula nie byla plaskim kolem.
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = r * 0.3f,
                center = Offset(c.x - r * 0.3f, c.y - r * 0.35f)
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = r,
                center = c,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
        }

        Text(
            text = elements.joinToString("") { it.icon },
            fontSize = if (elements.size > 1) 7.sp else 11.sp
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.clipRect(
    left: Float = 0f,
    top: Float = 0f,
    right: Float = size.width,
    bottom: Float = size.height,
    block: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    drawContext.canvas.save()
    drawContext.canvas.clipRect(androidx.compose.ui.geometry.Rect(left, top, right, bottom))
    block()
    drawContext.canvas.restore()
}
