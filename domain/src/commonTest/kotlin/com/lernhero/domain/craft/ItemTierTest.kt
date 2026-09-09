package com.lernhero.domain.craft

import com.lernhero.domain.common.Rarity
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.item.catalog.MonsterGear
import com.lernhero.domain.run.RunDifficulty
import com.lernhero.domain.story.StoryWorldCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Progi kuzni. Najwazniejsze nie jest to, ze dzialaja, tylko ze KAZDY przedmiot w grze na jakims
 * stoi - inaczej dopisanie miecza po cichu wypadaloby z bramkowania i bylo dostepne od pierwszej
 * minuty.
 */
class ItemTierTest {

    private val total = StoryWorldCatalog.totalStages

    private fun cleared(stages: Int, tier: RunDifficulty = RunDifficulty.entries.first()) =
        mapOf(tier.name to stages)

    @Test
    fun `kazdy przedmiot w grze ma swoj prog`() {
        val all = ItemCatalog.all + MonsterGear.all
        all.forEach { item ->
            // Rzuci, gdy ktos doda rzadkosc bez progu - i o to chodzi.
            ItemTier.of(item)
        }
        assertEquals(
            Rarity.entries.size,
            ItemTier.entries.size,
            "Kazda rzadkosc ma miec dokladnie jeden prog"
        )
    }

    @Test
    fun `pierwszy prog jest otwarty od poczatku gry`() {
        assertTrue(ItemTier.Rough.isUnlocked(emptyMap()))
        assertEquals(ItemTier.Rough, ItemTier.highestUnlocked(emptyMap()))
    }

    @Test
    fun `wyzsze progi wymagaja postepu w kampanii`() {
        assertFalse(ItemTier.Mythic.isUnlocked(emptyMap()), "Legendy od startu psuja cala petle")
        assertTrue(ItemTier.Mythic.isUnlocked(cleared(total)))
    }

    @Test
    fun `progi otwieraja sie po kolei`() {
        val stages = ItemTier.entries.map { it.requiredStages() }
        assertEquals(stages.sorted(), stages, "Prog wyzszej rzadkosci ma byc dalej w kampanii")
    }

    @Test
    fun `liczy sie najdalszy postep z dowolnego tieru trudnosci`() {
        // Gracz, ktory przeszedl kampanie na trudniejszym poziomie, nie moze byc karany za to,
        // ze nie domknal latwiejszego.
        val hardOnly = mapOf(RunDifficulty.entries.last().name to total)

        assertEquals(ItemTier.Mythic, ItemTier.highestUnlocked(hardOnly))
    }

    @Test
    fun `prog wynika z rzadkosci przedmiotu`() {
        assertEquals(ItemTier.Rough, ItemTier.of(Rarity.Common))
        assertEquals(ItemTier.Mythic, ItemTier.of(Rarity.Legendary))
    }
}
