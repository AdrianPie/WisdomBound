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
    /** Czysty tekst - jedyny format, ktory dzis potrafimy pokazac w trybie reflow. */
    Txt,
    Epub,
    Pdf;

    /**
     * Czy format da sie przelac przez [BookContent] i pokazac jako prawdziwy tekst.
     * Tylko takie ksiazki maja tlumaczenie slow i uczciwy licznik slow - PDF zostaje
     * bitmapa, wiec nie ma w nim czego dotknac ani czego policzyc.
     */
    val supportsReflow: Boolean
        get() = this != Pdf

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
 */
@Serializable
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val format: BookFormat,
    val source: BookSource,
    val locator: String
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
            author = "Tekst przykladowy",
            format = BookFormat.Txt,
            source = BookSource.Bundled,
            locator = "files/books/sample_reflow.txt"
        ),
        Book(
            id = "bundled_sample_tales",
            title = "Przykladowe opowiadania",
            author = "Domena publiczna (przykladowy tekst)",
            format = BookFormat.Pdf,
            source = BookSource.Bundled,
            locator = "files/books/sample_tales.pdf"
        )
    )

    fun byId(id: String): Book? = ALL.firstOrNull { it.id == id }
}
