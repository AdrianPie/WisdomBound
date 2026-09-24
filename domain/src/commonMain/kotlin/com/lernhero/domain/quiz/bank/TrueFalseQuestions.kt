package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.TrueFalseEntry

/**
 * Pytania "prawda czy falsz". Zasada doboru: stwierdzenie ma albo obalac popularny mit, albo
 * brzmiec jak mit, a byc prawda - pytanie, na ktore kazdy zna odpowiedz ("slonce to gwiazda"),
 * jest przy dwoch odpowiedziach czystym rzutem moneta w druga strone.
 *
 * Celowo NIE powtarzaja faktow, o ktore pytaja pytania z wyborem w innych plikach banku - gracz
 * odbiera ten sam fakt w innym formacie jako to samo pytanie.
 */
object TrueFalseQuestions {

    private val E = QuizDifficulty.Easy
    private val M = QuizDifficulty.Medium
    private val H = QuizDifficulty.Hard
    private val X = QuizDifficulty.Expert

    private fun tf(id: String, statement: String, isTrue: Boolean, category: QuizCategory, difficulty: QuizDifficulty) =
        TrueFalseEntry(id = "tf_$id", statement = statement, isTrue = isTrue, category = category, difficulty = difficulty)

    private val Bio = QuizCategory.Biology
    private val Ani = QuizCategory.Animals
    private val His = QuizCategory.History
    private val Ele = QuizCategory.Elements
    private val Cap = QuizCategory.Capitals
    private val Sci = QuizCategory.Science
    private val Geo = QuizCategory.Geography
    private val Spo = QuizCategory.Sport
    private val Mat = QuizCategory.Math
    private val Lit = QuizCategory.Literature
    private val Tec = QuizCategory.Technology
    private val Myt = QuizCategory.Mythology
    private val Pol = QuizCategory.Poland

    val all: List<TrueFalseEntry> = listOf(
        /* ------------------------------ Biology ------------------------------ */
        tf("bio_serce_komory", "The human heart has four chambers.", true, Bio, E),
        tf("bio_fotosynteza_tlen", "Plants release oxygen during photosynthesis.", true, Bio, E),
        tf("bio_kosci_200", "An adult human skeleton has more than 200 bones.", true, Bio, E),
        tf("bio_zyly_niebieskie", "The blood inside your veins is blue.", false, Bio, E),
        tf("bio_mozg_10", "Humans use only about 10% of their brains.", false, Bio, M),
        tf("bio_banan_jagoda", "Botanically speaking, a banana is a berry.", true, Bio, M),
        tf("bio_truskawka_jagoda", "Botanically speaking, a strawberry is a berry.", false, Bio, M),
        tf("bio_antybiotyk_wirus", "Antibiotics are effective against viruses.", false, Bio, M),
        tf("bio_paznokcie_wlosy", "Fingernails and hair are made of the same protein.", true, Bio, M),
        tf("bio_bakterie_gwiazdy", "There are more bacterial cells in your gut than stars in the Milky Way.", true, Bio, H),
        tf("bio_blizniaki_odciski", "Identical twins have identical fingerprints.", false, Bio, H),
        tf("bio_serce_poza_cialem", "A heart can keep beating for a while after being removed from the body.", true, Bio, H),
        tf("bio_szympans_dna", "Humans and chimpanzees share more than 95% of their DNA.", true, Bio, H),
        tf("bio_mitochondria_dna", "Mitochondria carry their own DNA, separate from the cell nucleus.", true, Bio, X),
        tf("bio_erytrocyt_jadro", "Mature human red blood cells contain a nucleus.", false, Bio, X),
        tf("bio_pomidor_ziemniak", "Tomatoes and potatoes belong to the same plant family.", true, Bio, X),

        /* ------------------------------ Animals ------------------------------ */
        tf("ani_nietoperz_slepy", "Bats are blind.", false, Ani, E),
        tf("ani_pajak_owad", "A spider is an insect.", false, Ani, E),
        tf("ani_delfin_powietrze", "Dolphins breathe air.", true, Ani, E),
        tf("ani_pingwiny_arktyka", "Penguins live in the Arctic.", false, Ani, E),
        tf("ani_zlota_rybka", "A goldfish's memory lasts only about three seconds.", false, Ani, M),
        tf("ani_strus_piasek", "Ostriches bury their heads in the sand when frightened.", false, Ani, M),
        tf("ani_rekin_plywanie", "Some sharks must keep swimming to keep breathing.", true, Ani, M),
        tf("ani_koala_mis", "Koalas are a kind of bear.", false, Ani, M),
        tf("ani_leniwiec_oddech", "A sloth can hold its breath longer than a dolphin.", true, Ani, M),
        tf("ani_lew_polowanie", "In a lion pride, the males do most of the hunting.", false, Ani, M),
        tf("ani_rozgwiazda_mozg", "Starfish have no brain.", true, Ani, H),
        tf("ani_zolw_kloaka", "Some turtles can absorb oxygen through their rear end.", true, Ani, H),
        tf("ani_krewetka_serce", "A shrimp's heart is located in its head region.", true, Ani, H),
        tf("ani_motyl_stopy", "Butterflies taste with their feet.", true, Ani, H),
        tf("ani_pletwal_serce", "A blue whale's heart is roughly the size of a small car.", true, Ani, X),
        tf("ani_pszczoly_twarze", "Honeybees can learn to recognise human faces.", true, Ani, X),
        tf("ani_kangur_do_tylu", "Kangaroos can easily walk backwards.", false, Ani, X),

        /* ------------------------------ History ------------------------------ */
        tf("his_kolumb_plaska", "Columbus sailed west to prove that the Earth was round.", false, His, E),
        tf("his_kleopatra_egipcjanka", "Cleopatra was of Egyptian descent.", false, His, E),
        tf("his_sredniowiecze_kula", "Educated people in medieval Europe knew the Earth was round.", true, His, E),
        tf("his_mur_wiek", "The Great Wall of China was built within a single century.", false, His, E),
        tf("his_ciastko_antonina", "There is solid evidence that Marie Antoinette said 'Let them eat cake'.", false, His, M),
        tf("his_einstein_matma", "Albert Einstein failed mathematics at school.", false, His, M),
        tf("his_stuletnia_100", "The Hundred Years' War lasted exactly one hundred years.", false, His, M),
        tf("his_wieza_eiffla_1889", "The Eiffel Tower was built for the 1889 World's Fair.", true, His, M),
        tf("his_mongolowie_imperium", "The Mongol Empire was the largest contiguous land empire in history.", true, His, M),
        tf("his_bizancjum_xv", "The Byzantine Empire survived into the 15th century.", true, His, H),
        tf("his_magna_carta_monarchia", "The Magna Carta abolished the English monarchy.", false, His, H),
        tf("his_pazdziernik_listopad", "By today's calendar, Russia's October Revolution happened in November.", true, His, H),
        tf("his_titanic_najgorsza", "The Titanic is the deadliest peacetime shipwreck in history.", false, His, H),
        tf("his_trzydziestoletnia_rzesza", "The Thirty Years' War was fought mainly in the Holy Roman Empire.", true, His, X),
        tf("his_druk_chiny", "Movable-type printing existed in China centuries before Gutenberg.", true, His, X),
        tf("his_piotr_petersburg", "Peter the Great moved Russia's capital from Moscow to St Petersburg.", true, His, X),

        /* ------------------------------ Elements ----------------------------- */
        tf("ele_woda_sklad", "Water is made of hydrogen and oxygen.", true, Ele, E),
        tf("ele_zloto_magnes", "Gold is attracted to a magnet.", false, Ele, E),
        tf("ele_czysta_woda_prad", "Pure distilled water conducts electricity very well.", false, Ele, E),
        tf("ele_srebro_czernieje", "Silver tarnishes because it reacts with sulfur compounds in the air.", true, Ele, E),
        tf("ele_olow_benzyna", "Lead was once added to petrol.", true, Ele, M),
        tf("ele_miedz_srebro", "Copper conducts electricity better than silver.", false, Ele, M),
        tf("ele_wodor_najlzejszy", "Hydrogen is the lightest element.", true, Ele, M),
        tf("ele_sol_wrzenie", "Salty water boils at a lower temperature than fresh water.", false, Ele, M),
        tf("ele_puszka_cyna", "Tin cans are made mostly of tin.", false, Ele, M),
        tf("ele_diament_plonie", "A diamond can burn.", true, Ele, H),
        tf("ele_fluor_szlachetny", "Fluorine is a noble gas.", false, Ele, H),
        tf("ele_ciezsze_od_uranu", "Every element heavier than uranium was first made artificially.", true, Ele, H),
        tf("ele_azot_suchy_lod", "Liquid nitrogen is colder than dry ice.", true, Ele, H),
        tf("ele_zloto_kowalne", "Gold is the most malleable of all metals.", true, Ele, X),
        tf("ele_woda_4_stopnie", "Water is at its densest at about 4 degrees Celsius.", true, Ele, X),
        tf("ele_bizmut_promieniotworczy", "Bismuth is very slightly radioactive.", true, Ele, X),

        /* ------------------------------ Capitals ----------------------------- */
        tf("cap_sydney", "Sydney is the capital of Australia.", false, Cap, E),
        tf("cap_ottawa", "Ottawa is the capital of Canada.", true, Cap, E),
        tf("cap_stambul", "Istanbul is the capital of Turkey.", false, Cap, E),
        tf("cap_waszyngton_stan", "Washington, D.C. lies inside the state of Washington.", false, Cap, E),
        tf("cap_berno", "Bern is the capital of Switzerland.", true, Cap, M),
        tf("cap_almaty", "Almaty is the capital of Kazakhstan.", false, Cap, M),
        tf("cap_auckland", "Auckland is the capital of New Zealand.", false, Cap, M),
        tf("cap_amsterdam_haga", "Amsterdam is the capital of the Netherlands, though the government sits in The Hague.", true, Cap, M),
        tf("cap_canberra_budowana", "Canberra was purpose-built to be a capital city.", true, Cap, M),
        tf("cap_lagos", "Lagos is the capital of Nigeria.", false, Cap, H),
        tf("cap_boliwia_dwie", "Bolivia has two capitals, La Paz and Sucre.", true, Cap, H),
        tf("cap_rangun", "Yangon is the capital of Myanmar.", false, Cap, H),
        tf("cap_watykan", "Vatican City is both a country and its own capital.", true, Cap, H),
        tf("cap_rpa_trzy", "South Africa splits its government between three capital cities.", true, Cap, X),
        tf("cap_dar_es_salaam", "Dar es Salaam is the official capital of Tanzania.", false, Cap, X),
        tf("cap_reykjavik_polnoc", "Reykjavik is the northernmost capital of a sovereign state.", true, Cap, X),

        /* ------------------------------ Science ------------------------------ */
        tf("sci_dzwiek_swiatlo", "Sound travels faster than light.", false, Sci, E),
        tf("sci_ksiezyc_odbija", "The Moon reflects sunlight rather than making its own light.", true, Sci, E),
        tf("sci_lato_blizej", "Earth is closest to the Sun during the northern summer.", false, Sci, E),
        tf("sci_piorun_dwa_razy", "Lightning never strikes the same place twice.", false, Sci, E),
        tf("sci_brak_grawitacji", "There is no gravity in space.", false, Sci, M),
        tf("sci_ultrafiolet_oko", "The human eye can see ultraviolet light.", false, Sci, M),
        tf("sci_ciezsze_szybciej", "In a vacuum, heavier objects fall faster than lighter ones.", false, Sci, M),
        tf("sci_mars_ksiezyce", "Mars has two moons.", true, Sci, M),
        tf("sci_gwiazdy_migaja", "Stars twinkle because they switch on and off.", false, Sci, M),
        tf("sci_elektron_proton", "An electron is heavier than a proton.", false, Sci, H),
        tf("sci_diament_wegiel", "Most diamonds form from compressed coal.", false, Sci, H),
        tf("sci_atom_pusty", "Most of an atom's volume is empty space.", true, Sci, H),
        tf("sci_olbrzymy_pierscienie", "All four giant planets have ring systems.", true, Sci, H),
        tf("sci_splatanie_szybciej", "Quantum entanglement lets information travel faster than light.", false, Sci, X),
        tf("sci_czas_gora", "Time passes very slightly faster on a mountaintop than at sea level.", true, Sci, X),
        tf("sci_galaktyki_rozciagaja", "The expansion of the universe stretches galaxies themselves apart.", false, Sci, X),

        /* ------------------------------ Geography ---------------------------- */
        tf("geo_everest_najwyzszy", "Mount Everest is the highest mountain above sea level.", true, Geo, E),
        tf("geo_afryka_kraj", "Africa is a country.", false, Geo, E),
        tf("geo_australia_oba", "Australia is both a country and a continent.", true, Geo, E),
        tf("geo_pacyfik_najwiekszy", "The Pacific is the largest ocean.", true, Geo, E),
        tf("geo_grenlandia_afryka", "Greenland is larger than Africa.", false, Geo, M),
        tf("geo_islandia_grenlandia", "Iceland is mostly covered in ice, while Greenland is mostly green.", false, Geo, M),
        tf("geo_rownik_brazylia", "The equator passes through Brazil.", true, Geo, M),
        tf("geo_kanada_jeziora", "Canada has more lakes than the rest of the world combined.", true, Geo, M),
        tf("geo_londyn_nowy_jork", "London lies further north than New York City.", true, Geo, M),
        tf("geo_rzym_nowy_jork", "Rome lies further north than New York City.", true, Geo, H),
        tf("geo_morze_martwe_jezioro", "The Dead Sea is actually a lake.", true, Geo, H),
        tf("geo_kilimandzaro_rownik", "Mount Kilimanjaro stands exactly on the equator.", false, Geo, H),
        tf("geo_alaska_wschod_zachod", "Alaska is both the westernmost and the easternmost US state.", true, Geo, H),
        tf("geo_rosja_usa_5km", "At their closest, Russia and the United States are less than 5 km apart.", true, Geo, X),
        tf("geo_bajkal_wielkie_jeziora", "Lake Baikal holds more fresh water than all the North American Great Lakes combined.", true, Geo, X),
        tf("geo_nil_poludnie", "The Nile flows from north to south.", false, Geo, X),

        /* ------------------------------- Sport ------------------------------- */
        tf("spo_igrzyska_naprzemiennie", "Summer and Winter Olympics are held two years apart, alternating.", true, Spo, E),
        tf("spo_hokej_pilka", "Ice hockey is played with a ball.", false, Spo, E),
        tf("spo_hetman_najsilniejszy", "In chess, the queen is the most powerful piece.", true, Spo, E),
        tf("spo_mundial_cztery_lata", "The FIFA World Cup is held every four years.", true, Spo, E),
        tf("spo_rugby_okragla", "A rugby ball is round.", false, Spo, M),
        tf("spo_phelps_rekord", "Michael Phelps has won more Olympic gold medals than any other athlete.", true, Spo, M),
        tf("spo_lotka_pioro", "Top-level badminton shuttlecocks are traditionally made with goose feathers.", true, Spo, M),
        tf("spo_golf_ksiezyc", "Golf has been played on the Moon.", true, Spo, M),
        tf("spo_baseball_remis", "Under normal rules, a baseball game cannot end in a tie.", true, Spo, M),
        tf("spo_przeciaganie_liny", "Tug of war was once an Olympic sport.", true, Spo, H),
        tf("spo_dart_zewnetrzny_bull", "In darts, the outer (green) bull is worth 50 points.", false, Spo, H),
        tf("spo_rugby_przylozenie", "In rugby union, a try is worth more points than the conversion kick after it.", true, Spo, H),
        tf("spo_golf_dolek_108", "A golf hole is 108 mm in diameter.", true, Spo, H),
        tf("spo_mundial_kontynent", "No team has ever won the World Cup on a continent other than its own.", false, Spo, X),
        tf("spo_krykiet_baseball", "A cricket ball is heavier than a baseball.", true, Spo, X),
        tf("spo_flaga_olimpijska", "The Olympic flag was designed so that every nation's flag shares at least one of its colours.", true, Spo, X),

        /* -------------------------------- Math ------------------------------- */
        tf("mat_zero_parzyste", "Zero is an even number.", true, Mat, E),
        tf("mat_trojkat_dwa_proste", "A triangle can have two right angles.", false, Mat, E),
        tf("mat_kwadrat_prostokat", "Every square is a rectangle.", true, Mat, E),
        tf("mat_prostokat_kwadrat", "Every rectangle is a square.", false, Mat, E),
        tf("mat_jeden_pierwsza", "1 is a prime number.", false, Mat, M),
        tf("mat_pi_22_7", "Pi is exactly equal to 22/7.", false, Mat, M),
        tf("mat_minus_minus", "A negative number multiplied by a negative number is positive.", true, Mat, M),
        tf("mat_godziny_tydzien", "There are more hours in a week than days in a year.", false, Mat, M),
        tf("mat_zero_przez_zero", "Any number divided by itself equals 1 - zero included.", false, Mat, M),
        tf("mat_0999", "0.999... with nines repeating forever is exactly equal to 1.", true, Mat, H),
        tf("mat_pierwsze_nieskonczone", "There are infinitely many prime numbers.", true, Mat, H),
        tf("mat_kolo_symetria", "A circle has infinitely many lines of symmetry.", true, Mat, H),
        tf("mat_pierwiastek2_ulamek", "The square root of 2 can be written as a fraction of two whole numbers.", false, Mat, H),
        tf("mat_parzyste_naturalne", "There are exactly as many even numbers as there are whole numbers.", true, Mat, X),
        tf("mat_goldbach_udowodniony", "It has been proven that every even number above 2 is the sum of two primes.", false, Mat, X),
        tf("mat_mobius_krawedz", "A Mobius strip has only one edge.", true, Mat, X),

        /* ----------------------------- Literature ---------------------------- */
        tf("lit_holmes_christie", "Sherlock Holmes was created by Agatha Christie.", false, Lit, E),
        tf("lit_romeo_szczesliwie", "'Romeo and Juliet' has a happy ending.", false, Lit, E),
        tf("lit_frankenstein_potwor", "In Mary Shelley's novel, Frankenstein is the name of the monster.", false, Lit, E),
        tf("lit_kubus_mis", "Winnie-the-Pooh is a bear.", true, Lit, E),
        tf("lit_don_kichot_wiatraki", "Don Quixote attacks windmills he believes are giants.", true, Lit, M),
        tf("lit_orwell_blair", "George Orwell's real name was Eric Blair.", true, Lit, M),
        tf("lit_szekspir_30", "Shakespeare wrote more than 30 plays.", true, Lit, M),
        tf("lit_austen_nazwisko", "Jane Austen's novels carried her name when first published.", false, Lit, M),
        tf("lit_odyseja_wergiliusz", "The Odyssey was written by Virgil.", false, Lit, M),
        tf("lit_tolstoj_nobel", "Leo Tolstoy won the Nobel Prize in Literature.", false, Lit, H),
        tf("lit_dickens_odcinki", "Charles Dickens first published many of his novels in instalments.", true, Lit, H),
        tf("lit_grimm_wymyslili", "The Brothers Grimm invented their fairy tales entirely themselves.", false, Lit, H),
        tf("lit_christie_bestseller", "Agatha Christie is one of the best-selling novelists of all time.", true, Lit, H),
        tf("lit_dickinson_wiersze", "Emily Dickinson published only a handful of poems in her lifetime.", true, Lit, X),
        tf("lit_hemingway_pulitzer", "'The Old Man and the Sea' won Hemingway the Pulitzer Prize.", true, Lit, X),
        tf("lit_cervantes_szekspir", "Cervantes and Shakespeare died in the same year.", true, Lit, X),

        /* ----------------------------- Technology ---------------------------- */
        tf("tec_www_internet", "The World Wide Web and the Internet are the same thing.", false, Tec, E),
        tf("tec_zamykanie_apek", "Force-closing apps always makes a phone battery last longer.", false, Tec, E),
        tf("tec_incognito", "Incognito mode hides your browsing from your internet provider.", false, Tec, E),
        tf("tec_qr_adres", "A QR code can store a web address.", true, Tec, E),
        tf("tec_mysz_drewno", "The first computer mouse had a wooden case.", true, Tec, M),
        tf("tec_mac_wirusy", "Mac computers cannot get viruses.", false, Tec, M),
        tf("tec_https_zaufanie", "A padlock (HTTPS) means a website can be trusted.", false, Tec, M),
        tf("tec_email_www", "Email existed before the World Wide Web.", true, Tec, M),
        tf("tec_megapiksele", "More megapixels always means better photos.", false, Tec, M),
        tf("tec_dysk_lodowka", "The first hard drive to store over a gigabyte was about the size of a refrigerator.", true, Tec, H),
        tf("tec_java_javascript", "Java and JavaScript are the same programming language.", false, Tec, H),
        tf("tec_haslo_dlugie", "A long passphrase is generally harder to crack than a short, complex password.", true, Tec, H),
        tf("tec_usuwanie_pliku", "Deleting a file normally wipes its data from the disk immediately.", false, Tec, H),
        tf("tec_kwantowe_wszystko", "Quantum computers solve every problem faster than classical computers.", false, Tec, X),
        tf("tec_sha256_klucz", "SHA-256 output can be decrypted back into the original data with the right key.", false, Tec, X),
        tf("tec_ipv4_adresy", "IPv4 has room for about 4.3 billion addresses.", true, Tec, X),

        /* ----------------------------- Mythology ----------------------------- */
        tf("myt_zeus_nordycki", "Zeus was king of the Norse gods.", false, Myt, E),
        tf("myt_meduza_kamien", "Medusa's gaze could turn people to stone.", true, Myt, E),
        tf("myt_jednorozec_skrzydla", "In Greek myth, the winged horse is a unicorn.", false, Myt, E),
        tf("myt_herkules_rzymski", "Hercules is the Roman name for the Greek hero Heracles.", true, Myt, E),
        tf("myt_posejdon_podziemie", "Poseidon was the Greek god of the underworld.", false, Myt, M),
        tf("myt_loki_fenrir", "In Norse myth, Loki is the father of the wolf Fenrir.", true, Myt, M),
        tf("myt_kupidyn_eros", "Cupid is the Roman counterpart of the Greek god Eros.", true, Myt, M),
        tf("myt_kon_iliada", "The Trojan Horse is described in Homer's Iliad.", false, Myt, M),
        tf("myt_anubis_slonce", "Anubis is the Egyptian god of the sun.", false, Myt, M),
        tf("myt_achilles_parys", "Achilles was killed by an arrow shot by Paris.", true, Myt, H),
        tf("myt_afrodyta_piana", "Aphrodite was born from sea foam.", true, Myt, H),
        tf("myt_persefona_granat", "Persephone ate pomegranate seeds, binding her to the underworld.", true, Myt, H),
        tf("myt_walkirie_greckie", "Valkyries come from Greek mythology.", false, Myt, H),
        tf("myt_hel_loki", "Hel, the Norse ruler of the dead, is Loki's daughter.", true, Myt, X),
        tf("myt_horus_oko", "Horus lost an eye fighting Set.", true, Myt, X),
        tf("myt_minotaur_minos", "The Minotaur was King Minos's own son.", false, Myt, X),

        /* ------------------------------- Poland ------------------------------ */
        tf("pol_flaga_uklad", "Poland's flag is white on top and red below.", true, Pol, E),
        tf("pol_curie_warszawa", "Marie Curie was born in Warsaw.", true, Pol, E),
        tf("pol_chopin_polska", "Frederic Chopin was born in Poland.", true, Pol, E),
        tf("pol_krakow_stolica", "Krakow was never the capital of Poland.", false, Pol, M),
        tf("pol_grunwald_krzyzacy", "The Battle of Grunwald was fought against the Teutonic Knights.", true, Pol, M),
        tf("pol_niepodleglosc_1945", "Poland regained its independence in 1945.", false, Pol, M),
        tf("pol_kopernik_smierc", "Copernicus's book on the heliocentric system came out in the year he died.", true, Pol, H),
        tf("pol_wieliczka_kaplica", "The Wieliczka mine has a chapel carved entirely out of rock salt.", true, Pol, H),
        tf("pol_rzeczpospolita_elekcja", "Kings of the Polish-Lithuanian Commonwealth were elected.", true, Pol, X),
        tf("pol_kopernik_krakow", "Nicolaus Copernicus studied at the University of Krakow.", true, Pol, X)
    )
}
