package id.my.pjm.toys.nfcnci_patience.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import id.my.pjm.toys.nfcnci_patience.utils.Utils
import id.my.pjm.toys.nfcnci_patience.utils.Utils.YLogWrapper

@InjectYukiHookWithXposed(isUsingXposedModuleStatus = false)
class HookEntry : IYukiHookXposedInit {
    companion object {
        lateinit var mPresenceCheckWatchdog: Class<*>
        lateinit var mNativeNfcTag: Class<*>
        lateinit var mTagDisconnectedCallback: Class<*>
    }

    override fun onInit() = configs {
        debugLog {
            tag = "NfcNci-Patience"
            isEnable = true
        }
        isDebug = Utils.IS_DEBUG
        isEnableModuleAppResourcesCache = false
        isEnableDataChannel = false
    }

    override fun onHook() = encase {
        loadApp(name = "com.android.nfc") {
            YLogWrapper.info(msg = "Applying the NFC PresenceCheckWatchdog hook")

            mPresenceCheckWatchdog =
                "com.android.nfc.dhimpl.NativeNfcTag\$PresenceCheckWatchdog".toClass()
            mNativeNfcTag = "com.android.nfc.dhimpl.NativeNfcTag".toClass()
            mTagDisconnectedCallback =
                "com.android.nfc.DeviceHost\$TagDisconnectedCallback".toClass()

            mPresenceCheckWatchdog.constructor {
                param(
                    mNativeNfcTag, /* <parent::this> */
                    Int::class.java, /* presenceCheckDelay */
                    mTagDisconnectedCallback, /* callback */
                )
            }.hook {
                before {
                    YLogWrapper.info("Hooking PresenceCheckWatchdog constructor")

                    when (args[1]) {
                        is Int -> {
                            val presenceCheckDelay = args[1] as Int

                            YLogWrapper.debug("presenceCheckDelay: $presenceCheckDelay")

                            if (presenceCheckDelay < 1000) {
                                YLogWrapper.debug("presenceCheckDelay is less than 1000, setting it to 1000")
                                args[1] = 1000
                            } else {
                                YLogWrapper.debug("presenceCheckDelay is already greater than or equal to 1000, leaving it as is")
                            }
                        }

                        null -> YLogWrapper.error("args[1] [com.android.nfc.dhimpl.NativeNfcTag\$PresenceCheckWatchdog::<init>] is null")
                    }
                }
            }
        }
    }
}