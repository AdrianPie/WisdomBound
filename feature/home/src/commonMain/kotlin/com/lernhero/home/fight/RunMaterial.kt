package com.lernhero.home.fight

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.custom.CustomDeck

/**
 * Z czego wyprawa bedzie odpytywac gracza. Jedna rzecz naraz - i to jest cala zasada.
 *
 * Wczesniej wybor materialu byl DWOMA wyborami naraz: zakladka zrodla (wbudowany bank / wlasne)
 * plus wybor w srodku tej zakladki, a do tego osobny szczebel trudnosci pytan. Trzy decyzje przed
 * kazdym wejsciem do walki, z czego dwie gracz podejmowal raz i nigdy do nich nie wracal. Tutaj
 * zostaje JEDNA lista: temat, wlasna talia albo fiszki - kazda pozycja jest gotowym wyborem,
 * a nie krokiem do kolejnego menu.
 *
 * Trudnosc pytan znika z wyboru celowo: w wyprawie rosnie sama razem z fala (drabinka
 * [com.lernhero.domain.quiz.QuizBank.ladderFor] i poziom tematu), wiec staly szczebel walczyl
 * z krzywa, ktora i tak jest sercem tego trybu.
 *
 * [key] jest tekstem, bo wybor przezywa zmiane zakladki w `rememberSaveable` - a tam mieszcza sie
 * tylko proste typy. Format jest stabilny i czytelny w zapisie stanu, wiec da sie go debugowac
 * bez deserializatora.
 */
sealed interface RunMaterial {

    val key: String

    /** Losowe pytania z calego wbudowanego banku - wejscie bez zobowiazan, bez poziomu tematu. */
    data object Mixed : RunMaterial {
        override val key: String = "mixed"
    }

    /** Jeden temat z wbudowanego banku. To on ma poziom opanowania i mnoznik nagrod. */
    data class Topic(val category: QuizCategory) : RunMaterial {
        override val key: String = "topic:${category.name}"
    }

    /** Wlasna talia gracza - napisana recznie albo wygenerowana w Quizach. */
    data class Deck(val deckId: String) : RunMaterial {
        override val key: String = "deck:$deckId"
    }

    /** Fiszki zapisane w czytelni. */
    data object Words : RunMaterial {
        override val key: String = "words"
    }

    companion object {
        /** Odczyt z zapisanego stanu. Cokolwiek nierozpoznanego wraca jako [Mixed]. */
        fun parse(key: String?): RunMaterial = when {
            key == null -> Mixed
            key == Words.key -> Words
            key.startsWith("topic:") -> QuizCategory.entries
                .firstOrNull { it.name == key.removePrefix("topic:") }
                ?.let(::Topic)
                ?: Mixed

            key.startsWith("deck:") -> key.removePrefix("deck:")
                .takeIf { it.isNotBlank() }
                ?.let(::Deck)
                ?: Mixed

            else -> Mixed
        }
    }
}

/**
 * Talia stojaca za tym wyborem - albo null, gdy wybrano co innego, talia zniknela lub ma za malo
 * gotowych pytan.
 *
 * Sprawdzenie [CustomDeck.isPlayable] jest tu, a nie w samym [RunMaterial], bo wybor zapisuje sie
 * na dluzej niz zyje talia: gracz moze wybrac talie, wyjsc, skasowac w niej pytania i wrocic.
 * Wyprawa musi wtedy ruszyc z czegokolwiek, a nie z pustej puli.
 */
fun RunMaterial.deckIn(decks: List<CustomDeck>): CustomDeck? =
    (this as? RunMaterial.Deck)?.let { picked ->
        decks.firstOrNull { it.id == picked.deckId && it.isPlayable }
    }

/** Temat z wbudowanego banku, albo null - wtedy pytania lecą z calej puli albo z wlasnego materialu. */
fun RunMaterial.topic(): QuizCategory? = (this as? RunMaterial.Topic)?.category

/** Ikona przy nazwie - jeden znak, ten sam w pigulce na scenie i w liscie wyboru. */
fun RunMaterial.icon(): String = when (this) {
    RunMaterial.Mixed -> "🎲"
    is RunMaterial.Topic -> "📖"
    is RunMaterial.Deck -> "📝"
    RunMaterial.Words -> "🗂"
}

/**
 * Nazwa do pokazania. Talia, ktorej juz nie ma (albo nie da sie z niej grac), opisuje sie jako
 * mieszanka - dokladnie tym, czym bedzie w walce. Pigulka na scenie nie moze obiecywac czegos
 * innego niz to, co wystartuje po nacisnieciu START.
 */
fun RunMaterial.label(decks: List<CustomDeck>): String = when (this) {
    RunMaterial.Mixed -> "All topics"
    is RunMaterial.Topic -> category.label
    is RunMaterial.Deck -> deckIn(decks)?.displayName ?: "All topics"
    RunMaterial.Words -> "Saved words"
}
