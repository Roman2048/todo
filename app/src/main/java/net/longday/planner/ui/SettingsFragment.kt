package net.longday.planner.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import net.longday.planner.R

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    /**
     * Go to main screen if the back button what pressed
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity
            ?.onBackPressedDispatcher
            ?.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController()
                        .navigate(R.id.action_settingsFragment_to_homeFragment)
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton: ImageButton = view.findViewById(R.id.fragment_settings_back_button)
        val optimizeButton: MaterialButton = view.findViewById(R.id.settings_optimize_button)
        val remindersText: MaterialTextView =
            view.findViewById(R.id.fragment_settings_reminders_text)
        if (!isIgnoringBatteryOptimizations()) {
            remindersText.text =
                getString(R.string.settings_reminders_text_need_ignore_optimizations)
            optimizeButton.visibility = View.VISIBLE
        }
        optimizeButton.setOnClickListener {
            openPowerSettings()
        }
        val privacyPolicy: MaterialTextView =
            view.findViewById(R.id.fragment_settings_privacy_policy)
        backButton.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_settingsFragment_to_homeFragment)
        }
        privacyPolicy.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://longday.net/planner_policy.html")
                )
            )
        }
        val termsButton: MaterialTextView =
            view.findViewById(R.id.fragment_settings_terms)
        termsButton.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://longday.net/planner_terms.html")
                )
            )
        }
    }

    /**
     * Return true if in App's Battery settings "Not optimized"
     */
    private fun isIgnoringBatteryOptimizations(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (requireContext()
                .applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .isIgnoringBatteryOptimizations(requireContext().applicationContext.packageName)
        } else {
            true
        }
    }

    private fun openPowerSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isIgnoringBatteryOptimizations()) {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
        }
    }

    /**
     * Needed to refresh view when user is go back from battery settings
     */
    override fun onResume() {
        super.onResume()
        if (isIgnoringBatteryOptimizations()) {
            val remindersText =
                requireActivity()
                    .findViewById<MaterialTextView>(R.id.fragment_settings_reminders_text)
            val optimizeButton: MaterialButton =
                requireActivity().findViewById(R.id.settings_optimize_button)
            remindersText.text =
                getString(R.string.settings_reminders_text_battery_ok)
            optimizeButton.visibility = View.GONE
        }
    }
}