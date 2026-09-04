package com.lernhero.domain.spell

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Zaklecia zdobywa sie teraz wylacznie z drzewka talentow, a rzuca ich szansa zapisana przy
 * wezle ([com.lernhero.domain.talent.TalentEffect.UnlockSpell]). Te testy pilnuja samego katalogu:
 * kazde zaklecie musi dzialac w ZYWEJ walce, nie tylko w symulacji, i celowac we wlasciwa strone.
 */
class SpellCatalogTest {

    @Test
    fun `kazde zaklecie ma opis dla ZYWEJ walki, nie tylko dla symulacji`() {
        SpellCatalog.all.forEach { spell ->
            val live = spell.live
            val doesSomething = live.damageMultiplier > 0f || live.healMultiplier > 0f || live.freezeTurns > 0
            assertTrue(doesSomething, "${spell.id} nie robi nic w zywej walce")
        }
    }

    @Test
    fun `zaklecie leczace celuje w sojusznika, raniace we wroga`() {
        SpellCatalog.all.forEach { spell ->
            if (spell.live.heals) {
                assertEquals(SpellTarget.WoundedAlly, spell.live.target, "${spell.id} leczy, wiec cel to sojusznik")
            } else {
                assertTrue(
                    spell.live.target != SpellTarget.WoundedAlly,
                    "${spell.id} zadaje obrazenia, wiec nie moze celowac w sojusznika"
                )
            }
        }
    }
}
