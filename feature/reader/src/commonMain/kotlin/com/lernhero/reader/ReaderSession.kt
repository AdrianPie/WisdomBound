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
import kotlinx.coroutines.launch

data class ReaderUiState(
    val loading: Boolean = true,
    val pageIndex: Int = 0,
    val pageCount: Int = 0,
    val pageImage: ImageBitmap? = null,
    val error: String? = null
)

/**
 * Jedna sesja czytania PDF-a w trybie "oryginal" (strona renderowana na bitmapie). To nie jest
 * ViewModel - ksiazka i jej bajty sa znane dopiero w miejscu uzycia (ekran biblioteki), wiec
 * sesja zyje w `remember` na czas otwarcia czytnika i konczy sie wraz z ekranem.
 *
 * Odmierzaniem czasu zajmuje sie [ReadingClock] - ten sam, ktorego uzywa tryb reflow. Wczesniej
 * obie sciezki mialy wlasne, rownolegle kopie tej samej logiki (AFK-guard, zliczanie minut,
 * zapis do konta), co znaczylo dwa miejsca do poprawiania przy kazdej zmianie zasad naliczania.
 */
class ReaderSession(
    private val book: Book,
    private val bytes: ByteArray,
    startPage: Int,
    private val playerRepository: PlayerRepository,
    private val scope: CoroutineScope,
    todayBaseSeconds: Int = 0
) {
    var uiState by mutableStateOf(ReaderUiState())
        private set

    val clock = ReadingClock(playerRepository, scope, todayBaseSeconds)

    private var document: PdfDocumentHandle? = null

    init {
        scope.launch(Dispatchers.Default) {
            val doc = PdfEngine.open(bytes)
            if (doc == null) {
                uiState = uiState.copy(loading = false, error = "Could not open the file.")
                return@launch
            }
            document = doc
            val count = PdfEngine.pageCount(doc)
            uiState = uiState.copy(pageCount = count)
            goToPage(startPage.coerceIn(0, (count - 1).coerceAtLeast(0)))
        }
    }

    fun nextPage() = goToPage(uiState.pageIndex + 1)
    fun previousPage() = goToPage(uiState.pageIndex - 1)

    fun goToPage(index: Int) {
        val doc = document ?: return
        val clamped = index.coerceIn(0, (uiState.pageCount - 1).coerceAtLeast(0))
        clock.onInteraction()

        scope.launch(Dispatchers.Default) {
            val bitmap = PdfEngine.renderPage(doc, clamped, PAGE_RENDER_WIDTH_PX)
            val text = PdfEngine.pageText(doc, clamped)
            val isNewPage = clamped != uiState.pageIndex || uiState.pageImage == null

            if (isNewPage && clamped > uiState.pageIndex) {
                // Liczymy tylko postep do przodu - cofanie sie po ksiazce nie ma zdublowac stron.
                clock.onPageAdvanced(
                    words = text?.let(TextStats::countWords) ?: 0,
                    characters = text?.let(TextStats::countCharacters) ?: 0
                )
            }

            uiState = uiState.copy(loading = false, pageIndex = clamped, pageImage = bitmap)
            playerRepository.updateBookProgress(book.id, clamped)
        }
    }

    /** Zamyka dokument i wysyla ostatnia porcje postepu - wywolaj przy opuszczaniu czytnika. */
    fun close() {
        document?.let { PdfEngine.close(it) }
        clock.close()
    }

    companion object {
        private const val PAGE_RENDER_WIDTH_PX = 1000
    }
}
