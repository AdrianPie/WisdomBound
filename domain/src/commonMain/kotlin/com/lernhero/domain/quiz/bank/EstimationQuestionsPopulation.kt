package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.EstimationEntry

/**
 * Szacowanie LUDNOSCI - pierwsza pula, ktora nie pyta o rok.
 *
 * Zdarzenie bossa bylo do tej pory jedna umiejetnoscia: pamiecia do dat. Kto nie pamietal roku,
 * nie mial jak zgadnac. Populacja dziala inaczej - nawet nie znajac liczby da sie ja WYPROWADZIC
 * ("Nigeria to najludniejszy kraj Afryki, wiec grubo ponad sto milionow"), wiec pytanie nagradza
 * wyczucie skali, a nie zapamietanie cyfry.
 *
 * Liczby sa zaokraglone do rzedu, w ktorym zrodla sie zgadzaja, a tolerancja jest hojna - to ma byc
 * szacowanie, nie egzamin ze spisu powszechnego. Jednostka jest per pytanie ([EstimationEntry.unit]),
 * bo "1400 milionow" czyta sie gorzej niz "1400 mln", a maly kraj w milionach to samo zero.
 */
object EstimationQuestionsPopulation {

    /** Kraje - w milionach. Gorna granica z zapasem ponad najludniejszy kraj swiata. */
    private fun millions(
        id: String,
        prompt: String,
        value: Int,
        tolerance: Int,
        /** Sufit suwaka - domyslnie z zapasem ponad najludniejszy kraj, wiecej tylko dla kontynentow. */
        max: Int = 1600
    ) = EstimationEntry(
        id = "estpop_$id",
        prompt = prompt,
        minValue = 0,
        maxValue = max,
        correctValue = value,
        unit = "million people",
        tolerance = tolerance
    )

    /** Miasta i male kraje - w tysiacach, bo w milionach wyszlyby same zera. */
    private fun thousands(id: String, prompt: String, value: Int, tolerance: Int) = EstimationEntry(
        id = "estpop_$id",
        prompt = prompt,
        minValue = 0,
        maxValue = 40_000,
        correctValue = value,
        unit = "thousand people",
        tolerance = tolerance
    )

    val all: List<EstimationEntry> = listOf(

        /* ----------------------------- Kraje, giganci ----------------------------- */
        millions("india", "Roughly how many people live in India?", 1450, 90),
        millions("china", "Roughly how many people live in China?", 1410, 90),
        millions("usa", "Roughly how many people live in the United States?", 340, 25),
        millions("indonesia", "Roughly how many people live in Indonesia?", 285, 22),
        millions("pakistan", "Roughly how many people live in Pakistan?", 250, 20),
        millions("nigeria", "Roughly how many people live in Nigeria?", 230, 20),
        millions("brazil", "Roughly how many people live in Brazil?", 213, 18),
        millions("bangladesh", "Roughly how many people live in Bangladesh?", 174, 15),

        /* ----------------------------- Kraje, srednie ----------------------------- */
        millions("russia", "Roughly how many people live in Russia?", 144, 12),
        millions("ethiopia", "Roughly how many people live in Ethiopia?", 130, 12),
        millions("mexico", "Roughly how many people live in Mexico?", 131, 11),
        millions("japan", "Roughly how many people live in Japan?", 123, 10),
        millions("egypt", "Roughly how many people live in Egypt?", 116, 10),
        millions("philippines", "Roughly how many people live in the Philippines?", 116, 10),
        millions("dr_congo", "Roughly how many people live in the Democratic Republic of the Congo?", 109, 11),
        millions("vietnam", "Roughly how many people live in Vietnam?", 101, 9),
        millions("iran", "Roughly how many people live in Iran?", 92, 8),
        millions("turkey", "Roughly how many people live in Turkiye?", 87, 7),
        millions("germany", "Roughly how many people live in Germany?", 84, 6),
        millions("thailand", "Roughly how many people live in Thailand?", 72, 6),
        millions("united_kingdom", "Roughly how many people live in the United Kingdom?", 69, 5),
        millions("france", "Roughly how many people live in France?", 68, 5),
        millions("south_africa", "Roughly how many people live in South Africa?", 63, 5),
        millions("italy", "Roughly how many people live in Italy?", 59, 5),
        millions("kenya", "Roughly how many people live in Kenya?", 56, 5),
        millions("south_korea", "Roughly how many people live in South Korea?", 52, 4),
        millions("spain", "Roughly how many people live in Spain?", 48, 4),
        millions("argentina", "Roughly how many people live in Argentina?", 46, 4),
        millions("ukraine", "Roughly how many people live in Ukraine?", 38, 5),
        millions("canada", "Roughly how many people live in Canada?", 41, 4),
        millions("morocco", "Roughly how many people live in Morocco?", 38, 4),
        millions("saudi_arabia", "Roughly how many people live in Saudi Arabia?", 34, 4),
        millions("australia", "Roughly how many people live in Australia?", 27, 3),
        millions("netherlands", "Roughly how many people live in the Netherlands?", 18, 2),
        millions("chile", "Roughly how many people live in Chile?", 20, 2),
        millions("sweden", "Roughly how many people live in Sweden?", 11, 2),
        millions("greece", "Roughly how many people live in Greece?", 10, 2),
        millions("portugal", "Roughly how many people live in Portugal?", 10, 2),

        /* ------------------------- Male kraje - w tysiacach ------------------------ */
        thousands("norway", "Roughly how many people live in Norway?", 5_600, 500),
        thousands("new_zealand", "Roughly how many people live in New Zealand?", 5_300, 500),
        thousands("ireland", "Roughly how many people live in Ireland?", 5_300, 500),
        thousands("estonia", "Roughly how many people live in Estonia?", 1_370, 150),
        thousands("cyprus", "Roughly how many people live in Cyprus?", 1_300, 180),
        thousands("luxembourg", "Roughly how many people live in Luxembourg?", 670, 90),
        thousands("malta", "Roughly how many people live in Malta?", 560, 80),
        thousands("iceland", "Roughly how many people live in Iceland?", 390, 60),

        /* ------------------------ Miasta - aglomeracje ---------------------------- */
        thousands("tokyo", "Roughly how many people live in the Tokyo metropolitan area?", 37_000, 3_500),
        thousands("delhi", "Roughly how many people live in the Delhi metropolitan area?", 33_000, 3_500),
        thousands("shanghai", "Roughly how many people live in the Shanghai metropolitan area?", 29_000, 3_000),
        thousands("sao_paulo", "Roughly how many people live in the Sao Paulo metropolitan area?", 22_000, 2_500),
        thousands("cairo", "Roughly how many people live in the Cairo metropolitan area?", 22_000, 2_500),
        thousands("mexico_city", "Roughly how many people live in the Mexico City metropolitan area?", 22_000, 2_500),
        thousands("new_york", "Roughly how many people live in the New York metropolitan area?", 19_000, 2_200),
        thousands("lagos", "Roughly how many people live in the Lagos metropolitan area?", 16_000, 2_500),
        thousands("istanbul", "Roughly how many people live in the Istanbul metropolitan area?", 16_000, 2_000),
        thousands("buenos_aires", "Roughly how many people live in the Buenos Aires metropolitan area?", 15_000, 2_000),
        thousands("moscow", "Roughly how many people live in the Moscow metropolitan area?", 13_000, 1_800),
        thousands("paris", "Roughly how many people live in the Paris metropolitan area?", 11_000, 1_500),
        thousands("london", "Roughly how many people live in Greater London?", 8_900, 1_000),
        thousands("bangkok", "Roughly how many people live in the Bangkok metropolitan area?", 11_000, 1_500),
        thousands("berlin", "Roughly how many people live in the city of Berlin?", 3_700, 450),
        thousands("rome", "Roughly how many people live in the city of Rome?", 2_800, 400),
        thousands("amsterdam", "Roughly how many people live in the city of Amsterdam?", 930, 150),
        thousands("reykjavik", "Roughly how many people live in the city of Reykjavik?", 140, 30),

        /* --------------------------- Skala calego swiata -------------------------- */
        millions("world", "Roughly how many people live on Earth?", 8_100, 350, max = 12_000),
        millions("europe_continent", "Roughly how many people live in Europe?", 745, 50, max = 2_000),
        millions("africa_continent", "Roughly how many people live in Africa?", 1_490, 90, max = 4_000),
        millions("south_america_continent", "Roughly how many people live in South America?", 440, 35),
        millions("european_union", "Roughly how many people live in the European Union?", 449, 30)
    )
}
