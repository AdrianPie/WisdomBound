package com.lernhero.domain.notification

import com.lernhero.domain.player.Player
import com.lernhero.domain.story.IdleRewards
import com.lernhero.domain.story.StoryProgress
import com.lernhero.domain.summon.FreeSummon

/**
 * Rodzaj przypomnienia. Id jest STALE i uzywane jako klucz w systemowym harmonogramie - dzieki
 * temu przeliczenie planu nie mnozy powiadomien, tylko nadpisuje to, ktore juz stalo w kolejce.
 */
enum class ReminderKind(val id: String) {
    /** Magazyn nagrod idle doszedl do sufitu - od tej chwili kampania kapie w prozne. */
    IdleStorageFull("idle_storage_full"),

    /** Odliczanie darmowego summona doszlo do zera. */
    FreeSummonReady("free_summon_ready"),

    /** Wieczorne szturchniecie, gdy dzienny cel (kroki / czytanie) jest jeszcze nieodhaczony. */
    DailyGoal("daily_goal")
}

/**
 * Jedno zaplanowane przypomnienie. Czysta dana - domena nie wie nic o kanalach, ikonach ani
 * uprawnieniach; warstwa platformy dostaje gotowa tresc i moment.
 */
data class GameReminder(
    val kind: ReminderKind,
    val title: String,
    val body: String,
    /** Kiedy ma sie pokazac (epoch millis). Zawsze w przyszlosci - patrz [ReminderPlan]. */
    val triggerAtMs: Long
)

/**
 * Co i kiedy przypomniec - policzone z zapisanego stanu gracza, bez zadnego wlasnego stanu.
 *
 * To jest ta sama sztuczka, co w [IdleRewards] i produkcji w miescie: wszystko wynika z ROZNICY
 * CZASU, wiec nie ma czego trzymac w tle i nie da sie rozjechac. Plan przelicza sie od nowa przy
 * kazdym wejsciu do gry, a harmonogram systemowy tylko nadpisuje sie kluczem [ReminderKind.id].
 *
 * Powody sa DOKLADNIE trzy i kazdy odpowiada mechanice, ktora naprawde stoi i czeka na gracza.
 * Nie ma tu "wroc do gry" bez tresci - powiadomienie, ktore nie niesie konkretu, uczy gracza
 * zmiatac je bez czytania, a potem wylaczac wszystkie.
 */
object ReminderPlan {

    /** O ktorej godzinie (czasu lokalnego) wypada wieczorne przypomnienie o celu dziennym. */
    const val DAILY_GOAL_HOUR = 20

    private const val MILLIS_PER_HOUR = 3_600_000L

    /**
     * [todayGoalDone] mowi, czy dzisiejszy cel dzienny jest juz odhaczony - liczy go warstwa
     * wyzej, bo zalezy od danych z krokomierza, ktorych domena nie widzi.
     *
     * [nextDailyGoalSlotMs] to najblizsza [DAILY_GOAL_HOUR] czasu LOKALNEGO, tez podana z zewnatrz:
     * strefy czasowe nie naleza do regul gry, a domena ma zostac bez zaleznosci od kalendarza.
     */
    fun forPlayer(
        player: Player,
        now: Long,
        todayGoalDone: Boolean,
        nextDailyGoalSlotMs: Long?
    ): List<GameReminder> = buildList {
        idleStorageFull(player, now)?.let(::add)
        addAll(freeSummonReady(player, now))
        if (!todayGoalDone && nextDailyGoalSlotMs != null && nextDailyGoalSlotMs > now) {
            add(
                GameReminder(
                    kind = ReminderKind.DailyGoal,
                    title = "Your daily goal is still open",
                    body = "A few minutes of reading or a short walk still counts today.",
                    triggerAtMs = nextDailyGoalSlotMs
                )
            )
        }
    }

    /**
     * Kiedy magazyn idle dojdzie do sufitu. Null, gdy nie ma czego pilnowac: kampania sie jeszcze
     * nie zaczela (nic nie kapie) albo magazyn JUZ jest pelny - w tym drugim przypadku moment
     * minal, a powiadomienie o przeszlosci nie ma sensu.
     */
    private fun idleStorageFull(player: Player, now: Long): GameReminder? {
        val bestStage = StoryProgress.bestClearedStage(player.storyProgress)
        if (bestStage <= 0) return null

        val since = player.idleCollectedAt
        if (since <= 0L) return null

        val fullAt = since + (IdleRewards.CAP_HOURS * MILLIS_PER_HOUR).toLong()
        if (fullAt <= now) return null

        return GameReminder(
            kind = ReminderKind.IdleStorageFull,
            title = "Your camp storage is full",
            body = "Campaign rewards stopped piling up - collect them to keep them coming.",
            triggerAtMs = fullAt
        )
    }

    /**
     * Najblizszy gotowy darmowy summon. Bierzemy WCZESNIEJSZY z dwoch bannerow, bo obydwa dziela
     * jedno powiadomienie - dwa osobne o tej samej tresci to najkrotsza droga do wylaczenia
     * wszystkich powiadomien przez gracza.
     */
    private fun freeSummonReady(player: Player, now: Long): List<GameReminder> {
        val readyTimes = listOf(
            FreeSummon.readyAt(player.freeSummonClaims[FreeSummon.BASIC_BANNER], FreeSummon.BASIC_COOLDOWN_MS),
            FreeSummon.readyAt(player.freeSummonClaims[FreeSummon.HEROIC_BANNER], FreeSummon.HEROIC_COOLDOWN_MS)
        ).filter { it > now }

        val soonest = readyTimes.minOrNull() ?: return emptyList()
        return listOf(
            GameReminder(
                kind = ReminderKind.FreeSummonReady,
                title = "A free summon is ready",
                body = "Your free pull is waiting in the Summon Center.",
                triggerAtMs = soonest
            )
        )
    }

    // Id bannerow - te same, ktorych uzywa Summon Center przy zapisie odbioru.
}
