package com.lernhero.domain.progress

/**
 * Kategoria osiagniecia - po niej grupuje sie lista na ekranie postepu. Nauka jest pierwsza
 * swiadomie: to ona jest w tej grze wlasciwym osiagnieciem, walka tylko jej opakowaniem.
 */
enum class AchievementGroup(val label: String) {
    Learning("Learning"),
    Combat("Combat"),
    Journey("Journey"),
    Wealth("Wealth")
}

/** Stopien trudnosci - ten sam pomysl co rzadkosc lupu, tylko dla celow. */
enum class AchievementTier(val label: String) {
    Bronze("Bronze"),
    Silver("Silver"),
    Gold("Gold")
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
        // ---------------- Learning ----------------
        Achievement(
            id = "answers_50",
            title = "First Questions",
            description = "Answer 50 questions.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Bronze,
            target = 50,
            progressOf = { it.questionsAnswered }
        ),
        Achievement(
            id = "answers_500",
            title = "Student",
            description = "Answer 500 questions.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Silver,
            target = 500,
            progressOf = { it.questionsAnswered }
        ),
        Achievement(
            id = "answers_2500",
            title = "Scholar",
            description = "Answer 2500 questions.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Gold,
            target = 2500,
            progressOf = { it.questionsAnswered }
        ),
        Achievement(
            id = "correct_250",
            title = "Good Memory",
            description = "Answer correctly 250 times.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Silver,
            target = 250,
            progressOf = { it.questionsCorrect }
        ),
        Achievement(
            id = "streak_10",
            title = "Streak of Ten",
            description = "Get 10 answers right in a row.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Bronze,
            target = 10,
            progressOf = { it.bestAnswerStreak }
        ),
        Achievement(
            id = "streak_25",
            title = "Unstoppable",
            description = "Get 25 answers right in a row.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Gold,
            target = 25,
            progressOf = { it.bestAnswerStreak }
        ),
        Achievement(
            id = "categories_5",
            title = "Broad Horizons",
            description = "Answer correctly in 5 different categories.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Bronze,
            target = 5,
            progressOf = { stats -> stats.perCategory.count { it.value.correct > 0 } }
        ),

        // ---------------- Combat ----------------
        Achievement(
            id = "wins_1",
            title = "First Blood",
            description = "Win your first battle.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Bronze,
            target = 1,
            progressOf = { it.battlesWon }
        ),
        Achievement(
            id = "wins_50",
            title = "Veteran",
            description = "Win 50 battles.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Silver,
            target = 50,
            progressOf = { it.battlesWon }
        ),
        Achievement(
            id = "waves_100",
            title = "Wave Breaker",
            description = "Clear 100 waves in total.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Silver,
            target = 100,
            progressOf = { it.totalWavesCleared }
        ),
        Achievement(
            id = "endless_10",
            title = "Endless",
            description = "Reach wave 10 in endless mode.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Bronze,
            target = 10,
            progressOf = { it.bestEndlessWave }
        ),
        Achievement(
            id = "endless_30",
            title = "Arena Legend",
            description = "Reach wave 30 in endless mode.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Gold,
            target = 30,
            progressOf = { it.bestEndlessWave }
        ),

        // ---------------- Journey ----------------
        Achievement(
            id = "boss_1",
            title = "Slayer",
            description = "Defeat your first dungeon boss.",
            group = AchievementGroup.Journey,
            tier = AchievementTier.Bronze,
            target = 1,
            progressOf = { it.bossesDefeated }
        ),
        Achievement(
            id = "boss_10",
            title = "Boss Hunter",
            description = "Defeat 10 bosses.",
            group = AchievementGroup.Journey,
            tier = AchievementTier.Silver,
            target = 10,
            progressOf = { it.bossesDefeated }
        ),
        Achievement(
            id = "dungeons_5",
            title = "Cartographer",
            description = "Clear 5 dungeons all the way through.",
            group = AchievementGroup.Journey,
            tier = AchievementTier.Silver,
            target = 5,
            progressOf = { it.dungeonsCleared }
        ),

        // ---------------- Wealth ----------------
        Achievement(
            id = "gold_5000",
            title = "Full Purse",
            description = "Earn 5000 coins in total.",
            group = AchievementGroup.Wealth,
            tier = AchievementTier.Bronze,
            target = 5_000,
            progressOf = { it.goldEarned }
        ),
        Achievement(
            id = "gold_50000",
            title = "Treasury",
            description = "Earn 50,000 coins in total.",
            group = AchievementGroup.Wealth,
            tier = AchievementTier.Gold,
            target = 50_000,
            progressOf = { it.goldEarned }
        ),
        Achievement(
            id = "crafted_5",
            title = "Blacksmith",
            description = "Forge 5 items.",
            group = AchievementGroup.Wealth,
            tier = AchievementTier.Bronze,
            target = 5,
            progressOf = { it.itemsCrafted }
        ),
        Achievement(
            id = "summoned_5",
            title = "Summoner",
            description = "Summon 5 companions.",
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
