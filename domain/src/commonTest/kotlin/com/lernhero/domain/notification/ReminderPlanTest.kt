package com.lernhero.domain.notification

import com.lernhero.domain.player.Player
import com.lernhero.domain.run.RunDifficulty
import com.lernhero.domain.story.IdleRewards
import com.lernhero.domain.summon.FreeSummon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Plan przypomnien. Caly system opiera sie na jednej zasadzie: powiadomienie wychodzi TYLKO wtedy,
 * gdy naprawde cos stoi i czeka. Te testy pilnuja glownie sytuacji odwrotnych - kiedy plan ma byc
 * PUSTY - bo to one decyduja o tym, czy gracz zostawi powiadomienia wlaczone.
 */
class ReminderPlanTest {

    private val hour = 3_600_000L
    private val now = 1_700_000_000_000L

    private fun player(
        idleCollectedAt: Long = 0L,
        cleared: Map<String, Int> = emptyMap(),
        freeSummonClaims: Map<String, Long> = emptyMap()
    ) = Player(
        idleCollectedAt = idleCollectedAt,
        storyProgress = cleared,
        freeSummonClaims = freeSummonClaims
    )

    private fun plan(
        player: Player,
        todayGoalDone: Boolean = true,
        dailySlot: Long? = null
    ) = ReminderPlan.forPlayer(player, now, todayGoalDone, dailySlot)

    @Test
    fun `swieze konto nie dostaje zadnego powiadomienia`() {
        assertTrue(plan(player()).isEmpty(), "nic sie jeszcze nie dzieje, nie ma o czym przypominac")
    }

    @Test
    fun `bez zdobytej planszy nie ma przypomnienia o magazynie`() {
        // Nagrody idle kapia dopiero od pierwszej zdobytej planszy (IdleRewards.ratePerHour).
        val p = player(idleCollectedAt = now - hour, cleared = emptyMap())
        assertFalse(plan(p).any { it.kind == ReminderKind.IdleStorageFull })
    }

    @Test
    fun `magazyn zapelni sie po CAP_HOURS od ostatniego odbioru`() {
        val collectedAt = now - hour
        val p = player(idleCollectedAt = collectedAt, cleared = mapOf(RunDifficulty.Normal.name to 12))

        val reminder = plan(p).firstOrNull { it.kind == ReminderKind.IdleStorageFull }
        assertNotNull(reminder, "kampania ruszyla i magazyn kapie - powinno byc przypomnienie")
        assertEquals(
            collectedAt + (IdleRewards.CAP_HOURS * hour).toLong(),
            reminder.triggerAtMs
        )
        assertTrue(reminder.triggerAtMs > now, "moment musi byc w przyszlosci")
    }

    @Test
    fun `pelny magazyn nie generuje przypomnienia o przeszlosci`() {
        // Odbior byl dawno temu - sufit dawno przekroczony, moment "zapelnienia" juz minal.
        val p = player(
            idleCollectedAt = now - (hour * 100),
            cleared = mapOf(RunDifficulty.Normal.name to 12)
        )
        assertFalse(
            plan(p).any { it.kind == ReminderKind.IdleStorageFull },
            "nie przypominamy o czyms, co stalo sie wczoraj"
        )
    }

    @Test
    fun `darmowy summon - jedno przypomnienie o wczesniejszym z dwoch bannerow`() {
        val p = player(
            freeSummonClaims = mapOf(
                // Heroiczny odebrany przed chwila (3 dni cooldownu), zwykly godzine temu (6 godzin).
                "heroic" to now - hour,
                "basic" to now - hour
            )
        )
        val summonReminders = plan(p).filter { it.kind == ReminderKind.FreeSummonReady }
        assertEquals(1, summonReminders.size, "dwa bannery, ale jedno powiadomienie")
        assertEquals(
            (now - hour) + FreeSummon.BASIC_COOLDOWN_MS,
            summonReminders.single().triggerAtMs,
            "bierzemy WCZESNIEJSZY z dwoch - zwykly banner odnawia sie szybciej"
        )
    }

    @Test
    fun `gotowy summon nie generuje przypomnienia`() {
        val p = player(freeSummonClaims = mapOf("basic" to now - (hour * 50)))
        assertFalse(
            plan(p).any { it.kind == ReminderKind.FreeSummonReady },
            "los juz czeka w grze - powiadomienie o nim byloby o przeszlosci"
        )
    }

    @Test
    fun `odhaczony cel dzienny nie jest szturchany`() {
        val slot = now + hour * 5
        assertFalse(
            plan(player(), todayGoalDone = true, dailySlot = slot)
                .any { it.kind == ReminderKind.DailyGoal },
            "kto juz dzis cos zrobil, nie ma byc o to pytany"
        )
    }

    @Test
    fun `nieodhaczony cel dzienny dostaje wieczorne przypomnienie`() {
        val slot = now + hour * 5
        val reminder = plan(player(), todayGoalDone = false, dailySlot = slot)
            .firstOrNull { it.kind == ReminderKind.DailyGoal }
        assertNotNull(reminder)
        assertEquals(slot, reminder.triggerAtMs)
    }

    @Test
    fun `brak slotu z platformy nie wysadza planu`() {
        // Desktop zwraca null z nextLocalHourMillis - plan ma to znosic, a nie rzucac.
        assertFalse(
            plan(player(), todayGoalDone = false, dailySlot = null)
                .any { it.kind == ReminderKind.DailyGoal }
        )
    }

    @Test
    fun `kazde przypomnienie ma tresc i moment w przyszlosci`() {
        val p = player(
            idleCollectedAt = now - hour,
            cleared = mapOf(RunDifficulty.Normal.name to 12),
            freeSummonClaims = mapOf("basic" to now - hour)
        )
        val reminders = plan(p, todayGoalDone = false, dailySlot = now + hour * 5)
        assertEquals(3, reminders.size, "wszystkie trzy powody naraz")
        reminders.forEach {
            assertTrue(it.title.isNotBlank(), "${it.kind} bez tytulu")
            assertTrue(it.body.isNotBlank(), "${it.kind} bez tresci")
            assertTrue(it.triggerAtMs > now, "${it.kind} w przeszlosci")
        }
    }

    @Test
    fun `rodzaje maja unikalne id - to one sa kluczem w harmonogramie`() {
        val ids = ReminderKind.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
