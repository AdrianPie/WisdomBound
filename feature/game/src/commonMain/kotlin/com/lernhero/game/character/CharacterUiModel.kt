package com.lernhero.game.character


data class CharacterUiModel(
    val id: String,
    val name: String = id,
    val hp: Int,
    val maxHp: Int,
    val attack: Int,
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
    val battleScale: Float = 1f,
    /**
     * Jak daleko ten wrog jest jeszcze od linii starcia: 1f = poza ekranem, 0f = dotarl i bije.
     * Bez znaczenia dla graczy - stoja w miejscu.
     */
    val approach: Float = 0f
)
