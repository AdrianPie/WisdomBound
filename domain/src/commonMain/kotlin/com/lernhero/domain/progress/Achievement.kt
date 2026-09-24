package com.lernhero.domain.progress

import com.lernhero.domain.jewel.Jewel
import com.lernhero.domain.jewel.JewelGrade
import com.lernhero.domain.jewel.JewelType
import com.lernhero.domain.quiz.QuizCategory

/**
 * Kategoria osiagniecia - po niej grupuje sie lista na ekranie postepu. Nauka jest pierwsza
 * swiadomie: to ona jest w tej grze wlasciwym osiagnieciem, walka tylko jej opakowaniem.
 */
enum class AchievementGroup(val label: String) {
    Learning("Learning"),
    /** Serie w pojedynczych tematach - glowne zrodlo kamieni. */
    Topics("Topics"),
    Combat("Combat"),
    Journey("Journey"),
    Wealth("Wealth")
}

/**
 * Stopien trudnosci - ten sam pomysl co rzadkosc lupu, tylko dla celow.
 *
 * [reward] to zloto za odebranie osiagniecia tego stopnia. Nagroda siedzi na STOPNIU, a nie na
 * pojedynczym osiagnieciu, celowo: dopisanie nowego celu do katalogu nie wymaga wtedy wymyslania
 * mu kwoty i nie da sie przez przeoczenie dodac celu, ktory placi zero albo dziesiec razy za duzo.
 *
 * [gems] to jednorazowe diamenty - osiagniecia sa jedynym zrodlem diamentow, ktore nie jest
 * codzienne, wiec moga placic hojniej niz cele dzienne, nie rozpedzajac przy tym dziennego dochodu.
 */
enum class AchievementTier(val label: String, val reward: Int, val gems: Int, val jewelGrade: JewelGrade) {
    Bronze("Bronze", 250, 10, JewelGrade.Chipped),
    Silver("Silver", 1_000, 30, JewelGrade.Flawed),
    Gold("Gold", 4_000, 100, JewelGrade.Polished)
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
    val progressOf: (GameStats) -> Int,
    /** Rodzaj kamienia w nagrodzie; szlif wynika ze stopnia ([AchievementTier.jewelGrade]). */
    val jewelType: JewelType? = null,
    /** Temat, ktorego dotyczy osiagniecie z grupy [AchievementGroup.Topics]. */
    val topic: QuizCategory? = null
) {
    val jewel: Jewel? get() = jewelType?.let { Jewel(it, tier.jewelGrade) }

    fun progress(stats: GameStats): Int = progressOf(stats).coerceAtMost(target)
    fun isUnlocked(stats: GameStats): Boolean = progressOf(stats) >= target
    fun fraction(stats: GameStats): Float =
        if (target <= 0) 1f else (progressOf(stats).toFloat() / target).coerceIn(0f, 1f)

    /** Zloto za odebranie - bierze sie ze stopnia, patrz [AchievementTier.reward]. */
    val reward: Int get() = tier.reward

    /** Diamenty za odebranie - tez ze stopnia. */
    val gems: Int get() = tier.gems

    /**
     * Czy jest co odebrac: zdobyte, ale jeszcze nieodebrane. Osiagniecie samo nie trzyma stanu
     * (postep liczy sie z [GameStats]), wiec JEDYNE, co trzeba zapisac, to fakt odbioru nagrody -
     * stad [claimed] z zewnatrz zamiast pola w tej klasie.
     */
    fun isClaimable(stats: GameStats, claimed: Set<String>): Boolean =
        isUnlocked(stats) && id !in claimed
}

/**
 * Katalog osiagniec. Nowe = jeden wpis; ekran, liczniki i paski postepu podlapia je same.
 *
 * Progi sa dobrane tak, zeby pierwsze wpadaly w ciagu pierwszej sesji (gracz od razu widzi, ze
 * system istnieje), a najwyzsze wymagaly tygodni - klasyczna drabinka, ktora daje co robic po
 * przejsciu tresci.
 */
object AchievementCatalog {

    val all: List<Achievement> by lazy { fixed + topics }

    private val fixed: List<Achievement> = listOf(
        // ---------------- Learning ----------------
        Achievement(
            id = "answers_50",
            title = "First Questions",
            description = "Answer 50 questions.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Bronze,
            target = 50,
            jewelType = JewelType.Onyx,
            progressOf = { it.questionsAnswered }
        ),
        Achievement(
            id = "answers_500",
            title = "Student",
            description = "Answer 500 questions.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Silver,
            target = 500,
            jewelType = JewelType.Topaz,
            progressOf = { it.questionsAnswered }
        ),
        Achievement(
            id = "answers_2500",
            title = "Scholar",
            description = "Answer 2500 questions.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Gold,
            target = 2500,
            jewelType = JewelType.Amethyst,
            progressOf = { it.questionsAnswered }
        ),
        Achievement(
            id = "correct_250",
            title = "Good Memory",
            description = "Answer correctly 250 times.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Silver,
            target = 250,
            jewelType = JewelType.Sapphire,
            progressOf = { it.questionsCorrect }
        ),
        Achievement(
            id = "streak_10",
            title = "Streak of Ten",
            description = "Get 10 answers right in a row.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Bronze,
            target = 10,
            jewelType = JewelType.Ruby,
            progressOf = { it.bestAnswerStreak }
        ),
        Achievement(
            id = "streak_25",
            title = "Unstoppable",
            description = "Get 25 answers right in a row.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Gold,
            target = 25,
            jewelType = JewelType.Topaz,
            progressOf = { it.bestAnswerStreak }
        ),
        Achievement(
            id = "categories_5",
            title = "Broad Horizons",
            description = "Answer correctly in 5 different categories.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Bronze,
            target = 5,
            jewelType = JewelType.Emerald,
            progressOf = { stats -> stats.perCategory.count { it.value.correct > 0 } }
        ),

        Achievement(
            id = "streak_50",
            title = "Flawless Mind",
            description = "Get 50 answers right in a row.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Gold,
            target = 50,
            jewelType = JewelType.Amethyst,
            progressOf = { it.bestAnswerStreak }
        ),
        Achievement(
            id = "correct_1000",
            title = "Sage",
            description = "Answer correctly 1000 times.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Gold,
            target = 1000,
            jewelType = JewelType.Sapphire,
            progressOf = { it.questionsCorrect }
        ),
        Achievement(
            id = "categories_10",
            title = "Explorer",
            description = "Answer correctly in 10 different categories.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Silver,
            target = 10,
            jewelType = JewelType.Onyx,
            progressOf = { stats -> stats.perCategory.count { it.value.correct > 0 } }
        ),
        Achievement(
            id = "polymath",
            title = "Polymath",
            description = "Reach a streak of 10 in 5 different topics.",
            group = AchievementGroup.Learning,
            tier = AchievementTier.Gold,
            target = 5,
            jewelType = JewelType.Emerald,
            progressOf = { stats -> stats.bestCategoryStreaks.count { it.value >= 10 } }
        ),

        // ---------------- Combat ----------------
        Achievement(
            id = "wins_1",
            title = "First Blood",
            description = "Win your first battle.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Bronze,
            target = 1,
            jewelType = JewelType.Ruby,
            progressOf = { it.battlesWon }
        ),
        Achievement(
            id = "wins_50",
            title = "Veteran",
            description = "Win 50 battles.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Silver,
            target = 50,
            jewelType = JewelType.Ruby,
            progressOf = { it.battlesWon }
        ),
        Achievement(
            id = "waves_100",
            title = "Wave Breaker",
            description = "Clear 100 waves in total.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Silver,
            target = 100,
            jewelType = JewelType.Onyx,
            progressOf = { it.totalWavesCleared }
        ),
        Achievement(
            id = "endless_10",
            title = "Endless",
            description = "Reach wave 10 in endless mode.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Bronze,
            target = 10,
            jewelType = JewelType.Sapphire,
            progressOf = { it.bestEndlessWave }
        ),
        Achievement(
            id = "endless_30",
            title = "Arena Legend",
            description = "Reach wave 30 in endless mode.",
            group = AchievementGroup.Combat,
            tier = AchievementTier.Gold,
            target = 30,
            jewelType = JewelType.Amethyst,
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
            jewelType = JewelType.Emerald,
            progressOf = { it.bossesDefeated }
        ),
        Achievement(
            id = "boss_10",
            title = "Boss Hunter",
            description = "Defeat 10 bosses.",
            group = AchievementGroup.Journey,
            tier = AchievementTier.Silver,
            target = 10,
            jewelType = JewelType.Ruby,
            progressOf = { it.bossesDefeated }
        ),
        Achievement(
            id = "dungeons_5",
            title = "Cartographer",
            description = "Clear 5 dungeons all the way through.",
            group = AchievementGroup.Journey,
            tier = AchievementTier.Silver,
            target = 5,
            jewelType = JewelType.Topaz,
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
            jewelType = JewelType.Topaz,
            progressOf = { it.goldEarned }
        ),
        Achievement(
            id = "gold_50000",
            title = "Treasury",
            description = "Earn 50,000 coins in total.",
            group = AchievementGroup.Wealth,
            tier = AchievementTier.Gold,
            target = 50_000,
            jewelType = JewelType.Amethyst,
            progressOf = { it.goldEarned }
        ),
        Achievement(
            id = "crafted_5",
            title = "Blacksmith",
            description = "Forge 5 items.",
            group = AchievementGroup.Wealth,
            tier = AchievementTier.Bronze,
            target = 5,
            jewelType = JewelType.Onyx,
            progressOf = { it.itemsCrafted }
        ),
        Achievement(
            id = "summoned_5",
            title = "Summoner",
            description = "Summon 5 companions.",
            group = AchievementGroup.Wealth,
            tier = AchievementTier.Bronze,
            target = 5,
            jewelType = JewelType.Emerald,
            progressOf = { it.monstersSummoned }
        )
    )

    /**
     * Po trzy stopnie serii w kazdym temacie z wbudowanego banku. Tematy lokalne (np. Polska)
     * sa pominiete, bo nie kazdy gracz je widzi - osiagniecie niemozliwe do zdobycia tylko by
     * irytowalo.
     */
    private val topics: List<Achievement> by lazy { buildTopics() }

    private fun buildTopics(): List<Achievement> = QuizCategory.builtin
        .filter { it.localeCode == null }
        .flatMap { category ->
            TOPIC_STEPS.map { (tier, target, title) ->
                Achievement(
                    id = "topic_${category.name.lowercase()}_$target",
                    title = "${category.label} $title",
                    description = "Answer $target ${category.label} questions right in a row. " +
                        "Only a wrong ${category.label} answer breaks the streak.",
                    group = AchievementGroup.Topics,
                    tier = tier,
                    target = target,
                    jewelType = TOPIC_JEWELS[category] ?: JewelType.Onyx,
                    topic = category,
                    progressOf = { it.bestStreakIn(category) }
                )
            }
        }

    private val TOPIC_STEPS = listOf(
        Triple(AchievementTier.Bronze, 10, "Apprentice"),
        Triple(AchievementTier.Silver, 30, "Adept"),
        Triple(AchievementTier.Gold, 60, "Master")
    )

    private val TOPIC_JEWELS: Map<QuizCategory, JewelType> = mapOf(
        QuizCategory.Biology to JewelType.Emerald,
        QuizCategory.Animals to JewelType.Onyx,
        QuizCategory.History to JewelType.Amethyst,
        QuizCategory.Elements to JewelType.Ruby,
        QuizCategory.Capitals to JewelType.Sapphire,
        QuizCategory.Science to JewelType.Topaz,
        QuizCategory.Flags to JewelType.Ruby,
        QuizCategory.Geography to JewelType.Sapphire,
        QuizCategory.Sport to JewelType.Onyx,
        QuizCategory.Math to JewelType.Topaz,
        QuizCategory.Literature to JewelType.Amethyst,
        QuizCategory.Technology to JewelType.Topaz,
        QuizCategory.Mythology to JewelType.Emerald
    )

    fun byGroup(group: AchievementGroup): List<Achievement> = all.filter { it.group == group }

    fun unlockedCount(stats: GameStats): Int = all.count { it.isUnlocked(stats) }

    fun byId(id: String): Achievement? = all.firstOrNull { it.id == id }

    /** Ile osiagniec czeka na odbior - z tego bierze sie kropka na zakladce Postep. */
    fun claimableCount(stats: GameStats, claimed: Set<String>): Int =
        all.count { it.isClaimable(stats, claimed) }

    /** Ile zlota lezy nieodebrane - pokazywane nad lista, zeby bylo widac po co tam wchodzic. */
    fun claimableGold(stats: GameStats, claimed: Set<String>): Int =
        all.filter { it.isClaimable(stats, claimed) }.sumOf { it.reward }

    /** Najblizsze do zdobycia - to one trafiaja na gore ekranu jako "jeszcze troche". */
    fun nextUp(stats: GameStats, limit: Int = 3): List<Achievement> =
        all.filterNot { it.isUnlocked(stats) }
            .sortedByDescending { it.fraction(stats) }
            .take(limit)
}
