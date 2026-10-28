package com.lernhero.domain.combat.live

/**
 * Pasek czasu na odpowiedz w biezacej turze i to, jak czas wplywa na sile salwy.
 *
 * ## Balans (wersja 2)
 *
 * Wczesniej pasek mial osiem schodkow od x1.5 do x0.5, a zla odpowiedz dawala x0.55. Skutek: dobra,
 * ale przemyslana odpowiedz (ostatni schodek, x0.5) bila SLABIEJ niz szybkie strzelanie na slepo.
 * Gra nagradzala zgadywanie zamiast wiedzy - dokladnie odwrotnie, niz powinna gra do nauki.
 *
 * Teraz obowiazuja trzy zasady:
 *  1. **Dobra odpowiedz nigdy nie bije slabiej niz x[CORRECT_FLOOR]** - czyli zawsze mniej wiecej
 *     dwa razy mocniej niz pudlo. Wiedza jest wazniejsza od refleksu.
 *  2. **Pierwsze [GRACE_FRACTION] okna to pelna moc** - tyle trwa spokojne przeczytanie pytania.
 *     Nikt nie traci na tym, ze czyta.
 *  3. Potem moc opada **plynnie** (bez schodkow) do podlogi na koncu okna. Czas nadal sie liczy,
 *     ale jako premia za pewnosc, a nie kara za myslenie.
 */
object AnswerTimer {
    /** 18s - podwojone z 9s, bo nawet to bylo za szybko na spokojne przeczytanie pytania i odpowiedz. */
    const val WINDOW_MS = 18000L

    /** Mnoznik szybkiej, dobrej odpowiedzi (w oknie [GRACE_FRACTION]). */
    const val CORRECT_PEAK = 1.4f

    /** Mnoznik dobrej odpowiedzi na ostatnia chwile. */
    const val CORRECT_FLOOR = 1.0f

    /** Jaka czesc okna (od poczatku) daje jeszcze pelna moc. 0.3 = ~5.4s z osiemnastu. */
    const val GRACE_FRACTION = 0.3f

    /**
     * Mnoznik dobrej odpowiedzi, gdy zostalo [remainingFraction] okna (1 = pytanie wlasnie sie
     * pojawilo, 0 = czas minal). Zla odpowiedz ma wlasny, staly mnoznik - czas jej nie dotyczy.
     */
    fun multiplierFor(remainingFraction: Float): Float {
        val remaining = remainingFraction.coerceIn(0f, 1f)
        val decayStart = 1f - GRACE_FRACTION
        if (remaining >= decayStart) return CORRECT_PEAK
        val t = remaining / decayStart // 1 na poczatku spadku, 0 na koncu okna
        return CORRECT_FLOOR + (CORRECT_PEAK - CORRECT_FLOOR) * t
    }

    /** Czy odpowiedz w tej chwili dalaby jeszcze pelna moc - pasek pokazuje wtedy "MAX". */
    fun isInGrace(remainingFraction: Float): Boolean = remainingFraction >= 1f - GRACE_FRACTION

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
     * **Tempo pytania** ([paceMs]): dodatek za dlugi tekst i za pytanie do przemyslenia (porownanie,
     * proporcja, rachunek) - liczy go [com.lernhero.domain.quiz.QuizPace.extraMs]. Dochodzi przed
     * skroceniem za fale, wiec gleboka fala tnie tez dodatek.
     *
     * Pasek na ekranie i mnoznik salwy licza sie z UŁAMKA tego okna, wiec obie te rzeczy dostraja
     * sie same - nie ma drugiego miejsca, ktore trzeba by zmienic razem z ta funkcja.
     */
    fun windowFor(optionCount: Int, wave: Int, paceMs: Long = 0L): Long {
        val extras = (optionCount - BASE_OPTIONS).coerceAtLeast(0)
        val sized = WINDOW_MS + extras * MS_PER_EXTRA_OPTION + paceMs.coerceAtLeast(0L)
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
 * Licznik ostatnich odpowiedzi - dziesiec "kratek", jak seria karnych. Pelne dziesiec zamyka cykl
 * i rozlicza wynik na buff albo debuff.
 *
 * Osobno od kratek biegnie [ultiCharge] - ladowanie kuli mocy. Rosnie z kazda dobra odpowiedzia,
 * zeruje sie na blednej i PO WYSTRZALE. Ile trzeba, mowi zalozona kula
 * ([com.lernhero.domain.ultimate.UltimateSpec.streak]) - dlatego to osobne pole, a nie reszta z
 * dzielenia [consecutiveCorrect], ktora dzialala tylko przy jednej, stalej dlugosci serii.
 */
data class StreakTracker(
    val pips: List<Boolean?> = List(CYCLE_SIZE) { null },
    val consecutiveCorrect: Int = 0,
    val ultiCharge: Int = 0
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

/** [ultiStreak] = ile dobrych z rzedu laduje zalozona kule (patrz [StreakTracker.ultiCharge]). */
fun StreakTracker.record(correct: Boolean, ultiStreak: Int = 5): StreakOutcome {
    val filledIndex = pips.indexOfFirst { it == null }
    val nextPips = if (filledIndex == -1) {
        List(CYCLE_SIZE) { index -> if (index == 0) correct else null }
    } else {
        pips.toMutableList().also { it[filledIndex] = correct }
    }
    val consecutive = if (correct) consecutiveCorrect + 1 else 0
    val charged = if (correct) ultiCharge + 1 else 0
    val ulti = charged >= ultiStreak.coerceAtLeast(1)
    val nextCharge = if (ulti) 0 else charged

    val cycleComplete = nextPips.none { it == null }
    return if (cycleComplete) {
        val correctCount = nextPips.count { it == true }
        val outcome = when {
            correctCount >= 7 -> CycleOutcome.Empowered
            correctCount <= 3 -> CycleOutcome.Weakened
            else -> CycleOutcome.Neutral
        }
        StreakOutcome(
            tracker = StreakTracker(pips = List(CYCLE_SIZE) { null }, consecutiveCorrect = consecutive, ultiCharge = nextCharge),
            ultiTriggered = ulti,
            cycleResult = outcome
        )
    } else {
        StreakOutcome(
            tracker = StreakTracker(pips = nextPips, consecutiveCorrect = consecutive, ultiCharge = nextCharge),
            ultiTriggered = ulti,
            cycleResult = null
        )
    }
}
