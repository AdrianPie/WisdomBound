package com.lernhero.home.domain

import com.lernhero.shared.Resources
import com.lernhero.shared.Screen
import org.jetbrains.compose.resources.DrawableResource

enum class BottomBarDestination(
    val icon : DrawableResource,
    val title: String,
    val route: Screen
){
    Home(
        icon = Resources.Icon.Home,
        title = "Home",
        route = Screen.Home

    ),
    Fight(
        icon = Resources.Icon.swords,
        title = "Fight",
        route = Screen.Team
    ),

    Equipment(
        icon = Resources.Icon.Person,
        title = "EQ",
        route = Screen.Equipment
    ),

    Shop(
        icon = Resources.Icon.ShoppingCart,
        title = "Shop",
        route = Screen.Shop
    )
}
