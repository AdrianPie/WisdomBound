package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.QuizSectionCatalog

/**
 * Dzial tematu - galaz albo lisc drzewa, w ktore jest poukladany duzy temat. Historia ma ponad
 * dwa tysiace pytan: bez podzialu "Historia" to worek, z ktorego nie da sie wybrac Cesarstwa
 * Rzymskiego ani sprawdzic, czy w banku nie brakuje sredniowiecza.
 *
 * [path] to sciezka jak w katalogu z danymi: "history/antiquity/roman_empire" znaczy temat
 * History, epoka Antiquity, dzial Roman Empire. Pierwszy czlon to zawsze temat, wiec sciezka
 * jest unikalna w calym banku. Pytania wisza tylko w lisciach (patrz [QuizEntry.section]),
 * a galaz obejmuje wszystko pod soba.
 *
 * Dzialy i ich kolejnosc (w historii chronologiczna) stoja w `domain/quizbank/sections.tsv`,
 * z ktorego powstaje [QuizSectionCatalog].
 */
data class QuizSection(
    val path: String,
    val label: String,
    /** Emoji dzialu - placeholder pod przyszla grafike, tak jak [QuizCategory.icon]. */
    val icon: String,
    val category: QuizCategory
) {
    /** Sciezka dzialu nadrzednego albo null, gdy dzial wisi wprost pod tematem (np. epoka). */
    val parentPath: String? get() = path.substringBeforeLast('/').takeIf { '/' in it }

    /** Czy pytanie lezy w tym dziale albo w ktorymkolwiek z jego poddzialow. */
    fun covers(entry: QuizEntry): Boolean {
        val section = entry.section ?: return false
        return section == path || section.startsWith("$path/")
    }
}

/** Drzewo dzialow calego banku. */
object QuizSections {

    val all: List<QuizSection> = QuizSectionCatalog.all

    private val byPath: Map<String, QuizSection> = all.associateBy { it.path }

    fun of(path: String): QuizSection? = byPath[path]

    /** Pierwszy szczebel pod tematem - w historii epoki, od prehistorii do dzis. */
    fun topLevel(category: QuizCategory): List<QuizSection> =
        all.filter { it.category == category && it.parentPath == null }

    fun children(section: QuizSection): List<QuizSection> = all.filter { it.parentPath == section.path }

    /** Droga od szczebla pod tematem do dzialu - np. [Antiquity, Roman Empire] - do okruszkow w UI. */
    fun trail(path: String): List<QuizSection> =
        generateSequence(byPath[path]) { section -> section.parentPath?.let(byPath::get) }.toList().asReversed()
}
