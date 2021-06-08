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
        val task: Task = arguments?.get("task") as Task
        editText.setText(task.title)
        saveButton.setOnClickListener {
            taskViewModel.update(
                Task(
                    task.id,
                    editText.text.toString(),
                    task.categoryId,
                    task.dateTime,
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
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
}