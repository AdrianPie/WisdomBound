package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Sport - pilka nozna, olimpiady, rekordy. */
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

        /* ------------------------------- Latwe ------------------------------- */
        ask(
            id = "druzyna_pilka",
            prompt = "Ilu zawodnikow liczy druzyna pilki noznej na boisku?",
            correct = "11",
            wrong = listOf("10", "12", "9"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kolarstwo",
            prompt = "Tour de France to wyscig w jakiej dyscyplinie?",
            correct = "Kolarstwie",
            wrong = listOf("Biegach", "Wioslarstwie", "Rajdach samochodowych"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kosz",
            prompt = "Ile punktow daje rzut zza linii w koszykowce?",
            correct = "3",
            wrong = listOf("2", "4", "1"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "olimpiada_kolka",
            prompt = "Ile kolek ma flaga olimpijska?",
            correct = "5",
            wrong = listOf("4", "6", "7"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "tenis_narzedzie",
            prompt = "Czym odbija sie pilke w tenisie?",
            correct = "Rakieta",
            wrong = listOf("Kijem", "Paletka do ping-ponga", "Maczuga"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "szachy_figury",
            prompt = "Ktora figura w szachach porusza sie tylko po skosie?",
            correct = "Goniec",
            wrong = listOf("Wieza", "Skoczek", "Hetman"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Srednie ------------------------------ */
        ask(
            id = "mundial_2018",
            prompt = "Kto wygral pilkarskie mistrzostwa swiata w 2018 roku?",
            correct = "Francja",
            wrong = listOf("Chorwacja", "Brazylia", "Niemcy"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "maraton",
            prompt = "Ile kilometrow ma maraton?",
            correct = "Okolo 42",
            wrong = listOf("Okolo 50", "Okolo 30", "Okolo 21"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "malysz",
            prompt = "W jakiej dyscyplinie slynal Adam Malysz?",
            correct = "Skoki narciarskie",
            wrong = listOf("Biegi narciarskie", "Biathlon", "Snowboard"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "lewandowski",
            prompt = "W jakim klubie Robert Lewandowski zostal krolem strzelcow Bundesligi?",
            correct = "Bayern Monachium",
            wrong = listOf("Real Madryt", "Manchester United", "Juventus"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "hokej",
            prompt = "Na czym rozgrywa sie mecz hokeja na lodzie?",
            correct = "Na lodowisku",
            wrong = listOf("Na trawie", "Na parkiecie", "Na piasku"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "siatkowka_sety",
            prompt = "Do ilu wygranych setow gra sie mecz siatkowki?",
            correct = "3",
            wrong = listOf("2", "4", "5"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Trudne ------------------------------- */
        ask(
            id = "bolt",
            prompt = "Jaki jest rekord swiata Usaina Bolta na 100 m?",
            correct = "9,58 s",
            wrong = listOf("9,69 s", "9,72 s", "9,49 s"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "olimpiada_1896",
            prompt = "Gdzie odbyly sie pierwsze nowozytne igrzyska olimpijskie?",
            correct = "Ateny",
            wrong = listOf("Paryz", "Rzym", "Londyn"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "wimbledon",
            prompt = "Na jakiej nawierzchni rozgrywany jest Wimbledon?",
            correct = "Trawa",
            wrong = listOf("Macka", "Beton", "Ceglana maczka i beton na zmiane"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "f1_tor_pl",
            prompt = "Ktory Polak jako pierwszy wygral wyscig Formuly 1?",
            correct = "Robert Kubica",
            wrong = listOf("Krzysztof Holowczyc", "Kajetan Kajetanowicz", "Sobieslaw Zasada"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "szachy_mistrz",
            prompt = "Ktory szachista byl mistrzem swiata najdluzej w XX wieku?",
            correct = "Emanuel Lasker",
            wrong = listOf("Garri Kasparow", "Bobby Fischer", "Anatolij Karpow"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Eksperckie ---------------------------- */
        ask(
            id = "mundial_1974",
            prompt = "Ktore miejsce zajela Polska na mundialu w 1974 roku?",
            correct = "3",
            wrong = listOf("1", "2", "4"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "dekatlon",
            prompt = "Z ilu konkurencji sklada sie dziesieciobój?",
            correct = "10",
            wrong = listOf("8", "12", "7"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "nba_wilt",
            prompt = "Kto zdobyl 100 punktow w jednym meczu NBA?",
            correct = "Wilt Chamberlain",
            wrong = listOf("Michael Jordan", "Kobe Bryant", "LeBron James"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
