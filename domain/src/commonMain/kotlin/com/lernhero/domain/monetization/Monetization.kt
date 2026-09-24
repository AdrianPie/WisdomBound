package com.lernhero.domain.monetization

import kotlinx.serialization.Serializable

/**
 * Co daje subskrypcja i ile mozna bez niej. Jedyne miejsce z tymi liczbami - czytaja je czytnik
 * (limit tlumaczen), ekran talii (generowanie AI) i paywall (lista korzysci), a serwer ma swoja
 * kopie limitu AI w ENV, bo to on go egzekwuje.
 */
object PremiumLimits {
    /** Tlumaczen slow/zdan w czytniku dziennie bez subskrypcji. Z subskrypcja - bez limitu. */
    const val FREE_TRANSLATIONS_PER_DAY = 5

    /** Generowan talii przez AI miesiecznie Z subskrypcja. Bez niej generowanie jest zablokowane. */
    const val PREMIUM_AI_QUIZZES_PER_MONTH = 200

    /**
     * Diamenty do odebrania codziennie z subskrypcja - odpowiednik "karty miesiecznej" z gier.
     * 30 dni x 50 = 1500 diamentow za cene miesiaca, czyli kilka razy wiecej niz pakiet za te same
     * pieniadze: subskrypcja ma byc NAJLEPSZYM zakupem w sklepie dla kogos, kto gra codziennie.
     */
    const val DAILY_GEMS = 50

    /** Klucz odbioru w `Player.dailyGoalClaims` - ta sama mapa co cele dzienne. */
    const val DAILY_GEMS_CLAIM_ID = "premium_daily_gems"
}

/** Nagrody za obejrzenie reklamy (albo za darmo, gdy gracz ma subskrypcje). */
object AdRewards {
    /** Ile razy dziennie mozna odebrac zloto za reklame z ekranu Domu. */
    const val GOLD_ADS_PER_DAY = 5

    /**
     * Zloto za jedna reklame. Rosnie z poziomem bohatera, ale POWOLI: +3% bazy na poziom, czyli
     * 10. poziom placi ~1.3x, a 50. ~2.5x tyle co pierwszy. Wczesniej bylo +10% na poziom (50. poziom
     * = prawie 6x) - awans robil z reklamy glowne zrodlo zlota, a nie drobny dodatek. Ta sama
     * krzywa skaluje skrzynie i paczki w sklepie ([GemValue.progressFactor]), wiec poziom daje
     * odczuwalny, ale maly progres wszedzie naraz.
     */
    fun goldFor(heroLevel: Int): Int = GOLD_BASE + GOLD_PER_LEVEL * (heroLevel - 1).coerceAtLeast(0)

    /** Wskrzeszenie po porazce stawia kazdego z druzyny na tym ulamku jego maksymalnego HP. */
    const val REVIVE_HP_FRACTION = 0.5f

    /** Cena wskrzeszenia za diamenty - dla tych, ktorzy nie chca albo nie moga obejrzec reklamy. */
    const val REVIVE_GEMS = 40

    private const val GOLD_BASE = 100
    private const val GOLD_PER_LEVEL = 3
}

/**
 * Licznik, ktory sam sie zeruje z nowym dniem. Trzymamy dzien i liczbe, a nie "pozostalo" -
 * wczorajszy stan przeczytany dzis po prostu daje zero, bez zadnego zadania czyszczacego.
 */
@Serializable
data class DailyCounter(
    val dateKey: String = "",
    val count: Int = 0
) {
    fun countOn(day: String): Int = if (dateKey == day) count else 0

    fun increment(day: String): DailyCounter = DailyCounter(day, countOn(day) + 1)

    fun remainingOn(day: String, limit: Int): Int = (limit - countOn(day)).coerceAtLeast(0)
}
