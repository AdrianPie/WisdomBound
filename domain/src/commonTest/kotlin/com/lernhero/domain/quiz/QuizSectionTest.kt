package com.lernhero.domain.quiz

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Drzewo dzialow ([QuizSection]) - temat podzielony na dzialy ma sens tylko wtedy, gdy KAZDE jego
 * pytanie lezy w jakims lisciu, a kazdy lisc ma z czego pytac. Dzial z trzema pytaniami to pusta
 * szuflada z ladna etykieta: gracz, ktory go wybierze, po pierwszej fali dostaje powtorki.
 */
class QuizSectionTest {

    private val all = QuizBank.all(Random(11))
    private val leaves = QuizSections.all.filter { QuizSections.children(it).isEmpty() }

    @Test
    fun `kazde pytanie tematu z dzialami lezy w lisciu drzewa`() {
        val split = QuizSections.all.map { it.category }.toSet()
        val leafPaths = leaves.map { it.path }.toSet()

        val homeless = all.filter { it.category in split && it.section !in leafPaths }
            .map { "${it.id} (${it.section})" }
        assertTrue(homeless.isEmpty(), "Pytania bez dzialu albo w galezi zamiast w lisciu: ${homeless.take(10)}")

        val stray = all.filter { it.section != null && it.category !in split }.map { it.id }
        assertTrue(stray.isEmpty(), "Dzial przy pytaniu tematu bez dzialow: ${stray.take(10)}")

        val wrongTopic = all.filter { entry ->
            entry.section?.let { QuizSections.of(it)?.category != entry.category } == true
        }
        assertTrue(wrongTopic.isEmpty(), "Dzial z innego tematu niz pytanie: ${wrongTopic.take(10).map { it.id }}")
    }

    @Test
    fun `kazdy lisc ma z czego pytac na kazdym poziomie wejscia`() {
        leaves.forEach { leaf ->
            val inLeaf = all.filter(leaf::covers)
            assertTrue(inLeaf.size >= MIN_PER_LEAF, "Dzial ${leaf.path} ma tylko ${inLeaf.size} pytan")
            // Pierwsza fala zaczyna od Easy/Medium - dzial bez nich zaczynalby od razu od trudnych.
            val gentle = inLeaf.count { it.difficulty == QuizDifficulty.Easy || it.difficulty == QuizDifficulty.Medium }
            assertTrue(gentle >= MIN_GENTLE_PER_LEAF, "Dzial ${leaf.path} ma tylko $gentle pytan Easy/Medium")
        }
    }

    @Test
    fun `sciezki dzialow tworza drzewo pod swoim tematem`() {
        val paths = QuizSections.all.map { it.path }
        assertEquals(paths.size, paths.distinct().size, "Zdublowane sciezki dzialow")
        QuizSections.all.forEachIndexed { index, section ->
            val parent = section.parentPath ?: return@forEachIndexed
            val parentIndex = paths.indexOf(parent)
            assertTrue(parentIndex in 0 until index, "Dzial ${section.path}: rodzic $parent musi stac wczesniej")
            assertEquals(section.category, QuizSections.of(parent)?.category, "Dzial ${section.path} w obcym temacie")
        }
        QuizSections.all.groupBy { it.parentPath to it.category }.forEach { (_, siblings) ->
            val labels = siblings.map { it.label }
            assertEquals(labels.size, labels.distinct().size, "Rodzenstwo z ta sama nazwa: $labels")
        }
    }

    @Test
    fun `galaz obejmuje dokladnie pytania swoich poddzialow`() {
        QuizSections.all.filter { QuizSections.children(it).isNotEmpty() }.forEach { branch ->
            val direct = QuizBank.inSection(branch).map { it.id }.toSet()
            val fromChildren = QuizSections.children(branch).flatMap { QuizBank.inSection(it) }.map { it.id }.toSet()
            assertEquals(fromChildren, direct, "Galaz ${branch.path} nie pokrywa sie z poddzialami")
        }
    }

    @Test
    fun `okruszki prowadza od epoki do dzialu`() {
        assertEquals(
            listOf("Antiquity", "Roman Empire"),
            QuizSections.trail("history/antiquity/roman_empire").map { it.label }
        )
        assertEquals(listOf("Prehistory"), QuizSections.trail("history/prehistory").map { it.label })
        assertTrue(QuizSections.trail("history/no_such_section").isEmpty())

        val eras = QuizSections.topLevel(QuizCategory.History).map { it.path }
        assertEquals("history/prehistory", eras.first(), "Epoki ida chronologicznie, od prehistorii")
        assertTrue("history/antiquity" in eras && "history/middle_ages" in eras, "Brak epok w historii: $eras")
    }

    private companion object {
        /** Tyle pytan musi miec kazdy lisc, zeby wybrany dzial wytrzymal kilka fal bez powtorek. */
        const val MIN_PER_LEAF = 25

        const val MIN_GENTLE_PER_LEAF = 10
    }
}
