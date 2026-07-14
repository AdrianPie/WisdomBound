package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * "Czego to stolica?" - i odwrotnie. Latwe koncza sie na Paryzu, eksperckie zaczynaja
 * tam, gdzie wiekszosc ludzi wpada w pulapke (Sydney, Rio, Stambul).
 */
object CapitalQuestions {

    private fun capital(
        country: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "cap_${correct.lowercase().replace(" ", "_")}",
        prompt = "Jaka jest stolica panstwa: $country?",
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Capitals,
        difficulty = difficulty
    )

    /** Odwrotnosc: podajemy miasto, gracz wskazuje kraj. */
    private fun country(
        city: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "cap_kraj_${city.lowercase().replace(" ", "_")}",
        prompt = "Stolica jakiego panstwa jest $city?",
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Capitals,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Latwe ------------------------------- */
        capital("Polska", "Warszawa", listOf("Krakow", "Lodz", "Gdansk"), QuizDifficulty.Easy),
        capital("Francja", "Paryz", listOf("Lyon", "Marsylia", "Nicea"), QuizDifficulty.Easy),
        capital("Niemcy", "Berlin", listOf("Monachium", "Hamburg", "Frankfurt"), QuizDifficulty.Easy),
        capital("Wlochy", "Rzym", listOf("Mediolan", "Neapol", "Turyn"), QuizDifficulty.Easy),
        capital("Hiszpania", "Madryt", listOf("Barcelona", "Sewilla", "Walencja"), QuizDifficulty.Easy),
        capital("Wielka Brytania", "Londyn", listOf("Manchester", "Edynburg", "Liverpool"), QuizDifficulty.Easy),
        capital("Japonia", "Tokio", listOf("Osaka", "Kioto", "Nagoja"), QuizDifficulty.Easy),
        country("Ateny", "Grecja", listOf("Turcja", "Cypr", "Bulgaria"), QuizDifficulty.Easy),

        /* ------------------------------ Srednie ------------------------------ */
        capital("Australia", "Canberra", listOf("Sydney", "Melbourne", "Perth"), QuizDifficulty.Medium),
        capital("Kanada", "Ottawa", listOf("Toronto", "Montreal", "Vancouver"), QuizDifficulty.Medium),
        capital("Brazylia", "Brasilia", listOf("Rio de Janeiro", "Sao Paulo", "Salvador"), QuizDifficulty.Medium),
        capital("Turcja", "Ankara", listOf("Stambul", "Izmir", "Bursa"), QuizDifficulty.Medium),
        capital("Szwajcaria", "Berno", listOf("Zurych", "Genewa", "Bazylea"), QuizDifficulty.Medium),
        capital("Portugalia", "Lizbona", listOf("Porto", "Braga", "Faro"), QuizDifficulty.Medium),
        capital("Norwegia", "Oslo", listOf("Bergen", "Trondheim", "Stavanger"), QuizDifficulty.Medium),
        capital("Ukraina", "Kijow", listOf("Lwow", "Odessa", "Charkow"), QuizDifficulty.Medium),
        country("Kopenhaga", "Dania", listOf("Szwecja", "Norwegia", "Holandia"), QuizDifficulty.Medium),
        country("Bruksela", "Belgia", listOf("Holandia", "Luksemburg", "Francja"), QuizDifficulty.Medium),

        /* ------------------------------ Trudne ------------------------------- */
        capital("Nowa Zelandia", "Wellington", listOf("Auckland", "Christchurch", "Dunedin"), QuizDifficulty.Hard),
        capital("Wietnam", "Hanoi", listOf("Ho Chi Minh", "Da Nang", "Hue"), QuizDifficulty.Hard),
        capital("Maroko", "Rabat", listOf("Casablanca", "Marrakesz", "Fez"), QuizDifficulty.Hard),
        capital("Nigeria", "Abuja", listOf("Lagos", "Kano", "Ibadan"), QuizDifficulty.Hard),
        capital("Islandia", "Reykjavik", listOf("Akureyri", "Keflavik", "Selfoss"), QuizDifficulty.Hard),
        capital("Kenia", "Nairobi", listOf("Mombasa", "Kisumu", "Nakuru"), QuizDifficulty.Hard),
        capital("Filipiny", "Manila", listOf("Cebu", "Davao", "Quezon"), QuizDifficulty.Hard),
        country("Pretoria", "Republika Poludniowej Afryki", listOf("Kenia", "Zimbabwe", "Namibia"), QuizDifficulty.Hard),

        /* ----------------------------- Eksperckie ---------------------------- */
        capital("Kazachstan", "Astana", listOf("Almaty", "Szymkent", "Karaganda"), QuizDifficulty.Expert),
        capital("Mjanma", "Naypyidaw", listOf("Rangun", "Mandalaj", "Bago"), QuizDifficulty.Expert),
        capital("Bhutan", "Thimphu", listOf("Paro", "Punakha", "Phuentsholing"), QuizDifficulty.Expert),
        capital("Mongolia", "Ulan Bator", listOf("Erdenet", "Darchan", "Chowd"), QuizDifficulty.Expert),
        capital("Kirgistan", "Biszkek", listOf("Osz", "Naryn", "Karakol"), QuizDifficulty.Expert),
        capital("Erytrea", "Asmara", listOf("Massawa", "Keren", "Assab"), QuizDifficulty.Expert),
        country("Wagadugu", "Burkina Faso", listOf("Mali", "Niger", "Czad"), QuizDifficulty.Expert),
        country("Tbilisi", "Gruzja", listOf("Armenia", "Azerbejdzan", "Uzbekistan"), QuizDifficulty.Expert)
    )
}
