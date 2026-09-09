package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Biology and the human body. Adding a question is one entry in the list - nothing more.
 */
object BiologyQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "bio_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Biology,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask(
            id = "serce",
            prompt = "Which organ pumps blood through the human body?",
            correct = "Heart",
            wrong = listOf("Liver", "Lungs", "Kidneys"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "fotosynteza",
            prompt = "What is the process called in which plants make food from light?",
            correct = "Photosynthesis",
            wrong = listOf("Fermentation", "Transpiration", "Cellular respiration"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "skora",
            prompt = "Which human organ is the largest?",
            correct = "Skin",
            wrong = listOf("Liver", "Lungs", "Small intestine"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "komorka",
            prompt = "What is the smallest building block of an organism called?",
            correct = "Cell",
            wrong = listOf("Tissue", "Organ", "Atom"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "krew_tlen",
            prompt = "What do red blood cells carry?",
            correct = "Oxygen",
            wrong = listOf("Sugar", "Proteins", "Vitamins"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kosci",
            prompt = "How many bones does an adult human skeleton have?",
            correct = "206",
            wrong = listOf("152", "300", "180"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Medium ------------------------------ */
        ask(
            id = "mitochondrium",
            prompt = "Which part of a cell is called its powerhouse?",
            correct = "Mitochondrion",
            wrong = listOf("Cell nucleus", "Ribosome", "Golgi apparatus"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "chromosomy",
            prompt = "How many chromosomes does a healthy human cell have?",
            correct = "46",
            wrong = listOf("23", "48", "44"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "chlorofil",
            prompt = "Which pigment gives plants their green color?",
            correct = "Chlorophyll",
            wrong = listOf("Carotene", "Melanin", "Hemoglobin"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "insulina",
            prompt = "Which organ produces insulin?",
            correct = "Pancreas",
            wrong = listOf("Liver", "Kidneys", "Thyroid"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "darwin",
            prompt = "Who formulated the theory of evolution by natural selection?",
            correct = "Charles Darwin",
            wrong = listOf("Gregor Mendel", "Louis Pasteur", "Carl Linnaeus"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "jamy_serca",
            prompt = "How many chambers does the human heart have?",
            correct = "4",
            wrong = listOf("2", "3", "6"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "witamina_d",
            prompt = "Which vitamin does the body produce from sunlight?",
            correct = "Vitamin D",
            wrong = listOf("Vitamin C", "Vitamin A", "Vitamin B12"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "zoladek",
            prompt = "Which acid helps digest food in the stomach?",
            correct = "Hydrochloric acid",
            wrong = listOf("Sulfuric acid", "Acetic acid", "Nitric acid"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Hard ------------------------------- */
        ask(
            id = "mejoza",
            prompt = "What is the cell division that produces sex cells called?",
            correct = "Meiosis",
            wrong = listOf("Mitosis", "Cytokinesis", "Apoptosis"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "mendel",
            prompt = "Who is considered the father of genetics for his research on peas?",
            correct = "Gregor Mendel",
            wrong = listOf("Charles Darwin", "James Watson", "Francis Crick"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "mozdzek",
            prompt = "Which part of the brain is responsible for balance and movement coordination?",
            correct = "Cerebellum",
            wrong = listOf("Cerebral cortex", "Medulla oblongata", "Hypothalamus"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "dawca",
            prompt = "Which blood type is the universal donor?",
            correct = "O Rh-",
            wrong = listOf("AB Rh+", "A Rh+", "B Rh-"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "neuron",
            prompt = "What is the place where one neuron passes a signal to another called?",
            correct = "Synapse",
            wrong = listOf("Axon", "Dendrite", "Myelin"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "nefron",
            prompt = "What is the kidney's basic filtration unit called?",
            correct = "Nephron",
            wrong = listOf("Alveolus", "Vesicle", "Bile duct"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Expert ---------------------------- */
        ask(
            id = "amylaza",
            prompt = "Which enzyme in saliva breaks down starch?",
            correct = "Amylase",
            wrong = listOf("Pepsin", "Lipase", "Trypsin"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "mrna",
            prompt = "Which type of RNA carries genetic information from the nucleus to the ribosome?",
            correct = "mRNA",
            wrong = listOf("tRNA", "rRNA", "siRNA"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "ektoderma",
            prompt = "Which germ layer does the nervous system develop from?",
            correct = "Ectoderm",
            wrong = listOf("Mesoderm", "Endoderm", "Notochord"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "atp",
            prompt = "What is the compound that's the cell's main energy carrier called?",
            correct = "ATP",
            wrong = listOf("DNA", "NADH", "Glucose"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "krebs",
            prompt = "What is the Krebs cycle also known as?",
            correct = "The citric acid cycle",
            wrong = listOf("The urea cycle", "The Calvin cycle", "Glycolysis"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
