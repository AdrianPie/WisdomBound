package com.lernhero.home.equipment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.item.ItemInstance
import com.lernhero.domain.item.resolve
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.toLoadout
import com.lernhero.domain.player.PlayerEquipment
import com.lernhero.game.character.HeroLook
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.home.player.PlayerViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.lernhero.domain.player.HeroCombatStats
import com.lernhero.domain.player.HeroStat
import com.lernhero.domain.player.PlayerProgress
import com.lernhero.domain.talent.TalentRules
import com.lernhero.home.ui.ItemDetailSheet
import com.lernhero.home.ui.format
import com.lernhero.home.ui.RarityPalette
import com.lernhero.home.ui.icon
import com.lernhero.home.ui.label
import com.lernhero.home.ui.shortLabel
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/* ---------------------------------------------------------------------------------------------
 *  Ta sama paleta co reszta gry (AppPalette) - lokalne aliasy, zeby nazwy zostaly krotkie.
 * ------------------------------------------------------------------------------------------- */
private val Panel = AppPalette.Panel
private val PanelRaised = AppPalette.PanelRaised
private val SlotBg = AppPalette.Ink
private val Gold = AppPalette.Gold
private val GoldDim = AppPalette.GoldDim
private val GoldFrame = AppPalette.GoldFrame
private val TextBody = AppPalette.TextBody
private val Muted = AppPalette.TextMuted
private val Success = AppPalette.Success

/**
 * Sufit rozmiaru slotu. Zszedl z 58 na 46: sloty sa RAMA ekranu, a nie jego trescia - to postac ma
 * byc najwieksza rzecza na karcie postaci. Przy 58dp osiem pustych kwadratow z plusem przykuwalo
 * wzrok mocniej niz bohater w srodku.
 */
private val SLOT_MAX = 46.dp

/** Odstep miedzy slotami w bocznej kolumnie - wchodzi tez w rachunek ich rozmiaru w [PaperDoll]. */
private val SLOT_GAP = 8.dp

private val BACKPACK_ICON = 64.dp

/**
 * Rozmiar rycerza w podgladzie - dopasowany do wysokosci [Viewport], zeby nic sie nie obcinalo.
 * Zmniejszony, gdy na ekran weszly poziom i statystyki bohatera: podglad byl najwiekszym elementem
 * ekranu, a niosl najmniej informacji, wiec to on oddal miejsce, a nie ekwipunek.
 */
/**
 * Ile wysokosci podgladu zajmuje sam rycerz. Nie 1f, bo sprite jest kwadratem z postacia stojaca
 * przy dolnej krawedzi - pelna wysokosc wpychalaby helm w ramke.
 */
private const val KNIGHT_HEIGHT_FRACTION = 0.92f

/**
 * Sufit wysokosci bloku z postacia, liczony z SZEROKOSCI panelu. Bez niego na wysokim telefonie
 * blok rozciagalby sie w pionowy slup, w ktorym sylwetka (ograniczona szerokoscia) tonelaby
 * w pustce - dokladnie to, co bylo widac przed ta poprawka.
 */
private const val DOLL_HEIGHT_LIMIT = 0.85f

private val LEFT_SLOTS = listOf(ItemSlot.Head, ItemSlot.Chest, ItemSlot.Amulet)
private val RIGHT_SLOTS = listOf(ItemSlot.Hands, ItemSlot.Legs, ItemSlot.Feet)
private val BOTTOM_SLOTS = listOf(ItemSlot.MainHand, ItemSlot.OffHand, ItemSlot.Ring)

@Composable
fun EquipmentScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    onOpenTalents: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    /** Slot, dla ktorego otwarto liste posiadanych przedmiotow. */
    var pickerSlot by remember { mutableStateOf<ItemSlot?>(null) }

    /** Przedmiot ogladany w karcie szczegolow - patrz [ItemDetailSheet]. */
    // Karta pokazuje KONKRETNA sztuke - dwie kopie miecza roznia sie poziomem, wiec typ nie wystarcza.
    var inspected by remember { mutableStateOf<ItemInstance?>(null) }

    /** Plecak jako nakladka, nie sekcja - ekran ma sie miescic w calosci, patrz [EquipmentScreen]. */
    var backpackOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // BEZ scrolla. Naglowek, statystyki i dolny pasek maja stala wysokosc, a CALA reszta
        // przypada lalce - dzieki temu drzewko talentow stoi zawsze w tym samym miejscu, zamiast
        // uciekac pod krawedz razem z dlugoscia plecaka.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Gora ekranu: kim jestes, na jakim poziomie i co masz do rozdania. Powrot siedzi
            // W naglowku, a nie nad nim - osobny rzad kosztowal caly wiersz wysokosci, a niosl
            // jeden przycisk.
            HeroHeader(
                name = state.name,
                money = state.money,
                progress = state.progress,
                onBack = onBack
            )

            // Lalka i liczby to JEDNO okno, nie dwa. Statystyki wisialy wczesniej jako osobne
            // karty pod ramka, z przerwa miedzy nimi - czytalo sie to jak dwa niezalezne widzety,
            // a nie jak karta postaci. Teraz siedza tuz pod sylwetka, w tym samym obrysie.
            CharacterPanel(
                knight = state.knightEquipment,
                look = state.heroLook,
                itemAt = state::itemIn,
                progress = state.progress,
                combat = state.combatStats,
                onSpend = viewModel::spendStatPoint,
                onSlotClick = { slot ->
                    // Tapniecie zajetego slotu POKAZUJE przedmiot, a nie zdejmuje go po cichu -
                    // to jedyny moment, w ktorym da sie zobaczyc, co ta rzecz w ogole robi.
                    val equipped = state.instanceIn(slot)
                    if (equipped != null) inspected = equipped else pickerSlot = slot
                },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )

            state.message?.let { HintBar(it) }

            // Dolny pasek stoi ZAWSZE w tym samym miejscu, jak zakladki w klasycznych oknach
            // postaci. Wczesniej talenty byly ostatnia sekcja dlugiej, scrollowanej kolumny -
            // czyli trzeba bylo ich szukac.
            BottomTabs(
                backpackCount = state.backpack.size,
                talentPoints = state.talentPoints,
                isAdmin = state.isAdmin,
                onBackpack = { backpackOpen = true },
                onTalents = onOpenTalents,
                onAdminKit = viewModel::grantAdminKit
            )
        }
    }

    if (backpackOpen) {
        BackpackDialog(
            items = state.backpack,
            onDismiss = { backpackOpen = false },
            onInspect = {
                backpackOpen = false
                inspected = it
            }
        )
    }

    pickerSlot?.let { slot ->
        ItemPickerDialog(
            slot = slot,
            items = state.ownedFor(slot),
            onDismiss = { pickerSlot = null },
            onPick = { item ->
                pickerSlot = null
                inspected = item
            }
        )
    }

    // Karta przedmiotu - jedno miejsce, w ktorym widac statystyki, opis i roznice wzgledem tego,
    // co masz zalozone. Zakladanie/zdejmowanie idzie stad, a nie z samego tapniecia w ikone.
    inspected?.let { instance ->
        val item = instance.resolve()
        if (item == null) {
            inspected = null
            return@let
        }
        // Porownujemy po UID, nie po typie: majac dwie kopie miecza, ta zalozona i ta z plecaka
        // to dwie rozne rzeczy i karta musi umiec pokazac roznice miedzy nimi.
        val wornInstance = state.instanceIn(item.slot)
        val worn = wornInstance?.resolve()
        ItemDetailSheet(
            item = item,
            comparedTo = worn?.takeIf { wornInstance.uid != instance.uid },
            equipped = wornInstance?.uid == instance.uid,
            onEquip = {
                viewModel.equipItem(instance)
                inspected = null
            },
            onUnequip = {
                viewModel.unequipSlot(item.slot)
                inspected = null
            },
            onDismiss = { inspected = null }
        )
    }
}

/* ---------------------------------------- Dolny pasek ---------------------------------------- */

/**
 * Staly pasek na dole ekranu - plecak i drzewko talentow.
 *
 * Obie rzeczy byly wczesniej SEKCJAMI scrollowanej kolumny, czyli ich pozycja zalezala od tego, ile
 * masz przedmiotow. Zakladka, ktora wedruje, przestaje byc zakladka: gracz musi jej szukac zamiast
 * siegac odruchowo w to samo miejsce. Teraz stoja tam zawsze, a plecak otwiera sie jako nakladka.
 *
 * Licznik na kafelku jest tu wazniejszy niz gdziekolwiek indziej: awans daje punkty talentow po
 * cichu, w tle walki, wiec bez plakietki nic by o nich nie mowilo.
 */
@Composable
private fun BottomTabs(
    backpackCount: Int,
    talentPoints: Int,
    /** Trzeci kafelek pokazuje sie WYLACZNIE kontom z [com.lernhero.domain.player.AdminAccounts]. */
    isAdmin: Boolean,
    onBackpack: () -> Unit,
    onTalents: () -> Unit,
    onAdminKit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BottomTab(
            icon = "\uD83C\uDF92",
            label = "Backpack",
            badge = backpackCount.takeIf { it > 0 }?.toString(),
            highlight = false,
            onClick = onBackpack,
            modifier = Modifier.weight(1f)
        )
        BottomTab(
            icon = "\uD83C\uDF33",
            label = "Talents",
            badge = talentPoints.takeIf { it > 0 }?.toString(),
            highlight = talentPoints > 0,
            onClick = onTalents,
            modifier = Modifier.weight(1f)
        )
        if (isAdmin) {
            BottomTab(
                icon = "\u2699",
                label = "Admin",
                badge = null,
                highlight = false,
                onClick = onAdminKit,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomTab(
    icon: String,
    label: String,
    badge: String?,
    /** Swieci, gdy jest tam cos DO ZROBIENIA - nierozdane punkty, nie sama zawartosc. */
    highlight: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (highlight) Gold.copy(alpha = 0.12f) else Panel)
            .border(
                width = if (highlight) 2.dp else 1.dp,
                color = if (highlight) Gold else GoldFrame.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 17.sp)
        Text(
            text = label,
            color = if (highlight) Gold else TextBody,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1
        )
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (highlight) Gold else SlotBg)
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    color = if (highlight) AppPalette.Ink else Muted,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/* ------------------------------------------ Title ------------------------------------------ */

/**
 * Naglowek karty postaci: powrot, poziom, imie, pasek XP i zloto - jeden wiersz zamiast trzech
 * osobnych elementow, bo na ekranie bez scrolla kazdy wiersz odbiera miejsce lalce.
 */
@Composable
private fun HeroHeader(name: String, money: Int, progress: PlayerProgress, onBack: () -> Unit) {
    val fill by animateFloatAsState(targetValue = progress.xpProgress, animationSpec = tween(600))

    OrnateFrame(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlotBg.copy(alpha = 0.8f))
                        .border(1.5.dp, GoldDim, RoundedCornerShape(10.dp))
                        .clickable(onClick = onBack)
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Text(text = "\u2190", color = Gold, fontFamily = FontPixel(), fontSize = 14.sp)
                }

                // Awatar niesie poziom - jedna rzecz zamiast dwoch, i od razu widac, gdzie sie patrzy.
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SlotBg)
                        .border(2.dp, Gold, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${progress.level}",
                        color = Gold,
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.ifBlank { "Sir Lernhero" },
                        color = Gold,
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Knight \u00B7 ${progress.xp}/${progress.xpForNextLevel} XP",
                        color = TextBody.copy(alpha = 0.7f),
                        fontFamily = FontFirst(),
                        fontSize = 10.sp
                    )
                }
                Pill(text = "\uD83D\uDCB0 $money")
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SlotBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fill)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(50))
                        .background(Gold)
                )
            }
        }
    }
}

/**
 * Dwie kolumny liczb pod postacia, w ukladzie znanym z klasycznych okien postaci: po lewej to, co
 * SAM rozdajesz, po prawej to, co z tego wychodzi w walce.
 *
 * Wczesniej te same liczby staly w dwoch odleglych miejscach ekranu - cztery kafle atrybutow tuz
 * pod naglowkiem i pasek walki przyklejony do lalki - wiec nie dalo sie zobaczyc zwiazku miedzy
 * nimi. Postawione obok siebie pokazuja go same: punkt w Strength rusza Attack w sasiedniej kolumnie.
 *
 * Wiersz zamiast kafla, bo w kolumnie miesci sie PELNA nazwa statystyki. Skroty (STR/VIT/FOC/LUC)
 * byly konsekwencja poprzedniego, poziomego ukladu, a nie swiadomym wyborem.
 */
@Composable
private fun StatsBoard(
    progress: PlayerProgress,
    combat: HeroCombatStats,
    onSpend: (HeroStat) -> Unit
) {
    val canSpend = progress.unspentPoints > 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatsCard(
            title = if (canSpend) "ATTRIBUTES \u00B7 ${progress.unspentPoints} PTS" else "ATTRIBUTES",
            highlight = canSpend,
            modifier = Modifier.weight(1f)
        ) {
            HeroStat.entries.forEach { stat ->
                StatLine(
                    label = stat.label,
                    value = "${progress.stats[stat]}",
                    onSpend = if (canSpend) ({ onSpend(stat) }) else null
                )
            }
        }

        StatsCard(title = "COMBAT", highlight = false, modifier = Modifier.weight(1f)) {
            StatLine(label = "Attack", value = "${combat.attack}")
            StatLine(label = "Health", value = "${combat.maxHp}")
            StatLine(label = "Crit", value = "${combat.critPercent}%")
            StatLine(label = "Quiz bonus", value = "+${combat.quizBonusPercent}%")
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    highlight: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SlotBg)
            .border(
                width = if (highlight) 1.5.dp else 1.dp,
                color = if (highlight) Success else GoldDim.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        // Pasek tytulu na calej szerokosci - to on trzyma obie kolumny w jednej linii, nawet gdy
        // po lewej pojawi sie dluzszy komunikat o punktach do rozdania.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (highlight) Success.copy(alpha = 0.20f) else Panel)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = title,
                color = if (highlight) Success else Muted,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            content = content
        )
    }
}

/**
 * Jeden wiersz: nazwa po lewej, wartosc po prawej. [onSpend] niepuste zamienia wartosc w przycisk
 * "+" - punkt rozdaje sie DOKLADNIE tam, gdzie sie widzi jego skutek, bez osobnego ekranu.
 */
@Composable
private fun StatLine(label: String, value: String, onSpend: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Muted,
            fontFamily = FontFirst(),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        // Liczby na zielono, opisy na szaro - to jedyne, co odroznia te tabelke od reszty ekranu,
        // i dlatego wystarczy. Kolor niesie tu znaczenie: zielone jest to, co rosnie.
        Text(
            text = value,
            color = Success,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        if (onSpend != null) {
            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Success)
                    .clickable(onClick = onSpend),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = AppPalette.Ink,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/* ---------------------------------------- Paper doll --------------------------------------- */

/**
 * Karta postaci jako JEDNO okno: sylwetka ze slotami u gory, liczby tuz pod nia, wszystko w jednej
 * ramce i na jednym tle.
 *
 * Wczesniej byly to dwa osobne bloki z przerwa miedzy nimi - sylwetka rozciagnieta na cala wolna
 * wysokosc i statystyki dryfujace pod spodem jak niezalezny widzet. Klasyczne okna ekwipunku robia
 * to inaczej i maja racje: liczby naleza do postaci, wiec maja stac przy niej, w tym samym obrysie.
 *
 * Podzial wysokosci: liczby biora tyle, ile potrzebuja, a sylwetka CALA reszte - ale nie wiecej niz
 * [DOLL_HEIGHT_LIMIT] szerokosci panelu. Ten sufit jest tu najwazniejszy: bez niego na wysokim
 * telefonie blok z postacia rozciagal sie w pionowy slup pelen pustki.
 */
@Composable
private fun CharacterPanel(
    knight: KnightEquipment,
    look: HeroLook,
    itemAt: (ItemSlot) -> Item?,
    progress: PlayerProgress,
    combat: HeroCombatStats,
    onSpend: (HeroStat) -> Unit,
    onSlotClick: (ItemSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    OrnateFrame(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(AppPalette.PanelRaised, AppPalette.Ink)))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PaperDoll(
                knight = knight,
                look = look,
                itemAt = itemAt,
                onSlotClick = onSlotClick,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )

            StatsBoard(progress = progress, combat = combat, onSpend = onSpend)
        }
    }
}

/**
 * Sylwetka miedzy dwiema rownymi kolumnami slotow, bronie w rzedzie pod spodem.
 *
 * Zeby to czytalo sie jak okno postaci, a nie jak porozrzucane kwadraty, trzymamy dwie zasady:
 *
 * 1. **Rowne kolumny.** Slot ma zawsze te sama wysokosc, niezaleznie od tego, czy jest pusty -
 *    podpis siedzi W SRODKU kafla, nie pod nim. Podpisy pod spodem robily z kolumny z jednym
 *    zalozonym przedmiotem inna wysokosc niz z kolumny z trzema pustymi.
 * 2. **Blok wysrodkowany, nie rozciagniety.** Zawartosc ma wlasna, policzona wysokosc i stoi na
 *    srodku dostepnego miejsca. Rozciaganie na sile robilo ogromne dziury miedzy slotami.
 */
@Composable
private fun PaperDoll(
    knight: KnightEquipment,
    look: HeroLook,
    itemAt: (ItemSlot) -> Item?,
    onSlotClick: (ItemSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val gap = 10.dp
        // Cztery sloty jeden pod drugim: trzy w bocznej kolumnie i rzad broni pod nia.
        val bySide = (maxHeight - SLOT_GAP * 2 - gap) / 4
        // Sufit trzyma rozmiar przy ziemi na tabletach, podloga pilnuje, zeby ikona zostala
        // klikalna nawet na najnizszym telefonie.
        val slot = bySide.coerceIn(38.dp, SLOT_MAX)
        val columnHeight = slot * 3 + SLOT_GAP * 2
        // Bohater moze byc WYZSZY niz kolumna trzech slotow - i ma byc, bo to on jest trescia
        // tego ekranu. Kolumny wysrodkowuja sie wzgledem niego.
        val dollHeight = maxOf(
            columnHeight,
            minOf(maxHeight - slot - gap, maxWidth * DOLL_HEIGHT_LIMIT)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(dollHeight),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SlotColumn(LEFT_SLOTS, slot, itemAt, onSlotClick)
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { Viewport(knight, look) }
                SlotColumn(RIGHT_SLOTS, slot, itemAt, onSlotClick)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                BOTTOM_SLOTS.forEach { s ->
                    GearSlot(slot = s, item = itemAt(s), size = slot, onClick = { onSlotClick(s) })
                }
            }
        }
    }
}

@Composable
private fun SlotColumn(
    slots: List<ItemSlot>,
    size: Dp,
    itemAt: (ItemSlot) -> Item?,
    onSlotClick: (ItemSlot) -> Unit
) {
    // Staly odstep, nie SpaceEvenly: kolumna ma dokladnie te wysokosc, ktora policzyl
    // [PaperDoll], wiec rozpychanie slotow po niej tylko robiloby dziury.
    Column(
        verticalArrangement = Arrangement.spacedBy(SLOT_GAP),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        slots.forEach { slot ->
            GearSlot(slot = slot, item = itemAt(slot), size = size, onClick = { onSlotClick(slot) })
        }
    }
}

/**
 * Podglad postaci.
 *
 * BEZ ramki i bez wlasnego tla - zostal sam blask pod bohaterem. Wczesniej stal tu czarny,
 * obramowany prostokat na cala wysokosc: przy waskiej kolumnie miedzy slotami wychodzil z tego
 * pionowy slup, w ktorym postac (ograniczona SZEROKOSCIA, nie wysokoscia) siedziala na dole,
 * a nad nia bylo pol ekranu pustki. Czarne pole bylo najwieksza rzecza na ekranie i nie niosl
 * zadnej informacji.
 *
 * Teraz bohater bierze tyle, ile pozwoli mu wezsza z dwoch osi, i stoi na srodku swojego miejsca.
 */
@Composable
private fun Viewport(knight: KnightEquipment, look: HeroLook) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Blask zamiast kadru: oddziela postac od tla aplikacji, nie rysujac wokol niej pudelka.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x66FFCC77),
                            Color(0x22FF9944),
                            Color.Transparent
                        )
                    )
                )
        )
        ModularKnight(
            equipment = knight,
            look = look,
            sizeDp = minOf(maxWidth, maxHeight * KNIGHT_HEIGHT_FRACTION),
            interactive = true
        )
    }
}

/**
 * Pojedynczy slot. Ramka bierze kolor z rzadkosci zalozonego przedmiotu, wiec legendarny miecz
 * widac z drugiego konca ekranu - i nie trzeba do tego zadnej wiedzy o samym mieczu.
 *
 * Pusty slot niesie swoj podpis W SRODKU kafla. Pod spodem podpis psul rownosc siatki: kolumna
 * z trzema pustymi slotami byla wyzsza od kolumny z trzema zalozonymi, wiec lewa i prawa strona
 * lalki rozjezdzaly sie wzgledem siebie.
 */
@Composable
private fun GearSlot(
    slot: ItemSlot,
    item: Item?,
    size: Dp,
    onClick: () -> Unit
) {
    val border = if (item != null) RarityPalette.frame(item.rarity) else GoldDim

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (item != null) {
                    Modifier.background(RarityPalette.cardBackground(item.rarity))
                } else {
                    Modifier.background(SlotBg)
                }
            )
            .border(if (item != null) 2.dp else 1.dp, border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (item != null) {
            // Nazwa przedmiotu nie miesci sie w kaflu i nie musi - pokazuje ja karta przedmiotu
            // po tapnieciu. Tu wystarczy ikona i kolor ramki.
            Text(text = item.icon(), fontSize = (size.value * 0.42f).sp)
        } else {
            Text(
                text = slot.shortLabel(),
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

/* ------------------------------------------ Backpack --------------------------------------- */

/**
 * Plecak jako NAKLADKA, nie sekcja ekranu.
 *
 * Jako sekcja mial dwie wady naraz: pusty zajmowal miejsce, zeby powiedziec, ze jest pusty, a pelny
 * rosl i spychal wszystko pod nim (stad znikajace drzewko talentow). Jako nakladka nie kosztuje nic,
 * dopoki gracz go nie otworzy - a wtedy dostaje caly ekran, zamiast jednego przewijanego rzedu.
 */
@Composable
private fun BackpackDialog(
    items: List<Pair<ItemInstance, Item>>,
    onDismiss: () -> Unit,
    onInspect: (ItemInstance) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = {
            Text(
                text = "Backpack",
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (items.isEmpty()) {
                Text(
                    text = "Backpack is empty. Forge gear at the Forge.",
                    color = Muted,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { (instance, item) ->
                        BackpackItem(
                            item = item,
                            level = instance.level,
                            onClick = { onInspect(instance) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = Gold, fontFamily = FontFirst())
            }
        }
    )
}

@Composable
private fun BackpackItem(item: Item, level: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(BACKPACK_ICON + 10.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(BACKPACK_ICON)
                .clip(RoundedCornerShape(10.dp))
                .background(RarityPalette.cardBackground(item.rarity))
                .border(2.dp, RarityPalette.frame(item.rarity), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = item.icon(), fontSize = 26.sp)
        }
        Text(
            text = item.name,
            color = TextBody,
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/* ------------------------------------------- Picker ----------------------------------------- */

/**
 * Lista posiadanych przedmiotow pasujacych do slotu. Pokazuje efekty kazdego z nich, bo to one -
 * a nie suche liczby - decyduja, co warto zalozyc.
 */
@Composable
private fun ItemPickerDialog(
    slot: ItemSlot,
    items: List<Pair<ItemInstance, Item>>,
    onDismiss: () -> Unit,
    onPick: (ItemInstance) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = {
            Text(
                text = slot.label(),
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (items.isEmpty()) {
                Text(
                    text = "You have nothing for this slot. Forge gear at the Forge.",
                    color = Muted,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEach { (instance, item) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlotBg)
                                .border(1.dp, RarityPalette.frame(item.rarity), RoundedCornerShape(8.dp))
                                .clickable { onPick(instance) }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Panel)
                                    .border(2.dp, RarityPalette.frame(item.rarity), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item.icon(), fontSize = 20.sp)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = item.name,
                                    color = RarityPalette.frame(item.rarity),
                                    fontFamily = FontFirst(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                item.effects.forEach { effect ->
                                    Text(
                                        text = "- ${effect.description}",
                                        color = Success,
                                        fontFamily = FontFirst(),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = Gold, fontFamily = FontFirst())
            }
        }
    )
}

/* ------------------------------------------- Stats ----------------------------------------- */

/** Etykieta po polsku dla kazdej statystyki - w kolejnosci, w jakiej maja sie pokazywac. */
@Composable
private fun OrnateFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Przeswitujaca ramka - tlo apki (HomeNavGraph) ma byc widoczne w calym obszarze
    // wokol slotow/kart, tylko one maja wlasne, solidne tla.
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, GoldDim.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
    ) {
        content()
    }
}

@Composable
private fun Pill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SlotBg)
            .border(1.dp, GoldFrame, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = Gold, fontFamily = FontFirst(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HintBar(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PanelRaised)
            .border(1.dp, GoldDim, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(message, color = Gold, fontFamily = FontFirst(), style = MaterialTheme.typography.labelMedium)
    }
}
