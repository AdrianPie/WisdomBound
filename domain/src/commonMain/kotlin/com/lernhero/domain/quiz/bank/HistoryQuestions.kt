package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Historia - swiatowa i polska. */
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

        /* ------------------------------- Latwe ------------------------------- */
        ask(
            id = "ii_wojna",
            prompt = "W ktorym roku wybuchla II wojna swiatowa?",
            correct = "1939",
            wrong = listOf("1914", "1945", "1918"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "ksiezyc",
            prompt = "Kto jako pierwszy stanal na Ksiezycu?",
            correct = "Neil Armstrong",
            wrong = listOf("Buzz Aldrin", "Jurij Gagarin", "Michael Collins"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "mona_lisa",
            prompt = "Kto namalowal Mona Lise?",
            correct = "Leonardo da Vinci",
            wrong = listOf("Michal Aniol", "Rafael Santi", "Pablo Picasso"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "chrzest",
            prompt = "W ktorym roku Polska przyjela chrzest?",
            correct = "966",
            wrong = listOf("1000", "1025", "1050"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "piramidy",
            prompt = "W ktorym kraju znajduja sie piramidy w Gizie?",
            correct = "Egipt",
            wrong = listOf("Meksyk", "Irak", "Sudan"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "niepodleglosc",
            prompt = "W ktorym roku Polska odzyskala niepodleglosc?",
            correct = "1918",
            wrong = listOf("1920", "1945", "1989"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "mur",
            prompt = "W ktorym roku upadl Mur Berlinski?",
            correct = "1989",
            wrong = listOf("1991", "1985", "1993"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Srednie ------------------------------ */
        ask(
            id = "i_wojna",
            prompt = "W ktorym roku wybuchla I wojna swiatowa?",
            correct = "1914",
            wrong = listOf("1912", "1918", "1939"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "gagarin",
            prompt = "Kto jako pierwszy czlowiek polecial w kosmos?",
            correct = "Jurij Gagarin",
            wrong = listOf("Neil Armstrong", "Alan Shepard", "Walentyna Tierieszkowa"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "kolumb",
            prompt = "W ktorym roku Krzysztof Kolumb dotarl do Ameryki?",
            correct = "1492",
            wrong = listOf("1453", "1500", "1488"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "grunwald",
            prompt = "Ktora bitwe stoczono w 1410 roku?",
            correct = "Bitwe pod Grunwaldem",
            wrong = listOf("Bitwe pod Legnica", "Bitwe pod Chocimiem", "Bitwe pod Wiedniem"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "waszyngton",
            prompt = "Kto byl pierwszym prezydentem Stanow Zjednoczonych?",
            correct = "Jerzy Waszyngton",
            wrong = listOf("Abraham Lincoln", "Thomas Jefferson", "John Adams"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "chrobry",
            prompt = "Kto byl pierwszym koronowanym krolem Polski?",
            correct = "Boleslaw Chrobry",
            wrong = listOf("Mieszko I", "Kazimierz Wielki", "Wladyslaw Lokietek"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "titanic",
            prompt = "W ktorym roku zatonal Titanic?",
            correct = "1912",
            wrong = listOf("1905", "1918", "1923"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "rewolucja_fr",
            prompt = "W ktorym roku wybuchla rewolucja francuska?",
            correct = "1789",
            wrong = listOf("1799", "1776", "1804"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Trudne ------------------------------- */
        ask(
            id = "ostatni_krol",
            prompt = "Kto byl ostatnim krolem Polski?",
            correct = "Stanislaw August Poniatowski",
            wrong = listOf("Jan III Sobieski", "August III Sas", "Stanislaw Leszczynski"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "wieden",
            prompt = "Kto dowodzil wojskami chrzescijanskimi w bitwie pod Wiedniem w 1683 roku?",
            correct = "Jan III Sobieski",
            wrong = listOf("Stefan Batory", "Zygmunt III Waza", "Wladyslaw IV"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "konstytucja",
            prompt = "W ktorym roku uchwalono Konstytucje 3 maja?",
            correct = "1791",
            wrong = listOf("1793", "1795", "1789"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "rzym",
            prompt = "W ktorym roku upadlo Cesarstwo Zachodniorzymskie?",
            correct = "476",
            wrong = listOf("410", "395", "527"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "piastowie",
            prompt = "Ktora dynastia rzadzila Polska jako pierwsza?",
            correct = "Piastowie",
            wrong = listOf("Jagiellonowie", "Wazowie", "Wettynowie"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "waterloo",
            prompt = "W ktorym roku Napoleon przegral bitwe pod Waterloo?",
            correct = "1815",
            wrong = listOf("1812", "1805", "1821"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "druk",
            prompt = "Kto w Europie upowszechnil druk ruchoma czcionka?",
            correct = "Jan Gutenberg",
            wrong = listOf("Mikolaj Kopernik", "Leonardo da Vinci", "Galileusz"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Eksperckie ---------------------------- */
        ask(
            id = "westfalski",
            prompt = "W ktorym roku pokoj westfalski zakonczyl wojne trzydziestoletnia?",
            correct = "1648",
            wrong = listOf("1618", "1659", "1667"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "oktawian",
            prompt = "Kto byl pierwszym cesarzem rzymskim?",
            correct = "Oktawian August",
            wrong = listOf("Juliusz Cezar", "Neron", "Tyberiusz"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "machiavelli",
            prompt = "Kto napisal traktat 'Ksiaze'?",
            correct = "Niccolo Machiavelli",
            wrong = listOf("Dante Alighieri", "Erazm z Rotterdamu", "Tomasz Morus"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "unia_lubelska",
            prompt = "W ktorym roku zawarto unie lubelska?",
            correct = "1569",
            wrong = listOf("1385", "1466", "1596"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "hastings",
            prompt = "W ktorym roku odbyla sie bitwa pod Hastings?",
            correct = "1066",
            wrong = listOf("1215", "1096", "1013"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "konstantynopol",
            prompt = "W ktorym roku Turcy zdobyli Konstantynopol?",
            correct = "1453",
            wrong = listOf("1492", "1389", "1521"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
