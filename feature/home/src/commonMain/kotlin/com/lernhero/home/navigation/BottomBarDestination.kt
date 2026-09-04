package com.lernhero.home.navigation

import com.lernhero.shared.i18n.AppStrings
import com.lernhero.shared.navigation.Screen
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.DrawableResource

/**
 * Pozycje dolnego paska. [icon] to grafika (PLACEHOLDER - patrz [Resources.NavIcon]).
 *
 * Kolejnosc jest celowa: "Walcz" stoi w SRODKU (indeks 2) i jest wyrozniona wizualnie jako
 * centralny, wystajacy przycisk - to najwazniejsze miejsce w grze, reszta to zaplecze.
 *
 * Karta postaci NIE ma tu wlasnej zakladki - wchodzi sie na nia z naglowka bohatera na ekranie Dom.
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
    Progress(
        icon = Resources.NavIcon.Progress,
        titleOf = { it.navProgress },
        route = Screen.Progress
    ),
    Shop(
        icon = Resources.NavIcon.Shop,
        titleOf = { it.navShop },
        route = Screen.Shop
    );

    fun title(strings: AppStrings): String = titleOf(strings)

    /** Centralny, wyrozniony przycisk paska. */
    val isPrimary: Boolean get() = this == Fight
}
