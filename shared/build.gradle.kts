import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
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
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.splash.screen)
            // Przypomnienia w grze (patrz shared/notification). WorkManager, a nie AlarmManager:
            // od Androida 12 dokladne alarmy wymagaja osobnego uprawnienia, a przypomnienie
            // "magazyn pelny" nie potrzebuje precyzji co do sekundy.
            implementation(libs.androidx.work.runtime)
            implementation(libs.androidx.core.ktx)
        }
        commonMain.dependencies {
            // Przypomnienia biora plan wprost z modelu gracza (domain/notification/ReminderPlan).
            implementation(project(path = ":domain"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.components.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.kotlinx.serialization)
            // Local (per-device) preference storage - app language lives here, not in Player,
            // because it must apply even before the player is loaded (Auth screen, splash).
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.apie.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    // Potrzebne dla BuildConfig.DEBUG, z ktorego czyta isDebugBuild (BuildInfo.android.kt) -
    // to on gasi narzedzia deweloperskie w buildzie sklepowym.
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
