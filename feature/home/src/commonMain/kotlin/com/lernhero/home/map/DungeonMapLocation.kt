package com.lernhero.home.map

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.run.BattleKind
import com.lernhero.domain.run.RunDifficulty

/**
 * Jedno klikalne miejsce na [Resources.Image.dungeonMap][com.lernhero.shared.resources.Resources.Image.dungeonMap].
 * [x]/[y] to ulamek (0..1) szerokosci/wysokosci PNG, nie pikseli - dzieki temu pozycja znacznika
 * jest poprawna niezaleznie od tego, jak bardzo mapa jest przybliozna/przesunieta na ekranie.
 */
data class DungeonMapLocation(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val kind: BattleKind,
    val difficulty: RunDifficulty,
    /** Null = losowa kategoria pytan (tak jak "Losowe" w starym pickerze). */
    val category: QuizCategory?,
    val waves: Int
)

/**
 * Wspolrzedne wyliczone recznie z opisow na PNG "The Blighted Realms" - to placeholdery
 * (przypisanie trudnosci/kategorii do nazwy jest wziete z klimatu miejsca, nie z projektu
 * rozgrywki). Latwo je pozniej podmienic, bo caly system czyta tylko te 15 wpisow.
 */
object DungeonMapLocations {
    val all = listOf(
        DungeonMapLocation(
            id = "gallows_wood",
            name = "Gallows Wood",
            x = 0.224f,
            y = 0.446f,
            kind = BattleKind.Normal,
            difficulty = RunDifficulty.Easy,
            category = QuizCategory.Biology,
            waves = 1
        ),
        DungeonMapLocation(
            id = "iron_peaks",
            name = "The Iron Peaks",
            x = 0.29f,
            y = 0.34f,
            kind = BattleKind.Elite,
            difficulty = RunDifficulty.Hard,
            category = QuizCategory.Elements,
            waves = 2
        ),
        DungeonMapLocation(
            id = "blackstone_castle",
            name = "Blackstone Castle",
            x = 0.505f,
            y = 0.30f,
            kind = BattleKind.Elite,
            difficulty = RunDifficulty.Normal,
            category = QuizCategory.History,
            waves = 2
        ),
        DungeonMapLocation(
            id = "crownless_abbey",
            name = "Crownless Abbey",
            x = 0.65f,
            y = 0.323f,
            kind = BattleKind.Normal,
            difficulty = RunDifficulty.Normal,
            category = QuizCategory.Mythology,
            waves = 1
        ),
        DungeonMapLocation(
            id = "dreadfall_lighthouse",
            name = "Dreadfall Lighthouse",
            x = 0.795f,
            y = 0.335f,
            kind = BattleKind.Elite,
            difficulty = RunDifficulty.Hard,
            category = QuizCategory.Geography,
            waves = 2
        ),
        DungeonMapLocation(
            id = "abandoned_keep",
            name = "Abandoned Keep",
            x = 0.366f,
            y = 0.443f,
            kind = BattleKind.Normal,
            difficulty = RunDifficulty.Easy,
            category = QuizCategory.History,
            waves = 1
        ),
        DungeonMapLocation(
            id = "forsaken_hamlet",
            name = "Forsaken Hamlet",
            x = 0.521f,
            y = 0.469f,
            kind = BattleKind.Normal,
            difficulty = RunDifficulty.Easy,
            category = QuizCategory.Science,
            waves = 1
        ),
        DungeonMapLocation(
            id = "ancient_caverns",
            name = "Ancient Caverns",
            x = 0.661f,
            y = 0.551f,
            kind = BattleKind.Elite,
            difficulty = RunDifficulty.Normal,
            category = QuizCategory.Science,
            waves = 2
        ),
        DungeonMapLocation(
            id = "ship_graveyard",
            name = "Ship Graveyard",
            x = 0.729f,
            y = 0.477f,
            kind = BattleKind.Normal,
            difficulty = RunDifficulty.Normal,
            category = QuizCategory.Geography,
            waves = 1
        ),
        DungeonMapLocation(
            id = "wailing_cove",
            name = "Wailing Cove",
            x = 0.784f,
            y = 0.585f,
            kind = BattleKind.Normal,
            difficulty = RunDifficulty.Normal,
            category = QuizCategory.Animals,
            waves = 1
        ),
        DungeonMapLocation(
            id = "sunken_mire",
            name = "The Sunken Mire",
            x = 0.24f,
            y = 0.531f,
            kind = BattleKind.Normal,
            difficulty = RunDifficulty.Easy,
            category = QuizCategory.Biology,
            waves = 1
        ),
        DungeonMapLocation(
            id = "ruins_of_aldmoor",
            name = "Ruins of Aldmoor",
            x = 0.435f,
            y = 0.605f,
            kind = BattleKind.Normal,
            difficulty = RunDifficulty.Normal,
            category = QuizCategory.History,
            waves = 1
        ),
        DungeonMapLocation(
            id = "weeping_mausoleum",
            name = "The Weeping Mausoleum",
            x = 0.563f,
            y = 0.672f,
            kind = BattleKind.Elite,
            difficulty = RunDifficulty.Hard,
            category = QuizCategory.Mythology,
            waves = 2
        ),
        DungeonMapLocation(
            id = "stonescar_island",
            name = "Stonescar Island",
            x = 0.724f,
            y = 0.723f,
            kind = BattleKind.Elite,
            difficulty = RunDifficulty.Normal,
            category = QuizCategory.Geography,
            waves = 2
        ),
        DungeonMapLocation(
            id = "crimson_court",
            name = "The Crimson Court",
            x = 0.319f,
            y = 0.695f,
            kind = BattleKind.Boss,
            difficulty = RunDifficulty.Hard,
            category = QuizCategory.Mythology,
            waves = 3
        )
    )
}
