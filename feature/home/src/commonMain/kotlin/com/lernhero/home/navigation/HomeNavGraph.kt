package com.lernhero.home.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.lernhero.home.story.WorldMapScreen
import com.lernhero.home.equipment.EquipmentScreen
import com.lernhero.home.fitness.FitnessScreen
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.progress.ProgressScreen
import com.lernhero.reader.ReaderChrome
import com.lernhero.reader.ReadingScreen
import com.lernhero.home.shop.ShopScreen
import com.lernhero.home.team.TeamScreen
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontPixel
import com.lernhero.shared.theme.FontSize
import com.lernhero.shared.navigation.Screen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNavGraph() {

    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState()
    // Rozwiazane RAZ tutaj (nie w kazdym ekranie osobno) - Home/Team/Equipment/Shop maja dzielic
    // JEDEN stan gracza, zeby zakup w sklepie od razu byl widoczny w ekwipunku/druzynie, bez
    // koniecznosci wyjscia i wejscia z powrotem na ekran.
    val playerViewModel = koinViewModel<PlayerViewModel>()

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
                currentRoute.value.isOnRoute<Screen.Equipment>() -> BottomBarDestination.Equipment
                currentRoute.value.isOnRoute<Screen.Fitness>() -> BottomBarDestination.Fitness
                currentRoute.value.isOnRoute<Screen.Books>() -> BottomBarDestination.Books
                currentRoute.value.isOnRoute<Screen.Progress>() -> BottomBarDestination.Progress
                currentRoute.value.isOnRoute<Screen.Shop>() -> BottomBarDestination.Shop
                else -> BottomBarDestination.Home
            }
        }
    }
    val topBarTitle by remember {
        derivedStateOf {
            when {
                currentRoute.value.isOnRoute<Screen.KnightPreview>() -> "Knight Lab"
                currentRoute.value.isOnRoute<Screen.ModularKnightPreview>() -> "Knight Preview"
                currentRoute.value.isOnRoute<Screen.MonsterAnchorLab>() -> "Monster Anchor Lab"
                else -> selectedDestination.title
            }
        }
    }
    val showBars by remember {
        derivedStateOf {
            !currentRoute.value.isOnRoute<Screen.BattleScreen>() &&
                !currentRoute.value.isOnRoute<Screen.StoryMap>() &&
                !currentRoute.value.isOnRoute<Screen.Sawmill>() &&
                !currentRoute.value.isOnRoute<Screen.Quarry>() &&
                !currentRoute.value.isOnRoute<Screen.Forge>() &&
                !currentRoute.value.isOnRoute<Screen.SummonCenter>() &&
                // Otwarta ksiazka w czytniku - tekst ma zajac caly ekran, bez paskow.
                !ReaderChrome.immersive
        }
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
                        onOpenSawmill = {
                            navController.navigate(Screen.Sawmill) {
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
                        }
                    )
                    }
                }
                composable<Screen.Sawmill> {
                    ProductionBuildingScreen(
                        building = ProductionBuilding.Sawmill,
                        onBack = { navController.popBackStack() }
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
                composable<Screen.Equipment> { TabChrome { EquipmentScreen(viewModel = playerViewModel) } }
                composable<Screen.Fitness> { TabChrome { FitnessScreen() } }
                // Czytelnia dostaje marginesy tylko na biblioteke - otwarta ksiazka ma isc na caly
                // ekran. Owiniecie calego ReadingScreen w TabChrome zmienialoby strukture drzewa
                // przy otwarciu ksiazki i gubilo jej stan.
                composable<Screen.Books> { ReadingScreen(libraryInsets = tabChromeInsets()) }
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
                enter = fadeIn(tween(BAR_FADE_MS)) + slideInVertically { -it },
                exit = fadeOut(tween(BAR_FADE_MS)) + slideOutVertically { -it },
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        // Sam fade, bez domyslnego skalowania i animacji rozmiaru - tytul zmienia
                        // dlugosc miedzy zakladkami, a domyslny SizeTransform rozpychal wtedy pasek.
                        AnimatedContent(
                            targetState = topBarTitle,
                            transitionSpec = { fadeIn(tween(BAR_FADE_MS)) togetherWith fadeOut(tween(BAR_FADE_MS)) }
                        ) { title ->
                            Text(
                                text = title,
                                fontFamily = FontPixel(),
                                fontSize = FontSize.MEDIUM,
                                color = AppPalette.Gold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = AppPalette.Gold
                    )
                )
            }

            AnimatedVisibility(
                visible = showBars,
                enter = fadeIn(tween(BAR_FADE_MS)) + slideInVertically { it },
                exit = fadeOut(tween(BAR_FADE_MS)) + slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                NavBottomBar(
                    modifier = Modifier.navigationBarsPadding(),
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
 * Wysokosci paskow. Stale, a nie mierzone - padding ekranow NIE MOZE zalezec od tego, czy paski
 * sa akurat widoczne, bo wtedy kazde wejscie na ekran pelnoekranowy przeklada layout ekranu,
 * ktory wlasnie z niego wychodzi.
 */
private val TopBarHeight = 64.dp
private val BottomBarHeight = 76.dp

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
        top = systemBars.calculateTopPadding() + TopBarHeight,
        bottom = systemBars.calculateBottomPadding() + BottomBarHeight
    )
}

private inline fun <reified T : Any> androidx.navigation.NavBackStackEntry?.isOnRoute(): Boolean {
    return this?.destination
        ?.hierarchy
        ?.any { destination -> destination.hasRoute(T::class) } == true
}
