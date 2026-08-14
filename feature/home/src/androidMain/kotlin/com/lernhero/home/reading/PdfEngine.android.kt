package com.lernhero.home.reading

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
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
 */
actual class PdfDocumentHandle(
    val renderer: PdfRenderer,
    val fileDescriptor: ParcelFileDescriptor,
    val tempFile: File
)

actual object PdfEngine {

    private fun appContext(): Context = KoinPlatform.getKoin().get()

    actual suspend fun open(bytes: ByteArray): PdfDocumentHandle? = withContext(Dispatchers.IO) {
        runCatching {
            val tempFile = File.createTempFile("book_", ".pdf", appContext().cacheDir)
            tempFile.writeBytes(bytes)
            val descriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            PdfDocumentHandle(PdfRenderer(descriptor), descriptor, tempFile)
        }.getOrNull()
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

    // Android nie ma wbudowanego API do wyciagania tekstu z PDF - PdfRenderer tylko rysuje strony
    // jako bitmapy. Liczba slow dla PDF-ow na tej platformie zostaje wiec niepoliczona (null).
    actual fun pageText(document: PdfDocumentHandle, pageIndex: Int): String? = null

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
        { launcher.launch(arrayOf("application/pdf")) }
    }
}

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
