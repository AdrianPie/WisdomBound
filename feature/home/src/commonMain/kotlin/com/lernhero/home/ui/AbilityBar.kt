package com.lernhero.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.spell.AbilityLoadout
import com.lernhero.domain.spell.Spell
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.jetbrains.compose.resources.painterResource

private val Ink = Color(0xFF0B0806)
private val Panel = Color(0xFF211A12)
private val Gold = Color(0xFFE8C15A)
private val GoldDim = Color(0xFF6E5730)
private val Muted = Color(0xFF9C8968)
private val AttackGrey = Color(0xFF6C7080)

/** Krok zmiany procentu jednym dotknieciem. */
private const val STEP = 5

/** Stala paleta kolorow segmentow zaklec na pasku - w kolejnosci przypisania. */
private val SpellColors = listOf(
    Color(0xFFE0654F), Color(0xFF3B9DFF), Color(0xFF62D98A),
    Color(0xFFB061FF), Color(0xFFE0913A)
)

/**
 * Pasek zdolnosci: wizualny podzial 100% tury na zwykly atak i przypisane zaklecia, z regulacja
 * procentow. Uzywany w ekwipunku (zapis stały) i przy ognisku (zmiany na czas runu) - dlatego jest
 * osobnym, bezstanowym komponentem: caly stan przychodzi z [ability], zmiany ida przez [onSet].
 */
@Composable
fun AbilityBar(
    ability: AbilityLoadout,
    ownedSpells: List<Spell>,
    onSet: (spellId: String, percent: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorFor = remember(ability.weights.keys) {
        ability.weights.keys.mapIndexed { i, id -> id to SpellColors[i % SpellColors.size] }.toMap()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Panel)
            .border(2.dp, GoldDim, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pasek zdolnosci",
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = "Wolne: ${ability.freePercent}% / ${ability.capacity}%",
                color = if (ability.freePercent == 0) Muted else Gold,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }

        StackedBar(ability = ability, colorFor = colorFor)

        // Legenda: zwykly atak + kazde przypisane zaklecie
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Dot(AttackGrey)
            Text(
                text = "Zwykly atak ${ability.attackPercent}%",
                color = Color(0xFFCDBB99),
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }

        if (ownedSpells.isEmpty()) {
            Text(
                text = "Nie masz zaklec. Kup je w sklepie, a potem przypisz je tutaj.",
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        } else {
            ownedSpells.forEach { spell ->
                SpellRow(
                    spell = spell,
                    percent = ability.percentOf(spell.id),
                    color = colorFor[spell.id] ?: SpellColors.first(),
                    canAdd = ability.freePercent >= STEP,
                    onSet = onSet
                )
            }
        }
    }
}

/** Poziomy pasek 100% zlozony z segmentow: zwykly atak (szary) + zaklecia (kolory). */
@Composable
private fun StackedBar(ability: AbilityLoadout, colorFor: Map<String, Color>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Ink)
    ) {
        // Zaklecia najpierw, atak wypelnia reszte - dzieki temu segmenty maja stabilna kolejnosc.
        ability.weights.entries.forEach { (id, pct) ->
            if (pct > 0) {
                Box(
                    modifier = Modifier
                        .weight(pct.toFloat())
                        .fillMaxHeight()
                        .background(colorFor[id] ?: SpellColors.first())
                )
            }
        }
        if (ability.attackPercent > 0) {
            Box(
                modifier = Modifier
                    .weight(ability.attackPercent.toFloat())
                    .fillMaxHeight()
                    .background(AttackGrey)
            )
        }
    }
}

@Composable
private fun SpellRow(
    spell: Spell,
    percent: Int,
    color: Color,
    canAdd: Boolean,
    onSet: (String, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Ink)
            .border(1.dp, if (percent > 0) color else GoldDim.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Panel)
                .border(1.dp, color, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(spell.icon()),
                contentDescription = spell.name,
                modifier = Modifier.size(20.dp),
                tint = color
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = spell.name,
                color = Gold,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1
            )
            Text(
                text = "$percent% szansy na ture",
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
        }
        StepButton("−", enabled = percent > 0) { onSet(spell.id, percent - STEP) }
        Text(
            text = "$percent%",
            color = color,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.width(38.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        StepButton("+", enabled = canAdd) { onSet(spell.id, percent + STEP) }
    }
}

@Composable
private fun StepButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) Color(0xFFB4451F) else Color(0x33B4451F))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = Color.White.copy(alpha = if (enabled) 1f else 0.4f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color)
    )
}
