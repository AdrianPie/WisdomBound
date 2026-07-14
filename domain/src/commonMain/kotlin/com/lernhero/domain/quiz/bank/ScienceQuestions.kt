package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Nauka, kosmos i fizyka. */
object ScienceQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "sci_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Science,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Latwe ------------------------------- */
        ask(
            id = "merkury",
            prompt = "Ktora planeta jest najblizej Slonca?",
            correct = "Merkury",
            wrong = listOf("Wenus", "Ziemia", "Mars"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "planety",
            prompt = "Ile planet liczy Uklad Sloneczny?",
            correct = "8",
            wrong = listOf("9", "7", "10"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "satelita",
            prompt = "Jak nazywa sie naturalny satelita Ziemi?",
            correct = "Ksiezyc",
            wrong = listOf("Fobos", "Tytan", "Europa"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "mars",
            prompt = "Ktora planete nazywamy Czerwona Planeta?",
            correct = "Mars",
            wrong = listOf("Wenus", "Merkury", "Jowisz"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "richter",
            prompt = "Co mierzy skala Richtera?",
            correct = "Sile trzesien ziemi",
            wrong = listOf("Temperature", "Cisnienie", "Predkosc wiatru"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kat",
            prompt = "Ile stopni ma kat pelny?",
            correct = "360",
            wrong = listOf("180", "90", "270"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Srednie ------------------------------ */
        ask(
            id = "jowisz",
            prompt = "Ktora planeta Ukladu Slonecznego jest najwieksza?",
            correct = "Jowisz",
            wrong = listOf("Saturn", "Neptun", "Uran"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "swiatlo",
            prompt = "Jaka jest predkosc swiatla w prozni?",
            correct = "Okolo 300 000 km/s",
            wrong = listOf("Okolo 30 000 km/s", "Okolo 3 000 km/s", "Okolo 1 000 000 km/s"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "galaktyka",
            prompt = "Jak nazywa sie galaktyka, w ktorej lezy Uklad Sloneczny?",
            correct = "Droga Mleczna",
            wrong = listOf("Andromeda", "Galaktyka Trojkata", "Wielki Oblok Magellana"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "kopernik",
            prompt = "Kto udowodnil, ze to Ziemia kraza wokol Slonca, a nie odwrotnie?",
            correct = "Mikolaj Kopernik",
            wrong = listOf("Isaac Newton", "Galileo Galilei", "Johannes Kepler"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "grawitacja",
            prompt = "Kto sformulowal prawo powszechnego ciazenia?",
            correct = "Isaac Newton",
            wrong = listOf("Albert Einstein", "Nikola Tesla", "Michael Faraday"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Trudne ------------------------------- */
        ask(
            id = "einstein",
            prompt = "Kto sformulowal teorie wzglednosci?",
            correct = "Albert Einstein",
            wrong = listOf("Isaac Newton", "Niels Bohr", "Max Planck"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "penicylina",
            prompt = "Kto odkryl penicyline?",
            correct = "Alexander Fleming",
            wrong = listOf("Ludwik Pasteur", "Robert Koch", "Jonas Salk"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "sila",
            prompt = "Jaka jest jednostka sily w ukladzie SI?",
            correct = "Niuton",
            wrong = listOf("Dzul", "Wat", "Paskal"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "zero",
            prompt = "Ile wynosi zero absolutne w stopniach Celsjusza?",
            correct = "Okolo -273",
            wrong = listOf("Okolo -100", "Okolo -200", "Okolo -460"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "dzwiek",
            prompt = "W ktorym osrodku dzwiek rozchodzi sie najszybciej?",
            correct = "W ciele stalym",
            wrong = listOf("W wodzie", "W powietrzu", "W prozni"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Eksperckie ---------------------------- */
        ask(
            id = "kwant",
            prompt = "Kto zapoczatkowal teorie kwantow?",
            correct = "Max Planck",
            wrong = listOf("Albert Einstein", "Werner Heisenberg", "Erwin Schrodinger"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "entropia",
            prompt = "Ktora zasada termodynamiki mowi o wzroscie entropii?",
            correct = "Druga",
            wrong = listOf("Pierwsza", "Trzecia", "Zerowa"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "hubble",
            prompt = "Kto odkryl, ze Wszechswiat sie rozszerza?",
            correct = "Edwin Hubble",
            wrong = listOf("Albert Einstein", "Carl Sagan", "Georges Lemaitre"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "czarna_dziura",
            prompt = "Jak nazywa sie granica czarnej dziury, zza ktorej nic nie ucieknie?",
            correct = "Horyzont zdarzen",
            wrong = listOf("Osobliwosc", "Dysk akrecyjny", "Granica Roche'a"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "neutrino",
            prompt = "Ktora czastka niemal nie oddzialuje z materia i przelatuje przez Ziemie?",
            correct = "Neutrino",
            wrong = listOf("Proton", "Elektron", "Foton"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
