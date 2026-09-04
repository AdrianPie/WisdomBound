package com.lernhero.domain.quiz

import com.lernhero.domain.monster.Element
import kotlin.random.Random

/**
 * Wybor kategorii pytania przed tura - mechanika rodem z Quiz RPG (Mystic Wiz): zamiast dostac
 * losowe pytanie, gracz widzi CZTERY kafle i sam decyduje, z czego chce byc odpytany.
 *
 * Kafel to nie tylko temat. Do kazdego dolosowany jest bonus frakcyjny ([CategoryOption.elements]),
 * wiec wybor jest realnym kompromisem: "znam historie, ale ten kafel wzmacnia Ogien, a ja mam
 * w druzynie dwa szkielety Smierci". Czasem kafel niesie DWA zywioly naraz - wtedy jest tym
 * lakomszy, mimo ze temat moze byc trudniejszy.
 *
 * Kategorie bez pokrycia w banku ([QuizCategory.Vocabulary], [QuizCategory.Custom]) nigdy tu nie
 * trafiaja - to material gracza, ktory ma wlasny tryb (endless "Wlasne"), a nie temat do wyboru.
 */
data class CategoryOffer(
    val options: List<CategoryOption>
) {
    companion object {
        /** Ile kafli pokazujemy - 2x2, tak jak w pierwowzorze. */
        const val OPTION_COUNT = 4

        /** Szansa, ze kafel niesie DWA zywioly zamiast jednego. */
        const val DUAL_ELEMENT_CHANCE = 0.25f

        /** O ile mocniej bije jednostka, ktorej zywiol zostal wzmocniony wybranym kaflem. */
        const val ELEMENT_BOOST = 0.20f

        /** Kategorie, ktore realnie maja pytania w [QuizBank]. */
        private val offerable: List<QuizCategory> get() = QuizCategory.builtin

        /**
         * Sklada oferte na jedna ture: cztery ROZNE kategorie, kazda z wlasnym bonusem frakcyjnym.
         * Zywioly moga sie powtarzac miedzy kaflami - to normalne, czasem po prostu wypada tura
         * dobra dla Ognia.
         */
        fun roll(random: Random): CategoryOffer {
            val categories = offerable.shuffled(random).take(OPTION_COUNT)
            return CategoryOffer(
                options = categories.map { category ->
                    val elements = if (random.nextFloat() < DUAL_ELEMENT_CHANCE) {
                        Element.entries.shuffled(random).take(2)
                    } else {
                        listOf(Element.entries.random(random))
                    }
                    CategoryOption(category = category, elements = elements)
                }
            )
        }
    }
}

/**
 * Jeden kafel oferty: temat pytania plus zywioly, ktore ten wybor wzmocni na CALA nadchodzaca ture
 * (patrz [CategoryOffer.ELEMENT_BOOST]).
 */
data class CategoryOption(
    val category: QuizCategory,
    /** Jeden albo dwa zywioly - patrz [CategoryOffer.DUAL_ELEMENT_CHANCE]. */
    val elements: List<Element>
)
