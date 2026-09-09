package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.EstimationEntry

/**
 * Szacowanie MIAR FIZYCZNYCH: wysokosci, dlugosci, glebokosci, powierzchnie, odleglosci.
 *
 * Druga - obok [EstimationQuestionsPopulation] - pula, w ktorej nie chodzi o pamiec do liczb, tylko
 * o wyczucie rzedu wielkosci. Gracz, ktory wie tylko, ze Mount Everest ma "ponad osiem tysiecy",
 * ma tu realna szanse, a ten, ktory strzela "dwa tysiace", nie.
 *
 * Kazde pytanie ma WLASNA jednostke i wlasna tolerancje, bo pomylka o 100 m przy Evereście jest
 * bez znaczenia, a przy Wiezy Eiffla to polowa budynku.
 */
object EstimationQuestionsMeasures {

    private fun measure(
        id: String,
        prompt: String,
        value: Int,
        unit: String,
        tolerance: Int,
        max: Int,
        min: Int = 0
    ) = EstimationEntry(
        id = "estmea_$id",
        prompt = prompt,
        minValue = min,
        maxValue = max,
        correctValue = value,
        unit = unit,
        tolerance = tolerance
    )

    val all: List<EstimationEntry> = listOf(

        /* -------------------------------- Szczyty (m) ------------------------------- */
        measure("everest", "How high is Mount Everest?", 8849, "m", 250, 10_000),
        measure("k2", "How high is K2, the second-highest mountain on Earth?", 8611, "m", 250, 10_000),
        measure("kilimanjaro", "How high is Kilimanjaro, the highest peak in Africa?", 5895, "m", 250, 10_000),
        measure("mont_blanc", "How high is Mont Blanc, the highest peak in the Alps?", 4808, "m", 200, 10_000),
        measure("denali", "How high is Denali, the highest peak in North America?", 6190, "m", 250, 10_000),
        measure("aconcagua", "How high is Aconcagua, the highest peak in South America?", 6961, "m", 250, 10_000),
        measure("elbrus", "How high is Mount Elbrus, the highest peak in the Caucasus?", 5642, "m", 250, 10_000),
        measure("fuji", "How high is Mount Fuji?", 3776, "m", 200, 10_000),

        /* -------------------------------- Rzeki (km) -------------------------------- */
        measure("nil", "How long is the Nile?", 6650, "km", 350, 8_000),
        measure("amazonka", "How long is the Amazon?", 6400, "km", 350, 8_000),
        measure("jangcy", "How long is the Yangtze, the longest river in Asia?", 6300, "km", 350, 8_000),
        measure("mississippi", "How long is the Mississippi itself (without the Missouri)?", 3766, "km", 300, 8_000),
        measure("wolga", "How long is the Volga, the longest river in Europe?", 3530, "km", 250, 8_000),
        measure("dunaj", "How long is the Danube?", 2850, "km", 200, 8_000),
        measure("ren", "How long is the Rhine?", 1230, "km", 120, 8_000),

        /* ------------------------------ Glebokosci (m) ------------------------------ */
        measure("rowmarianski", "How deep is the Mariana Trench at its deepest point?", 10935, "m", 400, 13_000),
        measure("bajkal", "How deep is Lake Baikal, the deepest lake on Earth?", 1642, "m", 120, 2_500),
        measure("morze_srodziemne", "What is the greatest depth of the Mediterranean Sea?", 5267, "m", 350, 7_000),
        measure("baltyk", "What is the greatest depth of the Baltic Sea?", 459, "m", 60, 1_500),

        /* --------------------------- Budowle i mosty (m) ---------------------------- */
        measure("burj_khalifa", "How tall is the Burj Khalifa, the tallest building in the world?", 828, "m", 60, 1_200),
        measure("wieza_eiffla", "How tall is the Eiffel Tower including its antennas?", 330, "m", 30, 600),
        measure("empire_state", "How tall is the Empire State Building to its tip?", 443, "m", 40, 800),
        measure("statua_wolnosci", "How tall is the Statue of Liberty including its pedestal?", 93, "m", 12, 250),
        measure("golden_gate", "How long is the main span of the Golden Gate Bridge?", 1280, "m", 130, 2_500),
        measure("piramida_cheopsa_wys", "How tall was the Great Pyramid of Giza when it was built?", 147, "m", 15, 400),
        measure("krzywa_wieza", "How tall is the Leaning Tower of Pisa?", 57, "m", 8, 200),

        /* ----------------------------- Kanaly, tunele (km) -------------------------- */
        measure("kanal_sueski_dl", "How long is the Suez Canal?", 193, "km", 20, 500),
        measure("kanal_panamski_dl", "How long is the Panama Canal?", 82, "km", 10, 300),
        measure("tunel_pod_kanalem", "How long is the Channel Tunnel?", 50, "km", 7, 200),

        /* ---------------------- Powierzchnie (tysiace km kw.) ----------------------- */
        measure("rosja_pow", "What is the area of Russia?", 17_098, "thousand km2", 1_200, 30_000),
        measure("kanada_pow", "What is the area of Canada?", 9_985, "thousand km2", 800, 30_000),
        measure("australia_pow", "What is the area of Australia?", 7_692, "thousand km2", 650, 30_000),
        measure("sahara_pow", "What is the area of the Sahara?", 9_200, "thousand km2", 900, 30_000),
        measure("amazonia_pow", "What is the area of the Amazon rainforest?", 5_500, "thousand km2", 600, 30_000),
        measure("grenlandia_pow", "What is the area of Greenland?", 2_166, "thousand km2", 250, 10_000),
        measure("morze_kaspijskie_pow", "What is the area of the Caspian Sea?", 371, "thousand km2", 45, 2_000),
        measure("francja_pow", "What is the area of France (mainland and Corsica)?", 552, "thousand km2", 60, 3_000),

        /* -------------------------- Kosmos i planeta (km) --------------------------- */
        measure("obwod_ziemi", "What is the circumference of the Earth at the equator?", 40_075, "km", 2_500, 80_000),
        measure("srednica_ziemi", "What is the mean diameter of the Earth?", 12_742, "km", 800, 30_000),
        measure("odleglosc_ksiezyc", "What is the average distance from the Earth to the Moon?", 384_400, "km", 30_000, 900_000),
        measure("srednica_slonca", "What is the diameter of the Sun?", 1_392_000, "km", 100_000, 3_000_000),
        measure("srednica_ksiezyca", "What is the diameter of the Moon?", 3_475, "km", 300, 10_000),
        measure("iss_wysokosc", "At what altitude does the International Space Station orbit?", 410, "km", 45, 1_500),
        measure("atmosfera_karman", "At what altitude is the Karman line, the conventional edge of space?", 100, "km", 15, 400),

        /* ------------------------------ Predkosci ---------------------------------- */
        measure("predkosc_dzwieku", "How fast does sound travel in air at 20 degrees Celsius?", 343, "m/s", 30, 1_200),
        measure("predkosc_swiatla", "How fast does light travel in a vacuum?", 300_000, "km/s", 20_000, 1_000_000),
        measure("predkosc_ziemi_orbita", "How fast does the Earth move along its orbit around the Sun?", 30, "km/s", 5, 150),
        measure("gepard_predkosc", "What is the top speed of a cheetah?", 110, "km/h", 12, 400),
        measure("jerzyk_predkosc", "What top speed can a peregrine falcon reach in a dive?", 350, "km/h", 40, 1_000),

        /* ------------------------------ Skala czasu -------------------------------- */
        measure("wiek_ziemi", "How old is the Earth?", 4_540, "million years", 350, 15_000),
        measure("wiek_wszechswiata", "How old is the universe?", 13_800, "million years", 900, 40_000),
        measure("dinozaury_wymarcie", "How long ago did the non-bird dinosaurs die out?", 66, "million years", 8, 300)
    )
}
