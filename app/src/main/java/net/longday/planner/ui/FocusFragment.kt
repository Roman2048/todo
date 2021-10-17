package net.longday.planner.ui

import android.os.Bundle
import android.view.View
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

    private val taskViewModel: TaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.focus_fragment_recycler)
        val openTaskDetails: (task: Task) -> Unit = {
            try {
                findNavController().navigate(
                    R.id.action_focusFragment_to_editTaskFragment,
                    bundleOf("task" to it)
                )
            } catch (e: IllegalArgumentException) {
            }
        }
        val adapter = FocusAdapter(openTaskDetails)
        recyclerView.adapter = adapter
        taskViewModel.tasks.observe(viewLifecycleOwner) {
            val focusedTasks = it.filter { task -> task.isFocused }
            adapter.submitList(focusedTasks)
        }
    }

}