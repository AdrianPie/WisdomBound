package com.lernhero.domain.dungeon

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DungeonMapGeneratorTest {

    private val seeds = listOf(1L, 42L, 1337L, -9000L, 123456789L)

    @Test
    fun `ten sam seed daje ta sama mape`() {
        val a = DungeonMapGenerator.generate(42L)
        val b = DungeonMapGenerator.generate(42L)
        assertEquals(a, b, "Mapa musi byc funkcja seeda - inaczej MMO nie zsynchronizuje graczy")
    }

    @Test
    fun `rozne seedy daja rozne mapy`() {
        val maps = seeds.map { DungeonMapGenerator.generate(it).nodes }
        assertEquals(maps.size, maps.distinct().size, "Seedy nie moga produkowac tej samej mapy")
    }

    @Test
    fun `pietra ustawione na sztywno maja wlasciwe typy`() {
        val config = DungeonConfig()
        seeds.forEach { seed ->
            val map = DungeonMapGenerator.generate(seed, config)

            assertTrue(map.nodesOnFloor(0).all { it.type == DungeonNodeType.Enemy }, "Start to zawsze walka")
            assertTrue(
                map.nodesOnFloor(config.treasureFloor).all { it.type == DungeonNodeType.Treasure },
                "Pietro skarbu"
            )
            assertTrue(
                map.nodesOnFloor(config.campfireFloor).all { it.type == DungeonNodeType.Rest },
                "Ognisko przed bossem"
            )

            val bossFloor = map.nodesOnFloor(config.bossFloor)
            assertEquals(1, bossFloor.size, "Boss stoi na mapie sam")
            assertEquals(DungeonNodeType.Boss, bossFloor.single().type)
        }
    }

    @Test
    fun `kazda sciezka prowadzi do bossa`() {
        seeds.forEach { seed ->
            val map = DungeonMapGenerator.generate(seed)
            val boss = map.boss!!

            // BFS od pol startowych - kazde osiagalne pole musi miec wyjscie, a boss musi byc osiagalny.
            val queue = ArrayDeque(map.entryNodes)
            val seen = mutableSetOf<String>()
            while (queue.isNotEmpty()) {
                val node = queue.removeFirst()
                if (!seen.add(node.id)) continue
                if (node.id == boss.id) continue

                assertTrue(node.next.isNotEmpty(), "Pole ${node.id} to slepy zaulek")
                node.next.forEach { nextId ->
                    val next = map.node(nextId)
                    assertTrue(next != null, "Krawedz do nieistniejacego pola $nextId")
                    assertEquals(node.floor + 1, next.floor, "Krawedz musi isc o jedno pietro w gore")
                    queue += next
                }
            }
            assertTrue(boss.id in seen, "Boss nieosiagalny dla seeda $seed")
            assertTrue(map.entryNodes.size >= 2, "Gracz musi miec realny wybor na starcie")
        }
    }

    @Test
    fun `krawedzie sie nie krzyzuja`() {
        seeds.forEach { seed ->
            val map = DungeonMapGenerator.generate(seed)
            val edges = map.nodes.flatMap { node ->
                node.next.mapNotNull { map.node(it) }.map { next ->
                    Triple(node.floor, node.column, next.column)
                }
            }.toSet()

            edges.forEach { (floor, from, to) ->
                if (from == to) return@forEach
                // Krzyzowanie: a->b oraz b->a miedzy tymi samymi pietrami.
                assertTrue(
                    Triple(floor, to, from) !in edges,
                    "Skrzyzowane krawedzie na pietrze $floor ($from<->$to), seed $seed"
                )
            }
        }
    }
}
