package net.longday.planner.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.ExportedTask
import net.longday.planner.data.entity.Task
import net.longday.planner.data.entity.export
import net.longday.planner.viewmodel.CategoryViewModel
import net.longday.planner.viewmodel.TaskViewModel
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val taskViewModel: TaskViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    private var tasks = listOf<Task>()
    private var categories = listOf<Category>()

    private val fileName = "LD_Planner_export.txt"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton: ImageButton = view.findViewById(R.id.fragment_settings_back_button)
        val exportTextView: MaterialTextView = view.findViewById(R.id.fragment_settings_export_text)
        val optimizeButton: MaterialButton = view.findViewById(R.id.settings_optimize_button)
        taskViewModel.tasks.observe(viewLifecycleOwner) {
            tasks = it
        }
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            categories = it
        }
        exportTextView.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val f = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
                )
                try {
                    f.appendText(export(tasks, categories))
                    Snackbar.make(
                        it,
                        "${getString(R.string.settings_fragment_export_success)}\\\"$fileName\"",
                        BaseTransientBottomBar.LENGTH_LONG
                    ).show()
                } catch (e: IOException) {
                    println(e.localizedMessage)
                    Snackbar.make(
                        it,
                        getString(R.string.settings_fragment_export_error),
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
                }
            } else {
                Snackbar.make(
                    it,
                    getString(R.string.settings_fragment_permission_error),
                    BaseTransientBottomBar.LENGTH_SHORT
                ).show()
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 42)
            }
        }
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
        backButton.setOnClickListener { findNavController().popBackStack() }
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
        val powerManager = requireContext().applicationContext
            .getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager
            .isIgnoringBatteryOptimizations(requireContext().applicationContext.packageName)
    }

    private fun openPowerSettings() {
        startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
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

    private fun requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), requestCode)
    }

    private fun export(tasks: List<Task>, categories: List<Category>): String {
        val exportedTasks = mutableListOf<ExportedTask>()
        tasks.forEach {
            exportedTasks.add(it.export(categories))
        }
        return Gson().toJson(exportedTasks)
    }
}