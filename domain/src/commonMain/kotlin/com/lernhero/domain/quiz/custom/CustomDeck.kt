package com.lernhero.domain.quiz.custom

import kotlinx.serialization.Serializable

/** Skad wzielo sie pytanie w talii - czysto informacyjne, ale gracz chce to widziec na liscie. */
@Serializable
enum class DeckOrigin(val label: String) {
    Manual("Manual"),
    Ai("From AI"),
    /** Wygenerowana przez model i pozniej poprawiona recznie - najczestszy przypadek docelowo. */
    AiEdited("AI + edits")
}

/**
 * Talia pytan napisana przez gracza (albo wygenerowana przez model i przez niego poprawiona).
 *
 * To jest odpowiedz na pytanie "chce cwiczyc na prawo jazdy, a nie na stolice Bhutanu": endless
 * pyta wtedy WYLACZNIE z tej talii, dokladnie tak, jak juz robi to z fiszkami z czytelni
 * (patrz [com.lernhero.domain.quiz.WordQuizBank]).
 *
 * Talia zyje we WLASNYM dokumencie (`player/{uid}/decks/{deckId}`), nie w dokumencie gracza -
 * setka pytan z AI to kilkadziesiat kilobajtow, a dokument gracza i tak niesie juz fiszki,
 * kolekcje i postep.
 */
@Serializable
data class CustomDeck(
    val id: String,
    val name: String = "",
    /** Temat podany przy generowaniu ("prawo jazdy kat. B") - wraca jako podpowiedz przy dogenerowaniu. */
    val topic: String = "",
    val origin: DeckOrigin = DeckOrigin.Manual,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val questions: List<CustomQuestion> = emptyList()
) {

    /** Pytania, ktore da sie uczciwie zadac - reszta czeka w edytorze na dokonczenie. */
    val ready: List<CustomQuestion> get() = questions.filter { it.isValid }

    val brokenCount: Int get() = questions.size - ready.size

    /**
     * Pytania na zwykla ture walki. "Oszacuj" tu nie wchodzi - w calej grze pada wylacznie jako
     * zdarzenie elity/bossa, a nie jako pytanie tury (patrz BattleViewModel).
     */
    val turnReady: List<CustomQuestion>
        get() = ready.filter { it.type != CustomQuestionType.Estimation }

    /** Czy da sie z tego zagrac endless - poniżej progu quiz zaczalby powtarzac to samo pytanie. */
    val isPlayable: Boolean get() = turnReady.size >= MIN_QUESTIONS

    val displayName: String get() = name.ifBlank { "Untitled deck" }

    fun withQuestion(question: CustomQuestion): CustomDeck {
        val index = questions.indexOfFirst { it.id == question.id }
        return copy(
            questions = if (index >= 0) {
                questions.toMutableList().also { it[index] = question }
            } else {
                questions + question
            }
        )
    }

    fun withoutQuestion(questionId: String): CustomDeck =
        copy(questions = questions.filterNot { it.id == questionId })

    companion object {
        /** Ile gotowych pytan musi byc, zeby ruszyc walke na tej talii. */
        const val MIN_QUESTIONS = 4

        /** Sufit na talie - trzymamy dokument Firestore z dala od limitu 1 MB. */
        const val MAX_QUESTIONS = 300

        /** Ile talii moze miec gracz - zeby lista nie zamienila sie w skladowisko prob z AI. */
        const val MAX_DECKS = 50
    }
}
