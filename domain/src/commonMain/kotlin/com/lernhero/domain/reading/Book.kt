package com.lernhero.domain.reading

import kotlinx.serialization.Serializable

/** Skad pochodzi plik ksiazki - decyduje, jak warstwa platformowa dobierze sie do bajtow. */
@Serializable
enum class BookSource {
    /** Zaszyta w zasobach aplikacji (composeResources/files/books/...). */
    Bundled,
    /** Wgrana przez gracza - [Book.locator] to identyfikator pliku wedlug platformy
     * (na Androidzie zapisany URI z trwalym uprawnieniem, na iOS dane zakladki bezpieczenstwa). */
    Imported
}

@Serializable
enum class BookFormat {
    Txt,
    /** Rozpakowanie zipa dochodzi pozniej - dzis zawsze konczy sie podgladem oryginalu. */
    Epub,
    /**
     * Tekst wyciagany PO IMPORCIE, na biezaco (PDFBox na Androidzie, PDFKit na iOS) - nie
     * kazdy PDF ma warstwe tekstowa (np. zeskanowana ksiazka bez OCR), wiec proba moze sie
     * nie udac. Wtedy ksiazka i tak zostaje otwarta, tylko w podgladzie oryginalu.
     */
    Pdf;

    companion object {
        /** Rozpoznanie po nazwie pliku - to wszystko, czym dysponujemy przy imporcie. */
        fun ofFileName(name: String): BookFormat = when {
            name.endsWith(".txt", ignoreCase = true) -> Txt
            name.endsWith(".epub", ignoreCase = true) -> Epub
            else -> Pdf
        }
    }
}

/**
 * Ksiazka w bibliotece gracza. [locator] jest nieprzezroczysty dla domeny - to warstwa
 * platformowa wie, czy to sciezka zasobu, czy zakodowany URI/zakladka do pliku uzytkownika.
 *
 * [coverLocator] dziala dokladnie tak samo, tylko wskazuje na obrazek okladki wgrany przez
 * gracza. Null = brak okladki, biblioteka rysuje wtedy zastepczy grzbiet z tytulem.
 */
@Serializable
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val format: BookFormat,
    val source: BookSource,
    val locator: String,
    val coverLocator: String? = null
)

/**
 * Kilka pozycji z wygasla licencja, gotowych do czytania od razu - bez importu wlasnego pliku.
 * Podmien pliki w `shared/src/commonMain/composeResources/files/books/` na prawdziwe teksty
 * z domeny publicznej (np. Wolne Lektury, Project Gutenberg), gdy tylko je przygotujesz.
 */
object BundledBooks {
    val ALL: List<Book> = listOf(
        Book(
            id = "bundled_lantern_keeper",
            title = "The Lantern Keeper",
            author = "LernHero Originals",
            format = BookFormat.Txt,
            source = BookSource.Bundled,
            locator = "files/books/sample_reflow.txt"
        ),
        Book(
            id = "bundled_sample_tales",
            title = "Sample Tales",
            author = "Public domain",
            format = BookFormat.Pdf,
            source = BookSource.Bundled,
            locator = "files/books/sample_tales.pdf"
        )
    )

    fun byId(id: String): Book? = ALL.firstOrNull { it.id == id }
}
