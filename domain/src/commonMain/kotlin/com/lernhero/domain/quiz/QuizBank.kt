package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.AnimalQuestions
import com.lernhero.domain.quiz.bank.BiologyQuestions
import com.lernhero.domain.quiz.bank.CapitalQuestions
import com.lernhero.domain.quiz.bank.ElementQuestions
import com.lernhero.domain.quiz.bank.FlagQuestions
import com.lernhero.domain.quiz.bank.GeographyQuestions
import com.lernhero.domain.quiz.bank.HistoryQuestions
import com.lernhero.domain.quiz.bank.LiteratureQuestions
import com.lernhero.domain.quiz.bank.MathQuestions
import com.lernhero.domain.quiz.bank.MythologyQuestions
import com.lernhero.domain.quiz.bank.PolandQuestions
import com.lernhero.domain.quiz.bank.PopCultureQuestions
import com.lernhero.domain.quiz.bank.ScienceQuestions
import com.lernhero.domain.quiz.bank.SportQuestions
import com.lernhero.domain.quiz.bank.TechnologyQuestions
import kotlin.random.Random

/**
 * Bank pytan i losowanie quizu.
 *
 * Quiz dziala jak drabinka w "Milionerach": pierwsze pytanie jest rozgrzewka, ostatnie potrafi
 * zabolec. Im dalsza fala, tym wyzej zaczyna sie drabinka - w fali 5 nie ma juz latwych pytan.
 *
 * **Jeden temat = jeden plik TSV** w `domain/quizbank` (history.tsv, biology.tsv, ...), ulozony
 * szczeblami Easy -> Expert. Z niego `tools/quizbank.py generate` robi [BiologyQuestions],
 * [AnimalQuestions], ... - tych plikow Kotlin nie edytuje sie recznie. Nowe pytania dopisuje
 * `tools/quizbank.py add`, ktore najpierw szuka dubli w calym banku (instrukcja:
 * domain/quizbank/README.md). Bank rozrosl sie wczesniej do kilkudziesieciu plikow-partii, w ktorych
 * ten sam fakt siedzial po trzy razy, a nikt nie widzial calego tematu naraz.
 *
 * Zasady dla kazdego pytania (czesci pilnuja testy, patrz QuizBankTest i QuizBankAuditTest):
 * 1. **Ma zaskakiwac albo bawic.** Po dobrym pytaniu gracz chce komus opowiedziec, czego sie
 *    dowiedzial. Definicje z podrecznika ("czym jest homeostaza?"), fiszki ("symbol O to...?")
 *    i rachunki z tabliczki mnozenia nie wchodza.
 * 2. **Latwe to nie zerowe.** Easy to fakt, ktory wiekszosc doroslych zna, ale musi sobie
 *    przypomniec - a nie cos, czego nie da sie nie wiedziec ("ile nog ma pajak?").
 * 3. **Dystraktory z tej samej polki.** Kazda bledna odpowiedz musi byc wiarygodna; najlepiej
 *    popularnym bledem (Sydney jako stolica Australii).
 * 4. **Dlugosc nie zdradza odpowiedzi.** Jedna rozwlekla odpowiedz wsrod trzech krotkich to
 *    najtansza sciaga w kazdym quizie.
 * 5. **Jeden fakt - jedno pytanie**, takze miedzy typami (wybor, prawda/falsz, zaznacz-wszystkie).
 * 6. **Tresci znane tylko w Polsce** trafiaja do [QuizCategory.Poland], ktora widza wylacznie
 *    gracze z polskim jezykiem aplikacji.
 * 7. **Nic, co sie zdezaktualizuje za rok** ("aktualny mistrz", "najnowszy telefon") - chyba ze
 *    pytanie mowi wprost o konkretnym roku.
 *
 * Id pytan zachowuja historyczne prefiksy (bio2_, trv_, wld_...) - musza byc tylko unikalne.
 * Nigdzie nie sa zapisywane na koncie gracza, pamieta je jedynie [QuestionHistory] w pamieci.
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
            PopCultureQuestions.all +
            // Temat lokalny - w banku siedzi zawsze, ale do wyboru trafia tylko przy jezyku PL
            // (patrz QuizCategory.builtinFor). Bank zna wszystko, filtruje dopiero UI.
            PolandQuestions.all

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
        random: Random = Random.Default,
        seen: (String) -> Boolean = { false }
    ): List<QuizEntry> {
        val everything = all(random)
        val preferred = if (category == null) everything else everything.filter { it.category == category }
        val preferredPool = preferred.groupBy { it.difficulty }
        val fullPool = everything.groupBy { it.difficulty }
        val used = mutableSetOf<String>()

        // Najpierw pytania jeszcze nie zadane ([seen] - pamiec calej sesji, patrz QuestionHistory),
        // w temacie, a potem w calym banku. Dopiero gdy temat jest wyczerpany do cna, wraca
        // pytanie juz widziane - i to z tematu, bo gracz go wybral.
        return ladder.mapNotNull { difficulty ->
            val fresh: (QuizEntry) -> Boolean = { it.id !in used && !seen(it.id) }
            val unused: (QuizEntry) -> Boolean = { it.id !in used }
            val entry = pickFrom(difficulty, preferredPool, fresh, random)
                ?: (if (category == null) null else pickFrom(difficulty, fullPool, fresh, random))
                ?: pickFrom(difficulty, preferredPool, unused, random)
                ?: pickFrom(difficulty, fullPool, unused, random)
            entry?.also { used += it.id }
        }
    }

    private fun pickFrom(
        difficulty: QuizDifficulty,
        pool: Map<QuizDifficulty, List<QuizEntry>>,
        allowed: (QuizEntry) -> Boolean,
        random: Random
    ): QuizEntry? {
        // Kolejnosc szukania: zadany poziom, potem coraz latwiejsze, na koncu trudniejsze.
        val fallback = QuizDifficulty.entries.sortedBy { candidate ->
            kotlin.math.abs(candidate.ordinal - difficulty.ordinal)
        }

        fallback.forEach { level ->
            val candidates = pool[level].orEmpty().filter(allowed)
            if (candidates.isNotEmpty()) return candidates[random.nextInt(candidates.size)]
        }
        return null
    }

    fun byDifficulty(difficulty: QuizDifficulty, random: Random = Random.Default): List<QuizEntry> =
        all(random).filter { it.difficulty == difficulty }

    fun byCategory(category: QuizCategory, random: Random = Random.Default): List<QuizEntry> =
        all(random).filter { it.category == category }
}
