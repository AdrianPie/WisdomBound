package com.lernhero.domain.town

import kotlinx.serialization.Serializable
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Surowce miasta. Money zostaje tam gdzie byl (Player.money) - to osobna, starsza waluta.
 *
 * Trzy surowce, a nie cztery. Drewno zostalo skasowane, bo nie bylo surowcem tylko ozdoba: jego
 * JEDYNYM odbiorca bylo wykuwanie (15-75 sztuk raz na przedmiot), a same skrzynie z bossow
 * kampanii wyplacaly go ponad 1600 - nie liczac tartaku, ktory na pierwszym poziomie robil
 * 60/h bez konca. Zasob, ktorego nie da sie ani zabraknac, ani wydac, nie jest decyzja gracza,
 * tylko czwartym kafelkiem na pasku i czwarta pozycja w kazdym oknie kosztu. Jego role przejal
 * [stone] - patrz [com.lernhero.domain.craft.Forge.costFor].
 *
 * [stone] produkuje kamieniolom, a [iron] i [essence] wypadaja WYLACZNIE w lochach - to one
 * spinaja loch z miastem: bijesz sie po to, zeby miec z czego kuc i przyzywac. Dzieki temu wyprawa
 * ma sens nawet wtedy, gdy gracz ma juz pelno zlota.
 */
@Serializable
data class TownResources(
    val stone: Int = 0,
    /** Zlom i ruda z lochow - jedyne zrodlo materialu na przedmioty w Kuzni. */
    val iron: Int = 0,
    /** Esencja z pokonanych wrogow - paliwo Summon Center (kompani i zaklecia). */
    val essence: Int = 0,
    /** Losy do Summon Center - zdobywane, nie kupowane za zloto. */
    val summonTickets: Int = 0
) {
    fun plus(type: ResourceType, amount: Int): TownResources = when (type) {
        ResourceType.Stone -> copy(stone = stone + amount)
        ResourceType.Iron -> copy(iron = iron + amount)
        ResourceType.Essence -> copy(essence = essence + amount)
    }

    fun amountOf(type: ResourceType): Int = when (type) {
        ResourceType.Stone -> stone
        ResourceType.Iron -> iron
        ResourceType.Essence -> essence
    }

    /** Dolicza cala zdobycz z jednego zejscia naraz - patrz [com.lernhero.domain.progress.RunStats]. */
    fun plusAll(stone: Int = 0, iron: Int = 0, essence: Int = 0, tickets: Int = 0): TownResources =
        copy(
            stone = this.stone + stone,
            iron = this.iron + iron,
            essence = this.essence + essence,
            summonTickets = this.summonTickets + tickets
        )
}

@Serializable
enum class ResourceType(val label: String, val icon: String) {
    Stone("Stone", "\uD83E\uDEA8"),
    Iron("Ore", "\u2699"),
    Essence("Essence", "\u2728")
}

/**
 * Budynek produkcyjny - trzyma wlasny [ProductionState] i produkuje wlasny surowiec.
 *
 * Zostal JEDEN. Tartak i kamieniolom byly tym samym budynkiem w dwoch skorkach: ta sama krzywa,
 * ten sam koszt, ten sam ekran, tylko inna ikona - a gracz musial odwiedzac oba, zeby nie tracic
 * produkcji. Podwojenie klikania nie jest glebia. Zostawiony jest [Quarry], bo kamien ma realnego
 * odbiorce (ulepszanie przedmiotow), a drewno nie mialo zadnego.
 */
@Serializable
enum class ProductionBuilding(val resource: ResourceType) {
    Quarry(ResourceType.Stone)
}

/**
 * Stan jednego budynku produkcyjnego. Wszystkie czasy to epoch millis.
 *
 * Nie trzymamy tu "ile juz wyprodukowano" - to zawsze wynika z [ProductionMath.pendingFor], czyli z
 * czasu, ktory uplynal od [lastCollectedAt]. Dzieki temu produkcja dziala tak samo, gdy gra jest
 * otwarta, jak i gdy gracz wyszedl na 10 godzin: nie trzeba nic tykac w tle ani niczego symulowac.
 */
@Serializable
data class ProductionState(
    val level: Int = 1,
    /** Kiedy rozwiazano quiz podbijajacy produkcje (0 = nigdy). */
    val boostStartedAt: Long = 0L,
    /** Mnoznik z tego quizu, np. 1.25f = +25%. */
    val boostMultiplier: Float = 1f,
    /** Ostatni odbior surowcow - od tego momentu liczy sie kolejna produkcja. */
    val lastCollectedAt: Long = 0L
)

/**
 * Cala matematyka produkcji w jednym miejscu, bez zadnego stanu - te same wzory licza pasek na
 * ekranie w czasie rzeczywistym I nagrode za 10 godzin nieobecnosci.
 */
object ProductionMath {

    /** Ile trwa bonus z quizu. */
    const val BOOST_DURATION_MS: Long = 24L * 60 * 60 * 1000

    /** Produkcja na godzine na 1. poziomie. */
    private const val BASE_PER_HOUR = 45f

    /** Kazdy poziom to +22% produkcji - wykladniczo, zeby ulepszanie bylo odczuwalne. */
    private const val LEVEL_GROWTH = 1.22f

    /**
     * Sufit budynku - i to jest cala roznica miedzy "kopalnia pomaga" a "kopalnia gra za gracza".
     *
     * Bez sufitu, przy 60/h i +35% na poziom, budynek na poziomie 12 robil 1629 kamienia na
     * godzine, czyli 13 000 na jedno odebranie magazynu. Ulepszenie przedmiotu na +10 kosztuje
     * najwyzej 3300 kamienia, wiec kopalnia sama, bez jednej odpowiedzi na pytanie, wyplacala
     * kilka pelnych ulepszen dziennie. Teraz ostatni poziom daje 270/h (2157 na magazyn), czyli
     * mniej wiecej JEDNO ulepszenie dziennie - realna pomoc, ktora nie zastepuje grania.
     */
    const val MAX_LEVEL = 10

    /** Magazyn to tyle, ile budynek wyprodukuje przez tyle godzin - potem przestaje zbierac. */
    private const val STORAGE_HOURS = 8f

    private const val MILLIS_PER_HOUR = 3_600_000f

    fun clampLevel(level: Int): Int = level.coerceIn(1, MAX_LEVEL)

    fun isMaxed(level: Int): Boolean = level >= MAX_LEVEL

    /** Produkcja/h bez bonusu z quizu. */
    fun baseRatePerHour(level: Int): Float =
        BASE_PER_HOUR * LEVEL_GROWTH.pow(clampLevel(level) - 1)

    /** Produkcja/h z uwzglednieniem aktywnego bonusu - to ta liczba idzie na ekran. */
    fun effectiveRatePerHour(state: ProductionState, now: Long): Float =
        baseRatePerHour(state.level) * activeMultiplier(state, now)

    /** 1f gdy bonus wygasl (albo nigdy nie byl aktywny). */
    fun activeMultiplier(state: ProductionState, now: Long): Float =
        if (isBoostActive(state, now)) state.boostMultiplier else 1f

    fun isBoostActive(state: ProductionState, now: Long): Boolean =
        state.boostStartedAt > 0L && now < state.boostStartedAt + BOOST_DURATION_MS

    /** Ile milisekund zostalo bonusu (0 = nieaktywny). */
    fun boostRemainingMillis(state: ProductionState, now: Long): Long =
        if (isBoostActive(state, now)) state.boostStartedAt + BOOST_DURATION_MS - now else 0L

    fun storageCap(level: Int): Int = (baseRatePerHour(level) * STORAGE_HOURS).roundToInt()

    /**
     * Ile surowca czeka do odebrania. Odcinek [lastCollectedAt, now] dzielimy na czesc objeta
     * bonusem i reszte, kazda liczymy osobno i sumujemy - stad poprawny wynik nawet wtedy, gdy
     * bonus wygasl w srodku nieobecnosci gracza. Wynik jest przycinany do [storageCap].
     */
    fun pendingFor(state: ProductionState, now: Long): Int {
        val since = state.lastCollectedAt
        if (since <= 0L || now <= since) return 0

        val boostEnd = state.boostStartedAt + BOOST_DURATION_MS
        val boostedMillis = if (state.boostStartedAt <= 0L) {
            0L
        } else {
            // Czesc wspolna przedzialu produkcji i przedzialu dzialania bonusu.
            (min(now, boostEnd) - maxOf(since, state.boostStartedAt)).coerceAtLeast(0L)
        }
        val plainMillis = (now - since - boostedMillis).coerceAtLeast(0L)

        val ratePerHour = baseRatePerHour(state.level)
        val produced = ratePerHour * (plainMillis + boostedMillis * state.boostMultiplier) / MILLIS_PER_HOUR
        return produced.roundToInt().coerceAtMost(storageCap(state.level))
    }

    /**
     * Koszt ulepszenia budynku na kolejny poziom - rosnie szybciej niz produkcja, wiec kazdy
     * kolejny poziom jest gorszym interesem od poprzedniego. Wyciagniecie kamieniolomu na
     * [MAX_LEVEL] kosztuje w sumie ok. 13 000 zlota, czyli mniej wiecej tyle, ile zostaje w
     * sakwie po przejsciu kampanii na Normalnym - to jest jedyny SKONCZONY sink na zloto w grze,
     * wiec ma byc wart calego zapasu, a nie drobnych.
     *
     * Zwraca 0 na [MAX_LEVEL] - nie ma ceny za poziom, ktorego nie ma.
     */
    fun upgradeCost(level: Int): Int =
        if (isMaxed(level)) 0 else (80f * 1.7f.pow(level - 1)).roundToInt()
}
