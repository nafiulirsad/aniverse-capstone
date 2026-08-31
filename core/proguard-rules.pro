# Rules used when this library is built on its own. The rules the consuming app needs are in
# consumer-rules.pro, which R8 merges into the app's configuration automatically.
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
