package com.lernhero.game.battle.component

import androidx.compose.animation.core.EaseInOutSine
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
 * Wybor tematu przed tura - siatka 2x2 kafli, wzorowana na Quiz RPG (Mystic Wiz): po lewej stronie
 * kafla swiecaca kula zywiolu, po prawej nazwa kategorii.
 *
 * Kafel z DWOMA zywiolami ([CategoryOption.elements]) ma kule przecieta na pol, po kolorze na
 * strone - dokladnie tak, jak w pierwowzorze sygnalizowane sa podwojne bonusy. To wizualnie
 * najwazniejsza informacja na tym ekranie, bo taki kafel wzmacnia dwie frakcje naraz.
 *
 * Kolory i "kule" sa PLACEHOLDERAMI z [Element.colorHex] - podmiana na prawdziwe PNG/efekty
 * dotyczy wylacznie [ElementOrb] ponizej, reszta ukladu zostaje bez zmian.
 */
@Composable
fun CategoryPickPanel(
    offer: CategoryOffer,
    onPick: (CategoryOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(PanelShape)
            .background(PanelGradient)
            .border(3.dp, FrameGradient, PanelShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "CHOOSE A TOPIC",
            color = Color(0xFF8A6B3A),
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Dwa wiersze po dwa kafle - kazdy bierze rowna czesc wysokosci panelu, zeby siatka
        // wygladala tak samo niezaleznie od tego, jak wysoki jest dol ekranu.
        offer.options.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { option ->
                    CategoryTile(
                        option = option,
                        onClick = { onPick(option) },
                        modifier = Modifier.weight(1f).fillMaxSize()
                    )
                }
                // Niepelny wiersz (gdyby kiedys bylo nieparzyscie) nie ma rozciagac ostatniego kafla.
                repeat(2 - row.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    option: CategoryOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dual = option.elements.size > 1
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFFF6EBD2), Color(0xFFE3D2AE)))
            )
            .border(
                width = if (dual) 3.dp else 2.dp,
                // Kafel dwuzywiolowy dostaje ramke w gradiencie obu kolorow - widac go z daleka.
                brush = if (dual) {
                    Brush.horizontalGradient(option.elements.map { Color(it.colorHex) })
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFFB99A5E), Color(0xFF8A6B3A)))
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ElementOrb(elements = option.elements)
        Text(
            text = option.category.label,
            color = Color(0xFF3B2E1C),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

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

    Box(modifier = modifier.size(40.dp), contentAlignment = Alignment.Center) {
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
            fontSize = if (elements.size > 1) 11.sp else 15.sp
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
