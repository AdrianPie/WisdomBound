package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Third batch of mythology - Egypt, Slavic myths, the Americas and Asia not yet covered. */
object MythologyQuestionsExtra2 {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "mit3_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Mythology,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("apollo_domena", "What was Apollo the god of?", "The sun, music and prophecy", listOf("War", "The sea", "Blacksmithing"), QuizDifficulty.Easy),
        ask("posejdon_domena", "What was Poseidon the god of?", "The sea", listOf("The sun", "War", "Love"), QuizDifficulty.Easy),
        ask("demeter_domena", "What was Demeter the goddess of?", "Harvest and fertility", listOf("War", "The sea", "The underworld"), QuizDifficulty.Easy),
        ask("herakles_ojciec", "Who was Heracles's divine father?", "Zeus", listOf("Poseidon", "Ares", "Hades"), QuizDifficulty.Easy),
        ask("pegaz", "What was Pegasus?", "A winged horse", listOf("A winged lion", "A winged dog", "A winged snake"), QuizDifficulty.Easy),
        ask("nimfy", "What were nymphs in Greek mythology?", "Nature spirits, e.g. of forests and springs", listOf("Goddesses of war", "Queens of Olympus", "Only daughters of Zeus and Hera"), QuizDifficulty.Easy),
        ask("horus", "Which animal/symbol often accompanies the Egyptian god Horus?", "A falcon", listOf("A cat", "A crocodile", "A jackal"), QuizDifficulty.Easy),
        ask("bastet", "Which animal did the Egyptian goddess Bastet represent?", "A cat", listOf("A crocodile", "A falcon", "A snake"), QuizDifficulty.Easy),
        ask("sfinks_egipt", "What is the Great Sphinx of Giza?", "A statue with a lion's body and a human head", listOf("A pyramid raised over a pharaoh's tomb", "A temple built for the god of the sun", "A walled garden beside the royal palace"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("ikar_lot", "What were Icarus's wings made of?", "Feathers and wax", listOf("Dragon skin", "Metal", "Leaves"), QuizDifficulty.Medium),
        ask("dedal", "Who was Daedalus in Greek mythology?", "An inventor, builder of the labyrinth on Crete", listOf("A god of war worshipped across Greece", "A king who ruled the city of Athens", "A hero who fought outside the walls of Troy"), QuizDifficulty.Medium),
        ask("orfeusz", "Who did Orpheus descend into the underworld for?", "His wife Eurydice", listOf("His mother", "His friend Heracles", "The Golden Fleece"), QuizDifficulty.Medium),
        ask("narcyz", "What happened to Narcissus in the Greek myth?", "He fell in love with his own reflection", listOf("He was turned into a laurel tree", "He was taken up among the gods", "He was killed in single combat"), QuizDifficulty.Medium),
        ask("echo_nimfa", "What happened to the nymph Echo in mythology?", "She could only repeat others' words", listOf("She turned into a flower", "She became a goddess", "She fled to Olympus"), QuizDifficulty.Medium),
        ask("midas", "What gift (and curse) did King Midas receive?", "Turning everything he touched to gold", listOf("Immortality", "Invisibility", "Eternally youthful looks"), QuizDifficulty.Medium),
        ask("bastet_rola", "What role does Bastet play in Egyptian mythology?", "Goddess of home and fertility", listOf("Goddess of war only", "God of the underworld", "God of the Nile"), QuizDifficulty.Medium),
        ask("sobek", "Which animal does the Egyptian god Sobek represent?", "A crocodile", listOf("A falcon", "A jackal", "A scarab"), QuizDifficulty.Medium),
        ask("quetzalcoatl", "Who was Quetzalcoatl in Aztec mythology?", "The feathered serpent god", listOf("The rain god only", "A human king", "A mountain spirit"), QuizDifficulty.Medium),
        ask("inti", "What was Inti the god of in Inca mythology?", "The sun", listOf("The moon", "The sea", "The earth"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("rusalka_slowianska", "What was a rusalka in Slavic beliefs?", "A water spirit, often the soul of a drowned girl", listOf("A goddess of fertile fields and the harvest", "A god who ruled the depths of the forest", "A friendly spirit that guarded the house"), QuizDifficulty.Hard),
        ask("domowik", "What was a domovoi in Slavic beliefs?", "A guardian spirit of the home and household", listOf("An evil demon that haunted the deep forest", "A goddess who watched over the harvest", "A spirit that rode in with the storms"), QuizDifficulty.Hard),
        ask("perun", "What was the Slavic god Perun the god of?", "Thunder and war", listOf("Harvest", "The sea", "The underworld"), QuizDifficulty.Hard),
        ask("mokosz", "What was the Slavic goddess Mokosh the patron of?", "Fertility, women and the earth", listOf("War, weapons and the fortress", "Thunder, lightning and oaks", "The sea, rivers and fishing"), QuizDifficulty.Hard),
        ask("baba_jaga", "Where does Baba Yaga usually live in Slavic folk tales?", "In a hut on chicken legs in the forest", listOf("In a castle standing by the sea", "In a cave high in the mountains", "On an island in a wide river"), QuizDifficulty.Hard),
        ask("huitzilopochtli", "What was the Aztec god Huitzilopochtli the god of?", "The sun and war", listOf("Rain", "The moon", "The underworld"), QuizDifficulty.Hard),
        ask("izanagi_izanami", "What did Izanagi and Izanami create in Japanese mythology?", "The islands of Japan", listOf("The sun and the moon", "Humanity directly", "Heaven and hell"), QuizDifficulty.Hard),
        ask("amaterasu", "What is Amaterasu the goddess of in Japanese mythology?", "The sun", listOf("The moon", "The sea", "War"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("tezkatlipoka", "What is the Aztec god Tezcatlipoca associated with?", "Night, fate and a magical obsidian mirror", listOf("The harvest and nothing else besides", "The sea and everything in it", "Craftsmanship and fine metalwork"), QuizDifficulty.Expert),
        ask("veles_ptice", "What form did the Slavic god Veles often take in myths?", "A serpent or dragon", listOf("A falcon", "A white horse", "A bear only"), QuizDifficulty.Expert),
        ask("marzanna", "What does the Slavic figure Marzanna symbolize?", "Winter and death, drowned to welcome spring", listOf("The summer harvest, burnt to bring rain", "Love, courtship and marriage", "War and the defence of the tribe"), QuizDifficulty.Expert),
        ask("thoth_pismo", "What did the god Thoth invent according to Egyptian myth?", "Hieroglyphic writing", listOf("The wheel", "The pyramid", "The solar calendar as his sole invention"), QuizDifficulty.Expert),
        ask("izydy_magia", "What was Isis famous for in Egyptian mythology besides motherhood?", "Great magical power", listOf("Physical strength", "Martial skills", "Ruling over the sea"), QuizDifficulty.Expert),
        ask("coyote_mit", "What role does Coyote often play in Native American mythologies?", "A trickster figure", listOf("The supreme creator god", "A god of war", "A god of the underworld"), QuizDifficulty.Expert),
        ask("anansi", "Where does the myth of Anansi, the trickster spider, come from?", "The Ashanti people's mythology in West Africa", listOf("The mythology of the Norse settlers", "The mythology of the Greek islands", "The mythology of imperial China"), QuizDifficulty.Expert)
    )
}
