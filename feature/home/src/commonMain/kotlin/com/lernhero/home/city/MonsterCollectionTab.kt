package com.lernhero.home.city

import com.lernhero.shared.theme.GlyphText
import androidx.compose.foundation.Image
import com.lernhero.home.ui.ItemArtImage
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemInstance
import com.lernhero.domain.item.resolve
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.catalog.MonsterGear
import com.lernhero.domain.monster.CompanionSpell
import com.lernhero.domain.monster.Element
import com.lernhero.domain.monster.CompanionPower
import kotlin.math.roundToInt
import com.lernhero.domain.monster.MonsterEquipment
import com.lernhero.domain.monster.MonsterSpellLevels
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.home.player.OwnedMonster
import com.lernhero.home.ui.FloatingIsland
import com.lernhero.home.ui.ParchmentPalette
import com.lernhero.home.ui.RarityPalette
import com.lernhero.home.ui.badgeColor
import com.lernhero.home.ui.format
import com.lernhero.home.ui.icon
import com.lernhero.home.ui.shortLabel
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/** Ktora tablica jest widoczna w dolnym panelu. */
private enum class Pane(val label: String) {
    Info("Info"),
    Gear("Gear")
}

/**
 * Kolekcja kompanow jako JEDEN ekran karty postaci - bez przewijania w pionie.
 *
 * ```
 *  ‹   [sylwetka]   ›     <- scena, strzalki przerzucaja kompana
 *  +-------------------+
 *  | Info | Ekwipunek  |  <- przelacznik SIEDZI W PANELU, nie przy sylwetce
 *  | statystyki/sloty  |
 *  +-------------------+
 *  [av][av][av][av] (‹›)  <- staly pasek miniatur, przewijany w POZIOMIE
 * ```
 *
 * Trzy sekcje maja stale proporcje ([Column] z waga na scenie), wiec ekran nigdy sie nie wydluza:
 * to pasek miniatur jeździ w bok, a nie caly widok w dol. Wczesniej wszystko bylo pod soba i
 * dwa razy wieksze - zeby zobaczyc statystyki, trzeba bylo scrollowac obok wlasnej postaci.
 */
@Composable
fun MonsterCollectionTab(
    collection: List<OwnedMonster>,
    money: Int,
    essence: Int,
    equipmentOf: (String) -> Map<String, String>,
    /**
     * Plecak sprzetu kompanow jako SZTUKI - poziom ulepszenia siedzi w egzemplarzu, a sloty
     * wskazuja na uid, wiec bez tej listy nie da sie powiedziec, co kompan ma na sobie.
     */
    bag: List<ItemInstance>,
    gearTakenBy: (String) -> Set<String>,
    onUpgrade: (OwnedMonster) -> Unit,
    onAscend: (OwnedMonster) -> Unit,
    onDissolve: (OwnedMonster) -> Unit,
    onUpgradeSpell: (OwnedMonster, CompanionSpell) -> Unit,
    onEquip: (String, ItemInstance) -> Unit,
    onUnequipSlot: (String, ItemSlot) -> Unit,
    onUnequipAll: (String) -> Unit,
    onAutoEquip: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (collection.isEmpty()) {
        EmptyCollection(modifier)
        return
    }

    var selectedId by remember(collection.map { it.card.id }) {
        mutableStateOf(collection.first().card.id)
    }
    var pane by remember { mutableStateOf(Pane.Info) }
    var pickerSlot by remember { mutableStateOf<ItemSlot?>(null) }
    var spellSheet by remember { mutableStateOf<CompanionSpell?>(null) }

    val index = collection.indexOfFirst { it.card.id == selectedId }.coerceAtLeast(0)
    val selected = collection[index]
    val equipment = equipmentOf(selected.card.id)

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Stage(
            monster = selected,
            // Strzalki przerzucaja kompana w kolko - na koncu listy wracamy na poczatek,
            // zeby przy trzech kartach nie trzeba bylo klikac w druga strone.
            onPrev = { selectedId = collection[(index - 1 + collection.size) % collection.size].card.id },
            onNext = { selectedId = collection[(index + 1) % collection.size].card.id },
            modifier = Modifier.weight(1f)
        )

        Board(
            monster = selected,
            equipment = equipment,
            bag = bag,
            money = money,
            essence = essence,
            pane = pane,
            onPane = { pane = it },
            onUpgrade = { onUpgrade(selected) },
            onAscend = { onAscend(selected) },
            onDissolve = { onDissolve(selected) },
            // Tapniecie w czar otwiera jego karte (co robi, co da nastepny poziom, ile kosztuje) -
            // ulepszenie dopiero stamtad, swiadomie, a nie przypadkiem przy probie podejrzenia.
            onUpgradeSpell = { spellSheet = it },
            onSlotClick = { slot ->
                // Zajety slot ZDEJMUJE, pusty otwiera liste - dwa rozne gesty na jednym
                // kafelku byłyby nie do odgadniecia.
                if (MonsterEquipment.instanceIn(equipment, slot, bag) != null) onUnequipSlot(selected.card.id, slot)
                else pickerSlot = slot
            },
            onUnequipAll = { onUnequipAll(selected.card.id) },
            onAutoEquip = { onAutoEquip(selected.card.id) }
        )

        CollectionStrip(
            collection = collection,
            selectedId = selected.card.id,
            onSelect = { selectedId = it }
        )
    }

    spellSheet?.let { spell ->
        SpellSheet(
            spell = spell,
            level = selected.spellLevel(spell),
            essence = essence,
            element = selected.card.element,
            onUpgrade = { onUpgradeSpell(selected, spell) },
            onDismiss = { spellSheet = null }
        )
    }

    pickerSlot?.let { slot ->
        val taken = gearTakenBy(selected.card.id)
        // Kandydaci to SZTUKI, nie typy: dwie kopie tego samego pazura to dwie osobne pozycje,
        // bo kazda moze byc na innym poziomie i tylko jedna z nich jest wolna.
        val candidates = bag
            .filter { it.uid !in taken }
            .mapNotNull { instance -> instance.resolve()?.let { instance to it } }
            .filter { (_, item) -> item.slot == slot }
            .sortedByDescending { (_, item) -> MonsterEquipment.scoreFor(item, selected.card.element) }

        GearPicker(
            slot = slot,
            items = candidates,
            element = selected.card.element,
            onPick = {
                onEquip(selected.card.id, it)
                pickerSlot = null
            },
            onDismiss = { pickerSlot = null }
        )
    }
}

/* --------------------------------------------- Scena -------------------------------------------- */

/** Sylwetka kompana ze strzalkami po bokach i podpisem u gory. Tlo to placeholder w kolorze zywiolu. */
@Composable
private fun Stage(
    monster: OwnedMonster,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val card = monster.card

    // Bez tla i bez ramki - tlo Summon Center jest globalne ([SummonCenterScreen]) i ma byc
    // widoczne wszedzie, a nie wycinane w prostokat tej sceny.
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        FloatingIsland(
            spritePainter = painterResource(MonsterVisualPresets.forSpecies(card.spriteKey).drawable),
            modifier = Modifier.fillMaxSize()
        )

        // Podpis lezy NA scenie, a nie nad nia - to on oszczedza pionowy pas, ktorego brakuje
        // na zmieszczenie wszystkiego bez scrolla.
        Row(
            modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlyphText(
                text = card.name,
                color = Color(0xFFFFF1D6),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black, androidx.compose.ui.geometry.Offset(0f, 3f), 6f)
                )
            )
            ElementDot(card.element)
        }

        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            repeat(5) { i ->
                GlyphText(
                    text = "★",
                    color = if (i < monster.stars) Color(0xFFFFC93C) else Color(0x88FFFFFF),
                    fontSize = 20.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(Color.Black, androidx.compose.ui.geometry.Offset(0f, 2f), 4f)
                    )
                )
            }
        }

        Arrow(text = "‹", modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp), onClick = onPrev)
        Arrow(text = "›", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp), onClick = onNext)

        GlyphText(
            text = "x${monster.count}",
            color = Color(0xFFEADFC8),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0x88000000))
                .padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun Arrow(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xCC3D2A18))
            .border(2.dp, ParchmentPalette.Button.copy(alpha = 0.8f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        GlyphText(text = text, color = ParchmentPalette.Button, fontSize = 26.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ElementDot(element: Element) {
    com.lernhero.game.character.ElementEmblem(element = element, size = 26.dp)
}

/* ------------------------------------------- Panel dolny ---------------------------------------- */

/**
 * Wysokosc TRESCI panelu - identyczna dla obu zakladek, zeby przelaczanie Info/Gear nie
 * podskakiwalo calym ekranem.
 */
private val PANE_HEIGHT = 136.dp

/*
 * Ciemny panel ze zlota oprawa - ten sam jezyk co zakladki i bannery Summon Center. Jasny
 * pergamin wygladal tu jak wklejka z innego ekranu, a drobne emoji i plaskie przyciski ginely.
 */
private val BoardTop = Color(0xFF36271A)
private val BoardMid = Color(0xFF23180F)
private val BoardBottom = Color(0xFF170F09)
private val BoardGold = Color(0xFFE8C56A)
private val BoardCream = Color(0xFFF3E6C8)
private val BoardMuted = Color(0xFFB09C7C)
private val SocketFill = Color(0xFF120B06)
private val GoldFrame = Brush.verticalGradient(listOf(Color(0xFFFFE9A8), Color(0xFFC8A45A), Color(0xFF8A6B34)))

@Composable
private fun Board(
    monster: OwnedMonster,
    equipment: Map<String, String>,
    bag: List<ItemInstance>,
    money: Int,
    essence: Int,
    pane: Pane,
    onPane: (Pane) -> Unit,
    onUpgrade: () -> Unit,
    onAscend: () -> Unit,
    onDissolve: () -> Unit,
    onUpgradeSpell: (CompanionSpell) -> Unit,
    onSlotClick: (ItemSlot) -> Unit,
    onUnequipAll: () -> Unit,
    onAutoEquip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(BoardTop, BoardMid, BoardBottom)))
            .border(2.5.dp, GoldFrame, RoundedCornerShape(16.dp))
            .padding(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        PaneTabs(pane = pane, onPane = onPane)

        Box(modifier = Modifier.fillMaxWidth().height(PANE_HEIGHT)) {
            when (pane) {
                Pane.Info -> InfoPane(
                    monster = monster,
                    equipment = equipment,
                    bag = bag,
                    money = money,
                    essence = essence,
                    onUpgrade = onUpgrade,
                    onAscend = onAscend,
                    onDissolve = onDissolve,
                    onUpgradeSpell = onUpgradeSpell
                )

                Pane.Gear -> GearPane(
                    monster = monster,
                    equipment = equipment,
                    bag = bag,
                    onSlotClick = onSlotClick,
                    onUnequipAll = onUnequipAll,
                    onAutoEquip = onAutoEquip
                )
            }
        }
    }
}

/** Zakladki segmentowe: ciemne gniazdo, aktywna to zloty "guzik" z ciemnym napisem. */
@Composable
private fun PaneTabs(pane: Pane, onPane: (Pane) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SocketFill)
            .border(1.dp, BoardGold.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Pane.entries.forEach { entry ->
            val active = entry == pane
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (active) Modifier
                            .background(Brush.verticalGradient(listOf(Color(0xFFFFE08A), Color(0xFFE8A63C))))
                            .border(1.dp, Color(0xFF8A5A10), RoundedCornerShape(8.dp))
                        else Modifier
                    )
                    .clickable { onPane(entry) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                GlyphText(
                    text = entry.label.uppercase(),
                    color = if (active) Color(0xFF2A1A08) else BoardCream.copy(alpha = 0.7f),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

/* -------------------------------------------- Info ---------------------------------------------- */

/**
 * Trzy wiersze:
 *
 * ```
 * ⚡381 POWER   ◆◆◇◇ [★0/2]            LV 1/10
 * [ HP 426 ][ ATK 56 ][ ARMOR 30% ][ SPEED 0 ]
 * [ LEVEL UP        💰 145 ]   (czar)(czar)
 * ```
 */
@Composable
private fun InfoPane(
    monster: OwnedMonster,
    equipment: Map<String, String>,
    bag: List<ItemInstance>,
    money: Int,
    essence: Int,
    onUpgrade: () -> Unit,
    onAscend: () -> Unit,
    onDissolve: () -> Unit,
    onUpgradeSpell: (CompanionSpell) -> Unit
) {
    // Statystyki pokazujemy JUZ z ekwipunkiem, w tej samej skali co walka (patrz [CompanionPower]).
    val gear = MonsterEquipment.statsOf(equipment, monster.card.element, bag)
    val hp = CompanionPower.hpAt(monster.card, monster.stars, monster.level, gear)
    val attack = CompanionPower.attackAt(monster.card, monster.stars, monster.level, gear)
    val armor = (CompanionPower.armorAt(monster.card, gear) * 100f).roundToInt()
    val speed = gear[StatType.Speed].toInt()
    val accent = monster.card.element.badgeColor()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                com.lernhero.shared.theme.StrokedText(
                    text = "⚡${attack * 3 + hp / 2}",
                    size = 18.sp,
                    color = BoardGold,
                    outline = Color(0xFF0E0804),
                    strokeWidth = 6f,
                    maxLines = 1
                )
                GlyphText(
                    text = "POWER",
                    color = BoardMuted,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            // Cztery klejnoty = cztery awanse gwiazdki ponad startowa.
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(4) { i -> TierGem(filled = i < monster.stars - 1, accent = accent) }
            }
            TinyButton(
                text = if (monster.maxStars) "MAX" else "★${monster.duplicates}/${monster.ascendCost}",
                enabled = monster.canAscend,
                color = Color(0xFFF2B832),
                onClick = onAscend
            )
            if (monster.canDissolve) {
                TinyButton(text = "✨", enabled = true, color = Color(0xFF9A6AD6), onClick = onDissolve)
            }
            Box(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.Bottom) {
                GlyphText(
                    text = "LV ",
                    color = BoardGold,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
                GlyphText(
                    text = "${monster.level}/${monster.levelCap}",
                    color = BoardCream,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            StatCell("❤️", "$hp", "HP", Color(0xFFFF6B6B), Modifier.weight(1f))
            StatCell("⚔️", "$attack", "ATTACK", Color(0xFFFFB347), Modifier.weight(1f))
            StatCell("🛡️", "$armor%", "ARMOR", Color(0xFF7FB3FF), Modifier.weight(1f))
            StatCell("👟", "$speed", "SPEED", Color(0xFF7CE08A), Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val canUp = !monster.atLevelCap && money >= monster.levelUpCost
            LevelUpButton(
                capped = monster.atLevelCap,
                cost = monster.levelUpCost,
                affordable = money >= monster.levelUpCost,
                enabled = canUp,
                onClick = onUpgrade,
                modifier = Modifier.weight(1f)
            )
            monster.card.spells.take(3).forEach { spell ->
                val level = monster.spellLevel(spell)
                val cost = MonsterSpellLevels.upgradeCost(level)
                SpellDisc(
                    spell = spell,
                    level = level,
                    accent = accent,
                    canUpgrade = !MonsterSpellLevels.isMaxed(level) && essence >= cost,
                    onClick = { onUpgradeSpell(spell) }
                )
            }
        }
    }
}

/** Jedna statystyka: kolorowa kreska u gory, etykieta i wartosc w ciemnym gniezdzie. */
@Composable
private fun StatCell(icon: String, value: String, label: String, tint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF2C2016), SocketFill)))
            .border(1.dp, tint.copy(alpha = 0.45f), RoundedCornerShape(9.dp))
            .drawBehind {
                drawRect(tint, size = androidx.compose.ui.geometry.Size(size.width, 2.dp.toPx()))
            }
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            GlyphText(text = icon, fontSize = 9.sp)
            GlyphText(
                text = label,
                color = BoardMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 8.sp,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
        }
        GlyphText(
            text = value,
            color = BoardCream,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            maxLines = 1
        )
    }
}

/** Klejnot gwiazdki: romb w kolorze zywiolu z blikiem albo puste, zlocone gniazdo. */
@Composable
private fun TierGem(filled: Boolean, accent: Color) {
    Box(
        modifier = Modifier
            .size(13.dp)
            .graphicsLayer { rotationZ = 45f }
            .clip(RoundedCornerShape(3.dp))
            .background(
                if (filled) Brush.linearGradient(listOf(Color.White, accent, accent.copy(red = accent.red * 0.6f, green = accent.green * 0.6f, blue = accent.blue * 0.6f)))
                else Brush.linearGradient(listOf(Color(0xFF2A1D12), SocketFill))
            )
            .border(1.dp, if (filled) Color.White.copy(alpha = 0.7f) else BoardGold.copy(alpha = 0.45f), RoundedCornerShape(3.dp))
    )
}

/**
 * Krazek zaklecia z poziomem w rogu. Tapniecie otwiera karte czaru ([SpellSheet]); zielona
 * strzalka w gornym rogu znaczy "stac cie na ulepszenie".
 */
@Composable
private fun SpellDisc(
    spell: CompanionSpell,
    level: Int,
    accent: Color,
    canUpgrade: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.size(40.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.45f), SocketFill)))
                .border(
                    width = 2.dp,
                    brush = if (canUpgrade) Brush.verticalGradient(listOf(Color(0xFFB8FF9A), Color(0xFF3FA83A)))
                    else GoldFrame,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            GlyphText(text = spell.icon, fontSize = 17.sp)
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(15.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A1A08))
                .border(1.dp, BoardGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            GlyphText(text = "$level", color = BoardCream, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 8.sp, lineHeight = 9.sp)
        }
        if (canUpgrade) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF8BE05A), Color(0xFF2F8A2A))))
                    .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                GlyphText(text = "▲", color = Color.White, fontWeight = FontWeight.Black, fontSize = 7.sp, lineHeight = 8.sp)
            }
        }
    }
}

/**
 * Karta czaru kompana: co robi, jak czesto wchodzi w walce, co da nastepny poziom i ile kosztuje.
 * Czary dzialaja w walce same - kompan zamiast zwyklego ciosu rzuca je z podana szansa.
 */
@Composable
private fun SpellSheet(
    spell: CompanionSpell,
    level: Int,
    essence: Int,
    element: Element,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    val maxed = MonsterSpellLevels.isMaxed(level)
    val cost = MonsterSpellLevels.upgradeCost(level)
    val canUpgrade = !maxed && essence >= cost
    val accent = element.badgeColor()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC07050B))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(BoardTop, BoardMid, BoardBottom)))
                .border(2.5.dp, GoldFrame, RoundedCornerShape(16.dp))
                .clickable(enabled = false) {}
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.5f), SocketFill)))
                        .border(2.dp, GoldFrame, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    GlyphText(text = spell.icon, fontSize = 26.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    com.lernhero.shared.theme.StrokedText(
                        text = spell.name,
                        size = 17.sp,
                        color = BoardCream,
                        outline = Color(0xFF0E0804),
                        strokeWidth = 5f,
                        maxLines = 1
                    )
                    GlyphText(
                        text = "${spell.kind.label.uppercase()} · LV $level/${MonsterSpellLevels.MAX_LEVEL}",
                        color = BoardGold,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp
                    )
                }
            }
            GlyphText(text = spell.description, color = BoardCream, fontFamily = FontFirst(), fontSize = 12.sp)
            SpellLine(label = "NOW", text = MonsterSpellLevels.describe(spell, level), tint = BoardGold)
            if (!maxed) {
                SpellLine(label = "NEXT", text = MonsterSpellLevels.describe(spell, level + 1), tint = Color(0xFF8BE05A))
            }
            GlyphText(
                text = "Cast automatically in battle instead of a normal hit.",
                color = BoardMuted,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
            com.lernhero.home.ui.ChunkyButton(
                text = when {
                    maxed -> "MAX LEVEL"
                    else -> "UPGRADE  ✨ $cost"
                },
                color = Color(0xFF4FAE3A),
                enabled = canUpgrade,
                textSize = 13,
                modifier = Modifier.fillMaxWidth(),
                onClick = onUpgrade
            )
            if (!maxed && !canUpgrade) {
                GlyphText(
                    text = "Need $cost essence (you have $essence) - dissolve duplicates to get more.",
                    color = Color(0xFFFF8A7A),
                    fontFamily = FontFirst(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SpellLine(label: String, text: String, tint: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        GlyphText(
            text = label,
            color = SocketFill,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(tint)
                .padding(horizontal = 5.dp, vertical = 1.dp)
        )
        GlyphText(text = text, color = BoardCream, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

/** Gruby przycisk awansu: napis LEVEL UP i koszt w srodku, zamiast golej strzalki obok liczby. */
@Composable
private fun LevelUpButton(
    capped: Boolean,
    cost: Int,
    affordable: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(11.dp)
    val face = if (enabled) Color(0xFF4FAE3A) else Color(0xFF3A2C20)
    Row(
        modifier = modifier
            .height(38.dp)
            .clip(shape)
            .background(if (enabled) Color(0xFF245418) else SocketFill)
            .padding(bottom = if (enabled) 3.dp else 0.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(face.copy(red = (face.red + 0.15f).coerceAtMost(1f), green = (face.green + 0.15f).coerceAtMost(1f)), face)))
            .border(1.5.dp, if (enabled) Color(0xFF1E4A14) else BoardGold.copy(alpha = 0.3f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (capped) {
            GlyphText(
                text = "LEVEL CAP · ascend a ★",
                color = BoardMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                maxLines = 1
            )
        } else {
            com.lernhero.shared.theme.StrokedText(
                text = "LEVEL UP",
                size = 13.sp,
                color = if (enabled) Color.White else BoardMuted,
                outline = if (enabled) Color(0xFF123A0C) else Color(0xFF0E0804),
                strokeWidth = 5f,
                maxLines = 1
            )
            Box(modifier = Modifier.weight(1f))
            GlyphText(text = "💰", fontSize = 12.sp)
            GlyphText(
                text = " $cost",
                color = if (affordable) Color(0xFFFFE08A) else Color(0xFFFF7A6B),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TinyButton(
    text: String,
    enabled: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (enabled) color.copy(red = color.red * 0.55f, green = color.green * 0.55f, blue = color.blue * 0.55f) else SocketFill)
            .padding(bottom = if (enabled) 2.dp else 0.dp)
            .clip(shape)
            .background(if (enabled) color else Color(0xFF2A1D12))
            .border(1.dp, if (enabled) Color.White.copy(alpha = 0.5f) else BoardGold.copy(alpha = 0.3f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        GlyphText(
            text = text,
            color = if (enabled) Color(0xFF1E1206) else BoardMuted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

/* ------------------------------------------ Ekwipunek -------------------------------------------- */

/** Rzad gniazd na sprzet i dwa grube przyciski pod spodem. */
@Composable
private fun GearPane(
    monster: OwnedMonster,
    equipment: Map<String, String>,
    bag: List<ItemInstance>,
    onSlotClick: (ItemSlot) -> Unit,
    onUnequipAll: () -> Unit,
    onAutoEquip: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MonsterGear.SLOTS.forEach { slot ->
                GearSlot(
                    slot = slot,
                    equipment = equipment,
                    bag = bag,
                    element = monster.card.element,
                    modifier = Modifier.weight(1f),
                    onClick = { onSlotClick(slot) }
                )
            }
        }
        GlyphText(
            text = "Tap an empty slot to equip · tap gear to take it off",
            color = BoardMuted,
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            com.lernhero.home.ui.ChunkyButton(
                text = "UNEQUIP ALL",
                color = Color(0xFFB8442E),
                enabled = equipment.isNotEmpty(),
                textSize = 12,
                modifier = Modifier.weight(1f),
                onClick = onUnequipAll
            )
            com.lernhero.home.ui.ChunkyButton(
                text = "AUTO-EQUIP",
                color = Color(0xFF4FAE3A),
                textSize = 12,
                modifier = Modifier.weight(1f),
                onClick = onAutoEquip
            )
        }
    }
}

@Composable
private fun GearSlot(
    slot: ItemSlot,
    equipment: Map<String, String>,
    bag: List<ItemInstance>,
    element: Element,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val item = MonsterEquipment.itemIn(equipment, slot, bag)
    // Niezgodny zywiol podswietlamy juz na slocie - inaczej kara wyjdzie dopiero w liczbach.
    val mismatch = item != null && MonsterGear.elementFor(item.id).let { it != null && it != element }
    val shape = RoundedCornerShape(10.dp)
    val frame = when {
        mismatch -> Color(0xFFD8402A)
        item != null -> RarityPalette.frame(item.rarity)
        else -> BoardGold.copy(alpha = 0.35f)
    }

    Box(
        modifier = modifier
            .height(GEAR_SLOT_HEIGHT)
            .clip(shape)
            .background(
                if (item != null) Brush.radialGradient(listOf(frame.copy(alpha = 0.35f), SocketFill))
                else Brush.verticalGradient(listOf(SocketFill, Color(0xFF22170E)))
            )
            .then(
                if (item != null) Modifier.border(2.dp, frame, shape)
                else Modifier.drawBehind {
                    drawRoundRect(
                        color = frame,
                        cornerRadius = CornerRadius(10.dp.toPx()),
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
                        )
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (item != null) {
            ItemArtImage(item = item, size = 38.dp)
            if (mismatch) {
                GlyphText(
                    text = "!",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD8402A))
                        .padding(horizontal = 4.dp)
                )
            }
        } else {
            // Pusty slot mowi, CO w niego wchodzi - blady zarys i zloty plusik w rogu.
            GlyphText(text = slot.slotGlyph(), fontSize = 20.sp, modifier = Modifier.alpha(0.28f))
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFE08A), Color(0xFFD99A1C))))
                    .border(1.dp, Color(0xFF6A4208), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                GlyphText(text = "+", color = Color(0xFF2A1A08), fontWeight = FontWeight.Black, fontSize = 10.sp, lineHeight = 10.sp)
            }
        }
    }
}

/** Wysokosc kafla slotu. */
private val GEAR_SLOT_HEIGHT = 58.dp

/** Podpowiedz, co wchodzi w slot - szary zarys zamiast pustego kwadratu. */
private fun ItemSlot.slotGlyph(): String = when (this) {
    ItemSlot.MainHand -> "🗡"
    ItemSlot.Chest -> "🧥"
    ItemSlot.Head -> "🪖"
    ItemSlot.Feet -> "🥾"
    ItemSlot.Amulet -> "📿"
    ItemSlot.Ring -> "💍"
    else -> "➕"
}

/* --------------------------------------- Pasek kolekcji ------------------------------------------ */

/**
 * Staly, niski pasek miniatur na dole. Jezdzi w POZIOMIE (LazyRow), wiec ekran nad nim stoi
 * w miejscu niezaleznie od tego, ilu kompanow ma gracz. Strzalki przewijaja o pol ekranu -
 * przy kilkunastu kartach szybciej niz palcem.
 */
@Composable
private fun CollectionStrip(
    collection: List<OwnedMonster>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(STRIP_HEIGHT)
            .clip(RoundedCornerShape(10.dp))
            .background(ParchmentPalette.Paper)
            .border(3.dp, ParchmentPalette.Frame, RoundedCornerShape(10.dp))
            .padding(5.dp)
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(end = 26.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(collection, key = { it.card.id }) { entry ->
                MiniAvatar(
                    monster = entry,
                    selected = entry.card.id == selectedId,
                    onClick = { onSelect(entry.card.id) }
                )
            }
        }

        // Strzalki w rogu - pokazuja sie dopiero, gdy jest co przewijac.
        if (collection.size > VISIBLE_HINT) {
            Column(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                StripArrow("‹") {
                    scope.launch {
                        listState.animateScrollToItem((listState.firstVisibleItemIndex - SCROLL_STEP).coerceAtLeast(0))
                    }
                }
                StripArrow("›") {
                    scope.launch {
                        listState.animateScrollToItem(
                            (listState.firstVisibleItemIndex + SCROLL_STEP).coerceAtMost(collection.lastIndex)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StripArrow(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 22.dp, height = 21.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(ParchmentPalette.Button)
            .border(1.dp, ParchmentPalette.ButtonEdge, RoundedCornerShape(5.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        GlyphText(text = text, color = ParchmentPalette.Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Miniatura kompana: sylwetka, poziom i gwiazdki. Docelowo w srodku stanie PNG z twarza -
 * dlatego sylwetka jest przycieta do kwadratu, a nie wpisana w calosci.
 */
@Composable
private fun MiniAvatar(
    monster: OwnedMonster,
    selected: Boolean,
    onClick: () -> Unit
) {
    val card = monster.card
    val accent = card.element.badgeColor()

    Column(
        modifier = Modifier
            .width(52.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(RarityPalette.cardBackground(card.rarity))
            .border(
                width = if (selected) 3.dp else 2.dp,
                color = if (selected) ParchmentPalette.Button else accent.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Image(
                painter = painterResource(MonsterVisualPresets.forSpecies(card.spriteKey).drawable),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(2.dp)
            )
            GlyphText(
                text = "${monster.level}",
                color = Color(0xFFF4E9D7),
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xCC8E3A2B))
                    .padding(horizontal = 3.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(2.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0x99000000)),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(monster.stars.coerceAtMost(5)) {
                GlyphText(text = "★", color = Color(0xFFFFC93C), fontSize = 6.sp)
            }
        }
    }
}

/* ---------------------------------------- Wybor sprzetu ------------------------------------------ */

@Composable
private fun GearPicker(
    slot: ItemSlot,
    /** Wolne SZTUKI sparowane z ich przedmiotem (juz z poziomem ulepszenia tej sztuki). */
    items: List<Pair<ItemInstance, Item>>,
    element: Element,
    onPick: (ItemInstance) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC07050B))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ParchmentPalette.Paper)
                .border(3.dp, ParchmentPalette.Frame, RoundedCornerShape(12.dp))
                .clickable(enabled = false) {}
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GlyphText(
                text = slot.shortLabel(),
                color = ParchmentPalette.Ink,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            if (items.isEmpty()) {
                GlyphText(
                    text = "No gear for this slot.\nIt drops from chests and bosses.",
                    color = ParchmentPalette.InkMuted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                items.forEach { (instance, item) ->
                    val matches = MonsterGear.elementFor(item.id).let { it == null || it == element }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ParchmentPalette.Inset)
                            .border(2.dp, RarityPalette.frame(item.rarity), RoundedCornerShape(8.dp))
                            .clickable { onPick(instance) }
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ItemArtImage(item = item, size = 28.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            GlyphText(
                                // Poziom przy nazwie: dwie kopie tego samego pazura roznia sie
                                // tylko nim, wiec bez niego picker pokazywalby dwa identyczne wiersze.
                                text = if (instance.level > 0) "${item.name} +${instance.level}" else item.name,
                                color = ParchmentPalette.Ink,
                                fontFamily = FontFirst(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            // Liczby, ktore ten pazur naprawde daje - bez nich picker byl lista
                            // nazw i gracz wybieral po ikonie.
                            GlyphText(
                                text = item.stats.asMap()
                                    .filterValues { it != 0f }
                                    .entries
                                    .joinToString("  ") { (stat, value) ->
                                        "${stat.shortLabel()} ${stat.format(value)}"
                                    },
                                color = ParchmentPalette.Ink,
                                fontFamily = FontPixel(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            GlyphText(
                                text = if (matches) "Matches element" else "Mismatched element",
                                color = if (matches) Color(0xFF2F7D32) else Color(0xFF9E2B20),
                                fontFamily = FontFirst(),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            com.lernhero.home.ui.ChunkyButton(
                text = "CLOSE",
                color = ParchmentPalette.Frame,
                textSize = 13,
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismiss
            )
        }
    }
}

/* ------------------------------------------- Pusto ---------------------------------------------- */

@Composable
private fun EmptyCollection(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(ParchmentPalette.Paper)
            .border(3.dp, ParchmentPalette.Frame, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            GlyphText(text = "🗿", fontSize = 34.sp)
            GlyphText(
                text = "You don't have any companions yet.",
                color = ParchmentPalette.Ink,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            GlyphText(
                text = "Start a ritual in the Summon tab.",
                color = ParchmentPalette.InkMuted,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }
    }
}

/** Wysokosc paska miniatur - staly kwadrat na dole, ktory nigdy nie rosnie z liczba kompanow. */
private val STRIP_HEIGHT = 74.dp

/** Od ilu kart pokazujemy strzalki przewijania - ponizej i tak wszystko widac. */
private const val VISIBLE_HINT = 5

/** O ile kart skacze jedno tapniecie strzalki. */
private const val SCROLL_STEP = 4
