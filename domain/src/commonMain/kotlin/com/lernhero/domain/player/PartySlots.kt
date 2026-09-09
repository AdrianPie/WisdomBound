package com.lernhero.domain.player

/**
 * Miejsca na kompanow obok bohatera i POZIOM, na ktorym kazde z nich sie otwiera.
 *
 * Wczesniej gracz dostawal wszystkie piec od pierwszej minuty i widzial piec pustych ramek, ktore
 * przez wiele godzin nie mialy czym byc wypelnione - pusty ekran zamiast obietnicy. Teraz kazde
 * kolejne miejsce jest nagroda za poziom, wiec awans daje cos, co widac na polu bitwy, a nie tylko
 * punkty statystyk.
 *
 * ## Skad te konkretne poziomy
 *
 * Progi sa dobrane pod tempo zdobywania XP w kampanii ([com.lernhero.domain.run.RunLoot]:
 * ~8-30 XP za odpowiedz i ~15-25 za fale, czyli grubo licząc **ok. 150 XP za jedno zejscie**
 * trwajace kilka minut). Zestawione z krzywa [LevelCurve] (BASE_XP 120, GROWTH 1.18) daje to:
 *
 * | Miejsce | Poziom | XP od zera | Mniej wiecej |
 * |---------|--------|-----------|--------------|
 * | 1       | 1      | 0         | od poczatku  |
 * | 2       | 3      | ~260      | kilka minut  |
 * | 3       | 6      | ~860      | ~20 minut    |
 * | 4       | 10     | ~2300     | ~45 minut    |
 * | 5       | 15     | ~6300     | ~2 godziny   |
 *
 * Odstepy celowo rosna: pierwsze miejsce ma przyjsc szybko, zeby gracz od razu zobaczyl, ze system
 * istnieje, a ostatnie ma byc celem na caly pierwszy wieczor. Zmiana krzywej XP przesuwa te czasy,
 * ale nie psuje kolejnosci - dlatego progi sa poziomami, a nie minutami.
 */
object PartySlots {

    /** Ile miejsc na kompanow istnieje w ogole. Bohater stoi obok nich, nie zajmuje zadnego. */
    const val TOTAL = 5

    /**
     * Poziom bohatera otwierajacy miejsce o danym indeksie. Rosnaco - [unlockedCount] na tym polega.
     */
    private val UNLOCK_LEVEL = listOf(1, 3, 6, 10, 15)

    init {
        require(UNLOCK_LEVEL.size == TOTAL) { "Kazde miejsce musi miec swoj prog" }
    }

    /** Ile miejsc stoi otworem przy takim poziomie. Zawsze co najmniej jedno. */
    fun unlockedCount(heroLevel: Int): Int = UNLOCK_LEVEL.count { heroLevel >= it }

    fun isUnlocked(slotIndex: Int, heroLevel: Int): Boolean =
        slotIndex in 0 until TOTAL && heroLevel >= requiredLevel(slotIndex)

    /** Poziom potrzebny do otwarcia tego miejsca - UI pokazuje go na klodce. */
    fun requiredLevel(slotIndex: Int): Int =
        UNLOCK_LEVEL.getOrElse(slotIndex) { Int.MAX_VALUE }

    /** Poziom otwierajacy NAJBLIZSZE zamkniete miejsce, albo null gdy wszystkie sa juz otwarte. */
    fun nextUnlockLevel(heroLevel: Int): Int? = UNLOCK_LEVEL.firstOrNull { it > heroLevel }

    /**
     * Sklad przyciety do tego, co gracz ma realnie otwarte.
     *
     * Bramka musi dzialac takze na ZAPISIE, nie tylko w UI: sklad zapisany przed wprowadzeniem
     * progow (albo po jakiejkolwiek przyszlej zmianie tabeli) ma peine piec miejsc, a walka czyta
     * druzyne wprost z zapisu. Bez tego przyciecia stary zapis omijalby caly system.
     */
    fun enforce(party: List<String?>, heroLevel: Int): List<String?> =
        List(TOTAL) { index -> if (isUnlocked(index, heroLevel)) party.getOrNull(index) else null }
}
