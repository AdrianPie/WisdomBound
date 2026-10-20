package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.MultiSelectEntry
import com.lernhero.domain.quiz.OrderingEntry
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry
import com.lernhero.domain.quiz.TrueFalseEntry
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Jedno panstwo z tabeli [CountryData] (zrodlo: domain/quizbank/data/countries.tsv - tam jest opis
 * kazdej kolumny). [neighbours] i [unclear] to kody panstw z tej samej tabeli.
 */
data class CountryFact(
    val code: String,
    val name: String,
    val capital: String,
    val continent: Continent,
    val population: Long,
    val areaKm2: Double,
    val capitalLat: Double,
    val capitalLon: Double,
    val landlocked: Boolean,
    val neighbours: Set<String>,
    val fame: Int,
    val capitalQuiz: Boolean,
    val unclear: Set<String>
) {
    enum class Continent { Europe, Asia, Africa, NorthAmerica, SouthAmerica, Oceania }

    val density: Double get() = population / areaKm2

    /** Nazwa w srodku zdania: "the Netherlands", ale "Poland". */
    val inSentence: String get() = if (name.startsWith("The ")) "the " + name.removePrefix("The ") else name
}

/**
 * Pytania LOSOWANE z tabeli danych o panstwach - tak, jak robia to quizy geograficzne w sieci:
 * "ktore z tych panstw ma najwiecej ludzi", "uloz od najmniejszego", "uloz stolice z zachodu na
 * wschod", "ktore z tych graniczy z Niemcami", "wyzej czy nizej" (prawda/falsz) i "zaznacz
 * wszystkie wieksze od...". Recznie pisanych pytan tego rodzaju nie da sie miec duzo bez powtorek,
 * a z tabeli wychodzi praktycznie nieskonczenie wiele zestawow - kazde losowanie jest inne.
 *
 * Poziom trudnosci wynika z dwoch rzeczy: jak znane sa panstwa ([CountryFact.fame]) i jak wyrazna
 * jest roznica (Easy: trzykrotna, Expert: o 30% i z jednego kontynentu). Dzieki temu progowi
 * pytanie zostaje poprawne mimo przyblizonych danych i tego, ze liczba ludnosci co roku sie zmienia.
 *
 * Wszystkie pytania stad maja kategorie [QuizCategory.Geography], dzial [SECTION] i
 * [QuizEntry.generated] = true. Id zawiera kody panstw, wiec ten sam zestaw dostaje to samo id
 * i [com.lernhero.domain.quiz.QuestionHistory] nie pozwoli mu wrocic za szybko.
 *
 * Losowanie idzie wylacznie przez przekazany [Random], wiec ten sam seed daje te same pytania.
 */
object CountryQuestions {

    /** Dzial, do ktorego trafiaja pytania z tabeli. */
    const val SECTION = "geography/human/countries"

    /** Prefiks id - zadne pytanie z TSV go nie uzywa. */
    const val ID_PREFIX = "cty_"

    /**
     * Ile pytan kazdego typu powstaje na poziom przy jednym losowaniu. To steruje UDZIALEM tych pytan
     * w puli (reczne pytania z geografii to ~1500 sztuk), a nie ich roznorodnoscia - przy kazdym
     * losowaniu i tak powstaja nowe zestawy.
     */
    const val CHOICES_PER_LEVEL = 40
    const val ORDERING_PER_LEVEL = 4
    const val TRUE_FALSE_PER_LEVEL = 6
    const val MULTI_SELECT_PER_LEVEL = 3

    private val countries: List<CountryFact> = CountryData.all
    private val byCode: Map<String, CountryFact> = countries.associateBy { it.code }

    /** Wybor z czterech: najwiecej/najmniej ludzi, powierzchnia, gestosc, stolice, granice, morze. */
    fun choices(random: Random, perLevel: Int = CHOICES_PER_LEVEL): List<QuizEntry> =
        generate(perLevel, random, choiceBuilders) { it.id }

    /** "Uloz w kolejnosci": ludnosc, powierzchnia, stolice z zachodu na wschod i z poludnia na polnoc. */
    fun ordering(random: Random, perLevel: Int = ORDERING_PER_LEVEL): List<OrderingEntry> =
        generate(perLevel, random, orderingBuilders) { it.id }

    /** "Wyzej czy nizej" jako prawda/falsz, granice i dostep do morza. */
    fun trueFalse(random: Random, perLevel: Int = TRUE_FALSE_PER_LEVEL): List<TrueFalseEntry> =
        generate(perLevel, random, trueFalseBuilders) { it.id }

    /** "Zaznacz wszystkie": sasiedzi, panstwa bez morza, wieksze/ludniejsze od wskazanego. */
    fun multiSelect(random: Random, perLevel: Int = MULTI_SELECT_PER_LEVEL): List<MultiSelectEntry> =
        generate(perLevel, random, multiSelectBuilders) { it.id }

    /* ------------------------------------------------------------------------------------------ */
    /* Poziomy                                                                                     */

    /**
     * [fame] - jakie panstwa wolno wylosowac; [ratio] - o ile razy musza sie roznic porownywane
     * liczby; [degrees] - o ile stopni porownywane stolice; [oneContinent] - zestaw z jednego
     * kontynentu (Rwanda obok Burundi jest trudniejsza niz Rwanda obok Rosji).
     */
    private class Level(
        val difficulty: QuizDifficulty,
        val fame: IntRange,
        val ratio: Double,
        val degrees: Double,
        val oneContinent: Boolean
    ) {
        /** Najmniej znane panstwo, ktore moze sie pojawic jako dystraktor albo sasiad. */
        val fameCap: Int get() = fame.last
        val atLeastMedium: Boolean get() = difficulty != QuizDifficulty.Easy
        val atLeastHard: Boolean get() = difficulty == QuizDifficulty.Hard || difficulty == QuizDifficulty.Expert
    }

    private val levels = listOf(
        Level(QuizDifficulty.Easy, 1..1, ratio = 3.0, degrees = 12.0, oneContinent = false),
        Level(QuizDifficulty.Medium, 1..2, ratio = 2.0, degrees = 7.0, oneContinent = false),
        Level(QuizDifficulty.Hard, 1..3, ratio = 1.5, degrees = 4.0, oneContinent = true),
        Level(QuizDifficulty.Expert, 2..3, ratio = 1.3, degrees = 2.5, oneContinent = true)
    )

    /** Ile razy probujemy zbudowac pytanie, zanim poziom zostanie z mniejsza liczba pytan. */
    private const val ATTEMPTS_PER_QUESTION = 25

    private fun <T> generate(
        perLevel: Int,
        random: Random,
        builders: List<(Level, Random) -> T?>,
        idOf: (T) -> String
    ): List<T> {
        val out = LinkedHashMap<String, T>()
        levels.forEach { level ->
            var made = 0
            var attempts = 0
            while (made < perLevel && attempts < perLevel * ATTEMPTS_PER_QUESTION) {
                attempts++
                val entry = builders[random.nextInt(builders.size)](level, random) ?: continue
                val id = idOf(entry)
                if (id !in out) {
                    out[id] = entry
                    made++
                }
            }
        }
        return out.values.toList()
    }

    /** Panstwa kazdego poziomu, tez rozbite na kontynenty - liczone raz, bo losowanie idzie przy kazdym pytaniu. */
    private val famous: Map<Level, List<CountryFact>> = levels.associateWith { level -> countries.filter { it.fame in level.fame } }
    private val famousOnContinent: Map<Level, Map<CountryFact.Continent, List<CountryFact>>> =
        famous.mapValues { (_, list) -> list.groupBy { it.continent } }

    /** Panstwa dozwolone na poziomie - z jednego wylosowanego kontynentu, gdy poziom tego chce. */
    private fun pool(level: Level, random: Random, filter: (CountryFact) -> Boolean = { true }): List<CountryFact> {
        if (!level.oneContinent) return famous.getValue(level).filter(filter)
        val continents = famousOnContinent.getValue(level)
        return continents.getValue(continents.keys.random(random)).filter(filter)
    }

    private fun <T> List<T>.pickOrNull(count: Int, random: Random): List<T>? =
        if (size < count) null else shuffled(random).take(count)

    private fun codes(countries: List<CountryFact>): String = countries.map { it.code }.sorted().joinToString("_")

    /* ------------------------------------------------------------------------------------------ */
    /* Granice                                                                                     */

    private fun unclear(a: CountryFact, b: CountryFact): Boolean = b.code in a.unclear || a.code in b.unclear

    private fun neighboursOf(country: CountryFact, level: Level): List<CountryFact> =
        country.neighbours.mapNotNull { byCode[it] }.filter { it.fame <= level.fameCap }

    /**
     * Panstwa, ktore NIE granicza z [country], a wygladaja, jakby mogly: najpierw sasiedzi sasiadow
     * (bez dalekich - Brazylia jest sasiadka sasiadki Hiszpanii tylko przez Gujane Francuska),
     * potem najblizsze panstwa kontynentu. Wyspy bez zadnej granicy ladowej odpadaja - Grenada
     * jako "moze graniczy z Gwatemala" niczego nie sprawdza. Pary z kolumny `unclear` odpadaja
     * w obie strony.
     */
    private fun decoysOf(country: CountryFact, level: Level, random: Random): List<CountryFact> {
        val excluded: (CountryFact) -> Boolean = {
            it.code == country.code || it.code in country.neighbours || unclear(country, it) ||
                it.fame > level.fameCap || it.neighbours.isEmpty()
        }
        val near = country.neighbours.flatMap { byCode[it]?.neighbours.orEmpty() }.distinct()
            .mapNotNull { byCode[it] }.filterNot(excluded)
            .filter { distanceKm(country, it) < NEAR_DECOY_KM }.shuffled(random)
        val rest = countries.filter { it.continent == country.continent && it !in near }.filterNot(excluded)
            .sortedBy { distanceKm(country, it) }.take(NEAREST_REST).shuffled(random)
        return near + rest
    }

    /* ------------------------------------------------------------------------------------------ */
    /* Wybor z czterech                                                                            */

    private fun choice(kind: String, key: String, prompt: String, correct: String, wrong: List<String>, level: Level) =
        QuizEntry(
            id = "${ID_PREFIX}${kind}_$key",
            prompt = prompt,
            correct = correct,
            wrong = wrong,
            category = QuizCategory.Geography,
            difficulty = level.difficulty,
            section = SECTION,
            generated = true
        )

    /** "Ktore z tych ma najwiecej/najmniej..." - zwyciezca musi wyprzedzac drugie miejsce o [Level.ratio]. */
    private fun extreme(
        kind: String,
        prompt: String,
        metric: (CountryFact) -> Double,
        largest: Boolean,
        mediumUp: Boolean = false
    ): (Level, Random) -> QuizEntry? = builder@{ level, random ->
        if (mediumUp && !level.atLeastMedium) return@builder null
        val four = pool(level, random).pickOrNull(4, random) ?: return@builder null
        val sorted = if (largest) four.sortedByDescending(metric) else four.sortedBy(metric)
        val (best, second) = sorted[0] to sorted[1]
        val gap = if (largest) metric(best) / metric(second) else metric(second) / metric(best)
        if (gap < level.ratio) return@builder null
        choice(kind, codes(four), prompt, best.name, sorted.drop(1).map { it.name }, level)
    }

    /**
     * "Ktora z tych stolic lezy najdalej na polnoc/wschod" - z zachodu na wschod tylko w obrebie
     * 150 stopni i z dala od linii zmiany daty, bo inaczej "na wschod" nie ma jednej odpowiedzi.
     */
    private fun capitalExtreme(
        kind: String,
        prompt: String,
        coordinate: (CountryFact) -> Double,
        largest: Boolean,
        eastWest: Boolean
    ): (Level, Random) -> QuizEntry? = builder@{ level, random ->
        if (eastWest && !level.atLeastMedium) return@builder null
        val four = pool(level, random) { it.capitalQuiz && (!eastWest || abs(it.capitalLon) < 170) }
            .pickOrNull(4, random) ?: return@builder null
        if (eastWest && four.maxOf { it.capitalLon } - four.minOf { it.capitalLon } > MAX_LONGITUDE_SPAN) return@builder null
        val sorted = if (largest) four.sortedByDescending(coordinate) else four.sortedBy(coordinate)
        if (abs(coordinate(sorted[0]) - coordinate(sorted[1])) < level.degrees) return@builder null
        choice(kind, codes(four), prompt, sorted[0].capital, sorted.drop(1).map { it.capital }, level)
    }

    private val borders: (Level, Random) -> QuizEntry? = builder@{ level, random ->
        val country = pool(level, random) { it.neighbours.isNotEmpty() }.randomOrNull(random) ?: return@builder null
        val neighbour = neighboursOf(country, level).randomOrNull(random) ?: return@builder null
        val decoys = decoysOf(country, level, random).take(3).takeIf { it.size == 3 } ?: return@builder null
        choice(
            "borders", "${country.code}_${codes(decoys + neighbour)}",
            "Which of these countries borders ${country.inSentence}?",
            neighbour.name, decoys.map { it.name }, level
        )
    }

    private val notBorders: (Level, Random) -> QuizEntry? = builder@{ level, random ->
        val country = pool(level, random) { it.neighbours.size >= 3 }.randomOrNull(random) ?: return@builder null
        val three = neighboursOf(country, level).pickOrNull(3, random) ?: return@builder null
        val decoy = decoysOf(country, level, random).firstOrNull() ?: return@builder null
        choice(
            "notborders", "${country.code}_${codes(three + decoy)}",
            "Which of these countries does NOT border ${country.inSentence}?",
            decoy.name, three.map { it.name }, level
        )
    }

    /** Jedno panstwo bez morza wsrod trzech nadmorskich (najchetniej jego sasiadow) - albo odwrotnie. */
    private fun landlockedOdd(seaIsAnswer: Boolean): (Level, Random) -> QuizEntry? = builder@{ level, random ->
        if (seaIsAnswer && !level.atLeastMedium) return@builder null
        val anchor = pool(level, random) { it.landlocked }.randomOrNull(random) ?: return@builder null
        val nearby = countries.filter {
            it.continent == anchor.continent && it.code != anchor.code && it.fame <= level.fameCap
        }.shuffled(random).sortedByDescending { it.code in anchor.neighbours }
        val (answer, others) = if (seaIsAnswer) {
            val coastal = nearby.firstOrNull { !it.landlocked } ?: return@builder null
            val landlocked = (listOf(anchor) + nearby.filter { it.landlocked }).take(3).takeIf { it.size == 3 } ?: return@builder null
            coastal to landlocked
        } else {
            val coastal = nearby.filter { !it.landlocked }.take(3).takeIf { it.size == 3 } ?: return@builder null
            anchor to coastal
        }
        choice(
            if (seaIsAnswer) "coast" else "landlocked", codes(others + answer),
            if (seaIsAnswer) "Which of these countries is NOT landlocked?" else "Which of these countries is landlocked?",
            answer.name, others.map { it.name }, level
        )
    }

    /**
     * "Roughly how many people live in X?" - odpowiedzi co trzy razy, wiec zaokraglenie i kilka lat
     * zmian ludnosci nie zrobia z dystraktora poprawnej odpowiedzi. Poziom = slawa panstwa.
     */
    private val populationRoughly: (Level, Random) -> QuizEntry? = builder@{ level, random ->
        if (!level.atLeastMedium) return@builder null
        val fame = when (level.difficulty) {
            QuizDifficulty.Medium -> 1
            QuizDifficulty.Hard -> 2
            else -> 3
        }
        val country = countries.filter { it.fame == fame && it.population >= 10_000 }.randomOrNull(random) ?: return@builder null
        // Bez absurdow: zadne panstwo nie ma 13 miliardow ludzi ani 300 mieszkancow. Z dozwolonych
        // mnoznikow bierzemy cztery najblizsze jedynki, a z nich trzy.
        val factors = listOf(1.0 / 27, 1.0 / 9, 1.0 / 3, 3.0, 9.0, 27.0)
            .filter { country.population * it in MIN_ROUGH_PEOPLE..MAX_ROUGH_PEOPLE }
            .sortedBy { if (it < 1) 1 / it else it }
            .take(4).shuffled(random).take(3)
        if (factors.size < 3) return@builder null
        val wrong = factors.map { roughly(country.population * it) }
        val correct = roughly(country.population.toDouble())
        if ((wrong + correct).distinct().size != 4) return@builder null
        choice("people", country.code, "Roughly how many people live in ${country.inSentence}?", correct, wrong, level)
    }

    private val closestCapital: (Level, Random) -> QuizEntry? = builder@{ level, random ->
        if (!level.atLeastHard) return@builder null
        val candidates = pool(level, random) { it.capitalQuiz }
        val from = candidates.randomOrNull(random) ?: return@builder null
        val four = candidates.filter { it.code != from.code }.pickOrNull(4, random) ?: return@builder null
        val sorted = four.sortedBy { distanceKm(from, it) }
        if (distanceKm(from, sorted[1]) / distanceKm(from, sorted[0]) < level.ratio) return@builder null
        choice(
            "closest", "${from.code}_${codes(four)}",
            "Which of these capitals is closest to ${from.capital}?",
            sorted[0].capital, sorted.drop(1).map { it.capital }, level
        )
    }

    private val choiceBuilders: List<(Level, Random) -> QuizEntry?> = listOf(
        extreme("mostpeople", "Which of these countries has the largest population?", { it.population.toDouble() }, largest = true),
        extreme("fewestpeople", "Which of these countries has the smallest population?", { it.population.toDouble() }, largest = false),
        extreme("largest", "Which of these countries is the largest by area?", { it.areaKm2 }, largest = true),
        extreme("smallest", "Which of these countries is the smallest by area?", { it.areaKm2 }, largest = false),
        extreme("densest", "Which of these countries is the most densely populated?", { it.density }, largest = true, mediumUp = true),
        capitalExtreme("north", "Which of these capitals lies furthest north?", { it.capitalLat }, largest = true, eastWest = false),
        capitalExtreme("south", "Which of these capitals lies furthest south?", { it.capitalLat }, largest = false, eastWest = false),
        capitalExtreme("east", "Which of these capitals lies furthest east?", { it.capitalLon }, largest = true, eastWest = true),
        capitalExtreme("west", "Which of these capitals lies furthest west?", { it.capitalLon }, largest = false, eastWest = true),
        borders,
        borders,
        notBorders,
        landlockedOdd(seaIsAnswer = false),
        landlockedOdd(seaIsAnswer = true),
        populationRoughly,
        closestCapital
    )

    /* ------------------------------------------------------------------------------------------ */
    /* Ukladanki                                                                                   */

    private fun ordered(kind: String, prompt: String, items: List<CountryFact>, label: (CountryFact) -> String, level: Level) =
        OrderingEntry(
            id = "${ID_PREFIX}ord_${kind}_${codes(items)}",
            prompt = prompt,
            items = items.map(label),
            category = QuizCategory.Geography,
            difficulty = level.difficulty
        )

    /** Sasiednie pozycje musza sie roznic o [Level.ratio] - kazda para da sie rozstrzygnac osobno. */
    private fun byMetric(kind: String, prompt: String, metric: (CountryFact) -> Double): (Level, Random) -> OrderingEntry? =
        builder@{ level, random ->
            val four = pool(level, random).pickOrNull(4, random)?.sortedBy(metric) ?: return@builder null
            if (four.zipWithNext().any { (a, b) -> metric(b) / metric(a) < level.ratio }) return@builder null
            ordered(kind, prompt, four, { it.name }, level)
        }

    private fun byCoordinate(kind: String, prompt: String, eastWest: Boolean): (Level, Random) -> OrderingEntry? =
        builder@{ level, random ->
            val coordinate: (CountryFact) -> Double = if (eastWest) ({ it.capitalLon }) else ({ it.capitalLat })
            val four = pool(level, random) { it.capitalQuiz && (!eastWest || abs(it.capitalLon) < 170) }
                .pickOrNull(4, random)?.sortedBy(coordinate) ?: return@builder null
            if (eastWest && four.last().capitalLon - four.first().capitalLon > MAX_LONGITUDE_SPAN) return@builder null
            if (four.zipWithNext().any { (a, b) -> coordinate(b) - coordinate(a) < level.degrees }) return@builder null
            ordered(kind, prompt, four, { it.capital }, level)
        }

    private val orderingBuilders: List<(Level, Random) -> OrderingEntry?> = listOf(
        byMetric("people", "Order these countries by population, smallest first.") { it.population.toDouble() },
        byMetric("area", "Order these countries by area, smallest first.") { it.areaKm2 },
        byCoordinate("westeast", "Order these capitals from west to east.", eastWest = true),
        byCoordinate("southnorth", "Order these capitals from south to north.", eastWest = false)
    )

    /* ------------------------------------------------------------------------------------------ */
    /* Prawda / falsz                                                                              */

    private fun statement(kind: String, key: String, text: String, isTrue: Boolean, level: Level) =
        TrueFalseEntry(
            id = "${ID_PREFIX}tf_${kind}_$key",
            statement = text,
            isTrue = isTrue,
            category = QuizCategory.Geography,
            difficulty = level.difficulty
        )

    /** "Wyzej czy nizej": para z wyrazna roznica, a kolejnosc w zdaniu losowa - polowa zdan jest falszywa. */
    private fun compare(kind: String, metric: (CountryFact) -> Double, text: (CountryFact, CountryFact) -> String): (Level, Random) -> TrueFalseEntry? =
        builder@{ level, random ->
            val (a, b) = pool(level, random).pickOrNull(2, random) ?: return@builder null
            val ratio = maxOf(metric(a), metric(b)) / minOf(metric(a), metric(b))
            if (ratio < level.ratio) return@builder null
            statement(kind, "${a.code}_${b.code}", text(a, b), metric(a) > metric(b), level)
        }

    private fun capitalCompare(kind: String, eastWest: Boolean, text: (CountryFact, CountryFact) -> String): (Level, Random) -> TrueFalseEntry? =
        builder@{ level, random ->
            if (eastWest && !level.atLeastMedium) return@builder null
            val coordinate: (CountryFact) -> Double = if (eastWest) ({ it.capitalLon }) else ({ it.capitalLat })
            val (a, b) = pool(level, random) { it.capitalQuiz && (!eastWest || abs(it.capitalLon) < 170) }
                .pickOrNull(2, random) ?: return@builder null
            val gap = abs(coordinate(a) - coordinate(b))
            if (gap < level.degrees || (eastWest && gap > MAX_LONGITUDE_SPAN)) return@builder null
            statement(kind, "${a.code}_${b.code}", text(a, b), coordinate(a) > coordinate(b), level)
        }

    private val sharesBorder: (Level, Random) -> TrueFalseEntry? = builder@{ level, random ->
        val country = pool(level, random) { it.neighbours.isNotEmpty() }.randomOrNull(random) ?: return@builder null
        val isTrue = random.nextBoolean()
        val other = (if (isTrue) neighboursOf(country, level).randomOrNull(random) else decoysOf(country, level, random).firstOrNull())
            ?: return@builder null
        statement(
            "border", "${country.code}_${other.code}",
            "${country.name} shares a land border with ${other.inSentence}.", isTrue, level
        )
    }

    private val isLandlocked: (Level, Random) -> TrueFalseEntry? = builder@{ level, random ->
        val landlocked = random.nextBoolean()
        val country = pool(level, random) { it.landlocked == landlocked && it.continent != CountryFact.Continent.Oceania }
            .randomOrNull(random) ?: return@builder null
        statement("landlocked", country.code, "${country.name} is landlocked.", landlocked, level)
    }

    private val trueFalseBuilders: List<(Level, Random) -> TrueFalseEntry?> = listOf(
        compare("people", { it.population.toDouble() }) { a, b -> "${a.name} has more people than ${b.inSentence}." },
        compare("area", { it.areaKm2 }) { a, b -> "${a.name} is larger than ${b.inSentence} by area." },
        capitalCompare("north", eastWest = false) { a, b -> "${a.capital} lies further north than ${b.capital}." },
        capitalCompare("east", eastWest = true) { a, b -> "${a.capital} lies further east than ${b.capital}." },
        sharesBorder,
        isLandlocked
    )

    /* ------------------------------------------------------------------------------------------ */
    /* Zaznacz wszystkie                                                                           */

    private fun selection(kind: String, key: String, prompt: String, correct: List<CountryFact>, wrong: List<CountryFact>, level: Level) =
        MultiSelectEntry(
            id = "${ID_PREFIX}ms_${kind}_$key",
            prompt = prompt,
            correct = correct.map { it.name },
            wrong = wrong.map { it.name },
            category = QuizCategory.Geography,
            difficulty = level.difficulty
        )

    private val bordersAll: (Level, Random) -> MultiSelectEntry? = builder@{ level, random ->
        val country = pool(level, random) { it.neighbours.size >= 2 }.randomOrNull(random) ?: return@builder null
        val neighbours = neighboursOf(country, level).shuffled(random)
        if (neighbours.size < 2) return@builder null
        val correct = neighbours.take(2 + random.nextInt(minOf(3, neighbours.size - 1)))
        val wrong = decoysOf(country, level, random).take(3).takeIf { it.size == 3 } ?: return@builder null
        selection(
            "borders", "${country.code}_${codes(correct + wrong)}",
            "Which of these countries border ${country.inSentence}?", correct, wrong, level
        )
    }

    private val landlockedAll: (Level, Random) -> MultiSelectEntry? = builder@{ level, random ->
        val candidates = pool(level, random) { it.continent != CountryFact.Continent.Oceania }
        val landlocked = candidates.filter { it.landlocked }.shuffled(random).take(2 + random.nextInt(2))
        val coastal = candidates.filter { !it.landlocked }.shuffled(random).take(3)
        if (landlocked.size < 2 || coastal.size < 3) return@builder null
        selection(
            "landlocked", codes(landlocked + coastal),
            "Which of these countries are landlocked?", landlocked, coastal, level
        )
    }

    /** "Ktore maja wiecej ludzi niz X" - poprawne co najmniej [Level.ratio] razy wyzej, bledne tyle samo nizej. */
    private fun moreThan(kind: String, metric: (CountryFact) -> Double, prompt: (CountryFact) -> String): (Level, Random) -> MultiSelectEntry? =
        builder@{ level, random ->
            val candidates = pool(level, random)
            val pivot = candidates.randomOrNull(random) ?: return@builder null
            val above = candidates.filter { metric(it) >= metric(pivot) * level.ratio }.shuffled(random).take(2 + random.nextInt(2))
            val below = candidates.filter { metric(it) * level.ratio <= metric(pivot) }.shuffled(random).take(3)
            if (above.size < 2 || below.size < 3) return@builder null
            selection(kind, "${pivot.code}_${codes(above + below)}", prompt(pivot), above, below, level)
        }

    private val multiSelectBuilders: List<(Level, Random) -> MultiSelectEntry?> = listOf(
        bordersAll,
        landlockedAll,
        moreThan("people", { it.population.toDouble() }) { "Which of these countries have more people than ${it.inSentence}?" },
        moreThan("area", { it.areaKm2 }) { "Which of these countries are larger than ${it.inSentence} by area?" }
    )

    /* ------------------------------------------------------------------------------------------ */
    /* Liczby i odleglosci                                                                         */

    /** Najwieksza rozpietosc dlugosci geograficznych przy pytaniach "na wschod/zachod". */
    private const val MAX_LONGITUDE_SPAN = 150.0

    /** Sasiad sasiada dalej niz tyle od stolicy pytanego panstwa nie jest wiarygodnym dystraktorem. */
    private const val NEAR_DECOY_KM = 3000.0

    /** Gdy sasiadow sasiadow brakuje, dystraktory ida z tylu najblizszych panstw kontynentu. */
    private const val NEAREST_REST = 8

    private const val MIN_ROUGH_PEOPLE = 1_000.0
    private const val MAX_ROUGH_PEOPLE = 2_500_000_000.0

    private const val EARTH_RADIUS_KM = 6371.0

    /** Odleglosc miedzy stolicami po powierzchni Ziemi (wzor haversine). */
    fun distanceKm(a: CountryFact, b: CountryFact): Double {
        val toRad = kotlin.math.PI / 180
        val dLat = (b.capitalLat - a.capitalLat) * toRad
        val dLon = (b.capitalLon - a.capitalLon) * toRad
        val h = sin(dLat / 2).pow(2) + cos(a.capitalLat * toRad) * cos(b.capitalLat * toRad) * sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS_KM * asin(sqrt(h))
    }

    /** "About 38 million", "About 1.4 billion", "About 39,000" - dwie cyfry znaczace. */
    fun roughly(people: Double): String {
        val rounded = twoSignificant(people)
        return when {
            rounded >= 1e9 -> "About ${decimal(rounded / 1e9)} billion"
            rounded >= 1e6 -> "About ${decimal(rounded / 1e6)} million"
            else -> "About ${grouped(rounded.roundToLong())}"
        }
    }

    private fun twoSignificant(value: Double): Double {
        var scale = 1.0
        while (value / scale >= 100) scale *= 10
        return (value / scale).roundToLong() * scale
    }

    /** 38.0 -> "38", 1.4 -> "1.4" (tylko jedno miejsce po przecinku - po dwoch cyfrach znaczacych wiecej nie ma). */
    private fun decimal(value: Double): String {
        val tenths = (value * 10).roundToLong()
        return if (tenths % 10 == 0L) (tenths / 10).toString() else "${tenths / 10}.${tenths % 10}"
    }

    private fun grouped(value: Long): String = value.toString().reversed().chunked(3).joinToString(",").reversed()
}
