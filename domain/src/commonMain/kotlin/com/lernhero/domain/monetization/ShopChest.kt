package com.lernhero.domain.monetization

import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.run.ChestSlot
import kotlin.random.Random

/**
 * Skrzynie ze sklepu. Kazda otwiera sie na dwa sposoby: za diamenty (zawsze) albo za reklame
 * (z limitem - zwykla [BASIC_ADS_PER_DAY] razy dziennie, epicka raz na [EPIC_AD_COOLDOWN_MS]).
 *
 * ## Co jest w srodku
 *
 * Skrzynia to PEWNY pakiet ([guaranteedFor]: zloto, ruda, esencja, w epickiej tez losy) plus JEDEN
 * los z kola ([weights]) - to on jest animacja otwarcia i to on bywa jackpotem. Wczesniej byl tylko
 * los z kola, wiec skrzynia za 100 diamentow dawala najczesciej 60 zlota, czyli byla najgorszym
 * zakupem w sklepie. Teraz srednia wartosc zwyklej to ~1.5x ceny, epickiej ~2x (patrz [GemValue]
 * i `EconomyTest`) - tak jak w grach, w ktorych skrzynie sie kupuje.
 *
 * Epicka ma dodatkowo **gwarancje legendy** ([EPIC_PITY]): najpozniej co dziesiata epicka skrzynia
 * daje legendarny przedmiot. Pech nie moze trwac w nieskonczonosc - gracz widzi licznik i wie, ze
 * kazde otwarcie przybliza go do celu.
 */
enum class ShopChest(
    val title: String,
    val gemCost: Int,
    private val weights: Map<ChestSlot, Float>
) {
    Basic(
        title = "Basic Chest",
        gemCost = 100,
        weights = mapOf(
            ChestSlot.SummonTicket to 22f,
            ChestSlot.ItemCommon to 40f,
            ChestSlot.ItemRare to 26f,
            ChestSlot.HeroicTicket to 8f,
            ChestSlot.ItemLegendary to 4f
        )
    ),
    Epic(
        title = "Epic Chest",
        gemCost = 300,
        weights = mapOf(
            ChestSlot.ItemRare to 62f,
            ChestSlot.HeroicTicket to 26f,
            ChestSlot.ItemLegendary to 12f
        )
    );

    fun chanceOf(slot: ChestSlot): Float = (weights[slot] ?: 0f) / weights.values.sum()

    /** Pola, ktore w ogole moga wypasc - do okna z szansami. */
    val slots: List<ChestSlot> get() = weights.keys.toList()

    /**
     * Pewna czesc zawartosci. Rosnie z poziomem bohatera ([GemValue.progressFactor]) - powoli,
     * zgodnie z reszta gry.
     */
    fun guaranteedFor(heroLevel: Int): Bundle = when (this) {
        Basic -> Bundle(
            gold = Bundle.scaled(500f, heroLevel),
            iron = Bundle.scaled(40f, heroLevel),
            essence = Bundle.scaled(40f, heroLevel)
        )
        Epic -> Bundle(
            gold = Bundle.scaled(2500f, heroLevel),
            iron = Bundle.scaled(150f, heroLevel),
            essence = Bundle.scaled(150f, heroLevel),
            tickets = 5
        )
    }

    fun roll(random: Random): ChestSlot {
        var pick = random.nextFloat() * weights.values.sum()
        for ((slot, weight) in weights) {
            pick -= weight
            if (pick <= 0f) return slot
        }
        return weights.keys.last()
    }

    /** Srednia wartosc jednego otwarcia w diamentach (bez gwarancji legendy - ta tylko dokłada). */
    fun expectedValue(heroLevel: Int): Float =
        GemValue.of(guaranteedFor(heroLevel), heroLevel) +
            weights.keys.sumOf { (chanceOf(it) * GemValue.of(it, heroLevel)).toDouble() }.toFloat()

    /**
     * Otwarcie: los z kola (z gwarancja legendy w epickiej), konkretny przedmiot i CALA paczka do
     * zapisania. Liczone tu, a nie w repozytorium ani na ekranie, zeby animacja pokazywala dokladnie
     * to, co trafi na konto - i zeby dalo sie to przetestowac bez bazy.
     *
     * [pity] = ile epickich skrzyn otwarto od ostatniej legendy.
     */
    fun open(heroLevel: Int, pity: Int, random: Random): ChestOpening {
        val forced = this == Epic && pity + 1 >= EPIC_PITY
        val slot = if (forced) ChestSlot.ItemLegendary else roll(random)
        val guaranteed = guaranteedFor(heroLevel)

        var itemId: String? = null
        val prize = slot.itemRarities?.let { rarities ->
            val pool = ItemCatalog.all.filter { it.rarity in rarities }
            if (pool.isEmpty()) {
                Bundle(gold = Bundle.scaled(300f, heroLevel))
            } else {
                itemId = pool[random.nextInt(pool.size)].id
                Bundle(itemIds = listOf(itemId!!))
            }
        } ?: Bundle(
            tickets = slot.ticketCount,
            gold = if (slot.goldAmount > 0) Bundle.scaled(slot.goldAmount.toFloat(), heroLevel) else 0
        )

        val nextPity = when {
            this != Epic -> pity
            slot == ChestSlot.ItemLegendary -> 0
            else -> pity + 1
        }
        return ChestOpening(
            chest = this,
            slot = slot,
            itemId = itemId,
            guaranteed = guaranteed,
            total = guaranteed + prize,
            nextPity = nextPity,
            pityTriggered = forced
        )
    }

    companion object {
        const val BASIC_ADS_PER_DAY = 3
        const val EPIC_AD_COOLDOWN_MS = 2L * 24 * 60 * 60 * 1000

        /** Najpozniej co tyle epickich skrzyn wypada legenda. */
        const val EPIC_PITY = 10

        /** Ile epickich skrzyn zostalo do pewnej legendy (1 = nastepna na pewno). */
        fun opensToLegendary(pity: Int): Int = (EPIC_PITY - pity).coerceIn(1, EPIC_PITY)
    }
}

/** Wynik jednego otwarcia - to, co pokazuje animacja, i to, co zapisuje repozytorium. */
data class ChestOpening(
    val chest: ShopChest,
    /** Pole z kola - glowna nagroda, ktora wyskakuje ze skrzyni. */
    val slot: ChestSlot,
    /** Konkretny przedmiot, gdy [slot] to przedmiot. */
    val itemId: String?,
    /** Pewna czesc - rysowana jako "a w srodku jeszcze...". */
    val guaranteed: Bundle,
    /** Wszystko razem, do zapisania. */
    val total: Bundle,
    val nextPity: Int,
    /** Legenda wypadla z gwarancji, a nie z losu - ekran moze to powiedziec wprost. */
    val pityTriggered: Boolean
)

/** Kiedy gracz ostatnio otwieral skrzynie ze sklepu za reklame. */
@kotlinx.serialization.Serializable
data class ShopChestAds(
    val basic: DailyCounter = DailyCounter(),
    val epicAt: Long = 0L
) {
    fun basicRemaining(day: String): Int = basic.remainingOn(day, ShopChest.BASIC_ADS_PER_DAY)

    /** 0 = mozna teraz; inaczej ile ms do nastepnej reklamy na epicka. */
    fun epicWaitMs(now: Long): Long = (epicAt + ShopChest.EPIC_AD_COOLDOWN_MS - now).coerceAtLeast(0L)

    fun canWatch(chest: ShopChest, day: String, now: Long): Boolean = when (chest) {
        ShopChest.Basic -> basicRemaining(day) > 0
        ShopChest.Epic -> epicWaitMs(now) == 0L
    }

    fun afterWatch(chest: ShopChest, day: String, now: Long): ShopChestAds = when (chest) {
        ShopChest.Basic -> copy(basic = basic.increment(day))
        ShopChest.Epic -> copy(epicAt = now)
    }
}

/**
 * Legend Pack - jednorazowy zakup na konto (produkt "non-consumable" w sklepie). Zawartosc jest
 * stala i jawna, zeby gracz wiedzial dokladnie, za co placi - bez losowania.
 */
object LegendPack {
    const val PRODUCT_ID = "legend_pack"
    const val GEMS = 2500
    const val TICKETS = 30
    val ITEM_IDS = listOf("dawnbreaker", "thousand_cuts", "worldroot_staff", "titans_carapace")
    /** Cena pokazywana, zanim sklep poda wlasna (w walucie gracza). */
    const val FALLBACK_PRICE = "49,99 zł"
}

/**
 * Pakiet diamentow za prawdziwe pieniadze - produkt "consumable" w sklepie (mozna kupowac wiele
 * razy). [bonus] to diamenty ekstra ponad baze - rosna z wielkoscia pakietu, zeby wiekszy pakiet
 * byl realnie oplacalniejszy, a nie tylko drozszy. Cena ze sklepu ma pierwszenstwo przed
 * [fallbackPrice], ktora pokazujemy tylko, dopoki sklep nie odpowie.
 *
 * Pierwszy zakup KAZDEGO pakietu daje podwojna liczbe diamentow ([firstPurchaseTotal]) - klasyczna
 * zacheta z gier mobilnych: pierwsza wplata ma byc wyraznie oplacalna, zeby w ogole sie wydarzyla.
 */
enum class GemPack(
    val productId: String,
    val title: String,
    val base: Int,
    val bonus: Int,
    val fallbackPrice: String,
    val tag: String? = null
) {
    Handful("gems_100", "Handful", 100, 0, "4,99 zł"),
    Pouch("gems_550", "Pouch", 500, 50, "19,99 zł"),
    Sack("gems_1200", "Sack", 1000, 200, "39,99 zł", tag = "POPULAR"),
    Chest("gems_2600", "Chest", 2000, 600, "79,99 zł"),
    Vault("gems_7000", "Vault", 5000, 2000, "179,99 zł", tag = "BEST VALUE"),
    Hoard("gems_15000", "Dragon Hoard", 10000, 5000, "349,99 zł");

    val total: Int get() = base + bonus
    val bonusPercent: Int get() = bonus * 100 / base

    /** Diamenty przy PIERWSZYM zakupie tego pakietu - podwojone. */
    val firstPurchaseTotal: Int get() = total * 2

    /** Ile diamentow wyplacic, wiedzac, ktore pakiety gracz juz kiedys kupil. */
    fun grantFor(boughtBefore: Collection<String>): Int =
        if (productId in boughtBefore) total else firstPurchaseTotal
}

/**
 * Losy do Summon Center kupowane za diamenty - im wiekszy pakiet, tym tanszy los. Najmniejszy to
 * dokladnie jeden Heroiczny Summon (3 losy) po kursie [GemValue.TICKET]; epicka skrzynia daje losy
 * taniej, bo placi sie w niej jeszcze za szanse na jackpot.
 */
enum class TicketPack(val title: String, val tickets: Int, val gems: Int, val note: String? = null) {
    Few("Summon Pouch", 3, 45, "1 heroic summon"),
    Crate("Summon Crate", 30, 400, "-11%"),
    Hoard("Summon Hoard", 90, 1100, "-19%")
}
