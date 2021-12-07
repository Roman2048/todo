package net.longday.planner.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.SubtaskAdapter
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
    private lateinit var editTaskTitle: AppCompatEditText
    private lateinit var editTaskContent: AppCompatEditText
    private lateinit var deleteTaskButton: MaterialButton
    private lateinit var backButton: AppCompatImageButton
    private lateinit var doneCheckBox: MaterialCheckBox
    private lateinit var setTimeButton: MaterialTextView
    private lateinit var prioritySwitch: SwitchMaterial
    private lateinit var resetTimeButton: AppCompatImageButton
    private lateinit var shareButton: MaterialButton
    private lateinit var focusSwitch: SwitchMaterial
    private lateinit var createdTextView: MaterialTextView
    private lateinit var cancelButton: MaterialButton
    private lateinit var subtaskRecycler: RecyclerView


    private var sortedCategories = listOf<Category>()
    private var tasks = listOf<Task>()
    private var reminders = listOf<Reminder>()
    private var category: Category? = null
    private lateinit var task: Task
    private var taskTime: Long? = null
    private var isAllDay = true
    private var categoryChanged = false
    private var isUpdating = false

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

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
                private var rememberedActionState = ACTION_STATE_IDLE
                private var from = -1
                private var to = -1
                private var isDraggable = true
                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    return if (isDraggable) super.getMovementFlags(recyclerView, viewHolder) else 0
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    if (from == -1) from = viewHolder.adapterPosition
                    if (from == recyclerView.adapter?.itemCount?.minus(1)) {
                        return false
                    }
                    if (target.adapterPosition != recyclerView.adapter?.itemCount?.minus(1)) {
                        to = target.adapterPosition
                        recyclerView.adapter?.notifyItemMoved(
                            viewHolder.adapterPosition,
                            target.adapterPosition
                        )
                    } else {
                        to = target.adapterPosition - 1
                        recyclerView.adapter?.notifyItemMoved(
                            viewHolder.adapterPosition,
                            target.adapterPosition - 1
                        )
                    }
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    super.onSelectedChanged(viewHolder, actionState)
                    if (rememberedActionState == ACTION_STATE_DRAG
                        && actionState == ACTION_STATE_IDLE
                        && from != -1
                        && to != -1
                        && from != to
                    ) {
                        Log.d("DRAGDRAG", "drag and drop $from to $to")
                        moveItems(from, to)
                    }
                    rememberedActionState = actionState
                    from = -1
                    to = -1
                }
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }

    fun moveItems(from: Int, to: Int) {

//        subtaskRecycler.adapter?.notifyItemMoved(from, to)
        val subtasks = tasks
            .filter { currentTask -> currentTask.parentTaskId == task.id }
            .sortedBy { it.orderInTask }
        Log.d("SORTSORT", "до изменения \n${subtasks.joinToString { "${it.title} :${it.orderInTask}\n" }}")
//        subtasks.forEachIndexed { index, task ->
//            task.orderInTask = index + 1
//        }
//        subtasks.forEach { taskViewModel.update(it) }

        val mutableSortedSubTasks = subtasks.toMutableList()
        val itemToMove = subtasks[from]
        mutableSortedSubTasks.removeAt(from)
        mutableSortedSubTasks.add(to, itemToMove)

        mutableSortedSubTasks.forEachIndexed { index, task ->
            task.orderInTask = index
        }
        isUpdating = true

        mutableSortedSubTasks.forEach {
            isUpdating = true
            taskViewModel.update(it)
        }
        isUpdating = false
//        subtaskRecycler.adapter?.notifyItemMoved(from, to)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews()
        task = arguments?.get("task") as Task
        if (task.parentTaskId != null) {
            binding.taskDetailsSubtasksImageView.visibility = View.GONE
            subtaskRecycler.visibility = View.GONE
        }
//        itemTouchHelper.attachToRecyclerView(subtaskRecycler)
        val openTaskDetails: (task: Task) -> Unit = {
            findNavController().navigate(R.id.editTaskFragment, bundleOf("task" to it))
        }
        val updateTask: (task: Task) -> Unit = { taskViewModel.update(it) }
        val addSubTask: () -> Unit = {
            try {
                findNavController().navigate(
                    R.id.addTaskFragment,
                    bundleOf("category" to category, "task" to task)
                )
            } catch (e: IllegalArgumentException) {
                println(e)
            }
        }
        val subtaskAdapter = SubtaskAdapter(openTaskDetails, updateTask, addSubTask)
        subtaskRecycler.adapter = subtaskAdapter
        taskTime = task.dateTime
        isAllDay = task.isAllDay
        prioritySwitch.isChecked = task.priority != null
        if (task.isDone) cancelButton.isEnabled = false
        if (task.isCanceled) {
            binding.fragmentEditTaskDoneCheckbox.isEnabled = false
            binding.editTaskCancelButton.text = getString(R.string.mark_active)
            binding.editTaskInfoCanceledTitle.visibility = View.VISIBLE
            binding.editTaskCancelTime.visibility = View.VISIBLE
            binding.editTaskCancelTime.text =
                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(task.cancelTime)
            if (!task.cancelReason.isNullOrBlank()) {
                binding.editTaskInfoCancelReasonTitle.visibility = View.VISIBLE
                binding.editTaskCancelReasonText.visibility = View.VISIBLE
                binding.editTaskCancelReasonText.text = task.cancelReason
            }
        } else {
            binding.editTaskInfoCanceledTitle.visibility = View.GONE
            binding.editTaskCancelTime.visibility = View.GONE
            binding.editTaskInfoCancelReasonTitle.visibility = View.GONE
            binding.editTaskCancelReasonText.visibility = View.GONE
        }
        taskViewModel.tasks.observe(viewLifecycleOwner) {
            if (isUpdating) return@observe
            tasks = it
            val subtasks = tasks
                .filter { currentTask -> currentTask.parentTaskId == task.id }
                .sortedBy { t -> t.orderInTask }
            Log.d("SORTSORT", "\n${subtasks.joinToString { "${it.title} :${it.orderInTask}\n" }}")
            subtaskAdapter.submitList(subtasks)
        }
        reminderViewModel.reminders.observe(viewLifecycleOwner) { reminders = it }
        handleChooseCategoryTextInput()
        showDateOrTime()
        doneCheckBox.isChecked = task.isDone
        focusSwitch.isChecked = task.isFocused
        editTaskTitle.setText(task.title)
        editTaskContent.setText(task.content)
        setBackButton()
        setDeleteTaskButton()
        setDateTimePickerButton()
        shareButton.setOnClickListener {
            startActivity(
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, task.title)
                    type = "text/plain"
                }
            )
        }
        prioritySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                task.priority = "HIGH"
            } else {
                task.priority = null
            }
        }
        if (taskTime == null) {
            resetTimeButton.visibility = View.GONE
        }
        resetTimeButton.setOnClickListener {
            taskTime = null
            isAllDay = true
            setTimeButton.text = requireContext().getString(R.string.edit_task_set_time_text)
            resetTimeButton.visibility = View.GONE
        }
        focusSwitch.setOnClickListener {
            task.isFocused = focusSwitch.isChecked
        }
        createdTextView.text =
            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(task.createdTime)
        setCancelButton()
        doneCheckBox.setOnCheckedChangeListener { _, isChecked ->
            cancelButton.isEnabled = !isChecked
        }
    }

    private fun setCancelButton() {
        cancelButton.setOnClickListener {
            if (task.isCanceled) {
                task.isCanceled = false
                task.cancelReason = null
                task.cancelTime = null
                taskViewModel.update(task)
                cancelButton.text = getString(R.string.cancel)
                doneCheckBox.isEnabled = true
            } else {
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(R.layout.cancel_task)
                    .create()
                dialog.show()
                val textInput =
                    dialog.findViewById<TextInputEditText>(R.id.cancel_task_reason_text_input)
                val okButton = dialog.findViewById<MaterialButton>(R.id.cancel_task_ok_button)
                val backButton = dialog.findViewById<MaterialButton>(R.id.cancel_task_back_button)
                okButton?.setOnClickListener {
                    task.isDone = false
                    doneCheckBox.isChecked = false
                    doneCheckBox.isEnabled = false
                    task.isCanceled = true
                    task.cancelReason = textInput?.text.toString()
                    task.cancelTime = System.currentTimeMillis()
                    taskViewModel.update(task)
                    dialog.cancel()
                    cancelButton.text = getString(R.string.mark_active)
                }
                backButton?.setOnClickListener {
                    dialog.cancel()
                }
            }
        }
    }

    private fun bindViews() {
        taskListTitle = binding.fragmentEditTaskTopLabel
        autoCompleteTextView = binding.fragmentEditTaskTopLabelAutoComplete
        editTaskTitle = binding.taskDetailsTitleEditText
        editTaskContent = binding.taskDetailsDetailsEditText
        deleteTaskButton = binding.editTaskDeleteButton
        backButton = binding.fragmentEditTaskBackButton
        doneCheckBox = binding.fragmentEditTaskDoneCheckbox
        setTimeButton = binding.taskDetailsAddDateAndTimeTextView
        prioritySwitch = binding.fragmentEditTaskSwitchPriority
        resetTimeButton = binding.taskDetailsResetDateAndTimeImageButton
        shareButton = binding.editTaskShareButton
        focusSwitch = binding.editTaskFocusButton
        createdTextView = binding.editTaskCreatedTime
        cancelButton = binding.editTaskCancelButton
        subtaskRecycler = binding.editTaskSubtaskRecycler
    }

    private fun handleChooseCategoryTextInput() {
        /* Fill autoComplete with values */
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            sortedCategories = categories.sortedBy { it.position }
            if (sortedCategories.isEmpty()) {
                taskListTitle.isEnabled = false
            }
            category = categories.first { it.id == task.categoryId }
            taskListTitle.editText?.setText(category?.title ?: "")
            (taskListTitle.editText as? AutoCompleteTextView)?.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.edit_task_title,
                    sortedCategories.map { it.title }
                )
            )
        }
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            categoryChanged = true
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
            if (doneCheckBox.isChecked && task.isCanceled) task.isDone = false
            /* Set task orderInCategory to top position if category was changed */
            if (categoryChanged) {
                category?.let { task.orderInCategory = -1 }
                categoryChanged = false
            }
            /* Create updated task */
            val editedTask = task
            editedTask.title = editTaskTitle.text.toString()
            editedTask.content = editTaskContent.text.toString()
            editedTask.dateTime = taskTime
            editedTask.completedTime =
                if (doneCheckBox.isChecked) System.currentTimeMillis() else null
            editedTask.isDone = doneCheckBox.isChecked
            editedTask.orderInCategory = if (doneCheckBox.isChecked) -1 else task.orderInCategory
            editedTask.isAllDay = isAllDay
            editedTask.priority = if (prioritySwitch.isChecked) "HIGH" else null
            /* Update task in database */
            taskViewModel.update(editedTask)
            if (!isAllDay) {
                cancelRemindersForTask(task)
                taskTime?.let { time ->
                    if (time - Calendar.getInstance().timeInMillis > 0) {
                        val workerId =
                            scheduleOneTimeNotification(
                                time,
                                editTaskTitle.text.toString()
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
            findNavController().popBackStack()
//            try {
//                findNavController().navigate(R.id.action_editTaskFragment_to_homeFragment)
//            } catch (e: IllegalArgumentException) {
//            }
            it.hideKeyboard()
        }
    }

    private fun setDeleteTaskButton() {
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

                findNavController().popBackStack()
                it.hideKeyboard()
//                try {
//                    findNavController().navigate(R.id.action_editTaskFragment_to_homeFragment)
//                } catch (e: IllegalArgumentException) {
//                }
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
            materialDatePicker.addOnPositiveButtonClickListener {
                resetTimeButton.visibility = View.VISIBLE
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

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        imm.hideSoftInputFromWindow(this.windowToken, 0)
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