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
        ask("kura_jajo", "What does a hen lay?", "Eggs", listOf("Milk", "Honey", "Wool"), QuizDifficulty.Easy),
        ask("owca_produkt", "What is obtained from sheep?", "Wool", listOf("Honey", "Silk", "Wax"), QuizDifficulty.Easy),
        ask("pszczola_produkt", "What do bees produce?", "Honey", listOf("Wool", "Silk", "Milk"), QuizDifficulty.Easy),
        ask("jedwabnik", "What is silk made from?", "Silkworm cocoons", listOf("Sheep's wool", "Plant fibers", "Goat hair"), QuizDifficulty.Easy),
        ask("zaba_srodowisko", "Where do frogs usually live?", "In water and on land", listOf("Only in the sea", "Only in the desert", "Only in the air"), QuizDifficulty.Easy),
        ask("waz_poruszanie", "How does a snake move?", "Slithering without legs", listOf("Hopping on two legs", "Flying", "Only swimming"), QuizDifficulty.Easy),
        ask("kot_futro_kolor", "What determines a cat's coat coloring?", "Genes", listOf("Diet", "Age", "Weather"), QuizDifficulty.Easy),
        ask("pies_rasy", "How do dog breeds differ?", "In appearance and traits bred by humans", listOf("In biological species", "In number of legs", "In how they breathe"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("mrowkolew", "How does an antlion larva hunt?", "It digs a pit trap in the sand and waits for prey", listOf("It chases prey on foot", "It flies and catches prey mid-air", "It spins a web like a spider"), QuizDifficulty.Medium),
        ask("panda_kciuk", "What helps a panda hold bamboo?", "An extra 'thumb' made from an enlarged wrist bone", listOf("Suckers on its paws", "Long claws alone", "A prehensile tail"), QuizDifficulty.Medium),
        ask("wilk_komunikacja", "What is wolf howling used for?", "Long-distance communication within the pack", listOf("Scent-marking territory only", "Stunning prey", "Calling rain"), QuizDifficulty.Medium),
        ask("delfin_echolokacja", "How do dolphins navigate underwater in murky water?", "Using echolocation", listOf("Using sight alone", "Using their sense of taste", "Using a magnetic field alone"), QuizDifficulty.Medium),
        ask("mrowki_feromony", "What do ants mark a trail to food with?", "Pheromones", listOf("Saliva", "Droppings alone", "Sound"), QuizDifficulty.Medium),
        ask("niedzwiedz_hibernacja", "What happens to a bear's heart rate during hibernation?", "It drops significantly", listOf("It rises", "It doesn't change", "It stops completely"), QuizDifficulty.Medium),
        ask("kot_pazury_wciagane", "Why does a cat retract its claws while walking?", "To keep them from getting dull on the ground", listOf("Because walking with them out hurts", "Because it makes them look bigger", "Because it regulates body temperature"), QuizDifficulty.Medium),
        ask("slon_pamiec", "What is an elephant's memory famous for?", "Elephants remember routes to water for years", listOf("Elephants forget after a week", "Elephants have no long-term memory", "Elephants only remember their own calves"), QuizDifficulty.Medium),
        ask("mucha_oczy", "What characterizes a fly's eyes?", "They're made of thousands of lenses", listOf("They only see black and white", "They only have one eye", "They can't see motion"), QuizDifficulty.Medium),
        ask("waz_jad_rodzaje", "What does most venomous snakes' venom act on?", "The nervous system or blood clotting", listOf("The skin only", "The victim's eyesight", "The victim's hearing"), QuizDifficulty.Medium),
        ask("kon_zeby_wiek", "What can a horse's age be roughly judged by?", "Its teeth", listOf("The length of its tail", "The color of its eyes", "The growth of its hooves"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("slon_infradzwieki", "How do elephants communicate over long distances?", "Infrasound felt through the ground", listOf("Trumpeting alone", "Long-range urine scent", "Flapping their ears in daylight"), QuizDifficulty.Hard),
        ask("motyl_monarcha", "How far do monarch butterflies migrate?", "Thousands of kilometers between Canada and Mexico", listOf("A few dozen kilometers locally", "Only within a single meadow", "They don't migrate at all"), QuizDifficulty.Hard),
        ask("rekin_mlot_oczy", "Why does the hammerhead shark have such widely-spaced eyes?", "For better stereoscopic vision and electric-field detection", listOf("For a better sense of smell", "For faster swimming", "For better hearing"), QuizDifficulty.Hard),
        ask("orangutan_narzedzia", "What can orangutans do in the wild?", "Use simple tools, like sticks", listOf("Build dams like beavers", "Hunt in groups like wolves", "Migrate between continents"), QuizDifficulty.Hard),
        ask("kalmar_kamuflaz", "How do cephalopods like squid change skin color?", "Through chromatophores - pigment cells", listOf("By changing body temperature", "By secreting paint", "By changing their diet"), QuizDifficulty.Hard),
        ask("zuraw_taniec", "What is the crane's dance used for?", "Courtship and strengthening the pair bond", listOf("Scaring off predators", "Group hunting", "Marking territory"), QuizDifficulty.Hard),
        ask("mrowki_niewolnictwo", "What do some ant species called 'slave-making ants' do?", "They steal other ants' pupae to work in their own colony", listOf("They trade food with other colonies", "They only farm aphids like cattle", "They only build bridges from their own bodies"), QuizDifficulty.Hard),
        ask("wieloryb_babelkowanie", "How do humpback whales hunt together using 'bubble netting'?", "They surround fish with a ring of air bubbles", listOf("They stun fish with sound", "They release poison into the water", "They use rays as bait"), QuizDifficulty.Hard),
        ask("nietoperz_zapylanie", "Besides eating insects, what role do many bat species play in nature?", "They pollinate flowers and spread seeds", listOf("They produce honey", "They're producers in the food chain", "They filter water in rivers"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("niesporczak_kryptobioza", "How do tardigrades survive extreme conditions, like a vacuum?", "They enter cryptobiosis, drying their body out almost entirely", listOf("They enter normal hibernation", "They change their blood chemistry", "They don't survive such conditions"), QuizDifficulty.Expert),
        ask("ryba_elektryczna", "How does an electric eel generate its discharges?", "Using specialized cells called electrocytes", listOf("Using a magnet in its head", "By rubbing its scales together", "By absorbing current from its surroundings"), QuizDifficulty.Expert),
        ask("ptak_nawigacja_kwant", "What mechanism is considered in the magnetic navigation of some birds?", "Spin-dependent reactions in eye proteins (cryptochromes)", listOf("Metal particles in the beak only", "Analyzing stars only in daytime", "Echolocation like bats"), QuizDifficulty.Expert),
        ask("mrowki_rolnictwo", "What do leafcutter ants of the genus Atta do?", "Farm fungus on the leaves they bring as their main food", listOf("Only hunt other insects", "Live in symbiosis with fish", "Migrate to another continent every year"), QuizDifficulty.Expert),
        ask("plaszczka_manta_mozg", "What's unusual about the manta ray's brain among fish?", "It's one of the largest relative to body mass among fish", listOf("It's the smallest among fish", "It has no brain, only nerve nets", "It consists of two separate organs"), QuizDifficulty.Expert),
        ask("hydra_regeneracja", "What is the hydra famous for among animals?", "It practically doesn't age and can regenerate almost its whole body", listOf("It lives only in the ocean", "It has the longest lifespan of any animal", "It's the largest invertebrate"), QuizDifficulty.Expert),
        ask("orka_kultura", "What is observed among different orca populations worldwide?", "Different 'cultures' - distinct hunting techniques passed on socially", listOf("Identical behavior everywhere", "A total lack of social organization", "A complete lack of communication"), QuizDifficulty.Expert),
        ask("meduza_immortal_mechanizm", "How does Turritopsis dohrnii 'reverse' its aging?", "Through cell transdifferentiation back to an earlier polyp stage", listOf("By cloning itself every year", "By replacing its entire DNA", "By hibernating on the ocean floor"), QuizDifficulty.Expert)
    )
}
