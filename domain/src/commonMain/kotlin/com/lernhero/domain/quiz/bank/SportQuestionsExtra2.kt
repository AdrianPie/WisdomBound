package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Third batch of sports - more disciplines, rules and records not yet covered. */
object SportQuestionsExtra2 {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "spo3_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Sport,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("badminton_czym_gra", "What do you play badminton with?", "A shuttlecock", listOf("A ball", "A puck", "A quill pen"), QuizDifficulty.Easy),
        ask("kregle_ile_kregli", "How many pins are set up in classic bowling?", "Ten", listOf("Eight", "Twelve", "Six"), QuizDifficulty.Easy),
        ask("szachy_figury_ile", "How many pieces does one player have at the start of a chess game?", "Sixteen", listOf("Eight", "Twelve", "Twenty"), QuizDifficulty.Easy),
        ask("szachy_krol_ruch", "How does the king move in chess?", "One square in any direction", listOf("Only diagonally", "Only in straight lines, any distance", "In an L-shape like a knight"), QuizDifficulty.Easy),
        ask("kolarstwo_kask", "What should a cyclist wear for safety?", "A helmet", listOf("Boxing gloves", "Noise-canceling earmuffs", "Swimming goggles"), QuizDifficulty.Easy),
        ask("narciarstwo_sprzet", "What do you use to ski?", "Skis and poles", listOf("A skateboard", "A tennis racket", "A ball"), QuizDifficulty.Easy),
        ask("plywanie_basen_dlugosc", "How many meters long is a standard Olympic pool?", "50", listOf("25", "100", "33"), QuizDifficulty.Easy),
        ask("lucznictwo_cel", "What do archers aim at?", "A target", listOf("A moving disc", "A goal", "A hoop"), QuizDifficulty.Easy),
        ask("wspinaczka_sprzet", "What is used for belaying in climbing?", "A rope and harness", listOf("A ski pole", "A racket", "A boxing helmet"), QuizDifficulty.Easy),
        ask("zapasy_cel", "What is the goal in wrestling?", "Pinning the opponent's shoulders to the mat", listOf("Knocking them off the mat", "Scoring more shooting points", "Throwing a javelin farther"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("triathlon_dyscypliny", "What three disciplines make up a triathlon?", "Swimming, cycling and running", listOf("Running, jumping and throwing", "Swimming, climbing and running", "Cycling, wrestling and shooting"), QuizDifficulty.Medium),
        ask("biathlon_dyscypliny", "What two disciplines make up biathlon?", "Cross-country skiing and shooting", listOf("Running and swimming", "Skiing and jumping", "Skating and shooting"), QuizDifficulty.Medium),
        ask("curling_cel", "What is the goal of curling?", "Placing a stone as close as possible to the target's center on the ice", listOf("Knocking down all the pins", "Getting a puck into a goal", "Running the farthest"), QuizDifficulty.Medium),
        ask("squash_gra_gdzie", "Where is a squash match played?", "In an enclosed room with four walls", listOf("On an open court", "On ice", "In a pool"), QuizDifficulty.Medium),
        ask("bilard_ile_bil", "How many balls (cue ball included) are used in classic American pool (8-ball)?", "16", listOf("10", "12", "20"), QuizDifficulty.Medium),
        ask("olimpiada_letnia_dyscyplin", "Which of these disciplines is NOT held at the Summer Olympics?", "Cross-country skiing", listOf("Swimming", "Track and field", "Judo"), QuizDifficulty.Medium),
        ask("hokej_krazek", "What is a hockey puck made of?", "Vulcanized rubber", listOf("Metal", "Wood", "Bone"), QuizDifficulty.Medium),
        ask("koszykowka_kosz_wysokosc", "At what height is the hoop mounted in basketball?", "10 ft (3.05 m)", listOf("8 ft", "11.5 ft", "9 ft"), QuizDifficulty.Medium),
        ask("siatkowka_siatka_wys_mezczyzni", "Roughly how high is the net in men's volleyball?", "About 2.43 m", listOf("About 2.00 m", "About 2.80 m", "About 3.00 m"), QuizDifficulty.Medium),
        ask("pilka_nozna_kartka_zolta", "What does a yellow card mean in soccer?", "A warning for a foul", listOf("Ejection from the match", "A penalty kick", "A substitution"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("rugby_punktacja", "How many points is a try worth in rugby union?", "Five", listOf("Three", "Seven", "Ten"), QuizDifficulty.Hard),
        ask("baseball_out", "After how many strikes is a batter out in baseball?", "Three", listOf("Two", "Four", "Five"), QuizDifficulty.Hard),
        ask("golf_dolki_runda", "How many holes does a standard round of golf have?", "18", listOf("9", "12", "24"), QuizDifficulty.Hard),
        ask("krykiet_kraj", "In which country is cricket one of the most popular sports?", "India", listOf("Canada", "Germany", "Brazil"), QuizDifficulty.Hard),
        ask("szermierka_bronie", "Which of these weapons is NOT used in Olympic fencing?", "Katana", listOf("Foil", "Epee", "Saber"), QuizDifficulty.Hard),
        ask("igrzyska_paraolimpijskie", "Who are the Paralympic Games held for?", "Athletes with disabilities", listOf("Juniors", "Seniors over 60", "Unlicensed amateurs"), QuizDifficulty.Hard),
        ask("formula1_kwalifikacje", "What determines starting position in Formula 1?", "Qualifying results", listOf("A pre-race draw", "The previous season's results", "Alphabetical order"), QuizDifficulty.Hard),
        ask("olimpiada_letnia_1936", "In which city were the controversial 1936 Olympics held?", "Berlin", listOf("Rome", "London", "Paris"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("owens_1936", "How many gold medals did Jesse Owens win at the 1936 Berlin Olympics?", "Four", listOf("Two", "Three", "Five"), QuizDifficulty.Expert),
        ask("nadia_comaneci", "Who was the first gymnast in Olympic history to receive a perfect 10.0?", "Nadia Comaneci", listOf("Simone Biles", "Larisa Latynina", "Olga Korbut"), QuizDifficulty.Expert),
        ask("latynina_medale", "Which gymnast was, for decades, the most decorated Olympian in history?", "Larisa Latynina", listOf("Nadia Comaneci", "Simone Biles", "Olga Korbut"), QuizDifficulty.Expert),
        ask("bolt_100m_rekord", "What is Usain Bolt's current world record in the 100 meters?", "9.58 s", listOf("9.69 s", "9.78 s", "9.48 s"), QuizDifficulty.Expert),
        ask("marathon_kobiety_rekord_era", "Who broke the modern-era women's marathon world record with a time under 2:12?", "Tigist Assefa", listOf("Paula Radcliffe as the current record", "Brigid Kosgei as the current record", "Eliud Kipchoge (a man)"), QuizDifficulty.Expert),
        ask("kipchoge_maraton", "Who was the first to run a marathon distance in under two hours (an exhibition run)?", "Eliud Kipchoge", listOf("Mo Farah", "Haile Gebrselassie", "Kenenisa Bekele"), QuizDifficulty.Expert),
        ask("pierwsza_kobieta_igrzyska", "Starting with which Games could women officially compete in the modern Olympics?", "1900 (Paris)", listOf("1896", "1924", "1948"), QuizDifficulty.Expert)
    )
}
