package com.lernhero.domain.run

import com.lernhero.domain.quiz.QuizDifficulty

/**
 * Poziom trudnosci calego runu. W kampanii to sa zakladki nad mapa swiata (jak w Idle Heroes):
 * przechodzisz wszystkie krainy na [Normal], co odblokowuje [Nightmare] na TYCH SAMYCH krainach,
 * mocniejszych i lepiej platnych - i tak dalej az do [Death]. Endless zawsze stoi na [Normal],
 * bo tam za trudnosc odpowiada wylacznie rosnaca fala, nie ten mnoznik.
 *
 * Sterowanie dwoma rzeczami naraz: jak mocni sa wrogowie i z jakiego szczebla drabinki losuja sie
 * pytania. Trudniejszy poziom placi wiecej monet - inaczej nikt by go nie wybieral po odblokowaniu.
 */
enum class RunDifficulty(
    val label: String,
    val description: String,
    val icon: String,
    val enemyHpMultiplier: Float,
    val enemyAttackMultiplier: Float,
    val coinMultiplier: Float
) {
    Normal(
        label = "Normal",
        description = "Balanced fights and questions from easy to hard.",
        icon = "⚔",
        enemyHpMultiplier = 1f,
        enemyAttackMultiplier = 1f,
        coinMultiplier = 1f
    ),
    Nightmare(
        label = "Nightmare",
        description = "Enemies and questions noticeably tougher. Unlocked after clearing Normal.",
        icon = "💀",
        enemyHpMultiplier = 1.5f,
        enemyAttackMultiplier = 1.35f,
        coinMultiplier = 1.6f
    ),
    Hell(
        label = "Hell",
        description = "The same campaign, much harsher. Unlocked after clearing Nightmare.",
        icon = "👹",
        enemyHpMultiplier = 2.2f,
        enemyAttackMultiplier = 1.8f,
        coinMultiplier = 2.4f
    ),
    Death(
        label = "Death",
        description = "The peak of the campaign. Unlocked after clearing Hell.",
        icon = "🔥",
        enemyHpMultiplier = 3.2f,
        enemyAttackMultiplier = 2.4f,
        coinMultiplier = 3.6f
    );

    /** Drabinka pytan przy bramie lochu - piec pytan otwierajacych run. */
    val gateLadder: List<QuizDifficulty>
        get() = when (this) {
            Normal -> listOf(
                QuizDifficulty.Easy, QuizDifficulty.Easy, QuizDifficulty.Medium,
                QuizDifficulty.Medium, QuizDifficulty.Hard
            )
            Nightmare -> listOf(
                QuizDifficulty.Easy, QuizDifficulty.Medium, QuizDifficulty.Medium,
                QuizDifficulty.Hard, QuizDifficulty.Hard
            )
            Hell, Death -> listOf(
                QuizDifficulty.Medium, QuizDifficulty.Medium, QuizDifficulty.Hard,
                QuizDifficulty.Hard, QuizDifficulty.Expert
            )
        }

    /** Drabinka trzech pytan otwierajacych walke na polu mapy. Boss podnosi poprzeczke. */
    fun battleLadder(kind: BattleKind): List<QuizDifficulty> = when (this) {
        Normal -> when (kind) {
            BattleKind.Normal -> listOf(QuizDifficulty.Easy, QuizDifficulty.Easy, QuizDifficulty.Medium)
            BattleKind.Elite -> listOf(QuizDifficulty.Easy, QuizDifficulty.Medium, QuizDifficulty.Medium)
            BattleKind.Boss -> listOf(QuizDifficulty.Medium, QuizDifficulty.Medium, QuizDifficulty.Hard)
        }
        Nightmare -> when (kind) {
            BattleKind.Normal -> listOf(QuizDifficulty.Easy, QuizDifficulty.Medium, QuizDifficulty.Medium)
            BattleKind.Elite -> listOf(QuizDifficulty.Medium, QuizDifficulty.Medium, QuizDifficulty.Hard)
            BattleKind.Boss -> listOf(QuizDifficulty.Medium, QuizDifficulty.Hard, QuizDifficulty.Expert)
        }
        Hell, Death -> when (kind) {
            BattleKind.Normal -> listOf(QuizDifficulty.Medium, QuizDifficulty.Hard, QuizDifficulty.Hard)
            BattleKind.Elite -> listOf(QuizDifficulty.Hard, QuizDifficulty.Hard, QuizDifficulty.Expert)
            BattleKind.Boss -> listOf(QuizDifficulty.Hard, QuizDifficulty.Expert, QuizDifficulty.Expert)
        }
    }

    /** Nastepny szczebel trudnosci kampanii - null na [Death], juz nie ma wyzej. */
    val next: RunDifficulty? get() = entries.getOrNull(ordinal + 1)
}
