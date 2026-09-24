package com.lernhero.domain.energy

import kotlinx.serialization.Serializable

/**
 * Energia - zasob, ktory placi za wejscie do walki i sam sie odnawia z czasem.
 *
 * Zapisujemy STAN + MOMENT, a nie "ile przybylo": ile energii ma gracz, wynika z roznicy czasu,
 * dokladnie tak samo jak produkcja w miescie i nagrody idle. Dzieki temu nie ma zadnego licznika
 * w tle, ktory moglby sie rozjechac miedzy urzadzeniami albo po zabiciu aplikacji - a gra nie
 * musi nic robic, gdy jest zamknieta.
 *
 * [updatedAt] rowne zeru znaczy "zapis sprzed energii" - taki gracz dostaje pelny pasek przy
 * pierwszym wczytaniu, zamiast zaczynac od zera przez cudzy brak pola w bazie.
 */
@Serializable
data class EnergyState(
    val amount: Int = EnergyRules.MAX,
    /** Epoch millis ostatniego przeliczenia. 0 = zapis sprzed wprowadzenia energii. */
    val updatedAt: Long = 0L
)

/**
 * Balans energii. Liczby sa dobrane tak, jak robia to mobilne RPG z tym samym zasobem (AFK Arena,
 * Shiba Inu Adventure, Brawl Stars): pasek ma wystarczyc na KILKA walk pod rzad po dluzszej
 * przerwie, a pelne napelnienie ma trwac mniej wiecej jedna noc.
 *
 * - [MAX] 40 i [BATTLE_COST] 5 daje osiem walk z pelnego paska - tyle trwa jedna dluzsza sesja.
 * - [REFILL_MILLIS] 8 minut na punkt to 40 minut na jedna walke i 5h20m na pelny pasek: gracz,
 *   ktory wraca po pracy albo po nocy, zastaje pasek pelny, a gracz, ktory wlasnie wszystko wydal,
 *   ma po godzinie znowu na walke. To jest cala rola tego zasobu - rozlozyc granie na kilka
 *   podejsc, a nie zablokowac nauke.
 *
 * Energia NIE przepada powyzej [MAX] - nadmiar po prostu sie nie nalicza, wiec nikt nie jest
 * karany za to, ze nie wszedl do gry o konkretnej godzinie.
 */
object EnergyRules {

    const val MAX = 40

    /** Koszt wejscia do jednej walki. */
    const val BATTLE_COST = 5

    /** Ile czasu odbudowuje JEDEN punkt energii. */
    const val REFILL_MILLIS = 8 * 60 * 1000L

    /**
     * Stan po doliczeniu tego, co naroslo do [now].
     *
     * Reszta niepelnego punktu NIE przepada: nowy znacznik czasu przesuwa sie o pelne punkty
     * ([gained] * [REFILL_MILLIS]), a nie na [now]. Inaczej gracz zagladajacy do gry co minute
     * nie doczekalby sie ani jednego punktu.
     */
    fun regenerated(state: EnergyState, now: Long): EnergyState {
        // Zapis sprzed energii albo zegar cofniety (strefa czasowa, reczna zmiana daty) - w obu
        // wypadkach nie ma z czego liczyc, wiec po prostu zaczynamy mierzyc od teraz.
        if (state.updatedAt <= 0L || now < state.updatedAt) {
            return EnergyState(amount = state.amount.coerceAtLeast(0), updatedAt = now)
        }
        if (state.amount >= MAX) return EnergyState(amount = state.amount, updatedAt = now)

        val gained = ((now - state.updatedAt) / REFILL_MILLIS).toInt()
        if (gained <= 0) return state

        val amount = (state.amount + gained).coerceAtMost(MAX)
        val updatedAt = if (amount >= MAX) now else state.updatedAt + gained * REFILL_MILLIS
        return EnergyState(amount = amount, updatedAt = updatedAt)
    }

    /** Ile zostalo do nastepnego punktu. Zero, gdy pasek jest pelny. */
    fun millisToNext(state: EnergyState, now: Long): Long {
        val current = regenerated(state, now)
        if (current.amount >= MAX) return 0L
        val elapsed = (now - current.updatedAt).coerceAtLeast(0L)
        return (REFILL_MILLIS - elapsed % REFILL_MILLIS).coerceIn(0L, REFILL_MILLIS)
    }

    /** Ile czasu do pelnego paska - to pokazuje sie, gdy gracz nie ma juz na walke. */
    fun millisToFull(state: EnergyState, now: Long): Long {
        val current = regenerated(state, now)
        val missing = (MAX - current.amount).coerceAtLeast(0)
        if (missing == 0) return 0L
        return (missing - 1) * REFILL_MILLIS + millisToNext(current, now)
    }

    fun canAfford(state: EnergyState, now: Long, cost: Int = BATTLE_COST): Boolean =
        regenerated(state, now).amount >= cost

    /**
     * Stan po zaplaceniu [cost], albo null, gdy nie ma z czego zaplacic.
     *
     * Zejscie z pelnego paska ZACZYNA odliczanie od [now] - inaczej pierwszy punkt po wydaniu
     * energii wpadalby natychmiast, bo znacznik czasu stalby w miejscu od ostatniego napelnienia.
     */
    fun spend(state: EnergyState, now: Long, cost: Int = BATTLE_COST): EnergyState? {
        val current = regenerated(state, now)
        if (current.amount < cost) return null
        val left = current.amount - cost
        val updatedAt = if (current.amount >= MAX) now else current.updatedAt
        return EnergyState(amount = left, updatedAt = updatedAt)
    }

    /** Dolozenie energii (nagrody, zestaw testowy) - nie przekracza [MAX]. */
    fun grant(state: EnergyState, now: Long, amount: Int): EnergyState {
        val current = regenerated(state, now)
        return current.copy(amount = (current.amount + amount).coerceAtMost(MAX))
    }
}
