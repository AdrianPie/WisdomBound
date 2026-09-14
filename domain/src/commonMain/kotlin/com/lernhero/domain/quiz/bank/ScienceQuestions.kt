package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Science, space and physics. */
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

        /* ------------------------------- Easy ------------------------------- */
        ask(
            id = "merkury",
            prompt = "Which planet is closest to the Sun?",
            correct = "Mercury",
            wrong = listOf("Venus", "Earth", "Mars"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "planety",
            prompt = "How many planets does the Solar System have?",
            correct = "8",
            wrong = listOf("9", "7", "10"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "satelita",
            prompt = "What is Earth's natural satellite called?",
            correct = "The Moon",
            wrong = listOf("Phobos", "Titan", "Europa"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "mars",
            prompt = "Which planet do we call the Red Planet?",
            correct = "Mars",
            wrong = listOf("Venus", "Mercury", "Jupiter"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "richter",
            prompt = "What does the Richter scale measure?",
            correct = "Earthquake magnitude",
            wrong = listOf("Temperature", "Pressure", "Wind speed"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kat",
            prompt = "How many degrees is a full angle?",
            correct = "360",
            wrong = listOf("180", "90", "270"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Medium ------------------------------ */
        ask(
            id = "jowisz",
            prompt = "Which planet of the Solar System is the largest?",
            correct = "Jupiter",
            wrong = listOf("Saturn", "Neptune", "Uranus"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "swiatlo",
            prompt = "What is the speed of light in a vacuum?",
            correct = "About 186,000 mi/s (300,000 km/s)",
            wrong = listOf("About 18,600 mi/s (30,000 km/s)", "About 1,860 mi/s (3,000 km/s)", "About 620,000 mi/s (1,000,000 km/s)"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "galaktyka",
            prompt = "What is the galaxy the Solar System sits in called?",
            correct = "The Milky Way",
            wrong = listOf("Andromeda", "The Triangulum Galaxy", "The Large Magellanic Cloud"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "kopernik",
            prompt = "Who proved that the Earth orbits the Sun, not the other way around?",
            correct = "Nicolaus Copernicus",
            wrong = listOf("Isaac Newton", "Galileo Galilei", "Johannes Kepler"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "grawitacja",
            prompt = "Who formulated the law of universal gravitation?",
            correct = "Isaac Newton",
            wrong = listOf("Albert Einstein", "Nikola Tesla", "Michael Faraday"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Hard ------------------------------- */
        ask(
            id = "einstein",
            prompt = "Who formulated the theory of relativity?",
            correct = "Albert Einstein",
            wrong = listOf("Isaac Newton", "Niels Bohr", "Max Planck"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "penicylina",
            prompt = "Who discovered penicillin?",
            correct = "Alexander Fleming",
            wrong = listOf("Louis Pasteur", "Robert Koch", "Jonas Salk"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "sila",
            prompt = "What is the SI unit of force?",
            correct = "Newton",
            wrong = listOf("Joule", "Watt", "Pascal"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "zero",
            prompt = "What is absolute zero in degrees Celsius?",
            correct = "About -273",
            wrong = listOf("About -100", "About -200", "About -460"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "dzwiek",
            prompt = "In which medium does sound travel fastest?",
            correct = "In a solid",
            wrong = listOf("In water", "In air", "In a vacuum"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Expert ---------------------------- */
        ask(
            id = "kwant",
            prompt = "Who launched quantum theory?",
            correct = "Max Planck",
            wrong = listOf("Albert Einstein", "Werner Heisenberg", "Erwin Schrodinger"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "entropia",
            prompt = "Which law of thermodynamics states that entropy increases?",
            correct = "The second",
            wrong = listOf("The first", "The third", "The zeroth"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "hubble",
            prompt = "Who discovered that the Universe is expanding?",
            correct = "Edwin Hubble",
            wrong = listOf("Albert Einstein", "Carl Sagan", "Georges Lemaitre"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "czarna_dziura",
            prompt = "What is the boundary of a black hole called, beyond which nothing can escape?",
            correct = "The event horizon",
            wrong = listOf("The singularity", "The accretion disk", "The Roche limit"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "neutrino",
            prompt = "Which particle barely interacts with matter and passes straight through Earth?",
            correct = "Neutrino",
            wrong = listOf("Proton", "Electron", "Photon"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
