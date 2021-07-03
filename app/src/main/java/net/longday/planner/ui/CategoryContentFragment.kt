package net.longday.planner.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.CategoryAdapter
import net.longday.planner.adapter.DoneTaskAdapter
import net.longday.planner.adapter.TaskAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.CategoryViewModel
import net.longday.planner.viewmodel.TaskViewModel


/**
 * Фрагмент, содержащий список задач, входящих в данную категорию
 */
@AndroidEntryPoint
class CategoryContentFragment : Fragment(R.layout.fragment_category_content) {

    private val taskViewModel: TaskViewModel by viewModels()

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, 0) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val adapter = recyclerView.adapter as TaskAdapter
                    val innerFrom = viewHolder.adapterPosition
                    val innerTo = target.adapterPosition
                    moveItem(innerFrom, innerTo, taskViewModel)
                    adapter.notifyItemMoved(innerFrom, innerTo)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }

    /**
     * Получаем категорию из бандла, фильтруем таски по id полученной категории, передаем в адаптер
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView: RecyclerView = view.findViewById(R.id.task_recycler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        val emptyImageView: AppCompatImageView =
            view.findViewById(R.id.fragment_category_content_empty_image_view)
//        val doneRecyclerView: RecyclerView = view.findViewById(R.id.done_task_recycler)
        // Да как так то почему он вертикальный ****
//        recyclerView.addItemDecoration(
//            DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
//        )
        val adapter = TaskAdapter(listOf())
        val doneAdapter = DoneTaskAdapter(listOf())
        recyclerView.adapter = adapter
//        doneRecyclerView.adapter = doneAdapter
//        recyclerView.addItemDecoration(
//            DividerItemDecoration(
//                recyclerView.context,
//                DividerItemDecoration.HORIZONTAL
//            )
//        )
        val category: Category = arguments?.get("category") as Category
        taskViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks.none { it.categoryId == category.id }) {
                emptyImageView.visibility = View.VISIBLE
            } else {
                emptyImageView.visibility = View.GONE
            }
            // TODO: зарандомить изображения кактуса
            // TODO: если категория "All" то не фильтруем список
            if (false) {
                recyclerView.adapter = TaskAdapter(filterTasks(tasks))
            } else {
                recyclerView.adapter =
                    TaskAdapter(filterTasks(tasks).filter { it.categoryId == category.id })
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun filterTasks(tasks: List<Task>): List<Task> {
        return tasks.sortedBy { it.orderInCategory }
    }

    private fun moveItem(from: Int, to: Int, taskViewModel: TaskViewModel) {
        val tasks = taskViewModel.tasks.value ?: listOf<Task>()
        val sortedTasks = tasks.sortedBy { it.orderInCategory }
        val mutableSortedTasks = sortedTasks.toMutableList()
        val itemToMove = sortedTasks[from]
        mutableSortedTasks.removeAt(from)
        mutableSortedTasks.add(to, itemToMove)
        mutableSortedTasks.forEachIndexed { index, category ->
            category.orderInCategory = index
        }
        mutableSortedTasks.forEach {
            taskViewModel.update(
                Task(
                    id = it.id,
                    categoryId = it.categoryId,
                    dateTime = it.dateTime,
                    title = it.title,
                    orderInCategory = it.orderInCategory
                )
            )
        }
    }
}