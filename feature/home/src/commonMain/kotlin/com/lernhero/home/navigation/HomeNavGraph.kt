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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.lernhero.home.dev.KnightPreviewScreen
import com.lernhero.home.dev.ModularKnightPreviewScreen
import com.lernhero.home.dev.MonsterAnchorLabScreen
import com.lernhero.home.dev.MonsterAnimationLabScreen
import com.lernhero.home.story.WorldMapScreen
import com.lernhero.home.talent.TalentScreen
import com.lernhero.home.equipment.EquipmentScreen
import com.lernhero.home.growth.GrowthScreen
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.quiz.CustomQuizViewModel
import com.lernhero.home.quiz.DeckEditorScreen
import com.lernhero.home.settings.SettingsScreen
import com.lernhero.home.progress.ProgressScreen
import com.lernhero.reader.ReaderChrome
import com.lernhero.reader.VocabularyScreen
import com.lernhero.home.shop.ShopScreen
import com.lernhero.home.team.TeamScreen
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
                currentRoute.value.isOnRoute<Screen.Progress>() -> BottomBarDestination.Progress
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
                !currentRoute.value.isOnRoute<Screen.Talents>() &&
                // Karta postaci wchodzi z naglowka na Dom - jest podekranem, nie zakladka.
                !currentRoute.value.isOnRoute<Screen.Equipment>() &&
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
    val onHome by remember {
        derivedStateOf { currentRoute.value.isOnRoute<Screen.Home>() }
    }
    val runSession = koinInject<RunSession>()
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
            animationSpec = tween(SCREEN_FADE_IN_MS),
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
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                startDestination = Screen.Home,
                // Wygaszanie konczy sie, ZANIM zacznie sie rozjasnianie (stad delay rowny
                // czasowi wyjscia). Domyslny crossfade pokazuje oba ekrany naraz, a ze wszystkie
                // sa przezroczyste (tlo apki to jedna warstwa pod spodem), poprzedni ekran
                // przeswitywal przez nowy.
                enterTransition = { fadeIn(tween(SCREEN_FADE_IN_MS, delayMillis = SCREEN_FADE_OUT_MS)) },
                exitTransition = { fadeOut(tween(SCREEN_FADE_OUT_MS)) },
                popEnterTransition = { fadeIn(tween(SCREEN_FADE_IN_MS, delayMillis = SCREEN_FADE_OUT_MS)) },
                popExitTransition = { fadeOut(tween(SCREEN_FADE_OUT_MS)) }
            ) {
                composable<Screen.Home> {
                    TabChrome {
                    HomeScreen(
                        viewModel = playerViewModel,
                        onOpenCharacter = {
                            navController.navigate(Screen.Equipment) {
                                launchSingleTop = true
                            }
                        },
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
                        onOpenSettings = {
                            navController.navigate(Screen.Settings) {
                                launchSingleTop = true
                            }
                        }
                    )
                    }
                }
                composable<Screen.Settings> {
                    SettingsScreen(
                        playerViewModel = playerViewModel,
                        onBack = { navController.popBackStack() },
                        onSignedOut = onSignedOut,
                        insets = WindowInsets.systemBars.asPaddingValues()
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
                    TabChrome {
                        TeamScreen(
                            viewModel = playerViewModel,
                            quizViewModel = customQuizViewModel,
                            onEditDeck = { deckId, startWithAi ->
                                navController.navigate(Screen.DeckEditor(deckId, startWithAi)) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenMap = {
                                navController.navigate(Screen.StoryMap) {
                                    launchSingleTop = true
                                }
                            },
                            onEnterBattle = {
                                navController.navigate(Screen.BattleScreen) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
                composable<Screen.StoryMap> {
                    WorldMapScreen(
                        playerViewModel = playerViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Screen.Equipment> {
                    EquipmentScreen(
                        viewModel = playerViewModel,
                        onBack = { navController.popBackStack() },
                        onOpenTalents = {
                            navController.navigate(Screen.Talents) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<Screen.Talents> {
                    TalentScreen(
                        viewModel = playerViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                // Rozwoj = Czytelnia + Trening w dwoch podzakladkach. NIE owijamy w TabChrome:
                // przy otwartej ksiazce czytnik idzie na caly ekran, a staly padding TabChrome
                // zmienialby strukture drzewa i gubil jego stan. GrowthScreen sam rozdziela
                // marginesy miedzy przelacznik podzakladek a tresc.
                composable<Screen.Growth> {
                    GrowthScreen(
                        tabInsets = tabChromeInsets(),
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
                        insets = WindowInsets.systemBars.asPaddingValues()
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
                        insets = WindowInsets.systemBars.asPaddingValues()
                    )
                }
                composable<Screen.Shop> { TabChrome { ShopScreen(viewModel = playerViewModel) } }
                composable<Screen.Progress> { TabChrome { ProgressScreen(viewModel = playerViewModel) } }
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
    }
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
private val BottomBarHeight = 70.dp

/** Welon nad tlem na ekranie Dom - lekki, bo mapa miasta jest tu trescia, nie dekoracja. */
private const val HOME_SCRIM_ALPHA = 0.18f

/** Welon na ekranach z panelami i tekstem - mocniejszy, zeby tresc miala spokojne tlo. */
private const val CONTENT_SCRIM_ALPHA = 0.42f

private const val SCREEN_FADE_OUT_MS = 90
private const val SCREEN_FADE_IN_MS = 200
private const val BAR_FADE_MS = 160

/**
 * Marginesy pod paski dla ekranow zakladek - stale przez cale zycie ekranu.
 * Ekrany pelnoekranowe (walka, budynki miasta, mapa, otwarty czytnik) nie uzywaja tego wcale.
 */
@Composable
private fun TabChrome(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(tabChromeInsets())
    ) {
        content()
    }
}

/** To samo co [TabChrome], ale jako gotowe wartosci - dla ekranow, ktore same decyduja, gdzie je nalozyc. */
@Composable
private fun tabChromeInsets(): PaddingValues {
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    return PaddingValues(
        top = systemBars.calculateTopPadding() + TabTopGap,
        bottom = systemBars.calculateBottomPadding() + BottomBarHeight
    )
}

private inline fun <reified T : Any> androidx.navigation.NavBackStackEntry?.isOnRoute(): Boolean {
    return this?.destination
        ?.hierarchy
        ?.any { destination -> destination.hasRoute(T::class) } == true
}
