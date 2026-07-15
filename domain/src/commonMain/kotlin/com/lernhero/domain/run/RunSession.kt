package com.lernhero.domain.run

/**
 * Bonusy zebrane w trakcie jednego zejscia do lochu - z obozowisk i perkow. Licza sie **tylko**
 * do konca runu; nowy run startuje z czystym kontem.
 *
 * [spellWeightOverride] pozwala przemeblowac pasek zdolnosci przy ognisku, nie ruszajac zapisu
 * z ekwipunku. Null = uzyj tego, co gracz ustawil w EQ.
 */
data class RunModifiers(
    val bonusSpellCapacity: Int = 0,
    val bonusAttack: Int = 0,
    val bonusMaxHp: Int = 0,
    val bonusSpellPower: Float = 0f,
    val spellWeightOverride: Map<String, Int>? = null
)

/**
 * Trzyma stan biezacego runu. Jeden obiekt na aplikacje (wstrzykiwany przez Koin): mapa lochu go
 * zeruje na starcie, ogniska go modyfikuja, a walka go czyta. Dzieki temu perki z ogniska docieraja
 * do bitwy bez przepychania ich przez nawigacje.
 */
class RunSession {

    var modifiers: RunModifiers = RunModifiers()
        private set

    /** Nowe zejscie do lochu - kasuje wszystkie perki z poprzedniego. */
    fun startNewRun() {
        modifiers = RunModifiers()
    }

    fun apply(change: (RunModifiers) -> RunModifiers) {
        modifiers = change(modifiers)
    }

    fun overrideSpellWeights(weights: Map<String, Int>) {
        modifiers = modifiers.copy(spellWeightOverride = weights)
    }
}
