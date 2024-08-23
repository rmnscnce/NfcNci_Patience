package id.my.pjm.toys.nfcnci_patience.utils

import com.highcapable.yukihookapi.hook.log.YLog
import id.my.pjm.toys.nfcnci_patience.BuildConfig

object Utils {
    object YLogWrapper {
        fun debug(msg: String) {
            if (BuildConfig.DEBUG) {
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