package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.EstimationEntry

/**
 * "Zgadnij rok" dla zdarzen elit i bossow - wszystkie na wspolnej skali suwaka
 * ([ESTIMATION_YEAR_MIN]..[ESTIMATION_YEAR_MAX]), wiec UI moze uzyc jednego suwaka bez wzgledu na
 * to, ktore pytanie wypadlo. Tolerancja idzie z epoki ([com.lernhero.domain.quiz.defaultYearTolerance]).
 *
 * To sa trzy dawne partie (EstimationQuestions, Extra, Extra2) zlaczone w jeden plik, bez pytan
 * zadanych dwa razy (Gagarin, iPhone, Grunwald i Gutenberg siedzialy w dwoch partiach naraz).
 * Pozostale pule szacowan sa tematyczne - patrz [EstimationQuestionsWorld],
 * [EstimationQuestionsPopulation], [EstimationQuestionsMeasures], [EstimationQuestionsModern],
 * [EstimationQuestionsScale].
 */
const val ESTIMATION_YEAR_MIN = -3000
const val ESTIMATION_YEAR_MAX = 2025

object EstimationQuestions {

    private fun year(id: String, prompt: String, value: Int) = EstimationEntry(
        id = id,
        prompt = prompt,
        minValue = ESTIMATION_YEAR_MIN,
        maxValue = ESTIMATION_YEAR_MAX,
        correctValue = value,
        unit = "year"
    )

    /**
     * Wariant lokalny: pytanie idzie na zdarzenie bossa TYLKO u gracza w danym jezyku
     * (patrz [EstimationEntry.localeCode]). Bez tego data z historii jednego kraju trafialaby
     * do gracza, ktory nie ma jak jej znac.
     */
    private fun localYear(id: String, prompt: String, value: Int, locale: String) = EstimationEntry(
        id = id,
        prompt = prompt,
        minValue = ESTIMATION_YEAR_MIN,
        maxValue = ESTIMATION_YEAR_MAX,
        correctValue = value,
        unit = "year",
        localeCode = locale
    )

    val all: List<EstimationEntry> = listOf(
        year("est2_piramida_cheopsa", "Around which year BC was the Great Pyramid of Giza built?", -2560),
        year("est2_olimpiada_antyczna", "Around which year BC were the first Olympic Games held at Olympia?", -776),
        year("est2_rzym_zalozenie", "In which year BC does legend place the founding of Rome?", -753),
        year("est2_aleksander_smierc", "In which year BC did Alexander the Great die?", -323),
        year("est2_chinski_mur", "Around which year BC did construction linking the Great Wall of China begin?", -221),
        year("est2_cezar_smierc", "In which year BC was Julius Caesar assassinated?", -44),
        year("est_rome_fall", "In which year did the Western Roman Empire fall?", 476),
        year("est_islam_founding", "In which year did Muhammad receive the first revelation, giving rise to Islam?", 610),
        localYear("est2_chrzest_polski", "In which year did Poland adopt Christianity?", 966, locale = "pl"),
        year("est2_hastings", "In which year was the Battle of Hastings fought?", 1066),
        year("est_genghis_khan_birth", "In which year was Genghis Khan born?", 1162),
        year("est_magna_carta", "In which year was the Magna Carta signed?", 1215),
        localYear("est2_grunwald", "In which year was the Battle of Grunwald fought?", 1410, locale = "pl"),
        year("est2_konstantynopol", "In which year did the Ottomans capture Constantinople?", 1453),
        year("est2_gutenberg", "Around which year did Gutenberg print his Bible?", 1455),
        year("est_columbus_america", "In which year did Columbus reach America?", 1492),
        year("est3_reformacja_luter", "In which year did Martin Luther post his theses, starting the Reformation?", 1517),
        year("est3_unia_lubelska_est", "In which year was the Union of Lublin signed?", 1569),
        year("est3_bitwa_pod_lepanto", "In which year was the naval Battle of Lepanto fought?", 1571),
        year("est3_wojna_trzydziestoletnia_start", "In which year did the Thirty Years' War break out?", 1618),
        year("est2_wieden_odsiecz", "In which year was the Relief of Vienna?", 1683),
        year("est3_deklaracja_niepodleglosci_usa_est", "In which year was the US Declaration of Independence signed?", 1776),
        year("est_french_revolution", "In which year did the French Revolution break out?", 1789),
        localYear("est2_konstytucja_3maja", "In which year was the Constitution of May 3 adopted?", 1791, locale = "pl"),
        year("est3_bitwa_pod_waterloo", "In which year did Napoleon lose the Battle of Waterloo?", 1815),
        year("est3_wiosna_ludow_est", "In which year did the Spring of Nations break out in Europe?", 1848),
        year("est3_powstanie_styczniowe_est", "In which year did the January Uprising break out?", 1863),
        year("est3_wynalazek_telefonu", "In which year did Alexander Graham Bell patent the telephone?", 1876),
        year("est3_wynalazek_zarowki_est", "In which year did Edison patent the long-lasting light bulb?", 1879),
        year("est3_bracia_wright_lot", "In which year did the Wright brothers make the first documented powered flight?", 1903),
        year("est2_titanic", "In which year did the Titanic sink?", 1912),
        year("est_ww1_start", "In which year did World War I break out?", 1914),
        year("est2_wzglednosc", "In which year did Einstein publish the general theory of relativity?", 1915),
        year("est2_rewolucja_pazdziernikowa_est", "In which year did the October Revolution occur in Russia?", 1917),
        localYear("est2_niepodleglosc", "In which year did Poland regain independence?", 1918, locale = "pl"),
        year("est2_koniec_ww1", "In which year did World War I end?", 1918),
        year("est2_wersal", "In which year was the Treaty of Versailles signed?", 1919),
        localYear("est2_bitwa_warszawska_est", "In which year was the Battle of Warsaw fought?", 1920, locale = "pl"),
        year("est2_penicylina", "In which year did Alexander Fleming discover penicillin?", 1928),
        year("est3_krach_gieldowy", "In which year did the great stock market crash hit New York?", 1929),
        year("est2_ww2_wybuch", "In which year did World War II break out?", 1939),
        year("est2_hiroszima_rok", "In which year was the atomic bomb dropped on Hiroshima?", 1945),
        year("est3_ii_wojna_koniec_europa", "In which year did World War II end in Europe?", 1945),
        year("est3_powstanie_onz", "In which year was the United Nations founded?", 1945),
        year("est3_powstanie_izraela", "In which year was the State of Israel proclaimed?", 1948),
        year("est2_dna_struktura", "In which year was the structure of the DNA double helix described?", 1953),
        year("est2_sputnik_rok", "In which year was Sputnik 1 launched?", 1957),
        year("est2_gagarin", "In which year did Yuri Gagarin fly into space?", 1961),
        year("est_moon_landing", "In which year did humans first land on the Moon?", 1969),
        year("est2_solidarnosc", "In which year was the Solidarity movement founded?", 1980),
        year("est2_czarnobyl", "In which year did the Chernobyl disaster occur?", 1986),
        year("est2_mur_berlinski", "In which year did the Berlin Wall fall?", 1989),
        year("est2_internet_www", "In which year did Tim Berners-Lee propose the World Wide Web?", 1989),
        year("est3_rozpad_zsrr", "In which year did the Soviet Union formally dissolve?", 1991),
        localYear("est3_wstapienie_polski_ue", "In which year did Poland join the European Union?", 2004, locale = "pl"),
        year("est2_iphone", "In which year was the first iPhone unveiled?", 2007),
        year("est3_wybuch_pandemii_covid", "In which year did the World Health Organization declare the COVID-19 pandemic?", 2020),
        year("est2_mistrzostwa_2022", "In which year did Argentina win the World Cup in Qatar?", 2022)
    )
}
