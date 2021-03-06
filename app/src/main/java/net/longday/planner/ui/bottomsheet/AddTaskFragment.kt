package net.longday.planner.ui.bottomsheet

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.R.style.AddTaskBottomSheetStyle
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Reminder
import net.longday.planner.data.entity.Task
import net.longday.planner.databinding.FragmentAddTaskBinding
import net.longday.planner.viewmodel.CategoryViewModel
import net.longday.planner.viewmodel.ReminderViewModel
import net.longday.planner.viewmodel.TaskViewModel
import net.longday.planner.work.OneTimeScheduleWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class AddTaskFragment : DialogFragment(R.layout.fragment_add_task) {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private val reminderViewModel: ReminderViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    private lateinit var priorityButton: AppCompatImageButton
    private lateinit var resetTimeButton: AppCompatImageButton
    private lateinit var chooseCategoryTextInput: TextInputLayout
    private lateinit var chooseCategoryAutoComplete: AutoCompleteTextView
    private lateinit var editText: TextInputLayout
    private lateinit var dateTimePicker: AppCompatImageButton
    private lateinit var timeTextView: MaterialTextView
    private lateinit var saveButton: AppCompatImageButton
    private lateinit var addContentButton: AppCompatImageButton

    private var tasks = listOf<Task>()
    private var sortedCategories = listOf<Category>()
    private var category: Category? = null
    private var priority: Boolean = false
    private var dayTime: Long? = null
    private var isAllDay = true
    private var intent: Intent? = null
    private var isContentInputVisible = false
    private var parentTask: Task? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("isContentInputVisible", isContentInputVisible)
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isContentInputVisible = savedInstanceState?.getBoolean("isContentInputVisible") ?: false
        binding.addTaskContentTextInputLayout.visibility =
            if (isContentInputVisible) VISIBLE else GONE
        fixLayoutDefaults()
        bindViews()
        editText.requestFocus()
        view.postDelayed({ view.showKeyboard() }, 100)
        taskViewModel.tasks.observe(viewLifecycleOwner) { tasks = it }
        category = arguments?.get("category") as Category?
        parentTask = arguments?.get("task") as Task?
        chooseCategoryTextInput.editText?.setText(category?.title ?: "")
        hideResetButtonIfNoDayTime()
        handlePlainTextIntent()
        handleChooseCategoryTextInput()
        setResetTimeButton()
        setDateTimePickerButton()
        setPriorityButton()
        setSaveButton()
        setAddContentButton()
    }

    private fun setAddContentButton() {
        addContentButton.setOnClickListener {
            binding.addTaskContentTextInputLayout.visibility =
                if (isContentInputVisible) GONE else VISIBLE
            isContentInputVisible = !isContentInputVisible
            binding.addTaskContentTextInputLayoutEditText.text = null
        }
    }

    private fun setSaveButton() {
        saveButton.setOnClickListener {
            if (category?.id != "") {
                val newTask = Task(
                    id = UUID.randomUUID().toString(),
                    title = editText.editText?.text.toString(),
                    categoryId = category?.id ?: parentTask?.categoryId ?: "",
                    createdTime = System.currentTimeMillis(),
                    timeZone = TimeZone.getDefault().id,
                    content = binding.addTaskContentTextInputLayoutEditText.text.toString(),
                    dateTime = dayTime,
                    isAllDay = isAllDay,
                    priority = if (priority) "HIGH" else null,
                    parentTaskId = parentTask?.id
                )
                taskViewModel.insert(newTask)
                category?.let { category -> refreshOrder(category) }
                createNotification(newTask)
            }
            editText.editText?.setText("")
            dayTime = null
            findNavController().popBackStack()
        }
    }

    private fun fixLayoutDefaults() {
        requireDialog().window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
        requireDialog().window?.setGravity(Gravity.BOTTOM)
    }

    private fun bindViews() {
        priorityButton = binding.addTaskFragmentSetPriority
        resetTimeButton = binding.fragmentAddTaskResetTimeButton
        chooseCategoryTextInput = binding.fragmentAddTaskChooseCategory
        chooseCategoryAutoComplete = binding.fragmentAddTaskChooseCategoryAutoComplete
        editText = binding.fragmentAddTaskTextInput
        dateTimePicker = binding.newTaskSetTime
        timeTextView = binding.addTaskFragmentTimeTextView
        saveButton = binding.addTaskSaveButton
        addContentButton = binding.newTaskAddContentButton
    }

    private fun hideResetButtonIfNoDayTime() {
        if (dayTime == null) {
            resetTimeButton.visibility = View.GONE
        }
    }

    /**
     * Set new task title if user send text to the app
     */
    private fun handlePlainTextIntent() {
        intent = arguments?.get("intent") as Intent?
        editText.editText?.setText(intent?.getStringExtra(Intent.EXTRA_TEXT) ?: "")
        intent?.action = ""
    }

    private fun handleChooseCategoryTextInput() {
        /* Fill autoComplete with values */
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            sortedCategories = categories.sortedBy { it.position }
            if (sortedCategories.isEmpty()) {
                chooseCategoryTextInput.isEnabled = false
            }
            (chooseCategoryTextInput.editText as? AutoCompleteTextView)?.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.edit_task_title,
                    sortedCategories.map { it.title }
                )
            )
        }
        chooseCategoryAutoComplete.setOnItemClickListener { _, _, position, _ ->
            category = sortedCategories[position]
            // Unselect input after choosing category
            chooseCategoryTextInput.clearFocus()
        }
    }

    private fun setResetTimeButton() {
        resetTimeButton.setOnClickListener {
            dayTime = null
            isAllDay = true
            timeTextView.text = ""
            resetTimeButton.visibility = View.GONE
        }
    }

    /* Date time pickers */
    private fun setDateTimePickerButton() {
        dateTimePicker.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.addOnNegativeButtonClickListener {
                it.postDelayed({ it.showKeyboard() }, 100)
            }
            datePicker.addOnPositiveButtonClickListener {
                resetTimeButton.visibility = View.VISIBLE
                dayTime = datePicker.selection
                isAllDay = true
                timeTextView.text =
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(dayTime)
                val timePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .build()
                timePicker.addOnNegativeButtonClickListener {
                    it.postDelayed({ it.showKeyboard() }, 100)
                }
                timePicker.addOnPositiveButtonClickListener {
                    it.postDelayed({ it.showKeyboard() }, 100)
                    val newHour: Int = timePicker.hour
                    val newMinute: Int = timePicker.minute
                    val plus =
                        (newHour * 3600000) + (newMinute * 60000) - TimeZone.getDefault().rawOffset
                    dayTime = dayTime?.plus(plus)
                    timeTextView.text =
                        SimpleDateFormat("MMM d\nHH:mm", Locale.getDefault()).format(dayTime)
                    isAllDay = false
                }
                timePicker.show(childFragmentManager, "fragment_time_picker_tag")
            }
            datePicker.show(childFragmentManager, "fragment_date_picker_tag")
        }
    }

    private fun createNotification(newTask: Task) {
        if (!isAllDay) {
            dayTime?.let { time ->
                if (time - Calendar.getInstance().timeInMillis > 0) {
                    val workerId =
                        scheduleOneTimeNotification(
                            time,
                            editText.editText?.text.toString()
                        )
                    reminderViewModel.insert(
                        Reminder(
                            taskId = newTask.id,
                            workerId = workerId.toString(),
                            time = dayTime,
                        )
                    )
                }
            }
        }
    }

    private fun setPriorityButton() {
        priorityButton.setOnClickListener {
            priority = !priority
            if (priority) {
                priorityButton.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        android.R.color.holo_green_dark
                    ), PorterDuff.Mode.SRC_IN
                )
            } else {
                priorityButton.colorFilter = null
            }
        }
    }

    /**
     * Set BottomSheetDialog theme
     */
    override fun getTheme() = AddTaskBottomSheetStyle

    /**
     * Show keyboard when the dialog is opened
     */
    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(SHOW_IMPLICIT, 0)
    }

    /**
     * Schedule One-time notification
     */
    private fun scheduleOneTimeNotification(scheduledTime: Long, title: String): UUID {
        val diff: Long = scheduledTime - Calendar.getInstance().timeInMillis
        val work = OneTimeWorkRequestBuilder<OneTimeScheduleWorker>()
            .setInputData(workDataOf(Pair("content", title)))
            .setInitialDelay(diff, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(requireContext()).enqueue(work)
        return work.id
    }

    /**
     * Refresh ordering number in each task when new task added
     */
    private fun refreshOrder(currentCategory: Category) {
        tasks.filter { it.categoryId == currentCategory.id && !it.isDone }
            .sortedBy { it.orderInCategory }
            .toMutableList()
            .forEachIndexed { index, category ->
                category.orderInCategory = index
                taskViewModel.update(
                    category
                )
            }
    }
}