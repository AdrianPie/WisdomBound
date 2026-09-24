package com.lernhero.shared.i18n

/**
 * All player-facing UI text, as Kotlin - not XML string resources.
 *
 * Compose Multiplatform's resource pipeline picks a language from the OS locale, with no simple
 * runtime override across Android/iOS/Desktop alike. A plain Kotlin interface sidesteps that
 * entirely: [current] below just reads [AppLanguageStore], so switching language in Settings
 * recomposes every open screen immediately, and every string is typo-checked by the compiler
 * instead of by a key that only fails at runtime.
 *
 * This grows screen by screen as the UI is migrated off hardcoded text - a missing entry here is
 * a screen not yet converted, not a bug in this file. Group by SCREEN, not by word, so translating
 * one screen means reading one contiguous block.
 */
interface AppStrings {

    /**
     * Kod jezyka tej wersji tekstow ("en"/"pl"). Trzymany TUTAJ, a nie tylko w [AppLanguageStore],
     * bo ekrany czytaja jezyk przez [strings] i nie musza wtedy siegac po singleton ze store'a -
     * a niektore decyzje (np. ktore kategorie quizu pokazac, patrz
     * [com.lernhero.domain.quiz.QuizCategory.builtinFor]) zaleza od jezyka, nie od pojedynczego napisu.
     */
    val languageCode: String

    /* ---- common (shared across many screens/dialogs) ---- */
    val commonStart: String
    val commonCancel: String
    val commonSave: String
    val commonDelete: String
    val commonBack: String
    val commonClose: String
    val commonConfirm: String
    val commonLoading: String

    /* ---- bottom nav bar ---- */
    val navHome: String
    val navGrowth: String
    val navFight: String
    val navProgress: String
    /** Zakladka z wlasnymi quizami - patrz Screen.QuizStudio. */
    val navQuizzes: String
    val navShop: String

    /* ---- settings screen ---- */
    val settingsTitle: String
    val settingsLanguage: String
    val settingsLanguageHint: String
    val settingsNotifications: String
    val settingsNotificationsIntro: String
    val settingsNotificationsAsk: String
    val settingsNotificationsBlocked: String
    val settingsNotificationsAll: String
    val settingsNotificationsAllHint: String
    val settingsReminderStorage: String
    val settingsReminderStorageHint: String
    val settingsReminderSummon: String
    val settingsReminderSummonHint: String
    val settingsReminderDaily: String
    val settingsReminderDailyHint: String
    val settingsReminderHour: String
    val settingsBattle: String
    val settingsBattleSpeed: String
    val settingsBattleSpeedHint: String
    val settingsAccount: String
    val settingsSignedInAs: String
    val settingsSignedInUnknown: String
    val settingsSignOut: String
    val settingsSignOutHint: String
    val settingsSignOutConfirm: String
    val settingsDeleteAccount: String
    val settingsAdPrivacy: String
    val settingsDeleteAccountConfirm: String
    val settingsDeleteAccountHint: String
    val settingsDeleteNeedsLogin: String
    val settingsDeleteFailed: String
    val settingsAbout: String
    val settingsDangerZone: String
    val settingsVersion: String
    val settingsGeneral: String
    val settingsReset: String
    val settingsResetHint: String
    val settingsResetDone: String
    val settingsOn: String
    val settingsOff: String

    /* ---- endless setup / custom quiz (feature/home/story + feature/home/quiz) ---- */
    val endlessSourceBuiltin: String
    val endlessSourceCustom: String
    val endlessLevelLabel: String
    val endlessLevelRising: String
    val endlessLevelRisingHint: String
    fun endlessLevelFixedHint(level: String): String
    val endlessCategoryLabel: String
    val endlessCategoryRandom: String
    val endlessStart: String
    val endlessBossTest: String

    val deckSourceWordsTitle: String
    fun deckSourceWordsReady(count: Int): String
    fun deckSourceWordsNotReady(have: Int, need: Int): String
    val deckMyDecksLabel: String
    val deckLoading: String
    val deckEmptyHint: String
    val deckNewDeck: String
    val deckNewDeckWithAi: String
    fun deckNotReadyHint(minQuestions: Int): String
    fun deckWordsNotReadyHint(need: Int): String
    fun deckReadyCount(ready: Int): String
    fun deckBrokenCount(broken: Int): String

    /* ---- auth screen (feature/auth) ---- */
    val authTagline: String
    val authContinueWithGoogle: String
    val authContinueWithApple: String
    val authEmail: String
    val authPassword: String
    val authSignIn: String
    val authCreateAccount: String
    val authHaveAccount: String
    val authNoAccount: String
    val authOr: String
    val authErrorEmptyFields: String
    val authErrorShortPassword: String
    val authErrorNoNetwork: String
    val authErrorPlayServices: String
    fun authErrorGeneric(message: String): String
    val authWelcomeBack: String
    val authWelcomeBackHint: String
    val authWelcomeNew: String
    val authWelcomeNewHint: String
    val authConfirmPassword: String
    val authPasswordRule: String
    val authForgotPassword: String
    fun authResetSent(email: String): String
    val authResetNeedEmail: String
    val authErrorPasswordsDiffer: String
    val authErrorWrongCredentials: String
    val authErrorEmailInUse: String
    val authErrorInvalidEmail: String
    val authErrorWeakPassword: String
    val authErrorTooManyAttempts: String
    val authGoogleWaiting: String

    /* ---- character creation (feature/auth) ---- */
    val creationTitle: String
    val creationSubtitle: String
    val creationNameLabel: String
    val creationNameHint: String
    val creationPresetMale: String
    val creationPresetFemale: String
    val creationSkin: String
    val creationHair: String
    val creationGiftTitle: String
    val creationGiftHint: String
    val creationStart: String
    val creationErrorNoName: String
    val creationRandomName: String
    val creationTapHero: String
    val creationNamePlaceholder: String
}

/** English - the default and the language the app ships with. */
object EnStrings : AppStrings {

    override val languageCode = "en"

    override val commonStart = "Start"
    override val commonCancel = "Cancel"
    override val commonSave = "Save"
    override val commonDelete = "Delete"
    override val commonBack = "Back"
    override val commonClose = "Close"
    override val commonConfirm = "Confirm"
    override val commonLoading = "Loading..."

    override val navHome = "Home"
    override val navGrowth = "Growth"
    override val navFight = "Fight"
    override val navProgress = "Progress"
    override val navQuizzes = "Quizzes"
    override val navShop = "Shop"

    override val settingsTitle = "Settings"
    override val settingsLanguage = "Language"
    override val settingsLanguageHint = "Changes the whole app immediately - no restart needed."

    override val settingsNotifications = "Notifications"
    override val settingsNotificationsIntro =
        "We only send three, and only when something is actually waiting for you."
    override val settingsNotificationsAsk = "Turn on reminders"
    override val settingsNotificationsBlocked =
        "Notifications are switched off for WisdomBound in your system settings. Tap to reopen them."
    override val settingsNotificationsAll = "All reminders"
    override val settingsNotificationsAllHint = "Off clears everything already queued up."
    override val settingsReminderStorage = "Camp storage full"
    override val settingsReminderStorageHint = "Campaign rewards stopped piling up."
    override val settingsReminderSummon = "Free summon ready"
    override val settingsReminderSummonHint = "Your free pull is waiting in the Summon Center."
    override val settingsReminderDaily = "Daily goal"
    override val settingsReminderDailyHint = "An evening nudge, only if today is still unticked."
    override val settingsReminderHour = "Evening nudge at"

    override val settingsBattle = "Battle"
    override val settingsBattleSpeed = "Starting speed"
    override val settingsBattleSpeedHint =
        "Every battle starts at this pace. You can still change it mid-fight in the HUD."

    override val settingsAccount = "Account"
    override val settingsSignedInAs = "Signed in as"
    override val settingsSignedInUnknown = "Signed in"
    override val settingsSignOut = "Sign out"
    override val settingsSignOutHint =
        "Your hero and progress stay on the account - sign back in and everything is where you left it."
    override val settingsSignOutConfirm = "Sign out for real?"
    override val settingsDeleteAccount = "Delete account"
    override val settingsAdPrivacy = "Ad privacy settings"
    override val settingsDeleteAccountConfirm = "Delete forever? Tap again"
    override val settingsDeleteAccountHint =
        "Deletes your hero, progress and purchases' in-game rewards for good. An active subscription " +
            "is not cancelled - cancel it in Google Play / App Store."
    override val settingsDeleteNeedsLogin =
        "For safety, sign out and sign back in, then delete the account within 5 minutes."
    override val settingsDeleteFailed = "Could not delete the account. Check your connection and try again."
    override val settingsAbout = "About"
    override val settingsDangerZone = "Danger zone"
    override val settingsVersion = "Version"
    override val settingsGeneral = "General"
    override val settingsReset = "Restore defaults"
    override val settingsResetHint = "Only the settings on this screen. Your hero and progress stay untouched."
    override val settingsResetDone = "Settings restored to defaults."
    override val settingsOn = "On"
    override val settingsOff = "Off"

    override val endlessSourceBuiltin = "Question bank"
    override val endlessSourceCustom = "Custom"
    override val endlessLevelLabel = "Question level"
    override val endlessLevelRising = "Rising"
    override val endlessLevelRisingHint = "Difficulty rises with every wave - the classic endurance test."
    override fun endlessLevelFixedHint(level: String) = "All questions at \"$level\" - training a single level."
    override val endlessCategoryLabel = "Category"
    override val endlessCategoryRandom = "Random"
    override val endlessStart = "START"
    override val endlessBossTest = "▶ TEST: FIGHT THE BOSS NOW"

    override val deckSourceWordsTitle = "WORDS FROM READING"
    override fun deckSourceWordsReady(count: Int) =
        "$count translated words - each round asks about one of them."
    override fun deckSourceWordsNotReady(have: Int, need: Int) =
        "Not enough words yet: $have of $need. Tap a word in the reader, translate it, tap the tab."
    override val deckMyDecksLabel = "My decks"
    override val deckLoading = "Loading decks..."
    override val deckEmptyHint = "You don't have a deck yet. Make your own - write questions " +
        "yourself or ask the model for a ready set and tweak it afterward."
    override val deckNewDeck = "+ NEW DECK"
    override val deckNewDeckWithAi = "✨ WITH AI"
    override fun deckNotReadyHint(minQuestions: Int) =
        "This deck needs $minQuestions finished questions - open it and finish them."
    override fun deckWordsNotReadyHint(need: Int) =
        "Save $need more word(s) so the quiz has enough for four answers."
    override fun deckReadyCount(ready: Int) = "$ready ready questions"
    override fun deckBrokenCount(broken: Int) = "$broken unfinished"

    override val authTagline = "Learn. Fight. Level up."
    override val authContinueWithGoogle = "Continue with Google"
    override val authContinueWithApple = "Continue with Apple"
    override val authEmail = "Email"
    override val authPassword = "Password"
    override val authSignIn = "Sign in"
    override val authCreateAccount = "Create account"
    override val authHaveAccount = "Already have an account? Sign in"
    override val authNoAccount = "No account yet? Create one"
    override val authOr = "or"
    override val authErrorEmptyFields = "Fill in the email and the password."
    override val authErrorShortPassword = "The password needs at least 6 characters."
    override val authErrorNoNetwork = "Internet connection not available"
    override val authErrorPlayServices = "Google Play Services not available"
    override fun authErrorGeneric(message: String) = "Sign in failed: $message"
    override val authWelcomeBack = "Welcome back"
    override val authWelcomeBackHint = "Sign in and pick up where you left off."
    override val authWelcomeNew = "Begin your journey"
    override val authWelcomeNewHint = "Create an account - your hero and progress are saved in the cloud."
    override val authConfirmPassword = "Repeat password"
    override val authPasswordRule = "At least 6 characters."
    override val authForgotPassword = "Forgot password?"
    override fun authResetSent(email: String) = "Password reset link sent to $email."
    override val authResetNeedEmail = "Type your email first, then tap Forgot password."
    override val authErrorPasswordsDiffer = "The passwords are not the same."
    override val authErrorWrongCredentials = "Wrong email or password."
    override val authErrorEmailInUse = "This email already has an account - sign in instead."
    override val authErrorInvalidEmail = "That doesn't look like an email address."
    override val authErrorWeakPassword = "That password is too weak - use at least 6 characters."
    override val authErrorTooManyAttempts = "Too many attempts. Wait a moment and try again."
    override val authGoogleWaiting = "Please wait..."

    override val creationTitle = "Your hero"
    override val creationSubtitle = "The name and the gift stay with your hero for good."
    override val creationNameLabel = "Name"
    override val creationNameHint = "Hero name"
    override val creationPresetMale = "Warrior"
    override val creationPresetFemale = "Warrioress"
    override val creationSkin = "Skin"
    override val creationHair = "Hair"
    override val creationGiftTitle = "Starting gift"
    override val creationGiftHint = "You start the game wearing it."
    override val creationStart = "START THE ADVENTURE"
    override val creationErrorNoName = "Your hero needs a name."
    override val creationRandomName = "Random"
    override val creationTapHero = "Tap the hero to swing"
    override val creationNamePlaceholder = "Nameless"
}

/** Polish - the original dev language, kept as a full alternative. */
object PlStrings : AppStrings {

    override val languageCode = "pl"

    override val commonStart = "Start"
    override val commonCancel = "Anuluj"
    override val commonSave = "Zapisz"
    override val commonDelete = "Usun"
    override val commonBack = "Wstecz"
    override val commonClose = "Zamknij"
    override val commonConfirm = "Potwierdz"
    override val commonLoading = "Wczytuje..."

    override val navHome = "Dom"
    override val navGrowth = "Rozwoj"
    override val navFight = "Walcz"
    override val navProgress = "Postep"
    override val navQuizzes = "Quizy"
    override val navShop = "Sklep"

    override val settingsTitle = "Ustawienia"
    override val settingsLanguage = "Jezyk"
    override val settingsLanguageHint = "Zmienia cala apke od razu - bez restartu."

    override val settingsNotifications = "Powiadomienia"
    override val settingsNotificationsIntro =
        "Wysylamy dokladnie trzy i tylko wtedy, gdy cos naprawde na Ciebie czeka."
    override val settingsNotificationsAsk = "Wlacz przypomnienia"
    override val settingsNotificationsBlocked =
        "Powiadomienia dla WisdomBound sa wylaczone w ustawieniach systemu. Tapnij, zeby je otworzyc."
    override val settingsNotificationsAll = "Wszystkie przypomnienia"
    override val settingsNotificationsAllHint = "Wylaczenie kasuje tez to, co juz stoi w kolejce."
    override val settingsReminderStorage = "Pelny magazyn w obozie"
    override val settingsReminderStorageHint = "Nagrody z kampanii przestaly sie zbierac."
    override val settingsReminderSummon = "Darmowy summon gotowy"
    override val settingsReminderSummonHint = "Darmowe losowanie czeka w Summon Center."
    override val settingsReminderDaily = "Cel dzienny"
    override val settingsReminderDailyHint = "Wieczorne szturchniecie - tylko gdy dzis jeszcze nic nie odhaczone."
    override val settingsReminderHour = "Wieczorem o"

    override val settingsBattle = "Walka"
    override val settingsBattleSpeed = "Tempo startowe"
    override val settingsBattleSpeedHint =
        "Od tego tempa startuje kazda walka. W trakcie dalej zmienisz je w HUD-zie."

    override val settingsAccount = "Konto"
    override val settingsSignedInAs = "Zalogowany jako"
    override val settingsSignedInUnknown = "Zalogowany"
    override val settingsSignOut = "Wyloguj"
    override val settingsSignOutHint =
        "Bohater i postep zostaja na koncie - po ponownym zalogowaniu wszystko jest na swoim miejscu."
    override val settingsSignOutConfirm = "Na pewno wylogowac?"
    override val settingsDeleteAccount = "Usun konto"
    override val settingsAdPrivacy = "Prywatnosc reklam"
    override val settingsDeleteAccountConfirm = "Usunac na zawsze? Tapnij ponownie"
    override val settingsDeleteAccountHint =
        "Trwale usuwa bohatera, postep i nagrody z zakupow w grze. Aktywna subskrypcja nie zostanie " +
            "anulowana - anuluj ja w Google Play / App Store."
    override val settingsDeleteNeedsLogin =
        "Dla bezpieczenstwa wyloguj sie i zaloguj ponownie, a potem usun konto w ciagu 5 minut."
    override val settingsDeleteFailed = "Nie udalo sie usunac konta. Sprawdz polaczenie i sprobuj ponownie."
    override val settingsAbout = "O aplikacji"
    override val settingsDangerZone = "Strefa zagrozenia"
    override val settingsVersion = "Wersja"
    override val settingsGeneral = "Ogolne"
    override val settingsReset = "Przywroc domyslne"
    override val settingsResetHint = "Tylko ustawienia z tego ekranu. Bohater i postep zostaja nietkniete."
    override val settingsResetDone = "Przywrocono ustawienia domyslne."
    override val settingsOn = "Wl."
    override val settingsOff = "Wyl."

    override val endlessSourceBuiltin = "Bank pytan"
    override val endlessSourceCustom = "Wlasne"
    override val endlessLevelLabel = "Poziom pytan"
    override val endlessLevelRising = "Rosnacy"
    override val endlessLevelRisingHint = "Trudnosc rosnie z kazda fala - klasyczny sprawdzian wytrzymalosci."
    override fun endlessLevelFixedHint(level: String) = "Wszystkie pytania na poziomie \"$level\" - trening jednego progu."
    override val endlessCategoryLabel = "Kategoria"
    override val endlessCategoryRandom = "Losowa"
    override val endlessStart = "ROZPOCZNIJ"
    override val endlessBossTest = "▶ TEST: WALKA Z BOSSEM OD RAZU"

    override val deckSourceWordsTitle = "SLOWKA Z CZYTELNI"
    override fun deckSourceWordsReady(count: Int) =
        "$count przetlumaczonych slowek - kazda runda pyta o jedno z nich."
    override fun deckSourceWordsNotReady(have: Int, need: Int) =
        "Za malo fiszek: $have z $need. Dotknij slowa w czytniku, przetlumacz, kliknij zakladke."
    override val deckMyDecksLabel = "Moje talie"
    override val deckLoading = "Wczytuje talie..."
    override val deckEmptyHint = "Nie masz jeszcze zadnej talii. Zrob wlasna - napisz pytania sam " +
        "albo popros model o gotowy zestaw i popraw go po swojemu."
    override val deckNewDeck = "+ NOWA TALIA"
    override val deckNewDeckWithAi = "✨ Z POMOCA AI"
    override fun deckNotReadyHint(minQuestions: Int) =
        "Ta talia potrzebuje $minQuestions gotowych pytan - otworz ja i dokoncz."
    override fun deckWordsNotReadyHint(need: Int) =
        "Zapisz jeszcze $need slowko/a, zeby quiz mial z czego zbudowac cztery odpowiedzi."
    override fun deckReadyCount(ready: Int) = "$ready gotowych pytan"
    override fun deckBrokenCount(broken: Int) = "$broken do dokonczenia"

    override val authTagline = "Ucz sie. Walcz. Rosnij w sile."
    override val authContinueWithGoogle = "Dalej z Google"
    override val authContinueWithApple = "Dalej z Apple"
    override val authEmail = "E-mail"
    override val authPassword = "Haslo"
    override val authSignIn = "Zaloguj sie"
    override val authCreateAccount = "Zaloz konto"
    override val authHaveAccount = "Masz juz konto? Zaloguj sie"
    override val authNoAccount = "Nie masz konta? Zaloz je"
    override val authOr = "albo"
    override val authErrorEmptyFields = "Wpisz e-mail i haslo."
    override val authErrorShortPassword = "Haslo musi miec co najmniej 6 znakow."
    override val authErrorNoNetwork = "Brak polaczenia z internetem"
    override val authErrorPlayServices = "Brak Google Play Services"
    override fun authErrorGeneric(message: String) = "Logowanie nie powiodlo sie: $message"
    override val authWelcomeBack = "Witaj ponownie"
    override val authWelcomeBackHint = "Zaloguj sie i graj dalej od miejsca, w ktorym skonczyles."
    override val authWelcomeNew = "Zacznij przygode"
    override val authWelcomeNewHint = "Zaloz konto - bohater i postep zapisza sie w chmurze."
    override val authConfirmPassword = "Powtorz haslo"
    override val authPasswordRule = "Co najmniej 6 znakow."
    override val authForgotPassword = "Nie pamietasz hasla?"
    override fun authResetSent(email: String) = "Link do zmiany hasla wyslany na $email."
    override val authResetNeedEmail = "Najpierw wpisz e-mail, potem kliknij Nie pamietasz hasla."
    override val authErrorPasswordsDiffer = "Hasla nie sa takie same."
    override val authErrorWrongCredentials = "Zly e-mail albo haslo."
    override val authErrorEmailInUse = "Ten e-mail ma juz konto - zaloguj sie."
    override val authErrorInvalidEmail = "To nie wyglada na adres e-mail."
    override val authErrorWeakPassword = "Za slabe haslo - uzyj co najmniej 6 znakow."
    override val authErrorTooManyAttempts = "Za duzo prob. Odczekaj chwile i sprobuj ponownie."
    override val authGoogleWaiting = "Chwileczke..."

    override val creationTitle = "Twoj bohater"
    override val creationSubtitle = "Imie i dar zostaja z bohaterem na stale."
    override val creationNameLabel = "Imie"
    override val creationNameHint = "Imie bohatera"
    override val creationPresetMale = "Wojownik"
    override val creationPresetFemale = "Wojowniczka"
    override val creationSkin = "Skora"
    override val creationHair = "Wlosy"
    override val creationGiftTitle = "Dar na start"
    override val creationGiftHint = "Zaczynasz gre juz z nim na sobie."
    override val creationStart = "RUSZAJ NA PRZYGODE"
    override val creationErrorNoName = "Bohater potrzebuje imienia."
    override val creationRandomName = "Losuj"
    override val creationTapHero = "Tapnij bohatera, zeby machnal mieczem"
    override val creationNamePlaceholder = "Bezimienny"
}

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.English -> EnStrings
    AppLanguage.Polish -> PlStrings
}
