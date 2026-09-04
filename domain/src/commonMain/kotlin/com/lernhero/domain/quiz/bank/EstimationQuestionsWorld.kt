package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.EstimationEntry

/**
 * "Guess the year" - world history, WEIGHTED TOWARD OLDER ERAS.
 *
 * The reason is numeric, not thematic. The distribution of years in the pool so far looked like
 * this: 12 questions BC, 4 for the 1st-10th centuries AD, and a whopping 72 for 1900+ alone -
 * more than half the pool. And since tolerance grows with a question's age
 * ([com.lernhero.domain.quiz.defaultYearTolerance]), the player in practice kept getting the
 * most strictly-scored dates, from the last hundred years, over and over.
 *
 * This file weighs the pool back toward antiquity and the Middle Ages, where tolerance is wide
 * and the question tests a feel for the era instead of memory for one exact year. It's also
 * entirely international - not a single date from Polish history.
 */
object EstimationQuestionsWorld {

    private fun year(id: String, prompt: String, value: Int, tolerance: Int? = null): EstimationEntry {
        val base = EstimationEntry(
            id = "estw_$id",
            prompt = prompt,
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = value,
            unit = "year"
        )
        return if (tolerance == null) base else base.copy(tolerance = tolerance)
    }

    val all: List<EstimationEntry> = listOf(

        /* ------------------------- Antiquity (BC) ------------------------- */
        year("piramida_cheopsa", "Around which year was the Great Pyramid of Giza completed?", -2560),
        year("hammurabi_kodeks", "Around which year was the Code of Hammurabi written?", -1754),
        year("tutanchamon_smierc", "Around which year did the pharaoh Tutankhamun die?", -1323),
        year("wojna_trojanska", "Which year is the Trojan War traditionally dated to?", -1184),
        year("rzym_zalozenie", "According to tradition, in which year was Rome founded?", -753),
        year("igrzyska_pierwsze", "In which year were the first ancient Olympic Games held?", -776),
        year("budda_narodziny", "Around which year was the Buddha born?", -563),
        year("maraton_bitwa", "In which year was the Battle of Marathon fought?", -490),
        year("termopile_bitwa", "In which year was the Battle of Thermopylae fought?", -480),
        year("aleksander_smierc", "In which year did Alexander the Great die?", -323),
        year("hannibal_alpy", "In which year did Hannibal cross the Alps?", -218),
        year("kartagina_upadek", "In which year did Rome destroy Carthage?", -146),
        year("cezar_smierc", "In which year was Julius Caesar murdered?", -44),
        year("kleopatra_smierc", "In which year did Cleopatra VII die?", -30),

        /* --------------------------- 1st-10th century AD --------------------------- */
        year("wezuwiusz_pompeje", "In which year did the eruption of Vesuvius bury Pompeii?", 79),
        year("kolosseum_otwarcie", "In which year did the Roman Colosseum open?", 80),
        year("edykt_mediolanski", "In which year did the Edict of Milan legalize Christianity in Rome?", 313),
        year("rzym_podzial", "In which year did the Roman Empire split into East and West?", 395),
        year("hagia_sophia", "In which year was the Hagia Sophia in Constantinople completed?", 537),
        year("mahomet_hidzra", "In which year did the Hijra take place, marking the start of the Islamic calendar?", 622),
        year("karol_wielki_koronacja", "In which year was Charlemagne crowned emperor?", 800),
        year("lindisfarne", "In which year did the Vikings raid Lindisfarne monastery?", 793),

        /* ----------------------------- Middle Ages ----------------------------- */
        year("schizma_wschodnia", "In which year did the Great Schism split the Church?", 1054),
        year("krucjata_pierwsza", "In which year was the First Crusade proclaimed?", 1095),
        year("magna_carta_rok", "England's Magna Carta was signed in which year?", 1215),
        year("czyngis_smierc", "In which year did Genghis Khan die?", 1227),
        year("marco_polo_wyprawa", "Around which year did Marco Polo reach China?", 1275),
        year("czarna_smierc_szczyt", "In which year did the Black Death reach Western Europe?", 1347),
        year("wojna_stuletnia_start", "In which year did the Hundred Years' War begin?", 1337),
        year("joanna_darc_smierc", "In which year was Joan of Arc burned at the stake?", 1431),

        /* ------------------------ Discoveries and the early modern age ------------------------ */
        year("kolumb_ameryka", "Columbus's ships reached America in which year?", 1492),
        year("vasco_da_gama", "In which year did Vasco da Gama sail to India?", 1498),
        year("luter_tezy", "In which year did Martin Luther post his 95 theses?", 1517),
        year("magellan_oplyniecie", "In which year did Magellan's expedition complete its circumnavigation of Earth?", 1522),
        year("aztekowie_upadek", "In which year did Cortes capture the Aztec capital?", 1521),
        year("kopernik_dzielo", "In which year was 'On the Revolutions of the Heavenly Spheres' published?", 1543),
        year("armada_hiszpanska", "In which year did England defeat the Spanish Armada?", 1588),
        year("westfalski_pokoj", "In which year was the Peace of Westphalia signed?", 1648),
        year("newton_principia", "In which year did Newton publish the 'Principia'?", 1687),

        /* ------------------------------ 18th-19th century ------------------------------ */
        year("usa_niepodleglosc", "In which year was the US Declaration of Independence proclaimed?", 1776),
        year("waterloo_bitwa", "In which year was the Battle of Waterloo fought?", 1815),
        year("parowoz_stephenson", "In which year did Stephenson's first public railway line open?", 1825),
        year("darwin_gatunki", "In which year did Darwin publish 'On the Origin of Species'?", 1859),
        year("wojna_secesyjna_start", "In which year did the American Civil War begin?", 1861),
        year("kanal_sueski_otwarcie", "In which year did the Suez Canal open?", 1869),
        year("telefon_patent", "In which year did Bell file his telephone patent?", 1876),
        year("wieza_eiffla", "In which year was the Eiffel Tower completed?", 1889),
        year("igrzyska_nowozytne", "In which year were the first modern Olympic Games held?", 1896),

        /* -------------------------------- 20th century -------------------------------- */
        year("wright_lot", "In which year did the Wright brothers make their first powered flight?", 1903),
        year("einstein_wzglednosc", "In which year did Einstein publish the special theory of relativity?", 1905),
        year("ww1_start", "The outbreak of World War I happened in which year?", 1914),
        year("penicylina_odkrycie", "In which year did Fleming discover penicillin?", 1928),
        year("krach_gieldowy", "In which year did the New York stock market crash?", 1929),
        year("indie_niepodleglosc", "In which year did India gain independence?", 1947),
        year("dna_helisa", "In which year was the DNA double helix described?", 1953),
        year("sputnik", "In which year was Sputnik, the first artificial satellite, launched?", 1957),
        year("ksiezyc_ladowanie", "In which year did humans first set foot on the Moon?", 1969),
        year("mandela_wolnosc", "In which year did Nelson Mandela walk free from prison?", 1990),
        year("zsrr_koniec", "In which year was the Soviet Union dissolved?", 1991)
    )
}
