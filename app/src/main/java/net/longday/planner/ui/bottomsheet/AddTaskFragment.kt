package net.longday.planner.ui.bottomsheet

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
import net.longday.planner.data.entity.Reminder
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.ReminderViewModel
import net.longday.planner.viewmodel.TaskViewModel
import net.longday.planner.work.OneTimeScheduleWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class AddTaskFragment : BottomSheetDialogFragment() {

    private val taskViewModel: TaskViewModel by viewModels()

    private val reminderViewModel: ReminderViewModel by viewModels()

    private var tasks = listOf<Task>()

    private lateinit var priorityButton: AppCompatImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        priorityButton = view.findViewById(R.id.add_task_fragment_set_priority)
        var priority = false
        taskViewModel.tasks.observe(viewLifecycleOwner) { tasks = it }
        super.onViewCreated(view, savedInstanceState)
        val editText: TextInputLayout = view.findViewById(R.id.fragment_add_task_text_input)
        val dateTimePicker: AppCompatImageButton = view.findViewById(R.id.new_task_set_time)
        val timeTextView: MaterialTextView =
            view.findViewById(R.id.add_task_fragment_time_text_view)
        val category: Category? = arguments?.get("category") as Category?
        val intent: Intent? = arguments?.get("intent") as Intent?
        intent.let {
            if (intent != null) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    editText.editText?.setText(it)
                }
                intent.action = ""
            }
        }
        editText.requestFocus()
        val navController =
            activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment)
                ?.findNavController()
        var dayTime: Long? = null
        var isAllDay = true
        /* Save button */
        editText.setEndIconOnClickListener {
            if (category?.id != "") {
                val newTask = Task(
                    id = UUID.randomUUID().toString(),
                    title = editText.editText?.text.toString(),
                    categoryId = category?.id ?: "",
                    createdTime = System.currentTimeMillis(),
                    timeZone = TimeZone.getDefault().id,
                    content = "",
                    dateTime = dayTime,
                    isAllDay = isAllDay,
                    priority = if (priority) "HIGH" else null
                )
                taskViewModel.insert(newTask)

                refreshOrder(category!!)
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
            editText.editText?.setText("")
            dayTime = null
            navController?.navigate(R.id.action_addTaskFragment_to_homeFragment)
        }
        /* Date time pickers */
        dateTimePicker.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.addOnPositiveButtonClickListener {
                dayTime = datePicker.selection
                isAllDay = true
                timeTextView.text =
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(dayTime)
                val timePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .build()
                timePicker.addOnPositiveButtonClickListener {
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
        priorityButton.setOnClickListener {
            priority = !priority
            if (priority) {
                priorityButton.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        android.R.color.holo_green_dark
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                priorityButton.colorFilter = null
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetStyle
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