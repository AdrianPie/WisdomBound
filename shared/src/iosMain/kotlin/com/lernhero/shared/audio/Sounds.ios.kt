package com.lernhero.shared.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.create

actual fun platformSoundPlayer(): SoundPlayer = IosSoundPlayer()

/**
 * AVAudioPlayer na kazdy efekt. Kategoria Ambient: dzwieki gry nie przerywaja muzyki gracza
 * i milkna przy wyciszonym telefonie - tak jak oczekuje sie tego od gry.
 *
 * Na nakladanie sie tego samego efektu trzymamy kilka odtwarzaczy i gramy pierwszym wolnym.
 */
@OptIn(ExperimentalForeignApi::class)
private class IosSoundPlayer : SoundPlayer {
    private val players = mutableMapOf<Sfx, List<AVAudioPlayer>>()

    init {
        runCatching {
            AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryAmbient, null)
            AVAudioSession.sharedInstance().setActive(true, null)
        }
    }

    override fun load(sfx: Sfx, wav: ByteArray) {
        val data = wav.usePinned { NSData.create(bytes = it.addressOf(0), length = wav.size.toULong()) }
        players[sfx] = List(VOICES) {
            AVAudioPlayer(data = data, error = null).apply { prepareToPlay() }
        }
    }

    override fun play(sfx: Sfx, volume: Float) {
        val voices = players[sfx] ?: return
        val player = voices.firstOrNull { !it.playing } ?: voices.first()
        player.currentTime = 0.0
        player.volume = volume
        player.play()
    }

    private companion object {
        const val VOICES = 3
    }
}
