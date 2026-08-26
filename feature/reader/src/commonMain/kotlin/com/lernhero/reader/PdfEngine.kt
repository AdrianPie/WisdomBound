package com.lernhero.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/** Uchwyt do otwartego dokumentu PDF - nieprzezroczysty dla wspolnego kodu. */
expect class PdfDocumentHandle

/**
 * Renderowanie PDF - Android uzywa `android.graphics.pdf.PdfRenderer`, iOS uzywa PDFKit.
 * Obie platformy dostaja te sama porcje bajtow (bundlowana ksiazka albo wgrany plik), wiec
 * silnik nie musi wiedziec, skad plik pochodzi.
 */
expect object PdfEngine {
    /** Otwiera PDF z surowych bajtow. Null, gdy plik jest uszkodzony albo niepoprawny. */
    suspend fun open(bytes: ByteArray): PdfDocumentHandle?

    fun pageCount(document: PdfDocumentHandle): Int

    /** Renderuje strone jako bitmape o zadanej szerokosci; wysokosc wynika z proporcji strony. */
    suspend fun renderPage(document: PdfDocumentHandle, pageIndex: Int, widthPx: Int): ImageBitmap

    /**
     * Tekst strony, jesli platforma potrafi go wyciagnac. Android nie ma do tego wbudowanego
     * API (`PdfRenderer` tylko rysuje strony), wiec tam zawsze zwraca null - liczba slow
     * nie liczy sie wtedy dla PDF-ow. iOS ma `PDFPage.string` i liczy naprawde.
     */
    fun pageText(document: PdfDocumentHandle, pageIndex: Int): String?

    fun close(document: PdfDocumentHandle)
}

/** Plik wybrany przez uzytkownika - bajty do odczytu i trwaly identyfikator do zapisania w bibliotece. */
data class PickedFile(
    val name: String,
    val bytes: ByteArray,
    /** Pozwala platformie odzyskac dostep do pliku przy nastepnym uruchomieniu (URI/zakladka). */
    val persistentLocator: String
)

/**
 * Otwiera systemowy wybor pliku (Storage Access Framework na Androidzie,
 * UIDocumentPickerViewController na iOS). Zwraca funkcje, ktora na kliknieciu odpala wybor.
 */
@Composable
expect fun rememberBookFilePicker(onPicked: (PickedFile) -> Unit): () -> Unit

/** Odczytuje bajty wczesniej zaimportowanej ksiazki z zapisanego [locator]. */
expect suspend fun loadImportedBookBytes(locator: String): ByteArray?
