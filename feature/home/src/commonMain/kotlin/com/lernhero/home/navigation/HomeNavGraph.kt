package com.lernhero.home.navigation

import androidx.compose.animation.AnimatedContent
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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lernhero.domain.run.BattleRequest
import com.lernhero.domain.run.RunSession
import com.lernhero.game.battle.BattleScreen
import com.lernhero.home.HomeScreen
import com.lernhero.home.dev.KnightPreviewScreen
import com.lernhero.home.dev.ModularKnightPreviewScreen
import com.lernhero.home.dungeon.DungeonCrawlScreen
import com.lernhero.home.equipment.EquipmentScreen
import com.lernhero.home.fitness.FitnessScreen
import com.lernhero.home.reading.ReaderChrome
import com.lernhero.home.reading.ReadingScreen
import com.lernhero.home.shop.ShopScreen
import com.lernhero.home.team.TeamScreen
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontPixel
import com.lernhero.shared.theme.FontSize
import com.lernhero.shared.navigation.Screen
import kotlin.random.Random
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNavGraph() {

    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState()

    val selectedDestination by remember {
        derivedStateOf {
            when {
                currentRoute.value.isOnRoute<Screen.Home>() -> BottomBarDestination.Home
                currentRoute.value.isOnRoute<Screen.Team>() -> BottomBarDestination.Fight
                currentRoute.value.isOnRoute<Screen.Equipment>() -> BottomBarDestination.Equipment
                currentRoute.value.isOnRoute<Screen.Fitness>() -> BottomBarDestination.Fitness
                currentRoute.value.isOnRoute<Screen.Books>() -> BottomBarDestination.Books
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
                else -> selectedDestination.title
            }
        }
    }
    val showBars by remember {
        derivedStateOf {
            !currentRoute.value.isOnRoute<Screen.BattleScreen>() &&
                !currentRoute.value.isOnRoute<Screen.DungeonCrawl>() &&
                // Otwarta ksiazka w czytniku - tekst ma zajac caly ekran, bez paskow.
                !ReaderChrome.immersive
        }
    }
    val runSession = koinInject<RunSession>()
    Scaffold(
        containerColor = AppPalette.Background,
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
                        containerColor = AppPalette.Background,
                        scrolledContainerColor = AppPalette.Background,
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
                        onOpenFight = {
                            navController.navigate(Screen.Team) {
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
                        onOpenEquipment = {
                            navController.navigate(Screen.Equipment) {
                                launchSingleTop = true
                            }
                        },
                        onOpenShop = {
                            navController.navigate(Screen.Shop) {
                                launchSingleTop = true
                            }
                        },
                        onOpenFitness = {
                            navController.navigate(Screen.Fitness) {
                                launchSingleTop = true
                            }
                        },
                        onOpenBooks = {
                            navController.navigate(Screen.Books) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<Screen.Team> {
                    TeamScreen(
                        onStartBattle = {
                            navController.navigate(Screen.DungeonCrawl) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<Screen.DungeonCrawl> {
                    DungeonCrawlScreen(
                        onFinished = { navController.popBackStack() },
                        onEnterCombat = { kind ->
                            // 1-3 fale na starcie z korytarza - zeby walka faktycznie sie konczyla
                            // i dalo sie wrocic do chodzenia (patrz BattleViewModel.finishVictory).
                            runSession.pendingBattle = BattleRequest(kind = kind, waves = Random.nextInt(1, 4), seed = Random.nextLong())
                            navController.navigate(Screen.BattleScreen)
                        }
                    )
                }
                composable<Screen.Equipment> { EquipmentScreen() }
                composable<Screen.Fitness> { FitnessScreen() }
                composable<Screen.Books> { ReadingScreen() }
                composable<Screen.Shop> { ShopScreen() }
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
                composable<Screen.BattleScreen> {
                    BattleScreen(
                        // Koniec walki (wygranej lub nie) - wracamy na mape lochu.
                        onLeaveBattle = { navController.popBackStack() }
                    )
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
