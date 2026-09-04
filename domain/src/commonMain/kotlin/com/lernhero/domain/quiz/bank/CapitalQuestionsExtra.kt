package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Second batch of capitals - the rest of Europe, Asia, Africa and the islands people forget. */
object CapitalQuestionsExtra {

    private fun capital(
        country: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "cap2_${correct.lowercase().replace(" ", "_")}",
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
        id = "cap2_kraj_${city.lowercase().replace(" ", "_")}",
        prompt = "$city is the capital of which country?",
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Capitals,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        capital("Czechia", "Prague", listOf("Brno", "Ostrava", "Pilsen"), QuizDifficulty.Easy),
        capital("Slovakia", "Bratislava", listOf("Kosice", "Zilina", "Nitra"), QuizDifficulty.Easy),
        capital("Russia", "Moscow", listOf("Saint Petersburg", "Novosibirsk", "Kazan"), QuizDifficulty.Easy),
        capital("Greece", "Athens", listOf("Thessaloniki", "Patras", "Heraklion"), QuizDifficulty.Easy),
        capital("Austria", "Vienna", listOf("Salzburg", "Graz", "Linz"), QuizDifficulty.Easy),
        capital("Hungary", "Budapest", listOf("Debrecen", "Szeged", "Miskolc"), QuizDifficulty.Easy),
        capital("China", "Beijing", listOf("Shanghai", "Guangzhou", "Shenzhen"), QuizDifficulty.Easy),
        capital("Egypt", "Cairo", listOf("Alexandria", "Giza", "Luxor"), QuizDifficulty.Easy),
        country("Stockholm", "Sweden", listOf("Norway", "Finland", "Denmark"), QuizDifficulty.Easy),
        country("Amsterdam", "Netherlands", listOf("Belgium", "Denmark", "Germany"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        capital("Finland", "Helsinki", listOf("Tampere", "Turku", "Espoo"), QuizDifficulty.Medium),
        capital("Ireland", "Dublin", listOf("Cork", "Galway", "Limerick"), QuizDifficulty.Medium),
        capital("Romania", "Bucharest", listOf("Cluj-Napoca", "Timisoara", "Iasi"), QuizDifficulty.Medium),
        capital("Bulgaria", "Sofia", listOf("Plovdiv", "Varna", "Burgas"), QuizDifficulty.Medium),
        capital("Croatia", "Zagreb", listOf("Split", "Rijeka", "Dubrovnik"), QuizDifficulty.Medium),
        capital("Serbia", "Belgrade", listOf("Novi Sad", "Nis", "Subotica"), QuizDifficulty.Medium),
        capital("India", "New Delhi", listOf("Mumbai", "Kolkata", "Bangalore"), QuizDifficulty.Medium),
        capital("Argentina", "Buenos Aires", listOf("Cordoba", "Rosario", "Mendoza"), QuizDifficulty.Medium),
        capital("Mexico", "Mexico City", listOf("Guadalajara", "Monterrey", "Cancun"), QuizDifficulty.Medium),
        capital("South Korea", "Seoul", listOf("Busan", "Incheon", "Daegu"), QuizDifficulty.Medium),
        capital("Israel", "Jerusalem", listOf("Tel Aviv", "Haifa", "Eilat"), QuizDifficulty.Medium),
        country("Ljubljana", "Slovenia", listOf("Croatia", "Slovakia", "Austria"), QuizDifficulty.Medium),
        country("Vilnius", "Lithuania", listOf("Latvia", "Estonia", "Belarus"), QuizDifficulty.Medium),
        country("Riga", "Latvia", listOf("Lithuania", "Estonia", "Finland"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        capital("Estonia", "Tallinn", listOf("Riga", "Vilnius", "Helsinki"), QuizDifficulty.Hard),
        capital("Albania", "Tirana", listOf("Durres", "Vlore", "Shkoder"), QuizDifficulty.Hard),
        capital("Belarus", "Minsk", listOf("Gomel", "Brest", "Vitebsk"), QuizDifficulty.Hard),
        capital("Peru", "Lima", listOf("Cusco", "Arequipa", "Trujillo"), QuizDifficulty.Hard),
        capital("Chile", "Santiago", listOf("Valparaiso", "Concepcion", "Antofagasta"), QuizDifficulty.Hard),
        capital("Colombia", "Bogota", listOf("Medellin", "Cali", "Cartagena"), QuizDifficulty.Hard),
        capital("Cuba", "Havana", listOf("Santiago de Cuba", "Camaguey", "Holguin"), QuizDifficulty.Hard),
        capital("Indonesia", "Jakarta", listOf("Surabaya", "Bandung", "Medan"), QuizDifficulty.Hard),
        capital("Pakistan", "Islamabad", listOf("Karachi", "Lahore", "Peshawar"), QuizDifficulty.Hard),
        capital("Saudi Arabia", "Riyadh", listOf("Jeddah", "Mecca", "Medina"), QuizDifficulty.Hard),
        capital("Iran", "Tehran", listOf("Isfahan", "Mashhad", "Shiraz"), QuizDifficulty.Hard),
        capital("Ethiopia", "Addis Ababa", listOf("Dire Dawa", "Gondar", "Mekelle"), QuizDifficulty.Hard),
        capital("Tanzania", "Dodoma", listOf("Dar es Salaam", "Arusha", "Mwanza"), QuizDifficulty.Hard),
        country("Ulaanbaatar", "Mongolia", listOf("Kazakhstan", "Kyrgyzstan", "Tibet"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        capital("Bolivia (constitutional capital)", "Sucre", listOf("La Paz", "Santa Cruz", "Cochabamba"), QuizDifficulty.Expert),
        capital("Ivory Coast", "Yamoussoukro", listOf("Abidjan", "Bouake", "Korhogo"), QuizDifficulty.Expert),
        capital("Sri Lanka (administrative capital)", "Sri Jayawardenepura Kotte", listOf("Colombo", "Kandy", "Galle"), QuizDifficulty.Expert),
        capital("Tajikistan", "Dushanbe", listOf("Khujand", "Kulob", "Qurghonteppa"), QuizDifficulty.Expert),
        capital("Turkmenistan", "Ashgabat", listOf("Turkmenbashy", "Mary", "Dashoguz"), QuizDifficulty.Expert),
        capital("Azerbaijan", "Baku", listOf("Ganja", "Sumgait", "Lankaran"), QuizDifficulty.Expert),
        capital("Nepal", "Kathmandu", listOf("Pokhara", "Lalitpur", "Biratnagar"), QuizDifficulty.Expert),
        capital("Laos", "Vientiane", listOf("Luang Prabang", "Pakse", "Savannakhet"), QuizDifficulty.Expert),
        capital("Cambodia", "Phnom Penh", listOf("Siem Reap", "Battambang", "Sihanoukville"), QuizDifficulty.Expert),
        capital("Madagascar", "Antananarivo", listOf("Toamasina", "Mahajanga", "Toliara"), QuizDifficulty.Expert),
        capital("Namibia", "Windhoek", listOf("Walvis Bay", "Swakopmund", "Oshakati"), QuizDifficulty.Expert),
        capital("Papua New Guinea", "Port Moresby", listOf("Lae", "Mount Hagen", "Madang"), QuizDifficulty.Expert),
        country("Suva", "Fiji", listOf("Samoa", "Tonga", "Vanuatu"), QuizDifficulty.Expert),
        country("Nicosia", "Cyprus", listOf("Malta", "Greece", "Lebanon"), QuizDifficulty.Expert)
    )
}
