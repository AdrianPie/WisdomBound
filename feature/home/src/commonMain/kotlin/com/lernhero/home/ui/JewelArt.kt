package com.lernhero.home.ui

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.jewel.Jewel
import com.lernhero.domain.jewel.Jewels
import com.lernhero.shared.theme.FontFirst

fun Jewel.color(): Color = Color(type.colorHex)

/**
 * Kamien rysowany wektorowo - zastepczo, dopoki nie ma PNG. Szesciokat z fasetkami w kolorze
 * rodzaju; szlif widac po liczbie kropek pod spodem i po sile blasku.
 */
@Composable
fun JewelIcon(jewel: Jewel, size: Dp, modifier: Modifier = Modifier) {
    val base = jewel.color()
    val glow = 0.15f + 0.12f * jewel.grade.ordinal
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            drawCircle(
                brush = Brush.radialGradient(listOf(base.copy(alpha = glow), Color.Transparent)),
                radius = w * 0.5f
            )
            val top = Offset(w * 0.5f, h * 0.10f)
            val upperLeft = Offset(w * 0.18f, h * 0.32f)
            val upperRight = Offset(w * 0.82f, h * 0.32f)
            val lowerLeft = Offset(w * 0.24f, h * 0.62f)
            val lowerRight = Offset(w * 0.76f, h * 0.62f)
            val bottom = Offset(w * 0.5f, h * 0.84f)
            val outline = Path().apply {
                moveTo(top.x, top.y)
                lineTo(upperRight.x, upperRight.y)
                lineTo(lowerRight.x, lowerRight.y)
                lineTo(bottom.x, bottom.y)
                lineTo(lowerLeft.x, lowerLeft.y)
                lineTo(upperLeft.x, upperLeft.y)
                close()
            }
            drawPath(outline, Brush.verticalGradient(listOf(base.lighten(0.45f), base, base.darken(0.55f))))
            val table = Path().apply {
                moveTo(top.x, top.y)
                lineTo(upperRight.x, upperRight.y)
                lineTo(w * 0.5f, h * 0.44f)
                lineTo(upperLeft.x, upperLeft.y)
                close()
            }
            drawPath(table, Color.White.copy(alpha = 0.35f))
            drawLine(Color.Black.copy(alpha = 0.25f), Offset(w * 0.5f, h * 0.44f), bottom, strokeWidth = w * 0.02f)
            drawPath(outline, Color(0xFF1B120A), style = Stroke(width = w * 0.05f))
        }
        Row(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(size * 0.03f)
        ) {
            repeat(jewel.grade.level) {
                Box(
                    modifier = Modifier
                        .size(size * 0.09f)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF1C2))
                        .border(0.5.dp, Color(0xFF1B120A), CircleShape)
                )
            }
        }
    }
}

/** Pusty slot na kamien - wglebienie z plusem, gdy da sie w nim cos osadzic. */
@Composable
fun EmptySocket(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0xFF2A1E14), Color(0xFF4A3620))))
            .border(2.dp, ParchmentPalette.Frame, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("+", color = ParchmentPalette.PaperDark, fontWeight = FontWeight.Black, fontSize = (size.value * 0.4f).sp)
    }
}

/** Lista kamieni z sakiewki do osadzenia w wolnym slocie. */
@Composable
fun JewelPickerDialog(
    pouch: Map<String, Int>,
    onPick: (Jewel) -> Unit,
    onDismiss: () -> Unit
) {
    val owned = Jewels.all.filter { (pouch[it.id] ?: 0) > 0 }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC0B0805))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .padding(24.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(ParchmentPalette.Frame)
                .padding(4.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ParchmentPalette.Paper)
                .clickable(enabled = false) {}
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Choose a jewel", color = ParchmentPalette.Ink, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 17.sp)
            if (owned.isEmpty()) {
                Text(
                    "No jewels yet. Earn them from achievements in the Temple.",
                    color = ParchmentPalette.InkMuted,
                    fontFamily = FontFirst(),
                    fontSize = 12.sp
                )
            }
            Column(
                modifier = Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                owned.forEach { jewel ->
                    JewelRow(jewel = jewel, count = pouch[jewel.id] ?: 0, onClick = { onPick(jewel) })
                }
            }
            ParchmentButton(text = "CLOSE", color = ParchmentPalette.Inset, onClick = onDismiss, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun JewelRow(
    jewel: Jewel,
    count: Int,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ParchmentPalette.Card)
            .border(1.5.dp, jewel.color().copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JewelIcon(jewel = jewel, size = 40.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(jewel.name, color = ParchmentPalette.Ink, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 13.sp)
            Text(jewel.bonusText, color = ParchmentPalette.InkMuted, fontFamily = FontFirst(), fontSize = 11.sp)
        }
        Text("x$count", color = ParchmentPalette.Ink, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 14.sp)
        trailing?.invoke()
    }
}

private fun Color.lighten(factor: Float): Color =
    Color(red + (1f - red) * factor, green + (1f - green) * factor, blue + (1f - blue) * factor, alpha)
