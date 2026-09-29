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
 *
 * [localeCode] oznacza temat LOKALNY - sensowny tylko dla gracza w danym jezyku. Gra jest wydana
 * globalnie, a pytanie "w ktorym roku wybuchlo powstanie styczniowe?" dla gracza z Teksasu nie jest
 * trudne, tylko niemozliwe i nudne. Zamiast rozpuszczac takie pytania po History/Literature
 * (gdzie kazdy je dostawal), maja wlasna kategorie, ktora pokazuje sie wylacznie przy pasujacym
 * jezyku aplikacji - patrz [builtinFor].
 */
enum class QuizCategory(
    val label: String,
    /** Emoji tematu na kaflach wyboru - placeholder pod przyszla grafike, tak jak [com.lernhero.domain.monster.Element.icon]. */
    val icon: String = "📖",
    val inBuiltinBank: Boolean = true,
    val localeCode: String? = null
) {
    Biology("Biology", icon = "🧬"),
    Animals("Animals", icon = "🦊"),
    History("History", icon = "🏛️"),
    Elements("Elements", icon = "⚗️"),
    Capitals("Capitals", icon = "🏙️"),
    Science("Science", icon = "🔬"),
    Flags("Flags", icon = "🚩"),
    Geography("Geography", icon = "🗺️"),
    Sport("Sport", icon = "⚽"),
    Math("Math", icon = "➗"),
    Literature("Literature", icon = "📚"),
    Technology("Technology", icon = "💻"),
    Mythology("Mythology", icon = "🐉"),

    /** Film, muzyka, seriale, gry wideo, komiksy i zabawki - patrz [com.lernhero.domain.quiz.bank.PopCultureQuestions]. */
    PopCulture("Pop culture", icon = "🎬"),

    /** Temat lokalny: historia, kultura i geografia Polski. Widoczny tylko przy jezyku polskim. */
    Poland("Poland", icon = "🇵🇱", localeCode = "pl"),

    /** Slowka zapisane w czytniku - budowane w locie z fiszek, patrz [WordQuizBank]. */
    Vocabulary("Words", icon = "🗂️", inBuiltinBank = false),

    /** Pytania z wlasnej talii gracza - patrz [com.lernhero.domain.quiz.custom.CustomDeck]. */
    Custom("Custom", icon = "📝", inBuiltinBank = false);

    /** Czy ten temat wolno pokazac graczowi grajacemu w jezyku [languageCode]. */
    fun availableIn(languageCode: String): Boolean =
        localeCode == null || localeCode == languageCode

    companion object {
        /**
         * Tematy, ktore realnie maja pytania w [QuizBank] - LACZNIE z lokalnymi. To jest widok
         * "co jest w banku" (uzywaja go testy pokrycia), a nie "co pokazac graczowi" - do tego
         * sluzy [builtinFor].
         */
        val builtin: List<QuizCategory> get() = entries.filter { it.inBuiltinBank }

        /**
         * Tematy do pokazania graczowi w danym jezyku: bank minus kategorie lokalne innych jezykow.
         */
        fun builtinFor(languageCode: String): List<QuizCategory> =
            builtin.filter { it.availableIn(languageCode) }
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

/**
 * Pytanie "prawda czy falsz": jedno stwierdzenie i czy jest prawdziwe. Najlepiej dzialaja tu
 * popularne MITY ("nietoperze sa slepe") i fakty, ktore brzmia jak mit ("banany to jagody") -
 * wtedy odpowiedz wymaga wiedzy, a nie tylko rzutu moneta.
 */
data class TrueFalseEntry(
    val id: String,
    val statement: String,
    val isTrue: Boolean,
    val category: QuizCategory,
    val difficulty: QuizDifficulty
)
