package net.longday.planner.ui.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.TaskViewModel
import net.longday.planner.work.OneTimeScheduleWorker
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class AddTaskFragment : BottomSheetDialogFragment() {

    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editText: TextInputLayout = view.findViewById(R.id.fragment_add_task_text_input)
        val dateTimePicker: AppCompatImageButton = view.findViewById(R.id.new_task_set_time)
        val timeTextView: MaterialTextView =
            view.findViewById(R.id.add_task_fragment_time_text_view)
        val category: Category? = arguments?.get("category") as Category?
        editText.requestFocus()
        val navController =
            activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment)
                ?.findNavController()
        var dayTime: Long? = null
        /* Save button */
        editText.setEndIconOnClickListener {
            if (category?.id != "") {
                taskViewModel.insert(
                    Task(
                        id = UUID.randomUUID().toString(),
                        title = editText.editText?.text.toString(),
                        categoryId = category?.id ?: "",
                        dateTime = dayTime,
                        createdTime = System.currentTimeMillis(),
                        content = "",
                        timeZone = "Europe/Moscow",
                    )
                )
                dayTime?.let { time ->
                    scheduleOneTimeNotification(time, editText.editText?.text.toString())
                }
            }
            editText.editText?.setText("")
            dayTime = null
            navController?.navigate(
                R.id.action_addTaskFragment_to_homeFragment,
                bundleOf("categoryId" to "notnulcategoryid")
            )
        }
        /* Date time pickers */
        dateTimePicker.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.addOnPositiveButtonClickListener {
                dayTime = datePicker.selection
                timeTextView.text =
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(dayTime)
                        ?: error("dayTime is null")
                val timePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .build()
                timePicker.addOnPositiveButtonClickListener {
                    val newHour: Int = timePicker.hour
                    val newMinute: Int = timePicker.minute
                    val plus =
                        (newHour * 3600000) + (newMinute * 60000) - TimeZone.getDefault().rawOffset
                    dayTime = dayTime?.plus(plus)
                    Log.d("PICKER", "dayTime = $dayTime")
                    Log.d("PICKER", "newHour = $newHour")
                    Log.d("PICKER", "displayName = ${TimeZone.getDefault().displayName}")
                    Log.d("PICKER", "rawOffset = ${TimeZone.getDefault().rawOffset}")


                    timeTextView.text =
                        SimpleDateFormat("MMM d\nHH:mm", Locale.getDefault()).format(dayTime)
                            ?: error("dayTime is null")
                }
                timePicker.show(childFragmentManager, "fragment_time_picker_tag")
            }
            datePicker.show(childFragmentManager, "fragment_date_picker_tag")
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetStyle
    }

    // Уведомление
    private fun scheduleOneTimeNotification(scheduledTime: Long, title: String) {
        val calendar = Calendar.getInstance()
        val diff: Long = scheduledTime - calendar.timeInMillis
        Log.d("PICKER", "calendar.timeInMillis = ${calendar.timeInMillis}")
        Log.d("PICKER", "diff = $diff")
        Log.d(
            "PICKER",
            "calendar.timeInMillis = ${
                SimpleDateFormat("MMM d\nHH:mm", Locale.getDefault()).format(calendar.timeInMillis)
            }"
        )
        val work =
            OneTimeWorkRequestBuilder<OneTimeScheduleWorker>()
                .setInputData(workDataOf(Pair("title", title)))
                .setInitialDelay(diff, TimeUnit.MILLISECONDS)
                .addTag("WORK_TAG")
                .build()
        WorkManager.getInstance(requireContext()).enqueue(work)
    }
}