plugins {
    alias(libs.plugins.android.dynamic.feature)
}

android {
    namespace = "com.nafiulirsad.capstone.favorite"
    compileSdk = 37

    defaultConfig {
        // Must match the base module, otherwise bundletool rejects the app bundle.
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // The base module brings :core (and therefore the domain layer) along with it.
    implementation(project(":app"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.google.material)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
}
