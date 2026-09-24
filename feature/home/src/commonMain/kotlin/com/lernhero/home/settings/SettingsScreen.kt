package com.lernhero.home.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lernhero.domain.notification.ReminderKind
import com.lernhero.domain.repository.AccountSession
import com.lernhero.domain.run.RunSession
import com.lernhero.domain.story.CampaignSelection
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.shared.isDebugBuild
import com.lernhero.shared.i18n.AppLanguage
import com.lernhero.shared.i18n.AppLanguageStore
import com.lernhero.shared.i18n.strings
import com.lernhero.shared.notification.ReminderScheduler
import com.lernhero.shared.notification.requestNotificationAccess
import com.lernhero.shared.prefs.AppPreferences
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.koin.compose.koinInject

/**
 * Ustawienia - preferencje URZADZENIA (nie konta gracza).
 *
 * Zasada tego ekranu: **kazdy przelacznik ma czytelnika w kodzie gry**. Jezyk zmienia teksty
 * natychmiast ([AppLanguageStore] jest stanem Compose), przypomnienia przeliczaja kolejke
 * systemowa w tej samej chwili ([PlayerViewModel.remindersPreferencesChanged]), a tempo walki
 * jest tym, od ktorego startuje nastepna walka ([AppPreferences.battleSpeed] czytane w
 * BattleViewModel). Nic tu nie jest atrapa - i nic nowego nie powinno nia byc.
 *
 * Stan zyje w [AppPreferences] i [AppLanguageStore], nie w tym pliku: ekran nie trzyma wlasnej
 * kopii, wiec nie da sie go rozjechac z tym, co naprawde zapisane.
 */
@Composable
fun SettingsScreen(
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    insets: PaddingValues = PaddingValues(0.dp)
) {
    val languageStore = koinInject<AppLanguageStore>()
    val preferences = koinInject<AppPreferences>()
    val scheduler = koinInject<ReminderScheduler>()

    var resetMessage by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
        // Pasek z powrotem i tytulem zostaje na miejscu - przewija sie sama lista ustawien.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "‹",
                    color = Gold,
                    fontFamily = FontPixel(),
                    fontSize = 22.sp,
                    modifier = Modifier.clickable(onClick = onBack)
                )
                Text(
                    text = strings.settingsTitle.uppercase(),
                    color = Gold,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            SettingsSection(title = strings.settingsLanguage) {
                AppLanguage.entries.forEach { language ->
                    SettingsOptionRow(
                        label = language.nativeName,
                        selected = language == languageStore.current,
                        onClick = { languageStore.set(language) }
                    )
                }
                SettingsHint(strings.settingsLanguageHint)
            }

            NotificationsSection(
                preferences = preferences,
                scheduler = scheduler,
                onChanged = { playerViewModel.remindersPreferencesChanged() }
            )

            SettingsSection(title = strings.settingsBattle) {
                Text(
                    text = strings.settingsBattleSpeed,
                    color = Cream,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                SettingsChoiceRow(
                    // Lista predkosci pochodzi z HUD-u walki, wiec nie da sie tu ustawic tempa,
                    // ktorego walka nie zna.
                    options = BattleSpeed.options,
                    selected = preferences.battleSpeed,
                    label = { "${it.toInt()}x" },
                    onSelect = { preferences.chooseBattleSpeed(it) }
                )
                SettingsHint(strings.settingsBattleSpeedHint)
            }

            AccountSection(onSignedOut = onSignedOut)

            SettingsSection(title = strings.settingsAbout) {
                // Napis bierzemy TERAZ, w ciele composable - w lambdzie tapniecia `strings`
                // juz nie ma, bo to wlasnosc czytana z kompozycji.
                val doneText = strings.settingsResetDone
                SettingsButton(
                    text = strings.settingsReset,
                    filled = false,
                    onClick = {
                        preferences.resetToDefaults()
                        playerViewModel.remindersPreferencesChanged()
                        resetMessage = doneText
                    }
                )
                SettingsHint(resetMessage ?: strings.settingsResetHint)
                if (isDebugBuild) SettingsHint("debug build")
            }

            Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Powiadomienia. Trzy powody, wypisane wprost - gracz ma zobaczyc, na co sie zgadza, ZANIM
 * systemowe okienko zapyta. Prosba idzie dopiero po tapnieciu w przycisk, nigdy przy starcie gry:
 * zgoda wyproszona w pierwszej sekundzie, bez kontekstu, to najkrotsza droga do odmowy na zawsze.
 *
 * Stan zgody czytamy przy KAZDYM powrocie na ekran, bo odpowiedz z systemu przychodzi
 * asynchronicznie i moze przyjsc z ustawien systemowych, spoza aplikacji.
 *
 * Bez zgody systemu przelaczniki sa widoczne, ale przygaszone i martwe - chowanie ich sprawiloby,
 * ze po tapnieciu "Wlacz przypomnienia" polowa sekcji wyskakuje znikad.
 */
@Composable
private fun NotificationsSection(
    preferences: AppPreferences,
    scheduler: ReminderScheduler,
    onChanged: () -> Unit
) {
    var permitted by remember { mutableStateOf(scheduler.isPermitted) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val fresh = scheduler.isPermitted
                // Zgoda mogla wlasnie przyjsc z systemowego okienka - wtedy kolejka jest pusta
                // i trzeba ja zbudowac, bo poprzednie przeliczenie wyszlo z niej na starcie.
                if (fresh != permitted) {
                    permitted = fresh
                    if (fresh) onChanged()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val remindersOn = permitted && preferences.remindersEnabled

    SettingsSection(title = strings.settingsNotifications) {
        Text(
            text = strings.settingsNotificationsIntro,
            color = Cream,
            fontFamily = FontFirst(),
            fontSize = 11.sp
        )

        if (!permitted) {
            SettingsButton(
                text = strings.settingsNotificationsAsk,
                onClick = { requestNotificationAccess() }
                // Odpowiedz przychodzi asynchronicznie - stan podlapie ON_RESUME wyzej.
            )
            SettingsHint(strings.settingsNotificationsBlocked)
        }

        SettingsToggleRow(
            label = strings.settingsNotificationsAll,
            hint = strings.settingsNotificationsAllHint,
            checked = preferences.remindersEnabled,
            enabled = permitted,
            onCheckedChange = {
                preferences.setAllReminders(it)
                onChanged()
            }
        )

        ReminderKindRow(
            kind = ReminderKind.IdleStorageFull,
            label = strings.settingsReminderStorage,
            hint = strings.settingsReminderStorageHint,
            preferences = preferences,
            enabled = remindersOn,
            onChanged = onChanged
        )
        ReminderKindRow(
            kind = ReminderKind.FreeSummonReady,
            label = strings.settingsReminderSummon,
            hint = strings.settingsReminderSummonHint,
            preferences = preferences,
            enabled = remindersOn,
            onChanged = onChanged
        )
        ReminderKindRow(
            kind = ReminderKind.DailyGoal,
            label = strings.settingsReminderDaily,
            hint = strings.settingsReminderDailyHint,
            preferences = preferences,
            enabled = remindersOn,
            onChanged = onChanged
        )

        Text(
            text = strings.settingsReminderHour,
            color = Cream,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        SettingsChoiceRow(
            options = (AppPreferences.MIN_HOUR..AppPreferences.MAX_HOUR step 2).toList(),
            selected = preferences.dailyGoalHour.nearestOffered(),
            label = { "$it:00" },
            enabled = remindersOn && preferences.isReminderAllowed(ReminderKind.DailyGoal),
            onSelect = {
                preferences.chooseDailyGoalHour(it)
                onChanged()
            }
        )
    }
}

/**
 * Godziny pokazujemy co dwie ([AppPreferences.MIN_HOUR]..[AppPreferences.MAX_HOUR]), a zapisana
 * moze byc kazda - chocby domyslna z [com.lernhero.domain.notification.ReminderPlan]. Zamiast
 * zostawic rzad bez zadnego zaznaczenia, podswietlamy kafel najblizszy zapisanej godzinie.
 */
private fun Int.nearestOffered(): Int =
    (AppPreferences.MIN_HOUR..AppPreferences.MAX_HOUR step 2).minBy { hour ->
        val diff = hour - this
        if (diff < 0) -diff else diff
    }

@Composable
private fun ReminderKindRow(
    kind: ReminderKind,
    label: String,
    hint: String,
    preferences: AppPreferences,
    enabled: Boolean,
    onChanged: () -> Unit
) {
    SettingsToggleRow(
        label = label,
        hint = hint,
        // Przy wylaczonym glownym przelaczniku wiersz pokazuje WLASNY stan (przygaszony),
        // a nie falszywe "wylaczone" - po ponownym wlaczeniu wszystkiego nic sie nie gubi.
        checked = preferences.isReminderAllowed(kind),
        enabled = enabled,
        onCheckedChange = {
            preferences.setReminderEnabled(kind, it)
            onChanged()
        }
    )
}

/**
 * Konto. Do tej pory z gry NIE DALO SIE wyjsc - raz zalogowane konto zostawalo na telefonie na
 * zawsze, wiec drugi gracz w domu nie mial jak wejsc na swoje.
 *
 * Wylogowanie kasuje sesje lokalnie, a nie konto: postep siedzi w Firestore i wraca w calosci.
 * Dlatego wystarczy jedno potwierdzenie, bez straszenia utrata danych.
 *
 * Przed odeslaniem na logowanie czyscimy obiekty, ktore zyja tak dlugo jak proces ([RunSession],
 * [CampaignSelection]) - bez tego nastepne konto na tym samym telefonie zaczynaloby z krainą,
 * plansza i niedokonczonym runem poprzednika.
 */
@Composable
private fun AccountSection(onSignedOut: () -> Unit) {
    val session = koinInject<AccountSession>()
    val runSession = koinInject<RunSession>()
    val campaign = koinInject<CampaignSelection>()
    val scope = rememberCoroutineScope()

    var confirming by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf(false) }

    SettingsSection(title = strings.settingsAccount) {
        Text(
            text = session.email?.let { "${strings.settingsSignedInAs} $it" }
                ?: strings.settingsSignedInUnknown,
            color = Cream,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        SettingsButton(
            text = if (confirming) strings.settingsSignOutConfirm else strings.settingsSignOut,
            filled = confirming,
            onClick = {
                if (working) return@SettingsButton
                if (!confirming) {
                    confirming = true
                    return@SettingsButton
                }
                working = true
                scope.launch {
                    session.signOut()
                    runSession.startNewRun()
                    campaign.reset()
                    onSignedOut()
                }
            }
        )
        SettingsHint(strings.settingsSignOutHint)
    }
}
