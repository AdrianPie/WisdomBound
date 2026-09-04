package com.lernhero.game.character

data class KnightEquipment(
    val head: Boolean = false,
    val chest: Boolean = false,
    val hands: Boolean = false,
    val legs: Boolean = false,
    val feet: Boolean = false,
    val mainHand: Boolean = false,
) {
    companion object {
        val Empty = KnightEquipment()
        val FullPlate = KnightEquipment(
            head = true,
            chest = true,
            hands = true,
            legs = true,
            feet = true,
            mainHand = true,
        )
    }
}

enum class KnightEquipmentSlot(
    val label: String,
    val shortLabel: String,
) {
    Head("Head", "Head"),
    Neck("Neck", "Neck"),
    Shoulder("Shoulder", "Shldr"),
    Back("Back", "Back"),
    Chest("Chest", "Chest"),
    Hands("Hands", "Hands"),
    Waist("Waist", "Waist"),
    Legs("Legs", "Legs"),
    Feet("Feet", "Feet"),
    MainHand("Main Hand", "Main"),
    OffHand("Off Hand", "Off"),
    ;

    val isEquippable: Boolean
        get() = when (this) {
            Head, Chest, Hands, Legs, Feet, MainHand -> true
            else -> false
        }

    fun toggle(equipment: KnightEquipment): KnightEquipment = when (this) {
        Head -> equipment.copy(head = !equipment.head)
        Chest -> equipment.copy(chest = !equipment.chest)
        Hands -> equipment.copy(hands = !equipment.hands)
        Legs -> equipment.copy(legs = !equipment.legs)
        Feet -> equipment.copy(feet = !equipment.feet)
        MainHand -> equipment.copy(mainHand = !equipment.mainHand)
        else -> equipment
    }

    fun isEquipped(equipment: KnightEquipment): Boolean = when (this) {
        Head -> equipment.head
        Chest -> equipment.chest
        Hands -> equipment.hands
        Legs -> equipment.legs
        Feet -> equipment.feet
        MainHand -> equipment.mainHand
        else -> false
    }
}
