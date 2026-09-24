package com.lernhero.home.city

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.monster.CompanionPower
import com.lernhero.domain.monster.CompanionSpell
import com.lernhero.domain.monster.Element
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.domain.monster.MonsterCatalog
import com.lernhero.domain.monster.MonsterLevels
import com.lernhero.domain.monster.MonsterSpellLevels
import com.lernhero.domain.monster.MonsterStars
import com.lernhero.game.character.ElementEmblem
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.shared.theme.FontFirst
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

/**
 * Indeks potworow w Summon Center - cala pula bannerow w jednej tabeli: HP / ATK / DEF, moc bierna
 * i zaklecia kazdego gatunku. Gracz ma wiedziec, CO moze wylosowac i czym te potwory sie roznia,
 * zanim wyda losy - a nie odkrywac to dopiero po summonie.
 *
 * Liczby to skala WALKI na ★1 i 1. poziomie ([CompanionPower]) - te same, ktore pokaze karta
 * swiezo wylosowanego kompana, wiec tabela niczego nie obiecuje na wyrost.
 */
@Composable
internal fun MonsterIndexTab(
    ownedCounts: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    var rarity by remember { mutableStateOf<Rarity?>(null) }
    var element by remember { mutableStateOf<Element?>(null) }

    val monsters = remember(rarity, element) {
        MonsterCatalog.all
            .filter { rarity == null || it.rarity == rarity }
            .filter { element == null || it.element == element }
            .sortedWith(compareByDescending<MonsterCard> { it.rarity }.thenBy { it.element.ordinal }.thenBy { it.name })
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(label = "All", selected = rarity == null, color = Gold) { rarity = null }
            Rarity.entries.forEach { entry ->
                FilterChip(label = entry.shortLabel(), selected = rarity == entry, color = entry.indexColor()) {
                    rarity = if (rarity == entry) null else entry
                }
            }
            Box(modifier = Modifier.width(4.dp))
            Element.entries.forEach { entry ->
                ElementChip(element = entry, selected = element == entry) {
                    element = if (element == entry) null else entry
                }
            }
        }

        TableHeader(count = monsters.size)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(monsters, key = { it.id }) { card ->
                MonsterIndexRow(card = card, owned = ownedCounts[card.id] ?: 0)
            }
        }
    }
}

@Composable
private fun TableHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Ink.copy(alpha = 0.85f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$count MONSTERS  ·  ★1 LV 1",
            color = Muted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            modifier = Modifier.weight(1f)
        )
        HeaderCell("HP", StatHpColor)
        HeaderCell("ATK", StatAtkColor)
        HeaderCell("DEF", StatDefColor)
    }
}

@Composable
private fun HeaderCell(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Black,
        fontSize = 10.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(STAT_COLUMN)
    )
}

/** Jeden wiersz tabeli: miniatura, nazwa i trzy kolumny liczb, pod spodem moc bierna i zaklecia. */
@Composable
private fun MonsterIndexRow(card: MonsterCard, owned: Int) {
    val accent = card.rarity.indexColor()
    val hp = CompanionPower.hpAt(card, MonsterStars.MIN_STARS, MonsterLevels.MIN_LEVEL)
    val attack = CompanionPower.attackAt(card, MonsterStars.MIN_STARS, MonsterLevels.MIN_LEVEL)
    val defense = (CompanionPower.armorAt(card) * 100).roundToInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.22f), Ink.copy(alpha = 0.92f), Ink.copy(alpha = 0.92f))))
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Portrait(card = card, accent = accent, owned = owned > 0)
            Column(
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = card.name,
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = card.rarity.shortLabel().uppercase(),
                        color = accent,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp
                    )
                    Text(
                        text = if (owned > 0) "OWNED ×$owned" else "NOT OWNED",
                        color = if (owned > 0) Color(0xFF7BE88A) else Muted,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp
                    )
                }
            }
            StatCell("$hp", StatHpColor)
            StatCell("$attack", StatAtkColor)
            StatCell("$defense%", StatDefColor)
        }

        card.passive?.let { passive ->
            AbilityLine(
                icon = passive.icon,
                iconFill = Color(0xFF3B2A0C),
                title = passive.label,
                tag = "PASSIVE",
                tagColor = Gold,
                body = passive.describe()
            )
        }
        card.spells.forEach { spell -> SpellLine(spell) }
    }
}

@Composable
private fun Portrait(card: MonsterCard, accent: Color, owned: Boolean) {
    Box(
        modifier = Modifier
            .size(PORTRAIT)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.45f), Color.Black.copy(alpha = 0.35f))))
            .border(1.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
    ) {
        Image(
            painter = painterResource(MonsterVisualPresets.forSpecies(card.spriteKey).drawable),
            contentDescription = card.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(PORTRAIT)
                .padding(3.dp)
                // Nieposiadane lekko przygaszone - od razu widac, czego jeszcze brakuje w kolekcji.
                .graphicsLayer { alpha = if (owned) 1f else 0.6f }
        )
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)) {
            ElementEmblem(element = card.element, size = 16.dp)
        }
    }
}

@Composable
private fun StatCell(value: String, color: Color) {
    Text(
        text = value,
        color = color,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Black,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = Modifier.width(STAT_COLUMN)
    )
}

@Composable
private fun SpellLine(spell: CompanionSpell) {
    AbilityLine(
        icon = spell.icon,
        iconFill = Color(0xFF14263D),
        title = spell.name,
        tag = spell.kind.label.uppercase(),
        tagColor = if (spell.supportsAllies) Color(0xFF7BE88A) else Color(0xFF8FD0FF),
        body = MonsterSpellLevels.describe(spell, MonsterSpellLevels.MIN_LEVEL)
    )
}

/** Moc albo zaklecie: ikona w kolku, nazwa z etykieta rodzaju, pod spodem co to robi. */
@Composable
private fun AbilityLine(
    icon: String,
    iconFill: Color,
    title: String,
    tag: String,
    tagColor: Color,
    body: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(iconFill)
                .border(1.dp, tagColor.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 12.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = TextBody,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Text(
                    text = tag,
                    color = tagColor,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 7.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(tagColor.copy(alpha = 0.14f))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
            Text(
                text = body,
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) Ink else color,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Black,
        fontSize = 10.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) color else Ink.copy(alpha = 0.8f))
            .border(1.dp, color.copy(alpha = if (selected) 1f else 0.5f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun ElementChip(element: Element, selected: Boolean, onClick: () -> Unit) {
    val color = Color(element.colorHex)
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (selected) color.copy(alpha = 0.55f) else Ink.copy(alpha = 0.8f))
            .border(if (selected) 2.dp else 1.dp, color.copy(alpha = if (selected) 1f else 0.5f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        ElementEmblem(element = element, size = 20.dp)
    }
}

private fun Rarity.shortLabel(): String = when (this) {
    Rarity.Common -> "Common"
    Rarity.Uncommon -> "Uncommon"
    Rarity.Rare -> "Rare"
    Rarity.Epic -> "Epic"
    Rarity.Legendary -> "Legendary"
}

private fun Rarity.indexColor(): Color = when (this) {
    Rarity.Common -> Color(0xFF9DB2C4)
    Rarity.Uncommon -> Color(0xFF3CC878)
    Rarity.Rare -> Color(0xFF4AA3F0)
    Rarity.Epic -> Color(0xFFA67CFA)
    Rarity.Legendary -> Color(0xFFFFB020)
}

private val StatHpColor = Color(0xFF7CF25A)
private val StatAtkColor = Color(0xFFFFB25C)
private val StatDefColor = Color(0xFFB9D3EE)

private val STAT_COLUMN: Dp = 40.dp
private val PORTRAIT: Dp = 50.dp
