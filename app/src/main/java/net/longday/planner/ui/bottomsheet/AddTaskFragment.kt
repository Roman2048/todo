package net.longday.planner.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
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
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.TaskViewModel
import net.longday.planner.work.OneTimeScheduleWorker
import java.text.SimpleDateFormat
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
                        createdTime = System.currentTimeMillis(),
                        timeZone = TimeZone.getDefault().displayName,
                        content = "",
                        dateTime = dayTime,
                    )
                )
                refreshOrder(category!!)
                dayTime?.let { time ->
                    scheduleOneTimeNotification(time, editText.editText?.text.toString())
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
                }
                timePicker.show(childFragmentManager, "fragment_time_picker_tag")
            }
            datePicker.show(childFragmentManager, "fragment_date_picker_tag")
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetStyle
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

    /**
     * Refresh ordering number in each task when new task added
     */
    private fun refreshOrder(currentCategory: Category) {
        taskViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            tasks
                .filter { it.categoryId == currentCategory.id && !it.isDone }
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
}