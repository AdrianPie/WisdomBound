package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Partia po przegladzie banku. Przeglad wycial ~370 pytan - glownie ten sam fakt zapisany
 * dwa-piec razy w roznych plikach (gracz widzial to jako "to samo pytanie po trzech falach"),
 * mity podane jako fakt i pytania za latwe na swoj szczebel. Ta partia dosypuje GORNE szczeble
 * tam, gdzie po cieciu zabraklo glebi, plus pytania o historii Polski przeniesione z tematu
 * ogolnego do lokalnego (gracz spoza Polski ich nie dostaje).
 */
object QuestionsRefresh {

    private val M = QuizDifficulty.Medium
    private val H = QuizDifficulty.Hard
    private val X = QuizDifficulty.Expert

    private fun q(id: String, prompt: String, correct: String, wrong: List<String>, category: QuizCategory, difficulty: QuizDifficulty) =
        QuizEntry(id = "ref_$id", prompt = prompt, correct = correct, wrong = wrong, category = category, difficulty = difficulty)

    private val Ani = QuizCategory.Animals
    private val Ele = QuizCategory.Elements
    private val Spo = QuizCategory.Sport
    private val Mat = QuizCategory.Math
    private val Lit = QuizCategory.Literature
    private val Tec = QuizCategory.Technology
    private val Myt = QuizCategory.Mythology
    private val Pol = QuizCategory.Poland

    val all: List<QuizEntry> = listOf(
        /* ------------------------------ Animals ------------------------------ */
        q("ani_neotenia", "An axolotl keeps its larval features for life. What is this called?", "Neoteny", listOf("Metamorphosis", "Diapause", "Estivation"), Ani, X),
        q("ani_wydra_futro", "Which mammal has the densest fur of any animal?", "The sea otter", listOf("The polar bear", "The beaver", "The musk ox"), Ani, X),
        q("ani_dziobak_elektro", "How does a platypus find prey underwater with its eyes closed?", "It senses electric fields with its bill", listOf("It uses echolocation", "It sees in infrared", "It smells through its tail"), Ani, X),
        q("ani_flamingi_grupa", "What is a group of flamingos called?", "A flamboyance", listOf("A parade", "A blush", "A pageant"), Ani, X),
        q("ani_poskoczek", "Which fish can walk on mudflats using its fins and breathe air through its skin?", "The mudskipper", listOf("The coelacanth", "The pufferfish", "The ocean sunfish"), Ani, X),
        q("ani_mleko_foka", "Which animal produces the fattiest known milk, at around 60% fat?", "The hooded seal", listOf("The blue whale", "The reindeer", "The domestic cow"), Ani, X),
        q("ani_kukulka", "What is the cuckoo's famous breeding trick called?", "Brood parasitism", listOf("Kleptoparasitism", "Parthenogenesis", "Cooperative breeding"), Ani, X),

        /* ------------------------------ Elements ----------------------------- */
        q("ele_selen_ksiezyc", "Which element takes its name from the Greek word for the Moon?", "Selenium", listOf("Tellurium", "Cerium", "Palladium"), Ele, X),
        q("ele_tellur_ziemia", "Which element takes its name from the Latin word for the Earth?", "Tellurium", listOf("Selenium", "Terbium", "Germanium"), Ele, X),
        q("ele_elektroujemnosc", "Which element has the highest electronegativity?", "Fluorine", listOf("Oxygen", "Chlorine", "Nitrogen"), Ele, X),
        q("ele_cialo_masa", "Which element makes up the largest share of the human body by mass?", "Oxygen", listOf("Carbon", "Hydrogen", "Nitrogen"), Ele, X),
        q("ele_radon_piwnice", "Which radioactive noble gas can build up in basements?", "Radon", listOf("Xenon", "Krypton", "Argon"), Ele, X),
        q("ele_glin_skorupa", "Which is the most abundant metal in Earth's crust?", "Aluminium", listOf("Iron", "Calcium", "Sodium"), Ele, X),
        q("ele_tryt", "Tritium, used in glowing watch dials, is an isotope of which element?", "Hydrogen", listOf("Helium", "Lithium", "Radium"), Ele, X),
        q("ele_eka_glin", "Which element did Mendeleev predict as 'eka-aluminium' before it was found?", "Gallium", listOf("Germanium", "Scandium", "Indium"), Ele, X),

        /* ------------------------------- Sport ------------------------------- */
        q("spo_mundial_1930", "Which country won the first FIFA World Cup, in 1930?", "Uruguay", listOf("Argentina", "Brazil", "Italy"), Spo, X),
        q("spo_yurchenko", "In which sport would you perform a 'Yurchenko'?", "Gymnastics", listOf("Figure skating", "Diving", "Trampolining"), Spo, X),
        q("spo_eagan", "Who is the only person to win Olympic gold in both Summer and Winter Games in different sports?", "Eddie Eagan", listOf("Clara Hughes", "Christa Luding-Rothenburger", "Lauryn Williams"), Spo, X),

        /* -------------------------------- Math ------------------------------- */
        q("mat_osmiokat_katy", "What do the interior angles of an octagon add up to?", "1080 degrees", listOf("900 degrees", "1260 degrees", "720 degrees"), Mat, H),
        q("mat_szesciokat_przekatne", "How many diagonals does a hexagon have?", "9", listOf("6", "12", "8"), Mat, H),
        q("mat_procent_procentu", "What is 15% of 15% of 1000?", "22.5", listOf("2.25", "225", "30"), Mat, H),
        q("mat_szereg_polowki", "What is the sum of the endless series 1/2 + 1/4 + 1/8 + ...?", "1", listOf("2", "Infinity", "0.99"), Mat, X),
        q("mat_dwa_razy_cyfry", "Which two-digit number is exactly twice the sum of its digits?", "18", listOf("12", "27", "36"), Mat, X),
        q("mat_dwudziestoscian", "How many edges does a regular icosahedron have?", "30", listOf("20", "12", "60"), Mat, X),
        q("mat_hex_ff", "What is the hexadecimal number FF in decimal?", "255", listOf("256", "240", "1515"), Mat, X),

        /* ----------------------------- Literature ---------------------------- */
        q("lit_martwe_dusze", "Who wrote 'Dead Souls'?", "Nikolai Gogol", listOf("Ivan Turgenev", "Anton Chekhov", "Alexander Pushkin"), Lit, X),
        q("lit_slepota", "Whose novel 'Blindness' imagines an epidemic of white blindness?", "Jose Saramago", listOf("Gabriel Garcia Marquez", "Italo Calvino", "Mario Vargas Llosa"), Lit, X),
        q("lit_godot_jezyk", "In which language did Samuel Beckett first write 'Waiting for Godot'?", "French", listOf("English", "German", "Irish"), Lit, X),

        /* ----------------------------- Technology ---------------------------- */
        q("tec_solid_s", "What does the 'S' in the SOLID design principles stand for?", "Single responsibility", listOf("Separation of concerns", "Simplicity", "Scalability"), Tec, X),
        q("tec_heapsort", "Which sort runs in O(n log n) even in the worst case and sorts in place using a binary heap?", "Heapsort", listOf("Quicksort", "Merge sort", "Insertion sort"), Tec, X),

        /* ----------------------------- Mythology ----------------------------- */
        q("myt_maat", "Which Egyptian goddess of truth weighed the hearts of the dead against her feather?", "Ma'at", listOf("Isis", "Hathor", "Sekhmet"), Myt, H),
        q("myt_freja_koty", "Which Norse goddess rides a chariot pulled by two cats?", "Freyja", listOf("Frigg", "Hel", "Sif"), Myt, H),
        q("myt_iris", "Which Greek goddess of the rainbow carried messages for the gods?", "Iris", listOf("Hebe", "Eos", "Nike"), Myt, H),
        q("myt_hermes_lira", "What did the infant Hermes make from a tortoise shell?", "The lyre", listOf("The flute", "A drum", "A shield"), Myt, H),
        q("myt_janus", "Which two-faced Roman god ruled beginnings, doorways and January?", "Janus", listOf("Terminus", "Vesta", "Saturn"), Myt, H),
        q("myt_hanuman", "Which monkey god helps Rama rescue Sita in the Ramayana?", "Hanuman", listOf("Ganesha", "Garuda", "Krishna"), Myt, H),
        q("myt_asgard", "What is the realm of the Aesir gods called in Norse myth?", "Asgard", listOf("Midgard", "Niflheim", "Jotunheim"), Myt, H),
        q("myt_eos", "Who was the Greek goddess of the dawn?", "Eos", listOf("Selene", "Iris", "Hemera"), Myt, H),
        q("myt_augiasz", "Which hero cleaned the Augean stables in a day by diverting two rivers?", "Heracles", listOf("Theseus", "Perseus", "Jason"), Myt, H),
        q("myt_kronos_dzieci", "What did the Titan Cronus do with his children as soon as they were born?", "Swallowed them", listOf("Buried them in the earth", "Turned them to stone", "Cast them into the sea"), Myt, X),
        q("myt_baldr", "Which Norse god was killed by a dart made of mistletoe?", "Baldr", listOf("Freyr", "Heimdall", "Tyr"), Myt, X),
        q("myt_set", "Which Egyptian god ruled chaos, storms and the desert?", "Set", listOf("Apep", "Horus", "Ptah"), Myt, X),
        q("myt_cuchulainn", "Who is the great hero of the Irish Ulster Cycle?", "Cu Chulainn", listOf("Fionn mac Cumhaill", "Lugh", "Bran the Blessed"), Myt, X),
        q("myt_susanoo", "Which Japanese storm god slew the eight-headed serpent Yamata no Orochi?", "Susanoo", listOf("Raijin", "Hachiman", "Tsukuyomi"), Myt, X),
        q("myt_ambrozja", "What was ambrosia in Greek myth?", "The food of the gods", listOf("Dionysus's sacred wine", "The fire stolen by Prometheus", "Water from the river Lethe"), Myt, X),

        /* ------------------------------- Poland ------------------------------ */
        q("pol_grunwald_rok", "In which year was the Battle of Grunwald fought?", "1410", listOf("1385", "1466", "1525"), Pol, M),
        q("pol_sobieski_wieden", "Which Polish king led the relief of Vienna in 1683?", "John III Sobieski", listOf("Stephen Bathory", "Sigismund III Vasa", "Wladyslaw IV Vasa"), Pol, M),
        q("pol_konstytucja_rok", "The Constitution of 3 May was adopted in which year?", "1791", listOf("1772", "1795", "1807"), Pol, M),
        q("pol_solidarnosc_miasto", "In which city's shipyard was the Solidarity movement born?", "Gdansk", listOf("Gdynia", "Szczecin", "Warsaw"), Pol, M),
        q("pol_kopernik_torun", "In which city was Nicolaus Copernicus born?", "Torun", listOf("Krakow", "Frombork", "Gdansk"), Pol, M),
        q("pol_pilsudski_1920", "Who was the Polish commander-in-chief at the Battle of Warsaw in 1920?", "Jozef Pilsudski", listOf("Wladyslaw Sikorski", "Jozef Haller", "Roman Dmowski"), Pol, H),
        q("pol_getto_rok", "In which year did the Warsaw Ghetto Uprising break out?", "1943", listOf("1939", "1944", "1945"), Pol, H),
        q("pol_armia_krajowa", "What was the main Polish underground army of World War II called?", "The Home Army", listOf("The People's Army", "The Polish Legions", "The Blue Army"), Pol, H)
    )
}
