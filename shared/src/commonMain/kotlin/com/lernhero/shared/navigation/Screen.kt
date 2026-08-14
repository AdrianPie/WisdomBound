package com.lernhero.shared.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Auth : Screen()
    @Serializable
    data object CharacterCreation : Screen()
    @Serializable
    data object HomeGraph : Screen()
    @Serializable
    data object Home: Screen()
    @Serializable
    data object BattleScreen : Screen()
    /** Zakladka "Fight": sklad druzyny (u gory) i mapa krainy do wyboru lokacji (na dole). */
    @Serializable
    data object Team : Screen()
    /** Wnetrze wybranego lochu: druzyna u gory, kompaktowa mapa wezlow na dole. */
    @Serializable
    data object DungeonCrawl : Screen()
    @Serializable
    data object KnightPreview : Screen()
    @Serializable
    data object ModularKnightPreview : Screen()
    @Serializable
    data object Equipment : Screen()
    @Serializable
    data object Shop : Screen()
    /** Kroki, treningi i odbior nagrod fitness. */
    @Serializable
    data object Fitness : Screen()
    /** Czytnik PDF: biblioteka ksiazek i odbior nagrod za czytanie. */
    @Serializable
    data object Books : Screen()
}
