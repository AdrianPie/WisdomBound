package com.lernhero.shared.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Auth : Screen()
    /**
     * Tworzenie postaci. [devPreview] to wejscie deweloperskie z ekranu logowania: ten sam ekran,
     * ale bez zalogowanego uzytkownika, wiec "Ruszaj na przygode" NICZEGO nie zapisuje i tylko
     * wraca. Sluzy do ogladania ekranu, nie do zakladania konta.
     */
    @Serializable
    data class CharacterCreation(val devPreview: Boolean = false) : Screen()
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
    /** Dev Animation Center - os czasu do animowania czesci potworow (patrz MonsterAnimationLabScreen). */
    @Serializable
    data object MonsterAnimationLab : Screen()
    @Serializable
    data object Equipment : Screen()
    /** Drzewko talentow - trzy sciezki rozwoju bohatera. Wchodzi sie z karty postaci. */
    @Serializable
    data object Talents : Screen()
    @Serializable
    data object Shop : Screen()
    /** Poziom bohatera, statystyki nauki i osiagniecia - patrz feature/home/progress. */
    @Serializable
    data object Progress : Screen()
    /**
     * Zakladka "Rozwoj": czytanie ksiazek i trening z urzadzenia jako jedno miejsce - obie te
     * rzeczy robi sie NAPRAWDE, poza gra, i obie daja bonusy. W srodku dwie podzakladki
     * (Ksiazki / Trening), patrz feature/home/growth.
     */
    @Serializable
    data object Growth : Screen()
    /**
     * Slowka zapisane z czytnika - osobna instancja obok Czytelni, bo powtarzanie fiszek to inna
     * robota niz czytanie. Dwa archiwa (do nauki / opanowane), patrz VocabularyScreen.
     */
    @Serializable
    data object Vocabulary : Screen()

    /**
     * Edytor wlasnej talii pytan (endless -> "Wlasne"). Wchodzi sie z zakladki "Walcz", a nie
     * z dolnego paska - talia jest przygotowaniem do walki, nie osobnym dzialem gry.
     *
     * [startWithAi] rozwija od razu panel generowania: "wygeneruj mi talie" i "napisz talie" to
     * dwie rozne intencje, mimo ze koncza sie na tym samym ekranie.
     */
    @Serializable
    data class DeckEditor(val deckId: String, val startWithAi: Boolean = false) : Screen()

    /**
     * Ustawienia: jezyk, przypomnienia, tempo walki i konto (z wylogowaniem wlacznie) - wchodzi
     * sie z zebatki obok karty postaci na Domu.
     */
    @Serializable
    data object Settings : Screen()

    /* -- Budynki Miasta (ekran Dom) - kazda wyspa to osobna "instancja", patrz HomeScreen. -- */
    @Serializable
    data object Quarry : Screen()
    @Serializable
    data object Forge : Screen()
    @Serializable
    data object SummonCenter : Screen()
}
