# Proguard Rules for Nate TV App

# Keep standard Gson & Retrofit models
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep our TMDB API data models from being obfuscated or stripped
-keep class com.nate.app.data.model.** { *; }

# Keep Compose TV clickable surface and layout structures
-keep class androidx.tv.** { *; }
-keep class androidx.compose.** { *; }

# Keep Coil image loader
-keep class io.coilkt.** { *; }

# Keep Retrofit classes
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Keep OkHttp classes
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Keep Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep DataStore & Preferences
-keep class androidx.datastore.** { *; }

