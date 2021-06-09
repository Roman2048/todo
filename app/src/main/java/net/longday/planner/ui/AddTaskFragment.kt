package net.longday.planner.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.TaskViewModel
import java.time.LocalDateTime
import java.util.*

@AndroidEntryPoint
class AddTaskFragment : Fragment(R.layout.fragment_add_task) {

    private val taskViewModel: TaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val textEdit: EditText = view.findViewById(R.id.add_task_edit_text)
        val saveButton: Button = view.findViewById(R.id.add_task_save_button)
        val backButton: Button = view.findViewById(R.id.add_task_back_button)
        val categoryId: String = arguments?.get("category_id").toString()

        saveButton.setOnClickListener {
            if (categoryId != "") {
                taskViewModel.insert(
                    Task(
                        id = UUID.randomUUID().toString(),
                        title = textEdit.text.toString(),
                        categoryId = categoryId,
                        dateTime = System.currentTimeMillis(),
                    )
                )
            }
            view.findNavController()
                .navigate(R.id.action_addTaskFragment_to_homeFragment)
            it.hideKeyboard()
        }

        backButton.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_addTaskFragment_to_homeFragment)
            it.hideKeyboard()
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }
}