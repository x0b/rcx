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

# don't change names - this is open source anyways, and the biggest size
# contributor are native libs which we can't shrink.
-dontobfuscate

# ignore okhttp conscrypt warning
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# keep model classes
# -keep public class ca.pkay.rcloneexplorer.RcloneRcd.*
-keep class ca.pkay.rcloneexplorer.** { *; }
