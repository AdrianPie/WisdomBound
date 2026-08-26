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
    /**
     * Mapa kampanii: krainy jak wyspy, w kazdej ciag ponumerowanych plansz (jak w Idle Heroes).
     * Zastapila DungeonCrawl - nie ma juz "chodzenia" po lochu, sam wybor planszy i walka.
     */
    @Serializable
    data object StoryMap : Screen()
    @Serializable
    data object KnightPreview : Screen()
    @Serializable
    data object ModularKnightPreview : Screen()
    @Serializable
    data object MonsterAnchorLab : Screen()
    @Serializable
    data object Equipment : Screen()
    @Serializable
    data object Shop : Screen()
    /** Poziom bohatera, statystyki nauki i osiagniecia - patrz feature/home/progress. */
    @Serializable
    data object Progress : Screen()
    /** Kroki, treningi i odbior nagrod fitness. */
    @Serializable
    data object Fitness : Screen()
    /** Czytnik PDF: biblioteka ksiazek i odbior nagrod za czytanie. */
    @Serializable
    data object Books : Screen()

    /* -- Budynki Miasta (ekran Dom) - kazda wyspa to osobna "instancja", patrz HomeScreen. -- */
    @Serializable
    data object Sawmill : Screen()
    @Serializable
    data object Quarry : Screen()
    @Serializable
    data object Forge : Screen()
    @Serializable
    data object SummonCenter : Screen()
}
