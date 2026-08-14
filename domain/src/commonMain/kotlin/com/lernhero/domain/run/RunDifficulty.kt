package com.lernhero.domain.run

import com.lernhero.domain.quiz.QuizDifficulty

/**
 * Poziom trudnosci calego runu, wybierany przed wejsciem do lochu. Steruje dwoma rzeczami
 * naraz: jak mocni sa wrogowie i z jakiego szczebla drabinki losuja sie pytania.
 * Trudniejszy run placi wiecej monet - inaczej nikt by go nie wybieral.
 */
enum class RunDifficulty(
    val label: String,
    val description: String,
    val enemyHpMultiplier: Float,
    val enemyAttackMultiplier: Float,
    val coinMultiplier: Float
) {
    Easy(
        label = "Latwy",
        description = "Lagodni wrogowie i proste pytania. Dobry na rozgrzewke.",
        enemyHpMultiplier = 0.8f,
        enemyAttackMultiplier = 0.8f,
        coinMultiplier = 0.8f
    ),
    Normal(
        label = "Normalny",
        description = "Wyrownana walka i pytania od latwych po trudne.",
        enemyHpMultiplier = 1f,
        enemyAttackMultiplier = 1f,
        coinMultiplier = 1f
    ),
    Hard(
        label = "Trudny",
        description = "Mocni wrogowie i pytania, ktore potrafia zabolec. Placi najlepiej.",
        enemyHpMultiplier = 1.35f,
        enemyAttackMultiplier = 1.25f,
        coinMultiplier = 1.5f
    );

    /** Drabinka pytan przy bramie lochu - piec pytan otwierajacych run. */
    val gateLadder: List<QuizDifficulty>
        get() = when (this) {
            Easy -> listOf(
                QuizDifficulty.Easy, QuizDifficulty.Easy, QuizDifficulty.Easy,
                QuizDifficulty.Medium, QuizDifficulty.Medium
            )
            Normal -> listOf(
                QuizDifficulty.Easy, QuizDifficulty.Easy, QuizDifficulty.Medium,
                QuizDifficulty.Medium, QuizDifficulty.Hard
            )
            Hard -> listOf(
                QuizDifficulty.Medium, QuizDifficulty.Medium, QuizDifficulty.Hard,
                QuizDifficulty.Hard, QuizDifficulty.Expert
            )
        }

    /** Drabinka trzech pytan otwierajacych walke na polu mapy. Boss podnosi poprzeczke. */
    fun battleLadder(kind: BattleKind): List<QuizDifficulty> = when (this) {
        Easy -> when (kind) {
            BattleKind.Normal -> listOf(QuizDifficulty.Easy, QuizDifficulty.Easy, QuizDifficulty.Medium)
            BattleKind.Elite -> listOf(QuizDifficulty.Easy, QuizDifficulty.Medium, QuizDifficulty.Medium)
            BattleKind.Boss -> listOf(QuizDifficulty.Medium, QuizDifficulty.Medium, QuizDifficulty.Hard)
        }
        Normal -> when (kind) {
            BattleKind.Normal -> listOf(QuizDifficulty.Easy, QuizDifficulty.Medium, QuizDifficulty.Medium)
            BattleKind.Elite -> listOf(QuizDifficulty.Medium, QuizDifficulty.Medium, QuizDifficulty.Hard)
            BattleKind.Boss -> listOf(QuizDifficulty.Medium, QuizDifficulty.Hard, QuizDifficulty.Expert)
        }
        Hard -> when (kind) {
            BattleKind.Normal -> listOf(QuizDifficulty.Medium, QuizDifficulty.Hard, QuizDifficulty.Hard)
            BattleKind.Elite -> listOf(QuizDifficulty.Hard, QuizDifficulty.Hard, QuizDifficulty.Expert)
            BattleKind.Boss -> listOf(QuizDifficulty.Hard, QuizDifficulty.Expert, QuizDifficulty.Expert)
        }
    }
}
