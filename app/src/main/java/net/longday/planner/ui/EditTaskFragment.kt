package net.longday.planner.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.TaskViewModel
import net.longday.planner.work.OneTimeScheduleWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class EditTaskFragment : Fragment(R.layout.fragment_edit_task) {

    private val taskViewModel: TaskViewModel by viewModels()

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
                        .navigate(R.id.action_editTaskFragment_to_homeFragment)
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val editText: TextInputLayout = view.findViewById(R.id.edit_task_edit_text)
        val deleteButton: MaterialButton = view.findViewById(R.id.edit_task_delete_button)
        val backButton: AppCompatImageButton =
            view.findViewById(R.id.fragment_edit_task_back_button)
        val doneCheckBox: MaterialCheckBox =
            view.findViewById(R.id.fragment_edit_task_done_checkbox)
        val setTimeButton: MaterialButton =
            view.findViewById(R.id.fragment_edit_task_date_time_button)
        val task: Task = arguments?.get("task") as Task
        var dayTime: Long? = null
        if (task.dateTime != null) {
            setTimeButton.text =
                if (SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                    ).format(task.dateTime) == "00:00"
                ) {
                    // Если дата то показываем как есть
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(task.dateTime)
                } else {
                    SimpleDateFormat("MMM d HH:mm", Locale.getDefault()).format(task.dateTime)
                }
        }
        doneCheckBox.isChecked = task.isDone
        editText.editText?.setText(task.title)
        editText.requestFocus()

        backButton.setOnClickListener {
            taskViewModel.update(
                Task(
                    id = task.id,
                    timeZone = "Europe/Moscow",
                    title = editText.editText?.text.toString(),
                    categoryId = task.categoryId,
                    dateTime = if (dayTime == null) task.dateTime else dayTime,
                    isDone = doneCheckBox.isChecked,
                    orderInCategory = task.orderInCategory,
                )
            )
            dayTime?.let { time ->
                scheduleOneTimeNotification(time, editText.editText?.text.toString())
            }
            view.findNavController().navigate(
                R.id.action_editTaskFragment_to_homeFragment,
                bundleOf("categoryId" to task.categoryId)
            )
            it.hideKeyboard()
        }
        deleteButton.setOnClickListener {
            taskViewModel.delete(task)
            view.findNavController().navigate(R.id.action_editTaskFragment_to_homeFragment)
            it.hideKeyboard()
        }

        setTimeButton.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build()
            materialDatePicker.addOnPositiveButtonClickListener {
                dayTime = materialDatePicker.selection
                val materialTimePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .build()
                materialTimePicker.addOnPositiveButtonClickListener {
                    val newHour: Int = materialTimePicker.hour
                    val newMinute: Int = materialTimePicker.minute
                    val plus =
                        (newHour * 3600000) + (newMinute * 60000) - TimeZone.getDefault().rawOffset
                    dayTime = dayTime?.plus(plus)
                    setTimeButton.text =
                        SimpleDateFormat("MMM d HH:mm", Locale.getDefault()).format(dayTime)
                }
                materialTimePicker.show(childFragmentManager, "fragment_time_picker_tag")
            }
            materialDatePicker.show(childFragmentManager, "fragment_date_picker_tag")
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    // Create Notification
    private fun scheduleOneTimeNotification(scheduledTime: Long, title: String) {
        val diff: Long = scheduledTime - Calendar.getInstance().timeInMillis
        val work = OneTimeWorkRequestBuilder<OneTimeScheduleWorker>()
            .setInputData(workDataOf(Pair("content", title)))
            .setInitialDelay(diff, TimeUnit.MILLISECONDS)
            .addTag("WORK_TAG")
            .build()
        WorkManager.getInstance(requireContext()).enqueue(work)
    }
}