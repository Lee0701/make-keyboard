package ee.oyatl.ime.make

import android.app.Application
import com.google.android.material.color.DynamicColors

class MakeKeyboardApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}