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
        ask("ucho_funkcja", "Besides hearing, what is the ear responsible for?", "Balance", listOf("Smell", "Taste", "Sight"), QuizDifficulty.Easy),
        ask("owad_nogi", "How many legs does an insect have?", "Six", listOf("Four", "Eight", "Ten"), QuizDifficulty.Easy),
        ask("skorpion_nogi", "How many legs does a scorpion have?", "Eight", listOf("Six", "Ten", "Four"), QuizDifficulty.Easy),
        ask("bezkregowiec_przyklad", "Which of these animals is an invertebrate?", "Earthworm", listOf("Sparrow", "Frog", "Fish"), QuizDifficulty.Easy),
        ask("plyn_krwi", "What is the liquid part of blood called?", "Plasma", listOf("Lymph", "Mucus", "Bile"), QuizDifficulty.Easy),
        ask("roslina_oddycha", "What does a plant use to exchange gases?", "Stomata in its leaves", listOf("Lungs", "Gills", "Roots only"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("gruczoly_dokrewne", "What does the endocrine system deal with?", "Secreting hormones", listOf("Digesting food", "Filtering blood", "Producing red blood cells only"), QuizDifficulty.Medium),
        ask("gruczoly_potowe", "What are sweat glands for?", "Regulating body temperature", listOf("Digesting fats", "Producing hormones", "Strengthening bones"), QuizDifficulty.Medium),
        ask("kregoslup_odcinki", "How many sections does the human spine have?", "Five", listOf("Three", "Four", "Seven"), QuizDifficulty.Medium),
        ask("uklad_limfatyczny", "What is the lymphatic system responsible for?", "Immunity and fluid drainage", listOf("Producing urine", "Digesting proteins", "Secreting insulin"), QuizDifficulty.Medium),
        ask("genotyp_fenotyp", "How does phenotype differ from genotype?", "Phenotype is visible traits, genotype is the genetic code", listOf("The phenotype applies only to plants and fungi", "The genotype keeps changing during life", "They are two names for the same thing"), QuizDifficulty.Medium),
        ask("ropucha_aga", "Which animal, brought to Australia in 1935 to protect sugar cane, became a plague itself?", "The cane toad", listOf("The rabbit", "The dingo", "The emu"), QuizDifficulty.Medium),
        ask("nisza_ekologiczna", "What is an ecological niche?", "A species' role in an ecosystem", listOf("A site used only for wintering", "An area protected by law", "A particular type of soil"), QuizDifficulty.Medium),
        ask("uv_dna", "Which part of sunlight damages DNA in skin cells and causes sunburn?", "Ultraviolet radiation", listOf("Infrared radiation", "Visible light", "Radio waves"), QuizDifficulty.Medium),
        ask("gatunek_endemiczny", "What is an endemic species?", "One found only in a specific, limited area", listOf("One that died out within living memory", "One bred on farms for its meat", "One protected by international law"), QuizDifficulty.Medium),
        ask("las_gatunki", "Which land habitat holds the greatest number of species?", "Tropical rainforest", listOf("Boreal forest (taiga)", "Temperate grassland", "Tundra"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("epigenetyka", "What does epigenetics study?", "Changes in gene expression without changing the DNA sequence", listOf("The inheritance of sex from the parents alone", "The three-dimensional folding of protein chains", "Reproduction without seeds or spores"), QuizDifficulty.Hard),
        ask("homeostaza", "What is homeostasis?", "Maintaining stable internal conditions in an organism", listOf("A disease in which the body attacks itself", "The slow process of growing old", "A form of cell division in embryos"), QuizDifficulty.Hard),
        ask("gatunek_kluczowy", "What is a keystone species in an ecosystem?", "One with a disproportionately large effect on the whole community", listOf("Always the most numerous species living in that area", "Always the predator at the very top of the chain", "One that is found only in the open sea"), QuizDifficulty.Hard),
        ask("sukcesja_ekologiczna", "What is ecological succession?", "The gradual change of communities over time", listOf("A sudden shift in the regional climate", "The dying out of a whole species", "The seasonal migration of animals"), QuizDifficulty.Hard),
        ask("konwergencja_ewolucyjna", "What is convergent evolution?", "Different species independently developing similar traits", listOf("Two closely related species interbreeding freely", "Two species descending from one shared ancestor", "A species dying out and leaving no descendants"), QuizDifficulty.Hard),
        ask("leki_mozg", "Why do many drugs that work elsewhere in the body fail to treat brain disease?", "They cannot cross the blood-brain barrier", listOf("The brain has no blood supply", "The brain destroys every drug at once", "Brain cells have no receptors"), QuizDifficulty.Hard),
        ask("plazmid", "What is a plasmid in bacteria?", "A small, circular piece of DNA outside the chromosome", listOf("A protective coating around the whole cell", "A whip-like tail used for swimming", "A protein embedded in the membrane"), QuizDifficulty.Hard),
        ask("chemotaksja", "What is chemotaxis in single-celled organisms?", "Movement toward or away from a chemical stimulus", listOf("Movement that has no direction at all", "The splitting of the cell in two", "A response to light and nothing else"), QuizDifficulty.Hard),
        ask("ptaki_migracja_nawigacja", "What do migratory birds rely on for navigation over long distances?", "Among other things, Earth's magnetic field and star position", listOf("Their sense of smell alone, and nothing besides", "Their hearing alone, and nothing besides", "Changes in the temperature of the air"), QuizDifficulty.Hard),
        ask("mikrobiom", "What is the human gut microbiome?", "The community of microorganisms living in the intestines", listOf("A digestive enzyme made in the wall of the gut", "The mucous layer that lines the stomach", "A kind of white cell found in the gut"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("horyzontalny_transfer_genow", "What is horizontal gene transfer?", "Passing genetic material without reproduction", listOf("Inheriting genes from both parents at once", "A single letter changing in the DNA", "Making an exact copy of a whole cell"), QuizDifficulty.Expert),
        ask("apopktoza_kaskada", "Which enzymes do most of the work during apoptosis?", "Caspases", listOf("Lipases", "Amylases", "Digestive proteases"), QuizDifficulty.Expert),
        ask("optogenetyka", "What is optogenetics used for?", "Controlling neuron activity with light", listOf("Imaging bone through the soft tissue", "Producing insulin in modified yeast", "Cloning a whole animal from one cell"), QuizDifficulty.Expert),
        ask("telomeraza", "What does the enzyme telomerase do?", "Rebuilds telomeres at chromosome ends", listOf("Cuts DNA into fragments", "Links amino acids into a protein", "Breaks down RNA"), QuizDifficulty.Expert),
        ask("kod_genetyczny_uniwersalnosc", "What does the universality of the genetic code mean?", "Most organisms use the same codons for the same amino acids", listOf("Every organism carries exactly the same genes", "DNA is found in animals but not in plants", "Mutations never occur in the coding parts"), QuizDifficulty.Expert),
        ask("efekt_zalozyciela", "What is the founder effect in population genetics?", "A limited gene pool in a new population founded by few individuals", listOf("Sexual selection acting inside a very large population", "A slow drift in climate over many generations", "Two nearby species producing hybrid offspring"), QuizDifficulty.Expert),
        ask("bioluminescencja_mechanizm", "What accounts for bioluminescence in most organisms?", "A luciferase reaction with luciferin", listOf("Reflection of moonlight", "Fluorescence of photosynthetic proteins", "Electric discharges"), QuizDifficulty.Expert),
        ask("crispr_pochodzenie", "Where does the CRISPR-Cas system used for gene editing today originally come from?", "Bacteria's natural defense mechanism against viruses", listOf("A protein designed from scratch in a laboratory", "The chloroplasts found inside plant cells", "A protein borrowed from the HIV virus"), QuizDifficulty.Expert),
        ask("hipokamp_pamiec", "Which brain structure turned out to be essential for forming new long-term memories?", "The hippocampus", listOf("The cerebellum", "The amygdala", "The thalamus"), QuizDifficulty.Expert),
        ask("dobor_plciowy", "How does sexual selection differ from natural selection?", "It favors traits that boost mating success, not necessarily survival", listOf("It works purely at random, with no effect on the species", "It applies to plants and fungi but never to animals", "It never changes the make-up of a population"), QuizDifficulty.Expert)
    )
}
