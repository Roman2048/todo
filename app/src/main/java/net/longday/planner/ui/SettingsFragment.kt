package net.longday.planner.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.textview.MaterialTextView
import net.longday.planner.R

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton: ImageButton = view.findViewById(R.id.fragment_settings_back_button)
        val privacyPolicy: MaterialTextView =
            view.findViewById(R.id.fragment_settings_version_policy)
        backButton.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_settingsFragment_to_homeFragment)
        }
        privacyPolicy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://longday.net")))
        }
    }
}