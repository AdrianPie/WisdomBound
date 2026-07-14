# Architektura LernHero

Ten plik odpowiada na jedno pytanie: **gdzie dopisac nowa rzecz, zeby projekt nie zamienil sie
z powrotem w skladowisko.** Jesli nie wiesz, gdzie cos wrzucic - odpowiedz jest tutaj.

## Zasada nadrzedna

Regula gry nie wie nic o ekranie, a ekran nie liczy regul gry.

Walka jest **symulowana z gory** w `:domain` (silnik zwraca strumien zdarzen), a `:feature:game`
jedynie odtwarza gotowy skrypt. Dzieki temu ta sama walka da sie przyspieszyc 16x, powtorzyc
z seeda i - docelowo - zweryfikowac na serwerze. Ta sama sztuczka trzyma mape lochu i quiz:
z seeda odtwarza sie wszystko.

## Moduly

```
:domain      reguly gry. Czysty Kotlin, zero Compose. Tu mieszka prawda o swiecie.
:data        implementacje repozytoriow (Firestore).
:shared      design system + zasoby + trasy nawigacji. Wspolne dla wszystkich ekranow.
:feature:*   ekrany. Kazdy trzyma swoj ViewModel obok swojego ekranu.
:navigation  graf nawigacji calej aplikacji.
:di          Koin - jedyne miejsce, gdzie cokolwiek jest sklejane.
:composeApp  punkt wejscia.
```

Kierunek zaleznosci jest jednostronny: `feature -> domain -> (nic)`. Gdy `:domain` zaczyna
potrzebowac Compose, to znak, ze cos wyladowalo w zlym module.

## :domain

```
combat/          silnik walki
  Stats, Combatant, BattleState        stan i liczby
  Effect, EffectCondition              cztery rodzaje efektow + bramki aktywacji
  effects/                             biblioteka gotowych efektow (nowy efekt = nowy plik tutaj)
  engine/                              petla tur i potok obrazen
  playback/                            tlumaczy zdarzenia na kroki dla animacji
item/            przedmiot = dane + lista efektow. catalog/ to przepisy (bron, pancerz, dodatki)
quiz/            bank pytan; bank/ to tresc podzielona na kategorie
dungeon/         generator mapy z seeda
player/          Player, PlayerEquipment, klasy postaci
monster/         karty potworow
common/          Rarity - dzielona przez przedmioty i potwory
repository/      interfejsy repozytoriow (implementacje sa w :data)
```

**Dodajesz miecz?** `item/catalog/Weapons.kt`. **Nowa mechanika?** `combat/effects/`.
**Nowe pytanie?** `quiz/bank/`. Silnika nie dotykasz w zadnym z tych przypadkow - to jest caly
sens tego podzialu.

## :feature:game

```
battle/
  BattleScreen        ekran + sterowanie odtwarzaniem skryptu
  BattleViewModel     stan gry, fazy (walka -> quiz -> nagrody)
  BattleUiState       model stanu dla ekranu
  BattleSpeed         jedno pokretlo tempa - kazde opoznienie w walce przechodzi przez nie
  component/          pole bitwy, HUD, dialogi (quiz, nagrody, porazka)
character/            renderowanie: sprite'y, modularny rycerz, stany animacji
```

## :feature:home

Kazdy ekran ma swoj folder. Wspolny `PlayerViewModel` siedzi w `player/`, bo dziela go trzy
ekrany (druzyna, sklep, ekwipunek) - nie ma sensu mnozyc go razy trzy.

```
navigation/   graf zakladek + dolny pasek
team/         zakladka Fight: sklad druzyny, wejscie do lochu
shop/         sklep
equipment/    ekwipunek (paper doll)
dungeon/      mapa lochu
player/       PlayerViewModel + stan gracza
ui/           rzeczy dzielone miedzy ekranami (RarityPalette, ItemArt, MonsterCardView)
dev/          narzedzia do strojenia grafiki. Nie jest to kod produkcyjny
```

## Gdzie sa granice grafiki

Domena nie zna obrazkow. Przedmiot niesie `iconKey: String`, potwor `spriteKey`, pytanie o flage
kod kraju - a warstwa UI zamienia te teksty na zasoby w **jednym pliku**:

| co | gdzie podmieniasz grafike |
|---|---|
| przedmioty | `feature/home/ui/ItemArt.kt` |
| pola na mapie lochu | `feature/home/dungeon/DungeonNodeArt.kt` |
| potwory | `feature/game/character/MonsterSprites.kt` |
| flagi | `shared/resources/FlagResources.kt` |

## Testy

`:domain` jest testowany, bo tam mieszka logika. 29 testow pilnuje generatora mapy, silnika walki
(efekty naprawde dzialaja w symulacji), migracji ekwipunku i banku pytan.

Warstwa UI nie ma testow - i to jest swiadome. Skoro walka rozstrzyga sie w domenie, to test
domeny sprawdza mechanike, a ekran zostaje tylko odtwarzaczem.
