package id.my.pjm.toys.nfcnci_patience.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XSharedPreferences
import id.my.pjm.toys.nfcnci_patience.BuildConfig
import id.my.pjm.toys.nfcnci_patience.utils.PreferencesManager.TIMEOUT
import id.my.pjm.toys.nfcnci_patience.utils.Utils
import id.my.pjm.toys.nfcnci_patience.utils.Utils.YLogWrapper
import kotlin.properties.Delegates

@InjectYukiHookWithXposed(isUsingXposedModuleStatus = true)
class HookEntry : IYukiHookXposedInit {
    private companion object {
        lateinit var mPresenceCheckWatchdog: Class<*>
        lateinit var mNativeNfcTag: Class<*>
        lateinit var mTagDisconnectedCallback: Class<*>

        var timeout by Delegates.notNull<Int>()

        object Preferences {
            private lateinit var prefs: XSharedPreferences

            private fun prefs(): XSharedPreferences {
                prefs = XSharedPreferences(
                    BuildConfig.APPLICATION_ID, "${BuildConfig.APPLICATION_ID}_prefs"
                )
                return prefs
            }

            val timeout
                get() = try {
                    prefs().getString(TIMEOUT, "1000")!!.toInt()
                } catch (e: NumberFormatException) {
                    YLogWrapper.warn("Saved timeout preference value is not a number, setting it to 1000")

                    1000
                }
        }
    }

    override fun onInit() = configs {
        debugLog {
            tag = "NfcNci-Patience"
            isEnable = true
        }
        isDebug = Utils.IS_DEBUG
        isEnableHookSharedPreferences = true
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

                    timeout = Preferences.timeout
                    YLogWrapper.info("Timeout is set to $timeout")

                    when (args[1]) {
                        is Int -> {
                            val presenceCheckDelay = args[1] as Int

                            YLogWrapper.debug("presenceCheckDelay: $presenceCheckDelay")

                            if (presenceCheckDelay < timeout) {
                                YLogWrapper.debug("presenceCheckDelay is less than $timeout, setting it to $timeout")
                                args[1] = timeout
                            } else {
                                YLogWrapper.debug("presenceCheckDelay is already greater than or equal to $timeout, leaving it as is")
                            }
                        }

                        null -> YLogWrapper.error("args[1] [com.android.nfc.dhimpl.NativeNfcTag\$PresenceCheckWatchdog::<init>] is null")
                    }
                }
            }
        }
    }
}