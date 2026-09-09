package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Second batch of literature - assigned reading, world classics and a few for the well-read. */
object LiteratureQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "lit2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Literature,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("kubus", "Who wrote 'Winnie the Pooh'?", "A.A. Milne", listOf("Lewis Carroll", "Roald Dahl", "Astrid Lindgren"), QuizDifficulty.Easy),
        ask("pippi", "Who created the character Pippi Longstocking?", "Astrid Lindgren", listOf("Tove Jansson", "Selma Lagerlof", "Hans Christian Andersen"), QuizDifficulty.Easy),
        ask("muminki", "Who created the Moomins?", "Tove Jansson", listOf("Astrid Lindgren", "Jan Brzechwa", "Julian Tuwim"), QuizDifficulty.Easy),
        ask("alicja", "Who wrote 'Alice's Adventures in Wonderland'?", "Lewis Carroll", listOf("J.M. Barrie", "C.S. Lewis", "Frank Baum"), QuizDifficulty.Easy),
        ask("narnia", "Who wrote 'The Chronicles of Narnia'?", "C.S. Lewis", listOf("J.R.R. Tolkien", "Philip Pullman", "Ursula Le Guin"), QuizDifficulty.Easy),
        ask("maly_ksiaze_autor", "Who wrote 'The Little Prince'?", "Antoine de Saint-Exupery", listOf("Albert Camus", "Jules Verne", "Andre Gide"), QuizDifficulty.Easy),
        ask("robinson", "Who wrote 'Robinson Crusoe'?", "Daniel Defoe", listOf("Jonathan Swift", "Robert Louis Stevenson", "Jules Verne"), QuizDifficulty.Easy),
        ask("wyspa_skarbow", "Who wrote 'Treasure Island'?", "Robert Louis Stevenson", listOf("Daniel Defoe", "Herman Melville", "Jack London"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("moby_dick", "Who wrote 'Moby Dick'?", "Herman Melville", listOf("Jack London", "Joseph Conrad", "Ernest Hemingway"), QuizDifficulty.Medium),
        ask("stary_czlowiek", "Who wrote 'The Old Man and the Sea'?", "Ernest Hemingway", listOf("John Steinbeck", "William Faulkner", "F. Scott Fitzgerald"), QuizDifficulty.Medium),
        ask("wielki_gatsby", "Who wrote 'The Great Gatsby'?", "F. Scott Fitzgerald", listOf("Ernest Hemingway", "John Steinbeck", "Truman Capote"), QuizDifficulty.Medium),
        ask("duma_uprzedzenie", "Who wrote 'Pride and Prejudice'?", "Jane Austen", listOf("Emily Bronte", "Charlotte Bronte", "George Eliot"), QuizDifficulty.Medium),
        ask("wichrowe_wzgorza", "Who wrote 'Wuthering Heights'?", "Emily Bronte", listOf("Jane Austen", "Charlotte Bronte", "Mary Shelley"), QuizDifficulty.Medium),
        ask("frankenstein", "Who wrote 'Frankenstein'?", "Mary Shelley", listOf("Bram Stoker", "Edgar Allan Poe", "Robert Louis Stevenson"), QuizDifficulty.Medium),
        ask("drakula", "Who wrote 'Dracula'?", "Bram Stoker", listOf("Mary Shelley", "H.P. Lovecraft", "Edgar Allan Poe"), QuizDifficulty.Medium),
        ask("hobbit_autor", "Who wrote 'The Hobbit'?", "J.R.R. Tolkien", listOf("C.S. Lewis", "George R.R. Martin", "Terry Pratchett"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("proces", "Who wrote 'The Trial'?", "Franz Kafka", listOf("Thomas Mann", "Hermann Hesse", "Robert Musil"), QuizDifficulty.Hard),
        ask("czarodziejska_gora", "Who wrote 'The Magic Mountain'?", "Thomas Mann", listOf("Franz Kafka", "Hermann Hesse", "Stefan Zweig"), QuizDifficulty.Hard),
        ask("mistrz_malgorzata", "Who wrote 'The Master and Margarita'?", "Mikhail Bulgakov", listOf("Fyodor Dostoevsky", "Leo Tolstoy", "Anton Chekhov"), QuizDifficulty.Hard),
        ask("wojna_i_pokoj", "Who wrote 'War and Peace'?", "Leo Tolstoy", listOf("Fyodor Dostoevsky", "Ivan Turgenev", "Nikolai Gogol"), QuizDifficulty.Hard),
        ask("sto_lat_samotnosci", "Who wrote 'One Hundred Years of Solitude'?", "Gabriel Garcia Marquez", listOf("Mario Vargas Llosa", "Jorge Luis Borges", "Julio Cortazar"), QuizDifficulty.Hard),
        ask("eneida", "Who wrote 'The Aeneid'?", "Virgil", listOf("Ovid", "Horace", "Homer"), QuizDifficulty.Hard),
        ask("don_kichot_autor", "Who wrote 'Don Quixote'?", "Miguel de Cervantes", listOf("Lope de Vega", "Calderon de la Barca", "Federico Garcia Lorca"), QuizDifficulty.Hard),
        ask("faust", "Who wrote 'Faust'?", "Johann Wolfgang von Goethe", listOf("Friedrich Schiller", "Heinrich Heine", "Gotthold Lessing"), QuizDifficulty.Hard),
        ask("nedznicy", "Who wrote 'Les Miserables'?", "Victor Hugo", listOf("Honore de Balzac", "Emile Zola", "Gustave Flaubert"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("ulisses_autor", "Who wrote 'Ulysses'?", "James Joyce", listOf("Virginia Woolf", "Samuel Beckett", "T.S. Eliot"), QuizDifficulty.Expert),
        ask("pani_dalloway", "Who wrote 'Mrs Dalloway'?", "Virginia Woolf", listOf("Katherine Mansfield", "Doris Lessing", "Iris Murdoch"), QuizDifficulty.Expert),
        ask("czekajac_na_godota", "Who wrote 'Waiting for Godot'?", "Samuel Beckett", listOf("Eugene Ionesco", "Jean Genet", "Harold Pinter"), QuizDifficulty.Expert),
        ask("ziemia_jalowa", "Who wrote the poem 'The Waste Land'?", "T.S. Eliot", listOf("Ezra Pound", "W.B. Yeats", "W.H. Auden"), QuizDifficulty.Expert),
        ask("schulz_zaginione", "Which lost novel by Bruno Schulz is still being searched for?", "The Messiah", listOf("Sanatorium", "The Shops", "Spring"), QuizDifficulty.Expert),
        ask("lem_dziela", "Which Stanislaw Lem novel tells of an expedition to a planet covered by an ocean?", "Solaris", listOf("The Invincible", "Eden", "Fiasco"), QuizDifficulty.Expert),
        ask("kafka_przemiana", "What did Gregor Samsa turn into in 'The Metamorphosis'?", "A giant insect", listOf("A dog", "A bird", "A stone"), QuizDifficulty.Expert),
        ask("proust_madeleine", "What triggers a flood of memories in Proust's novel?", "A madeleine cake", listOf("An old photograph", "The smell of the sea", "The sound of a piano"), QuizDifficulty.Expert),
        ask("borges_biblioteka", "Which Borges story describes an infinite library?", "The Library of Babel", listOf("The Aleph", "The Garden of Forking Paths", "Funes the Memorious"), QuizDifficulty.Expert),
        ask("dostojewski_bracia", "Which Dostoevsky novel ends with a trial for patricide?", "The Brothers Karamazov", listOf("The Idiot", "Demons", "The Gambler"), QuizDifficulty.Expert)
    )
}
