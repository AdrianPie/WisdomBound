#!/bin/bash
# Testy pakietu quiz (domain/.../quiz) bez Gradle: samodzielny kompilator Kotlina + JUnit.
#
# Normalnie testy puszcza sie przez Gradle:
#     ./gradlew :domain:desktopTest --tests 'com.lernhero.domain.quiz.*'
# Ten skrypt jest na srodowiska, w ktorych Gradle nie dziala (np. brak dostepu do repozytorium
# Google z wtyczka Androida). Potrzebuje tylko javy (17+) i curl - jary kompilatora sciaga raz
# z Maven Central do $QUIZTEST_CACHE (domyslnie ~/.cache/lernhero-quiztest).
#
#     tools/quiztest.sh                       # wszystkie testy quizu
#     tools/quiztest.sh QuizSectionTest       # tylko wybrane klasy testow
set -euo pipefail

ROOT=$(cd "$(dirname "$0")/.." && pwd)
SRC=$ROOT/domain/src
CACHE=${QUIZTEST_CACHE:-$HOME/.cache/lernhero-quiztest}
VERSIONS=$ROOT/gradle/libs.versions.toml
KOTLIN=$(sed -n 's/^kotlin = "\(.*\)"/\1/p' "$VERSIONS")
SERIALIZATION=$(sed -n 's/^serialization = "\(.*\)"/\1/p' "$VERSIONS")
MAVEN=https://repo1.maven.org/maven2

# grupa/artefakt/wersja -> plik w cache
jar() {
    local group=$1 name=$2 version=$3
    local file=$CACHE/$name-$version.jar
    if [ ! -s "$file" ]; then
        mkdir -p "$CACHE"
        echo "Pobieram $name $version" >&2
        curl -fsSL --retry 4 --retry-delay 2 -o "$file.part" "$MAVEN/${group//.//}/$name/$version/$name-$version.jar"
        mv "$file.part" "$file"
    fi
    echo "$file"
}

K=org.jetbrains.kotlin
COMPILER=$(jar $K kotlin-compiler-embeddable "$KOTLIN"):$(jar $K kotlin-stdlib "$KOTLIN"):$(jar $K kotlin-script-runtime "$KOTLIN")
COMPILER=$COMPILER:$(jar $K kotlin-reflect "$KOTLIN"):$(jar $K kotlin-daemon-embeddable "$KOTLIN")
COMPILER=$COMPILER:$(jar org.jetbrains.intellij.deps trove4j 1.0.20200330)
COMPILER=$COMPILER:$(jar org.jetbrains.kotlinx kotlinx-coroutines-core-jvm 1.8.0):$(jar org.jetbrains annotations 13.0)
PLUGIN=$(jar $K kotlin-serialization-compiler-plugin-embeddable "$KOTLIN")
LIBS=$(jar $K kotlin-stdlib "$KOTLIN")
LIBS=$LIBS:$(jar org.jetbrains.kotlinx kotlinx-serialization-core-jvm "$SERIALIZATION")
LIBS=$LIBS:$(jar org.jetbrains.kotlinx kotlinx-serialization-json-jvm "$SERIALIZATION")
TEST_LIBS=$(jar $K kotlin-test "$KOTLIN"):$(jar $K kotlin-test-junit "$KOTLIN")
TEST_LIBS=$TEST_LIBS:$(jar junit junit 4.13.2):$(jar org.hamcrest hamcrest-core 1.3)

OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT
QUIZ=com/lernhero/domain/quiz

kotlinc() {
    java -Xmx3g -cp "$COMPILER" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -no-reflect -jvm-target 17 \
        -nowarn "$@"
}

# Pakiet quiz bez dwoch plikow, ktore siegaja poza niego (oferta kategorii, slowka).
MAIN=$(ls "$SRC"/commonMain/kotlin/$QUIZ/*.kt "$SRC"/commonMain/kotlin/$QUIZ/bank/*.kt \
    | grep -v -e CategoryOffer.kt -e WordQuizBank.kt)
echo "Kompiluje pakiet quiz..." >&2
kotlinc -Xplugin="$PLUGIN" -Xmulti-platform -cp "$LIBS" -d "$OUT/main" $MAIN

TESTS="$(ls "$SRC"/commonTest/kotlin/$QUIZ/*.kt | grep -v WordQuizBankTest) $(ls "$SRC"/desktopTest/kotlin/$QUIZ/*.kt)"
echo "Kompiluje testy..." >&2
kotlinc -cp "$LIBS:$OUT/main:$TEST_LIBS" -d "$OUT/test" $TESTS

if [ $# -gt 0 ]; then
    CLASSES=$(for name in "$@"; do echo "com.lernhero.domain.quiz.$name"; done)
else
    CLASSES=$(cd "$OUT/test" && find . -name '*Test.class' | sed 's|^\./||; s|\.class$||; s|/|.|g' | grep -v '\$')
fi
# Testy czytaja TSV wzgledem katalogu modulu - tak jak Gradle.
cd "$ROOT/domain"
java -cp "$LIBS:$OUT/main:$TEST_LIBS:$OUT/test" org.junit.runner.JUnitCore $CLASSES
