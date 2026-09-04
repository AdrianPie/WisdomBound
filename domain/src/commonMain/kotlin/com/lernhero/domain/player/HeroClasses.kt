package com.lernhero.domain.player

object HeroClasses {
    val Knight = HeroClass(
        id = "knight",
        isPlayer = true,
        hp = 50,
        maxHp = 50,
        attack = 10,

    )
    val Sorcerer = HeroClass(
        id = "sorcerer",
        isPlayer = true,
        hp = 30,
        maxHp = 30,
        attack = 15,
    )

}