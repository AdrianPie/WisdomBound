package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Skrajnosci: najszybsze, najglebsze, najstarsze, najdziwniejsze.
 *
 * Rekord jest wdziecznym materialem na pytanie, bo odpowiedz zapada w pamiec sama - ale tylko
 * wtedy, gdy roznica miedzy opcjami jest wyrazna. Dlatego nie ma tu pytan o to, ktora gora jest
 * o 40 metrow wyzsza; sa takie, w ktorych poprawna odpowiedz zaskakuje sama skala ("blekitny
 * wieloryb ma serce wielkosci malego samochodu") albo lamie oczekiwanie ("najwieksza pustynia
 * swiata jest lodowa").
 *
 * Dystraktory sa z tej samej polki co odpowiedz - patrz [TriviaQuestionsNature].
 */
object TriviaQuestionsExtremes {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "ext_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = category,
        difficulty = difficulty
    )

    private val A = QuizCategory.Animals
    private val B = QuizCategory.Biology
    private val S = QuizCategory.Science
    private val G = QuizCategory.Geography
    private val P = QuizCategory.Sport
    private val N = QuizCategory.Math
    private val C = QuizCategory.Elements

    private val E = QuizDifficulty.Easy
    private val D = QuizDifficulty.Medium
    private val R = QuizDifficulty.Hard
    private val X = QuizDifficulty.Expert

    val all: List<QuizEntry> = listOf(

        /* ------------------------------ Zwierzeta ------------------------------ */
        ask(
            id = "gepard_rozped",
            prompt = "A cheetah reaches its top speed in roughly how long?",
            correct = "About three seconds",
            wrong = listOf("About ten seconds", "About thirty seconds", "Just under a minute"),
            category = A,
            difficulty = D
        ),
        ask(
            id = "jerzyk_lot",
            prompt = "The common swift can stay airborne without landing for how long?",
            correct = "Months",
            wrong = listOf("Hours", "A few days", "A couple of weeks"),
            category = A,
            difficulty = R
        ),
        ask(
            id = "niedzwiadek_wytrzymalosc",
            prompt = "Which tiny animal survives vacuum, radiation and near-absolute-zero cold?",
            correct = "The tardigrade",
            wrong = listOf("The dust mite", "The springtail", "The rotifer"),
            category = A,
            difficulty = D
        ),
        ask(
            id = "krewetka_uderzenie",
            prompt = "The mantis shrimp's strike is so fast that it does what to the water?",
            correct = "Boils it briefly",
            wrong = listOf("Freezes it", "Turns it to foam", "Splits it into gases"),
            category = A,
            difficulty = R
        ),
        ask(
            id = "slimak_zeby",
            prompt = "Which of these animals has the most teeth, by a wide margin?",
            correct = "A garden snail",
            wrong = listOf("A crocodile", "A dolphin", "A shark"),
            category = A,
            difficulty = R
        ),
        ask(
            id = "meduza_niesmiertelna",
            prompt = "Turritopsis dohrnii is famous among biologists for being able to do what?",
            correct = "Revert to an earlier life stage",
            wrong = listOf("Regrow a severed head", "Survive being frozen solid", "Live without water"),
            category = B,
            difficulty = X
        ),
        ask(
            id = "wieloryb_jezyk",
            prompt = "A blue whale's tongue alone can weigh about as much as what?",
            correct = "An elephant",
            wrong = listOf("A horse", "A cow", "A small car"),
            category = A,
            difficulty = D
        ),
        ask(
            id = "rekin_wiek",
            prompt = "The Greenland shark is thought to live for how long?",
            correct = "Centuries",
            wrong = listOf("About 30 years", "About 60 years", "About 90 years"),
            category = A,
            difficulty = R
        ),
        ask(
            id = "mrowka_ciezar",
            prompt = "Leafcutter ants routinely carry loads of roughly what multiple of their body weight?",
            correct = "Fifty times",
            wrong = listOf("Twice", "Five times", "Fifteen times"),
            category = A,
            difficulty = D
        ),

        /* -------------------------------- Cialo -------------------------------- */
        ask(
            id = "jelito_powierzchnia",
            prompt = "Spread flat, the lining of the human small intestine would cover roughly what?",
            correct = "A tennis court",
            wrong = listOf("A dinner table", "A car park space", "A football pitch"),
            category = B,
            difficulty = R
        ),
        ask(
            id = "kosci_niemowle",
            prompt = "A newborn baby has roughly how many bones compared with an adult?",
            correct = "About 90 more",
            wrong = listOf("The same number", "About 30 fewer", "About 200 more"),
            category = B,
            difficulty = D
        ),
        ask(
            id = "oko_rozdzielczosc",
            prompt = "On a clear night the unaided human eye can see a candle flame from about how far?",
            correct = "A few kilometres",
            wrong = listOf("A few hundred metres", "About 50 kilometres", "Over 100 kilometres"),
            category = B,
            difficulty = R
        ),
        ask(
            id = "mozg_energia",
            prompt = "The brain is about 2% of body weight but uses roughly what share of the body's energy?",
            correct = "A fifth",
            wrong = listOf("A fiftieth", "A twentieth", "A half"),
            category = B,
            difficulty = D
        ),

        /* ------------------------------ Ziemia ------------------------------ */
        ask(
            id = "rowu_glebokosc",
            prompt = "If Mount Everest were dropped into the Mariana Trench, what would happen?",
            correct = "Its peak would still be underwater",
            wrong = listOf("It would stick out by a kilometre", "It would fit exactly", "It would stick out by half"),
            category = G,
            difficulty = D
        ),
        ask(
            id = "wodospad_podwodny",
            prompt = "The largest waterfall on the planet is found where?",
            correct = "Underwater",
            wrong = listOf("In Venezuela", "On the Zambezi", "In Norway"),
            category = G,
            difficulty = R
        ),
        ask(
            id = "jezioro_bajkal",
            prompt = "Which lake holds about a fifth of the world's unfrozen fresh water?",
            correct = "Lake Baikal",
            wrong = listOf("Lake Superior", "Lake Victoria", "Lake Tanganyika"),
            category = G,
            difficulty = R
        ),
        ask(
            id = "atacama_deszcz",
            prompt = "Parts of the Atacama Desert have gone how long without measurable rain?",
            correct = "Decades",
            wrong = listOf("A few months", "About two years", "About five years"),
            category = G,
            difficulty = R
        ),
        ask(
            id = "rosja_strefy",
            prompt = "Russia spans roughly how many time zones?",
            correct = "Eleven",
            wrong = listOf("Five", "Eight", "Fourteen"),
            category = G,
            difficulty = D
        ),
        ask(
            id = "wyspa_grenlandia",
            prompt = "Greenland's ice sheet is thick enough to have done what to the land beneath?",
            correct = "Pushed its centre below sea level",
            wrong = listOf("Raised it into a plateau", "Split it into two islands", "Left it perfectly flat"),
            category = G,
            difficulty = X
        ),
        ask(
            id = "amazonka_most",
            prompt = "How many bridges cross the main stretch of the Amazon River?",
            correct = "None",
            wrong = listOf("Three", "Around twenty", "Over a hundred"),
            category = G,
            difficulty = R
        ),
        ask(
            id = "wulkan_mauna",
            prompt = "Measured from its base on the sea floor, which mountain is the tallest on Earth?",
            correct = "Mauna Kea",
            wrong = listOf("Mount Everest", "Denali", "Aconcagua"),
            category = G,
            difficulty = R
        ),

        /* ------------------------------ Kosmos ------------------------------ */
        ask(
            id = "slonce_masa",
            prompt = "The Sun accounts for roughly what share of the Solar System's total mass?",
            correct = "Over 99%",
            wrong = listOf("About 70%", "About 85%", "About 95%"),
            category = S,
            difficulty = D
        ),

        /* ---------------------------- Materia i liczby ---------------------------- */
        ask(
            id = "diament_twardosc",
            prompt = "On the Mohs scale, which mineral sits at the very top?",
            correct = "Diamond",
            wrong = listOf("Corundum", "Topaz", "Quartz"),
            category = C,
            difficulty = E
        ),
        ask(
            id = "wolfram_topnienie",
            prompt = "Which metal has the highest melting point?",
            correct = "Tungsten",
            wrong = listOf("Titanium", "Platinum", "Iron"),
            category = C,
            difficulty = D
        ),
        ask(
            id = "hel_temperatura",
            prompt = "Which element refuses to freeze solid at normal pressure, no matter how cold it gets?",
            correct = "Helium",
            wrong = listOf("Nitrogen", "Hydrogen", "Neon"),
            category = C,
            difficulty = R
        ),
        ask(
            id = "zloto_kowalnosc",
            prompt = "A single gram of gold can be beaten into a sheet of roughly what size?",
            correct = "About a square metre",
            wrong = listOf("A postage stamp", "A sheet of paper", "A tennis court"),
            category = C,
            difficulty = R
        ),
        ask(
            id = "tasowanie_talii",
            prompt = "A shuffled deck of 52 cards has likely landed in an order that has what property?",
            correct = "Never occurred before in history",
            wrong = listOf("Repeats roughly yearly", "Repeats every million shuffles", "Is one of a few billion"),
            category = N,
            difficulty = R
        ),
        ask(
            id = "urodziny_paradoks",
            prompt = "In a room of 23 people, the chance that two share a birthday is closest to what?",
            correct = "50%",
            wrong = listOf("6%", "23%", "90%"),
            category = N,
            difficulty = R
        ),
        ask(
            id = "kartka_skladanie",
            prompt = "Folded in half 42 times, a sheet of paper would be thick enough to reach what?",
            correct = "The Moon",
            wrong = listOf("The top of Everest", "Low Earth orbit", "The Sun"),
            category = N,
            difficulty = X
        ),
        ask(
            id = "ziarna_szachownica",
            prompt = "Doubling one grain of wheat on each of a chessboard's 64 squares gives a total closest to what?",
            correct = "More than all wheat ever grown",
            wrong = listOf("One full granary", "A year's world harvest", "A single field's yield"),
            category = N,
            difficulty = R
        ),

        /* ------------------------------- Sport ------------------------------- */
        ask(
            id = "badminton_lotka",
            prompt = "Which sport features the fastest recorded projectile hit by a player?",
            correct = "Badminton",
            wrong = listOf("Tennis", "Golf", "Ice hockey"),
            category = P,
            difficulty = R
        ),
        ask(
            id = "maraton_dystans",
            prompt = "The marathon's odd distance of 42.195 km was fixed to suit what?",
            correct = "A royal viewing box",
            wrong = listOf("A stadium's track length", "A city's ring road", "An ancient road's length"),
            category = P,
            difficulty = R
        ),
        ask(
            id = "pilka_dolki",
            prompt = "Why is a golf ball covered in dimples?",
            correct = "They make it fly farther",
            wrong = listOf("They improve grip", "They reduce its weight", "They help it roll straight"),
            category = P,
            difficulty = D
        ),
        ask(
            id = "olimpiada_sztuka",
            prompt = "Between 1912 and 1948, Olympic medals were also awarded for what?",
            correct = "Art",
            wrong = listOf("Chess", "Debating", "Ballooning"),
            category = P,
            difficulty = R
        ),
        ask(
            id = "krykiet_mecz",
            prompt = "The longest official cricket match in history lasted how long before being abandoned?",
            correct = "Twelve days",
            wrong = listOf("Three days", "Five days", "Eight days"),
            category = P,
            difficulty = X
        ),
        ask(
            id = "tour_kalorie",
            prompt = "A Tour de France rider can burn roughly how many calories on a hard mountain stage?",
            correct = "Around 8,000",
            wrong = listOf("Around 2,000", "Around 4,000", "Around 20,000"),
            category = P,
            difficulty = D
        )
    )
}
