package com.lernhero.shared.audio

import android.media.AudioAttributes
import android.media.SoundPool
import com.lernhero.shared.notification.ReminderContext
import java.io.File

actual fun platformSoundPlayer(): SoundPlayer =
    ReminderContext.application?.let { AndroidSoundPlayer(it.cacheDir) } ?: NoSound

/**
 * SoundPool - do krotkich efektow w grach: kilka nakladajacych sie dzwiekow bez opoznien.
 * SoundPool czyta z pliku, wiec bajty z zasobow zapisujemy raz do cache aplikacji.
 */
private class AndroidSoundPlayer(private val cacheDir: File) : SoundPlayer {
    private val pool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val ids = mutableMapOf<Sfx, Int>()

    override fun load(sfx: Sfx, wav: ByteArray) {
        val file = File(cacheDir, "sfx_${sfx.file}.wav")
        if (!file.exists() || file.length() != wav.size.toLong()) file.writeBytes(wav)
        ids[sfx] = pool.load(file.absolutePath, 1)
    }

    override fun play(sfx: Sfx, volume: Float) {
        val id = ids[sfx] ?: return
        pool.play(id, volume, volume, 1, 0, 1f)
    }
}

internal object NoSound : SoundPlayer {
    override fun load(sfx: Sfx, wav: ByteArray) = Unit
    override fun play(sfx: Sfx, volume: Float) = Unit
}
