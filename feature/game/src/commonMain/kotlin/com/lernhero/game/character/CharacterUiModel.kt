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
    val currentTargetId: String? = null,
    val flipSprite: Boolean = false,
    /**
     * When set, this fighter is rendered as the modular paper-doll hero (with equipment) instead
     * of a sprite sheet, so the gear equipped in the Equipment screen shows up in battle.
     */
    val knightEquipment: KnightEquipment? = null,
    /**
     * Visible-size multiplier relative to the battle card. Each sprite sheet frames its character
     * at a different scale, so this normalises every fighter to a consistent on-screen size.
     * Applied to the sprite width (or the paper-doll canvas for the hero).
     */
    val battleScale: Float = 1f
)
