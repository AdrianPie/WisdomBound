package com.lernhero.domain.quiz

/**
 * Cztery progi trudnosci - jak drabinka w "Milionerach". Kazdy niesie swoja nagrode i czas
 * na odpowiedz, bo pytanie o stolice Bhutanu nie moze miec tyle sekund co pytanie o stolice Polski.
 */
enum class QuizDifficulty(
    val label: String,
    val reward: Int,
    val seconds: Int
) {
    Easy("Latwe", reward = 60, seconds = 10),
    Medium("Srednie", reward = 100, seconds = 13),
    Hard("Trudne", reward = 160, seconds = 16),
    Expert("Eksperckie", reward = 240, seconds = 20)
}

enum class QuizCategory(val label: String) {
    Biology("Biologia"),
    Animals("Zwierzeta"),
    History("Historia"),
    Elements("Pierwiastki"),
    Capitals("Stolice"),
    Science("Nauka"),
    Flags("Flagi")
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
