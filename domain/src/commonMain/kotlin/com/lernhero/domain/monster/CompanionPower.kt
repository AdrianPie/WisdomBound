package com.lernhero.domain.monster

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import kotlin.math.roundToInt

/**
 * Ile kompan ma NAPRAWDE HP i ataku - jedno miejsce dla karty w kolekcji i dla pola bitwy.
 *
 * ## Po co to powstalo
 *
 * Kompan istnial w DWOCH skalach naraz i nikt ich nie pogodzil:
 *
 * - karta w katalogu opisuje go "malymi" liczbami (55-100 HP, 14-22 ataku), zeby proporcje miedzy
 *   potworami (tank / glass cannon) dalo sie czytac na oko;
 * - walka mnozyla te liczby przez [HP_SCALE]/[ATTACK_SCALE], zeby kompan w ogole przezyl salwe
 *   stojac obok bohatera (420 HP / 78 ataku).
 *
 * Sprzet kompanow ([com.lernhero.domain.item.catalog.MonsterGear]) byl pisany w skali WALKI -
 * widac to po proporcjach: pancerz +120 HP to +57% na kompanie majacym 210 HP, czyli ten sam rzad,
 * co +48 HP na bohaterze z 420. W skali karty bylby to +171%, liczba, ktorej nikt swiadomie nie
 * napisal. Skutki byly dwa i oba zle:
 *
 * 1. **Walka w ogole nie czytala sprzetu kompanow.** Caly modul `:feature:game` ani razu nie
 *    wspominal o `MonsterEquipment` - ekwipunek zakladalo sie w Summon Center, karta pokazywala
 *    wyzsze liczby, a `buildPlayerParty` bralo sam wynik [MonsterStars]. 28 przedmiotow, wlasna
 *    zakladka w Kuzni, premia za zywiol, auto-zakladanie - i zero wplywu na walke.
 * 2. **Karta zawyzala trzykrotnie**, bo dodawala sprzet w skali walki do statystyk w skali karty.
 *
 * Dlatego jedna funkcja i jedna skala: wszystko, co pokazuje gracz i czym liczy walka, przechodzi
 * TUTAJ i wychodzi w skali WALKI. Karta kompana jest przez to porownywalna z karta bohatera -
 * i to jest jej jedyny sensowny uklad odniesienia.
 */
object CompanionPower {

    /**
     * O ile walka podbija HP z karty. Karty sa opisane w "malych" liczbach, zeby proporcje miedzy
     * potworami zostaly czytelne; przeskalowanie robi sie tu, a nie w katalogu.
     */
    const val HP_SCALE = 3.0f

    /** To samo dla ataku - nizej niz HP, bo kompan ma byc glownie tarcza, nie drugim bohaterem. */
    const val ATTACK_SCALE = 2.0f

    /** HP kompana w walce: karta razy gwiazdki i poziom, przeskalowana, plus sprzet. */
    fun hpAt(card: MonsterCard, stars: Int, level: Int, gear: Stats = Stats.Empty): Int =
        (MonsterStars.hpAt(card, stars, level) * HP_SCALE + gear[StatType.MaxHp]).roundToInt()

    /** Atak kompana w walce - patrz [hpAt]. */
    fun attackAt(card: MonsterCard, stars: Int, level: Int, gear: Stats = Stats.Empty): Int =
        (MonsterStars.attackAt(card, stars, level) * ATTACK_SCALE + gear[StatType.Attack]).roundToInt()

    /**
     * Pancerz kompana w walce (ulamek 0..[com.lernhero.domain.combat.Armor.MAX]): wlasna obrona
     * gatunku ([MonsterCard.defense]) plus to, co daje sprzet. Obrona karty wchodzi jak redukcja
     * obrazen (liniowo), obrona ze sprzetu przez krzywa [com.lernhero.domain.combat.Armor.of].
     */
    fun armorAt(card: MonsterCard, gear: Stats = Stats.Empty): Float =
        com.lernhero.domain.combat.Armor.of(
            defense = gear[StatType.Defense],
            damageReduction = gear[StatType.DamageReduction] + card.defense / 100f
        )
}
