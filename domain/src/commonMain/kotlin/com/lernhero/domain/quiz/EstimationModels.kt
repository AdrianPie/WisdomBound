package com.lernhero.domain.quiz

/**
 * Pytanie "oszacuj wartosc" - na razie tylko warianty "zgadnij rok" (patrz [EstimationQuestions]),
 * ale [minValue]/[maxValue]/[unit] sa juz na tyle ogolne, zeby pomiescic inne skale liczbowe pozniej.
 * Uzywane wylacznie przez specjalne zdarzenia elit/bossow w walce - to nie jest zwykly quiz tury.
 */
data class EstimationEntry(
    val id: String,
    val prompt: String,
    val minValue: Int,
    val maxValue: Int,
    val correctValue: Int,
    val unit: String
)
