package id.my.pjm.toys.nfcnci_patience.utils

import com.highcapable.yukihookapi.hook.log.YLog
import id.my.pjm.toys.nfcnci_patience.BuildConfig

internal object Utils {
    // Set "false" to "true" on local development to enforce debug logging even on release build
    const val IS_DEBUG = true

    internal object YLogWrapper {
        fun debug(msg: String) {
            if (IS_DEBUG) {
                YLog.debug(msg = msg)
            }
        }

        fun info(msg: String) {
            YLog.info(msg = msg)
        }

        fun warn(msg: String) {
            YLog.warn(msg = msg)
        }

        fun error(msg: String) {
            YLog.error(msg = msg)
        }
    }
}