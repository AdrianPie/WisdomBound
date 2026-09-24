package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Third batch of capitals - the rest of the Americas, Asia and Africa not yet covered. */
object CapitalQuestionsExtra2 {

    private fun capital(
        country: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "cap3_${correct.lowercase().replace(" ", "_")}",
        prompt = "What is the capital of $country?",
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Capitals,
        difficulty = difficulty
    )

    private fun country(
        city: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "cap3_kraj_${city.lowercase().replace(" ", "_")}",
        prompt = "$city is the capital of which country?",
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Capitals,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        capital("Venezuela", "Caracas", listOf("Bogota", "Lima", "Quito"), QuizDifficulty.Easy),
        capital("Ecuador", "Quito", listOf("Bogota", "Lima", "Caracas"), QuizDifficulty.Easy),
        capital("Kuwait", "Kuwait City", listOf("Riyadh", "Doha", "Manama"), QuizDifficulty.Easy),
        capital("Qatar", "Doha", listOf("Dubai", "Abu Dhabi", "Manama"), QuizDifficulty.Easy),
        capital("United Arab Emirates", "Abu Dhabi", listOf("Dubai", "Doha", "Manama"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        capital("Uruguay", "Montevideo", listOf("Buenos Aires", "Santiago", "Asuncion"), QuizDifficulty.Medium),
        capital("Paraguay", "Asuncion", listOf("Montevideo", "La Paz", "Santa Cruz"), QuizDifficulty.Medium),
        capital("Bolivia (seat of government)", "La Paz", listOf("Sucre", "Santa Cruz", "Cochabamba"), QuizDifficulty.Medium),
        capital("Costa Rica", "San Jose", listOf("Panama City", "Managua", "Tegucigalpa"), QuizDifficulty.Medium),
        capital("Panama", "Panama City", listOf("San Jose", "Managua", "Bogota"), QuizDifficulty.Medium),
        capital("Malaysia", "Kuala Lumpur", listOf("Singapore", "Jakarta", "Bangkok"), QuizDifficulty.Medium),
        capital("Singapore", "Singapore", listOf("Kuala Lumpur", "Jakarta", "Manila"), QuizDifficulty.Medium),
        capital("Thailand", "Bangkok", listOf("Hanoi", "Phnom Penh", "Vientiane"), QuizDifficulty.Medium),
        capital("Algeria", "Algiers", listOf("Tunis", "Tripoli", "Rabat"), QuizDifficulty.Medium),
        capital("Tunisia", "Tunis", listOf("Algiers", "Tripoli", "Rabat"), QuizDifficulty.Medium),
        capital("Libya", "Tripoli", listOf("Tunis", "Algiers", "Cairo"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        capital("Ghana", "Accra", listOf("Lagos", "Abidjan", "Lome"), QuizDifficulty.Hard),
        capital("Senegal", "Dakar", listOf("Bamako", "Conakry", "Nouakchott"), QuizDifficulty.Hard),
        capital("Mali", "Bamako", listOf("Dakar", "Ouagadougou", "Niamey"), QuizDifficulty.Hard),
        capital("Zambia", "Lusaka", listOf("Harare", "Lilongwe", "Gaborone"), QuizDifficulty.Hard),
        capital("Zimbabwe", "Harare", listOf("Lusaka", "Maputo", "Gaborone"), QuizDifficulty.Hard),
        capital("Angola", "Luanda", listOf("Maputo", "Kinshasa", "Windhoek"), QuizDifficulty.Hard),
        capital("Democratic Republic of the Congo", "Kinshasa", listOf("Brazzaville", "Luanda", "Kigali"), QuizDifficulty.Hard),
        capital("Uganda", "Kampala", listOf("Nairobi", "Kigali", "Bujumbura"), QuizDifficulty.Hard),
        capital("Rwanda", "Kigali", listOf("Bujumbura", "Kampala", "Nairobi"), QuizDifficulty.Hard),
        capital("Bangladesh", "Dhaka", listOf("Chittagong", "Kolkata", "Karachi"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        capital("Botswana", "Gaborone", listOf("Windhoek", "Lusaka", "Maseru"), QuizDifficulty.Expert),
        capital("Lesotho", "Maseru", listOf("Mbabane", "Gaborone", "Bloemfontein"), QuizDifficulty.Expert),
        capital("Eswatini", "Mbabane", listOf("Maseru", "Pretoria", "Maputo"), QuizDifficulty.Expert),
        capital("Malawi", "Lilongwe", listOf("Blantyre", "Lusaka", "Harare"), QuizDifficulty.Expert),
        capital("Sudan", "Khartoum", listOf("Cairo", "Addis Ababa", "Juba"), QuizDifficulty.Expert),
        capital("South Sudan", "Juba", listOf("Khartoum", "Kampala", "Nairobi"), QuizDifficulty.Expert),
        capital("Yemen", "Sanaa", listOf("Aden", "Muscat", "Riyadh"), QuizDifficulty.Expert),
        capital("Oman", "Muscat", listOf("Sanaa", "Manama", "Abu Dhabi"), QuizDifficulty.Expert),
        capital("Brunei", "Bandar Seri Begawan", listOf("Kuala Lumpur", "Manila", "Jakarta"), QuizDifficulty.Expert),
        capital("Mauritania", "Nouakchott", listOf("Dakar", "Bamako", "Nouadhibou"), QuizDifficulty.Expert)
    )
}
