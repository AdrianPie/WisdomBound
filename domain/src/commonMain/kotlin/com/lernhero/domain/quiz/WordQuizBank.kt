package com.lernhero.domain.quiz

import com.lernhero.domain.reading.SavedWord
import com.lernhero.domain.reading.VocabularyDeck
import kotlin.random.Random

/**
 * Zamienia fiszki zapisane w czytniku na pytania quizowe: pokazujemy angielskie slowo (i zdanie,
 * w ktorym gracz je spotkal, jako podpowiedz-kontekst w tresci pytania), gracz wybiera poprawne
 * polskie tlumaczenie sposrod czterech.
 *
 * Zyje w domenie, nie w czytniku - to jest zwykly [QuizEntry], wiec walka nie musi wiedziec,
 * ze pytanie pochodzi z fiszek, a nie z wbudowanego banku.
 */
object WordQuizBank {

    /** Ile roznych przetlumaczonych slow trzeba miec zapisanych, zeby quiz mial z czego losowac. */
    const val MIN_WORDS = 4

    fun eligible(words: List<SavedWord>): List<SavedWord> =
        words
            .filter { !it.isSentence && it.translation.isNotBlank() && !it.text.containsWhitespace() }
            .distinctBy { it.text.lowercase() }

    // Dodatkowa siatka bezpieczenstwa obok [SavedWord.isSentence]: fiszki zapisane przed
    // wprowadzeniem tej flagi maja ja domyslnie na `false`, wiec bez tego stare zdania nadal
    // wpadalyby do puli quizu jako "slowo". Prawdziwe slowo nigdy nie ma w sobie spacji.
    private fun String.containsWhitespace(): Boolean = any { it.isWhitespace() }

    /** Czy jest wystarczajaco fiszek, zeby zaczac endless na wlasnych slowkach. */
    fun hasEnoughWords(words: List<SavedWord>): Boolean = eligible(words).size >= MIN_WORDS

    /**
     * Losuje jedno pytanie. Null, gdy fiszek jest za malo na cztery rozne odpowiedzi
     * (wywolujacy powinien wtedy sprawdzic [hasEnoughWords] wczesniej i w ogole nie proponowac
     * tego trybu, ale tu i tak nie zwracamy pytania, ktorego nie da sie uczciwie zadac).
     */
    fun draw(words: List<SavedWord>, random: Random = Random.Default): QuizEntry? {
        val pool = eligible(words)
        if (pool.size < MIN_WORDS) return null

        // Pytamy o slowa, ktorych gracz sie JESZCZE uczy - opanowane zostaja w puli tylko jako
        // dystraktory. Inaczej im dluzej gra, tym czesciej quiz odpytywalby go z rzeczy, ktore
        // juz umie, i tym rzadziej z tych, na ktorych mu zalezy. Gdy cala pula jest opanowana,
        // wracamy do niej w calosci - lepsza powtorka niz brak pytania.
        val target = (VocabularyDeck.learning(pool).ifEmpty { pool }).random(random)
        val wrong = pool
            .filter { it.id != target.id && it.translation != target.translation }
            .shuffled(random)
            .distinctBy { it.translation }
            .take(3)
            .map { it.translation }

        // Nawet po odsianiu duplikatow tlumaczen moze zabraknac trzech dystraktorow (np. wiele
        // slow ma to samo tlumaczenie) - wtedy nie udajemy quizu z czterema opcjami.
        if (wrong.size < 3) return null

        return QuizEntry(
            id = "$ENTRY_PREFIX${target.id}",
            prompt = wordPrompt(target),
            correct = target.translation,
            wrong = wrong,
            category = QuizCategory.Vocabulary,
            difficulty = QuizDifficulty.Easy
        )
    }

    /** Prefiks [QuizEntry.id] pytan ze slowek - patrz [savedWordIdOf]. */
    private const val ENTRY_PREFIX = "word_"

    /**
     * Wyciaga id fiszki z id pytania - null, gdy pytanie nie pochodzi ze slowek. Dzieki temu walka
     * moze odnotowac wynik na konkretnym slowku, nie wiedzac nic o [SavedWord]: dostaje samo id
     * pytania, ktore i tak juz ma.
     */
    fun savedWordIdOf(quizEntryId: String): String? =
        quizEntryId.removePrefix(ENTRY_PREFIX).takeIf { it != quizEntryId && it.isNotBlank() }

    private fun wordPrompt(word: SavedWord): String =
        if (word.sentence.isBlank()) {
            "Co znaczy \"${word.text}\"?"
        } else {
            "Co znaczy \"${word.text}\" w tym zdaniu?\n\"${word.sentence}\""
        }
}
