package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Mythology - gods, heroes and monsters (Greek, Norse, Egyptian and Slavic). */
object MythologyQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "mit_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Mythology,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask(
            id = "zeus",
            prompt = "Who was king of the Greek gods?",
            correct = "Zeus",
            wrong = listOf("Poseidon", "Hades", "Apollo"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "posejdon",
            prompt = "Which Greek god ruled the seas?",
            correct = "Poseidon",
            wrong = listOf("Zeus", "Ares", "Hermes"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "thor",
            prompt = "What weapon did the Norse god Thor use?",
            correct = "A hammer",
            wrong = listOf("A sword", "A spear", "A bow"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "olimp",
            prompt = "On which mountain did the Greek gods live?",
            correct = "Olympus",
            wrong = listOf("Parnassus", "Etna", "Ida"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "meduza",
            prompt = "What did the monster Medusa have on her head instead of hair?",
            correct = "Snakes",
            wrong = listOf("Flames", "Feathers", "Worms"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "herkules",
            prompt = "What was Heracles famous for?",
            correct = "Immense strength",
            wrong = listOf("Speed", "Beauty", "Wisdom"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Medium ------------------------------ */
        ask(
            id = "hades",
            prompt = "Who ruled the Greek underworld?",
            correct = "Hades",
            wrong = listOf("Poseidon", "Cronus", "Dionysus"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "atena",
            prompt = "Which goddess was the patron of wisdom and Athens?",
            correct = "Athena",
            wrong = listOf("Hera", "Aphrodite", "Artemis"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "odyn",
            prompt = "What was the chief Norse god called?",
            correct = "Odin",
            wrong = listOf("Thor", "Loki", "Baldur"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "anubis",
            prompt = "Which Egyptian god had the head of a jackal?",
            correct = "Anubis",
            wrong = listOf("Ra", "Horus", "Osiris"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "minotaur",
            prompt = "Where did the Minotaur live?",
            correct = "In the labyrinth on Crete",
            wrong = listOf("On the slopes of Olympus", "Behind the walls of Troy", "In the depths of Hades"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Hard ------------------------------- */
        ask(
            id = "yggdrasil",
            prompt = "What is the cosmic tree in Norse mythology called?",
            correct = "Yggdrasil",
            wrong = listOf("Valhalla", "Bifrost", "Midgard"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "perun",
            prompt = "Which Slavic god ruled thunder?",
            correct = "Perun",
            wrong = listOf("Svarog", "Veles", "Svetovid"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Expert ---------------------------- */
        ask(
            id = "ragnarok",
            prompt = "What is the Norse end of the world called?",
            correct = "Ragnarok",
            wrong = listOf("Valhalla", "Niflheim", "Ginnungagap"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "sfinks_zagadka",
            prompt = "Who solved the Sphinx's riddle at Thebes?",
            correct = "Oedipus",
            wrong = listOf("Theseus", "Perseus", "Jason"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
