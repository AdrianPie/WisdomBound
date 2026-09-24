package com.lernhero.home.fight

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.monster.CompanionPower
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.domain.monster.MonsterStars
import com.lernhero.domain.player.PartySlots
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.home.ui.RarityPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.jetbrains.compose.resources.painterResource

/**
 * Okno druzyny: kto idzie z bohaterem i kto czeka w rezerwie.
 *
 * ## Dlaczego nie tak, jak bylo
 *
 * Skladu pilnowaly wczesniej DWA miejsca: siatka druzyny w panelu zakladki i osobne okno wyboru
 * kompana, ktore otwieralo sie NAD nia. Zeby wymienic jednego kompana, trzeba bylo otworzyc panel,
 * tapnac kafel (to go zdejmowalo), tapnac "+", znalezc nowego w drugim oknie i je zamknac - piec
 * krokow i dwa okna na jedna podmiane, przy czym po drodze druzyna znikala z oczu.
 *
 * Tutaj jedno i drugie stoi w JEDNYM oknie: u gory miejsca, pod nimi rezerwa. Tapniecie miejsca
 * mowi "tutaj", tapniecie kompana mowi "ten" - i to juz cala obsluga. Po wstawieniu zaznaczenie
 * samo przechodzi na kolejne wolne miejsce, wiec skompletowanie druzyny od zera to piec tapniec
 * pod rzad, bez ani jednego powrotu do gory.
 *
 * Kompani stojacy juz w druzynie NIE pokazuja sie w rezerwie ([com.lernhero.home.player.PlayerUiState.companionInventory]) -
 * jeden kompan moze stac w skladzie tylko raz, a dublowanie go w obu listach kazaloby graczowi
 * zgadywac, ktora jest prawdziwa.
 */
@Composable
fun PartySheet(
    /** Miejsca druzyny - null to wolne. Lista jest zawsze pelnej dlugosci ([PartySlots.TOTAL]). */
    party: List<MonsterCard?>,
    /** Kompani poza druzyna - to, co gracz moze wstawic. */
    reserve: List<MonsterCard>,
    heroLevel: Int,
    levelOf: (String) -> Int,
    starsOf: (String) -> Int,
    /** Miejsce zaznaczone przy otwarciu - to, ktore gracz tapnal na scenie. */
    initialSlot: Int,
    /** Czy w Centrum Przyzywania czeka darmowy los - kropka na kaflu przyzwania. */
    freeSummonReady: Boolean,
    onPlace: (monsterId: String, slot: Int) -> Unit,
    onRemove: (slot: Int) -> Unit,
    onSummon: () -> Unit,
    onClose: () -> Unit
) {
    // Zaznaczenie startuje na miejscu, ktore gracz tapnal - a gdy tapnal zamkniete (albo wszedl
    // przyciskiem z boku), na pierwszym wolnym otwartym. Otwarte okno ma byc gotowe do roboty,
    // a nie czekac na jeszcze jedno tapniecie.
    var target by remember {
        mutableStateOf(
            initialSlot.takeIf { PartySlots.isUnlocked(it, heroLevel) }
                ?: firstFreeSlot(party, heroLevel)
                ?: 0
        )
    }

    val filled = party.filterNotNull()
    val teamHp = filled.sumOf { CompanionPower.hpAt(it, starsOf(it.id), levelOf(it.id)) }
    val teamAttack = filled.sumOf { CompanionPower.attackAt(it, starsOf(it.id), levelOf(it.id)) }
    val openSlots = PartySlots.unlockedCount(heroLevel)

    FightSheet(
        title = "PARTY",
        subtitle = "Tap a slot, then pick who stands there.",
        onClose = onClose
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            party.forEachIndexed { index, card ->
                PartySlotTile(
                    card = card,
                    level = card?.let { levelOf(it.id) } ?: 1,
                    unlocked = PartySlots.isUnlocked(index, heroLevel),
                    requiredLevel = PartySlots.requiredLevel(index),
                    selected = index == target,
                    onClick = { target = index },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        SheetStatRow {
            StatCell(icon = "❤", value = "$teamHp", tint = SheetRed)
            StatCell(icon = "⚔", value = "$teamAttack", tint = Color(0xFFF97316))
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$openSlots/${PartySlots.TOTAL} slots",
                color = SheetMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        // Wiersz kontekstowy: co sie stanie z tapnieciem w rezerwie i jak oproznic miejsce.
        // Zdejmowanie kompana siedzi TU, a nie pod tapnieciem kafla w gornym rzedzie - tamto
        // wybiera miejsce, a jedno tapniecie nie moze znaczyc dwoch roznych rzeczy.
        val standing = party.getOrNull(target)
        when {
            !PartySlots.isUnlocked(target, heroLevel) -> SheetHint(
                "Slot ${target + 1} opens at hero level ${PartySlots.requiredLevel(target)}."
            )

            standing == null -> SheetHint("Slot ${target + 1} is empty - pick a companion below.")

            else -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SheetHint(
                    text = "${standing.name} holds slot ${target + 1}.",
                    modifier = Modifier.weight(1f)
                )
                SmallAction(label = "SEND BACK", onClick = { onRemove(target) })
            }
        }

        SheetSectionLabel("Reserve")

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            reserve.forEach { card ->
                ReserveTile(
                    card = card,
                    level = levelOf(card.id),
                    stars = starsOf(card.id),
                    onClick = {
                        // Wstawiamy w zaznaczone miejsce, a potem przesuwamy zaznaczenie na
                        // kolejne wolne - kompletowanie skladu ma byc ciagiem tapniec, a nie
                        // seria powrotow do gornego rzedu.
                        if (PartySlots.isUnlocked(target, heroLevel)) {
                            onPlace(card.id, target)
                            val next = party.toMutableList().also { it[target] = card }
                            target = firstFreeSlot(next, heroLevel) ?: target
                        }
                    }
                )
            }
            SummonTile(ready = freeSummonReady, onClick = onSummon)
        }

        SheetHint(
            if (reserve.isEmpty()) {
                "Every companion you own is already standing with you. Summon more to rotate the party."
            } else {
                "Companions keep their level and stars - swapping costs nothing."
            }
        )
    }
}

/** Pierwsze wolne OTWARTE miejsce, albo null gdy sklad jest pelny. */
private fun firstFreeSlot(party: List<MonsterCard?>, heroLevel: Int): Int? =
    party.indices.firstOrNull { party[it] == null && PartySlots.isUnlocked(it, heroLevel) }

/**
 * Jedno miejsce w druzynie. Zamkniete POKAZUJE poziom, ktory je otworzy - klodka bez liczby
 * mowilaby "nie wolno", a ma mowic "jeszcze nie teraz".
 */
@Composable
private fun PartySlotTile(
    card: MonsterCard?,
    level: Int,
    unlocked: Boolean,
    requiredLevel: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edge by animateColorAsState(
        when {
            selected -> SheetGold
            card != null -> RarityPalette.frame(card.rarity)
            else -> SheetFrameDim
        },
        tween(180, easing = FastOutSlowInEasing),
        label = "slotEdge"
    )
    val edgeWidth by animateDpAsState(
        if (selected) 2.5.dp else 1.5.dp,
        tween(180, easing = FastOutSlowInEasing),
        label = "slotEdgeWidth"
    )

    Box(
        modifier = modifier
            .aspectRatio(0.86f)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (card != null) {
                    Modifier.background(RarityPalette.cardBackground(card.rarity))
                } else {
                    Modifier.background(if (unlocked) SheetInk else SheetInk.copy(alpha = 0.5f))
                }
            )
            .border(edgeWidth, edge, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            !unlocked -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(text = "🔒", fontSize = 13.sp)
                Text(
                    text = "Lv$requiredLevel",
                    color = SheetMuted.copy(alpha = 0.8f),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            }

            card == null -> Text(
                text = "+",
                color = if (selected) SheetGold else SheetFrameDim,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            else -> {
                Image(
                    painter = painterResource(MonsterVisualPresets.forSpecies(card.spriteKey).drawable),
                    contentDescription = card.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(4.dp)
                )
                ElementDot(
                    colorHex = card.element.colorHex,
                    modifier = Modifier.align(Alignment.TopStart).padding(3.dp)
                )
                Text(
                    text = "$level",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SheetInk.copy(alpha = 0.85f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * Kafel rezerwy. Portret jest maly (nie karta kolekcji), bo to lista do SZYBKIEGO wyboru - pelna
 * karta z opisem zaklec nalezy do Centrum Przyzywania, gdzie sie kompanow oglada i rozwija.
 */
@Composable
private fun ReserveTile(
    card: MonsterCard,
    level: Int,
    stars: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(RESERVE_TILE)
            .clip(RoundedCornerShape(12.dp))
            .background(RarityPalette.cardBackground(card.rarity))
            .border(1.5.dp, RarityPalette.frame(card.rarity), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SheetInk),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(MonsterVisualPresets.forSpecies(card.spriteKey).drawable),
                contentDescription = card.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(0.78f).padding(bottom = 2.dp)
            )
            ElementDot(
                colorHex = card.element.colorHex,
                modifier = Modifier.align(Alignment.TopStart).padding(3.dp)
            )
            Text(
                text = "LV$level",
                color = SheetGold,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(SheetInk.copy(alpha = 0.85f))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            )
        }

        Text(
            text = card.name,
            color = SheetCream,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            (1..MonsterStars.MAX_STARS).forEach { index ->
                Text(
                    text = "★",
                    color = if (index <= stars) SheetGold else Color(0xFF3A2E22),
                    fontSize = 7.sp
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = "${CompanionPower.hpAt(card, stars, level)}",
                color = SheetRed,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
            Text(
                text = "${CompanionPower.attackAt(card, stars, level)}",
                color = Color(0xFFF97316),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }
    }
}

/**
 * Wejscie do przyzywania - ostatni kafel rezerwy, a nie osobny przycisk gdzies pod lista.
 *
 * Stoi tam, gdzie gracz wlasnie zobaczyl, ze nie ma kogo wstawic: pusta rezerwa i kafel "przyzwij"
 * obok siebie to jedno zdanie. Kropka zapala sie, gdy czeka darmowy los - to jedyny powod, zeby
 * isc tam natychmiast.
 */
@Composable
private fun SummonTile(ready: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.TopEnd) {
        Column(
            modifier = Modifier
                .width(RESERVE_TILE)
                .clip(RoundedCornerShape(12.dp))
                .background(SheetInk)
                .border(1.5.dp, SheetFrame.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "✨", fontSize = 24.sp)
            Text(
                text = "Summon",
                color = SheetGold,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Text(
                text = if (ready) "Free pull" else "Center",
                color = if (ready) SheetGreen else SheetMuted,
                fontFamily = FontFirst(),
                fontSize = 9.sp
            )
        }
        if (ready) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(SheetGreen)
            )
        }
    }
}

/** Zywiol jako kropka, nie napis - w tej skali i tak nikt by go nie przeczytal. */
@Composable
private fun ElementDot(colorHex: Long, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(9.dp)
            .clip(CircleShape)
            .background(Color(colorHex))
            .border(1.dp, Color(0x66000000), CircleShape)
    )
}

@Composable
private fun StatCell(icon: String, value: String, tint: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 11.sp)
        Text(
            text = value,
            color = tint,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

/** Maly przycisk tekstowy w oknie - do rzeczy odwracalnych (zdjecie kompana ze skladu). */
@Composable
private fun SmallAction(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = SheetGold,
        fontFamily = FontPixel(),
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SheetHeaderBg)
            .border(1.5.dp, SheetFrameDim, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

/** Bok kafla rezerwy - cztery mieszcza sie w rzedzie na typowym telefonie. */
private val RESERVE_TILE = 78.dp
