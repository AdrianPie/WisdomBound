package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * "What's the capital of this?" - and the reverse. Easy ones end at Paris, expert ones start
 * right where most people trip up (Sydney, Rio, Istanbul).
 */
object CapitalQuestions {

    private fun capital(
        country: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "cap_${correct.lowercase().replace(" ", "_")}",
        prompt = "What is the capital of $country?",
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Capitals,
        difficulty = difficulty
    )

    /** Reverse: we give the city, the player names the country. */
    private fun country(
        city: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "cap_kraj_${city.lowercase().replace(" ", "_")}",
        prompt = "$city is the capital of which country?",
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Capitals,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        capital("Poland", "Warsaw", listOf("Krakow", "Lodz", "Gdansk"), QuizDifficulty.Easy),
        capital("France", "Paris", listOf("Lyon", "Marseille", "Nice"), QuizDifficulty.Easy),
        capital("Germany", "Berlin", listOf("Munich", "Hamburg", "Frankfurt"), QuizDifficulty.Easy),
        capital("Italy", "Rome", listOf("Milan", "Naples", "Turin"), QuizDifficulty.Easy),
        capital("Spain", "Madrid", listOf("Barcelona", "Seville", "Valencia"), QuizDifficulty.Easy),
        capital("United Kingdom", "London", listOf("Manchester", "Edinburgh", "Liverpool"), QuizDifficulty.Easy),
        capital("Japan", "Tokyo", listOf("Osaka", "Kyoto", "Nagoya"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        capital("Australia", "Canberra", listOf("Sydney", "Melbourne", "Perth"), QuizDifficulty.Medium),
        capital("Canada", "Ottawa", listOf("Toronto", "Montreal", "Vancouver"), QuizDifficulty.Medium),
        capital("Brazil", "Brasilia", listOf("Rio de Janeiro", "Sao Paulo", "Salvador"), QuizDifficulty.Medium),
        capital("Turkey", "Ankara", listOf("Istanbul", "Izmir", "Bursa"), QuizDifficulty.Medium),
        capital("Switzerland", "Bern", listOf("Zurich", "Geneva", "Basel"), QuizDifficulty.Medium),
        capital("Portugal", "Lisbon", listOf("Porto", "Braga", "Faro"), QuizDifficulty.Medium),
        capital("Norway", "Oslo", listOf("Bergen", "Trondheim", "Stavanger"), QuizDifficulty.Medium),
        capital("Ukraine", "Kyiv", listOf("Lviv", "Odesa", "Kharkiv"), QuizDifficulty.Medium),
        country("Copenhagen", "Denmark", listOf("Sweden", "Norway", "Netherlands"), QuizDifficulty.Medium),
        country("Brussels", "Belgium", listOf("Netherlands", "Luxembourg", "France"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        capital("New Zealand", "Wellington", listOf("Auckland", "Christchurch", "Dunedin"), QuizDifficulty.Hard),
        capital("Vietnam", "Hanoi", listOf("Ho Chi Minh City", "Da Nang", "Hue"), QuizDifficulty.Hard),
        capital("Morocco", "Rabat", listOf("Casablanca", "Marrakesh", "Fez"), QuizDifficulty.Hard),
        capital("Nigeria", "Abuja", listOf("Lagos", "Kano", "Ibadan"), QuizDifficulty.Hard),
        capital("Iceland", "Reykjavik", listOf("Akureyri", "Keflavik", "Selfoss"), QuizDifficulty.Hard),
        capital("Kenya", "Nairobi", listOf("Mombasa", "Kisumu", "Nakuru"), QuizDifficulty.Hard),
        capital("Philippines", "Manila", listOf("Cebu", "Davao", "Quezon City"), QuizDifficulty.Hard),
        country("Pretoria", "South Africa", listOf("Kenya", "Zimbabwe", "Namibia"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        capital("Myanmar", "Naypyidaw", listOf("Yangon", "Mandalay", "Bago"), QuizDifficulty.Expert),
        capital("Bhutan", "Thimphu", listOf("Paro", "Punakha", "Phuentsholing"), QuizDifficulty.Expert),
        capital("Mongolia", "Ulaanbaatar", listOf("Erdenet", "Darkhan", "Khovd"), QuizDifficulty.Expert),
        capital("Kyrgyzstan", "Bishkek", listOf("Osh", "Naryn", "Karakol"), QuizDifficulty.Expert),
        capital("Eritrea", "Asmara", listOf("Massawa", "Keren", "Assab"), QuizDifficulty.Expert),
        country("Ouagadougou", "Burkina Faso", listOf("Mali", "Niger", "Chad"), QuizDifficulty.Expert),
        country("Tbilisi", "Georgia", listOf("Armenia", "Azerbaijan", "Uzbekistan"), QuizDifficulty.Expert)
    )
}
