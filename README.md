# WisdomBound – Idle Quiz RPG

**Learning that feels like a game.** WisdomBound is an idle RPG for Android and iOS. Quiz answers drive the fights: answer correctly and quickly, and your hero hits harder. Between sessions your hero keeps gathering resources while you're away.

Built with **Kotlin Multiplatform + Compose Multiplatform**: one shared codebase for Android and iOS. It also includes a small **Ktor** server for AI-generated quiz decks, and **RevenueCat** for subscriptions and in-app purchases.

---

## What's in the game

- **Quiz battles:** each battle is simulated ahead of time by a deterministic engine, then replayed on screen. Your quiz answers during the fight add damage multipliers based on speed and streaks.
- **Campaign map:** stages generated from a seed, with enemy species and roles, bosses and a difficulty tier.
- **Built-in question bank:** thousands of hand-curated questions in 15 categories, including science, history, geography, flags, literature, mythology, pop culture and more.
- **Your own decks and AI decks:** write your own question decks, or type a topic and get a generated deck from the AI server.
- **Hero progression:** equipment, a forge, talents, spells, companions with a star/duplicate system, summons, achievements and idle rewards.
- **Reader:** a book reader with tap-to-translate. It uses on-device translation (Apple Translation on iOS).
- **Accounts:** Sign in with Apple, Google or email. Progress syncs through Firestore, and users can delete their account from inside the app.
- **Languages:** English and Polish.

## Monetization (RevenueCat)

All purchases go through **RevenueCat**, on Google Play and the App Store:

| Product | Type | What it gives |
|---|---|---|
| **Premium** (monthly / yearly) | auto-renewable subscription, entitlement `premium` | 50 gems/day, 200 AI quiz decks/month, unlimited reader translations, ad rewards without watching ads |
| `legend_pack` | non-consumable | 4 legendary items, 2,500 gems, 30 summon tickets |
| `gems_100` … `gems_15000` | consumables | gem packs; the first purchase of each pack gives double gems |

How it's built:
- The client uses the RevenueCat SDK: native on Android, and through Swift Package Manager on iOS, bridged into Kotlin in `iosApp/iosApp/MonetizationBridge.swift`. Offerings, prices, purchases and restore all come from RevenueCat.
- **The paywall is a RevenueCat Paywall** (RevenueCatUI), designed in the RevenueCat dashboard, so its look, copy and A/B tests change without a new build. On Android it is `PaywallDialog`; on iOS a `PaywallViewController` presented from Swift. Both sit behind one Kotlin `expect/actual` call (`shared/.../monetization/RevenueCatPaywall.kt`).
- **Placements:** every place that opens the paywall passes its own placement (`quiz_creation`, `ai_decks`, `translations`, `shop`). Each one can get its own offering and paywall in the dashboard, and falls back to the current offering if none is set.
- **What's gated:** free players can write 1 custom quiz deck, and Premium unlocks unlimited decks. AI-generated decks are Premium-only. Free players get 5 reader translations per day.
- If RevenueCat can't show a paywall (no store on the platform, or offerings failed to load), a built-in fallback paywall with the same products appears instead of a dead end.
- The **server does not trust the client** for premium status. Before it spends money on an AI call, it checks the `premium` entitlement directly with the RevenueCat REST API, using a secret key that only the server holds (`server/.../Entitlements.kt`).
- Optional rewarded ads (AdMob) are the free alternative to paying.

Relevant code: `shared/src/*/monetization/`, `domain/.../monetization/`, `iosApp/iosApp/MonetizationBridge.swift`.

## Architecture

```
:domain      game rules – pure Kotlin, no UI (combat engine, items, spells, quiz bank, economy)
:data        repositories (Firebase Auth / Firestore via GitLive SDK, AI quiz client)
:shared      design system, resources, i18n, monetization and platform bridges
:feature:*   screens (auth, home, game, reader) – each with its ViewModel
:navigation  app navigation graph
:di          Koin wiring
:composeApp  Android / iOS entry point
:server      Ktor server: AI quiz generation (Gemini or Claude), Firebase ID-token auth,
             RevenueCat entitlement check, monthly quota in Firestore
iosApp/      Xcode project (SwiftUI host + Swift bridges for RevenueCat, AdMob, Translation)
```

For more detail, see [ARCHITECTURE.md](ARCHITECTURE.md) (in Polish).

## Running it

### Requirements
- JDK 17+, Android Studio (recent), Android SDK 36
- For iOS: macOS with Xcode 16+ (iOS 18.2+ deployment target)

### Android
```bash
./gradlew :composeApp:installDebug
```
You can also open the project in Android Studio and run `composeApp`. Release signing is optional: the build uses `keystore.properties` only if that file exists, and it is gitignored.

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode, select your team under *Signing & Capabilities*, and run. Swift packages (RevenueCat, Google Mobile Ads, Google Sign-In) are resolved automatically.

### Firebase
The repo includes the client configs (`composeApp/google-services.json`, `iosApp/iosApp/GoogleService-Info.plist`). These are public identifiers, not secrets, so the app runs against the live project out of the box. Access is enforced by [firestore.rules](firestore.rules).

To use your own Firebase project:
1. Create a project and enable **Authentication**: Email/Password, Google, Apple.
2. Create a **Firestore** database and deploy the rules: `firebase deploy --only firestore:rules`
3. Replace the two config files above with your project's files.

### RevenueCat
Public SDK keys are in `shared/src/commonMain/kotlin/com/lernhero/shared/monetization/MonetizationConfig.kt`; they are safe to ship. To use your own project, create products with the IDs above, add an entitlement `premium` and a current offering with monthly and yearly packages, then swap in your keys. If a key is empty, the store is simply turned off and the game still works.

### AI quiz server (optional)
The app points to the hosted server in `data/.../quiz/ServerConfig.kt`. To run your own:
```bash
cp server/.env.example server/.env   # fill in keys, then export them
gcloud auth application-default login
./gradlew :server:runJvm             # or: docker build -t wisdombound-server .
```
The server needs one AI key (`GEMINI_API_KEY` or `ANTHROPIC_API_KEY`) and your `FIREBASE_PROJECT_ID`. The RevenueCat **secret** key is only needed for premium checks. After that, set `AI_QUIZ_SERVER_BASE_URL` to your server's URL.

### Tests
```bash
./gradlew :domain:test
```

## Secrets policy
No secrets are committed. The AI provider keys, the RevenueCat secret key, Firebase admin credentials and the Android signing keystore all live only on the server or the developer's machine (see `.gitignore` and `server/.env.example`).

## License
Code is released under the [MIT License](LICENSE). Fonts: Pixelify Sans (SIL OFL) and Roboto (Apache 2.0).
