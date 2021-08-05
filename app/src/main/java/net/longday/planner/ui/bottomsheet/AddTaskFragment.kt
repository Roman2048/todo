package net.longday.planner.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.TaskViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@AndroidEntryPoint
class AddTaskFragment : BottomSheetDialogFragment() {

    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editText: TextInputLayout = view.findViewById(R.id.fragment_add_task_text_input)
        val saveButton: AppCompatImageButton = view.findViewById(R.id.fragment_add_task_save_button)
        val dateTimePicker: AppCompatImageButton = view.findViewById(R.id.new_task_set_time)
        val category: Category? = arguments?.get("category") as Category?
        editText.requestFocus()
        val navController =
            activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment)
                ?.findNavController()
        var dayTime: Long? = null
        /* Save button */
        saveButton.setOnClickListener {
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
                    val plus = (newHour * 3600000) + (newMinute * 60000)
                    dayTime = dayTime?.plus(plus)
                }
                materialTimePicker.show(childFragmentManager, "fragment_time_picker_tag")
            }
            materialDatePicker.show(childFragmentManager, "fragment_date_picker_tag")
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetStyle
    }
}