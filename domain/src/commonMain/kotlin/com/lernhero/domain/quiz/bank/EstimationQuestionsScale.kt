package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.EstimationEntry

/**
 * Szacowania skali: kosmos, cialo, technika, sport. Czwarta pula BEZ dat.
 *
 * Powstala razem z przebudowa kampanii na bieg dwudziestofalowy: elita wchodzi co piata fale, boss
 * co dziesiata, wiec jeden bieg potrafi zadac cztery pytania-szacowania, a jedna plansza powtarzana
 * kilka razy - kilkanascie. Poprzednie pule wystarczaly, gdy zdarzenie bossa wypadalo raz na
 * plansze.
 *
 * Tolerancje sa dobrane tak, zeby "wiem mniej wiecej" bylo nagradzane, a "strzelam" nie: przy
 * wartosciach rzedu setek to kilka procent zakresu, przy milionach - kilkanascie.
 */
object EstimationQuestionsScale {

    private fun count(
        id: String,
        prompt: String,
        value: Int,
        unit: String,
        tolerance: Int,
        max: Int,
        min: Int = 0
    ) = EstimationEntry(
        id = "estscale_$id",
        prompt = prompt,
        minValue = min,
        maxValue = max,
        correctValue = value,
        unit = unit,
        tolerance = tolerance
    )

    val all: List<EstimationEntry> = listOf(

        /* ------------------------------ Kosmos ------------------------------ */
        count("ksiezyc_dystans", "How far is the Moon from Earth, in thousands of kilometers?", 384, "thousand km", 30, 1000),
        count("slonce_srednica", "How many times wider is the Sun than Earth?", 109, "times", 15, 400),
        count("iss_wysokosc", "At what altitude does the ISS orbit, in kilometers?", 408, "km", 50, 2000),
        count("mars_doba", "How many minutes longer is a day on Mars than on Earth?", 37, "minutes", 8, 300),
        count("swiatlo_slonce", "How many minutes does sunlight take to reach Earth?", 8, "minutes", 2, 60),
        count("ksiezyce_jowisza", "How many confirmed moons does Jupiter have (as of recent counts)?", 95, "moons", 15, 300),
        count("predkosc_swiatla", "What is the speed of light, in thousands of km per second?", 300, "thousand km/s", 25, 1000),

        /* ------------------------- Cialo i biologia -------------------------- */
        count("serce_uderzenia", "How many times does an average heart beat per minute at rest?", 72, "beats", 10, 220),
        count("krew_litry", "How many liters of blood does an adult body hold?", 5, "liters", 1, 30),
        count("mozg_neurony", "How many billion neurons are in a human brain?", 86, "billion", 15, 400),
        count("plwoc_dni", "How many days does a red blood cell live, roughly?", 120, "days", 20, 500),
        count("kubki_smakowe", "How many taste buds does an adult tongue hold?", 2000, "taste buds", 500, 10_000),
        count("wlosy_glowa", "How many hairs grow on an average human head, in thousands?", 100, "thousand", 25, 400),
        count("sen_godziny", "How many hours of sleep are recommended for an adult?", 8, "hours", 1, 24),

        /* ------------------------------ Technika ----------------------------- */
        count("everest", "How tall is Mount Everest, in meters?", 8849, "m", 300, 20000),
        count("titanic_dlugosc", "How long was the Titanic, in meters?", 269, "m", 30, 800),
        count("eiffel", "How tall is the Eiffel Tower, in meters?", 330, "m", 30, 1000),
        count("burj", "How tall is the Burj Khalifa, in meters?", 828, "m", 60, 2000),
        count("samolot_wysokosc", "At what altitude does an airliner usually cruise, in thousands of feet?", 35, "thousand ft", 5, 100),
        count("metro_londyn", "How many stations does the London Underground have?", 272, "stations", 35, 800),
        count("kanal_sueski", "How long is the Suez Canal, in kilometers?", 193, "km", 25, 700),

        /* -------------------------------- Sport ------------------------------ */
        count("maraton_km", "How many kilometers is a marathon (rounded)?", 42, "km", 4, 150),
        count("boisko_dlugosc", "How long is a standard football pitch, in meters?", 105, "m", 12, 300),
        count("kosz_wysokosc", "How high is a basketball hoop, in centimeters?", 305, "cm", 25, 900),
        count("basen_olimpijski", "How long is an Olympic swimming pool, in meters?", 50, "m", 6, 200),
        count("tour_dystans", "How many kilometers is the Tour de France, roughly?", 3400, "km", 400, 10000),
        count("golf_dolki", "How many holes does a full round of golf have?", 18, "holes", 3, 100),

        /* ------------------------------- Swiat ------------------------------- */
        count("jezyki_swiat", "How many living languages are spoken worldwide, in thousands?", 7, "thousand", 2, 40),
        count("panstwa_onz", "How many member states does the UN have?", 193, "states", 15, 400),
        count("ocean_glebokosc", "How deep is the Mariana Trench, in meters?", 10935, "m", 800, 30000),
        count("nil_dlugosc", "How long is the Nile, in kilometers?", 6650, "km", 500, 20000),
        count("amazonka_dorzecze", "How many million square kilometers does the Amazon basin cover?", 7, "million km2", 2, 40)
    )
}
