package com.lernhero.shared

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
    data object FightScreen : Screen()
    /** Zakladka "Fight": sklad druzyny i wejscie do lochu. */
    @Serializable
    data object Team : Screen()
    /** Mapa lochu. Seed jednoznacznie okresla caly graf, wiec wystarczy go przekazac w trasie. */
    @Serializable
    data class Dungeon(val seed: Long) : Screen()
    @Serializable
    data object KnightPreview : Screen()
    @Serializable
    data object ModularKnightPreview : Screen()
    @Serializable
    data object Equipment : Screen()
    @Serializable
    data object Shop : Screen()
}
