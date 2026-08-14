package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Geografia - rzeki, gory, morza i kontynenty. */
object GeographyQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "geo_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Geography,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Latwe ------------------------------- */
        ask(
            id = "najdluzsza_rzeka_pl",
            prompt = "Ktora rzeka jest najdluzsza w Polsce?",
            correct = "Wisla",
            wrong = listOf("Odra", "Warta", "Bug"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "najwyzszy_szczyt_pl",
            prompt = "Jak nazywa sie najwyzszy szczyt Polski?",
            correct = "Rysy",
            wrong = listOf("Giewont", "Sniezka", "Babia Gora"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kontynenty",
            prompt = "Ile jest kontynentow na Ziemi?",
            correct = "7",
            wrong = listOf("5", "6", "8"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "najwiekszy_ocean",
            prompt = "Ktory ocean jest najwiekszy?",
            correct = "Spokojny",
            wrong = listOf("Atlantycki", "Indyjski", "Arktyczny"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "morze_pl",
            prompt = "Nad jakim morzem lezy Polska?",
            correct = "Baltyckim",
            wrong = listOf("Polnocnym", "Srodziemnym", "Czarnym"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "sahara",
            prompt = "Na ktorym kontynencie lezy Sahara?",
            correct = "Afryka",
            wrong = listOf("Azja", "Australia", "Ameryka Poludniowa"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Srednie ------------------------------ */
        ask(
            id = "najdluzsza_rzeka_swiat",
            prompt = "Ktora rzeka jest uznawana za najdluzsza na swiecie?",
            correct = "Nil",
            wrong = listOf("Amazonka", "Jangcy", "Missisipi"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "everest",
            prompt = "W jakich gorach lezy Mount Everest?",
            correct = "Himalaje",
            wrong = listOf("Andy", "Alpy", "Kaukaz"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "najwieksze_panstwo",
            prompt = "Ktore panstwo ma najwieksza powierzchnie na swiecie?",
            correct = "Rosja",
            wrong = listOf("Kanada", "Chiny", "USA"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "wielki_kanion",
            prompt = "W ktorym panstwie znajduje sie Wielki Kanion?",
            correct = "USA",
            wrong = listOf("Meksyk", "Australia", "RPA"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "najwieksza_wyspa",
            prompt = "Ktora wyspa jest najwieksza na swiecie?",
            correct = "Grenlandia",
            wrong = listOf("Madagaskar", "Borneo", "Nowa Gwinea"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "wulkan_wl",
            prompt = "Jak nazywa sie slynny wulkan kolo Neapolu?",
            correct = "Wezuwiusz",
            wrong = listOf("Etna", "Stromboli", "Krakatau"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "rzeka_egipt",
            prompt = "Ktora rzeka przeplywa przez Egipt?",
            correct = "Nil",
            wrong = listOf("Eufrat", "Tygrys", "Kongo"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Trudne ------------------------------- */
        ask(
            id = "bajkal",
            prompt = "Ktore jezioro jest najglebsze na swiecie?",
            correct = "Bajkal",
            wrong = listOf("Tanganika", "Morze Kaspijskie", "Superior"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "atacama",
            prompt = "Na ktorym kontynencie lezy pustynia Atacama?",
            correct = "Ameryka Poludniowa",
            wrong = listOf("Afryka", "Azja", "Australia"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "ciesnina",
            prompt = "Jaka ciesnina oddziela Europe od Afryki?",
            correct = "Gibraltarska",
            wrong = listOf("Bosfor", "Beringa", "Ormuz"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "kilimandzaro",
            prompt = "W ktorym panstwie lezy Kilimandzaro?",
            correct = "Tanzania",
            wrong = listOf("Kenia", "Etiopia", "Uganda"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "aconcagua",
            prompt = "Najwyzszy szczyt Ameryki Poludniowej to...",
            correct = "Aconcagua",
            wrong = listOf("Chimborazo", "Huascaran", "Ojos del Salado"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Eksperckie ---------------------------- */
        ask(
            id = "row_marianski",
            prompt = "Jak nazywa sie najglebsze miejsce oceanow?",
            correct = "Glebia Challengera",
            wrong = listOf("Rów Tonga", "Rów Jawajski", "Glebia Milwaukee"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "kraj_bez_rzek",
            prompt = "Ktore z tych panstw nie ma ani jednej stalej rzeki?",
            correct = "Arabia Saudyjska",
            wrong = listOf("Mongolia", "Kazachstan", "Iran"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "titicaca",
            prompt = "Na granicy ktorych panstw lezy jezioro Titicaca?",
            correct = "Peru i Boliwii",
            wrong = listOf("Chile i Argentyny", "Ekwadoru i Kolumbii", "Brazylii i Paragwaju"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
