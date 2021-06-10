package net.longday.planner.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.TaskViewModel

@AndroidEntryPoint
class UpdateTaskFragment : Fragment(R.layout.fragment_update_task) {

    private val taskViewModel: TaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val editText: EditText = view.findViewById(R.id.edit_task_edit_text)
        val saveButton: Button = view.findViewById(R.id.edit_task_save_button)
        val deleteButton: Button = view.findViewById(R.id.edit_task_delete_button)
        val backButton: Button = view.findViewById(R.id.edit_task_back_button)
        val undoneButton: Button = view.findViewById(R.id.edit_task_undone_button)
        val setTimeButton: Button = view.findViewById(R.id.edit_task_set_time_button)
        val task: Task = arguments?.get("task") as Task
        if (!task.isDone) {
            undoneButton.text = "Done"
        }
        editText.setText(task.title)
        var dayTime: Long? = null
        saveButton.setOnClickListener {
            taskViewModel.update(
                Task(
                    task.id,
                    editText.text.toString(),
                    task.categoryId,
                    if (dayTime == null) task.dateTime else dayTime,
                )
            )
            view.findNavController().navigate(R.id.action_editTaskFragment_to_homeFragment)
            it.hideKeyboard()
        }
        deleteButton.setOnClickListener {
            taskViewModel.delete(task)
            view.findNavController().navigate(R.id.action_editTaskFragment_to_homeFragment)
            it.hideKeyboard()
        }
        backButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_editTaskFragment_to_homeFragment)
            it.hideKeyboard()
        }

        undoneButton.setOnClickListener {
            taskViewModel.update(
                Task(
                    task.id,
                    editText.text.toString(),
                    task.categoryId,
                    task.dateTime,
                    isDone = !task.isDone,
                )
            )
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
                    val plus = (newHour * 3600000) + (newMinute * 60000)
                    dayTime = dayTime?.plus(plus)
//                            Toast.makeText(requireContext(), "newHour = $newHour, newMinute = $newMinute", Toast.LENGTH_LONG).show()
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
}