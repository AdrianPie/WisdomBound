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
    /** Zloto. */
    val reward: Int,
    val gems: Int = 0,
    val tickets: Int = 0
) {
    fun progress(today: ReadingDayProgress): Int = when (kind) {
        ReadingGoalKind.Minutes -> today.activeSeconds / 60
        ReadingGoalKind.Pages -> today.pages
    }

    fun isComplete(today: ReadingDayProgress): Boolean = progress(today) >= threshold
}

/**
 * Dwa cele, nie cztery: krotka lista czyta sie jak obietnica ("15 minut i masz skrzynie"), a dluga
 * jak obowiazek. 15 minut to glowny nawyk, godzina - wyzwanie dla wciagnietych. Nagrody sa celowo
 * hojne (diamenty + losy), bo to one maja byc powodem, zeby codziennie siasc do ksiazki.
 */
object ReadingGoalCatalog {
    val DAILY: List<ReadingGoal> = listOf(
        ReadingGoal(
            id = "daily_read_15",
            title = "Daily Dose",
            description = "Read for 15 minutes today.",
            kind = ReadingGoalKind.Minutes,
            threshold = 15,
            reward = 300,
            gems = 25,
            tickets = 1
        ),
        ReadingGoal(
            id = "daily_read_60",
            title = "Deep Reader",
            description = "Read for a full hour today.",
            kind = ReadingGoalKind.Minutes,
            threshold = 60,
            reward = 1000,
            gems = 80,
            tickets = 3
        )
    )
}
