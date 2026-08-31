plugins {
    alias(libs.plugins.android.application)
    jacoco
}

android {
    namespace = "com.nafiulirsad.capstone"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.nafiulirsad.capstone"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // The release build is signed with the debug key on purpose: this is a course submission,
        // so the reviewer (and CI) can install the obfuscated APK without any private keystore.
        getByName("debug") {
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            // Feeds the JaCoCo coverage report produced in CI.
            enableUnitTestCoverage = true
        }
        release {
            // Obfuscation + shrinking. R8 in full mode is the ProGuard implementation AGP ships.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        abortOnError = true
        // The UI copy is Indonesian, so the English spell checker only reports false positives.
        disable += "Typos"
        warningsAsErrors = false
        checkDependencies = true
        htmlReport = true
        xmlReport = true
        sarifReport = true
    }

    // The favorite screen ships as an on-device dynamic feature module.
    dynamicFeatures += setOf(":favorite")
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

/**
 * Coverage report for the JVM unit tests, consumed by the CI pipeline.
 * AGP 9 writes the compiled Kotlin classes under `built_in_kotlinc`, and the execution data under
 * `outputs/unit_test_code_coverage`, so both paths are wired up explicitly here.
 */
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Generates a JaCoCo coverage report for the debug unit tests."

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }

    val generated = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*_Impl*.*",
        "**/databinding/**",
        "**/*Binding*.*",
        "**/*Directions*.*",
        "**/*Args*.*",
    )

    classDirectories.setFrom(
        fileTree(layout.buildDirectory) {
            include(
                "intermediates/built_in_kotlinc/debug/**/classes/**/*.class",
                "tmp/kotlin-classes/debug/**/*.class",
                "intermediates/javac/debug/**/classes/**/*.class",
            )
            exclude(generated)
        },
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("outputs/unit_test_code_coverage/debugUnitTest/*.exec")
        },
    )
}

dependencies {
    // `api` so the :favorite dynamic feature can reach the domain layer through this module.
    api(project(":core"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.google.material)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.dynamic.features)

    implementation(libs.glide)

    // Heap analysis, debug builds only: it must never reach the shipped APK.
    debugImplementation(libs.leakcanary.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
