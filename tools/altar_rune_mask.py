"""
Wycina z grafiki oltarza (`summon_center.webp`) maske SAMYCH rytych znakow i zapisuje ja jako
`summon_center_runes.webp` - bialy obrazek, w ktorym alfa = glebokosc rytu.

Po co: zeby runy dalo sie PRZEBARWIC na kolor rzadkosci summona (ColorFilter.tint na masce), a nie
tylko rozjasnic swiatlem - dodawanie swiatla na szarym rycie robi z niego jasnoszara plame, nie
zielony znak. Patrz `feature/home/.../city/AltarRunes.kt`.

Maski nie rysujemy recznie, bo musialaby byc przerysowywana przy kazdej poprawce grafiki. Tutaj
wynika z samego pliku: ryt to piksele wyraznie ciemniejsze od LOKALNEGO tla kamienia. Obszary sa
wypisane jawnie, bo inaczej maska lapie pekniecia kamieni i cienie stopni kregu.

Uruchomienie po podmianie grafiki oltarza:
    python3 tools/altar_rune_mask.py
"""

from PIL import Image, ImageFilter
import numpy as np

SRC = "shared/src/commonMain/composeResources/drawable/summon_center.webp"
DST = "shared/src/commonMain/composeResources/drawable/summon_center_runes.webp"

# Ryte znaki na kamieniach - prostokaty (x0, y0, x1, y1) jako ulamki kwadratowego kadru.
STONE_RUNES = [
    (0.470, 0.130, 0.600, 0.280),  # kamien centralny: rogaty symbol z okiem
    (0.490, 0.275, 0.580, 0.385),  # kamien centralny: znak pod nim
    (0.145, 0.330, 0.228, 0.450),  # kamien lewy: gorny
    (0.155, 0.445, 0.240, 0.540),  # kamien lewy: dolny
    (0.770, 0.355, 0.855, 0.450),  # kamien prawy: gorny
    (0.765, 0.450, 0.845, 0.540),  # kamien prawy: dolny
]

# Godla na swiecznikach - to nie ryt, tylko ciemna plama, wiec licza sie z jasnosci bezwzglednej.
HOLDER_EMBLEMS = [
    (0.320, 0.430, 0.375, 0.495),
    (0.212, 0.740, 0.268, 0.800),
    (0.817, 0.620, 0.872, 0.685),
    (0.692, 0.775, 0.747, 0.840),
]

# Gorna tarcza kregu jako ELIPSA (srodek x, srodek y, promien x, promien y) - prostokat lapalby
# jeszcze cienie stopni pod nia.
DISC = (0.487, 0.590, 0.233, 0.107)


def main() -> None:
    im = Image.open(SRC).convert("RGBA")
    size = im.size[0]
    a = np.asarray(im).astype(float)
    alpha = a[..., 3]
    lum = a[..., 0] * 0.3 + a[..., 1] * 0.59 + a[..., 2] * 0.11

    # Lokalne tlo kamienia; ryt to odchylka w dol od niego.
    blur = np.asarray(
        Image.fromarray(lum.astype(np.uint8)).filter(ImageFilter.BoxBlur(10))
    ).astype(float)
    # Obrys bryly sam w sobie jest ciemny - odsuwamy sie od krawedzi sylwetki.
    inside = np.asarray(
        Image.fromarray(((alpha > 128) * 255).astype(np.uint8)).filter(ImageFilter.MinFilter(15))
    ) > 128
    base = (alpha > 200) & inside & (lum > 45)

    grid_y, grid_x = np.mgrid[0:size, 0:size]
    xf, yf = grid_x / size, grid_y / size
    mask = np.zeros((size, size))

    def carve(low: float, high: float) -> np.ndarray:
        """Miekkie krycie rytu: `low` = jeszcze plaski kamien, `high` = pelna glebokosc."""
        depth = np.clip((blur - lum - low) / (high - low), 0, 1)
        depth[~base] = 0
        return depth

    # Ryty w kamieniach sa plytsze niz te w plycie, stad czulszy prog.
    shallow = carve(10, 30)
    for x0, y0, x1, y1 in STONE_RUNES:
        box = (xf >= x0) & (xf <= x1) & (yf >= y0) & (yf <= y1)
        mask[box] = np.maximum(mask[box], shallow[box])

    cx, cy, rx, ry = DISC
    disc = ((xf - cx) / rx) ** 2 + ((yf - cy) / ry) ** 2 <= 1
    deep = carve(20, 42)
    mask[disc] = np.maximum(mask[disc], deep[disc])

    flat = np.clip((72.0 - lum) / 22.0, 0, 1)
    flat[~(alpha > 200)] = 0
    for x0, y0, x1, y1 in HOLDER_EMBLEMS:
        box = (xf >= x0) & (xf <= x1) & (yf >= y0) & (yf <= y1)
        mask[box] = np.maximum(mask[box], flat[box])

    out = np.zeros((size, size, 4), np.uint8)
    out[..., 0:3] = 255
    out[..., 3] = (np.clip(mask, 0, 1) * 255).astype(np.uint8)
    Image.fromarray(out).save(DST, "WEBP", lossless=True, method=6)
    print(f"{DST}: {(out[..., 3] > 0).sum()} pikseli maski")


if __name__ == "__main__":
    main()
