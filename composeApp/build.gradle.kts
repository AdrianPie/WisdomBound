
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.services)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // Reader eksponuje swoj most tlumaczen (patrz feature/reader/.../TranslationSource.ios.kt)
            // do Swift - Apple `Translation` framework jest czystym Swiftem bez naglowka ObjC,
            // wiec Kotlin/Native nie moze go wywolac wprost, tylko odwrotnie: Swift woła w Kotlina.
            export(project(":feature:reader"))
            // Most sklepu i reklam (shared/monetization/Monetization.ios.kt) - ta sama sztuczka:
            // RevenueCat i Google Mobile Ads to Swift z SPM, wiec to Swift woła w Kotlina.
            export(project(":shared"))
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.splash.screen)
            implementation(libs.koin.android)
            implementation(project.dependencies.platform(libs.google.firebase.bom))
            implementation(libs.google.firebase.common)
        }

        named("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.components.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.auth.kmp)
            implementation(libs.firebase.app)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // api, nie implementation - export() w bloku framework wymaga tego dla dzialania.
            api(project(path = ":feature:reader"))

            implementation(project(path = ":navigation"))
            api(project(path = ":shared"))
            implementation(project(path = ":domain"))
            implementation(project(path = ":di"))
            implementation(project(path = ":data"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Wczytanie danych do podpisywania aplikacji z keystore.properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "org.apie.lernhero"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.intelligentape.wisdombound"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 5
        versionName = "1.5"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(
                keystoreProperties["storeFile"] as String
            )
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            // Symbole natywne (libtranslate_jni.so itd.) do Play - czytelne stacktrace crashy
            // z warstwy C++ zamiast surowych adresow.
            ndk { debugSymbolLevel = "FULL" }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
}

compose.desktop {
    application {
        mainClass = "org.apie.lernhero.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "WisdomBound"
            packageVersion = "1.0.0"
        }
    }
}