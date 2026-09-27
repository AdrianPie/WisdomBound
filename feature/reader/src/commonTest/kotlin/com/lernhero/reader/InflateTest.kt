package com.lernhero.reader

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Dekompresor platformowy - ten sam test leci na JVM i na iOS (zlib). */
class InflateTest {
    private val compressed = byteArrayOf(-13,-55,44,41,-49,87,84,-16,-49,74,-82,-86,-84,-54,-53,87,-56,-51,-49,74,84,84,-16,25,21,29,-28,-94,0)

    @Test
    fun inflatesRawDeflate() {
        assertEquals("Litwo! Ojczyzno moja! ".repeat(20), inflateRaw(compressed, 440)?.decodeToString())
    }

    @Test
    fun corruptDataGivesNull() {
        assertNull(inflateRaw(byteArrayOf(1, 2, 3, 4), 100))
    }
}
