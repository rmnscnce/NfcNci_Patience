package id.my.pjm.toys.nfcnci_patience.hook

import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.IBinder
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import id.my.pjm.toys.nfcnci_patience.utils.Utils.YLogWrapper

@InjectYukiHookWithXposed(isUsingXposedModuleStatus = false)
class HookEntry : IYukiHookXposedInit {
    override fun onInit() = configs {
        debugLog {
            tag = "NfcNci-Patience"
            isEnable = true
        }
        isEnableModuleAppResourcesCache = false
        isEnableDataChannel = false
    }

    override fun onHook() = encase {
        loadApp(name = "com.android.nfc") {
            YLogWrapper.info(msg = "Applying NfcAdapterService hook")

            // Hook NfcService.updateReaderModeParams
            "com.android.nfc.NfcService\$NfcAdapterService".toClass().method {
                name = "updateReaderModeParams"
                param(
                    "android.nfc.IAppCallback".toClass() /* callback */,
                    Int::class.java /* flags */,
                    Bundle::class.java /* extras? */,
                    IBinder::class.java /* binder */,
                    Int::class.java /* uid */
                )
            }.hook {
                before {
                    YLogWrapper.info("Hooking NFC service for uid=${args[4] as Int}")

                    when (args[2]) {
                        null -> {
                            YLogWrapper.debug(msg = "Arg \"extras\" is null, instantiating a new Bundle to set presence check delay")

                            var extras = Bundle()
                            extras.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1000)
                            args[2] = extras
                        }

                        is Bundle -> {
                            val extras = args[2] as Bundle
                            val presenceCheckDelay =
                                extras.getInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY)

                            YLogWrapper.debug(msg = "Extra presence check delay: $presenceCheckDelay")

                            if (presenceCheckDelay < 1000) {
                                YLogWrapper.debug(msg = "Presence check delay is less than 1000ms, setting to 1000ms")
                                extras.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1000)
                            }

                            args[2] = extras
                        }
                    }
                }
            }
        }
    }
}