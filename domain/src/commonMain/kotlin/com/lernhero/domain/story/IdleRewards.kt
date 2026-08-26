package com.lernhero.domain.story

import kotlin.math.roundToInt

/**
 * Nagrody idle - to, co "kapie" z kampanii, gdy gracza nie ma w grze. Odpowiednik skrzyni AFK
 * z idli: im dalej doszedles, tym wiecej na godzine, a magazyn ma sufit, wiec po [CAP_HOURS]
 * przestaje sie zbierac (to jest wlasnie ten delikatny przymus, zeby zajrzec raz dziennie).
 *
 * Cala matematyka jest bezstanowa i liczona z ROZNICY CZASU, dokladnie jak
 * [com.lernhero.domain.town.ProductionMath] - dzieki temu 10 godzin offline i 10 sekund
 * z otwartym ekranem przechodza ta sama sciezka i nie trzeba niczego tykac w tle.
 */
object IdleRewards {

    /** Po tylu godzinach magazyn jest pelny i nic wiecej sie nie zbiera. */
    const val CAP_HOURS = 12f

    private const val MILLIS_PER_HOUR = 3_600_000f

    /** Bez zadnej zdobytej planszy nie ma z czego kapac - kampania musi sie najpierw zaczac. */
    fun ratePerHour(highestClearedStage: Int): IdleRate {
        if (highestClearedStage <= 0) return IdleRate()
        return IdleRate(
            gold = 30f + highestClearedStage * 6f,
            xp = 12f + highestClearedStage * 3f,
            iron = 2f + highestClearedStage * 0.5f,
            essence = 3f + highestClearedStage * 0.7f
        )
    }

    /**
     * Ile czeka do odebrania. [lastCollectedAt] = 0 oznacza "jeszcze nigdy nie odbierano" - wtedy
     * liczymy od [now], zeby nowe konto nie dostalo naliczenia od epoki Uniksa.
     */
    fun pendingFor(highestClearedStage: Int, lastCollectedAt: Long, now: Long): IdleReward {
        if (lastCollectedAt <= 0L || now <= lastCollectedAt) return IdleReward()

        val hours = ((now - lastCollectedAt) / MILLIS_PER_HOUR).coerceAtMost(CAP_HOURS)
        val rate = ratePerHour(highestClearedStage)
        return IdleReward(
            gold = (rate.gold * hours).roundToInt(),
            xp = (rate.xp * hours).roundToInt(),
            iron = (rate.iron * hours).roundToInt(),
            essence = (rate.essence * hours).roundToInt(),
            hoursAccrued = hours
        )
    }

    /** Ulamek zapelnienia magazynu - pasek "ile jeszcze zmiesci sie zanim przestanie kapac". */
    fun storageFraction(lastCollectedAt: Long, now: Long): Float {
        if (lastCollectedAt <= 0L || now <= lastCollectedAt) return 0f
        return (((now - lastCollectedAt) / MILLIS_PER_HOUR) / CAP_HOURS).coerceIn(0f, 1f)
    }
}

data class IdleRate(
    val gold: Float = 0f,
    val xp: Float = 0f,
    val iron: Float = 0f,
    val essence: Float = 0f
)

data class IdleReward(
    val gold: Int = 0,
    val xp: Int = 0,
    val iron: Int = 0,
    val essence: Int = 0,
    val hoursAccrued: Float = 0f
) {
    val isEmpty: Boolean get() = gold <= 0 && xp <= 0 && iron <= 0 && essence <= 0
}
