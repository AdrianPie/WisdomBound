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
            id = "chrzest",
            prompt = "In which year did Poland adopt Christianity?",
            correct = "966",
            wrong = listOf("1000", "1025", "1050"),
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
            id = "niepodleglosc",
            prompt = "In which year did Poland regain independence?",
            correct = "1918",
            wrong = listOf("1920", "1945", "1989"),
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
            id = "grunwald",
            prompt = "Which battle was fought in 1410?",
            correct = "The Battle of Grunwald",
            wrong = listOf("The Battle of Legnica", "The Battle of Chocim", "The Battle of Vienna"),
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
            id = "chrobry",
            prompt = "Who was the first crowned King of Poland?",
            correct = "Boleslaw the Brave",
            wrong = listOf("Mieszko I", "Casimir the Great", "Wladyslaw the Elbow-high"),
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
            id = "ostatni_krol",
            prompt = "Who was the last King of Poland?",
            correct = "Stanislaw August Poniatowski",
            wrong = listOf("John III Sobieski", "Augustus III", "Stanislaw Leszczynski"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "wieden",
            prompt = "Who commanded the Christian forces at the Battle of Vienna in 1683?",
            correct = "John III Sobieski",
            wrong = listOf("Stephen Bathory", "Sigismund III Vasa", "Wladyslaw IV"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "konstytucja",
            prompt = "In which year was the Constitution of May 3 adopted?",
            correct = "1791",
            wrong = listOf("1793", "1795", "1789"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "rzym",
            prompt = "In which year did the Western Roman Empire fall?",
            correct = "476",
            wrong = listOf("410", "395", "527"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "piastowie",
            prompt = "Which dynasty ruled Poland first?",
            correct = "The Piasts",
            wrong = listOf("The Jagiellons", "The Vasas", "The Wettins"),
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
            id = "unia_lubelska",
            prompt = "In which year was the Union of Lublin signed?",
            correct = "1569",
            wrong = listOf("1385", "1466", "1596"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "hastings",
            prompt = "In which year was the Battle of Hastings?",
            correct = "1066",
            wrong = listOf("1215", "1096", "1013"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "konstantynopol",
            prompt = "In which year did the Ottomans capture Constantinople?",
            correct = "1453",
            wrong = listOf("1492", "1389", "1521"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
