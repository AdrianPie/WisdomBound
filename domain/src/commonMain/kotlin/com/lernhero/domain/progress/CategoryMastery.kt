package com.lernhero.domain.progress

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import kotlin.math.min

/**
 * Poziom OPANOWANIA pojedynczego tematu - osobna oś rozwoju obok poziomu bohatera.
 *
 * Endless przestaje byc "falami bez konca dla samych fal": gracz wybiera temat, przechodzi
 * [WAVES_PER_LEVEL] fal i ten temat awansuje. Awans nie jest odznaka - zmienia nastepne wejscie:
 * pytania startuja od wyzszego szczebla drabinki ([startingDifficulty]), a lup i XP ida w gore
 * ([rewardMultiplier]). Dzieki temu powtorne wejscie w ten sam temat jest inna gra niz pierwsze,
 * a nie tym samym z innym numerem przy nazwie.
 *
 * **Poziom nie ma sufitu**, ale trudnosc go ma: od [MAX_DIFFICULTY_LEVEL] pytania stoja juz na
 * [QuizDifficulty.Expert] i wyzej sie nie da. Dalsze poziomy podnosza wylacznie statystyki wrogow
 * i nagrody - inaczej po kilkunastu przejsciach temat stalby sie niegrywalny zamiast bardziej
 * oplacalny.
 *
 * Premia idzie za TRUDNOSCIA, nie za sama odznaka: wejscie z recznie przybitym, latwiejszym
 * poziomem pytan placi tyle, ile ten poziom jest wart, i nie pcha tematu wyzej - patrz
 * [effectiveLevel] i [countsForMastery].
 */
object CategoryMastery {

    /**
     * Ile fal trzeba wyczyscic, zeby temat awansowal o poziom.
     *
     * Bylo 20 i tyle nie dawalo sie zrobic. Symulacja endless (druzyna z realnymi kompanami,
     * karty ulepszen co trzy fale, bezbledny quiz) konczy sie na fali 18 dopiero przy bohaterze
     * z poziomem 30 - a poziom 30 to juz koniec calej kampanii z zapasem. Gracz, ktory wybieral
     * temat i czytal "wyczysc 20 fal", dostawal cel, ktorego jego konto nie moglo osiagnac przez
     * pierwsze kilkadziesiat godzin gry; tryb opanowania tematu nie awansowal ANI RAZU.
     *
     * 10 fal to pulap bohatera okolo poziomu 12-15, czyli srodka kampanii - pierwszy poziom tematu
     * wpada wtedy, kiedy gracz ma jeszcze po co wracac, a nie po tym, jak przestal potrzebowac
     * czegokolwiek. Wyzsze poziomy tematu i tak podnosza sobie poprzeczke same, bo wzmacniaja
     * wrogow ([enemyMultiplier]).
     */
    const val WAVES_PER_LEVEL = 10

    /** Poziom, na ktorym pytania osiagaja [QuizDifficulty.Expert] - wyzej trudnosc juz nie rosnie. */
    const val MAX_DIFFICULTY_LEVEL = 7

    /** Kazdy poziom powyzej pierwszego dodaje tyle do HP i ataku wrogow. */
    private const val ENEMY_PER_LEVEL = 0.08f

    /** Kazdy poziom powyzej pierwszego dodaje tyle do mnoznika lupu i XP - do progu trudnosci. */
    private const val REWARD_PER_LEVEL = 0.15f

    /**
     * Przyrost nagrody za kazdy poziom POWYZEJ [MAX_DIFFICULTY_LEVEL]. Rowny [ENEMY_PER_LEVEL]
     * i to jest caly sens tej stalej: dopoki pytania robia sie trudniejsze, nagroda ma rosnac
     * szybciej od wrogow (to jest zaplata za nauke). Gdy trudnosc juz stoi na Expert, jedyne, co
     * sie jeszcze zmienia, to statystyki wrogow - wiec nagroda idzie od tego miejsca noga w noge
     * z nimi. Inaczej kazdy kolejny poziom bylby czysta inflacja zlota i po dwudziestu wejsciach
     * endless placilby wiecej niz cala kampania, nie bedac ani o jote trudniejszym.
     */
    private const val REWARD_PER_LEVEL_AFTER_CAP = ENEMY_PER_LEVEL

    /** Poziom startowy kazdego tematu, jeszcze nietkniętego. */
    const val FIRST_LEVEL = 1

    /**
     * Od ktorego szczebla drabinki startuja pytania na danym poziomie. Rosnie co dwa poziomy, zeby
     * awans byl odczuwalny, ale nie wywracal tematu z fali na fale: 1-2 Easy, 3-4 Medium,
     * 5-6 Hard, 7+ Expert.
     */
    fun startingDifficulty(level: Int): QuizDifficulty {
        val step = (level.coerceAtLeast(FIRST_LEVEL) - 1) / 2
        val index = min(step, QuizDifficulty.entries.size - 1)
        return QuizDifficulty.entries[index]
    }

    /**
     * Drabinka pytan w endless: startuje na [startingDifficulty], a im dalsza fala, tym wyzej -
     * dokladnie tak, jak drabinka kampanii rosnie z numerem fali. Jedno wejscie potrafi wiec
     * przejsc od poziomu tematu az do [QuizDifficulty.Expert], jesli gracz dociagnie daleko.
     */
    fun difficultyFor(level: Int, wave: Int): QuizDifficulty {
        val base = startingDifficulty(level).ordinal
        // Co trzecia fala jeden szczebel wyzej - przy [WAVES_PER_LEVEL] falach daje to trzy stopnie
        // awansu wewnatrz jednego wejscia. Dzielnik chodzi w parze z liczba fal: gdy ta spadla
        // z 20 na 10, podzial co piata fala zostawilby graczowi tylko dwa stopnie na cale wejscie.
        val climb = (wave - 1).coerceAtLeast(0) / 3
        val index = min(base + climb, QuizDifficulty.entries.size - 1)
        return QuizDifficulty.entries[index]
    }

    /**
     * Mnoznik lupu i XP na danym poziomie tematu. Rosnie bez konca, ale ZALAMUJE SIE na progu
     * trudnosci ([MAX_DIFFICULTY_LEVEL]) - patrz [REWARD_PER_LEVEL_AFTER_CAP].
     */
    fun rewardMultiplier(level: Int): Float {
        val steps = level.coerceAtLeast(FIRST_LEVEL) - 1
        val beforeCap = steps.coerceAtMost(MAX_DIFFICULTY_LEVEL - 1)
        val afterCap = steps - beforeCap
        return 1f + beforeCap * REWARD_PER_LEVEL + afterCap * REWARD_PER_LEVEL_AFTER_CAP
    }

    /**
     * Poziom, ktory realnie GRA w tym wejsciu - czyli po ktorym liczy sie nagroda i awans.
     *
     * Endless pozwala przybic staly poziom pytan ([com.lernhero.domain.run.RunSession.quizDifficulty])
     * i ten wybor ma pierwszenstwo przed drabinka tematu. Bez tej funkcji gracz z tematem na
     * poziomie 9 wybieral "Easy", dostawal rozgrzewkowe pytania i inkasowal za nie mnoznik
     * poziomu 9 - najoplacalniejszym sposobem gry w tryb nauki bylo NIE uczyc sie niczego.
     *
     * Regula jest wiec prosta: placi ten poziom, na ktorym naprawde sie gralo. [fixed] = null
     * (drabinka tematu) nic nie zmienia, a wybor TRUDNIEJSZY niz poziom tematu nie daje premii
     * ponad ten poziom - inaczej dalo by sie kupic mnoznik samym kliknieciem w "Expert".
     */
    fun effectiveLevel(level: Int, fixed: QuizDifficulty?): Int {
        val real = level.coerceAtLeast(FIRST_LEVEL)
        val chosen = fixed ?: return real
        return min(real, levelOf(chosen))
    }

    /**
     * Czy takie wejscie liczy sie do AWANSU tematu. Trenowanie latwiejszego szczebla niz wlasny
     * poziom tematu jest calkowicie w porzadku (po to jest wybor poziomu) - ale nie pcha tematu
     * wyzej, bo nie o tym mowi odznaka przy jego nazwie.
     */
    fun countsForMastery(level: Int, fixed: QuizDifficulty?): Boolean =
        effectiveLevel(level, fixed) >= level.coerceAtLeast(FIRST_LEVEL)

    /** Odwrotnosc [startingDifficulty]: najnizszy poziom tematu, na ktorym pytania startuja od [difficulty]. */
    private fun levelOf(difficulty: QuizDifficulty): Int = difficulty.ordinal * 2 + FIRST_LEVEL

    /**
     * Mnoznik HP i ataku wrogow. Do progu trudnosci rosnie WOLNIEJ niz nagrody ([REWARD_PER_LEVEL])
     * i to jest celowe: wyzszy poziom tematu ma sie oplacac, a nie tylko bolec. Powyzej progu oba
     * przyrosty sa juz rowne - patrz [REWARD_PER_LEVEL_AFTER_CAP].
     */
    fun enemyMultiplier(level: Int): Float =
        1f + (level.coerceAtLeast(FIRST_LEVEL) - 1) * ENEMY_PER_LEVEL

    /** Czy wyczyszczenie [wave] fal domyka poziom - patrz [WAVES_PER_LEVEL]. */
    fun clearsLevel(wave: Int): Boolean = wave >= WAVES_PER_LEVEL
}

/**
 * Poziomy wszystkich tematow gracza. Osobny typ zamiast golej mapy w [com.lernhero.domain.player.Player],
 * zeby czytanie poziomu nietknietego jeszcze tematu ("brak klucza") mialo jedna, oczywista
 * odpowiedz zamiast `?: 1` rozsianego po ekranach.
 *
 * Klucz to [QuizCategory.name], a nie ordinal - dopisanie kategorii w srodku enuma nie moze
 * przestawic graczom poziomow.
 */
@kotlinx.serialization.Serializable
data class CategoryLevels(
    val levels: Map<String, Int> = emptyMap()
) {
    fun levelOf(category: QuizCategory): Int =
        levels[category.name]?.coerceAtLeast(CategoryMastery.FIRST_LEVEL) ?: CategoryMastery.FIRST_LEVEL

    /** Temat po awansie o jeden poziom. */
    fun advanced(category: QuizCategory): CategoryLevels =
        copy(levels = levels + (category.name to levelOf(category) + 1))

    /** Suma poziomow ponad start - jedna liczba na "ile juz przerobilem", do ekranu postepu. */
    val totalMastery: Int
        get() = levels.values.sumOf { (it - CategoryMastery.FIRST_LEVEL).coerceAtLeast(0) }
}
