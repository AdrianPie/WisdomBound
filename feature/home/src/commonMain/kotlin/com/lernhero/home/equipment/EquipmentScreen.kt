package com.lernhero.home.equipment

import com.lernhero.home.ui.JewelPickerDialog
import com.lernhero.domain.jewel.Sockets
import com.lernhero.domain.item.socketedJewels
import com.lernhero.domain.item.base
import com.lernhero.domain.item.Inventory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.item.GearPower
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemInstance
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.resolve
import com.lernhero.domain.player.HeroCombatStats
import com.lernhero.domain.player.HeroStat
import com.lernhero.domain.player.PlayerProgress
import com.lernhero.game.character.HeroLook
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.talent.TalentDialog
import com.lernhero.home.ui.DIALOG_CLOSE_LANE
import com.lernhero.home.ui.DialogCloseButton
import com.lernhero.home.ui.DialogScrim
import com.lernhero.home.ui.ItemDetailSheet
import com.lernhero.home.ui.ItemIcon
import com.lernhero.home.ui.ParchmentPalette
import com.lernhero.home.ui.RarityPalette
import com.lernhero.home.ui.label
import com.lernhero.home.ui.shortLabel
import com.lernhero.shared.theme.FontFirst

/* ---------------------------------------------------------------------------------------------
 *  Karta postaci jest PERGAMINOWA, nie ciemna.
 *
 *  Ekran byl wczesniej czarny ze zlotymi kreskami - wszystko na nim mialo ten sam ciezar, wiec nic
 *  nie bylo widac: sylwetka tonela w tle tego samego koloru co sloty, a liczby czytalo sie z wysilku.
 *  Mobilne RPG rysuja karte postaci ODWROTNIE: jasny papier w ciemnej oprawie, ciemne sloty, jedna
 *  wyrazna liczba mocy. Kontrast robi wtedy cala robote za nas - papier odcina sie od ciemnego tla
 *  aplikacji, a ikony przedmiotow od papieru.
 *
 *  Kolory ida z [ParchmentPalette], czyli z tego samego zrodla co kuznia - jeden zestaw dla calej gry.
 * ------------------------------------------------------------------------------------------- */
private val Paper = ParchmentPalette.Paper
private val Card = ParchmentPalette.Card
private val Inset = ParchmentPalette.Inset
private val Frame = ParchmentPalette.Frame
private val FrameLight = ParchmentPalette.FrameLight
private val Ink = ParchmentPalette.Ink
private val InkMuted = ParchmentPalette.InkMuted
private val Button = ParchmentPalette.Button

/** Granat pod odznaka mocy i pigulkami statystyk - jedyny ciemny akcent na papierze. */
private val Navy = Color(0xFF2E4258)
private val ClassRed = Color(0xFF9E3226)
private val Green = Color(0xFF3F8F3F)

/**
 * Sufit rozmiaru slotu. Sloty sa RAMA ekranu, a nie jego trescia - to postac ma byc najwieksza
 * rzecza na karcie.
 */
private val SLOT_MAX = 58.dp

/** Odstep miedzy slotami w bocznej kolumnie - wchodzi tez w rachunek ich rozmiaru w [PaperDoll]. */
private val SLOT_GAP = 8.dp

private val BACKPACK_ICON = 60.dp

/** Szerokosc slotu jako ulamek szerokosci karty - zmierzone na karcie postaci ze wzorca. */
private const val SLOT_WIDTH_FRACTION = 0.155f

/** Sufit szerokosci calej karty. Na tablecie panel ma zostac panelem, a nie rozlac sie na ekran. */
private val BOARD_MAX_WIDTH = 380.dp

/**
 * Ile wysokosci podgladu zajmuje sam rycerz. Nie 1f, bo sprite jest kwadratem z postacia stojaca
 * przy dolnej krawedzi - pelna wysokosc wpychalaby helm w ramke.
 */
private const val KNIGHT_HEIGHT_FRACTION = 0.92f

/**
 * Sufit wysokosci bloku z postacia, liczony z SZEROKOSCI panelu. Bez niego na wysokim telefonie
 * blok rozciagalby sie w pionowy slup, w ktorym sylwetka (ograniczona szerokoscia) tonelaby w pustce.
 */
private const val DOLL_HEIGHT_LIMIT = 0.62f

/**
 * Ile SZEROKOSCI panelu zajmuje sam bohater. Wiecej niz zostaje miedzy kolumnami slotow - i o to
 * chodzi: sylwetka lezy POD slotami, wiec moze byc szersza niz szczelina miedzy nimi, zamiast
 * kulic sie do jej rozmiaru. Tak samo robi to karta postaci w grach, z ktorych ten uklad pochodzi.
 */
private const val KNIGHT_WIDTH_FRACTION = 0.58f

private val LEFT_SLOTS = listOf(ItemSlot.Head, ItemSlot.Chest, ItemSlot.Amulet)
private val RIGHT_SLOTS = listOf(ItemSlot.Hands, ItemSlot.Legs, ItemSlot.Feet)
private val BOTTOM_SLOTS = listOf(ItemSlot.MainHand, ItemSlot.OffHand, ItemSlot.Ring)

/**
 * Karta postaci jako NAKLADKA nad ekranem, z ktorego ja otwarto - a nie jako osobny ekran.
 *
 * Jako ekran miala dwie wady naraz: wchodzila na caly ekran (wiec musiala sama odtwarzac pasek
 * gry, tlo i powrot), a wyjscie z niej cofalo nawigacje - gracz ogladajacy ekwipunek z Kuzni czy
 * ze Sklepu ladowal po zamknieciu tam, gdzie akurat wypadl back stack. Jako okno nie rusza
 * nawigacji w ogole: zamkniecie po prostu je gasi i pod spodem jest dokladnie to, co bylo.
 *
 * Tak samo robia to gry, z ktorych ten uklad pochodzi - karta postaci jest tam oknem z krzyzykiem,
 * otwieranym portretem z gornego paska.
 */
@Composable
fun EquipmentDialog(
    viewModel: PlayerViewModel,
    onClose: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    /** Slot, dla ktorego otwarto liste posiadanych przedmiotow. */
    var pickerSlot by remember { mutableStateOf<ItemSlot?>(null) }

    /** Przedmiot ogladany w karcie szczegolow - patrz [ItemDetailSheet]. */
    // Karta pokazuje KONKRETNA sztuke - dwie kopie miecza roznia sie poziomem, wiec typ nie wystarcza.
    var inspected by remember { mutableStateOf<ItemInstance?>(null) }

    /** Plecak jako nakladka, nie sekcja - ekran ma sie miescic w calosci. */
    var backpackOpen by remember { mutableStateOf(false) }

    /**
     * Drzewko talentow - KOLEJNE okno nad karta postaci, a nie skok do osobnego ekranu.
     *
     * Jako ekran wygladalo to na blad: karta postaci znikala, tlo zmienialo sie z papieru na
     * czern, pasek gry zjezdzal, a powrot wracal do zakladki, z ktorej gracz wszedl - nie do
     * karty. Okno nad oknem zachowuje kontekst: pod spodem caly czas lezy ta sama karta i to do
     * niej wraca sie krzyzykiem.
     */
    var talentsOpen by remember { mutableStateOf(false) }

    /** Egzemplarz, do ktorego wolnego slotu wybieramy kamien z sakiewki. */
    var socketTarget by remember { mutableStateOf<ItemInstance?>(null) }

  Box(modifier = Modifier.fillMaxSize()) {
    // Przyciemnienie tla razem z paskiem gry - okno karty postaci ma byc JEDYNA rzecza, z ktora
    // da sie teraz rozmawiac.
    DialogScrim(onDismiss = onClose)

    EquipmentBoard(
        modifier = Modifier.padding(bottom = DIALOG_CLOSE_LANE),
        name = state.name,
        money = state.money,
        progress = state.progress,
        combat = state.combatStats,
        knight = state.knightEquipment,
        look = state.heroLook,
        itemAt = state::itemIn,
        levelAt = { slot -> state.instanceIn(slot)?.level ?: 0 },
        backpackCount = state.backpack.size,
        talentPoints = state.talentPoints,
        isAdmin = state.isAdmin,
        message = state.message,
        onSpend = viewModel::spendStatPoint,
        onSlotClick = { slot ->
            // Tapniecie zajetego slotu POKAZUJE przedmiot, a nie zdejmuje go po cichu - to jedyny
            // moment, w ktorym da sie zobaczyc, co ta rzecz w ogole robi.
            val equipped = state.instanceIn(slot)
            if (equipped != null) inspected = equipped else pickerSlot = slot
        },
        onBackpack = { backpackOpen = true },
        onTalents = { talentsOpen = true },
        onAdminKit = viewModel::grantAdminKit
    )

    DialogCloseButton(
        onClick = onClose,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 14.dp)
    )
  }

    if (talentsOpen) {
        TalentDialog(viewModel = viewModel, onClose = { talentsOpen = false })
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
    inspected?.let { stale ->
        // Po osadzeniu kamienia sztuka w plecaku jest nowsza niz ta zapamietana przy otwarciu karty.
        val instance = Inventory.byUid(state.heroBag, stale.uid) ?: stale
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
            level = instance.level,
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
            onDismiss = { inspected = null },
            socketCount = instance.base()?.let(Sockets::countFor) ?: 0,
            socketed = instance.socketedJewels(),
            onSocketClick = { index ->
                if (index != null) viewModel.unsocketJewel(instance, index) else socketTarget = instance
            }
        )
    }

    socketTarget?.let { target ->
        JewelPickerDialog(
            pouch = state.jewels,
            onPick = { jewel ->
                viewModel.socketJewel(target, jewel.id)
                socketTarget = null
            },
            onDismiss = { socketTarget = null }
        )
    }
}

/**
 * Caly ekran BEZ stanu - sam uklad i dane wejsciowe.
 *
 * Wydzielony z [EquipmentScreen], bo tamten potrzebuje calego [PlayerViewModel] (a wiec i Koina,
 * i repozytorium gracza), zeby pokazac cztery liczby. Osobna, bezstanowa warstwa da sie obejrzec
 * w podgladzie i przetestowac z recznie zlozonym ekwipunkiem - a to jedyny sposob, zeby sprawdzic,
 * jak karta wyglada z PELNYM zestawem, nie czekajac, az ktos wykuje dziewiec przedmiotow.
 */
@Composable
internal fun EquipmentBoard(
    modifier: Modifier = Modifier,
    name: String,
    money: Int,
    progress: PlayerProgress,
    combat: HeroCombatStats,
    knight: KnightEquipment,
    look: HeroLook,
    itemAt: (ItemSlot) -> Item?,
    levelAt: (ItemSlot) -> Int,
    backpackCount: Int,
    talentPoints: Int,
    isAdmin: Boolean,
    message: String?,
    onSpend: (HeroStat) -> Unit,
    onSlotClick: (ItemSlot) -> Unit,
    onBackpack: () -> Unit,
    onTalents: () -> Unit,
    onAdminKit: () -> Unit
) {
  // Scroll siedzi NA TLE, nie w karcie: gdy karta miesci sie w calosci (a na telefonie miesci),
  // stoi po prostu na srodku ekranu; dopiero niski ekran zaczyna ja przesuwac.
  Box(
      modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
      contentAlignment = Alignment.Center
  ) {
    // JEDEN dymek, a nie trzy. Wczesniej naglowek, postac i atrybuty mialy KAZDE wlasna gruba
    // brazowa oprawe, wiec okno czytalo sie jak trzy niezalezne widzety przypadkiem ustawione
    // jeden pod drugim - a nie jak jedna karta postaci. Oprawa jest teraz na zewnatrz, a sekcje
    // w srodku sa lekkimi wglebieniami w papierze. To ta sama zasada, ktora trzyma razem karte
    // przedmiotu: jedno pudelko, kilka sekcji.
    PaperPanel(
        modifier = Modifier
            .widthIn(max = BOARD_MAX_WIDTH)
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            HeroHeader(
                name = name,
                money = money,
                progress = progress,
                isAdmin = isAdmin,
                onAdminKit = onAdminKit
            )

            Section {
                CharacterSection(
                    knight = knight,
                    look = look,
                    itemAt = itemAt,
                    levelAt = levelAt,
                    combat = combat,
                    onSlotClick = onSlotClick
                )
            }

            Section { AttributesSection(progress = progress, onSpend = onSpend) }

            message?.let { HintBar(it) }

            // Na dole mieszcza sie DWIE rzeczy i tyle ich jest: plecak i talenty. Trzeci kafelek
            // (zestaw testowy) sciskal napisy tak, ze zostawaly z nich skroty - a jest narzedziem
            // dewelopera, nie zakladka gracza, wiec przeniosl sie do naglowka.
            BottomTabs(
                backpackCount = backpackCount,
                talentPoints = talentPoints,
                onBackpack = onBackpack,
                onTalents = onTalents
            )
        }
    }
  }
}

/** Sekcja w srodku dymka - wglebienie w papierze, bez wlasnej grubej oprawy. */
@Composable
private fun Section(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Card)
            .border(1.5.dp, ParchmentPalette.CardEdge, RoundedCornerShape(12.dp))
            .padding(7.dp)
    ) {
        content()
    }
}

/* ------------------------------------------ Naglowek ---------------------------------------- */

/**
 * Pasek tozsamosci: portret z poziomem, imie, klasa, pasek XP i zloto.
 *
 * Wzorowany na karcie gracza z mobilnych RPG - portret po lewej, a po prawej WIERSZE "etykieta ->
 * wartosc" rozdzielone cienka kreska. Wiersz czyta sie szybciej niz kafelek, bo oko skanuje jedna
 * kolumne etykiet zamiast szukac liczby w kazdym pudelku osobno.
 */
@Composable
private fun HeroHeader(
    name: String,
    money: Int,
    progress: PlayerProgress,
    isAdmin: Boolean,
    onAdminKit: () -> Unit
) {
    val fill by animateFloatAsState(targetValue = progress.xpProgress, animationSpec = tween(600))

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Portret niesie poziom - jedna rzecz zamiast dwoch, i od razu widac, gdzie sie patrzy.
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF6FA8DC), Color(0xFF4A7FB5))))
                    .border(2.dp, Frame, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${progress.level}",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Text(
                text = name.ifBlank { "Sir Lernhero" },
                color = Ink,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            TagPill(text = "You", background = ClassRed)
            GoldPill(money = money)

            // Zestaw testowy WYLACZNIE dla kont z [com.lernhero.domain.player.AdminAccounts] -
            // jako maly kafelek przy imieniu, a nie jako trzecia zakladka na dole.
            if (isAdmin) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Inset)
                        .border(1.5.dp, Frame, RoundedCornerShape(9.dp))
                        .clickable(onClick = onAdminKit),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "\u2699", fontSize = 13.sp)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Experience",
                color = InkMuted,
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                modifier = Modifier.weight(1f)
            )
            // Mala premia do zlota z poziomu - patrz [com.lernhero.domain.player.LevelGoldBonus].
            val goldBonus = com.lernhero.domain.player.LevelGoldBonus.percent(progress.level)
            if (goldBonus > 0) {
                Text(
                    text = "🪙 +$goldBonus% gold   ",
                    color = Color(0xFF9A6A12),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
            Text(
                text = "${progress.xp} / ${progress.xpForNextLevel}",
                color = Ink,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(50))
                .background(Inset)
                .border(1.dp, FrameLight.copy(alpha = 0.6f), RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fill)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50))
                    .background(Button)
            )
        }
    }
}

/* ------------------------------------- Karta postaci ---------------------------------------- */

/**
 * Sylwetka miedzy dwiema kolumnami slotow, a pod nia pigulki z liczbami bojowymi - wszystko w
 * JEDNYM oknie z papieru.
 *
 * Nad sylwetka stoja dwie plakietki, dokladnie tak jak w grach, z ktorych ten uklad pochodzi:
 * po lewej klasa, po prawej [GearPower] - jedna liczba mowiaca "ile jestes wart". Bez niej gracz
 * musialby porownywac cztery statystyki naraz, zeby stwierdzic, czy w ogole urosl.
 */
@Composable
private fun CharacterSection(
    knight: KnightEquipment,
    look: HeroLook,
    itemAt: (ItemSlot) -> Item?,
    levelAt: (ItemSlot) -> Int,
    combat: HeroCombatStats,
    onSlotClick: (ItemSlot) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TagPill(text = "\u2694 Knight", background = ClassRed)
            Box(modifier = Modifier.weight(1f))
            PowerPill(power = GearPower.of(combat))
        }

        PaperDoll(
            knight = knight,
            look = look,
            itemAt = itemAt,
            levelAt = levelAt,
            onSlotClick = onSlotClick,
            modifier = Modifier.fillMaxWidth()
        )

        CombatPills(combat = combat)
    }
}

/**
 * Sylwetka miedzy dwiema rownymi kolumnami slotow, bronie w rzedzie pod spodem.
 *
 * Zeby to czytalo sie jak okno postaci, a nie jak porozrzucane kwadraty, trzymamy dwie zasady:
 *
 * 1. **Rowne kolumny.** Slot ma zawsze te sama wysokosc, niezaleznie od tego, czy jest pusty -
 *    podpis siedzi W SRODKU kafla, nie pod nim.
 * 2. **Blok wysrodkowany, nie rozciagniety.** Zawartosc ma wlasna, policzona wysokosc i stoi na
 *    srodku dostepnego miejsca. Rozciaganie na sile robilo ogromne dziury miedzy slotami.
 */
@Composable
private fun PaperDoll(
    knight: KnightEquipment,
    look: HeroLook,
    itemAt: (ItemSlot) -> Item?,
    levelAt: (ItemSlot) -> Int,
    onSlotClick: (ItemSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val gap = 8.dp
        // Slot to staly UŁAMEK szerokosci karty, a nie funkcja wolnej wysokosci ekranu: na kazdym
        // telefonie ma wyjsc ta sama, spokojna siatka, a nie szesc coraz wiekszych kwadratow.
        val slot = (maxWidth * SLOT_WIDTH_FRACTION).coerceIn(38.dp, SLOT_MAX)
        val columnHeight = slot * 3 + SLOT_GAP * 2
        val dollHeight = maxOf(columnHeight, maxWidth * DOLL_HEIGHT_LIMIT)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bohater lezy POD kolumnami slotow, a nie miedzy nimi. Gdy siedzial w srodkowej
            // kolumnie, jego rozmiar ograniczala szczelina miedzy slotami - byl przez to o jedna
            // trzecia mniejszy, niz pozwalal ekran, i wokol niego robily sie puste pasy.
            Box(modifier = Modifier.fillMaxWidth().height(dollHeight)) {
                Viewport(
                    knight = knight,
                    look = look,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(KNIGHT_WIDTH_FRACTION)
                        .fillMaxHeight()
                )
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SlotColumn(LEFT_SLOTS, slot, itemAt, levelAt, onSlotClick)
                    SlotColumn(RIGHT_SLOTS, slot, itemAt, levelAt, onSlotClick)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                BOTTOM_SLOTS.forEach { s ->
                    GearSlot(
                        slot = s,
                        item = itemAt(s),
                        level = levelAt(s),
                        size = slot,
                        onClick = { onSlotClick(s) }
                    )
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
    levelAt: (ItemSlot) -> Int,
    onSlotClick: (ItemSlot) -> Unit
) {
    // Staly odstep, nie SpaceEvenly: kolumna ma dokladnie te wysokosc, ktora policzyl [PaperDoll].
    Column(
        verticalArrangement = Arrangement.spacedBy(SLOT_GAP),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        slots.forEach { slot ->
            GearSlot(
                slot = slot,
                item = itemAt(slot),
                level = levelAt(slot),
                size = size,
                onClick = { onSlotClick(slot) }
            )
        }
    }
}

/**
 * Podglad postaci na papierze: cien pod stopami zamiast ramki.
 *
 * Wczesniej stal tu czarny, obramowany prostokat - na jasnej karcie bylby jeszcze gorszy, bo to
 * ON bylby najciemniejsza rzecza na ekranie. Postac potrzebuje tylko jednego: zeby bylo widac,
 * ze STOI. Wystarcza do tego elipsa cienia i cieply poblask pod nia.
 */
@Composable
private fun Viewport(knight: KnightEquipment, look: HeroLook, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val size = minOf(maxWidth, maxHeight * KNIGHT_HEIGHT_FRACTION)

        // Poblask i cien siedza w pudelku SYLWETKI, a nie calego viewportu. Gdy byly przypiete do
        // viewportu, cien zostawal przy jego dolnej krawedzi - czyli kilkadziesiat dp pod stopami,
        // jak kaluza, ktora z postacia nie ma nic wspolnego.
        Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x55FFF3D6),
                                Color(0x22C9A96E),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = size * 0.12f)
                    .fillMaxWidth(0.46f)
                    .height(size * 0.045f)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x38705030))
            )
            ModularKnight(
                equipment = knight,
                look = look,
                sizeDp = size,
                interactive = true
            )
        }
    }
}

/**
 * Pojedynczy slot. Ramka bierze kolor z rzadkosci zalozonego przedmiotu, wiec legendarny miecz
 * widac z drugiego konca ekranu - i nie trzeba do tego zadnej wiedzy o samym mieczu.
 *
 * Pusty slot jest CIEMNY (brazowe wglebienie w papierze) i niesie swoj podpis w srodku kafla.
 * Pod spodem podpis psul rownosc siatki: kolumna z trzema pustymi slotami byla wyzsza od kolumny
 * z trzema zalozonymi.
 */
@Composable
private fun GearSlot(
    slot: ItemSlot,
    item: Item?,
    level: Int,
    size: Dp,
    onClick: () -> Unit
) {
    if (item != null) {
        Box(modifier = Modifier.clickable(onClick = onClick)) {
            ItemIcon(item = item, level = level, rarity = RarityPalette.frame(item.rarity), size = size)
        }
        return
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.17f))
            .background(Inset)
            .border(2.dp, FrameLight, RoundedCornerShape(size * 0.17f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = slot.shortLabel(),
            color = ParchmentPalette.SlotFill.copy(alpha = 0.75f),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

/* ----------------------------------------- Liczby ------------------------------------------- */

/**
 * Pasek pigulek pod postacia: zycie, atak, krytyk, bonus z quizu.
 *
 * To sa liczby, ktore zmieniaja sie po KAZDEJ zmianie w slotach, wiec stoja tuz pod ekwipunkiem -
 * zalozenie miecza i skok ataku widac w jednym ruchu oka. Wczesniej te same liczby siedzialy w
 * osobnej tabelce "COMBAT" obok atrybutow, czyli daleko od przyczyny.
 */
@Composable
private fun CombatPills(combat: HeroCombatStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CombatPill(icon = "❤️", tint = Color(0xFFD4483B), value = "${combat.maxHp}", modifier = Modifier.weight(1f))
        CombatPill(icon = "⚔️", tint = Color(0xFF4A7FB5), value = "${combat.attack}", modifier = Modifier.weight(1f))
        CombatPill(icon = "💥", tint = Color(0xFFD9922B), value = "${combat.critPercent}%", modifier = Modifier.weight(1f))
        CombatPill(icon = "🧠", tint = Green, value = "+${combat.quizBonusPercent}%", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CombatPill(icon: String, tint: Color, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Inset)
            .border(2.dp, FrameLight, RoundedCornerShape(50))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kolorowe kolko z ikona - to ono niesie ZNACZENIE pigulki; sama liczba bez niego
        // wygladalaby jak cztery takie same kafelki z roznymi cyframi.
        Box(
            modifier = Modifier.size(22.dp).clip(CircleShape).background(tint),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 11.sp)
        }
        Text(
            text = value,
            color = Ink,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Atrybuty rozdawane po awansie - osobna karta z wlasna belka tytulowa.
 *
 * Belka niesie licznik nierozdanych punktow i zapala sie na zielono, gdy jest co rozdac: awans
 * daje punkty po cichu, w tle walki, wiec bez tego nic by o nich nie mowilo.
 */
@Composable
private fun AttributesSection(progress: PlayerProgress, onSpend: (HeroStat) -> Unit) {
    val canSpend = progress.unspentPoints > 0

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (canSpend) Green.copy(alpha = 0.22f) else Inset)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Attributes",
                color = Ink,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            if (canSpend) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Green)
                        .padding(horizontal = 7.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${progress.unspentPoints} PTS",
                        color = Color.White,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
        HeroStat.entries.forEachIndexed { index, stat ->
            if (index > 0) Hairline()
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.label,
                    color = Ink,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${progress.stats[stat]}",
                    color = Green,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (canSpend) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(22.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Button)
                            .border(1.5.dp, ParchmentPalette.ButtonEdge, RoundedCornerShape(7.dp))
                            .clickable { onSpend(stat) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            color = Ink,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/* ------------------------------------- Wspolne klocki --------------------------------------- */

/** Panel z papieru w grubej brazowej oprawie - podstawowy budulec tego ekranu. */
@Composable
private fun PaperPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Frame)
            .padding(3.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.verticalGradient(listOf(Paper, ParchmentPalette.PaperDark)))
            .border(1.5.dp, FrameLight.copy(alpha = 0.6f), RoundedCornerShape(15.dp))
    ) {
        content()
    }
}

/** Plakietka klasy / przynaleznosci - ciemna pigulka z bialym napisem. */
@Composable
private fun TagPill(text: String, background: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

/** Odznaka mocy postaci - ta sama skala i ten sam wyglad co na karcie przedmiotu. */
@Composable
private fun PowerPill(power: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Navy)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🛡", fontSize = 12.sp)
        Text(
            text = "$power",
            color = Color.White,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun GoldPill(money: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Button)
            .border(2.dp, ParchmentPalette.ButtonEdge, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "💰", fontSize = 12.sp)
        Text(
            text = "$money",
            color = Ink,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun Hairline() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ParchmentPalette.CardStripe)
    )
}

/* ---------------------------------------- Dolny pasek ---------------------------------------- */

/**
 * Staly pasek na dole ekranu - plecak i drzewko talentow.
 *
 * Obie rzeczy byly wczesniej SEKCJAMI scrollowanej kolumny, czyli ich pozycja zalezala od tego, ile
 * masz przedmiotow. Zakladka, ktora wedruje, przestaje byc zakladka.
 */
@Composable
private fun BottomTabs(
    backpackCount: Int,
    talentPoints: Int,
    onBackpack: () -> Unit,
    onTalents: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
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
            .background(if (highlight) Button else Card)
            .border(
                width = if (highlight) 3.dp else 2.dp,
                color = if (highlight) ParchmentPalette.ButtonEdge else Frame,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = label,
            color = Ink,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1
        )
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (highlight) ClassRed else Inset)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = badge,
                    color = if (highlight) Color.White else Ink,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun HintBar(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Card)
            .border(2.dp, Frame, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(text = message, color = Ink, fontFamily = FontFirst(), fontSize = 12.sp)
    }
}

/* ------------------------------------------ Backpack --------------------------------------- */

/**
 * Plecak jako NAKLADKA, nie sekcja ekranu.
 *
 * Jako sekcja mial dwie wady naraz: pusty zajmowal miejsce, zeby powiedziec, ze jest pusty, a pelny
 * rosl i spychal wszystko pod nim. Jako nakladka nie kosztuje nic, dopoki gracz go nie otworzy.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BackpackDialog(
    items: List<Pair<ItemInstance, Item>>,
    onDismiss: () -> Unit,
    onInspect: (ItemInstance) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Paper,
        titleContentColor = Ink,
        textContentColor = Ink,
        shape = RoundedCornerShape(18.dp),
        title = {
            Text(text = "Backpack", color = Ink, fontFamily = FontFirst(), fontWeight = FontWeight.Bold)
        },
        text = {
            if (items.isEmpty()) {
                Text(
                    text = "Backpack is empty. Forge gear at the Forge.",
                    color = InkMuted,
                    fontFamily = FontFirst(),
                    fontSize = 12.sp
                )
            } else {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
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
                Text(text = "Close", color = Ink, fontFamily = FontFirst(), fontWeight = FontWeight.Bold)
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
        ItemIcon(item = item, level = level, rarity = RarityPalette.frame(item.rarity), size = BACKPACK_ICON)
        Text(
            text = item.name,
            color = Ink,
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
        containerColor = Paper,
        titleContentColor = Ink,
        textContentColor = Ink,
        shape = RoundedCornerShape(18.dp),
        title = {
            Text(text = slot.label(), color = Ink, fontFamily = FontFirst(), fontWeight = FontWeight.Bold)
        },
        text = {
            if (items.isEmpty()) {
                Text(
                    text = "You have nothing for this slot. Forge gear at the Forge.",
                    color = InkMuted,
                    fontFamily = FontFirst(),
                    fontSize = 12.sp
                )
            } else {
                Column(
                    modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { (instance, item) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Card)
                                .border(2.dp, RarityPalette.frame(item.rarity), RoundedCornerShape(10.dp))
                                .clickable { onPick(instance) }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ItemIcon(
                                item = item,
                                level = instance.level,
                                rarity = RarityPalette.frame(item.rarity),
                                size = 44.dp
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = item.name,
                                    color = Ink,
                                    fontFamily = FontFirst(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "🛡 ${GearPower.of(item)}",
                                    color = InkMuted,
                                    fontFamily = FontFirst(),
                                    fontSize = 10.sp
                                )
                                item.effects.forEach { effect ->
                                    Text(
                                        text = "✦ ${effect.description}",
                                        color = Green,
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
                Text(text = "Close", color = Ink, fontFamily = FontFirst(), fontWeight = FontWeight.Bold)
            }
        }
    )
}
