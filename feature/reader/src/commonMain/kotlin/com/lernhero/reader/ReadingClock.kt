package com.lernhero.reader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lernhero.domain.repository.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReadingClockState(
    /** Brak interakcji przez zbyt dlugo - zegar stoi, dopoki gracz nie przewroci strony. */
    val paused: Boolean = false,
    val sessionSeconds: Int = 0,
    val sessionPages: Int = 0,
    val sessionWords: Int = 0
) {
    /**
     * Tempo biezacej sesji w slowach na minute.
     *
     * WPM, a nie strony na minute - strona zalezy od rozmiaru czcionki i wielkosci ekranu, wiec
     * "stron/min" nie da sie porownac ani miedzy uzytkownikami, ani nawet miedzy dwoma
     * ustawieniami tego samego czytnika. Slowo jest jednostka stala.
     *
     * Zaokraglamy dopiero po przekroczeniu [MIN_SECONDS_FOR_WPM] - w pierwszych sekundach
     * sesji dzielenie przez ulamek minuty daje absurdalne tysiace.
     */
    val wpm: Int
        get() = if (sessionSeconds < MIN_SECONDS_FOR_WPM) 0
        else (sessionWords * 60.0 / sessionSeconds).toInt()

    private companion object {
        const val MIN_SECONDS_FOR_WPM = 30
    }
}

/**
 * Zegar aktywnego czytania - wspolny dla trybu reflow i dla podgladu oryginalu.
 *
 * AFK-guard: kazde przewrocenie strony "doladowuje" budzet bezczynnosci do [IDLE_BUDGET_SECONDS].
 * Zegar tyka tylko, dopoki budzet > 0 - zostawienie apki otwartej na jednej stronie zatrzymuje
 * odliczanie, wiec 15 minut "czytania" bez kartkowania nie zaliczy dziennego questa.
 */
class ReadingClock(
    private val playerRepository: PlayerRepository,
    scope: CoroutineScope
) {
    var state by mutableStateOf(ReadingClockState())
        private set

    private var idleBudgetSeconds = IDLE_BUDGET_SECONDS
    private var secondsTowardNextMinute = 0

    private var pendingMinutes = 0
    private var pendingPages = 0
    private var pendingWords = 0
    private var pendingCharacters = 0

    init {
        scope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000)
                if (idleBudgetSeconds > 0) {
                    idleBudgetSeconds -= 1
                    secondsTowardNextMinute += 1
                    state = state.copy(paused = false, sessionSeconds = state.sessionSeconds + 1)
                    if (secondsTowardNextMinute >= 60) {
                        secondsTowardNextMinute -= 60
                        pendingMinutes += 1
                    }
                } else {
                    state = state.copy(paused = true)
                }
                // Regularny odplyw do konta - nawet gdy gracz zabije apke bez pozegnania,
                // traci co najwyzej kilkanascie sekund, nie caly postep sesji.
                if (pendingMinutes > 0) flush()
            }
        }
    }

    fun onInteraction() {
        idleBudgetSeconds = IDLE_BUDGET_SECONDS
    }

    /**
     * Gracz przeszedl na nowa strone do przodu. Cofanie sie po ksiazce NIE wola tej metody -
     * inaczej kartkowanie w tam i z powrotem nabijaloby slowa i statystyki bylyby fikcja.
     */
    fun onPageAdvanced(words: Int, characters: Int = 0) {
        onInteraction()
        pendingPages += 1
        pendingWords += words
        pendingCharacters += characters
        state = state.copy(
            sessionPages = state.sessionPages + 1,
            sessionWords = state.sessionWords + words
        )
    }

    private suspend fun flush() {
        val payload = takePending() ?: return
        withContext(Dispatchers.IO) { payload.send() }
    }

    /**
     * Wysyla ostatnia porcje postepu - wywolaj przy opuszczaniu czytnika.
     *
     * Celowo NIE uzywa scope'u ekranu: w praktyce bywa on juz anulowany, zanim
     * `DisposableEffect.onDispose` zdazy odpalic ten kod, przez co ostatni zapis nigdy sie nie
     * wykonywal i statystyki z konca sesji przepadaly. GlobalScope przezyje zamkniecie ekranu.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun close() {
        val payload = takePending() ?: return
        GlobalScope.launch(Dispatchers.IO) { payload.send() }
    }

    private fun takePending(): Pending? {
        if (pendingMinutes == 0 && pendingPages == 0 && pendingWords == 0 && pendingCharacters == 0) {
            return null
        }
        val payload = Pending(pendingMinutes, pendingPages, pendingWords, pendingCharacters)
        pendingMinutes = 0
        pendingPages = 0
        pendingWords = 0
        pendingCharacters = 0
        return payload
    }

    private inner class Pending(
        val minutes: Int,
        val pages: Int,
        val words: Int,
        val characters: Int
    ) {
        suspend fun send() {
            playerRepository.recordReading(
                dateKey = todayDateKey(),
                minutesDelta = minutes,
                pagesDelta = pages,
                wordsDelta = words,
                charactersDelta = characters
            )
        }
    }

    private companion object {
        /** Ile sekund bezczynnosci toleruje zegar, zanim wstrzyma odliczanie. */
        const val IDLE_BUDGET_SECONDS = 90
    }
}
