package com.lernhero.domain.story

import com.lernhero.domain.monster.EnemySpecies
import com.lernhero.domain.run.BattleKind
import com.lernhero.domain.run.RunDifficulty
import com.lernhero.domain.run.RunLoot
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Jedna plansza kampanii, gotowa do pokazania na mapie: kim walczysz, ile ich, co dostaniesz.
 * Wszystko liczone z gory (deterministycznie z [world]+[stageInWorld]+[tier]), wiec podglad na
 * mapie i prawdziwa walka zawsze sie zgadzaja - nie ma dwoch osobnych zrodel prawdy o tym, kto
 * czeka za drzwiami.
 */
data class StoryStage(
    val world: StoryWorld,
    val stageInWorld: Int,
    val globalIndex: Int,
    val tier: RunDifficulty,
    val kind: BattleKind,
    /** Gatunki przeciwnikow do pokazania jako ikony podgladu - jeden wpis na kazdego wroga fali. */
    val enemyPreview: List<String>,
    val waves: Int,
    val rewardGold: Int,
    val rewardXp: Int
) {
    val isBoss: Boolean get() = kind == BattleKind.Boss
    val label: String get() = "${world.order}-$stageInWorld"

    /** Seed walki - ta sama plansza na tym samym tierze zawsze skladaja sie tak samo. */
    val seed: Long get() = globalIndex.toLong() * 1_000_003L + tier.ordinal
}

/**
 * Liczy [StoryStage] na zadanie - nic tu nie jest trzymane w pamieci, wiec kampania moze byc
 * dowolnie dluga bez generowania z gory tysiecy obiektow.
 */
object StoryStageCatalog {

    fun stageAt(globalIndex: Int, tier: RunDifficulty): StoryStage? {
        val world = StoryWorldCatalog.worldForGlobalStage(globalIndex) ?: return null
        val stageInWorld = StoryWorldCatalog.stageInWorld(globalIndex)
        val isBoss = isBossStage(stageInWorld, world.stageCount)
        val kind = if (isBoss) BattleKind.Boss else BattleKind.Normal
        val random = Random(globalIndex.toLong() * 31 + tier.ordinal)

        val enemyCount = if (isBoss) 1 else (2 + random.nextInt(3)).coerceAtMost(5)
        val enemyPreview = if (isBoss) {
            listOf(EnemySpecies.BOSS_SPECIES)
        } else {
            List(enemyCount) { EnemySpecies.all[random.nextInt(EnemySpecies.all.size)] }
        }

        // Fale rosna WEWNATRZ krainy, a nie skacza. Dotad bylo to `stageInWorld % 3`, czyli
        // 2-3-1-2-3: trzecia plansza kazdej krainy byla krotsza od drugiej i od pierwszej, wiec
        // droga przez kraine raz po raz cofala sie do rozgrzewki. Lacznie fal jest tyle samo -
        // zmienila sie tylko ich kolejnosc.
        val waves = if (isBoss) 1 else (1 + stageInWorld / 2)

        return StoryStage(
            world = world,
            stageInWorld = stageInWorld,
            globalIndex = globalIndex,
            tier = tier,
            kind = kind,
            enemyPreview = enemyPreview,
            waves = waves,
            rewardGold = rewardGoldFor(globalIndex, kind, tier, waves),
            rewardXp = rewardXpFor(globalIndex, kind, tier, waves)
        )
    }

    /** W krainie jest [StoryWorld.stageCount] plansz i tylko ostatnia z nich jest bossem/elita. */
    fun isBossStage(stageInWorld: Int, worldStageCount: Int): Boolean =
        stageInWorld == worldStageCount

    /**
     * Podglad nagrody na kafelku mapy - przyblizony, nie musi co do grosza zgadzac sie z tym, co
     * faktycznie wyplaci walka (na to wplywa jeszcze quiz i passa), tylko dac graczowi realne
     * wyobrazenie, ile ta plansza jest warta.
     */
    private fun rewardGoldFor(globalIndex: Int, kind: BattleKind, tier: RunDifficulty, waves: Int): Int {
        val kindBonus = if (kind == BattleKind.Boss) 2.4f else 1f
        return (GOLD_PER_WAVE * waves * kindBonus * tier.coinMultiplier *
            RunLoot.depthMultiplier(globalIndex)).roundToInt()
    }

    private fun rewardXpFor(globalIndex: Int, kind: BattleKind, tier: RunDifficulty, waves: Int): Int {
        val kindBonus = if (kind == BattleKind.Boss) 2f else 1f
        return (XP_PER_WAVE * waves * kindBonus * tier.coinMultiplier *
            RunLoot.depthMultiplier(globalIndex)).roundToInt()
    }

    /**
     * Z czego liczony jest podglad: tyle mniej wiecej placi JEDNA fala (patrz [RunLoot]), a plansza
     * ma ich [StoryStage.waves]. Wczesniej podglad mial wlasny wzor z numerem planszy, a walka
     * placila plaska stawke z numeru fali - dwa rozne zrodla prawdy o tej samej liczbie, ktore
     * rozjezdzaly sie tym bardziej, im glebiej gracz zaszedl. Teraz obie strony przechodza przez
     * [RunLoot.depthMultiplier], wiec kafel na mapie i sakwa po walce mowia to samo.
     */
    private const val GOLD_PER_WAVE = 60f
    private const val XP_PER_WAVE = 25f
}

/**
 * Postep gracza w kampanii: dla kazdego poziomu trudnosci ([RunDifficulty]) - najwyzszy oczyszczony
 * globalny numer planszy (0 = jeszcze nic). Klucz mapy to [RunDifficulty.name], nie sam enum -
 * zwykla ochrona przy (de)serializacji.
 */
object StoryProgress {

    /** Pierwsza NIEZDOBYTA plansza na danym tierze - to na nia celuje "graj dalej". */
    fun frontier(cleared: Map<String, Int>, tier: RunDifficulty): Int = (cleared[tier.name] ?: 0) + 1

    /** Plansza jest dostepna do gry, gdy jest juz zdobyta (powtorka) albo jest wlasnie frontier. */
    fun isStagePlayable(cleared: Map<String, Int>, tier: RunDifficulty, globalIndex: Int): Boolean =
        globalIndex <= frontier(cleared, tier)

    /**
     * Tier jest odblokowany, gdy poprzedni zostal ukonczony w calosci (kampania od deski do deski) -
     * albo gdy to [RunDifficulty.Normal], ktory jest dostepny zawsze. Dokladnie jak w Idle Heroes:
     * najpierw przechodzisz cala kampanie na Normalnym, dopiero to odblokowuje Koszmar na tych
     * samych krainach.
     */
    fun isTierUnlocked(cleared: Map<String, Int>, tier: RunDifficulty): Boolean {
        val previous = RunDifficulty.entries.getOrNull(tier.ordinal - 1) ?: return true
        return (cleared[previous.name] ?: 0) >= StoryWorldCatalog.totalStages
    }

    /** Zapisuje nowy rekord - MAX z tego, co juz bylo, nigdy nie cofa postepu. */
    fun withCleared(cleared: Map<String, Int>, tier: RunDifficulty, globalIndex: Int): Map<String, Int> {
        val current = cleared[tier.name] ?: 0
        if (globalIndex <= current) return cleared
        return cleared + (tier.name to globalIndex)
    }

    fun isCampaignComplete(cleared: Map<String, Int>, tier: RunDifficulty): Boolean =
        (cleared[tier.name] ?: 0) >= StoryWorldCatalog.totalStages

    /**
     * Najdalsza zdobyta plansza NIEZALEZNIE od tieru - z niej liczy sie tempo nagrod idle
     * ([IdleRewards.ratePerHour]) i to, czy w ogole jest co przypominac ([ReminderPlan]).
     *
     * Maksimum ze wszystkich tierow, a nie z biezacego: gracz, ktory doszedl daleko na Normalnym
     * i wlasnie zaczyna Koszmar, nie moze na chwile stracic calego tempa kapania.
     */
    fun bestClearedStage(cleared: Map<String, Int>): Int = cleared.values.maxOrNull() ?: 0
}
