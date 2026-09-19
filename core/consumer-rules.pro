# Core module consumer ProGuard rules
-keepattributes Signature, InnerClasses, AnnotationDefault, EnclosingMethod, *Annotation*

# Retrofit, OkHttp, and Gson reflections
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }

# TMDB API Data Transfer Objects & Serialization
-keep class com.nate.core.data.api.** { *; }
-keepclassmembers class com.nate.core.data.api.** {
    <fields>;
    <methods>;
}

# Room Database Entities and DAOs
-dontwarn androidx.room.**
-keep class androidx.room.** { *; }
-keep class com.nate.core.data.local.** { *; }
-keepclassmembers class com.nate.core.data.local.** {
    <fields>;
    <methods>;
}

# Domain Models
-keep class com.nate.core.domain.model.** { *; }
-keepclassmembers class com.nate.core.domain.model.** {
    <fields>;
    <methods>;
}

# Media3 ExoPlayer
-dontwarn androidx.media3.**
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.ui.** { *; }
