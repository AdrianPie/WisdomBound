package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.OrderingEntry
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty

/**
 * Pula testowa pytan typu "ulóz w kolejnosci" - chronologia, rozmiar, odleglosc, wartosc liczbowa.
 * [OrderingEntry.items] jest zawsze zapisane juz we wlasciwym porzadku.
 */
object OrderingQuestions {
    val all: List<OrderingEntry> = listOf(
        OrderingEntry(
            id = "ord_geo_ny_distance",
            prompt = "Zaczynajac od najblizszego, ulóz te miasta wedlug odleglosci od Nowego Jorku.",
            items = listOf("Boston", "Detroit", "Chicago", "Los Angeles"),
            category = QuizCategory.Geography,
            difficulty = QuizDifficulty.Medium
        ),
        OrderingEntry(
            id = "ord_history_wars",
            prompt = "Ulóz te wydarzenia historyczne od najstarszego do najnowszego.",
            items = listOf(
                "Upadek Cesarstwa Rzymskiego",
                "Odkrycie Ameryki przez Kolumba",
                "Rewolucja Francuska",
                "Pierwsza Wojna Swiatowa"
            ),
            category = QuizCategory.History,
            difficulty = QuizDifficulty.Medium
        ),
        OrderingEntry(
            id = "ord_history_inventions",
            prompt = "Ulóz te wynalazki od najstarszego do najnowszego.",
            items = listOf("Kolo", "Prasa drukarska", "Zarowka", "Internet"),
            category = QuizCategory.Technology,
            difficulty = QuizDifficulty.Easy
        ),
        OrderingEntry(
            id = "ord_bio_animal_size",
            prompt = "Ulóz te zwierzeta od najmniejszego do najwiekszego.",
            items = listOf("Mrowka", "Kot", "Kon", "Słoń"),
            category = QuizCategory.Animals,
            difficulty = QuizDifficulty.Easy
        ),
        OrderingEntry(
            id = "ord_bio_animal_lifespan",
            prompt = "Ulóz te zwierzeta od najkrótszej do najdluzszej sredniej dlugosci zycia.",
            items = listOf("Mucha owocowa", "Mysz", "Pies", "Zólw slonowy"),
            category = QuizCategory.Animals,
            difficulty = QuizDifficulty.Hard
        ),
        OrderingEntry(
            id = "ord_science_planets",
            prompt = "Zaczynajac od najblizszej Slonca, ulóz te planety wedlug odleglosci od Slonca.",
            items = listOf("Merkury", "Ziemia", "Mars", "Jowisz"),
            category = QuizCategory.Science,
            difficulty = QuizDifficulty.Medium
        ),
        OrderingEntry(
            id = "ord_geo_mountains",
            prompt = "Ulóz te góry od najnizszej do najwyzszej.",
            items = listOf("Sniezka", "Mont Blanc", "Kilimandzaro", "Mount Everest"),
            category = QuizCategory.Geography,
            difficulty = QuizDifficulty.Hard
        ),
        OrderingEntry(
            id = "ord_math_numbers",
            prompt = "Ulóz te liczby od najmniejszej do najwiekszej.",
            items = listOf("7", "23", "108", "965"),
            category = QuizCategory.Math,
            difficulty = QuizDifficulty.Easy
        ),
        OrderingEntry(
            id = "ord_math_fractions",
            prompt = "Ulóz te ulamki od najmniejszego do najwiekszego.",
            items = listOf("1/8", "1/4", "1/2", "3/4"),
            category = QuizCategory.Math,
            difficulty = QuizDifficulty.Medium
        ),
        OrderingEntry(
            id = "ord_geo_rivers",
            prompt = "Ulóz te rzeki od najkrótszej do najdluzszej.",
            items = listOf("Wisla", "Dunaj", "Nil", "Amazonka"),
            category = QuizCategory.Geography,
            difficulty = QuizDifficulty.Hard
        ),
        OrderingEntry(
            id = "ord_history_dynasties",
            prompt = "Ulóz tych polskich wladcow od najwczesniejszego do najpozniejszego panowania.",
            items = listOf("Mieszko I", "Kazimierz Wielki", "Stefan Batory", "Stanislaw August Poniatowski"),
            category = QuizCategory.History,
            difficulty = QuizDifficulty.Expert
        ),
        OrderingEntry(
            id = "ord_science_hardness",
            prompt = "Ulóz te materialy od najmiekszego do najtwardszego.",
            items = listOf("Talk", "Gips", "Kwarc", "Diament"),
            category = QuizCategory.Science,
            difficulty = QuizDifficulty.Expert
        )
    )
}
