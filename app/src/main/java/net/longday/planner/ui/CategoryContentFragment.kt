package net.longday.planner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.DoneTaskAdapter
import net.longday.planner.adapter.TaskAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task
import net.longday.planner.databinding.FragmentCategoryContentBinding
import net.longday.planner.viewmodel.TaskViewModel

/**
 * Fragment with list of tasks
 */
@AndroidEntryPoint
class CategoryContentFragment : Fragment(R.layout.fragment_category_content) {

    private val taskViewModel: TaskViewModel by viewModels()

    private lateinit var currentCategory: Category

    private var _binding: FragmentCategoryContentBinding? = null

    private val binding get() = _binding!!

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Get category form the bundle, filter tasks by the bundle, send to adapter
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        itemTouchHelper.attachToRecyclerView(binding.taskRecycler)
//        recyclerView.addItemDecoration(
//            DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
//        )
        val updateTask: (task: Task) -> Unit = { taskViewModel.update(it) }
        val adapter = TaskAdapter(listOf(), updateTask)
        val doneAdapter = DoneTaskAdapter(listOf(), updateTask)
        binding.taskRecycler.adapter = adapter
        binding.doneTaskRecycler.adapter = doneAdapter
        val category: Category = arguments?.get("category") as Category
        currentCategory = category
        taskViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks.none { it.categoryId == category.id }) {
                binding.emptyImageView.visibility = View.VISIBLE
            } else {
                binding.emptyImageView.visibility = View.GONE
                binding.categoryContentImageCard.visibility = View.GONE
            }
            if (tasks.none { it.categoryId == category.id && !it.isDone }) {
                binding.categoryContentTaskRecyclerCard.visibility = View.GONE
                binding.emptyImageView.visibility = View.VISIBLE
                binding.categoryContentImageCard.visibility = View.VISIBLE

            }
            // Set gone visibility for done task card to remove margin
            if (tasks.none { it.categoryId == category.id && it.isDone }) {
                binding.categoryContentDoneTaskRecyclerCard.visibility = View.GONE
            } else {
                binding.categoryContentDoneTaskRecyclerCard.visibility = View.VISIBLE
            }
            binding.doneTaskRecycler.adapter =
                DoneTaskAdapter(
                    tasks
                        .filter { it.categoryId == category.id && it.isDone }
                        .sortedBy { it.completedTime }
                        .reversed(), updateTask)
            binding.taskRecycler.adapter =
                TaskAdapter(
                    filterTasks(tasks).filter { it.categoryId == category.id && !it.isDone },
                    updateTask
                )
            adapter.notifyDataSetChanged()
            doneAdapter.notifyDataSetChanged()
        }
    }

    private fun filterTasks(tasks: List<Task>): List<Task> {
        return tasks.sortedBy { it.orderInCategory }
    }

    private fun moveItem(from: Int, to: Int, taskViewModel: TaskViewModel) {
        val tasks = taskViewModel.tasks.value ?: listOf<Task>()
        val tasksByCategory = tasks.filter { it.categoryId == currentCategory.id && !it.isDone }
        val sortedTasks = tasksByCategory.sortedBy { it.orderInCategory }
        val mutableSortedTasks = sortedTasks.toMutableList()
        val itemToMove = sortedTasks[from]
        mutableSortedTasks.removeAt(from)
        mutableSortedTasks.add(to, itemToMove)
        mutableSortedTasks.forEachIndexed { index, category ->
            category.orderInCategory = index
            taskViewModel.update(category)
        }
    }
}