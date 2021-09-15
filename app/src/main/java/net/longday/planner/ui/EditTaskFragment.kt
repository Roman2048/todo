package net.longday.planner.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
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
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Reminder
import net.longday.planner.data.entity.Task
import net.longday.planner.databinding.FragmentEditTaskBinding
import net.longday.planner.viewmodel.CategoryViewModel
import net.longday.planner.viewmodel.ReminderViewModel
import net.longday.planner.viewmodel.TaskViewModel
import net.longday.planner.work.OneTimeScheduleWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class EditTaskFragment : Fragment(R.layout.fragment_edit_task) {

    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val reminderViewModel: ReminderViewModel by viewModels()

    private lateinit var taskListTitle: TextInputLayout
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var editTaskTitle: TextInputLayout
    private lateinit var editTaskContent: TextInputLayout
    private lateinit var deleteTaskButton: MaterialButton
    private lateinit var backButton: AppCompatImageButton
    private lateinit var doneCheckBox: MaterialCheckBox
    private lateinit var setTimeButton: MaterialButton
    private lateinit var prioritySwitch: SwitchMaterial

    private var sortedCategories = listOf<Category>()
    private var tasks = listOf<Task>()
    private var reminders = listOf<Reminder>()
    private var category: Category? = null
    private lateinit var task: Task
    private var taskTime: Long? = null
    private var isAllDay = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews()
        editTaskTitle.requestFocus()
        task = arguments?.get("task") as Task
        taskTime = task.dateTime
        isAllDay = task.isAllDay
        prioritySwitch.isChecked = task.priority != null
        taskViewModel.tasks.observe(viewLifecycleOwner) { tasks = it }
        reminderViewModel.reminders.observe(viewLifecycleOwner) { reminders = it }
        handleChooseCategoryTextInput()
        showDateOrTime()
        doneCheckBox.isChecked = task.isDone
        editTaskTitle.editText?.setText(task.title)
        editTaskContent.editText?.setText(task.content)
        setBackButton()
        setDeleteTaskButton(view)
        setDateTimePickerButton()
        prioritySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                task.priority = "HIGH"
            } else {
                task.priority = null
            }
        }
    }

    private fun bindViews() {
        taskListTitle = binding.fragmentEditTaskTopLabel
        autoCompleteTextView = binding.fragmentEditTaskTopLabelAutoComplete
        editTaskTitle = binding.editTaskEditText
        editTaskContent = binding.editTaskEditContent
        deleteTaskButton = binding.editTaskDeleteButton
        backButton = binding.fragmentEditTaskBackButton
        doneCheckBox = binding.fragmentEditTaskDoneCheckbox
        setTimeButton = binding.fragmentEditTaskDateTimeButton
        prioritySwitch = binding.fragmentEditTaskSwitchPriority
    }

    private fun handleChooseCategoryTextInput() {
        /* Fill autoComplete with values */
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            sortedCategories = categories.sortedBy { it.position }
            if (sortedCategories.isEmpty()) {
                taskListTitle.isEnabled = false
            }
            taskListTitle.editText?.setText(categories.first { it.id == task.categoryId }.title)
            (taskListTitle.editText as? AutoCompleteTextView)?.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.edit_task_title,
                    sortedCategories.map { it.title }
                )
            )
        }
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            category = sortedCategories[position]
            task.categoryId = category!!.id
        }
    }

    /* Set task due date or time if present */
    private fun showDateOrTime() {
        if (task.dateTime != null) {
            setTimeButton.text =
                if (task.isAllDay) {
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(task.dateTime)
                } else {
                    SimpleDateFormat("MMM d HH:mm", Locale.getDefault()).format(task.dateTime)
                }
        }
    }

    private fun setBackButton() {
        backButton.setOnClickListener {
            /* Set task orderInCategory to top position if category was changed */
            category?.let { task.orderInCategory = -1 }
            /* Create updated task */
            val editedTask = Task(
                id = task.id,
                title = editTaskTitle.editText?.text.toString(),
                categoryId = task.categoryId,
                createdTime = task.createdTime,
                timeZone = task.timeZone,
                content = editTaskContent.editText?.text.toString(),
                dateTime = taskTime,
                completedTime = if (doneCheckBox.isChecked) System.currentTimeMillis() else null,
                deletedTime = task.deletedTime,
                isDone = doneCheckBox.isChecked,
                isDeleted = task.isDeleted,
                isScheduled = task.isScheduled,
                orderInCategory = if (doneCheckBox.isChecked) -1 else task.orderInCategory,
                isAllDay = isAllDay,
                priority = if (prioritySwitch.isChecked) "HIGH" else null,
            )
            /* Update task in database */
            taskViewModel.update(editedTask)
            if (!isAllDay) {
                cancelRemindersForTask(task)
                taskTime?.let { time ->
                    if (time - Calendar.getInstance().timeInMillis > 0) {
                        val workerId =
                            scheduleOneTimeNotification(
                                time,
                                editTaskTitle.editText?.text.toString()
                            )
                        reminderViewModel.insert(
                            Reminder(
                                taskId = editedTask.id,
                                workerId = workerId.toString(),
                                time = taskTime,
                            )
                        )
                    }
                }
            }
            /* Remove reminder if task is done */
            if (doneCheckBox.isChecked) {
                cancelRemindersForTask(task)
            }
            /* Navigate to the main screen */
            findNavController().navigate(R.id.action_editTaskFragment_to_homeFragment)
            it.hideKeyboard()
        }
    }

    private fun setDeleteTaskButton(view: View) {
        deleteTaskButton.setOnClickListener {
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
    }

    private fun setDateTimePickerButton() {
        setTimeButton.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker().build()
            materialDatePicker.addOnNegativeButtonClickListener {
                taskTime = null
                isAllDay = true
                setTimeButton.text = requireContext().getString(R.string.edit_task_set_time_text)
            }
            materialDatePicker.addOnPositiveButtonClickListener {
                taskTime = materialDatePicker.selection
                isAllDay = true
                setTimeButton.text =
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(taskTime)
                val materialTimePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .build()
                materialTimePicker.addOnPositiveButtonClickListener {
                    val newHour: Int = materialTimePicker.hour
                    val newMinute: Int = materialTimePicker.minute
                    val plus =
                        (newHour * 3600000) + (newMinute * 60000) - TimeZone.getDefault().rawOffset
                    taskTime = taskTime?.plus(plus)
                    isAllDay = false
                    setTimeButton.text =
                        SimpleDateFormat("MMM d HH:mm", Locale.getDefault()).format(taskTime)
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

    /* Crete new reminder for task */
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