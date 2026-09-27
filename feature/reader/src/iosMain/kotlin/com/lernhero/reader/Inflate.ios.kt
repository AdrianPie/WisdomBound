@file:OptIn(ExperimentalForeignApi::class)

package com.lernhero.reader

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import platform.zlib.MAX_WBITS
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.Z_FINISH
import platform.zlib.ZLIB_VERSION
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2_
import platform.zlib.z_stream

/** zlib z systemu; ujemne windowBits = surowy deflate bez naglowka. */
actual fun inflateRaw(compressed: ByteArray, uncompressedSize: Int): ByteArray? {
    if (uncompressedSize == 0) return ByteArray(0)
    if (compressed.isEmpty()) return null
    val out = ByteArray(uncompressedSize)
    val ok = memScoped {
        val stream = alloc<z_stream>()
        compressed.usePinned { input ->
            out.usePinned { output ->
                stream.next_in = input.addressOf(0).reinterpret()
                stream.avail_in = compressed.size.toUInt()
                stream.next_out = output.addressOf(0).reinterpret()
                stream.avail_out = uncompressedSize.toUInt()
                if (inflateInit2_(stream.ptr, -MAX_WBITS, ZLIB_VERSION, sizeOf<z_stream>().toInt()) != Z_OK) {
                    return@usePinned false
                }
                val result = inflate(stream.ptr, Z_FINISH)
                inflateEnd(stream.ptr)
                result == Z_STREAM_END && stream.total_out.toInt() == uncompressedSize
            }
        }
    }
    return if (ok) out else null
}
