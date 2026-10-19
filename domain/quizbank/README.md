# Baza pytan quizu

Pytania wyboru (jedna poprawna odpowiedz + trzy bledne) mieszkaja tutaj. Jedno pytanie = jedna linia
TSV, kolumny rozdzielone tabulatorem:

```
id	difficulty	prompt	correct	wrong1	wrong2	wrong3
his_churchill	Easy	Who was Britain's prime minister for most of World War II?	Winston Churchill	Neville Chamberlain	Clement Attlee	Anthony Eden
```

Linie z `#` na poczatku pliku to opis - trafia do komentarza w wygenerowanym Kotlinie.

Temat to **jeden plik** (`biology.tsv`, `geography.tsv`, ...) albo, gdy jest duzy, **katalog dzialow**:

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

## Dopisywanie pytan - cala sesja

```bash
python3 tools/quizbank.py stats                  # ile pytan w temacie, w kazdym dziale i na kazdym poziomie
python3 tools/quizbank.py list history/antiquity -d E   # podglad tematu albo dzialu; -g fraza, -w bledne
python3 tools/quizbank.py find "titanic"         # czy fakt juz jest - w CALYM banku, wszystkie typy
python3 tools/quizbank.py check nowe.txt         # sprawdz nowe pytania, niczego nie zapisuje
python3 tools/quizbank.py add nowe.txt           # check + dopisz do TSV + wygeneruj Kotlin
python3 tools/quizbank.py move ID... --to vikings  # przenies pytania do innego dzialu (albo tematu)
./gradlew :domain:desktopTest --tests 'com.lernhero.domain.quiz.*'
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

Pelna lista w KDoc `QuizBank`. W skrocie: pytanie ma zaskakiwac albo bawic (zadnych definicji
z podrecznika i fiszek), Easy to fakt znany wiekszosci doroslych, ale do przypomnienia (nie zerowka),
dystraktory z tej samej polki i podobnej dlugosci, jeden fakt = jedno pytanie (takze miedzy typami),
tresci znane tylko w Polsce ida do `poland`, nic, co sie zdezaktualizuje za rok.

### Historia

- Historia powszechna, celowo bez watkow polskich (te siedza w `poland`).
- Daty tylko te, ktore kazdy powinien kojarzyc (1492, 1789, 1914, 1989). Zamiast traktatow i sejmow
  znanych wylacznie z podrecznika - historie z puenta: kto, dlaczego i co z tego wyszlo.
- Dzial wybiera sie po epoce i temacie pytania. Dzialy przekrojowe (`us_presidents`,
  `famous_women`) dostaja pytania, w ktorych sedno to sama postac; pytanie o wojne, ktora prezydent
  prowadzil, zostaje w dziale tej wojny. Zakres kazdego dzialu opisuje komentarz na gorze jego pliku.
- `QuizSectionTest` pilnuje, zeby kazdy lisc mial co najmniej 25 pytan, w tym 10 Easy/Medium.

## Nowy temat albo nowy dzial

Nowy temat:
1. Wpis w `QuizCategory` (domain) i nowy plik TSV z naglowkiem kolumn.
2. Wpis w `TOPICS` w `tools/quizbank.py` i w `categoryOf` w `QuizBankTsvSyncTest`.
3. `python3 tools/quizbank.py generate`, a nowy obiekt dopisac do `QuizBank.textEntries`.

Nowy dzial (albo podzial tematu na dzialy): plik `<temat>/<galaz>/<dzial>.tsv` z naglowkiem kolumn
i wiersze w `sections.tsv` dla kazdej nowej galezi i liscia. Pytania z dotychczasowego miejsca
przenosi `move`, a `generate` sam sprawdza, czy pliki i `sections.tsv` sie zgadzaja.
