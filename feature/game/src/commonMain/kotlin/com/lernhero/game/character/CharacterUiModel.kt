package com.lernhero.game.character

import com.lernhero.domain.enum.CharacterState

data class CharacterUiModel(
    val id: String,
    val name: String = id,
    val hp: Int,
    val maxHp: Int,
    val attack: Int,
    val mana: Int,
    val isPlayer: Boolean,
    val sprites: CharacterSprites,
    val state: CharacterState = CharacterState.IDLE,
    val currentTargetId: String? = null
)
