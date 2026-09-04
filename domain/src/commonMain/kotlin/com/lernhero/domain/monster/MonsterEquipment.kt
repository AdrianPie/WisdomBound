package com.lernhero.domain.monster

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.craft.ItemUpgrades
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.catalog.MonsterGear

/**
 * Ekwipunek jednego kompana i cala logika zakladania.
 *
 * Zapis to mapa `nazwaSlotu -> idPrzedmiotu` (a nie `ItemSlot -> Item`), bo tak wlasnie ląduje
 * w Firestore: proste stringi, ktore przezyja dodanie nowego slotu albo usuniecie przedmiotu
 * z katalogu. Rozwiazywanie na prawdziwe obiekty dzieje sie tutaj i tylko tutaj.
 */
object MonsterEquipment {

    /** Pusty ekwipunek - wygodniejsze niz powtarzanie emptyMap() w kazdym wywolaniu. */
    val EMPTY: Map<String, String> = emptyMap()

    fun itemIn(equipment: Map<String, String>, slot: ItemSlot): Item? =
        equipment[slot.name]?.let { MonsterGear.byId(it) }

    /** Wszystkie zalozone przedmioty - nieznane id po prostu wypada, nie wysadza ekranu. */
    fun itemsOf(equipment: Map<String, String>): List<Item> =
        MonsterGear.SLOTS.mapNotNull { itemIn(equipment, it) }

    fun with(equipment: Map<String, String>, item: Item): Map<String, String> =
        equipment + (item.slot.name to item.id)

    fun without(equipment: Map<String, String>, slot: ItemSlot): Map<String, String> =
        equipment - slot.name

    /**
     * Sumaryczne statystyki z zalozonego sprzetu, juz przeskalowane premia za zgodnosc zywiolu
     * ([MonsterGear.matchBonus]). Dzieki temu "pazur ognia na kompanie ognia" naprawde daje
     * wiecej niz ten sam pazur na kimkolwiek innym - a nie tylko tak brzmi w opisie.
     */
    fun statsOf(
        equipment: Map<String, String>,
        element: Element?,
        upgrades: Map<String, Int> = emptyMap()
    ): Stats =
        itemsOf(equipment).fold(Stats.Empty) { total, item ->
            // Poziom ulepszenia mnozy sie z premia za zywiol - to dwa niezalezne powody, dla
            // ktorych ten sam pazur bywa mocniejszy (patrz ItemUpgrades i MonsterGear.matchBonus).
            val upgraded = ItemUpgrades.upgraded(item, ItemUpgrades.levelOf(upgrades, item.id))
            total + upgraded.stats.scaledBy(MonsterGear.matchBonus(item.id, element))
        }

    /**
     * Ocena przydatnosci przedmiotu dla konkretnego kompana - im wyzej, tym lepiej.
     *
     * Wazymy atak i HP, bo to one realnie decyduja o walce kompana; reszta statystyk dokłada
     * drobne punkty, zeby przy remisie wygral przedmiot bogatszy. Premia za zywiol wchodzi
     * do wyniku, wiec auto-equip sam z siebie preferuje sprzet w kolorze kompana.
     */
    fun scoreFor(item: Item, element: Element?): Float {
        val bonus = MonsterGear.matchBonus(item.id, element)
        val s = item.stats
        val core = s[StatType.Attack] * ATTACK_WEIGHT + s[StatType.MaxHp] * HP_WEIGHT
        val extras = s[StatType.CritChance] * 40f +
            s[StatType.CritDamage] * 15f +
            s[StatType.Speed] * 0.4f +
            s[StatType.Lifesteal] * 60f +
            s[StatType.DamageReduction] * 80f +
            s[StatType.HealPower] * 20f +
            s[StatType.SpellPower] * 25f +
            s[StatType.Dodge] * 50f
        return (core + extras) * bonus
    }

    /**
     * Dobiera najlepszy dostepny przedmiot do KAZDEGO slotu i zwraca gotowy ekwipunek.
     *
     * [ownedIds] to cala pula gracza; [takenBy] mowi, co nosza juz INNI kompani - ten sam pazur
     * nie moze wisiec na dwoch naraz, bo pula liczy sztuki, a nie kopie. Przedmioty noszone przez
     * tego kompana zostaja dostepne, inaczej auto-equip zdejmowalby mu wlasny sprzet.
     */
    fun autoEquip(
        ownedIds: List<String>,
        element: Element?,
        takenBy: Set<String> = emptySet()
    ): Map<String, String> {
        val available = ownedIds
            .asSequence()
            .filter { it !in takenBy }
            .mapNotNull { MonsterGear.byId(it) }
            .toList()

        return MonsterGear.SLOTS.mapNotNull { slot ->
            available
                .filter { it.slot == slot }
                .maxByOrNull { scoreFor(it, element) }
                ?.let { slot.name to it.id }
        }.toMap()
    }

    /** Wszystkie id noszone przez kompanow INNYCH niz [exceptMonsterId]. */
    fun equippedElsewhere(
        allEquipment: Map<String, Map<String, String>>,
        exceptMonsterId: String
    ): Set<String> = allEquipment
        .filterKeys { it != exceptMonsterId }
        .values
        .flatMap { it.values }
        .toSet()

    private const val ATTACK_WEIGHT = 3f
    private const val HP_WEIGHT = 0.5f
}

/** Przeskalowanie calego worka statystyk - potrzebne do premii za zgodnosc zywiolu. */
private fun Stats.scaledBy(factor: Float): Stats {
    if (factor == 1f) return this
    return StatType.entries.fold(Stats.Empty) { total, type ->
        val value = this[type]
        if (value == 0f) total else total + Stats.of(type to value * factor)
    }
}
