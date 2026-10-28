---
name: dodaj-pytania
description: Tura dopisywania pytan do banku quizu (domain/quizbank) - "dodaj N pytan z historii/geografii/...". Rozdziela pytania na dzialy, zleca partie autorom, sprawdza, dopisuje, testuje i commituje.
---

# Tura nowych pytan do banku quizu

Uzyj, gdy uzytkownik prosi o kolejne pytania ("dodaj 500 pytan z historii i 300 z geografii").
Narzedzie: `tools/quizbank.py` (opis w `domain/quizbank/README.md`), zasady pisania:
`domain/quizbank/WRITING_GUIDE.md`.

## 0. Tryb samodzielny (prosba bez liczb i tematow)

"Rozbuduj bank", "dodaj ciekawe pytania", "zrob najlepsza baze" - sam ukladasz ture. Cel: bank
szeroki i ciekawy dla gracza w walce na czas (patrz "Pytania pod te gre" w `CLAUDE.md`).

1. **Rozeznanie** (`stats`): dzialy ponizej ~80 pytan, poziomy daleko od E18/M37/H30/X15, tematy
   bez dzialow, ktore urosly ponad ~600, i proporcje typow (prawda/falsz, ukladanki,
   zaznacz-wszystkie, szacowanie - wszystkie w Kotlinie, duzo mniej niz pytan wyboru).
2. **Czego brakuje** - pomysl jak tworca quizu, nie jak podrecznik. Sprawdzone formy, ktore gracze
   lubia: "higher or lower" (porownania), rekordy i "naj-", "co bylo pierwsze", "prawda czy mit",
   "skad pochodzi" (jedzenie, slowa, wynalazki), liczby szacowane, "kto to powiedzial". Tematy:
   to, o czym ludzie rozmawiaja (jedzenie, zwierzeta, kosmos, cialo, film, muzyka, sport, podroze),
   a nie to, co bylo na sprawdzianie. Nowy obszar = nowy dzial (README: "Nowy temat albo nowy dzial").
3. **Plan tury** - 5-10 linijek w odpowiedzi do uzytkownika, potem od razu dzialasz:
   ~60% uzupelnianie dziur, ~40% jeden-dwa nowe dzialy, w sumie 500-1000 pytan. Kazdy dzial z
   jednym zdaniem "dlaczego to bedzie ciekawe". Pytaj tylko, gdy plan zmienia gre (nowy temat
   w `QuizCategory`, nowy typ pytania, czas albo balans).
4. Pytania "do przemyslenia" (porownania, proporcje, rachunki) tylko w dzialach z
   `QuizPace.THINKING_SECTIONS` albo nowym dziale, ktory tam dopiszesz (+4 s w grze).
5. Dalej zwykla tura (punkty 1-5 nizej). W raporcie: co dodales, dlaczego, i 2-3 propozycje
   na nastepna ture.

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

Dla kazdego zlecenia jeden agent w tle, najwyzej 5 naraz (wiecej wyczerpuje limit sesji i partie
przepadaja w polowie; kolejne zlecenia uruchamiaj, gdy wczesniejsze sie koncza). Gdy liscie jest
jeden, a pytan 200, podziel go na dwa podzakresy (kazdy autor swoj), zamiast dawac jednemu 200.
Polecenie:

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

Przejrzana partia idzie do banku od razu (`add`, `generate --check`, commit, push) - nie czekaj na
wszystkie; nastepne partie i tak sprawdzaja sie wobec banku z poprzednimi.

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

## Przeglad i poprawki istniejacych pytan

Gdy uzytkownik prosi o poprawki ("poprawiaj"), albo co kilka tur: recenzenci w tle, kazdy z jednym
tematem lub dzialem (do ~1000 pytan), z poleceniem:

> Read domain/quizbank/REVIEW_GUIDE.md and domain/quizbank/WRITING_GUIDE.md. Review every question of
> `<temat>` (`python3 tools/quizbank.py list <temat> -w -i`). Write fixes to `<rN>/fix_<temat>.txt`,
> run `python3 tools/quizbank.py fix -n <file>` until 0 errors. Do not run fix without -n and do not
> modify repo files. Reply with path, counts and one line per factual fix.

Potem samemu: przeczytaj kazda poprawke (recenzent tez sie myli - odrzuc zmiany bez powodu z
REVIEW_GUIDE), `fix -n` na wszystkich plikach razem, `fix`, testy, commit "Poprawki: ...".
