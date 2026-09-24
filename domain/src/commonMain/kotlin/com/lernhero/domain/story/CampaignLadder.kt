package com.lernhero.domain.story

import com.lernhero.domain.run.BattleKind
import com.lernhero.domain.run.RunLadder
import kotlin.math.pow

/**
 * Balans KAMPANII: jedna plansza = jeden dwudziestofalowy bieg, taki sam w rytmie jak wyprawa.
 *
 * ## Co bylo nie tak
 *
 * Plansza kampanii miala od jednej do czterech fal (`1 + stageInWorld / 2`), a boss - jedna. Przez
 * to kampania nie byla rozgrywka, tylko klikaniem: wchodzisz, odpowiadasz na dwa pytania, wychodzisz
 * z lupem. Nie bylo w niej miejsca ani na krzywa trudnosci (dwie fale to nie krzywa), ani na wybory
 * roguelike (karty ulepszen wypadaja co [BUFF_EVERY_WAVES] fal - przy trzech falach gracz widzial
 * jedna, albo zadnej), ani na wyzwanie: sila wroga zalezala prawie wylacznie od numeru planszy, czyli
 * od tego, jak daleko sie zaszlo, a nie od tego, jak sie GRA.
 *
 * ## Jak jest teraz
 *
 * Kazda plansza to [WAVES_PER_STAGE] fal z wlasna krzywa. Plansza zalicza sie dopiero po
 * dwudziestej fali - i dopiero to otwiera kolejna. Kto nie da rady, wraca na te sama plansze tyle
 * razy, ile chce (kampania nie blokuje powtorek, tylko tnie za nie lup - patrz [StageRepeats]).
 *
 * Trudnosc sklada sie z trzech NIEZALEZNYCH dzwigni, kazda o swojej roli:
 *  - **plansza** ([stageHpMultiplier]) - pulap calego biegu, rosnie liniowo przez cala kampanie;
 *  - **fala** ([waveMultiplier]) - gorka wewnatrz jednego biegu, wykladnicza, ale lagodna;
 *  - **tier mapy** (RunDifficulty) - Koszmar/Pieklo, mnoznik na wszystkim.
 *
 * ## Liczba, ktora tu naprawde rzadzi
 *
 * To nie jest sila wroga, tylko [relativeDifficulty]: o ile wrogowie wyprzedzili druzyne, ktora
 * rosnie W TRAKCIE biegu na kartach ulepszen. Sam mnoznik wroga nic nie znaczy, dopoki nie zestawi
 * sie go z tym, co gracz zebral po drodze.
 *
 * Cel dla dwudziestej fali to okolo **2.1** - czyli dwa razy trudniej niz na pierwszej. To jest
 * "ciezko, ale wykonalne przy dobrych odpowiedziach": mnoznik z paska czasu siega 1.5, passa
 * dorzuca 1.35, wiec gracz, ktory odpowiada szybko i trafnie, gra na ~2x mocniejszej druzynie niz
 * ten, ktory zgaduje. Sciana ma wypadac na quizie, nie na sprzecie - i to jest cala umowa tego
 * trybu.
 */
object CampaignLadder {

    /** Ile fal ma plansza. Po tylu jest zaliczona i otwiera nastepna. */
    const val WAVES_PER_STAGE = 20

    /**
     * Co tyle fal gracz dostaje karte ulepszenia - ta sama liczba co w wyprawie, bo to ten sam
     * system ([com.lernhero.domain.run.EndlessBuff]) i ta sama matematyka mocy druzyny.
     * Dwudziestofalowy bieg daje wiec szesc wyborow.
     */
    const val BUFF_EVERY_WAVES = RunLadder.BUFF_EVERY_WAVES

    /** Co tyle fal zamiast tlumu wchodzi jeden boss: fale 10 i 20. */
    const val BOSS_INTERVAL = 10

    /** Co tyle fal wchodzi elita - z tego wypadaja fale 5 i 15 (10 i 20 zabiera boss). */
    const val ELITE_INTERVAL = 5

    /**
     * Wzrost sily wrogow na fale w obrebie planszy.
     *
     * 7% na fale przy szesciu kartach ulepszen do dwudziestej fali daje [relativeDifficulty] ~2.1
     * na mecie. Wyprawa ma tu 6% ([RunLadder.WAVE_STEP]) i konczy blizej 1.8 - kampania ma byc
     * odrobine ostrzejsza, bo w niej meta jest WARUNKIEM postepu, a nie tylko rekordem.
     */
    const val WAVE_STEP = 1.07f

    /**
     * Wzrost za dwudziesta fala. Plansza jest juz zaliczona, wiec kazda kolejna fala to czysty
     * bonus - i dlatego wolno jej byc sciana: 12% na fale przy ~3% przyrostu druzyny konczy bieg
     * w kilka fal.
     */
    const val OVERTIME_STEP = 1.12f

    /**
     * Wzrost HP/ataku wrogow z GLEBOKOSCIA planszy - liniowy, przez cala kampanie.
     *
     * Te dwie liczby siedzialy dotad w BattleViewModel (LEVEL_HP_STEP/LEVEL_ATK_STEP), a
     * [com.lernhero.domain.run.RunLoot.depthMultiplier] odwolywal sie do nich komentarzem - czyli
     * dwa pliki trzymaly jedna umowe balansu i nic ich nie pilnowalo. Teraz jest jedno zrodlo.
     */
    const val STAGE_HP_STEP = 0.12f
    const val STAGE_ATTACK_STEP = 0.09f

    /**
     * O ile mocniejsza jest OSTATNIA plansza krainy (ta z bossem) od zwyklej na tej samej
     * glebokosci. To jest jedyne, co zostalo z dawnego podzialu "plansza normalna / plansza boss":
     * rodzaj walki wynika teraz z numeru fali, a nie z planszy.
     */
    const val BOSS_STAGE_MULTIPLIER = 1.22f

    /** Co [BOSS_INTERVAL] fala to boss, co [ELITE_INTERVAL] - elita, reszta to tlum. */
    fun kindFor(wave: Int): BattleKind = when {
        wave > 0 && wave % BOSS_INTERVAL == 0 -> BattleKind.Boss
        wave > 0 && wave % ELITE_INTERVAL == 0 -> BattleKind.Elite
        else -> BattleKind.Normal
    }

    /** Czy doczyszczenie tej fali zalicza plansze (i odblokowuje kolejna). */
    fun clearsStage(wave: Int): Boolean = wave >= WAVES_PER_STAGE

    /** Ile fal zostalo do zaliczenia planszy. Zero i mniej = gracz jest juz za meta. */
    fun wavesToClear(wave: Int): Int = WAVES_PER_STAGE - wave

    /** Czy po tej fali wypada karta ulepszenia. */
    fun offersUpgrade(wave: Int): Boolean = wave > 0 && wave % BUFF_EVERY_WAVES == 0

    /** Gorka wewnatrz jednego biegu - lagodna do mety, stroma za nia. */
    fun waveMultiplier(wave: Int): Float {
        val safe = wave.coerceAtLeast(1)
        val inStage = WAVE_STEP.toDouble().pow((minOf(safe, WAVES_PER_STAGE) - 1).toDouble())
        val overtime = if (safe > WAVES_PER_STAGE) {
            OVERTIME_STEP.toDouble().pow((safe - WAVES_PER_STAGE).toDouble())
        } else {
            1.0
        }
        return (inStage * overtime).toFloat()
    }

    /** Pulap calego biegu z glebokosci planszy. [globalStage] jest 1-indeksowany. */
    fun stageHpMultiplier(globalStage: Int, isBossStage: Boolean = false): Float =
        (1f + (globalStage - 1).coerceAtLeast(0) * STAGE_HP_STEP) *
            if (isBossStage) BOSS_STAGE_MULTIPLIER else 1f

    fun stageAttackMultiplier(globalStage: Int, isBossStage: Boolean = false): Float =
        (1f + (globalStage - 1).coerceAtLeast(0) * STAGE_ATTACK_STEP) *
            if (isBossStage) BOSS_STAGE_MULTIPLIER else 1f

    /**
     * Ile razy mocniejsza jest druzyna na [wave] niz na pierwszej fali - z samych kart ulepszen
     * zebranych w tym biegu. Sprzet, poziom i kompani wchodza do biegu juz na starcie, wiec
     * skracaja sie w [relativeDifficulty] i nie ma ich w tym wzorze.
     */
    fun partyPowerAt(wave: Int): Float = RunLadder.partyPowerAt(wave)

    /**
     * PRAWDZIWA trudnosc fali: o ile wrogowie wyprzedzili druzyne wzgledem startu biegu.
     * 1.0 = tak samo trudno jak na pierwszej fali, 2.0 = dwa razy trudniej.
     *
     * Plansza i tier sie tu skracaja - one podnosza CALY bieg, a nie jego ksztalt. Dlatego ta
     * liczba odpowiada na jedyne sensowne pytanie o krzywa: "czy w miare grania robi sie ciezej
     * szybciej, niz gracz rosnie?".
     */
    fun relativeDifficulty(wave: Int): Float = waveMultiplier(wave) / partyPowerAt(wave)

    /**
     * Orientacyjna fala, na ktorej bieg sie konczy: pierwsza, na ktorej wrogowie wyprzedzaja
     * druzyne o [wipeAt] razy. Sluzy WYLACZNIE testom balansu - walka jej nie czyta.
     */
    fun estimatedWall(wipeAt: Float = 2.5f): Int {
        var wave = 1
        while (wave < 200 && relativeDifficulty(wave) < wipeAt) wave++
        return wave
    }
}
