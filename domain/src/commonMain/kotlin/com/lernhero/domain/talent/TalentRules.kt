package com.lernhero.domain.talent

import com.lernhero.domain.run.EndlessBuff

/** Dlaczego danego stopnia NIE da sie teraz wykupic - UI pokazuje to wprost, zamiast gasic wezel bez slowa. */
enum class TalentBlock(val reason: String) {
    NoPoints("No talent points left"),
    MaxRank("Talent is already maxed out"),
    TierLocked("Not enough points in this tree"),
    ParentMissing("Requires")
}

/** Stan jednego wezla policzony dla konkretnego gracza - wszystko, czego ekran potrzebuje do rysowania. */
data class TalentNodeState(
    val talent: Talent,
    val rank: Int,
    val block: TalentBlock?
) {
    val isMaxed: Boolean get() = rank >= talent.maxRank
    val isLearned: Boolean get() = rank > 0
    val canSpend: Boolean get() = block == null

    /** Wezel jest "osiagalny" - rzad otwarty i rodzic spelniony, nawet jesli brakuje akurat punktu. */
    val isReachable: Boolean get() = block == null || block == TalentBlock.NoPoints || block == TalentBlock.MaxRank
}

/**
 * Cala logika drzewka: co wolno wykupic, ile punktow siedzi w ktorym drzewku i jak zamienic
 * wykupione stopnie w bonusy do walki.
 *
 * Rozdzielone od [TalentCatalog] (dane) i od repozytorium (zapis), bo to jedyna czesc, ktora ma
 * prawo powiedziec "nie" - i musi to robic tak samo w UI (wygaszony wezel) jak i przy zapisie
 * (odrzucone zadanie). Jedno zrodlo regul zamiast dwoch, ktore i tak by sie rozjechaly.
 */
object TalentRules {

    /** Ile punktow w drzewku otwiera kolejny rzad. Rzad 0 jest od razu, rzad 1 po 3, rzad 2 po 6... */
    const val POINTS_PER_TIER = 3

    /** Ile punktow talentow daje jeden awans - patrz [com.lernhero.domain.player.LevelCurve]. */
    const val POINTS_PER_LEVEL = 1

    /**
     * Ile punktow talentow nalezy sie za osiagniety poziom. Poziom 1 to zero - punkt wpada za
     * KAZDY awans, wiec bohater na poziomie 12 ma ich 11.
     */
    fun pointsEarnedBy(level: Int): Int = (level - 1).coerceAtLeast(0) * POINTS_PER_LEVEL

    /**
     * Ile punktow gracz ma jeszcze do wydania.
     *
     * Liczone, a NIE przechowywane: nalezne punkty wynikaja z poziomu, wydane siedza w samych
     * stopniach. Osobny licznik "nierozdanych" musialby byc pilnowany przy kazdym awansie i przy
     * kazdym zerowaniu drzewka - a gracze, ktorzy awansowali ZANIM drzewko powstalo, mieliby go
     * na zero mimo trzydziestu poziomow na karcie.
     */
    fun availablePoints(level: Int, ranks: Map<String, Int>): Int =
        (pointsEarnedBy(level) - totalSpent(ranks)).coerceAtLeast(0)

    /** Wykupione stopnie: id talentu -> stopien. Brak wpisu = 0. */
    fun rankOf(ranks: Map<String, Int>, talentId: String): Int {
        val raw = ranks[talentId] ?: 0
        // Stary zapis moze miec wiecej stopni, niz talent ma po zmianie balansu - liczy sie maksimum.
        return TalentCatalog.byId(talentId)?.let { raw.coerceIn(0, it.maxRank) } ?: raw
    }

    /**
     * Ile punktow gracz wydal lacznie (we wszystkich drzewkach).
     *
     * Stopien liczy sie najwyzej do [Talent.maxRank]: gdy balans obetnie talentowi stopnie
     * (np. Wampiryzm 4 -> 2), nadmiar ze starego zapisu WRACA do puli, zamiast przepasc.
     */
    fun totalSpent(ranks: Map<String, Int>): Int =
        ranks.entries.sumOf { (id, rank) -> TalentCatalog.byId(id)?.let { rank.coerceIn(0, it.maxRank) } ?: 0 }

    /** Ile punktow siedzi w JEDNYM drzewku - to od tego zaleza progi rzedow. */
    fun pointsIn(ranks: Map<String, Int>, tree: TalentTree): Int =
        ranks.entries.sumOf { (id, rank) ->
            TalentCatalog.byId(id)?.takeIf { it.tree == tree }?.let { rank.coerceIn(0, it.maxRank) } ?: 0
        }

    /**
     * Ile stakow mechaniki moga dac SAME talenty: polowa sufitu z endless, zaokraglona w gore.
     *
     * Talenty dzialaja w kazdej walce, na stale - karty z biegu tylko do konca biegu. Gdyby
     * talent mogl zapelnic mechanike do pelna, karty z endless przestalyby sie pokazywac (sa juz
     * wymaksowane), a stala sila bohatera bylaby taka jak na koncu najlepszego biegu.
     */
    fun talentCap(buff: EndlessBuff): Int = (buff.maxStacks + 1) / 2

    /** Ile punktow trzeba miec w drzewku, zeby otworzyc [tier]. */
    fun pointsRequiredFor(tier: Int): Int = tier * POINTS_PER_TIER

    /**
     * Czemu tego stopnia nie da sie teraz wykupic - albo null, gdy wolno.
     *
     * Kolejnosc sprawdzen jest celowa: najpierw powody trwale (wymaksowany, zamkniety rzad,
     * brak rodzica), a dopiero na koncu brak punktu. Dzieki temu wezel, ktory czeka tylko na
     * kolejny awans, wyglada inaczej niz taki, do ktorego gracz w ogole jeszcze nie doszedl.
     */
    fun blockFor(
        talent: Talent,
        ranks: Map<String, Int>,
        availablePoints: Int
    ): TalentBlock? {
        if (rankOf(ranks, talent.id) >= talent.maxRank) return TalentBlock.MaxRank
        if (pointsIn(ranks, talent.tree) < pointsRequiredFor(talent.tier)) return TalentBlock.TierLocked

        val parentId = talent.requires
        if (parentId != null) {
            val parent = TalentCatalog.byId(parentId)
            // Brakujacy rodzic w katalogu to blad danych, ale nie moze wywrocic ekranu - blokujemy.
            if (parent == null || rankOf(ranks, parentId) < parent.maxRank) return TalentBlock.ParentMissing
        }

        if (availablePoints <= 0) return TalentBlock.NoPoints
        return null
    }

    fun canSpend(talent: Talent, ranks: Map<String, Int>, availablePoints: Int): Boolean =
        blockFor(talent, ranks, availablePoints) == null

    /**
     * Dokłada jeden stopien. Zwraca null, gdy nie wolno - dzieki temu repozytorium nie musi
     * powtarzac regul, tylko sprawdza, czy cos wrocilo.
     */
    fun spend(
        talent: Talent,
        ranks: Map<String, Int>,
        availablePoints: Int
    ): Map<String, Int>? {
        if (!canSpend(talent, ranks, availablePoints)) return null
        return ranks + (talent.id to rankOf(ranks, talent.id) + 1)
    }

    /** Stan wszystkich wezlow drzewka - jedno przejscie zamiast liczenia w kazdym wezle osobno. */
    fun statesOf(
        tree: TalentTree,
        ranks: Map<String, Int>,
        availablePoints: Int
    ): List<TalentNodeState> = TalentCatalog.forTree(tree).map { talent ->
        TalentNodeState(
            talent = talent,
            rank = rankOf(ranks, talent.id),
            block = blockFor(talent, ranks, availablePoints)
        )
    }

    /**
     * Sumuje wszystkie wykupione stopnie w jeden worek bonusow.
     *
     * Staki buffow sa przycinane do [talentCap] - polowy sufitu z endless. Reszta zostaje dla kart
     * z biegu, a walka i tak przycina sume do [EndlessBuff.maxStacks].
     */
    fun bonusesOf(ranks: Map<String, Int>): TalentBonuses {
        if (ranks.isEmpty()) return TalentBonuses.EMPTY

        val buffs = mutableMapOf<EndlessBuff, Int>()
        var attack = 0
        var maxHp = 0
        var crit = 0f
        var quiz = 0f
        val spells = mutableMapOf<String, Int>()

        ranks.forEach { (id, rank) ->
            if (rank <= 0) return@forEach
            val talent = TalentCatalog.byId(id) ?: return@forEach
            val effectiveRank = rank.coerceAtMost(talent.maxRank)

            talent.effects.forEach { effect ->
                when (effect) {
                    is TalentEffect.Buff ->
                        buffs[effect.buff] = (buffs[effect.buff] ?: 0) + effect.stacksPerRank * effectiveRank
                    is TalentEffect.Attack -> attack += effect.perRank * effectiveRank
                    is TalentEffect.MaxHp -> maxHp += effect.perRank * effectiveRank
                    is TalentEffect.Crit -> crit += effect.perRank * effectiveRank
                    is TalentEffect.QuizDamage -> quiz += effect.perRank * effectiveRank
                    // Zaklecie nie ma stopni - wystarczy jeden wykupiony, zeby je miec. Gdyby dwa
                    // talenty odblokowaly ten sam czar, liczy sie wyzsza szansa.
                    is TalentEffect.UnlockSpell ->
                        spells[effect.spellId] =
                            maxOf(spells[effect.spellId] ?: 0, effect.castChancePercent)
                }
            }
        }

        return TalentBonuses(
            buffStacks = buffs.mapValues { (buff, stacks) -> stacks.coerceAtMost(talentCap(buff)) },
            attack = attack,
            maxHp = maxHp,
            critChance = crit,
            quizDamage = quiz,
            spellChances = spells
        )
    }

    /** Wszystkie zaklecia odblokowane talentami wraz z ich szansa na rzut w walce. */
    fun spellChances(ranks: Map<String, Int>): Map<String, Int> = bonusesOf(ranks).spellChances

    /** Same id odblokowanych zaklec. */
    fun unlockedSpellIds(ranks: Map<String, Int>): Set<String> = bonusesOf(ranks).unlockedSpellIds
}
