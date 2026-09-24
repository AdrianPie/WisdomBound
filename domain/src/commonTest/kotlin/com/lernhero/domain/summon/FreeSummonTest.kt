package com.lernhero.domain.summon

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FreeSummonTest {

    private val now = 1_700_000_000_000L

    @Test
    fun `a brand new player has a free summon waiting`() {
        // Brak wpisu = los czeka od razu. Nowy gracz ma dostac cos w rece, zanim zacznie liczyc
        // godziny - i plakietka w oknie druzyny ma go tam zaprowadzic.
        assertTrue(FreeSummon.anyReady(emptyMap(), now))
    }

    @Test
    fun `one ready banner is enough to light the badge`() {
        val claims = mapOf(
            FreeSummon.BASIC_BANNER to now - FreeSummon.BASIC_COOLDOWN_MS,
            FreeSummon.HEROIC_BANNER to now
        )
        // Zwykly juz sie odnowil, heroiczny dopiero co odebrany - plakietka i tak ma sie palic,
        // bo jest po co tam isc.
        assertTrue(FreeSummon.anyReady(claims, now))
    }

    @Test
    fun `nothing ready means no badge`() {
        val claims = mapOf(
            FreeSummon.BASIC_BANNER to now,
            FreeSummon.HEROIC_BANNER to now
        )
        assertFalse(FreeSummon.anyReady(claims, now))
        // ...i zapala sie dokladnie w chwili, gdy krotszy cooldown mija.
        assertTrue(FreeSummon.anyReady(claims, now + FreeSummon.BASIC_COOLDOWN_MS))
    }
}
