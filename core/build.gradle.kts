plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.nafiulirsad.capstone.core"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
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

    lint {
        abortOnError = true
        warningsAsErrors = false
        htmlReport = true
        xmlReport = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
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
    // Exposed to the modules above: they are allowed to know about coroutines and Koin only.
    api(libs.androidx.core.ktx)
    api(libs.kotlinx.coroutines.android)
    api(libs.koin.android)

    // Data-layer details stay private to this module so the presentation layer
    // physically cannot reach Room/Retrofit types (Clean Architecture dependency rule).
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Database encryption: SQLCipher provides the SQLite implementation Room opens the file with.
    implementation(libs.androidx.sqlite)
    implementation(libs.sqlcipher.android)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
