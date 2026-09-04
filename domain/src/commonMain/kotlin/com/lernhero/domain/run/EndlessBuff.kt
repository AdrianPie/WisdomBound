package com.lernhero.domain.run

import kotlin.random.Random

/**
 * Klasa jakosci ulepszenia. Im lepiej idzie quiz, tym czesciej wypadaja te mocniejsze - patrz
 * [EndlessBuff.offer]. [baseWeight] to waga przy fatalnym quizie, [skillWeight] przy idealnym;
 * miedzy nimi wagi sa po prostu interpolowane, wiec nie ma progu, na ktorym cos "nagle sie odblokowuje" -
 * legendarne moze trafic sie kazdemu, tylko rzadko.
 */
enum class UpgradeRarity(val label: String, private val baseWeight: Float, private val skillWeight: Float) {
    Common("COMMON", baseWeight = 55f, skillWeight = 12f),
    Rare("RARE", baseWeight = 30f, skillWeight = 30f),
    Epic("EPIC", baseWeight = 12f, skillWeight = 36f),
    Legendary("LEGENDARY", baseWeight = 3f, skillWeight = 22f);

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
    // --- COMMON: simple, numeric boosts that always help --------------------
    Might(
        title = "Party Might",
        description = "+8% party attack per stack.",
        rarity = UpgradeRarity.Common,
        maxStacks = 5
    ),
    Fortress(
        title = "Fortress",
        description = "-7% damage taken per stack (max 60%).",
        rarity = UpgradeRarity.Common,
        maxStacks = 5
    ),
    Precision(
        title = "Precision",
        description = "+8% critical hit chance per stack.",
        rarity = UpgradeRarity.Common,
        maxStacks = 4
    ),
    Vampirism(
        title = "Vampirism",
        description = "Heal for 5% of damage dealt per stack.",
        rarity = UpgradeRarity.Common,
        maxStacks = 4
    ),
    Vitality(
        title = "Vitality",
        description = "+10% max party HP per stack - and heals for the same amount instantly.",
        rarity = UpgradeRarity.Common,
        maxStacks = 4
    ),
    Regeneration(
        title = "Regeneration",
        description = "After every wave cleared, the party heals 7% of max HP per stack.",
        rarity = UpgradeRarity.Common,
        maxStacks = 4
    ),

    // --- RARE: change how the party hits or takes hits -------------------------------
    MultiShot(
        title = "Double Shot",
        description = "The hero fires +1 extra projectile per attack - one animation, two hits.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 3
    ),
    Cleave(
        title = "Cleave",
        description = "Every party attack also hits an extra enemy for 60% damage.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 2
    ),
    Guard(
        title = "Guard",
        description = "Every hero blocks the first hit of the turn - no damage taken.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 2
    ),
    Thorns(
        title = "Thorns",
        description = "The attacker takes 25% of the damage it dealt to the party.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 3
    ),
    Executioner(
        title = "Executioner",
        description = "+35% damage to targets below 35% HP.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 2
    ),
    Blessing(
        title = "Blessing",
        description = "+30% to EVERY heal (vampirism, regeneration, potions).",
        rarity = UpgradeRarity.Rare,
        maxStacks = 2
    ),
    Momentum(
        title = "Momentum",
        description = "+6% attack per correct answer in a row (up to +30%).",
        rarity = UpgradeRarity.Rare,
        maxStacks = 2
    ),

    // --- EPIC: give the whole party something it didn't have before -------------------
    Volley(
        title = "Volley",
        description = "The WHOLE party fires +1 extra projectile per attack.",
        rarity = UpgradeRarity.Epic,
        maxStacks = 2
    ),
    Frenzy(
        title = "Frenzy",
        description = "+1 extra exchange of blows every turn.",
        rarity = UpgradeRarity.Epic,
        maxStacks = 2
    ),
    SecondWind(
        title = "Second Wind",
        description = "Once per wave the hero survives a fatal hit with 25% HP.",
        rarity = UpgradeRarity.Epic,
        maxStacks = 2
    ),
    Brutality(
        title = "Brutality",
        description = "+40% damage from critical hits.",
        rarity = UpgradeRarity.Epic,
        maxStacks = 2
    ),
    Overload(
        title = "Overload",
        description = "The ultimate hits 60% harder per stack.",
        rarity = UpgradeRarity.Epic,
        maxStacks = 2
    ),

    // --- LEGENDARY: rare, dramatically change the flow of battle ---------------------------------
    Avatar(
        title = "Avatar",
        description = "+20% attack AND +20% max HP for the whole party.",
        rarity = UpgradeRarity.Legendary,
        maxStacks = 2
    ),
    Aegis(
        title = "Aegis",
        description = "Every hero blocks the first hit of EVERY round, not just the turn.",
        rarity = UpgradeRarity.Legendary,
        maxStacks = 1
    ),
    Riposte(
        title = "Riposte",
        description = "A blocked hit bounces back at the attacker for 150% of its damage.",
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
