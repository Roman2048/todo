package net.longday.planner.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.TaskAdapter
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.TaskViewModel
import java.util.*

/**
 * Фрагмент, содержащий список задач, входящих в данную категорию
 */
@AndroidEntryPoint
class CategoryContentFragment : Fragment() {

//    private val categoryViewModel: CategoryViewModel by viewModels()

    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView: RecyclerView = view.findViewById(R.id.task_recycler)
        val list = listOf(
            Task(UUID.randomUUID().toString(), "123", "1234"),
            Task(UUID.randomUUID().toString(), "123", "1234"),
            Task(UUID.randomUUID().toString(), "123", "1234"),
        )
        val adapter = TaskAdapter(list)
        recyclerView.adapter = adapter
        arguments?.takeIf { it.containsKey("position") }?.apply {
            recyclerView.adapter = adapter
            TODO("отфильтровать таски по позиции")
        }
        taskViewModel.tasks.observe(viewLifecycleOwner) {
            recyclerView.adapter = TaskAdapter(it)
            adapter.notifyDataSetChanged()

        }


    }
}