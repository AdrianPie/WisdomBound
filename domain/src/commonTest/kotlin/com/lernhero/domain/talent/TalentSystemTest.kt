package com.lernhero.domain.talent

import com.lernhero.domain.player.LevelCurve
import com.lernhero.domain.player.PlayerProgress
import com.lernhero.domain.spell.SpellCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Drzewko jest w calosci danymi, wiec najgrozniejsze bledy to nie wyjatki, tylko wezly, do ktorych
 * NIE DA SIE dojsc: talent wymagajacy rodzica z innego drzewka, rzad droższy niz da sie oplacic,
 * albo zaklecie o id, ktorego nie ma w katalogu. Tego nie widac na ekranie - widac dopiero, gdy
 * gracz wyda punkty i nic sie nie stanie.
 */
class TalentSystemTest {

    /* ------------------------------- Spojnosc katalogu ------------------------------- */

    @Test
    fun `identyfikatory talentow sa unikalne`() {
        val duplicates = TalentCatalog.all.groupBy { it.id }.filter { it.value.size > 1 }.keys
        assertTrue(duplicates.isEmpty(), "Zdublowane id talentow: $duplicates")
    }

    @Test
    fun `kazde drzewko ma od 10 do 15 talentow`() {
        TalentTree.entries.forEach { tree ->
            val count = TalentCatalog.forTree(tree).size
            assertTrue(count in 10..15, "Drzewko ${tree.label} ma $count talentow")
        }
    }

    @Test
    fun `rodzic talentu jest w tym samym drzewku i wyzej`() {
        TalentCatalog.all.forEach { talent ->
            val parentId = talent.requires ?: return@forEach
            val parent = TalentCatalog.byId(parentId)

            assertNotNull(parent, "${talent.id} wymaga nieistniejacego ${parentId}")
            assertEquals(talent.tree, parent.tree, "${talent.id} wymaga talentu z innego drzewka")
            assertTrue(
                parent.tier < talent.tier,
                "${talent.id} wymaga ${parent.id} z tego samego lub nizszego rzedu - nie da sie tego spelnic"
            )
        }
    }

    @Test
    fun `kazdy rzad da sie oplacic talentami z rzedow nad nim`() {
        // Prog rzedu liczy sie z punktow wydanych W TYM drzewku, wiec rzad N musi miec nad soba
        // co najmniej tyle stopni, ile sam wymaga. Inaczej powstaje wezel nie do odblokowania.
        TalentTree.entries.forEach { tree ->
            val talents = TalentCatalog.forTree(tree)
            (1 until TalentCatalog.tierCount(tree)).forEach { tier ->
                val capacityAbove = talents.filter { it.tier < tier }.sumOf { it.maxRank }
                val required = TalentRules.pointsRequiredFor(tier)
                assertTrue(
                    capacityAbove >= required,
                    "${tree.label}: rzad $tier wymaga $required pkt, a nad nim mozna wydac tylko $capacityAbove"
                )
            }
        }
    }

    @Test
    fun `talenty odblokowuja wylacznie istniejace zaklecia`() {
        TalentCatalog.all.forEach { talent ->
            talent.effects.filterIsInstance<TalentEffect.UnlockSpell>().forEach { effect ->
                assertNotNull(
                    SpellCatalog.byId(effect.spellId),
                    "${talent.id} odblokowuje nieistniejace zaklecie '${effect.spellId}'"
                )
            }
        }
    }

    @Test
    fun `kazde zaklecie da sie zdobyc z drzewka`() {
        // Zaklec nie kupuje sie juz za esencje, wiec drzewko jest ich JEDYNYM zrodlem - zaklecie
        // spoza drzewka byloby martwym wpisem w katalogu.
        val fromTalents = TalentCatalog.all
            .flatMap { it.effects }
            .filterIsInstance<TalentEffect.UnlockSpell>()
            .map { it.spellId }
            .toSet()

        SpellCatalog.all.forEach { spell ->
            assertTrue(spell.id in fromTalents, "Zaklecia '${spell.id}' nie da sie zdobyc zadnym talentem")
        }
    }

    /* ---------------------------------- Reguly wydawania ---------------------------------- */

    @Test
    fun `pierwszy rzad jest otwarty od razu`() {
        val talent = TalentCatalog.forTree(TalentTree.Dps).first { it.tier == 0 }
        assertTrue(TalentRules.canSpend(talent, ranks = emptyMap(), availablePoints = 1))
    }

    @Test
    fun `dalszy rzad wymaga punktow w tym samym drzewku`() {
        val deep = TalentCatalog.forTree(TalentTree.Tank).first { it.tier == 1 }

        assertEquals(
            TalentBlock.TierLocked,
            TalentRules.blockFor(deep, ranks = emptyMap(), availablePoints = 5)
        )

        // Punkty w INNYM drzewku nie otwieraja tego rzedu - o to chodzi w specjalizacji.
        val elsewhere = mapOf("dps_might" to 3)
        assertEquals(
            TalentBlock.TierLocked,
            TalentRules.blockFor(deep, ranks = elsewhere, availablePoints = 5)
        )

        val inTree = mapOf("tank_hide" to TalentRules.POINTS_PER_TIER)
        assertNull(TalentRules.blockFor(deep, ranks = inTree, availablePoints = 5))
    }

    @Test
    fun `talent z rodzicem czeka na jego pelny stopien`() {
        val child = TalentCatalog.all.first { it.requires != null }
        val parent = TalentCatalog.byId(child.requires!!)!!

        // Prog rzedu spelniony "na sile", zeby zostal wylacznie brak rodzica.
        val openTier = mapOf(parent.id to parent.maxRank - 1) +
            TalentCatalog.forTree(child.tree)
                .filter { it.tier == 0 && it.id != parent.id }
                .associate { it.id to it.maxRank }

        assertEquals(
            TalentBlock.ParentMissing,
            TalentRules.blockFor(child, ranks = openTier, availablePoints = 9)
        )

        val maxedParent = openTier + (parent.id to parent.maxRank)
        assertNull(TalentRules.blockFor(child, ranks = maxedParent, availablePoints = 9))
    }

    @Test
    fun `nie da sie przekroczyc maksymalnego stopnia`() {
        val talent = TalentCatalog.byId("dps_might")!!
        val maxed = mapOf(talent.id to talent.maxRank)

        assertEquals(TalentBlock.MaxRank, TalentRules.blockFor(talent, maxed, availablePoints = 9))
        assertNull(TalentRules.spend(talent, maxed, availablePoints = 9))
    }

    @Test
    fun `bez wolnego punktu nie da sie nic wykupic`() {
        val talent = TalentCatalog.forTree(TalentTree.Healer).first { it.tier == 0 }
        assertEquals(TalentBlock.NoPoints, TalentRules.blockFor(talent, emptyMap(), availablePoints = 0))
    }

    @Test
    fun `wydanie punktu podnosi stopien o jeden`() {
        val talent = TalentCatalog.byId("heal_vampirism")!!
        val after = TalentRules.spend(talent, emptyMap(), availablePoints = 1)

        assertNotNull(after)
        assertEquals(1, TalentRules.rankOf(after, talent.id))
        assertEquals(1, TalentRules.totalSpent(after))
    }

    /* ------------------------------------ Bonusy ------------------------------------ */

    @Test
    fun `bonusy sumuja sie ze stopni`() {
        // dps_edge daje +6 ataku za stopien.
        val bonuses = TalentRules.bonusesOf(mapOf("dps_edge" to 3))
        assertEquals(18, bonuses.attack)
    }

    @Test
    fun `staki buffow nie przebijaja sufitu mechaniki`() {
        val talent = TalentCatalog.byId("dps_might")!!
        val buff = (talent.effects.first() as TalentEffect.Buff).buff

        // Zapis z absurdalnie wysokim stopniem (np. po zmianie katalogu) nie moze dac wiecej stakow,
        // niz silnik walki uznaje za maksimum - inaczej Twierdza redukowalaby ponad 100% obrazen.
        val bonuses = TalentRules.bonusesOf(mapOf(talent.id to 99))
        assertEquals(buff.maxStacks, bonuses.buffStacks[buff])
    }

    @Test
    fun `nieznane id w zapisie jest ignorowane`() {
        // Talent usuniety z katalogu miedzy wersjami gry nie moze wywracac liczenia bonusow.
        val bonuses = TalentRules.bonusesOf(mapOf("talent_ktorego_nie_ma" to 4, "dps_edge" to 1))
        assertEquals(6, bonuses.attack)
        assertEquals(0, TalentRules.totalSpent(mapOf("talent_ktorego_nie_ma" to 4)))
    }

    @Test
    fun `puste drzewko nie daje zadnych bonusow`() {
        assertTrue(TalentRules.bonusesOf(emptyMap()).isEmpty)
    }

    @Test
    fun `punkty licza sie osobno dla kazdego drzewka`() {
        val ranks = mapOf("dps_might" to 2, "tank_hide" to 3, "heal_vampirism" to 1)

        assertEquals(2, TalentRules.pointsIn(ranks, TalentTree.Dps))
        assertEquals(3, TalentRules.pointsIn(ranks, TalentTree.Tank))
        assertEquals(1, TalentRules.pointsIn(ranks, TalentTree.Healer))
        assertEquals(6, TalentRules.totalSpent(ranks))
    }

    @Test
    fun `zaklecie wpada do puli po jednym stopniu`() {
        val spellTalent = TalentCatalog.all.first { talent ->
            talent.effects.any { it is TalentEffect.UnlockSpell }
        }
        val spellId = spellTalent.effects.filterIsInstance<TalentEffect.UnlockSpell>().first().spellId

        assertTrue(spellId in TalentRules.unlockedSpellIds(mapOf(spellTalent.id to 1)))
        assertTrue(TalentRules.unlockedSpellIds(emptyMap()).isEmpty())
    }

    @Test
    fun `odblokowane zaklecie niesie dodatnia szanse na rzut`() {
        TalentCatalog.all.forEach { talent ->
            talent.effects.filterIsInstance<TalentEffect.UnlockSpell>().forEach { effect ->
                assertTrue(
                    effect.castChancePercent in 1..100,
                    "${talent.id} odblokowuje '${effect.spellId}' z bezsensowna szansa ${effect.castChancePercent}"
                )
            }
        }
        val chances = TalentRules.spellChances(mapOf("dps_fireball" to 1))
        assertEquals(15, chances["fireball"])
    }

    /* ------------------------------------ Awanse ------------------------------------ */

    @Test
    fun `awans daje punkt talentu obok punktow statystyk`() {
        val result = LevelCurve.award(PlayerProgress(), amount = 100_000)

        assertTrue(result.levelsGained > 0, "Test wymaga chocby jednego awansu")
        assertEquals(result.levelsGained * TalentRules.POINTS_PER_LEVEL, result.talentPointsGained)
        assertEquals(result.levelsGained * LevelCurve.POINTS_PER_LEVEL, result.pointsGained)
    }

    @Test
    fun `brak XP nie daje punktow talentow`() {
        assertEquals(0, LevelCurve.award(PlayerProgress(), amount = 0).talentPointsGained)
    }

    @Test
    fun `punkty naleza sie za osiagniety poziom`() {
        assertEquals(0, TalentRules.pointsEarnedBy(1))
        assertEquals(11, TalentRules.pointsEarnedBy(12))
        // Bohater, ktory awansowal ZANIM drzewko powstalo, i tak ma czym je zapelnic - to jest
        // caly powod, dla ktorego punkty licza sie z poziomu zamiast siedziec w osobnym liczniku.
        assertEquals(31, TalentRules.availablePoints(level = 32, ranks = emptyMap()))
    }

    @Test
    fun `wydane stopnie zabieraja z puli`() {
        val ranks = mapOf("dps_might" to 3, "tank_hide" to 2)
        assertEquals(11 - 5, TalentRules.availablePoints(level = 12, ranks = ranks))
    }

    @Test
    fun `pula nigdy nie schodzi ponizej zera`() {
        // Zapis z czasow innej krzywej (wiecej wydanych, niz dzis sie nalezy) nie moze dac
        // ujemnej puli - wtedy KAZDY wezel bylby zablokowany bez powodu widocznego dla gracza.
        val ranks = mapOf("dps_might" to 5, "dps_edge" to 5)
        assertEquals(0, TalentRules.availablePoints(level = 3, ranks = ranks))
    }

    @Test
    fun `zerowanie drzewka oddaje wszystkie punkty`() {
        val ranks = mapOf("dps_might" to 5, "dps_precision" to 2)
        assertEquals(4, TalentRules.availablePoints(level = 12, ranks = ranks))
        assertEquals(11, TalentRules.availablePoints(level = 12, ranks = emptyMap()))
    }

    @Test
    fun `stan wezlow pokrywa cale drzewko`() {
        val states = TalentRules.statesOf(TalentTree.Dps, ranks = emptyMap(), availablePoints = 1)
        assertEquals(TalentCatalog.forTree(TalentTree.Dps).size, states.size)
        assertTrue(states.none { it.isLearned }, "Puste drzewko nie ma wykupionych wezlow")
    }
}
