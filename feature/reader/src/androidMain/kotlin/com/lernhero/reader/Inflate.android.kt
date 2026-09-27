package com.lernhero.reader

import java.util.zip.Inflater

actual fun inflateRaw(compressed: ByteArray, uncompressedSize: Int): ByteArray? {
    val inflater = Inflater(true)
    return try {
        inflater.setInput(compressed)
        val out = ByteArray(uncompressedSize)
        var written = 0
        while (written < uncompressedSize && !inflater.finished()) {
            val n = inflater.inflate(out, written, uncompressedSize - written)
            if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) break
            written += n
        }
        if (written == uncompressedSize) out else null
    } catch (e: Exception) {
        null
    } finally {
        inflater.end()
    }
}
