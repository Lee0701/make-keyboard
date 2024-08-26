package ee.oyatl.ime.make.settings

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceFragmentCompat.OnPreferenceStartFragmentCallback
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import ee.oyatl.ime.make.R
import ee.oyatl.ime.make.service.IMEService

class SettingsActivity
    : AppCompatActivity(), OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)

        setContentView(R.layout.activity_settings)
        if(savedInstanceState == null) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        }
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setDefaultValues(this)
    }

    override fun onStop() {
        super.onStop()
        IMEService.restartService()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_container)
        val destination = navController.graph.find { dest ->
            pref.fragment?.endsWith(dest.label ?: "") == true
        }
        if(destination != null) {
            val direction = ActionOnlyNavDirections(destination.id)
            navController.navigate(direction, navOptions {
                anim {
                    enter = R.anim.slide_in_right
                    exit = R.anim.slide_out_left
                    popEnter = R.anim.slide_in_left
                    popExit = R.anim.slide_out_right
                }
            })
        }
        return true
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

    companion object {
        fun setDefaultValues(context: Context) {
            PreferenceManager.setDefaultValues(context, R.xml.preferences_root, true)
            PreferenceManager.setDefaultValues(context, R.xml.preferences_method, true)
            PreferenceManager.setDefaultValues(context, R.xml.preferences_appearance, true)
            PreferenceManager.setDefaultValues(context, R.xml.preferences_layout, true)
            PreferenceManager.setDefaultValues(context, R.xml.preferences_behaviour, true)
            PreferenceManager.setDefaultValues(context, R.xml.preferences_about, true)
        }
    }
}