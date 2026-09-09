package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Sports - soccer, the Olympics, records. */
object SportQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "spo_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Sport,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask(
            id = "druzyna_pilka",
            prompt = "How many players does a soccer team have on the field?",
            correct = "11",
            wrong = listOf("10", "12", "9"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kolarstwo",
            prompt = "The Tour de France is a race in which sport?",
            correct = "Cycling",
            wrong = listOf("Running", "Rowing", "Car rallying"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kosz",
            prompt = "How many points is a shot from behind the line worth in basketball?",
            correct = "3",
            wrong = listOf("2", "4", "1"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "olimpiada_kolka",
            prompt = "How many rings does the Olympic flag have?",
            correct = "5",
            wrong = listOf("4", "6", "7"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "szachy_figury",
            prompt = "Which chess piece moves only diagonally?",
            correct = "The bishop",
            wrong = listOf("The rook", "The knight", "The queen"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Medium ------------------------------ */
        ask(
            id = "mundial_2018",
            prompt = "Who won the soccer World Cup in 2018?",
            correct = "France",
            wrong = listOf("Croatia", "Brazil", "Germany"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "lewandowski",
            prompt = "At which club did Robert Lewandowski become the Bundesliga's top scorer?",
            correct = "Bayern Munich",
            wrong = listOf("Real Madrid", "Manchester United", "Juventus"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "hokej",
            prompt = "How long is one period in an ice hockey game?",
            correct = "20 minutes",
            wrong = listOf("15 minutes", "25 minutes", "30 minutes"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "siatkowka_sety",
            prompt = "How many sets won does it take to win a volleyball match?",
            correct = "3",
            wrong = listOf("2", "4", "5"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Hard ------------------------------- */
        ask(
            id = "bolt",
            prompt = "What is Usain Bolt's world record in the 100 m?",
            correct = "9.58 s",
            wrong = listOf("9.69 s", "9.72 s", "9.49 s"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "olimpiada_1896",
            prompt = "Where were the first modern Olympic Games held?",
            correct = "Athens",
            wrong = listOf("Paris", "Rome", "London"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "wimbledon",
            prompt = "What surface is Wimbledon played on?",
            correct = "Grass",
            wrong = listOf("Sand", "Concrete", "Clay and hard court, alternating"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "szachy_mistrz",
            prompt = "Which chess player held the world championship title the longest in the 20th century?",
            correct = "Emanuel Lasker",
            wrong = listOf("Garry Kasparov", "Bobby Fischer", "Anatoly Karpov"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Expert ---------------------------- */
        ask(
            id = "dekatlon",
            prompt = "Which event closes the men's Olympic decathlon on the second day?",
            correct = "The 1500 metres",
            wrong = listOf("The javelin throw", "The pole vault", "The 110 m hurdles"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "nba_wilt",
            prompt = "Who scored 100 points in a single NBA game?",
            correct = "Wilt Chamberlain",
            wrong = listOf("Michael Jordan", "Kobe Bryant", "LeBron James"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
