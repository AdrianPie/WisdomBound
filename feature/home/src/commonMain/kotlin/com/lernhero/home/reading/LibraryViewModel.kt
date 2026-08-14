package com.lernhero.home.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.reading.Book
import com.lernhero.domain.reading.BookFormat
import com.lernhero.domain.reading.BookSource
import com.lernhero.domain.reading.BundledBooks
import com.lernhero.domain.reading.ReadingDayProgress
import com.lernhero.domain.reading.ReadingGoal
import com.lernhero.domain.reading.ReadingStats
import com.lernhero.domain.repository.PlayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.resources.readFileBytes

data class LibraryUiState(
    val loading: Boolean = true,
    val bundledBooks: List<Book> = BundledBooks.ALL,
    val importedBooks: List<Book> = emptyList(),
    val readingStats: ReadingStats = ReadingStats(),
    val readingToday: ReadingDayProgress = ReadingDayProgress(),
    val bookProgress: Map<String, Int> = emptyMap(),
    val dailyGoalClaims: Map<String, String> = emptyMap(),
    val message: String? = null
) {
    fun claimedToday(goal: ReadingGoal): Boolean = dailyGoalClaims[goal.id] == readingToday.dateKey
    fun pageFor(book: Book): Int = bookProgress[book.id] ?: 0
}

/**
 * Biblioteka: ksiazki wbudowane + wgrane przez gracza, statystyki czytania i cele dzienne.
 * Otwieranie ksiazki (zaladowanie bajtow) siedzi tutaj tez, bo i bundlowane, i importowane
 * pliki koncza sie tym samym - tablica bajtow dla [PdfEngine].
 */
class LibraryViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val player = playerRepository.getPlayer()
            val today = todayDateKey()
            _uiState.update { state ->
                state.copy(
                    loading = false,
                    importedBooks = player?.importedBooks ?: state.importedBooks,
                    readingStats = player?.readingStats ?: state.readingStats,
                    readingToday = (player?.readingToday ?: state.readingToday).forToday(today),
                    bookProgress = player?.bookProgress ?: state.bookProgress,
                    dailyGoalClaims = player?.dailyGoalClaims ?: state.dailyGoalClaims
                )
            }
        }
    }

    /** Dopisuje wybrany plik do biblioteki i zwraca gotowa [Book] do natychmiastowego otwarcia. */
    fun onBookImported(picked: PickedFile): Book {
        val book = Book(
            id = "imported_${picked.persistentLocator.hashCode()}",
            title = picked.name.ifBlank { "Ksiazka" },
            author = "Zaimportowano",
            format = BookFormat.Pdf,
            source = BookSource.Imported,
            locator = picked.persistentLocator
        )
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.addImportedBook(book)
            refresh()
        }
        return book
    }

    /** Bajty ksiazki gotowe do otwarcia w czytniku - bundlowana z zasobow, wgrana z platformy. */
    suspend fun loadBytes(book: Book): ByteArray? = when (book.source) {
        BookSource.Bundled -> runCatching { Resources.readFileBytes(book.locator) }.getOrNull()
        BookSource.Imported -> loadImportedBookBytes(book.locator)
    }

    fun claim(goal: ReadingGoal) {
        val state = _uiState.value
        if (state.claimedToday(goal) || !goal.isComplete(state.readingToday)) return

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.claimDailyGoalReward(
                goalId = goal.id,
                dateKey = state.readingToday.dateKey,
                reward = goal.reward
            )
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Nie udalo sie odebrac nagrody - sprobuj ponownie.")
                } else {
                    current.copy(
                        dailyGoalClaims = updated.dailyGoalClaims,
                        message = "Odebrano +${goal.reward} zl: ${goal.title}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
