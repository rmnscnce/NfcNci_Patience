package id.my.pjm.toys.nfcnci_patience

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.content.SharedPreferences
import android.net.Uri
import android.view.View

class ModuleSettingsActivity : Activity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module_settings)

        prefs = getSharedPreferences("${BuildConfig.APPLICATION_ID}_prefs", MODE_PRIVATE)

        actionBar?.subtitle = getString(
            R.string.module_version_subtitle,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
            BuildConfig.BUILD_TYPE
        )

        setupTimeout()
    }


    private fun setupTimeout() {
        val summaryView = findViewById<TextView>(R.id.timeout_summary)
        val currentTimeout = prefs.getString("timeout", null) ?: "1000"
        summaryView.text = getString(R.string.pref_summary_timeout_value, currentTimeout)

        findViewById<View>(R.id.timeout_item).setOnClickListener {
            showTimeoutDialog(summaryView)
        }
    }

    private fun showTimeoutDialog(summaryView: TextView) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(prefs.getString("timeout", null) ?: "1000")
            setSelection(text.length)
        }

        val container = FrameLayout(this).apply {
            val dp16 = (16 * resources.displayMetrics.density).toInt()
            setPadding(dp16, 0, dp16, 0)
            addView(input)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.pref_title_timeout)
            .setMessage(R.string.pref_dialog_message_timeout)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val str = input.text.toString()
                val v = str.toIntOrNull() ?: return@setPositiveButton
                if (v > 5000) {
                    showHighTimeoutWarning(str, summaryView)
                } else {
                    saveTimeout(str, summaryView)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        input.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val v = s?.toString()?.toIntOrNull()
                val isValid = v != null && v >= 125
                input.error = if (isValid) null else getString(R.string.pref_error_timeout_invalid)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = isValid
            }
        })

        dialog.setOnShowListener {
            val v = input.text.toString().toIntOrNull()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = v != null && v >= 125
        }

        dialog.show()
    }

    private fun showHighTimeoutWarning(value: String, summaryView: TextView) {
        AlertDialog.Builder(this)
            .setTitle(R.string.pref_warning_timeout_title)
            .setMessage(R.string.pref_warning_timeout_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                saveTimeout(value, summaryView)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun saveTimeout(value: String, summaryView: TextView) {
        with(prefs.edit()) {
            putString("timeout", value)
            apply()
        }
        summaryView.text = getString(R.string.pref_summary_timeout_value, value)
        runCatching {
            contentResolver.notifyChange(
                Uri.parse("content://${BuildConfig.APPLICATION_ID}.provider/config"), null
            )
        }
    }
}