# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line number information for crash logging and debugging
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod

# Keep Retrofit interface methods and annotations
-keepattributes *Annotation*
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep all models in com.example package (since we serialize them as maps and use Moshi/reflection)
-keep class com.example.** { *; }

# Keep Moshi generic signature and generated code
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Keep OkHttp & Retrofit classes
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animalsniffer.**

