package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Math - numbers, shapes and quick mental arithmetic. */
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

        /* ------------------------------- Easy ------------------------------- */
        ask(
            id = "7x8",
            prompt = "What is 7 x 8?",
            correct = "56",
            wrong = listOf("54", "58", "64"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kwadrans",
            prompt = "How many minutes are in a quarter hour?",
            correct = "15",
            wrong = listOf("10", "20", "30"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "tuzin",
            prompt = "How many items make a dozen?",
            correct = "12",
            wrong = listOf("10", "6", "20"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "9kw",
            prompt = "What is 9 squared?",
            correct = "81",
            wrong = listOf("72", "89", "91"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Medium ------------------------------ */
        ask(
            id = "pi",
            prompt = "What is the approximate value of pi?",
            correct = "3.14",
            wrong = listOf("3.41", "2.71", "3.04"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "liczba_pierwsza",
            prompt = "Which of these numbers is prime?",
            correct = "17",
            wrong = listOf("15", "21", "27"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "kat_prosty",
            prompt = "How many degrees is a right angle?",
            correct = "90",
            wrong = listOf("45", "180", "60"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "procent",
            prompt = "What is 20% of 150?",
            correct = "30",
            wrong = listOf("25", "35", "20"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "szesc_szescian",
            prompt = "How many faces does a cube have?",
            correct = "6",
            wrong = listOf("8", "4", "12"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "rzym_l",
            prompt = "What number does the Roman numeral L represent?",
            correct = "50",
            wrong = listOf("100", "40", "500"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Hard ------------------------------- */
        ask(
            id = "silnia_5",
            prompt = "What is 5! (5 factorial)?",
            correct = "120",
            wrong = listOf("100", "60", "150"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "pitagoras",
            prompt = "A triangle with legs of 3 and 4 has a hypotenuse of...",
            correct = "5",
            wrong = listOf("6", "7", "4.5"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "suma_katow",
            prompt = "What is the sum of angles in a triangle?",
            correct = "180 degrees",
            wrong = listOf("360 degrees", "90 degrees", "270 degrees"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "fibonacci",
            prompt = "What number comes next in the sequence 1, 1, 2, 3, 5, 8...?",
            correct = "13",
            wrong = listOf("11", "12", "16"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "log_100",
            prompt = "What is the base-10 logarithm of 100?",
            correct = "2",
            wrong = listOf("10", "1", "100"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Expert ---------------------------- */
        ask(
            id = "liczba_e",
            prompt = "What is the approximate value of e?",
            correct = "2.72",
            wrong = listOf("3.14", "1.62", "2.24"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "liczby_doskonale",
            prompt = "Which of these numbers is perfect (equal to the sum of its divisors)?",
            correct = "28",
            wrong = listOf("24", "30", "36"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "duzy_pierwszy",
            prompt = "Which of these numbers is NOT prime?",
            correct = "91",
            wrong = listOf("89", "97", "83"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
