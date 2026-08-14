package com.lernhero.home.fitness

import androidx.compose.runtime.Composable
import com.lernhero.domain.fitness.FitnessAvailability
import com.lernhero.domain.fitness.FitnessSnapshot

/**
 * Zrodlo danych o aktywnosci - Health Connect na Androidzie, HealthKit na iOS. Kazda platforma
 * skleja to z wlasnego API; tutaj siedzi tylko kontrakt, ktorego ekran naprawde potrzebuje.
 */
expect object FitnessTracker {
    /**
     * Czy platforma w ogole potrafi dostarczyc te dane - i jesli nie, dlaczego. Roznicujemy
     * "nie ma Health Connect" od "jest, ale za stary", bo kazde ma inna akcje naprawcza.
     */
    suspend fun checkAvailability(): FitnessAvailability

    /** Czy mamy juz zgode na odczyt krokow/treningow. */
    suspend fun hasPermission(): Boolean

    /** Kroki, aktywne minuty i treningi od poczatku dzisiejszego dnia. */
    suspend fun today(): FitnessSnapshot

    /** Otwiera sklep z aplikacja Health Connect (instalacja/aktualizacja). Na iOS/desktop - nic. */
    fun openInstallPage()
}

/**
 * Przycisk "Polacz" potrzebuje launchera przywiazanego do ekranu (Health Connect na Androidzie
 * dziala przez ActivityResultContract), wiec prosba o zgode jest kompozowalna - tak samo jak
 * [com.lernhero.game.FightScreenOrientation] zaleznie od platformy siega po inne API systemowe.
 *
 * Zwraca funkcje, ktora na kliknieciu odpala prosbe o zgode; [onResult] dostaje wynik.
 */
@Composable
expect fun rememberFitnessPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit
