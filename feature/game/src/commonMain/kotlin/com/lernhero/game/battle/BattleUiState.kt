package com.lernhero.game.battle

import com.lernhero.domain.combat.playback.BattlePlaybackScript
import com.lernhero.domain.combat.playback.BattlePlaybackStep
import com.lernhero.game.character.CharacterState
import com.lernhero.game.character.CharacterUiModel
import org.jetbrains.compose.resources.DrawableResource

typealias CharacterId = String

enum class GamePhase {
    Battle,
    Quiz,
    Upgrade,
    Defeat
}

enum class BattleSide {
    Players,
    Enemies
}

data class BattleUiState(
    val phase: GamePhase = GamePhase.Battle,
    val wave: Int = 1,
    val rewardPoints: Int = 0,
    val rewardCoins: Int = 0,
    val rewardSaved: Boolean = false,
    val totalScore: Int = 0,
    val battleTurn: BattleSide = BattleSide.Players,
    val characters: Map<CharacterId, CharacterUiModel> = emptyMap(),
    val playback: BattlePlaybackUiState? = null,
    val quiz: QuizState? = null,
    val upgrade: UpgradeState? = null,
    val message: String? = null
)

data class BattlePlaybackUiState(
    val script: BattlePlaybackScript,
    val currentStepIndex: Int = 0
) {
    val currentStep: BattlePlaybackStep?
        get() = script.steps.getOrNull(currentStepIndex)

    val attackSteps: List<BattlePlaybackStep.Attack>
        get() = script.steps.filterIsInstance<BattlePlaybackStep.Attack>()

    val playedAttackCount: Int
        get() = script.steps
            .take(currentStepIndex)
            .count { step -> step is BattlePlaybackStep.Attack }

    val totalAttackCount: Int
        get() = attackSteps.size

    val progressLabel: String
        get() = "$playedAttackCount/$totalAttackCount"
}

data class QuizState(
    val questions: List<QuizQuestion>,
    val currentQuestionIndex: Int = 0,
    val correctAnswers: Int = 0,
    /** Punkty zebrane w tym quizie - trudniejsze pytania placa wiecej. */
    val earnedPoints: Int = 0,
    /** Co kliknal gracz. Null, gdy skonczyl mu sie czas. */
    val selectedAnswerId: String? = null,
    /**
     * Czy odslonilismy juz wynik. Przez chwile po odpowiedzi quiz stoi: poprawna odpowiedz
     * swieci na zielono, zle klikniecie na czerwono - zeby bylo wiadomo, jak bylo naprawde.
     */
    val revealed: Boolean = false
) {
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentQuestionIndex)

    val correctAnswer: QuizAnswer?
        get() = currentQuestion?.answers?.firstOrNull { it.isCorrect }

    /** Czy gracz trafil. Fałsz takze wtedy, gdy czas minal bez odpowiedzi. */
    val answeredCorrectly: Boolean
        get() = selectedAnswerId != null && selectedAnswerId == correctAnswer?.id
}

data class QuizQuestion(
    val id: String,
    val prompt: String,
    val flag: DrawableResource? = null,
    val answers: List<QuizAnswer>,
    /** "Trudne", "Eksperckie" - drabinka jak w Milionerach. */
    val difficulty: String,
    val category: String,
    val reward: Int,
    /** Ile sekund na odpowiedz. Pytanie o stolice Bhutanu dostaje wiecej czasu niz o Paryz. */
    val seconds: Int,
    val explanation: String? = null
)

data class QuizAnswer(
    val id: String,
    val text: String,
    val isCorrect: Boolean
)

data class UpgradeState(
    val choices: List<UpgradeChoice>,
    val selectedChoiceIds: Set<String> = emptySet()
)

data class UpgradeChoice(
    val id: String,
    val title: String,
    val description: String,
    val cost: Int,
    val rarity: UpgradeRarity,
    val type: UpgradeType
)

enum class UpgradeType {
    TeamHealth,
    TeamAttack,
    Shield,
    CriticalFocus,
    HealTeam
}

enum class UpgradeRarity(
    val label: String
) {
    Common("Common"),
    Rare("Rare"),
    Epic("Epic"),
    Legendary("Legendary")
}

/** Czy trwa animacja ataku. Dopoki trwa, odtwarzanie skryptu czeka. */
internal fun BattleUiState.isBattleBusy(): Boolean =
    characters.values.any { character ->
        character.state == CharacterState.MOVING_TO_ATK ||
            character.state == CharacterState.ATTACK ||
            character.state == CharacterState.MOVING_BACK
    }
