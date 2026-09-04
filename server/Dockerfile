# Wieloetapowy build: Gradle w kontenerze buildera, a do produkcyjnego obrazu trafia tylko
# gotowa dystrybucja (executable() z jvm{} w server/build.gradle.kts) - zaden kod zrodlowy ani
# Gradle nie leci na Cloud Run.
#
# UWAGA: mimo ze serwer sam nie dotyka Androida, ":domain" (od ktorego :server zalezy) to modul
# KMP z targetem Android, wiec sama KONFIGURACJA calego builda (settings.gradle.kts wciaga
# wszystkie moduly, w tym :composeApp) wymaga Android SDK w obrazie buildera - std. bez tego
# `./gradlew` nie ruszy nawet dla samego `:server:installJvmDist`.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=$ANDROID_HOME
RUN apt-get update && apt-get install -y --no-install-recommends unzip && rm -rf /var/lib/apt/lists/*
RUN mkdir -p $ANDROID_HOME/cmdline-tools && \
    curl -sSL -o /tmp/cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip && \
    unzip -q /tmp/cmdline-tools.zip -d $ANDROID_HOME/cmdline-tools && \
    mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
RUN yes | sdkmanager --licenses > /dev/null && \
    sdkmanager "platforms;android-36" "build-tools;35.0.0"

# Warstwy cache'owane osobno od zrodel - `./gradlew --version` samo sciaga wrapper i nic wiecej.
COPY gradlew gradlew.bat ./
COPY gradle gradle
RUN ./gradlew --version

# Cala reszta repo - settings.gradle.kts wciaga wszystkie moduly (patrz uwaga wyzej), wiec
# konfiguracja builda i tak ich potrzebuje na dysku, nawet gdy budujemy tylko :server.
COPY . .
RUN ./gradlew :server:installJvmDist --console=plain --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/server/build/install/server-jvm /app

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["/app/bin/server"]
