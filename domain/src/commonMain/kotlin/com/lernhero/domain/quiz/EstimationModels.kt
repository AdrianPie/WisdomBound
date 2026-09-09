package com.lernhero.domain.quiz

import kotlin.math.abs
import kotlin.random.Random

/**
 * Pytanie "oszacuj wartosc" - na razie tylko warianty "zgadnij rok" (patrz [EstimationQuestions]),
 * ale [minValue]/[maxValue]/[unit] sa juz na tyle ogolne, zeby pomiescic inne skale liczbowe pozniej.
 * Uzywane wylacznie przez specjalne zdarzenia elit/bossow w walce - to nie jest zwykly quiz tury.
 */
data class EstimationEntry(
    val id: String,
    val prompt: String,
    val minValue: Int,
    val maxValue: Int,
    val correctValue: Int,
    val unit: String,
    /**
     * Kod jezyka, dla ktorego to pytanie ma sens - tak samo jak
     * [com.lernhero.domain.quiz.QuizCategory.localeCode]. Null = pytanie globalne.
     *
     * Pula szacowan nie ma kategorii, wiec bez tego pola "w ktorym roku wybuchlo powstanie
     * styczniowe?" trafialoby na zdarzenie bossa u kazdego gracza na swiecie - filtruje
     * [EstimationBank.forLanguage].
     */
    val localeCode: String? = null,
    /**
     * Ile jednostek obok wciaz liczy sie za STRZAL W DZIESIATKE.
     *
     * To najwazniejsza liczba przy ocenianiu i musi byc WLASNA dla kazdego pytania: 10 lat pomylki
     * przy dacie urodzin Mahometa to praktycznie trafienie, a 10 lat przy premierze iPhone'a to
     * kompletne pudlo. Wczesniej ocena szla od szerokosci suwaka, wiec kazde pytanie bylo mierzone
     * ta sama miarka i te dwa przypadki dostawaly identyczna ocene.
     *
     * Domyslnie wyliczana z epoki ([defaultYearTolerance]) - im blizej wspolczesnosci, tym ostrzej.
     */
    val tolerance: Int = defaultYearTolerance(correctValue)
) {
    /**
     * Blad odpowiedzi w "tolerancjach": 0 = idealnie, 1 = na granicy trafienia, 3 = trzy razy dalej
     * niz wynosi tolerancja tego pytania. To jest jedyna miara, jakiej powinno uzywac ocenianie.
     */
    fun errorUnits(guess: Int): Float =
        abs(guess - correctValue).toFloat() / tolerance.coerceAtLeast(1)

    /**
     * Celnosc 0..1 liczona wzgledem tolerancji TEGO pytania.
     *
     * Spada do zera dopiero przy [MISS_AT_UNITS] tolerancjach bledu - dzieki temu miedzy
     * "prawie idealnie" a "kompletne pudlo" jest realna, wyczuwalna skala, a nie skok.
     */
    fun closeness(guess: Int): Float =
        (1f - errorUnits(guess) / MISS_AT_UNITS).coerceIn(0f, 1f)

    /**
     * Widelki suwaka dla JEDNEJ rozgrywki tego pytania.
     *
     * Suwak obejmuje [SLIDER_WINDOW_UNITS] tolerancji w kazda strone, a nie caly zakres epoki -
     * bez tego przy skali -1000..2025 jeden piksel wart byl kilkanascie lat i trafienie w konkretny
     * rok bylo fizycznie niewykonalne.
     *
     * Poprawna wartosc jest CELOWO przesunieta losowo z centrum: gdyby zawsze siedziala na srodku,
     * gracz trafialby w nia nie znajac odpowiedzi, po samym ksztalcie suwaka.
     */
    fun sliderRange(random: Random): IntRange {
        val halfWidth = (tolerance * SLIDER_WINDOW_UNITS).coerceAtLeast(4)
        // Przesuniecie do +/-40% polowy szerokosci - odpowiedz laduje gdzies w srodkowych 2/3 suwaka,
        // nigdy zaraz przy krawedzi (tam bylaby rownie latwa do zgadniecia jak na srodku).
        val drift = ((random.nextFloat() - 0.5f) * 0.8f * halfWidth).toInt()
        val center = correctValue + drift
        return (center - halfWidth).coerceAtLeast(minValue)..(center + halfWidth).coerceAtMost(maxValue)
    }

    companion object {
        /** Przy ilu tolerancjach bledu celnosc spada do zera. */
        const val MISS_AT_UNITS = 6f

        /** Ile tolerancji miesci sie na polowie suwaka. */
        const val SLIDER_WINDOW_UNITS = 7
    }
}

/** Czy to pytanie wolno pokazac graczowi grajacemu w jezyku [languageCode]. */
fun EstimationEntry.availableIn(languageCode: String): Boolean =
    localeCode == null || localeCode == languageCode

/**
 * Domyslna tolerancja dla pytania o rok, dobrana wedlug EPOKI.
 *
 * Im blizej wspolczesnosci, tym gestsze i lepiej udokumentowane wydarzenia, wiec i oczekiwana
 * precyzja rosnie. Dla starozytnosci nikt nie pamieta dokladnego roku i nie powinien - liczy sie
 * trafienie w stulecie.
 */
fun defaultYearTolerance(year: Int): Int = when {
    year < 0 -> 150
    year < 1000 -> 80
    year < 1500 -> 40
    year < 1800 -> 15
    year < 1900 -> 8
    year < 1980 -> 4
    else -> 2
}
