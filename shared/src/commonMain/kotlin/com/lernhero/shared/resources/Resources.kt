package com.lernhero.shared.resources

import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import lernhero.shared.generated.resources.Res
import lernhero.shared.generated.resources.back_arrow
import lernhero.shared.generated.resources.background
import lernhero.shared.generated.resources.bloody_mines
import lernhero.shared.generated.resources.forgotten_forest
import lernhero.shared.generated.resources.frozen_wastes
import lernhero.shared.generated.resources.brick_dungeon
import lernhero.shared.generated.resources.battlegroundTwo
import lernhero.shared.generated.resources.book
import lernhero.shared.generated.resources.border
import lernhero.shared.generated.resources.summoner_collection_screen
import lernhero.shared.generated.resources.flying_stone
import lernhero.shared.generated.resources.flying_stone1
import lernhero.shared.generated.resources.flying_stone2
import lernhero.shared.generated.resources.flying_stone3
import lernhero.shared.generated.resources.flying_stone4
import lernhero.shared.generated.resources.flying_stone5
import lernhero.shared.generated.resources.cat
import lernhero.shared.generated.resources.check
import lernhero.shared.generated.resources.checkmark_image
import lernhero.shared.generated.resources.close
import lernhero.shared.generated.resources.delete
import lernhero.shared.generated.resources.dollar
import lernhero.shared.generated.resources.edit
import lernhero.shared.generated.resources.google_logo
import lernhero.shared.generated.resources.grid
import lernhero.shared.generated.resources.home
import lernhero.shared.generated.resources.india
import lernhero.shared.generated.resources.knight_avatar2
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
import lernhero.shared.generated.resources.right_arrow
import lernhero.shared.generated.resources.search
import lernhero.shared.generated.resources.serbia
import lernhero.shared.generated.resources.shopping_cart
import lernhero.shared.generated.resources.shopping_cart_image
import lernhero.shared.generated.resources.smite_island
import lernhero.shared.generated.resources.sorcerer_avatar2
import lernhero.shared.generated.resources.summon_center
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
import lernhero.shared.generated.resources.answer_frame
import lernhero.shared.generated.resources.answer_fill
import lernhero.shared.generated.resources.answer_gems
import lernhero.shared.generated.resources.answer_badge_frame
import lernhero.shared.generated.resources.answer_badge_gem
import lernhero.shared.generated.resources.ramka

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


        val knightAvatar = Res.drawable.knight_avatar2
        val sorcererAvatar = Res.drawable.sorcerer_avatar2

        /**
         * Ozdobna ramka 9-slice (rogi w oryginalnym rozmiarze, boki rozciagane, srodek
         * przezroczysty). Uzywana przez [com.lernhero.home.ui.FramedPanel]. Podmiana pliku
         * `drawable/border.png` na inny 512x512 z transparentnym srodkiem wystarcza.
         */
        val forgeFrame = Res.drawable.border

        val corridorBrick = Res.drawable.brick_dungeon
        val treasureChest = Res.drawable.treasure

        /** Tlo ekranu kolekcji kompanow (zakladka Collection w Summon Center). */
        val summonerCollectionBackground = Res.drawable.summoner_collection_screen

        /**
         * Latajaca wyspa pod sylwetka kompana: jeden duzy kamien + piec malych kamykow, ktore
         * lewituja kazdy we wlasnym tempie ([com.lernhero.home.ui.FloatingIsland]).
         */
        val floatingIslandSlab = Res.drawable.flying_stone
        val floatingIslandPebbles = listOf(
            Res.drawable.flying_stone1,
            Res.drawable.flying_stone2,
            Res.drawable.flying_stone3,
            Res.drawable.flying_stone4,
            Res.drawable.flying_stone5,
        )

        /**
         * Tla krain story mode - jedno zrodlo prawdy dla WSZYSTKICH miejsc, ktore rysuja krainy:
         * podglad w panelu kampanii, ekran Mapy ([com.lernhero.home.story.StoryWorldArt]) i tlo
         * samej walki ([com.lernhero.game.battle.component.BattleField]). Null = kraina jeszcze
         * bez grafiki (wtedy walka spada na [battlegroundFirst], a karty krain na gradient).
         */
        val worldForest = Res.drawable.forgotten_forest
        val worldMine = Res.drawable.bloody_mines
        val worldIcefield = Res.drawable.frozen_wastes

        fun worldBackground(worldId: String): DrawableResource? = when (worldId) {
            "forest" -> worldForest
            "mine" -> worldMine
            "icefield" -> worldIcefield
            else -> null
        }
    }
    /** Wyspy-budynki na ekranie Miasta (Dom) - kazda to osobna klikalna "instancja" (patrz HomeScreen). */
    object CityIsland {
        /** Pusty oltarz w Summon Center - scena, na ktorej "wychodzi" wylosowany summon. */
        val altar = Res.drawable.summon_center
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
    /**
     * Ozdobny dymek odpowiedzi w quizie, rozbity na warstwy - bo kolor kamieni i tla ma zalezec od
     * numeru odpowiedzi i od tego, czy byla trafiona. Zloto zostaje w oryginalnych barwach, a
     * [answerBubbleFill] i [answerBubbleGems] sa w ODCIENIACH SZAROSCI: rysowane przez
     * `ColorFilter.tint(kolor, BlendMode.Modulate)` przyjmuja dowolna barwe, zachowujac cieniowanie
     * i fasetki z oryginalnej grafiki. Wszystkie warstwy leza na tym samym canvasie 1233x325, wiec
     * naklada sie je jedna na druga bez pozycjonowania - kolejnosc: fill, frame, gems.
     *
     * Wygenerowane z `answer.png` / `answer_number.png` - po podmianie zrodel trzeba wygenerowac
     * warstwy na nowo. Uzywa ich [com.lernhero.game.battle.component.LiveQuizBar].
     */
    object QuizArt {
        val answerBubbleFrame = Res.drawable.answer_frame
        val answerBubbleFill = Res.drawable.answer_fill
        val answerBubbleGems = Res.drawable.answer_gems

        /** Krazek z numerem odpowiedzi - canvas 264x255, ta sama zasada dwoch warstw co wyzej. */
        val answerBadgeFrame = Res.drawable.answer_badge_frame
        val answerBadgeGem = Res.drawable.answer_badge_gem

        /**
         * Gotowa, ozdobna ramka pod PYTANIE (1996x665) - cala oprawa razem z tlem, wiec rysuje sie
         * ja jednym Image, bez skladania z warstw. Proporcje trzyma [QUESTION_FRAME_RATIO].
         */
        val questionFrame = Res.drawable.ramka

        /** Szerokosc do wysokosci [questionFrame] - do `Modifier.aspectRatio`. */
        const val QUESTION_FRAME_RATIO = 1996f / 665f

        /**
         * Jaka czesc szerokosci ramki moze zajac tekst pytania. Wnetrze siega 87% szerokosci, ale
         * przy samych krawedziach jest juz plytkie (wcinaja sie narozniki), wiec tekst dostaje
         * wezszy pas - zmierzony tak, by trzymal sie czesci o pelnej wysokosci wnetrza.
         */
        const val QUESTION_TEXT_WIDTH_FRACTION = 0.78f
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
