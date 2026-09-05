package com.lernhero.domain.fitness

/**
 * Dzienna skrzynia za kroki: przechodzisz [STEPS_REQUIRED] krokow, otwierasz JEDEN raz, dostajesz
 * losowa nagrode z tego samego kola co skrzynia po bossie (patrz `ChestSlot`/`ChestWheel`).
 *
 * Celowo nie jest to kolejny [FitnessGoal]: cele placa staly, znany z gory ryczalt zlota, a tu
 * chodzi o cos odwrotnego - o losowanie, ktore czasem trafia legendarny przedmiot. Wspolny jest
 * tylko sposob zapamietywania odbioru: [CLAIM_ID] laduje w tej samej mapie `dailyGoalClaims`, wiec
 * "raz dziennie" pilnuje ten sam, juz sprawdzony kod repozytorium.
 */
object StepChest {

    /** Klucz w `Player.dailyGoalClaims`. Musi byc rozlaczny z id celow z [FitnessGoalCatalog]. */
    const val CLAIM_ID = "daily_step_chest"

    /** Prog otwarcia. Okragle 10k to najpowszechniejszy dzienny cel krokow - nie wymysla nowego. */
    const val STEPS_REQUIRED = 10_000

    /** Ile krokow ponad prog podnosi kolo o jeden poziom - patrz [wheelLevel]. */
    private const val STEPS_PER_EXTRA_LEVEL = 5_000

    /** Sufit poziomu kola, zeby chodzenie caly dzien nie robilo z jackpota pewniaka. */
    private const val MAX_WHEEL_LEVEL = 5

    fun isUnlocked(snapshot: FitnessSnapshot): Boolean = snapshot.steps >= STEPS_REQUIRED

    /** 0f..1f - postep do otwarcia, gotowy do narysowania jako pasek. */
    fun progress(snapshot: FitnessSnapshot): Float =
        (snapshot.steps.toFloat() / STEPS_REQUIRED).coerceIn(0f, 1f)

    fun stepsRemaining(snapshot: FitnessSnapshot): Int =
        (STEPS_REQUIRED - snapshot.steps).coerceAtLeast(0)

    /**
     * Poziom kola fortuny dla tylu krokow - wchodzi w `ChestWheel.roll` jako "glebokosc lochu".
     * Kto zrobil 25k krokow, ma realnie lepsze wagi na rzadkie pola niz kto ledwo dobil do progu,
     * ale roznica jest w granicach kilku procent, a nie rzedu wielkosci.
     */
    fun wheelLevel(steps: Int): Int {
        val extra = ((steps - STEPS_REQUIRED).coerceAtLeast(0)) / STEPS_PER_EXTRA_LEVEL
        return (1 + extra).coerceAtMost(MAX_WHEEL_LEVEL)
    }
}
