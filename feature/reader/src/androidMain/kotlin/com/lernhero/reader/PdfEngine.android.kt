package com.lernhero.reader

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.mp.KoinPlatform

/**
 * Trzyma otwarty [PdfRenderer] razem z jego deskryptorem pliku i tymczasowym plikiem -
 * `PdfRenderer` potrzebuje `ParcelFileDescriptor`, nie surowych bajtow, wiec bajty ladujemy
 * do pliku w cache przy kazdym otwarciu.
 *
 * [pageTexts] jest wyciagniety RAZEM z otwarciem (PDFBox), nie na zadanie w [PdfEngine.pageText] -
 * ten interfejs jest synchroniczny (dziedziczy sygnature po iOS, gdzie `PDFPage.string` jest
 * tania), a parsowanie calego PDF-a przez PDFBox tanie nie jest. Placimy raz przy [open], zamiast
 * ryzykowac zawieszenie UI przy kazdym wywolaniu.
 */
actual class PdfDocumentHandle(
    val renderer: PdfRenderer,
    val fileDescriptor: ParcelFileDescriptor,
    val tempFile: File,
    val pageTexts: List<String?>
)

actual object PdfEngine {

    private fun appContext(): Context = KoinPlatform.getKoin().get()

    actual suspend fun open(bytes: ByteArray): PdfDocumentHandle? = withContext(Dispatchers.IO) {
        runCatching {
            val tempFile = File.createTempFile("book_", ".pdf", appContext().cacheDir)
            tempFile.writeBytes(bytes)
            val descriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(descriptor)
            PdfDocumentHandle(renderer, descriptor, tempFile, extractPageTexts(tempFile, renderer.pageCount))
        }.getOrNull()
    }

    /**
     * PDFBox-Android - `PdfRenderer` systemowy tylko rysuje strony, nie ma zadnego API do
     * tekstu. Bledy pojedynczej strony (zeskanowany obraz bez warstwy tekstu, uszkodzony font)
     * nie maja zawalic calej ksiazki - stad `null` per strona zamiast wyjatku dla calego dokumentu.
     */
    private fun extractPageTexts(file: File, pageCount: Int): List<String?> {
        PDFBoxResourceLoader.init(appContext())
        return runCatching {
            PDDocument.load(file).use { document ->
                val stripper = PDFTextStripper()
                (1..pageCount).map { page ->
                    stripper.startPage = page
                    stripper.endPage = page
                    runCatching { stripper.getText(document) }.getOrNull()
                }
            }
        }.getOrElse { List(pageCount) { null } }
    }

    actual fun pageCount(document: PdfDocumentHandle): Int = document.renderer.pageCount

    actual suspend fun renderPage(document: PdfDocumentHandle, pageIndex: Int, widthPx: Int): ImageBitmap =
        withContext(Dispatchers.IO) {
            document.renderer.openPage(pageIndex).use { page ->
                val heightPx = (widthPx.toFloat() * page.height / page.width).toInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap.asImageBitmap()
            }
        }

    actual fun pageText(document: PdfDocumentHandle, pageIndex: Int): String? =
        document.pageTexts.getOrNull(pageIndex)

    actual fun close(document: PdfDocumentHandle) {
        runCatching { document.renderer.close() }
        runCatching { document.fileDescriptor.close() }
        runCatching { document.tempFile.delete() }
    }
}

@Composable
actual fun rememberBookFilePicker(onPicked: (PickedFile) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            // Bez tego dostep do pliku zniknalby po zamknieciu aplikacji.
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
            val name = displayNameOf(context.contentResolver, uri) ?: "Ksiazka"
            withContext(Dispatchers.Main) {
                onPicked(PickedFile(name = name, bytes = bytes, persistentLocator = uri.toString()))
            }
        }
    }

    return remember(launcher) {
        // Nie sam PDF - czytnik obsluguje tez czysty tekst, a EPUB dochodzi wkrotce.
        { launcher.launch(arrayOf("application/pdf", "text/plain", "application/epub+zip")) }
    }
}

@Composable
actual fun rememberCoverImagePicker(onPicked: (PickedFile) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            // Okladka musi przezyc restart aplikacji tak samo jak sama ksiazka - bez trwalego
            // uprawnienia polka rozsypalaby sie na szare placeholdery po pierwszym zamknieciu.
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
            val name = displayNameOf(context.contentResolver, uri) ?: "Okladka"
            withContext(Dispatchers.Main) {
                onPicked(PickedFile(name = name, bytes = bytes, persistentLocator = uri.toString()))
            }
        }
    }

    return remember(launcher) { { launcher.launch(arrayOf("image/*")) } }
}

actual fun decodeImageBytes(bytes: ByteArray): ImageBitmap? =
    runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() }.getOrNull()

actual suspend fun loadImportedBookBytes(locator: String): ByteArray? = withContext(Dispatchers.IO) {
    runCatching {
        val context = KoinPlatform.getKoin().get<Context>()
        context.contentResolver.openInputStream(Uri.parse(locator))?.use { it.readBytes() }
    }.getOrNull()
}

private fun displayNameOf(resolver: ContentResolver, uri: Uri): String? =
    resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }
