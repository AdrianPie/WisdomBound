package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.OrderingEntry
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty

/**
 * Test pool of "put in order" questions - chronology, size, distance, numeric value.
 * [OrderingEntry.items] is always already stored in the correct order.
 */
object OrderingQuestions {
    val all: List<OrderingEntry> = listOf(
        OrderingEntry(
            id = "ord_geo_ny_distance",
            prompt = "Starting with the closest, order these cities by distance from New York.",
            items = listOf("Boston", "Detroit", "Chicago", "Los Angeles"),
            category = QuizCategory.Geography,
            difficulty = QuizDifficulty.Medium
        ),
        OrderingEntry(
            id = "ord_history_wars",
            prompt = "Order these historical events from oldest to most recent.",
            items = listOf(
                "The fall of the Roman Empire",
                "Columbus's discovery of America",
                "The French Revolution",
                "World War I"
            ),
            category = QuizCategory.History,
            difficulty = QuizDifficulty.Medium
        ),
        OrderingEntry(
            id = "ord_history_inventions",
            prompt = "Order these inventions from oldest to newest.",
            items = listOf("The wheel", "The printing press", "The light bulb", "The internet"),
            category = QuizCategory.Technology,
            difficulty = QuizDifficulty.Easy
        ),
        OrderingEntry(
            id = "ord_bio_animal_lifespan",
            prompt = "Order these animals from shortest to longest average lifespan.",
            items = listOf("Fruit fly", "Mouse", "Dog", "Giant tortoise"),
            category = QuizCategory.Animals,
            difficulty = QuizDifficulty.Hard
        ),
        OrderingEntry(
            id = "ord_science_planets",
            prompt = "Starting with the closest to the Sun, order these planets by distance from the Sun.",
            items = listOf("Mercury", "Earth", "Mars", "Jupiter"),
            category = QuizCategory.Science,
            difficulty = QuizDifficulty.Medium
        ),
        OrderingEntry(
            id = "ord_geo_mountains",
            prompt = "Order these mountains from shortest to tallest.",
            items = listOf("Sniezka", "Mont Blanc", "Kilimanjaro", "Mount Everest"),
            category = QuizCategory.Geography,
            difficulty = QuizDifficulty.Hard
        ),
        OrderingEntry(
            id = "ord_math_fractions",
            prompt = "Order these fractions from smallest to largest.",
            items = listOf("1/8", "1/4", "1/2", "3/4"),
            category = QuizCategory.Math,
            difficulty = QuizDifficulty.Medium
        ),
        OrderingEntry(
            id = "ord_geo_rivers",
            prompt = "Order these rivers from shortest to longest.",
            items = listOf("Vistula", "Danube", "Nile", "Amazon"),
            category = QuizCategory.Geography,
            difficulty = QuizDifficulty.Hard
        ),
        OrderingEntry(
            id = "ord_history_dynasties",
            prompt = "Order these Polish rulers from earliest to latest reign.",
            items = listOf("Mieszko I", "Casimir the Great", "Stephen Bathory", "Stanislaw August Poniatowski"),
            category = QuizCategory.History,
            difficulty = QuizDifficulty.Expert
        ),
        OrderingEntry(
            id = "ord_science_hardness",
            prompt = "Order these materials from softest to hardest.",
            items = listOf("Talc", "Gypsum", "Quartz", "Diamond"),
            category = QuizCategory.Science,
            difficulty = QuizDifficulty.Expert
        )
    )
}
