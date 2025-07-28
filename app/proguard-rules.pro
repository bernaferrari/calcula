# Modern Android App ProGuard Rules
# Optimized for Jetpack Compose and Hilt

# Print out the full proguard config used for every build
-printconfiguration build/outputs/fullProguardConfig.pro

# Kotlin metadata and reflection
-keep class kotlin.Metadata { *; }

# Keep annotation classes
-dontwarn javax.annotation.**
-dontwarn javax.lang.model.**
-dontwarn com.google.j2objc.annotations.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# WorkManager rules
-keep class androidx.work.** { *; }
-keep class androidx.work.impl.** { *; }
-keep class androidx.work.WorkerParameters { *; }

# Keep all classes that extend Worker or CoroutineWorker
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker

# Keep Hilt Worker factory
-keep class androidx.hilt.work.HiltWorkerFactory { *; }
-keep class dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories$InternalFactoryFactory { *; }

# Keep our specific workers
-keep class com.bernaferrari.calculator.core.SyncWorker { *; }
-keep class com.bernaferrari.calculator.core.PackageWorker { *; }