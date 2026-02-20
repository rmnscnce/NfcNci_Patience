package id.my.pjm.toys.nfcnci_patience.hook

import android.app.Application
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class HookEntry : IXposedHookLoadPackage {
    private companion object {
        const val TAG = "NfcNci-Patience"
    }

    @Volatile
    private var currentTimeout = 1000

    private val providerUri = Uri.parse("content://id.my.pjm.toys.nfcnci_patience.provider/config")

    private fun updateConfig(context: Context) {
        runCatching {
            context.contentResolver.query(
                Uri.withAppendedPath(providerUri, "timeout"), null, null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    currentTimeout = cursor.getString(0).toIntOrNull() ?: 1000
                    XposedBridge.log("$TAG: Updated timeout from provider to $currentTimeout ms")
                }
            }
        }.onFailure {
            XposedBridge.log("$TAG: Failed to update config from provider")
            XposedBridge.log(it)
        }
    }

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != "com.android.nfc") return

        XposedBridge.log("$TAG: Applying hooks")

        runCatching {
            val cl = lpparam.classLoader
            
            XposedBridge.log("$TAG: Hooking Application.onCreate")
            XposedHelpers.findAndHookMethod(Application::class.java, "onCreate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val app = param.thisObject as Application
                    updateConfig(app)
                    
                    app.contentResolver.registerContentObserver(
                        providerUri, true,
                        object : ContentObserver(null) {
                            override fun onChange(selfChange: Boolean) = updateConfig(app)
                        }
                    )
                }
            })

            val watchdog = XposedHelpers.findClass("com.android.nfc.dhimpl.NativeNfcTag\$PresenceCheckWatchdog", cl)
            val tag = XposedHelpers.findClass("com.android.nfc.dhimpl.NativeNfcTag", cl)
            val cb = XposedHelpers.findClass("com.android.nfc.DeviceHost\$TagDisconnectedCallback", cl)

            XposedBridge.log("$TAG: Hooking NativeNfcTag.PresenceCheckWatchdog constructor")
            XposedHelpers.findAndHookConstructor(watchdog, tag, Int::class.javaPrimitiveType, cb,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val timeout = currentTimeout
                        val delay = param.args[1] as? Int ?: return

                        XposedBridge.log("$TAG: presenceCheckDelay before hook: $delay ms")

                        if (delay < timeout) {
                            param.args[1] = timeout
                            XposedBridge.log("$TAG: presenceCheckDelay after hook: $timeout ms")
                        }
                    }
                }
            )
        }.onFailure {
            XposedBridge.log("$TAG: Hook error")
            XposedBridge.log(it)
        }
    }
}