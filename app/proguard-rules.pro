# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Kotlinx Serialization
# Keep all @Serializable classes for kotlinx.serialization
-keep @kotlinx.serialization.Serializable class * { *; }

# Keep serializers
-keep class **$$serializer { *; }

# Keep Companion objects for serializable classes
-keepclassmembers class * {
    *** Companion;
}

# Keep classes with KSerializer
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep attributes needed for serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**