package com.lernhero.domain.player

import com.lernhero.domain.item.Inventory
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.item.catalog.MonsterGear
import com.lernhero.domain.item.withMigratedItems
import com.lernhero.domain.monster.MonsterCatalog
import com.lernhero.domain.run.RunDifficulty
import com.lernhero.domain.story.StoryWorldCatalog
import com.lernhero.domain.town.TownResources

/**
 * Konta z dostepem do zestawu testowego ([AdminKit]).
 *
 * Lista adresow, a nie flaga w zapisie: flage dalo by sie ustawic z aplikacji i kazdy gracz mialby
 * do niej droge. Adres jest sprawdzany po stronie kodu, wiec zeby dopisac sie do tej listy, trzeba
 * miec repozytorium - czyli dokladnie to uprawnienie, ktore ta lista ma reprezentowac.
 *
 * **Dopisz sie tutaj**: wpisz swoj adres z logowania, malymi literami.
 */
object AdminAccounts {

    private val EMAILS: Set<String> = setOf(
        "test@test.pl"
    )

    fun isAdmin(email: String?): Boolean =
        email != null && email.trim().lowercase() in EMAILS
}

/**
 * Zestaw testowy: wszystko naraz, zeby dalo sie ogladac ekrany, ktore normalnie sa efektem wielu
 * godzin gry - pelny ekwipunek w kuzni, wysokie progi przedmiotow, otwarte miejsca w druzynie.
 *
 * Nie jest to "cheat dla gracza", tylko narzedzie do patrzenia na UI: [PlayerRepository] wpuszcza
 * go wylacznie kontom z [AdminAccounts], a sama funkcja jest czysta, wiec da sie ja sprawdzic
 * testem bez sieci.
 *
 * Przyznaje CALY komplet, bo polowiczny zestaw nie pokazalby tego, co ma pokazac: same przedmioty
 * bez postepu kampanii wygladalyby na zamkniete (progi [com.lernhero.domain.craft.ItemTier]), a sam
 * poziom bez kompanow zostawialby puste miejsca w druzynie, ktore wlasnie sie odblokowaly.
 */
object AdminKit {

    /** Poziom bohatera. Otwiera wszystkie miejsca w druzynie ([PartySlots]) z duzym zapasem. */
    const val LEVEL = 60

    /** Ile sztuk kazdego przedmiotu kompana - tyle, zeby obdzielic cala druzyne. */
    const val MONSTER_COPIES = PartySlots.TOTAL

    private const val MONEY = 9_999_999
    private const val RESOURCE = 999_999
    private const val POINTS_PER_STAT = 40
    private const val UNSPENT_POINTS = 20

    fun grant(player: Player): Player {
        // Najpierw normalizacja starego zapisu - inaczej dosypane egzemplarze staneleyby obok
        // nieprzerobionych list z dawnego formatu i czesc sprzetu zniknelaby graczowi z oczu.
        val base = player.withMigratedItems()

        var heroBag = base.heroInventory
        ItemCatalog.all.forEach { item ->
            if (Inventory.countOf(heroBag, item.id) == 0) {
                heroBag = Inventory.add(heroBag, item.id)
            }
        }

        var monsterBag = base.monsterInventory
        MonsterGear.all.forEach { item ->
            val missing = MONSTER_COPIES - Inventory.countOf(monsterBag, item.id)
            if (missing > 0) monsterBag = Inventory.add(monsterBag, item.id, missing)
        }

        return base.copy(
            money = MONEY,
            progress = PlayerProgress(
                level = LEVEL,
                xp = 0,
                // Kilka punktow ZOSTAJE nierozdanych - bez nich zielone "+" przy atrybutach nigdy
                // by sie nie pokazalo i nie dalo by sie sprawdzic, czy w ogole dziala.
                unspentPoints = UNSPENT_POINTS,
                // Rozdane, a nie "do rozdania": chodzi o to, zeby od razu bylo widac liczby walki.
                stats = HeroStats(
                    strength = POINTS_PER_STAT,
                    vitality = POINTS_PER_STAT,
                    focus = POINTS_PER_STAT,
                    luck = POINTS_PER_STAT
                )
            ),
            heroInventory = heroBag,
            monsterInventory = monsterBag,
            ownedMonsters = base.ownedMonsters + MonsterCatalog.all.map { it.id },
            townResources = TownResources(
                wood = RESOURCE,
                stone = RESOURCE,
                iron = RESOURCE,
                essence = RESOURCE,
                summonTickets = 999
            ),
            // Kampania zaliczona na kazdym tierze - to ona otwiera progi kuzni.
            storyProgress = RunDifficulty.entries.associate { it.name to StoryWorldCatalog.totalStages }
        )
    }
}
