package com.lernhero.domain.reading

/**
 * Minimalny czytnik archiwum ZIP - tylko tyle, ile potrzeba do EPUB-a: spis plikow z katalogu
 * centralnego i wyjecie jednego pliku.
 *
 * Dekompresja (deflate) NIE jest tu zaimplementowana - wchodzi z zewnatrz jako [inflate], bo
 * kazda platforma ma ja gotowa (java.util.zip na JVM, zlib na iOS). Pliki zapisane bez kompresji
 * (metoda 0 - np. `mimetype` w kazdym EPUB-ie) czytamy bezposrednio.
 */
class ZipArchive(
    private val bytes: ByteArray,
    private val inflate: (compressed: ByteArray, uncompressedSize: Int) -> ByteArray?
) {
    private class Entry(val method: Int, val compressedSize: Int, val uncompressedSize: Int, val localHeaderOffset: Int)

    private val entries: Map<String, Entry> = readCentralDirectory()

    val names: Set<String> get() = entries.keys

    fun read(name: String): ByteArray? {
        val entry = entries[name] ?: return null
        val offset = entry.localHeaderOffset
        if (u32(offset) != LOCAL_HEADER_SIG) return null
        // Dlugosci nazwy i "extra" w naglowku lokalnym potrafia sie roznic od tych z katalogu
        // centralnego, wiec poczatek danych liczymy z naglowka lokalnego.
        val dataStart = offset + 30 + u16(offset + 26) + u16(offset + 28)
        val dataEnd = dataStart + entry.compressedSize
        if (dataEnd > bytes.size) return null
        val data = bytes.copyOfRange(dataStart, dataEnd)
        return when (entry.method) {
            METHOD_STORED -> data
            METHOD_DEFLATED -> inflate(data, entry.uncompressedSize)
            else -> null
        }
    }

    fun readText(name: String): String? = read(name)?.decodeToString()

    private fun readCentralDirectory(): Map<String, Entry> {
        val eocd = findEndOfCentralDirectory() ?: return emptyMap()
        val count = u16(eocd + 10)
        var pos = u32(eocd + 16).toInt()
        val result = LinkedHashMap<String, Entry>(count)
        repeat(count) {
            if (pos + 46 > bytes.size || u32(pos) != CENTRAL_HEADER_SIG) return result
            val nameLength = u16(pos + 28)
            val extraLength = u16(pos + 30)
            val commentLength = u16(pos + 32)
            val name = bytes.copyOfRange(pos + 46, pos + 46 + nameLength).decodeToString()
            result[name] = Entry(
                method = u16(pos + 10),
                compressedSize = u32(pos + 20).toInt(),
                uncompressedSize = u32(pos + 24).toInt(),
                localHeaderOffset = u32(pos + 42).toInt()
            )
            pos += 46 + nameLength + extraLength + commentLength
        }
        return result
    }

    /** Rekord konca katalogu stoi na koncu pliku, ewentualnie za nim jest komentarz (max 64 KB). */
    private fun findEndOfCentralDirectory(): Int? {
        val lowest = maxOf(0, bytes.size - 22 - 0xFFFF)
        for (pos in bytes.size - 22 downTo lowest) {
            if (u32(pos) == EOCD_SIG) return pos
        }
        return null
    }

    private fun u16(pos: Int): Int =
        (bytes[pos].toInt() and 0xFF) or ((bytes[pos + 1].toInt() and 0xFF) shl 8)

    private fun u32(pos: Int): Long =
        (u16(pos).toLong()) or (u16(pos + 2).toLong() shl 16)

    private companion object {
        const val LOCAL_HEADER_SIG = 0x04034b50L
        const val CENTRAL_HEADER_SIG = 0x02014b50L
        const val EOCD_SIG = 0x06054b50L
        const val METHOD_STORED = 0
        const val METHOD_DEFLATED = 8
    }
}
