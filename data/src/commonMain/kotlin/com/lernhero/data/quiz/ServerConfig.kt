package com.lernhero.data.quiz

/**
 * Adres wlasnego serwisu `:server` (Ktor na Cloud Run) - jedyne, co trzeba zmienic po wdrozeniu
 * pierwszej wersji na Cloud Run. Puste = [RemoteQuizGenerator.isAvailable] wraca `false` i UI
 * zachowuje sie tak, jakby dalej siedzial [com.lernhero.domain.quiz.ai.QuizGenerator.Stub] -
 * apka dziala bez serwera, dopoki ktos nie wklei tutaj prawdziwy URL.
 */
object ServerConfig {
    const val AI_QUIZ_SERVER_BASE_URL: String = "https://lernhero-server-792351484406.europe-west1.run.app"
}
