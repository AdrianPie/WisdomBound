package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Wiedza "z zycia": jedzenie, dom, pieniadze, prawo drogowe, cialo. Druga pula po
 * [EverydayQuestions] i jedyna, ktora nie wymaga niczego ze szkoly - dzieki temu kazdy bieg ma
 * pytania, na ktore da sie odpowiedziec doswiadczeniem, a nie tylko pamiecia do dat.
 *
 * Kategoria [QuizCategory.Technology] i [QuizCategory.Science] biora tu wiekszosc wpisow, bo
 * losowanie ciagnie z kategorii, a nie z plikow - rozbicie po dwoch kategoriach rozklada nowe
 * pytania rownomiernie na cala gre zamiast spietrzac je w jednym temacie.
 */
object EverydayQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty,
        category: QuizCategory = QuizCategory.Technology
    ) = QuizEntry(
        id = "day2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = category,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("wrzenie", "At what temperature does water boil at sea level?", "212 F (100 C)", listOf("180 F", "250 F", "150 F"), QuizDifficulty.Easy, QuizCategory.Science),
        ask("bateria_aa", "What does the "+"\"AA\""+" on a battery describe?", "Its size and shape", listOf("Its brand", "Its voltage only", "Its country of origin"), QuizDifficulty.Easy),
        ask("wifi_pasmo", "Which of these is a common Wi-Fi band?", "2.4 GHz", listOf("2.4 kHz", "24 GHz", "240 Hz"), QuizDifficulty.Easy),
        ask("usb", "What does USB primarily do?", "Connects devices and transfers data", listOf("Stores photos by itself", "Generates power", "Encrypts files"), QuizDifficulty.Easy),
        ask("ram_rola", "What is RAM used for in a computer?", "Holding data the machine is working on right now", listOf("Storing files for the long term, between sessions", "Cooling the processor while it is under heavy load", "Connecting the machine to the local network"), QuizDifficulty.Easy),
        ask("kalorie", "What does a calorie measure in food?", "Energy", listOf("Weight", "Volume", "Sugar content"), QuizDifficulty.Easy, QuizCategory.Science),
        ask("gasnica", "What should you never use to put out a burning pan of oil?", "Water", listOf("A lid", "A fire blanket", "Baking soda"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("procent_skladany", "What is compound interest?", "Interest earned on interest already added", listOf("A one-time fee", "A fixed monthly cost", "Interest paid only at the end"), QuizDifficulty.Medium, QuizCategory.Math),
        ask("vpn", "What does a VPN mainly do?", "Routes your traffic through an encrypted tunnel", listOf("Speeds up the hardware you already own", "Blocks every advertisement on every site", "Backs up your files to off-site storage"), QuizDifficulty.Medium),
        ask("ssd_hdd", "What is the main advantage of an SSD over a hard disk drive?", "No moving parts, so much faster access", listOf("It never fails", "It stores more per dollar", "It uses no power"), QuizDifficulty.Medium),
        ask("opony_cisnienie", "What happens if car tires are badly under-inflated?", "More fuel use and worse handling", listOf("Better grip in all conditions", "Nothing measurable", "The engine runs cooler"), QuizDifficulty.Medium),
        ask("mydlo", "Why does soap clean grease?", "Its molecules bind both water and fat", listOf("It dissolves fat into acid", "It boils the grease away", "It makes water hotter"), QuizDifficulty.Medium, QuizCategory.Science),
        ask("inflacja", "What does inflation mean for your savings?", "The same money buys less over time", listOf("Your balance shrinks", "Interest rates always fall", "Prices stay fixed"), QuizDifficulty.Medium, QuizCategory.Math),
        ask("lodowka_dzialanie", "How does a refrigerator cool its inside?", "It moves heat outward using a refrigerant", listOf("It creates cold air from nothing", "It blocks heat from getting inside", "It freezes the air in the cabinet"), QuizDifficulty.Medium, QuizCategory.Science),
        ask("kofeina", "Roughly how long does half the caffeine leave an adult's body?", "About 5 hours", listOf("About 30 minutes", "About 24 hours", "About 3 days"), QuizDifficulty.Medium, QuizCategory.Biology),

        /* ------------------------------- Hard ------------------------------- */
        ask("prad_zmienny", "Why is alternating current used for power grids?", "Its voltage is easy to transform for long-distance transfer", listOf("It is safer to touch at absolutely any voltage", "It travels faster along the wire than direct current", "It needs no cables at all between towns"), QuizDifficulty.Hard, QuizCategory.Science),
        ask("led_zysk", "Why does an LED bulb use less power than an incandescent one?", "It converts far less energy into heat", listOf("It runs at lower voltage", "It shines less brightly", "It uses no current"), QuizDifficulty.Hard, QuizCategory.Science),
        ask("kredyt_rrso", "What does an annual percentage rate include that a nominal rate does not?", "Fees and other costs of the loan", listOf("Only the interest, nothing else", "The length of the loan in years", "The credit score of the borrower"), QuizDifficulty.Hard, QuizCategory.Math),
        ask("pasteryzacja", "What does pasteurization do?", "Kills most harmful microbes with heat", listOf("Removes all bacteria permanently", "Adds preservatives", "Freezes the product"), QuizDifficulty.Hard, QuizCategory.Biology),
        ask("ram_vs_dysk", "Why does a computer slow down when RAM runs out?", "It starts swapping data to much slower storage", listOf("The processor overheats and throttles", "The screen starts refreshing slower", "The network connection is throttled"), QuizDifficulty.Hard),

        /* ------------------------------ Expert ------------------------------ */
        ask("klucz_publiczny", "In public-key cryptography, what may be shared freely?", "The public key", listOf("The private key", "Both keys", "Neither key"), QuizDifficulty.Expert),
        ask("efekt_cieplarniany", "Why do greenhouse gases warm the planet?", "They absorb outgoing infrared radiation", listOf("They block incoming sunlight", "They thicken the ozone layer", "They raise air pressure"), QuizDifficulty.Expert, QuizCategory.Science),
        ask("antybiotykoopornosc", "How does antibiotic resistance spread so quickly among bacteria?", "Resistance genes pass between cells directly", listOf("Bacteria learn from exposure", "Antibiotics mutate", "It spreads only by inheritance"), QuizDifficulty.Expert, QuizCategory.Biology),
        ask("latency", "What does network latency measure?", "The delay before data starts arriving", listOf("The total bandwidth of the link", "The number of devices connected", "The strength of the radio signal"), QuizDifficulty.Expert),
        ask("indeks_glikemiczny", "What does the glycemic index rank?", "How fast a food raises blood glucose", listOf("How many calories it has", "How much fat it holds", "How long it keeps"), QuizDifficulty.Expert, QuizCategory.Biology),
    )
}
