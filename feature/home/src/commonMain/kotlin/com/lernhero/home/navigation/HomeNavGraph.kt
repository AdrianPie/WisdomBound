package com.lernhero.home.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.painterResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.lernhero.domain.run.RunSession
import com.lernhero.game.battle.BattleScreen
import com.lernhero.domain.town.ProductionBuilding
import com.lernhero.home.HomeScreen
import com.lernhero.home.city.ForgeScreen
import com.lernhero.home.city.ProductionBuildingScreen
import com.lernhero.home.city.SummonCenterScreen
import com.lernhero.home.dev.ChestPreviewScreen
import com.lernhero.home.dev.KnightPreviewScreen
import com.lernhero.home.dev.ModularKnightPreviewScreen
import com.lernhero.home.dev.MonsterAnchorLabScreen
import com.lernhero.home.dev.MonsterAnimationLabScreen
import com.lernhero.home.story.WorldMapScreen
import com.lernhero.home.equipment.EquipmentDialog
import com.lernhero.home.growth.GrowthScreen
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.quiz.CustomQuizViewModel
import com.lernhero.home.quiz.DeckEditorScreen
import com.lernhero.home.settings.SettingsScreen
import com.lernhero.home.progress.ProgressScreen
import com.lernhero.home.quiz.QuizStudioScreen
import com.lernhero.reader.ReaderChrome
import com.lernhero.reader.VocabularyScreen
import com.lernhero.home.shop.ShopScreen
import com.lernhero.home.ui.GameTopBar
import com.lernhero.home.ui.TOP_BAR_HEIGHT
import com.lernhero.home.ui.TOP_BAR_PLATE_HEIGHT
import com.lernhero.domain.item.GearPower
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.saveable.rememberSaveable
import com.lernhero.domain.energy.EnergyRules
import com.lernhero.home.ui.EnergyRefillDialog
import com.lernhero.domain.progress.CategoryMastery
import com.lernhero.domain.run.BattleKind
import com.lernhero.domain.run.BattleRequest
import com.lernhero.domain.run.RunDifficulty
import com.lernhero.domain.run.RunLadder
import com.lernhero.domain.story.CampaignSelection
import com.lernhero.domain.story.StageRepeats
import com.lernhero.domain.story.StoryStageCatalog
import com.lernhero.domain.story.StoryWorldCatalog
import com.lernhero.domain.quiz.WordQuizBank
import com.lernhero.domain.summon.FreeSummon
import com.lernhero.home.fight.LavaBackdrop
import com.lernhero.home.fight.FightMode
import com.lernhero.home.fight.FightScreen
import com.lernhero.home.fight.MaterialSheet
import com.lernhero.home.fight.PartySheet
import com.lernhero.home.fight.RunMaterial
import com.lernhero.home.fight.deckIn
import com.lernhero.home.fight.icon
import com.lernhero.home.fight.label
import com.lernhero.home.fight.topic
import com.lernhero.home.fight.ladderStageLabel
import com.lernhero.home.ui.DialogCloseButton
import kotlin.random.Random
import kotlin.time.Clock
import com.lernhero.home.story.CampaignBoard
import com.lernhero.home.story.CampaignRewards
import com.lernhero.home.story.payoutLabel
import com.lernhero.home.story.rememberCampaignLauncher
import com.lernhero.shared.navigation.Screen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeNavGraph(
    /**
     * Wyjscie z konta. Ten graf ma WLASNY NavController, wiec z wylogowania nie da sie dojsc do
     * ekranu logowania inaczej niz oddajac decyzje grafowi nadrzednemu (patrz
     * com.lernhero.navigation.SetupNavGraph).
     */
    onSignedOut: () -> Unit = {}
) {

    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState()
    // Rozwiazane RAZ tutaj (nie w kazdym ekranie osobno) - Home/Team/Equipment/Shop maja dzielic
    // JEDEN stan gracza, zeby zakup w sklepie od razu byl widoczny w ekwipunku/druzynie, bez
    // koniecznosci wyjscia i wejscia z powrotem na ekran.
    val playerViewModel = koinViewModel<PlayerViewModel>()
    // Talie pytan dzieli zakladka "Walcz" z edytorem - edytor musi widziec te sama liste, w
    // ktorej gracz zaraz wybierze talie do walki, bez czekania na ponowne wczytanie z bazy.
    val customQuizViewModel = koinViewModel<CustomQuizViewModel>()

    // Zakladki to jeden zywy back stack (saveState/restoreState), wiec Team NIE recomponuje sie
    // od zera przy kazdym powrocie na nia - LaunchedEffect(Unit) w jej wnetrzu odpaliby sie tylko
    // raz. Bez tego slowka zapisane w Czytelni nie byly widoczne w Endless "Wlasne" az do restartu
    // aplikacji (PlayerViewModel czytal stan gracza tylko raz, przy pierwszym wejsciu).
    LaunchedEffect(currentRoute.value) {
        if (currentRoute.value.isOnRoute<Screen.Team>()) {
            playerViewModel.refresh()
        }
    }

    val selectedDestination by remember {
        derivedStateOf {
            when {
                currentRoute.value.isOnRoute<Screen.Home>() -> BottomBarDestination.Home
                currentRoute.value.isOnRoute<Screen.Team>() -> BottomBarDestination.Fight
                currentRoute.value.isOnRoute<Screen.Growth>() -> BottomBarDestination.Growth
                currentRoute.value.isOnRoute<Screen.QuizStudio>() -> BottomBarDestination.Quizzes
                currentRoute.value.isOnRoute<Screen.Shop>() -> BottomBarDestination.Shop
                else -> BottomBarDestination.Home
            }
        }
    }
    val showBars by remember {
        derivedStateOf {
            !currentRoute.value.isOnRoute<Screen.BattleScreen>() &&
                !currentRoute.value.isOnRoute<Screen.StoryMap>() &&
                !currentRoute.value.isOnRoute<Screen.Quarry>() &&
                !currentRoute.value.isOnRoute<Screen.Forge>() &&
                !currentRoute.value.isOnRoute<Screen.SummonCenter>() &&
                !currentRoute.value.isOnRoute<Screen.Progress>() &&
                // Dev Animation Center jest projektowany na CALY ekran bez przewijania - pasek
                // zakladek zjadalby dolny panel z suwakami.
                !currentRoute.value.isOnRoute<Screen.MonsterAnimationLab>() &&
                // Edytor talii pisze sie na pelnym ekranie - patrz composable<Screen.DeckEditor>.
                !currentRoute.value.isOnRoute<Screen.DeckEditor>() &&
                !currentRoute.value.isOnRoute<Screen.Settings>() &&
                // Otwarta ksiazka w czytniku - tekst ma zajac caly ekran, bez paskow.
                !ReaderChrome.immersive
        }
    }
    // Gorny pasek znika w walce, w otwartej ksiazce i na ekranach, ktore maja WLASNY powrot i
    // wlasny naglowek (mapa swiata, budynki miasta, Swiatynia, Lab animacji).
    //
    // Te ekrany renderowaly sie ZANIM istnial globalny pasek: same siegaja pod pasek systemowy
    // (statusBarsPadding/systemBarsPadding) i same rysuja strzalke powrotu, tytul i (w miastie)
    // pasek zasobow. Gdy dolaczyl do nich globalny pasek, dostawaly go NAD swoim wlasnym naglowkiem
    // - dwa paski jeden pod drugim, a tlo (ktore ma isc pod OBA) zaczynalo sie dopiero pod nimi,
    // wiec wygladalo, jakby nie siegalo calego ekranu. Wylaczajac globalny pasek tutaj, kazdy z
    // tych ekranow wraca do bycia jedynym wlascicielem swojej gornej krawedzi - dokladnie tak, jak
    // Ustawienia i edytor talii juz sa (one nie maja wlasnego naglowka i licza na to, ze kontener
    // odsunie je od gory - stad NIE sa na tej liscie).
    val ownsTopChrome by remember {
        derivedStateOf {
            currentRoute.value.isOnRoute<Screen.StoryMap>() ||
                currentRoute.value.isOnRoute<Screen.Quarry>() ||
                currentRoute.value.isOnRoute<Screen.Forge>() ||
                currentRoute.value.isOnRoute<Screen.SummonCenter>() ||
                currentRoute.value.isOnRoute<Screen.Progress>() ||
                currentRoute.value.isOnRoute<Screen.MonsterAnimationLab>()
        }
    }
    val showTopBar by remember {
        derivedStateOf {
            !currentRoute.value.isOnRoute<Screen.BattleScreen>() &&
                !ownsTopChrome &&
                !ReaderChrome.immersive
        }
    }
    val onHome by remember {
        derivedStateOf { currentRoute.value.isOnRoute<Screen.Home>() }
    }
    val runSession = koinInject<RunSession>()
    // Ta sama instancja, ktora czyta [CampaignBoard] - "gdzie na mapie stoje" ma byc JEDNYM
    // stanem, dzielonym miedzy kropkami na scenie i plansza, ktora otworzy sie po START.
    val selection = koinInject<CampaignSelection>()
    // Karta postaci jest OKNEM nad biezacym ekranem, nie zakladka i nie osobnym wpisem w
    // nawigacji - zamkniecie ma oddawac gracza dokladnie tam, gdzie byl, a nie cofac back stack.
    var characterOpen by remember { mutableStateOf(false) }
    // Okna wyprawy tez sa NAD paskami (patrz nizej, przy [EquipmentDialog]), wiec ich stan musi
    // zyc tutaj, a nie w samej zakladce - inaczej pasek zakladek zaslanialby krzyzyk okna.
    var partySlot by remember { mutableStateOf<Int?>(null) }
    var materialOpen by remember { mutableStateOf(false) }
    // Material przezywa zmiane zakladki i powrot z walki - inaczej po kazdym wejsciu trzeba by
    // wybierac swoja talie od nowa.
    var materialKey by rememberSaveable { mutableStateOf(RunMaterial.Mixed.key) }
    // Tryb walki zyje tu, obok materialu, a nie w samej zakladce - przycisk BATTLE na talii w
    // Quizach przestawia oba naraz (talia jako material, tryb Endless) i dopiero wtedy przechodzi
    // na Walke.
    var fightMode by rememberSaveable { mutableStateOf(FightMode.Endless.name) }
    // Pasek systemowy + wysokosc paska gry - tyle miejsca u gory nalezy do chromu, nie do ekranu.
    val topBarSpace = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + TOP_BAR_HEIGHT
    Box(modifier = Modifier.fillMaxSize()) {
        // Tlo calej apki - jedna warstwa pod wszystkim, widoczna na kazdej zakladce
        // (rowniez pod bottom barem/top barem, ktore nakladaja sie na wierzchu).
        Image(
            painter = painterResource(Resources.Image.appBackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Przyciemnienie tla. Bez niego jasne chmury bija sie z trescia: zloty tekst na rozowej
        // chmurze traci kontrast, a panele w polprzezroczystym tle robia sie brudne.
        //
        // Dom dostaje LZEJSZY welon (mapa miasta JEST trescia i ma byc widoczna), a ekrany z
        // panelami i tekstem mocniejszy - to ten sam obraz w tle, ale inna rola.
        val scrim by animateFloatAsState(
            targetValue = if (onHome) HOME_SCRIM_ALPHA else CONTENT_SCRIM_ALPHA,
            // Ten sam czas, co przenikanie ekranow - welon i ekran maja zmieniac sie razem,
            // a nie jedno po drugim.
            animationSpec = tween(SCREEN_FADE_MS),
            label = "bgScrim"
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    // Gradient, nie plaski kolor: gora i dol (gdzie stoja paski i naglowki) sa
                    // ciemniejsze, srodek janiejszy - tlo dalej "oddycha".
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = scrim + 0.10f),
                        0.35f to Color.Black.copy(alpha = scrim),
                        0.75f to Color.Black.copy(alpha = scrim),
                        1f to Color.Black.copy(alpha = scrim + 0.12f)
                    )
                )
        )
        // Paski NIE sa czescia Scaffolda, tylko nakladka nad NavHostem. Gdy siedzialy w
        // Scaffoldzie, ich zniknicie zabieralo padding CALEJ zawartosci - a poniewaz dzieje sie
        // to w chwili rozpoczecia nawigacji, wychodzacy ekran przeskakiwal (wyspy na Home
        // "teleportowaly sie") jeszcze zanim zdazyl sie wygasic.
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                // Kontener NIE paddinguje juz zawartosci pod gorny pasek - robi to kazdy ekran u
                // siebie ([TabChrome] / [contentInsets]).
                //
                // Wczesniej padding byl TUTAJ i zalezal od AKTUALNEJ trasy. Trasa zmienia sie w
                // chwili rozpoczecia nawigacji, a wychodzacy ekran jest wtedy jeszcze widoczny -
                // wiec w polowie wlasnego wygaszania dostawal skok ukladu (wyjscie z Domu
                // spychalo cala mape o wysokosc paska w dol). To ten przeskok czytal sie jako
                // "miganie" przy przelaczaniu zakladek. Padding policzony w srodku ekranu nalezy
                // do jego wlasnej kompozycji i zyje tak dlugo jak on.
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                startDestination = Screen.Home,
                // Krotkie przenikanie, bez przerwy posrodku.
                //
                // Bylo tu wygaszanie i DOPIERO POTEM rozjasnianie (rozjasnianie mialo opoznienie
                // rowne czasowi wygaszania). Miedzy nimi byla klatka, w ktorej nie bylo juz nic -
                // i to jest dokladnie to "tu cos znika, tu cos sie pojawia": przelaczenie
                // zakladki trwalo 290 ms, z pusta dziura w srodku. Powodem tamtej sekwencji bylo
                // przeswitywanie ekranow przez siebie, ale przy [SCREEN_FADE_MS] tak krotkim
                // faza, w ktorej widac oba, to jedno mrugniecie - a zysk na plynnosci jest duzy.
                enterTransition = { fadeIn(tween(SCREEN_FADE_MS)) },
                exitTransition = { fadeOut(tween(SCREEN_FADE_MS)) },
                popEnterTransition = { fadeIn(tween(SCREEN_FADE_MS)) },
                popExitTransition = { fadeOut(tween(SCREEN_FADE_MS)) }
            ) {
                composable<Screen.Home> {
                    TabChrome(underBar = true, underTopBar = true) {
                    HomeScreen(
                        viewModel = playerViewModel,
                        contentPadding = PaddingValues(
                            top = topBarSpace,
                            bottom = bottomBarSpace()
                        ),
                        // Farba gornego paska konczy sie na jego plycie, a nie na calej
                        // zarezerwowanej wysokosci - portret zwiesza sie nizej, ale obok niego
                        // jest przezroczysto. Mapa ma siegac dokladnie dotad.
                        coverPadding = PaddingValues(
                            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() +
                                TOP_BAR_PLATE_HEIGHT,
                            bottom = bottomBarSpace()
                        ),
                        onOpenQuarry = {
                            navController.navigate(Screen.Quarry) {
                                launchSingleTop = true
                            }
                        },
                        onOpenForge = {
                            navController.navigate(Screen.Forge) {
                                launchSingleTop = true
                            }
                        },
                        onOpenSummonCenter = {
                            navController.navigate(Screen.SummonCenter) {
                                launchSingleTop = true
                            }
                        },
                        onOpenTemple = {
                            navController.navigate(Screen.Progress) { launchSingleTop = true }
                        },
                        onOpenKnightPreview = {
                            navController.navigate(Screen.KnightPreview) {
                                launchSingleTop = true
                            }
                        },
                        onOpenModularKnightPreview = {
                            navController.navigate(Screen.ModularKnightPreview) {
                                launchSingleTop = true
                            }
                        },
                        onOpenMonsterAnchorLab = {
                            navController.navigate(Screen.MonsterAnchorLab) {
                                launchSingleTop = true
                            }
                        },
                        onOpenMonsterAnimationLab = {
                            navController.navigate(Screen.MonsterAnimationLab) {
                                launchSingleTop = true
                            }
                        },
                        onOpenChestPreview = {
                            navController.navigate(Screen.ChestPreview) {
                                launchSingleTop = true
                            }
                        },
                    )
                    }
                }
                composable<Screen.Settings> {
                    SettingsScreen(
                        playerViewModel = playerViewModel,
                        onBack = { navController.popBackStack() },
                        onSignedOut = onSignedOut,
                        insets = contentInsets()
                    )
                }
                composable<Screen.Quarry> {
                    ProductionBuildingScreen(
                        building = ProductionBuilding.Quarry,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Screen.Forge> {
                    ForgeScreen(
                        playerViewModel = playerViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Screen.SummonCenter> {
                    SummonCenterScreen(
                        playerViewModel = playerViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Screen.Team> {
                    // Zakladka Walki to SCENA, nie panel sterowania: gdzie ide, kim ide i jeden
                    // wielki START. Wszystko, co sie przed walka USTAWIA, otwiera sie jako okno
                    // NA tej scenie - sklad ([PartySheet]) i material ([MaterialSheet]) - zeby
                    // gracz ani na chwile nie tracil z oczu tego, dokad idzie.
                    var boardOpen by remember { mutableStateOf(false) }

                    val playerState by playerViewModel.uiState.collectAsState()
                    val quizState by customQuizViewModel.uiState.collectAsState()
                    val tier = RunLadder.tierFor(playerState.stats.bestEndlessWave)
                    val material = RunMaterial.parse(materialKey)
                    val endless = FightMode.valueOf(fightMode) == FightMode.Endless
                    val eligibleWords = remember(playerState.savedWords) {
                        WordQuizBank.eligible(playerState.savedWords)
                    }

                    // Kropki pod przyciskiem naleza WYLACZNIE do kampanii - to postep przez
                    // KRAINE, a wyprawa nie ma krain. Liczymy je z tego samego wyboru, ktory
                    // widzi [CampaignBoard] (ta sama kraina i plansza, ktora otworzy sie po START),
                    // zeby kropki na scenie i plansza pod nimi zawsze mowily to samo.
                    val campaignWorld = selection.resolveWorld(playerState.storyProgress)
                    val campaignStageIndex = selection.resolveStage(playerState.storyProgress)
                    val campaignStage = StoryWorldCatalog.stageInWorld(campaignStageIndex)
                    // Wybrana plansza w calosci - to JA odpala START, wiec musi byc policzona tu,
                    // a nie dopiero w oknie wyboru plansz.
                    val campaignEntry = StoryStageCatalog.stageAt(campaignStageIndex, selection.tier)
                    val campaignClears = campaignEntry?.let {
                        StageRepeats.clearsOf(playerState.stageClears, it.tier, it.globalIndex)
                    } ?: 0

                    // Jeden launcher na cala zakladke: uzywa go i START, i panel plansz. Skrzynia,
                    // ktora po powrocie stawia, rysuje sie na koncu ekranu ([CampaignRewards]).
                    val campaignLauncher = rememberCampaignLauncher(playerViewModel) {
                        boardOpen = false
                        navController.navigate(Screen.BattleScreen) { launchSingleTop = true }
                    }
                    // Wyprawa pokazuje w tym miejscu poziom wybranego tematu zamiast kropek - null,
                    // gdy material to fiszki/wlasna talia/wszystkie tematy, bo wtedy nie ma czyjego
                    // poziomu pokazac.
                    val topicLevel = if (endless) {
                        material.topic()?.let { playerState.categoryLevels.levelOf(it) }
                    } else {
                        null
                    }

                    TabChrome(underBar = true, underTopBar = true) {
                        FightScreen(
                            contentPadding = PaddingValues(
                                top = topBarSpace,
                                bottom = bottomBarSpace()
                            ),
                            knight = playerState.knightEquipment,
                            look = playerState.heroLook,
                            place = if (endless) "Endless Depths" else campaignWorld.name,
                            stage = if (endless) {
                                ladderStageLabel(tier)
                            } else {
                                "Stage ${campaignEntry?.label ?: "-"}"
                            },
                            backdrop = LavaBackdrop,
                            // W kampanii w tej linijce nie ma czego pochwalic - jest za to jedyna
                            // rzecz, ktora naprawde zmienia decyzje: ile zaplaci TO wejscie. Lup za
                            // powtorke leci w dol ([StageRepeats]) i bez tego zdania wyglada to jak
                            // blad gry, a nie jak zasada, wedlug ktorej mozna wybrac inna plansze.
                            best = when {
                                !endless -> payoutLabel(campaignClears)
                                playerState.stats.bestEndlessWave > 0 ->
                                    "Best: wave ${playerState.stats.bestEndlessWave}"
                                else -> "No run finished yet"
                            },
                            companions = playerState.partyMonsters,
                            heroLevel = playerState.progress.level,
                            levelOf = playerState::levelOf,
                            campaignStagesCleared = campaignStage - 1,
                            campaignStagesTotal = campaignWorld.stageCount,
                            topicLevel = topicLevel,
                            mode = FightMode.valueOf(fightMode),
                            // O material pyta sie TYLKO wyprawa. W kampanii decyduje o nim plansza
                            // (jej kraina i tier), wiec pigulka nie ma tam czego przelaczac.
                            materialIcon = if (endless) material.icon() else null,
                            materialLabel = if (endless) material.label(quizState.decks) else null,
                            onMaterial = { materialOpen = true },
                            energy = EnergyRules.regenerated(
                                playerState.energy,
                                Clock.System.now().toEpochMilliseconds()
                            ).amount,
                            onMode = { fightMode = it.name },
                            onStart = {
                                when (FightMode.valueOf(fightMode)) {
                                    // Wyprawa startuje wprost z tego przycisku - dwadziescia fal,
                                    // boss na koncu, poziom wyliczony z rekordu ([RunLadder]).
                                    FightMode.Endless -> playerViewModel.enterBattle {
                                        val deck = material.deckIn(quizState.decks)
                                        val topic = material.topic()
                                        runSession.startNewRun(
                                            category = topic,
                                            difficulty = RunDifficulty.Normal,
                                            // Staly szczebel pytan zniknal z wyboru: w wyprawie
                                            // trudnosc rosnie razem z fala i poziomem tematu,
                                            // wiec null to tutaj jedyna uczciwa odpowiedz.
                                            quizDifficulty = null,
                                            customWords = if (material == RunMaterial.Words) {
                                                eligibleWords
                                            } else {
                                                emptyList()
                                            },
                                            customDeck = deck,
                                            // Poziom opanowania tematu steruje trudnoscia startowa
                                            // pytan i mnoznikiem nagrod - patrz [CategoryMastery].
                                            categoryLevel = topic
                                                ?.let { playerState.categoryLevels.levelOf(it) }
                                                ?: CategoryMastery.FIRST_LEVEL,
                                            // Poziom drabinki decyduje o sile wrogow od PIERWSZEJ
                                            // fali - patrz [RunLadder].
                                            ladderTier = tier
                                        )
                                        runSession.healParty()
                                        runSession.pendingBattle = BattleRequest(
                                            kind = BattleKind.Normal,
                                            waves = RunLadder.WAVES_PER_TIER,
                                            seed = Random.nextLong(),
                                            isEndless = true
                                        )
                                        navController.navigate(Screen.BattleScreen) {
                                            launchSingleTop = true
                                        }
                                    }
                                    // Kampania rusza tak samo od razu: START = walka na wybranej
                                    // planszy. Wybor planszy jest OSOBNYM wejsciem (tabliczka
                                    // miejsca / kropki postepu), bo robi sie go raz na kilka
                                    // zejsc, a grac chce sie za kazdym razem.
                                    // Gdy kampania na tym tierze jest juz skonczona, planszy nie
                                    // ma czym odpalic - wtedy START otwiera okno plansz, w ktorym
                                    // stoi jedyne zdanie, jakie ma tu sens ("wybierz wyzszy tier").
                                    FightMode.Campaign -> campaignEntry
                                        ?.let(campaignLauncher::launch)
                                        ?: run { boardOpen = true }
                                }
                            },
                            onOpenStages = if (endless) null else ({ boardOpen = true }),
                            // Tapniecie miniatury otwiera okno druzyny NA TYM miejscu - gracz
                            // celowal w konkretny slot, wiec to on ma byc zaznaczony.
                            onCompanion = { index -> partySlot = index },
                            onOpenParty = { partySlot = -1 },
                            onOpenMap = {
                                navController.navigate(Screen.StoryMap) { launchSingleTop = true }
                            }
                        )
                    }

                    if (boardOpen) {
                        CampaignBoard(
                            viewModel = playerViewModel,
                            launcher = campaignLauncher,
                            onClose = { boardOpen = false },
                            onOpenMap = {
                                boardOpen = false
                                navController.navigate(Screen.StoryMap) { launchSingleTop = true }
                            }
                        )
                    }

                    // Na SAMYM koncu zakladki, bo skrzynia ma lezec nad scena i nad panelem plansz.
                    CampaignRewards(launcher = campaignLauncher, viewModel = playerViewModel)
                }
                composable<Screen.StoryMap> {
                    WorldMapScreen(
                        playerViewModel = playerViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Screen.Growth> {
                    GrowthScreen(
                        tabInsets = tabChromeInsets(),
                        onRewardClaimed = playerViewModel::refresh,
                        onOpenVocabulary = {
                            navController.navigate(Screen.Vocabulary) { launchSingleTop = true }
                        }
                    )
                }
                composable<Screen.Vocabulary> {
                    // Slowka to ekran PELNY, nie zakladka - dostaje wiec same marginesy systemowe.
                    // Wczesniej dostawal tabChromeInsets i rezerwowal u dolu miejsce na pasek
                    // zakladek, ktorego na tym ekranie w ogole nie ma.
                    VocabularyScreen(
                        onBack = { navController.popBackStack() },
                        insets = contentInsets()
                    )
                }
                composable<Screen.DeckEditor> { entry ->
                    val route: Screen.DeckEditor = entry.toRoute()
                    // Ekran PELNY, jak Slowka: pisanie pytan potrzebuje calej wysokosci, a pasek
                    // zakladek zjadalby klawiature na malych telefonach.
                    DeckEditorScreen(
                        viewModel = customQuizViewModel,
                        deckId = route.deckId,
                        startWithAi = route.startWithAi,
                        onBack = { navController.popBackStack() },
                        insets = contentInsets()
                    )
                }
                composable<Screen.Shop> { TabChrome(underBar = true, underTopBar = true) { ShopScreen(viewModel = playerViewModel) } }
                composable<Screen.QuizStudio> {
                    TabChrome(underBar = true) {
                        QuizStudioScreen(
                            viewModel = customQuizViewModel,
                            onEditDeck = { deckId, startWithAi ->
                                navController.navigate(Screen.DeckEditor(deckId, startWithAi)) {
                                    launchSingleTop = true
                                }
                            },
                            // Talia idzie do walki tak samo, jak z okna materialu na Walce: staje sie
                            // materialem wyprawy, a gracz laduje przed START z ta talia na pigulce.
                            onPlayDeck = { deckId ->
                                materialKey = RunMaterial.Deck(deckId).key
                                fightMode = FightMode.Endless.name
                                navController.navigate(BottomBarDestination.Fight.route) {
                                    launchSingleTop = true
                                    popUpTo<Screen.Home> {
                                        saveState = true
                                        inclusive = false
                                    }
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
                composable<Screen.Progress> {
                    // Swiatynia jest PELNYM ekranem z powrotem, a nie zakladka - wchodzi sie tu
                    // z wyspy na mapie Domu.
                    ProgressScreen(
                        viewModel = playerViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Screen.KnightPreview> {
                    KnightPreviewScreen(
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable<Screen.ModularKnightPreview> {
                    ModularKnightPreviewScreen(
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable<Screen.MonsterAnimationLab> {
                    MonsterAnimationLabScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Screen.MonsterAnchorLab> {
                    MonsterAnchorLabScreen(
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable<Screen.ChestPreview> {
                    ChestPreviewScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Screen.BattleScreen> {
                    BattleScreen(
                        // Koniec walki (wygranej lub nie) - wracamy na mape lochu. Walka rozliczyla
                        // sie wlasnym zapisem (XP, poziom, surowce, statystyki), a PlayerViewModel
                        // zyje przez cala zakladke i o tym nie wie - bez odswiezenia zakladki Postac
                        // i Postep pokazywalyby stan sprzed walki az do restartu aplikacji.
                        onLeaveBattle = {
                            playerViewModel.refresh()
                            navController.popBackStack()
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = showTopBar,
                enter = fadeIn(tween(BAR_FADE_MS)),
                exit = fadeOut(tween(BAR_FADE_MS)),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                val playerState by playerViewModel.uiState.collectAsState()
                GameTopBar(
                    name = playerState.name,
                    level = playerState.progress.level,
                    power = GearPower.of(playerState.combatStats),
                    money = playerState.money,
                    gems = playerState.gems,
                    energy = playerState.energy,
                    knight = playerState.knightEquipment,
                    look = playerState.heroLook,
                    onOpenCharacter = { characterOpen = true },
                    onOpenSettings = {
                        navController.navigate(Screen.Settings) { launchSingleTop = true }
                    },
                    onAddGems = {
                        playerViewModel.requestShopTab("Gems")
                        navController.navigate(Screen.Shop) { launchSingleTop = true }
                    },
                    onAddGold = {
                        playerViewModel.requestShopTab("Supplies")
                        navController.navigate(Screen.Shop) { launchSingleTop = true }
                    },
                    onAddEnergy = playerViewModel::showEnergyRefill
                )
            }

            AnimatedVisibility(
                visible = showBars,
                // Sam fade, bez slide - slide przycina wystajacy nad pasek przycisk "Walcz".
                enter = fadeIn(tween(BAR_FADE_MS)),
                exit = fadeOut(tween(BAR_FADE_MS)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                NavBottomBar(
                    selected = selectedDestination,
                    onSelect = { destination ->
                        navController.navigate(destination.route) {
                            launchSingleTop = true
                            popUpTo<Screen.Home> {
                                saveState = true
                                inclusive = false
                            }
                            restoreState = true
                        }
                    }
                )
            }
        }

        // Okna rysuja sie NAD paskami - razem z przyciemnieniem, ktore je zakrywa. Inaczej portret
        // w gornym pasku zostawalby klikalny pod wlasnym oknem, a dolny pasek zjadalby krzyzyk.
        //
        // Stan zbiera sie dopiero TUTAJ, a nie na wierzchu calego grafu: dopoki zadne z okien nie
        // jest otwarte, nic sie nie subskrybuje i zmiana stanu gracza nie przeklada calej
        // nawigacji.
        if (partySlot != null || materialOpen) {
            val sheetState by playerViewModel.uiState.collectAsState()
            val sheetQuiz by customQuizViewModel.uiState.collectAsState()

            partySlot?.let { slot ->
                PartySheet(
                    party = sheetState.partyMonsters,
                    reserve = sheetState.companionInventory,
                    heroLevel = sheetState.progress.level,
                    levelOf = sheetState::levelOf,
                    starsOf = sheetState::starsOf,
                    initialSlot = slot,
                    freeSummonReady = FreeSummon.anyReady(
                        sheetState.freeSummonClaims,
                        Clock.System.now().toEpochMilliseconds()
                    ),
                    onPlace = playerViewModel::placeInParty,
                    onRemove = playerViewModel::removeFromParty,
                    onSummon = {
                        partySlot = null
                        navController.navigate(Screen.SummonCenter) { launchSingleTop = true }
                    },
                    onClose = { partySlot = null }
                )
            }

            if (materialOpen) {
                MaterialSheet(
                    selected = RunMaterial.parse(materialKey),
                    decks = sheetQuiz.decks,
                    decksLoading = sheetQuiz.loading,
                    eligibleWordCount = WordQuizBank.eligible(sheetState.savedWords).size,
                    levels = sheetState.categoryLevels,
                    onSelect = { picked ->
                        materialKey = picked.key
                        // Wybor JEST zamknieciem okna - lista sluzy do wybrania jednej rzeczy,
                        // wiec osobne "zatwierdz" byloby tylko drugim tapnieciem w to samo.
                        materialOpen = false
                    },
                    onEditDeck = { deckId ->
                        materialOpen = false
                        navController.navigate(Screen.DeckEditor(deckId, false)) {
                            launchSingleTop = true
                        }
                    },
                    onOpenQuizzes = {
                        materialOpen = false
                        navController.navigate(Screen.QuizStudio) { launchSingleTop = true }
                    },
                    onBossTest = {
                        materialOpen = false
                        runSession.startNewRun(difficulty = RunDifficulty.Normal)
                        runSession.healParty()
                        runSession.pendingBattle = BattleRequest(
                            kind = BattleKind.Boss,
                            waves = 1,
                            seed = Random.nextLong(),
                            isEndless = false,
                            enemyHpMultiplier = 30f
                        )
                        navController.navigate(Screen.BattleScreen) { launchSingleTop = true }
                    },
                    onClose = { materialOpen = false }
                )
            }
        }

        if (characterOpen) {
            EquipmentDialog(
                viewModel = playerViewModel,
                onClose = { characterOpen = false }
            )
        }

        // Doladowanie energii - z kazdego miejsca, ktore probowalo wejsc do walki bez energii,
        // i z "+" przy energii w gornym pasku. Jedno okno dla calej gry; stan czyta osobna
        // funkcja, zeby zmiana gracza nie przekladala calego grafu.
        EnergyRefillHost(
            playerViewModel = playerViewModel,
            onGetGems = {
                playerViewModel.requestShopTab("Gems")
                navController.navigate(Screen.Shop) { launchSingleTop = true }
            }
        )
    }
}

@Composable
private fun EnergyRefillHost(playerViewModel: PlayerViewModel, onGetGems: () -> Unit) {
    val state by playerViewModel.uiState.collectAsState()
    if (!state.energyPrompt) return
    EnergyRefillDialog(
        energy = state.energy,
        gems = state.gems,
        refillsToday = playerViewModel.energyRefillsToday(),
        onRefill = { playerViewModel.buyEnergyRefill {} },
        onGetGems = {
            playerViewModel.dismissEnergyRefill()
            onGetGems()
        },
        onClose = playerViewModel::dismissEnergyRefill
    )
}

/**
 * Wysokosc dolnego paska + maly odstep u gory. Stale, a nie mierzone - padding ekranow NIE MOZE
 * zalezec od tego, czy pasek jest akurat widoczny, bo wtedy kazde wejscie na ekran pelnoekranowy
 * przeklada layout ekranu, ktory wlasnie z niego wychodzi.
 *
 * Gornego paska z tytulem zakladki juz nie ma - byl czystym napisem ("Postep", "Dom"...) i zjadal
 * wysokosc. Zostaje tylko waski odstep, zeby tresc nie zaczynala sie tuz pod statusbarem.
 */
private val TabTopGap = 8.dp
/** Musi isc w parze z wysokoscia plyty w [NavBottomBar] - to o nia odsuwa sie tresc zakladek. */
private val BottomBarHeight = 74.dp

/** Welon nad tlem na ekranie Dom - lekki, bo mapa miasta jest tu trescia, nie dekoracja. */
private const val HOME_SCRIM_ALPHA = 0.18f

/** Welon na ekranach z panelami i tekstem - mocniejszy, zeby tresc miala spokojne tlo. */
private const val CONTENT_SCRIM_ALPHA = 0.42f

/**
 * Czas przenikania miedzy ekranami. Jedna liczba dla obu stron, bo przenikanie ma byc symetryczne;
 * dwie osobne (wyjscie/wejscie) robily z tego sekwencje z przerwa.
 */
private const val SCREEN_FADE_MS = 120
private const val BAR_FADE_MS = 160

/**
 * Marginesy pod paski dla ekranow zakladek - stale przez cale zycie ekranu.
 * Ekrany pelnoekranowe (walka, budynki miasta, mapa, otwarty czytnik) nie uzywaja tego wcale.
 */
@Composable
private fun TabChrome(
    /**
     * Czy tresc ma wjezdzac POD dolny pasek. Ekrany przewijane tak wlasnie wygladaja najlepiej:
     * lista chowa sie za brazowa plyta zamiast urywac sie kilkanascie dp nad nia. Taki ekran musi
     * jednak zakonczyc swoja przewijana tresc pustym ogonem o wysokosci [bottomBarSpace] - inaczej
     * ostatniej pozycji nie da sie wysunac spod paska.
     */
    underBar: Boolean = false,
    /**
     * Czy tresc ma siegac az do GORNEJ krawedzi ekranu. Pelnoekranowa scena (zakladka Walki) musi:
     * inaczej zostaje nad nia cienki pasek tla aplikacji - ten sam odstep, ktory zwyklym zakladkom
     * daje oddech pod paskiem gry, tam wyglada jak niedorysowany czubek obrazka.
     */
    underTopBar: Boolean = false,
    content: @Composable () -> Unit
) {
    val insets = tabChromeInsets()
    CompositionLocalProvider(LocalBottomBarSpace provides insets.calculateBottomPadding()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (underTopBar) 0.dp else insets.calculateTopPadding(),
                    bottom = if (underBar) 0.dp else insets.calculateBottomPadding()
                )
        ) {
            content()
        }
    }
}

/**
 * Marginesy ekranow PELNYCH (Slowka, Ustawienia, edytor talii): gora nalezy juz do kontenera
 * NavHosta (pasek systemowy + pasek gry), wiec zostaje sam dol.
 */
@Composable
private fun contentInsets(): PaddingValues {
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    return PaddingValues(
        // Te ekrany nie maja wlasnego naglowka, wiec gorny pasek gry stoi nad nimi - i to one
        // musza sie od niego odsunac. Wczesniej robil to za nie kontener NavHosta.
        top = systemBars.calculateTopPadding() + TOP_BAR_HEIGHT,
        bottom = systemBars.calculateBottomPadding()
    )
}

/** To samo co [TabChrome], ale jako gotowe wartosci - dla ekranow, ktore same decyduja, gdzie je nalozyc. */
@Composable
private fun tabChromeInsets(): PaddingValues {
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    return PaddingValues(
        // Pelna gora: pasek systemowy + pasek gry + odstep. Liczy sie TUTAJ, a nie w kontenerze
        // NavHosta, bo tam zalezalo to od aktualnej trasy i przestawialo sie pod wychodzacym
        // ekranem w trakcie jego wygaszania - patrz komentarz przy [NavHost].
        top = systemBars.calculateTopPadding() + TOP_BAR_HEIGHT + TabTopGap,
        bottom = systemBars.calculateBottomPadding() + BottomBarHeight
    )
}

private inline fun <reified T : Any> androidx.navigation.NavBackStackEntry?.isOnRoute(): Boolean {
    return this?.destination
        ?.hierarchy
        ?.any { destination -> destination.hasRoute(T::class) } == true
}
