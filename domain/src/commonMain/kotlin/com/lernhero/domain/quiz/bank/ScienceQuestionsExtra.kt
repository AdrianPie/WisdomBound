package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Second batch of science - physics, chemistry and space, from school basics to trivia. */
object ScienceQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "sci2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Science,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("woda_wrzenie", "At what temperature does water boil at normal pressure?", "100 C", listOf("80 C", "120 C", "60 C"), QuizDifficulty.Easy),
        ask("woda_zamarza", "At what temperature does water freeze?", "0 C", listOf("-10 C", "4 C", "10 C"), QuizDifficulty.Easy),
        ask("stany_skupienia", "How many basic states of matter are taught in school?", "Three", listOf("Two", "Four", "Five"), QuizDifficulty.Easy),
        ask("magnes_bieguny", "How many poles does a magnet have?", "Two", listOf("One", "Three", "Four"), QuizDifficulty.Easy),
        ask("tecza_barwy", "How many colors are traditionally listed in a rainbow?", "Seven", listOf("Five", "Six", "Eight"), QuizDifficulty.Easy),
        ask("slonce_typ", "What is the Sun?", "A star", listOf("A planet", "A comet", "A moon"), QuizDifficulty.Easy),
        ask("grawitacja_kierunek", "What makes objects fall to the ground?", "Gravity", listOf("Friction", "Pressure", "Magnetism"), QuizDifficulty.Easy),
        ask("swiatlo_zrodlo", "Which of these light sources is natural?", "The Sun", listOf("A light bulb", "A flashlight", "A phone screen"), QuizDifficulty.Easy),
        ask("dzien_rok", "What causes the cycle of day and night?", "Earth rotating on its own axis", listOf("Earth orbiting the Sun", "The Moon's movement", "Changing distance from the Sun"), QuizDifficulty.Easy),
        ask("pory_roku", "What causes the seasons?", "The tilt of Earth's axis", listOf("Changing distance from the Sun", "The Moon's phase", "Earth's rotation speed"), QuizDifficulty.Easy),
        ask("cien_powstaje", "When does a shadow form?", "When an object blocks light", listOf("When light bends", "When the air is humid", "When an object is warm"), QuizDifficulty.Easy),
        ask("dzwiek_prozn", "Does sound travel through a vacuum?", "No, it needs a medium", listOf("Yes, just like in air", "Yes, but slower", "Only at high volume"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("jednostka_mocy", "What is the SI unit of power?", "Watt", listOf("Joule", "Newton", "Ampere"), QuizDifficulty.Medium),
        ask("jednostka_energii", "What is the SI unit of energy?", "Joule", listOf("Watt", "Kelvin", "Pascal"), QuizDifficulty.Medium),
        ask("jednostka_cisnienia", "What is the SI unit of pressure?", "Pascal", listOf("Bar", "Newton", "Tesla"), QuizDifficulty.Medium),
        ask("prawo_ohma", "What does Ohm's law relate?", "Voltage, current and resistance", listOf("Mass, force and acceleration", "Pressure and volume", "Charge and time"), QuizDifficulty.Medium),
        ask("archimedes", "What does Archimedes' principle concern?", "Buoyant force in fluids", listOf("Conservation of momentum", "Thermal expansion", "Reflection of light"), QuizDifficulty.Medium),
        ask("ph_kwas", "What pH does an acidic solution have?", "Below 7", listOf("Equal to 7", "Above 7", "Above 14"), QuizDifficulty.Medium),
        ask("atom_jadro", "What's inside an atom's nucleus?", "Protons and neutrons", listOf("Only electrons", "Protons and electrons", "Only neutrons"), QuizDifficulty.Medium),
        ask("elektron_ladunek", "What charge does an electron have?", "Negative", listOf("Positive", "Zero", "Variable"), QuizDifficulty.Medium),
        ask("ksiezyc_fazy", "How long is a full cycle of Moon phases?", "About 29.5 days", listOf("About 24 days", "Exactly 30 days", "About 365 days"), QuizDifficulty.Medium),
        ask("planeta_pierscienie", "Which planet is famous for its spectacular rings?", "Saturn", listOf("Jupiter", "Uranus", "Neptune"), QuizDifficulty.Medium),
        ask("planeta_najgoretsza", "Which Solar System planet has the highest surface temperature?", "Venus", listOf("Mercury", "Mars", "Jupiter"), QuizDifficulty.Medium),
        ask("kometa_ogon", "Why does a comet have a tail?", "Solar wind blows gas and dust off it", listOf("It burns from friction", "Jupiter's gravity pulls it out", "It spins very fast"), QuizDifficulty.Medium),
        ask("rok_swietlny", "What is a light-year?", "A unit of distance", listOf("A unit of time", "A unit of brightness", "A unit of a star's mass"), QuizDifficulty.Medium),
        ask("stan_plazmy", "Which state of matter dominates in stars?", "Plasma", listOf("Gas", "Liquid", "Solid"), QuizDifficulty.Medium),
        ask("mieszanina_jednorodna", "What is a solution of salt in water?", "A homogeneous mixture", listOf("A chemical compound", "A heterogeneous mixture", "An element"), QuizDifficulty.Medium),
        ask("kataliza", "What does a catalyst do?", "Speeds up a reaction without being consumed itself", listOf("Increases the mass of products", "Changes the reaction's products", "Lowers the surrounding temperature"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("newton_trzecia", "What does Newton's third law state?", "Every action has an equal and opposite reaction", listOf("A body with no force moves at constant velocity", "Force equals mass times acceleration", "Energy is never lost"), QuizDifficulty.Hard),
        ask("doppler", "What does the Doppler effect concern?", "A wave's frequency shift when its source moves", listOf("Light bending in a prism", "Nuclear decay", "Heat conduction"), QuizDifficulty.Hard),
        ask("polprzewodnik", "What characterizes a semiconductor?", "It conducts better when heated or doped", listOf("It never conducts electricity", "It always conducts perfectly", "It loses resistance at any temperature"), QuizDifficulty.Hard),
        ask("izotop", "What differs between isotopes of the same element?", "The number of neutrons", listOf("The number of protons", "The nuclear charge", "The atomic number"), QuizDifficulty.Hard),
        ask("polowiczny_rozpad", "What does half-life mean?", "The time after which half of the nuclei have decayed", listOf("The time for total decay", "The time to emit one quantum", "The time for one electron orbit"), QuizDifficulty.Hard),
        ask("entropia_pojecie", "What is entropy, roughly speaking?", "A measure of a system's disorder", listOf("A measure of a system's mass", "A measure of reaction speed", "A measure of charge"), QuizDifficulty.Hard),
        ask("supernowa", "How does the life of a very massive star end?", "A supernova explosion", listOf("Turning into a planet", "Slowly cooling into a brown dwarf", "Breaking apart into comets"), QuizDifficulty.Hard),
        ask("czarna_dziura_masa", "What forms from the core of the most massive stars after a supernova?", "A black hole", listOf("A white dwarf", "A planetary nebula", "A quasar"), QuizDifficulty.Hard),
        ask("bialy_karzel", "What will the Sun become at the end of its life?", "A white dwarf", listOf("A black hole", "A neutron star", "A supernova"), QuizDifficulty.Hard),
        ask("czastka_higgsa", "At which facility was the Higgs boson discovered?", "CERN", listOf("NASA", "Fermilab", "MIT"), QuizDifficulty.Hard),
        ask("laser_skrot", "What is the core mechanism behind a laser?", "Stimulated emission of radiation", listOf("Nuclear fission", "Electron friction", "Gas compression"), QuizDifficulty.Hard),
        ask("nadprzewodnik", "What happens to a superconductor's resistance below its critical temperature?", "It drops to zero", listOf("It rises sharply", "It stays the same", "It changes sign"), QuizDifficulty.Hard),
        ask("moly_liczba", "How many particles does one mole of a substance contain?", "About 6.02 x 10^23", listOf("About 3.14 x 10^12", "Exactly one million", "About 10^100"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("heisenberg", "What does Heisenberg's uncertainty principle concern?", "The limit on simultaneously measuring momentum and position", listOf("Conservation of energy", "Beta decay", "The speed of light in a medium"), QuizDifficulty.Expert),
        ask("splatanie", "What is quantum entanglement?", "Correlated particle states regardless of distance", listOf("A collision of two photons", "Current flow without resistance", "A particle decaying into three"), QuizDifficulty.Expert),
        ask("cmb", "What is the cosmic microwave background radiation?", "A remnant of the early Universe", listOf("Emission from the Milky Way", "Radiation from the Sun", "Detector noise"), QuizDifficulty.Expert),
        ask("fale_graw", "When were gravitational waves first directly detected?", "2015", listOf("1998", "2020", "1974"), QuizDifficulty.Expert),
        ask("granica_chandrasekhara", "What does the Chandrasekhar limit concern?", "The maximum mass of a white dwarf", listOf("Earth's escape velocity", "The temperature of hydrogen fusion", "A galaxy's size"), QuizDifficulty.Expert),
        ask("hawking", "What does Hawking radiation predict?", "That black holes slowly evaporate", listOf("That the Universe is shrinking", "That gravity is a strong force", "That time flows backward"), QuizDifficulty.Expert),
        ask("neutrino_masa", "What was discovered about neutrinos through observing their oscillation?", "That they have a nonzero mass", listOf("That they move faster than light", "That they carry a positive charge", "That they don't exist"), QuizDifficulty.Expert),
        ask("model_standardowy", "How many fundamental interactions does the Standard Model describe?", "Three", listOf("Four", "Two", "Five"), QuizDifficulty.Expert),
        ask("kwark_rodzaje", "How many types (flavors) of quarks are there?", "Six", listOf("Three", "Four", "Eight"), QuizDifficulty.Expert),
        ask("stala_hubble", "What does the Hubble constant describe?", "The rate at which the Universe is expanding", listOf("The Milky Way's mass", "Supernova brightness", "The Sun's age"), QuizDifficulty.Expert),
        ask("bose_einstein", "What is a Bose-Einstein condensate?", "A state of matter near absolute zero", listOf("Stellar plasma", "A supercritical fluid", "A metallic crystal"), QuizDifficulty.Expert),
        ask("efekt_fotoelektryczny", "For explaining which phenomenon did Einstein win the Nobel Prize?", "The photoelectric effect", listOf("The theory of relativity", "Brownian motion", "Mass-energy equivalence"), QuizDifficulty.Expert)
    )
}
