@file:OptIn(ExperimentalForeignApi::class)

package com.lernhero.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
// Wildcard - sporo z tych funkcji (CGSizeMake, UIImagePNGRepresentation, NSData.dataWithBytes...)
// to rozszerzenia pakietowe wygenerowane z kategorii Objective-C, patrz notatka w FitnessTracker.ios.kt.
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.PDFKit.*
import platform.UIKit.*
import platform.darwin.NSObject
import platform.posix.memcpy

actual class PdfDocumentHandle(val document: PDFDocument)

private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { pinned -> NSData.dataWithBytes(pinned.addressOf(0), size.toULong()) }
}

private fun NSData.toByteArray(): ByteArray {
    val len = length.toInt()
    val out = ByteArray(len)
    if (len > 0) {
        out.usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    }
    return out
}

actual object PdfEngine {

    actual suspend fun open(bytes: ByteArray): PdfDocumentHandle? =
        runCatching { PdfDocumentHandle(PDFDocument(bytes.toNSData())) }.getOrNull()

    actual fun pageCount(document: PdfDocumentHandle): Int = document.document.pageCount.toInt()

    actual suspend fun renderPage(document: PdfDocumentHandle, pageIndex: Int, widthPx: Int): ImageBitmap {
        val page = document.document.pageAtIndex(pageIndex.toULong())
            ?: return ImageBitmap(width = widthPx.coerceAtLeast(1), height = 1)

        val (pageWidth, pageHeight) = page.boundsForBox(kPDFDisplayBoxMediaBox).useContents {
            size.width to size.height
        }
        val heightPx = if (pageWidth <= 0.0) 1 else (widthPx * pageHeight / pageWidth).toInt().coerceAtLeast(1)

        val thumbnail = page.thumbnailOfSize(
            CGSizeMake(widthPx.toDouble(), heightPx.toDouble()),
            kPDFDisplayBoxMediaBox
        )
        val pngData = UIImagePNGRepresentation(thumbnail)
            ?: return ImageBitmap(width = widthPx.coerceAtLeast(1), height = heightPx)

        return Image.makeFromEncoded(pngData.toByteArray()).toComposeImageBitmap()
    }

    // iOS ma prawdziwe wyciaganie tekstu przez PDFKit - w przeciwienstwie do Androida.
    actual fun pageText(document: PdfDocumentHandle, pageIndex: Int): String? =
        document.document.pageAtIndex(pageIndex.toULong())?.string

    actual fun close(document: PdfDocumentHandle) = Unit
}

/**
 * Trzyma delegata pickera, dopoki system nie wywola callbacku - inaczej ARC moglby go
 * zwolnic zaraz po powrocie z tej funkcji, zanim uzytkownik cokolwiek wybierze.
 */
private var activeDocumentPickerDelegate: UIDocumentPickerDelegateProtocol? = null

/** Nasz wlasny katalog na ksiazki wewnatrz Documents - trwaly, nie czyszczony przez system. */
private const val BOOKS_DIRECTORY = "Books"

private fun documentsPath(): String =
    NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        .first() as String

private fun booksDirectoryPath(): String {
    val path = documentsPath() + "/" + BOOKS_DIRECTORY
    NSFileManager.defaultManager.createDirectoryAtPath(path, true, null, null)
    return path
}

/**
 * Sciezka pliku ksiazki z jej [locator].
 *
 * [locator] to SAMA NAZWA PLIKU, nigdy sciezka bezwzgledna - i to jest tu sedno. Absolutna
 * sciezka w iOS zawiera UUID kontenera aplikacji, ktory zmienia sie przy kazdej reinstalacji
 * i aktualizacji. Zapisana na trwale wskazywalaby po takiej zmianie w prozne miejsce i ksiazka
 * nie chcialaby sie otworzyc, dopoki gracz nie zaimportowal jej ponownie.
 */
private fun bookPath(locator: String): String =
    if (locator.startsWith("/")) locator // stary zapis sprzed tej zmiany - probujemy, moze jeszcze zyje
    else booksDirectoryPath() + "/" + locator

@Composable
actual fun rememberBookFilePicker(onPicked: (PickedFile) -> Unit): () -> Unit {
    val viewController = LocalUIViewController.current

    return remember(viewController) {
        {
            val picker = UIDocumentPickerViewController(
                documentTypes = listOf("com.adobe.pdf", "public.plain-text", "org.idpf.epub-container"),
                // Import kopiuje plik do sandboxa apki - nie potrzeba zakladek bezpieczenstwa.
                // Kopia ladzie jednak w tymczasowym Inbox, wiec i tak przenosimy ja do siebie.
                inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
            )

            val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentsAtURLs: List<*>
                ) {
                    activeDocumentPickerDelegate = null
                    val pickedPath = (didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.path ?: return
                    val data = NSData.dataWithContentsOfFile(pickedPath) ?: return

                    val fileName = pickedPath.substringAfterLast("/")
                    val storedName = uniqueFileName(fileName)
                    // Przenosimy do wlasnego katalogu: plik z Inboxa system moze skasowac.
                    data.writeToFile(booksDirectoryPath() + "/" + storedName, true)

                    onPicked(
                        PickedFile(
                            name = fileName.substringBeforeLast("."),
                            bytes = data.toByteArray(),
                            persistentLocator = storedName
                        )
                    )
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    activeDocumentPickerDelegate = null
                }
            }

            activeDocumentPickerDelegate = delegate
            picker.delegate = delegate
            viewController.presentViewController(picker, true, null)
        }
    }
}

/** Dwie ksiazki o tej samej nazwie nie moga sie nadpisac - stad przyrostek, gdy nazwa zajeta. */
private fun uniqueFileName(fileName: String): String {
    val manager = NSFileManager.defaultManager
    val directory = booksDirectoryPath()
    if (!manager.fileExistsAtPath("$directory/$fileName")) return fileName

    val base = fileName.substringBeforeLast(".", fileName)
    val extension = fileName.substringAfterLast(".", "")
    val suffix = if (extension.isEmpty()) "" else ".$extension"
    var index = 2
    while (manager.fileExistsAtPath("$directory/$base ($index)$suffix")) index++
    return "$base ($index)$suffix"
}

/**
 * Okladka ladzie w tym samym katalogu co ksiazki i pod tym samym rodzajem locatora (sama nazwa
 * pliku), wiec odczytuje sie ja zwyklym [loadImportedBookBytes] - nie ma potrzeby drugiego
 * magazynu tylko dlatego, ze plik jest obrazkiem, a nie tekstem.
 */
@Composable
actual fun rememberCoverImagePicker(onPicked: (PickedFile) -> Unit): () -> Unit {
    val viewController = LocalUIViewController.current

    return remember(viewController) {
        {
            val picker = UIDocumentPickerViewController(
                documentTypes = listOf("public.image"),
                inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
            )

            val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentsAtURLs: List<*>
                ) {
                    activeDocumentPickerDelegate = null
                    val pickedPath = (didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.path ?: return
                    val data = NSData.dataWithContentsOfFile(pickedPath) ?: return

                    val fileName = pickedPath.substringAfterLast("/")
                    val storedName = uniqueFileName(fileName)
                    data.writeToFile(booksDirectoryPath() + "/" + storedName, true)

                    onPicked(
                        PickedFile(
                            name = fileName.substringBeforeLast("."),
                            bytes = data.toByteArray(),
                            persistentLocator = storedName
                        )
                    )
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    activeDocumentPickerDelegate = null
                }
            }

            activeDocumentPickerDelegate = delegate
            picker.delegate = delegate
            viewController.presentViewController(picker, true, null)
        }
    }
}

actual fun decodeImageBytes(bytes: ByteArray): ImageBitmap? =
    runCatching { Image.makeFromEncoded(bytes).toComposeImageBitmap() }.getOrNull()

actual suspend fun loadImportedBookBytes(locator: String): ByteArray? =
    NSData.dataWithContentsOfFile(bookPath(locator))?.toByteArray()
