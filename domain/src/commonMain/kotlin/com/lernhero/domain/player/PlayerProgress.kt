package com.lernhero.domain.player

import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Cztery statystyki rozdawane po awansie - klasyczny zestaw RPG, ale kazda z nich musi realnie
 * zmieniac walke, ktora juz masz, a nie byc ozdobna liczba na karcie postaci:
 *
 * - [Strength] podbija atak calej druzyny (mnozy sie z ekwipunkiem),
 * - [Vitality] podbija maks. HP bohatera i kompanow,
 * - [Focus] wzmacnia mnoznik obrazen z quizu - czyli oplaca sie tym, ktorzy odpowiadaja dobrze,
 * - [Luck] daje szanse na trafienie krytyczne i lepsze losowania nagrod.
 *
 * Swiadomie NIE ma tu "obrony" ani "szybkosci": walka jest turowa i sterowana quizem, wiec te
 * statystyki nie mialyby czego dotknac - byłyby dokladnie tym, przed czym ostrzega audyt.
 */
@Serializable
enum class HeroStat(val label: String, val short: String, val description: String) {
    Strength("Sila", "SIL", "+2 ataku druzyny za punkt."),
    Vitality("Wytrzymalosc", "WYT", "+8 maks. HP druzyny za punkt."),
    Focus("Skupienie", "SKU", "+2% obrazen z dobrej odpowiedzi za punkt."),
    Luck("Szczescie", "SZC", "+0,6% szansy na cios krytyczny za punkt.")
}

/**
 * Rozdane punkty statystyk. Osobny typ zamiast mapy, zeby zapis w Firestore mial staly ksztalt
 * i zeby dodanie piatej statystyki bylo swiadoma zmiana modelu, a nie przypadkowym kluczem.
 */
@Serializable
data class HeroStats(
    val strength: Int = 0,
    val vitality: Int = 0,
    val focus: Int = 0,
    val luck: Int = 0
) {
    operator fun get(stat: HeroStat): Int = when (stat) {
        HeroStat.Strength -> strength
        HeroStat.Vitality -> vitality
        HeroStat.Focus -> focus
        HeroStat.Luck -> luck
    }

    fun plus(stat: HeroStat, amount: Int = 1): HeroStats = when (stat) {
        HeroStat.Strength -> copy(strength = strength + amount)
        HeroStat.Vitality -> copy(vitality = vitality + amount)
        HeroStat.Focus -> copy(focus = focus + amount)
        HeroStat.Luck -> copy(luck = luck + amount)
    }

    val total: Int get() = strength + vitality + focus + luck

    /** Mnoznik ataku druzyny z [HeroStat.Strength] - liczony wzgledem bazy, nie dodawany na sztywno. */
    val attackBonus: Int get() = strength * ATTACK_PER_POINT

    /** Dodatkowe maks. HP z [HeroStat.Vitality]. */
    val maxHpBonus: Int get() = vitality * MAX_HP_PER_POINT

    /** Mnoznik obrazen za dobra odpowiedz z [HeroStat.Focus] (1.0 = bez bonusu). */
    val quizDamageMultiplier: Float get() = 1f + focus * QUIZ_DAMAGE_PER_POINT

    /** Dodatkowa szansa na cios krytyczny z [HeroStat.Luck]. */
    val critChanceBonus: Float get() = luck * CRIT_PER_POINT

    companion object {
        const val ATTACK_PER_POINT = 2
        const val MAX_HP_PER_POINT = 8
        const val QUIZ_DAMAGE_PER_POINT = 0.02f
        const val CRIT_PER_POINT = 0.006f
    }
}

/**
 * Poziom bohatera i nierozdane punkty. Poziom jest JEDYNA osia rozwoju, ktora nie wymaga wydawania
 * waluty - dzieki temu kazda walka posuwa postac do przodu, nawet gdy gracz nie ma na co wydac zlota.
 *
 * [unspentPoints] to punkty czekajace na rozdanie; [stats] to te juz rozdane. Trzymamy jedno i drugie,
 * bo inaczej po zmianie krzywej awansu nie dalo by sie odtworzyc, ile gracz faktycznie dostal.
 */
@Serializable
data class PlayerProgress(
    val level: Int = 1,
    val xp: Int = 0,
    val unspentPoints: Int = 0,
    val stats: HeroStats = HeroStats()
) {
    /** Ile XP trzeba uzbierac na kolejny poziom (nie licznik od zera - patrz [LevelCurve]). */
    val xpForNextLevel: Int get() = LevelCurve.xpForLevel(level + 1)

    val xpProgress: Float
        get() = if (xpForNextLevel <= 0) 1f else (xp.toFloat() / xpForNextLevel).coerceIn(0f, 1f)
}

/** Wynik doliczenia XP - ile poziomow wskoczylo i ile punktow z tego wpadlo. */
data class LevelUpResult(
    val progress: PlayerProgress,
    val levelsGained: Int,
    val pointsGained: Int
) {
    val leveledUp: Boolean get() = levelsGained > 0
}

/**
 * Krzywa awansu. Wykladnicza, ale lagodna: pierwsze poziomy lecą po jednej-dwoch walkach (gracz od
 * razu czuje, ze cos sie dzieje), a dalsze wymagaja juz serii dobrych zejsc. Bez sufitu - endless
 * ma sens tylko wtedy, gdy zawsze jest kolejny poziom do zdobycia.
 */
object LevelCurve {

    /** Ile punktow statystyk daje jeden awans. */
    const val POINTS_PER_LEVEL = 3

    private const val BASE_XP = 120f
    private const val GROWTH = 1.18f

    /** XP potrzebne, zeby przejsc Z poziomu [level] - 1 NA poziom [level]. */
    fun xpForLevel(level: Int): Int =
        if (level <= 1) 0 else (BASE_XP * GROWTH.pow(level - 2)).roundToInt()

    /**
     * Dolicza [amount] XP i rozlicza wszystkie awanse, ktore z tego wyszly (jedna walka moze dac
     * wiecej niz jeden poziom - i ma prawo, inaczej gracz wracajacy po przerwie utknalby w petli
     * "awans, ekran, awans, ekran").
     */
    fun award(progress: PlayerProgress, amount: Int): LevelUpResult {
        if (amount <= 0) return LevelUpResult(progress, levelsGained = 0, pointsGained = 0)

        var level = progress.level
        var xp = progress.xp + amount
        var levels = 0

        while (xp >= xpForLevel(level + 1)) {
            xp -= xpForLevel(level + 1)
            level += 1
            levels += 1
            // Bezpiecznik na wypadek absurdalnego wejscia (np. bledu w nagrodach) - nie zapetlamy sie.
            if (levels > 500) break
        }

        val points = levels * POINTS_PER_LEVEL
        return LevelUpResult(
            progress = progress.copy(
                level = level,
                xp = xp,
                unspentPoints = progress.unspentPoints + points
            ),
            levelsGained = levels,
            pointsGained = points
        )
    }

    /** Rozdaje jeden punkt. Zwraca null, gdy nie ma czego rozdawac - UI nie musi tego sprawdzac. */
    fun spendPoint(progress: PlayerProgress, stat: HeroStat): PlayerProgress? {
        if (progress.unspentPoints <= 0) return null
        return progress.copy(
            unspentPoints = progress.unspentPoints - 1,
            stats = progress.stats.plus(stat)
        )
    }
}
