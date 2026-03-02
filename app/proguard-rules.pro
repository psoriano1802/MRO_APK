# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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
# ==== Retrofit + Gson ====
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keep class com.google.gson.** { *; }

# ==== Room (Database) ====
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Room

# ==== ML Kit (Face & Barcode) ====
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ==== CameraX ====
-keep class androidx.camera.** { *; }

# ==== General model classes (tu paquete) ====
-keep class com.example.baseloginapp.models.** { *; }

# ==== Evita eliminar clases usadas por reflexión ====
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# ==== Evita errores con view binding o synthetic ====
-keep class **.databinding.* { *; }

# ==== Otras optimizaciones ====
-dontwarn java.nio.file.*
