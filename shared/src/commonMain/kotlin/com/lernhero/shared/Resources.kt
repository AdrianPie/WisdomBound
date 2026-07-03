package com.lernhero.shared

import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lernhero.shared.generated.resources.Res
import lernhero.shared.generated.resources.back_arrow
import lernhero.shared.generated.resources.battle_mage_avatar
import lernhero.shared.generated.resources.battlegroundTwo
import lernhero.shared.generated.resources.body2
import lernhero.shared.generated.resources.book
import lernhero.shared.generated.resources.cat
import lernhero.shared.generated.resources.check
import lernhero.shared.generated.resources.checkmark_image
import lernhero.shared.generated.resources.close
import lernhero.shared.generated.resources.delete
import lernhero.shared.generated.resources.dollar
import lernhero.shared.generated.resources.edit
import lernhero.shared.generated.resources.fire_sprite
import lernhero.shared.generated.resources.google_logo
import lernhero.shared.generated.resources.grid
import lernhero.shared.generated.resources.head
import lernhero.shared.generated.resources.home
import lernhero.shared.generated.resources.india
import lernhero.shared.generated.resources.knight_attack_sprite
import lernhero.shared.generated.resources.knight_avatar2
import lernhero.shared.generated.resources.knight_sprite
import lernhero.shared.generated.resources.rightx_down_hand
import lernhero.shared.generated.resources.rightx_down_leg
import lernhero.shared.generated.resources.rightx_up_hand
import lernhero.shared.generated.resources.rightx_up_leg
import lernhero.shared.generated.resources.leftx_foot
import lernhero.shared.generated.resources.right_foot
import lernhero.shared.generated.resources.plate_head
import lernhero.shared.generated.resources.plate_left_down_hand
import lernhero.shared.generated.resources.plate_left_down_leg
import lernhero.shared.generated.resources.plate_left_foot
import lernhero.shared.generated.resources.plate_left_up_leg
import lernhero.shared.generated.resources.plate_right_down_hand
import lernhero.shared.generated.resources.plate_right_down_leg
import lernhero.shared.generated.resources.plate_right_foot
import lernhero.shared.generated.resources.plate_right_up_leg
import lernhero.shared.generated.resources.plate_sword_down
import lernhero.shared.generated.resources.plate_sword_up
import lernhero.shared.generated.resources.log_in
import lernhero.shared.generated.resources.log_out
import lernhero.shared.generated.resources.logo_splash
import lernhero.shared.generated.resources.magskeleton_idle
import lernhero.shared.generated.resources.map_pin
import lernhero.shared.generated.resources.menu
import lernhero.shared.generated.resources.minus
import lernhero.shared.generated.resources.paypal_logo
import lernhero.shared.generated.resources.plus
import lernhero.shared.generated.resources.ramka_test
import lernhero.shared.generated.resources.ramka_test2
import lernhero.shared.generated.resources.right_arrow
import lernhero.shared.generated.resources.left_down_hand
import lernhero.shared.generated.resources.left_down_leg
import lernhero.shared.generated.resources.right_up_hand2
import lernhero.shared.generated.resources.left_up_leg
import lernhero.shared.generated.resources.search
import lernhero.shared.generated.resources.serbia
import lernhero.shared.generated.resources.shopping_cart
import lernhero.shared.generated.resources.shopping_cart_image
import lernhero.shared.generated.resources.sorcerer_avatar2
import lernhero.shared.generated.resources.swords_24px
import lernhero.shared.generated.resources.testattack
import lernhero.shared.generated.resources.unlock
import lernhero.shared.generated.resources.usa
import lernhero.shared.generated.resources.user
import lernhero.shared.generated.resources.vertical_menu
import lernhero.shared.generated.resources.warning
import lernhero.shared.generated.resources.weight
import org.jetbrains.compose.resources.DrawableResource

object Resources {
    object Icon {
        val Plus = Res.drawable.plus
        val Minus = Res.drawable.minus
        val SignIn = Res.drawable.log_in
        val SignOut = Res.drawable.log_out
        val Unlock = Res.drawable.unlock
        val Search = Res.drawable.search
        val Person = Res.drawable.user
        val Checkmark = Res.drawable.check
        val Edit = Res.drawable.edit
        val Menu = Res.drawable.menu
        val BackArrow = Res.drawable.back_arrow
        val RightArrow = Res.drawable.right_arrow
        val Home = Res.drawable.home
        val ShoppingCart = Res.drawable.shopping_cart
        val Categories = Res.drawable.grid
        val Dollar = Res.drawable.dollar
        val MapPin = Res.drawable.map_pin
        val Close = Res.drawable.close
        val Book = Res.drawable.book
        val VerticalMenu = Res.drawable.vertical_menu
        val Delete = Res.drawable.delete
        val Warning = Res.drawable.warning
        val Weight = Res.drawable.weight
        val swords = Res.drawable.swords_24px
    }
    object Image {
        val battlegroundFirst = Res.drawable.battlegroundTwo
        val ShoppingCart = Res.drawable.shopping_cart_image
        val Checkmark = Res.drawable.checkmark_image
        val logo = Res.drawable.logo_splash
        val Cat = Res.drawable.cat
        val GoogleLogo = Res.drawable.google_logo
        val PaypalLogo = Res.drawable.paypal_logo

        val boundsDragonArmor = Res.drawable.ramka_test
        val boundsDruidArmor = Res.drawable.ramka_test2

        val battleMageAvatarGame = Res.drawable.battle_mage_avatar
        val knightAvatar = Res.drawable.knight_avatar2
        val sorcererAvatar = Res.drawable.sorcerer_avatar2
    }
    object Player {
            val head = Res.drawable.head
            val body = Res.drawable.body2

            // Prawa reka (anatomicznie) - na ekranie po lewej, reka za cialem (idle)
            val rightUpHand = Res.drawable.rightx_up_hand
            val rightDownHand = Res.drawable.rightx_down_hand

            // Lewa reka (anatomicznie) - na ekranie po prawej, reka rozdzki/miecza z przodu.
            // Gorna czesc uzywa asset right_up_hand2 (najlepszy kawalek do uniesionej pozy).
            val leftUpHand = Res.drawable.right_up_hand2
            val leftDownHand = Res.drawable.left_down_hand

            // Prawa noga (anatomicznie) - na ekranie po lewej, noga z przodu
            val rightUpLeg = Res.drawable.rightx_up_leg
            val rightDownLeg = Res.drawable.rightx_down_leg
            val rightFoot = Res.drawable.right_foot

            // Lewa noga (anatomicznie) - na ekranie po prawej, noga z tylu
            val leftUpLeg = Res.drawable.left_up_leg
            val leftDownLeg = Res.drawable.left_down_leg
            val leftFoot = Res.drawable.leftx_foot
    }
    // Zbroja plytowa - nakladki rysowane na tych samych warstwach co czesci ciala
    object PlateArmor {
            val head = Res.drawable.plate_head

            val rightDownHand = Res.drawable.plate_right_down_hand
            val leftDownHand = Res.drawable.plate_left_down_hand

            val rightUpLeg = Res.drawable.plate_right_up_leg
            val rightDownLeg = Res.drawable.plate_right_down_leg
            val rightFoot = Res.drawable.plate_right_foot

            val leftUpLeg = Res.drawable.plate_left_up_leg
            val leftDownLeg = Res.drawable.plate_left_down_leg
            val leftFoot = Res.drawable.plate_left_foot

            // Miecz trzymany w lewej dloni: ostrze przed dlonia, rekojesc wystaje za nia
            val swordFront = Res.drawable.plate_sword_up
            val swordBehind = Res.drawable.plate_sword_down
    }
    object KnightPivots {
        // Centrum postaci
        val head = TransformOrigin(0.5f, 0.2f)
        val body = TransformOrigin(0.499f, 0.348f)
        // Punkt przy stopach - caly rycerz kolysze sie wokol niego, wiec stopy prawie sie nie ruszaja
        val ground = TransformOrigin(0.555f, 0.9f)

        // Prawa reka (anatomicznie) - Tyl, idle - na ekranie po lewej
        val rightUpHand = TransformOrigin(0.412f, 0.262f)
        val rightDownHand = TransformOrigin(0.354f, 0.359f)
        // Lewa reka (anatomicznie) - Przod, rozdzka/miecz - na ekranie po prawej
        val leftUpHand = TransformOrigin(0.573f, 0.276f)
        val leftDownHand = TransformOrigin(0.61f, 0.373f)

        // Prawa noga (anatomicznie) - Przod - na ekranie po lewej
        val rightUpLeg = TransformOrigin(0.461f, 0.5f)
        val rightDownLeg = TransformOrigin(0.417f, 0.677f)
        val rightFoot = TransformOrigin(0.387f, 0.852f)
        // Lewa noga (anatomicznie) - Tyl - na ekranie po prawej
        val leftUpLeg = TransformOrigin(0.542f, 0.523f)
        val leftDownLeg = TransformOrigin(0.575f, 0.679f)
        val leftFoot = TransformOrigin(0.595f, 0.85f)
    }
    object Flag {
        val India = Res.drawable.india
        val Usa = Res.drawable.usa
        val Serbia = Res.drawable.serbia
    }
    object Sprite{
        val knight: SpriteAsset = SpriteAsset(
            drawable = Res.drawable.knight_sprite,
            frameWidth = 1280,
            frameHeight = 720,
            totalFrames = 29,
            framesPerRow = 6,
            animationSpeed = 62,
            localWidth = 100.dp,
            renderScale = 1f,
            playBackwards = true
        )
        val knightAtk: SpriteAsset = SpriteAsset(
            drawable = Res.drawable.knight_attack_sprite,
            frameWidth = 1280,
            frameHeight = 720,
            totalFrames = 32,
            framesPerRow = 6,
            animationSpeed = 42,
            localWidth = 100.dp,
            renderScale = 1f,
        )
        val magSkeletoniIdle: SpriteAsset = SpriteAsset(
            drawable = Res.drawable.magskeleton_idle,
            frameWidth = 1280,
            frameHeight = 720,
            totalFrames = 18,
            framesPerRow = 5,
            animationSpeed = 42,
            localWidth = 100.dp,
            renderScale = 1f,
            pingPong = true,
        )
        val knightAtkTest: SpriteAsset = SpriteAsset(
            drawable = Res.drawable.testattack,
            frameWidth = 587,
            frameHeight = 707,
            totalFrames = 1,
            framesPerRow = 1,
            animationSpeed = 100,
            localWidth = 100.dp,
            renderScale = 1f
        )
        val fireEffect: SpriteAsset = SpriteAsset(
            drawable = Res.drawable.fire_sprite,
            frameWidth = 128,
            frameHeight = 128,
            totalFrames = 21,
            framesPerRow = 7,
            animationSpeed = 100,
            localWidth = 70.dp,
            renderScale = 1f
        )
    }
}
data class SpriteAsset(
    val drawable: DrawableResource,
    val frameWidth: Int,
    val frameHeight: Int,
    val totalFrames: Int,
    val framesPerRow: Int,
    val animationSpeed: Long,
    val localWidth: Dp,
    val renderScale: Float = 1f,
    val playBackwards: Boolean = false,
    val pingPong: Boolean = false,
){
    fun scaleWidthFactor(density: Density): Float {
        val frameWidthDp = with(density) { frameWidth.toDp() }
        return localWidth / frameWidthDp
    }
    val effectiveWidth: Dp
        get() = localWidth * renderScale
    val ratio: Float
        get() = frameHeight.toFloat() / frameWidth.toFloat()

    fun scaleFactorRelativeTo(
        other: SpriteAsset,
        factor: Float,
        density: Density
    ): Float {
        val frameWidthDp = with(density) { frameWidth.toDp() }
        val otherLogical = other.localWidth * factor
        return otherLogical / frameWidthDp
    }
}
