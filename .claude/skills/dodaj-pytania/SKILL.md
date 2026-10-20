---
name: dodaj-pytania
description: Tura dopisywania pytan do banku quizu (domain/quizbank) - "dodaj N pytan z historii/geografii/...". Rozdziela pytania na dzialy, zleca partie autorom, sprawdza, dopisuje, testuje i commituje.
---

# Tura nowych pytan do banku quizu

Uzyj, gdy uzytkownik prosi o kolejne pytania ("dodaj 500 pytan z historii i 300 z geografii").
Narzedzie: `tools/quizbank.py` (opis w `domain/quizbank/README.md`), zasady pisania:
`domain/quizbank/WRITING_GUIDE.md`.

## 1. Plan

```bash
python3 tools/quizbank.py stats                                   # gdzie jest malo pytan
python3 tools/quizbank.py plan history=500 geography=500 -o <scratchpad>/rN
```

`plan` rozdziela liczby na liscie (polowa po rowno, polowa dla najmniejszych dzialow, najwyzej dwie
srednie na dzial) i na poziomy (tam, gdzie dzialowi brakuje do E18/M37/H30/X15, zmiana: `--mix`).
Mozna podac galaz albo dzial (`geography/physical=200 vikings=20`). Pisze zlecenia
`brief_NN.md` (~100 pytan, `-b`): liczby, zakres kazdego dzialu, sasiednie dzialy i liste pytan,
ktore juz w nim sa. Bez liczb od uzytkownika: podziel prosbe po rowno miedzy wymienione tematy.

Jesli liscie urosly ponad ~150 pytan albo prosba dotyczy nowego obszaru, najpierw rozwaz nowy dzial
(README: "Nowy temat albo nowy dzial") i dopiero potem `plan`.

## 2. Autorzy

Dla kazdego zlecenia jeden agent w tle (rownolegle), z poleceniem:

> Read domain/quizbank/WRITING_GUIDE.md and the "Zasady pisania pytan" section of
> domain/quizbank/README.md, then your brief `<rN>/brief_NN.md`. Write the batch file named there,
> run `python3 tools/quizbank.py check -s <file>` until 0 errors and only harmless warnings, counts
> matching the brief. Do not modify repo files or run add/move/generate. Reply with path, counts
> and number of warnings left on purpose.

## 3. Przeglad (zawsze samemu, przed `add`)

```bash
python3 tools/quizbank.py check -s <rN>/batch_*.txt     # razem - lapie duble MIEDZY partiami
```

- Usun albo popraw wszystko z `BLAD`; uwagi o tej samej odpowiedzi miedzy partiami to zwykle ten
  sam fakt napisany przez dwoch autorow - zostaw jedno.
- Przeczytaj kazde pytanie: fakt prawdziwy? jedna poprawna? dystraktory na pewno bledne? liczby
  odlegle od siebie? Easy naprawde znane? Watpliwe - popraw albo wyrzuc (skrypt prawdy nie sprawdzi).

## 4. Dopisanie i testy

```bash
python3 tools/quizbank.py add <rN>/batch_*.txt
python3 tools/quizbank.py generate --check
./gradlew :domain:desktopTest --tests 'com.lernhero.domain.quiz.*'   # albo, gdy Gradle nie dziala:
tools/quiztest.sh
```

`QuizBankTest` ma prog wielkosci banku (`all.size >= ...`) - po duzej turze mozna go podniesc.

## 5. Commit

Osobne commity na temat albo na grupe partii, wiadomosc po polsku bez polskich znakow, np.
"Historia: 500 nowych pytan (sredniowiecze, XIX wiek, ...)". Potem push na galaz robocza.
W raporcie dla uzytkownika: ile pytan doszlo w ktorym temacie/dziale, co wyrzucono w przegladzie
i wynik testow.
