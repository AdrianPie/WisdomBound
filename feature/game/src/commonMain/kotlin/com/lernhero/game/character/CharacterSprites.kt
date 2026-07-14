package com.lernhero.game.character



import com.lernhero.shared.resources.SpriteAsset

data class CharacterSprites(
    val idle: SpriteAsset,
    val attack: SpriteAsset,
    val hit: SpriteAsset,
    val dead: SpriteAsset
) {
    fun forState(state: CharacterState): SpriteAsset =
        when (state) {
            CharacterState.IDLE -> idle
            CharacterState.MOVING_TO_ATK -> idle
            CharacterState.ATTACK -> attack
            CharacterState.MOVING_BACK -> idle
            CharacterState.HIT -> hit
            CharacterState.DEAD -> dead
        }
}
