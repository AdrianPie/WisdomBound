package com.lernhero.game.battle.fx

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.lernhero.domain.monster.Element
import com.lernhero.game.battle.BattleSpeed
import kotlin.time.TimeSource

/** Rodzaj efektu w miejscu (trafienie, leczenie, kurz...) - kazdy ma wlasny rysunek w [VfxLayer]. */
enum class ImpactType(val baseMs: Int) {
    /** Ciecie mieczem/pazurem - lukowate smugi i iskry w kierunku ciosu. */
    Slash(420),
    /** Strzala wbija sie w cel - drzazgi i kurz. */
    Pierce(360),
    /** Wybuch magicznej kuli w kolorze zywiolu. */
    Burst(560),
    /** Olbrzym uderza w ziemie - fala po podlozu, pekniecia, kurz. */
    Smash(620),
    Heal(900),
    Shield(900),
    Block(460),
    Dodge(420),
    Thorns(460),
    /** Ulti: slup swiatla z nieba prosto w cel. */
    SkyStrike(760),
    Poison(820),
    Death(1100),
    /** Kurz spod stop w biegu. */
    Dust(520),
    /** Ladowanie na miejscu po wejsciu na arene. */
    Landing(620),
    /** Boss spada z nieba - wielka fala i gruz. */
    BossLanding(1100)
}

/** Jeden zywy efekt. [startNs] ustawia silnik; [progress] 0..1 liczy sie z czasu, nie z klatek. */
sealed class Vfx(val id: Long, val startNs: Long, val durationNs: Long) {
    fun progress(nowNs: Long): Float = ((nowNs - startNs).toFloat() / durationNs).coerceIn(0f, 1f)
}

class ProjectileVfx(
    id: Long,
    startNs: Long,
    durationNs: Long,
    val kind: ProjectileKind,
    val from: Offset,
    val to: Offset,
    val tint: Color,
    val element: Element?
) : Vfx(id, startNs, durationNs)

class ImpactVfx(
    id: Long,
    startNs: Long,
    durationNs: Long,
    val type: ImpactType,
    val at: Offset,
    val tint: Color,
    /** Kierunek ciosu: +1 = w prawo (bije nasz), -1 = w lewo (bije wrog). */
    val dir: Float,
    /** Wielkosc wzgledem zwyklej postaci - olbrzym i krytyk dostaja wiecej. */
    val scale: Float,
    val crit: Boolean,
    val element: Element?
) : Vfx(id, startNs, durationNs)

/**
 * Silnik efektow walki: lista zywych efektow i zegar. Rysuje je [VfxLayer] - jeden Canvas na cala
 * arene, wiec nawet kilkanascie wybuchow naraz to garsc wywolan rysowania, a nie kilkanascie
 * komponentow z wlasnymi animacjami.
 *
 * Czas plynie z zegara monotonicznego, nie z klatek: efekt odpalony w polowie klatki startuje
 * dokladnie wtedy, kiedy trzeba, a spozniona klatka nie spowalnia wybuchu. Wszystkie czasy
 * przechodza przez [BattleSpeed], wiec na 4x/8x efekty sa odpowiednio krotsze.
 */
@Stable
class VfxEngine {
    private var nextId = 0L
    internal val active = mutableStateListOf<Vfx>()

    /** Zegar czytany przy rysowaniu - pisze go petla klatek w [VfxLayer], tylko gdy cos zyje. */
    internal var nowNs by mutableLongStateOf(0L)

    /**
     * Zegar EFEKTOW - biegnie jak zegar prawdziwy, dopoki [slowMotion] go nie przyhamuje. Efekty
     * startuja i koncza sie wedlug niego, wiec w zwolnieniu wybuch trwa dluzej, a nie przeskakuje.
     */
    private var virtualNs = 0L
    private var lastRealNs = monoNow()
    private var slowScale = 1f
    private var slowHoldEndNs = 0L
    private var slowEndNs = 0L

    private fun timeScaleAt(realNs: Long): Float = when {
        realNs < slowHoldEndNs -> slowScale
        realNs < slowEndNs -> slowScale + (1f - slowScale) * ((realNs - slowHoldEndNs).toFloat() / (slowEndNs - slowHoldEndNs))
        else -> 1f
    }

    private fun clock(): Long {
        val real = monoNow()
        virtualNs += ((real - lastRealNs) * timeScaleAt(real)).toLong()
        lastRealNs = real
        return virtualNs
    }

    /**
     * Zwolnienie czasu efektow ("bullet time") - na [holdMs] czas plynie z predkoscia [scale],
     * potem przez [recoverMs] wraca plynnie do normy. Czasy podaje wolajacy, juz przepuszczone
     * przez [BattleSpeed] - na 8x zwolnienie tez ma byc krotsze.
     */
    fun slowMotion(scale: Float, holdMs: Int, recoverMs: Int) {
        clock() // dotychczasowy czas liczy sie jeszcze po staremu
        val now = monoNow()
        slowScale = scale.coerceIn(0.05f, 1f)
        slowHoldEndNs = now + holdMs * 1_000_000L
        slowEndNs = slowHoldEndNs + recoverMs.coerceAtLeast(1) * 1_000_000L
    }

    fun projectile(kind: ProjectileKind, from: Offset, to: Offset, durationMs: Int, element: Element?) {
        active += ProjectileVfx(
            id = nextId++,
            startNs = clock(),
            durationNs = durationMs.toLong() * 1_000_000L,
            kind = kind,
            from = from,
            to = to,
            tint = projectileTint(kind, element),
            element = element
        )
    }

    fun impact(
        type: ImpactType,
        at: Offset,
        tint: Color,
        dir: Float = 1f,
        scale: Float = 1f,
        crit: Boolean = false,
        element: Element? = null,
        durationMs: Int = type.baseMs
    ) {
        active += ImpactVfx(
            id = nextId++,
            startNs = clock(),
            durationNs = BattleSpeed.scale(durationMs).toLong() * 1_000_000L,
            type = type,
            at = at,
            tint = tint,
            dir = dir,
            scale = scale,
            crit = crit,
            element = element
        )
    }

    /** Wolane co klatke przez [VfxLayer]: przesuwa zegar i sprzata efekty, ktore sie skonczyly. */
    internal fun onFrame() {
        val now = clock()
        nowNs = now
        if (active.any { now - it.startNs >= it.durationNs }) {
            active.removeAll { now - it.startNs >= it.durationNs }
        }
    }

    companion object {
        private val origin = TimeSource.Monotonic.markNow()
        fun monoNow(): Long = origin.elapsedNow().inWholeNanoseconds
    }
}

/** Kolor swiatla zywiolu w efektach - jasniejszy i bardziej nasycony niz kolor odznaki. */
fun Element?.vfxColor(): Color = when (this) {
    Element.Fire -> Color(0xFFFF7A1A)
    Element.Water -> Color(0xFF3FA9FF)
    Element.Nature -> Color(0xFF5BD95E)
    Element.Death -> Color(0xFFA35CFF)
    null -> Color(0xFFFFE08A)
}

private fun projectileTint(kind: ProjectileKind, element: Element?): Color = when (kind) {
    ProjectileKind.Fireball -> Color(0xFFFF7A1A)
    ProjectileKind.PoisonGlob -> Color(0xFF8BE03A)
    ProjectileKind.Arcane -> Color(0xFFB98CFF)
    ProjectileKind.Arrow -> element?.vfxColor() ?: Color(0xFFFFF4D6)
    ProjectileKind.SlashWave -> element?.vfxColor() ?: Color(0xFFFFE08A)
    ProjectileKind.GroundWave -> element?.vfxColor() ?: Color(0xFFFFB070)
    ProjectileKind.Bolt -> element.vfxColor()
}
