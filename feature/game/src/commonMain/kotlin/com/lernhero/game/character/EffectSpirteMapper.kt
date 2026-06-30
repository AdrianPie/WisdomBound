package com.lernhero.game.character



import com.lernhero.shared.Resources
import com.lernhero.shared.SpriteAsset



fun effectSpriteFor(effectId: String): SpriteAsset =
    when (effectId) {
        EffectId.HIT.name -> Resources.Sprite.fireEffect
        EffectId.FIRE.name -> Resources.Sprite.fireEffect
        EffectId.POISON.name -> Resources.Sprite.fireEffect
        EffectId.HEAL.name -> Resources.Sprite.fireEffect
        else -> {Resources.Sprite.fireEffect}
    }