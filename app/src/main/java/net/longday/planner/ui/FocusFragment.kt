package net.longday.planner.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.FocusAdapter
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.TaskViewModel

@AndroidEntryPoint
class FocusFragment : Fragment(R.layout.fragment_focus) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: AppCompatImageButton
    private lateinit var clearButton: AppCompatImageButton

    private val taskViewModel: TaskViewModel by viewModels()
    private var focusedTasks: List<Task> = listOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setBackButton()
        setClearButton()
        val openTaskDetails: (task: Task) -> Unit = {
            findNavController().navigate(R.id.editTaskFragment, bundleOf("task" to it))
        }
        val adapter = FocusAdapter(openTaskDetails)
        recyclerView.adapter = adapter
        taskViewModel.tasks.observe(viewLifecycleOwner) {
            focusedTasks = it.filter { task -> task.isFocused }
            adapter.submitList(focusedTasks)
        }
    }

    private fun setClearButton() {
        clearButton.setOnClickListener {
            focusedTasks.forEach { task ->
                task.isFocused = false
                taskViewModel.update(task)
            }
        }
    }

    private fun setBackButton() {
        backButton.setOnClickListener { findNavController().popBackStack() }
    }

    private fun bindViews(view: View) {
        recyclerView = view.findViewById(R.id.focus_fragment_recycler)
        backButton = view.findViewById(R.id.fragment_focus_back_button)
        clearButton = view.findViewById(R.id.fragment_focus_clear_button)
    }
}