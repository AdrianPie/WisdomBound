package com.lernhero.domain.reading

/**
 * Liczenie slow i znakow - tak jak robi to Word/Google Docs: slowo to token oddzielony bialymi
 * znakami (spacja, tabulacja, nowa linia), nie "prawdziwe slowo jezykowe". To standardowa,
 * przewidywalna metoda, ktora dziala tak samo bez wzgledu na jezyk tekstu.
 */
object TextStats {

    private val WHITESPACE = Regex("\\s+")

    fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split(WHITESPACE).count { it.isNotBlank() }
    }

    /** Znaki bez bialych znakow - druga standardowa miara obok liczby slow. */
    fun countCharacters(text: String): Int = text.count { !it.isWhitespace() }
}
