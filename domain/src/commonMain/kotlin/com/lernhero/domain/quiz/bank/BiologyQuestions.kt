package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Biologia i cialo czlowieka. Dopisanie pytania to jedna pozycja na liscie - nic wiecej.
 */
object BiologyQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "bio_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Biology,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Latwe ------------------------------- */
        ask(
            id = "serce",
            prompt = "Ktory narzad pompuje krew w ciele czlowieka?",
            correct = "Serce",
            wrong = listOf("Watroba", "Pluca", "Nerki"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "pluca",
            prompt = "Ktory narzad odpowiada za oddychanie?",
            correct = "Pluca",
            wrong = listOf("Zoladek", "Trzustka", "Sledziona"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "fotosynteza",
            prompt = "Jak nazywa sie proces, w ktorym rosliny wytwarzaja pokarm ze swiatla?",
            correct = "Fotosynteza",
            wrong = listOf("Fermentacja", "Transpiracja", "Oddychanie komorkowe"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "skora",
            prompt = "Ktory narzad czlowieka jest najwiekszy?",
            correct = "Skora",
            wrong = listOf("Watroba", "Pluca", "Jelito cienkie"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "komorka",
            prompt = "Jak nazywa sie najmniejsza jednostka budulcowa organizmu?",
            correct = "Komorka",
            wrong = listOf("Tkanka", "Narzad", "Atom"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "krew_tlen",
            prompt = "Co przenosza czerwone krwinki?",
            correct = "Tlen",
            wrong = listOf("Cukier", "Bialka", "Witaminy"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kosci",
            prompt = "Ile kosci ma szkielet doroslego czlowieka?",
            correct = "206",
            wrong = listOf("152", "300", "180"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "zmysly",
            prompt = "Ktory narzad odpowiada za widzenie?",
            correct = "Oko",
            wrong = listOf("Ucho", "Nos", "Jezyk"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Srednie ------------------------------ */
        ask(
            id = "mitochondrium",
            prompt = "Ktora czesc komorki nazywana jest jej elektrownia?",
            correct = "Mitochondrium",
            wrong = listOf("Jadro komorkowe", "Rybosom", "Aparat Golgiego"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "chromosomy",
            prompt = "Ile chromosomow ma zdrowa komorka czlowieka?",
            correct = "46",
            wrong = listOf("23", "48", "44"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "chlorofil",
            prompt = "Jaki barwnik nadaje roslinom zielony kolor?",
            correct = "Chlorofil",
            wrong = listOf("Karoten", "Melanina", "Hemoglobina"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "insulina",
            prompt = "Ktory narzad produkuje insuline?",
            correct = "Trzustka",
            wrong = listOf("Watroba", "Nerki", "Tarczyca"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "darwin",
            prompt = "Kto sformulowal teorie ewolucji przez dobor naturalny?",
            correct = "Karol Darwin",
            wrong = listOf("Grzegorz Mendel", "Ludwik Pasteur", "Carl Linneusz"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "jamy_serca",
            prompt = "Ile jam ma serce czlowieka?",
            correct = "4",
            wrong = listOf("2", "3", "6"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "witamina_d",
            prompt = "Ktora witamine organizm wytwarza pod wplywem slonca?",
            correct = "Witamine D",
            wrong = listOf("Witamine C", "Witamine A", "Witamine B12"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "zoladek",
            prompt = "Jaki kwas pomaga trawic pokarm w zoladku?",
            correct = "Kwas solny",
            wrong = listOf("Kwas siarkowy", "Kwas octowy", "Kwas azotowy"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Trudne ------------------------------- */
        ask(
            id = "mejoza",
            prompt = "Jak nazywa sie podzial komorkowy prowadzacy do powstania komorek plciowych?",
            correct = "Mejoza",
            wrong = listOf("Mitoza", "Cytokineza", "Apoptoza"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "mendel",
            prompt = "Kto jest uwazany za ojca genetyki dzieki badaniom nad groszkiem?",
            correct = "Grzegorz Mendel",
            wrong = listOf("Karol Darwin", "James Watson", "Francis Crick"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "mozdzek",
            prompt = "Ktora czesc mozgu odpowiada za rownowage i koordynacje ruchow?",
            correct = "Mozdzek",
            wrong = listOf("Kora mozgowa", "Rdzen przedluzony", "Podwzgorze"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "dawca",
            prompt = "Ktora grupa krwi jest uniwersalnym dawca?",
            correct = "0 Rh-",
            wrong = listOf("AB Rh+", "A Rh+", "B Rh-"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "neuron",
            prompt = "Jak nazywa sie miejsce, w ktorym jeden neuron przekazuje sygnal drugiemu?",
            correct = "Synapsa",
            wrong = listOf("Akson", "Dendryt", "Mielina"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "nefron",
            prompt = "Jak nazywa sie podstawowa jednostka filtracyjna nerki?",
            correct = "Nefron",
            wrong = listOf("Pecherzyk", "Alweola", "Kanalik zolciowy"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Eksperckie ---------------------------- */
        ask(
            id = "amylaza",
            prompt = "Ktory enzym zawarty w slinie rozklada skrobie?",
            correct = "Amylaza",
            wrong = listOf("Pepsyna", "Lipaza", "Trypsyna"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "mrna",
            prompt = "Ktory rodzaj RNA przenosi informacje genetyczna z jadra do rybosomu?",
            correct = "mRNA",
            wrong = listOf("tRNA", "rRNA", "siRNA"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "ektoderma",
            prompt = "Z ktorego listka zarodkowego rozwija sie uklad nerwowy?",
            correct = "Ektoderma",
            wrong = listOf("Mezoderma", "Endoderma", "Chorda"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "atp",
            prompt = "Jak nazywa sie zwiazek bedacy podstawowym nosnikiem energii w komorce?",
            correct = "ATP",
            wrong = listOf("DNA", "NADH", "Glukoza"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "krebs",
            prompt = "Jak inaczej nazywa sie cykl Krebsa?",
            correct = "Cykl kwasu cytrynowego",
            wrong = listOf("Cykl mocznikowy", "Cykl Calvina", "Glikoliza"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
