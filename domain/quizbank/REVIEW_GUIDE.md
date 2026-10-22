# Jak przegladac istniejace pytania

Dla recenzenta, ktory dostaje temat albo dzial do przejrzenia. Wynik to plik poprawek dla
`python3 tools/quizbank.py fix` (opis w README.md, "Poprawki istniejacych pytan"). Pytania
pisze sie dalej wedlug `WRITING_GUIDE.md` - te same zasady obowiazuja poprawione brzmienie.

## Skad wziac pytania

```bash
python3 tools/quizbank.py list <temat-albo-dzial> -w -i     # id | poziom | tresc -> poprawna (bledne)
python3 tools/quizbank.py find <slowo>                      # czy fakt nie wystepuje gdzie indziej
```

## Na co patrzec (w kolejnosci waznosci)

1. **Fakt nieprawdziwy albo watpliwy** - zla data, liczba, autor, "pierwszy/najwiekszy", ktore sie
   nie zgadza. Pewny blad: popraw. Fakt sporny, ktorego nie da sie ujac bezpiecznie: `DELETE`.
2. **Bledna odpowiedz, ktora tez jest poprawna** (albo bywa tak nazywana) - zamien ja na na pewno
   bledna z tej samej kategorii.
3. **Przestarzale** - rekordy, "obecny", "najnowszy", liczby, ktore sie zmienily. Przepisz na fakt
   z data ("in 2023") albo na niezmienny, albo `DELETE`.
4. **Niejasne pytanie** - da sie je zrozumiec na dwa sposoby, albo poprawna zalezy od interpretacji.
5. **Zly poziom** - Easy, ktorego przecietny dorosly nie wie, albo Expert, ktore zna kazdy. Zmien
   tylko poziom (cala linia z nowym poziomem).
6. **Zdradzanie odpowiedzi** - poprawna wyraznie dluzsza albo jedyna "pasujaca" gramatycznie,
   bledne absurdalne. Wyrownaj bledne odpowiedzi.
7. **Dubel** - ten sam fakt drugi raz w banku (takze w innym temacie albo jako prawda/falsz).
   Zostaw lepsze, gorsze `DELETE`.

Nie poprawiaj stylu dla samego stylu: pytanie poprawne i jasne zostaje, nawet jesli napisalbys je
inaczej. Kazda poprawka ma powod z listy powyzej.

## Format pliku poprawek

```
id | difficulty | prompt | correct | wrong1 | wrong2 | wrong3
bio_krew_serce | M | How many chambers does the human heart have? | Four | Two | Three | Six
his_stara_data | DELETE
```

- Linia poprawki to CALE nowe brzmienie (wszystkie kolumny), nawet gdy zmienia sie jedno slowo.
- Mozna dodac linie komentarza `# powod` nad poprawka - `fix` je pomija, a przegladajacy widzi,
  dlaczego.
- Sprawdz: `python3 tools/quizbank.py fix -n <plik>` az do braku bledow. Nie uruchamiaj `fix` bez
  `-n` - zapisuje ten, kto zlecil przeglad.

## Raport recenzenta

Sciezka pliku, ile poprawek i usuniec, a przy kazdej poprawce faktu jedno zdanie: co bylo zle.
