package com.lernhero.home.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.ai.QuizGenerationRequest
import com.lernhero.domain.quiz.ai.QuizGenerationResult
import com.lernhero.domain.quiz.ai.QuizGenerator
import com.lernhero.domain.quiz.custom.CustomDeck
import com.lernhero.domain.quiz.custom.CustomQuestion
import com.lernhero.domain.quiz.custom.CustomQuestionType
import com.lernhero.domain.quiz.custom.DeckOrigin
import com.lernhero.domain.repository.PlayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class CustomQuizUiState(
    val loading: Boolean = true,
    val decks: List<CustomDeck> = emptyList(),
    /**
     * Talia otwarta w edytorze. Zmiany ida TUTAJ, a nie prosto do Firestore - inaczej kazde
     * nacisniecie klawisza w tresci pytania bylby osobnym zapisem do bazy.
     */
    val draft: CustomDeck? = null,
    val dirty: Boolean = false,
    val generating: Boolean = false,
    /** Co model odrzucil przy ostatnim generowaniu - wisi pod lista, dopoki gracz nie zamknie. */
    val warnings: List<String> = emptyList(),
    val message: String? = null,
    /**
     * Stan tygodniowego limitu generowania - null, dopoki gracz nic jeszcze nie wygenerowal w tej
     * sesji. Serwer jest jedynym, ktory to liczy (patrz [QuizGenerationResult.Generated]) - apka
     * tylko wyswietla ostatnia wartosc, jaka dostala.
     */
    val remainingAiGenerationsThisWeek: Int? = null
)

/**
 * Wlasne talie pytan: lista, edytor i generowanie przez model.
 *
 * Talie leza w podkolekcji, a nie w dokumencie gracza (patrz [PlayerRepository.getCustomDecks]),
 * wiec maja tez wlasny ViewModel zamiast doklejac sie do [com.lernhero.home.player.PlayerViewModel] -
 * ekran "Walcz" i tak potrzebuje z nich tylko listy do wyboru.
 */
class CustomQuizViewModel(
    private val playerRepository: PlayerRepository,
    private val generator: QuizGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomQuizUiState())
    val uiState = _uiState.asStateFlow()

    /** Czy w ogole pokazywac przycisk generowania - patrz [QuizGenerator.Stub]. */
    val aiAvailable: Boolean get() = generator.isAvailable

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val decks = playerRepository.getCustomDecks()
            _uiState.update { it.copy(loading = false, decks = decks) }
        }
    }

    /**
     * Nowa, jeszcze niezapisana talia - zwraca jej id, zeby ekran mogl od razu przejsc do edytora.
     * Do bazy trafia dopiero przy [save], wiec kliknicie "nowa" i cofniecie nie zostawia smiecia.
     */
    fun newDeck(): String {
        val deck = CustomDeck(
            id = "deck_${now()}",
            createdAt = now(),
            updatedAt = now()
        )
        _uiState.update { it.copy(draft = deck, dirty = false, warnings = emptyList()) }
        return deck.id
    }

    /** Otwiera talie w edytorze. Gdy w drafcie siedzi juz ta sama talia, zostawia go nietknietego. */
    fun open(deckId: String) {
        val current = _uiState.value
        if (current.draft?.id == deckId) return
        val deck = current.decks.firstOrNull { it.id == deckId }
        _uiState.update { it.copy(draft = deck, dirty = false, warnings = emptyList()) }
    }

    fun rename(name: String) = editDraft { it.copy(name = name.take(MAX_NAME_LENGTH)) }

    fun setTopic(topic: String) = editDraft { it.copy(topic = topic) }

    fun upsertQuestion(question: CustomQuestion) = editDraft { deck ->
        if (deck.questions.none { it.id == question.id } && deck.questions.size >= CustomDeck.MAX_QUESTIONS) {
            message("Deck can hold at most ${CustomDeck.MAX_QUESTIONS} questions.")
            deck
        } else {
            deck.withQuestion(question)
        }
    }

    fun removeQuestion(questionId: String) = editDraft { it.withoutQuestion(questionId) }

    /** Puste pytanie danego typu - edytor dostaje gotowy szkielet, nie buduje go sam. */
    fun blankQuestion(type: CustomQuestionType, difficulty: QuizDifficulty): CustomQuestion =
        CustomQuestion.blank(id = "q_${now()}_${questionCounter++}", type = type, difficulty = difficulty)

    /**
     * Zapisuje talie z edytora. Bez nazwy i bez pytan nie ma czego zapisywac - taki draft po prostu
     * przepada, zeby lista nie zapelniala sie pustymi taliami po wejsciu i wyjsciu z edytora.
     */
    fun save(onDone: (Boolean) -> Unit = {}) {
        val draft = _uiState.value.draft
        if (draft == null || (draft.name.isBlank() && draft.questions.isEmpty())) {
            onDone(false)
            return
        }
        val toSave = draft.copy(
            name = draft.name.ifBlank { "Untitled deck" },
            updatedAt = now()
        )
        viewModelScope.launch(Dispatchers.IO) {
            val saved = playerRepository.saveCustomDeck(toSave)
            if (saved) {
                val decks = playerRepository.getCustomDecks()
                _uiState.update {
                    it.copy(decks = decks, draft = toSave, dirty = false, message = "Deck saved.")
                }
            } else {
                _uiState.update { it.copy(message = "Couldn't save the deck.") }
            }
            onDone(saved)
        }
    }

    fun delete(deckId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val removed = playerRepository.deleteCustomDeck(deckId)
            val decks = if (removed) playerRepository.getCustomDecks() else _uiState.value.decks
            _uiState.update { state ->
                state.copy(
                    decks = decks,
                    draft = if (state.draft?.id == deckId) null else state.draft,
                    message = if (removed) "Deck deleted." else "Couldn't delete the deck."
                )
            }
        }
    }

    /** Zamyka edytor bez zapisu - draft znika, lista zostaje taka, jaka byla w bazie. */
    fun closeEditor() {
        _uiState.update { it.copy(draft = null, dirty = false, warnings = emptyList()) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearWarnings() {
        _uiState.update { it.copy(warnings = emptyList()) }
    }

    /**
     * Dogenerowuje pytania do OTWARTEJ talii - nie podmienia jej. Dzieki temu "wygeneruj 10, popraw
     * dwa, dogeneruj jeszcze 10" jest zwyklym przebiegiem, a nie utrata poprzedniej pracy.
     */
    fun generate(
        topic: String,
        count: Int,
        difficulty: QuizDifficulty?,
        types: Set<CustomQuestionType>,
        notes: String
    ) {
        val draft = _uiState.value.draft ?: return
        if (topic.isBlank()) {
            message("Give the model a topic to ask about.")
            return
        }
        if (types.isEmpty()) {
            message("Select at least one question type.")
            return
        }

        _uiState.update { it.copy(generating = true, warnings = emptyList(), message = null) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = generator.generate(
                QuizGenerationRequest(
                    topic = topic,
                    count = count.coerceIn(QuizGenerationRequest.MIN_COUNT, QuizGenerationRequest.MAX_COUNT),
                    difficulty = difficulty,
                    types = types,
                    notes = notes
                )
            )
            _uiState.update { state ->
                val current = state.draft ?: draft
                when (result) {
                    is QuizGenerationResult.Generated -> {
                        val room = CustomDeck.MAX_QUESTIONS - current.questions.size
                        val added = result.questions.take(room.coerceAtLeast(0))
                        state.copy(
                            generating = false,
                            dirty = true,
                            draft = current.copy(
                                topic = current.topic.ifBlank { topic },
                                // Talia dotknieta przez gracza po generowaniu zostaje "AI + poprawki" -
                                // to jest ten przypadek, o ktory chodzi najczesciej.
                                origin = if (current.questions.isEmpty()) DeckOrigin.Ai else current.origin.afterAi(),
                                questions = current.questions + added
                            ),
                            warnings = result.warnings,
                            message = "Added ${added.size} questions - review and fix them before heading into battle.",
                            remainingAiGenerationsThisWeek = result.remainingThisWeek
                                ?: state.remainingAiGenerationsThisWeek
                        )
                    }

                    QuizGenerationResult.Unavailable -> state.copy(
                        generating = false,
                        message = "The model isn't connected yet - for now you can write questions by hand."
                    )

                    is QuizGenerationResult.Failed -> state.copy(
                        generating = false,
                        message = result.reason
                    )

                    is QuizGenerationResult.QuotaExceeded -> state.copy(
                        generating = false,
                        remainingAiGenerationsThisWeek = 0,
                        message = "You've used up this week's AI generations. Try again next week, " +
                            "or write questions by hand for now."
                    )
                }
            }
        }
    }

    private fun DeckOrigin.afterAi(): DeckOrigin = when (this) {
        DeckOrigin.Manual -> DeckOrigin.AiEdited
        DeckOrigin.Ai -> DeckOrigin.Ai
        DeckOrigin.AiEdited -> DeckOrigin.AiEdited
    }

    private fun editDraft(change: (CustomDeck) -> CustomDeck) {
        _uiState.update { state ->
            val draft = state.draft ?: return@update state
            val next = change(draft)
            // Talia zaczeta recznie po dolozeniu pytan z AI juz nie jest "wlasna" - to rozroznienie
            // robi sama generacja, tutaj tylko odnotowujemy, ze cos sie zmienilo.
            state.copy(draft = next, dirty = state.dirty || next != draft)
        }
    }

    private fun message(text: String) {
        _uiState.update { it.copy(message = text) }
    }

    private var questionCounter = 0

    private fun now(): Long = Clock.System.now().toEpochMilliseconds()

    private companion object {
        const val MAX_NAME_LENGTH = 60
    }
}
