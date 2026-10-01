package com.lernhero.home.equipment

import com.lernhero.home.tutorial.TutorialTarget
import com.lernhero.home.tutorial.tutorialTarget
import com.lernhero.shared.theme.StrokedText
import com.lernhero.home.city.compact

import com.lernhero.home.ui.JewelPickerDialog
import com.lernhero.domain.jewel.Sockets
import com.lernhero.domain.item.socketedJewels
import com.lernhero.domain.item.base
import com.lernhero.domain.item.Inventory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import com.lernhero.home.ui.darken
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.Popup
import kotlin.math.roundToInt
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.lernhero.domain.ultimate.Ultimates
import com.lernhero.game.battle.component.PowerOrb
import com.lernhero.game.battle.component.color

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
private val SLOT_MAX = 76.dp

/** Odstep miedzy slotami w bocznej kolumnie - wchodzi tez w rachunek ich rozmiaru w [PaperDoll]. */
private val SLOT_GAP = 8.dp

/** Szerokosc slotu jako ulamek szerokosci karty - zmierzone na karcie postaci ze wzorca. */
private const val SLOT_WIDTH_FRACTION = 0.185f

/** Sufit szerokosci calej karty. Na tablecie panel ma zostac panelem, a nie rozlac sie na ekran. */
private val BOARD_MAX_WIDTH = 350.dp

/**
 * Ile wysokosci podgladu zajmuje sam rycerz. Nie 1f, bo sprite jest kwadratem z postacia stojaca
 * przy dolnej krawedzi - pelna wysokosc wpychalaby helm w ramke.
 */
private const val KNIGHT_HEIGHT_FRACTION = 0.92f

/**
 * Sufit wysokosci bloku z postacia, liczony z SZEROKOSCI panelu. Bez niego na wysokim telefonie
 * blok rozciagalby sie w pionowy slup, w ktorym sylwetka (ograniczona szerokoscia) tonelaby w pustce.
 */
private const val DOLL_HEIGHT_LIMIT = 0.54f

/**
 * Ile SZEROKOSCI panelu zajmuje sam bohater. Wiecej niz zostaje miedzy kolumnami slotow - i o to
 * chodzi: sylwetka lezy POD slotami, wiec moze byc szersza niz szczelina miedzy nimi, zamiast
 * kulic sie do jej rozmiaru. Tak samo robi to karta postaci w grach, z ktorych ten uklad pochodzi.
 */
private const val KNIGHT_WIDTH_FRACTION = 0.58f

private val LEFT_SLOTS = listOf(ItemSlot.Chest, ItemSlot.Amulet)
private val RIGHT_SLOTS = listOf(ItemSlot.Hands, ItemSlot.Feet)
/** Ulti na koncu rzedu - obok broni, bo tak jak ona decyduje o tym, jak sie bije. */
private val BOTTOM_SLOTS = listOf(ItemSlot.MainHand, ItemSlot.OffHand, ItemSlot.Ring, ItemSlot.Ultimate)

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
            val owned = state.ownedFor(slot)
            when {
                equipped != null -> inspected = equipped
                // Jedyna rzecz, ktora pasuje do slotu (np. miecz nowego gracza) - od razu jej karta
                // z EQUIP, bez listy z jedna pozycja. Tak tez idzie krok samouczka "zaloz miecz".
                owned.size == 1 -> inspected = owned.first().first
                else -> pickerSlot = slot
            }
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
            .tutorialTarget(TutorialTarget.HeroClose)
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
    // statusBarsPadding: bez niego na telefonach z wycieciem/wyspa gora karty (wstazka "You",
    // imie) wchodzila pod pasek statusu i byla ucieta. Dodatkowe 18 dp u gory sadza karte nieco
    // nizej - blizej kciuka i krzyzyka pod nia.
    PaperPanel(
        modifier = Modifier
            .widthIn(max = BOARD_MAX_WIDTH)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            HeroHeader(
                name = name,
                money = money,
                progress = progress
            )

            Section {
                CharacterSection(
                    knight = knight,
                    look = look,
                    itemAt = itemAt,
                    levelAt = levelAt,
                    combat = combat,
                    isAdmin = isAdmin,
                    onAdminKit = onAdminKit,
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
 * Pasek tozsamosci jak w mobilnych RPG: tarcza z poziomem, imie, wstazka "You" zwisajaca z
 * gornej krawedzi karty, zloto w ciemnej pigulce z moneta, a pod spodem pasek doswiadczenia.
 * Wszystko rysowane w kodzie - bez grafik, wiec ostre na kazdej gestosci ekranu.
 */
@Composable
private fun HeroHeader(
    name: String,
    money: Int,
    progress: PlayerProgress
) {
    val fill by animateFloatAsState(targetValue = progress.xpProgress, animationSpec = tween(600))

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LevelShield(level = progress.level)

                Text(
                    text = name.ifBlank { "Sir Lernhero" },
                    color = Ink,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    lineHeight = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Zloto przy PRAWEJ krawedzi i nic za nim - wstazka "You" wisi na srodku, wiec
                // kazdy dodatkowy przycisk w tym wierszu spychal pigulke pod nia.
                GoldPill(money = money)
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Experience",
                    color = Ink,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                // Mala premia do zlota z poziomu - patrz [com.lernhero.domain.player.LevelGoldBonus].
                val goldBonus = com.lernhero.domain.player.LevelGoldBonus.percent(progress.level)
                if (goldBonus > 0) {
                    GoldCoin(size = 12.dp)
                    Text(
                        text = " +$goldBonus% gold   ",
                        color = Ink,
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

            // Pasek XP: ciemny rowek i zloty, lekko blyszczacy wypelniacz.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF2A2118))
                    .border(1.5.dp, Color(0xFF1A140E), RoundedCornerShape(50))
                    .padding(1.5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fill)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(Brush.verticalGradient(listOf(Color(0xFFFFE27A), Color(0xFFF2A922), Color(0xFFC77A0E))))
                )
            }
        }

        // Wstazka "You" zwisa z gornej krawedzi karty - nie zajmuje miejsca w wierszu z imieniem.
        YouRibbon(modifier = Modifier.align(Alignment.TopCenter).offset(y = (-9).dp))
    }
}

/** Heraldyczna tarcza z numerem poziomu: zlota oprawa, niebieskie pole, bialy obrysowany numer. */
@Composable
private fun LevelShield(level: Int) {
    Box(modifier = Modifier.size(width = 50.dp, height = 58.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            fun shield(inset: Float): androidx.compose.ui.graphics.Path {
                val w = size.width; val h = size.height
                return androidx.compose.ui.graphics.Path().apply {
                    moveTo(inset, inset + h * 0.06f)
                    quadraticTo(w * 0.25f, inset + h * 0.01f, w / 2f, inset)
                    quadraticTo(w * 0.75f, inset + h * 0.01f, w - inset, inset + h * 0.06f)
                    lineTo(w - inset, h * 0.55f)
                    quadraticTo(w - inset, h * 0.82f, w / 2f, h - inset)
                    quadraticTo(inset, h * 0.82f, inset, h * 0.55f)
                    close()
                }
            }
            val d = density
            drawPath(shield(0f), Color(0xFF3A2410))
            drawPath(shield(2f * d), Brush.verticalGradient(listOf(Color(0xFFFFE28A), Color(0xFFD7A13A), Color(0xFF9C6A1C))))
            drawPath(shield(5f * d), Color(0xFF1D3252))
            drawPath(shield(6.5f * d), Brush.verticalGradient(listOf(Color(0xFF4E8BD6), Color(0xFF2A5BA6), Color(0xFF1B3E7A))))
            // Polysk w gornej czesci pola.
            drawPath(
                shield(6.5f * d),
                Brush.verticalGradient(
                    0f to Color.White.copy(alpha = 0.28f),
                    0.45f to Color.Transparent,
                    endY = size.height
                )
            )
        }
        StrokedText(
            text = "$level",
            size = 20.sp,
            color = Color.White,
            outline = Color(0xFF0E1C33),
            strokeWidth = 7f,
            tight = true,
            modifier = Modifier.offset(y = (-3).dp)
        )
    }
}

/** Czerwona wstazka z wcieciem na dole, jak proporzec. */
@Composable
private fun YouRibbon(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(width = 58.dp, height = 36.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height; val notch = h * 0.24f
            fun banner(inset: Float) = androidx.compose.ui.graphics.Path().apply {
                moveTo(inset, 0f)
                lineTo(w - inset, 0f)
                lineTo(w - inset, h - inset)
                lineTo(w / 2f, h - notch - inset * 0.3f)
                lineTo(inset, h - inset)
                close()
            }
            drawPath(banner(0f), Color(0xFF3A0E0A))
            drawPath(banner(2f * density), Brush.verticalGradient(listOf(Color(0xFF8E1F18), Color(0xFFC63A2B), Color(0xFF9E2A1F))))
            // Ciemniejszy pasek u gory - wstazka "wychodzi" spod krawedzi karty.
            drawRect(Color.Black.copy(alpha = 0.25f), size = androidx.compose.ui.geometry.Size(w, h * 0.16f))
        }
        StrokedText(
            text = "You",
            size = 13.sp,
            color = Color.White,
            outline = Color(0xFF3A0E0A),
            strokeWidth = 5f,
            modifier = Modifier.offset(y = (-3).dp)
        )
    }
}

/** Zlota moneta ze znakiem - ta sama w pigulce zlota i przy premii do zlota. */
@Composable
private fun GoldCoin(size: androidx.compose.ui.unit.Dp) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = this.size.minDimension / 2f
            drawCircle(Color(0xFF7A4A0C), r)
            drawCircle(Brush.verticalGradient(listOf(Color(0xFFFFE070), Color(0xFFE9A21C))), r * 0.86f)
            drawCircle(Color(0xFFB9720F), r * 0.62f, style = androidx.compose.ui.graphics.drawscope.Stroke(r * 0.1f))
        }
        Text(
            text = "$",
            color = Color(0xFF8A560E),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.55f).sp,
            lineHeight = (size.value * 0.6f).sp
        )
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
    isAdmin: Boolean,
    onAdminKit: () -> Unit,
    onSlotClick: (ItemSlot) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TagPill(text = "\u2694 Knight", background = ClassRed)
            Box(modifier = Modifier.weight(1f))
            // Zestaw testowy WYLACZNIE dla kont z [com.lernhero.domain.player.AdminAccounts].
            // Stoi tu, a nie w naglowku, bo tam wpychal zloto pod wstazke "You".
            if (isAdmin) {
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFFE9D9B8), Color(0xFFCDB48A))))
                        .border(2.dp, Frame, RoundedCornerShape(8.dp))
                        .clickable(onClick = onAdminKit),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "\u2699", fontSize = 15.sp, color = Frame)
                }
            }
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
        val columnHeight = slot * LEFT_SLOTS.size + SLOT_GAP * (LEFT_SLOTS.size - 1)
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
                                Color(0x40FFF3D6),
                                Color(0x14C9A96E),
                                Color.Transparent
                            )
                        )
                    )
            )
            // Miekka elipsa, ktorej SRODEK lezy na linii podeszew - wczesniej byl to plaski pasek
            // na wysokosci lydek, wiec wygladal jak cos za nogami, a nie cien pod nimi.
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = size * 0.075f)
                    .fillMaxWidth(0.5f)
                    .height(size * 0.08f)
            ) {
                scale(scaleX = 1f, scaleY = this.size.height / this.size.width, pivot = center) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x80503018), Color(0x40503018), Color.Transparent),
                            center = center,
                            radius = this.size.width / 2f
                        ),
                        radius = this.size.width / 2f,
                        center = center
                    )
                }
            }
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
    // Slot broni to cel samouczka - "zaloz miecz" zaczyna sie od tapniecia w niego.
    val tutorial = if (slot == ItemSlot.MainHand) Modifier.tutorialTarget(TutorialTarget.WeaponSlot) else Modifier
    if (item != null) {
        Box(modifier = Modifier.clickable(onClick = onClick).then(tutorial)) {
            ItemIcon(item = item, level = level, rarity = RarityPalette.frame(item.rarity), size = size)
            Ultimates.specFor(item.id)?.let { spec ->
                OrbChargeBadge(spec = spec, modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp))
            }
        }
        return
    }
    if (slot == ItemSlot.Ultimate) {
        EmptyUltimateSlot(size = size, onClick = onClick)
        return
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.17f))
            .background(Inset)
            .border(2.dp, FrameLight, RoundedCornerShape(size * 0.17f))
            .clickable(onClick = onClick)
            .then(tutorial),
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

/**
 * Pusty slot ulti - wglebienie z przygaszona kula, zeby bylo widac, ze wchodzi tu cos innego niz
 * zbroja. Bez kuli bohater i tak ma wbudowane ulti ([Ultimates.BASIC]), stad podpis z seria.
 */
@Composable
private fun EmptyUltimateSlot(size: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.17f))
            .background(Brush.radialGradient(listOf(Color(0xFF6B4A8F), Color(0xFF3A2748))))
            .border(2.dp, Color(0xFFB88AE0), RoundedCornerShape(size * 0.17f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        PowerOrb(color = Color(0xFFB88AE0), size = size * 0.62f, glow = 0.4f, modifier = Modifier.offset(y = -(size * 0.08f)))
        Text(
            text = "ULTI",
            color = Color.White.copy(alpha = 0.85f),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp)
        )
    }
}

/** Malutka pigulka z dlugoscia serii kuli ("5🔥") - rog kafla w slocie, plecaku i liscie wyboru. */
@Composable
private fun OrbChargeBadge(spec: com.lernhero.domain.ultimate.UltimateSpec, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF231830))
            .border(1.dp, spec.color, RoundedCornerShape(50))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = "${spec.streak}\uD83D\uDD25",
            color = Color.White,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 8.sp,
            lineHeight = 9.sp
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
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kolorowe kolko z ikona - to ono niesie ZNACZENIE pigulki; sama liczba bez niego
        // wygladalaby jak cztery takie same kafelki z roznymi cyframi.
        Box(
            modifier = Modifier.size(20.dp).clip(CircleShape).background(tint),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 10.sp)
        }
        // 11 sp: przy wezszej karcie "+105%" przy 12 sp konczylo sie wielokropkiem.
        Text(
            text = value,
            color = Ink,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
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
    // Tapniecie w karte pokazuje NAD nia dymek z tym, co atrybut robi; drugie tapniecie chowa.
    var explained by remember { mutableStateOf<HeroStat?>(null) }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Attributes",
                color = Ink,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            if (canSpend) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFF5CC84A), Color(0xFF2F8A2A))))
                        .border(1.dp, Color(0xFF1F5E1C), RoundedCornerShape(20.dp))
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${progress.unspentPoints} points to spend",
                        color = Color.White,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            } else {
                Text(
                    text = "Tap a stat for details",
                    color = InkMuted,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
            }
        }
        HeroStat.entries.chunked(2).forEach { pair ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pair.forEach { stat ->
                    StatCard(
                        stat = stat,
                        points = progress.stats[stat],
                        canSpend = canSpend,
                        explained = explained == stat,
                        onToggle = { explained = if (explained == stat) null else stat },
                        onDismiss = { explained = null },
                        onSpend = { onSpend(stat) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Karta jednego atrybutu: kolorowe kolko z ikona, nazwa, duza wartosc i krotki efekt pod nia.
 * Kolor niesie tozsamosc atrybutu (sila - czerwien, zywotnosc - zielen...), wiec cztery karty
 * da sie odroznic bez czytania.
 */
@Composable
private fun StatCard(
    stat: HeroStat,
    points: Int,
    canSpend: Boolean,
    explained: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onSpend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val look = stat.look()
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.55f), look.color.copy(alpha = 0.14f))))
            .border(1.dp, look.color.copy(alpha = 0.45f), shape)
            .clickable(onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(look.color.lightened(0.35f), look.color)))
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = look.icon, fontSize = 14.sp, lineHeight = 16.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stat.label.uppercase(),
                color = InkMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                lineHeight = 10.sp,
                letterSpacing = 0.6.sp
            )
            Text(
                text = "$points",
                color = Ink,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                lineHeight = 20.sp
            )
            Text(
                text = stat.shortEffect(points),
                color = look.color.darken(0.25f),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                lineHeight = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (canSpend) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF6FD85A), Color(0xFF2F8A2A))))
                    .border(1.dp, Color(0xFF1F5E1C), CircleShape)
                    .clickable(onClick = onSpend),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    lineHeight = 16.sp
                )
            }
        }
        if (explained) {
            // Nakladka NAD karta - nie wpycha sie w uklad, nic pod nia sie nie przesuwa.
            Popup(popupPositionProvider = AboveAnchor, onDismissRequest = onDismiss) {
                StatBubble(stat = stat, points = points)
            }
        }
    }
}

private class StatLook(val icon: String, val color: Color)

private fun HeroStat.look(): StatLook = when (this) {
    HeroStat.Strength -> StatLook("⚔️", Color(0xFFD9503F))
    HeroStat.Vitality -> StatLook("❤️", Color(0xFF3FA356))
    HeroStat.Focus -> StatLook("🧠", Color(0xFF3F7FD9))
    HeroStat.Luck -> StatLook("🍀", Color(0xFFD9A020))
}

/** Krotki efekt pod wartoscia na karcie - pelne zdanie jest w dymku ([StatBubble]). */
private fun HeroStat.shortEffect(points: Int): String = when (this) {
    HeroStat.Strength -> "+${points * 2} attack"
    HeroStat.Vitality -> "+${points * 8} HP"
    HeroStat.Focus -> "+${points * 2}% quiz dmg"
    HeroStat.Luck -> "+${((points * 0.6f) * 10).roundToInt() / 10f}% crit"
}

private fun Color.lightened(amount: Float): Color = Color(
    red = red + (1f - red) * amount,
    green = green + (1f - green) * amount,
    blue = blue + (1f - blue) * amount,
    alpha = alpha
)

/* ------------------------------------- Wspolne klocki --------------------------------------- */

/** Panel z papieru w grubej brazowej oprawie - podstawowy budulec tego ekranu. */
/** Dymek pod wierszem atrybutu: co robi jeden punkt i ile daja punkty juz rozdane. */
@Composable
private fun StatBubble(stat: HeroStat, points: Int) {
    val total = when (stat) {
        HeroStat.Strength -> "+${points * 2} party attack"
        HeroStat.Vitality -> "+${points * 8} max party HP"
        HeroStat.Focus -> "+${points * 2}% damage from correct answers"
        HeroStat.Luck -> "+${((points * 0.6f) * 10).roundToInt() / 10f}% critical hit chance"
    }
    Column(modifier = Modifier.widthIn(max = 300.dp)) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(BubbleInk)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stat.description,
                color = Color(0xFFF4E9D7),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Text(
                text = "Now: $total",
                color = Color(0xFFFFD34D),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        // Ogonek dymka wskazujacy w dol, na wiersz.
        Box(
            modifier = Modifier
                .padding(start = 18.dp)
                .size(10.dp)
                .offset(y = (-5).dp)
                .rotate(45f)
                .background(BubbleInk)
        )
    }
}

/** Stawia dymek tuz nad kotwica (wierszem atrybutu), wyrownany do jego lewej krawedzi. */
private val AboveAnchor = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = IntOffset(
        x = anchorBounds.left.coerceAtMost(windowSize.width - popupContentSize.width).coerceAtLeast(0),
        y = (anchorBounds.top - popupContentSize.height).coerceAtLeast(0)
    )
}

private val BubbleInk = Color(0xFF3A2A1C)

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
    // Ciemna pigulka z moneta wystajaca z lewej krawedzi - jak licznik waluty w grach.
    Box(contentAlignment = Alignment.CenterStart) {
        Box(
            modifier = Modifier
                .padding(start = 14.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF4A3A2A), Color(0xFF2A2018))))
                .border(2.dp, Color(0xFF1A130C), RoundedCornerShape(20.dp))
                .padding(start = 18.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Text(
                // Skrot jak na gornym pasku (10.0M) - pelna liczba wpychala sie pod wstazke "You".
                text = money.compact(),
                color = Color(0xFFFFE08A),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                lineHeight = 16.sp
            )
        }
        GoldCoin(size = 28.dp)
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
            .padding(horizontal = 10.dp, vertical = 8.dp),
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

/** Ile kafli w rzedzie plecaka - cztery to jeszcze czytelna ikona i nazwa na waskim telefonie. */
private const val BACKPACK_COLUMNS = 4

/** Grupy filtra plecaka. Pusta lista slotow = wszystko. */
private enum class BackpackFilter(val label: String, val slots: Set<ItemSlot>) {
    All("All", emptySet()),
    Weapons("Weapons", setOf(ItemSlot.MainHand, ItemSlot.OffHand)),
    Armor("Armor", setOf(ItemSlot.Head, ItemSlot.Chest, ItemSlot.Hands, ItemSlot.Legs, ItemSlot.Feet)),
    Jewelry("Jewelry", setOf(ItemSlot.Ring, ItemSlot.Amulet)),
    Ulti("Ulti", setOf(ItemSlot.Ultimate));

    fun accepts(item: Item) = slots.isEmpty() || item.slot in slots
}

/**
 * Plecak jako NAKLADKA, nie sekcja ekranu - i to w tej samej oprawie co karta postaci, spod ktorej
 * sie go otwiera (papier w brazowej ramie, krzyzyk pod oknem).
 *
 * Wczesniej byl to systemowy AlertDialog: waski, z trzema kaflami przyklejonymi do lewej krawedzi,
 * nazwami, ktore wylazily z komorek, i ostatnim rzedem uscietym w pol. Teraz siatka zawsze wypelnia
 * cala szerokosc, kazda komorka ma miejsce na dwie linie nazwy (rzedy sa rowne), a najlepsze rzeczy
 * stoja na gorze - przy 60+ przedmiotach szukanie legendarnego miecza w kolejnosci zdobycia to
 * przewijanie na slepo.
 */
@Composable
private fun BackpackDialog(
    items: List<Pair<ItemInstance, Item>>,
    onDismiss: () -> Unit,
    onInspect: (ItemInstance) -> Unit
) {
    var filter by remember { mutableStateOf(BackpackFilter.All) }
    val sorted = remember(items) {
        items.sortedWith(
            compareByDescending<Pair<ItemInstance, Item>> { it.second.rarity.ordinal }
                .thenByDescending { it.first.level }
                .thenBy { it.second.name }
        )
    }
    val shown = sorted.filter { filter.accepts(it.second) }

    Box(modifier = Modifier.fillMaxSize()) {
        DialogScrim(onDismiss = onDismiss)

        PaperPanel(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = BOARD_MAX_WIDTH)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 14.dp, end = 14.dp, top = 16.dp, bottom = DIALOG_CLOSE_LANE)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "\uD83C\uDF92", fontSize = 20.sp)
                    Text(
                        text = "Backpack",
                        color = Ink,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TagPill(
                        text = if (items.size == 1) "1 item" else "${items.size} items",
                        background = Navy
                    )
                }

                if (items.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BackpackFilter.entries.forEach { f ->
                            FilterChip(
                                label = f.label,
                                count = items.count { f.accepts(it.second) },
                                selected = f == filter,
                                onClick = { filter = f },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Siatka w jasnym wglebieniu - ta sama "sekcja" co postac i atrybuty na karcie.
                // Przewija sie SAMA siatka: naglowek i filtry zostaja na miejscu.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Card)
                        .border(1.5.dp, ParchmentPalette.CardEdge, RoundedCornerShape(12.dp))
                ) {
                    if (shown.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 34.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "\uD83C\uDF92", fontSize = 34.sp)
                            Text(
                                text = if (items.isEmpty()) "Your backpack is empty." else "Nothing here yet.",
                                color = Ink,
                                fontFamily = FontFirst(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Gear drops from chests and bosses.",
                                color = InkMuted,
                                fontFamily = FontFirst(),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val gap = 8.dp
                            val pad = 10.dp
                            val cell = (maxWidth - pad * 2 - gap * (BACKPACK_COLUMNS - 1)) / BACKPACK_COLUMNS
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    // Wiecej miejsca u gory: klejnot rzadkosci wystaje ponad kafel.
                                    .padding(start = pad, end = pad, top = pad + 4.dp, bottom = pad),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                shown.chunked(BACKPACK_COLUMNS).forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                                        row.forEach { (instance, item) ->
                                            BackpackItem(
                                                item = item,
                                                level = instance.level,
                                                size = cell,
                                                onClick = { onInspect(instance) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        DialogCloseButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 14.dp)
        )
    }
}

/** Zakladka filtra: zolta, gdy wybrana, z licznikiem - zeby bylo widac, ze np. bizuterii nie masz wcale. */
@Composable
private fun FilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(if (selected) Button else Card)
            .border(if (selected) 2.dp else 1.5.dp, if (selected) ParchmentPalette.ButtonEdge else ParchmentPalette.CardEdge, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Ink,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 12.sp,
            maxLines = 1
        )
        Text(
            text = "$count",
            color = if (selected) Ink else InkMuted,
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            lineHeight = 10.sp
        )
    }
}

@Composable
private fun BackpackItem(item: Item, level: Int, size: Dp, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(size)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box {
            ItemIcon(item = item, level = level, rarity = RarityPalette.frame(item.rarity), size = size)
            Ultimates.specFor(item.id)?.let { spec ->
                OrbChargeBadge(spec = spec, modifier = Modifier.align(Alignment.TopEnd).offset(x = 3.dp, y = (-3).dp))
            }
        }
        // Zawsze DWIE linie miejsca na nazwe - inaczej rzad z "Bronze Warhammer" jest wyzszy
        // od sasiedniego i siatka sie rozjezdza.
        Text(
            text = item.name,
            color = Ink,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            lineHeight = 10.sp,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
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
                    text = if (slot == ItemSlot.Ultimate) {
                        "No power orbs yet - they drop from chests and bosses. Until then your hero uses " +
                            "${Ultimates.BASIC.name} (${Ultimates.BASIC.streak} correct in a row)."
                    } else {
                        "You have nothing for this slot yet. Gear drops from chests and bosses."
                    },
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
                                val orb = Ultimates.specFor(item.id)
                                Text(
                                    text = orb?.let { "${it.kind.icon} ${it.streak} in a row" } ?: "🛡 ${GearPower.of(item)}",
                                    color = InkMuted,
                                    fontFamily = FontFirst(),
                                    fontSize = 10.sp
                                )
                                orb?.let {
                                    Text(
                                        text = "✦ ${it.effectText}",
                                        color = Green,
                                        fontFamily = FontFirst(),
                                        fontSize = 9.sp
                                    )
                                }
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
