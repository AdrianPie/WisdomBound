package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Animals - from the kangaroo to the giant squid. */
object AnimalQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "ani_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Animals,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask(
            id = "najwieksze",
            prompt = "Which animal is the largest on Earth?",
            correct = "Blue whale",
            wrong = listOf("African elephant", "Giraffe", "Whale shark"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "pajak_nogi",
            prompt = "How many legs does a spider have?",
            correct = "8",
            wrong = listOf("6", "10", "4"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "gepard",
            prompt = "Which animal is the fastest on land?",
            correct = "Cheetah",
            wrong = listOf("Lion", "Horse", "Antelope"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "panda",
            prompt = "What does the giant panda eat?",
            correct = "Bamboo",
            wrong = listOf("Meat", "Fish", "Nuts"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kangur",
            prompt = "Which animal carries its young in a pouch?",
            correct = "Kangaroo",
            wrong = listOf("Llama", "Dingo", "Tapir"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "zyrafa",
            prompt = "Which land animal is the tallest?",
            correct = "Giraffe",
            wrong = listOf("Elephant", "Camel", "Moose"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "miod",
            prompt = "Which insect produces honey?",
            correct = "Bee",
            wrong = listOf("Wasp", "Ant", "Butterfly"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "krolowa_dzungli",
            prompt = "Which of these animals is a mammal?",
            correct = "Dolphin",
            wrong = listOf("Shark", "Crocodile", "Snake"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Medium ------------------------------ */
        ask(
            id = "strus",
            prompt = "Which bird cannot fly?",
            correct = "Ostrich",
            wrong = listOf("Falcon", "Swallow", "Heron"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "osmiornica_serca",
            prompt = "How many hearts does an octopus have?",
            correct = "3",
            wrong = listOf("1", "2", "5"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "kameleon",
            prompt = "Which reptile is famous for changing skin color?",
            correct = "Chameleon",
            wrong = listOf("Iguana", "Gecko", "Monitor lizard"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "zolw",
            prompt = "Which animal can live for over a hundred years?",
            correct = "Giant tortoise",
            wrong = listOf("Cat", "Wolf", "Hare"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "ciaza_slonia",
            prompt = "Which land animal has the longest pregnancy?",
            correct = "Elephant",
            wrong = listOf("Rhinoceros", "Horse", "Giraffe"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "nietoperz",
            prompt = "Which sense does a bat use to navigate in the dark?",
            correct = "Echolocation",
            wrong = listOf("Night vision", "Smell", "Magnetoreception"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "mlode_konia",
            prompt = "What is a baby horse called?",
            correct = "Foal",
            wrong = listOf("Calf", "Piglet", "Kid"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "kolibr",
            prompt = "Which bird can fly backwards?",
            correct = "Hummingbird",
            wrong = listOf("Sparrow", "Kingfisher", "Stork"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Hard ------------------------------- */
        ask(
            id = "dziobak",
            prompt = "Which mammal lays eggs?",
            correct = "Platypus",
            wrong = listOf("Armadillo", "Sloth", "Hedgehog"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "krowa_zoladek",
            prompt = "How many chambers does a cow's stomach have?",
            correct = "4",
            wrong = listOf("1", "2", "3"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "kalmar_oczy",
            prompt = "Which animal has an eye that can grow to the size of a dinner plate?",
            correct = "Giant squid",
            wrong = listOf("Owl", "Whale", "Elephant seal"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "taniec_pszczol",
            prompt = "Which insect shows its fellows the way to food with a dance?",
            correct = "Bee",
            wrong = listOf("Ant", "Wasp", "Termite"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "pingwiny_arktyka",
            prompt = "How many penguin species live wild at the North Pole?",
            correct = "None",
            wrong = listOf("Three", "Five", "Eight"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "gepard_pazury",
            prompt = "Which big cat cannot roar, but purrs instead?",
            correct = "Cheetah",
            wrong = listOf("Lion", "Tiger", "Jaguar"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Expert ---------------------------- */
        ask(
            id = "hemocyjanina",
            prompt = "Which animal has blue blood?",
            correct = "Octopus",
            wrong = listOf("Frog", "Snake", "Carp"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "aksolotl",
            prompt = "Which amphibian is famous for regrowing lost limbs?",
            correct = "Axolotl",
            wrong = listOf("Toad", "Alpine newt", "Tree frog"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "niesporczak",
            prompt = "Which animal survives the vacuum of space and extreme temperatures?",
            correct = "Tardigrade",
            wrong = listOf("Cockroach", "Scorpion", "Starfish"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "narwal",
            prompt = "Which marine mammal has a long, spiral tusk growing from its head?",
            correct = "Narwhal",
            wrong = listOf("Beluga whale", "Porpoise", "Manatee"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "echidna",
            prompt = "Which animal, alongside the platypus, is a monotreme?",
            correct = "Echidna",
            wrong = listOf("Opossum", "Wombat", "Koala"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
