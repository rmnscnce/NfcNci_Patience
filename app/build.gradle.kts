plugins {
    alias(libs.plugins.android.application)
}

val gitCommitCount = providers.exec {
    commandLine("git", "rev-list", "HEAD", "--count")
}.standardOutput.asText.map { it.trim().toIntOrNull() ?: 0 }.getOrElse(0)

val gitCommitHash = providers.exec {
    commandLine("git", "rev-parse", "--verify", "--short", "HEAD")
}.standardOutput.asText.map { it.trim() }.getOrElse("")

val privateBranchCommitCount = 4 // back when it's an internal WIP project 
val verCode = privateBranchCommitCount + gitCommitCount

android {
    namespace = "id.my.pjm.toys.nfcnci_patience"
    compileSdk = 36

    defaultConfig {
        applicationId = namespace
        minSdk = 27
        targetSdk = 36
        versionCode = verCode
        versionName = "0.3.0" + if (gitCommitHash.isNotEmpty()) "-$gitCommitHash" else ""
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        androidResources.localeFilters.add("en")
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
    lint { checkReleaseBuilds = false }
    packaging {
        resources.excludes += setOf(
            "kotlin/**",
            "META-INF/**",
            "DebugProbesKt.bin"
        )
    }
}

dependencies {
    compileOnly(libs.xposed.api)
    testImplementation(libs.junit)
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
    }
}
