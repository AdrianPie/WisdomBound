package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.CountryData
import com.lernhero.domain.quiz.bank.CountryFact
import com.lernhero.domain.quiz.bank.CountryQuestions
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pytania losowane z tabeli panstw ([CountryQuestions]) - nikt ich nie czyta przed gra, wiec
 * poprawnosc KAZDEGO typu sprawdza sie tu z tabeli, na wielu seedach. Wystarczy jedna pomylka
 * w budowaniu (odwrocony znak, zly dystraktor), zeby gra uczyla bzdur tysiace razy.
 */
class CountryQuestionsTest {

    private val countries = CountryData.all
    private val byCode = countries.associateBy { it.code }
    private val byName = countries.associateBy { it.name }
    private val byCapital = countries.associateBy { it.capital }
    private val seeds = (1L..40L).map { Random(it) }

    private fun kindOf(id: String, prefix: String = CountryQuestions.ID_PREFIX) =
        id.removePrefix(prefix).substringBefore('_')

    /** Pierwszy kod po rodzaju pytania - panstwo, o ktore pyta ("cty_borders_de_...") . */
    private fun subjectOf(id: String, prefix: String = CountryQuestions.ID_PREFIX) =
        byCode.getValue(id.removePrefix(prefix).substringAfter('_').substringBefore('_'))

    @Test
    fun `tabela panstw jest spojna`() {
        assertEquals(194, countries.size)
        assertEquals(countries.size, byCode.size, "Zdublowane kody")
        assertEquals(countries.size, byName.size, "Zdublowane nazwy - odpowiedzi musza sie roznic")
        assertEquals(countries.size, byCapital.size, "Zdublowane stolice")
        countries.forEach { c ->
            c.neighbours.forEach { n ->
                assertTrue(c.code in byCode.getValue(n).neighbours, "Granica ${c.code}-$n nie jest symetryczna")
            }
            assertTrue(c.unclear.none { it in c.neighbours }, "${c.code}: para niejasna jest wsrod sasiadow")
            assertTrue(c.fame in 1..3)
        }
        // Kilka faktow, ktore kazdy zna - tabela, ktora by je psula, psulaby wszystko.
        assertTrue(byCode.getValue("cn").population > byCode.getValue("us").population)
        assertTrue(byCode.getValue("ru").areaKm2 > byCode.getValue("ca").areaKm2)
        assertTrue(byCode.getValue("no").capitalLat > byCode.getValue("it").capitalLat)
        assertTrue("pl" in byCode.getValue("de").neighbours && "es" !in byCode.getValue("de").neighbours)
        assertTrue(byCode.getValue("ch").landlocked && !byCode.getValue("fr").landlocked)
    }

    @Test
    fun `pytania z wyborem zgadzaja sie z tabela`() {
        seeds.forEach { random ->
            val entries = CountryQuestions.choices(random)
            assertEquals(entries.size, entries.map { it.id }.toSet().size, "Zdublowane id")
            entries.forEach { e ->
                assertTrue(e.id.startsWith(CountryQuestions.ID_PREFIX) && e.generated, e.id)
                assertEquals(QuizCategory.Geography, e.category)
                assertEquals(CountryQuestions.SECTION, e.section)
                assertEquals(4, e.answers.distinct().size, "${e.id}: odpowiedzi sie powtarzaja")
                checkChoice(e)
            }
        }
    }

    private fun checkChoice(e: QuizEntry) {
        val kind = kindOf(e.id)
        fun countriesOf() = e.answers.map { byName.getValue(it) }
        fun capitalsOf() = e.answers.map { byCapital.getValue(it) }
        val ok = when (kind) {
            "mostpeople" -> countriesOf().maxBy { it.population }.name == e.correct
            "fewestpeople" -> countriesOf().minBy { it.population }.name == e.correct
            "largest" -> countriesOf().maxBy { it.areaKm2 }.name == e.correct
            "smallest" -> countriesOf().minBy { it.areaKm2 }.name == e.correct
            "densest" -> countriesOf().maxBy { it.density }.name == e.correct
            "north" -> capitalsOf().maxBy { it.capitalLat }.capital == e.correct
            "south" -> capitalsOf().minBy { it.capitalLat }.capital == e.correct
            "east" -> capitalsOf().maxBy { it.capitalLon }.capital == e.correct
            "west" -> capitalsOf().minBy { it.capitalLon }.capital == e.correct
            "borders" -> subjectOf(e.id).let { s ->
                byName.getValue(e.correct).code in s.neighbours && e.wrong.none { byName.getValue(it).code in s.neighbours }
            }
            "notborders" -> subjectOf(e.id).let { s ->
                byName.getValue(e.correct).code !in s.neighbours && e.wrong.all { byName.getValue(it).code in s.neighbours }
            }
            "landlocked" -> byName.getValue(e.correct).landlocked && e.wrong.none { byName.getValue(it).landlocked }
            "coast" -> !byName.getValue(e.correct).landlocked && e.wrong.all { byName.getValue(it).landlocked }
            "people" -> subjectOf(e.id).let { CountryQuestions.roughly(it.population.toDouble()) == e.correct }
            "closest" -> subjectOf(e.id).let { from ->
                capitalsOf().minBy { CountryQuestions.distanceKm(from, it) }.capital == e.correct
            }
            else -> false
        }
        assertTrue(ok, "Bledne pytanie ${e.id}: '${e.prompt}' -> ${e.correct} / ${e.wrong}")
    }

    @Test
    fun `niejasne granice nie trafiaja do pytan o sasiadow`() {
        seeds.forEach { random ->
            CountryQuestions.choices(random).filter { kindOf(it.id) in setOf("borders", "notborders") }.forEach { e ->
                val subject = subjectOf(e.id)
                e.answers.map { byName.getValue(it) }.forEach { other ->
                    assertTrue(other.code !in subject.unclear && subject.code !in other.unclear, "${e.id}: para niejasna")
                }
            }
        }
    }

    @Test
    fun `ukladanki sa w poprawnej kolejnosci`() {
        val prefix = CountryQuestions.ID_PREFIX + "ord_"
        seeds.forEach { random ->
            CountryQuestions.ordering(random).forEach { e ->
                assertEquals(4, e.items.distinct().size, e.id)
                val values: List<Double> = when (kindOf(e.id, prefix)) {
                    "people" -> e.items.map { byName.getValue(it).population.toDouble() }
                    "area" -> e.items.map { byName.getValue(it).areaKm2 }
                    "westeast" -> e.items.map { byCapital.getValue(it).capitalLon }
                    "southnorth" -> e.items.map { byCapital.getValue(it).capitalLat }
                    else -> error("Nieznany rodzaj ukladanki ${e.id}")
                }
                assertEquals(values.sorted(), values, "${e.id}: zla kolejnosc ${e.items}")
            }
        }
    }

    @Test
    fun `prawda falsz zgadza sie z tabela i nie jest jednostronna`() {
        val prefix = CountryQuestions.ID_PREFIX + "tf_"
        val all = seeds.flatMap { CountryQuestions.trueFalse(it) }
        all.forEach { e ->
            val codes = e.id.removePrefix(prefix).substringAfter('_').split('_').map { byCode.getValue(it) }
            val truth = when (kindOf(e.id, prefix)) {
                "people" -> codes[0].population > codes[1].population
                "area" -> codes[0].areaKm2 > codes[1].areaKm2
                "north" -> codes[0].capitalLat > codes[1].capitalLat
                "east" -> codes[0].capitalLon > codes[1].capitalLon
                "border" -> codes[1].code in codes[0].neighbours
                "landlocked" -> codes[0].landlocked
                else -> error("Nieznany rodzaj prawda/falsz ${e.id}")
            }
            assertEquals(truth, e.isTrue, "${e.id}: '${e.statement}'")
        }
        val trueShare = all.count { it.isTrue }.toFloat() / all.size
        assertTrue(trueShare in 0.35f..0.65f, "Udzial prawdziwych: $trueShare")
    }

    @Test
    fun `zaznacz wszystkie zgadza sie z tabela`() {
        val prefix = CountryQuestions.ID_PREFIX + "ms_"
        seeds.forEach { random ->
            CountryQuestions.multiSelect(random).forEach { e ->
                assertTrue(e.correct.size >= 2 && e.wrong.size >= 3, e.id)
                assertEquals(e.options.size, e.options.distinct().size, e.id)
                val good: (CountryFact) -> Boolean = when (kindOf(e.id, prefix)) {
                    "borders" -> subjectOf(e.id, prefix).let { s -> { it.code in s.neighbours } }
                    "landlocked" -> { c -> c.landlocked }
                    "people" -> subjectOf(e.id, prefix).let { p -> { it.population > p.population } }
                    "area" -> subjectOf(e.id, prefix).let { p -> { it.areaKm2 > p.areaKm2 } }
                    else -> error("Nieznany rodzaj zaznacz-wszystkie ${e.id}")
                }
                assertTrue(e.correct.all { good(byName.getValue(it)) }, "${e.id}: zla poprawna ${e.correct}")
                assertTrue(e.wrong.none { good(byName.getValue(it)) }, "${e.id}: poprawna wsrod blednych ${e.wrong}")
            }
        }
    }

    @Test
    fun `kazdy poziom dostaje swoje pytania i ten sam seed daje to samo`() {
        val choices = CountryQuestions.choices(Random(3))
        QuizDifficulty.entries.forEach { level ->
            val count = choices.count { it.difficulty == level }
            assertTrue(count >= CountryQuestions.CHOICES_PER_LEVEL * 3 / 4, "Poziom $level: tylko $count pytan z danych")
        }
        assertEquals(choices.map { it.id }, CountryQuestions.choices(Random(3)).map { it.id })
        // Latwe pytania tylko o panstwa, ktore zna kazdy.
        choices.filter { it.difficulty == QuizDifficulty.Easy && kindOf(it.id) in setOf("mostpeople", "largest") }
            .forEach { e -> assertTrue(e.answers.all { byName.getValue(it).fame == 1 }, e.id) }
    }

    @Test
    fun `liczby ludnosci zaokraglaja sie po ludzku`() {
        assertEquals("About 38 million", CountryQuestions.roughly(38_000_000.0))
        assertEquals("About 1.4 billion", CountryQuestions.roughly(1_419_000_000.0))
        assertEquals("About 5.6 million", CountryQuestions.roughly(5_600_000.0))
        assertEquals("About 130 million", CountryQuestions.roughly(130_900_000.0))
        assertEquals("About 39,000", CountryQuestions.roughly(39_000.0))
        assertEquals("About 1,100", CountryQuestions.roughly(1_111.0))
    }
}
