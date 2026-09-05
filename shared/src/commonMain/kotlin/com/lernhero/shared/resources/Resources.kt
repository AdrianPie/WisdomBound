package com.lernhero.shared.resources

import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lernhero.shared.generated.resources.Res
import lernhero.shared.generated.resources.back_arrow
import lernhero.shared.generated.resources.background
import lernhero.shared.generated.resources.bloody_mines
import lernhero.shared.generated.resources.forgotten_forest
import lernhero.shared.generated.resources.frozen_wastes
import lernhero.shared.generated.resources.battle_mage_avatar
import lernhero.shared.generated.resources.brick_dungeon
import lernhero.shared.generated.resources.battlegroundTwo
import lernhero.shared.generated.resources.book
import lernhero.shared.generated.resources.cat
import lernhero.shared.generated.resources.check
import lernhero.shared.generated.resources.checkmark_image
import lernhero.shared.generated.resources.close
import lernhero.shared.generated.resources.delete
import lernhero.shared.generated.resources.dollar
import lernhero.shared.generated.resources.edit
import lernhero.shared.generated.resources.game_map
import lernhero.shared.generated.resources.google_logo
import lernhero.shared.generated.resources.grid
import lernhero.shared.generated.resources.home
import lernhero.shared.generated.resources.india
import lernhero.shared.generated.resources.knight_avatar2
import lernhero.shared.generated.resources.altar_empty
import lernhero.shared.generated.resources.armor_new
import lernhero.shared.generated.resources.body_new
import lernhero.shared.generated.resources.boss_skeleton_dragon_2
import lernhero.shared.generated.resources.magic_missle_1
import lernhero.shared.generated.resources.magic_missle_2
import lernhero.shared.generated.resources.magic_missle_3
import lernhero.shared.generated.resources.boss_skeleton_dragon_body
import lernhero.shared.generated.resources.boss_skeleton_dragon_up_skull
import lernhero.shared.generated.resources.boss_skeleton_dragon_down_skull
import lernhero.shared.generated.resources.human_archer_2
import lernhero.shared.generated.resources.mage_missile_001_016
import lernhero.shared.generated.resources.magicboom_8_frames
import lernhero.shared.generated.resources.magic_missle_skeleton_mage
import lernhero.shared.generated.resources.orc_archer_2
import lernhero.shared.generated.resources.orc_warrior_2
import lernhero.shared.generated.resources.skeleton_archer_2
import lernhero.shared.generated.resources.skeleton_mage_2
import lernhero.shared.generated.resources.skeleton_warrior_2
import lernhero.shared.generated.resources.boot_left_new
import lernhero.shared.generated.resources.boot_right_new
import lernhero.shared.generated.resources.glove_left_new
import lernhero.shared.generated.resources.glove_right_new
import lernhero.shared.generated.resources.hair_new
import lernhero.shared.generated.resources.head_new
import lernhero.shared.generated.resources.left_hand_new
import lernhero.shared.generated.resources.left_leg_new
import lernhero.shared.generated.resources.right_hand_new
import lernhero.shared.generated.resources.right_leg_new
import lernhero.shared.generated.resources.sword_down_new
import lernhero.shared.generated.resources.sword_up_new
import lernhero.shared.generated.resources.log_in
import lernhero.shared.generated.resources.log_out
import lernhero.shared.generated.resources.logo_splash
import lernhero.shared.generated.resources.map_pin
import lernhero.shared.generated.resources.menu
import lernhero.shared.generated.resources.mine_island
import lernhero.shared.generated.resources.minus
import lernhero.shared.generated.resources.paypal_logo
import lernhero.shared.generated.resources.plus
import lernhero.shared.generated.resources.ramka_test
import lernhero.shared.generated.resources.ramka_test2
import lernhero.shared.generated.resources.right_arrow
import lernhero.shared.generated.resources.search
import lernhero.shared.generated.resources.serbia
import lernhero.shared.generated.resources.shopping_cart
import lernhero.shared.generated.resources.shopping_cart_image
import lernhero.shared.generated.resources.smite_island
import lernhero.shared.generated.resources.sorcerer_avatar2
import lernhero.shared.generated.resources.summon_island
import lernhero.shared.generated.resources.swords_24px
import lernhero.shared.generated.resources.tartak_island
import lernhero.shared.generated.resources.treasure
import lernhero.shared.generated.resources.unlock
import lernhero.shared.generated.resources.usa
import lernhero.shared.generated.resources.user
import lernhero.shared.generated.resources.vertical_menu
import lernhero.shared.generated.resources.warning
import lernhero.shared.generated.resources.weight
import lernhero.shared.generated.resources.nav_home
import lernhero.shared.generated.resources.nav_growth
import lernhero.shared.generated.resources.nav_fight
import lernhero.shared.generated.resources.nav_progress
import lernhero.shared.generated.resources.nav_shop
import lernhero.shared.generated.resources.summon_banner_basic
import lernhero.shared.generated.resources.summon_banner_heroic

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

    /**
     * Ikony dolnego paska nawigacji. Na razie PLACEHOLDERY - podmien pliki
     * `shared/src/commonMain/composeResources/drawable/nav_*.png` na docelowe grafiki,
     * nazwa pliku zostaje ta sama i nic w kodzie nie trzeba ruszac.
     */
    object NavIcon {
        val Home = Res.drawable.nav_home
        val Growth = Res.drawable.nav_growth
        val Fight = Res.drawable.nav_fight
        val Progress = Res.drawable.nav_progress
        val Shop = Res.drawable.nav_shop
    }

    /**
     * Ikony bannerow w Summon Center. Na razie PLACEHOLDERY - podmien pliki
     * `drawable/summon_banner_*.png` na docelowe posagi/figury, kod zostaje bez zmian.
     */
    object SummonBanner {
        val Basic = Res.drawable.summon_banner_basic
        val Heroic = Res.drawable.summon_banner_heroic
    }
    object Image {
        /** Tlo calej aplikacji - rysowane raz za Scaffoldem w HomeNavGraph, widoczne pod kazda zakladka. */
        val appBackground = Res.drawable.background

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

        val dungeonMap = Res.drawable.game_map
        val corridorBrick = Res.drawable.brick_dungeon
        val treasureChest = Res.drawable.treasure

        /** Tla krain story mode - podpiete w [com.lernhero.home.story.StoryWorldArt]. */
        val worldForest = Res.drawable.forgotten_forest
        val worldMine = Res.drawable.bloody_mines
        val worldIcefield = Res.drawable.frozen_wastes
    }
    /** Wyspy-budynki na ekranie Miasta (Dom) - kazda to osobna klikalna "instancja" (patrz HomeScreen). */
    object CityIsland {
        /** Pusty oltarz w Summon Center - scena, na ktorej "wychodzi" wylosowany summon. */
        val altar = Res.drawable.altar_empty
        val sawmill = Res.drawable.tartak_island
        val quarry = Res.drawable.mine_island
        val forge = Res.drawable.smite_island
        val summon = Res.drawable.summon_island
    }
    object Player {
            val head = Res.drawable.head_new
            val body = Res.drawable.body_new

            /**
             * Same wlosy, wyciete z [head] i sprowadzone do bieli (zachowany tylko relatywny cien).
             * Rysuje sie JE NA glowie i mnozy przez wybrany kolor - dzieki temu farbowanie wlosow
             * nie wymaga osobnego sprite'a na kazdy kolor. Wygenerowane z head_new.png, wiec po
             * podmianie glowy trzeba wygenerowac na nowo takze ten plik.
             */
            val hair = Res.drawable.hair_new

            // Prawa reka (anatomicznie) - na ekranie po lewej, reka za cialem (idle).
            // Konczyna jednoczesciowa (bez podzialu na bicep/przedramie) - rotuje sztywno wokol barku.
            val rightHand = Res.drawable.right_hand_new

            // Lewa reka (anatomicznie) - na ekranie po prawej, reka z mieczem z przodu.
            val leftHand = Res.drawable.left_hand_new

            // Prawa noga (anatomicznie) - na ekranie po lewej, noga z przodu.
            // Konczyna jednoczesciowa (bez podzialu na udo/lydke) - rotuje sztywno wokol biodra.
            val rightLeg = Res.drawable.right_leg_new

            // Lewa noga (anatomicznie) - na ekranie po prawej, noga z tylu
            val leftLeg = Res.drawable.left_leg_new
    }
    // Ekwipunek - nakladki rysowane na tych samych warstwach co czesci ciala.
    // Brak osobnego helmu i plyt na nogi w nowym zestawie grafik - te sloty na razie bez wizualizacji.
    object PlateArmor {
            val chest = Res.drawable.armor_new

            val rightHand = Res.drawable.glove_right_new
            val leftHand = Res.drawable.glove_left_new

            val rightFoot = Res.drawable.boot_right_new
            val leftFoot = Res.drawable.boot_left_new

            // Miecz trzymany w lewej dloni: ostrze przed dlonia, rekojesc wystaje za nia
            val swordFront = Res.drawable.sword_up_new
            val swordBehind = Res.drawable.sword_down_new
    }
    object KnightPivots {
        // Centrum postaci
        val head = TransformOrigin(0.512f, 0.568f)
        val body = TransformOrigin(0.512f, 0.657f)
        // Punkt przy stopach - caly rycerz kolysze sie wokol niego, wiec stopy prawie sie nie ruszaja
        val ground = TransformOrigin(0.555f, 0.9f)

        // Prawa reka (anatomicznie) - Tyl, idle - na ekranie po lewej. Pivot: bark.
        val rightHand = TransformOrigin(0.461f, 0.573f)
        // Lewa reka (anatomicznie) - Przod, miecz - na ekranie po prawej. Pivot: bark.
        val leftHand = TransformOrigin(0.546f, 0.575f)

        // Prawa noga (anatomicznie) - Przod - na ekranie po lewej. Pivot: biodro.
        val rightLeg = TransformOrigin(0.468f, 0.721f)
        // Lewa noga (anatomicznie) - Tyl - na ekranie po prawej. Pivot: biodro.
        val leftLeg = TransformOrigin(0.554f, 0.726f)
    }
    /** Anchor punkty efektow walki na rycerzu - ten sam typ co [com.lernhero.shared.resources.MonsterAnchors], zeby sekwencer/warstwa efektow nie musialy rozrozniac hero od monstera. */
    val KnightCombatAnchors = CombatAnchorSet(
        root = KnightPivots.ground,
        effectAnchor = TransformOrigin(0.5f, 0.5f),
        hitPoint = TransformOrigin(0.5f, 0.46f),
        projectileOrigin = TransformOrigin(0.58f, 0.42f),
        statusEffectAnchor = TransformOrigin(0.5f, 0.12f)
    )
    /** Pojedyncze PNG nowych potworow (bez sprite-sheetow) - klucz jak w [com.lernhero.domain.monster.MonsterCard.spriteKey]. */
    object MonsterArt {
        val skeletonWarrior = Res.drawable.skeleton_warrior_2
        val skeletonArcher = Res.drawable.skeleton_archer_2
        val skeletonMage = Res.drawable.skeleton_mage_2
        val orcWarrior = Res.drawable.orc_warrior_2
        val orcArcher = Res.drawable.orc_archer_2
        val humanArcher = Res.drawable.human_archer_2
        val bossSkeletonDragon = Res.drawable.boss_skeleton_dragon_2

        // Boss rozbity na 3 warstwy do animacji szczeki (patrz MonsterRigs.forSpecies) - kazdy PNG
        // ma ten sam canvas 768x768 co plaski bossSkeletonDragon, wiec warstwy same trafiaja na miejsce.
        val bossSkeletonDragonBody = Res.drawable.boss_skeleton_dragon_body
        val bossSkeletonDragonSkullUp = Res.drawable.boss_skeleton_dragon_up_skull
        val bossSkeletonDragonSkullDown = Res.drawable.boss_skeleton_dragon_down_skull
    }
    /** Grafiki warstwy efektow walki (pociski, hit/spell effects) - patrz com.lernhero.game.battle.effects. */
    object EffectArt {
        /** Plonacy czaszkowy pocisk, 16 klatek 256x256 w siatce 4x4 - zostaje nieuzywany, za szybki lot by bylo widac animacje. */
        val mageMissileSheet = Res.drawable.mage_missile_001_016
        /** Ten sam pocisk jako jeden statyczny PNG - uzywany narazie do kazdego trafienia (bez animacji klatek). */
        val mageMissile = Res.drawable.magic_missle_skeleton_mage
        /** Wybuch trafienia - rosnie od iskry do pelnego wybuchu, 8 klatek, siatka 4x2 (256x144/klatka). */
        val magicBomb = Res.drawable.magicboom_8_frames

        /**
         * Pocisk magiczny zlozony z TRZECH pierscieni - kazdy to osobny PNG na tym samym canvasie
         * 256x256, wiec naklada sie bez pozycjonowania. Kazdy kreci sie z wlasna predkoscia
         * (patrz com.lernhero.game.battle.effects.LayeredEffectCatalog).
         */
        val magicMissileRing1 = Res.drawable.magic_missle_1
        val magicMissileRing2 = Res.drawable.magic_missle_2
        val magicMissileRing3 = Res.drawable.magic_missle_3
    }
    object Flag {
        val India = Res.drawable.india
        val Usa = Res.drawable.usa
        val Serbia = Res.drawable.serbia
    }
}

/**
 * Surowe bajty pliku z `composeResources/files/...` (np. bundlowanej ksiazki PDF).
 * Wygenerowany `Res` jest domyslnie wewnetrzny dla tego modulu - to jedyne publiczne wyjscie.
 */
suspend fun Resources.readFileBytes(path: String): ByteArray = Res.readBytes(path)
