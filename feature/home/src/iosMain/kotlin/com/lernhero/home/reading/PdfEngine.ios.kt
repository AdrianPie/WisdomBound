@file:OptIn(ExperimentalForeignApi::class)

package com.lernhero.home.reading

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

@Composable
actual fun rememberBookFilePicker(onPicked: (PickedFile) -> Unit): () -> Unit {
    val viewController = LocalUIViewController.current

    return remember(viewController) {
        {
            val picker = UIDocumentPickerViewController(
                documentTypes = listOf("com.adobe.pdf"),
                // Import kopiuje plik do sandboxa naszej apki - nie potrzeba zakladek bezpieczenstwa,
                // dostep zostaje trwaly sam z siebie.
                inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
            )

            val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentsAtURLs: List<*>
                ) {
                    activeDocumentPickerDelegate = null
                    val path = (didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.path ?: return
                    val data = NSData.dataWithContentsOfFile(path) ?: return
                    val name = path.substringAfterLast("/").substringBeforeLast(".")
                    onPicked(PickedFile(name = name, bytes = data.toByteArray(), persistentLocator = path))
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

actual suspend fun loadImportedBookBytes(locator: String): ByteArray? =
    NSData.dataWithContentsOfFile(locator)?.toByteArray()
