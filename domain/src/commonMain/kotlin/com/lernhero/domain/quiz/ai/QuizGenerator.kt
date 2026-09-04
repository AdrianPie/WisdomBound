package com.lernhero.domain.quiz.ai

import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.custom.CustomQuestion
import com.lernhero.domain.quiz.custom.CustomQuestionType
import kotlinx.serialization.Serializable

/**
 * Zrodlo pytan generowanych przez model - dokladnie ten sam uklad, co
 * `TranslationSource` w czytniku: interfejs i miejsce w UI istnieja
 * od razu, a silnik podpina sie pozniej jedna klasa.
 *
 * Prawdziwa implementacja NIE trzyma klucza API w aplikacji - klucz w apce mobilnej dalby sie
 * z niej wyciagnac. Zyje wylacznie w `:server` (patrz `AnthropicQuizGenerator`), a apka woła
 * WYLACZNIE wlasny serwer (`RemoteQuizGenerator` w `:data`), nigdy dostawcy modelu wprost.
 *
 * [QuizGenerationRequest] i [QuizGenerationResult] sa @Serializable, bo to jest jednoczesnie
 * ksztalt wywolania w pamieci NA SERWERZE i format JSON requestu/odpowiedzi miedzy apka a
 * `:server` - jeden typ, jeden parser, zero osobnych DTO do zsynchronizowania.
 *
 * Cala trudna czesc - zamiana odpowiedzi modelu na pytania i odsianie tego, co przyszlo zle -
 * siedzi w [QuizGenerationParser] i jest przetestowana bez zadnej sieci.
 */
interface QuizGenerator {

    /** Czy da sie w ogole generowac - steruje widocznoscia przycisku w UI. */
    val isAvailable: Boolean get() = true

    suspend fun generate(request: QuizGenerationRequest): QuizGenerationResult

    /**
     * To, czego uzywa dzis apka: caly ekran generowania dziala, ale konczy sie uczciwym
     * "model nie jest jeszcze podpiety" zamiast udawac, ze cos wygenerowal.
     */
    object Stub : QuizGenerator {
        override val isAvailable: Boolean get() = false
        override suspend fun generate(request: QuizGenerationRequest): QuizGenerationResult =
            QuizGenerationResult.Unavailable
    }
}

/**
 * Zamowienie na talie: o czym, ile i jak trudno. [types] to typy, na ktore gracz sie zgodzil -
 * model dostaje je w prompcie, a [QuizGenerationParser] odsiewa wszystko poza nimi, gdyby model
 * dorzucil cos od siebie.
 */
@Serializable
data class QuizGenerationRequest(
    val topic: String,
    val count: Int = DEFAULT_COUNT,
    /** Null = wymieszane poziomy, jak drabinka w banku wbudowanym. */
    val difficulty: QuizDifficulty? = null,
    val types: Set<CustomQuestionType> = setOf(CustomQuestionType.Choice),
    val language: String = "English",
    /** Dodatkowe zyczenia gracza ("tylko znaki zakazu", "z odniesieniem do kodeksu drogowego"). */
    val notes: String = ""
) {
    companion object {
        const val DEFAULT_COUNT = 10
        const val MIN_COUNT = 1
        const val MAX_COUNT = 30
    }
}

@Serializable
sealed interface QuizGenerationResult {
    /**
     * Pytania, ktore przeszly walidacje. [warnings] mowi, ile i dlaczego odpadlo - gracz ma
     * wiedziec, ze prosil o 10, a dostal 8, zamiast domyslac sie tego z liczby wierszy.
     *
     * [remainingThisWeek] - stan limitu PO tym wywolaniu, doklejony przez serwer. Apka pokazuje
     * go w UI i NIE liczy limitu sama - patrz notatka przy [QuizGenerator].
     */
    @Serializable
    data class Generated(
        val questions: List<CustomQuestion>,
        val warnings: List<String> = emptyList(),
        val remainingThisWeek: Int? = null
    ) : QuizGenerationResult

    /** Model nie jest podpiety (patrz [QuizGenerator.Stub]). */
    @Serializable
    data object Unavailable : QuizGenerationResult

    @Serializable
    data class Failed(val reason: String) : QuizGenerationResult

    /** Limit tygodniowy wyczerpany - serwer odmowil, zanim w ogole zapytal model. */
    @Serializable
    data class QuotaExceeded(val resetsAt: Long) : QuizGenerationResult
}
