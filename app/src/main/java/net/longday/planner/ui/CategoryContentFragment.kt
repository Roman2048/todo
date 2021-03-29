package net.longday.planner.ui

import android.hardware.SensorManager.getOrientation
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.TaskAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.viewmodel.TaskViewModel


/**
 * Фрагмент, содержащий список задач, входящих в данную категорию
 */
@AndroidEntryPoint
class CategoryContentFragment : Fragment(R.layout.fragment_category_content) {

    private val taskViewModel: TaskViewModel by viewModels()

    /**
     * Получаем категорию из бандла, фильтруем таски по id полученной категории, передаем в адаптер
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView: RecyclerView = view.findViewById(R.id.task_recycler)
//        recyclerView.addItemDecoration(
//            DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL)
//        )
        val adapter = TaskAdapter(listOf())
        recyclerView.adapter = adapter
        val category: Category = arguments?.get("category") as Category
        taskViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            if (false) {
                recyclerView.adapter = TaskAdapter(tasks)
            } else {
                recyclerView.adapter = TaskAdapter(tasks.filter { it.categoryId == category.id })
            }
            adapter.notifyDataSetChanged()
        }
    }
}