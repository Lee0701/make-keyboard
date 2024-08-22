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
            .replace(R.id.settings, RootPreferencesFragment())
            .commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)

        PreferenceManager.setDefaultValues(this, R.xml.preferences_root, true)
        PreferenceManager.setDefaultValues(this, R.xml.preferences_method, true)
        PreferenceManager.setDefaultValues(this, R.xml.preferences_appearance, true)
        PreferenceManager.setDefaultValues(this, R.xml.preferences_layout, true)
        PreferenceManager.setDefaultValues(this, R.xml.preferences_behaviour, true)
        PreferenceManager.setDefaultValues(this, R.xml.preferences_about, true)
    }

    override fun onStop() {
        super.onStop()
        IMEService.sendReloadIntent(this)
    }

    class RootPreferencesFragment: PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_root, rootKey)
        }
    }

    class MethodPreferencesFragment: PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_method, rootKey)
        }
    }

    class AppearancePreferencesFragment: PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_appearance, rootKey)
        }
    }

    class LayoutPreferencesFragment: PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_layout, rootKey)
        }
    }

    class BehaviourPreferencesFragment: PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_behaviour, rootKey)
        }
    }

    class AboutPreferencesFragment: PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_about, rootKey)
        }
    }
}