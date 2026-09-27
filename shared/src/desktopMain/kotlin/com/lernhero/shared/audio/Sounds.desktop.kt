package com.lernhero.shared.audio

/** Desktop to tylko podglad deweloperski - bez dzwieku. */
actual fun platformSoundPlayer(): SoundPlayer = object : SoundPlayer {
    override fun load(sfx: Sfx, wav: ByteArray) = Unit
    override fun play(sfx: Sfx, volume: Float) = Unit
}
