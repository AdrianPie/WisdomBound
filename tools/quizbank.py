#!/usr/bin/env python3
"""
Baza pytan quizu LernHero: podglad, sprawdzanie, dopisywanie i generowanie kodu.

Pytania wyboru (jedna poprawna + trzy bledne odpowiedzi) mieszkaja w domain/quizbank/<temat>.tsv:
jedno pytanie = jedna linia, kolumny rozdzielone tabulatorem. To jest ZRODLO PRAWDY. Pliki
<Temat>Questions.kt w domain/src/commonMain/.../quiz/bank sa z niego GENEROWANE - recznych zmian
w nich sie nie robi, bo nastepne `generate` je nadpisze (a QuizBankTsvSyncTest wylapie rozjazd).

Typowa sesja dopisywania pytan (szczegoly: domain/quizbank/README.md):

    python3 tools/quizbank.py stats                  # ile pytan w ktorym temacie i na ktorym poziomie
    python3 tools/quizbank.py list history -d E      # podglad tematu (opcjonalnie poziomu / frazy)
    python3 tools/quizbank.py find churchill         # szukaj w CALYM banku, wszystkie typy pytan
    python3 tools/quizbank.py check nowe.txt         # sprawdz nowe pytania - niczego nie zapisuje
    python3 tools/quizbank.py add nowe.txt           # check + dopisz do TSV + wygeneruj Kotlin
    python3 tools/quizbank.py generate [--check]     # TSV -> Kotlin (--check: tylko porownaj)

Plik z nowymi pytaniami: naglowek + jedna linia na pytanie, kolumny rozdzielone tabulatorem albo
znakiem '|'. Kolumna id jest opcjonalna - bez niej skrypt nada unikalne id sam.

    topic | difficulty | prompt | correct | wrong1 | wrong2 | wrong3
    history | Easy | Who led the Soviet Union during World War II? | Joseph Stalin | Vladimir Lenin | ...

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
import sys
import unicodedata
from collections import Counter, defaultdict
from dataclasses import dataclass, field

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_DIR = os.path.join(ROOT, "domain", "quizbank")
KOTLIN_DIR = os.path.join(ROOT, "domain", "src", "commonMain", "kotlin", "com", "lernhero", "domain", "quiz", "bank")
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


@dataclass(frozen=True)
class Topic:
    key: str  # nazwa pliku TSV i nazwa tematu w plikach z nowymi pytaniami
    obj: str  # generowany obiekt Kotlin
    category: str  # QuizCategory
    prefix: str  # prefiks id nadawanych nowym pytaniom

    @property
    def tsv(self) -> str:
        return os.path.join(DATA_DIR, self.key + ".tsv")

    @property
    def kt(self) -> str:
        return os.path.join(KOTLIN_DIR, self.obj + ".kt")


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


def topic_named(name: str) -> Topic | None:
    return TOPIC_BY_NAME.get(_name_key(name))


# ------------------------------------------------------------------------------------------------
# Kotlin: escapowanie i parsowanie

STR = r'"((?:[^"\\]|\\.)*)"'
STR_NC = r'"(?:[^"\\]|\\.)*"'
Q_LINE = re.compile(
    r"^\s*q\(\s*" + STR + r"\s*,\s*" + STR + r"\s*,\s*" + STR + r"\s*,\s*listOf\(\s*" + STR + r"\s*,\s*"
    + STR + r"\s*,\s*" + STR + r"\s*\)\s*,\s*([EMHX])\s*\),?\s*$"
)
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

    @property
    def answers(self) -> list[str]:
        return [self.correct] + self.wrong

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
    comments: dict[str, list[str]] = field(default_factory=dict)

    def all_ids(self) -> set[str]:
        return {q.id for q in self.choice} | {o.id for o in self.others}


# ------------------------------------------------------------------------------------------------
# TSV


def read_topic(topic: Topic) -> tuple[list[str], list[Question]]:
    comments: list[str] = []
    rows: list[Question] = []
    header = None
    with open(topic.tsv, encoding="utf-8") as f:
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
                    sys.exit(f"{topic.tsv}:{lineno}: zly naglowek {header}, oczekiwany {COLUMNS}")
                continue
            if len(cells) != len(COLUMNS):
                sys.exit(f"{topic.tsv}:{lineno}: {len(cells)} kolumn zamiast {len(COLUMNS)}")
            qid, level, prompt, correct, w1, w2, w3 = cells
            level_name = LEVEL_BY_ALIAS.get(level.strip().lower())
            if level_name is None:
                sys.exit(f"{topic.tsv}:{lineno}: nieznany poziom {level!r}")
            rows.append(Question(topic, level_name, qid, prompt, correct, [w1, w2, w3], f"{topic.key}.tsv:{lineno}"))
    return comments, rows


def write_topic(topic: Topic, comments: list[str], rows: list[Question]) -> None:
    os.makedirs(DATA_DIR, exist_ok=True)
    lines = [("# " + c) if c else "#" for c in comments]
    lines.append("\t".join(COLUMNS))
    lines += [q.tsv_line() for q in rows]
    with open(topic.tsv, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")


# ------------------------------------------------------------------------------------------------
# Generowanie Kotlina

SECTION = {
    "Easy": "/* ------------------------------ Easy ------------------------------ */",
    "Medium": "/* ----------------------------- Medium ----------------------------- */",
    "Hard": "/* ------------------------------ Hard ------------------------------ */",
    "Expert": "/* ----------------------------- Expert ----------------------------- */",
}


def render_kotlin(topic: Topic, comments: list[str], rows: list[Question]) -> str:
    out = [
        f"// PLIK GENEROWANY z domain/quizbank/{topic.key}.tsv przez `python3 tools/quizbank.py generate`.",
        "// Nie edytuj go recznie - pytania dopisuje sie do TSV (patrz domain/quizbank/README.md).",
        "package com.lernhero.domain.quiz.bank",
        "",
        "import com.lernhero.domain.quiz.QuizCategory",
        "import com.lernhero.domain.quiz.QuizDifficulty",
        "import com.lernhero.domain.quiz.QuizEntry",
        "",
    ]
    if comments:
        out.append("/**")
        out += [(" * " + c) if c else " *" for c in comments]
        out.append(" */")
    out += [
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
        entries = []
        for q in by_level[level]:
            wrong = ", ".join(kt_string(w) for w in q.wrong)
            entries.append(
                f"        q({kt_string(q.id)}, {kt_string(q.prompt)}, {kt_string(q.correct)}, listOf({wrong}), {LETTER[level]}),"
            )
        entries[-1] = entries[-1][:-1]
        out += entries + ["    )"]
    out += ["}", ""]
    return "\n".join(out)


def parse_kotlin_topic(topic: Topic) -> tuple[list[str], list[Question]]:
    """Czyta recznie pisany (albo wygenerowany) plik Kotlin tematu - uzywane przy migracji."""
    with open(topic.kt, encoding="utf-8") as f:
        text = f.read()
    comments: list[str] = []
    doc = re.search(r"/\*\*(.*?)\*/\s*object\s+" + re.escape(topic.obj) + r"\b", text, re.S)
    if doc:
        for raw in doc.group(1).split("\n"):
            line = raw.strip()
            if line.startswith("*"):
                line = line[1:]
            if line.startswith(" "):
                line = line[1:]
            comments.append(line.rstrip())
        while comments and not comments[0]:
            comments.pop(0)
        while comments and not comments[-1]:
            comments.pop()
    rows: list[Question] = []
    level_by_letter = {v: k for k, v in LETTER.items()}
    for lineno, line in enumerate(text.split("\n"), 1):
        if not re.match(r"^\s*q\(", line):
            continue
        m = Q_LINE.match(line)
        if not m:
            sys.exit(f"{topic.kt}:{lineno}: nie umiem przeczytac linii pytania:\n{line}")
        qid, prompt, correct, w1, w2, w3, letter = m.groups()
        rows.append(
            Question(
                topic,
                level_by_letter[letter],
                kt_unescape(qid),
                kt_unescape(prompt),
                kt_unescape(correct),
                [kt_unescape(w1), kt_unescape(w2), kt_unescape(w3)],
                f"{topic.obj}.kt:{lineno}",
            )
        )
    return comments, rows


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


def load_bank(from_kotlin: bool = False) -> Bank:
    choice: list[Question] = []
    comments: dict[str, list[str]] = {}
    for topic in TOPICS:
        if from_kotlin or not os.path.exists(topic.tsv):
            c, rows = parse_kotlin_topic(topic)
        else:
            c, rows = read_topic(topic)
        comments[topic.key] = c
        choice += rows
    return Bank(choice, load_others(), comments)


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
    "topic": "topic", "temat": "topic", "category": "topic",
    "difficulty": "difficulty", "level": "difficulty", "poziom": "difficulty",
    "prompt": "prompt", "question": "prompt", "pytanie": "prompt",
    "correct": "correct", "answer": "correct",
    "wrong1": "wrong1", "wrong2": "wrong2", "wrong3": "wrong3",
    "id": "id",
}


def read_new(path: str) -> tuple[list[Question], list[str]]:
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
            topic = topic_named(row["topic"])
            level = LEVEL_BY_ALIAS.get(row["difficulty"].lower())
            if topic is None:
                problems.append(f"{where}: nieznany temat {row['topic']!r} (znane: {', '.join(t.key for t in TOPICS)})")
                continue
            if level is None:
                problems.append(f"{where}: nieznany poziom {row['difficulty']!r} (Easy/Medium/Hard/Expert albo E/M/H/X)")
                continue
            questions.append(
                Question(topic, level, row.get("id", ""), row["prompt"], row["correct"],
                         [row["wrong1"], row["wrong2"], row["wrong3"]], where)
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
    return f"[{q.topic.key}/{LETTER[q.level]} {q.where or q.id}] '{q.prompt}' -> {q.correct}"


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
        print(f"\n{q.where}  {q.topic.key}/{q.level}  '{q.prompt}' -> {q.correct}")
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


def insert_into_tsv(topic: Topic, rows: list[Question]) -> None:
    with open(topic.tsv, encoding="utf-8") as f:
        lines = f.read().rstrip("\n").split("\n")
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
    with open(topic.tsv, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")


# ------------------------------------------------------------------------------------------------
# Komendy


def cmd_generate(args) -> int:
    stale = []
    for topic in TOPICS:
        comments, rows = read_topic(topic)
        content = render_kotlin(topic, comments, rows)
        current = open(topic.kt, encoding="utf-8").read() if os.path.exists(topic.kt) else ""
        if current == content:
            continue
        stale.append(topic)
        if not args.check:
            with open(topic.kt, "w", encoding="utf-8") as f:
                f.write(content)
    if args.check:
        if stale:
            print("Nieaktualne pliki Kotlin (uruchom `python3 tools/quizbank.py generate`):")
            for t in stale:
                print("  " + os.path.relpath(t.kt, ROOT))
            return 1
        print("Pliki Kotlin sa zgodne z TSV.")
        return 0
    print(f"Wygenerowano {len(stale)} plikow Kotlin." if stale else "Nic do zrobienia - Kotlin zgodny z TSV.")
    return 0


def cmd_migrate(args) -> int:
    existing = [t for t in TOPICS if os.path.exists(t.tsv)]
    if existing and not args.force:
        print("TSV juz istnieja (" + ", ".join(t.key for t in existing) + "). --force, zeby nadpisac z Kotlina.")
        return 1
    total = 0
    for topic in TOPICS:
        comments, rows = parse_kotlin_topic(topic)
        write_topic(topic, comments, rows)
        total += len(rows)
        print(f"{topic.key:12s} {len(rows):5d} pytan -> {os.path.relpath(topic.tsv, ROOT)}")
    print(f"Razem {total} pytan.")
    return 0


def cmd_stats(args) -> int:
    bank = load_bank()
    counts = Counter((q.topic.key, q.level) for q in bank.choice)
    print(f"{'temat':12s} " + " ".join(f"{level:>7s}" for level in LEVELS) + "   razem")
    for topic in TOPICS:
        row = [counts[(topic.key, level)] for level in LEVELS]
        print(f"{topic.key:12s} " + " ".join(f"{n:7d}" for n in row) + f"   {sum(row):5d}")
    totals = [sum(counts[(t.key, level)] for t in TOPICS) for level in LEVELS]
    print(f"{'RAZEM':12s} " + " ".join(f"{n:7d}" for n in totals) + f"   {sum(totals):5d}")
    kinds = Counter(o.kind for o in bank.others)
    print("\nInne typy (w Kotlinie): " + ", ".join(f"{k}: {n}" for k, n in sorted(kinds.items())))
    print("Flagi losuja sie w FlagQuestions.kt i nie sa w TSV.")
    return 0


def cmd_list(args) -> int:
    bank = load_bank()
    topics = TOPICS if args.topic.lower() == "all" else [topic_named(args.topic)]
    if topics == [None]:
        print(f"Nieznany temat {args.topic!r}. Znane: all, " + ", ".join(t.key for t in TOPICS))
        return 1
    level = LEVEL_BY_ALIAS.get(args.difficulty.lower()) if args.difficulty else None
    pattern = re.compile(args.grep, re.I) if args.grep else None
    for q in bank.choice:
        if q.topic not in topics or (level and q.level != level):
            continue
        line = f"{LETTER[q.level]} | {q.prompt} -> {q.correct}"
        if args.wrong:
            line += "  (" + " / ".join(q.wrong) + ")"
        if len(topics) > 1:
            line = f"{q.topic.key:11s} " + line
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
            print(f"wybor      {q.topic.key}/{LETTER[q.level]} {q.id}: {text}")
            hits += 1
    for o in bank.others:
        if pattern.search(o.text):
            print(f"{o.kind:10s} {o.id}: {o.text}")
            hits += 1
    print(f"-- {hits} trafien")
    return 0


def _load_new(paths: list[str]) -> tuple[list[Question], list[str]]:
    new: list[Question] = []
    problems: list[str] = []
    for path in paths:
        q, p = read_new(path)
        new += q
        problems += p
    return new, problems


def cmd_check(args) -> int:
    new, problems = _load_new(args.files)
    report = check_questions(new, load_bank(), load_rules())
    print_report(new, report, problems, show_ok=args.all)
    return 1 if problems or any(report.errors.values()) else 0


def cmd_add(args) -> int:
    new, problems = _load_new(args.files)
    bank = load_bank()
    report = check_questions(new, bank, load_rules())
    blocked = problems or any(report.errors.values())
    if blocked and not args.force:
        print_report(new, report, problems)
        print("\nNic nie zapisano - popraw bledy (albo --force, jesli to swiadoma decyzja).")
        return 1
    if args.verbose:
        print_report(new, report, problems)
    taken = bank.all_ids()
    by_topic: dict[str, list[Question]] = defaultdict(list)
    for i, q in enumerate(new):
        if report.errors.get(i) and not args.force:
            continue
        q.id = q.id or make_id(q, taken)
        taken.add(q.id)
        by_topic[q.topic.key].append(q)
    for topic in TOPICS:
        if by_topic.get(topic.key):
            insert_into_tsv(topic, by_topic[topic.key])
    added = sum(len(v) for v in by_topic.values())
    levels = Counter(q.level for v in by_topic.values() for q in v)
    print(f"Dopisano {added} pytan: " + ", ".join(f"{t}: {len(v)}" for t, v in sorted(by_topic.items())))
    print("Poziomy: " + ", ".join(f"{level}: {levels[level]}" for level in LEVELS))
    n_warn = sum(1 for i in range(len(new)) if report.warnings.get(i))
    if n_warn and not args.verbose:
        print(f"({n_warn} pytan mialo uwagi - `check` pokaze je przed dopisaniem)")
    return cmd_generate(argparse.Namespace(check=False))


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="command", required=True)

    sub.add_parser("stats", help="ile pytan w kazdym temacie i na kazdym poziomie")

    p = sub.add_parser("list", help="podglad pytan tematu (albo 'all')")
    p.add_argument("topic")
    p.add_argument("-d", "--difficulty", help="E/M/H/X albo Easy/Medium/Hard/Expert")
    p.add_argument("-g", "--grep", help="tylko linie pasujace do wyrazenia regularnego")
    p.add_argument("-w", "--wrong", action="store_true", help="pokaz tez bledne odpowiedzi")

    p = sub.add_parser("find", help="szukaj w calym banku (wszystkie typy pytan)")
    p.add_argument("pattern", help="wyrazenie regularne, bez rozrozniania wielkosci liter")

    p = sub.add_parser("check", help="sprawdz plik(i) z nowymi pytaniami - niczego nie zapisuje")
    p.add_argument("files", nargs="+")
    p.add_argument("--all", action="store_true", help="wypisz tez pytania bez uwag")

    p = sub.add_parser("add", help="check + dopisz nowe pytania do TSV + wygeneruj Kotlin")
    p.add_argument("files", nargs="+")
    p.add_argument("--force", action="store_true", help="dopisz mimo bledow")
    p.add_argument("-v", "--verbose", action="store_true", help="wypisz tez uwagi")

    p = sub.add_parser("generate", help="TSV -> pliki Kotlin")
    p.add_argument("--check", action="store_true", help="tylko sprawdz, czy Kotlin jest aktualny")

    p = sub.add_parser("migrate", help="(jednorazowo) zrob TSV z obecnych plikow Kotlin")
    p.add_argument("--force", action="store_true")

    args = parser.parse_args()
    handlers = {
        "stats": cmd_stats, "list": cmd_list, "find": cmd_find, "check": cmd_check,
        "add": cmd_add, "generate": cmd_generate, "migrate": cmd_migrate,
    }
    return handlers[args.command](args)


if __name__ == "__main__":
    sys.exit(main())
