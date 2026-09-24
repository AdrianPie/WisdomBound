package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Kultura, jezyk i sztuka swiata - film, muzyka, malarstwo, mity jezykowe.
 *
 * Trafia do [QuizCategory.Literature] i [QuizCategory.Mythology], bo to one byly najplytsze:
 * bieg ma dwadziescia fal, a wybor tematu przed tura potrafi wrocic do tej samej kategorii kilka
 * razy pod rzad. Przy dotychczasowej glebokosci gracz widzial te same pytania w ciagu jednej
 * planszy.
 *
 * Zadne pytanie nie wymaga znajomosci jednego kraju - to jest pula globalna. Lokalne trafia do
 * [QuizCategory.Poland] i tam zostaje.
 */
object CultureQuestionsWorld {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty,
        category: QuizCategory = QuizCategory.Literature
    ) = QuizEntry(
        id = "cultw_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = category,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("hamlet_autor", "Who wrote the play Hamlet?", "William Shakespeare", listOf("Charles Dickens", "Homer", "Moliere"), QuizDifficulty.Easy),
        ask("dracula", "Who wrote the novel Dracula?", "Bram Stoker", listOf("Mary Shelley", "Edgar Allan Poe", "H. G. Wells"), QuizDifficulty.Easy),
        ask("romeo", "In which Italian city is Romeo and Juliet set?", "Verona", listOf("Venice", "Florence", "Rome"), QuizDifficulty.Easy),

        ask("van_gogh", "Which painter cut off part of his own ear?", "Vincent van Gogh", listOf("Paul Gauguin", "Edvard Munch", "Henri Matisse"), QuizDifficulty.Easy),
        ask("loki_rola", "What kind of figure is Loki in Norse myth?", "A trickster", listOf("A god of war", "A sea giant", "A guardian of the dead"), QuizDifficulty.Easy, QuizCategory.Mythology),

        /* ------------------------------ Medium ------------------------------ */
        ask("cerber", "What did Cerberus guard in Greek myth?", "The gates of the underworld", listOf("The gates of Olympus", "The golden fleece", "The oracle at Delphi"), QuizDifficulty.Medium, QuizCategory.Mythology),
        ask("wojna_pokoj", "Who wrote War and Peace?", "Leo Tolstoy", listOf("Fyodor Dostoevsky", "Anton Chekhov", "Ivan Turgenev"), QuizDifficulty.Medium),
        ask("don_kichot", "Which novel is often called the first modern European novel?", "Don Quixote", listOf("Robinson Crusoe", "Gulliver's Travels", "The Decameron"), QuizDifficulty.Medium),
        ask("odyn_ptaki", "Which two ravens brought Odin news of the world?", "Huginn and Muninn", listOf("Geri and Freki", "Sleipnir and Fenrir", "Vidar and Vali"), QuizDifficulty.Medium, QuizCategory.Mythology),
        ask("kafka", "Which writer is known for The Trial and The Metamorphosis?", "Franz Kafka", listOf("Thomas Mann", "Hermann Hesse", "Robert Musil"), QuizDifficulty.Medium),
        ask("orwell_1984", "Which novel gave us the phrase Big Brother is watching you?", "1984", listOf("Brave New World", "Fahrenheit 451", "Animal Farm"), QuizDifficulty.Medium),
        ask("ragnarok", "What is Ragnarok in Norse myth?", "The final battle that ends the world", listOf("The hall of fallen warriors", "A feast of the gods", "The bridge to Asgard"), QuizDifficulty.Medium, QuizCategory.Mythology),
        ask("moby", "Which novel opens with the words Call me Ishmael?", "Moby-Dick", listOf("Treasure Island", "The Old Man and the Sea", "Robinson Crusoe"), QuizDifficulty.Medium),

        /* ------------------------------- Hard ------------------------------- */
        ask("boska_komedia", "How many parts does Dante's Divine Comedy have?", "Three", listOf("Two", "Four", "Seven"), QuizDifficulty.Hard),
        ask("izanagi", "In Japanese myth, which pair created the islands of Japan?", "Izanagi and Izanami", listOf("Amaterasu and Susanoo", "Raijin and Fujin", "Inari and Hachiman"), QuizDifficulty.Hard, QuizCategory.Mythology),
        ask("ulisses_joyce", "In which city does Joyce's Ulysses take place over one day?", "Dublin", listOf("London", "Trieste", "Paris"), QuizDifficulty.Hard),
        ask("gilgamesz", "What is the Epic of Gilgamesh usually called?", "One of the oldest surviving works of literature", listOf("The first printed book in Europe", "The earliest known play ever staged", "The first novel written in prose"), QuizDifficulty.Hard),
        ask("anansi", "In West African folklore, what kind of creature is Anansi?", "A spider", listOf("A lion", "A crocodile", "A hawk"), QuizDifficulty.Hard, QuizCategory.Mythology),

        ask("tezcatlipoca", "Aztec gods were often linked to which two forces above all?", "The sun and human sacrifice", listOf("The sea and safe travel", "Music and dance alone", "Trade and written law"), QuizDifficulty.Hard, QuizCategory.Mythology),
        ask("saga_islandzka", "Icelandic sagas were mostly written down in which period?", "The Middle Ages", listOf("Ancient Rome", "The Renaissance", "The nineteenth century"), QuizDifficulty.Hard),

        /* ------------------------------ Expert ------------------------------ */
        ask("mahabharata", "Which Sanskrit epic contains the Bhagavad Gita?", "The Mahabharata", listOf("The Ramayana", "The Rigveda", "The Upanishads"), QuizDifficulty.Expert, QuizCategory.Mythology),
        ask("genji", "The Tale of Genji, often called the first novel, came from which country?", "Japan", listOf("China", "Korea", "India"), QuizDifficulty.Expert),
        ask("tyche", "Which Greek goddess personified luck and fortune?", "Tyche", listOf("Nemesis", "Hecate", "Eris"), QuizDifficulty.Expert, QuizCategory.Mythology),
        ask("mabinogion", "The Mabinogion collects the myths of which people?", "The Welsh", listOf("The Irish", "The Scots", "The Bretons"), QuizDifficulty.Expert, QuizCategory.Mythology)
    )
}
