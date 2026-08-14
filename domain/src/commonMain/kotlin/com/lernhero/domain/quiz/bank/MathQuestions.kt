package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Matematyka - liczby, figury i szybkie rachunki. */
object MathQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "mat_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Math,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Latwe ------------------------------- */
        ask(
            id = "7x8",
            prompt = "Ile to 7 x 8?",
            correct = "56",
            wrong = listOf("54", "58", "64"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "trojkat_boki",
            prompt = "Ile bokow ma trojkat?",
            correct = "3",
            wrong = listOf("4", "2", "5"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "polowa_90",
            prompt = "Ile to polowa z 90?",
            correct = "45",
            wrong = listOf("40", "50", "35"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kwadrans",
            prompt = "Ile minut ma kwadrans?",
            correct = "15",
            wrong = listOf("10", "20", "30"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "tuzin",
            prompt = "Ile sztuk to tuzin?",
            correct = "12",
            wrong = listOf("10", "6", "20"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "9kw",
            prompt = "Ile to 9 do kwadratu?",
            correct = "81",
            wrong = listOf("72", "89", "91"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Srednie ------------------------------ */
        ask(
            id = "pi",
            prompt = "Jaka wartosc ma w przyblizeniu liczba pi?",
            correct = "3,14",
            wrong = listOf("3,41", "2,71", "3,04"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "liczba_pierwsza",
            prompt = "Ktora z tych liczb jest pierwsza?",
            correct = "17",
            wrong = listOf("15", "21", "27"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "kat_prosty",
            prompt = "Ile stopni ma kat prosty?",
            correct = "90",
            wrong = listOf("45", "180", "60"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "procent",
            prompt = "Ile to 20% ze 150?",
            correct = "30",
            wrong = listOf("25", "35", "20"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "szesc_szescian",
            prompt = "Ile scian ma szescian?",
            correct = "6",
            wrong = listOf("8", "4", "12"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "rzym_l",
            prompt = "Jaka liczbe oznacza rzymskie L?",
            correct = "50",
            wrong = listOf("100", "40", "500"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Trudne ------------------------------- */
        ask(
            id = "silnia_5",
            prompt = "Ile wynosi 5! (5 silnia)?",
            correct = "120",
            wrong = listOf("100", "60", "150"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "pitagoras",
            prompt = "Trojkat o bokach 3 i 4 ma przeciwprostokatna o dlugosci...",
            correct = "5",
            wrong = listOf("6", "7", "4,5"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "suma_katow",
            prompt = "Ile wynosi suma katow w trojkacie?",
            correct = "180 stopni",
            wrong = listOf("360 stopni", "90 stopni", "270 stopni"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "fibonacci",
            prompt = "Jaka liczba jest nastepna w ciagu 1, 1, 2, 3, 5, 8...?",
            correct = "13",
            wrong = listOf("11", "12", "16"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "log_100",
            prompt = "Ile wynosi logarytm dziesietny ze 100?",
            correct = "2",
            wrong = listOf("10", "1", "100"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Eksperckie ---------------------------- */
        ask(
            id = "liczba_e",
            prompt = "Jaka wartosc ma w przyblizeniu liczba e?",
            correct = "2,72",
            wrong = listOf("3,14", "1,62", "2,24"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "liczby_doskonale",
            prompt = "Ktora z tych liczb jest doskonala (rowna sumie swoich dzielnikow)?",
            correct = "28",
            wrong = listOf("24", "30", "36"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "duzy_pierwszy",
            prompt = "Ktora z tych liczb NIE jest pierwsza?",
            correct = "91",
            wrong = listOf("89", "97", "83"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
