package com.lernhero.domain.run

import kotlin.random.Random

/**
 * Klasa jakosci ulepszenia. Im lepiej idzie quiz, tym czesciej wypadaja te mocniejsze - patrz
 * [EndlessBuff.offer]. [baseWeight] to waga przy fatalnym quizie, [skillWeight] przy idealnym;
 * miedzy nimi wagi sa po prostu interpolowane, wiec nie ma progu, na ktorym cos "nagle sie odblokowuje" -
 * legendarne moze trafic sie kazdemu, tylko rzadko.
 */
enum class UpgradeRarity(val label: String, private val baseWeight: Float, private val skillWeight: Float) {
    Common("ZWYKLE", baseWeight = 55f, skillWeight = 12f),
    Rare("RZADKIE", baseWeight = 30f, skillWeight = 30f),
    Epic("EPICKIE", baseWeight = 12f, skillWeight = 36f),
    Legendary("LEGENDARNE", baseWeight = 3f, skillWeight = 22f);

    /** [quality] 0..1 - jakosc dotychczasowego quizu (patrz [EndlessBuff.qualityFor]). */
    fun weightFor(quality: Float): Float = baseWeight + (skillWeight - baseWeight) * quality.coerceIn(0f, 1f)
}

/**
 * Ulepszenie z endless mode - roguelike'owy perk WYLACZNIE na biezacy bieg (patrz [BattleRequest.isEndless]).
 * Co kilka oczyszczonych fal gracz dostaje do wyboru [OFFER_SIZE] kart (patrz [offer]) i zabiera jedna
 * na stale, do konca biegu. Powtorzony wybor STAKUJE (mocniejszy efekt) az do [maxStacks] - dlatego
 * [BattleUiState.endlessBuffs][com.lernhero.game.battle.BattleUiState] to mapa bonus -> liczba stakow,
 * a nie zbior.
 *
 * Cala mechanika kazdego z nich siedzi w BattleViewModel (szukaj `endlessBuffs[EndlessBuff.` ) -
 * tutaj sa tylko dane: co to jest, jak rzadkie i ile razy mozna wziac.
 */
enum class EndlessBuff(
    val title: String,
    val description: String,
    val rarity: UpgradeRarity,
    val maxStacks: Int
) {
    // --- ZWYKLE: proste, liczbowe wzmocnienia, ktore zawsze sie przydaja --------------------
    Might(
        title = "Moc druzyny",
        description = "+8% ataku calej druzyny za kazdy stak.",
        rarity = UpgradeRarity.Common,
        maxStacks = 5
    ),
    Fortress(
        title = "Twierdza",
        description = "-7% otrzymywanych obrazen za kazdy stak (max 60%).",
        rarity = UpgradeRarity.Common,
        maxStacks = 5
    ),
    Precision(
        title = "Precyzja",
        description = "+8% szansy na cios krytyczny za kazdy stak.",
        rarity = UpgradeRarity.Common,
        maxStacks = 4
    ),
    Vampirism(
        title = "Wampiryzm",
        description = "Leczysz sie za 5% zadanych obrazen za kazdy stak.",
        rarity = UpgradeRarity.Common,
        maxStacks = 4
    ),
    Vitality(
        title = "Zywotnosc",
        description = "+10% maks. HP druzyny za stak - i od razu tyle samo leczenia.",
        rarity = UpgradeRarity.Common,
        maxStacks = 4
    ),
    Regeneration(
        title = "Regeneracja",
        description = "Po kazdej oczyszczonej fali druzyna leczy 7% maks. HP za stak.",
        rarity = UpgradeRarity.Common,
        maxStacks = 4
    ),

    // --- RZADKIE: zmieniaja sposob, w jaki druzyna bije albo obrywa -------------------------
    MultiShot(
        title = "Podwojny strzal",
        description = "Bohater wypuszcza +1 pocisk na atak - jedna animacja, dwa trafienia.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 3
    ),
    Cleave(
        title = "Rozdarcie",
        description = "Kazdy atak druzyny siega dodatkowego wroga za 60% obrazen.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 2
    ),
    Guard(
        title = "Garda",
        description = "Kazdy bohater blokuje pierwszy cios w turze - bez obrazen.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 2
    ),
    Thorns(
        title = "Kolce",
        description = "Napastnik dostaje 25% obrazen, ktore zadal druzynie.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 3
    ),
    Executioner(
        title = "Kat",
        description = "+35% obrazen w cele ponizej 35% HP.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 2
    ),
    Blessing(
        title = "Blogoslawienstwo",
        description = "+30% do KAZDEGO leczenia (wampiryzm, regeneracja, mikstury).",
        rarity = UpgradeRarity.Rare,
        maxStacks = 2
    ),
    Momentum(
        title = "Rozped",
        description = "+6% ataku za kazda dobra odpowiedz z rzedu (do +30%).",
        rarity = UpgradeRarity.Rare,
        maxStacks = 2
    ),

    // --- EPICKIE: dokladaja calej druzynie cos, czego wczesniej nie miala -------------------
    Volley(
        title = "Salwa",
        description = "CALA druzyna wypuszcza +1 pocisk na atak.",
        rarity = UpgradeRarity.Epic,
        maxStacks = 2
    ),
    Frenzy(
        title = "Szal",
        description = "+1 runda wymiany ciosow w kazdej turze.",
        rarity = UpgradeRarity.Epic,
        maxStacks = 2
    ),
    SecondWind(
        title = "Drugi oddech",
        description = "Raz na fale bohater przezywa smiertelny cios z 25% HP.",
        rarity = UpgradeRarity.Epic,
        maxStacks = 2
    ),
    Brutality(
        title = "Brutalnosc",
        description = "+40% obrazen z ciosow krytycznych.",
        rarity = UpgradeRarity.Epic,
        maxStacks = 2
    ),
    Overload(
        title = "Przeciazenie",
        description = "Ulti bije o 60% mocniej za kazdy stak.",
        rarity = UpgradeRarity.Epic,
        maxStacks = 2
    ),

    // --- LEGENDARNE: rzadkie, wyraznie zmieniaja bieg walki ---------------------------------
    Avatar(
        title = "Awatar",
        description = "+20% ataku I +20% maks. HP calej druzyny.",
        rarity = UpgradeRarity.Legendary,
        maxStacks = 2
    ),
    Aegis(
        title = "Egida",
        description = "Kazdy bohater blokuje pierwszy cios w KAZDEJ rundzie, nie tylko w turze.",
        rarity = UpgradeRarity.Legendary,
        maxStacks = 1
    ),
    Riposte(
        title = "Riposta",
        description = "Zablokowany cios wraca do napastnika za 150% jego obrazen.",
        rarity = UpgradeRarity.Legendary,
        maxStacks = 1
    );

    companion object {
        /** Ile kart dostaje gracz do wyboru przy jednym ulepszeniu. */
        const val OFFER_SIZE = 3

        /**
         * Jakosc quizu (0..1) przelozona z surowej celnosci odpowiedzi. Ponizej [FLOOR_ACCURACY]
         * (zgadywanie na chybil trafil w pytaniu z czterema odpowiedziami) to zero - dopiero
         * powyzej tego progu ulepszenia zaczynaja sie poprawiac, a [CEILING_ACCURACY] to juz pelna
         * pula. Bez pytan w ogole (poczatek biegu) jest neutralnie, czyli zero.
         */
        fun qualityFor(correctAnswers: Int, answeredQuestions: Int): Float {
            if (answeredQuestions <= 0) return 0f
            val accuracy = correctAnswers.toFloat() / answeredQuestions
            return ((accuracy - FLOOR_ACCURACY) / (CEILING_ACCURACY - FLOOR_ACCURACY)).coerceIn(0f, 1f)
        }

        /**
         * Losuje [OFFER_SIZE] ROZNYCH ulepszen do wyboru. Najpierw losowana jest klasa jakosci
         * (wagi zalezne od [quality] - patrz [UpgradeRarity.weightFor]), potem konkretna karta z tej
         * klasy; jesli w danej klasie nie ma juz nic, czego gracz nie wymaksowal, losowanie schodzi
         * do sasiednich klas, zeby oferta ZAWSZE byla pelna.
         *
         * [owned] to biezace staki - wymaksowane ulepszenia w ogole sie nie pokazuja, zeby karta
         * nie byla "pusta".
         */
        fun offer(
            owned: Map<EndlessBuff, Int>,
            quality: Float,
            random: Random
        ): List<EndlessBuff> {
            val available = entries.filter { (owned[it] ?: 0) < it.maxStacks }.toMutableList()
            val picked = mutableListOf<EndlessBuff>()
            repeat(OFFER_SIZE) {
                if (available.isEmpty()) return@repeat
                val choice = drawOne(available, quality, random)
                picked += choice
                available -= choice
            }
            return picked
        }

        private fun drawOne(pool: List<EndlessBuff>, quality: Float, random: Random): EndlessBuff {
            val rarities = pool.map { it.rarity }.distinct()
            val totalWeight = rarities.sumOf { it.weightFor(quality).toDouble() }.toFloat()
            var roll = random.nextFloat() * totalWeight
            val rarity = rarities.firstOrNull { candidate ->
                roll -= candidate.weightFor(quality)
                roll <= 0f
            } ?: rarities.last()
            val bucket = pool.filter { it.rarity == rarity }
            return bucket[random.nextInt(bucket.size)]
        }

        private const val FLOOR_ACCURACY = 0.35f
        private const val CEILING_ACCURACY = 0.9f
    }
}
