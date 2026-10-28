# Baza pytan quizu

Pytania wyboru (jedna poprawna odpowiedz + trzy bledne) mieszkaja tutaj. Jedno pytanie = jedna linia
TSV, kolumny rozdzielone tabulatorem:

```
id	difficulty	prompt	correct	wrong1	wrong2	wrong3
his_churchill	Easy	Who was Britain's prime minister for most of World War II?	Winston Churchill	Neville Chamberlain	Clement Attlee	Anthony Eden
```

Linie z `#` na poczatku pliku to opis - trafia do komentarza w wygenerowanym Kotlinie.

Temat to **jeden plik** (`biology.tsv`, `science.tsv`, ...) albo, gdy jest duzy, **katalog dzialow**:

```
history/
  prehistory.tsv                     Prehistory
  antiquity/                         Antiquity
    ancient_egypt.tsv                  Ancient Egypt
    roman_republic.tsv                 Roman Republic
    roman_empire.tsv                   Roman Empire
    ...
  middle_ages/                       Middle Ages
    vikings.tsv, crusades.tsv, ...
  ...
  us_presidents.tsv                  US Presidents (dzial przekrojowy)
  medicine.tsv, spies.tsv, ...       kolejne dzialy przekrojowe
geography/
  continents/                        Continents & Regions
    europe.tsv, asia.tsv, ...          panstwa i regiony kontynentow
  physical/                          Physical Geography
    oceans_seas.tsv, rivers_lakes.tsv, mountains.tsv, climate.tsv, ...
  human/                             People & Places
    countries.tsv, cities.tsv, languages_customs.tsv, food_drink.tsv
  comparisons/                       Higher or Lower
    population.tsv, area.tsv, skylines.tsv, nature.tsv, by_numbers.tsv
data/
  countries.tsv                      dane o panstwach (nie pytania) - patrz nizej
```

Sciezka pliku to sciezka dzialu: `history/antiquity/roman_empire`. Pytania leza tylko w lisciach
(plikach), katalog to galaz obejmujaca wszystko pod soba. Nazwy dzialow w interfejsie, ikony i ich
kolejnosc (w historii chronologiczna) stoja w **`sections.tsv`** - kazdy plik i katalog dzialu musi
miec tam wiersz, galaz przed swoimi poddzialami. W grze kazde pytanie niesie sciezke swojego dzialu
w `QuizEntry.section`, a drzewo daje `QuizSections` (patrz `QuizSection.kt`).

**TSV to zrodlo prawdy.** Pliki `domain/src/commonMain/.../quiz/bank/<Temat>Questions.kt`
i `QuizSectionCatalog.kt` sa z nich generowane przez `tools/quizbank.py` i nie edytuje sie ich
recznie - `QuizBankTsvSyncTest` (desktopTest) wyklada sie, gdy Kotlin i TSV sie rozjada. Pliki TSV
mozna otwierac w arkuszu kalkulacyjnym (zapis: tekst rozdzielany tabulatorami, UTF-8).

Poza TSV, w Kotlinie, zostaja: flagi (`FlagQuestions`, losuja bledne odpowiedzi), prawda/falsz,
zaznacz-wszystkie, ukladanki i szacowanie. Skrypt czyta je przy sprawdzaniu dubli.

### Dane o panstwach (`data/countries.tsv`)

Tabela 194 panstw: ludnosc, powierzchnia, wspolrzedne stolicy, dostep do morza, sasiedzi z granica
ladowa i "slawa" panstwa (1 zna kazdy, 3 tylko zapaleni geografowie). To nie sa pytania, tylko
material: `CountryQuestions` losuje z niej przy kazdym losowaniu nowe zestawy, tak jak robia to quizy
geograficzne w sieci (Sporcle, JetPunk, "higher or lower", Worldle):

- wybor z czterech: najwiecej/najmniej ludzi, najwieksze/najmniejsze, najgesciej zaludnione, stolica
  najdalej na polnoc/poludnie/wschod/zachod, ktore graniczy (albo NIE graniczy) z X, ktore jest bez
  dostepu do morza, ile mniej wiecej ludzi mieszka w X, ktora stolica jest najblizej Y;
- ukladanki: od najmniejszej ludnosci/powierzchni, stolice z zachodu na wschod i z poludnia na polnoc;
- prawda/falsz w stylu "wyzej czy nizej" ("Canada has more people than Poland"), granice, morze;
- zaznacz wszystkie: sasiedzi X, panstwa bez morza, panstwa ludniejsze/wieksze od X.

Poziom wynika z tego, jak znane sa panstwa i jak wyrazna jest roznica (Easy trzykrotna, Expert
o 30% i w obrebie jednego kontynentu) - prog chroni przed tym, ze dane sa przyblizone i co roku sie
zmieniaja. Pytania z tabeli maja `QuizEntry.generated = true` i dzial `geography/human/countries`.
Po zmianie tabeli `python3 tools/quizbank.py generate` robi `CountryData.kt` (sprawdza tez spojnosc:
symetrie granic, kody, liczby), a `CountryQuestionsTest` sprawdza kazdy typ pytania z tabela.
Recznie pisane pytania o panstwa nie powinny powtarzac tych prostych porownan - lepiej fakty
z historia (najwiecej sasiadow, jedyna nieprostokatna flaga, co znaczy nazwa).

## Tura nowych pytan (duzo naraz)

Gdy pytan ma przybyc kilkaset, robi sie to turami (skill `.claude/skills/dodaj-pytania`):

```bash
python3 tools/quizbank.py plan history=500 geography=500 -o /tmp/runda   # rozdziel i napisz zlecenia
#   kazde zlecenie /tmp/runda/brief_NN.md -> autor pisze /tmp/runda/batch_NN.txt (wg WRITING_GUIDE.md)
python3 tools/quizbank.py check -s /tmp/runda/batch_*.txt                # razem: duble tez miedzy partiami
python3 tools/quizbank.py add /tmp/runda/batch_*.txt
tools/quiztest.sh                                                        # testy quizu, gdy Gradle nie dziala
```

`plan` dzieli liczbe pytan na liscie (polowa po rowno, polowa dla najmniejszych dzialow, najwyzej
dwie srednie na dzial) i poziomy (wedlug brakow do proporcji E18/M37/H30/X15, `--mix` zmienia).
Zlecenie `brief_NN.md` (domyslnie ~100 pytan, `-b`) ma tabele "dzial x poziom", zakres kazdego
dzialu z komentarza pliku, sasiednie dzialy i liste pytan, ktore juz w dziale sa - autor nie musi
niczego szukac, zeby nie dublowac. Zasady dla autorow: **`WRITING_GUIDE.md`**.

## Poprawki istniejacych pytan (`fix`)

Bank pisalo wielu autorow, wiec co jakis czas warto go przejrzec: zly fakt, druga poprawna
odpowiedz wsrod blednych, przestarzaly rekord, za latwe Expert albo za trudne Easy.

```bash
python3 tools/quizbank.py list biology -w -i > /tmp/biology.txt   # pytania z id i blednymi odpowiedziami
#   recenzent pisze /tmp/poprawki.txt wg REVIEW_GUIDE.md
python3 tools/quizbank.py fix -n /tmp/poprawki.txt                 # sprawdz (jak `check`), nic nie zapisuje
python3 tools/quizbank.py fix /tmp/poprawki.txt                    # zapisz + wygeneruj Kotlin
```

Plik poprawek ma naglowek `id | difficulty | prompt | correct | wrong1 | wrong2 | wrong3` i jedna
linie na pytanie: cale nowe brzmienie pytania o tym id (zostaje w tym samym miejscu pliku, a przy
zmianie poziomu przechodzi do bloku nowego poziomu) albo samo `id | DELETE`. Poprawione pytania
przechodza te same sprawdzenia co nowe, wobec banku bez nich samych. Zasady przegladu:
**`REVIEW_GUIDE.md`**.

## Dopisywanie pytan - cala sesja

```bash
python3 tools/quizbank.py stats                  # ile pytan w temacie, w kazdym dziale i na kazdym poziomie
python3 tools/quizbank.py list history/antiquity -d E   # podglad tematu albo dzialu; -g fraza, -w bledne
python3 tools/quizbank.py find "titanic"         # czy fakt juz jest - w CALYM banku, wszystkie typy
python3 tools/quizbank.py check -s nowe.txt      # sprawdz nowe pytania, niczego nie zapisuje (-s: podsumowanie, -q: krotko)
python3 tools/quizbank.py add nowe.txt           # check + dopisz do TSV + wygeneruj Kotlin
python3 tools/quizbank.py add nowe.txt --skip-errors  # dopisz poprawne, pomin te z bledami
python3 tools/quizbank.py add nowe.txt --only-clean   # dopisz tylko pytania bez bledow i bez uwag
python3 tools/quizbank.py move ID... --to vikings  # przenies pytania do innego dzialu (albo tematu)
./gradlew :domain:desktopTest --tests 'com.lernhero.domain.quiz.*'   # albo tools/quiztest.sh bez Gradle
```

Plik z nowymi pytaniami (dowolna nazwa, najlepiej poza repo): naglowek i jedna linia na pytanie,
kolumny rozdzielone `|` albo tabulatorem. Kolumna `id` jest opcjonalna - bez niej skrypt nada unikalne
id sam. W kolumnie `topic` stoi temat (`biology animals elements capitals science geography sport
math literature technology mythology pop_culture poland`) albo - w temacie z dzialami - dzial:
pelna sciezka (`history/antiquity/roman_empire`) albo sama nazwa liscia, gdy jest jednoznaczna
(`roman_empire`). Gole `history` jest bledem: kazde pytanie historii musi trafic do dzialu.
Poziomy: `Easy Medium Hard Expert` (albo `E M H X`).

```
topic | difficulty | prompt | correct | wrong1 | wrong2 | wrong3
world_war_2 | Easy | Who led the Soviet Union during World War II? | Joseph Stalin | Vladimir Lenin | Nikita Khrushchev | Leon Trotsky
vikings | Medium | Which English city was the Viking capital known as 'Jorvik'? | York | Lincoln | Norwich | Chester
```

`add` wstawia kazde pytanie na koniec bloku swojego poziomu w pliku jego dzialu (albo tematu), wiec
pliki zostaja ulozone szczeblami Easy -> Expert.

## Co sprawdza `check`

Bledy (blokuja `add`) - te same warunki, ktore potem sprawdzaja QuizBankTest i QuizBankAuditTest
(progi i listy skrypt czyta wprost z pliku testu):

- identyczna tresc albo id juz w banku, powtorzona odpowiedz wsrod czterech,
- poprawna odpowiedz dluzsza o 15+ znakow od kazdej blednej (zdradza sie dlugoscia),
- tresc tylko polska (Malysz, Wawel, ...) poza tematem `poland`,
- ten sam fakt: ta sama odpowiedz i pokrycie slow kluczowych >= 0.55,
- nieznany temat albo dzial, temat z dzialami bez podanego dzialu.

Uwagi (do przejrzenia okiem, nie blokuja): ta sama odpowiedz przy podobnym pytaniu, bardzo podobna
tresc przy innej odpowiedzi, odpowiedz zdradzona w tresci innego pytania tego tematu (albo odwrotnie),
podobienstwo do pytan prawda/falsz, zaznacz-wszystkie, ukladanek i szacowania, poprawna odpowiedz
wyraznie najdluzsza, polskie znaki i cudzyslowy (bank pisze `Krakow`, `Pele` i 'tytuly' w apostrofach).

## Zasady pisania pytan

Przewodnik dla autorow: `WRITING_GUIDE.md`, pelna lista w KDoc `QuizBank`. W skrocie: pytanie ma zaskakiwac albo bawic (zadnych definicji
z podrecznika i fiszek), Easy to fakt znany wiekszosci doroslych, ale do przypomnienia (nie zerowka),
dystraktory z tej samej polki i podobnej dlugosci, jeden fakt = jedno pytanie (takze miedzy typami),
tresci znane tylko w Polsce ida do `poland`, nic, co sie zdezaktualizuje za rok. Czas na odpowiedz
liczy `QuizPace` (domain/quiz): dodatek za tekst dluzszy niz ~120 znakow i +4 s dla pytan "do
przemyslenia" - tych z dzialow `QuizPace.THINKING_SECTIONS` (dzis `geography/comparisons`) i z `math`.

### Historia

- Historia powszechna, celowo bez watkow polskich (te siedza w `poland`).
- Daty tylko te, ktore kazdy powinien kojarzyc (1492, 1789, 1914, 1989). Zamiast traktatow i sejmow
  znanych wylacznie z podrecznika - historie z puenta: kto, dlaczego i co z tego wyszlo.
- Dzial wybiera sie po epoce i temacie pytania. Dzialy przekrojowe (`us_presidents`,
  `famous_women`, `pacific`, `medicine`, `spies`, `famous_words`, `royal_nicknames`) dostaja
  pytania, w ktorych sedno to sama postac, cytat, przydomek albo watek; pytanie o wojne, ktora
  prezydent prowadzil, zostaje w dziale tej wojny. Zakres kazdego dzialu opisuje komentarz na gorze jego pliku.
- `QuizSectionTest` pilnuje, zeby kazdy lisc mial co najmniej 25 pytan, w tym 10 Easy/Medium.

### Geografia

- Cztery galezie: `continents` (panstwa i regiony danego kontynentu, polarne), `physical` (oceany,
  rzeki i jeziora, gory, pustynie i lasy, wyspy i wybrzeza, klimat i sily natury, Ziemia i mapy),
  `human` (panstwa i granice, miasta i zabytki, jezyki/waluty/zwyczaje, kuchnia) i `comparisons`
  ("higher or lower": kto ma wiecej ludzi, co jest wieksze, ktore miasto ma wiecej wiezowcow,
  ktory kraj pije wiecej kawy). W `comparisons` sedno pytania to samo porownanie albo proporcja
  ("A vs B", "ile razy wiecej"); poziom rosnie, gdy roznica maleje (Easy ~3x, Expert ~1.1-1.3x),
  a dla liczb, ktore dryfuja (ludnosc, rankingi), roznica zostaje co najmniej 15%.
- Obiekt przyrody idzie do dzialu fizycznego, nawet gdy pytanie wymienia kontynent ("najdluzsza rzeka
  Afryki" to `rivers_lakes`); dzial kontynentu dostaje to, co dotyczy samego kraju albo regionu
  (dawne nazwy, przydomki, regiony, rekordy panstw). Pytanie "jaka rzeka plynie przez miasto" to
  `rivers_lakes`, zabytek - `cities`. Stolice panstw maja osobny temat `capitals`.
- Liczby ("roughly how deep is the Challenger Deep?") z odpowiedziami odleglymi co najmniej
  o polowe, zeby zaokraglenie nie robilo z dystraktora poprawnej odpowiedzi.

## Nowy temat albo nowy dzial

Nowy temat:
1. Wpis w `QuizCategory` (domain) i nowy plik TSV z naglowkiem kolumn.
2. Wpis w `TOPICS` w `tools/quizbank.py` i w `categoryOf` w `QuizBankTsvSyncTest`.
3. `python3 tools/quizbank.py generate`, a nowy obiekt dopisac do `QuizBank.textEntries`.

Nowy dzial (albo podzial tematu na dzialy): plik `<temat>/<galaz>/<dzial>.tsv` z naglowkiem kolumn
i wiersze w `sections.tsv` dla kazdej nowej galezi i liscia. Pytania z dotychczasowego miejsca
przenosi `move`, a `generate` sam sprawdza, czy pliki i `sections.tsv` sie zgadzaja.
