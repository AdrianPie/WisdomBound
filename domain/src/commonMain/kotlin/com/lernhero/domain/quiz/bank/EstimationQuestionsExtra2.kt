package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.EstimationEntry

/**
 * Third batch of "guess the year" questions for elite/boss events. All stick to the shared
 * [ESTIMATION_YEAR_MIN]/[ESTIMATION_YEAR_MAX] scale, so the UI can use one slider.
 */
object EstimationQuestionsExtra2 {

    private fun year(id: String, prompt: String, value: Int) = EstimationEntry(
        id = "est3_$id",
        prompt = prompt,
        minValue = ESTIMATION_YEAR_MIN,
        maxValue = ESTIMATION_YEAR_MAX,
        correctValue = value,
        unit = "year"
    )

    val all: List<EstimationEntry> = listOf(
        year("grunwald_est2", "In which year did Wladyslaw Jagiello defeat the Teutonic Knights at Grunwald?", 1410),
        year("reformacja_luter", "In which year did Martin Luther post his theses, starting the Reformation?", 1517),
        year("wynalazek_druku", "Around which year did Gutenberg invent movable-type printing?", 1440),
        year("bitwa_pod_lepanto", "In which year was the naval Battle of Lepanto fought?", 1571),
        year("unia_lubelska_est", "In which year was the Union of Lublin signed?", 1569),
        year("wojna_trzydziestoletnia_start", "In which year did the Thirty Years' War break out?", 1618),
        year("deklaracja_niepodleglosci_usa_est", "In which year was the US Declaration of Independence signed?", 1776),
        year("bitwa_pod_waterloo", "In which year did Napoleon lose the Battle of Waterloo?", 1815),
        year("wiosna_ludow_est", "In which year did the Spring of Nations break out in Europe?", 1848),
        year("powstanie_styczniowe_est", "In which year did the January Uprising break out?", 1863),
        year("wynalazek_telefonu", "In which year did Alexander Graham Bell patent the telephone?", 1876),
        year("wynalazek_zarowki_est", "In which year did Edison patent the long-lasting light bulb?", 1879),
        year("bracia_wright_lot", "In which year did the Wright brothers make the first documented powered flight?", 1903),
        year("krach_gieldowy", "In which year did the great stock market crash hit New York?", 1929),
        year("ii_wojna_koniec_europa", "In which year did World War II end in Europe?", 1945),
        year("powstanie_onz", "In which year was the United Nations founded?", 1945),
        year("powstanie_izraela", "In which year was the State of Israel proclaimed?", 1948),
        year("pierwszy_lot_kosmiczny_czlowieka", "In which year did Yuri Gagarin become the first human in space?", 1961),
        year("rozpad_zsrr", "In which year did the Soviet Union formally dissolve?", 1991),
        year("wstapienie_polski_ue", "In which year did Poland join the European Union?", 2004),
        year("pierwszy_iphone_est", "In which year was the first iPhone unveiled to the world?", 2007),
        year("wybuch_pandemii_covid", "In which year did the World Health Organization declare the COVID-19 pandemic?", 2020)
    )
}
