package com.lernhero.domain.player

/**
 * Tytul gracza - to, co stoi pod imieniem na gornym pasku ("Scout II").
 *
 * Poziom sam w sobie jest liczba i nic nie mowi; tytul nazywa etap gry, w ktorym gracz jest, i
 * zmienia sie DWA razy czesciej niz stopien - dzieki temu awans widac nawet wtedy, gdy nowy
 * poziom nie odblokowal jeszcze niczego innego. Tak robi to kazda gra z rangami: liczba jest dla
 * systemu, nazwa dla gracza.
 *
 * Progi sa czysto kosmetyczne - nic w grze nie sprawdza rangi, wiec przestrojenie tej listy nie
 * rusza zadnej mechaniki.
 */
object HeroRank {

    private val TITLES = listOf(
        "Novice", "Scout", "Squire", "Fighter", "Veteran",
        "Knight", "Champion", "Warlord", "Hero", "Legend"
    )

    /** Ile poziomow trwa jeden tytul. Cztery stopnie rzymskie w kazdym - stad 4 * [STEP]. */
    private const val STEP = 3

    fun titleFor(level: Int): String {
        val safe = level.coerceAtLeast(1) - 1
        val title = TITLES[(safe / (STEP * 4)).coerceAtMost(TITLES.lastIndex)]
        val grade = (safe / STEP) % 4
        return "$title ${ROMAN[grade]}"
    }

    private val ROMAN = listOf("I", "II", "III", "IV")
}
