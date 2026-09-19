plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

import java.util.Properties
import java.util.Base64

fun obfuscateApiKey(key: String): String {
    if (key.isEmpty()) return ""
    val xorKey = "NateStream"
    val keyBytes = key.toByteArray()
    val xorBytes = xorKey.toByteArray()
    val result = ByteArray(keyBytes.size)
    for (i in keyBytes.indices) {
        result[i] = (keyBytes[i].toInt() xor xorBytes[i % xorBytes.size].toInt()).toByte()
    }
    return Base64.getEncoder().encodeToString(result)
}

fun tmdbApiKeyFromConfig(): String {
    val localProps = Properties()
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { localProps.load(it) }
    }
    val fromLocal = localProps.getProperty("TMDB_API_KEY")?.trim().orEmpty()
    val fromEnv = System.getenv("TMDB_API_KEY")?.trim().orEmpty()
    return if (fromLocal.isNotEmpty()) fromLocal else fromEnv
}

android {
    namespace = "com.nate.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nate.app"
        minSdk = 26 // Required for advanced WebView and TV features
        targetSdk = 34
        versionCode = 3
        versionName = "1.2.0"
        val tmdbApiKey = tmdbApiKeyFromConfig()
        val obfuscatedKey = obfuscateApiKey(tmdbApiKey)
        buildConfigField("String", "TMDB_API_KEY", "\"$obfuscatedKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Core Compose TV libraries (Crucial for D-Pad navigation)
    implementation("androidx.tv:tv-material:1.0.0-rc01")
    implementation("androidx.tv:tv-foundation:1.0.0-alpha11")
    
    // Core Compose UI and Navigation
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    // Coil Image Loading (TV Optimized)
    implementation("io.coil-kt:coil-compose:2.6.0")
    
    // HTTP Networking (Retrofit + OkHttp) for direct TMDB parsing
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Native playback engine (Jetpack Media3 / ExoPlayer)
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Local Data Storage for TMDB Key & Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
