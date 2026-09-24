package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Third batch of math - more arithmetic, units and concepts not yet covered. */
object MathQuestionsExtra2 {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "mat3_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Math,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("6x11", "What is 6 x 11?", "66", listOf("60", "72", "56"), QuizDifficulty.Easy),
        ask("81_9", "What is 81 / 9?", "9", listOf("8", "7", "11"), QuizDifficulty.Easy),
        ask("200_45", "What is 200 - 45?", "155", listOf("145", "165", "150"), QuizDifficulty.Easy),
        ask("cwiartka_z_80", "What is 1/4 of 80?", "20", listOf("16", "40", "10"), QuizDifficulty.Easy),
        ask("boki_szescioakta", "How many sides does a hexagon have?", "Six", listOf("Five", "Seven", "Eight"), QuizDifficulty.Easy),
        ask("kat_polny_stopnie", "How many degrees is a straight angle?", "180", listOf("90", "360", "45"), QuizDifficulty.Easy),
        ask("procent_10_50", "What is 10% of 50?", "5", listOf("10", "15", "50"), QuizDifficulty.Easy),
        ask("liczba_rzymska_m", "What number does the Roman numeral M represent?", "1000", listOf("500", "100", "5000"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("procent_60_150", "What is 60% of 150?", "90", listOf("75", "100", "60"), QuizDifficulty.Medium),
        ask("procent_120_zniz", "An item costs \$120 and is discounted by 25%. What does it cost now?", "\$90", listOf("\$100", "\$95", "\$80"), QuizDifficulty.Medium),
        ask("pole_prostokata", "What is the area of a rectangle with sides 6 and 9?", "54", listOf("15", "36", "45"), QuizDifficulty.Medium),
        ask("obwod_prostokata", "What is the perimeter of a rectangle with sides 4 and 7?", "22", listOf("28", "11", "18"), QuizDifficulty.Medium),
        ask("pole_trapezu", "What is the formula for a trapezoid's area?", "(a + b) x h / 2", listOf("a x b", "(a + b) x h", "a x h / 2"), QuizDifficulty.Medium),
        ask("pierwiastek_144", "What is the square root of 144?", "12", listOf("11", "13", "14"), QuizDifficulty.Medium),
        ask("dwa_do_dziesiatej", "What is 2 to the power of 10?", "1024", listOf("512", "2048", "100"), QuizDifficulty.Medium),
        ask("kat_rozwarty", "What angle do we call obtuse?", "Greater than 90, less than 180 degrees", listOf("Smaller than 90 degrees, but not zero", "Exactly 90 degrees", "Exactly 180 degrees"), QuizDifficulty.Medium),
        ask("liczby_wzgledne_pierwsze", "Which pair of numbers is coprime (GCD=1)?", "8 and 9", listOf("6 and 9", "10 and 15", "12 and 18"), QuizDifficulty.Medium),
        ask("proporcja_prosta", "If 3 apples cost \$6, how much do 5 apples cost?", "\$10", listOf("\$8", "\$12", "\$15"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("twierdzenie_talesa", "What does Thales's theorem concern?", "Proportional segments cut by parallel lines", listOf("The sum of angles in a triangle", "A circle's area", "Prime numbers"), QuizDifficulty.Hard),
        ask("objetosc_stozka", "What is the formula for a cone's volume?", "1/3 x pi x r squared x h", listOf("pi x r squared x h", "4/3 x pi x r cubed", "2 x pi x r x h"), QuizDifficulty.Hard),
        ask("objetosc_kuli", "What is the formula for a sphere's volume?", "4/3 x pi x r cubed", listOf("pi x r squared x h", "4 x pi x r squared", "2/3 x pi x r cubed"), QuizDifficulty.Hard),
        ask("silnia_7", "What is 7! (7 factorial)?", "5040", listOf("720", "2520", "10080"), QuizDifficulty.Hard),
        ask("logarytm_naturalny", "What is the base-e logarithm called?", "The natural logarithm", listOf("The base-10 logarithm", "The binary logarithm", "The complex logarithm"), QuizDifficulty.Hard),
        ask("wzor_kwadratowy_delta", "What is the formula for the discriminant in ax2+bx+c=0?", "b squared minus 4ac", listOf("b squared plus 4ac", "a squared minus 4bc", "2b minus 4ac"), QuizDifficulty.Hard),
        ask("prawdopodobienstwo_moneta", "What is the probability of flipping heads twice in a row on two coin flips?", "1/4", listOf("1/2", "1/3", "1/8"), QuizDifficulty.Hard),
        ask("wektor_dlugosc", "What is a vector's length called?", "The vector's norm (magnitude)", listOf("The vector's angle", "A vector component", "A vector projection"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("liczby_zespolone", "What is the imaginary unit i?", "The square root of -1", listOf("An irrational number equal to pi", "A symbol for infinity", "The base of the natural logarithm"), QuizDifficulty.Expert),
        ask("macierz_wyznacznik", "What does a zero determinant of a square matrix mean?", "The matrix is singular (non-invertible)", listOf("The matrix is the identity matrix", "The matrix is symmetric", "The system has exactly one solution"), QuizDifficulty.Expert),
        ask("szereg_harmoniczny", "Does the harmonic series (sum of 1/n) converge?", "No, it diverges", listOf("Yes, it converges to 2", "Yes, it converges to pi", "Yes, it converges to e"), QuizDifficulty.Expert),
        ask("liczby_pierwsze_blizniacze", "What do we call pairs of primes differing by 2, like 11 and 13?", "Twin primes", listOf("Perfect numbers", "Amicable numbers", "Mersenne primes"), QuizDifficulty.Expert),
        ask("teoria_grafow_euler", "Which problem gave rise to graph theory?", "The Seven Bridges of Konigsberg problem", listOf("The four color problem", "The traveling salesman problem", "Zeno's paradox"), QuizDifficulty.Expert),
        ask("cztery_barwy", "What does the four color theorem state?", "Any map can be colored with four colors with no adjacent regions sharing one", listOf("Every graph contains a cycle passing through all of its vertices", "Every whole number has exactly four divisors", "Every triangle has four distinct altitudes"), QuizDifficulty.Expert),
        ask("pierwiastki_niewspolmierne", "What are irrational numbers?", "Numbers that cannot be written as a ratio of two integers", listOf("Numbers that lie below zero on the number line", "Numbers that include an imaginary part", "Numbers divisible only by one and themselves"), QuizDifficulty.Expert)
    )
}
