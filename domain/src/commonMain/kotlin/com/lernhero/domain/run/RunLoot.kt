package com.lernhero.domain.run

import com.lernhero.domain.quiz.QuizDifficulty
import kotlin.math.roundToInt

/**
 * Ile jedno zejscie daje XP i materialow. Wszystko w jednym miejscu, bo to jest krzywa progresji
 * calej gry - rozrzucona po viewmodelach nie dalaby sie ani przestroic, ani przetestowac.
 *
 * Dwie zasady, ktore trzymaja balans:
 *
 * 1. **XP plynie z NAUKI, nie tylko z bicia.** Dobra odpowiedz daje XP wprost ([xpForAnswer]),
 *    i to jest wieksza czesc zysku niz sama oczyszczona fala. Gracz, ktory odpowiada poprawnie,
 *    awansuje wyraznie szybciej niz ten, ktory klika na oslep i wygrywa przewaga statystyk.
 * 2. **Materialy plyna z WALKI.** Ruda i esencja wypadaja za fale i za bossow, bo to one maja
 *    dawac powod, zeby wracac do lochu nawet z pelna sakwa.
 * 3. **Plansza placi za to, co na niej stoi** ([depthMultiplier]). Przez dluzszy czas nie placila:
 *    stawka liczyla sie wylacznie z numeru FALI, a kampania ma ich najwyzej trzy - wiec plansza 36
 *    Otchlani, z wrogami o piec razy grubszymi, wyplacala dokladnie tyle samo co plansza 1 Lasu.
 *    Skutkiem nie bylo "za malo zlota", tylko zerwana petla: gracz nie mial powodu wracac glebiej,
 *    a poziom bohatera przestawal nadazac za krzywa wrogow dokladnie w polowie kampanii.
 */
object RunLoot {

    // ---------------------------------------------------------------- XP

    /** XP za jedno pytanie. Zla odpowiedz tez cos daje - probowanie ma byc lepsze niz omijanie. */
    fun xpForAnswer(correct: Boolean, difficulty: QuizDifficulty): Int {
        val base = when (difficulty) {
            QuizDifficulty.Easy -> 8
            QuizDifficulty.Medium -> 13
            QuizDifficulty.Hard -> 20
            QuizDifficulty.Expert -> 30
        }
        return if (correct) base else (base * WRONG_ANSWER_XP_FRACTION).roundToInt()
    }

    /** XP za oczyszczona fale - rosnie z numerem fali, rodzajem walki i GLEBOKOSCIA planszy. */
    fun xpForWave(wave: Int, kind: BattleKind, difficulty: RunDifficulty, stage: Int = NO_STAGE): Int {
        val base = 12 + wave * 3
        return (base * kind.lootMultiplier * difficulty.coinMultiplier * depthMultiplier(stage))
            .roundToInt().coerceAtLeast(1)
    }

    /**
     * Ile razy hojniejsza jest plansza na glebokosci [stage] od pierwszej.
     *
     * Krok jest CELOWO rowny temu, o ile na tej samej glebokosci puchna wrogowie (LEVEL_HP_STEP
     * w BattleViewModel = 0.12). To jedyna proporcja, ktora da sie obronic: stawka rosnie dokladnie
     * w tempie oporu, wiec kolejna plansza jest zawsze warta tyle samo pracy co poprzednia - ani
     * nie robi sie darmowym farmieniem, ani pulapka, w ktorej gracz traci czas na walke placaca
     * jak rozgrzewka.
     *
     * [stage] = [NO_STAGE] (endless, walka testowa) nie zmienia niczego - tam glebokosci nie ma,
     * jej role pelni numer fali.
     */
    fun depthMultiplier(stage: Int): Float =
        1f + (stage - 1).coerceAtLeast(0) * DEPTH_STEP

    // ---------------------------------------------------------- Materialy

    /** Esencja z fali - paliwo Summon Center (kompani i zaklecia). */
    fun essenceForWave(wave: Int, kind: BattleKind, stage: Int = NO_STAGE): Int =
        ((3 + wave / 2f) * kind.lootMultiplier * depthMultiplier(stage)).roundToInt().coerceAtLeast(1)

    /** Ruda z fali - material na przedmioty w Kuzni. */
    fun ironForWave(wave: Int, kind: BattleKind, stage: Int = NO_STAGE): Int =
        ((2 + wave / 3f) * kind.lootMultiplier * depthMultiplier(stage)).roundToInt().coerceAtLeast(1)

    /**
     * Jednorazowa premia za pokonanie elity albo bossa - to ona robi z bossa cel sam w sobie.
     *
     * Kamien leci TYLKO stad (zwykle fale go nie daja): kamieniolom zostaje glownym zrodlem, a
     * lup z bossa jest dodatkiem, ktory przyspiesza kolejny craft - a nie zamiennikiem budynku.
     *
     * **Losy tez leca stad - i to jest naprawa suszy, ktora unieruchamiala cala kolekcje.** Do tej
     * pory jedynym zrodlem losow bylo kolo fortuny ze skrzyni, gdzie pole "los" ma wage 15 na 90.
     * Oczekiwana zdobycz z JEDNEJ skrzyni to 0.23-0.42 losa, czyli okolo dziewieciu losow z calej
     * kampanii przejsc na wszystkich czterech tierach. Tymczasem piata gwiazdka JEDNEJ karty
     * kosztuje 19 kopii, a komplet kolekcji ponad dwiescie losowan. Gwiazdki, rozpuszczanie
     * duplikatow w esencje i caly tor poziomow kompanow byly wiec zbudowane na walucie, ktorej gra
     * produkowala kilka sztuk NA ZAWSZE. Skrzynia zostaje jackpotem; tu dochodzi pewny, liczalny
     * przydzial, zeby kolekcja w ogole ruszyla z miejsca.
     */
    fun bossBonus(kind: BattleKind, dungeonLevel: Int): BossBonus {
        val depth = (dungeonLevel).coerceAtLeast(1)
        return when (kind) {
            BattleKind.Normal -> BossBonus()
            BattleKind.Elite -> BossBonus(
                iron = 12 + depth * 2,
                essence = 18 + depth * 3,
                stone = 20 + depth * 4,
                xp = 40 + depth * 8,
                tickets = 1
            )
            BattleKind.Boss -> BossBonus(
                iron = 30 + depth * 5,
                essence = 45 + depth * 7,
                stone = 60 + depth * 10,
                xp = 110 + depth * 20,
                tickets = BOSS_TICKETS + depth / STAGES_PER_EXTRA_TICKET
            )
        }
    }

    private const val WRONG_ANSWER_XP_FRACTION = 0.25f

    /** Brak planszy kampanii - endless i walka testowa. Patrz [depthMultiplier]. */
    const val NO_STAGE = 0

    /** Pewne losy za bossa - patrz [bossBonus]. */
    private const val BOSS_TICKETS = 2

    /** Co tyle plansz w glab boss daje jeden los wiecej. */
    private const val STAGES_PER_EXTRA_TICKET = 12

    /** O tyle hojniejsza jest kazda kolejna plansza kampanii - patrz [depthMultiplier]. */
    private const val DEPTH_STEP = 0.12f
}

data class BossBonus(
    val iron: Int = 0,
    val essence: Int = 0,
    val stone: Int = 0,
    val xp: Int = 0,
    /** Losy do Summon Center - JEDYNA pewna ich zrodlo w grze, patrz [RunLoot.bossBonus]. */
    val tickets: Int = 0
)

/** Ile razy hojniejsza jest walka z elita/bossem od zwyklej fali. */
val BattleKind.lootMultiplier: Float
    get() = when (this) {
        BattleKind.Normal -> 1f
        BattleKind.Elite -> 1.6f
        BattleKind.Boss -> 2.4f
    }
