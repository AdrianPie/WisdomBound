# Baza pytan quizu

Pytania wyboru (jedna poprawna odpowiedz + trzy bledne) mieszkaja tutaj, **jeden plik TSV na temat**
(`history.tsv`, `geography.tsv`, ...). Jedno pytanie = jedna linia, kolumny rozdzielone tabulatorem:

```
id	difficulty	prompt	correct	wrong1	wrong2	wrong3
his_churchill	Easy	Who was Britain's prime minister for most of World War II?	Winston Churchill	Neville Chamberlain	Clement Attlee	Anthony Eden
```

Linie z `#` na poczatku pliku to opis tematu - trafia do KDoc wygenerowanego obiektu.

**TSV to zrodlo prawdy.** Pliki `domain/src/commonMain/.../quiz/bank/<Temat>Questions.kt` sa z nich
generowane przez `tools/quizbank.py` i nie edytuje sie ich recznie - `QuizBankTsvSyncTest`
(desktopTest) wyklada sie, gdy Kotlin i TSV sie rozjada. Pliki TSV mozna otwierac w arkuszu
kalkulacyjnym (zapis: tekst rozdzielany tabulatorami, UTF-8).

Poza TSV, w Kotlinie, zostaja: flagi (`FlagQuestions`, losuja bledne odpowiedzi), prawda/falsz,
zaznacz-wszystkie, ukladanki i szacowanie. Skrypt czyta je przy sprawdzaniu dubli.

## Dopisywanie pytan - cala sesja

```bash
python3 tools/quizbank.py stats                 # ile pytan w ktorym temacie i na ktorym poziomie
python3 tools/quizbank.py list history -d E     # podglad tematu; -g fraza, -w pokazuje bledne odpowiedzi
python3 tools/quizbank.py find "titanic"        # czy fakt juz jest - w CALYM banku, wszystkie typy
python3 tools/quizbank.py check nowe.txt [-q]   # sprawdz nowe pytania, niczego nie zapisuje (-q: krotko)
python3 tools/quizbank.py add nowe.txt          # check + dopisz do TSV + wygeneruj Kotlin
python3 tools/quizbank.py add nowe.txt --skip-errors  # dopisz poprawne, pomin te z bledami
python3 tools/quizbank.py add nowe.txt --only-clean   # dopisz tylko pytania bez bledow i bez uwag
./gradlew :domain:desktopTest --tests 'com.lernhero.domain.quiz.*'
```

Plik z nowymi pytaniami (dowolna nazwa, najlepiej poza repo): naglowek i jedna linia na pytanie,
kolumny rozdzielone `|` albo tabulatorem. Kolumna `id` jest opcjonalna - bez niej skrypt nada unikalne
id sam. Tematy: `biology animals history elements capitals science geography sport math literature
technology mythology pop_culture poland`. Poziomy: `Easy Medium Hard Expert` (albo `E M H X`).

```
topic | difficulty | prompt | correct | wrong1 | wrong2 | wrong3
history | Easy | Who led the Soviet Union during World War II? | Joseph Stalin | Vladimir Lenin | Nikita Khrushchev | Leon Trotsky
```

`add` wstawia kazde pytanie na koniec bloku swojego poziomu w TSV tematu, wiec pliki zostaja
ulozone szczeblami Easy -> Expert.

## Co sprawdza `check`

Bledy (blokuja `add`) - te same warunki, ktore potem sprawdzaja QuizBankTest i QuizBankAuditTest
(progi i listy skrypt czyta wprost z pliku testu):

- identyczna tresc albo id juz w banku, powtorzona odpowiedz wsrod czterech,
- poprawna odpowiedz dluzsza o 15+ znakow od kazdej blednej (zdradza sie dlugoscia),
- tresc tylko polska (Malysz, Wawel, ...) poza tematem `poland`,
- ten sam fakt: ta sama odpowiedz i pokrycie slow kluczowych >= 0.55.

Uwagi (do przejrzenia okiem, nie blokuja): ta sama odpowiedz przy podobnym pytaniu, bardzo podobna
tresc przy innej odpowiedzi, odpowiedz zdradzona w tresci innego pytania tego tematu (albo odwrotnie),
podobienstwo do pytan prawda/falsz, zaznacz-wszystkie, ukladanek i szacowania, poprawna odpowiedz
wyraznie najdluzsza, polskie znaki i cudzyslowy (bank pisze `Krakow`, `Pele` i 'tytuly' w apostrofach).

## Zasady pisania pytan

Pelna lista w KDoc `QuizBank`. W skrocie: pytanie ma zaskakiwac albo bawic (zadnych definicji
z podrecznika i fiszek), Easy to fakt znany wiekszosci doroslych, ale do przypomnienia (nie zerowka),
dystraktory z tej samej polki i podobnej dlugosci, jeden fakt = jedno pytanie (takze miedzy typami),
tresci znane tylko w Polsce ida do `poland`, nic, co sie zdezaktualizuje za rok.

## Nowy temat

1. Wpis w `QuizCategory` (domain) i nowy plik TSV z naglowkiem kolumn.
2. Wpis w `TOPICS` w `tools/quizbank.py` i w `categoryOf` w `QuizBankTsvSyncTest`.
3. `python3 tools/quizbank.py generate`, a nowy obiekt dopisac do `QuizBank.textEntries`.
