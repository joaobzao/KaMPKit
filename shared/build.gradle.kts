
plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("kotlinx-serialization")
    id("com.android.library")
    id("com.squareup.sqldelight")
    id("co.touchlab.kotlinxcodesync")
}

android {
    compileSdkVersion(28)
    defaultConfig {
        minSdkVersion(Versions.min_sdk)
        targetSdkVersion(Versions.target_sdk)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    android()

    val buildForDevice = project.findProperty("kotlin.native.cocoapods.target") == "ios_arm"

    if (buildForDevice) {
        iosArm64("ios64")
    } else {
        iosX64("ios")
    }

    version = "1.0"

    sourceSets["commonMain"].dependencies {
        implementation(kotlin("stdlib-common", Versions.kotlin))
        implementation(Deps.SqlDelight.runtime)
        implementation(Deps.ktor.commonCore)
        implementation(Deps.ktor.commonJson)
        implementation(Deps.Coroutines.common)
        implementation(Deps.stately)
        implementation(Deps.multiplatformSettings)
        implementation(Deps.koinCore)
        implementation(Deps.ktor.commonSerialization)

    }

    sourceSets["commonTest"].dependencies {
        implementation(Deps.multiplatformSettingsTest)
    }

    sourceSets["androidMain"].dependencies {
        implementation(kotlin("stdlib", Versions.kotlin))
        implementation(Deps.SqlDelight.driverAndroid)
        implementation(Deps.ktor.jvmCore)
        implementation(Deps.ktor.jvmJson)
        implementation(Deps.Coroutines.jdk)
        implementation(Deps.Coroutines.android)
        implementation(Deps.ktor.androidSerialization)
    }

    sourceSets["iosMain"].dependencies {
        implementation(Deps.SqlDelight.driverIos)
        implementation(Deps.ktor.ios, Deps.coroutinesExcludeNative)
        implementation(Deps.ktor.iosCore, Deps.coroutinesExcludeNative)
        implementation(Deps.ktor.iosJson, Deps.coroutinesExcludeNative)
        implementation(Deps.Coroutines.native)
        implementation(Deps.ktor.iosSerialization)
    }

    cocoapods {
        summary = "Common library for the KaMP starter kit"
        homepage = "https://github.com/touchlab/KaMPStarter"
    }

    xcodeSync {
        projectPath = "../ios/KaMPStarteriOS.xcodeproj"
        target = "KaMPStarteriOS"
    }
}

sqldelight {
    database("KampstarterDb") {
        packageName = "co.touchlab.kampstarter.db"
    }
}