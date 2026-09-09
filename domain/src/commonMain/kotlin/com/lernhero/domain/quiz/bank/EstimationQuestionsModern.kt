package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.EstimationEntry

/**
 * Szacowania "miekkie": liczby z zycia codziennego i daty spoza podrecznika historii.
 *
 * Trzecia pula bez dat bitew. Poprzednie dwie ([EstimationQuestionsPopulation],
 * [EstimationQuestionsMeasures]) sprawdzaja wyczucie skali swiata - ta sprawdza wyczucie skali
 * rzeczy, ktore gracz ma pod reka: ile jest panstw w ONZ, ile kosci ma czlowiek, w ktorym roku
 * wyszedl pierwszy Mario. Zdarzenie bossa przestaje byc kartkowka z historii.
 *
 * Daty sa tu CELOWO nieliczne i wszystkie wspolczesne - pula "zgadnij rok" jest juz duza i mocno
 * przechylona ku XX wiekowi (pilnuje tego EstimationBalanceTest), wiec dokladanie do niej kolejnych
 * lat 1900+ pogorszyloby rozklad. Wiekszosc tego banku to liczby, nie lata.
 */
object EstimationQuestionsModern {

    private fun count(
        id: String,
        prompt: String,
        value: Int,
        unit: String,
        tolerance: Int,
        max: Int,
        min: Int = 0
    ) = EstimationEntry(
        id = "estmod_$id",
        prompt = prompt,
        minValue = min,
        maxValue = max,
        correctValue = value,
        unit = unit,
        tolerance = tolerance
    )

    private fun year(id: String, prompt: String, value: Int) = EstimationEntry(
        id = "estmod_$id",
        prompt = prompt,
        minValue = ESTIMATION_YEAR_MIN,
        maxValue = ESTIMATION_YEAR_MAX,
        correctValue = value,
        unit = "year"
    )

    val all: List<EstimationEntry> = listOf(

        /* ------------------------- Cialo i biologia czlowieka ----------------------- */
        count("kosci_czlowiek", "How many bones does an adult human skeleton have?", 206, "bones", 12, 600),
        count("chromosomy", "How many chromosomes does a human cell normally have?", 46, "chromosomes", 4, 200),
        count("zeby_dorosly", "How many teeth does an adult human usually have, including wisdom teeth?", 32, "teeth", 3, 120),
        count("tetno_spoczynkowe", "What is a typical resting heart rate for a healthy adult?", 70, "beats per minute", 8, 250),
        count("krew_litry", "How many litres of blood does an average adult have, times ten?", 50, "tenths of a litre", 6, 200),
        count("miesnie_ile", "Roughly how many skeletal muscles does the human body have?", 600, "muscles", 70, 2_000),

        /* ------------------------------- Swiat w liczbach --------------------------- */
        count("onz_panstwa", "How many member states does the United Nations have?", 193, "countries", 8, 600),
        count("ue_panstwa", "How many member states does the European Union have?", 27, "countries", 3, 100),
        count("jezyki_swiata", "Roughly how many living languages are spoken in the world?", 7_000, "languages", 800, 25_000),
        count("pierwiastki_uklad", "How many elements does the periodic table contain?", 118, "elements", 6, 400),
        count("strefy_czasowe", "How many standard time zones does the world use?", 24, "time zones", 3, 100),
        count("panstwa_afryka", "How many countries are there in Africa?", 54, "countries", 5, 200),
        count("igrzyska_dyscypliny", "Roughly how many sports were on the programme of the 2024 Summer Olympics?", 32, "sports", 4, 120),

        /* ------------------------------ Sport i rekordy ----------------------------- */
        count("maraton_dystans", "How long is a marathon, in metres?", 42_195, "m", 1_500, 100_000),
        count("boisko_pilkarskie", "How long is a typical football pitch?", 105, "m", 10, 400),
        count("basen_olimpijski", "How long is an Olympic swimming pool?", 50, "m", 6, 200),
        count("kosz_wysokosc", "How high is a basketball hoop above the floor, in centimetres?", 305, "cm", 25, 1_000),
        count("setka_rekord", "What is the men's 100 m world record, in hundredths of a second?", 958, "hundredths of a second", 60, 3_000),
        count("klawisze_fortepian", "How many keys does a standard piano have?", 88, "keys", 6, 300),

        /* -------------------------- Technologia i kultura --------------------------- */
        year("star_wars", "In which year was the first Star Wars film released?", 1977),
        year("super_mario", "In which year was the first Super Mario Bros. released?", 1985),
        year("wikipedia", "In which year was Wikipedia launched?", 2001),
        year("youtube", "In which year was YouTube founded?", 2005),
        year("minecraft", "In which year was Minecraft fully released?", 2011),
        year("euro_gotowka", "In which year did euro banknotes and coins enter circulation?", 2002),
        year("coca_cola", "In which year was Coca-Cola invented?", 1886),
        year("nintendo_zalozenie", "In which year was Nintendo founded?", 1889),

        /* --------------------------- Przyroda i zwierzeta --------------------------- */
        count("slon_waga", "How much does an adult African elephant weigh, in kilograms?", 5_000, "kg", 600, 20_000),
        count("plywacz_dlugosc", "How long can a blue whale grow, in metres?", 30, "m", 4, 120),
        count("zyrafa_wysokosc", "How tall is an adult giraffe, in metres times ten?", 55, "tenths of a metre", 6, 200),
        count("pszczola_skrzydla", "How many times per second does a honeybee beat its wings?", 230, "beats per second", 30, 900),
        count("kolibr_serce", "What is a hummingbird's heart rate in flight, in beats per minute?", 1_200, "beats per minute", 150, 4_000),
        count("sekwoja_wysokosc", "How tall is the tallest known coast redwood, in metres?", 116, "m", 12, 400),
        count("gatunki_ptakow", "Roughly how many bird species are known?", 11_000, "species", 1_200, 40_000),

        /* ------------------------------ Skala i fizyka ------------------------------ */
        count("temperatura_slonca", "What is the temperature of the Sun's surface, in degrees Celsius?", 5_500, "degrees C", 600, 20_000),
        count("temperatura_absolutna", "What is absolute zero, in negative degrees Celsius?", 273, "negative degrees C", 25, 1_000),
        count("cisnienie_hpa", "What is standard atmospheric pressure at sea level, in hectopascals?", 1_013, "hPa", 90, 4_000),
        count("dzwiek_bol", "At roughly how many decibels does sound become painful?", 130, "dB", 15, 500),
        count("everest_tlen", "Roughly what percentage of sea-level oxygen is available at the summit of Everest?", 33, "percent", 5, 100)
    )
}
