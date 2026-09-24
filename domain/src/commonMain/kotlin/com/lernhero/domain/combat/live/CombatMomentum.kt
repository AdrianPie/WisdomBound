package com.lernhero.domain.combat.live

/**
 * Pasek czasu na odpowiedz w biezacej turze - startuje pelny, gdy pojawia sie pytanie, i opada w
 * kilku segmentach. Im wiecej segmentow zostalo w chwili odpowiedzi, tym mocniej drużyna uderzy w
 * tej turze - im mniejszy pasek, tym mniejszy dmg.
 */
object AnswerTimer {
    /** 18s - podwojone z 9s, bo nawet to bylo za szybko na spokojne przeczytanie pytania i odpowiedz. */
    const val WINDOW_MS = 18000L
    const val SEGMENTS = 8

    /** Mnoznik obrazen dla kazdego segmentu - indeks 0 = ostatni zostaly segment (najwolniej), ostatni = pelny pasek. */
    private val MULTIPLIERS = listOf(0.5f, 0.64f, 0.79f, 0.93f, 1.07f, 1.21f, 1.36f, 1.5f)

    fun segmentIndexFor(remainingFraction: Float): Int =
        (remainingFraction.coerceIn(0f, 1f) * SEGMENTS).toInt().coerceIn(0, SEGMENTS - 1)

    fun multiplierFor(remainingFraction: Float): Float = MULTIPLIERS[segmentIndexFor(remainingFraction)]

    /**
     * Ile czasu dostaje KONKRETNE pytanie. Dwie rzeczy je zmieniaja i obie sa celowe.
     *
     * **Rozmiar pytania.** Cztery odpowiedzi czyta sie inaczej niz ukladanka z szesciu elementow
     * albo "zaznacz wszystkie prawdziwe" z siedmiu opcji. Staly czas dla wszystkich znaczyl, ze
     * dlugie pytania byly trudniejsze nie tresci, tylko przez czytanie - a to nie jest ta trudnosc,
     * o ktora w tej grze chodzi. Kazda opcja ponad cztery dokłada [MS_PER_EXTRA_OPTION].
     *
     * **Glebokosc fali.** Im dalej w biegu, tym mniej czasu - do [MIN_WAVE_FACTOR] wartosci
     * bazowej. To jest druga (obok sily wrogow) krzywa trudnosci biegu, i ta bardziej uczciwa:
     * nie zabiera graczowi mocy, tylko zadaje, zeby wiedzial odpowiedz OD RAZU, zamiast dochodzic
     * do niej przez eliminacje. Na dwudziestej fali zostaje okolo 13 sekund zamiast osiemnastu.
     *
     * Pasek na ekranie i mnoznik salwy licza sie z UŁAMKA tego okna, wiec obie te rzeczy dostraja
     * sie same - nie ma drugiego miejsca, ktore trzeba by zmienic razem z ta funkcja.
     */
    fun windowFor(optionCount: Int, wave: Int): Long {
        val extras = (optionCount - BASE_OPTIONS).coerceAtLeast(0)
        val sized = WINDOW_MS + extras * MS_PER_EXTRA_OPTION
        val factor = (1f - (wave.coerceAtLeast(1) - 1) * WAVE_PRESSURE_PER_WAVE)
            .coerceAtLeast(MIN_WAVE_FACTOR)
        return (sized * factor).toLong().coerceAtLeast(MIN_WINDOW_MS)
    }

    /** Ile opcji miesci sie w bazowym czasie - tyle ma zwykle pytanie wyboru. */
    private const val BASE_OPTIONS = 4
    private const val MS_PER_EXTRA_OPTION = 1500L

    /** O ile krocej na kazda fale i jak nisko moze to zejsc (0.62 = ~11s z osiemnastu). */
    private const val WAVE_PRESSURE_PER_WAVE = 0.015f
    private const val MIN_WAVE_FACTOR = 0.62f

    /** Twarda podloga - ponizej tego nie da sie przeczytac nawet krotkiego pytania. */
    private const val MIN_WINDOW_MS = 8000L
}

/**
 * Licznik ostatnich odpowiedzi - dziesiec "kratek", jak seria karnych. Piec trafien z rzedu (nawet
 * miedzy cyklami) odpala ulti; pelne dziesiec zamyka cykl i rozlicza wynik na buff albo debuff.
 */
data class StreakTracker(
    val pips: List<Boolean?> = List(CYCLE_SIZE) { null },
    val consecutiveCorrect: Int = 0
)

data class StreakOutcome(
    val tracker: StreakTracker,
    val ultiTriggered: Boolean,
    val cycleResult: CycleOutcome?
)

enum class CycleOutcome(val attackMultiplier: Float, val label: String) {
    Empowered(attackMultiplier = 1.35f, label = "On a streak! +35% attack for a moment"),
    Weakened(attackMultiplier = 0.75f, label = "Rough round... -25% attack for a moment"),
    Neutral(attackMultiplier = 1f, label = "No effect this round")
}

/** Ile ms trzyma sie efekt zamknietego cyklu (buff albo debuff). */
const val CYCLE_BUFF_DURATION_MS = 12_000L

private const val CYCLE_SIZE = 10
private const val ULTI_STREAK = 5

fun StreakTracker.record(correct: Boolean): StreakOutcome {
    val filledIndex = pips.indexOfFirst { it == null }
    val nextPips = if (filledIndex == -1) {
        List(CYCLE_SIZE) { index -> if (index == 0) correct else null }
    } else {
        pips.toMutableList().also { it[filledIndex] = correct }
    }
    val consecutive = if (correct) consecutiveCorrect + 1 else 0
    val ulti = correct && consecutive > 0 && consecutive % ULTI_STREAK == 0

    val cycleComplete = nextPips.none { it == null }
    return if (cycleComplete) {
        val correctCount = nextPips.count { it == true }
        val outcome = when {
            correctCount >= 7 -> CycleOutcome.Empowered
            correctCount <= 3 -> CycleOutcome.Weakened
            else -> CycleOutcome.Neutral
        }
        StreakOutcome(
            tracker = StreakTracker(pips = List(CYCLE_SIZE) { null }, consecutiveCorrect = consecutive),
            ultiTriggered = ulti,
            cycleResult = outcome
        )
    } else {
        StreakOutcome(
            tracker = StreakTracker(pips = nextPips, consecutiveCorrect = consecutive),
            ultiTriggered = ulti,
            cycleResult = null
        )
    }
}
