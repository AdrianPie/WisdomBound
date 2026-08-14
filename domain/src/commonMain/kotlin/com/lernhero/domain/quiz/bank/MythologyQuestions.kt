package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Mitologia - bogowie, herosi i potwory (grecka, nordycka, egipska i slowianska). */
object MythologyQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "mit_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Mythology,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Latwe ------------------------------- */
        ask(
            id = "zeus",
            prompt = "Kto byl krolem greckich bogow?",
            correct = "Zeus",
            wrong = listOf("Posejdon", "Hades", "Apollo"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "posejdon",
            prompt = "Ktory grecki bog wladal morzami?",
            correct = "Posejdon",
            wrong = listOf("Zeus", "Ares", "Hermes"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "thor",
            prompt = "Jakiej broni uzywal nordycki bog Thor?",
            correct = "Mlota",
            wrong = listOf("Miecza", "Wloczni", "Luku"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "olimp",
            prompt = "Na jakiej gorze mieszkali greccy bogowie?",
            correct = "Olimp",
            wrong = listOf("Parnas", "Etna", "Ida"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "meduza",
            prompt = "Co mial na glowie potwor Meduza zamiast wlosow?",
            correct = "Weze",
            wrong = listOf("Plomienie", "Pioro", "Robaki"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "herkules",
            prompt = "Z czego slynal Herakles?",
            correct = "Z ogromnej sily",
            wrong = listOf("Z szybkosci", "Z piekna", "Z madrosci"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Srednie ------------------------------ */
        ask(
            id = "hades",
            prompt = "Kto wladal greckim swiatem umarlych?",
            correct = "Hades",
            wrong = listOf("Posejdon", "Kronos", "Dionizos"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "atena",
            prompt = "Ktora bogini byla patronka madrosci i Aten?",
            correct = "Atena",
            wrong = listOf("Hera", "Afrodyta", "Artemida"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "odyn",
            prompt = "Jak nazywal sie najwyzszy bog nordycki?",
            correct = "Odyn",
            wrong = listOf("Thor", "Loki", "Balder"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "anubis",
            prompt = "Ktory egipski bog mial glowe szakala?",
            correct = "Anubis",
            wrong = listOf("Ra", "Horus", "Ozyrys"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "minotaur",
            prompt = "Gdzie mieszkal Minotaur?",
            correct = "W labiryncie na Krecie",
            wrong = listOf("Na Olimpie", "W Troi", "W Hadesie"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "pandora",
            prompt = "Co wypuscila Pandora, otwierajac puszke?",
            correct = "Nieszczescia",
            wrong = listOf("Bogactwa", "Zwierzeta", "Deszcz"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Trudne ------------------------------- */
        ask(
            id = "achilles",
            prompt = "Ktora czesc ciala byla jedynym slabym punktem Achillesa?",
            correct = "Pieta",
            wrong = listOf("Dlon", "Kolano", "Kark"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "cerber",
            prompt = "Ile glow mial Cerber, pies pilnujacy Hadesu?",
            correct = "3",
            wrong = listOf("2", "5", "7"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "yggdrasil",
            prompt = "Jak nazywa sie kosmiczne drzewo w mitologii nordyckiej?",
            correct = "Yggdrasil",
            wrong = listOf("Valhalla", "Bifrost", "Midgard"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "perun",
            prompt = "Ktory slowianski bog wladal piorunami?",
            correct = "Perun",
            wrong = listOf("Swarog", "Weles", "Swiatowid"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "ikar",
            prompt = "Dlaczego Ikar spadl do morza?",
            correct = "Slonce stopilo wosk w jego skrzydlach",
            wrong = listOf("Zlamal skrzydlo o skale", "Postrzelil go Apollo", "Porwal go wiatr polnocny"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Eksperckie ---------------------------- */
        ask(
            id = "ragnarok",
            prompt = "Jak nazywa sie nordycki koniec swiata?",
            correct = "Ragnarok",
            wrong = listOf("Valhalla", "Niflheim", "Ginnungagap"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "syzyf_kara",
            prompt = "Na czym polegala kara Syzyfa?",
            correct = "Wtaczal glaz, ktory wciaz sie staczal",
            wrong = listOf("Ptak wyjadal mu watrobe", "Stal po szyje w wodzie, ktorej nie mogl sie napic", "Trzymal niebo na barkach"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "sfinks_zagadka",
            prompt = "Kto rozwiazal zagadke Sfinksa pod Tebami?",
            correct = "Edyp",
            wrong = listOf("Tezeusz", "Perseusz", "Jazon"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
