# Jak pisac pytania do banku

Przewodnik dla kazdego, kto pisze partie nowych pytan - czlowieka albo agenta, ktory dostal
zlecenie `brief_NN.md` z `tools/quizbank.py plan`. Narzedzie i uklad plikow opisuje `README.md`.

Autor partii pisze tylko swoj plik z pytaniami (poza repo). Nie zmienia plikow repo i nie uruchamia
`add`, `move` ani `generate` - to robi osoba skladajaca ture, po przejrzeniu wszystkich partii.

## Format partii

Pierwsza linia to naglowek, potem jedno pytanie na linie, kolumny rozdzielone ` | `:

```
topic | difficulty | prompt | correct | wrong1 | wrong2 | wrong3
roman_empire | M | Which emperor made his horse Incitatus a priest, according to legend? | Caligula | Nero | Commodus | Domitian
```

- `topic` - dzial z kolumny "dzial" zlecenia (pelna sciezka albo sama jednoznaczna nazwa liscia).
- `difficulty` - E (Easy), M (Medium), H (Hard), X (Expert).
- Po angielsku, czyste ASCII: bez diakrytykow (Sao Paulo, Reykjavik, Zurich), bez "krzywych"
  cudzyslowow i dlugich myslnikow. Tytuly i cytowane slowa w pojedynczych apostrofach: 'Hamlet'.
  Znaku `|` nie uzywa sie w zadnym polu.

## Co robi dobre pytanie

1. **Zaskakuje albo bawi.** Po dobrym pytaniu gracz chce komus opowiedziec, czego sie dowiedzial.
   Zadnych definicji z podrecznika ("what is a peninsula?") i fiszek.
2. **Poziomy:**
   - Easy - fakt znany wiekszosci doroslych, ale do przypomnienia (nie "ile nog ma pajak").
   - Medium - kojarzy go osoba, ktora lubi temat.
   - Hard - fan tematu.
   - Expert - ciekawostka dla zapalencow, ale wciaz z puenta, a nie data z przypisu.
3. **Dystraktory z tej samej polki:** kazda bledna odpowiedz musi byc wiarygodna, najlepiej to
   popularna pomylka (Sydney jako stolica Australii). Ten sam typ, ta sama epoka albo region,
   podobna forma.
4. **Dlugosc nie zdradza odpowiedzi:** poprawna nie moze byc o 15+ znakow dluzsza od kazdej
   blednej (narzedzie to blokuje). Najlepiej cztery odpowiedzi podobnej dlugosci i formy.
5. **Jeden fakt - jedno pytanie, w CALYM banku**, takze miedzy typami (prawda/falsz, zaznacz-wszystkie,
   ukladanki, szacowanie w Kotlinie). Zlecenie wypisuje pytania, ktore juz sa w dziale; poza nim
   szukaj: `python3 tools/quizbank.py find "slowo"`. Przeformulowanie nie robi z dubla nowego pytania.
6. **Nic znanego tylko w Polsce** (polscy krolowie, rzeki, gwiazdy) - takie tresci ida do tematu
   `poland`, nie do historii ani geografii.
7. **Nic, co sie zdezaktualizuje w rok** ("obecny mistrz", "najnowszy telefon", "obecny prezydent").
   Ludnosc i inne dryfujace liczby: "roughly"/"about", a opcje daleko od siebie (co najmniej x1.5-x2).
8. **Dokladnie jedna poprawna odpowiedz**, kazdy dystraktor na pewno bledny. Niepewny fakt - odpada.
   Sporne historie: "according to legend" / "is said to".
9. **Tresc nie zawiera odpowiedzi** i nie zdradza odpowiedzi innego pytania w banku.
10. **Pytania o liczby** ("roughly how deep is...") sa mile widziane, z opcjami wyraznie od siebie
    odleglymi: "About 11 km | About 4 km | About 25 km | About 7 km".
11. **Trzymaj sie swoich dzialow.** Rownolegle pisza inni autorzy; temat sasiedniego dzialu
    (zlecenie je wymienia) zostaw jego autorowi, zeby ten sam fakt nie powstal dwa razy.

Reguly konkretnych tematow (historia: daty, dzialy przekrojowe; geografia: przyroda vs kontynent,
`capitals` osobno, pytania z tabeli panstw) stoja w `README.md`, w "Zasady pisania pytan".

## Sprawdzanie (obowiazkowe)

`python3 tools/quizbank.py check -s <plik>` - powtarzaj do skutku.

- Kazdy `BLAD` trzeba usunac: dubel istniejacego pytania (wtedy wypada fakt, a nie zmieniaja sie
  slowa), zdradzajaca dlugosc, tresc tylko polska, nieznany dzial.
- Kazda `uwaga` wymaga decyzji:
  - "ta sama odpowiedz i podobne pytanie" z podobienstwem >= 0.4 to zwykle ten sam fakt - zmien
    fakt albo usun pytanie;
  - "odpowiedz pada w tresci innego pytania" / "tresc zdradza odpowiedz innego pytania" -
    przeformuluj albo usun, chyba ze to niegrozne (np. wspolna nazwa kraju).
- Koniec, gdy check pokazuje `0 z bledami`, zostale uwagi sa niegrozne, a podsumowanie `-s`
  zgadza sie z liczbami ze zlecenia.

## Raport autora

Na koniec tylko: sciezka pliku, liczba pytan, ile w kazdym dziale i na kazdym poziomie, i ile
uwag zostawiles swiadomie. Bez listy pytan.
