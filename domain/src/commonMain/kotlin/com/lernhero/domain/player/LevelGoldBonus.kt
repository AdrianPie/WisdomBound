package com.lernhero.domain.player

/**
 * Premia do zlota za poziom bohatera: +1% na poziom, z sufitem [MAX_BONUS].
 *
 * Celowo MALA. Glowna krzywa zlota idzie z GLEBOKOSCI planszy ([com.lernhero.domain.run.RunLoot.depthMultiplier]),
 * czyli z tego, jak daleko gracz doszedl - poziom ma byc tylko odczuwalnym dodatkiem ("awansowalem,
 * sakwa troche urosla"), a nie drugim mnoznikiem, ktory razem z pierwszym wystrzeliwuje zloto
 * w kosmos. 20. poziom = +19%, 51. i dalej = +50%.
 *
 * Dziala na zloto z walk (fale i pocieszenie po porazce) i z kapaczki idle. Nagrody o stalej,
 * z gory podanej kwocie (cele dzienne, osiagniecia) zostaja stale - gracz ma widziec dokladnie te
 * liczbe, ktora dostanie.
 */
object LevelGoldBonus {

    const val PER_LEVEL = 0.01f
    const val MAX_BONUS = 0.5f

    /** Mnoznik zlota na tym poziomie: 1.0 na pierwszym, najwyzej 1.5. */
    fun multiplier(heroLevel: Int): Float =
        1f + ((heroLevel - 1).coerceAtLeast(0) * PER_LEVEL).coerceAtMost(MAX_BONUS)

    /** Premia w procentach - do pokazania na karcie postaci ("Gold +19%"). */
    fun percent(heroLevel: Int): Int = ((multiplier(heroLevel) - 1f) * 100f + 0.5f).toInt()
}
