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
    Pdf
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
