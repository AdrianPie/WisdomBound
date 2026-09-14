package com.lernhero.domain.item.catalog

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.monster.CompanionPower
import com.lernhero.domain.monster.Element

/**
 * Sprzet dla kompanow - osobna linia od ekwipunku bohatera.
 *
 * Osobna, a nie wspolna pula - ale juz nie z powodu zapisu. Plecak liczy dzis SZTUKI
 * ([com.lernhero.domain.item.ItemInstance]), wiec technicznie nic nie stoi na przeszkodzie, zeby
 * bohater i kompan siegali do jednej puli. Zostaje podzial PROJEKTOWY: sprzet kompana ma wlasne
 * sloty, wlasna premie za zywiol i wlasne tempo zdobywania, a wspolna pula zamienilaby wybor
 * "dla kogo kuje" w licytacje o te same przedmioty.
 *
 * Typ [Item] jest ten sam, co u bohatera - dzieki temu kuznia, koszty i sumowanie statystyk
 * dzialaja bez ani jednej nowej galezi. Rozni je wylacznie katalog, w ktorym siedza.
 *
 * Kompani uzywaja SZESCIU slotow (pazur, pancerz, helm, buty, amulet, pierscien) - reszta slotow
 * bohatera nie ma dla nich sensu i celowo zostaje pusta.
 *
 * ## W jakiej skali sa te liczby
 *
 * W skali WALKI, czyli tej samej co statystyki bohatera - patrz [CompanionPower]. Karta kompana
 * w katalogu jest opisana "malymi" liczbami (55-100 HP), ale do walki wchodzi przemnozona; sprzet
 * dolicza sie juz PO tym przeskalowaniu. Pancerz +84 HP to wiec +40% na kompanie majacym 210 HP,
 * a nie +120% na karcie majacej 70.
 *
 * ## Dlaczego liczby sa o 30% nizsze, niz byly
 *
 * Bo sprzetu kompanow jest SZESC slotow razy PIECIU kompanow - trzydziesci sztuk przeciwko
 * dziewieciu u bohatera. Przy pierwotnych wartosciach pelny sprzet druzyny dawal +102% HP i +64%
 * ataku, przy +24%/+14% z calego kompletu bohatera podniesionego na +10. Cztery razy wiecej za
 * piec razy wiekszy koszt to prawie proporcja, ale sprzet bohatera przestawalby byc pierwszym
 * wyborem. Po przycieciu wychodzi ok. +71%/+45%, czyli **trzy razy komplet bohatera za piec razy
 * wiekszy koszt** - kompani zostaja najwiekszym pojedynczym systemem w grze (i maja nim byc, bo
 * sa najdrozsi), ale kuje sie najpierw dla bohatera.
 *
 * Na sama kampanie nie ma to wplywu: ogranicznikiem jest tam RUDA, nie sila przedmiotu. Do konca
 * Normalnego starcza jej na komplet Common dla bohatera i sprzet dla dwoch kompanow z pieciu.
 */
object MonsterGear {

    /** Sloty, ktore kompan faktycznie nosi. Ekran ekwipunku kompana rysuje dokladnie te i tylko te. */
    val SLOTS: List<ItemSlot> = listOf(
        ItemSlot.MainHand,
        ItemSlot.Chest,
        ItemSlot.Head,
        ItemSlot.Feet,
        ItemSlot.Amulet,
        ItemSlot.Ring
    )

    /**
     * Skrot na wpis katalogu. [element] wiaze przedmiot z zywiolem kompana - zalozony na kompanie
     * TEGO SAMEGO zywiolu daje pelnie statystyk (patrz [matchBonus]); null = pasuje do kazdego.
     */
    private fun gear(
        id: String,
        name: String,
        description: String,
        slot: ItemSlot,
        rarity: Rarity,
        iconKey: String,
        stats: Stats,
        cost: Int,
        element: Element? = null
    ) = Item(
        id = "mg_$id",
        name = name,
        description = description + (element?.let { " Full power on a companion of the ${it.label} element." } ?: ""),
        slot = slot,
        rarity = rarity,
        iconKey = iconKey,
        stats = stats,
        cost = cost
    ).also { elementOf[it.id] = element }

    /** Zywiol przypisany przedmiotowi (null = uniwersalny). Trzymane obok, zeby nie puchl typ [Item]. */
    private val elementOf = mutableMapOf<String, Element?>()

    fun elementFor(itemId: String): Element? = elementOf[itemId]

    /**
     * Premia za zgodnosc zywiolu przedmiotu i kompana. Przedmiot uniwersalny zawsze daje 1.0,
     * dopasowany [MATCH_BONUS], niedopasowany [MISMATCH_PENALTY] - ta sama logika, co przy
     * przewagach w walce, tylko na statystykach zamiast obrazen.
     */
    fun matchBonus(itemId: String, monster: Element?): Float {
        val itemElement = elementFor(itemId) ?: return 1f
        if (monster == null) return 1f
        return if (itemElement == monster) MATCH_BONUS else MISMATCH_PENALTY
    }

    const val MATCH_BONUS = 1.2f
    const val MISMATCH_PENALTY = 0.85f

    /* --------------------------------- Bron (pazury) --------------------------------- */

    val BONE_CLAW = gear(
        id = "bone_claw",
        name = "Bone Claw",
        description = "Razor-sharp, torn from something no longer alive.",
        slot = ItemSlot.MainHand,
        rarity = Rarity.Common,
        iconKey = "claw",
        stats = Stats.of(StatType.Attack to 3f),
        cost = 90,
        element = Element.Death
    )

    val EMBER_FANG = gear(
        id = "ember_fang",
        name = "Ember Fang",
        description = "A fang that hasn't cooled since the last fire.",
        slot = ItemSlot.MainHand,
        rarity = Rarity.Uncommon,
        iconKey = "fang",
        stats = Stats.of(StatType.Attack to 4f, StatType.CritChance to 0.03f),
        cost = 180,
        element = Element.Fire
    )

    val TIDE_SPINE = gear(
        id = "tide_spine",
        name = "Tide Spine",
        description = "Always wet, even in the desert.",
        slot = ItemSlot.MainHand,
        rarity = Rarity.Rare,
        iconKey = "spine",
        stats = Stats.of(StatType.Attack to 6f, StatType.Speed to 3f),
        cost = 320,
        element = Element.Water
    )

    val THORN_LASH = gear(
        id = "thorn_lash",
        name = "Thorn Lash",
        description = "A living vine that tightens on its own.",
        slot = ItemSlot.MainHand,
        rarity = Rarity.Rare,
        iconKey = "lash",
        stats = Stats.of(StatType.Attack to 5f, StatType.Lifesteal to 0.05f),
        cost = 300,
        element = Element.Nature
    )

    /* ------------------------------------ Pancerz ------------------------------------ */

    val HIDE_WRAP = gear(
        id = "hide_wrap",
        name = "Hide Wrap",
        description = "Simple protection - better than nothing, and cheaper than everything else.",
        slot = ItemSlot.Chest,
        rarity = Rarity.Common,
        iconKey = "hide",
        stats = Stats.of(StatType.MaxHp to 18f),
        cost = 100
    )

    val CHITIN_PLATE = gear(
        id = "chitin_plate",
        name = "Chitin Plate",
        description = "Hard as stone, light as an insect.",
        slot = ItemSlot.Chest,
        rarity = Rarity.Uncommon,
        iconKey = "chitin",
        stats = Stats.of(StatType.MaxHp to 31f, StatType.DamageReduction to 0.03f),
        cost = 210,
        element = Element.Nature
    )

    val GRAVE_SHROUD = gear(
        id = "grave_shroud",
        name = "Grave Shroud",
        description = "Fabric that has seen more funerals than the living.",
        slot = ItemSlot.Chest,
        rarity = Rarity.Rare,
        iconKey = "shroud",
        stats = Stats.of(StatType.MaxHp to 42f, StatType.Lifesteal to 0.04f),
        cost = 340,
        element = Element.Death
    )

    /* ----------------------------------- Helm i buty ---------------------------------- */

    val HORN_CROWN = gear(
        id = "horn_crown",
        name = "Horned Crown",
        description = "The bigger the horn, the fewer the questions.",
        slot = ItemSlot.Head,
        rarity = Rarity.Common,
        iconKey = "horns",
        stats = Stats.of(StatType.MaxHp to 13f, StatType.Attack to 1f),
        cost = 95
    )

    val EMBER_MASK = gear(
        id = "ember_mask",
        name = "Ember Mask",
        description = "Breathes smoke and it does nothing to it.",
        slot = ItemSlot.Head,
        rarity = Rarity.Uncommon,
        iconKey = "mask",
        stats = Stats.of(StatType.Attack to 4f, StatType.CritDamage to 0.10f),
        cost = 200,
        element = Element.Fire
    )

    val SWIFT_PAWS = gear(
        id = "swift_paws",
        name = "Swift Paws",
        description = "This companion strikes first, because it simply got there first.",
        slot = ItemSlot.Feet,
        rarity = Rarity.Common,
        iconKey = "paws",
        stats = Stats.of(StatType.Speed to 6f),
        cost = 110
    )

    val DEEP_FINS = gear(
        id = "deep_fins",
        name = "Deep Fins",
        description = "They look silly on land - not so much in battle.",
        slot = ItemSlot.Feet,
        rarity = Rarity.Uncommon,
        iconKey = "fins",
        stats = Stats.of(StatType.Speed to 9f, StatType.Dodge to 0.03f),
        cost = 190,
        element = Element.Water
    )

    /* --------------------------------- Bizuteria totemy -------------------------------- */

    val SPIRIT_TOTEM = gear(
        id = "spirit_totem",
        name = "Spirit Totem",
        description = "Whispers advice nobody asked for.",
        slot = ItemSlot.Amulet,
        rarity = Rarity.Uncommon,
        iconKey = "totem",
        stats = Stats.of(StatType.SpellPower to 0.08f, StatType.MaxHp to 14f),
        cost = 230
    )

    val TIDE_PEARL = gear(
        id = "tide_pearl",
        name = "Tide Pearl",
        description = "Cool to the touch no matter the weather.",
        slot = ItemSlot.Amulet,
        rarity = Rarity.Rare,
        iconKey = "pearl",
        stats = Stats.of(StatType.HealPower to 0.15f, StatType.MaxHp to 24f),
        cost = 360,
        element = Element.Water
    )

    val BLOOD_BAND = gear(
        id = "blood_band",
        name = "Blood Band",
        description = "Every hit gives back a share of what it took.",
        slot = ItemSlot.Ring,
        rarity = Rarity.Rare,
        iconKey = "band",
        stats = Stats.of(StatType.Lifesteal to 0.06f, StatType.Attack to 2f),
        cost = 330,
        element = Element.Death
    )

    val SEED_RING = gear(
        id = "seed_ring",
        name = "Seed Ring",
        description = "Grows along with whoever wears it.",
        slot = ItemSlot.Ring,
        rarity = Rarity.Uncommon,
        iconKey = "seed",
        stats = Stats.of(StatType.MaxHp to 21f, StatType.HealPower to 0.10f),
        cost = 205,
        element = Element.Nature
    )


    /* ------------------------------ Master (Epic) ------------------------------ */
    //
    // Sprzet kompana celowo zostaje przy CZYSTYCH STATYSTYKACH nawet na wysokich progach.
    // Zdolnosci sa dzialka bohatera: jest jeden, wiec jego efekty da sie zbalansowac. Kompanow
    // bywa szesciu i kazdy efekt mnozylby sie razy szesc - z "20% szansy na podwojny atak"
    // robi sie wtedy druzyna atakujaca dwa razy na ture. Kompani skaluja LICZBY, bohater REGULY.

    val DRAGON_TALON = gear(
        id = "dragon_talon",
        name = "Dragon Talon",
        description = "Torn from something that was still burning when it died.",
        slot = ItemSlot.MainHand,
        rarity = Rarity.Epic,
        iconKey = "talon",
        stats = Stats.of(StatType.Attack to 8f, StatType.CritChance to 0.05f),
        cost = 460,
        element = Element.Fire
    )

    val ABYSS_HOOK = gear(
        id = "abyss_hook",
        name = "Abyss Hook",
        description = "Drags things down. Everything comes back up lighter.",
        slot = ItemSlot.MainHand,
        rarity = Rarity.Epic,
        iconKey = "hook",
        stats = Stats.of(StatType.Attack to 7f, StatType.Lifesteal to 0.08f),
        cost = 450,
        element = Element.Water
    )

    val BARKHIDE_SHELL = gear(
        id = "barkhide_shell",
        name = "Barkhide Shell",
        description = "Living bark. Closes over wounds between fights.",
        slot = ItemSlot.Chest,
        rarity = Rarity.Epic,
        iconKey = "bark",
        stats = Stats.of(StatType.MaxHp to 59f, StatType.DamageReduction to 0.06f),
        cost = 480,
        element = Element.Nature
    )

    val TOMB_HELM = gear(
        id = "tomb_helm",
        name = "Tomb Helm",
        description = "Cold to the touch, and heavier than it looks.",
        slot = ItemSlot.Head,
        rarity = Rarity.Epic,
        iconKey = "helm",
        stats = Stats.of(StatType.Attack to 6f, StatType.MaxHp to 28f),
        cost = 440,
        element = Element.Death
    )

    val GALE_HOOVES = gear(
        id = "gale_hooves",
        name = "Gale Hooves",
        description = "Ground barely involved.",
        slot = ItemSlot.Feet,
        rarity = Rarity.Epic,
        iconKey = "hooves",
        stats = Stats.of(StatType.Speed to 14f, StatType.Dodge to 0.06f),
        cost = 420
    )

    val EMBERHEART_CHARM = gear(
        id = "emberheart_charm",
        name = "Emberheart Charm",
        description = "A coal that has refused to go out for a very long time.",
        slot = ItemSlot.Amulet,
        rarity = Rarity.Epic,
        iconKey = "charm",
        stats = Stats.of(StatType.SpellPower to 0.18f, StatType.Attack to 4f),
        cost = 455,
        element = Element.Fire
    )

    val IRONWOOD_RING = gear(
        id = "ironwood_ring",
        name = "Ironwood Ring",
        description = "Grown, not forged. It shows.",
        slot = ItemSlot.Ring,
        rarity = Rarity.Epic,
        iconKey = "ring",
        stats = Stats.of(StatType.MaxHp to 38f, StatType.DamageReduction to 0.05f),
        cost = 430,
        element = Element.Nature
    )

    /* ---------------------------- Mythic (Legendary) ---------------------------- */

    val WORLDEATER_MAW = gear(
        id = "worldeater_maw",
        name = "Worldeater Maw",
        description = "The single hardest thing a companion can carry into a fight.",
        slot = ItemSlot.MainHand,
        rarity = Rarity.Legendary,
        iconKey = "maw",
        stats = Stats.of(
            StatType.Attack to 11f,
            StatType.CritChance to 0.08f,
            StatType.CritDamage to 0.15f
        ),
        cost = 700
    )

    val AEONPLATE = gear(
        id = "aeonplate",
        name = "Aeonplate",
        description = "Outlasts the battle, the war, and usually the enemy.",
        slot = ItemSlot.Chest,
        rarity = Rarity.Legendary,
        iconKey = "plate",
        stats = Stats.of(StatType.MaxHp to 84f, StatType.DamageReduction to 0.10f),
        cost = 740
    )

    val CROWN_OF_TIDES = gear(
        id = "crown_of_tides",
        name = "Crown of Tides",
        description = "The sea keeps what it takes, and gives some of it back.",
        slot = ItemSlot.Head,
        rarity = Rarity.Legendary,
        iconKey = "crown",
        stats = Stats.of(StatType.MaxHp to 46f, StatType.HealPower to 0.25f, StatType.Attack to 5f),
        cost = 690,
        element = Element.Water
    )

    val SOULBINDER_BAND = gear(
        id = "soulbinder_band",
        name = "Soulbinder Band",
        description = "Every wound dealt is a wound repaid.",
        slot = ItemSlot.Ring,
        rarity = Rarity.Legendary,
        iconKey = "ring",
        stats = Stats.of(StatType.Lifesteal to 0.14f, StatType.Attack to 6f),
        cost = 710,
        element = Element.Death
    )

    val TOTEM_OF_AGES = gear(
        id = "totem_of_ages",
        name = "Totem of Ages",
        description = "Older than the forest it came from.",
        slot = ItemSlot.Amulet,
        rarity = Rarity.Legendary,
        iconKey = "totem",
        stats = Stats.of(
            StatType.SpellPower to 0.28f,
            StatType.HealPower to 0.2f,
            StatType.MaxHp to 42f
        ),
        cost = 730
    )

    val STORMSTRIDE_CLAWS = gear(
        id = "stormstride_claws",
        name = "Stormstride Claws",
        description = "Fast enough that the first blow is usually yours.",
        slot = ItemSlot.Feet,
        rarity = Rarity.Legendary,
        iconKey = "claws",
        stats = Stats.of(StatType.Speed to 20f, StatType.Dodge to 0.1f, StatType.Attack to 4f),
        cost = 680
    )

    val all: List<Item> = listOf(
        BONE_CLAW, EMBER_FANG, TIDE_SPINE, THORN_LASH,
        HIDE_WRAP, CHITIN_PLATE, GRAVE_SHROUD,
        HORN_CROWN, EMBER_MASK, SWIFT_PAWS, DEEP_FINS,
        SPIRIT_TOTEM, TIDE_PEARL, BLOOD_BAND, SEED_RING,
        DRAGON_TALON, ABYSS_HOOK, BARKHIDE_SHELL, TOMB_HELM, GALE_HOOVES,
        EMBERHEART_CHARM, IRONWOOD_RING,
        WORLDEATER_MAW, AEONPLATE, CROWN_OF_TIDES, SOULBINDER_BAND,
        TOTEM_OF_AGES, STORMSTRIDE_CLAWS
    )

    private val byId: Map<String, Item> = all.associateBy { it.id }

    fun byId(id: String): Item? = byId[id]

    /** Czy to id nalezy do sprzetu kompana - odroznia obie pule bez osobnego pola w [Item]. */
    fun isMonsterGear(id: String): Boolean = id.startsWith(ID_PREFIX)

    const val ID_PREFIX = "mg_"
}
