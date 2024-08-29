package id.my.pjm.toys.nfcnci_patience

import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication

internal lateinit var module_application: ModuleApplication

class ModuleApplication : ModuleApplication() {
    override fun onCreate() {
        super.onCreate()
        module_application = this
    }
}
