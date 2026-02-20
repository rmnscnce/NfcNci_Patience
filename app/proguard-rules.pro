# R8/ProGuard rules

-optimizationpasses 25
-dontusemixedcaseclassnames
-overloadaggressively
-allowaccessmodification
-repackageclasses

# Strip Kotlin null-check assertions (already disabled via compiler flags, belt-and-suspenders)
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static *** throwUninitializedProperty(...);
    public static *** throwUninitializedPropertyAccessException(...);
    public static *** checkNotNullParameter(...);
    public static *** checkNotNullExpressionValue(...);
    public static *** checkNotNull(...);
}

# Keep Activities (needed for manifest resolution)
-keep class * extends android.app.Activity

# Keep Xposed entry point
-keep class id.my.pjm.toys.nfcnci_patience.hook.HookEntry