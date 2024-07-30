package ee.oyatl.ime.make.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import ee.oyatl.ime.make.R
import ee.oyatl.ime.make.service.IMEService

class SettingsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)

        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
    }

    override fun onStop() {
        super.onStop()
        IMEService.sendReloadIntent(this)
    }

    class SettingsFragment: PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}