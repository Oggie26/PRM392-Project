plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.prm391_project"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.prm391_project"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    // ✅ Packaging conflict resolution for io.netty.versions.properties
    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/io.netty.versions.properties" // Added to resolve the warning
        }
    }
}

dependencies {
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation (libs.logging.interceptor)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.navigation.compose.android)
    implementation (libs.material3)
    implementation (libs.androidx.material.icons.extended)
    implementation (libs.androidx.navigation.compose)
    implementation (libs.androidx.activity.compose.v193)
    implementation("com.google.maps.android:maps-compose:2.11.4")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    implementation("com.mapbox.maps:android:11.2.0") // Mapbox Maps v11
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-services:6.12.0") // Mapbox Java SDK
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-services:5.8.0")


    implementation (libs.androidx.security.crypto.ktx.v110alpha06)
    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.airbnb.android:lottie-compose:6.4.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4") // <--- THÊM DÒNG NÀY



    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.androidx.espresso.core)

    implementation(libs.androidx.animation.core.lint)
    implementation(libs.androidx.security.crypto.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}