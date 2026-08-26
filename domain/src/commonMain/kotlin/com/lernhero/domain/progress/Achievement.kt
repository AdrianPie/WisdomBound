package com.lernhero.domain.progress

/**
 * Kategoria osiagniecia - po niej grupuje sie lista na ekranie postepu. Nauka jest pierwsza
 * swiadomie: to ona jest w tej grze wlasciwym osiagnieciem, walka tylko jej opakowaniem.
 */
enum class AchievementGroup(val label: String) {
    Learning("Nauka"),
    Combat("Walka"),
    Journey("Wyprawy"),
    Wealth("Dorobek")
}

/** Stopien trudnosci - ten sam pomysl co rzadkosc lupu, tylko dla celow. */
enum class AchievementTier(val label: String) {
    Bronze("Brazowe"),
    Silver("Srebrne"),
    Gold("Zlote")
}

/**
 * Jedno osiagniecie. NIE trzyma wlasnego stanu "odblokowane" - postep liczy sie w locie z
 * [GameStats] przez [progressOf]. Dzieki temu nie ma czego synchronizowac, nie da sie tego
 * rozjechac z faktycznymi statystykami, a dodanie osiagniecia dziala wstecz dla starych kont.
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val group: AchievementGroup,
    val tier: AchievementTier,
    val target: Int,
    val progressOf: (GameStats) -> Int
) {
    fun progress(stats: GameStats): Int = progressOf(stats).coerceAtMost(target)
    fun isUnlocked(stats: GameStats): Boolean = progressOf(stats) >= target
    fun fraction(stats: GameStats): Float =
        if (target <= 0) 1f else (progressOf(stats).toFloat() / target).coerceIn(0f, 1f)
}

/**
 * Katalog osiagniec. Nowe = jeden wpis; ekran, liczniki i paski postepu podlapia je same.
 *
 * Progi sa dobrane tak, zeby pierwsze wpadaly w ciagu pierwszej sesji (gracz od razu widzi, ze
 * system istnieje), a najwyzsze wymagaly tygodni - klasyczna drabinka, ktora daje co robic po
 * przejsciu tresci.
 */
object AchievementCatalog {

    val all: List<Achievement> = listOf(
        // ---------------- Nauka ----------------
        Achievement(
            id = "answers_50",
            title = "Pierwsze pytania",
            description = "Odpowiedz na 50 pytan.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Bronze,
            target = 50,
            progressOf = { it.questionsAnswered }
        ),
        Achievement(
            id = "answers_500",
            title = "Uczen",
            description = "Odpowiedz na 500 pytan.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Silver,
            target = 500,
            progressOf = { it.questionsAnswered }
        ),
        Achievement(
            id = "answers_2500",
            title = "Erudyta",
            description = "Odpowiedz na 2500 pytan.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Gold,
            target = 2500,
            progressOf = { it.questionsAnswered }
        ),
        Achievement(
            id = "correct_250",
            title = "Dobra pamiec",
            description = "Odpowiedz poprawnie 250 razy.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Silver,
            target = 250,
            progressOf = { it.questionsCorrect }
        ),
        Achievement(
            id = "streak_10",
            title = "Seria dziesieciu",
            description = "Trafnij 10 odpowiedzi z rzedu.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Bronze,
            target = 10,
            progressOf = { it.bestAnswerStreak }
        ),
        Achievement(
            id = "streak_25",
            title = "Nie do zatrzymania",
            description = "Trafnij 25 odpowiedzi z rzedu.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Gold,
            target = 25,
            progressOf = { it.bestAnswerStreak }
        ),
        Achievement(
            id = "categories_5",
            title = "Szerokie horyzonty",
            description = "Odpowiedz poprawnie w 5 roznych kategoriach.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Bronze,
            target = 5,
            progressOf = { stats -> stats.perCategory.count { it.value.correct > 0 } }
        ),

        // ---------------- Walka ----------------
        Achievement(
            id = "wins_1",
            title = "Pierwsza krew",
            description = "Wygraj pierwsza walke.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Bronze,
            target = 1,
            progressOf = { it.battlesWon }
        ),
        Achievement(
            id = "wins_50",
            title = "Weteran",
            description = "Wygraj 50 walk.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Silver,
            target = 50,
            progressOf = { it.battlesWon }
        ),
        Achievement(
            id = "waves_100",
            title = "Lamacz fal",
            description = "Oczysc lacznie 100 fal.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Silver,
            target = 100,
            progressOf = { it.totalWavesCleared }
        ),
        Achievement(
            id = "endless_10",
            title = "Bez konca",
            description = "Dojdz do 10. fali w trybie endless.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Bronze,
            target = 10,
            progressOf = { it.bestEndlessWave }
        ),
        Achievement(
            id = "endless_30",
            title = "Legenda areny",
            description = "Dojdz do 30. fali w trybie endless.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Gold,
            target = 30,
            progressOf = { it.bestEndlessWave }
        ),

        // ---------------- Wyprawy ----------------
        Achievement(
            id = "boss_1",
            title = "Pogromca",
            description = "Pokonaj pierwszego bossa lochu.",
            group = AchievementGroup.Journey,
            tier = AchievementTier.Bronze,
            target = 1,
            progressOf = { it.bossesDefeated }
        ),
        Achievement(
            id = "boss_10",
            title = "Lowca bossow",
            description = "Pokonaj 10 bossow.",
            group = AchievementGroup.Journey,
            tier = AchievementTier.Silver,
            target = 10,
            progressOf = { it.bossesDefeated }
        ),
        Achievement(
            id = "dungeons_5",
            title = "Kartograf",
            description = "Przejdz 5 lochow do konca.",
            group = AchievementGroup.Journey,
            tier = AchievementTier.Silver,
            target = 5,
            progressOf = { it.dungeonsCleared }
        ),

        // ---------------- Dorobek ----------------
        Achievement(
            id = "gold_5000",
            title = "Sakwa pelna",
            description = "Zdobadz lacznie 5000 monet.",
            group = AchievementGroup.Wealth,
            tier = AchievementTier.Bronze,
            target = 5_000,
            progressOf = { it.goldEarned }
        ),
        Achievement(
            id = "gold_50000",
            title = "Skarbiec",
            description = "Zdobadz lacznie 50 000 monet.",
            group = AchievementGroup.Wealth,
            tier = AchievementTier.Gold,
            target = 50_000,
            progressOf = { it.goldEarned }
        ),
        Achievement(
            id = "crafted_5",
            title = "Kowal",
            description = "Wykuj 5 przedmiotow.",
            group = AchievementGroup.Wealth,
            tier = AchievementTier.Bronze,
            target = 5,
            progressOf = { it.itemsCrafted }
        ),
        Achievement(
            id = "summoned_5",
            title = "Przyzywacz",
            description = "Przyzwij 5 kompanow.",
            group = AchievementGroup.Wealth,
            tier = AchievementTier.Bronze,
            target = 5,
            progressOf = { it.monstersSummoned }
        )
    )

    fun byGroup(group: AchievementGroup): List<Achievement> = all.filter { it.group == group }

    fun unlockedCount(stats: GameStats): Int = all.count { it.isUnlocked(stats) }

    /** Najblizsze do zdobycia - to one trafiaja na gore ekranu jako "jeszcze troche". */
    fun nextUp(stats: GameStats, limit: Int = 3): List<Achievement> =
        all.filterNot { it.isUnlocked(stats) }
            .sortedByDescending { it.fraction(stats) }
            .take(limit)
}
