package com.lernhero.home.dev

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.lernhero.shared.theme.ButtonPrimary
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.Surface
import com.lernhero.shared.theme.TextPrimary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@Composable
fun KnightPreviewScreen(
    onBack: () -> Unit
) {
    val parts = remember { knightWorkshopParts() }
    var selectedPart by remember { mutableStateOf(parts.first()) }
    var showContext by remember { mutableStateOf(true) }
    val clipboard = LocalClipboardManager.current

    val rotations = remember {
        mutableStateMapOf<String, Float>().apply {
            parts.forEach { part -> this[part.id] = 0f }
        }
    }
    val originXs = remember {
        mutableStateMapOf<String, Float>().apply {
            parts.forEach { part -> this[part.id] = 0.5f }
        }
    }
    val originYs = remember {
        mutableStateMapOf<String, Float>().apply {
            parts.forEach { part -> this[part.id] = 0.5f }
        }
    }
    // Pivoty zatwierdzone przyciskiem "Zapisz pivot" - tylko one traja do wygenerowanego kodu
    val savedOrigins = remember { mutableStateMapOf<String, Offset>() }

    val rotation = rotations[selectedPart.id] ?: 0f
    val originX = originXs[selectedPart.id] ?: 0.5f
    val originY = originYs[selectedPart.id] ?: 0.5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    text = "Wroc",
                    fontFamily = FontFirst()
                )
            }
            Text(
                text = "Knight Pivot Lab",
                color = TextPrimary,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.titleMedium
            )
        }

        KnightPivotStage(
            parts = parts,
            selectedPart = selectedPart,
            showContext = showContext,
            rotation = rotation,
            originX = originX,
            originY = originY
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(parts, key = { part -> part.id }) { part ->
                if (part.id == selectedPart.id) {
                    Button(
                        onClick = { selectedPart = part },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonPrimary,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(
                            text = part.shortLabel,
                            fontFamily = FontFirst(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { selectedPart = part },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(
                            text = part.shortLabel,
                            fontFamily = FontFirst(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        PivotSlider(
            title = "Rotacja",
            valueText = "${rotation.roundToInt()} deg",
            value = rotation,
            valueRange = -180f..180f,
            onValueChange = { rotations[selectedPart.id] = it }
        )
        PivotSlider(
            title = "Origin X",
            valueText = "${originX.toDisplayValue()} / ${(originX * 512f).roundToInt()} px",
            value = originX,
            valueRange = 0f..1f,
            onValueChange = { originXs[selectedPart.id] = it }
        )
        PivotSlider(
            title = "Origin Y",
            valueText = "${originY.toDisplayValue()} / ${(originY * 512f).roundToInt()} px",
            value = originY,
            valueRange = 0f..1f,
            onValueChange = { originYs[selectedPart.id] = it }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F5F5))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedPart.label,
                    color = TextPrimary,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = selectedPart.joint,
                    color = Color(0xFFF24C00),
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "TransformOrigin(${originX.toCodeValue()}, ${originY.toCodeValue()}) | rotationZ = ${rotation.roundToInt()}f",
                color = Color(0xFF4B4B4B),
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Punkt na pliku 512x512: x=${(originX * 512f).roundToInt()} px, y=${(originY * 512f).roundToInt()} px",
                color = Color(0xFF6D6D6D),
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodySmall
            )
            val isSaved = savedOrigins[selectedPart.id]?.let { it.x == originX && it.y == originY } == true
            Button(
                onClick = { savedOrigins[selectedPart.id] = Offset(originX, originY) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) Color(0xFF3C8A3C) else ButtonPrimary,
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    text = if (isSaved) "Zapisano ✓" else "Zapisz pivot",
                    fontFamily = FontFirst()
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E1E))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KnightPivots (${savedOrigins.size}/${parts.size} zapisane)",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(buildKnightPivotsCode(parts, savedOrigins)))
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Kopiuj kod", fontFamily = FontFirst(), style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                text = buildKnightPivotsCode(parts, savedOrigins),
                color = Color(0xFF9CDB9C),
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F5F5))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pokaz calego rycerza w tle",
                color = TextPrimary,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = showContext,
                onCheckedChange = { showContext = it }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    rotations[selectedPart.id] = 0f
                    originXs[selectedPart.id] = 0.5f
                    originYs[selectedPart.id] = 0.5f
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    text = "Reset czesci",
                    fontFamily = FontFirst()
                )
            }
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonPrimary,
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    text = "Wyjdz",
                    fontFamily = FontFirst()
                )
            }
        }
    }
}

@Composable
private fun KnightPivotStage(
    parts: List<KnightWorkshopPart>,
    selectedPart: KnightWorkshopPart,
    showContext: Boolean,
    rotation: Float,
    originX: Float,
    originY: Float
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E1E))
        ) {
            if (showContext) {
                parts
                    .filterNot { part -> part.id == selectedPart.id }
                    .forEach { part ->
                        KnightPartImage(
                            part = part,
                            alpha = 0.24f
                        )
                    }
            }

            KnightPartImage(
                part = selectedPart,
                rotation = rotation,
                originX = originX,
                originY = originY
            )

            PivotMarker(
                originX = originX,
                originY = originY
            )
        }
    }
}

@Composable
private fun KnightPartImage(
    part: KnightWorkshopPart,
    rotation: Float = 0f,
    originX: Float = 0.5f,
    originY: Float = 0.5f,
    alpha: Float = 1f
) {
    Image(
        painter = painterResource(part.resource),
        contentDescription = part.label,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationZ = rotation
                transformOrigin = TransformOrigin(originX, originY)
                this.alpha = alpha
            }
    )
}

@Composable
private fun PivotMarker(
    originX: Float,
    originY: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val x = size.width * originX
        val y = size.height * originY
        val gridColor = Color.White.copy(alpha = 0.12f)
        val markerColor = Color(0xFFFF4D00)

        for (index in 1..3) {
            val gridX = size.width * index / 4f
            val gridY = size.height * index / 4f
            drawLine(
                color = gridColor,
                start = Offset(gridX, 0f),
                end = Offset(gridX, size.height),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = gridColor,
                start = Offset(0f, gridY),
                end = Offset(size.width, gridY),
                strokeWidth = 1.dp.toPx()
            )
        }

        drawLine(
            color = markerColor.copy(alpha = 0.7f),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = markerColor.copy(alpha = 0.7f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.5.dp.toPx()
        )
        drawCircle(
            color = markerColor,
            radius = 7.dp.toPx(),
            center = Offset(x, y)
        )
        drawCircle(
            color = Color.White,
            radius = 2.5.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

@Composable
private fun PivotSlider(
    title: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 10.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = valueText,
                color = Color(0xFF6D6D6D),
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

private data class KnightWorkshopPart(
    val id: String,
    val label: String,
    val shortLabel: String,
    val joint: String,
    val resource: DrawableResource,
    /** Nazwa pola w Resources.KnightPivots, do ktorego trafi zapisana wartosc. */
    val fieldName: String
)

private fun knightWorkshopParts(): List<KnightWorkshopPart> = listOf(
    KnightWorkshopPart(
        id = "left_leg",
        label = "Lewa noga (tyl)",
        shortLabel = "L biodro",
        joint = "pivot: biodro",
        resource = Resources.Player.leftLeg,
        fieldName = "leftLeg"
    ),
    KnightWorkshopPart(
        id = "right_leg",
        label = "Prawa noga (przod)",
        shortLabel = "R biodro",
        joint = "pivot: biodro",
        resource = Resources.Player.rightLeg,
        fieldName = "rightLeg"
    ),
    KnightWorkshopPart(
        id = "right_hand",
        label = "Prawa reka (tyl, idle)",
        shortLabel = "R bark",
        joint = "pivot: bark",
        resource = Resources.Player.rightHand,
        fieldName = "rightHand"
    ),
    KnightWorkshopPart(
        id = "body",
        label = "Tulow",
        shortLabel = "Tulow",
        joint = "pivot: pas",
        resource = Resources.Player.body,
        fieldName = "body"
    ),
    KnightWorkshopPart(
        id = "head",
        label = "Glowa",
        shortLabel = "Glowa",
        joint = "pivot: szyja",
        resource = Resources.Player.head,
        fieldName = "head"
    ),
    KnightWorkshopPart(
        id = "left_hand",
        label = "Lewa reka (przod, miecz)",
        shortLabel = "L bark",
        joint = "pivot: bark",
        resource = Resources.Player.leftHand,
        fieldName = "leftHand"
    )
)

/** Generuje gotowy do wklejenia blok pol dla Resources.KnightPivots, na podstawie zapisanych pivotow. */
private fun buildKnightPivotsCode(
    parts: List<KnightWorkshopPart>,
    savedOrigins: Map<String, Offset>
): String = parts.joinToString(separator = "\n") { part ->
    val origin = savedOrigins[part.id]
    if (origin != null) {
        "val ${part.fieldName} = TransformOrigin(${origin.x.toCodeValue()}, ${origin.y.toCodeValue()})"
    } else {
        "// ${part.fieldName}: nie zapisano jeszcze pivota"
    }
}

private fun Float.toDisplayValue(): String {
    return ((this * 100f).roundToInt() / 100f).toString()
}

private fun Float.toCodeValue(): String {
    return "${((this * 1000f).roundToInt() / 1000f)}f"
}
