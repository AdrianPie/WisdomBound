# Architektura LernHero

Ten plik odpowiada na jedno pytanie: **gdzie dopisac nowa rzecz, zeby projekt nie zamienil sie
z powrotem w skladowisko.** Jesli nie wiesz, gdzie cos wrzucic - odpowiedz jest tutaj.

## Zasada nadrzedna

Regula gry nie wie nic o ekranie, a ekran nie liczy regul gry.

Walka jest **symulowana z gory** w `:domain` (silnik zwraca strumien zdarzen), a `:feature:game`
jedynie odtwarza gotowy skrypt. Dzieki temu ta sama walka da sie przyspieszyc 16x, powtorzyc
z seeda i - docelowo - zweryfikowac na serwerze. Ta sama sztuczka trzyma mape kampanii i quiz:
z seeda (numer planszy + tier trudnosci) odtwarza sie wszystko, wiec podglad przeciwnikow na
mapie i prawdziwa walka za "WALCZ" zawsze pokazuja to samo.

## Moduly

```
:domain      reguly gry. Czysty Kotlin, zero Compose. Tu mieszka prawda o swiecie.
:data        implementacje repozytoriow (Firestore).
:shared      design system + zasoby + trasy nawigacji. Wspolne dla wszystkich ekranow.
:feature:*   ekrany. Kazdy trzyma swoj ViewModel obok swojego ekranu.
:navigation  graf nawigacji calej aplikacji.
:di          Koin - jedyne miejsce, gdzie cokolwiek jest sklejane.
:composeApp  punkt wejscia.
```

Kierunek zaleznosci jest jednostronny: `feature -> domain -> (nic)`. Gdy `:domain` zaczyna
potrzebowac Compose, to znak, ze cos wyladowalo w zlym module.

`:domain` naprawde nie zalezy od NICZEGO - takze od `:shared`. Stala tam kiedys taka zaleznosc,
ktorej nic nie uzywalo, a ktora blokowala cyklem poprawny kierunek `:shared -> :domain`
(potrzebny, zeby warstwa platformy mogla czytac modele domeny - patrz powiadomienia nizej).

## :domain

```
combat/          silnik walki
  Stats, Combatant, BattleState        stan i liczby
  Effect, EffectCondition              cztery rodzaje efektow + bramki aktywacji
  effects/                             biblioteka gotowych efektow (nowy efekt = nowy plik tutaj)
  engine/                              petla tur i potok obrazen
  playback/                            tlumaczy zdarzenia na kroki dla animacji
  live/                                AnswerTimer + StreakTracker/CycleOutcome - tempo odpowiedzi
                                        w quizie zamienione na mnoznik obrazen w trakcie walki.
                                        Uzywane bezposrednio przez BattleViewModel/QuizDialog,
                                        nie przez BattleEngine (to warstwa "na zywo", nie symulacja)
item/            przedmiot = dane + lista efektow. catalog/ to przepisy (bron, pancerz, dodatki)
spell/           zaklecia BOHATERA. Kazde niesie DWA opisy: `cast` dla silnika symulacyjnego
                 (BattleEngine) i `live` (LiveSpellSpec) dla zywej walki na ekranie. Bez tego
                 drugiego zaklecie dziala w symulacji, a na ekranie nie robi nic - dokladnie tak
                 przez dluzszy czas wygladal caly pasek zdolnosci
craft/           koszty wytwarzania: Kuznia (przedmioty za rude) i Summon Center (kompani za losy,
                 zaklecia za esencje). Receptura liczy sie z rzadkosci przedmiotu, wiec nowy miecz
                 w katalogu OD RAZU ma swoja recepture - nic tu nie trzeba dopisywac
progress/        liczniki konta (GameStats/RunStats) i osiagniecia. Osiagniecie NIE ma wlasnego
                 stanu - postep liczy sie w locie z GameStats, wiec nie da sie go rozjechac.
                 Jedyne, co sie zapisuje, to fakt ODBIORU nagrody (Player.claimedAchievements);
                 sama kwota siedzi na STOPNIU (AchievementTier.reward), wiec nowy cel w katalogu
                 nie wymaga wymyslania mu ceny i nie da sie dodac takiego, ktory placi zero
monster/         karty kompanow i cala ekonomia kolekcji
  MonsterCard        dane karty + spellIds (zaklecia kompana)
  CompanionPower     JEDYNE miejsce liczace, ile kompan ma HP i ataku - czyta je i karta w
                     kolekcji, i pole bitwy. Kompan istnial wczesniej w dwoch skalach naraz
                     (karta 55-100 HP, walka razy trzy) i nikt ich nie pogodzil, przez co sprzet
                     kompanow - pisany w skali WALKI - nie dzialal ani tu, ani tam: modul
                     :feature:game ANI RAZU nie wspominal o MonsterEquipment, a karta dodawala
                     bonus ze zlej skali i zawyzala trzykrotnie
  MonsterLevels      poziomy za ZLOTO. MAX_LEVEL to sufit absolutny; realny limit daja gwiazdki
  MonsterStars       gwiazdki za DUPLIKATY (2/3/5/8 kopii = 19 lacznie do 5★). Kazda gwiazdka
                     zdejmuje sufit poziomu i dodaje +25% statystyk. Na 5★ nadmiarowe kopie
                     rozpuszczaja sie w esencje - to jest sink na duplikaty, bez ktorego kazde
                     losowanie po skompletowaniu kolekcji byloby zmarnowane
  CompanionSpell     zaklecia kompanow: szansa jest CECHA KARTY (nie rozdaje jej gracz, inaczej
                     niz w pasku zdolnosci bohatera), a poziom kupuje sie za esencje
  EnemySpecies       pool gatunkow/archetypow dla wrogow fali - dzielony przez BattleViewModel
                     i podglad przeciwnikow na mapie kampanii
quiz/            bank pytan; bank/ to tresc podzielona na kategorie
  custom/            WLASNE talie gracza - pytania pisane recznie albo wygenerowane przez model.
                     CustomQuestion jest jednym PLASKIM typem z polem `type` (a nie hierarchia
                     czterech klas), bo leci prosto do Firestore i przez jeden formularz w UI.
                     Walidacja (CustomQuestion.issues) jest jedynym miejscem, ktore decyduje,
                     czy pytanie wejdzie do walki - uzywa jej i edytor, i parser odpowiedzi AI
  ai/                generowanie talii przez model. QuizGenerator to SZEW (jak TranslationSource
                     w czytniku): dzis Stub, prawdziwy silnik to jedna klasa w :data pozniej.
                     Cala trudna czesc - QuizGenerationPrompt i QuizGenerationParser - dziala
                     bez sieci i jest przetestowana
story/           kampania: StoryWorld (kraina) + StoryStage (jedna plansza, liczona na zadanie
                 z globalnego numeru + tieru, nie trzymana w pamieci). Zero chodzenia - gracz
                 klika plansze, tak jak w Idle Heroes. Boss na ostatniej planszy kazdej krainy
                 i co BOSS_INTERVAL (5) po drodze
  CampaignSelection  gdzie gracz "stoi" (kraina + tier + wybrana plansza). Koin singleton, bo
                     ten wybor musi przezyc przejscie zakladka <-> ekran Mapy
  IdleRewards        nagrody kapiace offline (przycisk GET). Bezstanowa matematyka z roznicy
                     czasu, tak samo jak produkcja w miescie (town/); sufit CAP_HOURS.
                     **XP tu NIE MA i to jest regula, nie przeoczenie**: cala gra stoi na tym,
                     ze poziom placi sie dobra odpowiedzia (patrz run/RunLoot). Kapaczka dawala
                     1440 XP za dwanascie godzin nieobecnosci - poltora poziomu za nic - a po
                     pierwszej krainie 360 XP przy awansie kosztujacym niecale 240. Zostaje
                     zloto i materialy: powod, zeby zajrzec rano, a nie darmowy awans
  StageRepeats       ile placi PONOWNE przejscie tej samej planszy: 100% / 50% / 30% / 10% i tyle
                     juz zostaje. Wczesniej nic nie odrozniało pierwszego przejscia od setnego,
                     a boss ostatniej planszy to JEDNA fala placaca piec losow - cztery powtorki
                     bily caly tier kampanii (21 losow). Zadna sciana nie byla wtedy sciana, bo
                     zawsze taniej bylo pospamowac znane pole niz cokolwiek zdobyc. Licznik podejsc
                     siedzi w Player.stageClears ("TIER:numer" -> ile razy), osobno od
                     storyProgress, ktory pamieta tylko NAJDALSZA plansze
  StageLoot          jawna pula lupu planszy (przycisk LOOT) - rzadsze przedmioty wchodza do
                     puli dopiero glebiej i na wyzszych tierach. DWIE polowy: itemPool/dropChance
                     pokazuja graczowi, po co tu bije, a roll() faktycznie oddaje przedmiot po
                     wygranej. Przez dluzszy czas istniala tylko pierwsza - gra obiecywala lup
                     i nie dawala go nigdy. Lup jedzie na konto w RunStats.droppedItemIds, razem
                     z reszta wyniku, bo rozliczenie zejscia jest JEDNYM zapisem
player/          Player, PlayerEquipment, klasy postaci, PlayerProgress (poziom/XP/statystyki
                 rozdawane po awansie - LevelCurve to cala krzywa progresji)
  LevelCurve       BASE_XP 70, GROWTH 1.14. Ksztalt jest CELOWY i zmierzony na symulacji
                   kampanii (75% trafien): 5 min -> poziom 3, 30 min -> 11, 1 h -> 15, 2 h -> 21,
                   3 h -> 27. Czyli pierwsza godzina daje kilkanascie awansow, a kazda nastepna
                   coraz mniej. Ruszanie ktorejkolwiek z tych dwoch liczb przesuwa CALA gre -
                   razem z progami PartySlots, ktore sa pod nie dobrane
  PartySlots       miejsca na kompanow otwieraja sie na poziomach 1/3/7/12/20. To sa MINUTY
                   przelozone na numery: ~5 min, ~18 min, ~35 min, ~2 h. Po kazdej zmianie
                   LevelCurve trzeba je przeliczyc, inaczej caly komplet otworzy sie w pierwszej
                   godzinie - zanim gracz bedzie mial czym go zapelnic (losy leca ze skrzyn)
town/            miasto: surowce (TownResources) i produkcja (ProductionMath).
                 Surowcow sa TRZY - kamien (kamieniolom), ruda i esencja (tylko z walk) - plus
                 losy do summonow i zloto. Drewno i tartak zostaly skasowane: drewno mialo jednego
                 odbiorce (wykuwanie, 15-75 sztuk raz na przedmiot), a same skrzynie po bossach
                 wyplacaly go ponad 1600. Zasob, ktorego nie da sie ani zabraknac, ani wydac,
                 nie jest decyzja gracza - jest czwarta pozycja w kazdym oknie kosztu. Jego role
                 przejal kamien (Forge.costFor: 35 x tier, czyli suma obu dawnych pozycji).
  ProductionMath   budynek ma SUFIT (MAX_LEVEL 10) i to jest cala roznica miedzy "kopalnia
                   pomaga" a "kopalnia gra za gracza": bez sufitu poziom 12 robil 1629/h, czyli
                   13 000 kamienia na jedno odebranie magazynu, przy najdrozszym ulepszeniu
                   przedmiotu za 3300. Teraz maks to 270/h (magazyn 2157) - mniej wiecej jedno
                   ulepszenie dziennie. Caly tor kosztuje ok. 13 000 zlota i jest jedynym
                   SKONCZONYM sinkiem na zloto w grze. Pilnuje tego ProductionMathTest
talent/          drzewko rozwoju bohatera. Wiekszosc wezlow to staki [EndlessBuff], czyli mechanik,
                 ktore walka juz umiala liczyc dla endless - dzieki temu drzewko nie potrzebowalo ani
                 jednej nowej galezi w silniku. Uwaga przy strojeniu: SIEDEM buffow karmia po DWA
                 rozne wezly (Vitality, Avatar, Fortress, SecondWind, Thorns, Regeneration,
                 Vampirism), wiec sufit z opisu jednego wezla to polowa tego, co gracz moze zebrac.
                 Pilnuje tego TalentDescriptionTest - opis musi nazywac blizniaka
common/          Rarity - dzielona przez przedmioty i potwory
repository/      interfejsy repozytoriow (implementacje sa w :data)
fitness/         modele trackera fitness (dzienny snapshot, cele, katalog celi)
notification/    CO i KIEDY przypomniec - ReminderPlan liczy plan z zapisanego stanu gracza i
                 nie trzyma wlasnego (ta sama sztuczka co IdleRewards: wszystko z roznicy czasu,
                 wiec przeliczenie przy kazdym wejsciu do gry jest darmowe i nie da sie go
                 rozjechac). JAK - czyli kanaly, uprawnienia i harmonogram systemowy - siedzi w
                 shared/notification. Powody sa DOKLADNIE trzy i kazdy niesie konkret; powiadomienie
                 bez tresci uczy gracza zmiatac je bez czytania, a potem wylaczac wszystkie
reading/         modele czytnika (Book, BookContent, ReaderSettings, ReadingGoal, ReadingStats,
                 TextStats, PlainTextParser). BookContent jest niezalezny od formatu - parser
                 TXT/EPUB produkuje ten sam model, a strony NIE istnieja w domenie: powstaja
                 dopiero w UI, gdy znany jest ekran i typografia (patrz ReadingAnchor)
run/             stan pojedynczej walki/zejscia
  RunSession       zywy stan biezacego runu (RunModifiers, HP druzyny, lup)
  RunDifficulty    4 tiery kampanii (Normal/Nightmare/Hell/Death) - zakladki nad mapa, kazdy
                   nastepny odblokowuje sie dopiero po ukonczeniu poprzedniego OD DESKI DO DESKI
                   (patrz story/StoryProgress.isTierUnlocked). Endless zawsze stoi na Normal.
                   Tier steruje DWIEMA rzeczami: sila wrogow i - przez campaignLadder - poziomem
                   pytan. To drugie dlugo bylo martwe: trudnosc pytania brala sie wylacznie
                   z numeru fali, a plansza ma ich najwyzej trzy, wiec cala kampania, na kazdym
                   tierze, chodzila po poczatku drabinki
  RunLoot          ile jedno zejscie daje XP i materialow. XP plynie glownie z DOBRYCH
                   ODPOWIEDZI, materialy (ruda/esencja) z walki, a CALA stawka planszy kampanii
                   jest mnozona przez depthMultiplier - plansza placi w tym samym tempie, w jakim
                   rosna stojacy na niej wrogowie. Bez tego stawka liczyla sie z numeru FALI (a te
                   sa najwyzej trzy), wiec plansza 36 wyplacala tyle co plansza 1 i poziom bohatera
                   nie mial szans nadazyc za krzywa wrogow
  EndlessMilestones rytm endless: co 10 fal wchodzi JEDEN boss i to on placi skrzynie - losy,
                   kamien, premia XP - tym wieksze, im glebsza fala. Bez tego endless nie mial
                   czym placic: kazda jego fala startowala jako BattleKind.Normal, a bossBonus
                   dla Normal to same zera. Tryb pomyslany jako miejsce do farmienia nie wyplacal
                   ANI JEDNEGO losa - a sciana, w ktora gracz wchodzi po kampanii na Normalnym,
                   stoi wlasnie na gwiazdkach kompanow, czyli na duplikatach, czyli na losach
  BossChest        kolo fortuny ze skrzyni po bossie (jawne szanse). Nalezy sie WYLACZNIE za
                   pierwsze zdobycie planszy - patrz story/StageRepeats
  QuizBoost, RunPerk                         zbudowane, ale NIC jeszcze ich nie wola (zero
                                              miejsc uzycia w calym repo) - to swiadomy zapas
                                              pod przyszla integracje (boost z quizu przed
                                              walka, perk z obozowiska), nie zapomniany kod.
                                              Kazdy plik ma o tym notatke na gorze.
```

**Dodajesz miecz?** `item/catalog/Weapons.kt`. **Nowa mechanika?** `combat/effects/`.
**Nowe pytanie?** `quiz/bank/`. **Podpinasz model AI?** implementacja `QuizGenerator` w `:data`
i jedna linijka w `:di`. Silnika nie dotykasz w zadnym z tych przypadkow - to jest caly
sens tego podzialu.

## :feature:game

```
battle/
  BattleScreen        ekran + sterowanie odtwarzaniem skryptu
  BattleViewModel     stan gry, fazy walki (GamePhase: Battle -> Defeat/Victory)
  BattleUiState       model stanu dla ekranu
  BattleSpeed         jedno pokretlo tempa - kazde opoznienie w walce przechodzi przez nie
  component/          pole bitwy, HUD, dialogi (quiz, porazka)
character/            renderowanie: sprite'y, modularny rycerz, stany animacji
```

Nagrody i rozwoj postaci (dawniej osobny `UpgradeDialog` w fazie po walce) przeniosly sie do
`:feature:home` - pasek umiejetnosci (`home/ui/AbilityBar.kt`) i sklep. `:feature:game` odtwarza
juz tylko sama walke, koniec.

## :feature:home

Kazdy ekran ma swoj folder. Wspolny `PlayerViewModel` siedzi w `player/`, bo dziela go trzy
ekrany (druzyna, sklep, ekwipunek) - nie ma sensu mnozyc go razy trzy.

```
navigation/   graf zakladek + dolny pasek. Paski sa NAKLADKA nad NavHostem, nie slotami
              Scaffolda - inaczej ich zniknicie zabiera padding calej zawartosci i wychodzacy
              ekran przeskakuje w chwili nawigacji. Padding ekranu zakladki (TabChrome) jest
              staly przez cale jego zycie i NIE zalezy od tego, czy paski sa teraz widoczne.
              Przejscia sa sekwencyjne (wygaszenie konczy sie przed rozjasnieniem), bo ekrany
              sa przezroczyste i przy crossfade poprzedni przeswituje przez nowy
team/         zakladka "Walcz" - sklad druzyny + przelacznik KAMPANIA/ENDLESS + wlasciwy panel
              ze story/. To tutaj wybiera sie plansze i stad startuje walka
story/        czesci kampanii uzywane W ZAKLADCE "Walcz" (nie osobny ekran!):
                CampaignPanel      panel pod druzyna - tlo krainy z przeciwnikami, pasek nagrod
                                   idle z GET, przyciski MAPA/BATTLE/LOOT, rzad plansz na dole
                EndlessSetupPanel  konfiguracja endless: poziom pytan + kategoria, a w zakladce
                                   "Wlasne" wybor wlasnego materialu (patrz quiz/ nizej)
                StageLootDialog    podglad puli lupu planszy (przycisk LOOT)
                BossChestOverlay   kolo fortuny po pokonaniu bossa
                WorldMapScreen     JEDYNY osobny ekran - wybor krainy i tieru trudnosci (MAPA)
              Zastapilo dawny DungeonCrawlScreen (mapa-graf wezlow + spacer korytarzem) - razem
              z nim zniknely zdarzenia korytarzowe i wedrowny handlarz (byly przywiazane do
              "kroku w korytarzu", ktorego juz nie ma)
quiz/         wlasne talie pytan do endless: panel wyboru zrodla w zakladce "Walcz"
              (CustomSourcePanel - fiszki z czytelni ALBO talia), edytor talii na pelnym ekranie
              (DeckEditorScreen + QuestionEditorDialog) i CustomQuizViewModel. Talie zyja w
              podkolekcji `player/{uid}/decks`, nie w dokumencie gracza - stad wlasny ViewModel
              zamiast doklejania sie do PlayerViewModel
progress/     zakladka Postep: poziom bohatera i rozdawanie punktow, statystyki nauki
              (celnosc per kategoria), osiagniecia
shop/         sklep za PRAWDZIWA walute (klejnoty). Sprzetu sie tu NIE kupuje - przedmioty
              wykuwa sie w Kuzni, kompanow i zaklecia przyzywa w Summon Center, a materialy
              na jedno i drugie wypadaja wylacznie z walk kampanii/endless
city/         budynki miasta: kamieniolom, Kuznia (crafting), Summon Center - TRZY wyspy
              na mapie miasta, bo tartak zniknal razem z drewnem (patrz domain/town)
equipment/    ekwipunek (paper doll)
fitness/      zakladka fitness (krokomierz/trening przez Health Connect na Androidzie, HealthKit
              na iOS - patrz FitnessTracker.<platforma>.kt)
player/       PlayerViewModel + stan gracza
ui/           rzeczy dzielone miedzy ekranami (RarityPalette, ItemArt, MonsterCardView)
dev/          narzedzia do strojenia grafiki (podglad rycerza). W tej chwili SA dostepne z
              prawdziwego ekranu glownego (link w HomeScreen) - przed prawdziwym wydaniem warto
              schowac je za flaga debug, na razie zostawione bo przydatne do strojenia grafiki
```

## :feature:reader

Czytelnia - wyprowadzona z `:feature:home`, bo urosla o wlasny silnik skladu tekstu, parsery
formatow i (docelowo) tlumaczenia oraz fiszki.

Czytnik ma **dwa tryby**, o ktorych decyduje `ReaderSettings.mode`:

```
reflow/       tryb domyslny - tekst jako prawdziwy Compose Text
                TextPaginator  dzieli bloki na strony pod dany ekran i typografie. Mierzy KAZDY
                               BLOK OSOBNO, nie cala ksiazke naraz (jeden `measure` na powiesc
                               to sekundy i mnostwo pamieci, a repaginacja idzie po kazdej
                               zmianie czcionki). Lamie zawsze na granicy linii, wiec fragment
                               przepuszczony ponownie przez Text lamie sie identycznie
                ReflowReader   HorizontalPager po stronach + trafianie palcem w slowo
                WordPicker     granice slowa i zdania na czystym tekscie. Nie ma osobnego
                               klikalnego elementu na kazde slowo - Text oddaje offset znaku
                               (getOffsetForPosition), reszta to arytmetyka na Stringu
                ReaderPalette  kolory kartki (jasny/sepia/ciemny) - celowo NIE zlota paleta gry
ReaderSession + PdfEngine.<platforma>.kt
              tryb "oryginal" - PDF renderowany na bitmapie, jak dotad (`PdfRenderer` na
              Androidzie, PDFKit na iOS). PDF probuje przejsc do reflow od razu po imporcie:
              `LibraryViewModel.extractPdfContent` wyciaga caly tekst przez `PdfEngine.pageText`
              (PDFBox-Android / `PDFPage.string` na iOS) i przepuszcza przez PlainTextParser -
              oryginal zostaje tylko wtedy, gdy PDF nie ma warstwy tekstowej (skan bez OCR) albo
              platforma nie potrafi wyciagnac tekstu (desktop)
ReadingClock  zegar aktywnego czytania, wspolny dla obu trybow. AFK-guard: przewrocenie strony
              doladowuje budzet bezczynnosci, a bez kartkowania zegar staje - 15 minut apki
              otwartej na jednej stronie NIE zaliczy dziennego questa. Tempo liczy jako WPM,
              nie strony/min (strona zalezy od czcionki, wiec jest nieporownywalna)
```

Postep w ksiazce zapisujemy jako **pozycje w tekscie**, nie numer strony (`ReadingAnchor`) -
przy innym rozmiarze czcionki "strona 42" wskazuje zupelnie inny fragment.

## :shared - powiadomienia

```
notification/
  ReminderScheduler       expect/actual. Kontrakt jest NADPISUJACY: schedule() dostaje CALY plan
                          i zastepuje nim kolejke. Kazde przypomnienie ma staly klucz
                          (ReminderKind.id), wiec dziesiec wejsc do gry daje jedno powiadomienie,
                          a nie dziesiec
  .android                WorkManager - NIE AlarmManager: od Androida 12 dokladne alarmy wymagaja
                          osobnego uprawnienia, a "magazyn sie zapelnil" nie potrzebuje precyzji
                          co do sekundy. W zamian zadanie przezywa restart telefonu
  .ios                    UNTimeIntervalNotificationTrigger - sekundy od teraz, ten sam model
                          czasu co setInitialDelay po drugiej stronie
  .desktop                ReminderScheduler.None - swiadomy brak, nie luka
  nextLocalHourMillis     "najblizsza 20:00 czasu LOKALNEGO". Osobne expect/actual, bo wymaga
                          kalendarza i strefy czasowej - dokladnie tego, czego ReminderPlan
                          celowo nie zna. Domena dostaje gotowa liczbe
  requestNotificationAccess  prosba o zgode. Wolana WYLACZNIE z Ustawien, po tapnieciu w przycisk,
                          nigdy przy starcie gry - zgoda wyproszona w pierwszej sekundzie, bez
                          kontekstu, to najkrotsza droga do odmowy na zawsze
```

## :shared - preferencje urzadzenia

```
prefs/AppPreferences     wszystko, co gracz przestawia w Ustawieniach, a co NIE jest danymi konta:
                         przypomnienia (glowny wylacznik + kazdy z trzech rodzajow + godzina
                         wieczornego szturchniecia) i tempo, od ktorego startuje walka.
                         Zapis przez multiplatform-settings, tak samo jak jezyk (i18n/AppLanguageStore)
```

Konto (kto jest zalogowany, wylogowanie) idzie osobna sciezka: interfejs `AccountSession`
w `:domain/repository`, implementacja `FirebaseAccountSession` w `:data`. Osobno od
`PlayerRepository`, bo to dwie rozne rzeczy - tam POSTEP, tutaj TOZSAMOSC. Wylogowanie czysci
jeszcze obiekty zyjace tak dlugo jak proces (`RunSession`, `CampaignSelection`) i oddaje decyzje
`SetupNavGraph` przez `HomeNavGraph(onSignedOut = ...)` - graf Domu ma wlasny NavController
i sam nie dosiegnie ekranu logowania.

Zasada ekranu Ustawien: **kazde pole w `AppPreferences` ma swojego czytelnika w kodzie gry**.
Przypomnienia czyta `PlayerViewModel.rescheduleReminders` (plan liczy sie w domenie, Ustawienia sa
filtrem na koncu), tempo walki - `BattleViewModel` w `init`. Przelacznik, ktorego nikt nie czyta,
jest gorszy niz jego brak: uczy gracza, ze ustawienia nic nie robia. Nowa preferencja = pole tutaj
+ czytelnik + jedna linijka w `SettingsScreen` (klocki UI stoja obok, w `SettingsUi.kt`).

`isDebugBuild` (shared/BuildInfo.kt) tez jest expect/actual: to on gasi narzedzia deweloperskie
w buildzie sklepowym. Wczesniej robily to reczne stale `SHOW_DEV_TOOLS` w dwoch plikach i jedyne,
co stalo miedzy release'em a widocznym przyciskiem "dev: +zasoby", to pamiec czlowieka.

## Gdzie sa granice grafiki

Domena nie zna obrazkow. Przedmiot niesie `iconKey: String`, potwor `spriteKey`, pytanie o flage
kod kraju - a warstwa UI zamienia te teksty na zasoby w **jednym pliku**:

| co | gdzie podmieniasz grafike |
|---|---|
| przedmioty | `feature/home/ui/ItemArt.kt` |
| krainy kampanii | `feature/home/story/StoryWorldArt.kt` (funkcja `art()`) |
| potwory | `feature/game/character/MonsterVisualPreset.kt` |
| flagi | `shared/resources/FlagResources.kt` |

## Testy

`:domain` jest testowany, bo tam mieszka logika. Testy pilnuja generatora mapy, silnika walki
(efekty naprawde dzialaja w symulacji), migracji ekwipunku, systemu umiejetnosci, banku pytan,
zdarzen w lochu, kola fortuny, krzywej awansu, statystyk konta, kosztow wytwarzania, walidacji
i losowania wlasnych talii, parsera odpowiedzi modelu (ten ostatni to jedyny sposob, zeby
generowanie AI dalo sie sprawdzic, zanim jakikolwiek model zostanie podpiety), losowania lupu
planszy oraz planu przypomnien.

Ten ostatni jest testowany glownie od strony sytuacji, w ktorych plan ma byc PUSTY - bo to one
decyduja, czy gracz zostawi powiadomienia wlaczone.

Warstwa UI nie ma testow - i to jest swiadome. Skoro walka rozstrzyga sie w domenie, to test
domeny sprawdza mechanike, a ekran zostaje tylko odtwarzaczem.
