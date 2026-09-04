package com.lernhero.domain.item.catalog

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.monster.Element

/**
 * Sprzet dla kompanow - osobna linia od ekwipunku bohatera.
 *
 * Osobna, a nie wspolna pula, bo [com.lernhero.domain.player.Player.ownedItems] to lista ID bez
 * pojecia egzemplarza: gdyby ten sam miecz mogl nosic bohater ALBO kompan, trzeba by najpierw
 * przerobic caly zapis na sztuki i pilnowac, kto co ma na sobie. Osobny katalog daje to samo
 * uczucie ("wykuwam cos dla swojego kompana") bez migracji calego stanu gracza.
 *
 * Typ [Item] jest ten sam, co u bohatera - dzieki temu kuznia, koszty i sumowanie statystyk
 * dzialaja bez ani jednej nowej galezi. Rozni je wylacznie katalog, w ktorym siedza.
 *
 * Kompani uzywaja SZESCIU slotow (pazur, pancerz, helm, buty, amulet, pierscien) - reszta slotow
 * bohatera nie ma dla nich sensu i celowo zostaje pusta.
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
        stats = Stats.of(StatType.Attack to 4f),
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
        stats = Stats.of(StatType.Attack to 6f, StatType.CritChance to 0.03f),
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
        stats = Stats.of(StatType.Attack to 8f, StatType.Speed to 3f),
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
        stats = Stats.of(StatType.Attack to 7f, StatType.Lifesteal to 0.05f),
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
        stats = Stats.of(StatType.MaxHp to 25f),
        cost = 100
    )

    val CHITIN_PLATE = gear(
        id = "chitin_plate",
        name = "Chitin Plate",
        description = "Hard as stone, light as an insect.",
        slot = ItemSlot.Chest,
        rarity = Rarity.Uncommon,
        iconKey = "chitin",
        stats = Stats.of(StatType.MaxHp to 45f, StatType.DamageReduction to 0.03f),
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
        stats = Stats.of(StatType.MaxHp to 60f, StatType.Lifesteal to 0.04f),
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
        stats = Stats.of(StatType.MaxHp to 18f, StatType.Attack to 2f),
        cost = 95
    )

    val EMBER_MASK = gear(
        id = "ember_mask",
        name = "Ember Mask",
        description = "Breathes smoke and it does nothing to it.",
        slot = ItemSlot.Head,
        rarity = Rarity.Uncommon,
        iconKey = "mask",
        stats = Stats.of(StatType.Attack to 5f, StatType.CritDamage to 0.10f),
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
        stats = Stats.of(StatType.SpellPower to 0.08f, StatType.MaxHp to 20f),
        cost = 230
    )

    val TIDE_PEARL = gear(
        id = "tide_pearl",
        name = "Tide Pearl",
        description = "Cool to the touch no matter the weather.",
        slot = ItemSlot.Amulet,
        rarity = Rarity.Rare,
        iconKey = "pearl",
        stats = Stats.of(StatType.HealPower to 0.15f, StatType.MaxHp to 35f),
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
        stats = Stats.of(StatType.Lifesteal to 0.06f, StatType.Attack to 3f),
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
        stats = Stats.of(StatType.MaxHp to 30f, StatType.HealPower to 0.10f),
        cost = 205,
        element = Element.Nature
    )

    val all: List<Item> = listOf(
        BONE_CLAW, EMBER_FANG, TIDE_SPINE, THORN_LASH,
        HIDE_WRAP, CHITIN_PLATE, GRAVE_SHROUD,
        HORN_CROWN, EMBER_MASK, SWIFT_PAWS, DEEP_FINS,
        SPIRIT_TOTEM, TIDE_PEARL, BLOOD_BAND, SEED_RING
    )

    private val byId: Map<String, Item> = all.associateBy { it.id }

    fun byId(id: String): Item? = byId[id]

    /** Czy to id nalezy do sprzetu kompana - odroznia obie pule bez osobnego pola w [Item]. */
    fun isMonsterGear(id: String): Boolean = id.startsWith(ID_PREFIX)

    const val ID_PREFIX = "mg_"
}
