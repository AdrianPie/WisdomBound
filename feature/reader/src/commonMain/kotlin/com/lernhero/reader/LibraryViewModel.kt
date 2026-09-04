package com.lernhero.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.reading.Book
import com.lernhero.domain.reading.BookContent
import com.lernhero.domain.reading.BookFormat
import com.lernhero.domain.reading.BookSource
import com.lernhero.domain.reading.BundledBooks
import com.lernhero.domain.reading.PdfTextNormalizer
import com.lernhero.domain.reading.ReaderSettings
import com.lernhero.domain.reading.SavedWord
import com.lernhero.domain.reading.PlainTextParser
import com.lernhero.domain.reading.ReadingDayProgress
import com.lernhero.domain.reading.ReadingGoal
import com.lernhero.domain.reading.ReadingStats
import com.lernhero.domain.reading.VocabularyDeck
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
    /** Typografia czytnika zapisana na koncie - zeby nie wracala do domyslnej przy kazdej ksiazce. */
    val readerSettings: ReaderSettings = ReaderSettings(),
    /** Fiszki zapisane w czytniku - najnowsze pierwsze. */
    val savedWords: List<SavedWord> = emptyList(),
    /** Id ostatnio otwartej ksiazki - karta "Czytaj dalej" na gorze biblioteki. */
    val lastOpenedBookId: String? = null,
    val message: String? = null
) {
    fun claimedToday(goal: ReadingGoal): Boolean = dailyGoalClaims[goal.id] == readingToday.dateKey
    fun pageFor(book: Book): Int = bookProgress[book.id] ?: 0

    /** Wszystkie ksiazki na polce - wbudowane i wgrane razem, bo dla gracza to jedna biblioteka. */
    val allBooks: List<Book> get() = importedBooks + bundledBooks

    /**
     * Ksiazka do karty "Czytaj dalej": ostatnio otwarta, a gdy jeszcze zadnej nie bylo -
     * ta z najdalszym postepem. Null tylko przy zupelnie pustej polce.
     */
    val continueBook: Book?
        get() = lastOpenedBookId?.let { id -> allBooks.firstOrNull { it.id == id } }
            ?: allBooks.filter { (bookProgress[it.id] ?: 0) > 0 }.maxByOrNull { bookProgress[it.id] ?: 0 }

    /** Ile fiszek czeka na nauke, a ile juz opanowanych - licznik na kafelku "Slowka". */
    val wordsLearning: Int get() = VocabularyDeck.learning(savedWords).size
    val wordsMastered: Int get() = VocabularyDeck.mastered(savedWords).size
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
                    dailyGoalClaims = player?.dailyGoalClaims ?: state.dailyGoalClaims,
                    readerSettings = player?.readerSettings ?: state.readerSettings,
                    savedWords = (player?.savedWords ?: state.savedWords).sortedByDescending { it.savedAt }
                )
            }
        }
    }

    /** Dopisuje wybrany plik do biblioteki i zwraca gotowa [Book] do natychmiastowego otwarcia. */
    fun onBookImported(picked: PickedFile): Book {
        val book = Book(
            id = "imported_${picked.persistentLocator.hashCode()}",
            title = picked.name.ifBlank { "Book" },
            author = "Imported",
            format = BookFormat.ofFileName(picked.name),
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

    /**
     * Tresc ksiazki dla trybu reflow. Null oznacza, ze nie udalo sie wyciagnac z niej tekstu -
     * wtedy zostaje podglad oryginalu (PDF na bitmapie), bez tlumaczen i bez liczenia slow.
     */
    suspend fun loadContent(book: Book): BookContent? {
        val bytes = loadBytes(book) ?: return null
        return when (book.format) {
            BookFormat.Txt -> PlainTextParser.parse(bytes.decodeToString())
            BookFormat.Pdf -> extractPdfContent(bytes)
            // EPUB wymaga rozpakowania zipa - dochodzi w etapie 2.
            BookFormat.Epub -> null
        }
    }

    /**
     * Wyciaga tekst PDF-a przez [PdfEngine] i sprowadza go do akapitow.
     *
     * Miedzy ekstrakcja a [PlainTextParser] stoi [PdfTextNormalizer] i to on jest tu istotny:
     * ekstraktor PDF oddaje tekst linia-po-linii tak, jak lezal na stronie, bez zadnego znaku
     * konca akapitu. Bez normalizacji cala ksiazka wygladalaby w czytniku jak jedna scianka.
     *
     * Null, gdy platforma nie potrafi wyciagnac tekstu (desktop) albo PDF nie ma warstwy
     * tekstowej (skan bez OCR) - wtedy zostaje podglad oryginalu.
     */
    private suspend fun extractPdfContent(bytes: ByteArray): BookContent? {
        val document = PdfEngine.open(bytes) ?: return null
        return try {
            val pages = (0 until PdfEngine.pageCount(document))
                .map { page -> PdfEngine.pageText(document, page).orEmpty() }
            PdfTextNormalizer.normalize(pages)
                .takeIf { it.isNotBlank() }
                ?.let { PlainTextParser.parse(it) }
        } finally {
            PdfEngine.close(document)
        }
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
                    current.copy(message = "Couldn't claim the reward - try again.")
                } else {
                    current.copy(
                        dailyGoalClaims = updated.dailyGoalClaims,
                        message = "Claimed +${goal.reward} gold: ${goal.title}"
                    )
                }
            }
        }
    }

    /** Zapis typografii czytnika - wolane przy zamknieciu ksiazki, nie przy kazdej zmianie suwaka. */
    fun saveReaderSettings(settings: ReaderSettings) {
        _uiState.update { it.copy(readerSettings = settings) }
        viewModelScope.launch(Dispatchers.IO) { playerRepository.updateReaderSettings(settings) }
    }

    /** Usuwa fiszke - gracz juz umie to slowo albo zapisal je przez pomylke. */
    fun removeWord(word: SavedWord) {
        _uiState.update { it.copy(savedWords = it.savedWords.filterNot { saved -> saved.id == word.id }) }
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.removeSavedWord(word.id)
            refresh()
        }
    }

    /** Zapamietuje, ktora ksiazke gracz wlasnie otworzyl - stad bierze sie karta "Czytaj dalej". */
    fun onBookOpened(book: Book) {
        _uiState.update { it.copy(lastOpenedBookId = book.id) }
    }

    /** Podpina okladke wgrana przez gracza do ksiazki na polce. */
    fun setCover(book: Book, picked: PickedFile) {
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.setBookCover(book.id, picked.persistentLocator)
            refresh()
        }
    }

    /** Zdejmuje ksiazke z polki. Fiszki z niej zostaja - slowo raz nauczone jest warte tyle samo. */
    fun removeBook(book: Book) {
        _uiState.update { state ->
            state.copy(importedBooks = state.importedBooks.filterNot { it.id == book.id })
        }
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.removeImportedBook(book.id)
            refresh()
        }
    }

    /**
     * Bajty okladki do narysowania. Osobno od [loadBytes], bo okladka jest opcjonalna i jej brak
     * (skasowany plik, cofniete uprawnienie) nie moze wywrocic polki - zwracamy null i biblioteka
     * rysuje zastepczy grzbiet.
     */
    suspend fun loadCover(book: Book): ByteArray? {
        val locator = book.coverLocator ?: return null
        return runCatching { loadImportedBookBytes(locator) }.getOrNull()
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
