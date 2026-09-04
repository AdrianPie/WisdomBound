package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Second batch of sports - rules, disciplines, records and Polish highlights. */
object SportQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "spo2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Sport,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("kosz_zawodnicy", "How many players from one team play on court in basketball?", "Five", listOf("Six", "Seven", "Four"), QuizDifficulty.Easy),
        ask("siatka_zawodnicy", "How many players from one team are on court in volleyball?", "Six", listOf("Five", "Seven", "Eight"), QuizDifficulty.Easy),
        ask("mecz_pilki_czas", "How long is a soccer match without added time?", "90 minutes", listOf("60 minutes", "80 minutes", "120 minutes"), QuizDifficulty.Easy),
        ask("bramkarz_rece", "Which soccer player may use their hands inside their own penalty area?", "The goalkeeper", listOf("The captain", "A defender", "A striker"), QuizDifficulty.Easy),
        ask("olimpiada_czestotliwosc", "How often are the Summer Olympics held?", "Every four years", listOf("Every two years", "Every three years", "Every five years"), QuizDifficulty.Easy),
        ask("medal_zloto", "What medal does an Olympic event's winner receive?", "Gold", listOf("Silver", "Bronze", "Platinum"), QuizDifficulty.Easy),
        ask("tenis_zero", "What is a score of zero points in a tennis game called?", "Love", listOf("Deuce", "Ace", "Let"), QuizDifficulty.Easy),
        ask("plywanie_styl", "Which swimming stroke is the fastest?", "Freestyle (front crawl)", listOf("Breaststroke", "Backstroke", "Butterfly"), QuizDifficulty.Easy),
        ask("boks_ring", "What shape is a boxing ring?", "Square", listOf("Round", "Hexagonal", "Oval"), QuizDifficulty.Easy),
        ask("skoki_narciarskie", "Besides distance, what else earns points in ski jumping?", "Style", listOf("Time in the air", "Approach speed", "Equipment weight"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("hokej_zawodnicy", "How many players from one team are on the ice in ice hockey?", "Six", listOf("Five", "Seven", "Four"), QuizDifficulty.Medium),
        ask("rugby_zawodnicy", "How many players make up a rugby union team?", "Fifteen", listOf("Thirteen", "Eleven", "Seventeen"), QuizDifficulty.Medium),
        ask("nba_kwarty", "How many quarters does an NBA game have?", "Four", listOf("Two", "Three", "Five"), QuizDifficulty.Medium),
        ask("tour_de_france", "What jersey does the Tour de France leader wear?", "Yellow", listOf("Green", "Pink", "Polka-dot"), QuizDifficulty.Medium),
        ask("giro_koszulka", "What jersey does the Giro d'Italia leader wear?", "Pink", listOf("Yellow", "White", "Red"), QuizDifficulty.Medium),
        ask("f1_punkty", "How many points does a Formula 1 race winner get?", "25", listOf("20", "18", "30"), QuizDifficulty.Medium),
        ask("golf_dolek", "What does a birdie mean in golf?", "A score of one stroke under par", listOf("A score of one stroke over par", "Sinking it on the first stroke", "Hitting off the course"), QuizDifficulty.Medium),
        ask("maraton_dystans_dokladny", "How many meters exactly is a marathon?", "42195", listOf("42000", "40000", "45000"), QuizDifficulty.Medium),
        ask("dziesiecioboj_kobiety", "What is the women's equivalent of the decathlon called?", "The heptathlon", listOf("The pentathlon", "The octathlon", "The enneathlon"), QuizDifficulty.Medium),
        ask("judo_pas", "Which belt color marks the highest rank in judo?", "Black", listOf("White", "Red", "Brown"), QuizDifficulty.Medium),
        ask("siatkowka_libero", "What can a libero not do in volleyball?", "Attack with a jump above the net", listOf("Receive serves", "Play defense", "Sub in for another player"), QuizDifficulty.Medium),
        ask("pilka_reczna_zawodnicy", "How many players from one team play handball?", "Seven", listOf("Six", "Eight", "Five"), QuizDifficulty.Medium),
        ask("euro_czestotliwosc", "How often is the soccer European Championship held?", "Every four years", listOf("Every two years", "Every three years", "Every five years"), QuizDifficulty.Medium),
        ask("malysz_dyscyplina_klub", "In which countries is the Four Hills Tournament held?", "Germany and Austria", listOf("Poland and Czechia", "Norway and Finland", "Switzerland and Italy"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("mundial_najwiecej", "Which national team has won the most soccer World Cups?", "Brazil", listOf("Germany", "Italy", "Argentina"), QuizDifficulty.Hard),
        ask("mundial_2022", "Who won the 2022 soccer World Cup?", "Argentina", listOf("France", "Brazil", "Croatia"), QuizDifficulty.Hard),
        ask("igrzyska_1980", "Which city hosted the 1980 Summer Olympics?", "Moscow", listOf("Munich", "Montreal", "Los Angeles"), QuizDifficulty.Hard),
        ask("olimpiada_nowozytna_rok", "In which year were the first modern Olympic Games held?", "1896", listOf("1900", "1888", "1912"), QuizDifficulty.Hard),
        ask("wielki_szlem_tenis", "How many tournaments make up tennis's Grand Slam?", "Four", listOf("Three", "Five", "Six"), QuizDifficulty.Hard),
        ask("wimbledon_nawierzchnia", "Which surface does Wimbledon use?", "Grass", listOf("Clay", "Hard court", "Carpet"), QuizDifficulty.Hard),
        ask("roland_garros", "What surface is Roland Garros played on?", "Clay", listOf("Grass", "Hard court", "Concrete"), QuizDifficulty.Hard),
        ask("radwanska_final", "Agnieszka Radwanska played the final of which Grand Slam tournament?", "Wimbledon", listOf("US Open", "Roland Garros", "Australian Open"), QuizDifficulty.Hard),
        ask("kubica_wygrana", "Which Grand Prix did Robert Kubica win?", "The Canadian Grand Prix", listOf("The Monaco Grand Prix", "The Italian Grand Prix", "The Belgian Grand Prix"), QuizDifficulty.Hard),
        ask("nba_najwiecej_tytulow", "Which clubs have the most NBA titles?", "The Boston Celtics and Los Angeles Lakers", listOf("The Chicago Bulls and Miami Heat", "Golden State and the Spurs", "The Knicks and the Nets"), QuizDifficulty.Hard),
        ask("kolarstwo_gorska", "What is the mountains classification jersey called in the Tour de France?", "The polka-dot jersey", listOf("The green jersey", "The white jersey", "The blue jersey"), QuizDifficulty.Hard),
        ask("bolt_200", "What is Usain Bolt's world record in the 200 m?", "19.19 s", listOf("19.59 s", "18.99 s", "19.79 s"), QuizDifficulty.Hard),
        ask("igrzyska_zimowe_polska", "Which city hosted the 2022 Winter Olympics?", "Beijing", listOf("Sochi", "Pyeongchang", "Vancouver"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("olimpiada_maraton_1908", "Why is a marathon 42195 m today?", "That distance was set at the 1908 London Olympics", listOf("That's how far Pheidippides ran", "Pierre de Coubertin fixed it that way", "It's exactly 26 Roman miles"), QuizDifficulty.Expert),
        ask("fosbury", "Who revolutionized the high jump with the backward flop technique?", "Dick Fosbury", listOf("Jesse Owens", "Carl Lewis", "Javier Sotomayor"), QuizDifficulty.Expert),
        ask("sotomayor", "Who holds the men's high jump world record?", "Javier Sotomayor", listOf("Mutaz Barshim", "Dick Fosbury", "Patrik Sjoberg"), QuizDifficulty.Expert),
        ask("duplantis", "Who has repeatedly broken the pole vault world record?", "Armand Duplantis", listOf("Sergey Bubka", "Renaud Lavillenie", "Piotr Lisek"), QuizDifficulty.Expert),
        ask("wlodarczyk", "In which event did Anita Wlodarczyk set world records?", "The hammer throw", listOf("The shot put", "The javelin throw", "The discus throw"), QuizDifficulty.Expert),
        ask("malysz_puchary", "How many times did Adam Malysz win the Ski Jumping World Cup?", "Four times", listOf("Twice", "Three times", "Five times"), QuizDifficulty.Expert),
        ask("stoch_igrzyska", "How many Olympic gold medals has Kamil Stoch won?", "Three", listOf("One", "Two", "Four"), QuizDifficulty.Expert),
        ask("szachy_mistrz", "Who was the first official World Chess Champion?", "Wilhelm Steinitz", listOf("Emanuel Lasker", "Jose Raul Capablanca", "Paul Morphy"), QuizDifficulty.Expert),
        ask("kasparov_deep_blue", "In which year did Deep Blue defeat Garry Kasparov in a match?", "1997", listOf("1996", "1999", "2001"), QuizDifficulty.Expert),
        ask("tour_najwiecej", "Who was the first to win the Tour de France five times?", "Jacques Anquetil", listOf("Eddy Merckx", "Bernard Hinault", "Miguel Indurain"), QuizDifficulty.Expert),
        ask("nba_punkty_kariera", "Who is the NBA's all-time leading scorer?", "LeBron James", listOf("Kareem Abdul-Jabbar", "Michael Jordan", "Karl Malone"), QuizDifficulty.Expert),
        ask("phelps_medale", "How many Olympic gold medals has Michael Phelps won?", "23", listOf("18", "28", "15"), QuizDifficulty.Expert)
    )
}
