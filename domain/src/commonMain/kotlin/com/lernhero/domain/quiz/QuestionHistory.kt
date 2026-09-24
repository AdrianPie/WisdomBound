package com.lernhero.domain.quiz

/**
 * Pamiec ostatnio zadanych pytan - wspolna dla WSZYSTKICH typow (wybor, prawda/falsz, ukladanka,
 * zaznacz-wszystkie). Bez niej kazde losowanie bylo niezalezne i przy temacie z kilkoma
 * pytaniami danego typu to samo pytanie wracalo po dwoch-trzech falach.
 *
 * Zyje w [com.lernhero.domain.run.RunSession], czyli przez cale uruchomienie gry, a nie jedna
 * walke - druga plansza pod rzad tez nie ma zaczynac od tych samych pytan. Pojemnosc jest
 * skonczona, zeby po wyczerpaniu tematu stare pytania mogly kiedys wrocic.
 */
class QuestionHistory(private val capacity: Int = DEFAULT_CAPACITY) {

    private val order = ArrayDeque<String>()
    private val ids = HashSet<String>()

    operator fun contains(id: String): Boolean = id in ids

    fun remember(id: String) {
        if (!ids.add(id)) order.remove(id)
        order.addLast(id)
        while (order.size > capacity) ids.remove(order.removeFirst())
    }

    companion object {
        const val DEFAULT_CAPACITY = 600
    }
}

/**
 * Wspolna regula dla bankow "specjalnych" typow (ukladanka, zaznacz-wszystkie, prawda/falsz):
 * tylko pytania jeszcze nie zadane, z tematu (gdy jest) i najwyzej o szczebel od zadanego poziomu.
 * Brak takiego = null, a walka bierze wtedy zwykle pytanie z wyborem - lepsze niz powtorka.
 */
internal fun <T> List<T>.drawFresh(
    difficulty: QuizDifficulty,
    category: QuizCategory?,
    random: kotlin.random.Random,
    seen: (String) -> Boolean,
    idOf: (T) -> String,
    categoryOf: (T) -> QuizCategory,
    difficultyOf: (T) -> QuizDifficulty
): T? {
    val fresh = filter { !seen(idOf(it)) && (category == null || categoryOf(it) == category) }
    val exact = fresh.filter { difficultyOf(it) == difficulty }
    if (exact.isNotEmpty()) return exact.random(random)
    val near = fresh.filter { kotlin.math.abs(difficultyOf(it).ordinal - difficulty.ordinal) == 1 }
    return near.randomOrNull(random)
}
