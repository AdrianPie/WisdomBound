package com.lernhero.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.item.Item
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

private val Sheet = Color(0xFF19120C)
private val Ink = Color(0xFF120E0A)
private val Frame = Color(0xFFC8A45A)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val GoldText = Color(0xFFFFD34D)
private val Better = Color(0xFF5FD08A)
private val Worse = Color(0xFFE56A5D)

/**
 * Karta przedmiotu - wzorowana na tym, jak robia to mobilne RPG (AFK Arena, Diablo Immortal,
 * RAID): TAPNIECIE, nie dlugie przytrzymanie, otwiera arkusz z pelnymi statystykami, opisem i
 * PORONWANIEM z tym, co masz aktualnie zalozone. Dopiero z tego arkusza zaklada sie przedmiot.
 *
 * Dlaczego tap, a nie long-press: na telefonie dlugie przytrzymanie jest niewykrywalne - nikt nie
 * zgadnie, ze trzeba trzymac. Tap jest jedynym gestem, ktory gracz na pewno wykona, a natychmiastowe
 * zakladanie po tapnieciu (tak bylo wczesniej) odbieralo jedyna okazje, zeby pokazac, CO ten
 * przedmiot w ogole robi.
 *
 * [comparedTo] to przedmiot aktualnie w tym slocie (null, gdy slot pusty) - roznice statystyk
 * pokazuja sie wtedy na zielono/czerwono, dokladnie jak w wymienionych grach.
 */
@Composable
fun ItemDetailSheet(
    item: Item,
    comparedTo: Item?,
    equipped: Boolean,
    onEquip: () -> Unit,
    onUnequip: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val frame = RarityPalette.frame(item.rarity)
    val rows = remember(item, comparedTo) { statRows(item, comparedTo) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC07050B))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Sheet)
                .border(2.dp, frame, RoundedCornerShape(16.dp))
                .clickable(enabled = false) {}
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RarityPalette.cardBackground(item.rarity))
                    .border(2.dp, frame, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon(), fontSize = 36.sp)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.name,
                    color = Cream,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${item.slot.label()} · ${RarityPalette.label(item.rarity)}",
                    color = frame,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            if (rows.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Ink)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rows.forEach { row -> StatRow(row) }
                }
            }

            // Efekty specjalne (rozciecie, odbicie, aury) - to one odrozniaja przedmiot legendarny
            // od "tego samego, tylko z wiekszymi liczbami".
            if (item.effects.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item.effects.forEach { effect ->
                        Text(
                            text = "✦ ${effect.description}",
                            color = GoldText,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            comparedTo?.let { current ->
                Text(
                    text = "Porownanie z: ${current.name}",
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (equipped) {
                    SheetButton(
                        label = "ZDEJMIJ",
                        accent = Worse,
                        onClick = onUnequip,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    SheetButton(
                        label = "ZALOZ",
                        accent = Better,
                        onClick = onEquip,
                        modifier = Modifier.weight(1f)
                    )
                }
                SheetButton(
                    label = "ZAMKNIJ",
                    accent = Frame,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Jedna linijka statystyki: ile daje ten przedmiot i o ile to lepiej/gorzej niz obecny. */
private data class StatRow(
    val label: String,
    val value: String,
    val delta: Float,
    val deltaLabel: String?
)

private fun statRows(item: Item, comparedTo: Item?): List<StatRow> =
    STAT_ORDER.mapNotNull { type ->
        val mine = item.stats[type]
        val theirs = comparedTo?.stats?.get(type) ?: 0f
        if (mine == 0f && theirs == 0f) return@mapNotNull null

        val delta = mine - theirs
        StatRow(
            label = type.label(),
            value = type.format(mine),
            delta = delta,
            deltaLabel = if (comparedTo != null && delta != 0f) {
                (if (delta > 0) "+" else "") + type.format(delta)
            } else {
                null
            }
        )
    }

@Composable
private fun StatRow(row: StatRow) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = row.label, color = Muted, fontFamily = FontFirst(), fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = row.value,
                color = Cream,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            row.deltaLabel?.let { label ->
                Text(
                    text = label,
                    color = if (row.delta > 0) Better else Worse,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun SheetButton(
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        color = accent,
        fontFamily = FontPixel(),
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(Ink)
            .border(1.5.dp, accent, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp)
    )
}

/* ---- Wspolne dla karty przedmiotu i panelu statystyk postaci ---- */

internal val STAT_ORDER = listOf(
    StatType.MaxHp, StatType.Attack, StatType.Defense, StatType.Speed,
    StatType.CritChance, StatType.CritDamage, StatType.Lifesteal, StatType.Dodge,
    StatType.DamageReduction, StatType.SpellPower, StatType.HealPower,
    StatType.CooldownReduction, StatType.AttackTargets
)

internal fun StatType.label(): String = when (this) {
    StatType.MaxHp -> "Zdrowie"
    StatType.Attack -> "Atak"
    StatType.Defense -> "Obrona"
    StatType.Speed -> "Szybkosc"
    StatType.CritChance -> "Szansa na kryta"
    StatType.CritDamage -> "Sila kryta"
    StatType.Dodge -> "Unik"
    StatType.Lifesteal -> "Kradziez zycia"
    StatType.DamageReduction -> "Redukcja obrazen"
    StatType.HealPower -> "Sila leczenia"
    StatType.CooldownReduction -> "Skrocenie odnowienia"
    StatType.AttackTargets -> "Dodatkowe cele"
    StatType.SpellPower -> "Sila zaklec"
}

internal fun StatType.isPercent(): Boolean = this in setOf(
    StatType.CritChance, StatType.CritDamage, StatType.Dodge, StatType.Lifesteal,
    StatType.DamageReduction, StatType.HealPower, StatType.CooldownReduction, StatType.SpellPower
)

internal fun StatType.format(value: Float): String =
    if (isPercent()) "${(value * 100).toInt()}%" else value.toInt().toString()
