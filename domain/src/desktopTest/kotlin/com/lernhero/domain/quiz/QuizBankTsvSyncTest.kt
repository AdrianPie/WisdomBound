package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.CountryData
import com.lernhero.domain.quiz.bank.CountryFact
import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Pytania wyboru maja zrodlo w `domain/quizbank/<temat>.tsv` (albo w katalogu dzialow
 * `domain/quizbank/<temat>/...`), a pliki `<Temat>Questions.kt` i [com.lernhero.domain.quiz.bank.QuizSectionCatalog]
 * sa z nich GENEROWANE przez `tools/quizbank.py generate`. Ten test pilnuje, zeby jedno z drugim sie
 * nie rozjechalo: reczna poprawka w Kotlinie przepadlaby przy nastepnym generowaniu, a pytanie
 * dopisane do TSV bez generowania nie trafiloby do gry.
 *
 * Siedzi w desktopTest, a nie w commonTest, bo czyta pliki z dysku. Katalog roboczy testow to
 * katalog modulu :domain.
 */
class QuizBankTsvSyncTest {

    private val dataDir = File("quizbank")

    /** Nazwa pliku TSV (albo katalogu dzialow) -> temat. Nowy temat wymaga wpisu tutaj i w TOPICS w tools/quizbank.py. */
    private val categoryOf = mapOf(
        "biology" to QuizCategory.Biology,
        "animals" to QuizCategory.Animals,
        "history" to QuizCategory.History,
        "elements" to QuizCategory.Elements,
        "capitals" to QuizCategory.Capitals,
        "science" to QuizCategory.Science,
        "geography" to QuizCategory.Geography,
        "sport" to QuizCategory.Sport,
        "math" to QuizCategory.Math,
        "literature" to QuizCategory.Literature,
        "technology" to QuizCategory.Technology,
        "mythology" to QuizCategory.Mythology,
        "pop_culture" to QuizCategory.PopCulture,
        "poland" to QuizCategory.Poland
    )

    @Test
    fun `pliki Kotlin banku sa wygenerowane z aktualnych TSV`() {
        assertTrue(dataDir.isDirectory, "Brak katalogu z TSV: ${dataDir.absolutePath}")
        // Sciezka wzgledem katalogu danych: "biology" albo "history/antiquity/roman_empire".
        val files = dataDir.walkTopDown()
            .onEnter { it.name != DATA_SUBDIR }
            .filter { it.isFile && it.extension == "tsv" && it.name != SECTIONS_FILE }
            .associateBy { it.relativeTo(dataDir).invariantSeparatorsPath.removeSuffix(".tsv") }
        val unknown = files.keys.filter { it.substringBefore('/') !in categoryOf }
        assertTrue(unknown.isEmpty(), "TSV bez przypisanego tematu: $unknown")

        val fromTsv = files.flatMap { (path, file) ->
            readTsv(file, categoryOf.getValue(path.substringBefore('/')), section = path.takeIf { '/' in it })
        }.associateBy { it.id }
        val fromKotlin = QuizBank.all(Random(1)).filter { !it.generated }.associateBy { it.id }

        val onlyTsv = fromTsv.keys - fromKotlin.keys
        val onlyKotlin = fromKotlin.keys - fromTsv.keys
        val different = fromTsv.keys.intersect(fromKotlin.keys).filter { fromTsv[it] != fromKotlin[it] }
        val hint = "Uruchom `python3 tools/quizbank.py generate` i nie poprawiaj plikow *Questions.kt recznie."

        assertTrue(onlyTsv.isEmpty(), "Pytania z TSV, ktorych nie ma w Kotlinie: ${onlyTsv.take(10)}. $hint")
        assertTrue(onlyKotlin.isEmpty(), "Pytania z Kotlina, ktorych nie ma w TSV: ${onlyKotlin.take(10)}. $hint")
        assertTrue(
            different.isEmpty(),
            "Rozne tresci w TSV i w Kotlinie: ${different.take(5).map { "${fromTsv[it]} != ${fromKotlin[it]}" }}. $hint"
        )
    }

    @Test
    fun `katalog dzialow jest wygenerowany z aktualnego sections tsv`() {
        val lines = File(dataDir, SECTIONS_FILE).readLines().filter { it.isNotBlank() && !it.startsWith("#") }
        assertTrue(lines.first() == "path\tlabel\ticon", "$SECTIONS_FILE: zly naglowek ${lines.first()}")
        val fromTsv = lines.drop(1).map { line ->
            val (path, label, icon) = line.split('\t')
            QuizSection(path, label, icon, categoryOf.getValue(path.substringBefore('/')))
        }

        assertTrue(
            fromTsv == QuizSections.all,
            "QuizSectionCatalog nie zgadza sie z $SECTIONS_FILE - uruchom `python3 tools/quizbank.py generate`."
        )
        fromTsv.forEach { section ->
            val segments = section.path.split('/').toTypedArray()
            val asFile = File(dataDir, segments.joinToString("/") + ".tsv")
            val asDir = File(dataDir, segments.joinToString("/"))
            assertTrue(asFile.isFile != asDir.isDirectory, "Dzial ${section.path}: potrzebny plik .tsv albo katalog")
        }
    }

    @Test
    fun `dane o panstwach sa wygenerowane z aktualnego countries tsv`() {
        val lines = File(dataDir, "$DATA_SUBDIR/$COUNTRIES_FILE").readLines().filter { it.isNotBlank() && !it.startsWith("#") }
        assertTrue(lines.first().startsWith("code\tcountry\tcapital\t"), "$COUNTRIES_FILE: zly naglowek ${lines.first()}")
        val continents = mapOf(
            "Europe" to CountryFact.Continent.Europe,
            "Asia" to CountryFact.Continent.Asia,
            "Africa" to CountryFact.Continent.Africa,
            "North America" to CountryFact.Continent.NorthAmerica,
            "South America" to CountryFact.Continent.SouthAmerica,
            "Oceania" to CountryFact.Continent.Oceania
        )
        fun codes(cell: String) = cell.split(' ').filter { it.isNotEmpty() }.toSet()
        val fromTsv = lines.drop(1).map { line ->
            val c = line.split('\t')
            assertTrue(c.size == 13, "$COUNTRIES_FILE: zla liczba kolumn w linii '$line'")
            CountryFact(
                code = c[0], name = c[1], capital = c[2], continent = continents.getValue(c[3]),
                population = c[4].toLong(), areaKm2 = c[5].toDouble(), capitalLat = c[6].toDouble(),
                capitalLon = c[7].toDouble(), landlocked = c[8] == "1", neighbours = codes(c[9]),
                fame = c[10].toInt(), capitalQuiz = c[11] == "1", unclear = codes(c[12])
            )
        }
        assertTrue(
            fromTsv == CountryData.all,
            "CountryData nie zgadza sie z $COUNTRIES_FILE - uruchom `python3 tools/quizbank.py generate`."
        )
    }

    private fun readTsv(file: File, category: QuizCategory, section: String?): List<QuizEntry> {
        val lines = file.readLines().filter { it.isNotBlank() && !it.startsWith("#") }
        val header = lines.first().split('\t')
        assertTrue(
            header == listOf("id", "difficulty", "prompt", "correct", "wrong1", "wrong2", "wrong3"),
            "${file.name}: zly naglowek $header"
        )
        return lines.drop(1).map { line ->
            val cells = line.split('\t')
            assertTrue(cells.size == 7, "${file.name}: zla liczba kolumn w linii '$line'")
            QuizEntry(
                id = cells[0],
                prompt = cells[2],
                correct = cells[3],
                wrong = cells.subList(4, 7),
                category = category,
                difficulty = QuizDifficulty.valueOf(cells[1]),
                section = section
            )
        }
    }

    private companion object {
        /** Spis dzialow - poza katalogiem [DATA_SUBDIR] jedyny TSV, ktory nie jest plikiem pytan. */
        const val SECTIONS_FILE = "sections.tsv"

        /** Katalog tabel z danymi (nie pytaniami) - z [COUNTRIES_FILE] losuje CountryQuestions. */
        const val DATA_SUBDIR = "data"
        const val COUNTRIES_FILE = "countries.tsv"
    }
}
