# ---------------------------------------------------------------------------------------------
# R8 / ProGuard configuration for the release build.
#
# `minifyEnabled true` + `shrinkResources true` in app/build.gradle.kts turn this on. R8 renames
# every class, method, and field that is not kept below, so a decompiled release APK shows
# `a.a.a` instead of the real package structure. Verify with:
#   ./gradlew assembleRelease && unzip -p app/build/outputs/apk/release/app-release.apk classes.dex
# and check app/build/outputs/mapping/release/mapping.txt for the renaming table.
# ---------------------------------------------------------------------------------------------

# Attributes needed by Gson (generic signatures) and by Room/Retrofit annotations.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Readable crash reports without giving away the original names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Obfuscate harder: flatten every remaining package and let R8 widen access where it helps.
-repackageclasses 'com.nafiulirsad.capstone'
-allowaccessmodification

# Strip every log call from the release binary, so nothing leaks through logcat.
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# --- Fragments -------------------------------------------------------------------------------
# Navigation Component instantiates fragments by class name from nav_graph.xml, so their names
# have to survive obfuscation. This includes the fragment inside the :favorite dynamic feature.
-keep public class * extends androidx.fragment.app.Fragment

# --- Gson / Retrofit DTOs --------------------------------------------------------------------
# Field names are the JSON keys, so they must not be renamed.
-keep class com.nafiulirsad.capstone.core.data.source.remote.response.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit builds its implementations from the interface's generic signatures.
-keep,allowobfuscation interface com.nafiulirsad.capstone.core.data.source.remote.network.ApiService
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**

# --- SQLCipher -------------------------------------------------------------------------------
# The encryption engine is reached through JNI, so its names are fixed.
-keep class net.zetetic.database.** { *; }
-keep class net.sqlcipher.** { *; }
-dontwarn net.zetetic.database.**

# --- Glide -----------------------------------------------------------------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** { **[] $VALUES; public *; }
