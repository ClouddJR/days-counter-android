# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Arkadiusz\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

  -dontwarn okhttp3.**
  -dontwarn org.jetbrains.anko.**
  -keepclassmembers class com.arkadiusz.dayscounter.data.model.** {
    *;
    }

  # Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
  # EnclosingMethod is required to use InnerClasses.
  -keepattributes Signature, InnerClasses, EnclosingMethod

  # Retain service method parameters when optimizing.
  -keepclassmembers,allowshrinking,allowobfuscation interface * {
      @retrofit2.http.* <methods>;
  }

  # Ignore annotation used for build tooling.
  -dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

  # Ignore JSR 305 annotations for embedding nullability information.
  -dontwarn javax.annotation.**

  # Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
  -dontwarn kotlin.Unit

  # Top-level functions that can only be used by Kotlin.
  -dontwarn retrofit2.-KotlinExtensions

  # Proguard complaining about displayEventOptions in Fragments
  -dontwarn com.arkadiusz.dayscounter.ui.events.**

  # Android-Image-Cropper
  -keep class androidx.appcompat.widget.** { *; }

  # Glide
  -keep public class * implements com.bumptech.glide.module.GlideModule
  -keep public class * extends com.bumptech.glide.module.AppGlideModule
  -keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
  }

  # Crashlytics
  -keepattributes *Annotation*
  -keepattributes SourceFile,LineNumberTable
  -keep public class * extends java.lang.Exception
