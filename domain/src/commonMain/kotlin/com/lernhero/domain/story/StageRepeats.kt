package com.lernhero.domain.story

import com.lernhero.domain.run.RunDifficulty

/**
 * Ile placi PONOWNE przejscie tej samej planszy kampanii.
 *
 * ## Po co to powstalo
 *
 * Nie bylo w kodzie ani jednego miejsca, ktore odrozniloby pierwsze przejscie od setnego. Boss na
 * ostatniej planszy kampanii to JEDNA fala, a placil za kazdym razem pelna stawke: 210 rudy,
 * 297 esencji, 420 kamienia, 830 XP i **piec losow**. Cala kampania na jednym tierze daje losow
 * dwadziescia jeden - czyli cztery powtorki jednego bossa byly warte tyle, co przejscie
 * wszystkich trzydziestu szesciu plansz.
 *
 * Skutek nie byl "za duzo nagrod", tylko zapadniecie sie calej reszty gry: zadna sciana nie byla
 * sciana (wystarczylo pospamowac znanego bossa), a endless - tryb zbudowany po to, zeby bylo gdzie
 * farmic - nie mial jak z tym konkurowac.
 *
 * Teraz kampania placi za POSTEP, a nie za powtarzanie: pierwsze przejscie pelna stawka, kolejne
 * coraz mniej, az do [FLOOR]. Kto chce farmic, idzie do endless - tam stawka rosnie z glebokoscia
 * ([com.lernhero.domain.run.EndlessMilestones]), czyli z tego, jak dobrze sie odpowiada.
 */
object StageRepeats {

    /**
     * Ulamek stawki przy kolejnych podejsciach. Indeks = ile razy ta plansza byla JUZ zdobyta:
     * 0 -> pierwsze przejscie (pelna stawka), 1 -> pierwsza powtorka (polowa), 2 -> 30%.
     */
    private val FRACTIONS = listOf(1f, 0.5f, 0.3f)

    /** Dalsze powtorki placa juz tyle i nigdy mniej - farmienie ma byc nieoplacalne, a nie zabronione. */
    const val FLOOR = 0.1f

    fun key(tier: RunDifficulty, globalStage: Int): String = "${tier.name}:$globalStage"

    /** Ile razy gracz zdobyl juz te plansze na tym tierze. */
    fun clearsOf(clears: Map<String, Int>, tier: RunDifficulty, globalStage: Int): Int =
        clears[key(tier, globalStage)] ?: 0

    /** Ulamek, ktorym mnozy sie CALY lup zejscia - XP, materialy, zloto i losy. */
    fun payoutFraction(previousClears: Int): Float =
        FRACTIONS.getOrElse(previousClears.coerceAtLeast(0)) { FLOOR }

    /** Czy to podejscie jest tym pierwszym - tylko ono otwiera skrzynie po bossie. */
    fun isFirstClear(clears: Map<String, Int>, tier: RunDifficulty, globalStage: Int): Boolean =
        clearsOf(clears, tier, globalStage) == 0

    /** Zapisuje kolejne zdobycie tej planszy. */
    fun withClear(clears: Map<String, Int>, tier: RunDifficulty, globalStage: Int): Map<String, Int> {
        val k = key(tier, globalStage)
        return clears + (k to (clears[k] ?: 0) + 1)
    }
}
