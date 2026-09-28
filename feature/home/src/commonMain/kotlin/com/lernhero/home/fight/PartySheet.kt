package com.lernhero.home.fight

import com.lernhero.home.tutorial.TutorialTarget
import com.lernhero.home.tutorial.tutorialTarget
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import com.lernhero.domain.monster.Element
import com.lernhero.game.character.ElementEmblem
import com.lernhero.home.ui.darken
import com.lernhero.shared.theme.StrokedText
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
        onClose = onClose,
        closeTarget = TutorialTarget.PartyClose
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
            StatCell(icon = "⚔️", value = "$teamAttack", tint = Color(0xFFF97316))
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

        // Filtr zywiolow jak w Idle Heroes - przy kilkudziesieciu kompanach szuka sie po kolorze.
        var elementFilter by remember { mutableStateOf<Element?>(null) }
        val elements = Element.entries
        run {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(selected = elementFilter == null, onClick = { elementFilter = null }) {
                    Text("ALL", color = SheetCream, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
                elements.forEach { e ->
                    FilterChip(selected = elementFilter == e, onClick = { elementFilter = if (elementFilter == e) null else e }) {
                        ElementEmblem(element = e, size = 46.dp)
                    }
                }
            }
        }

        // Najsilniejsi na poczatku - to ich gracz zwykle szuka, kompletujac sklad.
        val shown = remember(reserve, elementFilter) {
            reserve
                .filter { elementFilter == null || it.element == elementFilter }
                .sortedByDescending { CompanionPower.hpAt(it, starsOf(it.id), levelOf(it.id)) + CompanionPower.attackAt(it, starsOf(it.id), levelOf(it.id)) * 4 }
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val gap = 6.dp
            // -0.5dp: zaokraglenia pikseli potrafily zepchnac piaty kafel do nastepnego rzedu.
            val tile = (maxWidth - gap * (RESERVE_COLUMNS - 1)) / RESERVE_COLUMNS - 0.5.dp
            // STALA wysokosc siatki (RESERVE_ROWS rzedow) z przewijaniem w srodku - okno nie moze
            // rosnac ani kurczyc sie w zaleznosci od tego, ilu kompanow jest w rezerwie/filtrze.
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tile * RESERVE_ROWS + gap * (RESERVE_ROWS - 1))
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                shown.forEachIndexed { index, card ->
                    CompanionAvatar(
                        card = card,
                        level = levelOf(card.id),
                        stars = starsOf(card.id),
                        modifier = Modifier
                            .width(tile)
                            .then(if (index == 0) Modifier.tutorialTarget(TutorialTarget.ReserveMonster) else Modifier),
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
                SummonTile(ready = freeSummonReady, onClick = onSummon, modifier = Modifier.width(tile))
            }
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

    // Zajete miejsce to SAM portret - bez drugiej, przycinajacej ramki wokol niego. Zaznaczenie
    // to zlota obwodka NA portrecie.
    if (card != null) {
        Box(modifier = modifier.aspectRatio(1f)) {
            CompanionAvatar(
                card = card,
                level = level,
                stars = null,
                modifier = Modifier.fillMaxSize(),
                onClick = onClick
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(edgeWidth, edge, RoundedCornerShape(10.dp))
                )
            }
        }
        return
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
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

            else -> CompanionAvatar(
                card = card,
                level = level,
                stars = null,
                modifier = Modifier.fillMaxSize(),
                onClick = onClick
            )
        }
    }
}

/**
 * Portret kompana jak w Idle Heroes: kwadrat w zlocistej ramie, tlo w (przyciemnionym) kolorze
 * rzadkosci, zywiol w lewym gornym rogu, poziom w prawym, gwiazdki na dole na portrecie. Bez
 * nazwy i liczb - to lista do SZYBKIEGO wyboru, pelna karta jest w Centrum Przyzywania.
 */
@Composable
private fun CompanionAvatar(
    card: MonsterCard,
    level: Int,
    /** null = bez gwiazdek (miejsca druzyny, gdzie kafel jest za maly). */
    stars: Int?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val fill = RarityPalette.frame(card.rarity)
    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val s = maxWidth
        val corner = s * 0.14f
        val outer = RoundedCornerShape(corner)
        val inner = RoundedCornerShape(corner * 0.6f)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(outer)
                .background(Brush.verticalGradient(listOf(Color(0xFFFAEACB), Color(0xFFE3C390), Color(0xFFC39A60))))
                .border(1.dp, AvatarEdge, outer)
                .padding(s * 0.045f)
                .clip(inner)
                .background(Brush.verticalGradient(listOf(fill.darken(0.25f), fill.darken(0.6f))))
                .border(1.dp, fill.darken(0.65f), inner)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(MonsterVisualPresets.forSpecies(card.spriteKey).drawable),
                contentDescription = card.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                                                // Minimalne przyblizenie - potwor ma sie miescic caly, a nie byc uciety.
                        scaleX = 1.04f
                        scaleY = 1.04f
                        translationY = size.height * 0.02f
                    }
            )
            if (stars != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(s * 0.28f)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)))),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Row(
                        modifier = Modifier.padding(bottom = s * 0.02f),
                        horizontalArrangement = Arrangement.spacedBy((-1).dp)
                    ) {
                        repeat(stars.coerceIn(0, MonsterStars.MAX_STARS)) {
                            StrokedText(
                                text = "★",
                                size = (s.value * 0.15f).sp,
                                color = Color(0xFFFFD34D),
                                outline = AvatarEdge,
                                strokeWidth = 2.5f,
                                tight = true
                            )
                        }
                    }
                }
            }
        }
        ElementEmblem(
            element = card.element,
            size = s * 0.27f,
            modifier = Modifier.align(Alignment.TopStart).offset(x = -(s * 0.03f), y = -(s * 0.03f))
        )
        StrokedText(
            text = "$level",
            size = (s.value * 0.17f).sp,
            color = Color.White,
            outline = AvatarEdge,
            strokeWidth = 4f,
            tight = true,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = s * 0.07f, end = s * 0.08f)
        )
    }
}

/** Filtr zywiolu nad rezerwa: kolko z emblematem, zlota obwodka na wybranym. */
@Composable
private fun FilterChip(selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            // Tlo z ksztaltem, ale BEZ clip - emblemat ma wypelniac kolko i lekko z niego wystawac.
            .background(if (selected) SheetInk else SheetInk.copy(alpha = 0.55f), CircleShape)
            .border(if (selected) 2.dp else 1.dp, if (selected) SheetGold else SheetFrameDim, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private val AvatarEdge = Color(0xFF5E3F1F)

private const val RESERVE_COLUMNS = 5

/** Ile rzedow rezerwy widac naraz - reszta sie przewija. */
private const val RESERVE_ROWS = 4

/**
 * Wejscie do przyzywania - ostatni kafel rezerwy, a nie osobny przycisk gdzies pod lista.
 *
 * Stoi tam, gdzie gracz wlasnie zobaczyl, ze nie ma kogo wstawic: pusta rezerwa i kafel "przyzwij"
 * obok siebie to jedno zdanie. Kropka zapala sie, gdy czeka darmowy los - to jedyny powod, zeby
 * isc tam natychmiast.
 */
@Composable
private fun SummonTile(ready: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.TopEnd) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(SheetInk)
                .border(1.5.dp, SheetFrame.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                .clickable(onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "✨", fontSize = 20.sp, lineHeight = 22.sp)
            Text(
                text = if (ready) "Free" else "Summon",
                color = if (ready) SheetGreen else SheetGold,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                lineHeight = 10.sp
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
