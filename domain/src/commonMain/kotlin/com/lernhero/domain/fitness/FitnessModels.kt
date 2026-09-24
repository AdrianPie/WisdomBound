package com.lernhero.domain.fitness

/**
 * Zdjecie aktywnosci gracza z dzisiaj - kroki, aktywne minuty i zalogowane treningi.
 * Kazda platforma (Health Connect na Androidzie, HealthKit na iOS) skleja to z wlasnego API,
 * wiec [dateKey] pochodzi z zegara platformy - domena nie zna zadnej biblioteki dat.
 */
data class FitnessSnapshot(
    /** Data w formacie "yyyy-MM-dd", licznik odswiezania nagrod dziennych. */
    val dateKey: String,
    val steps: Int = 0,
    val activeMinutes: Int = 0,
    val workouts: List<WorkoutSummary> = emptyList(),
    val availability: FitnessAvailability = FitnessAvailability.Unsupported,
    val permissionGranted: Boolean = false
) {
    /** Czy platforma w ogole potrafi dostarczyc te dane (np. Health Connect zainstalowany i aktualny). */
    val available: Boolean get() = availability == FitnessAvailability.Available

    companion object {
        /** Puste zdjecie na czas ladowania albo gdy platforma nie jest dostepna. */
        fun empty(dateKey: String = "", availability: FitnessAvailability = FitnessAvailability.Unsupported): FitnessSnapshot =
            FitnessSnapshot(dateKey = dateKey, availability = availability)
    }
}

/**
 * Dokladny powod, dla ktorego platforma moze nie dostarczac danych - inny komunikat i inna
 * akcja dla "nie ma Health Connect" niz dla "jest, ale za stary".
 */
enum class FitnessAvailability {
    Available,
    /** Health Connect (albo rownowaznik platformy) nie jest zainstalowany. */
    NeedsInstall,
    /** Jest zainstalowany, ale w wersji za starej dla uzywanego SDK. */
    NeedsUpdate,
    /** Platforma nie ma zadnego zrodla danych o aktywnosci (np. desktop). */
    Unsupported
}

data class WorkoutSummary(
    val type: String,
    val durationMinutes: Int
)

enum class FitnessGoalKind {
    Steps,
    ActiveMinutes,
    Workout
}

/**
 * Cel dzienny za ktory placi sie zlotem. [threshold] porownuje sie z odpowiednia liczba
 * z [FitnessSnapshot] - progres i ukonczenie licza sie same, wiec dolozenie celu to jeden wpis.
 */
data class FitnessGoal(
    val id: String,
    val title: String,
    val description: String,
    val kind: FitnessGoalKind,
    val threshold: Int,
    /** Zloto. */
    val reward: Int,
    val gems: Int = 0,
    val tickets: Int = 0
) {
    fun progress(snapshot: FitnessSnapshot): Int = when (kind) {
        FitnessGoalKind.Steps -> snapshot.steps
        FitnessGoalKind.ActiveMinutes -> snapshot.activeMinutes
        FitnessGoalKind.Workout -> snapshot.workouts.size
    }

    fun isComplete(snapshot: FitnessSnapshot): Boolean = progress(snapshot) >= threshold
}

/** Pula celow dziennych. Nowy cel to jeden wpis - reszta ekranu doda go sama. */
/** Dwa glowne cele krokow - ten sam uklad co czytanie: nawyk (5 000) i wyzwanie (10 000). */
object FitnessGoalCatalog {
    val DAILY: List<FitnessGoal> = listOf(
        FitnessGoal(
            id = "daily_steps_5k",
            title = "Morning Walk",
            description = "Walk 5,000 steps today.",
            kind = FitnessGoalKind.Steps,
            threshold = 5_000,
            reward = 300,
            gems = 25,
            tickets = 1
        ),
        FitnessGoal(
            id = "daily_steps_10k",
            title = "Trailblazer",
            description = "Walk 10,000 steps today.",
            kind = FitnessGoalKind.Steps,
            threshold = 10_000,
            reward = 1000,
            gems = 80,
            tickets = 3
        )
    )
}
