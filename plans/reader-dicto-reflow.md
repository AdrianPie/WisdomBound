# Czytnik w stylu Dicto — reflow, tap-na-slowo, quiz ze slowek

## Cel

Czytelnia dostaje dwa tryby czytania:

- **Reflow (domyslny)** — tekst renderowany jako prawdziwy Compose `Text`: wlasna czcionka,
  rozmiar, justowanie, paginacja liczona z pomiaru tekstu. Tylko ten tryb ma tap-na-slowo,
  tlumaczenia i uczciwy licznik slow/WPM.
- **Original (opcjonalny)** — PDF/EPUB renderowany „jak jest", bez tlumaczen. Dla kogos, kto
  chce oryginalny layout. Przelacznik w ustawieniach czytnika.

## Dlaczego przepisujemy

Obecny czytnik renderuje strone PDF jako bitmape ([ReaderSession.kt]). To blokuje wszystko:

1. Na bitmapie nie ma slow, wiec tap-na-slowo jest niemozliwy.
2. `PdfEngine.pageText` na Androidzie **zawsze zwraca null** — `PdfRenderer` nie ma API do
   tekstu. Efekt: `totalWords` i `averageWpm` na Androidzie stoja na zerze. Licznik predkosci
   jest juz zepsuty i to jest przyczyna.
3. PDF ma sztywny layout — nie da sie zmienic rozmiaru czcionki ani zrobic wygladu Dicto.

Co zostaje bez zmian (dziala dobrze, nie ruszamy):

- `ReadingStats`, `ReadingDayProgress`, `ReadingGoalCatalog` — daily questy juz sa.
- AFK-guard i flush postepu w `ReaderSession` — logika zostaje, zmienia sie tylko zrodlo
  zdarzen (przewrocenie strony reflow zamiast strony PDF).

## Predkosc czytania — jak liczyc

Standardem jest **WPM (words per minute)**, nie strony/minute — strona zalezy od rozmiaru
czcionki, wiec strony/minute nie sa porownywalne miedzy uzytkownikami ani miedzy ksiazkami.

- `wpm = slowa na przeczytanych stronach / minuty AKTYWNEGO czytania` (te z AFK-guardem).
- Liczymy tylko ruch **do przodu** — cofanie sie nie dubluje slow (juz tak jest).
- W trybie reflow slowa liczymy z tekstu, ktory faktycznie trafil na wyrenderowana strone,
  wiec liczba jest dokladna na obu platformach.
- Strony/minute mozna pokazac obok jako ciekawostke, ale WPM jest miara wiodaca.
- Referencyjnie: przecietny doroslly czyta 200–250 wpm w jezyku ojczystym, wyraznie mniej
  w obcym — warto to pokazac jako kontekst przy statystyce gracza.

## Etapy

### Etap 1 — silnik reflow i wyglad Dicto (TERAZ)

- `domain/reading/BookContent.kt` — `BookContent(chapters)`, `Chapter(title, paragraphs)`.
  Format-agnostyczny: parser dowolnego formatu produkuje ten sam model.
- `domain/reading/ReaderSettings.kt` — rozmiar czcionki, interlinia, marginesy, justowanie,
  motyw (jasny/sepia/ciemny), `ReadingMode` (Reflow/Original). Zapisywane w ustawieniach.
- `feature/home/reading/reflow/TextPaginator.kt` — dzieli `BookContent` na strony przy pomocy
  `TextMeasurer` i `TextLayoutResult.getLineEnd`, pod aktualne `ReaderSettings` i rozmiar
  ekranu. Repaginacja przy zmianie ustawien/rotacji, z zachowaniem pozycji (kotwica znakowa,
  nie numer strony — numer strony zmienia znaczenie przy innym rozmiarze czcionki).
- `feature/home/reading/reflow/ReflowReader.kt` — `HorizontalPager` po stronach, tap po
  bokach kartkuje, tap w srodku pokazuje/chowa chrome. Typografia jak na screenie Dicto:
  serif, justowanie, wciecie akapitu, duza interlinia.
- Gorny pasek: powrot, spis tresci, ustawienia, licznik sesji, licznik zaznaczonych slow.
  Dolny: procent postepu.

### Etap 2 — parsowanie formatow

- TXT — trywialne.
- EPUB — rozpakowanie zipa + strip XHTML do akapitow. Zip w KMP wymaga `expect/actual`
  (Android `java.util.zip`, iOS przez PDFKit-owy odpowiednik albo czysto-kotlinowa biblioteka
  kompresji). **Do decyzji przy tym etapie.**
- PDF — zostaje wylacznie w trybie Original (bitmapa jak dzis).

### Etap 3 — tap-na-slowo i tlumaczenie

- Trafianie w slowo: `TextLayoutResult.getOffsetForPosition` → granice slowa wokol offsetu.
  Bez osobnych klikalnych boxow per slowo — precyzyjniej i taniej.
- Long-press / drag → zaznaczenie frazy albo calego zdania.
- Tlumaczenie: zapytanie ze slowem **i calym zdaniem jako kontekstem** (bez kontekstu slownik
  nie wie, ktore znaczenie wybrac). Warstwa `TranslationSource` z dwiema implementacjami:
  API tlumacza (tanie) i LLM (lepszy kontekst).
- Cache lokalny — to samo slowo nie jest tlumaczone i oplacane dwa razy.

### Etap 4 — quiz ze slowek

- Zaznaczone slowo zapisywane jako `SavedWord(slowo, tlumaczenie, zdanie-kontekst, ksiazka,
  data)`.
- Quiz losuje z tej puli i wpina sie w istniejacy system quizow (`domain/quiz/QuizModels.kt`),
  a nie buduje wlasnego.
- Powtorki rozlozone w czasie (spaced repetition) — slowa nietrafione wracaja czesciej.

### Etap 5 — questy i statystyki

- `ReadingGoalCatalog` dostaje cele oparte o nowe dane: „zaznacz 10 nowych slow",
  „zalicz quiz ze slowek", obok istniejacych minutowych.
- Ekran statystyk: WPM, laczny czas, seria dni, liczba nauczonych slow.
