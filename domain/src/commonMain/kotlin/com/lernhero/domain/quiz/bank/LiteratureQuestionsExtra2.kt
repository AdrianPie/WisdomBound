package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Third batch of literature - characters, quotes and genres not yet covered. */
object LiteratureQuestionsExtra2 {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "lit3_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Literature,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("harry_potter_szkola", "What is Harry Potter's school of magic called?", "Hogwarts", listOf("Beauxbatons", "Durmstrang", "Ilvermorny"), QuizDifficulty.Easy),
        ask("piotrus_pan", "Who wrote 'Peter Pan'?", "J.M. Barrie", listOf("Lewis Carroll", "C.S. Lewis", "Roald Dahl"), QuizDifficulty.Easy),
        ask("czarnoksieznik_z_krainy_oz", "Who wrote 'The Wonderful Wizard of Oz'?", "L. Frank Baum", listOf("Lewis Carroll", "J.M. Barrie", "Hans Christian Andersen"), QuizDifficulty.Easy),
        ask("basn_kopciuszek", "Who recorded one of the most famous versions of the Cinderella tale?", "The Brothers Grimm", listOf("Hans Christian Andersen", "Charles Perrault as sole author", "Aesop"), QuizDifficulty.Easy),
        ask("czerwony_kapturek", "Who is the heroine of the tale about meeting a wolf in the forest?", "Little Red Riding Hood", listOf("Cinderella", "Rapunzel", "Snow White"), QuizDifficulty.Easy),
        ask("pinokio_autor", "Who wrote 'Pinocchio'?", "Carlo Collodi", listOf("Hans Christian Andersen", "The Brothers Grimm", "Antoine de Saint-Exupery"), QuizDifficulty.Easy),
        ask("gucio_zaczarowany", "Who wrote 'About the Dwarves and Orphan Marysia'?", "Maria Konopnicka", listOf("Julian Tuwim", "Jan Brzechwa", "Ignacy Krasicki"), QuizDifficulty.Easy),
        ask("basnie_ezop", "Which country/era do the famous moral fables attributed to Aesop come from?", "Ancient Greece", listOf("Medieval France", "Renaissance Italy", "Modern-day England"), QuizDifficulty.Easy),
        ask("tolkien_pierscien", "What is the main character carrying the ring in 'The Lord of the Rings' called?", "Frodo Baggins", listOf("Bilbo Baggins", "Aragorn", "Gandalf"), QuizDifficulty.Easy),
        ask("harry_potter_pierwszy_tom", "What is the first book of the Harry Potter series called?", "Harry Potter and the Philosopher's Stone", listOf("Harry Potter and the Chamber of Secrets", "Harry Potter and the Goblet of Fire", "Harry Potter and the Order of the Phoenix"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("pan_tadeusz_bohater", "Who is the main character of 'Pan Tadeusz'?", "Tadeusz Soplica", listOf("Jacek Soplica", "Gerwazy", "The Judge"), QuizDifficulty.Medium),
        ask("pan_tadeusz_miejsce", "Where is 'Pan Tadeusz' set?", "Lithuania (in Soplicowo)", listOf("Krakow", "Warsaw", "France"), QuizDifficulty.Medium),
        ask("lalka_bohater", "Who is the main character of Boleslaw Prus's 'The Doll'?", "Stanislaw Wokulski", listOf("Ignacy Rzecki", "Julian Ochocki", "Tomasz Lecki"), QuizDifficulty.Medium),
        ask("quo_vadis_epoka", "In which era is 'Quo Vadis' set?", "Ancient Rome under Nero", listOf("Medieval Poland", "Renaissance Italy", "Early modern France"), QuizDifficulty.Medium),
        ask("krzyzacy_bohater", "Who is one of the main characters of Sienkiewicz's 'The Knights of the Cross'?", "Zbyszko of Bogdaniec", listOf("Zagloba", "Wolodyjowski", "Kmicic"), QuizDifficulty.Medium),
        ask("potop_bohater", "Who is the main character of Sienkiewicz's 'The Deluge'?", "Andrzej Kmicic", listOf("Zbyszko of Bogdaniec", "Wolodyjowski", "Skrzetuski"), QuizDifficulty.Medium),
        ask("wiedzmin_autor", "Who created the witcher Geralt?", "Andrzej Sapkowski", listOf("Jacek Dukaj", "Stanislaw Lem", "Jakub Malecki"), QuizDifficulty.Medium),
        ask("solaris_temat", "What is Lem's 'Solaris' about?", "Humans making contact with an intelligent ocean on an alien planet", listOf("Time travel", "An intergalactic war", "Artificial intelligence on Earth"), QuizDifficulty.Medium),
        ask("hobbit_bohater", "Who is the main character of 'The Hobbit'?", "Bilbo Baggins", listOf("Frodo Baggins", "Aragorn", "Legolas"), QuizDifficulty.Medium),
        ask("gra_o_tron_autor", "Who wrote the 'A Song of Ice and Fire' series (the basis for 'Game of Thrones')?", "George R.R. Martin", listOf("J.R.R. Tolkien", "Brandon Sanderson", "Terry Pratchett"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("dziady_czesc_iii_miejsce", "In which city is Part III of 'Forefathers' Eve' set?", "Vilnius", listOf("Warsaw", "Krakow", "Paris"), QuizDifficulty.Hard),
        ask("wesele_wesele_kogo", "Whose wedding is the backdrop of Wyspianski's play 'The Wedding'?", "A poet marrying a peasant woman in the village of Bronowice", listOf("A royal wedding", "A nobleman marrying an aristocrat", "A wedding in Warsaw"), QuizDifficulty.Hard),
        ask("ferdydurke_temat", "What does Gombrowicz mock in 'Ferdydurke'?", "Form, convention and the infantilization of people", listOf("War", "Colonialism", "Religion only"), QuizDifficulty.Hard),
        ask("przedwiosnie_bohater", "Who is the main character of 'The Coming Spring'?", "Cezary Baryka", listOf("Judym", "Wokulski", "Rzecki"), QuizDifficulty.Hard),
        ask("ludzie_bezdomni_bohater", "Who is the main character of Zeromski's 'Homeless People'?", "Tomasz Judym", listOf("Cezary Baryka", "Stanislaw Wokulski", "Janko the Musician"), QuizDifficulty.Hard),
        ask("nad_niemnem_autorka", "Who wrote 'On the Niemen'?", "Eliza Orzeszkowa", listOf("Maria Dabrowska", "Zofia Nalkowska", "Gabriela Zapolska"), QuizDifficulty.Hard),
        ask("noce_i_dnie_autorka", "Who wrote 'Nights and Days'?", "Maria Dabrowska", listOf("Eliza Orzeszkowa", "Zofia Nalkowska", "Narcyza Zmichowska"), QuizDifficulty.Hard),
        ask("konrad_wallenrod", "Who is the title character of Mickiewicz's 'Konrad Wallenrod'?", "A Lithuanian posing as a Teutonic Knight for revenge", listOf("A Polish king", "A Teutonic grand master by birth", "A Hanseatic merchant"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("granica_nalkowska", "Who wrote the novel 'Boundary'?", "Zofia Nalkowska", listOf("Maria Dabrowska", "Eliza Orzeszkowa", "Narcyza Zmichowska"), QuizDifficulty.Expert),
        ask("kordian_bohater", "What is the title character of Slowacki's play called?", "Kordian", listOf("Konrad", "Gustaw", "Wallenrod"), QuizDifficulty.Expert),
        ask("nieboska_komedia", "Who wrote 'The Undivine Comedy'?", "Zygmunt Krasinski", listOf("Juliusz Slowacki", "Adam Mickiewicz", "Cyprian Norwid"), QuizDifficulty.Expert),
        ask("trans_atlantyk", "Which Gombrowicz novel was written in exile in Argentina?", "Trans-Atlantyk", listOf("Ferdydurke", "Cosmos", "Pornografia"), QuizDifficulty.Expert),
        ask("mala_apokalipsa", "Who wrote 'A Minor Apocalypse'?", "Tadeusz Konwicki", listOf("Marek Hlasko", "Slawomir Mrozek", "Jerzy Andrzejewski"), QuizDifficulty.Expert),
        ask("popiol_i_diament_autor", "Who wrote 'Ashes and Diamonds'?", "Jerzy Andrzejewski", listOf("Tadeusz Konwicki", "Marek Hlasko", "Kazimierz Brandys"), QuizDifficulty.Expert),
        ask("tango_mrozek", "Who wrote the play 'Tango'?", "Slawomir Mrozek", listOf("Tadeusz Rozewicz", "Witold Gombrowicz", "Jerzy Andrzejewski"), QuizDifficulty.Expert),
        ask("proces_kafki_temat", "What is Kafka's 'The Trial' about, in brief?", "A man prosecuted by an unknown, absurd authority", listOf("A court case over an inheritance", "A technology company's process", "The process of writing a book"), QuizDifficulty.Expert)
    )
}
