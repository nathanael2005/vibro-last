# Mobile module ProGuard rules

# Dagger Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class * implements dagger.hilt.internal.GeneratedComponent
-keep @dagger.hilt.android.AndroidEntryPoint class *

# Media3 ExoPlayer & Session Service
-dontwarn androidx.media3.**
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }
-keep class androidx.media3.ui.** { *; }

# Keep PlaybackService name intact for Android System binding
-keep class com.nate.core.data.player.PlaybackService { *; }

# Gson Data Transfer Objects & Domain Models serialization protection
-keep class com.nate.core.data.model.** { *; }
-keep class com.nate.core.domain.model.** { *; }

# Retrofit, OkHttp, and Gson reflections
-keepattributes Signature, InnerClasses, AnnotationDefault, EnclosingMethod, *Annotation*
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
