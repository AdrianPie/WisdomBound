package com.lernhero.reader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
    onOpenVocabulary: () -> Unit = {}
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
        viewModel.refresh()
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
                onClose = ::closeReader
            )
        }
        book != null && bytes != null -> Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
            ReaderView(
                book = book,
                bytes = bytes,
                startPage = state.pageFor(book),
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
                loadCover = viewModel::loadCover,
                insets = libraryInsets
            )
        }
    }
}

/**
 * Biblioteka: cienki pasek liczb, karta "Czytaj dalej", polka z okladkami i wejscie do Slowek.
 *
 * Celow dziennych tu NIE MA - przeniosly sie do zakladki Postep, gdzie mieszkaja wszystkie inne
 * nagrody i osiagniecia. Czytelnia ma wygladac jak aplikacja do czytania, a nie jak lista zadan:
 * gracz wchodzi tu, zeby wrocic do ksiazki, nie zeby odebrac zloto.
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
    loadCover: suspend (Book) -> ByteArray?,
    insets: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Marginesy pod paski SA CZESCIA obszaru przewijania, nie ramka wokol niego - dzieki
            // temu tresc przewija sie pod paskiem, zamiast urywac sie tuz nad nim.
            .verticalScroll(rememberScrollState())
            .padding(insets)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Library",
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
            MiniStats(state)
        }

        state.continueBook?.let { book ->
            ContinueCard(
                book = book,
                page = state.pageFor(book),
                loading = openingId == book.id,
                loadCover = loadCover,
                onClick = { onOpen(book) }
            )
        }

        VocabularyCard(
            learning = state.wordsLearning,
            mastered = state.wordsMastered,
            onClick = onOpenVocabulary
        )

        Shelf(
            title = "Your Library",
            books = state.importedBooks,
            state = state,
            openingId = openingId,
            loadCover = loadCover,
            onOpen = onOpen,
            onPickCover = onPickCover,
            onRemove = onRemoveBook,
            emptyHint = "You haven't uploaded any books yet."
        )

        ImportButton(onClick = onImport)

        Shelf(
            title = "Z wygasla licencja",
            books = state.bundledBooks,
            state = state,
            openingId = openingId,
            loadCover = loadCover,
            onOpen = onOpen,
            // Bundlowanych nie da sie ani usunac, ani przebrac w cudza okladke - nie sa "twoje".
            onPickCover = null,
            onRemove = null
        )

        state.message?.let { message ->
            Text(text = message, color = Green, fontFamily = FontFirst(), fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** Minimalny licznik w naglowku - cztery liczby w jednej linii, bez wlasnej karty i ramki. */
@Composable
private fun MiniStats(state: LibraryUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
        MiniStat(value = "${state.readingStats.totalMinutes}", label = "min")
        MiniStat(value = "${state.readingStats.totalPages}", label = "pages")
        MiniStat(value = "${state.readingStats.averageWpm}", label = "wpm")
        MiniStat(value = "${state.readingToday.minutes}", label = "today", accent = true)
    }
}

@Composable
private fun MiniStat(value: String, label: String, accent: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = if (accent) Green else GoldFrame,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Text(text = label, color = Muted, fontFamily = FontFirst(), fontSize = 8.sp)
    }
}

/**
 * "Czytaj dalej" - najwazniejszy przycisk calego ekranu. Gracz w 95% przypadkow wchodzi tu po to,
 * zeby wrocic do tej jednej ksiazki, wiec stoi ona nad polka i mowi od razu, na ktorej stronie
 * zostal.
 */
@Composable
private fun ContinueCard(
    book: Book,
    page: Int,
    loading: Boolean,
    loadCover: suspend (Book) -> ByteArray?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Panel)
            .border(2.dp, GoldFrame, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(52.dp)) {
            CoverArt(book = book, loadCover = loadCover)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = "CZYTAJ DALEJ", color = GoldDim, fontFamily = FontPixel(), fontSize = 9.sp)
            Text(
                text = book.title,
                color = Cream,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (page > 0) "strona ${page + 1}" else "od poczatku",
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }
        if (loading) {
            CircularProgressIndicator(color = Gold, modifier = Modifier.size(20.dp))
        } else {
            Text(text = "▶", color = Gold, fontFamily = FontPixel(), fontSize = 18.sp)
        }
    }
}

/** Wejscie do osobnej instancji Slowek - z licznikiem obu archiwow, zeby bylo po co klikac. */
@Composable
private fun VocabularyCard(learning: Int, mastered: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Ink)
            .border(1.5.dp, GoldDim, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Words", color = Gold, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
                text = "$learning do nauki  ·  $mastered opanowanych",
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
        }
        Text(text = "›", color = GoldFrame, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

/** Rzad okladek przewijany w poziomie - polka, a nie lista wierszy. */
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
    emptyHint: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = Gold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        if (books.isEmpty()) {
            if (emptyHint != null) {
                Text(text = emptyHint, color = Muted, fontFamily = FontFirst(), fontSize = 11.sp)
            }
            return@Column
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
}

/** Szerokosc okladki na polce - okladki maja proporcje ksiazki (2:3), stad tylko szerokosc. */
private val COVER_WIDTH = 96.dp

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

        Text(
            text = book.title,
            color = Cream,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (page != null && page > 0) "str. ${page + 1}" else book.author,
            color = if (page != null && page > 0) GoldFrame else Muted,
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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

/**
 * Okladka: wgrany obrazek albo zastepczy grzbiet z tytulem. Bajty czytane sa leniwie, per
 * ksiazka - polka nie moze czekac z narysowaniem sie na dysk.
 */
@Composable
private fun CoverArt(book: Book, loadCover: suspend (Book) -> ByteArray?) {
    var cover by remember(book.id, book.coverLocator) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(book.id, book.coverLocator) {
        cover = loadCover(book)?.let { bytes -> decodeImageBytes(bytes) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.66f)
            .clip(RoundedCornerShape(8.dp))
            .background(Ink)
            .border(1.5.dp, GoldDim, RoundedCornerShape(8.dp)),
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
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = book.title,
                    color = GoldFrame,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = book.format.name.uppercase(), color = GoldDim, fontFamily = FontFirst(), fontSize = 8.sp)
            }
        }
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Panel)
                .border(2.dp, GoldFrame, RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = book.title,
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            onPickCover?.let {
                MenuButton(
                    text = if (book.coverLocator == null) "Upload Cover" else "Change Cover",
                    accent = GoldFrame,
                    onClick = it
                )
            }
            onRemove?.let {
                MenuButton(text = "Remove from Library", accent = Amber, onClick = it)
            }
            MenuButton(text = "Cancel", accent = GoldDim, onClick = onDismiss)
        }
    }
}

@Composable
private fun MenuButton(text: String, accent: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Ink)
            .border(1.5.dp, accent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = accent, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun ImportButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFB4451F))
            .border(1.5.dp, Gold, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "+ Dodaj ksiazke (TXT, PDF)", color = Color.White, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

/* --------------------------------------------------------------------------------------------- */

@Composable
private fun ReaderView(
    book: Book,
    bytes: ByteArray,
    startPage: Int,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val playerRepository = koinInject<PlayerRepository>()

    val session = remember(book.id) { ReaderSession(book, bytes, startPage, playerRepository, scope) }
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
