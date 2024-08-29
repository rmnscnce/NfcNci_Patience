package id.my.pjm.toys.nfcnci_patience.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import id.my.pjm.toys.nfcnci_patience.BuildConfig
import id.my.pjm.toys.nfcnci_patience.module_application

@SuppressLint("WorldReadableFiles")
internal object PreferencesManager {
    private object Log {
        private const val LOG_TAG = "NfcNci-Patience:PreferencesManager"

        fun w(msg: String) {
            android.util.Log.w(LOG_TAG, msg)
        }
    }

    internal const val VERSION = "version"
    internal const val TIMEOUT = "timeout"

    private const val PREFERENCES_FILE = "${BuildConfig.APPLICATION_ID}_prefs"

    private val preferences: SharedPreferences by lazy {
        try {
            module_application.getSharedPreferences(PREFERENCES_FILE, Context.MODE_WORLD_READABLE)
        } catch (e: SecurityException) {
            module_application.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        }
    }

    internal var timeout: String
        get() = preferences.getString(TIMEOUT, "1000") ?: "1000"
        set(value) {
            try {
                value.toInt().let {
                    when {
                        it in 125..5000 -> preferences.edit().putString(TIMEOUT, value).apply()
                        it < 125 -> {
                            Log.w("Timeout value is too low, setting to 125")
                            preferences.edit().putString(TIMEOUT, "125").apply()
                        }

                        else -> {
                            Log.w("Timeout value is too high, setting to 5000")
                            preferences.edit().putString(TIMEOUT, "5000").apply()
                        }
                    }
                }
            } catch (e: NumberFormatException) {
                Log.w("Timeout value is not a number, setting to 1000")
                preferences.edit().putString(TIMEOUT, "1000").apply()
            }
        }
}