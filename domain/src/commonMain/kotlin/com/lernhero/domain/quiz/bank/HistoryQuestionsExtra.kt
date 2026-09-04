package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Second batch of history - Poland, the world, and dates worth knowing. */
object HistoryQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "his2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.History,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("piramidy_kto", "Who built the great pyramids?", "The ancient Egyptians", listOf("The Romans", "The Maya", "The Persians"), QuizDifficulty.Easy),
        ask("rycerze_epoka", "In which era did knights in plate armor live?", "The Middle Ages", listOf("Antiquity", "The 20th century", "Prehistory"), QuizDifficulty.Easy),
        ask("ww2_koniec", "In which year did World War II end?", "1945", listOf("1944", "1946", "1943"), QuizDifficulty.Easy),
        ask("mur_co", "What did the Berlin Wall divide?", "East and West Berlin", listOf("Germany and France", "Poland and Germany", "Vienna and Bratislava"), QuizDifficulty.Easy),
        ask("wikingowie_skad", "Where did the Vikings come from?", "Scandinavia", listOf("Spain", "Greece", "Egypt"), QuizDifficulty.Easy),
        ask("kolo_wynalazek", "Which invention is older?", "The wheel", listOf("The steam engine", "The magnetic compass", "The printing press"), QuizDifficulty.Easy),
        ask("rzym_imperium", "Which city was the capital of the Roman Empire?", "Rome", listOf("Athens", "Carthage", "Alexandria"), QuizDifficulty.Easy),
        ask("solidarnosc_kraj", "In which country was the Solidarity movement founded?", "Poland", listOf("Hungary", "Czechoslovakia", "East Germany"), QuizDifficulty.Easy),
        ask("pierwsza_wojna_koniec", "In which year did World War I end?", "1918", listOf("1919", "1917", "1920"), QuizDifficulty.Easy),
        ask("faraon_tut", "Who was Tutankhamun?", "A Pharaoh of Egypt", listOf("An Emperor of China", "A King of Persia", "An Aztec chief"), QuizDifficulty.Easy),
        ask("kopernik_kraj", "Which country did Nicolaus Copernicus come from?", "Poland", listOf("Italy", "Germany", "Czechia"), QuizDifficulty.Easy),
        ask("husaria_kto", "What was the Polish winged hussars famous for?", "Heavy cavalry with wings", listOf("Marine infantry", "Siege artillery", "Light archery"), QuizDifficulty.Easy),
        ask("egipt_pismo", "What is the writing of the ancient Egyptians called?", "Hieroglyphs", listOf("Cuneiform", "Runic", "The Greek alphabet"), QuizDifficulty.Easy),
        ask("nowozytne_odkrycia", "Who sailed around the Cape of Good Hope on the way to India?", "Vasco da Gama", listOf("Columbus", "Cortes", "Cook"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("potop_szwedzki", "Which invasion do we call 'the Swedish Deluge'?", "Sweden's invasion of Poland in the 17th century", listOf("Denmark's invasion of Norway", "Russia's invasion in the 18th century", "The Teutonic Knights' invasion"), QuizDifficulty.Medium),
        ask("rozbiory_ile", "How many partitions of Poland were there?", "Three", listOf("Two", "Four", "Five"), QuizDifficulty.Medium),
        ask("rozbior_pierwszy", "In which year did the first partition of Poland occur?", "1772", listOf("1793", "1795", "1764"), QuizDifficulty.Medium),
        ask("powstanie_styczniowe", "In which year did the January Uprising break out?", "1863", listOf("1830", "1846", "1905"), QuizDifficulty.Medium),
        ask("powstanie_warszawskie", "In which year did the Warsaw Uprising break out?", "1944", listOf("1943", "1939", "1945"), QuizDifficulty.Medium),
        ask("rewolucja_pazdziernikowa", "In which year did the October Revolution occur in Russia?", "1917", listOf("1905", "1918", "1921"), QuizDifficulty.Medium),
        ask("hiroszima", "In which year was the atomic bomb dropped on Hiroshima?", "1945", listOf("1944", "1946", "1949"), QuizDifficulty.Medium),
        ask("czlowiek_kosmos_rok", "In which year did Yuri Gagarin fly into space?", "1961", listOf("1957", "1969", "1965"), QuizDifficulty.Medium),
        ask("sputnik", "What was Earth's first artificial satellite called?", "Sputnik 1", listOf("Vostok 1", "Explorer 1", "Luna 1"), QuizDifficulty.Medium),
        ask("napoleon_kraj", "Where did Napoleon Bonaparte come from?", "Corsica", listOf("Sicily", "Provence", "Sardinia"), QuizDifficulty.Medium),
        ask("wojna_secesyjna", "What was the American Civil War fought over, above all?", "Slavery and the union of the country", listOf("The border with Mexico", "California gold", "Independence from England"), QuizDifficulty.Medium),
        ask("chrzest_kto", "Which ruler adopted the baptism of Poland?", "Mieszko I", listOf("Boleslaw the Brave", "Casimir the Restorer", "Wladyslaw the Elbow-high"), QuizDifficulty.Medium),
        ask("unia_lubelska_co", "What was formed by the Union of Lublin?", "The Polish-Lithuanian Commonwealth", listOf("The Hanseatic League", "The Congress Kingdom", "The Teutonic Order's state"), QuizDifficulty.Medium),
        ask("krzyzacy_zakon", "What was the Teutonic Order's state on the Baltic called?", "The State of the Teutonic Order in Prussia", listOf("Swedish Livonia", "Courland", "Gdansk Pomerania"), QuizDifficulty.Medium),
        ask("wielka_emigracja", "After which uprising did the Great Emigration take place?", "The November Uprising", listOf("The January Uprising", "The Greater Poland Uprising", "The Kosciuszko Uprising"), QuizDifficulty.Medium),
        ask("kosciuszko_bitwa", "Near which town did Kosciuszko win a battle in 1794?", "Raclawice", listOf("Maciejowice", "Grochow", "Ostroleka"), QuizDifficulty.Medium),
        ask("wojna_stuletnia", "Between which countries was the Hundred Years' War fought?", "England and France", listOf("Spain and Portugal", "Sweden and Denmark", "Austria and Prussia"), QuizDifficulty.Medium),
        ask("renesans_kraj", "In which country did the Renaissance begin?", "Italy", listOf("France", "Germany", "England"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("bitwa_grunwald_kto", "Who commanded the Polish-Lithuanian forces at Grunwald?", "Wladyslaw Jagiello", listOf("Casimir the Great", "Vytautas alone", "Boleslaw the Wrymouth"), QuizDifficulty.Hard),
        ask("konstytucja_3maja_rok", "What was the Constitution of May 3 in Europe?", "The first in Europe", listOf("The third in Europe", "The first in the world", "The last of the 18th century"), QuizDifficulty.Hard),
        ask("traktat_wersalski", "In which year was the Treaty of Versailles signed?", "1919", listOf("1918", "1920", "1923"), QuizDifficulty.Hard),
        ask("bitwa_warszawska", "In which year was the Battle of Warsaw, called the Miracle at the Vistula?", "1920", listOf("1919", "1921", "1918"), QuizDifficulty.Hard),
        ask("kryzys_kubanski", "In which year did the Cuban Missile Crisis erupt?", "1962", listOf("1959", "1968", "1956"), QuizDifficulty.Hard),
        ask("mur_upadek_kto", "Which Soviet leader announced perestroika?", "Mikhail Gorbachev", listOf("Leonid Brezhnev", "Nikita Khrushchev", "Yuri Andropov"), QuizDifficulty.Hard),
        ask("sobor_trydencki", "What was the Council of Trent for?", "The Counter-Reformation in the Catholic Church", listOf("Dividing the Empire", "Settling the Hundred Years' War", "Fixing Byzantium's borders"), QuizDifficulty.Hard),
        ask("hanza", "What was the Hanseatic League?", "A league of merchant cities on the Baltic and North Sea", listOf("A knightly order", "A German dynasty", "An Italian military alliance"), QuizDifficulty.Hard),
        ask("liberum_veto", "What allowed a single deputy's vote to dissolve the Commonwealth's parliament?", "Liberum veto", listOf("Rokosz", "Confederation", "Election viritim"), QuizDifficulty.Hard),
        ask("sejm_wielki", "What is the Great Sejm otherwise known as?", "The Four-Year Sejm", listOf("The Silent Sejm", "The Convocation Sejm", "The Partition Sejm"), QuizDifficulty.Hard),
        ask("bitwa_pod_wieden_rok", "In which year was the Relief of Vienna?", "1683", listOf("1673", "1699", "1621"), QuizDifficulty.Hard),
        ask("cesarz_chin_pierwszy", "Who unified China as its first emperor?", "Qin Shi Huang", listOf("Kublai Khan", "Confucius", "Sun Tzu"), QuizDifficulty.Hard),
        ask("inkowie_kraj", "The heartland of the Inca empire lay in which present-day country?", "Peru", listOf("Mexico", "Brazil", "Colombia"), QuizDifficulty.Hard),
        ask("rewolucja_przemyslowa", "In which country did the Industrial Revolution begin?", "The United Kingdom", listOf("Germany", "France", "The USA"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("pokoj_torunski", "In which year was the Second Peace of Thorn signed?", "1466", listOf("1411", "1525", "1343"), QuizDifficulty.Expert),
        ask("holdpruski", "In which year was the Prussian Homage?", "1525", listOf("1466", "1569", "1505"), QuizDifficulty.Expert),
        ask("nihil_novi", "In which year was the Nihil novi constitution passed?", "1505", listOf("1573", "1454", "1493"), QuizDifficulty.Expert),
        ask("konfederacja_warszawska", "What did the Warsaw Confederation of 1573 concern?", "Religious tolerance", listOf("Military taxes", "Hereditary succession", "An alliance with Turkey"), QuizDifficulty.Expert),
        ask("bitwa_klushino", "Near which town did Hetman Zolkiewski defeat Russian forces in 1610?", "Klushino", listOf("Cecora", "Chocim", "Berestechko"), QuizDifficulty.Expert),
        ask("edykt_nantejski", "What did the Edict of Nantes guarantee?", "Rights for the Huguenots in France", listOf("Free trade in the Hanseatic League", "Independence for the Netherlands", "Dividing the Empire"), QuizDifficulty.Expert),
        ask("kongres_wiedenski", "In which years did the Congress of Vienna meet?", "1814-1815", listOf("1806-1807", "1830-1831", "1848-1849"), QuizDifficulty.Expert),
        ask("wiosna_ludow", "In which year did the Spring of Nations break out?", "1848", listOf("1830", "1863", "1871"), QuizDifficulty.Expert),
        ask("bitwa_lepanto", "Who lost the Battle of Lepanto in 1571?", "The Ottoman Empire", listOf("The Republic of Venice", "Spain", "The Papal States"), QuizDifficulty.Expert),
        ask("dynastia_ming", "Which dynasty ruled China before the Qing?", "Ming", listOf("Tang", "Han", "Song"), QuizDifficulty.Expert),
        ask("bizancjum_justynian", "Which Byzantine emperor had the famous Roman law code compiled?", "Justinian I", listOf("Constantine the Great", "Heraclius", "Basil II"), QuizDifficulty.Expert),
        ask("rewolta_taiping", "In which country did the Taiping Rebellion break out?", "China", listOf("Japan", "India", "Korea"), QuizDifficulty.Expert),
        ask("sejm_niemy", "In which year did the Silent Sejm convene?", "1717", listOf("1697", "1733", "1764"), QuizDifficulty.Expert)
    )
}
