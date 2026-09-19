# TV module ProGuard rules
-keepattributes Signature, InnerClasses, AnnotationDefault, EnclosingMethod, *Annotation*

# Dagger Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class * implements dagger.hilt.internal.GeneratedComponent
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *

# Media3 ExoPlayer & UI
-dontwarn androidx.media3.**
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.ui.** { *; }

# Coil Image Loader
-dontwarn coil.**
-keep class coil.** { *; }

# Data Transfer Objects, Room Entities, and Domain Models
-keep class com.nate.core.data.api.** { *; }
-keepclassmembers class com.nate.core.data.api.** { <fields>; <methods>; }
-keep class com.nate.core.data.local.** { *; }
-keepclassmembers class com.nate.core.data.local.** { <fields>; <methods>; }
-keep class com.nate.core.domain.model.** { *; }
-keepclassmembers class com.nate.core.domain.model.** { <fields>; <methods>; }

# Retrofit, OkHttp, Gson
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keepclassmembers enum * { *; }

# WebKit Javascript Interface & Stream Extractor
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.nate.tv.presentation.player.extractor.** { *; }
