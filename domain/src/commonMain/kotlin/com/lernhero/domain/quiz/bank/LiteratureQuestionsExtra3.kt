package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Literatura swiatowa - to, co czyta sie wszedzie.
 *
 * Zastepuje watki polskie, ktore wyjechaly do [PolandQuestions]. Celowo szeroko geograficznie
 * (Ameryka Lacinska, Azja, Afryka, Bliski Wschod), bo poprzedni bank konczyl sie mniej wiecej na
 * kanonie europejskim, i celowo z duzym udzialem prozy XX-wiecznej i fantastyki - to ksiazki,
 * ktore gracz ma szanse znac z okladki, a nie tylko ze spisu lektur.
 */
object LiteratureQuestionsExtra3 {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "lit4_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Literature,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("kipling_ksiega", "Who wrote 'The Jungle Book'?", "Rudyard Kipling", listOf("Jack London", "Robert Louis Stevenson", "H. Rider Haggard"), QuizDifficulty.Easy),
        ask("montgomery_ania", "Who wrote 'Anne of Green Gables'?", "L.M. Montgomery", listOf("Louisa May Alcott", "Frances Hodgson Burnett", "E. Nesbit"), QuizDifficulty.Easy),
        ask("swift_guliwer", "Who wrote 'Gulliver's Travels'?", "Jonathan Swift", listOf("Daniel Defoe", "Laurence Sterne", "Henry Fielding"), QuizDifficulty.Easy),
        ask("verne_80dni", "Who wrote 'Around the World in Eighty Days'?", "Jules Verne", listOf("H.G. Wells", "Alexandre Dumas", "Emilio Salgari"), QuizDifficulty.Easy),
        ask("dickens_oliver", "Who wrote 'Oliver Twist'?", "Charles Dickens", listOf("Thomas Hardy", "William Thackeray", "Anthony Trollope"), QuizDifficulty.Easy),
        ask("twain_tomsawyer", "Who wrote 'The Adventures of Tom Sawyer'?", "Mark Twain", listOf("Jack London", "O. Henry", "Washington Irving"), QuizDifficulty.Easy),
        ask("london_zew", "Who wrote 'The Call of the Wild'?", "Jack London", listOf("Mark Twain", "James Fenimore Cooper", "Rudyard Kipling"), QuizDifficulty.Easy),
        ask("moby_dick_zwierze", "Which animal is hunted in 'Moby-Dick'?", "A whale", listOf("A shark", "A giant squid", "A bear"), QuizDifficulty.Easy),
        ask("odyseja_bohater", "Who is the hero of the 'Odyssey'?", "Odysseus", listOf("Achilles", "Hector", "Aeneas"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("steinbeck_grona", "Who wrote 'The Grapes of Wrath'?", "John Steinbeck", listOf("Ernest Hemingway", "William Faulkner", "Sinclair Lewis"), QuizDifficulty.Medium),
        ask("camus_obcy", "Who wrote 'The Stranger' (L'Etranger)?", "Albert Camus", listOf("Jean-Paul Sartre", "Andre Malraux", "Simone de Beauvoir"), QuizDifficulty.Medium),
        ask("nowy_wspanialy_swiat", "Who wrote 'Brave New World'?", "Aldous Huxley", listOf("George Orwell", "H.G. Wells", "Philip K. Dick"), QuizDifficulty.Medium),
        ask("salinger_buszujacy", "Who wrote 'The Catcher in the Rye'?", "J.D. Salinger", listOf("Jack Kerouac", "Truman Capote", "John Updike"), QuizDifficulty.Medium),
        ask("flaubert_bovary", "Who wrote 'Madame Bovary'?", "Gustave Flaubert", listOf("Emile Zola", "Guy de Maupassant", "Stendhal"), QuizDifficulty.Medium),
        ask("lee_zabic", "Who wrote 'To Kill a Mockingbird'?", "Harper Lee", listOf("Toni Morrison", "Flannery O'Connor", "Carson McCullers"), QuizDifficulty.Medium),
        ask("morrison_umilowana", "Who wrote 'Beloved'?", "Toni Morrison", listOf("Alice Walker", "Maya Angelou", "Zora Neale Hurston"), QuizDifficulty.Medium),
        ask("bradbury_451", "Who wrote 'Fahrenheit 451'?", "Ray Bradbury", listOf("Isaac Asimov", "Philip K. Dick", "Arthur C. Clarke"), QuizDifficulty.Medium),
        ask("dante_jezyk", "In which language did Dante write 'The Divine Comedy'?", "Italian", listOf("Latin", "Provencal", "Greek"), QuizDifficulty.Medium),
        ask("dumas_hrabia", "Who wrote 'The Count of Monte Cristo'?", "Alexandre Dumas", listOf("Victor Hugo", "Stendhal", "Honore de Balzac"), QuizDifficulty.Medium),
        ask("things_fall_apart", "Chinua Achebe's 'Things Fall Apart' is set in which country?", "Nigeria", listOf("Kenya", "Ghana", "South Africa"), QuizDifficulty.Medium),
        ask("murakami_kraj", "Which country does the novelist Haruki Murakami come from?", "Japan", listOf("South Korea", "China", "Taiwan"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("lorca_kraj", "The poet and playwright Federico Garcia Lorca came from which country?", "Spain", listOf("Mexico", "Argentina", "Chile"), QuizDifficulty.Hard),
        ask("borges_forma", "Jorge Luis Borges is best known for which literary form?", "The short story", listOf("The epic poem", "The stage tragedy", "The historical novel"), QuizDifficulty.Hard),
        ask("pamuk_kraj", "Which country does the Nobel laureate Orhan Pamuk come from?", "Turkey", listOf("Greece", "Egypt", "Iran"), QuizDifficulty.Hard),
        ask("ishiguro_dzielo", "Who wrote 'The Remains of the Day'?", "Kazuo Ishiguro", listOf("Ian McEwan", "Julian Barnes", "Salman Rushdie"), QuizDifficulty.Hard),
        ask("opowiesc_podrecznej", "Who wrote 'The Handmaid's Tale'?", "Margaret Atwood", listOf("Doris Lessing", "Ursula K. Le Guin", "Alice Munro"), QuizDifficulty.Hard),
        ask("rzecz_o_ksiaze_genji", "'The Tale of Genji', often called the first novel, was written in which country?", "Japan", listOf("China", "Korea", "India"), QuizDifficulty.Hard),
        ask("nobel_marquez_nurt", "Which literary movement is Gabriel Garcia Marquez associated with?", "Magical realism", listOf("Naturalism", "Futurism", "The Beat generation"), QuizDifficulty.Hard),
        ask("beowulf_jezyk", "In which language was 'Beowulf' originally written?", "Old English", listOf("Old Norse", "Latin", "Middle High German"), QuizDifficulty.Hard),
        ask("achebe_jezyk", "Chinua Achebe wrote his most famous novels primarily in which language?", "English", listOf("Igbo", "French", "Swahili"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("gilgamesz_pochodzenie", "The Epic of Gilgamesh comes from which ancient civilization?", "Mesopotamia", listOf("Egypt", "Persia", "Phoenicia"), QuizDifficulty.Expert),
        ask("mahabharata_kraj", "The Mahabharata is an epic of which culture?", "India", listOf("Persia", "China", "Greece"), QuizDifficulty.Expert),
        ask("kafka_zyczenie", "What did Franz Kafka ask his friend Max Brod to do with his manuscripts?", "Burn them", listOf("Publish them abroad", "Translate them into Czech", "Sell them to a museum"), QuizDifficulty.Expert),
        ask("nobel_pierwsza_kobieta", "Who was the first woman to win the Nobel Prize in Literature?", "Selma Lagerlof", listOf("Grazia Deledda", "Sigrid Undset", "Pearl S. Buck"), QuizDifficulty.Expert),
        ask("soyinka_nobel", "Wole Soyinka was the first Nobel laureate in Literature from which continent?", "Africa", listOf("South America", "Asia", "Australia"), QuizDifficulty.Expert),
        ask("nabokov_jezyki", "Vladimir Nabokov wrote major novels in Russian and which other language?", "English", listOf("French", "German", "Czech"), QuizDifficulty.Expert),
        ask("calvino_dzielo", "Who wrote 'If on a winter's night a traveler'?", "Italo Calvino", listOf("Umberto Eco", "Primo Levi", "Cesare Pavese"), QuizDifficulty.Expert),
        ask("eco_imie_rozy_gatunek", "'The Name of the Rose' by Umberto Eco combines a medieval setting with which genre?", "A detective story", listOf("A space opera", "A romance novel", "A war memoir"), QuizDifficulty.Expert),
        ask("saramago_kraj", "Which country does the Nobel laureate Jose Saramago come from?", "Portugal", listOf("Spain", "Brazil", "Italy"), QuizDifficulty.Expert),
        ask("rumi_jezyk", "The poet Rumi wrote chiefly in which language?", "Persian", listOf("Arabic", "Turkish", "Urdu"), QuizDifficulty.Expert)
    )
}
