# Shipped with the library and merged into the app's R8 run.

# Gson maps JSON keys onto these field names, so they may not be renamed.
-keep class com.nafiulirsad.capstone.core.data.source.remote.response.** { *; }

# SQLCipher is loaded over JNI; its native symbols expect these exact class names.
-keep class net.zetetic.database.** { *; }
-dontwarn net.zetetic.database.**
