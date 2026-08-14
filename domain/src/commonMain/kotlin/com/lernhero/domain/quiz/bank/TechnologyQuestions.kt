package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Technologia - komputery, internet i wynalazki. */
object TechnologyQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "tec_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Technology,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Latwe ------------------------------- */
        ask(
            id = "www",
            prompt = "Co oznacza skrot WWW?",
            correct = "World Wide Web",
            wrong = listOf("World Web Wide", "Wide World Web", "Web World Wide"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "mysz",
            prompt = "Do czego sluzy mysz komputerowa?",
            correct = "Do sterowania kursorem",
            wrong = listOf("Do drukowania", "Do chlodzenia komputera", "Do zasilania"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "iphone_firma",
            prompt = "Ktora firma produkuje iPhone'y?",
            correct = "Apple",
            wrong = listOf("Samsung", "Google", "Nokia"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "zarowka",
            prompt = "Komu przypisuje sie upowszechnienie zarowki?",
            correct = "Thomasowi Edisonowi",
            wrong = listOf("Nikoli Tesli", "Alexandrowi Bellowi", "Jamesowi Wattowi"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "monitor",
            prompt = "Ktore urzadzenie wyswietla obraz z komputera?",
            correct = "Monitor",
            wrong = listOf("Skaner", "Mikrofon", "Router"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "google_typ",
            prompt = "Czym przede wszystkim jest Google?",
            correct = "Wyszukiwarka internetowa",
            wrong = listOf("Sklepem internetowym", "Komunikatorem", "Systemem bankowym"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Srednie ------------------------------ */
        ask(
            id = "cpu",
            prompt = "Co oznacza skrot CPU?",
            correct = "Procesor (Central Processing Unit)",
            wrong = listOf("Pamiec operacyjna", "Karta graficzna", "Dysk twardy"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "android_firma",
            prompt = "Ktora firma rozwija system Android?",
            correct = "Google",
            wrong = listOf("Apple", "Microsoft", "Meta"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "bit_bajt",
            prompt = "Ile bitow ma jeden bajt?",
            correct = "8",
            wrong = listOf("4", "16", "10"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "telefon_bell",
            prompt = "Kto opatentowal telefon?",
            correct = "Alexander Graham Bell",
            wrong = listOf("Thomas Edison", "Guglielmo Marconi", "Samuel Morse"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "html",
            prompt = "Do czego sluzy jezyk HTML?",
            correct = "Do budowy stron internetowych",
            wrong = listOf("Do sterowania robotami", "Do obliczen naukowych", "Do tworzenia baz danych"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "wifi",
            prompt = "Do czego sluzy Wi-Fi?",
            correct = "Do bezprzewodowej lacznosci z siecia",
            wrong = listOf("Do ladowania telefonu", "Do drukowania", "Do pomiaru temperatury"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Trudne ------------------------------- */
        ask(
            id = "turing",
            prompt = "Kto jest uznawany za ojca informatyki i zlamal szyfry Enigmy?",
            correct = "Alan Turing",
            wrong = listOf("John von Neumann", "Charles Babbage", "Claude Shannon"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "enigma_polacy",
            prompt = "Ktory Polak wspoltworzyl pierwsze zlamanie Enigmy?",
            correct = "Marian Rejewski",
            wrong = listOf("Stefan Banach", "Jan Czochralski", "Kazimierz Funk"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "linux",
            prompt = "Kto stworzyl jadro systemu Linux?",
            correct = "Linus Torvalds",
            wrong = listOf("Bill Gates", "Steve Wozniak", "Dennis Ritchie"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "binarnie_10",
            prompt = "Jak zapisuje sie liczba 10 w systemie dwojkowym?",
            correct = "1010",
            wrong = listOf("1100", "1001", "1110"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "pierwszy_program",
            prompt = "Kto jest uznawany za pierwsza programistke w historii?",
            correct = "Ada Lovelace",
            wrong = listOf("Grace Hopper", "Margaret Hamilton", "Marie Curie"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Eksperckie ---------------------------- */
        ask(
            id = "arpanet",
            prompt = "Jak nazywala sie siec, z ktorej wywodzi sie internet?",
            correct = "ARPANET",
            wrong = listOf("ETHERNET", "USENET", "TELENET"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "moore",
            prompt = "Czego dotyczy prawo Moore'a?",
            correct = "Podwajania liczby tranzystorow w ukladach",
            wrong = listOf("Predkosci internetu", "Cen pamieci RAM", "Zuzycia energii przez serwery"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "c_jezyk",
            prompt = "Kto stworzyl jezyk programowania C?",
            correct = "Dennis Ritchie",
            wrong = listOf("Bjarne Stroustrup", "James Gosling", "Guido van Rossum"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
