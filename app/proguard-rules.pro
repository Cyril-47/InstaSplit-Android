# Keep Hilt entry points
-keep class * extends dagger.hilt.android.internal.managers.* { *; }

# Keep data/domain models (used in SavedStateHandle)
-keep class com.instasplit.app.domain.model.** { *; }

# Keep Coil
-keep class coil3.** { *; }

# Keep Kotlin Serialization
-keepattributes *Annotation*
-keep class kotlinx.serialization.** { *; }

