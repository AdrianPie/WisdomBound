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

    /** XP za oczyszczona fale - rosnie z numerem fali i z rodzajem walki. */
    fun xpForWave(wave: Int, kind: BattleKind, difficulty: RunDifficulty): Int {
        val base = 12 + wave * 3
        return (base * kind.lootMultiplier * difficulty.coinMultiplier).roundToInt().coerceAtLeast(1)
    }

    // ---------------------------------------------------------- Materialy

    /** Esencja z fali - paliwo Summon Center (kompani i zaklecia). */
    fun essenceForWave(wave: Int, kind: BattleKind): Int =
        (3 + wave / 2f).roundToInt().coerceAtLeast(1).times(kind.lootMultiplier).roundToInt()

    /** Ruda z fali - material na przedmioty w Kuzni. */
    fun ironForWave(wave: Int, kind: BattleKind): Int =
        (2 + wave / 3f).roundToInt().coerceAtLeast(1).times(kind.lootMultiplier).roundToInt()

    /**
     * Jednorazowa premia za pokonanie elity albo bossa - to ona robi z bossa cel sam w sobie.
     *
     * Drewno i kamien leca TYLKO stad (zwykle fale ich nie daja): budynki produkcyjne zostaja
     * glownym zrodlem obu, a lup z bossa jest dodatkiem, ktory przyspiesza kolejny craft - a nie
     * zamiennikiem tartaku.
     */
    fun bossBonus(kind: BattleKind, dungeonLevel: Int): BossBonus {
        val depth = (dungeonLevel).coerceAtLeast(1)
        return when (kind) {
            BattleKind.Normal -> BossBonus()
            BattleKind.Elite -> BossBonus(
                iron = 12 + depth * 2,
                essence = 18 + depth * 3,
                wood = 20 + depth * 4,
                stone = 20 + depth * 4,
                xp = 40 + depth * 8
            )
            BattleKind.Boss -> BossBonus(
                iron = 30 + depth * 5,
                essence = 45 + depth * 7,
                wood = 60 + depth * 10,
                stone = 60 + depth * 10,
                xp = 110 + depth * 20
            )
        }
    }

    private const val WRONG_ANSWER_XP_FRACTION = 0.25f
}

data class BossBonus(
    val iron: Int = 0,
    val essence: Int = 0,
    val wood: Int = 0,
    val stone: Int = 0,
    val xp: Int = 0
)

/** Ile razy hojniejsza jest walka z elita/bossem od zwyklej fali. */
val BattleKind.lootMultiplier: Float
    get() = when (this) {
        BattleKind.Normal -> 1f
        BattleKind.Elite -> 1.6f
        BattleKind.Boss -> 2.4f
    }
