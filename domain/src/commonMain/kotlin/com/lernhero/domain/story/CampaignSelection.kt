package com.lernhero.domain.story

import com.lernhero.domain.run.RunDifficulty

/**
 * Gdzie gracz "stoi" w kampanii: ktora kraina i na jakim tierze trudnosci. Jeden obiekt na
 * aplikacje (Koin, tak samo jak [com.lernhero.domain.run.RunSession]), bo ten wybor musi przezyc
 * przejscie miedzy zakladka "Walcz" a ekranem Mapy i z powrotem - a to sa dwa rozne miejsca
 * nawigacji, wiec zwykly `remember` by go zgubil.
 *
 * To NIE jest stan runu - run zaczyna sie dopiero po kliknieciu BATTLE. Tutaj siedzi wylacznie
 * "co mam wybrane na mapie".
 */
class CampaignSelection {

    /** Kraina wybrana na Mapie. Null = jeszcze nie wybrano, uzyj tej, w ktorej gracz aktualnie jest. */
    var worldId: String? = null
        private set

    /** Tier trudnosci wybrany na Mapie. */
    var tier: RunDifficulty = RunDifficulty.Normal
        private set

    /**
     * Konkretna plansza wybrana w rzedzie na dole zakladki "Walcz". Null = trzymaj sie frontiera
     * (pierwszej niezdobytej) - dzieki temu po wygranej gracz automatycznie przesuwa sie dalej,
     * zamiast tkwic na planszy, ktora wlasnie przeszedl.
     */
    var stage: Int? = null
        private set

    /**
     * Zapomina, co bylo wybrane. Wolane przy wylogowaniu: ten obiekt zyje tak dlugo jak proces,
     * wiec bez tego nastepne konto zalogowane na tym samym telefonie zaczynaloby na krainie
     * i planszy poprzednika.
     */
    fun reset() {
        worldId = null
        tier = RunDifficulty.Normal
        stage = null
    }

    fun select(worldId: String, tier: RunDifficulty) {
        this.worldId = worldId
        this.tier = tier
        // Zmiana krainy/tieru uniewaznia recznie wybrana plansze - tamten numer nalezal do innego
        // miejsca na mapie i po przelaczeniu wskazywalby zupelnie nie to, co gracz widzi.
        this.stage = null
    }

    fun selectStage(globalStage: Int) {
        this.stage = globalStage
    }

    /** Po wygranej: przesuwamy sie na kolejna plansze zamiast zostawac na zdobytej. */
    fun clearStage() {
        this.stage = null
    }

    /**
     * Kraina, ktora ma byc pokazana: wybrana recznie, a gdy jej nie ma - ta, w ktorej lezy
     * frontier (czyli miejsce, do ktorego gracz doszedl).
     */
    fun resolveWorld(cleared: Map<String, Int>): StoryWorld {
        worldId?.let { id -> StoryWorldCatalog.byId(id)?.let { return it } }
        val frontier = StoryProgress.frontier(cleared, tier)
        return StoryWorldCatalog.worldForGlobalStage(frontier)
            ?: StoryWorldCatalog.all.last()
    }

    /** Plansza, ktora ma byc pokazana: wybrana recznie albo frontier, przyciety do widocznej krainy. */
    fun resolveStage(cleared: Map<String, Int>): Int {
        val world = resolveWorld(cleared)
        val start = StoryWorldCatalog.startIndexOf(world.id)
        val end = start + world.stageCount - 1
        val frontier = StoryProgress.frontier(cleared, tier)
        return (stage ?: frontier).coerceIn(start, end)
    }
}
