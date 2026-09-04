package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Third batch of biology - genetics, ecology and anatomy not yet covered. */
object BiologyQuestionsExtra2 {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "bio3_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Biology,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("oko_ile", "How many eyes does a human have?", "Two", listOf("One", "Three", "Four"), QuizDifficulty.Easy),
        ask("nos_funkcja", "Which sense is the nose responsible for?", "Smell", listOf("Taste", "Hearing", "Touch"), QuizDifficulty.Easy),
        ask("jezyk_smak", "Which organ is responsible for taste?", "The tongue", listOf("The nose", "The ear", "The skin"), QuizDifficulty.Easy),
        ask("ucho_funkcja", "Besides hearing, what is the ear responsible for?", "Balance", listOf("Smell", "Taste", "Sight"), QuizDifficulty.Easy),
        ask("roslina_potrzebuje", "What does a plant need for photosynthesis?", "Light", listOf("Darkness", "Salt", "Frost"), QuizDifficulty.Easy),
        ask("owad_nogi", "How many legs does an insect have?", "Six", listOf("Four", "Eight", "Ten"), QuizDifficulty.Easy),
        ask("skorpion_nogi", "How many legs does a scorpion have?", "Eight", listOf("Six", "Ten", "Four"), QuizDifficulty.Easy),
        ask("kregowiec_kregoslup", "What do all vertebrates have?", "A spine", listOf("Gills", "Wings", "Shells"), QuizDifficulty.Easy),
        ask("bezkregowiec_przyklad", "Which of these animals is an invertebrate?", "Earthworm", listOf("Sparrow", "Frog", "Fish"), QuizDifficulty.Easy),
        ask("ssak_karmienie", "What do baby mammals feed on?", "Milk", listOf("Nectar", "Plant sap", "Water"), QuizDifficulty.Easy),
        ask("plyn_krwi", "What is the liquid part of blood called?", "Plasma", listOf("Lymph", "Mucus", "Bile"), QuizDifficulty.Easy),
        ask("roslina_oddycha", "What does a plant use to exchange gases?", "Stomata in its leaves", listOf("Lungs", "Gills", "Roots only"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("gruczoly_dokrewne", "What does the endocrine system deal with?", "Secreting hormones", listOf("Digesting food", "Filtering blood", "Producing red blood cells only"), QuizDifficulty.Medium),
        ask("melanina", "What is melanin in the skin responsible for?", "Skin color and UV protection", listOf("Sensing pain", "Producing sweat", "Skin elasticity"), QuizDifficulty.Medium),
        ask("gruczoly_potowe", "What are sweat glands for?", "Regulating body temperature", listOf("Digesting fats", "Producing hormones", "Strengthening bones"), QuizDifficulty.Medium),
        ask("kregoslup_odcinki", "How many sections does the human spine have?", "Five", listOf("Three", "Four", "Seven"), QuizDifficulty.Medium),
        ask("uklad_limfatyczny", "What is the lymphatic system responsible for?", "Immunity and fluid drainage", listOf("Producing urine", "Digesting proteins", "Secreting insulin"), QuizDifficulty.Medium),
        ask("genotyp_fenotyp", "How does phenotype differ from genotype?", "Phenotype is visible traits, genotype is the genetic code", listOf("Phenotype only applies to plants", "Genotype changes during life", "They're synonyms"), QuizDifficulty.Medium),
        ask("gatunek_inwazyjny", "What is an invasive species?", "A foreign species that threatens native ecosystems", listOf("An extinct species", "A farmed species", "A protected species"), QuizDifficulty.Medium),
        ask("nisza_ekologiczna", "What is an ecological niche?", "A species' role in an ecosystem", listOf("A wintering site", "A protected area", "A soil type"), QuizDifficulty.Medium),
        ask("las_pieter", "How many main layers are distinguished in a typical forest?", "Four", listOf("Two", "Three", "Six"), QuizDifficulty.Medium),
        ask("mutacja_definicja", "What is a genetic mutation?", "A permanent change in genetic material", listOf("A change in animal behavior", "An infectious disease", "A type of diet"), QuizDifficulty.Medium),
        ask("gatunek_endemiczny", "What is an endemic species?", "One found only in a specific, limited area", listOf("An extinct species", "A farmed species", "Any protected species"), QuizDifficulty.Medium),
        ask("bioroznorodnosc", "What does biodiversity mean?", "The variety of life forms in a given area", listOf("The number of predators", "The size of a forest", "The amount of rainfall"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("epigenetyka", "What does epigenetics study?", "Changes in gene expression without changing the DNA sequence", listOf("Only sex inheritance", "Protein structure", "Vegetative reproduction"), QuizDifficulty.Hard),
        ask("homeostaza", "What is homeostasis?", "Maintaining stable internal conditions in an organism", listOf("A type of autoimmune disease", "The aging process", "A type of cell division"), QuizDifficulty.Hard),
        ask("gatunek_kluczowy", "What is a keystone species in an ecosystem?", "One with a disproportionately large effect on the whole community", listOf("Always the most numerous", "Always an apex predator", "Only found in the sea"), QuizDifficulty.Hard),
        ask("sukcesja_ekologiczna", "What is ecological succession?", "The gradual change of communities over time", listOf("A sudden climate shift", "Species extinction", "Animal migration"), QuizDifficulty.Hard),
        ask("konwergencja_ewolucyjna", "What is convergent evolution?", "Different species independently developing similar traits", listOf("Interbreeding of species", "A shared ancestor of two species", "A species dying out"), QuizDifficulty.Hard),
        ask("bariera_krew_mozg", "What is the blood-brain barrier for?", "Protects the brain from harmful substances in the blood", listOf("Produces cerebrospinal fluid", "Regulates blood pressure throughout the body", "Controls sleep"), QuizDifficulty.Hard),
        ask("plazmid", "What is a plasmid in bacteria?", "A small, circular piece of DNA outside the chromosome", listOf("A protective cell coating", "A type of flagellum", "A membrane protein"), QuizDifficulty.Hard),
        ask("chemotaksja", "What is chemotaxis in single-celled organisms?", "Movement toward or away from a chemical stimulus", listOf("Random movement", "Cell division", "A reaction to light only"), QuizDifficulty.Hard),
        ask("ptaki_migracja_nawigacja", "What do migratory birds rely on for navigation over long distances?", "Among other things, Earth's magnetic field and star position", listOf("Smell alone", "Hearing alone", "Air temperature"), QuizDifficulty.Hard),
        ask("mikrobiom", "What is the human gut microbiome?", "The community of microorganisms living in the intestines", listOf("A type of digestive enzyme", "The stomach's mucous layer", "A type of white blood cell"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("horyzontalny_transfer_genow", "What is horizontal gene transfer?", "Passing genetic material without reproduction", listOf("Inheritance from both parents", "A point mutation", "Cell cloning"), QuizDifficulty.Expert),
        ask("apopktoza_kaskada", "Which enzymes do most of the work during apoptosis?", "Caspases", listOf("Lipases", "Amylases", "Digestive proteases"), QuizDifficulty.Expert),
        ask("optogenetyka", "What is optogenetics used for?", "Controlling neuron activity with light", listOf("Imaging bones", "Producing insulin", "Cloning organisms"), QuizDifficulty.Expert),
        ask("telomeraza", "What does the enzyme telomerase do?", "Rebuilds telomeres at chromosome ends", listOf("Cuts DNA into fragments", "Links amino acids into a protein", "Breaks down RNA"), QuizDifficulty.Expert),
        ask("kod_genetyczny_uniwersalnosc", "What does the universality of the genetic code mean?", "Most organisms use the same codons for the same amino acids", listOf("All genes are identical", "DNA only occurs in animals", "Mutations never happen"), QuizDifficulty.Expert),
        ask("efekt_zalozyciela", "What is the founder effect in population genetics?", "A limited gene pool in a new population founded by few individuals", listOf("Natural sexual selection", "Climate drift", "A hybridization phenomenon"), QuizDifficulty.Expert),
        ask("bioluminescencja_mechanizm", "What accounts for bioluminescence in most organisms?", "A luciferase reaction with luciferin", listOf("Reflection of moonlight", "Fluorescence of photosynthetic proteins", "Electric discharges"), QuizDifficulty.Expert),
        ask("crispr_pochodzenie", "Where does the CRISPR-Cas system used for gene editing today originally come from?", "Bacteria's natural defense mechanism against viruses", listOf("A lab-designed protein", "Plant cells", "The HIV virus"), QuizDifficulty.Expert),
        ask("neuroplastycznosc", "What is the brain's neuroplasticity?", "The brain's ability to form new neural connections", listOf("Neurons' ability to divide", "The brain's resistance to injury", "Regeneration of the whole brain"), QuizDifficulty.Expert),
        ask("dobor_plciowy", "How does sexual selection differ from natural selection?", "It favors traits that boost mating success, not necessarily survival", listOf("It only applies to plants", "It's always random", "It doesn't change populations"), QuizDifficulty.Expert)
    )
}
