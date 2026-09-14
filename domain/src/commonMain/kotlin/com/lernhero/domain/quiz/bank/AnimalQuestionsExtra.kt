package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Second batch of animals - from baby-animal names to evolutionary record-holders. */
object AnimalQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "zwi2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Animals,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("mlode_zaby", "What is a baby frog called right after hatching?", "Tadpole", listOf("Larva", "Pupa", "Fry"), QuizDifficulty.Easy),
        ask("delfin_klasa", "Which animal group do dolphins belong to?", "Mammals", listOf("Fish", "Reptiles", "Amphibians"), QuizDifficulty.Easy),
        ask("wielblad_garby", "What does a camel store in its hump?", "Fat", listOf("Water", "Air", "Salt"), QuizDifficulty.Easy),
        ask("kangur_kraj", "Which continent do kangaroos come from?", "Australia", listOf("Africa", "South America", "Asia"), QuizDifficulty.Easy),
        ask("zyrafa_plamy", "What else is a giraffe known for besides its height?", "A long, black tongue", listOf("Gills", "Spikes on its back", "A trunk"), QuizDifficulty.Easy),
        ask("ryba_oddech", "What do fish breathe with?", "Gills", listOf("Lungs", "Skin", "Windpipes"), QuizDifficulty.Easy),
        ask("wilk_grupa", "What is a group of wolves called?", "A pack", listOf("A flock", "A swarm", "A shoal"), QuizDifficulty.Easy),
        ask("motyl_stadium", "What does a caterpillar become before it's a butterfly?", "A chrysalis (pupa)", listOf("An egg", "A tadpole", "A fly larva"), QuizDifficulty.Easy),
        ask("jez_obrona", "How does a hedgehog defend itself from predators?", "It curls into a spiky ball", listOf("It climbs a tree", "It sprays ink", "It pretends to be bigger"), QuizDifficulty.Easy),
        ask("bocian_zima", "What do white storks do for the winter?", "Fly to Africa", listOf("Hibernate", "Stay in their nests", "Move into the forest"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("rekin_szkielet", "What is a shark's skeleton made of?", "Cartilage", listOf("Bone", "Chitin", "Horn"), QuizDifficulty.Medium),
        ask("nietoperz_klasa", "Which class do bats belong to?", "Mammals", listOf("Birds", "Reptiles", "Insects"), QuizDifficulty.Medium),
        ask("zolw_wiek", "What is a tortoise's age most often estimated from?", "The rings on its shell scutes", listOf("The length of its tail", "The number of its toes", "Its eye color"), QuizDifficulty.Medium),
        ask("pszczola_rodzina", "How many queens live in one healthy bee colony?", "One", listOf("Three", "Ten", "None"), QuizDifficulty.Medium),
        ask("mrowka_sila", "What is an ant famous for, physically?", "Carrying many times its own body weight", listOf("Running the fastest of all insects", "Living the longest of all insects", "Seeing in infrared"), QuizDifficulty.Medium),
        ask("lososie_wedrowka", "Where do salmon return to spawn?", "The river where they hatched", listOf("The open ocean", "Polar lakes", "The Nile delta"), QuizDifficulty.Medium),
        ask("wieloryb_oddech", "What does a whale breathe with?", "Lungs", listOf("Gills", "Skin", "Windpipes"), QuizDifficulty.Medium),
        ask("strus_jajo", "Which living animal lays the largest eggs?", "Ostrich", listOf("Condor", "Pelican", "Albatross"), QuizDifficulty.Medium),
        ask("koala_dieta", "What does a koala eat?", "Eucalyptus leaves", listOf("Bamboo", "Mangoes", "Grass"), QuizDifficulty.Medium),
        ask("susel_zima", "What does a marmot do in winter?", "Hibernate", listOf("Migrate south", "Hunt under the snow", "Turn its fur white"), QuizDifficulty.Medium),
        ask("orka_grupa", "Which family do orcas belong to?", "Dolphins (Delphinidae)", listOf("Sharks", "Seals", "Porpoises"), QuizDifficulty.Medium),
        ask("plaszczka_bron", "How do most stingrays defend themselves?", "A venomous barb on their tail", listOf("An electric shock from their mouth", "Ink", "Spikes on their back"), QuizDifficulty.Medium),
        ask("kameleon_jezyk", "How does a chameleon catch insects?", "With a long, sticky tongue", listOf("With its front paws", "With its tail", "With spikes"), QuizDifficulty.Medium),
        ask("pingwin_gatunek", "Which penguin is the largest?", "Emperor penguin", listOf("White-flippered penguin", "King penguin", "Adelie penguin"), QuizDifficulty.Medium),
        ask("papuga_mowa", "Which parrot is famous for the best mimicry of speech?", "African grey parrot", listOf("Cockatiel", "Macaw", "Pink cockatoo"), QuizDifficulty.Medium),
        ask("lis_rodzina", "Which family do foxes belong to?", "Canids (dog family)", listOf("Cats", "Weasels", "Bears"), QuizDifficulty.Medium),
        ask("meduza_uklad", "What does a jellyfish not have?", "A brain", listOf("A mouth", "Tentacles", "Stinging cells"), QuizDifficulty.Medium),
        ask("bobr_budowa", "What do beavers build on rivers?", "Dams and lodges", listOf("Termite mounds", "Nests in trees", "Burrows in rock"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("gepard_predkosc", "What speed does a cheetah reach at peak sprint?", "About 110 km/h", listOf("About 60 km/h", "About 150 km/h", "About 200 km/h"), QuizDifficulty.Hard),
        ask("kolibr_serce", "How many beats per minute can a hummingbird's heart reach in flight?", "Over 1000", listOf("About 200", "About 60", "About 5000"), QuizDifficulty.Hard),
        ask("okapi_krewny", "Which animal is the okapi's closest living relative?", "Giraffe", listOf("Zebra", "Antelope", "Tapir"), QuizDifficulty.Hard),
        ask("aksolotl_kraj", "Which country does the axolotl come from?", "Mexico", listOf("Brazil", "China", "Australia"), QuizDifficulty.Hard),
        ask("kraken_kalmar", "Which animal is the largest invertebrate in the world?", "Giant squid", listOf("Giant octopus", "American lobster", "Lion's mane jellyfish"), QuizDifficulty.Hard),
        ask("mrowkojad_zeby", "How many teeth does a giant anteater have?", "None at all", listOf("Eight", "Twenty", "Forty-two"), QuizDifficulty.Hard),
        ask("goryl_dieta", "What does a mountain gorilla eat in the wild?", "Mostly plants", listOf("Mostly meat", "Mostly fish", "Mostly insects"), QuizDifficulty.Hard),
        ask("albatros_lot", "What is the wandering albatross famous for?", "The largest wingspan among birds", listOf("The fastest dive of any seabird", "The longest legs among seabirds", "The loudest call of any seabird"), QuizDifficulty.Hard),
        ask("nagi_kretoszczur", "What makes the naked mole-rat unusual?", "It lives in a colony with a queen, like social insects", listOf("It flies between burrows like a bat", "It changes sex halfway through its life", "It hibernates for ten years at a stretch"), QuizDifficulty.Hard),
        ask("sokolwedrowny", "Which animal is the fastest in the world during a dive?", "Peregrine falcon", listOf("Swift", "Golden eagle", "Hummingbird"), QuizDifficulty.Hard),
        ask("wal_grenlandzki", "Which mammal has the longest lifespan of all?", "Bowhead whale", listOf("African elephant", "Human", "Sperm whale"), QuizDifficulty.Hard),
        ask("skorpion_uv", "What happens to a scorpion under ultraviolet light?", "It glows blue-green", listOf("It becomes transparent", "It turns red", "It faints"), QuizDifficulty.Hard),
        ask("cykada_cykl", "Every how many years do periodical cicadas emerge en masse in the USA?", "Every 13 or 17 years", listOf("Every 2 years", "Every 7 years", "Every 25 years"), QuizDifficulty.Hard),
        ask("dziobak_jad", "What does a male platypus have on its hind legs?", "Venomous spurs", listOf("Webbed membranes with suckers", "Regrowing claws", "Scent glands"), QuizDifficulty.Hard),
        ask("koral_zwierze", "What are corals really?", "Animals", listOf("Plants", "Fungi", "Minerals"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("turritopsis", "Which animal can revert to an earlier life stage and is considered biologically immortal?", "Turritopsis dohrnii jellyfish", listOf("The freshwater hydra polyp", "The Antarctic tardigrade", "The common starfish"), QuizDifficulty.Expert),
        ask("mantis_wzrok", "What makes the mantis shrimp's vision unusual?", "It has a dozen or so types of color receptors", listOf("It sees only in black and white shades", "It has no compound eyes at all", "It sees only in the infrared range"), QuizDifficulty.Expert),
        ask("echidna_nazwa", "Taxonomically, what group does the echidna belong to?", "Monotremes", listOf("Marsupials", "Placental mammals", "Reptiles"), QuizDifficulty.Expert),
        ask("wieloryb_piesn", "Which whale is famous for the most complex mating songs?", "Humpback whale", listOf("Narwhal", "Killer whale", "Porpoise"), QuizDifficulty.Expert),
        ask("pistolet_krewetka", "How does a pistol shrimp stun its prey?", "A shockwave from its snapping claw", listOf("Neurotoxic venom", "An electric discharge", "An ink cloud"), QuizDifficulty.Expert),
        ask("mrowki_liscie", "What do leafcutter ants do with the leaves they bring back?", "Grow fungus on them", listOf("Eat them right away", "Build their nest from them", "Trade them with other colonies"), QuizDifficulty.Expert),
        ask("rekin_polarny", "Which shark can live for several hundred years?", "Greenland shark", listOf("Great white shark", "Whale shark", "Hammerhead shark"), QuizDifficulty.Expert),
        ask("ptak_jadowity", "Which bird is venomous thanks to toxins in its feathers?", "Hooded pitohui", listOf("Andean condor", "Toucan", "Kea parrot"), QuizDifficulty.Expert),
        ask("axolotl_regeneracja", "What else can an axolotl regenerate besides its limbs?", "Parts of its heart and brain", listOf("Only the tip of its tail", "Only patches of its skin", "Nothing beyond its limbs"), QuizDifficulty.Expert),
        ask("zabka_jad", "Which frog is considered the most poisonous land animal?", "Golden poison frog", listOf("Cane toad", "Tree frog", "African clawed frog"), QuizDifficulty.Expert),
        ask("konik_morski", "What's unusual about seahorse reproduction?", "The male carries the pregnancy", listOf("Eggs develop on the seabed", "Young are born live from the female", "It reproduces by splitting"), QuizDifficulty.Expert),
        ask("nietoperz_echo", "What is the phenomenon of bats navigating by reflected sound called?", "Echolocation", listOf("Magnetoreception", "Electrolocation", "Thermolocation"), QuizDifficulty.Expert)
    )
}
