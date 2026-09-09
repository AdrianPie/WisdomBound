package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Literature - assigned reading, writers and book characters. */
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

        /* ------------------------------- Easy ------------------------------- */
        ask(
            id = "harry",
            prompt = "Who wrote the Harry Potter series?",
            correct = "J.K. Rowling",
            wrong = listOf("J.R.R. Tolkien", "C.S. Lewis", "Roald Dahl"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "wladca",
            prompt = "Who wrote 'The Lord of the Rings'?",
            correct = "J.R.R. Tolkien",
            wrong = listOf("George R.R. Martin", "J.K. Rowling", "Terry Pratchett"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "pinokio",
            prompt = "What grew on Pinocchio when he lied?",
            correct = "His nose",
            wrong = listOf("His ears", "His hair", "His hands"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "andersen",
            prompt = "Who wrote the fairy tale 'The Ugly Duckling'?",
            correct = "Hans Christian Andersen",
            wrong = listOf("The Brothers Grimm", "Charles Perrault", "Aesop"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "romeo",
            prompt = "Who wrote 'Romeo and Juliet'?",
            correct = "William Shakespeare",
            wrong = listOf("Moliere", "Dante", "Goethe"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Medium ------------------------------ */
        ask(
            id = "quo_vadis",
            prompt = "Who wrote 'Quo Vadis'?",
            correct = "Henryk Sienkiewicz",
            wrong = listOf("Wladyslaw Reymont", "Stefan Zeromski", "Eliza Orzeszkowa"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "sherlock",
            prompt = "Who created the character Sherlock Holmes?",
            correct = "Arthur Conan Doyle",
            wrong = listOf("Agatha Christie", "Edgar Allan Poe", "Charles Dickens"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "wiedzmin",
            prompt = "Who wrote the saga about the witcher Geralt?",
            correct = "Andrzej Sapkowski",
            wrong = listOf("Jacek Dukaj", "Stanislaw Lem", "Remigiusz Mroz"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "maly_ksiaze",
            prompt = "Which asteroid did the Little Prince come from?",
            correct = "B-612",
            wrong = listOf("B-215", "A-330", "C-118"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Hard ------------------------------- */
        ask(
            id = "solaris",
            prompt = "Who wrote 'Solaris'?",
            correct = "Stanislaw Lem",
            wrong = listOf("Isaac Asimov", "Arthur C. Clarke", "Philip K. Dick"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "zbrodnia",
            prompt = "Who wrote 'Crime and Punishment'?",
            correct = "Fyodor Dostoevsky",
            wrong = listOf("Leo Tolstoy", "Anton Chekhov", "Ivan Turgenev"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "1984",
            prompt = "Who wrote the novel '1984'?",
            correct = "George Orwell",
            wrong = listOf("Aldous Huxley", "Ray Bradbury", "Franz Kafka"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "don_kichot",
            prompt = "What was the name of Don Quixote's squire?",
            correct = "Sancho Panza",
            wrong = listOf("Rocinante", "Dulcinea", "Figaro"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Expert ---------------------------- */
        ask(
            id = "iliada_wers",
            prompt = "What word does Homer's 'Iliad' open with?",
            correct = "Rage (wrath)",
            wrong = listOf("War", "Muse", "Gods"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "ulisses",
            prompt = "In which city is Joyce's 'Ulysses' set?",
            correct = "Dublin",
            wrong = listOf("London", "Edinburgh", "Belfast"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "proust",
            prompt = "Who wrote the cycle 'In Search of Lost Time'?",
            correct = "Marcel Proust",
            wrong = listOf("Gustave Flaubert", "Honore de Balzac", "Emile Zola"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
