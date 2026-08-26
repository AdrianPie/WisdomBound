package com.lernhero.reader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import com.lernhero.domain.reading.Book
import com.lernhero.domain.reading.TextStats
import com.lernhero.domain.repository.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReaderUiState(
    val loading: Boolean = true,
    val pageIndex: Int = 0,
    val pageCount: Int = 0,
    val pageImage: ImageBitmap? = null,
    /** Brak interakcji przez zbyt dlugo - zegar czytania stoi, dopoki gracz nie przewroci strony. */
    val paused: Boolean = false,
    val sessionSeconds: Int = 0,
    val sessionPages: Int = 0,
    val sessionWords: Int = 0,
    val error: String? = null
)

/**
 * Jedna sesja czytania jednej ksiazki. To nie jest ViewModel - ksiazka i jej bajty sa znane
 * dopiero w miejscu uzycia (ekran biblioteki), wiec sesja zyje w `remember` na czas otwarcia
 * czytnika i konczy sie wraz z ekranem.
 *
 * AFK-guard: kazde przewrocenie strony "doladowuje" budzet bezczynnosci do [IDLE_BUDGET_SECONDS].
 * Zegar aktywnego czytania tyka tylko, dopoki budzet > 0 - zostawienie apki otwartej na jednej
 * stronie zatrzymuje odliczanie po najwyzej [IDLE_BUDGET_SECONDS] sekundach, wiec 15 minut
 * "czytania" bez przewracania kartek nie da nagrody.
 */
class ReaderSession(
    private val book: Book,
    private val bytes: ByteArray,
    startPage: Int,
    private val playerRepository: PlayerRepository,
    private val scope: CoroutineScope
) {
    var uiState by mutableStateOf(ReaderUiState())
        private set

    private var document: PdfDocumentHandle? = null
    private var idleBudgetSeconds = IDLE_BUDGET_SECONDS
    private var secondsTowardNextMinute = 0

    private var pendingMinutes = 0
    private var pendingPages = 0
    private var pendingWords = 0
    private var pendingCharacters = 0

    init {
        scope.launch(Dispatchers.Default) {
            val doc = PdfEngine.open(bytes)
            if (doc == null) {
                uiState = uiState.copy(loading = false, error = "Nie udalo sie otworzyc pliku.")
                return@launch
            }
            document = doc
            val count = PdfEngine.pageCount(doc)
            uiState = uiState.copy(pageCount = count)
            goToPage(startPage.coerceIn(0, (count - 1).coerceAtLeast(0)))
            tick()
        }
    }

    private fun tick() {
        scope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000)
                if (idleBudgetSeconds > 0) {
                    idleBudgetSeconds -= 1
                    secondsTowardNextMinute += 1
                    uiState = uiState.copy(paused = false, sessionSeconds = uiState.sessionSeconds + 1)
                    if (secondsTowardNextMinute >= 60) {
                        secondsTowardNextMinute -= 60
                        pendingMinutes += 1
                    }
                } else {
                    uiState = uiState.copy(paused = true)
                }
                // Regularny odplyw do konta - nawet gdy uzytkownik zamknie apke bez pozegnania,
                // traci co najwyzej kilkanascie sekund czytania, nie caly postep.
                if (pendingMinutes > 0) flushProgress()
            }
        }
    }

    fun onInteraction() {
        idleBudgetSeconds = IDLE_BUDGET_SECONDS
    }

    fun nextPage() = goToPage(uiState.pageIndex + 1)
    fun previousPage() = goToPage(uiState.pageIndex - 1)

    fun goToPage(index: Int) {
        val doc = document ?: return
        val clamped = index.coerceIn(0, (uiState.pageCount - 1).coerceAtLeast(0))
        onInteraction()

        scope.launch(Dispatchers.Default) {
            val bitmap = PdfEngine.renderPage(doc, clamped, PAGE_RENDER_WIDTH_PX)
            val text = PdfEngine.pageText(doc, clamped)
            val isNewPage = clamped != uiState.pageIndex || uiState.pageImage == null

            if (isNewPage && clamped > uiState.pageIndex) {
                // Liczymy tylko postep do przodu - cofanie sie po ksiazce nie ma zdublowac stron.
                val words = text?.let(TextStats::countWords) ?: 0
                val characters = text?.let(TextStats::countCharacters) ?: 0
                pendingPages += 1
                pendingWords += words
                pendingCharacters += characters
                uiState = uiState.copy(
                    sessionPages = uiState.sessionPages + 1,
                    sessionWords = uiState.sessionWords + words
                )
            }

            uiState = uiState.copy(loading = false, pageIndex = clamped, pageImage = bitmap)
            playerRepository.updateBookProgress(book.id, clamped)
        }
    }

    private suspend fun flushProgress() {
        val minutes = pendingMinutes
        val pages = pendingPages
        val words = pendingWords
        val characters = pendingCharacters
        if (minutes == 0 && pages == 0 && words == 0 && characters == 0) return

        pendingMinutes = 0
        pendingPages = 0
        pendingWords = 0
        pendingCharacters = 0

        withContext(Dispatchers.IO) {
            playerRepository.recordReading(
                dateKey = todayDateKey(),
                minutesDelta = minutes,
                pagesDelta = pages,
                wordsDelta = words,
                charactersDelta = characters
            )
        }
    }

    /**
     * Zamyka dokument i wysyla ostatnia porcje postepu - wywolaj przy opuszczaniu czytnika.
     *
     * Celowo NIE uzywa [scope] (rememberCoroutineScope z ekranu, ktory wlasnie znika) - w
     * praktyce ten scope bywa juz anulowany, zanim `DisposableEffect.onDispose` zdazy odpalic
     * ten kod (znany klopot z Compose), przez co ostatni zapis nigdy sie nie wykonywal i
     * statystyki z konca sesji przepadaly. GlobalScope przezyje zamkniecie ekranu.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun close() {
        document?.let { PdfEngine.close(it) }
        val minutes = pendingMinutes
        val pages = pendingPages
        val words = pendingWords
        val characters = pendingCharacters
        if (minutes == 0 && pages == 0 && words == 0 && characters == 0) return
        pendingMinutes = 0
        pendingPages = 0
        pendingWords = 0
        pendingCharacters = 0

        GlobalScope.launch(Dispatchers.IO) {
            playerRepository.recordReading(
                dateKey = todayDateKey(),
                minutesDelta = minutes,
                pagesDelta = pages,
                wordsDelta = words,
                charactersDelta = characters
            )
        }
    }

    companion object {
        /** Ile sekund bezczynnosci toleruje zegar, zanim wstrzyma odliczanie. */
        private const val IDLE_BUDGET_SECONDS = 90
        private const val PAGE_RENDER_WIDTH_PX = 1000
    }
}
