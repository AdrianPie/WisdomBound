package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Zwierzeta - od kangura po kalmara olbrzymiego. */
object AnimalQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "ani_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Animals,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Latwe ------------------------------- */
        ask(
            id = "najwieksze",
            prompt = "Jakie zwierze jest najwieksze na Ziemi?",
            correct = "Pletwal blekitny",
            wrong = listOf("Slon afrykanski", "Zyrafa", "Rekin wielorybi"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "pajak_nogi",
            prompt = "Ile nog ma pajak?",
            correct = "8",
            wrong = listOf("6", "10", "4"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "gepard",
            prompt = "Ktore zwierze jest najszybsze na ladzie?",
            correct = "Gepard",
            wrong = listOf("Lew", "Kon", "Antylopa"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "panda",
            prompt = "Czym zywi sie panda wielka?",
            correct = "Bambusem",
            wrong = listOf("Miesem", "Rybami", "Orzechami"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kangur",
            prompt = "Ktore zwierze nosi mlode w torbie na brzuchu?",
            correct = "Kangur",
            wrong = listOf("Lama", "Dingo", "Tapir"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "zyrafa",
            prompt = "Ktore zwierze ladowe jest najwyzsze?",
            correct = "Zyrafa",
            wrong = listOf("Slon", "Wielblad", "Los"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "miod",
            prompt = "Ktory owad wytwarza miod?",
            correct = "Pszczola",
            wrong = listOf("Osa", "Mrowka", "Motyl"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "krolowa_dzungli",
            prompt = "Ktore z tych zwierzat jest ssakiem?",
            correct = "Delfin",
            wrong = listOf("Rekin", "Krokodyl", "Waz"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Srednie ------------------------------ */
        ask(
            id = "strus",
            prompt = "Ktory ptak nie potrafi latac?",
            correct = "Strus",
            wrong = listOf("Sokol", "Jaskolka", "Czapla"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "osmiornica_serca",
            prompt = "Ile serc ma osmiornica?",
            correct = "3",
            wrong = listOf("1", "2", "5"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "kameleon",
            prompt = "Ktory gad slynie ze zmiany koloru skory?",
            correct = "Kameleon",
            wrong = listOf("Iguana", "Gekon", "Waran"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "zolw",
            prompt = "Ktore zwierze moze dozyc ponad stu lat?",
            correct = "Zolw olbrzymi",
            wrong = listOf("Kot", "Wilk", "Zajac"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "ciaza_slonia",
            prompt = "Ktore ladowe zwierze ma najdluzsza ciaze?",
            correct = "Slon",
            wrong = listOf("Nosorozec", "Kon", "Zyrafa"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "nietoperz",
            prompt = "Jakim zmyslem nietoperz orientuje sie w ciemnosci?",
            correct = "Echolokacja",
            wrong = listOf("Wzrokiem nocnym", "Wechem", "Magnetoreceptorami"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "mlode_konia",
            prompt = "Jak nazywa sie mlode konia?",
            correct = "Zrebie",
            wrong = listOf("Ciele", "Prosie", "Kozle"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "kolibr",
            prompt = "Ktory ptak potrafi latac do tylu?",
            correct = "Koliber",
            wrong = listOf("Wrobel", "Zimorodek", "Bocian"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Trudne ------------------------------- */
        ask(
            id = "dziobak",
            prompt = "Ktory ssak sklada jaja?",
            correct = "Dziobak",
            wrong = listOf("Pancernik", "Leniwiec", "Jez"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "krowa_zoladek",
            prompt = "Ile komor ma zoladek krowy?",
            correct = "4",
            wrong = listOf("1", "2", "3"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "kalmar_oczy",
            prompt = "Ktore zwierze ma najwieksze oczy na swiecie?",
            correct = "Kalmar olbrzymi",
            wrong = listOf("Sowa", "Wieloryb", "Slon morski"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "taniec_pszczol",
            prompt = "Ktory owad wskazuje wspoltowarzyszom droge do pozywienia tancem?",
            correct = "Pszczola",
            wrong = listOf("Mrowka", "Osa", "Termit"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "pingwiny_arktyka",
            prompt = "Ile gatunkow pingwinow zyje dziko na biegunie polnocnym?",
            correct = "Zaden",
            wrong = listOf("Trzy", "Piec", "Osiem"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "gepard_pazury",
            prompt = "Ktory duzy kot nie potrafi ryczec, za to mruczy?",
            correct = "Gepard",
            wrong = listOf("Lew", "Tygrys", "Jaguar"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Eksperckie ---------------------------- */
        ask(
            id = "hemocyjanina",
            prompt = "Ktore zwierze ma niebieska krew?",
            correct = "Osmiornica",
            wrong = listOf("Zaba", "Waz", "Karp"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "aksolotl",
            prompt = "Ktory plaz slynie ze zdolnosci odtwarzania utraconych konczyn?",
            correct = "Aksolotl",
            wrong = listOf("Ropucha", "Traszka alpejska", "Rzekotka"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "niesporczak",
            prompt = "Ktore zwierze przetrwa prozne kosmosu i skrajne temperatury?",
            correct = "Niesporczak",
            wrong = listOf("Karaluch", "Skorpion", "Rozgwiazda"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "narwal",
            prompt = "Ktory ssak morski ma dlugi, spiralny cios wyrastajacy z glowy?",
            correct = "Narwal",
            wrong = listOf("Bialucha", "Morswin", "Manat"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "echidna",
            prompt = "Ktore zwierze, obok dziobaka, nalezy do stekowcow?",
            correct = "Kolczatka",
            wrong = listOf("Oposs", "Wombat", "Koala"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
