package com.lernhero.domain.run

import kotlin.math.pow

/**
 * Drabinka wypraw: tryb, w ktorym idzie sie fala po fali, a na dwudziestej stoi boss konczacy
 * POZIOM (tier). Jego pokonanie odblokowuje start od trudniejszego poziomu; dalej mozna isc, ale
 * krotko - za dwudziesta fala trudnosc przyspiesza tak, ze nikt nie dojdzie do setnej.
 *
 * ## Dlaczego tak, a nie "endless bez konca"
 *
 * Endless bez mety nagradza CZAS, a nie sile: kto ma wiecej cierpliwosci, ten zajdzie dalej.
 * Meta na dwudziestej fali zamienia to w cel: wyprawa ma koniec, koniec ma nagrode, a nagroda
 * (wyzszy tier) zmienia nastepna wyprawe. Dalsze fale zostaja jako rekord do bicia i zrodlo
 * lupu - dlatego wolno tam isc, ale tylko kilka fal, bo:
 *
 * - w obrebie tieru trudnosc rosnie spokojnie ([WAVE_STEP]) - to jest wlasciwa wyprawa,
 * - za meta fale mnoza sie przez [OVERTIME_STEP] zamiast [WAVE_STEP]. Roznica na fale jest mala,
 *   ale skladana - i, co wazniejsze, karty ulepszen przestaja za nia nadazac. Sciana wypada w
 *   okolicach fali 27 (patrz [estimatedWall]): jest po co tam isc, a nikt nie dojdzie do setnej.
 *
 * Wszystkie liczby sa TUTAJ, a nie w walce - przestrojenie balansu to zmiana jednej stalej, a nie
 * polowanie po BattleViewModel.
 */
object RunLadder {

    /** Fala, na ktorej stoi boss konczacy poziom. */
    const val WAVES_PER_TIER = 20

    /**
     * Ile odcinkow ma poziom - tyle kropek stoi pod przyciskiem startu. Ostatni odcinek konczy
     * boss, wiec ostatnia kropka to on.
     */
    const val STAGES_PER_TIER = 5

    /** Ile fal zapelnia jedna kropke. */
    const val WAVES_PER_STAGE = WAVES_PER_TIER / STAGES_PER_TIER

    /** Co tyle fal wchodzi boss. Dwudziesta wypada na wielokrotnosci, wiec meta TEZ jest bossem. */
    const val BOSS_INTERVAL = 10

    /**
     * Wzrost sily wrogow na fale W OBREBIE poziomu.
     *
     * Liczba jest mala (6% na fale), bo wrogowie NIE scigaja sie tu z gracza stanem konta, tylko
     * z druzyna, ktora rosnie W TRAKCIE BIEGU: co [BUFF_EVERY_WAVES] fal endless daje karte
     * ulepszenia i do dwudziestej fali jest ich juz szesc. Licze sie wiec nie z surowym mnoznikiem,
     * tylko z [relativeDifficulty] - i to ona ma rosnac lagodnie do mety.
     */
    const val WAVE_STEP = 1.06f

    /**
     * Wzrost sily wrogow za meta - to jest ta sciana.
     *
     * Uwaga przy strojeniu: karty ulepszen lecą DALEJ, wiec netto zostaje tylko roznica miedzy ta
     * liczba a przyrostem druzyny (~2.9% na fale). Przy 1.20 sciana wypadala juz na fali 23, czyli
     * trzy fale za meta - za malo, zeby bylo po co tam isc. 1.08 zostawia ok. osmiu fal zapasu
     * i konczy bieg w okolicach fali 28.
     */
    const val OVERTIME_STEP = 1.08f

    /** Ile mocniejszy jest KAZDY kolejny poziom na starcie. */
    const val TIER_STEP = 1.45f

    /** Co tyle oczyszczonych fal endless daje karte ulepszenia (patrz [EndlessBuff]). */
    const val BUFF_EVERY_WAVES = 3

    /**
     * Ile mocy daje jedna karta ulepszenia. Srednia z tego, co realnie wypada: +8% ataku, -7%
     * obrazen, +8% krytyka... Grubo, ale o to chodzi - to jest ZALOZENIE balansu, spisane w jednym
     * miejscu zamiast siedziec w niczyjej glowie.
     */
    const val BUFF_POWER_GAIN = 0.09f

    /** Ile procent nagrod dokłada jedna fala, i ile jeden poziom. */
    private const val REWARD_PER_WAVE = 0.08f
    private const val REWARD_PER_TIER = 0.5f

    /** Za meta lup rosnie szybciej niz do niej - to jedyny powod, zeby tam w ogole isc. */
    private const val REWARD_PER_OVERTIME_WAVE = 0.25f

    fun isBossWave(wave: Int): Boolean = wave > 0 && wave % BOSS_INTERVAL == 0

    /** Fala konczaca poziom - ta, po ktorej wyprawa jest ZALICZONA. */
    fun isTierBoss(wave: Int): Boolean = wave == WAVES_PER_TIER

    fun kindFor(wave: Int): BattleKind =
        if (isBossWave(wave)) BattleKind.Boss else BattleKind.Normal

    /** Ile fal zostalo do mety. Zero i mniej = gracz jest juz za nia. */
    fun wavesToFinish(wave: Int): Int = WAVES_PER_TIER - wave

    /** Czy doczyszczenie [wave] zalicza poziom [tier] (czyli odblokowuje kolejny). */
    fun clearsTier(tier: Int, wave: Int): Boolean = wave >= WAVES_PER_TIER && tier >= 0

    /**
     * Mnoznik statystyk wrogow dla danej fali na danym poziomie.
     *
     * Trzy czynniki, kazdy o swojej roli: poziom ([TIER_STEP]) ustawia PULAP calej wyprawy, fale
     * do mety ([WAVE_STEP]) rozkladaja ja na spokojna gorke, a fale za meta ([OVERTIME_STEP])
     * domykaja bieg.
     */
    fun enemyMultiplier(tier: Int, wave: Int): Float {
        val safeTier = tier.coerceAtLeast(0)
        val safeWave = wave.coerceAtLeast(1)
        val tierPart = TIER_STEP.toDouble().pow(safeTier.toDouble())
        val inTier = WAVE_STEP.toDouble().pow((minOf(safeWave, WAVES_PER_TIER) - 1).toDouble())
        val overtime = if (safeWave > WAVES_PER_TIER) {
            OVERTIME_STEP.toDouble().pow((safeWave - WAVES_PER_TIER).toDouble())
        } else {
            1.0
        }
        return (tierPart * inTier * overtime).toFloat()
    }

    /**
     * Ile razy mocniejsza jest druzyna na [wave] niz na pierwszej fali - z samych kart ulepszen
     * zebranych po drodze. Nie zaglada do stanu konta: sprzet i poziom bohatera wchodza do biegu
     * juz na starcie i skracaja sie w [relativeDifficulty].
     */
    fun partyPowerAt(wave: Int): Float {
        val picks = (wave.coerceAtLeast(1) - 1) / BUFF_EVERY_WAVES
        return (1f + BUFF_POWER_GAIN).toDouble().pow(picks.toDouble()).toFloat()
    }

    /**
     * PRAWDZIWA trudnosc fali: o ile wrogowie wyprzedzili druzyne wzgledem startu biegu.
     *
     * To jest liczba, o ktora chodzi w calym balansie - sam mnoznik wrogow nic nie mowi, dopoki
     * nie zestawi sie go z tym, co gracz zebral po drodze. 1.0 = tak samo trudno jak na pierwszej
     * fali, 2.0 = dwa razy trudniej.
     */
    fun relativeDifficulty(tier: Int, wave: Int): Float =
        (enemyMultiplier(tier, wave) / enemyMultiplier(tier, 1)) / partyPowerAt(wave)

    /** Ile kropek poziomu zapelnia [wave] fal oczyszczonych w tym biegu. */
    fun stagesCleared(waveInTier: Int): Int =
        (waveInTier.coerceAtLeast(0) / WAVES_PER_STAGE).coerceAtMost(STAGES_PER_TIER)

    /** Ile fal tego poziomu ma za soba gracz z rekordem [bestWave]. */
    fun waveInTier(bestWave: Int): Int =
        bestWave.coerceAtLeast(0) % WAVES_PER_TIER

    /**
     * Mnoznik lupu. Rosnie DUZO wolniej niz sila wrogow - inaczej jedna fala za meta bylaby warta
     * wiecej niz cala wyprawa i nikt nie zawracalby sobie glowy przechodzeniem poziomow.
     */
    fun rewardMultiplier(tier: Int, wave: Int): Float {
        val safeTier = tier.coerceAtLeast(0)
        val safeWave = wave.coerceAtLeast(1)
        val toFinish = minOf(safeWave, WAVES_PER_TIER)
        val overtime = (safeWave - WAVES_PER_TIER).coerceAtLeast(0)
        return (1f + REWARD_PER_TIER * safeTier) *
            (1f + REWARD_PER_WAVE * (toFinish - 1) + REWARD_PER_OVERTIME_WAVE * overtime)
    }

    /**
     * Poziom, na ktorym gracz moze zaczynac, wyliczony z jego NAJLEPSZEJ fali.
     *
     * Liczymy go z rekordu zamiast trzymac osobny licznik w zapisie: rekord i tak jest zapisany
     * ([com.lernhero.domain.progress.GameStats.bestEndlessWave]), a dwa zrodla tej samej prawdy
     * predzej czy pozniej sie rozjezdzaja. Przejscie mety raz = poziom drugi, dwa razy = trzeci.
     */
    fun tierFor(bestWave: Int): Int = (bestWave.coerceAtLeast(0) / WAVES_PER_TIER)

    /**
     * Orientacyjna fala, na ktorej konczy sie bieg: pierwsza, na ktorej wrogowie wyprzedzaja
     * druzyne o [wipeAt] razy. Sluzy WYLACZNIE testom balansu i podgladom - walka jej nie czyta.
     */
    fun estimatedWall(tier: Int, wipeAt: Float = 2.5f): Int {
        var wave = 1
        while (wave < 200 && relativeDifficulty(tier, wave) < wipeAt) wave++
        return wave
    }
}
