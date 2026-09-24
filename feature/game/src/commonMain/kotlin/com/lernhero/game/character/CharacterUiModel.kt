package com.lernhero.game.character

import com.lernhero.domain.monster.Element

data class CharacterUiModel(
    val id: String,
    val name: String = id,
    val hp: Int,
    val maxHp: Int,
    val attack: Int,
    val isPlayer: Boolean,
    /** Poziom do odznaki przy pasku HP (bohater, kompan, wrog). */
    val level: Int = 1,
    /** Pancerz: ulamek obrazen, ktory ta postac odcina - patrz [com.lernhero.domain.combat.Armor]. */
    val armor: Float = 0f,
    /** Klucz gatunku - patrz [MonsterVisualPresets.forSpecies]. Bez znaczenia dla hero (rysowany przez [ModularKnight]). */
    val avatarKey: String,
    /**
     * Zywiol walczacego - decyduje o przewagach ([Element.multiplier]). Bohater ma null i jest
     * neutralny w obie strony: gracz nie ma jak zmienic swojego typu, wiec kara za zly matchup
     * bylaby dla niego czysto losowa.
     */
    val element: Element? = null,
    val state: CharacterState = CharacterState.IDLE,
    val currentTargetId: String? = null,
    /**
     * When set, this fighter is rendered as the modular paper-doll hero (with equipment) instead
     * of a sprite sheet, so the gear equipped in the Equipment screen shows up in battle.
     */
    val knightEquipment: KnightEquipment? = null,
    /** Kolory skory/wlosow bohatera - ustawione tylko dla paper-dolla, patrz [HeroLook]. */
    val heroLook: HeroLook = HeroLook.Default,
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
    val approach: Float = 0f,
    /**
     * Nazwana animacja rigu, ktora ma teraz grac (null = stoi w spoczynku). Dziala tylko dla
     * gatunkow z rigiem wieloczesciowym - patrz [MonsterRigs]/[MonsterAnimations].
     */
    val animSlot: AnimSlot? = null,
    /**
     * Licznik odpalen [animSlot]. Rosnie za KAZDYM razem, gdy animacja ma zagrac od poczatku -
     * bez tego dwa te same zdarzenia z rzedu (np. dwa razy "leczenie") nie odtworzylyby jej
     * drugi raz, bo sam [animSlot] by sie nie zmienil.
     */
    val animTrigger: Int = 0,
    /**
     * Rozciaga animacje na zadany czas zamiast jej wlasnego [MonsterAnimationClip.durationMs].
     * Uzywane, gdy animacja ma sie ZGRAC z czyms innym na ekranie - zdarzenie bossa trwa dokladnie
     * tyle, co licznik na odpowiedz w quizie ([AnswerTimer.WINDOW_MS]), wiec pysk konczy sie
     * otwierac razem z koncem paska. Null = graj we wlasnym tempie klipu.
     *
     * Czas jest podany W REALNYCH ms i NIE podlega [BattleSpeed] - licznik quizu tez nie podlega,
     * a zgranie ma sie trzymac niezaleznie od tego, jak szybko gracz przewija walke.
     */
    val animDurationMs: Int? = null,
    /**
     * Przemalowanie efektow granej animacji na jeden odcien (ARGB 0xAARRGGBB), np. zielony przy
     * leczeniu bossa, zolty przy ogluszeniu. null = kolory z samego klipu.
     */
    val animEffectTintArgb: Long? = null,
    /**
     * Przyblizenie tej postaci do kamery (1f = normalnie). Uzywane, gdy boss laduje atak specjalny -
     * jego pysk najezdza na ekran, zeby bylo widac, ze cos szykuje. Skalowanie idzie wokol GLOWY,
     * a nie srodka sylwetki, wiec zbliza sie twarz, a nie caly szkielet naraz.
     */
    val cameraZoom: Float = 1f
)
