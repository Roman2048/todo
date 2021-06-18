package net.longday.planner.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import net.longday.planner.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}