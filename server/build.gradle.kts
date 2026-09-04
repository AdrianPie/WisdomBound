import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Zwykly modul KMP z jednym targetem "jvm" - a nie plugin kotlin.jvm - bo w tym buildzie
// kotlin.multiplatform jest juz zaladowany przez reszte modulow, a mieszanie tam dwoch
// wejsciowych pluginow Kotlina (multiplatform + jvm) w jednym buildzie wywala sie na etapie
// rozwiazywania pluginow ("already on the classpath with an unknown version"). Ten sam
// plugin co wszedzie indziej = zero konfliktu.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        binaries {
            executable {
                mainClass.set("com.lernhero.server.ApplicationKt")
            }
        }
    }

    sourceSets {
        jvmMain.dependencies {
            // Silnik walidacji/parsowania AI (QuizGenerationPrompt/Parser) i cala reszta symulacji -
            // ten sam kod, ktory dzis dziala w apce, teraz uzywany 1:1 na serwerze. Zero duplikacji.
            implementation(project(":domain"))

            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.server.status.pages)
            implementation(libs.ktor.client.serialization)

            // Klient wychodzacy - stad serwer sam woła dostawce modelu (klucz zyje TYLKO tutaj).
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.content.negotiation)

            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.coroutines.core)

            // Admin SDK - jedyny sposob, w jaki ten serwis dotyka Firestore. Omija Security Rules
            // (celowo - to jest wlasnie ten "zaufany backend", dla ktorego reguly robia wyjatek).
            implementation(libs.firebase.admin)
            implementation(libs.logback.classic)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// :domain zaciaga tez zaleznosci Compose/GitLive (uzywane w apce mobilnej) - dla headless
// serwera to tylko martwy balast w dystrybucji, a przy tym dwie z tych bibliotek trafiaja do
// dist pod tym samym plikiem jar w dwoch roznych wersjach. EXCLUDE = bierzemy pierwsza napotkana,
// co serwerowi nie przeszkadza (nie odwoluje sie do zadnej z tych klas).
tasks.withType<Sync>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.withType<Copy>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
