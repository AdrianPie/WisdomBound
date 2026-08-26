package com.lernhero.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Desktop nie ma na razie czytnika PDF - podobnie jak fitness, to platforma drugorzedna dla tej
 * gry (mobile-first). Zwraca puste wyniki zamiast udawac, ze cokolwiek dziala.
 */
actual class PdfDocumentHandle

actual object PdfEngine {
    actual suspend fun open(bytes: ByteArray): PdfDocumentHandle? = null
    actual fun pageCount(document: PdfDocumentHandle): Int = 0
    actual suspend fun renderPage(document: PdfDocumentHandle, pageIndex: Int, widthPx: Int): ImageBitmap =
        ImageBitmap(width = widthPx.coerceAtLeast(1), height = 1)
    actual fun pageText(document: PdfDocumentHandle, pageIndex: Int): String? = null
    actual fun close(document: PdfDocumentHandle) = Unit
}

@Composable
actual fun rememberBookFilePicker(onPicked: (PickedFile) -> Unit): () -> Unit = { }

actual suspend fun loadImportedBookBytes(locator: String): ByteArray? = null
