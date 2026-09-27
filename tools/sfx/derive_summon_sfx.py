"""
Efekty rytualu summona - POCHODNE gotowych plikow z files/sfx (te same paczki Kenney, CC0), wiec
nie trzeba niczego dociagac. Uruchom po build_sfx.py:

    python3 tools/sfx/derive_summon_sfx.py

- summon_charge: odwrocony dzwon + plyta (boss) i nizszy dzwon (wave). Odwrocone uderzenie to
  klasyczny "wdech" przed wybuchem - narasta z ciszy i konczy sie DOKLADNIE na szczycie, wiec
  ekran odpala go tak, zeby koniec wypadl w chwili blysku.
- summon_boom: sam wybuch - dzwon z plyta, mocny cios i nisko strojony dzwon pod spodem.
- summon_tier: jasny "ding", gdy slup zmienia kolor na rzadszy (niebieski -> fiolet -> zloto).
- card_flip: krotki, wyzszy swist obracanej karty.

Wszystko normalizowane do -6 dBFS, jak reszta efektow (patrz build_sfx.py).
"""

import os
import wave

import numpy as np

SR = 44100
DIR = os.path.join(os.path.dirname(__file__), "..", "..", "shared", "src", "commonMain",
                   "composeResources", "files", "sfx")


def read(name):
    with wave.open(os.path.join(DIR, name + ".wav")) as w:
        raw = w.readframes(w.getnframes())
    return np.frombuffer(raw, dtype=np.int16).astype(np.float64) / 32767.0


def pitch(x, factor):
    """factor > 1 = wyzej i krocej (jak szybsza tasma)."""
    idx = np.arange(0, len(x) - 1, factor)
    return np.interp(idx, np.arange(len(x)), x)


def mix(*parts):
    """parts: (start_s, sygnal, glosnosc)."""
    total = max(int(start * SR) + len(sig) for start, sig, _ in parts)
    out = np.zeros(total)
    for start, sig, gain in parts:
        i = int(start * SR)
        out[i:i + len(sig)] += sig * gain
    return out


def fade_out(x, seconds):
    n = min(len(x), int(SR * seconds))
    x = x.copy()
    x[-n:] *= np.linspace(1, 0, n) ** 2
    return x


def swell(x, portion=0.75):
    """Narastanie z ciszy przez `portion` dlugosci - krzywa wykladnicza, zeby nie bylo 'kliku'."""
    n = int(len(x) * portion)
    x = x.copy()
    x[:n] *= np.linspace(0, 1, n) ** 2.2
    return x


def ramp_loudness(x, start=0.04, end=0.5, max_gain=6.0):
    """
    Wyrownuje narastanie: odwrocony dzwon sam z siebie jest cichy przez 3/4 czasu i ozywa dopiero
    na koncu. Obwiednia (RMS z 30 ms) jest podciagana do liniowej rampy start -> end, z sufitem
    wzmocnienia, zeby nie wyciagac szumu z cichego ogona.
    """
    win = int(SR * 0.03)
    env = np.sqrt(np.convolve(x ** 2, np.ones(win) / win, mode="same")) + 1e-6
    target = np.linspace(start, end, len(x))
    gain = np.clip(target / env, 0.0, max_gain)
    gain = np.convolve(gain, np.ones(win) / win, mode="same")
    return x * gain


def norm(x, db=-6.0):
    peak = np.max(np.abs(x)) or 1.0
    return x / peak * 10 ** (db / 20)


def write(name, x):
    with wave.open(os.path.join(DIR, name + ".wav"), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes((np.clip(x, -1, 1) * 32767).astype(np.int16).tobytes())
    print(f"{name:14s} {len(x) / SR:4.2f}s")


if __name__ == "__main__":
    boss, bell, crit, correct, whoosh = (read(n) for n in ("boss", "wave", "crit", "correct", "whoosh"))

    # Oba odwrocone sygnaly koncza sie na swoim (dawnym) uderzeniu - wyrownujemy je do konca.
    low_bell = pitch(bell, 0.8)[::-1]
    rev_boss = boss[::-1]
    length = max(len(low_bell), len(rev_boss))
    charge = mix(((length - len(low_bell)) / SR, low_bell, 0.7), ((length - len(rev_boss)) / SR, rev_boss, 1.0))
    write("summon_charge", norm(swell(ramp_loudness(charge), portion=0.15)))

    boom = mix((0, boss, 1.0), (0, crit, 0.9), (0.02, pitch(bell, 0.75), 0.55))
    write("summon_boom", norm(fade_out(boom, 0.35)))

    tier = mix((0, pitch(bell, 1.5), 1.0), (0.01, correct, 0.45))
    write("summon_tier", norm(fade_out(tier, 0.25)))

    write("card_flip", norm(fade_out(pitch(whoosh, 1.45), 0.08)))
