# Proguard rules for Focal
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
