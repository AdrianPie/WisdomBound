package com.lernhero.home.navigation

import com.lernhero.shared.i18n.AppStrings
import com.lernhero.shared.navigation.Screen
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.DrawableResource

/**
 * Pozycje dolnego paska. [icon] to grafika (PLACEHOLDER - patrz [Resources.NavIcon]).
 *
 * Kolejnosc jest celowa: "Walcz" stoi w SRODKU - to najwazniejsze miejsce w grze, reszta to
 * zaplecze. Wyroznienie w pasku nie nalezy juz jednak do niej na stale: pomaranczowy, uniesiony
 * kafel wedruje za OTWARTA zakladka (patrz [NavBottomBar]), bo pasek ma mowic "gdzie jestem",
 * a nie "co jest wazne".
 *
 * Karta postaci NIE ma tu wlasnej zakladki - otwiera sie ja portretem z gornego paska gry.
 */
enum class BottomBarDestination(
    val icon: DrawableResource,
    /** Resolved against the active [AppStrings] at render time - see [title]. */
    private val titleOf: (AppStrings) -> String,
    val route: Screen
) {
    Home(
        icon = Resources.NavIcon.Home,
        titleOf = { it.navHome },
        route = Screen.Home
    ),
    /** Czytanie ksiazek + trening z urzadzenia. Podzakladki w srodku, patrz GrowthScreen. */
    Growth(
        icon = Resources.NavIcon.Growth,
        titleOf = { it.navGrowth },
        route = Screen.Growth
    ),
    Fight(
        icon = Resources.NavIcon.Fight,
        titleOf = { it.navFight },
        route = Screen.Team
    ),
    /**
     * Wlasne quizy. Dawniej stal tu Postep - ale podsumowanie osiagniec oglada sie raz na jakis
     * czas, a pytania pisze sie regularnie, wiec to ono zaslugiwalo na miejsce w pasku. Postep
     * przeniosl sie do Swiatyni na mapie Domu.
     */
    Quizzes(
        icon = Resources.NavIcon.Progress,
        titleOf = { it.navQuizzes },
        route = Screen.QuizStudio
    ),
    Shop(
        icon = Resources.NavIcon.Shop,
        titleOf = { it.navShop },
        route = Screen.Shop
    );

    fun title(strings: AppStrings): String = titleOf(strings)
}
