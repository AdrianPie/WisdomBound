package com.lernhero.domain.reading

enum class ReadingGoalKind {
    Minutes,
    Pages
}

/** Cel dzienny za czytanie - ten sam ksztalt co [com.lernhero.domain.fitness.FitnessGoal], osobna pula. */
data class ReadingGoal(
    val id: String,
    val title: String,
    val description: String,
    val kind: ReadingGoalKind,
    val threshold: Int,
    val reward: Int
) {
    fun progress(today: ReadingDayProgress): Int = when (kind) {
        ReadingGoalKind.Minutes -> today.minutes
        ReadingGoalKind.Pages -> today.pages
    }

    fun isComplete(today: ReadingDayProgress): Boolean = progress(today) >= threshold
}

object ReadingGoalCatalog {
    val DAILY: List<ReadingGoal> = listOf(
        ReadingGoal(
            id = "daily_read_15",
            title = "Codzienna dawka",
            description = "Czytaj aktywnie 15 minut dzisiaj.",
            kind = ReadingGoalKind.Minutes,
            threshold = 15,
            reward = 70
        ),
        ReadingGoal(
            id = "daily_read_30",
            title = "Molik ksiazkowy",
            description = "Czytaj aktywnie 30 minut dzisiaj.",
            kind = ReadingGoalKind.Minutes,
            threshold = 30,
            reward = 140
        ),
        ReadingGoal(
            id = "daily_read_60",
            title = "Bibliofil",
            description = "Czytaj aktywnie 60 minut dzisiaj.",
            kind = ReadingGoalKind.Minutes,
            threshold = 60,
            reward = 260
        ),
        ReadingGoal(
            id = "daily_pages_10",
            title = "Kartkarz",
            description = "Przewroc 10 stron dzisiaj.",
            kind = ReadingGoalKind.Pages,
            threshold = 10,
            reward = 60
        )
    )
}
