plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.ksp)
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
    compileSdk = 34

    defaultConfig {
        applicationId = "id.my.pjm.toys.nfcnci_patience"
        minSdk = 27
        targetSdk = 34
        versionCode = verCode
        versionName = "0.2.0" + if (gitCommitHash.isNotEmpty()) "-$gitCommitHash" else ""
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
    }
    lint { checkReleaseBuilds = false }
}

dependencies {
    compileOnly(libs.xposed.api)
    implementation(libs.yukihookapi.api)
    ksp(libs.yukihookapi.ksp)
    implementation(libs.drawabletoolbox)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
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
