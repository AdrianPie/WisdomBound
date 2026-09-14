package com.lernhero.home.city

import androidx.compose.foundation.Image
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
            onUpgradeSpell = { onUpgradeSpell(selected, it) },
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
            Text(
                text = card.name,
                color = Color(0xFFF4E9D7),
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            ElementDot(card.element)
        }

        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            repeat(5) { i ->
                Text(
                    text = "★",
                    color = if (i < monster.stars) Color(0xFFFFC93C) else Color(0x556B5A38),
                    fontSize = 10.sp
                )
            }
        }

        Arrow(text = "‹", modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp), onClick = onPrev)
        Arrow(text = "›", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp), onClick = onNext)

        Text(
            text = "x${monster.count}",
            color = Color(0xFFEADFC8),
            fontFamily = FontPixel(),
            fontSize = 10.sp,
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
            .size(30.dp)
            .clip(CircleShape)
            .background(Color(0xAA3D2A18))
            .border(2.dp, ParchmentPalette.Button.copy(alpha = 0.8f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = ParchmentPalette.Button, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ElementDot(element: Element) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(element.badgeColor())
            .border(1.dp, Color(0x66000000), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = element.icon, fontSize = 8.sp)
    }
}

/* ------------------------------------------- Panel dolny ---------------------------------------- */

/**
 * Wysokosc TRESCI panelu - identyczna dla obu zakladek.
 *
 * Stala, a nie wrapContent, z dwoch powodow: karta ma zajmowac okolo jednej piatej ekranu (reszta
 * nalezy do sylwetki), a przelaczanie Info/Ekwipunek nie moze podskakiwac calym ekranem. Kazda
 * zakladka musi sie w tym zmiescic - stad wszystko upchniete w trzy waskie wiersze zamiast
 * listy sekcji z naglowkami.
 */
private val PANE_HEIGHT = 96.dp

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
            .clip(RoundedCornerShape(11.dp))
            .background(ParchmentPalette.Paper)
            .border(3.dp, ParchmentPalette.Frame, RoundedCornerShape(11.dp))
            .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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

@Composable
private fun PaneTabs(pane: Pane, onPane: (Pane) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(ParchmentPalette.TitleBar)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Pane.entries.forEach { entry ->
            val active = entry == pane
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (active) ParchmentPalette.TabActive else Color.Transparent)
                    .clickable { onPane(entry) }
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.label,
                    color = if (active) ParchmentPalette.Ink else ParchmentPalette.Paper.copy(alpha = 0.8f),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/* -------------------------------------------- Info ---------------------------------------------- */

/**
 * Trzy wiersze zamiast szesciu sekcji:
 *
 * ```
 * 💪 423    ⬢⬢⬡⬡  ★2/5        LV 30/30
 * ❤️ 298   ⚔️ 92   🛡️ 0%   👟 3
 * [💰 1 234        ] [⬆]   (·)(·)
 * ```
 *
 * Etykiety "Tier"/"Awans poziomu" wylecialy swiadomie - klejnoty i moneta ze strzalka mowia
 * to samo, a kazdy naglowek kosztowal tu tyle wysokosci, co caly wiersz statystyk.
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
    // Statystyki pokazujemy JUZ z ekwipunkiem - karta ma mowic, czym kompan naprawde walczy,
    // a nie ile mial na sucho przed zalozeniem czegokolwiek.
    // Sprzet i baza sa w TEJ SAMEJ skali (walki) - patrz [CompanionPower]. Wczesniej nie byly:
    // statystyki szly ze skali karty (70 HP), a sprzet ze skali walki (+120 HP z pancerza), wiec
    // karta obiecywala trzy razy wiecej, niz sprzet naprawde dawal.
    val gear = MonsterEquipment.statsOf(equipment, monster.card.element, bag)
    val hp = CompanionPower.hpAt(monster.card, monster.stars, monster.level, gear)
    val attack = CompanionPower.attackAt(monster.card, monster.stars, monster.level, gear)
    val armor = (gear[StatType.DamageReduction] * 100f).toInt()
    val speed = gear[StatType.Speed].toInt()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "💪", fontSize = 12.sp)
            Text(
                text = "${attack * 3 + hp / 2}",
                color = ParchmentPalette.Ink,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.width(2.dp))
            // Cztery klejnoty = cztery awanse gwiazdki ponad startowa.
            repeat(4) { i ->
                TierGem(filled = i < monster.stars - 1, accent = monster.card.element.badgeColor())
            }
            TinyButton(
                text = if (monster.maxStars) "MAX" else "★${monster.duplicates}/${monster.ascendCost}",
                enabled = monster.canAscend,
                color = ParchmentPalette.Button,
                onClick = onAscend
            )
            if (monster.canDissolve) {
                TinyButton(text = "✨", enabled = true, color = Color(0xFFB08CD6), onClick = onDissolve)
            }

            Box(modifier = Modifier.weight(1f))
            Text(
                text = "LV ${monster.level}/${monster.levelCap}",
                color = Color(0xFF9E3B24),
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        // Cztery statystyki w JEDNYM pasku - pionowa lista byla czytelniejsza, ale zjadala
        // ponad sto punktow wysokosci, czyli wiecej niz caly reszta panelu.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(7.dp))
                .background(ParchmentPalette.Card)
                .border(1.dp, ParchmentPalette.CardEdge, RoundedCornerShape(7.dp))
                .padding(vertical = 4.dp)
        ) {
            StatCell("❤️", "$hp", "HP", Modifier.weight(1f))
            StatCell("⚔️", "$attack", "atak", Modifier.weight(1f))
            StatCell("🛡️", "$armor%", "pancerz", Modifier.weight(1f))
            StatCell("👟", "$speed", "szybkosc", Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(7.dp))
                    .background(ParchmentPalette.Card)
                    .border(1.dp, ParchmentPalette.CardEdge, RoundedCornerShape(7.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (monster.atLevelCap) {
                    Text(
                        text = "Capped - ascend a star",
                        color = ParchmentPalette.InkMuted,
                        fontFamily = FontFirst(),
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                } else {
                    Text(text = "💰", fontSize = 11.sp)
                    Text(
                        text = "${monster.levelUpCost}",
                        color = ParchmentPalette.Ink,
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            val canUp = !monster.atLevelCap && money >= monster.levelUpCost
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 26.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (canUp) Color(0xFF57C24A) else ParchmentPalette.ButtonDisabled)
                    .border(2.dp, if (canUp) Color(0xFF3E8F34) else ParchmentPalette.CardEdge, RoundedCornerShape(7.dp))
                    .clickable(enabled = canUp, onClick = onUpgrade),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⬆", fontSize = 13.sp)
            }

            monster.card.spells.take(3).forEach { spell ->
                val level = monster.spellLevel(spell)
                val cost = MonsterSpellLevels.upgradeCost(level)
                SpellDisc(
                    spell = spell,
                    level = level,
                    canUpgrade = !MonsterSpellLevels.isMaxed(level) && essence >= cost,
                    onClick = { onUpgradeSpell(spell) }
                )
            }
        }
    }
}

/** Jedna statystyka w pasku: ikona i wartosc w linii, malutka etykieta pod spodem. */
@Composable
private fun StatCell(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 10.sp)
            Text(
                text = value,
                color = ParchmentPalette.Ink,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
        Text(
            text = label,
            color = ParchmentPalette.InkMuted,
            fontFamily = FontFirst(),
            fontSize = 7.sp,
            maxLines = 1
        )
    }
}

/** Szescioboczny klejnot tieru - wypelniony kolorem zywiolu albo wglebiony, gdy jeszcze pusty. */
@Composable
private fun TierGem(filled: Boolean, accent: Color) {
    Box(
        modifier = Modifier
            .size(15.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (filled) accent else Color(0xFF9A7B54))
            .border(1.dp, if (filled) ParchmentPalette.Frame else ParchmentPalette.CardEdge, RoundedCornerShape(4.dp))
    )
}

/** Krazek zaklecia z poziomem w rogu - swieci na zielono, gdy stac na ulepszenie. */
@Composable
private fun SpellDisc(
    spell: CompanionSpell,
    level: Int,
    canUpgrade: Boolean,
    onClick: () -> Unit
) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(ParchmentPalette.SlotFill)
                .border(
                    width = 2.dp,
                    color = if (canUpgrade) Color(0xFF57C24A) else ParchmentPalette.CardEdge,
                    shape = CircleShape
                )
                .clickable(enabled = canUpgrade, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = spell.icon, fontSize = 12.sp)
        }
        Text(
            text = "$level",
            color = ParchmentPalette.Paper,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 7.sp,
            modifier = Modifier
                .clip(CircleShape)
                .background(ParchmentPalette.Frame)
                .padding(horizontal = 2.dp)
        )
    }
}

@Composable
private fun TinyButton(
    text: String,
    enabled: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(if (enabled) color else ParchmentPalette.ButtonDisabled)
            .border(1.dp, if (enabled) ParchmentPalette.ButtonEdge else ParchmentPalette.CardEdge, RoundedCornerShape(5.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 5.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) ParchmentPalette.Ink else ParchmentPalette.InkMuted,
            // FontFirst, nie FontPixel: font pikselowy rysuje 'Z' i 'J' odbite lustrzanie.
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

/* ------------------------------------------ Ekwipunek -------------------------------------------- */

/**
 * Szesc kwadratowych slotow w JEDNYM rzedzie plus dwa przyciski.
 *
 * Heksagram z pierwowzoru wypadl swiadomie: szesc slotow rozstawionych wokol gwiazdy potrzebuje
 * ze dwustu punktow wysokosci, a caly panel ma miescic sie w [PANE_HEIGHT]. Uklad w linii daje te
 * same szesc slotow i ta sama informacje za jedna trzecia miejsca.
 */
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
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            WideButton(
                text = "Unequip",
                enabled = equipment.isNotEmpty(),
                color = Color(0xFFE0713A),
                modifier = Modifier.weight(1f),
                onClick = onUnequipAll
            )
            WideButton(
                text = "Auto-equip",
                enabled = true,
                color = Color(0xFF57C24A),
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
    // Niezgodny zywiol podswietlamy juz na slocie - inaczej gracz zobaczy kare dopiero
    // w liczbach i nie skojarzy jej z konkretnym przedmiotem.
    val mismatch = item != null && MonsterGear.elementFor(item.id).let { it != null && it != element }

    // Pusty slot ma sie roznic TLEM, nie drobnym znaczkiem w rogu. Wczesniej pusty i zajety
    // wygladaly identycznie (ta sama ciemna plomba, ta sama ramka, przygaszona ikonka slotu w
    // srodku) i jedyna roznica byla zielona plakietka 13 dp - na pergaminie nie do wylapania.
    // Teraz: zajety = ciemne gniazdo z ramka rzadkosci, pusty = jasne wglebienie z PRZERYWANA
    // ramka. Roznice widac z drugiego konca ekranu, bez czytania.
    val shape = RoundedCornerShape(8.dp)
    val frame = when {
        mismatch -> Color(0xFF9E2B20)
        item != null -> RarityPalette.frame(item.rarity)
        else -> ParchmentPalette.FrameLight
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(GEAR_SLOT_HEIGHT)
            .clip(shape)
            .background(if (item != null) ParchmentPalette.SlotFill else ParchmentPalette.Inset)
            .then(
                if (item != null) {
                    Modifier.border(2.dp, frame, shape)
                } else {
                    // Przerywana ramka to uniwersalny znak "tu czegos brakuje" - Compose nie ma
                    // jej w border(), wiec rysujemy ja recznie.
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = frame,
                            cornerRadius = CornerRadius(8.dp.toPx()),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(5.dp.toPx(), 4.dp.toPx())
                                )
                            )
                        )
                    }
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (item != null) {
            Text(text = item.icon(), fontSize = 22.sp)
        } else {
            // Pusty slot dalej mowi, CO w niego wchodzi - ale juz jako blady zarys na jasnym tle,
            // z plusem obok, zeby nie dalo sie go pomylic z zalozonym przedmiotem.
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = slot.slotGlyph(), fontSize = 15.sp, modifier = Modifier.alpha(0.30f))
                Text(
                    text = "+",
                    color = ParchmentPalette.InkMuted,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/** Wysokosc kafla slotu - o oczko wyzszy niz wczesniej, bo ikona 20 sp gubila sie w 44 dp. */
private val GEAR_SLOT_HEIGHT = 50.dp

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

@Composable
private fun WideButton(
    text: String,
    enabled: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) color else ParchmentPalette.ButtonDisabled)
            .border(2.dp, ParchmentPalette.Frame.copy(alpha = if (enabled) 0.9f else 0.35f), RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else ParchmentPalette.InkMuted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
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
        Text(text = text, color = ParchmentPalette.Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            Text(
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
                Text(text = "★", color = Color(0xFFFFC93C), fontSize = 6.sp)
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
            Text(
                text = slot.shortLabel(),
                color = ParchmentPalette.Ink,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            if (items.isEmpty()) {
                Text(
                    text = "No gear for this slot.\nForge some in the Forge, Summons tab.",
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
                        Text(text = item.icon(), fontSize = 18.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
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
                            Text(
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
                            Text(
                                text = if (matches) "Matches element" else "Mismatched element",
                                color = if (matches) Color(0xFF2F7D32) else Color(0xFF9E2B20),
                                fontFamily = FontFirst(),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            WideButton(
                text = "Close",
                enabled = true,
                color = ParchmentPalette.Frame,
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
            Text(text = "🗿", fontSize = 34.sp)
            Text(
                text = "You don't have any companions yet.",
                color = ParchmentPalette.Ink,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
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
