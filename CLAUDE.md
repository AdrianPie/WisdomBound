# WisdomBound - wskazowki dla Claude

## Dopisywanie pytan quizu (najczestsze zadanie)

Baza: `domain/quizbank/<temat>.tsv` (zrodlo prawdy), narzedzie: `tools/quizbank.py`,
zasady pisania pytan: `domain/quizbank/README.md` (przeczytaj raz, jest krotki).

Oszczedzaj kontekst - bank ma ~870 KB:

- NIE czytaj calych plikow `*.tsv` ani generowanych `domain/src/commonMain/.../quiz/bank/*Questions.kt`.
- NIE rob `git diff` / `git show` tych plikow - wystarczy `git diff --stat`.
- Rozeznanie: `python3 tools/quizbank.py stats`, a czy fakt juz jest: `find "<fraza>"`.
  Podglad tematu tylko z filtrem i `| head`, np. `list mythology -d X | head -40`.
- Nowe pytania pisz do pliku w scratchpadzie (format `topic | difficulty | prompt | correct | wrong1 | wrong2 | wrong3`),
  paczki po ~100 pytan.
- Najtanszy obieg: `python3 tools/quizbank.py add paczka.txt --only-clean` - dopisuje tylko pytania
  bez bledow i bez uwag (uwagi to prawie zawsze duble ujete innymi slowami), reszte pomija bez wypisywania.
  Alternatywy: `check paczka.txt -q` (jedna linia na problem) i `add paczka.txt --skip-errors`.
- Bank ma juz oczywiste fakty: zwykle przechodzi 30-50% kandydatow. Pisz mniej oczywiste, ale
  ciekawe fakty i licz sie z tym, ze na 100 dopisanych trzeba napisac ~250.
- Na koniec: `python3 tools/quizbank.py generate --check` i
  `./gradlew :domain:desktopTest --tests 'com.lernhero.domain.quiz.*'` (w sesji w chmurze Gradle moze nie
  pobrac wtyczek Androida - wtedy wystarczy `generate --check`, bo `add` sprawdza te same warunki co testy),
  commit po kazdej sesji.

Nie musisz pamietac, co dodaly poprzednie sesje - `check` porownuje z calym bankiem i wylapuje duble.
