package com.lernhero.game.battle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Tempo odtwarzania walki - jedno pokretlo na wszystkie opoznienia.
 *
 * Walka jest w calosci policzona z gory (patrz BattlePlaybackEngine), a to, co widzi gracz, to
 * tylko odtwarzanie gotowego skryptu. Dlatego przyspieszenie jest bezpieczne: skalujemy wylacznie
 * czasy animacji i przerwy miedzy krokami, a nie sama logike - wynik walki jest identyczny przy
 * kazdej predkosci.
 *
 * Kazde miejsce, ktore czeka w trakcie walki, przepuszcza swoj czas przez [scale]. Nowa animacja
 * dopisana bez tego bedzie jedynym hamulcem w calym ekranie - i od razu to widac.
 */
object BattleSpeed {

    /** Predkosci dostepne z HUD-u. */
    val options: List<Float> = listOf(1f, 2f, 4f, 8f, 16f)

    /** Walka startuje w normalnym tempie, zeby bylo widac animacje. Przyspieszasz recznie w HUD. */
    const val DEFAULT: Float = 1f

    var multiplier: Float by mutableStateOf(DEFAULT)

    val label: String get() = "${multiplier.toInt()}x"

    /** Skraca czas trwania animacji lub przerwy. Nigdy nie schodzi do zera. */
    fun scale(millis: Long): Long = (millis / multiplier).toLong().coerceAtLeast(1L)

    fun scale(millis: Int): Int = (millis / multiplier).toInt().coerceAtLeast(1)

    val canSpeedUp: Boolean get() = multiplier < options.last()
    val canSlowDown: Boolean get() = multiplier > options.first()

    /** O jeden szczebel szybciej. Na maksie nic nie robi. */
    fun faster() {
        val index = options.indexOf(multiplier)
        if (index in 0 until options.lastIndex) multiplier = options[index + 1]
    }

    /** O jeden szczebel wolniej. Na 1x nic nie robi. */
    fun slower() {
        val index = options.indexOf(multiplier)
        if (index > 0) multiplier = options[index - 1]
    }
}
