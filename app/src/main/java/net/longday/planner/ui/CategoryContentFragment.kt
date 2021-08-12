package net.longday.planner.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.DoneTaskAdapter
import net.longday.planner.adapter.TaskAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.TaskViewModel

/**
 * Fragment with list of tasks
 */
@AndroidEntryPoint
class CategoryContentFragment : Fragment(R.layout.fragment_category_content) {

    private val taskViewModel: TaskViewModel by viewModels()

    private lateinit var currentCategory: Category

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, 0) {
                private var dragFromPosition = -1
                private var dragToPosition = -1
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val adapter = recyclerView.adapter as TaskAdapter
                    val innerFrom = viewHolder.adapterPosition
                    val innerTo = target.adapterPosition
                    if (dragFromPosition == -1) {
                        dragFromPosition = viewHolder.adapterPosition
                    }
                    dragToPosition = target.adapterPosition
                    adapter.notifyItemMoved(innerFrom, innerTo)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    super.onSelectedChanged(viewHolder, actionState)
                    when (actionState) {
                        ACTION_STATE_DRAG -> {
                            viewHolder?.also { dragToPosition = it.adapterPosition }
                        }
                        ACTION_STATE_IDLE -> {
                            if (dragFromPosition != -1
                                && dragToPosition != -1
                                && dragFromPosition != dragToPosition
                            ) {
                                // Item successfully dragged
                                moveItem(dragFromPosition, dragToPosition, taskViewModel)
                                // Reset drag positions
                                dragFromPosition = -1
                                dragToPosition = -1
                            }
                        }
                    }
                }
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }

    /**
     * Get category form the bundle, filter tasks by the bundle, send to adapter
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView: RecyclerView = view.findViewById(R.id.task_recycler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        val emptyImageView: AppCompatImageView =
            view.findViewById(R.id.fragment_category_content_empty_image_view)
        val doneRecyclerView: RecyclerView = view.findViewById(R.id.done_task_recycler)
        // Да как так то почему он вертикальный ****
//        recyclerView.addItemDecoration(
//            DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
//        )
        val adapter = TaskAdapter(listOf())
        val doneAdapter = DoneTaskAdapter(listOf())
        recyclerView.adapter = adapter
        doneRecyclerView.adapter = doneAdapter
        val imageCard = view.findViewById<MaterialCardView>(R.id.category_content_image_card)
        val category: Category = arguments?.get("category") as Category
        currentCategory = category
        val taskRecyclerCard =
            view.findViewById<MaterialCardView>(R.id.category_content_task_recycler_card)
        taskViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks.none { it.categoryId == category.id }) {
                emptyImageView.visibility = View.VISIBLE
            } else {
                emptyImageView.visibility = View.GONE
                imageCard.visibility = View.GONE
            }
            if (tasks.none { it.categoryId == category.id && !it.isDone }) {
                taskRecyclerCard.visibility = View.GONE
                emptyImageView.visibility = View.VISIBLE
                imageCard.visibility = View.VISIBLE
            }
            doneRecyclerView.adapter =
                DoneTaskAdapter(tasks.filter { it.categoryId == category.id && it.isDone })
            recyclerView.adapter =
                TaskAdapter(filterTasks(tasks).filter { it.categoryId == category.id && !it.isDone })
            adapter.notifyDataSetChanged()
            doneAdapter.notifyDataSetChanged()
        }
    }

    private fun filterTasks(tasks: List<Task>): List<Task> {
        return tasks.sortedBy { it.orderInCategory }
    }

    private fun moveItem(from: Int, to: Int, taskViewModel: TaskViewModel) {
        val tasks = taskViewModel.tasks.value ?: listOf<Task>()
        val tasksByCategory = tasks.filter { it.categoryId == currentCategory.id }
        val sortedTasks = tasksByCategory.sortedBy { it.orderInCategory }
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
                    title = it.title,
                    categoryId = it.categoryId,
                    createdTime = it.createdTime,
                    timeZone = it.timeZone,
                    content = it.content,
                    dateTime = it.dateTime,
                    completedTime = it.completedTime,
                    dueDate = it.dueDate,
                    isDone = it.isDone,
                    isDeleted = it.isDeleted,
                    isScheduled = it.isScheduled,
                    orderInCategory = it.orderInCategory
                )
            )
        }
    }
}