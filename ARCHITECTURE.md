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
                 stanu - postep liczy sie w locie z GameStats, wiec nie da sie go rozjechac
monster/         karty kompanow i cala ekonomia kolekcji
  MonsterCard        dane karty + spellIds (zaklecia kompana)
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
story/           kampania: StoryWorld (kraina) + StoryStage (jedna plansza, liczona na zadanie
                 z globalnego numeru + tieru, nie trzymana w pamieci). Zero chodzenia - gracz
                 klika plansze, tak jak w Idle Heroes. Boss na ostatniej planszy kazdej krainy
                 i co BOSS_INTERVAL (5) po drodze
  CampaignSelection  gdzie gracz "stoi" (kraina + tier + wybrana plansza). Koin singleton, bo
                     ten wybor musi przezyc przejscie zakladka <-> ekran Mapy
  IdleRewards        nagrody kapiace offline (przycisk GET). Bezstanowa matematyka z roznicy
                     czasu, tak samo jak town/ProductionMath; sufit CAP_HOURS
  StageLoot          jawna pula lupu planszy (przycisk LOOT) - rzadsze przedmioty wchodza do
                     puli dopiero glebiej i na wyzszych tierach
player/          Player, PlayerEquipment, klasy postaci, PlayerProgress (poziom/XP/statystyki
                 rozdawane po awansie - LevelCurve to cala krzywa progresji)
common/          Rarity - dzielona przez przedmioty i potwory
repository/      interfejsy repozytoriow (implementacje sa w :data)
fitness/         modele trackera fitness (dzienny snapshot, cele, katalog celi)
reading/         modele czytnika (Book, BookContent, ReaderSettings, ReadingGoal, ReadingStats,
                 TextStats, PlainTextParser). BookContent jest niezalezny od formatu - parser
                 TXT/EPUB produkuje ten sam model, a strony NIE istnieja w domenie: powstaja
                 dopiero w UI, gdy znany jest ekran i typografia (patrz ReadingAnchor)
run/             stan pojedynczej walki/zejscia
  RunSession       zywy stan biezacego runu (RunModifiers, HP druzyny, lup)
  RunDifficulty    4 tiery kampanii (Normal/Nightmare/Hell/Death) - zakladki nad mapa, kazdy
                   nastepny odblokowuje sie dopiero po ukonczeniu poprzedniego OD DESKI DO DESKI
                   (patrz story/StoryProgress.isTierUnlocked). Endless zawsze stoi na Normal
  RunLoot          ile jedno zejscie daje XP i materialow. XP plynie glownie z DOBRYCH
                   ODPOWIEDZI, materialy (ruda/esencja) z walki
  BossChest        kolo fortuny ze skrzyni po bossie (jawne szanse)
  QuizBoost, RunPerk                         zbudowane, ale NIC jeszcze ich nie wola (zero
                                              miejsc uzycia w calym repo) - to swiadomy zapas
                                              pod przyszla integracje (boost z quizu przed
                                              walka, perk z obozowiska), nie zapomniany kod.
                                              Kazdy plik ma o tym notatke na gorze.
```

**Dodajesz miecz?** `item/catalog/Weapons.kt`. **Nowa mechanika?** `combat/effects/`.
**Nowe pytanie?** `quiz/bank/`. Silnika nie dotykasz w zadnym z tych przypadkow - to jest caly
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
                EndlessSetupPanel  konfiguracja endless: poziom pytan + kategoria + zakladka
                                   "Wlasne" (placeholder pod wlasne talie / generowanie AI)
                StageLootDialog    podglad puli lupu planszy (przycisk LOOT)
                BossChestOverlay   kolo fortuny po pokonaniu bossa
                WorldMapScreen     JEDYNY osobny ekran - wybor krainy i tieru trudnosci (MAPA)
              Zastapilo dawny DungeonCrawlScreen (mapa-graf wezlow + spacer korytarzem) - razem
              z nim zniknely zdarzenia korytarzowe i wedrowny handlarz (byly przywiazane do
              "kroku w korytarzu", ktorego juz nie ma)
progress/     zakladka Postep: poziom bohatera i rozdawanie punktow, statystyki nauki
              (celnosc per kategoria), osiagniecia
shop/         sklep za PRAWDZIWA walute (klejnoty). Sprzetu sie tu NIE kupuje - przedmioty
              wykuwa sie w Kuzni, kompanow i zaklecia przyzywa w Summon Center, a materialy
              na jedno i drugie wypadaja wylacznie z walk kampanii/endless
city/         budynki miasta: tartak, kamieniolom, Kuznia (crafting), Summon Center
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

`:domain` jest testowany, bo tam mieszka logika. 65 testow pilnuje generatora mapy, silnika walki
(efekty naprawde dzialaja w symulacji), migracji ekwipunku, systemu umiejetnosci, banku pytan,
zdarzen w lochu, kola fortuny, krzywej awansu, statystyk konta i kosztow wytwarzania.

Warstwa UI nie ma testow - i to jest swiadome. Skoro walka rozstrzyga sie w domenie, to test
domeny sprawdza mechanike, a ekran zostaje tylko odtwarzaczem.
