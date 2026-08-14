package com.lernhero.game.battle

import com.lernhero.domain.combat.live.CycleOutcome
import com.lernhero.domain.combat.live.StreakTracker
import com.lernhero.domain.combat.playback.HitKind
import com.lernhero.game.character.CharacterUiModel
import org.jetbrains.compose.resources.DrawableResource

typealias CharacterId = String

/**
 * Ulotny efekt bojowy przypiety do walczacego: pocisk lecacy od [sourceId] do [targetId], a potem
 * wybuch i pływajaca liczba obrazen na celu. [sourceId] = null oznacza efekt bez zrodla (np. ulti
 * bijace w cala fale naraz) - wtedy leci prosto do wybuchu, bez lotu pocisku.
 *
 * [id] rosnie monotonicznie, wiec dwa te same trafienia nie zleja sie w jeden efekt.
 */
data class FloatingEffect(
    val id: Long,
    val targetId: String,
    val amount: Int,
    val kind: HitKind,
    val sourceId: String? = null
)

enum class GamePhase {
    /** Fale nadciagaja az do [com.lernhero.domain.run.BattleRequest.waves], quiz karmi dopalacz i passe. */
    Battle,
    /** Cala druzyna padla - walka skonczona, czas rozliczyc lup. */
    Defeat,
    /** Ostatnia fala oczyszczona - walka wygrana, caly lup wraca na konto. */
    Victory
}

/**
 * Faza biezacej tury: [Question] - walka stoi, czeka na odpowiedz, dol ekranu jest rozszerzony;
 * [Resolving] - odpowiedz padla, druzyny wymieniaja ciosy, pole bitwy jest rozszerzone.
 */
enum class TurnPhase { Question, Resolving }

/** Efekt zamknietego cyklu dziesieciu odpowiedzi - trzyma sie przez [expiresAtMs] (czas gry). */
data class CycleBuffState(
    val outcome: CycleOutcome,
    val expiresAtMs: Long
)

data class BattleUiState(
    val phase: GamePhase = GamePhase.Battle,
    /** Faza biezacej tury - steruje tym, czy dol ekranu (quiz) czy gora (pole bitwy) jest rozszerzona. */
    val turnPhase: TurnPhase = TurnPhase.Question,
    /** Numer biezacej fali - rosnie bez konca, to endless run. */
    val wave: Int = 1,
    val rewardCoins: Int = 0,
    val rewardSaved: Boolean = false,
    /** Suma punktow z poprawnych odpowiedzi w tym locie - przeklada sie na monety na koniec. */
    val totalScore: Int = 0,
    /** Ile pytan pojawilo sie w tym locie do tej pory - licznik pokazywany w gornym rogu HUD. */
    val answeredCount: Int = 0,
    val elapsedMs: Long = 0L,
    val characters: Map<CharacterId, CharacterUiModel> = emptyMap(),
    /** Dziesiec kratek ostatnich odpowiedzi + biezaca seria z rzedu (dla ulti). */
    val streak: StreakTracker = StreakTracker(),
    /** Buff/debuff z ostatnio zamknietego cyklu dziesieciu odpowiedzi, jesli jeszcze aktywny. */
    val activeCycleBuff: CycleBuffState? = null,
    /** Rosnace id - kazda zmiana odpala na ekranie krotki rozblysk ulti. Zero = jeszcze nic. */
    val ultiPulseId: Long = 0L,
    val quiz: LiveQuizState? = null,
    /** Pływajace liczby obrazen czekajace na odegranie. */
    val floatingEffects: List<FloatingEffect> = emptyList(),
    val message: String? = null
)

/** Aktualnie aktywny mnoznik z zamknietego cyklu dziesieciu odpowiedzi (Empowered/Weakened/Neutral), jesli jeszcze trwa. */
fun BattleUiState.activeCycleMultiplier(): Float =
    activeCycleBuff?.takeIf { elapsedMs < it.expiresAtMs }?.outcome?.attackMultiplier ?: 1f

/**
 * Jedno pytanie tury, podmieniane po kazdej rundzie ciosow. Pasek czasu na odpowiedz liczony jest
 * osobno w widoku (patrz [com.lernhero.game.battle.component.AnswerCountdownBar]) i w view modelu
 * (od momentu narysowania pytania) - tu trzymamy tylko sama tresc pytania i wynik.
 */
data class LiveQuizState(
    val question: QuizQuestion,
    val selectedAnswerId: String? = null,
    val revealed: Boolean = false
) {
    val correctAnswer: QuizAnswer?
        get() = question.answers.firstOrNull { it.isCorrect }

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
    val reward: Int
)

data class QuizAnswer(
    val id: String,
    val text: String,
    val isCorrect: Boolean
)
