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

    /**
     * Tysiace - WYLACZNIE dla miejsc ponizej miliona mieszkancow (Reykjavik, Malta, Islandia).
     *
     * Wszystko powyzej wrocilo do milionow, i to nie jest kosmetyka. "Ile osob mieszka w Norwegii?"
     * z odpowiedzia 5600 tysiecy jest pytaniem o przeliczanie, a nie o wyczucie skali: nikt nie ma
     * w glowie liczby mieszkancow w tysiacach, kazdy ma ja w milionach ("jakies piec i pol"). To
     * samo z Tokio - "37 000 tysiecy" trzeba sobie w glowie podzielic, zanim w ogole zacznie sie
     * myslec o odpowiedzi.
     */
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

        /* --- Male kraje. Granica jest przy ~2 mln: powyzej milion jest jednostka, w ktorej
           sie mysli, ponizej zaokraglenie do pelnych milionow zjadloby cala odpowiedz. --- */
        millions("norway", "Roughly how many people live in Norway?", 6, 1, max = 60),
        millions("new_zealand", "Roughly how many people live in New Zealand?", 5, 1, max = 60),
        millions("ireland", "Roughly how many people live in Ireland?", 5, 1, max = 60),
        thousands("estonia", "Roughly how many people live in Estonia?", 1_370, 150),
        thousands("cyprus", "Roughly how many people live in Cyprus?", 1_300, 180),
        thousands("luxembourg", "Roughly how many people live in Luxembourg?", 670, 90),
        thousands("malta", "Roughly how many people live in Malta?", 560, 80),
        thousands("iceland", "Roughly how many people live in Iceland?", 390, 60),

        /* ------------------------ Miasta - aglomeracje ---------------------------- */
        millions("tokyo", "Roughly how many people live in the Tokyo metropolitan area?", 37, 4, max = 60),
        millions("delhi", "Roughly how many people live in the Delhi metropolitan area?", 33, 4, max = 60),
        millions("shanghai", "Roughly how many people live in the Shanghai metropolitan area?", 29, 3, max = 60),
        millions("sao_paulo", "Roughly how many people live in the Sao Paulo metropolitan area?", 22, 2, max = 60),
        millions("cairo", "Roughly how many people live in the Cairo metropolitan area?", 22, 2, max = 60),
        millions("mexico_city", "Roughly how many people live in the Mexico City metropolitan area?", 22, 2, max = 60),
        millions("new_york", "Roughly how many people live in the New York metropolitan area?", 19, 2, max = 60),
        millions("lagos", "Roughly how many people live in the Lagos metropolitan area?", 16, 2, max = 60),
        millions("istanbul", "Roughly how many people live in the Istanbul metropolitan area?", 16, 2, max = 60),
        millions("buenos_aires", "Roughly how many people live in the Buenos Aires metropolitan area?", 15, 2, max = 60),
        millions("moscow", "Roughly how many people live in the Moscow metropolitan area?", 13, 2, max = 60),
        millions("paris", "Roughly how many people live in the Paris metropolitan area?", 11, 2, max = 60),
        millions("london", "Roughly how many people live in Greater London?", 9, 1, max = 60),
        millions("bangkok", "Roughly how many people live in the Bangkok metropolitan area?", 11, 2, max = 60),
        millions("berlin", "Roughly how many people live in the city of Berlin?", 4, 1, max = 60),
        millions("rome", "Roughly how many people live in the city of Rome?", 3, 1, max = 60),
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
