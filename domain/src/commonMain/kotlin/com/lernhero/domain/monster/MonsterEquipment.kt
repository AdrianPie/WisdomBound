package com.lernhero.domain.monster

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.item.Inventory
import com.lernhero.domain.item.ItemInstance
import com.lernhero.domain.item.resolve
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

    /**
     * Slot trzyma uid SZTUKI, wiec do odczytania go potrzebny jest plecak kompanow [bag].
     * Nieznany uid po prostu wypada - stary zapis albo sprzedany przedmiot nie moze wysadzic ekranu.
     */
    fun instanceIn(equipment: Map<String, String>, slot: ItemSlot, bag: List<ItemInstance>): ItemInstance? =
        equipment[slot.name]?.let { uid -> Inventory.byUid(bag, uid) }

    /** Przedmiot w slocie, juz z poziomem ulepszenia TEJ sztuki. */
    fun itemIn(equipment: Map<String, String>, slot: ItemSlot, bag: List<ItemInstance>): Item? =
        instanceIn(equipment, slot, bag)?.resolve()

    /** Wszystkie zalozone przedmioty. */
    fun itemsOf(equipment: Map<String, String>, bag: List<ItemInstance>): List<Item> =
        MonsterGear.SLOTS.mapNotNull { itemIn(equipment, it, bag) }

    /** Zaklada KONKRETNA sztuke - dwie kopie tego samego pazura to dwa rozne przedmioty. */
    fun with(equipment: Map<String, String>, instance: ItemInstance, slot: ItemSlot): Map<String, String> =
        equipment + (slot.name to instance.uid)

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
        bag: List<ItemInstance>
    ): Stats =
        // Poziom ulepszenia siedzi juz w sztuce (itemIn zwraca przedmiot po naliczeniu poziomu),
        // wiec zostaje tylko premia za zgodnosc zywiolu - drugi, niezalezny powod, dla ktorego
        // ten sam pazur bywa mocniejszy.
        itemsOf(equipment, bag).fold(Stats.Empty) { total, item ->
            total + item.stats.scaledBy(MonsterGear.matchBonus(item.id, element))
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
        bag: List<ItemInstance>,
        element: Element?,
        takenBy: Set<String> = emptySet()
    ): Map<String, String> {
        // Para (sztuka, jej przedmiot po naliczeniu poziomu) - ocena musi patrzec na ULEPSZONE
        // liczby, inaczej auto-equip zakladalby surowy pazur zamiast tego samego, ale na +8.
        val available = bag
            .filter { it.uid !in takenBy }
            .mapNotNull { instance -> instance.resolve()?.let { instance to it } }

        return MonsterGear.SLOTS.mapNotNull { slot ->
            available
                .filter { (_, item) -> item.slot == slot }
                .maxByOrNull { (_, item) -> scoreFor(item, element) }
                ?.let { (instance, _) -> slot.name to instance.uid }
        }.toMap()
    }

    /** Uid sztuk noszonych przez kompanow INNYCH niz [exceptMonsterId]. */
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
