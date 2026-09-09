package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Porownania i "co tu nie pasuje" poza geografia - odpowiednik [GeographyQuestionsCompare]
 * dla nauki, zwierzat, techniki, matematyki, sportu i mitologii.
 *
 * Ten format ratuje pytanie, ktore jako zwykla wiedza byloby albo trywialne, albo niemozliwe.
 * "Ile wynosi sila zgryzu krokodyla" to ciekawostka do sprawdzenia w internecie; "ktore z tych
 * zwierzat gryzie najmocniej" to juz pytanie, na ktore da sie odpowiedziec myslac.
 *
 * Pulapki sa tu celowe i zawsze tego samego rodzaju: dystraktor nalezy do TEJ SAMEJ rodziny pojec
 * co odpowiedz (pterozaur wsrod dinozaurow, HTML wsrod jezykow programowania), wiec pytanie
 * sprawdza granice wiedzy, a nie jej brak.
 */
object ScienceQuestionsCompare {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "scicmp_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = category,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Zwierzeta -------------------------------- */
        ask("najwieksze_zwierze", "Which of these animals is the largest?", "The blue whale", listOf("The African elephant", "The giraffe", "The great white shark"), QuizCategory.Animals, QuizDifficulty.Easy),
        ask("najciezsze_ladowe", "Which is the heaviest land animal?", "The African elephant", listOf("The hippopotamus", "The white rhinoceros", "The giraffe"), QuizCategory.Animals, QuizDifficulty.Easy),
        ask("najwiekszy_ptak", "Which of these birds is the largest?", "The ostrich", listOf("The emu", "The Andean condor", "The albatross"), QuizCategory.Animals, QuizDifficulty.Medium),
        ask("nie_ssak", "Which of these animals is NOT a mammal?", "The penguin", listOf("The bat", "The dolphin", "The platypus"), QuizCategory.Animals, QuizDifficulty.Easy),
        ask("nie_owad", "Which of these is NOT an insect?", "The spider", listOf("The ant", "The beetle", "The dragonfly"), QuizCategory.Animals, QuizDifficulty.Easy),
        ask("ktory_gad", "Which of these animals is a reptile?", "The crocodile", listOf("The frog", "The salamander", "The newt"), QuizCategory.Animals, QuizDifficulty.Medium),
        ask("najmocniejszy_zgryz", "Which of these animals has the strongest bite force?", "The saltwater crocodile", listOf("The lion", "The hyena", "The great white shark"), QuizCategory.Animals, QuizDifficulty.Hard),
        ask("trzy_serca", "Which of these creatures has three hearts?", "The octopus", listOf("The crab", "The jellyfish", "The starfish"), QuizCategory.Animals, QuizDifficulty.Hard),
        ask("jadowity_nie_trujacy", "Which of these animals is venomous rather than poisonous?", "The cobra", listOf("The poison dart frog", "The pufferfish", "The monarch butterfly"), QuizCategory.Animals, QuizDifficulty.Expert),
        ask("najwiecej_nog", "Which of these creatures has the most legs?", "The millipede", listOf("The spider", "The beetle", "The crab"), QuizCategory.Animals, QuizDifficulty.Medium),

        /* -------------------------------- Kosmos ---------------------------------- */
        ask("pierscienie_planeta", "Which planet has the most prominent ring system?", "Saturn", listOf("Jupiter", "Uranus", "Neptune"), QuizCategory.Science, QuizDifficulty.Easy),
        ask("najgoretsza_planeta", "Which is the hottest planet in the Solar System?", "Venus", listOf("Mercury", "Mars", "Jupiter"), QuizCategory.Science, QuizDifficulty.Medium),
        ask("nie_planeta", "Which of these is NOT classified as a planet?", "Pluto", listOf("Neptune", "Uranus", "Mercury"), QuizCategory.Science, QuizDifficulty.Easy),
        ask("co_najszybsze", "Which of these travels fastest?", "Light", listOf("Sound", "A passenger jet", "The Earth along its orbit"), QuizCategory.Science, QuizDifficulty.Easy),

        /* --------------------------- Materia i pierwiastki ------------------------ */
        ask("najtwardszy_material", "Which of these is the hardest natural material?", "Diamond", listOf("Quartz", "Steel", "Granite"), QuizCategory.Science, QuizDifficulty.Easy),
        ask("gaz_atmosfera", "Which gas makes up most of the Earth's atmosphere?", "Nitrogen", listOf("Oxygen", "Carbon dioxide", "Argon"), QuizCategory.Science, QuizDifficulty.Medium),
        ask("nie_stan_skupienia", "Which of these is NOT one of the classical states of matter?", "Light", listOf("Solid", "Liquid", "Gas"), QuizCategory.Science, QuizDifficulty.Easy),
        ask("co_najmniejsze", "Which of these is the smallest?", "An atom", listOf("A virus", "A bacterium", "A human cell"), QuizCategory.Science, QuizDifficulty.Medium),
        ask("najgestszy_metal_cmp", "Which of these metals is the densest?", "Gold", listOf("Iron", "Aluminium", "Lead"), QuizCategory.Elements, QuizDifficulty.Medium),
        ask("nie_gaz_szlachetny", "Which of these is NOT a noble gas?", "Nitrogen", listOf("Helium", "Neon", "Argon"), QuizCategory.Elements, QuizDifficulty.Easy),
        ask("najczestszy_skorupa", "Which element is the most abundant in the Earth's crust?", "Oxygen", listOf("Silicon", "Aluminium", "Iron"), QuizCategory.Elements, QuizDifficulty.Hard),
        ask("najwyzsza_wrzenia", "Which of these has the highest boiling point?", "Iron", listOf("Water", "Ethanol", "Mercury"), QuizCategory.Elements, QuizDifficulty.Hard),

        /* --------------------------- Cialo i biologia ----------------------------- */
        ask("tlen_we_krwi", "Which substance carries oxygen in the blood?", "Haemoglobin", listOf("Insulin", "Collagen", "Keratin"), QuizCategory.Biology, QuizDifficulty.Medium),

        /* -------------------------------- Technika -------------------------------- */
        ask("co_pierwsze_wynalazek", "Which of these came first?", "The printing press", listOf("The steam engine", "The telephone", "Radio"), QuizCategory.Technology, QuizDifficulty.Easy),
        ask("firma_najstarsza", "Which of these companies was founded first?", "IBM", listOf("Microsoft", "Apple", "Google"), QuizCategory.Technology, QuizDifficulty.Medium),
        ask("nie_jezyk_programowania", "Which of these is NOT a programming language?", "Docker", listOf("Python", "Java", "Ruby"), QuizCategory.Technology, QuizDifficulty.Medium),
        ask("najwiecej_danych", "Which of these holds the most data?", "A Blu-ray disc", listOf("A CD", "A DVD", "A floppy disk"), QuizCategory.Technology, QuizDifficulty.Medium),
        ask("co_pierwsze_internet", "Which of these appeared first?", "The World Wide Web", listOf("Google", "Facebook", "YouTube"), QuizCategory.Technology, QuizDifficulty.Easy),
        ask("jednostka_najwieksza", "Which of these data units is the largest?", "A terabyte", listOf("A kilobyte", "A megabyte", "A gigabyte"), QuizCategory.Technology, QuizDifficulty.Easy),
        ask("wynalazek_najnowszy", "Which of these was invented most recently?", "The smartphone", listOf("The transistor", "The laser", "The microprocessor"), QuizCategory.Technology, QuizDifficulty.Medium),

        /* ------------------------------ Matematyka -------------------------------- */
        ask("ktora_pierwsza", "Which of these three-digit numbers is prime?", "101", listOf("111", "121", "141"), QuizCategory.Math, QuizDifficulty.Medium),
        ask("najwiekszy_ulamek", "Which of these fractions is the largest?", "3/4", listOf("2/3", "5/8", "7/12"), QuizCategory.Math, QuizDifficulty.Hard),
        ask("liczba_niewymierna", "Which of these is an irrational number?", "The square root of 2", listOf("0.5", "3/7", "-4"), QuizCategory.Math, QuizDifficulty.Medium),
        ask("najwiecej_bokow", "Which of these shapes has the most sides?", "An octagon", listOf("A triangle", "A pentagon", "A hexagon"), QuizCategory.Math, QuizDifficulty.Easy),
        ask("najwieksza_potega", "Which of these is the largest?", "2 to the power of 10", listOf("10 cubed", "5 to the power of 4", "3 to the power of 6"), QuizCategory.Math, QuizDifficulty.Hard),
        ask("kat_rozwarty", "Which of these angles is obtuse?", "120 degrees", listOf("45 degrees", "90 degrees", "30 degrees"), QuizCategory.Math, QuizDifficulty.Easy),

        /* --------------------------------- Sport ---------------------------------- */
        ask("najwieksza_pilka", "Which of these sports uses the largest ball?", "Basketball", listOf("Football", "Volleyball", "Handball"), QuizCategory.Sport, QuizDifficulty.Medium),
        ask("nie_zimowy_sport", "Which of these is NOT a Winter Olympic sport?", "Field hockey", listOf("Curling", "Biathlon", "Luge"), QuizCategory.Sport, QuizDifficulty.Easy),
        ask("najdluzszy_bieg", "Which of these races is the longest?", "The marathon", listOf("The 10,000 m", "The 5,000 m", "The 1,500 m"), QuizCategory.Sport, QuizDifficulty.Easy),
        ask("ryder_cup", "In which sport is the Ryder Cup contested?", "Golf", listOf("Tennis", "Sailing", "Cycling"), QuizCategory.Sport, QuizDifficulty.Medium),
        ask("najwiecej_zawodnikow", "Which of these sports fields the most players per team?", "Rugby union", listOf("Football", "Ice hockey", "Basketball"), QuizCategory.Sport, QuizDifficulty.Medium),
        ask("najwiecej_zlota", "Which country has won the most Summer Olympic gold medals overall?", "The United States", listOf("The Soviet Union", "China", "Great Britain"), QuizCategory.Sport, QuizDifficulty.Hard),

        /* ------------------------------- Mitologia -------------------------------- */
        ask("odpowiednik_zeusa", "Who is the Roman equivalent of the Greek god Zeus?", "Jupiter", listOf("Mars", "Neptune", "Saturn"), QuizCategory.Mythology, QuizDifficulty.Easy),
        ask("nie_grecki_bog", "Which of these is NOT a Greek god?", "Odin", listOf("Zeus", "Hades", "Poseidon"), QuizCategory.Mythology, QuizDifficulty.Easy),
        ask("bog_morza_grecki", "Which Greek god rules the sea?", "Poseidon", listOf("Hades", "Ares", "Hermes"), QuizCategory.Mythology, QuizDifficulty.Easy),
        ask("nordycki_bog", "Which of these gods comes from Norse mythology?", "Thor", listOf("Apollo", "Osiris", "Vishnu"), QuizCategory.Mythology, QuizDifficulty.Easy),
        ask("lew_glowa_czlowieka", "Which creature has the body of a lion and the head of a human?", "The sphinx", listOf("The centaur", "The minotaur", "The griffin"), QuizCategory.Mythology, QuizDifficulty.Medium)
    )
}
