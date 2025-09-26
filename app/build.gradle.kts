plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.example.parrot"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.parrot"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    kapt {
        correctErrorTypes = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,LGPL-3.0.txt}" // Common general exclusions
            // Specific exclusion for your error:
            excludes += "META-INF/LICENSE-LGPL-3.txt"
            excludes += "META-INF/LICENSE-W3C-TEST"
            excludes += "META-INF/LICENSE-LGPL-2.1.txt"

            // Add the new exclusion for META-INF/DEPENDENCIES
            excludes += "META-INF/DEPENDENCIES"
            // Alternatively, you can use pickFirst if you are sure any version of the file is fine
            // pickFirsts += "META-INF/LICENSE-LGPL-3.txt"
        }
    }
}

dependencies {

    // Baseline & material components
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.circleimageview)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.play.services.basement)

    // Viewmodel and lifecycle
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.compiler)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.okhttp)
    implementation(libs.navigation.dynamic.features.fragment)
    implementation(libs.mammoth)
    implementation(libs.flexmark.all)
    implementation(libs.poi.ooxml)

    // Timber
    implementation(libs.timber)
    // Dependency injection
    // Hilt
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.android)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // image loading
    implementation(libs.coil)

    // Epoxy
    implementation(libs.epoxy)
    kapt(libs.epoxy.processor)
    implementation(libs.epoxy.paging3)

    // Serialization and Parallelization
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.kotlinx.serialization.json)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    // Data and view binding
    implementation(libs.androidx.viewbinding)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
