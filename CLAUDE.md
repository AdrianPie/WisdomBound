# WisdomBound - wskazowki dla Claude

## Git: jedna linia rozwoju

Kazda sesja zaczyna od aktualnego `origin/main` (`git fetch origin main` i praca na galezi
odbitej od niego). Przed koncem sesji: commit i push, a uzytkownik scala galaz do `main`, zanim
otworzy nastepna sesje - inaczej kazda sesja dopisuje pytania do innej wersji banku i potem trzeba
je recznie scalac i odsiewac duble.

## Dopisywanie pytan quizu (najczestsze zadanie)

Baza: `domain/quizbank/` (TSV to zrodlo prawdy; historia i geografia sa podzielone na dzialy),
narzedzie: `tools/quizbank.py`, zasady: `domain/quizbank/README.md` i `WRITING_GUIDE.md`,
duze tury: skill `.claude/skills/dodaj-pytania`.

Oszczedzaj kontekst - bank jest duzy:

- NIE czytaj calych plikow `*.tsv` ani generowanych `domain/src/commonMain/.../quiz/bank/*Questions.kt`.
- NIE rob `git diff` / `git show` tych plikow - wystarczy `git diff --stat`.
- Rozeznanie: `python3 tools/quizbank.py stats`, a czy fakt juz jest: `find "<fraza>"`.
  Podglad tematu albo dzialu tylko z filtrem i `| head`, np. `list mythology -d X | head -40`.
- Nowe pytania pisz do pliku w scratchpadzie (format `topic | difficulty | prompt | correct | wrong1 | wrong2 | wrong3`;
  w historii i geografii `topic` to dzial, np. `world_war_2`), paczki po ~100 pytan.
- Obieg: `check -q paczka.txt` (jedna linia na problem), potem `add paczka.txt --only-clean` (dopisuje
  tylko pytania bez bledow i bez uwag - uwagi to prawie zawsze duble ujete innymi slowami)
  albo `add paczka.txt --skip-errors`.
- `check` porownuje slowa kluczowe tresci, wiec przepuszcza fakt zadany zupelnie innymi slowami.
  Przy oczywistych faktach sprawdz `find "<odpowiedz>"`, zanim go dopiszesz.
- Bank ma juz oczywiste fakty: pisz mniej oczywiste, ale ciekawe. Zadnych fiszek ("symbol Au to?")
  i zerowek ("ktore zwierze robi 'muu'?").
- Na koniec: `python3 tools/quizbank.py generate --check` i testy: `./gradlew :domain:desktopTest --tests
  'com.lernhero.domain.quiz.*'`, a gdy Gradle nie pobierze wtyczek Androida (sesja w chmurze) - `tools/quiztest.sh`.
  Gdy `quiztest.sh` dostaje 429 z Maven Central, brakujace jary sciagnij z mirrora
  `https://maven-central.storage-download.googleapis.com/maven2/...` do `~/.cache/lernhero-quiztest/`.

## Pytania pod te gre

To nie jest quiz z kartka, tylko walka na czas: pytanie pojawia sie w trakcie bitwy, szybka dobra
odpowiedz bije mocniej, okno to 18 s (na dalekich falach do ~11 s). Dlatego:

- Pytanie ma sie dac przeczytac w kilka sekund: tresc do ~120 znakow, cztery krotkie odpowiedzi
  tej samej formy. Dluzszy tekst dostaje dodatek czasu (`QuizPace`), ale lepiej go nie potrzebowac.
- Dwa rodzaje pytan: **"wiesz albo nie"** (fakt, nazwa, kto/gdzie) i **"do przemyslenia"**
  (porownanie, proporcja, rachunek). Te drugie dostaja +4 s, ale tylko w dzialach i tematach z
  `QuizPace.THINKING_SECTIONS` / `THINKING_CATEGORIES` (dzis: `geography/comparisons`, `math`).
  Pytanie do przemyslenia piszesz tam, a nowy dzial tego rodzaju dopisujesz do tej listy.
- Gracz ma sie dobrze bawic i czegos dowiedziec: puenta, zaskoczenie, "musze to komus powiedziec".

## Prosba ogolna ("rozbuduj bank", "dodaj ciekawe pytania", bez liczb)

Sam decydujesz, co bank najbardziej zyska - procedura w skillu `dodaj-pytania`, sekcja "Tryb
samodzielny". W skrocie: `stats` -> dziury (male dzialy, brakujace poziomy, malo innych typow) i
brakujace obszary (nowe dzialy) -> plan w kilku linijkach w odpowiedzi -> tura. Pytaj tylko, gdy plan
zmienia gre (nowy temat w `QuizCategory`, nowy typ pytania, zmiana czasu albo balansu).

## Duze tury - lekcje

- Najwyzej 5 autorow naraz: przy 10 skonczyl sie limit sesji i polowa partii przepadla w polowie.
- Kazda partie przegladasz sam i dopisujesz od razu (`add`, commit, push) - nie czekaj na wszystkie.
- Liczby panstw (ludnosc, powierzchnia) sprawdzaj skryptem w `domain/quizbank/data/countries.tsv`.
