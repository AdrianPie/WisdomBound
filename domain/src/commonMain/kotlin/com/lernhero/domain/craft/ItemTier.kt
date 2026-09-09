package com.lernhero.domain.craft

import com.lernhero.domain.common.Rarity
import com.lernhero.domain.item.Item
import com.lernhero.domain.run.RunDifficulty
import com.lernhero.domain.story.StoryWorldCatalog

/**
 * Prog sprzetu w kuzni: piec szczebli, ktore odblokowuje sie POSTEPEM, a nie zlotem.
 *
 * Tier NIE jest osobnym polem przedmiotu - wynika wprost z jego [Rarity]. Dwie skale opisujace
 * to samo ("jak dobry jest ten miecz") zawsze sie w koncu rozjezdzaja: ktos dopisze legendarny
 * przedmiot z tierem 2 i nikt tego nie zauwazy, dopoki gracz nie znajdzie go w pierwszej krainie.
 * Skoro rzadkosc juz steruje kosztem ([Forge.costFor]) i sila ulepszen ([ItemUpgrades.costFor]),
 * niech steruje takze dostepnoscia.
 *
 * Bramka jest z KAMPANII: zeby kuc lepiej, trzeba zajsc dalej. To zamyka petle rozgrywki -
 * surowce z lochu placa za sprzet, ktory pozwala wejsc glebiej w loch. Zloto nie ma tu wstepu,
 * bo kupowanie postepu bylo dokladnie tym, co wyrzucono razem ze starym sklepem.
 */
enum class ItemTier(
    val label: String,
    val rarity: Rarity,
    /**
     * Ile plansz kampanii (na najlatwiejszym tierze) trzeba miec za soba. 0 = dostepne od startu.
     * Progi sa ulamkiem CALEJ kampanii, a nie sztywnymi liczbami - dolozenie krainy nie wymaga
     * wtedy przestrajania tej tabeli.
     */
    val campaignFraction: Float
) {
    Rough("Rough", Rarity.Common, 0f),
    Sturdy("Sturdy", Rarity.Uncommon, 0.15f),
    Fine("Fine", Rarity.Rare, 0.35f),
    Master("Master", Rarity.Epic, 0.60f),
    Mythic("Mythic", Rarity.Legendary, 0.85f);

    /** Ile plansz trzeba zdobyc, zeby ten prog sie otworzyl. */
    fun requiredStages(): Int = (StoryWorldCatalog.totalStages * campaignFraction).toInt()

    /**
     * Czy gracz z takim postepem moze juz kuc na tym progu.
     *
     * Liczymy NAJDALSZY postep z dowolnego tieru trudnosci - gracz, ktory przeszedl kampanie na
     * Koszmarze, nie ma powodu byc karany za to, ze nie domknal Normalnego.
     */
    fun isUnlocked(cleared: Map<String, Int>): Boolean = furthestStage(cleared) >= requiredStages()

    companion object {
        /** Prog, na ktorym stoi dany przedmiot. */
        fun of(item: Item): ItemTier = entries.first { it.rarity == item.rarity }

        fun of(rarity: Rarity): ItemTier = entries.first { it.rarity == rarity }

        /** Najwyzszy prog otwarty przy takim postepie - nigdy null, bo [Rough] stoi otworem zawsze. */
        fun highestUnlocked(cleared: Map<String, Int>): ItemTier =
            entries.last { it.isUnlocked(cleared) }

        private fun furthestStage(cleared: Map<String, Int>): Int =
            RunDifficulty.entries.maxOfOrNull { cleared[it.name] ?: 0 } ?: 0
    }
}
