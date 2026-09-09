package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Druga tura PRZEPROWADZKI do tematu lokalnego - patrz [PolandQuestions] po powod istnienia
 * kategorii.
 *
 * Pierwsza tura przeczesala historie, literature i geografie, ale przeoczyla sport: w globalnej
 * puli zostal caly zestaw pytan o Malysza, Kubice, Radwanska, Stocha i mundial 1974. Dla gracza
 * z Teksasu to nie sa pytania trudne, tylko niemozliwe - i dokladnie po to ta kategoria powstala.
 *
 * Granica przebiega tam, gdzie konczy sie **rozpoznawalnosc poza Polska**, a nie tam, gdzie
 * pada slowo "Polska". Dlatego w globalnej puli zostaja Kopernik, Maria Sklodowska-Curie,
 * Solidarnosc, Sobieski pod Wiedniem, Enigma i Lewandowski w Bayernie - o kazdym z nich moze
 * wiedziec ktos, kto nigdy nie byl w Polsce.
 */
object PolandQuestionsMoved {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "pol3_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Poland,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Literatura ------------------------------- */
        ask("pan_tadeusz", "Who wrote 'Pan Tadeusz'?", "Adam Mickiewicz", listOf("Juliusz Slowacki", "Henryk Sienkiewicz", "Boleslaw Prus"), QuizDifficulty.Easy),
        ask("nobel_szymborska", "Which Polish poet won the Nobel Prize in Literature?", "Wislawa Szymborska", listOf("Maria Konopnicka", "Halina Poswiatowska", "Agnieszka Osiecka"), QuizDifficulty.Medium),
        ask("chlopi_nobel", "For which novel did Reymont win the Nobel Prize?", "The Peasants", listOf("The Promised Land", "The Comedienne", "Fermenty"), QuizDifficulty.Hard),
        ask("nobel_tokarczuk", "What did Olga Tokarczuk win the Nobel Prize for?", "Her entire body of literary work", listOf("A single historical novel", "Journalism", "Poetry"), QuizDifficulty.Hard),

        /* -------------------------------- Historia -------------------------------- */
        ask("chrzest", "In which year did Poland adopt Christianity?", "966", listOf("1000", "1025", "1050"), QuizDifficulty.Easy),
        ask("niepodleglosc", "In which year did Poland regain independence?", "1918", listOf("1920", "1945", "1989"), QuizDifficulty.Easy),
        ask("chrobry", "Who was the first crowned King of Poland?", "Boleslaw the Brave", listOf("Mieszko I", "Casimir the Great", "Wladyslaw the Elbow-high"), QuizDifficulty.Medium),
        ask("ostatni_krol", "Who was the last King of Poland?", "Stanislaw August Poniatowski", listOf("John III Sobieski", "Augustus III", "Stanislaw Leszczynski"), QuizDifficulty.Hard),

        /* ------------------------------- Geografia -------------------------------- */
        ask("najwyzszy_szczyt", "What is the name of Poland's highest peak?", "Rysy", listOf("Giewont", "Sniezka", "Babia Gora"), QuizDifficulty.Easy),

        /* --------------------------------- Sport ---------------------------------- */
        ask("malysz", "Which sport was Adam Malysz famous for?", "Ski jumping", listOf("Cross-country skiing", "Biathlon", "Snowboarding"), QuizDifficulty.Medium),
        ask("f1_pierwszy_polak", "Which Pole was the first to win a Formula 1 race?", "Robert Kubica", listOf("Krzysztof Holowczyc", "Kajetan Kajetanowicz", "Sobieslaw Zasada"), QuizDifficulty.Hard),
        ask("kubica_wygrana", "Which Grand Prix did Robert Kubica win?", "The Canadian Grand Prix", listOf("The Monaco Grand Prix", "The Italian Grand Prix", "The Belgian Grand Prix"), QuizDifficulty.Hard),
        ask("radwanska_final", "Agnieszka Radwanska played the final of which Grand Slam tournament?", "Wimbledon", listOf("The US Open", "Roland Garros", "The Australian Open"), QuizDifficulty.Hard),
        ask("mundial_1974", "What place did Poland finish at the 1974 World Cup?", "3rd", listOf("1st", "2nd", "4th"), QuizDifficulty.Expert),
        ask("wlodarczyk", "In which event did Anita Wlodarczyk set world records?", "The hammer throw", listOf("The shot put", "The javelin throw", "The discus throw"), QuizDifficulty.Expert),
        ask("malysz_puchary", "How many times did Adam Malysz win the Ski Jumping World Cup?", "Four times", listOf("Twice", "Three times", "Five times"), QuizDifficulty.Expert),
        ask("stoch_igrzyska", "How many Olympic gold medals has Kamil Stoch won?", "Three", listOf("One", "Two", "Four"), QuizDifficulty.Expert),
    )
}
