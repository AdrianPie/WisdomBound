package com.lernhero.game.battle

/**
 * Scenariusz wystrzalu ulti - JEDNO zrodlo czasow dla view modelu i dla ekranu.
 *
 * View model zadaje obrazenia/leczenie dokladnie w [IMPACT_MS] (liczby wyskakuja w chwili
 * uderzenia, a nie w chwili odpowiedzi) i wstrzymuje nastepna ture do [TOTAL_MS]; ekran w tych
 * samych chwilach gasi tlo, wypuszcza baner i rozbija kule. Gdyby kazda strona miala wlasne
 * liczby, uderzenie na ekranie rozjechaloby sie z liczbami przy pierwszej zmianie tempa.
 *
 * Wszystko w milisekundach przy tempie 1x - przed uzyciem przechodzi przez [BattleSpeed.scale].
 */
object UltimateTiming {
    /**
     * Ile po zatwierdzeniu odpowiedzi (start [TurnPhase.Impact]) scena rusza: najpierw quiz musi
     * zjechac, a pole bitwy rozwinac sie na caly ekran (SCENE_TRANSITION_MS), plus krotki oddech.
     * Ulti odpalane przy wysunietym quizie gralo na skrawku ekranu - nic z niego nie bylo widac.
     */
    val START_AFTER_EXPAND_MS: Long = com.lernhero.game.battle.component.SCENE_TRANSITION_MS + 150L

    /** Tlo gasnie, kamera najezdza na cel. */
    const val DIM_IN_MS = 350

    /** Kula wznosi sie znad bohatera nad pole bitwy. */
    const val ORB_RISE_MS = 650

    /** Baner z nazwa kuli: wjazd, stoi, zjazd. */
    const val CUT_IN_START_MS = 280
    const val CUT_IN_END_MS = 1150

    /** Kula peka i uderza - w tej chwili view model zadaje obrazenia i leczy. */
    const val IMPACT_MS = 1100

    /** Od kiedy tlo sie rozjasnia i kamera wraca. */
    const val FADE_OUT_START_MS = 1650

    /** Koniec calej sceny - dopiero wtedy rusza salwa kolejnej tury. */
    const val TOTAL_MS = 2300
}
