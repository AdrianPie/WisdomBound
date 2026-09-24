package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Geography - rivers, mountains, seas and continents. */
object GeographyQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "geo_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Geography,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask(
            id = "kontynenty",
            prompt = "How many continents are there on Earth?",
            correct = "7",
            wrong = listOf("5", "6", "8"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "sahara",
            prompt = "On which continent is the Sahara desert?",
            correct = "Africa",
            wrong = listOf("Asia", "Australia", "South America"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Medium ------------------------------ */
        ask(
            id = "everest",
            prompt = "In which mountain range is Mount Everest?",
            correct = "Himalayas",
            wrong = listOf("Andes", "Alps", "Caucasus"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "wulkan_wl",
            prompt = "What is the name of the famous volcano near Naples?",
            correct = "Vesuvius",
            wrong = listOf("Etna", "Stromboli", "Krakatoa"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Hard ------------------------------- */
        ask(
            id = "bajkal",
            prompt = "Which lake is the deepest in the world?",
            correct = "Baikal",
            wrong = listOf("Tanganyika", "Caspian Sea", "Superior"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "kilimandzaro",
            prompt = "In which country is Mount Kilimanjaro?",
            correct = "Tanzania",
            wrong = listOf("Kenya", "Ethiopia", "Uganda"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "aconcagua",
            prompt = "The highest peak in South America is...",
            correct = "Aconcagua",
            wrong = listOf("Chimborazo", "Huascaran", "Ojos del Salado"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Expert ---------------------------- */
        ask(
            id = "row_marianski",
            prompt = "What is the deepest point in the oceans called?",
            correct = "Challenger Deep",
            wrong = listOf("Tonga Trench", "Java Trench", "Milwaukee Deep"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "kraj_bez_rzek",
            prompt = "Which of these countries has not a single permanent river?",
            correct = "Saudi Arabia",
            wrong = listOf("Mongolia", "Kazakhstan", "Iran"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "titicaca",
            prompt = "Lake Titicaca lies on the border of which two countries?",
            correct = "Peru and Bolivia",
            wrong = listOf("Chile and Argentina", "Ecuador and Colombia", "Brazil and Paraguay"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
