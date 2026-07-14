package com.lernhero.domain.preset

object CharacterPreset {
    val Knight = Character(
        id = "knight",
        isPlayer = true,
        hp = 50,
        maxHp = 50,
        attack = 10,

    )
    val Sorcerer = Character(
        id = "sorcerer",
        isPlayer = true,
        hp = 30,
        maxHp = 30,
        attack = 15,
    )

}