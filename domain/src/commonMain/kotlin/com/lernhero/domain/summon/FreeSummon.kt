package com.lernhero.domain.summon

/**
 * Darmowe przyzwania na zegarze - po jednym na banner.
 *
 * Zapis trzyma TYLKO moment ostatniego odbioru ([com.lernhero.domain.player.Player.freeSummonClaims]),
 * a nie "ile zostalo do konca". Dzieki temu licznik nie potrzebuje zadnego tykania w tle i nie da sie
 * go oszukac wyjsciem z aplikacji - odliczanie zawsze wynika z roznicy dwoch znacznikow czasu.
 *
 * Pierwsze wejscie (brak wpisu) daje darmowy summon OD RAZU: nowy gracz ma dostac cos w rece,
 * zanim zacznie liczyc, ile godzin musi poczekac.
 */
object FreeSummon {

    /** Zwykly banner - darmowy los co 6 godzin. */
    const val BASIC_COOLDOWN_MS: Long = 6L * 60 * 60 * 1000

    /** Heroiczny banner - rzadka nagroda, raz na 3 dni. */
    const val HEROIC_COOLDOWN_MS: Long = 3L * 24 * 60 * 60 * 1000

    /** Kiedy najwczesniej wolno odebrac kolejny darmowy summon. Brak wpisu = teraz. */
    fun readyAt(claimedAt: Long?, cooldownMs: Long): Long =
        if (claimedAt == null || claimedAt <= 0L) 0L else claimedAt + cooldownMs

    /** Ile milisekund zostalo do darmowego summona (0 = juz mozna). */
    fun remainingMs(claimedAt: Long?, cooldownMs: Long, now: Long): Long =
        (readyAt(claimedAt, cooldownMs) - now).coerceAtLeast(0L)

    fun isReady(claimedAt: Long?, cooldownMs: Long, now: Long): Boolean =
        remainingMs(claimedAt, cooldownMs, now) <= 0L

    /**
     * Odliczanie w formacie zaleznym od skali: sekundy przy krotkim czasie, dni przy dlugim.
     * Gracz patrzacy na heroiczny banner nie potrzebuje sekund - potrzebuje "jeszcze 2 dni".
     */
    fun format(remainingMs: Long): String {
        if (remainingMs <= 0L) return "GOTOWE"
        val totalSeconds = remainingMs / 1000
        val days = totalSeconds / 86_400
        val hours = (totalSeconds % 86_400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}:${minutes.pad()}:${seconds.pad()}"
            else -> "${minutes.pad()}:${seconds.pad()}"
        }
    }

    private fun Long.pad(): String = toString().padStart(2, '0')
}
