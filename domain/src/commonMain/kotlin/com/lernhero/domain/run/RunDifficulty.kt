package com.lernhero.domain.run

import com.lernhero.domain.quiz.QuizDifficulty

/**
 * Poziom trudnosci calego runu. W kampanii to sa zakladki nad mapa swiata (jak w Idle Heroes):
 * przechodzisz wszystkie krainy na [Normal], co odblokowuje [Nightmare] na TYCH SAMYCH krainach,
 * mocniejszych i lepiej platnych - i tak dalej az do [Death]. Endless zawsze stoi na [Normal],
 * bo tam za trudnosc odpowiada wylacznie rosnaca fala, nie ten mnoznik.
 *
 * Sterowanie dwoma rzeczami naraz: jak mocni sa wrogowie i z jakiego szczebla drabinki losuja sie
 * pytania. Trudniejszy poziom placi wiecej monet - inaczej nikt by go nie wybieral po odblokowaniu.
 */
enum class RunDifficulty(
    val label: String,
    val description: String,
    val icon: String,
    val enemyHpMultiplier: Float,
    val enemyAttackMultiplier: Float,
    val coinMultiplier: Float
) {
    Normal(
        label = "Normal",
        description = "Balanced fights and questions from easy to hard.",
        icon = "⚔",
        enemyHpMultiplier = 1f,
        enemyAttackMultiplier = 1f,
        coinMultiplier = 1f
    ),
    Nightmare(
        label = "Nightmare",
        description = "Enemies and questions noticeably tougher. Unlocked after clearing Normal.",
        icon = "💀",
        enemyHpMultiplier = 1.5f,
        enemyAttackMultiplier = 1.35f,
        coinMultiplier = 1.6f
    ),
    Hell(
        label = "Hell",
        description = "The same campaign, much harsher. Unlocked after clearing Nightmare.",
        icon = "👹",
        enemyHpMultiplier = 2.2f,
        enemyAttackMultiplier = 1.8f,
        coinMultiplier = 2.4f
    ),
    Death(
        label = "Death",
        description = "The peak of the campaign. Unlocked after clearing Hell.",
        icon = "🔥",
        enemyHpMultiplier = 3.2f,
        enemyAttackMultiplier = 2.4f,
        coinMultiplier = 3.6f
    );

    /**
     * Drabinka pytan jednej planszy kampanii - CZTERY szczeble, po ktorych walka chodzi w kolko
     * (patrz BattleViewModel.drawNextQuestion).
     *
     * Do tej pory o trudnosci pytan w kampanii decydowal wylacznie numer FALI
     * ([com.lernhero.domain.quiz.QuizBank.ladderFor]), a plansza ma ich od jednej do trzech -
     * wiec kampania nigdy nie wychodzila poza poczatek drabinki. Opis tieru obiecywal "pytania
     * wyraznie trudniejsze", a gracz na Smierci, na ostatniej planszy Otchlani, dostawal dokladnie
     * te same pytania co na pierwszej planszy Lasu na Normalnym. Tier skalowal wrogow i monety,
     * a jedyna rzecz, ktora w tej grze naprawde rozstrzyga walke - pytanie - zostawial nietknieta.
     *
     * Teraz szczebel startowy sklada sie z trzech rzeczy: TIERU (zakladka nad mapa), GLEBOKOSCI
     * kampanii ([globalStage]) i RODZAJU walki (boss podnosi poprzeczke). Na Normalnym plansza 1
     * wychodzi dokladnie tak, jak wygladala drabinka fali 1 - czyli poczatek gry nie zrobil sie
     * trudniejszy, zmienil sie tylko dalszy ciag.
     */
    fun campaignLadder(kind: BattleKind, globalStage: Int): List<QuizDifficulty> {
        val start = tierStep + depthStep(globalStage) + kind.ladderStep
        return listOf(start, start, start + 1, start + 1).map { rung ->
            QuizDifficulty.entries[rung.coerceIn(0, QuizDifficulty.entries.lastIndex)]
        }
    }

    /**
     * Ile szczebli doklada sam tier. [Hell] i [Death] stoja na tym samym stopniu celowo: pytanie
     * ma tylko cztery poziomy, a te dwa tiery rozni juz przede wszystkim to, ile wytrzymuja
     * wrogowie ([enemyHpMultiplier]) - nie da sie zadac "jeszcze bardziej eksperckiego" pytania.
     */
    private val tierStep: Int
        get() = when (this) {
            Normal -> 0
            Nightmare -> 1
            Hell, Death -> 2
        }

    /** Nastepny szczebel trudnosci kampanii - null na [Death], juz nie ma wyzej. */
    val next: RunDifficulty? get() = entries.getOrNull(ordinal + 1)

    private companion object {
        /**
         * Co tyle plansz kampania doklada jeden szczebel trudnosci pytan. Przy
         * [com.lernhero.domain.story.StoryWorldCatalog.totalStages] = 36 daje to trzy stopnie
         * na tier: dwie pierwsze krainy, dwie srodkowe, dwie ostatnie.
         */
        const val STAGES_PER_RUNG = 12

        fun depthStep(globalStage: Int): Int = (globalStage - 1).coerceAtLeast(0) / STAGES_PER_RUNG
    }
}

/** Ile szczebli drabinki pytan doklada sam rodzaj walki - patrz [RunDifficulty.campaignLadder]. */
private val BattleKind.ladderStep: Int
    get() = when (this) {
        BattleKind.Normal -> 0
        BattleKind.Elite -> 0
        BattleKind.Boss -> 1
    }
