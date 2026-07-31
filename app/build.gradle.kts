plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.operit.pet"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.operit.pet"
        minSdk = 26  // Android 8.0
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            // 复用 Android SDK 自带的 debug keystore
            storeFile = file(System.getenv("ANDROID_HOME") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    // Kotlin 协程 (SupabaseSync 需要)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // JSON parsing for Supabase sync
    implementation("org.json:json:20231013")
}