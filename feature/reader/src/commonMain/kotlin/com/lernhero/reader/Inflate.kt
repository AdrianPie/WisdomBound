package com.lernhero.reader

/**
 * Dekompresja surowego deflate (bez naglowka zlib) - tak spakowane sa pliki w zipie, a wiec i w
 * EPUB-ie. Kazda platforma ma gotowy dekompresor, wiec wspolny kod tylko go wola.
 * Null, gdy dane sa uszkodzone.
 */
expect fun inflateRaw(compressed: ByteArray, uncompressedSize: Int): ByteArray?
