package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Kosmos - od Ksiezyca po kosmologie. Nowa pula pod [QuizCategory.Science], bo dotychczasowa nauka
 * jest mocno przechylona ku chemii i fizyce szkolnej, a bieg ma teraz dwadziescia fal: przy takiej
 * dlugosci gracz widzi jedna kategorie kilkanascie razy w ciagu JEDNEJ planszy i powtorki zaczynaja
 * sie rzucac w oczy.
 *
 * Rozklad trudnosci jest CELOWO przesuniety w gore (malo Easy, duzo Hard/Expert): drabinka pytan
 * rosnie teraz takze z numerem fali ([com.lernhero.domain.run.RunDifficulty.campaignLadder]), wiec
 * to gornych szczebli brakowalo najbardziej - dwudziesta fala potrafila wracac do tych samych
 * kilkunastu pytan eksperckich.
 */
object SpaceQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "space_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Science,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("slonce_typ", "What kind of object is the Sun?", "A star", listOf("A planet", "A comet", "A nebula"), QuizDifficulty.Easy),
        ask("ksiezyc_ziemi", "How many natural moons does Earth have?", "One", listOf("Two", "None", "Four"), QuizDifficulty.Easy),
        ask("pierscienie", "Which planet is famous for its bright ring system?", "Saturn", listOf("Uranus", "Mars", "Venus"), QuizDifficulty.Easy),
        ask("galaktyka", "What is the name of our galaxy?", "The Milky Way", listOf("Andromeda", "Orion", "Centaurus"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("najgoretsza", "Which planet has the hottest surface?", "Venus", listOf("Mercury", "Mars", "Jupiter"), QuizDifficulty.Medium),
        ask("pas_planetoid", "Between which two planets does the main asteroid belt lie?", "Mars and Jupiter", listOf("Earth and Mars", "Jupiter and Saturn", "Venus and Earth"), QuizDifficulty.Medium),
        ask("plamy_sloneczne", "What are sunspots?", "Cooler regions of the Sun's surface", listOf("Holes punched through the Sun", "Clouds of interstellar dust", "Shadows cast by the planets"), QuizDifficulty.Medium),
        ask("kometa_ogon", "Why does a comet grow a tail near the Sun?", "Its ice turns to gas and is blown outward", listOf("It burns up in the solar wind", "It spins fast enough to shed rock", "It collides with belts of dust"), QuizDifficulty.Medium),
        ask("iss_orbita", "Roughly how long does the ISS take to orbit Earth once?", "About 90 minutes", listOf("About 24 hours", "About 12 hours", "About 3 hours"), QuizDifficulty.Medium),
        ask("faza_ksiezyca", "What causes the phases of the Moon?", "The changing angle of sunlight we see", listOf("The shadow Earth casts on it", "Clouds drifting between the two", "Light the Moon produces itself"), QuizDifficulty.Medium),
        ask("meteor_meteoryt", "What do we call a space rock that survives and lands on Earth?", "A meteorite", listOf("A meteor", "A meteoroid", "A comet"), QuizDifficulty.Medium),
        ask("wenus_obrot", "Which planet rotates in the opposite direction to most others?", "Venus", listOf("Mars", "Neptune", "Mercury"), QuizDifficulty.Medium),
        ask("teleskop_hubble", "Where is the Hubble Space Telescope located?", "In orbit around Earth", listOf("On the Moon", "At the South Pole", "On the ISS"), QuizDifficulty.Medium),

        /* ------------------------------- Hard ------------------------------- */
        ask("pluton_status", "What is Pluto classified as today?", "A dwarf planet", listOf("A planet", "A moon", "A comet"), QuizDifficulty.Hard),
        ask("najwiekszy_ksiezyc", "Which is the largest moon in the Solar System?", "Ganymede", listOf("Titan", "Europa", "The Moon"), QuizDifficulty.Hard),
        ask("europa_ocean", "Which moon of Jupiter is thought to hide an ocean under its ice?", "Europa", listOf("Io", "Callisto", "Titan"), QuizDifficulty.Hard),
        ask("tytan_atmosfera", "Which moon has a thick nitrogen atmosphere?", "Titan", listOf("Europa", "Phobos", "Triton"), QuizDifficulty.Hard),
        ask("czerwony_olbrzym", "What will the Sun become near the end of its life?", "A red giant, then a white dwarf", listOf("A black hole, then nothing", "A supernova, then a nebula", "A neutron star, then a pulsar"), QuizDifficulty.Hard),
        ask("efekt_dopplera", "What does redshift tell astronomers about a distant galaxy?", "It is moving away from us", listOf("It is hotter", "It is younger", "It is closer"), QuizDifficulty.Hard),
        ask("prom_wahadlowiec", "In which year did the Space Shuttle program end?", "2011", listOf("2005", "2015", "1999"), QuizDifficulty.Hard),
        ask("voyager", "Which spacecraft was the first to leave the heliosphere?", "Voyager 1", listOf("Voyager 2", "Pioneer 10", "New Horizons"), QuizDifficulty.Hard),
        ask("mleczna_droga_typ", "What type of galaxy is the Milky Way?", "A barred spiral", listOf("An elliptical", "An irregular", "A lenticular"), QuizDifficulty.Hard),
        ask("jowisz_plama", "What is Jupiter's Great Red Spot?", "A giant long-lived storm", listOf("A volcano", "A crater", "A dust cloud"), QuizDifficulty.Hard),
        ask("neptun_odkrycie", "Neptune was discovered mainly thanks to what?", "Mathematical prediction of its orbit", listOf("A lucky photograph of the sky", "A radio signal from its moons", "A comet passing right by it"), QuizDifficulty.Hard),
        ask("kepler", "What did Kepler's laws describe?", "The motion of planets around the Sun", listOf("The speed of light", "The mass of stars", "The age of the universe"), QuizDifficulty.Hard),

        /* ------------------------------ Expert ------------------------------ */
        ask("chandrasekhar", "What does the Chandrasekhar limit describe?", "The maximum mass of a stable white dwarf", listOf("The largest size a galaxy reaches", "The fastest spin a pulsar can hold", "The longest life of a red giant"), QuizDifficulty.Expert),
        ask("pulsar", "What is a pulsar?", "A rapidly rotating neutron star emitting beams", listOf("A red giant pulsating as it cools", "A pair of black holes in orbit", "A protostar still gathering mass"), QuizDifficulty.Expert),
        ask("soczewkowanie", "What is gravitational lensing?", "Mass bending the path of light", listOf("A telescope defect", "Light slowing in vacuum", "Refraction in the atmosphere"), QuizDifficulty.Expert),
        ask("hubble_stala", "What does the Hubble constant measure?", "The expansion rate of the universe", listOf("The mass of the Milky Way", "The brightness of supernovae", "The distance to the Sun"), QuizDifficulty.Expert),
        ask("supernowa_ia", "Why are Type Ia supernovae useful in astronomy?", "They have a known brightness, so they measure distance", listOf("They are the brightest objects ever observed", "They repeat on a fixed, predictable schedule", "They emit nothing except radio waves"), QuizDifficulty.Expert),
        ask("oort", "What is the Oort cloud believed to be?", "A distant shell of icy bodies around the Sun", listOf("A dust ring around Jupiter", "A nebula near Orion", "A belt between Mars and Jupiter"), QuizDifficulty.Expert),
        ask("lagrange", "What is special about a Lagrange point?", "Gravity lets an object hold position relative to two bodies", listOf("Gravity there cancels out to exactly zero", "It is the hottest point along the whole orbit", "It marks the outer edge of a solar system"), QuizDifficulty.Expert),
        ask("parallaksa", "What does stellar parallax allow astronomers to measure?", "The distance to nearby stars", listOf("A star's temperature", "A star's chemical makeup", "A star's rotation"), QuizDifficulty.Expert),
    )
}
