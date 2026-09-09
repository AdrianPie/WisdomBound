package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Third batch of science - the scientific method, inventions and experiments not yet covered. */
object ScienceQuestionsExtra2 {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "sci3_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Science,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("barometr_funkcja", "What does a barometer measure?", "Atmospheric pressure", listOf("Temperature", "Humidity", "Speed of sound"), QuizDifficulty.Easy),
        ask("silnik_parowy", "Who improved the steam engine, paving the way for the Industrial Revolution?", "James Watt", listOf("Thomas Edison", "Alexander Graham Bell", "Isaac Newton"), QuizDifficulty.Easy),
        ask("radio_wynalazca", "Who is credited with inventing radio?", "Guglielmo Marconi", listOf("Thomas Edison", "Alexander Bell", "Nikola Tesla, undisputedly alone"), QuizDifficulty.Easy),
        ask("penicylina_rok", "In which year did Fleming discover penicillin?", "1928", listOf("1918", "1938", "1948"), QuizDifficulty.Easy),
        ask("szczepionka_pasteur", "What did Louis Pasteur work on?", "Microbiology and vaccines", listOf("Nuclear physics", "Astronomy", "Electricity"), QuizDifficulty.Easy),
        ask("newton_jablko", "What supposedly inspired Newton's thinking on gravity?", "A falling apple", listOf("A comet in the sky", "A solar eclipse", "An earthquake"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("metoda_naukowa", "What is the first step of the scientific method?", "Asking a question or making an observation", listOf("Publishing the results", "Designing a poster", "Collecting an award"), QuizDifficulty.Medium),
        ask("hipoteza_definicja", "What is a hypothesis in science?", "A proposed explanation to be tested", listOf("A proven law", "A final conclusion", "A type of experiment"), QuizDifficulty.Medium),
        ask("grupa_kontrolna", "What is a control group used for in an experiment?", "Comparison with the test group", listOf("Speeding up the study", "Funding the research", "Publishing results"), QuizDifficulty.Medium),
        ask("curie_odkrycia", "For discovering which elements did Marie Sklodowska-Curie win a Nobel Prize?", "Polonium and radium", listOf("Oxygen and hydrogen", "Uranium and plutonium", "Gold and silver"), QuizDifficulty.Medium),
        ask("darwin_statek", "What was the name of the ship Charles Darwin sailed on?", "HMS Beagle", listOf("Endeavour", "Santa Maria", "Mayflower"), QuizDifficulty.Medium),
        ask("mendelejew_odkrycie", "What did Dmitri Mendeleev organize?", "The periodic table of elements", listOf("Laws of planetary motion", "The theory of evolution", "Star classification"), QuizDifficulty.Medium),
        ask("gps_technologia", "What technology does GPS rely on?", "Signals from satellites", listOf("Radio waves from ground towers only", "Earth's magnetic field", "Sound"), QuizDifficulty.Medium),
        ask("rentgen_promienie", "Who discovered X-rays?", "Wilhelm Rontgen", listOf("Marie Curie", "Max Planck", "Niels Bohr"), QuizDifficulty.Medium),
        ask("fleming_bakteria", "Which mold/bacterium did Fleming observe when discovering penicillin?", "Penicillium mold", listOf("E. coli bacteria", "The flu virus", "Baker's yeast"), QuizDifficulty.Medium),
        ask("chemia_analityczna", "What does analytical chemistry deal with?", "Determining the composition and amount of substances in a sample", listOf("Synthesizing new drugs only", "Studying stars", "Designing engines"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("teoria_falsyfikacji", "Who formulated falsifiability as a criterion of being scientific?", "Karl Popper", listOf("Thomas Kuhn", "Isaac Newton", "Francis Bacon"), QuizDifficulty.Hard),
        ask("kuhn_paradygmat", "What does Thomas Kuhn's concept of a 'paradigm shift' describe?", "A revolutionary change in science's dominant way of thinking", listOf("Gradually refining the same theory", "Discovering a new element", "Publishing in a journal"), QuizDifficulty.Hard),
        ask("michelson_morley", "What was the Michelson-Morley experiment trying to detect?", "The ether as a medium for light", listOf("Background radiation", "Radioactive decay", "Gravitational waves"), QuizDifficulty.Hard),
        ask("eksperyment_podwojnej_szczeliny", "What does the double-slit experiment show?", "The wave-particle duality of light and matter", listOf("That matter is always a wave", "That light has no mass", "That electrons don't exist"), QuizDifficulty.Hard),
        ask("cern_lhc", "What is the Large Hadron Collider (LHC) used for?", "Colliding particles at enormous energies", listOf("Observing distant galaxies", "Predicting earthquakes", "Producing electric power"), QuizDifficulty.Hard),
        ask("recenzja_naukowa", "What is scientific peer review?", "Evaluation of research by other experts before publication", listOf("An author's own proofreading", "Automatic grammar checking", "A public vote"), QuizDifficulty.Hard),
        ask("powtarzalnosc_wynikow", "Why does an experiment's reproducibility matter in science?", "It confirms the reliability of the result", listOf("It speeds up publication", "It lowers research costs", "It increases sensationalism"), QuizDifficulty.Hard),
        ask("teoria_a_prawo", "How does a scientific theory differ from a scientific law?", "A theory explains a phenomenon, a law describes its regularity mathematically", listOf("A theory is less certain than a guess", "A law is unchallengeable forever", "They're exactly the same"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("popper_demarkacja", "What is the problem of distinguishing science from pseudoscience called?", "The demarcation problem", listOf("The problem of induction", "The measurement problem", "The uncertainty principle"), QuizDifficulty.Expert),
        ask("bayesowskie_wnioskowanie", "What does Bayesian inference in science rely on?", "Updating a hypothesis's probability as new data arrives", listOf("Only controlled experiments", "A vote among experts", "Qualitative analysis without numbers"), QuizDifficulty.Expert),
        ask("cern_higgs_rok", "In which year was the discovery of the Higgs boson at CERN announced?", "2012", listOf("2008", "2016", "2000"), QuizDifficulty.Expert),
        ask("planck_kwant", "What launched Planck's quantum theory?", "Explaining black-body radiation", listOf("Discovering the electron", "The theory of relativity", "The Michelson-Morley experiment"), QuizDifficulty.Expert),
        ask("nauka_replication_crisis", "What is the so-called replication crisis in science?", "Difficulty reproducing the results of many published studies", listOf("A lack of science funding", "Too many scientists", "Technology advancing too fast"), QuizDifficulty.Expert),
        ask("occam_brzytwa", "What does Occam's razor state?", "Among equally valid explanations, pick the simplest", listOf("Every theory must be mathematical", "Complex theories are always better", "Science can never be simple"), QuizDifficulty.Expert),
        ask("wilson_chmura_pomiarowa", "What was Wilson's cloud chamber used for in physics?", "Visualizing the tracks of charged particles", listOf("Measuring the temperature of space", "Producing isotopes", "Cooling reactors"), QuizDifficulty.Expert)
    )
}
