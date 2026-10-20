#!/usr/bin/env python3
"""
Baza pytan quizu LernHero: podglad, sprawdzanie, dopisywanie i generowanie kodu.

Pytania wyboru (jedna poprawna + trzy bledne odpowiedzi) mieszkaja w domain/quizbank: jedno pytanie
= jedna linia TSV, kolumny rozdzielone tabulatorem. To jest ZRODLO PRAWDY. Pliki <Temat>Questions.kt
w domain/src/commonMain/.../quiz/bank sa z niego GENEROWANE - recznych zmian w nich sie nie robi,
bo nastepne `generate` je nadpisze (a QuizBankTsvSyncTest wylapie rozjazd).

Temat to albo jeden plik (biology.tsv), albo katalog podzielony na DZIALY (history/antiquity/
roman_empire.tsv). Nazwy i kolejnosc dzialow stoja w domain/quizbank/sections.tsv, a z niego
powstaje QuizSectionCatalog.kt. Tabela danych o panstwach (domain/quizbank/data/countries.tsv) to
nie pytania, tylko material dla CountryQuestions - z niej powstaje CountryData.kt.

Typowa sesja dopisywania pytan (szczegoly: domain/quizbank/README.md):

    python3 tools/quizbank.py stats                  # ile pytan w ktorym temacie, dziale i na ktorym poziomie
    python3 tools/quizbank.py list history/antiquity -d E   # podglad tematu albo dzialu
    python3 tools/quizbank.py find churchill         # szukaj w CALYM banku, wszystkie typy pytan
    python3 tools/quizbank.py check nowe.txt         # sprawdz nowe pytania - niczego nie zapisuje
    python3 tools/quizbank.py add nowe.txt           # check + dopisz do TSV + wygeneruj Kotlin
    python3 tools/quizbank.py move ID... --to DZIAL  # przenies pytania do innego dzialu
    python3 tools/quizbank.py plan history=500 geography=300   # tura: rozdziel pytania na dzialy, napisz zlecenia
    python3 tools/quizbank.py generate [--check]     # TSV -> Kotlin (--check: tylko porownaj)

Plik z nowymi pytaniami: naglowek + jedna linia na pytanie, kolumny rozdzielone tabulatorem albo
znakiem '|'. Kolumna id jest opcjonalna - bez niej skrypt nada unikalne id sam. W kolumnie topic
stoi temat (biology) albo, w temacie z dzialami, dzial: pelna sciezka (history/antiquity/roman_empire)
albo sama nazwa liscia, jesli jest jednoznaczna (roman_empire).

    topic | difficulty | prompt | correct | wrong1 | wrong2 | wrong3
    world_war_2 | Easy | Who led the Soviet Union during World War II? | Joseph Stalin | ...

`check` odtwarza warunki z QuizBankTest i QuizBankAuditTest (te same progi i listy, czytane wprost
z pliku testu) i dorzuca ostrzezenia, ktorych testy nie lapia: podobny fakt przy tej samej
odpowiedzi, odpowiedz zdradzona w tresci innego pytania, podobienstwo do pytan prawda/falsz,
zaznacz-wszystkie, ukladanek i szacowania. Bledy blokuja `add`, ostrzezenia trzeba przejrzec okiem.
"""

from __future__ import annotations

import argparse
import glob
import os
import re
import signal
import sys
import tempfile
import unicodedata
from collections import Counter, defaultdict
from dataclasses import dataclass, field

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_DIR = os.path.join(ROOT, "domain", "quizbank")
SECTIONS_TSV = os.path.join(DATA_DIR, "sections.tsv")
KOTLIN_DIR = os.path.join(ROOT, "domain", "src", "commonMain", "kotlin", "com", "lernhero", "domain", "quiz", "bank")
CATALOG_KT = os.path.join(KOTLIN_DIR, "QuizSectionCatalog.kt")
COUNTRIES_TSV = os.path.join(DATA_DIR, "data", "countries.tsv")
COUNTRIES_KT = os.path.join(KOTLIN_DIR, "CountryData.kt")
AUDIT_TEST = os.path.join(
    ROOT, "domain", "src", "commonTest", "kotlin", "com", "lernhero", "domain", "quiz", "QuizBankAuditTest.kt"
)

LEVELS = ["Easy", "Medium", "Hard", "Expert"]
LETTER = {"Easy": "E", "Medium": "M", "Hard": "H", "Expert": "X"}
LEVEL_BY_ALIAS = {}
for _level, _letter in LETTER.items():
    LEVEL_BY_ALIAS[_level.lower()] = _level
    LEVEL_BY_ALIAS[_letter.lower()] = _level

COLUMNS = ["id", "difficulty", "prompt", "correct", "wrong1", "wrong2", "wrong3"]
SECTION_COLUMNS = ["path", "label", "icon"]
SEGMENT = re.compile(r"^[a-z][a-z0-9_]*$")


@dataclass(frozen=True)
class Topic:
    key: str  # nazwa pliku TSV (albo katalogu z dzialami) i nazwa tematu w plikach z nowymi pytaniami
    obj: str  # generowany obiekt Kotlin
    category: str  # QuizCategory
    prefix: str  # prefiks id nadawanych nowym pytaniom

    @property
    def tsv(self) -> str:
        return os.path.join(DATA_DIR, self.key + ".tsv")

    @property
    def dir(self) -> str:
        return os.path.join(DATA_DIR, self.key)

    @property
    def kt(self) -> str:
        return os.path.join(KOTLIN_DIR, self.obj + ".kt")

    @property
    def split(self) -> bool:
        """Czy temat jest podzielony na dzialy (katalog zamiast jednego pliku)."""
        return os.path.isdir(self.dir)


TOPICS = [
    Topic("biology", "BiologyQuestions", "Biology", "bio"),
    Topic("animals", "AnimalQuestions", "Animals", "ani"),
    Topic("history", "HistoryQuestions", "History", "his"),
    Topic("elements", "ElementQuestions", "Elements", "ele"),
    Topic("capitals", "CapitalQuestions", "Capitals", "cap"),
    Topic("science", "ScienceQuestions", "Science", "sci"),
    Topic("geography", "GeographyQuestions", "Geography", "geo"),
    Topic("sport", "SportQuestions", "Sport", "spo"),
    Topic("math", "MathQuestions", "Math", "mat"),
    Topic("literature", "LiteratureQuestions", "Literature", "lit"),
    Topic("technology", "TechnologyQuestions", "Technology", "tec"),
    Topic("mythology", "MythologyQuestions", "Mythology", "myt"),
    Topic("pop_culture", "PopCultureQuestions", "PopCulture", "pop"),
    Topic("poland", "PolandQuestions", "Poland", "pol"),
]


def _name_key(name: str) -> str:
    return re.sub(r"[\s_\-]", "", name).lower()


TOPIC_BY_NAME: dict[str, Topic] = {}
for _t in TOPICS:
    for _alias in (_t.key, _t.obj, _t.category, _t.obj.replace("Questions", ""), _t.prefix):
        TOPIC_BY_NAME[_name_key(_alias)] = _t
TOPIC_BY_KEY = {t.key: t for t in TOPICS}


def topic_named(name: str) -> Topic | None:
    return TOPIC_BY_NAME.get(_name_key(name))


# ------------------------------------------------------------------------------------------------
# Dzialy (sections.tsv)


@dataclass(frozen=True)
class Section:
    path: str  # history/antiquity/roman_empire
    label: str  # Roman Empire
    icon: str  # emoji pod przyszla grafike, jak QuizCategory.icon

    @property
    def topic(self) -> Topic | None:
        return TOPIC_BY_KEY.get(self.path.split("/")[0])

    @property
    def parent(self) -> str | None:
        """Sciezka dzialu nadrzednego albo None, gdy dzial wisi wprost pod tematem."""
        head = self.path.rsplit("/", 1)[0]
        return head if "/" in head else None

    @property
    def depth(self) -> int:
        return self.path.count("/")

    @property
    def tsv(self) -> str:
        return os.path.join(DATA_DIR, *self.path.split("/")) + ".tsv"

    @property
    def is_leaf(self) -> bool:
        return os.path.isfile(self.tsv)


def read_sections() -> list[Section]:
    if not os.path.exists(SECTIONS_TSV):
        return []
    sections: list[Section] = []
    header = None
    with open(SECTIONS_TSV, encoding="utf-8") as f:
        for lineno, raw in enumerate(f, 1):
            line = raw.rstrip("\r\n")
            if not line.strip() or line.startswith("#"):
                continue
            cells = line.split("\t")
            if header is None:
                header = cells
                if header != SECTION_COLUMNS:
                    sys.exit(f"{SECTIONS_TSV}:{lineno}: zly naglowek {header}, oczekiwany {SECTION_COLUMNS}")
                continue
            if len(cells) != len(SECTION_COLUMNS):
                sys.exit(f"{SECTIONS_TSV}:{lineno}: {len(cells)} kolumn zamiast {len(SECTION_COLUMNS)}")
            sections.append(Section(*[c.strip() for c in cells]))
    return sections


def validate_sections(sections: list[Section]) -> list[str]:
    """Bledy spojnosci sections.tsv z katalogami na dysku. Pusta lista = wszystko gra."""
    problems: list[str] = []
    seen: dict[str, Section] = {}
    for s in sections:
        segments = s.path.split("/")
        if s.path in seen:
            problems.append(f"dzial {s.path} wystepuje dwa razy")
        if s.topic is None or not s.topic.split:
            problems.append(f"dzial {s.path}: {segments[0]!r} nie jest tematem podzielonym na katalog")
        if len(segments) < 2 or not all(SEGMENT.match(seg) for seg in segments):
            problems.append(f"dzial {s.path}: sciezka to temat/dzial[/poddzial], same male litery, cyfry i _")
        if not s.label or not s.icon:
            problems.append(f"dzial {s.path}: brak nazwy albo ikony")
        if s.parent and s.parent not in seen:
            problems.append(f"dzial {s.path}: dzial nadrzedny {s.parent} musi stac w sections.tsv wyzej")
        is_dir = os.path.isdir(os.path.join(DATA_DIR, *segments))
        if s.is_leaf and is_dir:
            problems.append(f"dzial {s.path}: jest i plik {s.path}.tsv, i katalog {s.path}/ - zostaw jedno")
        elif not s.is_leaf and not is_dir:
            problems.append(f"dzial {s.path}: brak pliku {s.path}.tsv (lisc) albo katalogu {s.path}/ (galaz)")
        elif is_dir and not any(o.path.startswith(s.path + "/") for o in sections):
            problems.append(f"dzial {s.path}: katalog bez poddzialow w sections.tsv")
        seen[s.path] = s
    for topic in TOPICS:
        if not topic.split:
            continue
        if os.path.exists(topic.tsv):
            problems.append(f"temat {topic.key} ma i katalog, i plik {topic.key}.tsv - zostaw jedno")
        for path in sorted(glob.glob(os.path.join(topic.dir, "**", "*.tsv"), recursive=True)):
            rel = os.path.relpath(path, DATA_DIR)[: -len(".tsv")].replace(os.sep, "/")
            if rel not in seen:
                problems.append(f"plik {rel}.tsv nie ma wpisu w sections.tsv")
    return problems


def checked_sections() -> list[Section]:
    sections = read_sections()
    problems = validate_sections(sections)
    if problems:
        sys.exit("sections.tsv nie zgadza sie z katalogami:\n  " + "\n  ".join(problems))
    return sections


def leaves_of(topic: Topic, sections: list[Section]) -> list[Section]:
    return [s for s in sections if s.topic == topic and s.is_leaf]


@dataclass(frozen=True)
class Target:
    """Gdzie trafia pytanie: temat i - w temacie z dzialami - lisc drzewa."""

    topic: Topic
    section: Section | None = None

    @property
    def tsv(self) -> str:
        return self.section.tsv if self.section else self.topic.tsv

    @property
    def name(self) -> str:
        return self.section.path if self.section else self.topic.key


def resolve_target(name: str, sections: list[Section]) -> tuple[Target | None, str]:
    """Temat albo dzial po nazwie: 'biology', 'history/antiquity/roman_empire', 'antiquity/roman_empire',
    'roman_empire'. Zwraca (cel, opis problemu)."""
    clean = name.strip().strip("/").lower()
    leaves = [s for s in sections if s.is_leaf]
    exact = [s for s in leaves if s.path == clean]
    if not exact:
        exact = [s for s in leaves if s.path.endswith("/" + clean)]
    if len(exact) == 1:
        return Target(exact[0].topic, exact[0]), ""
    if len(exact) > 1:
        return None, f"niejednoznaczny dzial {name!r}: " + ", ".join(s.path for s in exact)
    topic = topic_named(clean)
    if topic is None:
        return None, f"nieznany temat albo dzial {name!r} (tematy: {', '.join(t.key for t in TOPICS)})"
    if topic.split:
        return None, (
            f"temat {topic.key} jest podzielony na dzialy - podaj dzial, np. "
            + ", ".join(s.path for s in leaves_of(topic, sections)[:3]) + " (lista: `stats`)"
        )
    return Target(topic), ""


def under(path: str | None, prefix: str) -> bool:
    return path is not None and (path == prefix or path.startswith(prefix + "/"))


# ------------------------------------------------------------------------------------------------
# Kotlin: escapowanie i parsowanie

STR = r'"((?:[^"\\]|\\.)*)"'
STR_NC = r'"(?:[^"\\]|\\.)*"'
LIST_OF = r"listOf\(\s*((?:" + STR_NC + r"\s*,?\s*)*)\)"
TF_CALL = re.compile(r"tf\(\s*" + STR + r"\s*,\s*" + STR + r"\s*,\s*(true|false)\s*,\s*(\w+)\s*,\s*(\w+)\s*\)")
MS_CALL = re.compile(r"ms\(\s*" + STR + r"\s*,\s*" + STR + r"\s*,\s*" + LIST_OF + r"\s*,\s*" + LIST_OF + r"\s*,\s*(\w+)\s*,\s*(\w+)\s*\)")
ORD_CALL = re.compile(r"ord\(\s*" + STR + r"\s*,\s*" + STR + r"\s*,\s*" + LIST_OF + r"\s*,\s*(\w+)\s*,\s*(\w+)\s*\)")
EST_CALL = re.compile(r"\(\s*\"([a-z][a-z0-9_]*)\"\s*,\s*" + STR)

KT_ESCAPES = {"\\": "\\", '"': '"', "$": "$", "'": "'", "n": "\n", "t": "\t"}


def kt_unescape(s: str) -> str:
    out, i = [], 0
    while i < len(s):
        if s[i] == "\\" and i + 1 < len(s) and s[i + 1] in KT_ESCAPES:
            out.append(KT_ESCAPES[s[i + 1]])
            i += 2
        else:
            out.append(s[i])
            i += 1
    return "".join(out)


def kt_string(s: str) -> str:
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$") + '"'


def kt_strings(list_body: str) -> list[str]:
    return [kt_unescape(m) for m in re.findall(STR, list_body)]


# ------------------------------------------------------------------------------------------------
# Model


@dataclass
class Question:
    topic: Topic
    level: str
    id: str
    prompt: str
    correct: str
    wrong: list[str]
    where: str = ""  # plik:linia - do raportow
    section: str | None = None  # sciezka dzialu w temacie z dzialami

    @property
    def answers(self) -> list[str]:
        return [self.correct] + self.wrong

    @property
    def place(self) -> str:
        return self.section or self.topic.key

    def tsv_line(self) -> str:
        cells = [self.id, self.level, self.prompt, self.correct] + self.wrong
        for cell in cells:
            if "\t" in cell or "\n" in cell:
                raise ValueError(f"Tabulator albo nowa linia w polu: {cell!r}")
        return "\t".join(cells)


@dataclass
class Other:
    """Pytanie innego typu (prawda/falsz, zaznacz-wszystkie, ukladanka, szacowanie) - tylko do porownan."""

    kind: str
    id: str
    text: str
    category: str = ""
    level: str = ""


@dataclass
class Bank:
    choice: list[Question]
    others: list[Other]
    sections: list[Section] = field(default_factory=list)

    def all_ids(self) -> set[str]:
        return {q.id for q in self.choice} | {o.id for o in self.others}


# ------------------------------------------------------------------------------------------------
# TSV


def read_tsv(path: str, topic: Topic, section: str | None) -> tuple[list[str], list[Question]]:
    comments: list[str] = []
    rows: list[Question] = []
    header = None
    name = os.path.relpath(path, DATA_DIR)
    with open(path, encoding="utf-8") as f:
        for lineno, raw in enumerate(f, 1):
            line = raw.rstrip("\r\n")
            if not line.strip():
                continue
            if line.startswith("#"):
                if header is None:
                    comments.append(line[2:] if line.startswith("# ") else line[1:])
                continue
            cells = line.split("\t")
            if header is None:
                header = cells
                if header != COLUMNS:
                    sys.exit(f"{path}:{lineno}: zly naglowek {header}, oczekiwany {COLUMNS}")
                continue
            if len(cells) != len(COLUMNS):
                sys.exit(f"{path}:{lineno}: {len(cells)} kolumn zamiast {len(COLUMNS)}")
            qid, level, prompt, correct, w1, w2, w3 = cells
            level_name = LEVEL_BY_ALIAS.get(level.strip().lower())
            if level_name is None:
                sys.exit(f"{path}:{lineno}: nieznany poziom {level!r}")
            rows.append(Question(topic, level_name, qid, prompt, correct, [w1, w2, w3], f"{name}:{lineno}", section))
    return comments, rows


@dataclass
class TopicData:
    """Calosc tematu: komentarz pliku (temat bez dzialow) albo komentarze i pytania kazdego dzialu."""

    topic: Topic
    comments: list[str]
    parts: list[tuple[Section | None, list[str], list[Question]]]

    @property
    def rows(self) -> list[Question]:
        return [q for _, _, rows in self.parts for q in rows]


def read_topic(topic: Topic, sections: list[Section]) -> TopicData:
    if not topic.split:
        comments, rows = read_tsv(topic.tsv, topic, None)
        return TopicData(topic, comments, [(None, comments, rows)])
    parts = []
    for s in leaves_of(topic, sections):
        comments, rows = read_tsv(s.tsv, topic, s.path)
        parts.append((s, comments, rows))
    return TopicData(topic, [], parts)


# ------------------------------------------------------------------------------------------------
# Generowanie Kotlina

SECTION = {
    "Easy": "/* ------------------------------ Easy ------------------------------ */",
    "Medium": "/* ----------------------------- Medium ----------------------------- */",
    "Hard": "/* ------------------------------ Hard ------------------------------ */",
    "Expert": "/* ----------------------------- Expert ----------------------------- */",
}


def _q_line(q: Question) -> str:
    wrong = ", ".join(kt_string(w) for w in q.wrong)
    return f"        q({kt_string(q.id)}, {kt_string(q.prompt)}, {kt_string(q.correct)}, listOf({wrong}), {LETTER[q.level]}),"


def _kotlin_head(topic: Topic, source: str) -> list[str]:
    return [
        f"// PLIK GENEROWANY z {source} przez `python3 tools/quizbank.py generate`.",
        "// Nie edytuj go recznie - pytania dopisuje sie do TSV (patrz domain/quizbank/README.md).",
        "package com.lernhero.domain.quiz.bank",
        "",
        "import com.lernhero.domain.quiz.QuizCategory",
        "import com.lernhero.domain.quiz.QuizDifficulty",
        "import com.lernhero.domain.quiz.QuizEntry",
        "",
    ]


def _kotlin_helpers(topic: Topic) -> list[str]:
    return [
        f"object {topic.obj} {{",
        "",
        "    private val E = QuizDifficulty.Easy",
        "    private val M = QuizDifficulty.Medium",
        "    private val H = QuizDifficulty.Hard",
        "    private val X = QuizDifficulty.Expert",
        "",
        "    private fun q(id: String, prompt: String, correct: String, wrong: List<String>, difficulty: QuizDifficulty) =",
        f"        QuizEntry(id = id, prompt = prompt, correct = correct, wrong = wrong, category = QuizCategory.{topic.category}, difficulty = difficulty)",
        "",
    ]


def section_function(section: Section) -> str:
    """history/antiquity/roman_empire -> antiquityRomanEmpire"""
    words = [w for seg in section.path.split("/")[1:] for w in seg.split("_") if w]
    return words[0] + "".join(w[:1].upper() + w[1:] for w in words[1:])


def render_kotlin(data: TopicData, sections: list[Section]) -> str:
    topic = data.topic
    if topic.split:
        return render_kotlin_sections(data, sections)
    rows = data.rows
    out = _kotlin_head(topic, f"domain/quizbank/{topic.key}.tsv")
    if data.comments:
        out.append("/**")
        out += [(" * " + c) if c else " *" for c in data.comments]
        out.append(" */")
    out += _kotlin_helpers(topic)
    by_level = {level: [q for q in rows if q.level == level] for level in LEVELS}
    levels = [level for level in LEVELS if by_level[level]]
    out += [
        "    val all: List<QuizEntry> = " + " + ".join(f"{level.lower()}()" for level in levels),
        "",
        "    // Kazdy poziom w osobnej funkcji: jedna wielka lista w inicjalizatorze obiektu przekroczylaby",
        "    // z czasem limit 64 KB kodu na metode JVM (735 pytan historii zajmowalo w nim juz ~36 KB).",
    ]
    for level in levels:
        out += ["", "    " + SECTION[level], f"    private fun {level.lower()}(): List<QuizEntry> = listOf("]
        entries = [_q_line(q) for q in by_level[level]]
        entries[-1] = entries[-1][:-1]
        out += entries + ["    )"]
    out += ["}", ""]
    return "\n".join(out)


def section_title(section: Section, sections: list[Section]) -> str:
    """Antiquity / Roman Empire"""
    labels = {s.path: s.label for s in sections}
    trail, path = [], section.path
    while "/" in path:
        trail.append(labels.get(path, path))
        path = path.rsplit("/", 1)[0]
    return " / ".join(reversed(trail))


def render_kotlin_sections(data: TopicData, sections: list[Section]) -> str:
    topic = data.topic
    parts = [(s, c, rows) for s, c, rows in data.parts if rows]
    out = _kotlin_head(topic, f"domain/quizbank/{topic.key}/ (dzialy: domain/quizbank/sections.tsv)")
    out += [
        "/**",
        f" * Temat podzielony na dzialy - kazdy plik w domain/quizbank/{topic.key}/ to jeden dzial, a nazwy",
        " * i kolejnosc dzialow stoja w domain/quizbank/sections.tsv (patrz [QuizSectionCatalog]).",
        " * Kazde pytanie niesie sciezke swojego dzialu w [QuizEntry.section].",
        " */",
    ]
    out += _kotlin_helpers(topic)
    out += [
        "    private fun section(path: String, vararg entries: QuizEntry): List<QuizEntry> =",
        "        entries.map { it.copy(section = path) }",
        "",
        "    val all: List<QuizEntry> = listOf(",
    ]
    calls = [f"        {section_function(s)}()," for s, _, _ in parts]
    calls[-1] = calls[-1][:-1]
    out += calls + [
        "    ).flatten()",
        "",
        "    // Kazdy dzial w osobnej funkcji - jedna wielka lista przekroczylaby limit 64 KB kodu na metode JVM.",
    ]
    for s, comments, rows in parts:
        out += ["", f"    /* {section_title(s, sections)} - {s.path}.tsv */"]
        out += [("    // " + c).rstrip() for c in comments]
        out += [f"    private fun {section_function(s)}(): List<QuizEntry> = section(", f"        {kt_string(s.path)},"]
        entries = [_q_line(q) for q in rows]
        entries[-1] = entries[-1][:-1]
        out += entries + ["    )"]
    out += ["}", ""]
    return "\n".join(out)


def render_catalog(sections: list[Section]) -> str:
    out = [
        "// PLIK GENEROWANY z domain/quizbank/sections.tsv przez `python3 tools/quizbank.py generate`.",
        "// Nie edytuj go recznie - dzialy dopisuje sie do sections.tsv (patrz domain/quizbank/README.md).",
        "package com.lernhero.domain.quiz.bank",
        "",
        "import com.lernhero.domain.quiz.QuizCategory",
        "import com.lernhero.domain.quiz.QuizSection",
        "",
        "/**",
        " * Wszystkie dzialy tematow podzielonych na katalogi, w kolejnosci z sections.tsv - galaz zawsze",
        " * przed swoimi poddzialami. Wygodny dostep do drzewa daje [com.lernhero.domain.quiz.QuizSections].",
        " */",
        "object QuizSectionCatalog {",
        "",
        "    val all: List<QuizSection> = listOf(",
    ]
    lines = []
    for s in sections:
        lines.append(
            f"        QuizSection({kt_string(s.path)}, {kt_string(s.label)}, {kt_string(s.icon)}, "
            f"QuizCategory.{s.topic.category}),"
        )
    if lines:
        lines[-1] = lines[-1][:-1]
    out += lines + ["    )", "}", ""]
    return "\n".join(out)


# ------------------------------------------------------------------------------------------------
# Dane o panstwach (material dla CountryQuestions, nie pytania)

COUNTRY_COLUMNS = [
    "code", "country", "capital", "continent", "population", "area_km2", "capital_lat", "capital_lon",
    "landlocked", "neighbours", "fame", "capital_quiz", "unclear",
]
CONTINENTS = {
    "Europe": "Europe", "Asia": "Asia", "Africa": "Africa", "North America": "NorthAmerica",
    "South America": "SouthAmerica", "Oceania": "Oceania",
}


def read_countries() -> list[dict]:
    """Czyta countries.tsv i pilnuje spojnosci: kody, liczby, symetria granic, pary niejasne."""
    rows: list[dict] = []
    header = None
    problems: list[str] = []
    with open(COUNTRIES_TSV, encoding="utf-8") as f:
        for lineno, raw in enumerate(f, 1):
            line = raw.rstrip("\r\n")
            if not line.strip() or line.startswith("#"):
                continue
            cells = line.split("\t")
            if header is None:
                header = cells
                if header != COUNTRY_COLUMNS:
                    sys.exit(f"{COUNTRIES_TSV}:{lineno}: zly naglowek {header}, oczekiwany {COUNTRY_COLUMNS}")
                continue
            if len(cells) != len(COUNTRY_COLUMNS):
                sys.exit(f"{COUNTRIES_TSV}:{lineno}: {len(cells)} kolumn zamiast {len(COUNTRY_COLUMNS)}")
            row = dict(zip(COUNTRY_COLUMNS, cells))
            row["line"] = lineno
            try:
                row["population"] = int(row["population"])
                row["area_km2"] = float(row["area_km2"])
                row["capital_lat"] = float(row["capital_lat"])
                row["capital_lon"] = float(row["capital_lon"])
                row["fame"] = int(row["fame"])
            except ValueError as e:
                sys.exit(f"{COUNTRIES_TSV}:{lineno}: zla liczba ({e})")
            row["neighbours"] = row["neighbours"].split()
            row["unclear"] = row["unclear"].split()
            rows.append(row)
    codes = {r["code"] for r in rows}
    by_code = {r["code"]: r for r in rows}
    for r in rows:
        where = f"{COUNTRIES_TSV}:{r['line']} ({r['code']})"
        if not re.fullmatch(r"[a-z]{2}", r["code"]):
            problems.append(f"{where}: kod ma byc dwuliterowy, malymi literami")
        if r["continent"] not in CONTINENTS:
            problems.append(f"{where}: nieznany kontynent {r['continent']!r}")
        if r["landlocked"] not in ("0", "1") or r["capital_quiz"] not in ("0", "1"):
            problems.append(f"{where}: landlocked i capital_quiz to 0 albo 1")
        if r["fame"] not in (1, 2, 3):
            problems.append(f"{where}: fame to 1, 2 albo 3")
        if r["population"] <= 0 or r["area_km2"] <= 0:
            problems.append(f"{where}: ludnosc i powierzchnia musza byc dodatnie")
        if not (-90 <= r["capital_lat"] <= 90 and -180 <= r["capital_lon"] <= 180):
            problems.append(f"{where}: wspolrzedne poza zakresem")
        if not r["country"].isascii() or not r["capital"].isascii():
            problems.append(f"{where}: nazwy tylko w ASCII")
        for n in r["neighbours"]:
            if n not in codes:
                problems.append(f"{where}: nieznany sasiad {n}")
            elif r["code"] not in by_code[n]["neighbours"]:
                problems.append(f"{where}: {n} nie ma {r['code']} wsrod sasiadow (granica jest symetryczna)")
        for n in r["unclear"]:
            if n not in codes:
                problems.append(f"{where}: nieznany kod w unclear: {n}")
            elif n in r["neighbours"]:
                problems.append(f"{where}: {n} jest jednoczesnie sasiadem i para niejasna")
    if len(codes) != len(rows):
        problems.append(f"{COUNTRIES_TSV}: zdublowane kody panstw")
    for field_name in ("country", "capital"):
        seen = Counter(r[field_name] for r in rows)
        dup = [name for name, n in seen.items() if n > 1]
        if dup:
            problems.append(f"{COUNTRIES_TSV}: powtorzone wartosci w kolumnie {field_name}: {dup}")
    if problems:
        sys.exit("\n".join(problems))
    return rows


def render_countries(rows: list[dict]) -> str:
    out = [
        "// PLIK GENEROWANY z domain/quizbank/data/countries.tsv przez `python3 tools/quizbank.py generate`.",
        "// Nie edytuj go recznie - dane panstw poprawia sie w TSV (patrz domain/quizbank/README.md).",
        "package com.lernhero.domain.quiz.bank",
        "",
        "import com.lernhero.domain.quiz.bank.CountryFact.Continent",
        "",
        "/** Tabela panstw dla [CountryQuestions] - opis kolumn stoi na gorze countries.tsv. */",
        "object CountryData {",
        "",
        "    private fun c(",
        "        code: String,",
        "        name: String,",
        "        capital: String,",
        "        continent: Continent,",
        "        population: Long,",
        "        areaKm2: Double,",
        "        capitalLat: Double,",
        "        capitalLon: Double,",
        "        landlocked: Boolean,",
        "        neighbours: String,",
        "        fame: Int,",
        "        capitalQuiz: Boolean,",
        "        unclear: String",
        "    ) = CountryFact(",
        "        code, name, capital, continent, population, areaKm2, capitalLat, capitalLon, landlocked,",
        "        neighbours.split(' ').filter { it.isNotEmpty() }.toSet(), fame, capitalQuiz,",
        "        unclear.split(' ').filter { it.isNotEmpty() }.toSet()",
        "    )",
        "",
        "    val all: List<CountryFact> = listOf(",
    ]
    lines = []
    for r in rows:
        lines.append(
            f"        c({kt_string(r['code'])}, {kt_string(r['country'])}, {kt_string(r['capital'])}, "
            f"Continent.{CONTINENTS[r['continent']]}, {r['population']}, {r['area_km2']!r}, "
            f"{r['capital_lat']!r}, {r['capital_lon']!r}, {'true' if r['landlocked'] == '1' else 'false'}, "
            f"{kt_string(' '.join(r['neighbours']))}, {r['fame']}, {'true' if r['capital_quiz'] == '1' else 'false'}, "
            f"{kt_string(' '.join(r['unclear']))}),"
        )
    if lines:
        lines[-1] = lines[-1][:-1]
    out += lines + ["    )", "}", ""]
    return "\n".join(out)


# ------------------------------------------------------------------------------------------------
# Wczytanie calego banku


def load_others() -> list[Other]:
    others: list[Other] = []

    def read(name: str) -> str:
        path = os.path.join(KOTLIN_DIR, name)
        if not os.path.exists(path):
            return ""
        with open(path, encoding="utf-8") as f:
            return f.read()

    for m in TF_CALL.finditer(read("TrueFalseQuestions.kt")):
        qid, statement, is_true, cat, level = m.groups()
        verdict = "PRAWDA" if is_true == "true" else "FALSZ"
        others.append(Other("prawda/falsz", qid, f"{kt_unescape(statement)} [{verdict}]", cat, level))
    for m in MS_CALL.finditer(read("MultiSelectQuestions.kt")):
        qid, prompt, correct, wrong, cat, level = m.groups()
        text = f"{kt_unescape(prompt)} -> {', '.join(kt_strings(correct))}"
        others.append(Other("zaznacz-wszystkie", qid, text, cat, level))
    for m in ORD_CALL.finditer(read("OrderingQuestions.kt")):
        qid, prompt, items, cat, level = m.groups()
        text = f"{kt_unescape(prompt)} -> {', '.join(kt_strings(items))}"
        others.append(Other("ukladanka", qid, text, cat, level))
    for path in sorted(glob.glob(os.path.join(KOTLIN_DIR, "Estimation*.kt"))):
        with open(path, encoding="utf-8") as f:
            for m in EST_CALL.finditer(f.read()):
                qid, prompt = m.groups()
                prompt = kt_unescape(prompt)
                if len(prompt) > 15 and prompt.rstrip().endswith("?"):
                    others.append(Other("szacowanie", qid, prompt))
    return others


def load_bank() -> Bank:
    sections = checked_sections()
    choice: list[Question] = []
    for topic in TOPICS:
        choice += read_topic(topic, sections).rows
    return Bank(choice, load_others(), sections)


# ------------------------------------------------------------------------------------------------
# Reguly z QuizBankAuditTest (czytane z pliku testu, zeby skrypt i test sie nie rozjechaly)

DEFAULT_STOP = {
    "the", "a", "an", "of", "in", "to", "is", "are", "was", "were", "and", "or", "for", "on", "at", "by",
    "with", "from", "which", "what", "who", "whose", "how", "many", "much", "does", "did", "this", "these",
    "those", "that", "as", "be", "been", "not", "called", "its", "it",
}


@dataclass
class Rules:
    stop: set[str]
    poland_only: list[str]
    duplicate_threshold: float
    length_giveaway: int


def load_rules() -> Rules:
    rules = Rules(set(DEFAULT_STOP), [], 0.55, 15)
    if not os.path.exists(AUDIT_TEST):
        return rules
    with open(AUDIT_TEST, encoding="utf-8") as f:
        text = f.read()
    m = re.search(r"val STOP = setOf\((.*?)\)", text, re.S)
    if m:
        rules.stop = set(re.findall(r'"([^"]*)"', m.group(1)))
    m = re.search(r"val POLAND_ONLY = listOf\((.*?)\)", text, re.S)
    if m:
        rules.poland_only = re.findall(r'"([^"]*)"', m.group(1))
    m = re.search(r"DUPLICATE_THRESHOLD = ([0-9.]+)", text)
    if m:
        rules.duplicate_threshold = float(m.group(1))
    m = re.search(r"LENGTH_GIVEAWAY = (\d+)", text)
    if m:
        rules.length_giveaway = int(m.group(1))
    return rules


def keywords(text: str, stop: set[str]) -> set[str]:
    # Tak samo jak String.keywords() w QuizBankAuditTest: apostrof nie skleja sie ze slowem.
    return {w for w in re.findall(r"[a-z]+", text.lower().replace("'", " ")) if len(w) > 3 and w not in stop}


def jaccard(a: set[str], b: set[str]) -> float:
    if not a or not b:
        return 0.0
    return len(a & b) / len(a | b)


def normalized_answer(answer: str) -> str:
    s = answer.lower()
    if s.startswith("the "):
        s = s[4:]
    return s.strip()


def contains_phrase(haystack: str, needle: str) -> bool:
    return re.search(r"(?<![a-z0-9])" + re.escape(needle) + r"(?![a-z0-9])", haystack) is not None


NUMBER_WORDS = {
    "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve",
    "none", "zero", "yes", "no", "true", "false",
}


def is_specific(answer: str, min_len: int) -> bool:
    """Czy odpowiedz jest na tyle konkretna, ze jej obecnosc w innej tresci cos zdradza."""
    plain = answer.replace(",", "").replace(".", "").replace(" ", "")
    return len(answer) >= min_len and not plain.isdigit() and answer not in NUMBER_WORDS


def answers_match(a: str, b: str) -> bool:
    """Ta sama odpowiedz, takze zapisana inaczej: 'rowling' ~ 'j.k. rowling', 'usa' ~ 'the usa'."""
    if a == b:
        return True
    if min(len(a), len(b)) >= 4 and (contains_phrase(a, b) or contains_phrase(b, a)):
        return True
    ta, tb = set(re.findall(r"[a-z0-9]+", a)), set(re.findall(r"[a-z0-9]+", b))
    return len(ta) > 1 and len(tb) > 1 and jaccard(ta, tb) >= 0.6


# ------------------------------------------------------------------------------------------------
# Plik z nowymi pytaniami

NEW_COLUMN_ALIASES = {
    "topic": "topic", "temat": "topic", "category": "topic", "section": "topic", "dzial": "topic",
    "difficulty": "difficulty", "level": "difficulty", "poziom": "difficulty",
    "prompt": "prompt", "question": "prompt", "pytanie": "prompt",
    "correct": "correct", "answer": "correct",
    "wrong1": "wrong1", "wrong2": "wrong2", "wrong3": "wrong3",
    "id": "id",
}


def read_new(path: str, sections: list[Section]) -> tuple[list[Question], list[str]]:
    questions: list[Question] = []
    problems: list[str] = []
    header: list[str] | None = None
    sep = "\t"
    name = os.path.basename(path)
    with open(path, encoding="utf-8") as f:
        for lineno, raw in enumerate(f, 1):
            line = raw.rstrip("\r\n")
            if not line.strip() or line.lstrip().startswith("#"):
                continue
            if header is None:
                sep = "\t" if "\t" in line else "|"
                header = [NEW_COLUMN_ALIASES.get(c.strip().lower(), c.strip().lower()) for c in line.split(sep)]
                missing = {"topic", "difficulty", "prompt", "correct", "wrong1", "wrong2", "wrong3"} - set(header)
                if missing:
                    sys.exit(f"{path}:{lineno}: w naglowku brakuje kolumn {sorted(missing)}")
                continue
            cells = [c.strip() for c in line.split(sep)]
            where = f"{name}:{lineno}"
            if len(cells) != len(header):
                problems.append(f"{where}: {len(cells)} kolumn zamiast {len(header)} - linia pominieta")
                continue
            row = dict(zip(header, cells))
            target, problem = resolve_target(row["topic"], sections)
            level = LEVEL_BY_ALIAS.get(row["difficulty"].lower())
            if target is None:
                problems.append(f"{where}: {problem}")
                continue
            if level is None:
                problems.append(f"{where}: nieznany poziom {row['difficulty']!r} (Easy/Medium/Hard/Expert albo E/M/H/X)")
                continue
            questions.append(
                Question(target.topic, level, row.get("id", ""), row["prompt"], row["correct"],
                         [row["wrong1"], row["wrong2"], row["wrong3"]], where,
                         target.section.path if target.section else None)
            )
    return questions, problems


# ------------------------------------------------------------------------------------------------
# Sprawdzanie

DIACRITIC_FREE_OK = set("²³°×±·–—’‘“”…")


@dataclass
class Report:
    errors: dict[int, list[str]] = field(default_factory=lambda: defaultdict(list))
    warnings: dict[int, list[str]] = field(default_factory=lambda: defaultdict(list))


def _short(q: Question) -> str:
    return f"[{q.place}/{LETTER[q.level]} {q.where or q.id}] '{q.prompt}' -> {q.correct}"


def check_questions(new: list[Question], bank: Bank, rules: Rules) -> Report:
    report = Report()
    stop = rules.stop
    existing = bank.choice
    ex_kw = [keywords(q.prompt, stop) for q in existing]
    ex_by_answer: dict[str, list[int]] = defaultdict(list)
    postings: dict[str, list[int]] = defaultdict(list)
    for i, q in enumerate(existing):
        ex_by_answer[normalized_answer(q.correct)].append(i)
        for w in ex_kw[i]:
            postings[w].append(i)
    ex_prompts = {q.prompt.strip().lower(): q for q in existing}
    taken_ids = bank.all_ids()
    others_kw = [(o, keywords(o.text, stop)) for o in bank.others]
    ex_prompt_lower = [q.prompt.lower() for q in existing]

    seen_prompts: dict[str, int] = {}
    seen_ids: dict[str, int] = {}
    new_kw = [keywords(q.prompt, stop) for q in new]

    for i, q in enumerate(new):
        err, warn = report.errors[i], report.warnings[i]
        cells = [q.prompt, q.correct] + q.wrong
        if any(not c for c in cells):
            err.append("puste pole")
        for c in cells:
            if "\t" in c:
                err.append("tabulator w tresci")
        if len({normalized_answer(a) for a in q.answers}) != 4:
            err.append("odpowiedzi sie powtarzaja - potrzebna jedna poprawna i trzy ROZNE bledne")
        key = q.prompt.strip().lower()
        if key in ex_prompts:
            err.append(f"identyczna tresc juz jest w banku: {_short(ex_prompts[key])}")
        if key in seen_prompts:
            err.append(f"identyczna tresc jak w tym pliku: {_short(new[seen_prompts[key]])}")
        seen_prompts.setdefault(key, i)
        if q.id:
            if q.id in taken_ids:
                err.append(f"id {q.id} juz istnieje")
            if q.id in seen_ids:
                err.append(f"id {q.id} powtarza sie w tym pliku")
            seen_ids.setdefault(q.id, i)
        longest_wrong = max(len(w) for w in q.wrong)
        if len(q.correct) - longest_wrong >= rules.length_giveaway:
            err.append(
                f"poprawna odpowiedz dluzsza o {len(q.correct) - longest_wrong} znakow od najdluzszej blednej "
                f"(limit testu: {rules.length_giveaway}) - zdradza sie dlugoscia"
            )
        elif len(q.correct) - longest_wrong >= 10:
            warn.append(f"poprawna odpowiedz wyraznie najdluzsza (+{len(q.correct) - longest_wrong} znakow)")
        if q.topic.key != "poland":
            text = q.prompt + " " + q.correct
            for marker in rules.poland_only:
                if re.search(r"\b" + re.escape(marker), text, re.I):
                    err.append(f"tresc tylko polska ('{marker}') poza tematem poland")

        # Ten sam fakt: ta sama odpowiedz + podobne slowa kluczowe (regula z QuizBankAuditTest).
        answer = normalized_answer(q.correct)
        for j in ex_by_answer.get(answer, []):
            sim = jaccard(new_kw[i], ex_kw[j])
            if sim >= rules.duplicate_threshold:
                err.append(f"ten sam fakt co {_short(existing[j])} (podobienstwo {sim:.2f})")
        for j in range(i):
            other = normalized_answer(new[j].correct)
            sim = jaccard(new_kw[i], new_kw[j])
            if other == answer and sim >= rules.duplicate_threshold:
                err.append(f"ten sam fakt co w tym pliku: {_short(new[j])} ({sim:.2f})")
            elif answers_match(answer, other) and sim >= 0.25 and len(new_kw[i] & new_kw[j]) >= 2:
                warn.append(f"ta sama odpowiedz co w tym pliku ({sim:.2f}): {_short(new[j])}")

        # Uwagi licza sie tylko dla pytan, ktore maja z nowym co najmniej dwa wspolne slowa kluczowe -
        # jedno wspolne slowo ("country", "river") to szablon pytania, a nie ten sam temat.
        candidates = Counter(j for w in new_kw[i] for j in postings.get(w, []))
        related = [j for j, shared in candidates.items() if shared >= 2]
        prompt_lower = q.prompt.lower()
        for j in related:
            sim = jaccard(new_kw[i], ex_kw[j])
            other = normalized_answer(existing[j].correct)
            if answers_match(answer, other):
                if other != answer and sim >= rules.duplicate_threshold:
                    warn.append(f"prawie ta sama odpowiedz i to samo pytanie ({sim:.2f}): {_short(existing[j])}")
                elif other != answer or sim < rules.duplicate_threshold:
                    if sim >= 0.25:
                        warn.append(f"ta sama odpowiedz i podobne pytanie ({sim:.2f}): {_short(existing[j])}")
            elif sim >= 0.75:
                # Prawie identyczna tresc i INNA odpowiedz: przeformulowany dubel albo sprzecznosc.
                warn.append(f"prawie ta sama tresc, inna odpowiedz ({sim:.2f}): {_short(existing[j])}")
            # Odpowiedz zdradzona przez inne pytanie (w ta albo w druga strone).
            if existing[j].topic == q.topic:
                if is_specific(answer, 5) and contains_phrase(ex_prompt_lower[j], answer):
                    warn.append(f"odpowiedz pada w tresci innego pytania: {_short(existing[j])}")
                if is_specific(other, 6) and contains_phrase(prompt_lower, other):
                    warn.append(f"tresc zdradza odpowiedz innego pytania: {_short(existing[j])}")

        # Pytania innych typow - jeden fakt to jedno pytanie, takze miedzy typami.
        full_kw = keywords(q.prompt + " " + q.correct, stop)
        for o, okw in others_kw:
            sim = jaccard(full_kw, okw)
            if sim >= 0.34 and len(full_kw & okw) >= 2:
                warn.append(f"podobne do pytania typu {o.kind} ({sim:.2f}): [{o.id}] {o.text}")

        # Styl banku.
        if not q.prompt.rstrip("'\"").endswith(("?", "...")):
            warn.append("tresc nie konczy sie znakiem zapytania")
        for c in "".join(cells):
            if ord(c) > 127 and c not in DIACRITIC_FREE_OK and unicodedata.category(c).startswith("L"):
                warn.append(f"znak spoza ASCII '{c}' - bank pisze nazwy bez diakrytykow (Krakow, Pele)")
                break
        if '"' in "".join(cells):
            warn.append("cudzyslow \" w tresci - bank uzywa pojedynczych apostrofow")

    # Uporzadkuj: bez powtorzen, w kolejnosci wystapienia.
    for bucket in (report.errors, report.warnings):
        for i in list(bucket):
            bucket[i] = list(dict.fromkeys(bucket[i]))
            if not bucket[i]:
                del bucket[i]
    return report


def print_report(new: list[Question], report: Report, problems: list[str], show_ok: bool = False) -> None:
    for p in problems:
        print("BLAD  " + p)
    for i, q in enumerate(new):
        errs, warns = report.errors.get(i, []), report.warnings.get(i, [])
        if not errs and not warns and not show_ok:
            continue
        print(f"\n{q.where}  {q.place}/{q.level}  '{q.prompt}' -> {q.correct}")
        for e in errs:
            print(f"  BLAD   {e}")
        for w in warns:
            print(f"  uwaga  {w}")
    n_err = sum(1 for i in range(len(new)) if report.errors.get(i))
    n_warn = sum(1 for i in range(len(new)) if report.warnings.get(i))
    print(
        f"\n{len(new)} pytan: {n_err} z bledami, {n_warn} z uwagami"
        + (f", {len(problems)} linii nieprzeczytanych" if problems else "")
        + ". Bledy blokuja `add`; uwagi przejrzyj okiem."
    )


# ------------------------------------------------------------------------------------------------
# Nadawanie id i dopisywanie do TSV

ID_STOP = {
    "the", "and", "for", "with", "from", "which", "what", "who", "whom", "whose", "how", "many", "much",
    "does", "did", "this", "these", "those", "that", "was", "were", "are", "its", "his", "her", "their",
    "into", "onto", "about", "called", "known", "famous", "name", "named", "first", "most", "one", "has",
    "have", "had", "can", "could", "would", "will", "you", "your", "our", "after", "before", "during",
    "where", "when", "why", "than", "then", "there", "they", "them", "been", "being", "also", "only",
}


def ascii_fold(s: str) -> str:
    return unicodedata.normalize("NFKD", s).encode("ascii", "ignore").decode("ascii")


def make_id(q: Question, taken: set[str]) -> str:
    words = [w for w in re.findall(r"[a-z0-9]+", ascii_fold(q.prompt).lower()) if len(w) > 2 and w not in ID_STOP]
    base = f"{q.topic.prefix}_" + ("_".join(words[:3]) or "q")
    candidate, n = base, 2
    while candidate in taken:
        candidate, n = f"{base}_{n}", n + 1
    taken.add(candidate)
    return candidate


def insert_into_tsv(path: str, rows: list[Question]) -> None:
    """Wstawia kazde pytanie na koniec bloku jego poziomu, wiec plik zostaje ulozony Easy -> Expert.
    Nowy plik dzialu dostaje sam naglowek kolumn."""
    lines = []
    if os.path.exists(path):
        with open(path, encoding="utf-8") as f:
            lines = f.read().rstrip("\n").split("\n")
    if not any(line and not line.startswith("#") for line in lines):
        lines.append("\t".join(COLUMNS))
    order = {level: k for k, level in enumerate(LEVELS)}
    for q in rows:
        header_at, insert_at = None, None
        for idx, line in enumerate(lines):
            if not line.strip() or line.startswith("#"):
                continue
            if header_at is None:
                header_at = idx
                insert_at = idx
                continue
            level = LEVEL_BY_ALIAS.get(line.split("\t")[1].lower(), "Expert")
            if order[level] <= order[q.level]:
                insert_at = idx
        lines.insert(insert_at + 1, q.tsv_line())
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")


def remove_from_tsv(path: str, ids: set[str]) -> None:
    with open(path, encoding="utf-8") as f:
        lines = f.read().rstrip("\n").split("\n")
    kept = [line for line in lines if line.startswith("#") or line.split("\t")[0] not in ids]
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(kept) + "\n")


# ------------------------------------------------------------------------------------------------
# Komendy


def cmd_generate(args) -> int:
    sections = checked_sections()
    outputs: list[tuple[str, str]] = []
    for topic in TOPICS:
        data = read_topic(topic, sections)
        outputs.append((topic.kt, render_kotlin(data, sections)))
    outputs.append((CATALOG_KT, render_catalog(sections)))
    outputs.append((COUNTRIES_KT, render_countries(read_countries())))
    stale = []
    for path, content in outputs:
        current = open(path, encoding="utf-8").read() if os.path.exists(path) else ""
        if current == content:
            continue
        stale.append(path)
        if not args.check:
            with open(path, "w", encoding="utf-8") as f:
                f.write(content)
    if args.check:
        if stale:
            print("Nieaktualne pliki Kotlin (uruchom `python3 tools/quizbank.py generate`):")
            for path in stale:
                print("  " + os.path.relpath(path, ROOT))
            return 1
        print("Pliki Kotlin sa zgodne z TSV.")
        return 0
    print(f"Wygenerowano {len(stale)} plikow Kotlin." if stale else "Nic do zrobienia - Kotlin zgodny z TSV.")
    return 0


def _level_counts(rows: list[Question]) -> str:
    counts = Counter(q.level for q in rows)
    return " ".join(f"{counts[level]:7d}" for level in LEVELS) + f"   {len(rows):5d}"


def cmd_stats(args) -> int:
    bank = load_bank()
    counts = Counter((q.topic.key, q.level) for q in bank.choice)
    print(f"{'temat':12s} " + " ".join(f"{level:>7s}" for level in LEVELS) + "   razem")
    for topic in TOPICS:
        row = [counts[(topic.key, level)] for level in LEVELS]
        print(f"{topic.key:12s} " + " ".join(f"{n:7d}" for n in row) + f"   {sum(row):5d}")
    totals = [sum(counts[(t.key, level)] for t in TOPICS) for level in LEVELS]
    print(f"{'RAZEM':12s} " + " ".join(f"{n:7d}" for n in totals) + f"   {sum(totals):5d}")

    for topic in TOPICS:
        if not topic.split:
            continue
        print(f"\nDzialy tematu {topic.key}:")
        width = max(len("  " * (s.depth - 1) + s.path.split("/")[-1]) for s in bank.sections if s.topic == topic) + 2
        print(f"{'':{width}s} " + " ".join(f"{level:>7s}" for level in LEVELS) + "   razem")
        for s in bank.sections:
            if s.topic != topic:
                continue
            rows = [q for q in bank.choice if under(q.section, s.path)]
            name = "  " * (s.depth - 1) + s.path.split("/")[-1]
            print(f"{name:{width}s} " + _level_counts(rows) + f"   {s.label}")

    kinds = Counter(o.kind for o in bank.others)
    print("\nInne typy (w Kotlinie): " + ", ".join(f"{k}: {n}" for k, n in sorted(kinds.items())))
    print("Flagi losuja sie w FlagQuestions.kt i nie sa w TSV.")
    return 0


def _matches_place(q: Question, name: str, sections: list[Section]) -> bool | None:
    """Czy pytanie lezy w temacie/dziale `name`; None, gdy nazwa nic nie znaczy."""
    if name.lower() == "all":
        return True
    clean = name.strip().strip("/").lower()
    branch = [s.path for s in sections if s.path == clean or s.path.endswith("/" + clean)]
    if len(branch) == 1:
        return under(q.section, branch[0])
    topic = topic_named(clean)
    if topic is None:
        return None
    return q.topic == topic


def cmd_list(args) -> int:
    bank = load_bank()
    if bank.choice and _matches_place(bank.choice[0], args.topic, bank.sections) is None:
        print(f"Nieznany temat albo dzial {args.topic!r}. Znane tematy: all, " + ", ".join(t.key for t in TOPICS)
              + " (dzialy: `stats`)")
        return 1
    level = LEVEL_BY_ALIAS.get(args.difficulty.lower()) if args.difficulty else None
    pattern = re.compile(args.grep, re.I) if args.grep else None
    picked = [q for q in bank.choice if _matches_place(q, args.topic, bank.sections) and not (level and q.level != level)]
    wide = len({q.place for q in picked}) > 1
    for q in picked:
        line = f"{LETTER[q.level]} | {q.prompt} -> {q.correct}"
        if args.wrong:
            line += "  (" + " / ".join(q.wrong) + ")"
        if wide:
            line = f"{q.place:40s} " + line
        if pattern and not pattern.search(line + " " + " ".join(q.wrong)):
            continue
        print(line)
    return 0


def cmd_find(args) -> int:
    bank = load_bank()
    pattern = re.compile(args.pattern, re.I)
    hits = 0
    for q in bank.choice:
        text = f"{q.prompt} -> {q.correct}  ({' / '.join(q.wrong)})"
        if pattern.search(text):
            print(f"wybor      {q.place}/{LETTER[q.level]} {q.id}: {text}")
            hits += 1
    for o in bank.others:
        if pattern.search(o.text):
            print(f"{o.kind:10s} {o.id}: {o.text}")
            hits += 1
    print(f"-- {hits} trafien")
    return 0


def _load_new(paths: list[str], sections: list[Section]) -> tuple[list[Question], list[str]]:
    new: list[Question] = []
    problems: list[str] = []
    for path in paths:
        q, p = read_new(path, sections)
        new += q
        problems += p
    return new, problems


def cmd_check(args) -> int:
    bank = load_bank()
    new, problems = _load_new(args.files, bank.sections)
    report = check_questions(new, bank, load_rules())
    print_report(new, report, problems, show_ok=args.all)
    if args.summary:
        print(f"\n{'dzial':50s} " + " ".join(f"{level:>6s}" for level in LEVELS) + "   razem")
        for place in sorted({q.place for q in new}):
            rows = [q for q in new if q.place == place]
            levels = Counter(q.level for q in rows)
            print(f"{place:50s} " + " ".join(f"{levels[level]:6d}" for level in LEVELS) + f"   {len(rows):5d}")
    return 1 if problems or any(report.errors.values()) else 0


def cmd_add(args) -> int:
    bank = load_bank()
    new, problems = _load_new(args.files, bank.sections)
    report = check_questions(new, bank, load_rules())
    blocked = problems or any(report.errors.values())
    if blocked and not args.force:
        print_report(new, report, problems)
        print("\nNic nie zapisano - popraw bledy (albo --force, jesli to swiadoma decyzja).")
        return 1
    if args.verbose:
        print_report(new, report, problems)
    taken = bank.all_ids()
    by_file: dict[str, list[Question]] = defaultdict(list)
    for i, q in enumerate(new):
        if report.errors.get(i) and not args.force:
            continue
        q.id = q.id or make_id(q, taken)
        taken.add(q.id)
        target, _ = resolve_target(q.place, bank.sections)
        by_file[target.tsv].append(q)
    for path, rows in by_file.items():
        insert_into_tsv(path, rows)
    # Kotlin najpierw: przerwany wydruk (np. `add ... | head`) nie moze zostawic TSV bez wygenerowanego kodu.
    result = cmd_generate(argparse.Namespace(check=False))
    added = [q for rows in by_file.values() for q in rows]
    places = Counter(q.place for q in added)
    levels = Counter(q.level for q in added)
    print(f"Dopisano {len(added)} pytan: " + ", ".join(f"{p}: {n}" for p, n in sorted(places.items())))
    print("Poziomy: " + ", ".join(f"{level}: {levels[level]}" for level in LEVELS))
    n_warn = sum(1 for i in range(len(new)) if report.warnings.get(i))
    if n_warn and not args.verbose:
        print(f"({n_warn} pytan mialo uwagi - `check` pokaze je przed dopisaniem)")
    return result


def cmd_move(args) -> int:
    bank = load_bank()
    target, problem = resolve_target(args.to, bank.sections)
    if target is None:
        print(problem)
        return 1
    by_id = {q.id: q for q in bank.choice}
    missing = [i for i in args.ids if i not in by_id]
    if missing:
        print("Nie ma pytan o id: " + ", ".join(missing))
        return 1
    moving = [by_id[i] for i in dict.fromkeys(args.ids) if by_id[i].place != target.name]
    sources: dict[str, set[str]] = defaultdict(set)
    for q in moving:
        source, _ = resolve_target(q.place, bank.sections)
        sources[source.tsv].add(q.id)
    for path, ids in sources.items():
        remove_from_tsv(path, ids)
    for q in moving:
        q.topic, q.section = target.topic, target.section.path if target.section else None
    insert_into_tsv(target.tsv, moving)
    result = cmd_generate(argparse.Namespace(check=False))
    print(f"Przeniesiono {len(moving)} pytan do {target.name}.")
    return result


# ------------------------------------------------------------------------------------------------
# Plan tury: N nowych pytan rozdzielonych na dzialy i poziomy + zlecenia dla autorow

WRITING_GUIDE = os.path.join(DATA_DIR, "WRITING_GUIDE.md")
DEFAULT_MIX = "E=18,M=37,H=30,X=15"
BATCH_HEADER = "topic | difficulty | prompt | correct | wrong1 | wrong2 | wrong3"


@dataclass
class Place:
    """Miejsce, do ktorego trafiaja nowe pytania: lisc drzewa albo temat bez dzialow."""

    target: Target
    comments: list[str]
    rows: list[Question]
    quota: int = 0
    levels: dict[str, int] = field(default_factory=dict)

    @property
    def label(self) -> str:
        return self.target.section.label if self.target.section else self.target.topic.key


def split_evenly(total: int, weights: list[float]) -> list[int]:
    """Rozdziela `total` proporcjonalnie do wag metoda najwiekszych reszt (suma zawsze = total)."""
    if total <= 0 or not weights:
        return [0] * len(weights)
    s = sum(weights)
    if s <= 0:
        weights, s = [1.0] * len(weights), float(len(weights))
    exact = [total * w / s for w in weights]
    parts = [int(x) for x in exact]
    for i in sorted(range(len(exact)), key=lambda i: parts[i] - exact[i])[: total - sum(parts)]:
        parts[i] += 1
    return parts


def parse_mix(text: str) -> dict[str, float]:
    mix: dict[str, float] = {}
    for part in text.split(","):
        key, _, value = part.partition("=")
        level = LEVEL_BY_ALIAS.get(key.strip().lower())
        if level is None or not value.strip().replace(".", "").isdigit():
            sys.exit(f"zly --mix {text!r} - przyklad: {DEFAULT_MIX}")
        mix[level] = float(value)
    return {level: mix.get(level, 0.0) for level in LEVELS}


def places_for(name: str, sections: list[Section]) -> list[Place]:
    """Temat, galaz albo lisc -> liscie (albo sam temat bez dzialow), w kolejnosci z sections.tsv."""
    clean = name.strip().strip("/").lower()
    topic = topic_named(clean)
    if topic is not None and not topic.split:
        comments, rows = read_tsv(topic.tsv, topic, None)
        return [Place(Target(topic), comments, rows)]
    if topic is not None:
        prefix = topic.key
    else:
        matches = [s for s in sections if s.path == clean] or [s for s in sections if s.path.endswith("/" + clean)]
        if len(matches) != 1:
            sys.exit(f"nieznany albo niejednoznaczny temat/dzial {name!r} (lista: `stats`)")
        prefix = matches[0].path
    places = []
    for s in sections:
        if s.is_leaf and under(s.path, prefix):
            comments, rows = read_tsv(s.tsv, s.topic, s.path)
            places.append(Place(Target(s.topic, s), comments, rows))
    return places


def plan_quotas(places: list[Place], total: int, mix: dict[str, float]) -> None:
    """Polowa puli po rowno, polowa dla najmniejszych dzialow (wyrownywanie). Poziomy: polowa wedlug
    docelowych proporcji, polowa tam, gdzie dzialowi najbardziej brakuje do tych proporcji."""
    even = split_evenly(total - total // 2, [1.0] * len(places))
    filling = [0] * len(places)
    # Zaden dzial nie dostaje wiecej niz dwie srednie - zeby maly dzial nie musial wyciskac 60 faktow naraz.
    cap = max(2, -(-2 * total // len(places)))
    for _ in range(total // 2):
        open_places = [k for k in range(len(places)) if even[k] + filling[k] < cap] or list(range(len(places)))
        i = min(open_places, key=lambda k: (len(places[k].rows) + filling[k] + even[k], k))
        filling[i] += 1
    share = sum(mix.values())
    for place, a, b in zip(places, even, filling):
        place.quota = a + b
        have = Counter(q.level for q in place.rows)
        final = len(place.rows) + place.quota
        deficit = [max(0.0, mix[level] / share * final - have[level]) for level in LEVELS]
        d_sum = sum(deficit) or 1.0
        weights = [mix[level] / share + d / d_sum for level, d in zip(LEVELS, deficit)]
        place.levels = dict(zip(LEVELS, split_evenly(place.quota, weights)))


def write_brief(path: str, number: int, batch: list[Place], sections: list[Section], out_file: str) -> None:
    n = sum(p.quota for p in batch)
    lines = [
        f"# Zlecenie {number:02d}: {n} nowych pytan",
        "",
        f"Przeczytaj najpierw zasady: `{os.path.relpath(WRITING_GUIDE, ROOT)}`.",
        f"Wynik zapisz do: `{out_file}` - pierwsza linia to naglowek:",
        "",
        "    " + BATCH_HEADER,
        "",
        f"Sprawdzaj: `python3 tools/quizbank.py check {out_file}` - az do `0 z bledami`.",
        "Repo niczego nie zmieniasz (zadnego `add`, `move`, `generate`).",
        "",
        "Ile pytan, w ktorym dziale i na jakim poziomie (o 1-2 w gore lub w dol mozna, suma ma sie zgadzac):",
        "",
        "| dzial (kolumna topic) | Easy | Medium | Hard | Expert | razem | juz w banku |",
        "|---|---|---|---|---|---|---|",
    ]
    for p in batch:
        lv = " | ".join(str(p.levels[level]) for level in LEVELS)
        lines.append(f"| `{p.target.name}` | {lv} | {p.quota} | {len(p.rows)} |")
    for p in batch:
        lines += ["", f"## `{p.target.name}` - {p.label}", ""]
        if p.comments:
            lines += ["Zakres dzialu: " + " ".join(c.strip() for c in p.comments), ""]
        if p.target.section:
            siblings = [s.path.split("/")[-1] for s in sections
                        if s.is_leaf and s.parent == p.target.section.parent and s != p.target.section
                        and s.topic == p.target.topic]
            if siblings:
                lines += ["Sasiednie dzialy (ich tematy pisz tam, nie tutaj): " + ", ".join(siblings), ""]
        lines.append(f"Juz w banku ({len(p.rows)}) - tych faktow nie powtarzaj, takze innymi slowami:")
        lines.append("")
        lines += [f"- {LETTER[q.level]} {q.prompt} -> {q.correct}" for q in p.rows]
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")


def cmd_plan(args) -> int:
    sections = checked_sections()
    mix = parse_mix(args.mix)
    places: list[Place] = []
    for spec in args.targets:
        name, _, count = spec.partition("=")
        if not count.isdigit():
            sys.exit(f"zly cel {spec!r} - podaj temat_albo_dzial=liczba, np. history=500 vikings=20")
        chosen = places_for(name, sections)
        plan_quotas(chosen, int(count), mix)
        places += [p for p in chosen if p.quota]
    total = sum(p.quota for p in places)
    batches: list[list[Place]] = [[]]
    for p in places:
        if batches[-1] and sum(q.quota for q in batches[-1]) + p.quota / 2 > args.batch_size:
            batches.append([])
        batches[-1].append(p)

    out = os.path.abspath(args.out)
    os.makedirs(out, exist_ok=True)
    stale = glob.glob(os.path.join(out, "brief_*.md"))
    for path in stale:
        os.remove(path)
    print(f"{'dzial':50s} " + " ".join(f"{level:>6s}" for level in LEVELS) + "   razem  w banku")
    for number, batch in enumerate(batches, 1):
        brief = os.path.join(out, f"brief_{number:02d}.md")
        result = os.path.join(out, f"batch_{number:02d}.txt")
        write_brief(brief, number, batch, sections, result)
        print(f"-- zlecenie {number:02d}: {sum(p.quota for p in batch)} pytan -> {brief}")
        for p in batch:
            print(f"   {p.target.name:47s} " + " ".join(f"{p.levels[level]:6d}" for level in LEVELS)
                  + f"   {p.quota:5d}  {len(p.rows):7d}")
    levels = Counter()
    for p in places:
        levels.update(p.levels)
    print(f"\nRazem {total} pytan w {len(batches)} zleceniach: "
          + ", ".join(f"{level} {levels[level]}" for level in LEVELS))
    print(f"Zlecenia: {out}/brief_NN.md, wyniki autorow: {out}/batch_NN.txt\n"
          f"Potem: python3 tools/quizbank.py check {out}/batch_*.txt && python3 tools/quizbank.py add {out}/batch_*.txt")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="command", required=True)

    sub.add_parser("stats", help="ile pytan w kazdym temacie, dziale i na kazdym poziomie")

    p = sub.add_parser("list", help="podglad pytan tematu albo dzialu (albo 'all')")
    p.add_argument("topic", help="temat (biology), dzial (history/antiquity, roman_empire) albo all")
    p.add_argument("-d", "--difficulty", help="E/M/H/X albo Easy/Medium/Hard/Expert")
    p.add_argument("-g", "--grep", help="tylko linie pasujace do wyrazenia regularnego")
    p.add_argument("-w", "--wrong", action="store_true", help="pokaz tez bledne odpowiedzi")

    p = sub.add_parser("find", help="szukaj w calym banku (wszystkie typy pytan)")
    p.add_argument("pattern", help="wyrazenie regularne, bez rozrozniania wielkosci liter")

    p = sub.add_parser("check", help="sprawdz plik(i) z nowymi pytaniami - niczego nie zapisuje")
    p.add_argument("files", nargs="+")
    p.add_argument("--all", action="store_true", help="wypisz tez pytania bez uwag")
    p.add_argument("-s", "--summary", action="store_true", help="na koncu: ile nowych pytan w kazdym dziale i poziomie")

    p = sub.add_parser("add", help="check + dopisz nowe pytania do TSV + wygeneruj Kotlin")
    p.add_argument("files", nargs="+")
    p.add_argument("--force", action="store_true", help="dopisz mimo bledow")
    p.add_argument("-v", "--verbose", action="store_true", help="wypisz tez uwagi")

    p = sub.add_parser("move", help="przenies pytania (po id) do innego dzialu albo tematu")
    p.add_argument("ids", nargs="+")
    p.add_argument("--to", required=True, help="dzial docelowy (history/antiquity/roman_empire) albo temat")

    p = sub.add_parser("plan", help="rozdziel N nowych pytan na dzialy i poziomy, napisz zlecenia dla autorow")
    p.add_argument("targets", nargs="+", help="temat, galaz albo dzial z liczba: history=500 geography/physical=200")
    p.add_argument("-o", "--out", default=os.path.join(tempfile.gettempdir(), "quizbank-plan"),
                   help="katalog na zlecenia (brief_NN.md) i wyniki autorow (batch_NN.txt)")
    p.add_argument("-b", "--batch-size", type=int, default=100, help="ile pytan na jedno zlecenie (domyslnie 100)")
    p.add_argument("--mix", default=DEFAULT_MIX, help=f"docelowe proporcje poziomow (domyslnie {DEFAULT_MIX})")

    p = sub.add_parser("generate", help="TSV -> pliki Kotlin")
    p.add_argument("--check", action="store_true", help="tylko sprawdz, czy Kotlin jest aktualny")

    args = parser.parse_args()
    handlers = {
        "stats": cmd_stats, "list": cmd_list, "find": cmd_find, "check": cmd_check,
        "add": cmd_add, "move": cmd_move, "plan": cmd_plan, "generate": cmd_generate,
    }
    return handlers[args.command](args)


if __name__ == "__main__":
    # `stats | head` ma sie po prostu urwac, a nie konczyc stosem wyjatkow BrokenPipeError.
    if hasattr(signal, "SIGPIPE"):
        signal.signal(signal.SIGPIPE, signal.SIG_DFL)
    sys.exit(main())
