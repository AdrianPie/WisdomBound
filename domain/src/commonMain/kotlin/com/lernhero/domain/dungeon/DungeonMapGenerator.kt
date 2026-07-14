package com.lernhero.domain.dungeon

import kotlin.random.Random

/**
 * Generator mapy w stylu Slay the Spire: kilka losowych sciezek idacych od pietra 0 w gore,
 * ktore sie schodza i rozchodza, a na szczycie laczy je boss.
 *
 * Cala mapa jest funkcja seeda - `generate(42)` zawsze da ten sam graf. To wazne dla MMO:
 * serwer losuje jedna liczbe, kazdy klient odtwarza identyczna mape.
 */
object DungeonMapGenerator {

    fun generate(seed: Long, config: DungeonConfig = DungeonConfig()): DungeonMap {
        val random = Random(seed)
        val graph = buildGraph(random, config)
        val types = assignTypes(random, config, graph)

        val nodes = graph.map { (cell, next) ->
            DungeonNode(
                id = cell.id,
                floor = cell.floor,
                column = cell.column,
                type = types.getValue(cell),
                next = next.map { it.id }.sorted()
            )
        }.sortedWith(compareBy({ it.floor }, { it.column }))

        return DungeonMap(
            seed = seed,
            floors = config.floors,
            columns = config.columns,
            nodes = nodes
        )
    }

    // region Graf

    private data class Cell(val floor: Int, val column: Int) {
        val id: String get() = "f${floor}c$column"
    }

    /**
     * Puszcza [DungeonConfig.paths] losowych spacerow od pietra 0 do pietra pod bossem.
     * Kazdy krok idzie o jedna kolumne w lewo, prosto albo w prawo - dzieki temu krawedzie
     * sa krotkie i mapa wyglada na recznie rysowana, a nie na plataniny linii.
     */
    private fun buildGraph(random: Random, config: DungeonConfig): Map<Cell, Set<Cell>> {
        val edges = mutableMapOf<Cell, MutableSet<Cell>>()
        val topFloor = config.bossFloor - 1

        val startColumns = mutableListOf<Int>()
        repeat(config.paths) { pathIndex ->
            var column = random.nextInt(config.columns)
            // Druga sciezka musi ruszyc z innej kolumny niz pierwsza, inaczej mapa potrafi
            // zaczac sie od jednego pola i pierwszy wybor gracza jest pozorny.
            if (pathIndex == 1 && column == startColumns.first() && config.columns > 1) {
                column = (column + 1 + random.nextInt(config.columns - 1)) % config.columns
            }
            startColumns += column

            for (floor in 0 until topFloor) {
                val from = Cell(floor, column)
                val nextColumn = pickNextColumn(random, config, edges, from)
                edges.getOrPut(from) { mutableSetOf() } += Cell(floor + 1, nextColumn)
                column = nextColumn
            }
            edges.getOrPut(Cell(topFloor, column)) { mutableSetOf() }
        }

        // Boss zbiera wszystkie sciezki - kazde pole ostatniego zwyklego pietra prowadzi do niego.
        val boss = Cell(config.bossFloor, config.columns / 2)
        edges.keys.filter { it.floor == topFloor }.forEach { cell ->
            edges.getValue(cell) += boss
        }
        edges[boss] = mutableSetOf()

        return edges
    }

    /**
     * Wybiera kolumne na kolejnym pietrze sposrod {-1, 0, +1}, odrzucajac ruchy poza mape
     * i takie, ktore skrzyzowalyby sie z juz istniejaca krawedzia.
     */
    private fun pickNextColumn(
        random: Random,
        config: DungeonConfig,
        edges: Map<Cell, Set<Cell>>,
        from: Cell
    ): Int {
        val candidates = listOf(-1, 0, 1)
            .map { from.column + it }
            .filter { it in 0 until config.columns }
            .filterNot { wouldCross(edges, from, it) }

        // Gdy wszystko odpadnie (ciasny rog mapy), zostajemy w tej samej kolumnie.
        if (candidates.isEmpty()) return from.column
        return candidates[random.nextInt(candidates.size)]
    }

    /**
     * Dwie krawedzie krzyzuja sie, gdy zamieniaja sie kolumnami: a->b oraz b->a miedzy tymi
     * samymi pietrami. Slay the Spire tego zakazuje, bo skrzyzowane linie sa nieczytelne -
     * gracz nie widzi, ktora sciezka gdzie prowadzi.
     */
    private fun wouldCross(edges: Map<Cell, Set<Cell>>, from: Cell, toColumn: Int): Boolean {
        if (toColumn == from.column) return false
        val neighbour = Cell(from.floor, toColumn)
        val mirror = Cell(from.floor + 1, from.column)
        return edges[neighbour]?.contains(mirror) == true
    }

    // endregion

    // region Typy pol

    /**
     * Nadaje polom typy. Kilka pieter jest ustawionych na sztywno (start, skarb, ognisko, boss),
     * reszta idzie z losowania wagowego - a potem poprawiamy wynik, zeby dwa te same "specjalne"
     * pola nie stykaly sie krawedzia.
     */
    private fun assignTypes(
        random: Random,
        config: DungeonConfig,
        graph: Map<Cell, Set<Cell>>
    ): Map<Cell, DungeonNodeType> {
        val types = mutableMapOf<Cell, DungeonNodeType>()

        graph.keys.sortedWith(compareBy({ it.floor }, { it.column })).forEach { cell ->
            types[cell] = when (cell.floor) {
                config.bossFloor -> DungeonNodeType.Boss
                // Pierwsze pietro zawsze walka: run ma sie zaczac od czegos, co uczy zasad.
                0 -> DungeonNodeType.Enemy
                config.treasureFloor -> DungeonNodeType.Treasure
                config.campfireFloor -> DungeonNodeType.Rest
                else -> rollType(random, config, cell.floor)
            }
        }

        fixAdjacentDuplicates(random, config, graph, types)
        return types
    }

    private fun rollType(random: Random, config: DungeonConfig, floor: Int): DungeonNodeType {
        val weights = buildList {
            add(DungeonNodeType.Enemy to 45)
            add(DungeonNodeType.Event to 22)
            if (floor >= config.minEliteFloor) add(DungeonNodeType.Elite to 16)
            if (floor >= config.minShopFloor) add(DungeonNodeType.Shop to 8)
            if (floor >= config.minRestFloor) add(DungeonNodeType.Rest to 10)
        }

        var roll = random.nextInt(weights.sumOf { it.second })
        for ((type, weight) in weights) {
            roll -= weight
            if (roll < 0) return type
        }
        return DungeonNodeType.Enemy
    }

    /**
     * Dwa sklepy (albo dwa ogniska, albo dwie elity) jedno nad drugim to zmarnowany wybor -
     * gracz nie ma czego wazyc. Takie pary rozbijamy, zamieniajac gorne pole na walke lub event.
     */
    private fun fixAdjacentDuplicates(
        random: Random,
        config: DungeonConfig,
        graph: Map<Cell, Set<Cell>>,
        types: MutableMap<Cell, DungeonNodeType>
    ) {
        val unique = setOf(DungeonNodeType.Shop, DungeonNodeType.Rest, DungeonNodeType.Elite)

        graph.forEach { (from, targets) ->
            val fromType = types[from] ?: return@forEach
            if (fromType !in unique) return@forEach

            targets.forEach { to ->
                // Pieter ustawionych na sztywno nie ruszamy - one maja byc przewidywalne.
                if (to.floor == config.bossFloor || to.floor == config.campfireFloor) return@forEach
                if (to.floor == config.treasureFloor) return@forEach

                if (types[to] == fromType) {
                    types[to] = if (random.nextInt(100) < 70) DungeonNodeType.Enemy else DungeonNodeType.Event
                }
            }
        }
    }

    // endregion
}
