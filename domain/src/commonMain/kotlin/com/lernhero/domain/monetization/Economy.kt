package com.lernhero.domain.monetization

import com.lernhero.domain.common.Rarity
import com.lernhero.domain.energy.EnergyRules
import com.lernhero.domain.item.Inventory
import com.lernhero.domain.player.Player
import com.lernhero.domain.run.ChestSlot
import kotlin.math.roundToInt

/**
 * Kurs wymiany calej gry na DIAMENTY - jedyna miara, ktora pozwala porownac skrzynie z paczka
 * losow, paczke losow z pakietem zlota, a to wszystko z tym, co gracz dostaje za darmo.
 *
 * ## Po co to powstalo
 *
 * Do tej pory kazda cena byla wymyslona osobno i nic ich ze soba nie wiazalo. Efekt byl taki, ze
 * zwykla skrzynia za 100 diamentow dawala JEDNA nagrode z kola (najczesciej 60-150 zlota albo
 * pospolity przedmiot), czyli srednio kilkanascie diamentow wartosci - a obok stal los za 9.
 * Gracz, ktory raz to policzyl, nigdy wiecej nie kupil skrzyni, a bez skrzyn diamenty nie mialy
 * na co isc.
 *
 * Teraz dziala zasada z gier, ktore umieja to robic (Clash Royale, AFK Arena, Brawl Stars):
 *
 * 1. **Wszystko ma cene w diamentach** ([goldPerGem], [TICKET], [itemValue] ...).
 * 2. **Zakup wprost placi 1:1** - paczka zlota za 100 diamentow to dokladnie 100 diamentow zlota
 *    (plus rabat za ilosc). To jest pewniak, bez losowania.
 * 3. **Skrzynia placi WIECEJ niz kosztuje** - zwykla ~1.5x, epicka ~2x - bo czesc tej wartosci
 *    siedzi w losowaniu. Hazard ma sie oplacac srednio, inaczej nikt go nie wybiera.
 * 4. **Wieksze = oplacalniejsze**: wiekszy pakiet diamentow daje wiecej za zlotowke, wieksza
 *    paczka losow tansza sztuke, epicka skrzynia wiecej wartosci na diament niz zwykla.
 *
 * Kurs zlota i materialow rosnie z poziomem bohatera ([progressFactor]) - stale 500 zlota byloby
 * fortuna na 1. poziomie i napiwkiem na 40. Dzieki temu ta sama skrzynia jest warta tyle samo
 * PRACY na kazdym etapie gry. Wszystkie te zasady pilnuja testy w `EconomyTest`.
 */
object GemValue {

    /** Los do Summon Center. Heroiczny summon (3 losy) = 45 diamentow. */
    const val TICKET = 15

    /** Punkt energii - pelny pasek (40) ma cene pierwszego doladowania dnia. */
    const val ENERGY_POINT = 1.25f

    /**
     * Mnoznik postepu: tyle razy wiecej zlota i materialow placi gra na tym poziomie niz na
     * pierwszym. Celowo ta sama krzywa co [AdRewards.goldFor] - reklama, skrzynia i paczka zlota
     * rosna razem, wiec nigdy nie rozjada sie ze soba.
     */
    fun progressFactor(heroLevel: Int): Float = AdRewards.goldFor(heroLevel) / AdRewards.goldFor(1).toFloat()

    /** Ile zlota wart jest jeden diament. 10 na 1. poziomie - jedna reklama to 10 diamentow. */
    fun goldPerGem(heroLevel: Int): Float = 10f * progressFactor(heroLevel)

    fun orePerGem(heroLevel: Int): Float = 2f * progressFactor(heroLevel)

    fun essencePerGem(heroLevel: Int): Float = 2f * progressFactor(heroLevel)

    fun stonePerGem(heroLevel: Int): Float = 10f * progressFactor(heroLevel)

    /**
     * Przedmiot ze skrzyni. Legendarny jest wart duzo, bo nie da sie go kupic ani pewnie
     * wylosowac - to on robi ze skrzyni skrzynie.
     */
    fun itemValue(rarity: Rarity): Int = when (rarity) {
        Rarity.Common -> 15
        Rarity.Uncommon -> 30
        Rarity.Rare -> 60
        Rarity.Epic -> 120
        Rarity.Legendary -> 400
    }

    /** Wartosc paczki w diamentach - przedmioty liczone po sredniej ich rzadkosci. */
    fun of(bundle: Bundle, heroLevel: Int, itemRarities: List<Rarity> = emptyList()): Float =
        bundle.gems +
            bundle.gold / goldPerGem(heroLevel) +
            bundle.iron / orePerGem(heroLevel) +
            bundle.essence / essencePerGem(heroLevel) +
            bundle.stone / stonePerGem(heroLevel) +
            bundle.tickets * TICKET +
            bundle.energy * ENERGY_POINT +
            itemRarities.sumOf { itemValue(it) }

    /** Srednia wartosc pola kola fortuny - przedmiot to srednia z jego puli rzadkosci. */
    fun of(slot: ChestSlot, heroLevel: Int): Float {
        val items = slot.itemRarities
        if (items != null) return items.map { itemValue(it) }.average().toFloat()
        return slot.ticketCount * TICKET + slot.goldAmount * progressFactor(heroLevel) / goldPerGem(heroLevel)
    }
}

/**
 * Wszystko, co jedna transakcja dopisuje graczowi - skrzynia, paczka zlota, doladowanie energii.
 * Jeden ksztalt dla wszystkich zakupow, zeby repozytorium mialo JEDNA droge dopisywania nagrod
 * (patrz [plusBundle]) zamiast osobnej funkcji na kazdy przycisk w sklepie.
 */
data class Bundle(
    val gold: Int = 0,
    val gems: Int = 0,
    val tickets: Int = 0,
    val iron: Int = 0,
    val essence: Int = 0,
    val stone: Int = 0,
    val energy: Int = 0,
    val itemIds: List<String> = emptyList()
) {
    operator fun plus(other: Bundle) = Bundle(
        gold = gold + other.gold,
        gems = gems + other.gems,
        tickets = tickets + other.tickets,
        iron = iron + other.iron,
        essence = essence + other.essence,
        stone = stone + other.stone,
        energy = energy + other.energy,
        itemIds = itemIds + other.itemIds
    )

    /** Pozycje do narysowania jako chipy "+1 200 💰" - bez zer i bez przedmiotow (te maja wlasna karte). */
    fun entries(): List<Pair<String, Int>> = buildList {
        if (gold > 0) add("🪙" to gold)
        if (gems > 0) add("💎" to gems)
        if (tickets > 0) add("🎟" to tickets)
        if (iron > 0) add("🔩" to iron)
        if (essence > 0) add("✨" to essence)
        if (stone > 0) add("🪨" to stone)
        if (energy > 0) add("⚡" to energy)
    }

    companion object {
        /** Kwota w walucie, ktora rosnie z postepem - zaokraglona do "sklepowej" liczby. */
        fun scaled(base: Float, heroLevel: Int): Int = roundNice(base * GemValue.progressFactor(heroLevel))

        /** 1234 -> 1250, 87 -> 90: ceny w sklepie maja wygladac jak ceny, a nie jak wynik mnozenia. */
        fun roundNice(value: Float): Int {
            val step = when {
                value >= 5000f -> 250f
                value >= 1000f -> 50f
                value >= 200f -> 10f
                value >= 50f -> 5f
                else -> 1f
            }
            return ((value / step).roundToInt() * step).roundToInt()
        }
    }
}

/**
 * Codzienne i stale sposoby WYDANIA diamentow poza skrzyniami. Kazda oferta to jawna, pewna
 * zamiana po kursie [GemValue] - im wieksza, tym lepszy kurs ([bonusPercent]).
 *
 * Te oferty nie maja limitu - to zwykla wymiana, ktora mozna robic do woli. Limit i rosnaca cene
 * ma tylko energia ([EnergyRefill]), bo to ona jest hamulcem tempa gry.
 */
enum class SupplyDeal(
    val id: String,
    val title: String,
    val icon: String,
    val gems: Int,
    /** Rabat za ilosc - o tyle procent wiecej, niz wynikaloby z samego kursu. */
    val bonusPercent: Int
) {
    GoldPouch("gold_pouch", "Pouch of Gold", "🪙", gems = 50, bonusPercent = 0),
    GoldSack("gold_sack", "Sack of Gold", "💰", gems = 200, bonusPercent = 10),
    GoldVault("gold_vault", "Gold Vault", "🏦", gems = 600, bonusPercent = 25),
    OreCrate("ore_crate", "Ore Crate", "🔩", gems = 60, bonusPercent = 0),
    EssenceFlask("essence_flask", "Essence Flask", "✨", gems = 60, bonusPercent = 0),
    StoneCart("stone_cart", "Stone Cart", "🪨", gems = 40, bonusPercent = 0);

    /** Co dokladnie dostaje gracz na tym poziomie - ekran pokazuje te liczbe przed zakupem. */
    fun bundleFor(heroLevel: Int): Bundle {
        val bonus = 1f + bonusPercent / 100f
        val worth = gems * bonus
        return when (this) {
            GoldPouch, GoldSack, GoldVault -> Bundle(gold = Bundle.roundNice(worth * GemValue.goldPerGem(heroLevel)))
            OreCrate -> Bundle(iron = Bundle.roundNice(worth * GemValue.orePerGem(heroLevel)))
            EssenceFlask -> Bundle(essence = Bundle.roundNice(worth * GemValue.essencePerGem(heroLevel)))
            StoneCart -> Bundle(stone = Bundle.roundNice(worth * GemValue.stonePerGem(heroLevel)))
        }
    }
}

/**
 * Doladowanie energii za diamenty - najwazniejszy "sink" w grach z energia. Pelny pasek, cena
 * rosnie z kazdym zakupem tego samego dnia, a limit dzienny trzyma tempo w ryzach: kto czyta
 * i chodzi, zarabia na pierwsze doladowanie codziennie (patrz cele dzienne), kto chce grac
 * caly dzien - placi coraz wiecej.
 *
 * Energia z doladowania MOZE przekroczyc [com.lernhero.domain.energy.EnergyRules.MAX] - kupiona
 * nie moze przepasc tylko dlatego, ze pasek nie byl pusty.
 */
object EnergyRefill {
    const val DEAL_ID = "energy_refill"
    const val AMOUNT = com.lernhero.domain.energy.EnergyRules.MAX
    private val PRICES = listOf(50, 80, 120, 160, 200)
    val MAX_PER_DAY: Int get() = PRICES.size

    /** Cena kolejnego doladowania, gdy dzis kupiono juz [boughtToday]; null = limit wyczerpany. */
    fun priceFor(boughtToday: Int): Int? = PRICES.getOrNull(boughtToday.coerceAtLeast(0))
}

/**
 * Dopisuje paczke do konta. Jedyne miejsce, ktore wie, gdzie laduje kazda waluta - repozytorium
 * woła to przy KAZDYM zakupie, wiec skrzynia, paczka zlota i doladowanie energii nie moga sie
 * rozjechac w tym, co faktycznie dostaje gracz.
 *
 * Plecak musi byc juz w nowym formacie (`withMigratedItems`) - przedmioty dochodza jako sztuki.
 * Energia z zakupu moze przekroczyc sufit paska, patrz [EnergyRefill].
 */
fun Player.plusBundle(bundle: Bundle, now: Long): Player {
    val energyNow = if (bundle.energy > 0) {
        val current = EnergyRules.regenerated(energy, now)
        current.copy(amount = current.amount + bundle.energy)
    } else {
        energy
    }
    return copy(
        money = money + bundle.gold,
        gems = gems + bundle.gems,
        townResources = townResources.plusAll(
            stone = bundle.stone,
            iron = bundle.iron,
            essence = bundle.essence,
            tickets = bundle.tickets
        ),
        heroInventory = bundle.itemIds.fold(heroInventory) { bag, id -> Inventory.add(bag, id) },
        energy = energyNow
    )
}
