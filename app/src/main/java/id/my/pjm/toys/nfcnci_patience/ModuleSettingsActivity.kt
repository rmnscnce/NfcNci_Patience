package id.my.pjm.toys.nfcnci_patience

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.xposed.prefs.ui.ModulePreferenceFragment
import id.my.pjm.toys.nfcnci_patience.utils.PreferencesManager

class ModuleSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module_settings)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsPreferenceFragment()).commit()
        }
    }

    private class SettingsPreferenceDataStore : PreferenceDataStore() {
        override fun getString(key: String?, defValue: String?): String {
            return when (key) {
                PreferencesManager.TIMEOUT -> PreferencesManager.timeout
                else -> {
                    throw IllegalArgumentException("Unknown key: $key")
                }
            }
        }

        override fun putString(key: String?, value: String?) {
            when (key) {
                PreferencesManager.TIMEOUT -> PreferencesManager.timeout = value!!
                else -> {
                    throw IllegalArgumentException("Unknown key: $key")
                }
            }
        }
    }

    internal class SettingsPreferenceFragment : ModulePreferenceFragment() {
        override fun onCreatePreferencesInModuleApp(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager?.preferenceDataStore = SettingsPreferenceDataStore()
            setPreferencesFromResource(R.xml.module_preferences, rootKey)

            val version = preferenceScreen.findPreference<Preference>(PreferencesManager.VERSION)

            version?.title = String.format(
                getString(R.string.pref_title_version),
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                BuildConfig.BUILD_TYPE
            )
            version?.summary = String.format(
                getString(R.string.pref_summary_version),
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                YukiHookAPI.Status.Executor.apiLevel,
                YukiHookAPI.Status.Executor.name
            )

            val timeout =
                preferenceScreen.findPreference<EditTextPreference>(PreferencesManager.TIMEOUT)

            timeout?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    when (preference.key) {
                        PreferencesManager.TIMEOUT -> {
                            val value = newValue as String
                            try {
                                value.toInt().let {
                                    when {
                                        it in 125..5000 -> true
                                        else -> {
                                            false
                                        }
                                    }
                                }
                            } catch (e: NumberFormatException) {
                                false
                            }
                        }

                        else -> {
                            false
                        }
                    }
                }
        }
    }
}