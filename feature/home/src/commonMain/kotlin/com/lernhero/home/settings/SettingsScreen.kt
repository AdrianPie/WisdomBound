package com.lernhero.home.settings

import com.lernhero.shared.monetization.AdPrivacy
import com.lernhero.domain.repository.AccountDeletion
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.lernhero.shared.appVersionName
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.painterResource
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
import androidx.compose.ui.text.style.TextOverflow
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
import org.koin.compose.koinInject

/**
 * Ustawienia - preferencje URZADZENIA i konto.
 *
 * Zasada tego ekranu: **kazdy przelacznik ma czytelnika w kodzie gry**. Jezyk zmienia teksty
 * natychmiast ([AppLanguageStore] jest stanem Compose), przypomnienia przeliczaja kolejke
 * systemowa w tej samej chwili ([PlayerViewModel.remindersPreferencesChanged]), a tempo walki
 * jest tym, od ktorego startuje nastepna walka ([AppPreferences.battleSpeed] czytane w
 * BattleViewModel). Nic tu nie jest atrapa - i nic nowego nie powinno nia byc.
 *
 * Kolejnosc grup: konto na gorze (kim jestem), potem to, co sie zmienia najczesciej, a akcje
 * nieodwracalne osobno na samym dole, w czerwonej strefie - zeby nikt ich nie tapnal przypadkiem.
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
    val session = koinInject<AccountSession>()

    var resetMessage by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
        ) {
            // Pasek z powrotem i tytulem zostaje na miejscu - przewija sie sama lista ustawien.
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Panel)
                        .border(1.dp, FrameDim, CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "‹", color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = strings.settingsTitle,
                    color = Cream,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                AccountCard(email = session.email)

                SettingsGroup(title = strings.settingsGeneral) {
                    SettingsRow(
                        icon = "🌐",
                        iconTint = Color(0xFF5AA9FF),
                        title = strings.settingsLanguage,
                        subtitle = strings.settingsLanguageHint,
                        below = {
                            SettingsSegmented(
                                options = AppLanguage.entries,
                                selected = languageStore.current,
                                label = { it.nativeName },
                                onSelect = { languageStore.set(it) }
                            )
                        }
                    )
                    SettingsRow(
                        icon = "🔊",
                        iconTint = Color(0xFF6FD08C),
                        title = strings.settingsSound,
                        subtitle = strings.settingsSoundHint,
                        onClick = { preferences.chooseSound(!preferences.soundEnabled) },
                        trailing = { SettingsSwitch(checked = preferences.soundEnabled) }
                    )
                    SettingsRow(
                        icon = "⚔️",
                        iconTint = Color(0xFFFF8A4C),
                        title = strings.settingsBattleSpeed,
                        subtitle = strings.settingsBattleSpeedHint,
                        divider = false,
                        below = {
                            SettingsSegmented(
                                // Lista predkosci pochodzi z HUD-u walki, wiec nie da sie tu
                                // ustawic tempa, ktorego walka nie zna.
                                options = BattleSpeed.options,
                                selected = preferences.battleSpeed,
                                label = { "${it.toInt()}x" },
                                onSelect = { preferences.chooseBattleSpeed(it) }
                            )
                        }
                    )
                }

                NotificationsGroup(
                    preferences = preferences,
                    scheduler = scheduler,
                    onChanged = { playerViewModel.remindersPreferencesChanged() }
                )

                // Napis bierzemy TERAZ, w ciele composable - w lambdzie tapniecia `strings`
                // juz nie ma, bo to wlasnosc czytana z kompozycji.
                val doneText = strings.settingsResetDone
                SettingsGroup(title = strings.settingsAbout, footer = resetMessage) {
                    if (AdPrivacy.optionsRequired) {
                        SettingsRow(
                            icon = "🛡️",
                            iconTint = Color(0xFF6FD08C),
                            title = strings.settingsAdPrivacy,
                            onClick = AdPrivacy::showOptions
                        )
                    }
                    SettingsRow(
                        icon = "📖",
                        iconTint = Color(0xFFFFD34D),
                        title = strings.settingsHowToPlay,
                        subtitle = strings.settingsHowToPlayHint,
                        onClick = {
                            // Samouczek rysuje sie nad cala gra (HomeNavGraph) - wracamy do gry i tam
                            // zaczyna sie od powitania medrca.
                            preferences.startTutorial()
                            onBack()
                        }
                    )
                    SettingsRow(
                        icon = "↺",
                        iconTint = Color(0xFFB38CFF),
                        title = strings.settingsReset,
                        subtitle = strings.settingsResetHint,
                        onClick = {
                            preferences.resetToDefaults()
                            playerViewModel.remindersPreferencesChanged()
                            resetMessage = doneText
                        }
                    )
                    SettingsRow(
                        icon = "ℹ️",
                        iconTint = Muted,
                        title = strings.settingsVersion,
                        divider = false,
                        trailing = {
                            SettingsValue(appVersionName() + if (isDebugBuild) " · debug" else "")
                        }
                    )
                }

                DangerZone(onSignedOut = onSignedOut)

                Footer()
            }
        }
    }
}

/** Karta "kim jestem" - godlo i adres konta. */
@Composable
private fun AccountCard(email: String?) {
    SettingsGroup(title = strings.settingsAccount) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Ink)
                    .border(2.dp, Gold.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Resources.Image.emblem),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (email != null) strings.settingsSignedInAs else strings.settingsSignedInUnknown,
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
                if (email != null) {
                    Text(
                        text = email,
                        color = Cream,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun Footer() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            painter = painterResource(Resources.Image.emblem),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            alpha = 0.5f
        )
        Text(
            text = "WisdomBound  v${appVersionName()}",
            color = Muted.copy(alpha = 0.7f),
            fontFamily = FontFirst(),
            fontSize = 11.sp
        )
    }
}

/**
 * Powiadomienia. Trzy powody, wypisane wprost - gracz ma zobaczyc, na co sie zgadza, ZANIM
 * systemowe okienko zapyta. Prosba idzie dopiero po tapnieciu w przycisk, nigdy przy starcie gry.
 *
 * Stan zgody czytamy przy KAZDYM powrocie na ekran, bo odpowiedz z systemu przychodzi
 * asynchronicznie i moze przyjsc z ustawien systemowych, spoza aplikacji.
 *
 * Bez zgody systemu przelaczniki sa widoczne, ale przygaszone i martwe.
 */
@Composable
private fun NotificationsGroup(
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

    SettingsGroup(
        title = strings.settingsNotifications,
        footer = strings.settingsNotificationsIntro
    ) {
        if (!permitted) {
            SettingsRow(
                icon = "🔕",
                iconTint = Gold,
                title = strings.settingsNotificationsAsk,
                subtitle = strings.settingsNotificationsBlocked,
                // Odpowiedz przychodzi asynchronicznie - stan podlapie ON_RESUME wyzej.
                onClick = { requestNotificationAccess() },
                trailing = { SettingsPillButton(strings.settingsOn) { requestNotificationAccess() } }
            )
        }

        SettingsRow(
            icon = "🔔",
            iconTint = Gold,
            title = strings.settingsNotificationsAll,
            subtitle = strings.settingsNotificationsAllHint,
            enabled = permitted,
            onClick = {
                preferences.setAllReminders(!preferences.remindersEnabled)
                onChanged()
            },
            trailing = { SettingsSwitch(checked = preferences.remindersEnabled) }
        )
        ReminderKindRow(ReminderKind.IdleStorageFull, "🏕️", Color(0xFF6FD08C),
            strings.settingsReminderStorage, strings.settingsReminderStorageHint, preferences, remindersOn, onChanged)
        ReminderKindRow(ReminderKind.FreeSummonReady, "✨", Color(0xFFB38CFF),
            strings.settingsReminderSummon, strings.settingsReminderSummonHint, preferences, remindersOn, onChanged)
        ReminderKindRow(ReminderKind.DailyGoal, "🎯", Color(0xFFFF8A4C),
            strings.settingsReminderDaily, strings.settingsReminderDailyHint, preferences, remindersOn, onChanged)

        SettingsRow(
            icon = "🌙",
            iconTint = Color(0xFF5AA9FF),
            title = strings.settingsReminderHour,
            divider = false,
            enabled = remindersOn && preferences.isReminderAllowed(ReminderKind.DailyGoal),
            below = {
                SettingsSegmented(
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
    icon: String,
    tint: Color,
    label: String,
    hint: String,
    preferences: AppPreferences,
    enabled: Boolean,
    onChanged: () -> Unit
) {
    // Przy wylaczonym glownym przelaczniku wiersz pokazuje WLASNY stan (przygaszony),
    // a nie falszywe "wylaczone" - po ponownym wlaczeniu wszystkiego nic sie nie gubi.
    val checked = preferences.isReminderAllowed(kind)
    SettingsRow(
        icon = icon,
        iconTint = tint,
        title = label,
        subtitle = hint,
        enabled = enabled,
        onClick = {
            preferences.setReminderEnabled(kind, !checked)
            onChanged()
        },
        trailing = { SettingsSwitch(checked = checked) }
    )
}

/**
 * Wylogowanie i usuniecie konta - osobno, na dole, na czerwono.
 *
 * Wylogowanie kasuje sesje lokalnie, a nie konto: postep siedzi w Firestore i wraca w calosci,
 * wiec wystarczy jedno potwierdzenie. Usuniecie jest nieodwracalne - drugie tapniecie.
 *
 * Przed odeslaniem na logowanie czyscimy obiekty, ktore zyja tak dlugo jak proces ([RunSession],
 * [CampaignSelection]) - bez tego nastepne konto na tym samym telefonie zaczynaloby z kraina,
 * plansza i niedokonczonym runem poprzednika.
 */
@Composable
private fun DangerZone(onSignedOut: () -> Unit) {
    val session = koinInject<AccountSession>()
    val runSession = koinInject<RunSession>()
    val campaign = koinInject<CampaignSelection>()
    val scope = rememberCoroutineScope()

    var confirming by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    val needsLoginText = strings.settingsDeleteNeedsLogin
    val failedText = strings.settingsDeleteFailed

    SettingsGroup(title = strings.settingsDangerZone, footer = deleteError) {
        SettingsRow(
            icon = "🚪",
            iconTint = Gold,
            title = if (confirming) strings.settingsSignOutConfirm else strings.settingsSignOut,
            titleColor = if (confirming) Gold else Cream,
            subtitle = strings.settingsSignOutHint,
            onClick = {
                if (working) return@SettingsRow
                if (!confirming) {
                    confirming = true
                    return@SettingsRow
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
        SettingsRow(
            icon = "🗑️",
            iconTint = Danger,
            title = if (confirmingDelete) strings.settingsDeleteAccountConfirm else strings.settingsDeleteAccount,
            titleColor = Danger,
            subtitle = strings.settingsDeleteAccountHint,
            divider = false,
            onClick = {
                if (working) return@SettingsRow
                if (!confirmingDelete) {
                    confirmingDelete = true
                    deleteError = null
                    return@SettingsRow
                }
                working = true
                scope.launch {
                    when (session.deleteAccount()) {
                        AccountDeletion.Deleted -> {
                            runSession.startNewRun()
                            campaign.reset()
                            onSignedOut()
                        }
                        AccountDeletion.NeedsRecentLogin -> deleteError = needsLoginText
                        AccountDeletion.Failed -> deleteError = failedText
                    }
                    confirmingDelete = false
                    working = false
                }
            }
        )
    }
}
