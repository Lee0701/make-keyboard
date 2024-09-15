package ee.oyatl.ime.make.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceFragmentCompat.OnPreferenceStartFragmentCallback
import androidx.preference.PreferenceHeaderFragmentCompat
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
            // Enable two-panel layout on tablets
            if(resources.getBoolean(R.bool.activity_settings_two_panel_layout)) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment_container, PreferenceHeaderFragment())
                    .commit()
            }
        }

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

    class PreferenceHeaderFragment: PreferenceHeaderFragmentCompat() {
        override fun onCreatePreferenceHeader(): PreferenceFragmentCompat {
            return RootPreferencesFragment()
        }
    }

    abstract class TitledPreferenceFragment: PreferenceFragmentCompat() {
        @get:StringRes abstract val titleResId: Int
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            // If not using two-panel layout, update title in the action bar
            if(!resources.getBoolean(R.bool.activity_settings_two_panel_layout)) {
                val activity = requireActivity() as AppCompatActivity
                activity.supportActionBar?.setTitle(titleResId)
            }
        }
    }

    class RootPreferencesFragment: TitledPreferenceFragment() {
        override val titleResId: Int = R.string.title_activity_settings
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_root, rootKey)
        }
    }

    class MethodPreferencesFragment: TitledPreferenceFragment() {
        override val titleResId: Int = R.string.pref_screen_method_title
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_method, rootKey)
        }
    }

    class AppearancePreferencesFragment: TitledPreferenceFragment() {
        override val titleResId: Int = R.string.pref_screen_appearance_title
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_appearance, rootKey)
        }
    }

    class LayoutPreferencesFragment: TitledPreferenceFragment() {
        override val titleResId: Int = R.string.pref_screen_layout_title
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_layout, rootKey)
        }
    }

    class BehaviourPreferencesFragment: TitledPreferenceFragment() {
        override val titleResId: Int = R.string.pref_screen_behaviour_title
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_behaviour, rootKey)
        }
    }

    class AboutPreferencesFragment: TitledPreferenceFragment() {
        override val titleResId: Int = R.string.pref_screen_about_title
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