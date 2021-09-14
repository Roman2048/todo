package net.longday.planner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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

    private var _binding: FragmentCategoryContentBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()

    private lateinit var currentCategory: Category

    private var filterByImportance = false

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
//                binding.categoryContentTaskRecyclerCard.visibility = View.VISIBLE
            }
            if (tasks.none { it.categoryId == category.id && !it.isDone }) {
                binding.categoryContentTaskRecyclerCard.visibility = View.GONE
                binding.emptyImageView.visibility = View.VISIBLE
                binding.categoryContentImageCard.visibility = View.VISIBLE
            } else {
                binding.categoryContentTaskRecyclerCard.visibility = View.VISIBLE
            }
            // Set gone visibility for done task card to remove margin
            if (tasks.none { it.categoryId == category.id && it.isDone }) {
                binding.categoryContentDoneTaskRecyclerCard.visibility = View.GONE
                binding.fragmentCategoryDeleteDoneButton.visibility = View.GONE
            } else {
                binding.categoryContentDoneTaskRecyclerCard.visibility = View.VISIBLE
                binding.fragmentCategoryDeleteDoneButton.visibility = View.VISIBLE
            }
            binding.doneTaskRecycler.adapter =
                DoneTaskAdapter(
                    tasks
                        .filter { it.categoryId == category.id && it.isDone }
                        .sortedBy { it.completedTime }
                        .reversed(), updateTask)
            binding.taskRecycler.adapter =
                if (filterByImportance) {
                    TaskAdapter(
                        filterTasks(tasks).filter {
                            it.categoryId == category.id && !it.isDone && it.priority == "HIGH"
                        },
                        updateTask
                    )
                } else {
                    TaskAdapter(
                        filterTasks(tasks).filter { it.categoryId == category.id && !it.isDone },
                        updateTask
                    )
                }
            adapter.notifyDataSetChanged()
            doneAdapter.notifyDataSetChanged()
        }
        binding.fragmentCategoryShowDoneTasks.setOnClickListener {
            changeDoneTaskRecyclerVisibility()
        }
        binding.fragmentCategoryDeleteDoneButton.setOnClickListener {
            onAlertDialog()
        }
        binding.fragmentCategoryContentDoneTaskTitleText.setOnClickListener {
            changeDoneTaskRecyclerVisibility()
        }
        binding.fragmentCategoryShowActiveTasks.setOnClickListener {
            changeActiveTaskRecyclerVisibility()
        }
        // FIXME: find why button don't work
        binding.fragmentCategoryFilterByPriorityButton.visibility = View.GONE
        binding.fragmentCategoryFilterByPriorityButton.setOnClickListener {
            filterByImportance = !filterByImportance
            adapter.notifyDataSetChanged()
        }
        binding.fragmentCategoryContentActiveTaskTitleText.setOnClickListener {
            changeActiveTaskRecyclerVisibility()
        }
    }

    private fun changeDoneTaskRecyclerVisibility() {
        if (binding.doneTaskRecycler.visibility == View.GONE) {
            binding.doneTaskRecycler.visibility = View.VISIBLE
            binding.fragmentCategoryShowDoneTasks
                .setImageResource(R.drawable.ic_round_keyboard_arrow_down_24)
            binding.fragmentCategoryDeleteDoneButton.visibility = View.VISIBLE
        } else {
            binding.doneTaskRecycler.visibility = View.GONE
            binding.fragmentCategoryShowDoneTasks
                .setImageResource(R.drawable.ic_round_keyboard_arrow_left_24)
            binding.fragmentCategoryDeleteDoneButton.visibility = View.GONE
        }
    }

    private fun changeActiveTaskRecyclerVisibility() {
        if (binding.taskRecycler.visibility == View.GONE) {
            binding.taskRecycler.visibility = View.VISIBLE
            binding.fragmentCategoryShowActiveTasks
                .setImageResource(R.drawable.ic_round_keyboard_arrow_down_24)
            // FIXME: find why button don't work (change to VISIBLE)
            binding.fragmentCategoryFilterByPriorityButton.visibility = View.GONE
        } else {
            binding.taskRecycler.visibility = View.GONE
            binding.fragmentCategoryShowActiveTasks
                .setImageResource(R.drawable.ic_round_keyboard_arrow_left_24)
            binding.fragmentCategoryFilterByPriorityButton.visibility = View.GONE
        }
    }

    private fun filterTasks(tasks: List<Task>): List<Task> {
        return tasks.sortedBy { it.orderInCategory }
    }

    private fun moveItem(from: Int, to: Int, taskViewModel: TaskViewModel) {
        val tasks = taskViewModel.tasks.value ?: listOf()
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

    private fun onAlertDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.delete_done_task_dialog_title))
        builder.setMessage(getString(R.string.delete_done_task_dialog_message))
        builder.setPositiveButton(
            getString(R.string.fragment_edit_category_delete_button_text)
        ) { _, _ ->
            taskViewModel.tasks.value
                ?.filter { it.categoryId == currentCategory.id && it.isDone }
                ?.forEach {
                    it.isDeleted = true
                    taskViewModel.update(it)
                }
        }
        builder.setNegativeButton(
            getString(R.string.delete_done_task_dialog_cancel_button_text)
        ) { _, _ -> }
        builder.show()
    }
}