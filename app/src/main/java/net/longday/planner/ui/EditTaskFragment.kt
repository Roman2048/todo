package net.longday.planner.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
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
import net.longday.planner.data.entity.Reminder
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.ReminderViewModel
import net.longday.planner.viewmodel.TaskViewModel
import net.longday.planner.work.OneTimeScheduleWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class EditTaskFragment : Fragment(R.layout.fragment_edit_task) {

    private val taskViewModel: TaskViewModel by viewModels()

    private val reminderViewModel: ReminderViewModel by viewModels()

    private var reminders = listOf<Reminder>()

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
        reminderViewModel.reminders.observe(viewLifecycleOwner) {
            reminders = it
        }
        val editText: TextInputLayout = view.findViewById(R.id.edit_task_edit_text)
        val editContent: TextInputLayout = view.findViewById(R.id.edit_task_edit_content)
        val deleteButton: MaterialButton = view.findViewById(R.id.edit_task_delete_button)
        val backButton: AppCompatImageButton =
            view.findViewById(R.id.fragment_edit_task_back_button)
        val doneCheckBox: MaterialCheckBox =
            view.findViewById(R.id.fragment_edit_task_done_checkbox)
        val setTimeButton: MaterialButton =
            view.findViewById(R.id.fragment_edit_task_date_time_button)
        val task: Task = arguments?.get("task") as Task
        var dayTime: Long? = task.dateTime
        var isAllDay = task.isAllDay
        if (task.dateTime != null) {
            setTimeButton.text =
                if (task.isAllDay) {
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(task.dateTime)
                } else {
                    SimpleDateFormat("MMM d HH:mm", Locale.getDefault()).format(task.dateTime)
                }
        }
        doneCheckBox.isChecked = task.isDone
        if (task.isDone) {
            doneCheckBox.text = getString(R.string.fragment_edit_task_checkbox_text_done)
        }
        doneCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                doneCheckBox.text = getString(R.string.fragment_edit_task_checkbox_text_done)
            } else {
                doneCheckBox.text = getString(R.string.fragment_edit_task_checkbox_text_active)
            }
        }
        editText.editText?.setText(task.title)
        editContent.editText?.setText(task.content)
        editText.requestFocus()

        backButton.setOnClickListener {
            val editedTask = Task(
                id = task.id,
                title = editText.editText?.text.toString(),
                categoryId = task.categoryId,
                createdTime = task.createdTime,
                timeZone = task.timeZone,
                content = editContent.editText?.text.toString(),
                dateTime = dayTime,
                completedTime = if (doneCheckBox.isChecked) System.currentTimeMillis() else null,
                deletedTime = task.deletedTime,
                isDone = doneCheckBox.isChecked,
                isDeleted = task.isDeleted,
                isScheduled = task.isScheduled,
                orderInCategory = if (doneCheckBox.isChecked) -1 else task.orderInCategory,
                isAllDay = isAllDay,
            )
            taskViewModel.update(editedTask)
            if (!isAllDay) {
                cancelRemindersForTask(task)
                dayTime?.let { time ->
                    if (time - Calendar.getInstance().timeInMillis > 0) {
                        val workerId =
                            scheduleOneTimeNotification(time, editText.editText?.text.toString())
                        reminderViewModel.insert(
                            Reminder(
                                taskId = editedTask.id,
                                workerId = workerId.toString(),
                                time = dayTime,
                            )
                        )
                    }
                }
            }
            if (doneCheckBox.isChecked) {
                cancelRemindersForTask(task)
            }
            view.findNavController().navigate(
                R.id.action_editTaskFragment_to_homeFragment,
                bundleOf("categoryId" to task.categoryId)
            )
            it.hideKeyboard()
        }
        deleteButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.delete_done_task_dialog_title))
            builder.setMessage(getString(R.string.delete_task_dialog_message))
            builder.setPositiveButton(
                getString(R.string.fragment_edit_category_delete_button_text)
            ) { _, _ ->
                task.deletedTime = System.currentTimeMillis()
                task.isDeleted = true
                taskViewModel.update(task)
                cancelRemindersForTask(task)
                view.findNavController().navigate(R.id.action_editTaskFragment_to_homeFragment)
                it.hideKeyboard()
            }
            builder.setNegativeButton(
                getString(R.string.delete_done_task_dialog_cancel_button_text)
            ) { _, _ -> }
            builder.show()
        }

        setTimeButton.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker().build()
            materialDatePicker.addOnNegativeButtonClickListener {
                dayTime = null
                isAllDay = true
                setTimeButton.text = requireContext().getString(R.string.edit_task_set_time_text)
            }
            materialDatePicker.addOnPositiveButtonClickListener {
                dayTime = materialDatePicker.selection
                isAllDay = true
                setTimeButton.text =
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(dayTime)
                val materialTimePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .build()
                materialTimePicker.addOnPositiveButtonClickListener {
                    val newHour: Int = materialTimePicker.hour
                    val newMinute: Int = materialTimePicker.minute
                    val plus =
                        (newHour * 3600000) + (newMinute * 60000) - TimeZone.getDefault().rawOffset
                    dayTime = dayTime?.plus(plus)
                    isAllDay = false
                    setTimeButton.text =
                        SimpleDateFormat("MMM d HH:mm", Locale.getDefault()).format(dayTime)
                }
                materialTimePicker.show(childFragmentManager, "fragment_time_picker_tag")
            }
            materialDatePicker.show(childFragmentManager, "fragment_date_picker_tag")
        }
    }

    /**
     * Cancel active notifications if the task is done or marked as deleted
     */
    private fun cancelRemindersForTask(task: Task) {
        reminders
            .filter { reminder -> reminder.taskId == task.id }
            .forEach { reminder ->
                WorkManager.getInstance(requireContext())
                    .cancelWorkById(UUID.fromString(reminder.workerId))
                reminderViewModel.delete(reminder)
            }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    // Create Notification
    private fun scheduleOneTimeNotification(scheduledTime: Long, title: String): UUID {
        val diff: Long = scheduledTime - Calendar.getInstance().timeInMillis
        val work = OneTimeWorkRequestBuilder<OneTimeScheduleWorker>()
            .setInputData(workDataOf(Pair("content", title)))
            .setInitialDelay(diff, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(requireContext()).enqueue(work)
        return work.id
    }
}