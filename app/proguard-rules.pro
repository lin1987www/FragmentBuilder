# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\android\sdk/tools/proguard/proguard-android.txt
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


# Butter knife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}


# facebook
-keep class com.facebook.** { *; }
-keepattributes Signature
# facebook-conceal
-keep,allowobfuscation @interface com.facebook.proguard.annotations.DoNotStrip
-keep,allowobfuscation @interface com.facebook.proguard.annotations.KeepGettersAndSetters
# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.proguard.annotations.DoNotStrip class *
-keepclassmembers class * {
@com.facebook.proguard.annotations.DoNotStrip *;
}
-keepclassmembers @com.facebook.proguard.annotations.KeepGettersAndSetters class * {
void set*(***);
*** get*();
}


## Google Play Services 4.3.23 specific rules ##
## https://developer.android.com/google/play-services/setup.html#Proguard ##
-keep class * extends java.util.ListResourceBundle {
protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
@com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
public static final ** CREATOR;
}


# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**


# OkHttp3
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**


#retrofit2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions


#ok.io
-dontwarn okio.**


# jackson
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**
-keep class org.codehaus.** { *; }
-keepclassmembers public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *;
}
## your package, if didn't using @JsonCreator
-keep public class com.wanbaolu.** {
public void set*(***);
public *** get*();
}


# Event Bus
-keepclassmembers class ** {
    public void onEvent*(**);
}
# Only required if you use AsyncExecutor
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}


# glide
-keep public class * implements com.bumptech.glide.module.GlideModule


# OrmLite uses reflection
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }
-keepclassmembers class * {
  public <init>(android.content.Context);
}


#dagger
#-Keep the names of classes that have fields annotated with @Inject and the fields themselves.
-keepclasseswithmembernames class * {
  @javax.inject.* <fields>;
  @javax.inject.* <init>(...);
}


#Gradle Retrolambda Plugin
-dontwarn java.lang.invoke.*

#
-dontwarn im.delight.android.webview.**


# Creative SDK
-keep class com.aviary.**
-keepclassmembers class com.aviary.** { *; }


# 支付寶
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
-dontwarn com.alipay.**


# 自己添加
-keep class com.facebook.**
-dontwarn com.facebook.**

-keep class com.google.**
-dontwarn com.google.**

-keep class org.rajawali3d.**
-dontwarn org.rajawali3d.**

-dontwarn com.wanbaolu.chainby.rx.ApiUnwrap

-keepclassmembers class ** {
    public void onPopFragment(**);
}

-keepclassmembers class ** {
    public boolean onDuty(**);
}

-keepclassmembers class * extends android.support.v7.widget.ViewHolder {
    <init>(...);
}

# 有用到內部屬性名稱
-keep class android.support.v4.widget.SwipeRefreshLayout{*;}

-keep class * {
@com.fasterxml.jackson.annotation.** *;
}

-keep @com.j256.ormlite.** class * {
  @com.j256.ormlite.** <fields>;
  void set*(***);
  *** get*();
}

#-keep class ** implements android.os.Parcelable {
#  public static final android.os.Parcelable$Creator *;
#}

# ref: http://blog.osom.info/2014/02/guarding-enumeration-classes-from.html
-keepclassmembers,allowoptimization enum * {
      public static **[] values();
      public static ** valueOf(java.lang.String);
}

# debug 時專用
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-dump proguard/class_files.txt
-printseeds proguard/seeds.txt
-printusage proguard/unused.txt
-printmapping proguard/mapping.txt