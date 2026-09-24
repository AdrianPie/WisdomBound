package com.lernhero.reader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lernhero.domain.reading.Book
import com.lernhero.domain.reading.BookContent
import com.lernhero.domain.repository.PlayerRepository
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val Bg = Color(0xFF120E0A)
private val Panel = Color(0xFF1A1410)
private val Ink = Color(0xFF0F0B08)
private val Gold = Color(0xFFFFD100)
private val GoldFrame = Color(0xFFC8A45A)
private val GoldDim = Color(0xFF6B5A38)
private val Muted = Color(0xFF9D8B6A)
private val Cream = Color(0xFFEADFC8)
private val Green = Color(0xFF4ADE80)
private val Amber = Color(0xFFE0913A)

/**
 * Zakladka "Czytelnia": biblioteka (bundlowane + wgrane ksiazki, statystyki, cele dzienne)
 * albo otwarty czytnik - jedno miejsce, przelaczane lokalnym stanem, bez osobnej trasy nawigacji
 * (bajty ksiazki nie nadaja sie do przekazania przez argumenty nawigacji).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReadingScreen(
    /**
     * Marginesy pod paski aplikacji - dotycza WYLACZNIE biblioteki. Otwarta ksiazka chowa paski
     * (ReaderChrome.immersive) i zajmuje caly ekran, wiec ich nie dostaje.
     */
    libraryInsets: PaddingValues = PaddingValues(0.dp),
    /** Wejscie do osobnej instancji "Slowka" - patrz [VocabularyScreen]. */
    onOpenVocabulary: () -> Unit = {},
    /** Po odebraniu nagrody za czytanie - zeby gorny pasek gry od razu pokazal nowe zloto/diamenty. */
    onRewardClaimed: () -> Unit = {}
) {
    val viewModel = koinViewModel<LibraryViewModel>()
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var openBook by remember { mutableStateOf<Book?>(null) }
    var openBytes by remember { mutableStateOf<ByteArray?>(null) }
    var openContent by remember { mutableStateOf<BookContent?>(null) }
    var opening by remember { mutableStateOf<String?>(null) }

    // Ksiazka, dla ktorej gracz wybiera wlasnie okladke. Picker jest jeden na ekran (systemowy
    // wybor pliku nie da sie zawolac "dla wiersza"), wiec cel zapamietujemy tutaj.
    var coverTarget by remember { mutableStateOf<Book?>(null) }

    val requestImport = rememberBookFilePicker(onPicked = { picked ->
        val book = viewModel.onBookImported(picked)
        viewModel.onBookOpened(book)
        openBook = book
        openBytes = picked.bytes
        scope.launch { openContent = viewModel.loadContent(book) }
    })

    val requestCover = rememberCoverImagePicker(onPicked = { picked ->
        coverTarget?.let { viewModel.setCover(it, picked) }
        coverTarget = null
    })

    fun open(book: Book) {
        opening = book.id
        scope.launch {
            // Reflow potrzebuje tekstu, podglad oryginalu - bajtow. Format decyduje, czego szukamy.
            val content = viewModel.loadContent(book)
            val bytes = if (content == null) viewModel.loadBytes(book) else null
            opening = null
            if (content != null || bytes != null) {
                viewModel.onBookOpened(book)
                openBook = book
                openContent = content
                openBytes = bytes
            }
        }
    }

    fun closeReader() {
        openBook = null
        openBytes = null
        openContent = null
        // Ostatnia porcja czasu leci na konto asynchronicznie (ReadingClock.close) - drugie
        // odswiezenie chwile pozniej lapie ja, zeby licznik dnia nie stal o kilka sekund w tyle.
        viewModel.refresh()
        scope.launch {
            kotlinx.coroutines.delay(1200)
            viewModel.refresh()
        }
    }

    val book = openBook
    val bytes = openBytes
    val content = openContent
    when {
        // Otwarta ksiazka MUSI miec pelne, nieprzezroczyste tlo - tekst ma byc czytelny, nie
        // walczyc z niebem Miasta pod spodem.
        book != null && content != null -> Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
            ReflowReaderView(
                book = book,
                content = content,
                startBlockIndex = state.pageFor(book),
                initialSettings = state.readerSettings,
                onSettingsChanged = viewModel::saveReaderSettings,
                onClose = ::closeReader,
                todayBaseSeconds = state.readingToday.activeSeconds
            )
        }
        book != null && bytes != null -> Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
            ReaderView(
                book = book,
                bytes = bytes,
                startPage = state.pageFor(book),
                todayBaseSeconds = state.readingToday.activeSeconds,
                onClose = ::closeReader
            )
        }
        // Biblioteka jest przezroczysta - niebo Miasta przebija jak na innych zakladkach.
        else -> Box(modifier = Modifier.fillMaxSize()) {
            LibraryView(
                state = state,
                openingId = opening,
                onOpen = ::open,
                onImport = requestImport,
                onOpenVocabulary = onOpenVocabulary,
                onPickCover = { target ->
                    coverTarget = target
                    requestCover()
                },
                onRemoveBook = viewModel::removeBook,
                onClaim = { goal -> viewModel.claim(goal, onRewardClaimed) },
                loadCover = viewModel::loadCover,
                insets = libraryInsets
            )
        }
    }
}

/**
 * Biblioteka w stylu ksiegarni: karta dziennego czytania z nagrodami na gorze (to ona ma ciagnac
 * do codziennych 15 minut), duza karta "Czytaj dalej", potem polki z okladkami stojacymi na
 * drewnianej desce - "Czytasz teraz", "Moje ksiazki" (z okladka "+ Dodaj") i darmowa klasyka.
 */
@Composable
private fun LibraryView(
    state: LibraryUiState,
    openingId: String?,
    onOpen: (Book) -> Unit,
    onImport: () -> Unit,
    onOpenVocabulary: () -> Unit,
    onPickCover: (Book) -> Unit,
    onRemoveBook: (Book) -> Unit,
    onClaim: (com.lernhero.domain.reading.ReadingGoal) -> Unit,
    loadCover: suspend (Book) -> ByteArray?,
    insets: PaddingValues
) {
    val readingNow = state.allBooks.filter { (state.bookProgress[it.id] ?: 0) > 0 && it.id != state.continueBook?.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(insets)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KitText(text = "Library", size = 28.sp, color = KitGold)
            Box(modifier = Modifier.weight(1f))
            HeaderChip("📖", "${state.allBooks.size}")
            HeaderChip("🔤", "${state.wordsLearning + state.wordsMastered}")
        }

        DailyReadingCard(
            today = state.readingToday,
            claimed = state::claimedToday,
            onClaim = onClaim
        )

        state.continueBook?.let { book ->
            ContinueCard(
                book = book,
                page = state.pageFor(book),
                loading = openingId == book.id,
                loadCover = loadCover,
                onClick = { onOpen(book) }
            )
        }

        if (readingNow.isNotEmpty()) {
            Shelf(
                title = "Reading Now",
                books = readingNow,
                state = state,
                openingId = openingId,
                loadCover = loadCover,
                onOpen = onOpen,
                onPickCover = null,
                onRemove = null
            )
        }

        Shelf(
            title = "My Books",
            books = state.importedBooks,
            state = state,
            openingId = openingId,
            loadCover = loadCover,
            onOpen = onOpen,
            onPickCover = onPickCover,
            onRemove = onRemoveBook,
            onAdd = onImport
        )

        Shelf(
            title = "Free Classics",
            books = state.bundledBooks,
            state = state,
            openingId = openingId,
            loadCover = loadCover,
            onOpen = onOpen,
            // Bundlowanych nie da sie ani usunac, ani przebrac w cudza okladke - nie sa "twoje".
            onPickCover = null,
            onRemove = null
        )

        VocabularyCard(
            learning = state.wordsLearning,
            mastered = state.wordsMastered,
            onClick = onOpenVocabulary
        )

        StatsPanel(state)

        state.message?.let { message ->
            Text(text = message, color = Green, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun HeaderChip(icon: String, value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xE60C0806))
            .border(1.5.dp, KitOutline, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 13.sp)
        Text(value, color = Color.White, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 13.sp)
    }
}

/** Liczby calego czytania - na dole, bo to podsumowanie, a nie powod wejscia do biblioteki. */
@Composable
private fun StatsPanel(state: LibraryUiState) {
    KitPanel(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KitText(text = "Your Reading", size = 17.sp, color = KitGold, strokeWidth = 5f)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("⏱", state.readingStats.activeSeconds.asHoursMinutes(), "Total time", Modifier.weight(1f))
                StatTile("📄", "${state.readingStats.totalPages}", "Pages", Modifier.weight(1f))
                StatTile("⚡", "${state.readingStats.averageWpm}", "Words/min", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x66000000))
            .border(2.dp, KitOutline, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(icon, fontSize = 16.sp)
        KitText(text = value, size = 17.sp, color = Color.White, maxLines = 1)
        Text(label.uppercase(), color = KitMuted, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 9.sp)
    }
}

/**
 * "Czytaj dalej" - najwazniejszy przycisk calego ekranu: duza okladka, tytul, autor, strona i
 * wielki zielony przycisk. Gracz w 95% przypadkow wchodzi tu po to, zeby wrocic do tej ksiazki.
 */
@Composable
private fun ContinueCard(
    book: Book,
    page: Int,
    loading: Boolean,
    loadCover: suspend (Book) -> ByteArray?,
    onClick: () -> Unit
) {
    KitPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(92.dp)) {
                CoverArt(book = book, loadCover = loadCover)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("CONTINUE READING", color = KitGold, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 10.sp)
                KitText(text = book.title, size = 18.sp, color = Color.White, maxLines = 2)
                Text(
                    text = book.author,
                    color = KitMuted,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (page > 0) "📍 Section ${page + 1}" else "📍 Start from the beginning",
                    color = Cream,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
                if (loading) {
                    CircularProgressIndicator(color = Gold, modifier = Modifier.size(24.dp))
                } else {
                    KitButton(label = "▶  READ", colors = GreenButton, onClick = onClick, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

/** Wejscie do osobnej instancji Slowek - z licznikiem obu archiwow, zeby bylo po co klikac. */
@Composable
private fun VocabularyCard(learning: Int, mastered: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF1E4F95), Color(0xFF12305E))))
            .border(2.5.dp, KitOutline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔤", fontSize = 30.sp)
        Column(modifier = Modifier.weight(1f)) {
            KitText(text = "Words", size = 18.sp, color = Color.White, strokeWidth = 5f)
            Text(
                text = "$learning to learn  ·  $mastered mastered",
                color = Color(0xFFBFD3F0),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        KitText(text = "›", size = 26.sp, color = KitGold)
    }
}

/**
 * Polka: tytul, a pod nim okladki STOJACE na drewnianej desce, przewijane w poziomie.
 * [onAdd] dodaje na poczatku okladke "+ Dodaj ksiazke" - import jest czescia polki, nie osobnym
 * przyciskiem pod nia.
 */
@Composable
private fun Shelf(
    title: String,
    books: List<Book>,
    state: LibraryUiState,
    openingId: String?,
    loadCover: suspend (Book) -> ByteArray?,
    onOpen: (Book) -> Unit,
    onPickCover: ((Book) -> Unit)?,
    onRemove: ((Book) -> Unit)?,
    onAdd: (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KitText(text = title, size = 18.sp, color = KitGold, strokeWidth = 5f)
            Text("${books.size}", color = KitMuted, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
        Box {
            // Deska polki pod okladkami.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 36.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF9A6A3A), Color(0xFF5A3A1C))))
                    .border(1.5.dp, KitOutline, RoundedCornerShape(4.dp))
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                if (onAdd != null) {
                    item(key = "add") { AddBookCard(onClick = onAdd) }
                }
                items(books, key = { it.id }) { book ->
                    BookCard(
                        book = book,
                        page = state.bookProgress[book.id],
                        loading = openingId == book.id,
                        loadCover = loadCover,
                        onOpen = { onOpen(book) },
                        onPickCover = onPickCover?.let { pick -> { pick(book) } },
                        onRemove = onRemove?.let { remove -> { remove(book) } }
                    )
                }
            }
        }
        if (books.isEmpty() && onAdd != null) {
            Text(
                text = "Upload any TXT or PDF - a school reading, a novel, notes. Tap a word while reading to translate and save it.",
                color = KitMuted,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }
    }
}

/** Szerokosc okladki na polce - okladki maja proporcje ksiazki (2:3), stad tylko szerokosc. */
private val COVER_WIDTH = 96.dp

@Composable
private fun AddBookCard(onClick: () -> Unit) {
    Column(modifier = Modifier.width(COVER_WIDTH), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.66f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x55000000))
                .border(2.dp, KitGold.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                KitText(text = "+", size = 36.sp, color = KitGold)
                Text("Add book", color = KitGold, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 11.sp)
                Text("TXT · PDF", color = KitMuted, fontFamily = FontFirst(), fontSize = 9.sp)
            }
        }
        Box(modifier = Modifier.height(30.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookCard(
    book: Book,
    page: Int?,
    loading: Boolean,
    loadCover: suspend (Book) -> ByteArray?,
    onOpen: () -> Unit,
    onPickCover: (() -> Unit)?,
    onRemove: (() -> Unit)?
) {
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.width(COVER_WIDTH),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onOpen,
                    // Dlugie przytrzymanie zamiast widocznego przycisku "..." - okladka ma byc
                    // okladka, a nie kafelkiem obwieszonym ikonkami.
                    onLongClick = { if (onPickCover != null || onRemove != null) menuOpen = true }
                )
        ) {
            CoverArt(book = book, loadCover = loadCover)

            if (page != null && page > 0) {
                Text(
                    text = "§${page + 1}",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF3F9E2B))
                        .border(1.5.dp, KitOutline, RoundedCornerShape(50))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }

            if (loading) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xAA0A0806)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Gold, modifier = Modifier.size(20.dp))
                }
            }
        }

        Column(modifier = Modifier.height(30.dp)) {
            Text(
                text = book.title,
                color = Cream,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.author,
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (menuOpen) {
        BookMenuDialog(
            book = book,
            onDismiss = { menuOpen = false },
            onPickCover = onPickCover?.let { pick -> { menuOpen = false; pick() } },
            onRemove = onRemove?.let { remove -> { menuOpen = false; remove() } }
        )
    }
}

/** Zastepcze okladki - kolor z tytulu, wiec ta sama ksiazka ma zawsze ten sam kolor. */
private val CoverColors = listOf(
    listOf(Color(0xFF7A2E2E), Color(0xFF3E1520)),
    listOf(Color(0xFF1E4F95), Color(0xFF12305E)),
    listOf(Color(0xFF2E6B3F), Color(0xFF173A22)),
    listOf(Color(0xFF6B3A8A), Color(0xFF341A48)),
    listOf(Color(0xFF8A5A1E), Color(0xFF4A2E0C)),
    listOf(Color(0xFF1E6B6B), Color(0xFF0E3A3A))
)

/**
 * Okladka: wgrany obrazek albo wygenerowana okladka (kolor z tytulu, grzbiet, zlote ramki).
 * Bajty czytane sa leniwie, per ksiazka - polka nie moze czekac z narysowaniem sie na dysk.
 */
@Composable
private fun CoverArt(book: Book, loadCover: suspend (Book) -> ByteArray?) {
    var cover by remember(book.id, book.coverLocator) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(book.id, book.coverLocator) {
        cover = loadCover(book)?.let { bytes -> decodeImageBytes(bytes) }
    }

    val colors = CoverColors[(book.title.hashCode() and 0x7fffffff) % CoverColors.size]
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.66f)
            .clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp, topEnd = 8.dp, bottomEnd = 8.dp))
            .background(Brush.verticalGradient(colors))
            .border(2.dp, KitOutline, RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp, topEnd = 8.dp, bottomEnd = 8.dp)),
        contentAlignment = Alignment.Center
    ) {
        val image = cover
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = book.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .border(1.dp, KitGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            )
            Column(
                modifier = Modifier.padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("✦", color = KitGold, fontSize = 12.sp)
                Text(
                    text = book.title,
                    color = Color(0xFFFFF1D6),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Text(book.format.name.uppercase(), color = KitGold.copy(alpha = 0.7f), fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 8.sp)
            }
        }
        // Grzbiet - ciemny pasek z polyskiem przy lewej krawedzi, jak u prawdziwej ksiazki.
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(7.dp)
                .background(Brush.horizontalGradient(listOf(Color(0x66000000), Color(0x22FFFFFF), Color(0x00000000))))
        )
    }
}

@Composable
private fun BookMenuDialog(
    book: Book,
    onDismiss: () -> Unit,
    onPickCover: (() -> Unit)?,
    onRemove: (() -> Unit)?
) {
    Dialog(onDismissRequest = onDismiss) {
        KitPanel(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KitText(text = book.title, size = 18.sp, color = KitGold, maxLines = 2)
                onPickCover?.let {
                    KitButton(
                        label = if (book.coverLocator == null) "Upload Cover" else "Change Cover",
                        colors = listOf(Color(0xFF5BB8FF), Color(0xFF2C6FC4)),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = it
                    )
                }
                onRemove?.let {
                    KitButton(
                        label = "Remove from Library",
                        colors = listOf(Color(0xFFE5645A), Color(0xFF8C2A22)),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = it
                    )
                }
                KitButton(label = "Cancel", colors = listOf(Color(0xFF6B5A48), Color(0xFF3A2E22)), modifier = Modifier.fillMaxWidth(), onClick = onDismiss)
            }
        }
    }
}

/* --------------------------------------------------------------------------------------------- */

@Composable
private fun ReaderView(
    book: Book,
    bytes: ByteArray,
    startPage: Int,
    todayBaseSeconds: Int,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val playerRepository = koinInject<PlayerRepository>()

    val session = remember(book.id) { ReaderSession(book, bytes, startPage, playerRepository, scope, todayBaseSeconds) }
    val state = session.uiState
    var zoom by remember { mutableStateOf(1f) }
    var chromeVisible by remember { mutableStateOf(true) }

    // Chowa gorny/dolny pasek nawigacji i systemowy pasek statusu na czas czytania -
    // strona ma zajac caly ekran, bez zadnego chromu ponad nia.
    androidx.compose.runtime.DisposableEffect(Unit) {
        ReaderChrome.immersive = true
        setStatusBarHidden(true)
        onDispose {
            ReaderChrome.immersive = false
            setStatusBarHidden(false)
        }
    }
    OnLeaveScreen { session.close() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0806))
    ) {
        when {
            state.error != null -> Text(
                text = state.error,
                color = Amber,
                fontFamily = FontFirst(),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).padding(24.dp)
            )

            state.loading || state.pageImage == null -> CircularProgressIndicator(color = Gold, modifier = Modifier.align(Alignment.Center))

            else -> {
                val image = state.pageImage
                val vScroll = remember(state.pageIndex) { androidx.compose.foundation.ScrollState(0) }
                Box(
                    modifier = Modifier.fillMaxSize().verticalScroll(vScroll),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = image,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = zoom,
                                scaleY = zoom,
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                            ),
                        contentScale = ContentScale.Fit
                    )
                }

                // Brzegi kartkuja strone, srodek chowa/pokazuje maly pasek na dole.
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxHeight().weight(0.2f).clickable { session.previousPage() })
                    Box(modifier = Modifier.fillMaxHeight().weight(0.6f).clickable { chromeVisible = !chromeVisible })
                    Box(modifier = Modifier.fillMaxHeight().weight(0.2f).clickable { session.nextPage() })
                }
            }
        }

        Text(
            text = "‹",
            color = Gold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 14.dp)
                .clip(CircleShape)
                .background(Color(0x99120E0A))
                .clickable(onClick = onClose)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )

        androidx.compose.animation.AnimatedVisibility(
            visible = chromeVisible,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it },
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReaderBottomBar(
                state = state,
                clock = session.clock.state,
                zoom = zoom,
                onZoomOut = { zoom = (zoom - 0.25f).coerceAtLeast(0.75f) },
                onZoomIn = { zoom = (zoom + 0.25f).coerceAtMost(2.5f) }
            )
        }
    }
}

/**
 * Cienki, polprzezroczysty pasek nad tekstem - powrot, licznik stron, czas aktywnego czytania
 * i przyciski powiekszenia. Nie zajmuje wlasnej przestrzeni w Column jak dawniej: lezy NA
 * stronie, wiec sama strona nadal ma cala wysokosc ekranu pod spodem.
 */
@Composable
private fun ReaderBottomBar(
    state: ReaderUiState,
    clock: ReadingClockState,
    zoom: Float,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(bottom = 22.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xCC120E0A))
            .border(1.dp, GoldDim, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (clock.paused) Amber else Green))
        Text(
            text = if (clock.paused) {
                "wstrzymano"
            } else {
                "${clock.sessionSeconds / 60}:${(clock.sessionSeconds % 60).toString().padStart(2, '0')}"
            },
            color = if (clock.paused) Amber else Muted,
            fontFamily = FontFirst(),
            fontSize = 10.sp
        )

        Text(
            text = "${state.pageIndex + 1}/${state.pageCount}",
            color = Gold,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )

        ZoomButton(text = "–", enabled = zoom > 0.75f, onClick = onZoomOut)
        Text(text = "${(zoom * 100).toInt()}%", color = Muted, fontFamily = FontFirst(), fontSize = 9.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
        ZoomButton(text = "+", enabled = zoom < 2.5f, onClick = onZoomIn)
    }
}

@Composable
private fun ZoomButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) Ink else Ink.copy(alpha = 0.4f))
            .border(1.dp, if (enabled) GoldFrame else GoldDim, RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = if (enabled) Gold else Muted, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun OnLeaveScreen(action: () -> Unit) {
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { action() }
    }
}
