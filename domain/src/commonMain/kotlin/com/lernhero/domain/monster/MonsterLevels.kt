package com.lernhero.domain.monster

import com.lernhero.domain.common.Rarity
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Poziomy kompanow - kupowane za ZLOTO, nie za doswiadczenie.
 *
 * To jest odpowiedz na dziure, ktora audyt nazwal wprost: zloto nie mialo zadnego stalego odplywu.
 * Po wykupieniu katalogu waluta przestawala miec sens, a razem z nia nagrody z walk, celow dziennych
 * i skrzyn. Poziomy kompanow sa sinkiem BEZ SUFITU tresci: nie trzeba dorysowywac nowych potworow,
 * zeby bylo na co wydawac.
 *
 * Poziom trzymany jest per KARTA (wszystkie twoje szkielety-wojownicy dziela poziom), a nie per
 * sztuka. Duplikaty nie sa wiec kopiami do postawienia obok siebie, tylko paliwem awansu -
 * patrz [MonsterStars].
 *
 * UWAGA: [MAX_LEVEL] to sufit ABSOLUTNY calego systemu. Realny sufit dla konkretnego kompana
 * ustawiaja jego gwiazdki ([MonsterStars.levelCapFor]) - to wlasnie po to sa awanse.
 */
object MonsterLevels {

    /** Poziom startowy kazdego swiezo przyzwanego kompana. */
    const val MIN_LEVEL = 1

    /** Sufit. Doniesienie go wyzej to zmiana jednej liczby - krzywa jest ciagla. */
    const val MAX_LEVEL = 50

    private const val HP_PER_LEVEL = 0.06f
    private const val ATTACK_PER_LEVEL = 0.05f

    /** Rosnie wykladniczo, wiec ostatnie poziomy sa realnym celem na dlugo, a nie formalnoscia. */
    private const val COST_GROWTH = 1.12f

    fun clamp(level: Int): Int = level.coerceIn(MIN_LEVEL, MAX_LEVEL)

    /** Poziom przyciety do sufitu wynikajacego z gwiazdek - to ten limit obowiazuje w praktyce. */
    fun clampToStars(level: Int, stars: Int): Int =
        level.coerceIn(MIN_LEVEL, MonsterStars.levelCapFor(stars))

    /** Czy kompan dobil do sufitu SWOJEJ gwiazdki (a nie do absolutnego [MAX_LEVEL]). */
    fun isAtStarCap(level: Int, stars: Int): Boolean =
        clamp(level) >= MonsterStars.levelCapFor(stars)

    fun hpMultiplier(level: Int): Float = 1f + (clamp(level) - MIN_LEVEL) * HP_PER_LEVEL

    fun attackMultiplier(level: Int): Float = 1f + (clamp(level) - MIN_LEVEL) * ATTACK_PER_LEVEL

    fun hpAt(card: MonsterCard, level: Int): Int = (card.hp * hpMultiplier(level)).roundToInt()

    fun attackAt(card: MonsterCard, level: Int): Int = (card.attack * attackMultiplier(level)).roundToInt()

    fun isMaxed(level: Int): Boolean = clamp(level) >= MAX_LEVEL

    /**
     * Koszt awansu Z [currentLevel] na kolejny. Rzadszy kompan kosztuje wiecej za ten sam poziom -
     * inaczej oplacaloby sie pompowac najtansza karte i ignorowac cala reszte kolekcji.
     */
    fun upgradeCost(currentLevel: Int, rarity: Rarity): Int {
        if (isMaxed(currentLevel)) return 0
        val base = 40 + rarity.ordinal * 35
        return (base * COST_GROWTH.pow(clamp(currentLevel) - MIN_LEVEL)).roundToInt()
    }

    /** Ile zlota trzeba by lacznie wlozyc, zeby dojsc z [MIN_LEVEL] do [level]. */
    fun totalInvested(level: Int, rarity: Rarity): Int =
        (MIN_LEVEL until clamp(level)).sumOf { upgradeCost(it, rarity) }

    // Sprzedazy duplikatow juz tu nie ma - duplikat jest paliwem awansu ([MonsterStars.ascendCost]),
    // a nadmiarowe kopie na maksymalnej gwiazdce rozpuszczaja sie w esencje
    // ([MonsterStars.dissolveValue]). "Sprzedam kopie za zloto" bylo najgorszym z mozliwych
    // przeznaczen: kasowalo jedyna rzecz, ktora w gachy daje powod do dalszego losowania.
}
