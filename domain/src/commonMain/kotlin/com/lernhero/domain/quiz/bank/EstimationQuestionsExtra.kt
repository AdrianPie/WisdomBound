package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.EstimationEntry

/**
 * Second batch of "guess the year" questions for elite/boss events. All stick to the shared
 * [ESTIMATION_YEAR_MIN]/[ESTIMATION_YEAR_MAX] scale, so the UI can use one slider.
 */
object EstimationQuestionsExtra {

    private fun year(id: String, prompt: String, value: Int) = EstimationEntry(
        id = "est2_$id",
        prompt = prompt,
        minValue = ESTIMATION_YEAR_MIN,
        maxValue = ESTIMATION_YEAR_MAX,
        correctValue = value,
        unit = "year"
    )

    val all: List<EstimationEntry> = listOf(
        year("chrzest_polski", "In which year did Poland adopt Christianity?", 966),
        year("grunwald", "In which year was the Battle of Grunwald fought?", 1410),
        year("konstytucja_3maja", "In which year was the Constitution of May 3 adopted?", 1791),
        year("niepodleglosc", "In which year did Poland regain independence?", 1918),
        year("ww2_wybuch", "In which year did World War II break out?", 1939),
        year("mur_berlinski", "In which year did the Berlin Wall fall?", 1989),
        year("titanic", "In which year did the Titanic sink?", 1912),
        year("gagarin", "In which year did Yuri Gagarin fly into space?", 1961),
        year("internet_www", "In which year did Tim Berners-Lee propose the World Wide Web?", 1989),
        year("iphone", "In which year was the first iPhone unveiled?", 2007),
        year("czarnobyl", "In which year did the Chernobyl disaster occur?", 1986),
        year("hiroszima_rok", "In which year was the atomic bomb dropped on Hiroshima?", 1945),
        year("konstantynopol", "In which year did the Ottomans capture Constantinople?", 1453),
        year("hastings", "In which year was the Battle of Hastings fought?", 1066),
        year("gutenberg", "Around which year did Gutenberg print his Bible?", 1455),
        year("wieden_odsiecz", "In which year was the Relief of Vienna?", 1683),
        year("wersal", "In which year was the Treaty of Versailles signed?", 1919),
        year("bitwa_warszawska_est", "In which year was the Battle of Warsaw fought?", 1920),
        year("solidarnosc", "In which year was the Solidarity movement founded?", 1980),
        year("penicylina", "In which year did Alexander Fleming discover penicillin?", 1928),
        year("wzglednosc", "In which year did Einstein publish the general theory of relativity?", 1915),
        year("dna_struktura", "In which year was the structure of the DNA double helix described?", 1953),
        year("koniec_ww1", "In which year did World War I end?", 1918),
        year("rewolucja_pazdziernikowa_est", "In which year did the October Revolution occur in Russia?", 1917),
        year("piramida_cheopsa", "Around which year BC was the Great Pyramid of Giza built?", -2560),
        year("olimpiada_antyczna", "Around which year BC were the first Olympic Games held at Olympia?", -776),
        year("rzym_zalozenie", "In which year BC does legend place the founding of Rome?", -753),
        year("aleksander_smierc", "In which year BC did Alexander the Great die?", -323),
        year("cezar_smierc", "In which year BC was Julius Caesar assassinated?", -44),
        year("chinski_mur", "Around which year BC did construction linking the Great Wall of China begin?", -221),
        year("sputnik_rok", "In which year was Sputnik 1 launched?", 1957),
        year("mistrzostwa_2022", "In which year did Argentina win the World Cup in Qatar?", 2022)
    )
}
