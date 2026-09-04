package com.lernhero.domain.reading

/**
 * Tresc ksiazki w postaci niezaleznej od formatu zrodlowego. Parser TXT, EPUB czy czegokolwiek
 * innego produkuje ten sam model, wiec czytnik nie wie i nie musi wiedziec, skad tekst pochodzi.
 *
 * Celowo NIE ma tu stron - strona w trybie reflow nie istnieje az do momentu, w ktorym znamy
 * rozmiar ekranu i ustawienia typografii. Paginacja to sprawa warstwy UI.
 */
data class BookContent(
    val chapters: List<Chapter>
) {
    /** Cala ksiazka splaszczona do listy blokow - w takiej postaci czyta ja paginator. */
    val blocks: List<ContentBlock> = chapters.flatMap { chapter ->
        buildList {
            chapter.title?.let { add(ContentBlock.Heading(it)) }
            chapter.paragraphs.forEach { add(ContentBlock.Paragraph(it)) }
        }
    }

    /** Indeks pierwszego bloku kazdego rozdzialu - do spisu tresci i skokow. */
    val chapterStarts: List<Int> = buildList {
        var index = 0
        chapters.forEach { chapter ->
            add(index)
            index += (if (chapter.title != null) 1 else 0) + chapter.paragraphs.size
        }
    }

    val totalWords: Int = blocks.sumOf { TextStats.countWords(it.text) }
}

data class Chapter(
    val title: String?,
    val paragraphs: List<String>
)

/** Pojedynczy blok tekstu. Naglowek lamie strone przed soba, akapit nie. */
sealed interface ContentBlock {
    val text: String

    data class Heading(override val text: String) : ContentBlock
    data class Paragraph(override val text: String) : ContentBlock
}

/**
 * Miejsce w ksiazce zapisywane jako pozycja w tekscie, a NIE numer strony.
 *
 * W trybie reflow numer strony nie jest stabilny - przy innym rozmiarze czcionki "strona 42"
 * wskazuje zupelnie inny fragment. Kotwica znakowa przezywa zmiane ustawien, rotacje ekranu
 * i przelaczenie miedzy trybem reflow a oryginalem.
 */
data class ReadingAnchor(
    val blockIndex: Int = 0,
    val charOffset: Int = 0
) {
    companion object {
        val START = ReadingAnchor()
    }
}
