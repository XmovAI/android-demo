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

#XLog
#-keep class com.tencent.mars.** {
#  public protected private *;
#}

# 如果使用了 单类注入，即不定义接口实现 IProvider，需添加下面规则，保护实现
# -keep class * implements com.alibaba.android.arouter.facade.template.IProvider

# https://github.com/suming77/SumTea_Android/issues/2
-keep class org.webrtc.** { *; }
-keepclasseswithmembernames class * { # 保持 native 方法不被混淆
native <methods>;
}
-dontwarn okhttp3.internal.connection.StreamAllocation
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMarkerBinder
-dontwarn dalvik.system.VMStack
-keep public class com.xmov.ai.core.** { *; }
# 保留注解和反射相关的属性（防止泛型/注解信息丢失）
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions

# 保留所有带有特定方法名的方法（适用于全局有多个inflate方法的场景）
-keepclassmembers class * {
    # 保留所有名为inflate、参数包含LayoutInflater的方法
    public ***inflate(android.view.LayoutInflater, ...);
    public static ***inflate(android.view.LayoutInflater, ...);
}

# 保留所有继承自View的类（避免自定义View的方法被混淆）
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...); # 保留setter方法（避免属性动画/数据绑定反射失败）
}

# 保留所有 ViewBinding 类的 inflate 方法（包括所有重载）
-keepclassmembers class **.databinding.*Binding {
    public static ***inflate(android.view.LayoutInflater);
    public static ***inflate(android.view.LayoutInflater, android.view.ViewGroup);
    public static ***inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
}

# 保持原始 XML 布局的类名
-keep class com.xmov.ai.echomind.demo.databinding.** { *; }

-keep class com.xmov.ai.echomind.demo.MainActivity$AssetConfig { *; }
-keep class org.msgpack.** { *; }
-keep interface org.msgpack.** { *; }
-dontwarn sun.nio.ch.DirectBuffer


-keep class com.xmov.xingyunavataragent.XingyunAvatarAgent { public *; }
-keep interface com.xmov.xingyunavataragent.XingyunAvatarAgentListener { *; }
-keep class com.xmov.xingyunavataragent.XingyunAvatarAgentConfig { public *; }
-keep class com.xmov.xingyunavataragent.AsrConfig { *; }
-keep class com.xmov.xingyunavataragent.BrainConfig { *; }
-keep class com.xmov.xingyunavataragent.FeaturesConfig { *; }
-keep class com.xmov.xingyunavataragent.AntiInterferenceConfig { *; }
-keep class com.xmov.xingyunavataragent.SpeechFrontendConfig { *; }
-keep class com.xmov.xingyunavataragent.XingyunAvatarAgentError { public *; }
-keep enum com.xmov.xingyunavataragent.ErrorDomain { *; }
-keep class com.xmov.xingyunavataragent.LlmUsage { public *; }
-keep class com.xmov.xingyunavataragent.SemanticJudgeResult { public *; }
-keep class com.xmov.xingyunavataragent.VolumeAndRepetitionTextResult { public *; }
-keep enum com.xmov.xingyunavataragent.XingyunAvatarAgentState { *; }

-keep public interface com.xmov.metahuman.sdk.IAvatarListener {
    public protected *;
}
-keep public interface com.xmov.metahuman.sdk.PreCacheListener {
    public protected *;
}
-keep public class com.xmov.metahuman.sdk.data.**{*;}
-keep public class com.xmov.metahuman.sdk.impl.data.**{*;}
-keep public class com.xmov.metahuman.sdk.impl.transport.http.**{*;}
-keep public class com.xmov.metahuman.sdk.impl.transport.session.**{*;}

-dontwarn java.awt.Color
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn javax.lang.model.element.Modifier
-dontwarn com.didi.hummer.context.HummerContext

# ========== 基础属性（AGP7.1.0 必配） ==========
-keepattributes Signature, InnerClasses, EnclosingMethod, Annotation
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-dontoptimize  # 关闭 R8 过度优化（适配旧版）
-dontshrink    # 避免移除未被内部引用的 Gson 方法
-dontwarn com.xmov.metahuman.sdk.impl.**

# ========== 2. Gson 2.10.1 专属规则（核心） ==========
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.** { *; }
-keep class com.google.gson.stream.** { *; }

-dontwarn com.tencent.smtt.export.external.extension.interfaces.IX5WebViewExtension
-dontwarn com.tencent.smtt.sdk.WebSettings$LayoutAlgorithm
-dontwarn com.tencent.smtt.sdk.WebSettings
-dontwarn com.tencent.smtt.sdk.WebView
-dontwarn io.github.jaredmdobson.concentus.OpusApplication
-dontwarn io.github.jaredmdobson.concentus.OpusEncoder
-dontwarn io.github.jaredmdobson.concentus.OpusSignal
-dontwarn io.github.jaredmdobson.concentus.OpusDecoder
