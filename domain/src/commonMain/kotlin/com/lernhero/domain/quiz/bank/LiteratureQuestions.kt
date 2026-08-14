package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Literatura - lektury, pisarze i bohaterowie ksiazek. */
object LiteratureQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "lit_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Literature,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Latwe ------------------------------- */
        ask(
            id = "harry",
            prompt = "Kto napisal serie o Harrym Potterze?",
            correct = "J.K. Rowling",
            wrong = listOf("J.R.R. Tolkien", "C.S. Lewis", "Roald Dahl"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "pan_tadeusz",
            prompt = "Kto napisal 'Pana Tadeusza'?",
            correct = "Adam Mickiewicz",
            wrong = listOf("Juliusz Slowacki", "Henryk Sienkiewicz", "Boleslaw Prus"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "wladca",
            prompt = "Kto jest autorem 'Wladcy Pierscieni'?",
            correct = "J.R.R. Tolkien",
            wrong = listOf("George R.R. Martin", "J.K. Rowling", "Terry Pratchett"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "pinokio",
            prompt = "Co roslo Pinokiowi, gdy klamal?",
            correct = "Nos",
            wrong = listOf("Uszy", "Wlosy", "Rece"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "andersen",
            prompt = "Kto napisal basn 'Brzydkie kaczatko'?",
            correct = "Hans Christian Andersen",
            wrong = listOf("Bracia Grimm", "Charles Perrault", "Ezop"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "romeo",
            prompt = "Kto napisal 'Romea i Julie'?",
            correct = "William Szekspir",
            wrong = listOf("Molier", "Dante", "Goethe"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Srednie ------------------------------ */
        ask(
            id = "quo_vadis",
            prompt = "Kto napisal 'Quo vadis'?",
            correct = "Henryk Sienkiewicz",
            wrong = listOf("Wladyslaw Reymont", "Stefan Zeromski", "Eliza Orzeszkowa"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "lalka",
            prompt = "Kto jest glownym bohaterem 'Lalki' Prusa?",
            correct = "Stanislaw Wokulski",
            wrong = listOf("Ignacy Rzecki", "Tomasz Judym", "Andrzej Kmicic"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "sherlock",
            prompt = "Kto stworzyl postac Sherlocka Holmesa?",
            correct = "Arthur Conan Doyle",
            wrong = listOf("Agatha Christie", "Edgar Allan Poe", "Charles Dickens"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "wiedzmin",
            prompt = "Kto napisal sage o wiedzminie Geralcie?",
            correct = "Andrzej Sapkowski",
            wrong = listOf("Jacek Dukaj", "Stanislaw Lem", "Remigiusz Mroz"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "nobel_szymborska",
            prompt = "Ktora polska poetka dostala literacka Nagrode Nobla?",
            correct = "Wislawa Szymborska",
            wrong = listOf("Maria Konopnicka", "Halina Poswiatowska", "Agnieszka Osiecka"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "maly_ksiaze",
            prompt = "Z jakiej planety pochodzil Maly Ksiaze?",
            correct = "B-612",
            wrong = listOf("B-215", "A-330", "C-118"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Trudne ------------------------------- */
        ask(
            id = "solaris",
            prompt = "Kto napisal 'Solaris'?",
            correct = "Stanislaw Lem",
            wrong = listOf("Isaac Asimov", "Arthur C. Clarke", "Philip K. Dick"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "zbrodnia",
            prompt = "Kto jest autorem 'Zbrodni i kary'?",
            correct = "Fiodor Dostojewski",
            wrong = listOf("Lew Tolstoj", "Antoni Czechow", "Iwan Turgieniew"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "1984",
            prompt = "Kto napisal powiesc '1984'?",
            correct = "George Orwell",
            wrong = listOf("Aldous Huxley", "Ray Bradbury", "Franz Kafka"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "chlopi_nobel",
            prompt = "Za jaka powiesc Reymont dostal Nagrode Nobla?",
            correct = "Chlopi",
            wrong = listOf("Ziemia obiecana", "Komediantka", "Fermenty"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "don_kichot",
            prompt = "Jak nazywal sie giermek Don Kichota?",
            correct = "Sancho Pansa",
            wrong = listOf("Rosynant", "Dulcynea", "Figaro"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Eksperckie ---------------------------- */
        ask(
            id = "iliada_wers",
            prompt = "Od jakiego slowa zaczyna sie 'Iliada' Homera?",
            correct = "Gniew",
            wrong = listOf("Wojna", "Muza", "Bogowie"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "ulisses",
            prompt = "W ktorym miescie rozgrywa sie akcja 'Ulissesa' Joyce'a?",
            correct = "Dublin",
            wrong = listOf("Londyn", "Edynburg", "Belfast"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "proust",
            prompt = "Kto napisal cykl 'W poszukiwaniu straconego czasu'?",
            correct = "Marcel Proust",
            wrong = listOf("Gustave Flaubert", "Honore de Balzac", "Emile Zola"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
