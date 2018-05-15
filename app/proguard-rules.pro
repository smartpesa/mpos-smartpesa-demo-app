-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-printmapping mapping.txt

-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

-dontwarn java.nio.files.**
-dontwarn okio.**
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn retrofit.**
-dontwarn com.squareup.**
-dontwarn rx.internal.**
-dontwarn smartpesa.sdk.**
-dontwarn com.bbpos.**

# SmartPesa SDK
-keepclassmembers class smartpesa.sdk.** { *; }
-keepclassmembers interface smartpesa.sdk.** { *; }
-keepclassmembers class * implements smartpesa.sdk.interfaces.DataCallback
-keep class com.bbpos.** { *; }
-keep class com.RT_Printer.** { *; }

## ButterKnife
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector {
    public static void inject(...);
    public static void reset(...);
}
-keep class **$$ViewBinder {
    public static void bind(...);
    public static void unbind(...);
}
-keepnames class * { @butterknife.Bind *;}

# Timber
-keep class timber.** { *; }

# Retrofit.
-keep class retrofit.** { *; } # Keep the annotations
-keep class ** { @retrofit.http.** *; } # Keep the annotated services

## Activity names
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

-keepclassmembers !abstract class !com.google.ads.** extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclassmembers !abstract class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# com.example.android.apis.animation.ShapeHolder,...
-keepclassmembers class **animation**Holder {
    public *** get*();
    public void set*(***);
}

-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# Ignore references to removed R classes.
-dontwarn android.support.**.R
-dontwarn android.support.**.R$*

# Support library
-keep class android.support.v4.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

# Ignore references from compatibility support classes to missing classes.
-dontwarn android.support.**

# Google Play Services.
-dontwarn com.google.android.gms.**
-dontnote com.google.android.gms.**

-keepclassmembers class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final java.lang.String NULL;
}

-keep,allowobfuscation @interface com.google.android.gms.common.annotation.KeepName
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

# GSON.
-keep @interface com.google.gson.annotations.*

-dontnote com.google.gson.annotations.Expose
-keepclassmembers class * {
    @com.google.gson.annotations.Expose <fields>;
}

-keepclasseswithmembers,allowobfuscation,includedescriptorclasses class * {
    @com.google.gson.annotations.Expose <fields>;
}

-dontnote com.google.gson.annotations.SerializedName
-keepclasseswithmembers,allowobfuscation,includedescriptorclasses class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keepclassmembers enum * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.** { *; }

# Enumerations.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Serializable classes.
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}