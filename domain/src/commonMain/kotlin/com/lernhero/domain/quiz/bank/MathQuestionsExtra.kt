package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Second batch of math - arithmetic, geometry, percentages and a bit of number theory. */
object MathQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "mat2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Math,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("12x12", "What is 12 x 12?", "144", listOf("124", "132", "154"), QuizDifficulty.Easy),
        ask("15x6", "What is 15 x 6?", "90", listOf("80", "96", "85"), QuizDifficulty.Easy),
        ask("100_7", "What is 100 - 37?", "63", listOf("73", "67", "57"), QuizDifficulty.Easy),
        ask("144_12", "What is 144 / 12?", "12", listOf("14", "11", "16"), QuizDifficulty.Easy),
        ask("polowa_250", "What is half of 250?", "125", listOf("120", "135", "150"), QuizDifficulty.Easy),
        ask("boki_kwadratu", "How many sides does a square have?", "Four", listOf("Three", "Five", "Six"), QuizDifficulty.Easy),
        ask("boki_pieciokata", "How many sides does a pentagon have?", "Five", listOf("Four", "Six", "Seven"), QuizDifficulty.Easy),
        ask("obwod_kwadratu", "What is the perimeter of a square with side 5?", "20", listOf("25", "10", "15"), QuizDifficulty.Easy),
        ask("pole_kwadratu", "What is the area of a square with side 6?", "36", listOf("24", "12", "30"), QuizDifficulty.Easy),
        ask("liczba_parzysta", "Which of these numbers is even?", "38", listOf("27", "45", "19"), QuizDifficulty.Easy),
        ask("polowa_setki", "What is 50% of 80?", "40", listOf("30", "45", "60"), QuizDifficulty.Easy),
        ask("godzina_minuty", "How many minutes are in an hour and a half?", "90", listOf("60", "100", "120"), QuizDifficulty.Easy),
        ask("liczba_rzymska_x", "What number does the Roman numeral X represent?", "10", listOf("5", "50", "100"), QuizDifficulty.Easy),
        ask("liczba_rzymska_c", "What number does the Roman numeral C represent?", "100", listOf("50", "500", "1000"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("procent_15_200", "What is 15% of 200?", "30", listOf("25", "35", "45"), QuizDifficulty.Medium),
        ask("procent_25_360", "What is 25% of 360?", "90", listOf("80", "72", "120"), QuizDifficulty.Medium),
        ask("pole_trojkata", "What is the formula for a triangle's area?", "(base x height) / 2", listOf("base x height", "2 x base + height", "base squared"), QuizDifficulty.Medium),
        ask("pole_kola", "What is the formula for a circle's area?", "pi x r squared", listOf("2 x pi x r", "pi x d", "pi x r"), QuizDifficulty.Medium),
        ask("obwod_kola", "What is the formula for a circle's circumference?", "2 x pi x r", listOf("pi x r squared", "pi x r", "4 x pi x r"), QuizDifficulty.Medium),
        ask("suma_katow_czworokat", "What is the sum of angles in a quadrilateral?", "360 degrees", listOf("180 degrees", "270 degrees", "540 degrees"), QuizDifficulty.Medium),
        ask("pierwiastek_81", "What is the square root of 81?", "9", listOf("8", "7", "11"), QuizDifficulty.Medium),
        ask("pierwiastek_169", "What is the square root of 169?", "13", listOf("12", "14", "17"), QuizDifficulty.Medium),
        ask("dwa_do_szostej", "What is 2 to the power of 6?", "64", listOf("32", "128", "36"), QuizDifficulty.Medium),
        ask("trzy_do_czwartej", "What is 3 to the power of 4?", "81", listOf("64", "27", "121"), QuizDifficulty.Medium),
        ask("nwd", "What is the greatest common divisor of 24 and 36?", "12", listOf("6", "18", "8"), QuizDifficulty.Medium),
        ask("nww", "What is the least common multiple of 4 and 6?", "12", listOf("24", "10", "18"), QuizDifficulty.Medium),
        ask("ulamek_dziesietny", "How do you write 3/4 as a decimal?", "0.75", listOf("0.34", "0.7", "1.33"), QuizDifficulty.Medium),
        ask("srednia", "What is the arithmetic mean of 4, 8 and 12?", "8", listOf("6", "9", "10"), QuizDifficulty.Medium),
        ask("kat_ostry", "What angle do we call acute?", "Less than 90 degrees", listOf("Equal to 90 degrees", "More than 90 degrees", "Equal to 180 degrees"), QuizDifficulty.Medium),
        ask("figury_przekatne", "How many diagonals does a quadrilateral have?", "Two", listOf("Four", "One", "Three"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("pitagoras", "What does the Pythagorean theorem concern?", "The relationship between the sides of a right triangle", listOf("The sum of angles in a circle", "A trapezoid's area", "Similarity of shapes"), QuizDifficulty.Hard),
        ask("przeciwprostokatna", "What is the hypotenuse of a triangle with legs 3 and 4?", "5", listOf("6", "7", "5.5"), QuizDifficulty.Hard),
        ask("objetosc_szescianu", "What is the volume of a cube with edge 4?", "64", listOf("16", "48", "32"), QuizDifficulty.Hard),
        ask("objetosc_walca", "What is the formula for a cylinder's volume?", "pi x r squared x h", listOf("2 x pi x r x h", "pi x r x h", "pi x d x h"), QuizDifficulty.Hard),
        ask("silnia_6", "What is 6! (6 factorial)?", "720", listOf("120", "360", "1440"), QuizDifficulty.Hard),
        ask("procent_skladany", "What sets compound interest apart from simple interest?", "Interest accrues on previous interest too", listOf("It's calculated only once in a lifetime", "It always gives less", "It only applies to loans"), QuizDifficulty.Hard),
        ask("liczba_wymierna", "Which of these numbers is NOT rational?", "The square root of 2", listOf("0.5", "7", "22/7"), QuizDifficulty.Hard),
        ask("delta", "How many roots does a quadratic function have when the discriminant is negative?", "Zero", listOf("One", "Two", "Infinitely many"), QuizDifficulty.Hard),
        ask("logarytm_2_8", "What is log base 2 of 8?", "3", listOf("2", "4", "8"), QuizDifficulty.Hard),
        ask("ciag_arytmetyczny", "What stays constant in an arithmetic sequence?", "The difference between consecutive terms", listOf("The ratio of consecutive terms", "The sum of all terms", "The first term"), QuizDifficulty.Hard),
        ask("ciag_geometryczny", "What stays constant in a geometric sequence?", "The ratio of consecutive terms", listOf("The difference between consecutive terms", "The sum of terms", "The number of terms"), QuizDifficulty.Hard),
        ask("prawdopodobienstwo_kostka", "What's the probability of rolling a six on a fair die?", "1/6", listOf("1/3", "1/2", "1/12"), QuizDifficulty.Hard),
        ask("kombinacje", "In how many ways can you arrange 4 different books in a row?", "24", listOf("12", "16", "36"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("euler_wzor", "What does Euler's identity relate?", "The numbers e, i, pi, 1 and 0", listOf("Only pi and e", "Areas and perimeters", "Derivatives and integrals"), QuizDifficulty.Expert),
        ask("pochodna_x2", "What is the derivative of x squared?", "2x", listOf("x", "x cubed / 3", "2"), QuizDifficulty.Expert),
        ask("calka_x", "What is the indefinite integral of x?", "x squared / 2 + C", listOf("2x + C", "1 + C", "ln x + C"), QuizDifficulty.Expert),
        ask("hipoteza_riemanna", "What does the Riemann hypothesis concern?", "The distribution of the zeta function's zeros", listOf("Differential equations", "The four color theorem", "Graph theory"), QuizDifficulty.Expert),
        ask("fermat", "How many years passed before Fermat's Last Theorem was proven?", "Over 350", listOf("About 50", "About 100", "Over 1000"), QuizDifficulty.Expert),
        ask("godel", "What do Godel's incompleteness theorems state?", "Any sufficiently rich system has undecidable statements", listOf("Every theorem can be proven", "Mathematics is contradictory", "There are finitely many primes"), QuizDifficulty.Expert),
        ask("liczba_pierwsza_najwieksza", "What form do the largest known prime numbers take?", "Mersenne primes", listOf("Fermat numbers", "Fibonacci numbers", "Perfect numbers"), QuizDifficulty.Expert),
        ask("topologia_kubek", "Which shape does a topologist consider equivalent to a coffee mug?", "A torus (donut)", listOf("A sphere", "A cube", "A Mobius strip"), QuizDifficulty.Expert),
        ask("zloty_podzial", "What is the approximate value of the golden ratio?", "1.618", listOf("1.414", "2.718", "3.142"), QuizDifficulty.Expert),
        ask("liczba_grahama", "What is Graham's number associated with?", "An extremely large number from combinatorics", listOf("The number of atoms in the Universe", "The limit of the Fibonacci sequence", "The base of the natural logarithm"), QuizDifficulty.Expert),
        ask("p_np", "What does the P vs NP problem concern?", "The computational complexity of problems", listOf("Non-Euclidean geometry", "Measure theory", "The Navier-Stokes equations"), QuizDifficulty.Expert),
        ask("banach_tarski", "What does the Banach-Tarski paradox state?", "A ball can be split apart and reassembled into two identical balls", listOf("Pi is rational", "Every set has a measure", "All infinities are equal"), QuizDifficulty.Expert)
    )
}
