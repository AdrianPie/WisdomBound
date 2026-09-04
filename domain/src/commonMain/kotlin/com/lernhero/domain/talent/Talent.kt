package com.lernhero.domain.talent

import com.lernhero.domain.run.EndlessBuff

/**
 * Trzy sciezki rozwoju bohatera. Nie sa klasami postaci - gracz sypie punkty gdzie chce
 * (patrz [TalentRules]), a specjalizacja bierze sie z tego, ze glebokie talenty wymagaja punktow
 * wydanych WLASNIE W TYM drzewku. Hybryda jest mozliwa, tylko kosztuje szczyty.
 */
enum class TalentTree(
    val label: String,
    val icon: String,
    val tagline: String
) {
    Dps(
        label = "Destruction",
        icon = "⚔️",
        tagline = "Hit harder, more often, and against more targets at once."
    ),
    Tank(
        label = "Bulwark",
        icon = "🛡️",
        tagline = "Survive hits that used to end previous runs."
    ),
    Healer(
        label = "Light",
        icon = "✨",
        tagline = "Recover HP during the fight, instead of relying on potions."
    )
}

/**
 * Co daje JEDEN stopien talentu. Swiadomie waskie: kazdy wariant musi miec dzialajacy odpowiednik
 * w zywej walce, inaczej talent bylby ozdobna liczba na ekranie.
 *
 * [Buff] to najwazniejszy z nich - siega po [EndlessBuff], czyli po mechaniki, ktore silnik walki
 * juz w pelni obsluguje (garda, kolce, wampiryzm, rozped...). Dzieki temu drzewko nie potrzebowalo
 * ANI JEDNEJ nowej linijki w samej walce: talenty tylko zasiedlaja mape bonusow, ktora walka
 * i tak czytala dla trybu endless.
 */
sealed interface TalentEffect {

    /** Stale staki mechaniki bojowej - patrz [EndlessBuff] po pelna liste i opisy dzialania. */
    data class Buff(val buff: EndlessBuff, val stacksPerRank: Int = 1) : TalentEffect

    /** Plaski atak calej druzyny - ten sam kanal, co [com.lernhero.domain.player.HeroStat.Strength]. */
    data class Attack(val perRank: Int) : TalentEffect

    /** Plaskie maks. HP calej druzyny - kanal [com.lernhero.domain.player.HeroStat.Vitality]. */
    data class MaxHp(val perRank: Int) : TalentEffect

    /** Szansa na krytyka, 0.02 = +2 punkty procentowe za stopien. */
    data class Crit(val perRank: Float) : TalentEffect

    /** Mnoznik obrazen z dobrej odpowiedzi, 0.03 = +3% za stopien. */
    data class QuizDamage(val perRank: Float) : TalentEffect

    /**
     * Odblokowuje zaklecie w walce. Zastapilo i kupowanie zaklec za esencje, i reczny pasek
     * procentow: czar jest nagroda za rozwoj postaci, a [castChancePercent] mowi, jaka czesc
     * ciosow bohatera zamienia w to zaklecie. Gracz niczego nie rozdaje - bierze talent i ma efekt.
     */
    data class UnlockSpell(val spellId: String, val castChancePercent: Int) : TalentEffect
}

/**
 * Pojedynczy wezel drzewka.
 *
 * [tier] to rzad w drzewku (0 = gora). Kolejne rzedy otwieraja sie po wydaniu [TalentRules.POINTS_PER_TIER]
 * punktow w tym samym drzewku - klasyczna drabinka z WoW/Diablo. [column] to tylko pozycja
 * w siatce; domena nie rysuje, ale uklad musi byc stabilny miedzy ekranem a testami.
 *
 * [requires] to dodatkowa strzalka: talent wymaga WYMAKSOWANEGO rodzica. Maja ja tylko nieliczne
 * wezly - sam prog rzedu wystarcza, zeby drzewko mialo ksztalt.
 */
data class Talent(
    val id: String,
    val tree: TalentTree,
    val tier: Int,
    val column: Int,
    val name: String,
    val description: String,
    val icon: String,
    val maxRank: Int,
    val effects: List<TalentEffect>,
    val requires: String? = null
) {
    init {
        require(maxRank >= 1) { "Talent $id musi miec co najmniej jeden stopien" }
        require(tier >= 0) { "Talent $id ma ujemny rzad" }
    }
}

/**
 * Zsumowane efekty wszystkich wykupionych stopni. Walka i karta postaci czytaja WYLACZNIE to -
 * nie chodza po katalogu i nie licza niczego same.
 */
data class TalentBonuses(
    val buffStacks: Map<EndlessBuff, Int> = emptyMap(),
    val attack: Int = 0,
    val maxHp: Int = 0,
    val critChance: Float = 0f,
    val quizDamage: Float = 0f,
    /** Zaklecia odblokowane talentami: id -> szansa (procent ciosow) na rzut w walce. */
    val spellChances: Map<String, Int> = emptyMap()
) {
    /** Same id odblokowanych zaklec - bez szans. */
    val unlockedSpellIds: Set<String> get() = spellChances.keys

    val isEmpty: Boolean
        get() = buffStacks.isEmpty() && attack == 0 && maxHp == 0 &&
            critChance == 0f && quizDamage == 0f && spellChances.isEmpty()

    /** Mnoznik obrazen z quizu (1.0 = bez bonusu) - ten sam ksztalt, co w [com.lernhero.domain.player.HeroStats]. */
    val quizDamageMultiplier: Float get() = 1f + quizDamage

    companion object {
        val EMPTY = TalentBonuses()
    }
}
