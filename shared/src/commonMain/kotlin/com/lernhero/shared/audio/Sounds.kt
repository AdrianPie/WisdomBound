package com.lernhero.shared.audio

import com.lernhero.shared.resources.Resources
import com.lernhero.shared.resources.readFileBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

/**
 * Efekty dzwiekowe gry: darmowe paczki Kenney (CC0), zlozone skryptem `tools/sfx/build_sfx.py`
 * do `composeResources/files/sfx/<file>.wav`.
 *
 * [volume] wyrownuje odczuwalna glosnosc: skrypt normalizuje kazdy plik do tego samego szczytu
 * (-6 dBFS), a klik przycisku przy tej samej wartosci co fanfara bylby o wiele za glosny.
 */
enum class Sfx(val file: String, val volume: Float) {
    Tap("tap", 0.5f),
    Correct("correct", 0.85f),
    Wrong("wrong", 0.75f),
    Hit("hit", 0.75f),
    Crit("crit", 0.9f),
    Hurt("hurt", 0.7f),
    Whoosh("whoosh", 0.55f),
    Victory("victory", 1.0f),
    Defeat("defeat", 0.9f),
    LevelUp("level_up", 1.0f),
    Chest("chest", 0.9f),
    Coins("coins", 0.8f),
    Purchase("purchase", 0.9f),
    Upgrade("upgrade", 0.9f),
    Summon("summon", 1.0f),
    /** Rytual summona - pochodne z `tools/sfx/derive_summon_sfx.py`. */
    SummonCharge("summon_charge", 0.95f),
    SummonTier("summon_tier", 0.8f),
    SummonBoom("summon_boom", 1.0f),
    CardFlip("card_flip", 0.6f),
    Heal("heal", 0.7f),
    Wave("wave", 0.7f),
    Boss("boss", 1.0f)
}

/** Platformowy odtwarzacz: laduje surowe bajty WAV raz, potem gra je bez opoznien. */
interface SoundPlayer {
    fun load(sfx: Sfx, wav: ByteArray)
    fun play(sfx: Sfx, volume: Float)
}

expect fun platformSoundPlayer(): SoundPlayer

/**
 * Jedno wejscie do dzwiekow dla calej gry: `Sounds.play(Sfx.Hit)` z viewmodelu albo z UI.
 *
 * - [isEnabled] i [masterVolume] czyta z ustawien (podpina je aplikacja przy starcie), wiec
 *   wylaczenie dzwieku w Ustawieniach dziala od razu, bez restartu.
 * - Ten sam efekt odpalony kilka razy w [MIN_GAP_MS] gra RAZ - salwa pieciu kompanow nie ma
 *   brzmiec jak karabin, tylko jak jedno mocne uderzenie.
 */
object Sounds {
    var isEnabled: () -> Boolean = { true }
    var masterVolume: () -> Float = { 1f }

    private val player: SoundPlayer by lazy { platformSoundPlayer() }
    private val lastPlayed = mutableMapOf<Sfx, TimeSource.Monotonic.ValueTimeMark>()
    private var loaded = false

    /** Laduje wszystkie efekty w tle. Wolac raz, przy starcie aplikacji. */
    fun preload() {
        if (loaded) return
        loaded = true
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            Sfx.entries.forEach { sfx ->
                runCatching { player.load(sfx, Resources.readFileBytes("files/sfx/${sfx.file}.wav")) }
            }
        }
    }

    fun play(sfx: Sfx) {
        if (!isEnabled()) return
        val now = TimeSource.Monotonic.markNow()
        val last = lastPlayed[sfx]
        if (last != null && (now - last).inWholeMilliseconds < MIN_GAP_MS) return
        lastPlayed[sfx] = now
        runCatching { player.play(sfx, (sfx.volume * masterVolume()).coerceIn(0f, 1f)) }
    }

    private const val MIN_GAP_MS = 60L
}
