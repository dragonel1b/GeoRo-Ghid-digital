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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Remove all Log statements except errors
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
}

# Remove all Flogger log statements
-assumenosideeffects class com.google.common.flogger.** {
    public static *** log*(...);
    public static *** log(...);
}

# Remove Google Maps SDK logs
-assumenosideeffects class com.google.android.gms.maps.** {
    public static void log*(...);
    private static void log*(...);
    public static void debug*(...);
    private static void debug*(...);
}

# Remove ProxyAndroidLoggerBackend logs
-assumenosideeffects class com.google.android.libraries.maps.api.internal.** {
    public static *** log*(...);
    private static *** log*(...);
}

# Keep required Google Maps classes that might be incorrectly removed
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-keep class com.google.android.gms.maps.model.** { *; }

# Keep required classes for reflection
-keep class android.util.Log { *; }
-keep class com.google.common.flogger.** { *; }
-keep class com.google.android.libraries.maps.api.internal.** { *; }

# Basic rules for Android apps
-keepattributes *Annotation*
-renamesourcefileattribute SourceFile

# Keep entry points
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View

# Keep all of your app's own classes
-keep class com.example.myapplication.** { *; }

# This will allow ProGuard to process the file while ignoring warnings
-ignorewarnings

# Keep important R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Enable optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Logging-specific rules are in proguard-logging.pro