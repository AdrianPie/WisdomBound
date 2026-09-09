package com.lernhero.domain.item.catalog

import com.lernhero.domain.combat.Effect
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.combat.effects.AllyAuraEffect
import com.lernhero.domain.combat.effects.BattleCryEffect
import com.lernhero.domain.combat.effects.CooldownReductionEffect
import com.lernhero.domain.combat.effects.CriticalChanceEffect
import com.lernhero.domain.combat.effects.CriticalDamageEffect
import com.lernhero.domain.combat.effects.EnemyAuraEffect
import com.lernhero.domain.combat.effects.HealAlliesEffect
import com.lernhero.domain.combat.effects.LifestealEffect
import com.lernhero.domain.combat.effects.SpeedEffect
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot

/**
 * Pierscienie i amulety - jedyne sloty, w ktorych przedmiot moze byc CZYSTA zdolnoscia.
 *
 * Bron i pancerz zawsze niosa ze soba liczby, wiec nie da sie zbudowac postaci wokol samego efektu.
 * Bizuteria ma tu wlasna role: prawie nie daje statystyk, za to kazdy wpis zmienia jedna regule
 * walki. To ona decyduje, czy druzyna gra na krytyki, na leczenie, czy na oslabianie wroga.
 *
 * Dlatego akcesoria sa tez jedynymi przedmiotami z AURAMI ([AllyAuraEffect]/[EnemyAuraEffect]) -
 * bohater jest jeden, a kompanow kilku, wiec efekt dzialajacy na cala druzyne musi wisiec na
 * bohaterze, inaczej mnozylby sie razy szesc.
 */
object AccessoriesExtra {

    private fun trinket(
        id: String,
        name: String,
        description: String,
        slot: ItemSlot,
        rarity: Rarity,
        iconKey: String,
        cost: Int,
        stats: Stats = Stats.Empty,
        effects: List<Effect> = emptyList()
    ) = Item(
        id = id,
        name = name,
        description = description,
        slot = slot,
        rarity = rarity,
        iconKey = iconKey,
        stats = stats,
        effects = effects,
        cost = cost
    )

    /* ------------------------------- Rough (Common) ------------------------------- */

    val COPPER_BAND = trinket(
        id = "copper_band",
        name = "Copper Band",
        description = "A beginner's ring. Steadies the hand, nothing more.",
        slot = ItemSlot.Ring,
        rarity = Rarity.Common,
        iconKey = "ring",
        cost = 85,
        effects = listOf(CriticalChanceEffect(percent = 0.05f))
    )

    val BONE_CHARM = trinket(
        id = "bone_charm",
        name = "Bone Charm",
        description = "Superstition with a small, measurable benefit.",
        slot = ItemSlot.Amulet,
        rarity = Rarity.Common,
        iconKey = "charm",
        cost = 90,
        stats = Stats.of(StatType.MaxHp to 10f)
    )

    /* ------------------------------ Sturdy (Uncommon) ----------------------------- */

    val LEECH_SIGNET = trinket(
        id = "leech_signet",
        name = "Leech Signet",
        description = "Turns a portion of what you deal into what you keep.",
        slot = ItemSlot.Ring,
        rarity = Rarity.Uncommon,
        iconKey = "ring",
        cost = 185,
        effects = listOf(LifestealEffect(percent = 0.12f))
    )

    val PENDANT_OF_THE_MEDIC = trinket(
        id = "pendant_of_the_medic",
        name = "Pendant of the Medic",
        description = "Patches up the two worst-off allies every other turn.",
        slot = ItemSlot.Amulet,
        rarity = Rarity.Uncommon,
        iconKey = "pendant",
        cost = 200,
        stats = Stats.of(StatType.HealPower to 0.2f),
        effects = listOf(HealAlliesEffect(targets = 2, amount = 10f, everyTurns = 2))
    )

    /* -------------------------------- Fine (Rare) --------------------------------- */

    val RING_OF_THE_DUELLIST = trinket(
        id = "ring_of_the_duellist",
        name = "Ring of the Duellist",
        description = "Fewer, better hits. Critical strikes land harder and more often.",
        slot = ItemSlot.Ring,
        rarity = Rarity.Rare,
        iconKey = "ring",
        cost = 300,
        effects = listOf(
            CriticalChanceEffect(percent = 0.12f),
            CriticalDamageEffect(multiplier = 0.35f)
        )
    )

    val TORC_OF_COMMAND = trinket(
        id = "torc_of_command",
        name = "Torc of Command",
        description = "The whole party hits a little harder just for having you in front.",
        slot = ItemSlot.Amulet,
        rarity = Rarity.Rare,
        iconKey = "torc",
        cost = 330,
        effects = listOf(
            AllyAuraEffect(
                id = "aura_command",
                description = "Allies gain +3 attack.",
                bonus = Stats.of(StatType.Attack to 3f)
            )
        )
    )

    /* ------------------------------- Master (Epic) -------------------------------- */

    val SIGIL_OF_DREAD = trinket(
        id = "sigil_of_dread",
        name = "Sigil of Dread",
        description = "Enemies swing weaker for as long as you are on the field.",
        slot = ItemSlot.Amulet,
        rarity = Rarity.Epic,
        iconKey = "sigil",
        cost = 470,
        effects = listOf(
            EnemyAuraEffect(
                id = "aura_dread",
                description = "Enemies lose 4 attack.",
                bonus = Stats.of(StatType.Attack to -4f)
            ),
            BattleCryEffect(stat = StatType.Defense, potency = 4f, turns = 4)
        )
    )

    val RING_OF_TEMPO = trinket(
        id = "ring_of_tempo",
        name = "Ring of Tempo",
        description = "You act sooner and your recurring effects come round faster.",
        slot = ItemSlot.Ring,
        rarity = Rarity.Epic,
        iconKey = "ring",
        cost = 450,
        effects = listOf(SpeedEffect(amount = 7f), CooldownReductionEffect(turns = 1))
    )

    /* ----------------------------- Mythic (Legendary) ----------------------------- */

    val HEART_OF_THE_GROVE = trinket(
        id = "heart_of_the_grove",
        name = "Heart of the Grove",
        description = "Keeps three allies standing every other turn, and everyone tougher throughout.",
        slot = ItemSlot.Amulet,
        rarity = Rarity.Legendary,
        iconKey = "heart",
        cost = 740,
        stats = Stats.of(StatType.HealPower to 0.35f),
        effects = listOf(
            HealAlliesEffect(targets = 3, amount = 18f, everyTurns = 2),
            AllyAuraEffect(
                id = "aura_grove",
                description = "Allies gain +20 max health.",
                bonus = Stats.of(StatType.MaxHp to 20f)
            )
        )
    )

    val SIGNET_OF_THE_FIRST_BLOW = trinket(
        id = "signet_of_the_first_blow",
        name = "Signet of the First Blow",
        description = "Opens every fight ahead: faster, angrier, and hitting critically far more often.",
        slot = ItemSlot.Ring,
        rarity = Rarity.Legendary,
        iconKey = "ring",
        cost = 770,
        effects = listOf(
            CriticalChanceEffect(percent = 0.18f),
            SpeedEffect(amount = 6f),
            BattleCryEffect(stat = StatType.Attack, potency = 7f, turns = 3)
        )
    )

    val all: List<Item> = listOf(
        COPPER_BAND, BONE_CHARM,
        LEECH_SIGNET, PENDANT_OF_THE_MEDIC,
        RING_OF_THE_DUELLIST, TORC_OF_COMMAND,
        SIGIL_OF_DREAD, RING_OF_TEMPO,
        HEART_OF_THE_GROVE, SIGNET_OF_THE_FIRST_BLOW
    )
}
