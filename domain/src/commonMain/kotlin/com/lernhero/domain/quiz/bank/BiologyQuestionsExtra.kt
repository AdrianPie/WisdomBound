package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Second batch of biology - added so the player doesn't see the same questions on repeat.
 * Same split as [BiologyQuestions]: from school anatomy to biochemistry for the stubborn.
 */
object BiologyQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "bio2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Biology,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("zeby", "How many teeth does an adult human have, wisdom teeth included?", "32", listOf("28", "36", "24"), QuizDifficulty.Easy),
        ask("krew_kolor", "What gives blood its red color?", "Hemoglobin", listOf("Plasma", "Lymph", "Insulin"), QuizDifficulty.Easy),
        ask("kregoslup", "Which body part protects the spinal cord?", "The spine", listOf("The ribs", "The sternum", "The pelvis"), QuizDifficulty.Easy),
        ask("zmysly", "How many basic senses are traditionally listed in humans?", "Five", listOf("Three", "Seven", "Four"), QuizDifficulty.Easy),
        ask("wlosy_rosna", "What are hair and nails made of?", "Keratin", listOf("Collagen", "Chitin", "Cellulose"), QuizDifficulty.Easy),
        ask("kosc_najdluzsza", "Which human bone is the longest?", "The femur", listOf("The humerus", "The tibia", "The clavicle"), QuizDifficulty.Easy),
        ask("miesien_serce", "What kind of muscle is the heart?", "Cardiac striated muscle", listOf("Smooth muscle", "Skeletal muscle", "A tendon"), QuizDifficulty.Easy),
        ask("korzen", "Which part of a plant absorbs water from the soil?", "The root", listOf("The leaf", "The flower", "The fruit"), QuizDifficulty.Easy),
        ask("oddech_czest", "What is the main breathing muscle called?", "The diaphragm", listOf("The cardiac muscle", "The biceps", "The rectus abdominis"), QuizDifficulty.Easy),
        ask("wzrok_narzad", "Which part of the eye regulates how much light enters?", "The pupil", listOf("The cornea", "The sclera", "The optic nerve"), QuizDifficulty.Easy),
        ask("temp_ciala", "What is normal human body temperature?", "About 98.6 F (36.6 C)", listOf("About 104 F", "About 86 F", "About 93 F"), QuizDifficulty.Easy),
        ask("witamina_c", "Which vitamin is found mainly in citrus fruits?", "Vitamin C", listOf("Vitamin K", "Vitamin B12", "Vitamin D"), QuizDifficulty.Easy),
        ask("bakterie_wielkosc", "What is a bacterium, roughly speaking?", "A single-celled organism", listOf("A virus", "A multicellular fungus", "A protein"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("dna_zasady", "How many different nitrogenous bases occur in DNA?", "Four", listOf("Two", "Five", "Twenty"), QuizDifficulty.Medium),
        ask("rybosom", "What are ribosomes responsible for?", "Protein synthesis", listOf("Cellular respiration", "Fat digestion", "Nuclear division"), QuizDifficulty.Medium),
        ask("watroba_rola", "Which organ detoxifies the body and produces bile?", "The liver", listOf("The spleen", "The pancreas", "The kidney"), QuizDifficulty.Medium),
        ask("krwinki_biale", "What are white blood cells responsible for?", "Immunity", listOf("Oxygen transport", "Blood clotting", "Hormone production"), QuizDifficulty.Medium),
        ask("plytki_krwi", "Which blood component is responsible for clotting?", "Platelets", listOf("Red blood cells", "Lymphocytes", "Plasma"), QuizDifficulty.Medium),
        ask("tarczyca", "Which gland produces thyroxine?", "The thyroid", listOf("The pancreas", "The adrenal gland", "The pituitary"), QuizDifficulty.Medium),
        ask("jelito_cienkie", "Where does most nutrient absorption take place?", "In the small intestine", listOf("In the stomach", "In the large intestine", "In the esophagus"), QuizDifficulty.Medium),
        ask("fotosynteza_produkt", "Which gas is a byproduct of photosynthesis?", "Oxygen", listOf("Nitrogen", "Carbon dioxide", "Methane"), QuizDifficulty.Medium),
        ask("chloroplast", "In which plant cell structure does photosynthesis take place?", "The chloroplast", listOf("The mitochondrion", "The vacuole", "The nucleus"), QuizDifficulty.Medium),
        ask("sciana_kom", "What is the plant cell wall made of?", "Cellulose", listOf("Chitin", "Keratin", "Peptidoglycan"), QuizDifficulty.Medium),
        ask("plyny_ustrojowe", "What percentage of human body mass is water?", "About 60%", listOf("About 20%", "About 90%", "About 40%"), QuizDifficulty.Medium),
        ask("insulina_rola", "What does insulin do in the body?", "Lowers blood sugar levels", listOf("Raises blood pressure", "Increases clotting", "Speeds up breathing"), QuizDifficulty.Medium),
        ask("wirus_zycie", "Why are viruses not considered fully-fledged living organisms?", "They can't reproduce without a host cell", listOf("They have no genetic material", "They can't mutate", "They don't respond to temperature"), QuizDifficulty.Medium),
        ask("antybiotyk", "What do antibiotics act against?", "Bacteria", listOf("Viruses", "Intestinal parasites", "Allergies"), QuizDifficulty.Medium),
        ask("mchy", "What do mosses lack compared to ferns?", "Vascular tissue", listOf("Chlorophyll", "Spores", "Leaves"), QuizDifficulty.Medium),
        ask("lancuch_pokarm", "What sits at the start of a typical food chain?", "A producer", listOf("An apex predator", "A decomposer", "A primary consumer"), QuizDifficulty.Medium),
        ask("symbioza", "What is the relationship called where both organisms benefit?", "Mutualism", listOf("Parasitism", "Competition", "Commensalism"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("golgi", "What is the Golgi apparatus responsible for?", "Processing and sorting proteins", listOf("Producing the cell's energy", "Storing the cell's own DNA", "Carrying out photosynthesis"), QuizDifficulty.Hard),
        ask("lizosom", "What do lysosomes contain?", "Digestive enzymes", listOf("Chlorophyll", "Ribosomal RNA", "Fat stores"), QuizDifficulty.Hard),
        ask("mitoza_faza", "In which phase of mitosis do chromosomes line up on the equatorial plane?", "Metaphase", listOf("Prophase", "Anaphase", "Telophase"), QuizDifficulty.Hard),
        ask("krew_ph", "What is the normal pH of human blood?", "About 7.4", listOf("About 5.5", "About 8.5", "Exactly 7.0"), QuizDifficulty.Hard),
        ask("nefron_czesc", "Where in the nephron does blood filtration occur?", "In the renal glomerulus", listOf("In the collecting duct", "In the loop of Henle", "In the ureter"), QuizDifficulty.Hard),
        ask("hormon_wzrostu", "Which gland secretes growth hormone?", "The pituitary gland", listOf("The thyroid", "The thymus", "The adrenal gland"), QuizDifficulty.Hard),
        ask("adrenalina", "Which organ secretes adrenaline?", "The adrenal glands", listOf("The liver", "The spleen", "The pancreas"), QuizDifficulty.Hard),
        ask("limfocyt_b", "What do B lymphocytes produce?", "Antibodies", listOf("Hemoglobin", "Histamine", "Insulin"), QuizDifficulty.Hard),
        ask("mielina", "What role does the myelin sheath play?", "Speeds up nerve impulse conduction", listOf("Nourishes the neuron", "Produces neurotransmitters", "Protects against toxins"), QuizDifficulty.Hard),
        ask("kodon", "How many nucleotides make up one codon?", "Three", listOf("Two", "Four", "Six"), QuizDifficulty.Hard),
        ask("enzym_hamowanie", "What is the part of an enzyme where a substrate binds called?", "The active site", listOf("The regulatory domain", "The prosthetic group", "The allosteric site"), QuizDifficulty.Hard),
        ask("osmoza", "What moves during osmosis?", "Water across a semipermeable membrane", listOf("Mineral salts along a nerve fibre", "Proteins out through the cell wall", "Oxygen bubbles through the cytoplasm"), QuizDifficulty.Hard),
        ask("hemofilia", "What is hemophilia associated with?", "A blood clotting disorder", listOf("An excess of red blood cells", "A lack of insulin", "Nerve damage"), QuizDifficulty.Hard),
        ask("gatunek_def", "What determines membership in a single species, biologically?", "The ability to produce fertile offspring", listOf("A close similarity in appearance", "Living within the same range", "Feeding on exactly the same diet"), QuizDifficulty.Hard),
        ask("tkanki_typy", "How many basic tissue types are distinguished in humans?", "Four", listOf("Two", "Six", "Eight"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("krebs_miejsce", "Where exactly does the Krebs cycle take place?", "In the mitochondrial matrix", listOf("In the cytosol", "On the nuclear membrane", "In the endoplasmic reticulum"), QuizDifficulty.Expert),
        ask("atp_zysk", "Roughly how many ATP molecules does the full oxidation of one glucose molecule yield?", "About 30-32", listOf("About 2", "About 12", "About 100"), QuizDifficulty.Expert),
        ask("telomer", "What protects chromosome ends from shortening?", "Telomeres", listOf("Centromeres", "Histones", "Introns"), QuizDifficulty.Expert),
        ask("apoptoza", "What is apoptosis?", "Programmed cell death", listOf("Uncontrolled division", "Tissue inflammation", "A point mutation"), QuizDifficulty.Expert),
        ask("prion", "What is a prion?", "An infectious, misfolded protein", listOf("An RNA virus", "An anaerobic bacterium", "A DNA fragment"), QuizDifficulty.Expert),
        ask("krzyzowka_mendel", "What phenotype ratio does a cross of two heterozygotes give in single-gene inheritance?", "3:1", listOf("1:1", "9:3:3:1", "2:2"), QuizDifficulty.Expert),
        ask("chemiosmoza", "Who described the chemiosmotic theory of ATP production?", "Peter Mitchell", listOf("Hans Krebs", "Otto Warburg", "Fritz Lipmann"), QuizDifficulty.Expert),
        ask("intron", "What are introns?", "Non-coding gene segments spliced out of pre-mRNA", listOf("Protein-coding fragments kept in the mRNA", "Histone proteins the DNA is wound around", "Repair enzymes that patch broken strands"), QuizDifficulty.Expert),
        ask("halden", "What is the state of allele frequency equilibrium in a large population called?", "Hardy-Weinberg equilibrium", listOf("Genetic drift", "The founder effect", "Nash equilibrium"), QuizDifficulty.Expert),
        ask("stromatolit", "Which organisms are credited with putting oxygen into Earth's atmosphere?", "Cyanobacteria", listOf("Fungi", "Methanogenic archaea", "Diatoms"), QuizDifficulty.Expert),
        ask("crispr", "What is the CRISPR-Cas9 technique used for?", "Precise genome editing", listOf("Sequencing proteins", "Growing stem cells", "Brain imaging"), QuizDifficulty.Expert),
        ask("krew_rh", "When does an Rh serological conflict occur?", "When an Rh- mother carries an Rh+ baby", listOf("When an Rh+ mother carries an Rh- baby", "When both parents are Rh-", "When the parents' blood types differ"), QuizDifficulty.Expert)
    )
}
