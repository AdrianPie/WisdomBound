package com.lernhero.game.battle

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.Dp
import com.lernhero.game.FightScreenOrientation
import com.lernhero.game.battle.component.BattleField
import com.lernhero.game.battle.component.BattleSummaryScreen
import com.lernhero.game.battle.component.BossEventQuizBar
import com.lernhero.game.battle.component.CategoryPickPanel
import com.lernhero.game.battle.component.EaseOutCubic
import com.lernhero.game.battle.component.LiveQuizBar
import com.lernhero.game.battle.component.MasteryDialog
import com.lernhero.game.battle.component.ResolvingStrip
import com.lernhero.game.battle.component.ReviveDialog
import com.lernhero.game.battle.component.BossChargeOverlay
import com.lernhero.game.battle.component.SCENE_TRANSITION_MS
import com.lernhero.game.battle.component.UpgradeChoiceDialog
import androidx.compose.foundation.layout.offset
import com.lernhero.game.battle.component.QuestionBubble
import com.lernhero.game.battle.component.QuestionEntrance
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.AppPalette
import org.koin.compose.viewmodel.koinViewModel

/** Ile ekranu zajmuje dol sceny w kazdym ze stanow tury - patrz [BattleScreen]. */
private const val BOTTOM_FRACTION_INTRO = 0f
private const val BOTTOM_FRACTION_RESOLVING = 0.22f

/**
 * Quiz. Mniej niz kiedys (0.68), bo ramka z pytaniem nie siedzi juz w panelu - wisi na jego gornej
 * krawedzi, polowa nad arena (patrz [BattleScreen]). Panel utrzymuje wiec tylko pasek czasu i liste
 * odpowiedzi, a odzyskana wysokosc dostaje arena.
 */
private const val BOTTOM_FRACTION_QUESTION = 0.58f

/**
 * Wybor tematu ([TurnPhase.CategoryPick]) dostaje WYRAZNIE mniej dolu niz quiz - to tylko siatka
 * 2x2 niskich kafli (kula + nazwa), nie ma pod nimi zadnej tresci, wiec 0.68 zostawialo pol ekranu
 * pustego, kremowego panelu.
 */
private const val BOTTOM_FRACTION_CATEGORY = 0.31f

/**
 * Zdarzenie bossa dostaje WYRAZNIE mniej dolu niz zwykly quiz - cala scena dzieje sie na arenie
 * (zbliżenie bossa, iskry, licznik stawki), wiec panel z suwakiem ma jej nie zaslaniac.
 */
private const val BOTTOM_FRACTION_BOSS_EVENT = 0.46f

/**
 * Ekran walki: fale wrogow nadciagaja bez konca, kazda tura to jedno pytanie quizu. Koniec przychodzi
 * tylko przez porazke albo kapitulacje - stad jedna droga powrotu, [onLeaveBattle].
 *
 * CALA scena (arena + dolny panel) jest sterowana JEDNA wartoscia - [bottomFraction] - zeby przejscie
 * "wejscie postaci -> quiz" bylo jednym, ciaglym ruchem kamery, a nie kilkoma przelaczanymi ekranami.
 * Wczesniej byly tu trzy niezalezne, skokowe zmiany i to one dawaly wrazenie teleportacji:
 *
 *  1. arena miala na sztywno `weight(1f)` dopoki trwalo intro, a w klatce przejscia na [GamePhase.Battle]
 *     przeskakiwala od razu na 0.78f (bo tam juz od dawna staly `animateFloatAsState`) - skok bez animacji,
 *  2. dolny slot w ogole nie istnial podczas intro, wiec zwolnione miejsce nie mialo czym byc wypelnione
 *     i przez dziure widac bylo tlo aplikacji (poprzedni ekran),
 *  3. dopiero potem ruszala druga animacja 0.78f -> 0.32f - czyli trzeci, osobny ruch.
 *
 * Teraz jest jedno zrodlo prawdy: [bottomFraction] plynnie interpoluje miedzy 0f (intro, arena na caly
 * ekran), 0.22f (rozliczanie tury) i 0.58f (pytanie). Arena bierze `weight(1f)`, czyli zawsze DOKLADNIE
 * to, co zostalo - nigdy nie ma szczeliny, przez ktora cokolwiek moglo by przeswitywac.
 *
 * Ta sama wartosc pozycjonuje ramke z pytaniem, ktora wisi na granicy areny i panelu - polowa nad,
 * polowa pod. Rysuje sie ja POZA [SceneBottomPanel] (ten przycina do swoich granic), na koncu tego
 * samego Boxa co cala scena.
 */
@Composable
fun BattleScreen(
    onLeaveBattle: () -> Unit = {}
) {
    FightScreenOrientation()

    val viewModel = koinViewModel<BattleViewModel>()
    val state by viewModel.uiState.collectAsState()

    // Zwykle pytanie i zaklecie bossa rozwijaja dol tak samo - zaklecie ma wygladac jak normalny quiz,
    // tylko z inna zawartoscia w srodku.
    val bossEvent = state.bossEvent
    // Podczas LADOWANIA bossa dol jest schowany - najpierw ma byc widac, co boss szykuje.
    // Panel z pytaniem wjezdza dopiero w fazie Quiz i jest nizszy niz zwykly quiz.
    // Panel jest na gorze tylko wtedy, gdy gracz ma cos do zrobienia albo do przeczytania.
    // Kulminacja (lot kuli) ma caly ekran dla siebie - nic jej nie zaslania.
    val bossQuizUp = bossEvent != null &&
        bossEvent.phase != BossEventPhase.Charging &&
        bossEvent.phase != BossEventPhase.Payoff
    val bottomExpanded = state.turnPhase == TurnPhase.Question ||
        state.turnPhase == TurnPhase.CategoryPick ||
        bossQuizUp
    val bottomFraction by animateFloatAsState(
        targetValue = when {
            state.phase != GamePhase.Battle -> BOTTOM_FRACTION_INTRO
            bossEvent != null && !bossQuizUp -> BOTTOM_FRACTION_RESOLVING
            bossQuizUp -> BOTTOM_FRACTION_BOSS_EVENT
            state.turnPhase == TurnPhase.CategoryPick -> BOTTOM_FRACTION_CATEGORY
            bottomExpanded -> BOTTOM_FRACTION_QUESTION
            else -> BOTTOM_FRACTION_RESOLVING
        },
        animationSpec = tween(durationMillis = SCENE_TRANSITION_MS, easing = EaseOutCubic),
        label = "sceneBottomFraction"
    )

    // Nieprzezroczyste tlo pod cala scena - ekran walki jest rysowany nad wspolnym tlem aplikacji
    // (niebo z HomeNavGraph), wiec bez tego kazda szczelina pokazywalaby poprzedni ekran.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPalette.Background)
    ) {
        val screenHeight = maxHeight

        // Ramka pytania idzie przez CALA szerokosc ekranu (dotyka obu krawedzi), wiec jej wysokosc
        // wynika wprost z proporcji grafiki - to samo wyliczenie robi w srodku QuestionBubble.
        val questionFrameHeight = maxWidth / Resources.QuizArt.QUESTION_FRAME_RATIO

        Column(modifier = Modifier.fillMaxSize()) {
            // Arena zawsze zajmuje CALA reszte wysokosci - kurczy sie plynnie razem z rosnacym dolem,
            // a postacie (rozstawione na ulamkach wysokosci) jada do gory dokladnie w tym samym rytmie.
            Box(modifier = Modifier.weight(1f)) {
                BattleField(
                    state = state,
                    onAttackFinished = viewModel::onAttackFinished,
                    onHitRecovered = viewModel::onHitRecovered,
                    onEffectShown = viewModel::consumeFloatingEffect,
                    onProjectileShown = viewModel::consumeProjectile,
                    onUltimateShown = viewModel::consumeUltimateCast,
                    onCapitulate = viewModel::capitulate,
                    modifier = Modifier.fillMaxSize()
                )

                // Inscenizacja zdarzenia bossa rysuje sie NAD arena i nie zabiera jej miejsca -
                // boss zostaje tam, gdzie stal, a scena dzieje sie wokol niego.
                if (bossEvent != null) {
                    BossChargeOverlay(event = bossEvent, modifier = Modifier.fillMaxSize())
                }
            }

            SceneBottomPanel(
                height = screenHeight * bottomFraction,
                // Panel pytania jest renderowany w stalej, docelowej wysokosci - dzieki temu wjezdza
                // w calosci, bez rozciagania sie w trakcie. Pasek rozliczania tury nie wjezdza,
                // wiec po prostu wypelnia to, co aktualnie ma do dyspozycji.
                pinnedContentHeight = when {
                    bossQuizUp -> screenHeight * BOTTOM_FRACTION_BOSS_EVENT
                    state.turnPhase == TurnPhase.CategoryPick -> screenHeight * BOTTOM_FRACTION_CATEGORY
                    bottomExpanded -> screenHeight * BOTTOM_FRACTION_QUESTION
                    else -> null
                }
            ) {
                if (state.phase == GamePhase.Battle) {
                    when {
                        bossQuizUp && bossEvent != null -> BossEventQuizBar(
                            event = bossEvent,
                            onGuessChange = viewModel::submitBossEventGuess,
                            onConfirm = viewModel::confirmBossEventGuess,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Wybor tematu wchodzi w to samo miejsce co quiz - to nadal "dol ekranu
                        // czeka na decyzje gracza", tylko decyzja jest inna.
                        state.categoryOffer != null -> CategoryPickPanel(
                            offer = state.categoryOffer!!,
                            onPick = viewModel::chooseCategory,
                            teamElements = state.characters.values
                                .filter { it.isPlayer && it.hp > 0 }
                                .mapNotNull { it.element }
                                .groupingBy { it }
                                .eachCount(),
                            modifier = Modifier.fillMaxSize()
                        )

                        state.turnPhase == TurnPhase.Question -> LiveQuizBar(
                            state = state,
                            onAnswer = viewModel::answerQuiz,
                            onOrderPick = viewModel::pickOrderItem,
                            onMultiToggle = viewModel::toggleMultiSelect,
                            onMultiConfirm = viewModel::confirmMultiSelect,
                            onContinue = viewModel::continueQuiz,
                            questionOverhang = questionFrameHeight / 2,
                            onTimerStart = viewModel::markQuestionShown,
                            modifier = Modifier.fillMaxSize()
                        )

                        else -> ResolvingStrip(
                            state = state,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Ramka z pytaniem NIE jest czescia dolnego panelu - ten przycina wszystko poza swoimi
        // granicami ([SceneBottomPanel] i jego clipToBounds), a pytanie ma polowa wisiec nad nim,
        // na arenie. Dlatego rysuje sie je tutaj, jako ostatnie dziecko tego samego Boxa co cala
        // scena: nad arena i nad panelem, przez pelna szerokosc ekranu.
        //
        // Pozycja jedzie za animowanym [bottomFraction], wiec ramka plynnie wjezdza razem z panelem
        // zamiast przeskakiwac na miejsce po zakonczeniu animacji.
        if (state.phase == GamePhase.Battle && state.turnPhase == TurnPhase.Question) {
            state.quiz?.let { quiz ->
                QuestionEntrance(questionId = quiz.question.id) {
                    QuestionBubble(
                        prompt = quiz.question.prompt,
                        modifier = Modifier.offset(
                            y = screenHeight * (1f - bottomFraction) - questionFrameHeight / 2
                        )
                    )
                }
            }
        }
    }

    // Awans tematu - lezy nad scena tak samo jak wybor ulepszenia i tak samo wstrzymuje walke,
    // dopoki gracz nie zdecyduje, czy wychodzi z sakwa, czy gra dalej.
    state.masteryOffer?.let { offer ->
        MasteryDialog(
            offer = offer,
            onClaim = viewModel::claimMastery,
            onPushOn = viewModel::pushOnAfterMastery,
            modifier = Modifier.fillMaxSize()
        )
    }

    // Wybor ulepszenia (endless) lezy NAD cala scena i blokuje wszystko pod spoda - walka rusza
    // dalej dopiero, gdy gracz wezmie jedna z kart (BattleViewModel.chooseUpgrade).
    state.upgradeOffer?.let { offer ->
        UpgradeChoiceDialog(
            offer = offer,
            onPick = viewModel::chooseUpgrade,
            modifier = Modifier.fillMaxSize()
        )
    }

    // Ostatnia szansa po wipe'ie - lezy nad scena i trzyma walke, dopoki gracz nie zdecyduje.
    state.reviveOffer?.let { offer ->
        ReviveDialog(
            offer = offer,
            onRevive = viewModel::acceptRevive,
            onReviveWithGems = viewModel::acceptReviveWithGems,
            onGiveUp = viewModel::declineRevive,
            modifier = Modifier.fillMaxSize()
        )
    }

    // Pelny ekran koncowy zamiast dawnego jednolinijkowego dialogu - rozliczenie calego zejscia
    // (nauka, lup, awanse) jest tu jedyna trescia, wiec nie ma sensu chowac go w okienku.
    if (state.phase == GamePhase.Defeat || state.phase == GamePhase.Victory) {
        BattleSummaryScreen(
            state = state,
            onLeave = onLeaveBattle,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Dol sceny. Sam kontener ma animowana wysokosc (to on zabiera miejsce arenie), a gdy podany jest
 * [pinnedContentHeight], zawartosc w srodku jest renderowana ZAWSZE w tej docelowej wysokosci i
 * przyklejona do GORNEJ krawedzi kontenera.
 *
 * Dzieki temu panel pytania nie rozciaga sie w trakcie animacji (zadnego przelewania tekstu ani
 * skakania elementow) - zamiast tego jego gorna krawedz, razem z zaokraglonymi rogami, plynnie jedzie
 * w gore, czyli panel realnie "wjezdza z dolu ekranu". Nadmiar zawartosci wystaje pod dolna krawedz
 * ekranu i jest przycinany przez [clipToBounds].
 */
@Composable
private fun SceneBottomPanel(
    height: Dp,
    pinnedContentHeight: Dp?,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (pinnedContentHeight != null) {
                        Modifier.requiredHeight(pinnedContentHeight)
                    } else {
                        Modifier.fillMaxSize()
                    }
                )
                .align(Alignment.TopCenter)
        ) {
            content()
        }
    }
}
