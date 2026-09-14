package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Technology - computers, the internet and inventions. */
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

        /* ------------------------------- Easy ------------------------------- */
        ask(
            id = "www",
            prompt = "What does the abbreviation WWW stand for?",
            correct = "World Wide Web",
            wrong = listOf("World Web Wide", "Wide World Web", "Web World Wide"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "iphone_firma",
            prompt = "Which company makes the iPhone?",
            correct = "Apple",
            wrong = listOf("Samsung", "Google", "Nokia"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "zarowka",
            prompt = "Who is credited with popularizing the light bulb?",
            correct = "Thomas Edison",
            wrong = listOf("Nikola Tesla", "Alexander Bell", "James Watt"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Medium ------------------------------ */
        ask(
            id = "cpu",
            prompt = "What does the abbreviation CPU stand for?",
            correct = "Central Processing Unit",
            wrong = listOf("Random-access memory", "Graphics card", "Hard drive"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "android_firma",
            prompt = "Which company develops the Android operating system?",
            correct = "Google",
            wrong = listOf("Apple", "Microsoft", "Meta"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "bit_bajt",
            prompt = "How many bits make up one byte?",
            correct = "8",
            wrong = listOf("4", "16", "10"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "telefon_bell",
            prompt = "Who patented the telephone?",
            correct = "Alexander Graham Bell",
            wrong = listOf("Thomas Edison", "Guglielmo Marconi", "Samuel Morse"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "html",
            prompt = "What is the HTML language used for?",
            correct = "Building web pages",
            wrong = listOf("Controlling robots", "Scientific calculations", "Creating databases"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "wifi",
            prompt = "What is Wi-Fi used for?",
            correct = "Wireless network connectivity",
            wrong = listOf("Charging a phone", "Printing", "Measuring temperature"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Hard ------------------------------- */
        ask(
            id = "turing",
            prompt = "Who is regarded as the father of computer science and cracked the Enigma cipher?",
            correct = "Alan Turing",
            wrong = listOf("John von Neumann", "Charles Babbage", "Claude Shannon"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "enigma_polacy",
            prompt = "Which Pole co-created the first break of the Enigma cipher?",
            correct = "Marian Rejewski",
            wrong = listOf("Stefan Banach", "Jan Czochralski", "Kazimierz Funk"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "linux",
            prompt = "Who created the Linux kernel?",
            correct = "Linus Torvalds",
            wrong = listOf("Bill Gates", "Steve Wozniak", "Dennis Ritchie"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "binarnie_10",
            prompt = "How is the number 10 written in binary?",
            correct = "1010",
            wrong = listOf("1100", "1001", "1110"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "pierwszy_program",
            prompt = "Who is regarded as the first programmer in history?",
            correct = "Ada Lovelace",
            wrong = listOf("Grace Hopper", "Margaret Hamilton", "Marie Curie"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Expert ---------------------------- */
        ask(
            id = "arpanet",
            prompt = "What was the network the internet grew out of called?",
            correct = "ARPANET",
            wrong = listOf("ETHERNET", "USENET", "TELENET"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "moore",
            prompt = "What does Moore's law concern?",
            correct = "The doubling of transistor count in chips",
            wrong = listOf("The doubling of internet speed each year", "The steady fall in the price of memory", "The rise in server power consumption"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "c_jezyk",
            prompt = "Who created the C programming language?",
            correct = "Dennis Ritchie",
            wrong = listOf("Bjarne Stroustrup", "James Gosling", "Guido van Rossum"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
