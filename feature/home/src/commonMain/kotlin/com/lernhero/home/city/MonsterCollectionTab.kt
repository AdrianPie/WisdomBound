package com.lernhero.home.city

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.monster.CompanionSpell
import com.lernhero.domain.monster.MonsterSpellLevels
import com.lernhero.domain.monster.MonsterStars
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.home.player.OwnedMonster
import com.lernhero.home.ui.RarityPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.jetbrains.compose.resources.painterResource

private val CollectionGood = Color(0xFF5FD08A)
private val CollectionBad = Color(0xFFD1453D)
private val StarGold = Color(0xFFFFD34D)
private val StarEmpty = Color(0xFF3A2E22)

/**
 * Kolekcja kompanow - uklad jak w mobilnych gachach (Idle Heroes, AFK Arena): **na gorze duzy
 * portret wybranej postaci ze wszystkim, co o niej wiadomo, na dole siatka malych awatarow do
 * przelaczania sie miedzy nimi**.
 *
 * Poprzednia wersja byla lista pelnych kart jedna pod druga - kazda niosla te same informacje,
 * zadna nie byla wyrozniona, a zeby porownac dwa kompany trzeba bylo scrollowac. Tutaj porownanie
 * jest jednym tapnieciem, a szczegoly maja tyle miejsca, ile potrzebuja.
 *
 * Dwie sciezki rozwoju widoczne obok siebie:
 * - **poziom** za zloto, z sufitem wynikajacym z gwiazdek,
 * - **gwiazdki** za DUPLIKATY, ktore ten sufit zdejmuja.
 */
@Composable
fun MonsterCollectionTab(
    collection: List<OwnedMonster>,
    money: Int,
    essence: Int,
    onUpgrade: (OwnedMonster) -> Unit,
    onAscend: (OwnedMonster) -> Unit,
    onDissolve: (OwnedMonster) -> Unit,
    onUpgradeSpell: (OwnedMonster, CompanionSpell) -> Unit,
    modifier: Modifier = Modifier
) {
    if (collection.isEmpty()) {
        EmptyCollection(modifier)
        return
    }

    var selectedId by remember(collection.map { it.card.id }) {
        mutableStateOf(collection.first().card.id)
    }
    val selected = collection.firstOrNull { it.card.id == selectedId } ?: collection.first()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Gorna polowa - wybrany kompan w calej okazalosci.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HeroSpotlight(monster = selected)
            ProgressPanel(
                monster = selected,
                money = money,
                onUpgrade = { onUpgrade(selected) },
                onAscend = { onAscend(selected) },
                onDissolve = { onDissolve(selected) }
            )
            if (selected.card.spells.isNotEmpty()) {
                SpellPanel(
                    monster = selected,
                    essence = essence,
                    onUpgradeSpell = { spell -> onUpgradeSpell(selected, spell) }
                )
            }
        }

        // Dolna polowa - siatka malych awatarow. Tylko tyle, zeby bylo wiadomo kto jest kim.
        CollectionGrid(
            collection = collection,
            selectedId = selected.card.id,
            onSelect = { selectedId = it },
            modifier = Modifier.height(150.dp)
        )
    }
}

@Composable
private fun EmptyCollection(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(text = "🗿", fontSize = 40.sp)
            Text(
                text = "Nie masz jeszcze zadnych kompanow",
                color = TextBody,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = "Przyzwij ich w zakladce obok - losy wypadaja ze skrzyni po bossie.",
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Duzy portret wybranego kompana: sprite, gwiazdki, poziom i statystyki bojowe. */
@Composable
private fun HeroSpotlight(monster: OwnedMonster) {
    val card = monster.card
    val accent = RarityPalette.frame(card.rarity)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(listOf(accent.copy(alpha = 0.30f), Color(0xFF0B0805)))
            )
            .border(2.dp, accent, RoundedCornerShape(14.dp))
    ) {
        Image(
            painter = painterResource(MonsterVisualPresets.forSpecies(card.spriteKey).drawable),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxSize()
                .padding(top = 34.dp, bottom = 40.dp)
        )

        Column(
            modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = card.name,
                color = TextBody,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            StarRow(stars = monster.stars)
            Text(
                text = "${RarityPalette.label(card.rarity)} · x${monster.count}",
                color = accent,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }

        Text(
            text = "LV ${monster.level}/${monster.levelCap}",
            color = StarGold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Ink.copy(alpha = 0.85f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Ink.copy(alpha = 0.85f))
                .padding(vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SpotlightStat("❤", "${monster.hp}", "HP")
            SpotlightStat("⚔", "${monster.attack}", "atak")
            if (monster.inParty > 0) SpotlightStat("🛡", "${monster.inParty}", "w druzynie")
        }
    }
}

@Composable
private fun SpotlightStat(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 11.sp)
            Text(
                text = value,
                color = TextBody,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Text(text = label, color = Muted, fontFamily = FontFirst(), fontSize = 8.sp)
    }
}

/** Rzad gwiazdek - zapalone do [stars], reszta wygaszona, zeby bylo widac ile jeszcze zostalo. */
@Composable
private fun StarRow(stars: Int, size: Int = 13) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        (1..MonsterStars.MAX_STARS).forEach { index ->
            Text(
                text = "★",
                color = if (index <= stars) StarGold else StarEmpty,
                fontSize = size.sp
            )
        }
    }
}

/** Dwie sciezki rozwoju obok siebie: poziom za zloto i gwiazdki za duplikaty. */
@Composable
private fun ProgressPanel(
    monster: OwnedMonster,
    money: Int,
    onUpgrade: () -> Unit,
    onAscend: () -> Unit,
    onDissolve: () -> Unit
) {
    TownPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- Poziom (zloto) ---
            LevelBar(monster = monster)
            ActionButton(
                label = if (monster.atLevelCap) "SUFIT ${monster.stars}★" else "POZIOM +1",
                detail = if (monster.atLevelCap) "awansuj gwiazdke" else "💰 ${monster.levelUpCost}",
                enabled = !monster.atLevelCap && money >= monster.levelUpCost,
                accent = StarGold,
                onClick = onUpgrade
            )

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(StarEmpty))

            // --- Gwiazdki (duplikaty) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AWANS GWIAZDKI",
                        color = StarGold,
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (monster.maxStars) {
                            "Maksymalne gwiazdki - duplikaty rozpuszczasz w esencje."
                        } else {
                            "Zdejmuje sufit poziomu do ${MonsterStars.levelCapFor(monster.stars + 1)}."
                        },
                        color = Muted,
                        fontFamily = FontFirst(),
                        fontSize = 9.sp
                    )
                }
                if (!monster.maxStars) {
                    Text(
                        text = "${monster.duplicates}/${monster.ascendCost} kopii",
                        color = if (monster.canAscend) CollectionGood else CollectionBad,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            if (monster.maxStars) {
                ActionButton(
                    label = "ROZPUSC KOPIE",
                    detail = "✨ +${monster.dissolveValue}",
                    enabled = monster.canDissolve,
                    accent = Color(0xFF8A5FD0),
                    onClick = onDissolve
                )
            } else {
                ActionButton(
                    label = "AWANSUJ ★",
                    detail = "${monster.ascendCost} duplikatow",
                    enabled = monster.canAscend,
                    accent = CollectionGood,
                    onClick = onAscend
                )
            }
        }
    }
}

@Composable
private fun LevelBar(monster: OwnedMonster) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "POZIOM",
                color = StarGold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Text(
                text = "${monster.level} / ${monster.levelCap}",
                color = if (monster.atLevelCap) CollectionGood else TextBody,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF120C08))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(monster.level.toFloat() / monster.levelCap.coerceAtLeast(1))
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50))
                    .background(if (monster.atLevelCap) CollectionGood else StarGold)
            )
        }
    }
}

/** Zaklecia kompana - druga sciezka rozwoju, tym razem za esencje. */
@Composable
private fun SpellPanel(
    monster: OwnedMonster,
    essence: Int,
    onUpgradeSpell: (CompanionSpell) -> Unit
) {
    TownPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TownSectionLabel("Zaklecia")
                Text(
                    text = "✨ $essence",
                    color = TextBody,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            monster.card.spells.forEach { spell ->
                val level = monster.spellLevel(spell)
                val maxed = MonsterSpellLevels.isMaxed(level)
                val cost = MonsterSpellLevels.upgradeCost(level)
                val affordable = !maxed && essence >= cost

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Ink.copy(alpha = 0.7f))
                        .border(1.dp, StarEmpty, RoundedCornerShape(10.dp))
                        .padding(horizontal = 9.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = spell.icon, fontSize = 18.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                text = spell.name,
                                color = TextBody,
                                fontFamily = FontFirst(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "LV$level",
                                color = StarGold,
                                fontFamily = FontFirst(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = MonsterSpellLevels.describe(spell, level),
                            color = Muted,
                            fontFamily = FontFirst(),
                            fontSize = 9.sp
                        )
                    }
                    Text(
                        text = if (maxed) "MAX" else "✨$cost",
                        color = when {
                            maxed -> CollectionGood
                            affordable -> StarGold
                            else -> CollectionBad
                        },
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (affordable) Color(0xFF2A2018) else Color.Transparent)
                            .border(
                                1.dp,
                                if (affordable) StarGold else Color.Transparent,
                                RoundedCornerShape(7.dp)
                            )
                            .clickable(enabled = affordable) { onUpgradeSpell(spell) }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

/**
 * Siatka malych awatarow. Celowo tylko sprite, gwiazdki i licznik kopii - to wystarczy, zeby
 * wiedziec "kto jest kim", a szczegoly i tak sa nad nia.
 */
@Composable
private fun CollectionGrid(
    collection: List<OwnedMonster>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 62.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Ink.copy(alpha = 0.6f))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(collection, key = { it.card.id }) { monster ->
            CollectionAvatar(
                monster = monster,
                selected = monster.card.id == selectedId,
                onClick = { onSelect(monster.card.id) }
            )
        }
    }
}

@Composable
private fun CollectionAvatar(
    monster: OwnedMonster,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = RarityPalette.frame(monster.card.rarity)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(RarityPalette.cardBackground(monster.card.rarity))
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = if (selected) StarGold else accent,
                shape = RoundedCornerShape(9.dp)
            )
            .clickable(onClick = onClick)
            .padding(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.BottomCenter) {
            Image(
                painter = painterResource(MonsterVisualPresets.forSpecies(monster.card.spriteKey).drawable),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            if (monster.duplicates > 0) {
                Text(
                    text = "+${monster.duplicates}",
                    color = Color.White,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFB4451F))
                        .padding(horizontal = 3.dp)
                )
            }
        }
        StarRow(stars = monster.stars, size = 7)
        Text(
            text = "LV${monster.level}",
            color = Muted,
            fontFamily = FontFirst(),
            fontSize = 8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    detail: String,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(if (enabled) Color(0xFF2A2018) else Color(0xFF181310))
            .border(1.5.dp, if (enabled) accent else StarEmpty, RoundedCornerShape(9.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 9.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (enabled) accent else Color(0xFF5A5048),
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
        Text(
            text = detail,
            color = if (enabled) Muted else Color(0xFF4A4038),
            fontFamily = FontFirst(),
            fontSize = 10.sp
        )
    }
}
