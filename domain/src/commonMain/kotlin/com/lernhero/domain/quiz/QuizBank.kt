package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.AnimalQuestions
import com.lernhero.domain.quiz.bank.AnimalQuestionsExtra
import com.lernhero.domain.quiz.bank.AnimalQuestionsExtra2
import com.lernhero.domain.quiz.bank.BiologyQuestions
import com.lernhero.domain.quiz.bank.BiologyQuestionsExtra
import com.lernhero.domain.quiz.bank.BiologyQuestionsExtra2
import com.lernhero.domain.quiz.bank.CapitalQuestions
import com.lernhero.domain.quiz.bank.CapitalQuestionsExtra
import com.lernhero.domain.quiz.bank.CapitalQuestionsExtra2
import com.lernhero.domain.quiz.bank.ElementQuestions
import com.lernhero.domain.quiz.bank.ElementQuestionsExtra
import com.lernhero.domain.quiz.bank.ElementQuestionsExtra2
import com.lernhero.domain.quiz.bank.EverydayQuestions
import com.lernhero.domain.quiz.bank.FlagQuestions
import com.lernhero.domain.quiz.bank.GeographyQuestions
import com.lernhero.domain.quiz.bank.GeographyQuestionsExtra
import com.lernhero.domain.quiz.bank.GeographyQuestionsExtra2
import com.lernhero.domain.quiz.bank.GeographyQuestionsCompare
import com.lernhero.domain.quiz.bank.HistoryQuestions
import com.lernhero.domain.quiz.bank.HistoryQuestionsExtra
import com.lernhero.domain.quiz.bank.HistoryQuestionsExtra2
import com.lernhero.domain.quiz.bank.WorldHistoryQuestions
import com.lernhero.domain.quiz.bank.LiteratureQuestions
import com.lernhero.domain.quiz.bank.LiteratureQuestionsExtra
import com.lernhero.domain.quiz.bank.LiteratureQuestionsExtra2
import com.lernhero.domain.quiz.bank.MasteryDepthQuestions
import com.lernhero.domain.quiz.bank.MathQuestions
import com.lernhero.domain.quiz.bank.MathQuestionsExtra
import com.lernhero.domain.quiz.bank.MathQuestionsExtra2
import com.lernhero.domain.quiz.bank.MythologyQuestions
import com.lernhero.domain.quiz.bank.MythologyQuestionsExtra
import com.lernhero.domain.quiz.bank.MythologyQuestionsExtra2
import com.lernhero.domain.quiz.bank.HistoryQuestionsExtra3
import com.lernhero.domain.quiz.bank.LandmarkQuestions
import com.lernhero.domain.quiz.bank.LiteratureQuestionsExtra3
import com.lernhero.domain.quiz.bank.PolandQuestions
import com.lernhero.domain.quiz.bank.PolandQuestionsExtra
import com.lernhero.domain.quiz.bank.PolandQuestionsMoved
import com.lernhero.domain.quiz.bank.ScienceQuestions
import com.lernhero.domain.quiz.bank.ScienceQuestionsExtra
import com.lernhero.domain.quiz.bank.ScienceQuestionsExtra2
import com.lernhero.domain.quiz.bank.ScienceQuestionsCompare
import com.lernhero.domain.quiz.bank.SportQuestions
import com.lernhero.domain.quiz.bank.SportQuestionsExtra
import com.lernhero.domain.quiz.bank.SportQuestionsExtra2
import com.lernhero.domain.quiz.bank.SportQuestionsWorld
import com.lernhero.domain.quiz.bank.TechnologyQuestions
import com.lernhero.domain.quiz.bank.TechnologyQuestionsExtra
import com.lernhero.domain.quiz.bank.TechnologyQuestionsExtra2
import com.lernhero.domain.quiz.bank.TriviaQuestionsCulture
import com.lernhero.domain.quiz.bank.TriviaQuestionsNature
import com.lernhero.domain.quiz.bank.TriviaQuestionsExtremes
import com.lernhero.domain.quiz.bank.TriviaQuestionsMastery
import com.lernhero.domain.quiz.bank.TriviaQuestionsOrigins
import kotlin.random.Random

/**
 * Bank pytan i losowanie quizu.
 *
 * Quiz dziala jak drabinka w "Milionerach": pierwsze pytanie jest rozgrzewka, ostatnie potrafi
 * zabolec. Im dalsza fala, tym wyzej zaczyna sie drabinka - w fali 5 nie ma juz latwych pytan.
 */
object QuizBank {

    /** Pytania tekstowe. Flagi dochodza osobno, bo losuja sobie bledne odpowiedzi. */
    private val textEntries: List<QuizEntry> =
        BiologyQuestions.all +
            AnimalQuestions.all +
            HistoryQuestions.all +
            ElementQuestions.all +
            CapitalQuestions.all +
            ScienceQuestions.all +
            GeographyQuestions.all +
            SportQuestions.all +
            MathQuestions.all +
            LiteratureQuestions.all +
            TechnologyQuestions.all +
            MythologyQuestions.all +
            BiologyQuestionsExtra.all +
            AnimalQuestionsExtra.all +
            HistoryQuestionsExtra.all +
            ElementQuestionsExtra.all +
            CapitalQuestionsExtra.all +
            ScienceQuestionsExtra.all +
            GeographyQuestionsExtra.all +
            SportQuestionsExtra.all +
            MathQuestionsExtra.all +
            LiteratureQuestionsExtra.all +
            TechnologyQuestionsExtra.all +
            MythologyQuestionsExtra.all +
            BiologyQuestionsExtra2.all +
            AnimalQuestionsExtra2.all +
            HistoryQuestionsExtra2.all +
            ElementQuestionsExtra2.all +
            CapitalQuestionsExtra2.all +
            ScienceQuestionsExtra2.all +
            GeographyQuestionsExtra2.all +
            SportQuestionsExtra2.all +
            MathQuestionsExtra2.all +
            LiteratureQuestionsExtra2.all +
            TechnologyQuestionsExtra2.all +
            MythologyQuestionsExtra2.all +
            // Historia powszechna - druga noga banku, celowo bez watkow polskich.
            WorldHistoryQuestions.all +
            // Temat lokalny - w banku siedzi zawsze, ale do wyboru trafia tylko przy jezyku PL
            // (patrz QuizCategory.builtinFor). Bank zna wszystko, filtruje dopiero UI.
            PolandQuestions.all +
            PolandQuestionsExtra.all +
            // Druga tura przeprowadzki do tematu lokalnego - patrz [PolandQuestionsMoved].
            PolandQuestionsMoved.all +
            // Swiat poza Europa - dowazenie banku po wyjezdzie watkow polskich.
            HistoryQuestionsExtra3.all +
            LiteratureQuestionsExtra3.all +
            // Pytania porownawcze ("gdzie wiecej", odd one out) - format, w ktorym da sie wygrac
            // rozumowaniem, a nie tylko pamiecia. Patrz [GeographyQuestionsCompare].
            GeographyQuestionsCompare.all +
            ScienceQuestionsCompare.all +
            // Wiedza codzienna - kuchnia, dom, muzyka, gry. Patrz [EverydayQuestions].
            EverydayQuestions.all +
            // Partia po audycie banku: pytania "chce sie zapamietac" w miejsce wycietych
            // pytan-zerowek ("ile oczu ma czlowiek"). Patrz [TriviaQuestionsNature].
            TriviaQuestionsNature.all +
            TriviaQuestionsCulture.all +
            // Sport bez watkow lokalnych - dowazenie kategorii po przeniesieniu pytan o polskich
            // sportowcach do tematu polskiego. Patrz [SportQuestionsWorld].
            SportQuestionsWorld.all +
            // Zabytki, rzeki i nieoczywiste stolice - patrz [LandmarkQuestions].
            LandmarkQuestions.all +
            // Skad sie wziely nazwy, slowa i wynalazki - patrz [TriviaQuestionsOrigins].
            TriviaQuestionsOrigins.all +
            // Rekordy i skrajnosci - patrz [TriviaQuestionsExtremes].
            TriviaQuestionsExtremes.all +
            // Dowazenie gornych szczebli pod tryb opanowania tematu - patrz [TriviaQuestionsMastery].
            TriviaQuestionsMastery.all +
            // Po czternascie pytan Hard/Expert na KAZDY temat - dokonczenie tego samego dowazenia,
            // tym razem rozlozone po kategoriach po rowno. Patrz [MasteryDepthQuestions].
            MasteryDepthQuestions.all

    fun all(random: Random = Random.Default): List<QuizEntry> =
        textEntries + FlagQuestions.entries(random)

    /**
     * Drabinka trudnosci dla danej fali. Cztery pytania, coraz ostrzejsze - i coraz ostrzejsze
     * z kazda kolejna fala.
     */
    fun ladderFor(wave: Int): List<QuizDifficulty> = when (wave) {
        1 -> listOf(QuizDifficulty.Easy, QuizDifficulty.Easy, QuizDifficulty.Medium, QuizDifficulty.Medium)
        2 -> listOf(QuizDifficulty.Easy, QuizDifficulty.Medium, QuizDifficulty.Medium, QuizDifficulty.Hard)
        3 -> listOf(QuizDifficulty.Medium, QuizDifficulty.Medium, QuizDifficulty.Hard, QuizDifficulty.Hard)
        4 -> listOf(QuizDifficulty.Medium, QuizDifficulty.Hard, QuizDifficulty.Hard, QuizDifficulty.Expert)
        else -> listOf(QuizDifficulty.Hard, QuizDifficulty.Hard, QuizDifficulty.Expert, QuizDifficulty.Expert)
    }

    /**
     * Losuje quiz na dana fale: jedno pytanie na kazdy szczebel drabinki, bez powtorek.
     *
     * Gdy pula danego poziomu sie wyczerpie (bo gracz dostal juz wszystkie eksperckie),
     * schodzimy o szczebel nizej zamiast zwracac puste pytanie.
     */
    fun drawForWave(wave: Int, random: Random = Random.Default): List<QuizEntry> =
        draw(ladderFor(wave), category = null, random = random)

    /**
     * Losuje quiz wedlug zadanej drabinki, opcjonalnie tylko z jednej kategorii
     * (null = losowe pytania z calego banku). Gdy kategoria nie ma pytania na danym
     * szczeblu, bierzemy najblizszy poziom **z tej samej kategorii** - dopiero gdy
     * cala kategoria sie wyczerpie, siegamy do reszty banku.
     */
    fun draw(
        ladder: List<QuizDifficulty>,
        category: QuizCategory? = null,
        random: Random = Random.Default
    ): List<QuizEntry> {
        val everything = all(random)
        val preferred = if (category == null) everything else everything.filter { it.category == category }
        val preferredPool = preferred.groupBy { it.difficulty }
        val fullPool = everything.groupBy { it.difficulty }
        val used = mutableSetOf<String>()

        return ladder.mapNotNull { difficulty ->
            val entry = pickFrom(difficulty, preferredPool, used, random)
                ?: pickFrom(difficulty, fullPool, used, random)
            entry?.also { used += it.id }
        }
    }

    private fun pickFrom(
        difficulty: QuizDifficulty,
        pool: Map<QuizDifficulty, List<QuizEntry>>,
        used: Set<String>,
        random: Random
    ): QuizEntry? {
        // Kolejnosc szukania: zadany poziom, potem coraz latwiejsze, na koncu trudniejsze.
        val fallback = QuizDifficulty.entries.sortedBy { candidate ->
            kotlin.math.abs(candidate.ordinal - difficulty.ordinal)
        }

        fallback.forEach { level ->
            val candidates = pool[level].orEmpty().filter { it.id !in used }
            if (candidates.isNotEmpty()) return candidates[random.nextInt(candidates.size)]
        }
        return null
    }

    fun byDifficulty(difficulty: QuizDifficulty, random: Random = Random.Default): List<QuizEntry> =
        all(random).filter { it.difficulty == difficulty }

    fun byCategory(category: QuizCategory, random: Random = Random.Default): List<QuizEntry> =
        all(random).filter { it.category == category }
}
