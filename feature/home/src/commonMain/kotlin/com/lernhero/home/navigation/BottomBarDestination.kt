package com.lernhero.home.navigation

import com.lernhero.shared.navigation.Screen

/** [icon] to emoji, nie zasob graficzny - te same glify co w kafelkach szybkich akcji na Home. */
enum class BottomBarDestination(
    val icon: String,
    val title: String,
    val route: Screen
) {
    Home(
        icon = "🏰",
        title = "Dom",
        route = Screen.Home
    ),
    Fight(
        icon = "⚔️",
        title = "Walcz",
        route = Screen.Team
    ),
    Equipment(
        icon = "🛡️",
        title = "Postac",
        route = Screen.Equipment
    ),
    Fitness(
        icon = "🏋️",
        title = "Trening",
        route = Screen.Fitness
    ),
    Books(
        icon = "📚",
        title = "Ksiazki",
        route = Screen.Books
    ),
    Progress(
        icon = "📊",
        title = "Postep",
        route = Screen.Progress
    ),
    Shop(
        icon = "🛒",
        title = "Sklep",
        route = Screen.Shop
    )
}
