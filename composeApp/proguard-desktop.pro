# https://github.com/ktorio/ktor-documentation/blob/main/codeSnippets/snippets/proguard/proguard.pro

# Disable optimization — ProGuard's optimizer breaks OkHttp/coroutine bytecode (VerifyError: Bad return type)
-dontoptimize

# Kotlin reflection metadata — required for reflection-based libraries (Koin, serialization, etc.)
-keep class kotlin.Metadata { *; }

# Coroutines — keep volatile fields used by AtomicFieldUpdater
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# OkHttp ships with optional integrations for GraalVM, Conscrypt, BouncyCastle, and OpenJSSE.
# These are not on the classpath for desktop JVM — suppress ProGuard warnings.
-dontwarn org.graalvm.**
-dontwarn com.oracle.svm.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Kotlin 2.3 — new atomics API exists in common only; JVM stubs reference the common classes.
-dontwarn kotlin.concurrent.atomics.**
-dontwarn kotlin.jvm.internal.EnhancedNullability
# Kotlin 2.3 metadata internals referenced across split JARs.
-dontwarn kotlin.metadata.internal.**

# Kotlinx Serialization
-keep @kotlinx.serialization.Serializable class * { *; }
-keep class **$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Koin annotations — keep so the DI graph resolves at runtime
-keep class org.koin.core.annotation.** { *; }
-keep @org.koin.core.annotation.* class * { *; }

# Room + SQLite bundled driver — nativeThreadSafeMode() is called via JNI/reflection
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.sqlite.driver.bundled.** { *; }
-keep class androidx.sqlite.** { *; }

# Ktor OkHttp engine — loaded reflectively at runtime
-keep class io.ktor.client.engine.okhttp.** { *; }

# OkHttp — keep core classes used by Ktor engine
-keep class okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Ktor serialization plugins — loaded via ServiceLoader
-keep class io.ktor.serialization.** { *; }
-keep class io.ktor.client.plugins.contentnegotiation.** { *; }

-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }