package com.lernhero.game.character

import com.lernhero.shared.resources.Resources
import com.lernhero.shared.resources.SpriteAsset

/**
 * Maps a monster/character sprite key (as stored on [com.lernhero.domain.monster.MonsterCard.spriteKey]
 * and used by the battle) to its animated sprite set. Single source of truth for both the battle
 * ([com.lernhero.game.BattleViewModel]) and the party/shop portraits in the home feature.
 */
object MonsterSprites {
    fun spritesFor(key: String): CharacterSprites = when (key) {
        "mag" -> CharacterSprites(
            idle = Resources.Sprite.magSkeletoniIdle,
            attack = Resources.Sprite.magSkeletoniIdle,
            hit = Resources.Sprite.magSkeletoniIdle,
            dead = Resources.Sprite.magSkeletoniIdle
        )

        "knight" -> CharacterSprites(
            idle = Resources.Sprite.knight,
            attack = Resources.Sprite.knightAtk,
            hit = Resources.Sprite.knight,
            dead = Resources.Sprite.knight
        )

        "skeleton" -> CharacterSprites(
            idle = Resources.Sprite.skeletonIdle,
            attack = Resources.Sprite.skeletonAttack,
            hit = Resources.Sprite.skeletonHit,
            dead = Resources.Sprite.skeletonDead
        )

        "wolf" -> CharacterSprites(
            idle = Resources.Sprite.wolfIdle,
            attack = Resources.Sprite.wolfAttack,
            hit = Resources.Sprite.wolfHit,
            dead = Resources.Sprite.wolfDead
        )

        else -> CharacterSprites(
            idle = Resources.Sprite.knight,
            attack = Resources.Sprite.knightAtk,
            hit = Resources.Sprite.knight,
            dead = Resources.Sprite.knight
        )
    }

    fun idleFor(key: String): SpriteAsset = spritesFor(key).idle
}
