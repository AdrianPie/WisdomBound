package com.lernhero.domain.run

/**
 * Rytm trybu endless: co [BOSS_INTERVAL] fal zamiast tlumu wchodzi JEDEN boss - i to on placi
 * skrzynie ([RunLoot.bossBonus]), razem z losami do Summon Center.
 *
 * ## Po co to powstalo
 *
 * Endless nie mial czego dac. Kazda jego fala startowala jako [BattleKind.Normal], a `bossBonus`
 * dla `Normal` zwraca same zera - wiec tryb, ktory z zalozenia jest miejscem do farmienia, nie
 * wyplacal ani jednego losa, ani kamienia, ani premii XP za bossa. Tymczasem sciana, w ktora
 * gracz wchodzi po ukonczeniu kampanii na Normalnym, stoi wlasnie na GWIAZDKACH kompanow - czyli
 * na duplikatach, czyli na losach. Farmienie endless dokladnie tej sciany nie ruszalo.
 *
 * Skoro losy leca z bossow, endless musi miec bossow. Glebokosc ([wave]) gra tu te sama role, co
 * numer planszy w kampanii: im dalej zajdziesz, tym grubsza skrzynia - wiec o wysokosc wyplaty
 * decyduje quiz, a nie czas spedzony w menu.
 */
object EndlessMilestones {

    /** Co tyle oczyszczonych fal wchodzi boss. */
    const val BOSS_INTERVAL = 10

    /**
     * Czym jest fala o tym numerze. Poza endless nie ma zastosowania - tam rodzaj walki przychodzi
     * z mapy kampanii ([BattleRequest.kind]).
     */
    fun kindFor(wave: Int): BattleKind =
        if (wave > 0 && wave % BOSS_INTERVAL == 0) BattleKind.Boss else BattleKind.Normal

    fun isBossWave(wave: Int): Boolean = kindFor(wave) == BattleKind.Boss

    /** Ile fal zostalo do najblizszego bossa - UI moze z tego zrobic zapowiedz. */
    fun wavesUntilBoss(wave: Int): Int = BOSS_INTERVAL - (wave % BOSS_INTERVAL)
}
