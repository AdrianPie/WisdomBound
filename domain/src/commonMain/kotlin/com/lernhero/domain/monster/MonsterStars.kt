package com.lernhero.domain.monster

import com.lernhero.domain.common.Rarity
import kotlin.math.roundToInt

/**
 * Gwiazdki kompana - klasyczny gacha: **duplikat nie jest smieciem, tylko awansem**.
 *
 * Petla wyglada tak:
 *
 * 1. Pierwsza kopia odblokowuje kompana na ★1 z sufitem poziomu [levelCapFor] = 10.
 * 2. Poziomy do sufitu kupujesz za ZLOTO ([MonsterLevels]).
 * 3. Sufit zdejmuje sie dopiero AWANSEM, a ten kosztuje DUPLIKATY tej samej karty.
 * 4. Na ★[MAX_STARS] awansu juz nie ma - kolejne duplikaty rozpuszczaja sie w esencje
 *    ([dissolveValue]), czyli w walute, ktora idzie na zaklecia i ulepszenia innych kompanow.
 *
 * Dzieki temu gracz ma powod, zeby summonowac dalej nawet z pelna kolekcja - to jest dokladnie
 * ten "endgame sink", bez ktorego kazde losowanie po skompletowaniu kolekcji jest zmarnowane.
 * Jednoczesnie nie ma nieskonczonego stakowania mocy: sufit gwiazdek jest twardy, wiec gracz z
 * pięcioma setkami kopii nie odjezdza reszcie na zawsze.
 */
object MonsterStars {

    const val MIN_STARS = 1
    const val MAX_STARS = 5

    /**
     * Ile DUPLIKATOW trzeba, zeby wejsc Z podanej gwiazdki na kolejna. Rosnie lagodnie, bo to jest
     * najdluzsza sciezka w calej grze - 2+3+5+8 = 18 kopii ponad ta pierwsza, odblokowujaca.
     */
    fun ascendCost(fromStars: Int): Int = when (fromStars) {
        1 -> 2
        2 -> 3
        3 -> 5
        4 -> 8
        else -> 0
    }

    /** Laczna liczba kopii (razem z ta pierwsza) potrzebna, zeby dojsc do [stars]. */
    fun totalCopiesFor(stars: Int): Int =
        1 + (MIN_STARS until stars.coerceIn(MIN_STARS, MAX_STARS)).sumOf { ascendCost(it) }

    /**
     * Sufit poziomu na danej gwiazdce. To on jest wlasciwa nagroda za awans - samo +% do statystyk
     * bylo by niewidoczne, a podniesiony sufit od razu daje co robic ze zlotem.
     */
    fun levelCapFor(stars: Int): Int = when (clampStars(stars)) {
        1 -> 10
        2 -> 20
        3 -> 30
        4 -> 40
        else -> MonsterLevels.MAX_LEVEL
    }

    /** Kazda gwiazdka to dodatkowo staly bonus do statystyk - poza podniesionym sufitem poziomu. */
    fun statMultiplier(stars: Int): Float = 1f + (clampStars(stars) - MIN_STARS) * STAT_PER_STAR

    fun clampStars(stars: Int): Int = stars.coerceIn(MIN_STARS, MAX_STARS)

    fun isMaxStars(stars: Int): Boolean = clampStars(stars) >= MAX_STARS

    /**
     * Ile esencji daje rozpuszczenie duplikatu kompana, ktory jest juz na maksymalnej gwiazdce.
     * Rzadszy kompan daje wiecej - inaczej legendarny duplikat bylby wart tyle, co pospolity.
     */
    fun dissolveValue(rarity: Rarity): Int = when (rarity) {
        Rarity.Common -> 15
        Rarity.Uncommon -> 30
        Rarity.Rare -> 60
        Rarity.Epic -> 120
        Rarity.Legendary -> 250
    }

    /** Statystyki kompana po zlozeniu gwiazdek i poziomu - jedno miejsce, zeby nie liczyc tego dwa razy. */
    fun hpAt(card: MonsterCard, stars: Int, level: Int): Int =
        (card.hp * statMultiplier(stars) * MonsterLevels.hpMultiplier(level)).roundToInt()

    fun attackAt(card: MonsterCard, stars: Int, level: Int): Int =
        (card.attack * statMultiplier(stars) * MonsterLevels.attackMultiplier(level)).roundToInt()

    private const val STAT_PER_STAR = 0.25f
}
