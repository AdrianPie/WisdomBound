"""
Sklada efekty dzwiekowe gry z darmowych paczek Kenney (CC0 - bez licencji i bez obowiazku
podawania autora, patrz files/sfx/LICENSE.txt).

Paczki (pobierz i rozpakuj do jednego katalogu, np. ~/kenney):
    https://kenney.nl/assets/interface-sounds
    https://kenney.nl/assets/impact-sounds
    https://kenney.nl/assets/rpg-audio
    https://kenney.nl/assets/music-jingles

Uruchomienie (potrzebny pakiet `soundfile` do czytania OGG):
    python3 tools/sfx/build_sfx.py ~/kenney

Kazdy efekt to lista warstw (plik, start w sekundach, glosnosc, zmiana wysokosci). Wynik to WAV
mono 44.1 kHz w shared/src/commonMain/composeResources/files/sfx/ - WAV, bo iOS nie czyta OGG.
Wszystko jest normalizowane do szczytu -6 dBFS: kilka efektow grajacych naraz sumuje sie
i przy -1 dBFS (jak w pierwszej, syntezowanej wersji) przesterowywalo sie na charczenie.
"""

import glob
import os
import sys
import wave

import numpy as np
import soundfile as sf

SR = 44100
OUT = os.path.join(os.path.dirname(__file__), "..", "..", "shared", "src", "commonMain",
                   "composeResources", "files", "sfx")

# efekt -> [(fragment nazwy pliku, start_s, glosnosc, wysokosc)]; wysokosc 1.0 = oryginal
RECIPES = {
    "tap": [("click_001", 0, 1.0, 1.0)],
    "correct": [("confirmation_001", 0, 1.0, 1.0)],
    "wrong": [("error_006", 0, 1.0, 1.0)],
    "hit": [("impactPunch_medium_000", 0, 1.0, 1.0)],
    "crit": [("impactPunch_heavy_001", 0, 1.0, 1.0), ("impactMetal_light_002", 0.01, 0.45, 1.0)],
    "hurt": [("impactSoft_heavy_000", 0, 1.0, 0.9)],
    "whoosh": [("cloth1", 0, 1.0, 1.2)],
    "heal": [("confirmation_003", 0, 1.0, 1.0)],
    "wave": [("impactBell_heavy_000", 0, 1.0, 1.0)],
    "boss": [("impactBell_heavy_002", 0, 1.0, 0.6), ("impactPlate_heavy_000", 0, 0.6, 0.7)],
    "victory": [("jingles_PIZZI02", 0, 1.0, 1.0)],
    "defeat": [("jingles_PIZZI01", 0, 1.0, 1.0)],
    "level_up": [("jingles_PIZZI16", 0, 1.0, 1.0)],
    "summon": [("jingles_STEEL02", 0, 1.0, 1.0)],
    "chest": [("creak1", 0, 0.8, 1.0), ("handleCoins", 0.28, 1.0, 1.0)],
    "coins": [("handleCoins2", 0, 1.0, 1.0)],
    "purchase": [("handleCoins", 0, 0.9, 1.0), ("confirmation_002", 0.08, 0.8, 1.0)],
    "upgrade": [("impactMetal_heavy_000", 0, 1.0, 1.0), ("confirmation_004", 0.22, 0.6, 1.0)],
}


def find(root, fragment):
    hits = sorted(p for p in glob.glob(os.path.join(root, "**", "*.ogg"), recursive=True)
                  if os.path.basename(p)[:-4] == fragment or os.path.basename(p)[:-4].startswith(fragment))
    if not hits:
        raise SystemExit(f"Brak pliku '{fragment}' w {root}")
    return hits[0]


def load(path, pitch):
    x, sr = sf.read(path, dtype="float64")
    if x.ndim > 1:
        x = x.mean(axis=1)
    # Resampling do 44.1 kHz i zmiana wysokosci jednym przejsciem (nizej = dluzej, jak na tasmie).
    ratio = sr / SR * pitch
    idx = np.arange(0, len(x) - 1, ratio)
    return np.interp(idx, np.arange(len(x)), x)


def trim(x, threshold_db=-50):
    peak = np.max(np.abs(x)) or 1.0
    loud = np.nonzero(np.abs(x) > peak * 10 ** (threshold_db / 20))[0]
    if not len(loud):
        return x
    start = max(0, loud[0] - int(SR * 0.002))
    end = min(len(x), loud[-1] + int(SR * 0.03))
    return x[start:end]


def build(root, layers):
    parts = [(int(start * SR), load(find(root, name), pitch) * gain) for name, start, gain, pitch in layers]
    total = max(offset + len(sig) for offset, sig in parts)
    mix = np.zeros(total)
    for offset, sig in parts:
        mix[offset:offset + len(sig)] += sig
    mix = trim(mix)
    fade = min(len(mix), int(SR * 0.02))
    mix[-fade:] *= np.linspace(1, 0, fade)
    peak = np.max(np.abs(mix)) or 1.0
    return mix / peak * 10 ** (-6 / 20)


def write(name, x):
    os.makedirs(OUT, exist_ok=True)
    with wave.open(os.path.join(OUT, name + ".wav"), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes((x * 32767).astype(np.int16).tobytes())
    print(f"{name:10s} {len(x) / SR:4.2f}s")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit(__doc__)
    for effect, layers in RECIPES.items():
        write(effect, build(sys.argv[1], layers))
