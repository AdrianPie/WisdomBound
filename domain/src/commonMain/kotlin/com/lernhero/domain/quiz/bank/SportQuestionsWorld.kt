package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Sport bez watkow lokalnych - dowazenie kategorii po przeniesieniu pytan o Malysza, Kubice
 * i mundial 1974 do tematu polskiego (patrz [PolandQuestionsMoved]).
 *
 * Sport byl najchudsza kategoria banku i najbardziej jednostronna: prawie wszystko krecilo sie
 * wokol pilki noznej i igrzysk. Ta partia celowo siega dalej - do zasad, ktorych kibic uzywa co
 * tydzien (spalony, tie-break, punkt w rugby), do sportow spoza europejskiego kanonu (baseball,
 * krykiet, sumo) i do rzeczy, ktore da sie WYLICZYC zamiast pamietac (ile gemow trzeba do seta).
 */
object SportQuestionsWorld {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "spw_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Sport,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("spalony_zasada", "In football, a player is offside if, when the ball is played, they are...", "Nearer the goal line than the ball and the second-last defender", listOf("Standing anywhere inside the opposing penalty area", "Standing behind the halfway line of the pitch", "Standing directly in front of the goalkeeper"), QuizDifficulty.Easy),
        ask("hat_trick", "What is a 'hat-trick' in football?", "Three goals by one player in a match", listOf("Three saves in a row", "Three matches without conceding", "Three yellow cards"), QuizDifficulty.Easy),
        ask("nba_kosz_punkty", "How many points is a free throw worth in basketball?", "One", listOf("Two", "Three", "Half a point"), QuizDifficulty.Easy),
        ask("tenis_set_gemy", "How many games do you normally need to win a set in tennis?", "Six, with a two-game lead", listOf("Five, with any lead", "Seven, always", "Four, with a two-game lead"), QuizDifficulty.Easy),
        ask("olimpijskie_haslo", "What does the Olympic motto 'Citius, Altius, Fortius' mean?", "Faster, Higher, Stronger", listOf("Peace, Unity, Sport", "Body, Mind, Spirit", "Honour, Courage, Glory"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("tie_break", "In tennis, when is a tie-break usually played?", "At 6-6 in a set", listOf("At 5-5 in a set", "After every set", "Only in the final set"), QuizDifficulty.Medium),
        ask("nba_faul", "How many personal fouls foul a player out of an NBA game?", "Six", listOf("Four", "Five", "Seven"), QuizDifficulty.Medium),
        ask("rugby_punkty_drop", "How many points is a drop goal worth in rugby union?", "Three", listOf("Two", "Five", "One"), QuizDifficulty.Medium),
        ask("golf_par", "What does 'par' mean on a golf hole?", "The number of strokes a good player is expected to take", listOf("The lowest score ever recorded on that hole", "The number of players allowed in one group", "The length of the hole in hundreds of metres"), QuizDifficulty.Medium),
        ask("kolarstwo_peleton", "What is the main group of riders in a road race called?", "The peloton", listOf("The breakaway", "The domestique", "The prologue"), QuizDifficulty.Medium),
        ask("plywanie_delfin", "Which swimming stroke requires both arms to move together over the water?", "Butterfly", listOf("Front crawl", "Backstroke", "Breaststroke"), QuizDifficulty.Medium),
        ask("boks_rundy", "How many rounds does a modern world title boxing fight last at most?", "Twelve", listOf("Ten", "Fifteen", "Eight"), QuizDifficulty.Medium),
        ask("nfl_pole", "How long is an American football field between the goal lines?", "100 yards", listOf("100 metres", "120 yards", "90 yards"), QuizDifficulty.Medium),
        ask("koszykowka_sekundy", "How long does an NBA team have to attempt a shot?", "24 seconds", listOf("30 seconds", "20 seconds", "35 seconds"), QuizDifficulty.Medium),
        ask("siatkowka_odbicia", "How many touches may a volleyball team make before returning the ball?", "Three", listOf("Two", "Four", "Unlimited"), QuizDifficulty.Medium),

        /* ------------------------------- Hard ------------------------------- */
        ask("krykiet_test", "How many days can a cricket Test match last?", "Five", listOf("Three", "One", "Seven"), QuizDifficulty.Hard),
        ask("f1_drs", "What does DRS do in Formula 1?", "Opens the rear wing to cut drag and help overtaking", listOf("Forces cool air over the brakes on long straights", "Limits how much fuel reaches the engine", "Adds a burst of power from the battery"), QuizDifficulty.Hard),
        ask("sumo_zwyciestwo", "How does a sumo wrestler win a bout?", "By forcing the opponent out of the ring or down onto the ground", listOf("By pinning both of the opponent's shoulders to the floor", "On points awarded by judges after three rounds", "By lifting the opponent clear above the head"), QuizDifficulty.Hard),
        ask("maraton_kobiet_pierwszy", "At which Olympics did women first run the marathon?", "Los Angeles 1984", listOf("Munich 1972", "Montreal 1976", "Seoul 1988"), QuizDifficulty.Hard),
        ask("triathlon_ironman", "How long is the run in a full Ironman triathlon?", "A full marathon, 42.2 km", listOf("A half marathon", "10 km", "21 km"), QuizDifficulty.Hard),
        ask("nba_triple_double", "What is a 'triple-double' in basketball?", "Double figures in three statistical categories in one game", listOf("Three double-doubles in three straight games", "Doubling your own score three times over", "Three periods of overtime in one match"), QuizDifficulty.Hard),
        ask("szachy_roszada", "In chess, what does castling move?", "The king and one rook, in a single move", listOf("The king and the queen, side by side", "Two pawns moved on the same turn", "A rook and a knight, swapping places"), QuizDifficulty.Hard),
        ask("tour_koszulka_biala", "In the Tour de France, who wears the white jersey?", "The best young rider", listOf("The best sprinter", "The best climber", "The last rider overall"), QuizDifficulty.Hard),

        /* ------------------------------ Expert ------------------------------ */
        ask("olimpiada_odwolane", "How many Olympic Games were cancelled because of the world wars?", "Five", listOf("Two", "Three", "Seven"), QuizDifficulty.Expert),
        ask("bannister_mila", "Who was the first man to run a mile in under four minutes?", "Roger Bannister", listOf("Jim Ryun", "Herb Elliott", "John Landy"), QuizDifficulty.Expert),
        ask("kraj_najwiecej_medali_zimowe", "Which country has won the most Winter Olympic medals overall?", "Norway", listOf("The United States", "Germany", "Russia"), QuizDifficulty.Expert),
        ask("krykiet_bradman", "Which cricketer finished with a Test batting average of 99.94?", "Don Bradman", listOf("Sachin Tendulkar", "Jack Hobbs", "Vivian Richards"), QuizDifficulty.Expert),
        ask("maraton_najstarszy", "Which is the oldest annual marathon in the world, first run in 1897?", "The Boston Marathon", listOf("The London Marathon", "The New York City Marathon", "The Berlin Marathon"), QuizDifficulty.Expert),
    )
}
