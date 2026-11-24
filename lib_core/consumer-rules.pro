-keepclasseswithmembernames class * { # 保持 native 方法不被混淆
native <methods>;
}
-keep public class com.tencent.aai.*
-keep public class com.qq.wx.voice.*
-keep public class com.xmov.ai.core.*
# 保留注解和反射相关的属性（防止泛型/注解信息丢失）
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions
# 如果有其他泛型子类，批量保留（例如所有继承自 BaseActivity 的类）
-keep public class * extends com.xmov.ai.core.base.BaseActivity {
    public <init>();
}

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

# 保持 ViewBinding 生成的绑定类
-keep class * extends androidx.viewbinding.ViewBinding {
    public static final ** inflate(**);
}
# 保持原始 XML 布局的类名
-keep class com.xmov.ai.core.databinding.** { *; }