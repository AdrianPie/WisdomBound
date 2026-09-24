package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Third batch of animals - behaviors, habitats and records not yet covered. */
object AnimalQuestionsExtra2 {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "zwi3_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Animals,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("pies_wech", "Which sense are dogs especially known for?", "Smell", listOf("Sight", "Taste", "Touch"), QuizDifficulty.Easy),
        ask("kot_widzenie", "When do cats see especially well?", "At dusk and in darkness", listOf("Only at noon", "Only underwater", "They can't see in the dark at all"), QuizDifficulty.Easy),
        ask("krolik_zeby", "What do a rabbit's teeth do throughout its life?", "Grow continuously", listOf("Fall out every year", "Not grow at all", "Change color"), QuizDifficulty.Easy),
        ask("wiewiorka_zapasy", "What do squirrels do in the fall?", "Store nuts", listOf("Migrate south", "Fall into deep sleep", "Shed their fur"), QuizDifficulty.Easy),
        ask("jedwabnik", "What is silk made from?", "Silkworm cocoons", listOf("Sheep's wool", "Plant fibers", "Goat hair"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("mrowkolew", "How does an antlion larva hunt?", "It digs a pit trap in the sand and waits for prey", listOf("It runs its prey down on foot in the open", "It catches insects in mid-air while flying", "It spins a sticky web the way a spider does"), QuizDifficulty.Medium),
        ask("panda_kciuk", "What helps a panda hold bamboo?", "An extra 'thumb' made from an enlarged wrist bone", listOf("Rough suction pads on the soles of its paws", "Unusually long claws and nothing else", "A tail it can curl round the stem"), QuizDifficulty.Medium),
        ask("wilk_komunikacja", "What is wolf howling used for?", "Long-distance communication within the pack", listOf("Marking the edges of the territory by scent", "Stunning small prey with the sound", "Calling in the rain during a drought"), QuizDifficulty.Medium),
        ask("delfin_echolokacja", "How do dolphins navigate underwater in murky water?", "Using echolocation", listOf("Using sight alone", "Using their sense of taste", "Using a magnetic field alone"), QuizDifficulty.Medium),
        ask("mrowki_feromony", "What do ants mark a trail to food with?", "Pheromones", listOf("Saliva", "Droppings alone", "Sound"), QuizDifficulty.Medium),
        ask("niedzwiedz_hibernacja", "What happens to a bear's heart rate during hibernation?", "It drops significantly", listOf("It rises", "It doesn't change", "It stops completely"), QuizDifficulty.Medium),
        ask("mucha_oczy", "What characterizes a fly's eyes?", "They're made of thousands of lenses", listOf("They only see black and white", "They only have one eye", "They can't see motion"), QuizDifficulty.Medium),
        ask("waz_jad_rodzaje", "What does most venomous snakes' venom act on?", "The nervous system or blood clotting", listOf("The skin and nothing deeper", "The eyesight of the victim", "The hearing of the victim"), QuizDifficulty.Medium),
        ask("kon_zeby_wiek", "What can a horse's age be roughly judged by?", "Its teeth", listOf("The length of its tail", "The color of its eyes", "The growth of its hooves"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("slon_infradzwieki", "How do elephants communicate over long distances?", "Infrasound felt through the ground", listOf("Trumpeting alone", "Long-range urine scent", "Flapping their ears in daylight"), QuizDifficulty.Hard),
        ask("motyl_monarcha", "How far do monarch butterflies migrate?", "Thousands of kilometers between Canada and Mexico", listOf("A few dozen kilometres around one meadow", "Only inside a single flowering meadow", "They do not migrate anywhere at all"), QuizDifficulty.Hard),
        ask("rekin_mlot_oczy", "Why does the hammerhead shark have such widely-spaced eyes?", "For better stereoscopic vision and electric-field detection", listOf("For a far keener sense of smell in the open ocean", "For faster swimming with less drag in the water", "For picking up low sounds from a long way off"), QuizDifficulty.Hard),
        ask("orangutan_narzedzia", "What can orangutans do in the wild?", "Use simple tools, like sticks", listOf("Build dams like beavers", "Hunt in groups like wolves", "Migrate between continents"), QuizDifficulty.Hard),
        ask("kalmar_kamuflaz", "How do cephalopods like squid change skin color?", "Through chromatophores - pigment cells", listOf("By changing body temperature", "By secreting paint", "By changing their diet"), QuizDifficulty.Hard),
        ask("zuraw_taniec", "What is the crane's dance used for?", "Courtship and strengthening the pair bond", listOf("Driving off predators from the nest", "Coordinating a hunt as a group", "Marking the edge of the territory"), QuizDifficulty.Hard),
        ask("mrowki_niewolnictwo", "What do some ant species called 'slave-making ants' do?", "They steal other ants' pupae to work in their own colony", listOf("They trade food with other colonies", "They only farm aphids like cattle", "They only build bridges from their own bodies"), QuizDifficulty.Hard),
        ask("wieloryb_babelkowanie", "How do humpback whales hunt together using 'bubble netting'?", "They surround fish with a ring of air bubbles", listOf("They stun fish with sound", "They release poison into the water", "They use rays as bait"), QuizDifficulty.Hard),
        ask("nietoperz_zapylanie", "Besides eating insects, what role do many bat species play in nature?", "They pollinate flowers and spread seeds", listOf("They produce honey", "They're producers in the food chain", "They filter water in rivers"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("niesporczak_kryptobioza", "How do tardigrades survive extreme conditions, like a vacuum?", "They enter cryptobiosis, drying their body out almost entirely", listOf("They fall into an ordinary hibernation, as bears do", "They change the chemistry of their blood", "They do not in fact survive such conditions"), QuizDifficulty.Expert),
        ask("ryba_elektryczna", "How does an electric eel generate its discharges?", "Using specialized cells called electrocytes", listOf("Using a magnet in its head", "By rubbing its scales together", "By absorbing current from its surroundings"), QuizDifficulty.Expert),
        ask("ptak_nawigacja_kwant", "What mechanism is considered in the magnetic navigation of some birds?", "Spin-dependent reactions in eye proteins (cryptochromes)", listOf("Grains of magnetic metal in the beak and nothing else", "Reading the stars, but only during daylight", "Echolocation of the kind bats use at night"), QuizDifficulty.Expert),
        ask("mrowki_rolnictwo", "What do leafcutter ants of the genus Atta do?", "Farm fungus on the leaves they bring as their main food", listOf("They hunt other insects and eat nothing else", "Live in symbiosis with fish in flooded nests", "Migrate to another continent every year"), QuizDifficulty.Expert),
        ask("plaszczka_manta_mozg", "What's unusual about the manta ray's brain among fish?", "It's one of the largest relative to body mass among fish", listOf("It is the smallest brain of any fish of that size", "It has no brain at all, only a net of nerves", "It is split into two entirely separate organs"), QuizDifficulty.Expert),
        ask("hydra_regeneracja", "What is the hydra famous for among animals?", "It practically doesn't age and can regenerate almost its whole body", listOf("It lives only in the open ocean and never in fresh water", "It has the longest lifespan of any known animal", "It is the largest invertebrate ever measured"), QuizDifficulty.Expert),
        ask("orka_kultura", "What is observed among different orca populations worldwide?", "Different 'cultures' - distinct hunting techniques passed on socially", listOf("Exactly the same hunting behaviour in every ocean they live in", "A complete absence of any social organisation", "No communication between members of a group"), QuizDifficulty.Expert),
        ask("meduza_immortal_mechanizm", "How does Turritopsis dohrnii 'reverse' its aging?", "Through cell transdifferentiation back to an earlier polyp stage", listOf("By cloning itself once a year and letting the old body die", "By replacing the whole of its own DNA", "By hibernating on the floor of the ocean"), QuizDifficulty.Expert)
    )
}
