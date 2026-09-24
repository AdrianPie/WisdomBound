package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** History - world and Polish. */
object HistoryQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "his_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.History,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask(
            id = "ii_wojna",
            prompt = "In which year did World War II break out?",
            correct = "1939",
            wrong = listOf("1914", "1945", "1918"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "ksiezyc",
            prompt = "Who was the first person to walk on the Moon?",
            correct = "Neil Armstrong",
            wrong = listOf("Buzz Aldrin", "Yuri Gagarin", "Michael Collins"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "mona_lisa",
            prompt = "Who painted the Mona Lisa?",
            correct = "Leonardo da Vinci",
            wrong = listOf("Michelangelo", "Raphael", "Pablo Picasso"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "piramidy",
            prompt = "In which country are the pyramids of Giza located?",
            correct = "Egypt",
            wrong = listOf("Mexico", "Iraq", "Sudan"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "mur",
            prompt = "In which year did the Berlin Wall fall?",
            correct = "1989",
            wrong = listOf("1991", "1985", "1993"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Medium ------------------------------ */
        ask(
            id = "i_wojna",
            prompt = "In which year did World War I break out?",
            correct = "1914",
            wrong = listOf("1912", "1918", "1939"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "gagarin",
            prompt = "Who was the first human to travel into space?",
            correct = "Yuri Gagarin",
            wrong = listOf("Neil Armstrong", "Alan Shepard", "Valentina Tereshkova"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "kolumb",
            prompt = "In which year did Christopher Columbus reach America?",
            correct = "1492",
            wrong = listOf("1453", "1500", "1488"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "waszyngton",
            prompt = "Who was the first President of the United States?",
            correct = "George Washington",
            wrong = listOf("Abraham Lincoln", "Thomas Jefferson", "John Adams"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "titanic",
            prompt = "In which year did the Titanic sink?",
            correct = "1912",
            wrong = listOf("1905", "1918", "1923"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "rewolucja_fr",
            prompt = "In which year did the French Revolution break out?",
            correct = "1789",
            wrong = listOf("1799", "1776", "1804"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Hard ------------------------------- */
        ask(
            id = "rzym",
            prompt = "In which year did the Western Roman Empire fall?",
            correct = "476",
            wrong = listOf("410", "395", "527"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "waterloo",
            prompt = "In which year did Napoleon lose the Battle of Waterloo?",
            correct = "1815",
            wrong = listOf("1812", "1805", "1821"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "druk",
            prompt = "Who popularized movable-type printing in Europe?",
            correct = "Johannes Gutenberg",
            wrong = listOf("Nicolaus Copernicus", "Leonardo da Vinci", "Galileo"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Expert ---------------------------- */
        ask(
            id = "westfalski",
            prompt = "In which year did the Peace of Westphalia end the Thirty Years' War?",
            correct = "1648",
            wrong = listOf("1618", "1659", "1667"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "oktawian",
            prompt = "Who was the first Roman emperor?",
            correct = "Augustus (Octavian)",
            wrong = listOf("Julius Caesar", "Nero", "Tiberius"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "machiavelli",
            prompt = "Who wrote the treatise 'The Prince'?",
            correct = "Niccolo Machiavelli",
            wrong = listOf("Dante Alighieri", "Erasmus of Rotterdam", "Thomas More"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "hastings",
            prompt = "In which year was the Battle of Hastings?",
            correct = "1066",
            wrong = listOf("1215", "1096", "1013"),
            difficulty = QuizDifficulty.Expert
        ),
    )
}
