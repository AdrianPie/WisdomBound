package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Temat LOKALNY: historia, literatura i geografia Polski.
 *
 * Te pytania siedzialy wczesniej rozsypane po History/Literature/Geography, wiec kazdy gracz na
 * swiecie dostawal je w zwyklej rotacji - a "w ktorym roku wybuchlo powstanie styczniowe?" dla
 * kogos spoza Polski nie jest pytaniem trudnym, tylko niemozliwym. Zebrane w jednej kategorii
 * [QuizCategory.Poland] pokazuja sie WYLACZNIE przy polskim jezyku aplikacji
 * (patrz [QuizCategory.builtinFor]) - dla gracza z Polski to najbardziej swojski temat w grze,
 * dla reszty swiata nie istnieje.
 *
 * Prefiks starego banku zostaje w id (pol_his2_..., pol_lit3_...), zeby dalo sie przesledzic,
 * skad pytanie przyszlo, i zeby nie dalo sie zdublowac identyfikatora przy kolejnych przenosinach.
 */
object PolandQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "pol_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Poland,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("geo2_gory_polska_pd", "Which mountains form Poland's southern border with Slovakia?", "The Tatras", listOf("The Alps", "The Karkonosze", "The Bieszczady"), QuizDifficulty.Easy),
        ask("geo2_polska_sasiedzi", "How many countries border Poland?", "Seven", listOf("Five", "Six", "Eight"), QuizDifficulty.Easy),
        ask("his2_husaria_kto", "What was the Polish winged hussars famous for?", "Heavy cavalry with wings", listOf("Marine infantry", "Siege artillery", "Light archery"), QuizDifficulty.Easy),
        ask("his3_piastowie", "What was the first ruling dynasty of Poland called?", "The Piasts", listOf("The Jagiellons", "The Vasas", "The Habsburgs"), QuizDifficulty.Easy),
        ask("his3_jagiellonowie", "Which dynasty ruled Poland after the Piasts (with an Angevin interlude)?", "The Jagiellons", listOf("The Vasas", "The Habsburgs", "The Romanovs"), QuizDifficulty.Easy),
        ask("his3_polska_stolica_pierwsza", "Which city was Poland's first capital?", "Gniezno", listOf("Warsaw", "Krakow", "Poznan"), QuizDifficulty.Easy),
        ask("lit2_lokomotywa", "Who wrote the poem 'The Locomotive'?", "Julian Tuwim", listOf("Jan Brzechwa", "Adam Mickiewicz", "Maria Konopnicka"), QuizDifficulty.Easy),
        ask("lit2_akademia_pana_kleksa", "Who wrote 'The Academy of Mr. Kleks'?", "Jan Brzechwa", listOf("Julian Tuwim", "Kornel Makuszynski", "Janusz Korczak"), QuizDifficulty.Easy),
        ask("lit3_gucio_zaczarowany", "Who wrote 'About the Dwarves and Orphan Marysia'?", "Maria Konopnicka", listOf("Julian Tuwim", "Jan Brzechwa", "Ignacy Krasicki"), QuizDifficulty.Easy),
        ask("mit3_smok_wawelski", "Who did the legendary Krakus (or a clever cobbler) defeat in Polish legend?", "The Wawel Dragon", listOf("A basilisk", "Cerberus", "The Minotaur"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("geo2_wisla_zrodlo", "Where does the Vistula river originate?", "In the Silesian Beskids", listOf("In the Tatras", "In the Sudetes", "In the Masurian Lake District"), QuizDifficulty.Medium),
        ask("geo2_polska_jezioro", "Which lake is the largest in Poland?", "Sniardwy", listOf("Mamry", "Hancza", "Lebsko"), QuizDifficulty.Medium),
        ask("geo2_polska_najglebsze", "Which lake is the deepest in Poland?", "Hancza", listOf("Sniardwy", "Wigry", "Mamry"), QuizDifficulty.Medium),
        ask("geo2_polska_wojewodztwa", "How many provinces (voivodeships) does Poland have?", "16", listOf("14", "18", "12"), QuizDifficulty.Medium),
        ask("geo3_strefa_klimatyczna_polska", "Which climate zone is Poland in?", "Temperate", listOf("Subtropical", "Equatorial", "Polar"), QuizDifficulty.Medium),
        ask("geo3_wiatr_halny", "What is the warm, gusty wind blowing from the Tatras called?", "The Halny", listOf("The Mistral", "The Sirocco", "Only the alpine Foehn"), QuizDifficulty.Medium),
        ask("his2_potop_szwedzki", "Which invasion do we call 'the Swedish Deluge'?", "Sweden's invasion of Poland in the 17th century", listOf("Denmark's invasion of Norway", "Russia's invasion in the 18th century", "The Teutonic Knights' invasion"), QuizDifficulty.Medium),
        ask("his2_rozbiory_ile", "How many partitions of Poland were there?", "Three", listOf("Two", "Four", "Five"), QuizDifficulty.Medium),
        ask("his2_rozbior_pierwszy", "In which year did the first partition of Poland occur?", "1772", listOf("1793", "1795", "1764"), QuizDifficulty.Medium),
        ask("his2_powstanie_styczniowe", "In which year did the January Uprising break out?", "1863", listOf("1830", "1846", "1905"), QuizDifficulty.Medium),
        ask("his2_powstanie_warszawskie", "In which year did the Warsaw Uprising break out?", "1944", listOf("1943", "1939", "1945"), QuizDifficulty.Medium),
        ask("his2_chrzest_kto", "Which ruler adopted the baptism of Poland?", "Mieszko I", listOf("Boleslaw the Brave", "Casimir the Restorer", "Wladyslaw the Elbow-high"), QuizDifficulty.Medium),
        ask("his2_unia_lubelska_co", "What was formed by the Union of Lublin?", "The Polish-Lithuanian Commonwealth", listOf("The Hanseatic League", "The Congress Kingdom", "The Teutonic Order's state"), QuizDifficulty.Medium),
        ask("his2_krzyzacy_zakon", "What was the Teutonic Order's state on the Baltic called?", "The State of the Teutonic Order in Prussia", listOf("The Duchy of Livonia under Swedish rule", "The Duchy of Courland and Semigallia", "Royal Prussia around the city of Gdansk"), QuizDifficulty.Medium),
        ask("his2_wielka_emigracja", "After which uprising did the Great Emigration take place?", "The November Uprising", listOf("The January Uprising", "The Greater Poland Uprising", "The Kosciuszko Uprising"), QuizDifficulty.Medium),
        ask("his2_kosciuszko_bitwa", "Near which town did Kosciuszko win a battle in 1794?", "Raclawice", listOf("Maciejowice", "Grochow", "Ostroleka"), QuizDifficulty.Medium),
        ask("his3_krakow_stolica_do", "Until which century was Krakow the capital of Poland?", "The early 17th century", listOf("The 15th century", "The 19th century", "The 20th century"), QuizDifficulty.Medium),
        ask("his3_zygmunt_iii_stolica", "Which king moved Poland's capital to Warsaw?", "Sigismund III Vasa", listOf("Wladyslaw Jagiello", "Casimir the Great", "John III Sobieski"), QuizDifficulty.Medium),
        ask("his3_kazimierz_wielki_przydomek", "What did Casimir the Great earn his epithet for?", "Expanding and building cities in stone", listOf("Conquering Rus alone", "Winning wars against the Teutonic Knights", "Founding Warsaw"), QuizDifficulty.Medium),
        ask("his3_akademia_krakowska", "Who founded the Krakow Academy (today's Jagiellonian University)?", "Casimir the Great", listOf("Wladyslaw Jagiello", "Boleslaw the Brave", "Sigismund the Old"), QuizDifficulty.Medium),
        ask("lit2_chlopi_autor", "Who wrote 'The Peasants'?", "Wladyslaw Reymont", listOf("Stefan Zeromski", "Boleslaw Prus", "Henryk Sienkiewicz"), QuizDifficulty.Medium),
        ask("lit2_przedwiosnie", "Who wrote 'The Coming Spring'?", "Stefan Zeromski", listOf("Wladyslaw Reymont", "Zofia Nalkowska", "Maria Dabrowska"), QuizDifficulty.Medium),
        ask("lit2_ferdydurke", "Who wrote 'Ferdydurke'?", "Witold Gombrowicz", listOf("Bruno Schulz", "Stanislaw Witkiewicz", "Tadeusz Rozewicz"), QuizDifficulty.Medium),
        ask("lit2_sklepy_cynamonowe", "Who wrote 'The Street of Crocodiles' (Cinnamon Shops)?", "Bruno Schulz", listOf("Witold Gombrowicz", "Julian Tuwim", "Jaroslaw Iwaszkiewicz"), QuizDifficulty.Medium),
        ask("lit2_dziady_autor", "Who wrote 'Forefathers' Eve'?", "Adam Mickiewicz", listOf("Juliusz Slowacki", "Zygmunt Krasinski", "Cyprian Norwid"), QuizDifficulty.Medium),
        ask("lit2_balladyna", "Who wrote 'Balladyna'?", "Juliusz Slowacki", listOf("Adam Mickiewicz", "Aleksander Fredro", "Stanislaw Wyspianski"), QuizDifficulty.Medium),
        ask("lit2_wesele_autor", "Who wrote 'The Wedding'?", "Stanislaw Wyspianski", listOf("Gabriela Zapolska", "Stefan Zeromski", "Juliusz Slowacki"), QuizDifficulty.Medium),
        ask("lit2_zemsta", "Who wrote the comedy 'The Revenge'?", "Aleksander Fredro", listOf("Stanislaw Wyspianski", "Michal Balucki", "Gabriela Zapolska"), QuizDifficulty.Medium),
        ask("lit3_pan_tadeusz_bohater", "Who is the main character of 'Pan Tadeusz'?", "Tadeusz Soplica", listOf("Jacek Soplica", "Gerwazy", "The Judge"), QuizDifficulty.Medium),
        ask("lit3_pan_tadeusz_miejsce", "Where is 'Pan Tadeusz' set?", "Lithuania (in Soplicowo)", listOf("Krakow and its outskirts", "Warsaw under the Tsar", "France, in exile in Paris"), QuizDifficulty.Medium),
        ask("lit3_lalka_bohater", "Who is the main character of Boleslaw Prus's 'The Doll'?", "Stanislaw Wokulski", listOf("Ignacy Rzecki", "Julian Ochocki", "Tomasz Lecki"), QuizDifficulty.Medium),
        ask("lit3_krzyzacy_bohater", "Who is one of the main characters of Sienkiewicz's 'The Knights of the Cross'?", "Zbyszko of Bogdaniec", listOf("Zagloba", "Wolodyjowski", "Kmicic"), QuizDifficulty.Medium),
        ask("lit3_potop_bohater", "Who is the main character of Sienkiewicz's 'The Deluge'?", "Andrzej Kmicic", listOf("Zbyszko of Bogdaniec", "Wolodyjowski", "Skrzetuski"), QuizDifficulty.Medium),

        /* ------------------------------- Hard ------------------------------- */
        ask("geo2_polska_najnizej", "Where is Poland's lowest point located?", "Near Raczki Elblaskie", listOf("On the Vistula Spit", "In the Klodzko Valley", "In the Oder valley"), QuizDifficulty.Hard),
        ask("his2_bitwa_grunwald_kto", "Who commanded the Polish-Lithuanian forces at Grunwald?", "Wladyslaw Jagiello", listOf("Casimir the Great", "Vytautas alone", "Boleslaw the Wrymouth"), QuizDifficulty.Hard),
        ask("his2_konstytucja_3maja_rok", "What was the Constitution of May 3 in Europe?", "The first in Europe", listOf("The third in Europe", "The first in the world", "The last of the 18th century"), QuizDifficulty.Hard),
        ask("his2_bitwa_warszawska", "In which year was the Battle of Warsaw, called the Miracle at the Vistula?", "1920", listOf("1919", "1921", "1918"), QuizDifficulty.Hard),
        ask("his2_liberum_veto", "What allowed a single deputy's vote to dissolve the Commonwealth's parliament?", "Liberum veto", listOf("Rokosz", "Confederation", "Election viritim"), QuizDifficulty.Hard),
        ask("his2_sejm_wielki", "What is the Great Sejm otherwise known as?", "The Four-Year Sejm", listOf("The Silent Sejm", "The Convocation Sejm", "The Partition Sejm"), QuizDifficulty.Hard),
        ask("his3_unia_krewska", "What did the Union of Krewo of 1385 establish?", "A union of Poland with Lithuania and Lithuania's baptism", listOf("A partition of Poland between its neighbours", "An alliance struck with the Teutonic Knights", "The treaty that ended the Thirteen Years' War"), QuizDifficulty.Hard),
        ask("his3_wojna_trzynastoletnia", "How did the Thirteen Years' War with the Teutonic Knights end?", "The Peace of Thorn and Pomerania joining Poland", listOf("Losing Pomerania to the Teutonic Knights", "The partition of Poland", "A union with the Teutonic Knights"), QuizDifficulty.Hard),
        ask("his3_bitwa_pod_beresteczkiem", "Who did the Commonwealth fight at Berestechko in 1651?", "Khmelnytsky's Cossacks and the Tatars", listOf("The Swedish army under its king", "The Ottoman Turks and their vassals", "The army of the Tsar of Moscow"), QuizDifficulty.Hard),
        ask("his3_chmielnicki_powstanie", "Where did the Khmelnytsky Uprising break out?", "Ukraine", listOf("Lithuania", "Prussia", "Silesia"), QuizDifficulty.Hard),
        ask("his3_wojna_o_niepodleglosc_usa_pomoc", "Which Pole fought in the American War of Independence?", "Tadeusz Kosciuszko", listOf("Jozef Pilsudski", "John III Sobieski", "Wladyslaw Sikorski"), QuizDifficulty.Hard),
        ask("his3_pulaski", "Which Pole died fighting for the American side in the War of Independence?", "Casimir Pulaski", listOf("Tadeusz Kosciuszko", "Jozef Bem", "Ignacy Moscicki"), QuizDifficulty.Hard),
        ask("his3_wiosna_ludow_polska", "Where in Polish lands did the Spring of Nations uprisings of 1848 occur?", "Greater Poland (the 1848 Greater Poland Uprising)", listOf("Silesia, under Prussian rule at the time", "Pomerania, along the Baltic coast", "Masuria, among the eastern lakes"), QuizDifficulty.Hard),
        ask("lit2_nobel_milosz", "Which Polish poet won the Nobel Prize in 1980?", "Czeslaw Milosz", listOf("Zbigniew Herbert", "Tadeusz Rozewicz", "Julian Przybos"), QuizDifficulty.Hard),
        ask("lit3_dziady_czesc_iii_miejsce", "In which city is Part III of 'Forefathers' Eve' set?", "Vilnius", listOf("Warsaw", "Krakow", "Paris"), QuizDifficulty.Hard),
        ask("lit3_wesele_wesele_kogo", "Whose wedding is the backdrop of Wyspianski's play 'The Wedding'?", "A poet marrying a peasant woman in the village of Bronowice", listOf("A king marrying a foreign princess in the capital", "A nobleman marrying into an aristocratic family", "A wedding held in a townhouse in Warsaw"), QuizDifficulty.Hard),
        ask("lit3_ferdydurke_temat", "What does Gombrowicz mock in 'Ferdydurke'?", "Form, convention and the infantilization of people", listOf("War and the way it deforms a whole generation", "Colonial rule and the peoples it subjected", "Religion, and nothing else besides"), QuizDifficulty.Hard),
        ask("lit3_ludzie_bezdomni_bohater", "Who is the main character of Zeromski's 'Homeless People'?", "Tomasz Judym", listOf("Cezary Baryka", "Stanislaw Wokulski", "Janko the Musician"), QuizDifficulty.Hard),
        ask("lit3_konrad_wallenrod", "Who is the title character of Mickiewicz's 'Konrad Wallenrod'?", "A Lithuanian posing as a Teutonic Knight for revenge", listOf("A Polish king who fought the Order for years", "A grand master of the Order by birth and blood", "A Hanseatic merchant trading on the Baltic"), QuizDifficulty.Hard),

        /* ------------------------------ Expert ------------------------------ */
        ask("geo2_trojstyk_polska", "Where do the borders of Poland, Czechia and Slovakia meet?", "At the tripoint near Jaworzynka", listOf("At Sniezka", "In Ustrzyki Gorne", "At the Dukla Pass"), QuizDifficulty.Expert),
        ask("his2_pokoj_torunski", "In which year was the Second Peace of Thorn signed?", "1466", listOf("1411", "1525", "1343"), QuizDifficulty.Expert),
        ask("his2_holdpruski", "In which year was the Prussian Homage?", "1525", listOf("1466", "1569", "1505"), QuizDifficulty.Expert),
        ask("his2_konfederacja_warszawska", "What did the Warsaw Confederation of 1573 concern?", "Religious tolerance", listOf("Military taxes", "Hereditary succession", "An alliance with Turkey"), QuizDifficulty.Expert),
        ask("his2_bitwa_klushino", "Near which town did Hetman Zolkiewski defeat Russian forces in 1610?", "Klushino", listOf("Cecora", "Chocim", "Berestechko"), QuizDifficulty.Expert),
        ask("his2_sejm_niemy", "In which year did the Silent Sejm convene?", "1717", listOf("1697", "1733", "1764"), QuizDifficulty.Expert),
        ask("his3_unia_horodelska", "What did the Union of Horodlo of 1413 regulate?", "Equalizing the rights of Lithuanian and Polish nobility", listOf("Adding the whole of Pomerania to the Crown", "Ending the Thirteen Years' War with the Order", "Sealing a lasting peace with the Swedish king"), QuizDifficulty.Expert),
        ask("his3_konfederacja_barska", "Who was the Bar Confederation (1768) directed against?", "King Poniatowski and Russian influence", listOf("The Teutonic Order and its Prussian state", "The Swedish king and his armies", "The Ottoman Turks in the south"), QuizDifficulty.Expert),
        ask("his3_insurekcja_kosciuszkowska_rok", "In which year did the Kosciuszko Uprising break out?", "1794", listOf("1791", "1830", "1863"), QuizDifficulty.Expert),
        ask("his3_krolestwo_kongresowe", "When was the Congress Kingdom of Poland formed?", "At the Congress of Vienna in 1815", listOf("After the November Uprising", "After the first partition", "After regaining independence in 1918"), QuizDifficulty.Expert),
        ask("his3_wielka_wojna_pln_skutek", "What was one of the main consequences of the Great Northern War for the Commonwealth?", "Weakening and growing dependence on Russia", listOf("Gaining new territory in the north", "Growing military power", "A personal union with Sweden"), QuizDifficulty.Expert),
        ask("his3_sejm_czteroletni_reformy", "Which reform did the Four-Year Sejm introduce besides the constitution?", "Expanding the army and townspeople's rights", listOf("Abolishing liberum veto permanently before 1791", "Establishing a republic", "Abolishing the monarchy"), QuizDifficulty.Expert),
        ask("his3_polski_rzad_na_uchodzstwie", "Where did the Polish government-in-exile operate during World War II?", "London (earlier in France)", listOf("Moscow, alongside the Red Army", "Washington, with the Allies", "Rome, after the Italian campaign"), QuizDifficulty.Expert),
        ask("his3_dywizjon_303", "What was 303 Squadron known for in the Battle of Britain?", "It was one of the RAF's most effective fighter squadrons", listOf("It defended Warsaw through September 1939", "It fought its way up the hill at Monte Cassino", "It was a naval unit escorting convoys"), QuizDifficulty.Expert),
        ask("lit2_gombrowicz_dziennik", "Which Gombrowicz work was written over years in exile?", "Diary", listOf("Ferdydurke", "The Marriage", "Cosmos"), QuizDifficulty.Expert),
        ask("lit2_herbert_bohater", "What is the character of Zbigniew Herbert's poem cycle called?", "Mr. Cogito", listOf("Mr. Tadeusz", "Mr. Wolodyjowski", "Mr. Kleks"), QuizDifficulty.Expert),
        ask("lit3_kordian_bohater", "What is the title character of Slowacki's play called?", "Kordian", listOf("Konrad", "Gustaw", "Wallenrod"), QuizDifficulty.Expert),
        ask("lit3_nieboska_komedia", "Who wrote 'The Undivine Comedy'?", "Zygmunt Krasinski", listOf("Juliusz Slowacki", "Adam Mickiewicz", "Cyprian Norwid"), QuizDifficulty.Expert),
        ask("lit3_trans_atlantyk", "Which Gombrowicz novel was written in exile in Argentina?", "Trans-Atlantyk", listOf("Ferdydurke", "Cosmos", "Pornografia"), QuizDifficulty.Expert),
        ask("lit3_tango_mrozek", "Who wrote the play 'Tango'?", "Slawomir Mrozek", listOf("Tadeusz Rozewicz", "Witold Gombrowicz", "Jerzy Andrzejewski"), QuizDifficulty.Expert),
    )
}
