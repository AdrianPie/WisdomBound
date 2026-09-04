package com.lernhero.domain.quiz

import kotlinx.serialization.Serializable

/**
 * Cztery progi trudnosci - jak drabinka w "Milionerach". Kazdy niesie swoja nagrode i czas
 * na odpowiedz, bo pytanie o stolice Bhutanu nie moze miec tyle sekund co pytanie o stolice Polski.
 *
 * @Serializable - ten enum leci przez sieciowe DTO generowania AI (patrz quiz/ai), nie tylko
 * w pamieci aplikacji.
 */
@Serializable
enum class QuizDifficulty(
    val label: String,
    val reward: Int,
    val seconds: Int
) {
    Easy("Easy", reward = 60, seconds = 10),
    Medium("Medium", reward = 100, seconds = 13),
    Hard("Hard", reward = 160, seconds = 16),
    Expert("Expert", reward = 240, seconds = 20)
}

/**
 * Temat pytania. Dwa ostatnie wpisy NIE MAJA pokrycia w [QuizBank] - powstaja z materialu gracza
 * (fiszki z czytelni, wlasna talia), wiec wszedzie, gdzie wybiera sie temat z wbudowanego banku,
 * chodzi o [QuizCategory.builtin], a nie o cale `entries`.
 */
enum class QuizCategory(val label: String, val inBuiltinBank: Boolean = true) {
    Biology("Biology"),
    Animals("Animals"),
    History("History"),
    Elements("Elements"),
    Capitals("Capitals"),
    Science("Science"),
    Flags("Flags"),
    Geography("Geography"),
    Sport("Sport"),
    Math("Math"),
    Literature("Literature"),
    Technology("Technology"),
    Mythology("Mythology"),
    /** Slowka zapisane w czytniku - budowane w locie z fiszek, patrz [WordQuizBank]. */
    Vocabulary("Words", inBuiltinBank = false),

    /** Pytania z wlasnej talii gracza - patrz [com.lernhero.domain.quiz.custom.CustomDeck]. */
    Custom("Custom", inBuiltinBank = false);

    companion object {
        /** Tematy, ktore realnie maja pytania w [QuizBank] - jedyne, ktore wolno pokazac do wyboru. */
        val builtin: List<QuizCategory> get() = entries.filter { it.inBuiltinBank }
    }
}

/**
 * Pytanie w postaci czysto danych: tresc, jedna poprawna odpowiedz i trzy bledne.
 * Kolejnosc odpowiedzi ustala sie dopiero przy losowaniu, wiec bank nie zdradza, ktora jest ktora.
 *
 * [flagCode] to dwuliterowy kod kraju. Domena nie zna obrazkow - warstwa UI zamienia kod na flage.
 */
data class QuizEntry(
    val id: String,
    val prompt: String,
    val correct: String,
    val wrong: List<String>,
    val category: QuizCategory,
    val difficulty: QuizDifficulty,
    val flagCode: String? = null
) {
    val answers: List<String> get() = listOf(correct) + wrong
}

/**
 * Pytanie typu "ulóz w kolejnosci": gracz nie wybiera jednej poprawnej odpowiedzi, tylko ustawia
 * wszystkie [items] we wlasciwym porzadku (chronologicznie, od najmniejszego do najwiekszego, itp.).
 * [items] jest juz zapisane w poprawnej kolejnosci - warstwa UI tasuje je do wyswietlenia.
 */
/**
 * Pytanie "zaznacz wszystkie prawdziwe": gracz dostaje liste i musi wybrac KAZDA pasujaca pozycje,
 * nie tylko jedna. To inny rodzaj wiedzy niz pytanie zamkniete - nie wystarczy rozpoznac jednej
 * odpowiedzi, trzeba wiedziec o calym zbiorze (ktore z tych win sa czerwone, ktore z tych panstw
 * leza w Afryce).
 *
 * [correct] i [wrong] trzymamy osobno, a warstwa UI je miesza i tasuje - bank nie zdradza,
 * ktora jest ktora, i to samo pytanie za kazdym razem wyglada inaczej.
 */
data class MultiSelectEntry(
    val id: String,
    val prompt: String,
    val correct: List<String>,
    val wrong: List<String>,
    val category: QuizCategory,
    val difficulty: QuizDifficulty
) {
    val options: List<String> get() = correct + wrong
}

data class OrderingEntry(
    val id: String,
    val prompt: String,
    val items: List<String>,
    val category: QuizCategory,
    val difficulty: QuizDifficulty
)
