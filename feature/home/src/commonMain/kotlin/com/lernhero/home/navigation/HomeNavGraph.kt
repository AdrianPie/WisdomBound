package com.lernhero.home.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
        // Tlo calej apki - jedna warstwa pod Scaffoldem, widoczna na kazdej zakladce
        // (rowniez pod bottom barem/top barem, ktore nakladaja sie na wierzchu).
        Image(
            painter = painterResource(Resources.Image.appBackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = if (showBars) {
            {
                CenterAlignedTopAppBar(
                    title = {
                        AnimatedContent(
                            targetState = topBarTitle
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
        } else {
            { }
        },
        bottomBar = if (showBars) {
            {
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
        } else {
            { }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (showBars) {
                        Modifier.padding(paddingValues)
                    } else {
                        Modifier
                    }
                )
        ) {
            NavHost(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                startDestination = Screen.Home
            ) {
                composable<Screen.Home> {
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
                composable<Screen.StoryMap> {
                    WorldMapScreen(
                        playerViewModel = playerViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Screen.Equipment> { EquipmentScreen(viewModel = playerViewModel) }
                composable<Screen.Fitness> { FitnessScreen() }
                composable<Screen.Books> { ReadingScreen() }
                composable<Screen.Shop> { ShopScreen(viewModel = playerViewModel) }
                composable<Screen.Progress> { ProgressScreen(viewModel = playerViewModel) }
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

        }

    }

    }
}

private inline fun <reified T : Any> androidx.navigation.NavBackStackEntry?.isOnRoute(): Boolean {
    return this?.destination
        ?.hierarchy
        ?.any { destination -> destination.hasRoute(T::class) } == true
}
