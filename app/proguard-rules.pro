# 桌宠 ProGuard 规则
# 保持 WebView JS 接口
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
# 保持 Config 对象不被混淆
-keep class com.operit.pet.Config { *; }