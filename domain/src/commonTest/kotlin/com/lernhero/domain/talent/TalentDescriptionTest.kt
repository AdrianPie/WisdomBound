package com.lernhero.domain.talent

import com.lernhero.domain.run.EndlessBuff
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * **Opis wezla ma mowic to, co gracz dostanie.**
 *
 * Audyt balansu znalazl siedem mechanik ([EndlessBuff]) zasilanych przez DWA rozne talenty naraz.
 * Sufity zaprojektowane dla jednego wezla cicho sie podwajaly: "Vitality" i "Breath of Life" to
 * lacznie +80% HP, choc kazdy z nich obiecuje +40%; "Fortress" plus "Sanctuary" to osiem stakow
 * redukcji przy twardym suficie szesciu. Samo sumowanie jest w porzadku - to nagroda za rozlozenie
 * punktow na dwa drzewka i kosztuje punkty z obu. Nie jest w porzadku to, ze gracz nie ma jak sie
 * o nim dowiedziec inaczej niz czytajac kod.
 *
 * Stad ta regula: jesli dwa wezly karmia ten sam buff, KAZDY z nich musi nazwac tego drugiego.
 */
class TalentDescriptionTest {

    @Test
    fun `wezly dzielace ten sam buff nazywaja sie nawzajem`() {
        val feeders: Map<EndlessBuff, List<Talent>> = TalentCatalog.all
            .flatMap { talent -> talent.effects.filterIsInstance<TalentEffect.Buff>().map { it.buff to talent } }
            .groupBy({ it.first }, { it.second })
            .filterValues { it.size > 1 }

        val silent = feeders.flatMap { (buff, talents) ->
            talents.filter { talent ->
                val siblings = talents.filter { it.id != talent.id }
                siblings.none { talent.description.contains(it.name, ignoreCase = true) }
            }.map { "${it.id} (${buff.name}) nie wspomina o blizniaku: ${it.description}" }
        }

        assertTrue(silent.isEmpty(), "Opis milczy o sumowaniu: $silent")
    }

    /**
     * Mnozenie pociskow bylo jedyna mechanika "uderz wiecej razy" bez dyskontu - rozciecie miesci
     * dodatkowe trafienia w 60% od zawsze. Skoro pociski dostaly ten sam wspolczynnik, opis musi
     * o nim mowic, inaczej talent obiecuje pelna salwe, a daje niepelna.
     */
    @Test
    fun `talenty od dodatkowych pociskow mowia o dyskoncie`() {
        val projectile = TalentCatalog.all.filter { talent ->
            talent.effects.filterIsInstance<TalentEffect.Buff>()
                .any { it.buff == EndlessBuff.MultiShot || it.buff == EndlessBuff.Volley }
        }

        assertTrue(projectile.isNotEmpty(), "Nie ma z czego robic testu - zniknely talenty od pociskow")
        projectile.forEach { talent ->
            assertTrue(
                talent.description.contains("60%"),
                "Talent ${talent.id} obiecuje dodatkowe pociski, nie mowiac, ze bija slabiej"
            )
        }
    }
}
