package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Second batch of mythology - Greece, Rome, the North, Egypt and Slavic myths. */
object MythologyQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "mit2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Mythology,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("afrodyta", "Which Greek goddess presided over love and beauty?", "Aphrodite", listOf("Hera", "Artemis", "Demeter"), QuizDifficulty.Easy),
        ask("ares", "Which Greek god was the god of war?", "Ares", listOf("Apollo", "Hermes", "Hephaestus"), QuizDifficulty.Easy),
        ask("hermes", "Which god was the messenger of the Greek gods?", "Hermes", listOf("Ares", "Dionysus", "Poseidon"), QuizDifficulty.Easy),
        ask("olimp", "Where did the Greek gods live?", "On Olympus", listOf("In Delphi", "On Crete", "In Sparta"), QuizDifficulty.Easy),
        ask("trojkonik", "What attribute did Poseidon carry?", "A trident", listOf("A thunderbolt", "A lyre", "A shield with a Gorgon"), QuizDifficulty.Easy),
        ask("zeus_bron", "What weapon did Zeus wield?", "A thunderbolt", listOf("A trident", "A flaming sword", "A bow"), QuizDifficulty.Easy),
        ask("kon_trojanski", "What did the Greeks use to capture Troy?", "A wooden horse", listOf("A battering ram", "A tunnel", "The queen's betrayal"), QuizDifficulty.Easy),
        ask("syreny", "What did sirens lure sailors with?", "Singing", listOf("Gold", "Lighthouse light", "Scent"), QuizDifficulty.Easy),
        ask("centaur", "What was a centaur?", "Half man, half horse", listOf("Half man, half bull", "Half bird, half lion", "A giant one-eyed being"), QuizDifficulty.Easy),
        ask("cyklop", "What set a cyclops apart?", "One eye", listOf("Wings", "A snake's tail", "Three heads"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("hera_rola", "What was Hera to Zeus?", "His wife", listOf("Only his sister", "His daughter", "His mother"), QuizDifficulty.Medium),
        ask("hefajstos", "What did Hephaestus work with?", "Blacksmithing and fire", listOf("Sailing", "Poetry", "Hunting"), QuizDifficulty.Medium),
        ask("artemida", "What was Artemis the patron of?", "The hunt and the moon", listOf("The hearth", "Trade", "The vine"), QuizDifficulty.Medium),
        ask("dionizos", "What was Dionysus the god of?", "Wine and revelry", listOf("War", "Blacksmithing", "The sea"), QuizDifficulty.Medium),
        ask("prometeusz", "What did Prometheus steal from the gods for humanity?", "Fire", listOf("Gold", "Nectar", "Ambrosia"), QuizDifficulty.Medium),
        ask("syzyf", "What punishment was Sisyphus sentenced to?", "Endlessly pushing a boulder uphill", listOf("Eternal hunger and thirst", "Being chained to a rock", "Wandering a labyrinth forever"), QuizDifficulty.Medium),
        ask("tantal", "What was Tantalus's torment?", "Water and fruit slipping away whenever he reached", listOf("Carrying the sky on his shoulders", "Being devoured by an eagle", "Rolling a boulder"), QuizDifficulty.Medium),
        ask("atlas", "What did Atlas carry?", "The vault of the sky", listOf("Mount Olympus", "The Earth", "The gates of Hades"), QuizDifficulty.Medium),
        ask("jazon", "What was Jason searching for with the Argonauts?", "The Golden Fleece", listOf("The Holy Grail", "The apples of the Hesperides", "Medusa's hair"), QuizDifficulty.Medium),
        ask("tezeusz", "Who killed the Minotaur?", "Theseus", listOf("Heracles", "Perseus", "Achilles"), QuizDifficulty.Medium),
        ask("perseusz", "Who killed Medusa?", "Perseus", listOf("Theseus", "Odysseus", "Jason"), QuizDifficulty.Medium),
        ask("loki", "Who was the Norse trickster god?", "Loki", listOf("Baldur", "Heimdall", "Tyr"), QuizDifficulty.Medium),
        ask("walhalla", "Who was admitted to Valhalla?", "Warriors fallen in battle", listOf("All the dead", "Priests", "Kings"), QuizDifficulty.Medium),
        ask("ra", "Which Egyptian god ruled over the sun?", "Ra", listOf("Osiris", "Set", "Horus"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("hades_rzeka", "Which river had to be crossed to enter the land of the dead?", "The Styx", listOf("The Nile", "The Tiber", "The Euphrates"), QuizDifficulty.Hard),
        ask("charon", "Who ferried souls across the river of the dead?", "Charon", listOf("Hermes", "Thanatos", "Cerberus"), QuizDifficulty.Hard),
        ask("mojry", "How many Fates (Moirai), spinners of human destiny, were there?", "Three", listOf("Two", "Four", "Nine"), QuizDifficulty.Hard),
        ask("muzy", "How many Muses were there in Greek mythology?", "Nine", listOf("Three", "Seven", "Twelve"), QuizDifficulty.Hard),
        ask("herakles_prace", "How many labors did Heracles complete?", "Twelve", listOf("Seven", "Ten", "Fifteen"), QuizDifficulty.Hard),
        ask("nemezis", "What was Nemesis the goddess of?", "Vengeance and just retribution", listOf("Wisdom", "Fertility", "Sleep"), QuizDifficulty.Hard),
        ask("hypnos", "Who was the Greek god of sleep?", "Hypnos", listOf("Morpheus", "Thanatos", "Nyx"), QuizDifficulty.Hard),
        ask("odyn_ofiara", "What did Odin sacrifice for wisdom?", "One eye", listOf("A hand", "His hair", "His spear"), QuizDifficulty.Hard),
        ask("fenrir", "What was Fenrir in Norse mythology?", "A giant wolf", listOf("A sea serpent", "Odin's raven", "A frost giant"), QuizDifficulty.Hard),
        ask("jormungand", "What was Jormungand?", "A serpent encircling the world", listOf("A dragon guarding gold", "Odin's steed", "A god of the sea"), QuizDifficulty.Hard),
        ask("sleipnir", "How many legs did Sleipnir, Odin's horse, have?", "Eight", listOf("Four", "Six", "Twelve"), QuizDifficulty.Hard),
        ask("anubis_rola", "What did Anubis do?", "Embalming and guiding the dead", listOf("Waging war", "Overseeing harvests", "Navigating the Nile"), QuizDifficulty.Hard),
        ask("ozyrys", "Who killed Osiris according to Egyptian myth?", "Set", listOf("Horus", "Ra", "Thoth"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("hekate", "What was Hecate the patron of?", "Magic and crossroads", listOf("Harvests", "Naval warfare", "Blacksmithing"), QuizDifficulty.Expert),
        ask("erynie", "Who were the Furies (Erinyes)?", "Goddesses of vengeance pursuing wrongdoers", listOf("Spring nymphs", "Goddesses of fertility", "Guardians of Olympus"), QuizDifficulty.Expert),
        ask("hesperydy", "What did the Hesperides guard?", "Golden apples", listOf("The Golden Fleece", "Prometheus's fire", "The gates of Hades"), QuizDifficulty.Expert),
        ask("chimera", "What was the Chimera made up of?", "A lion, a goat and a snake", listOf("A horse and an eagle", "A bull and a man", "A lion and an eagle"), QuizDifficulty.Expert),
        ask("grif", "What was a griffin made up of?", "A lion and an eagle", listOf("A horse and a fish", "A goat and a snake", "A bull and bat wings"), QuizDifficulty.Expert),
        ask("mimir", "Who guarded the well of wisdom in Norse mythology?", "Mimir", listOf("Heimdall", "Bragi", "Ullr"), QuizDifficulty.Expert),
        ask("bifrost", "What was Bifrost?", "The rainbow bridge to Asgard", listOf("Odin's sword", "The ship of the dead", "Thor's shield"), QuizDifficulty.Expert),
        ask("valkirie", "What did the Valkyries do?", "Chose the slain for Valhalla", listOf("Guarded the Nibelung treasure", "Brewed the mead of poetry", "Watched over Bifrost"), QuizDifficulty.Expert),
        ask("swietowit", "How many faces did the Slavic god Svetovid have?", "Four", listOf("Two", "Three", "Six"), QuizDifficulty.Expert),
        ask("weles", "What was the Slavic god Veles the god of?", "The underworld, cattle and oaths", listOf("War and thunder", "The sun and fire", "Wind and sailing"), QuizDifficulty.Expert),
        ask("thot", "What was the Egyptian god Thoth the patron of?", "Writing and knowledge", listOf("War", "Harvests", "Sailing"), QuizDifficulty.Expert),
        ask("izyda", "What was Isis to Osiris?", "His sister and wife", listOf("His daughter", "His mother", "His enemy"), QuizDifficulty.Expert)
    )
}
