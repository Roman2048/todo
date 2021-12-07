package net.longday.planner.ui

import android.graphics.PorterDuff
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.CanceledTaskAdapter
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

    private lateinit var canceledTaskCard: MaterialCardView

    private val taskViewModel: TaskViewModel by viewModels()
    private var allTasks: List<Task> = listOf()
    private var todoTasks: List<Task> = listOf()
    private var doneTasks: List<Task> = listOf()
    private var canceledTasks: List<Task> = listOf()

    private lateinit var currentCategory: Category

    private var filterByImportance = false
    private var filterByUrgency = false
    private var isCanceledTasksCollapsed = true
    private var isUpdating = false

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(UP or DOWN, 0) {
                private var dragFromPosition = -1
                private var dragToPosition = -1
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    if (!filterByImportance && !filterByUrgency) {
                        val adapter = recyclerView.adapter as TaskAdapter
                        val innerFrom = viewHolder.adapterPosition
                        val innerTo = target.adapterPosition
                        if (dragFromPosition == -1) {
                            dragFromPosition = viewHolder.adapterPosition
                        }
                        dragToPosition = target.adapterPosition
                        adapter.notifyItemMoved(innerFrom, innerTo)
                        return true
                    } else {
                        return false
                    }
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

    /**
     * Get category form the bundle, filter tasks by the bundle, send to adapter
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews()
        itemTouchHelper.attachToRecyclerView(binding.taskRecycler)
        val updateTask: (task: Task) -> Unit = { taskViewModel.update(it) }
        var taskAdapter: TaskAdapter
        var doneTaskAdapter: DoneTaskAdapter
        var canceledTaskAdapter: CanceledTaskAdapter
        ViewCompat.setNestedScrollingEnabled(binding.taskRecycler, false)
        ViewCompat.setNestedScrollingEnabled(binding.doneTaskRecycler, false)
        ViewCompat.setNestedScrollingEnabled(binding.canceledTaskRecycler, false)
        val category: Category = arguments?.get("category") as Category
        currentCategory = category
        binding.doneTaskRecycler.visibility = GONE
        binding.fragmentCategoryShowDoneTasks
            .setImageResource(R.drawable.ic_round_keyboard_arrow_left_24)
        binding.fragmentCategoryDeleteDoneButton.visibility = GONE

        taskViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            if (isUpdating) return@observe
            allTasks = tasks.filter { it.categoryId == category.id && it.parentTaskId == null }
            todoTasks = allTasks.filter { !it.isDone && !it.isCanceled }
            doneTasks = allTasks.filter { it.isDone }
            canceledTasks = allTasks.filter { it.isCanceled && it.parentTaskId == null }
            /* Canceled tasks visibility */
            canceledTaskCard.visibility = if (canceledTasks.none()) {
                GONE
            } else {
                setCanceledTasksVisibilityByCollapseState()
                VISIBLE
            }
            // To-do tasks
            binding.emptyImageView.visibility = if (allTasks.none()) VISIBLE else GONE
            if (todoTasks.none()) {
                binding.categoryContentTaskRecyclerCard.visibility = GONE
                binding.emptyImageView.visibility = VISIBLE
            } else {
                binding.categoryContentTaskRecyclerCard.visibility = VISIBLE
            }
            /* Set gone visibility for done task card to remove margin */
            binding.categoryContentDoneTaskRecyclerCard.visibility =
                if (doneTasks.none()) GONE else VISIBLE
            /* If there is no important tasks, disable priority switch */
            binding.fragmentCategoryFilterByPriorityButton.isEnabled =
                !allTasks.none { it.categoryId == category.id && !it.isDone && it.priority != null }
            /* If there is no urgency tasks, disable urgency switch */
            binding.fragmentCategoryFilterByUrgencyButton.isEnabled =
                !allTasks.none {
                    it.categoryId == category.id && !it.isDone && (
                            DateUtils.isToday(it.dateTime ?: 0)
                                    || DateUtils
                                .isToday(it.dateTime?.minus(86_400_000) ?: 0)
                            )
                }
            taskAdapter = TaskAdapter(sortTasksByOrder(todoTasks), updateTask)
            binding.taskRecycler.adapter = taskAdapter
            doneTaskAdapter = DoneTaskAdapter(
                doneTasks.sortedBy { it.completedTime }.reversed(), updateTask
            )
            binding.doneTaskRecycler.adapter = doneTaskAdapter
            canceledTaskAdapter = CanceledTaskAdapter(sortTasksByOrder(canceledTasks), updateTask)
            binding.canceledTaskRecycler.adapter = canceledTaskAdapter
            taskAdapter.notifyDataSetChanged()
            doneTaskAdapter.notifyDataSetChanged()
            canceledTaskAdapter.notifyDataSetChanged()
            isUpdating = false
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
        binding.fragmentCategoryContentActiveTaskTitleText.setOnClickListener {
            changeActiveTaskRecyclerVisibility()
        }
        binding.fragmentCategoryFilterByPriorityButton.setOnClickListener {
            filterByImportance = !filterByImportance
            binding.taskRecycler.adapter = setTaskRecyclerAdapterByFilters(category, updateTask)
        }
        /* Urgency filter */
        binding.fragmentCategoryFilterByUrgencyButton.setOnClickListener {
            filterByUrgency = !filterByUrgency
            binding.taskRecycler.adapter = setTaskRecyclerAdapterByFilters(category, updateTask)
        }
        binding.fragmentCategoryShowCanceledTasks.setOnClickListener {
            isCanceledTasksCollapsed = !isCanceledTasksCollapsed
            setCanceledTasksVisibilityByCollapseState()
        }
        binding.fragmentCategoryContentCanceledTaskTitleText.setOnClickListener {
            isCanceledTasksCollapsed = !isCanceledTasksCollapsed
            setCanceledTasksVisibilityByCollapseState()
        }
        binding.fragmentCategoryDeleteCanceledButton.setOnClickListener {
            showDeleteCanceledTasksDialog()
        }
    }

    private fun showDeleteCanceledTasksDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.delete_done_task_dialog_title))
        builder.setMessage(getString(R.string.delete_canceled_tasks_button_text))
        builder.setPositiveButton(
            getString(R.string.fragment_edit_category_delete_button_text)
        ) { _, _ ->
            canceledTasks.forEach {
                it.isDeleted = true
                taskViewModel.update(it)
            }
        }
        builder.setNegativeButton(
            getString(R.string.delete_done_task_dialog_cancel_button_text)
        ) { _, _ -> }
        builder.show()
    }

    private fun setCanceledTasksVisibilityByCollapseState() {
        if (isCanceledTasksCollapsed) {
            binding.canceledTaskRecycler.visibility = GONE
            binding.fragmentCategoryShowCanceledTasks.setImageResource(R.drawable.ic_round_keyboard_arrow_left_24)
            binding.fragmentCategoryDeleteCanceledButton.visibility = GONE
        } else {
            binding.canceledTaskRecycler.visibility = VISIBLE
            binding.fragmentCategoryShowCanceledTasks.setImageResource(R.drawable.ic_round_keyboard_arrow_down_24)
            binding.fragmentCategoryDeleteCanceledButton.visibility = VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindViews() {
        canceledTaskCard = binding.categoryContentCanceledTaskRecyclerCard
    }

    /**
     * Filter tasks by priority (yes/no) or / and urgency (yes/no)
     */
    private fun setTaskRecyclerAdapterByFilters(
        category: Category,
        updateTask: (task: Task) -> Unit
    ): RecyclerView.Adapter<*> = when {
        filterByImportance && filterByUrgency -> {
            itemTouchHelper.attachToRecyclerView(null)
            binding.fragmentCategoryFilterByPriorityButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.holo_green_dark
                ), PorterDuff.Mode.SRC_IN
            )
            binding.fragmentCategoryFilterByUrgencyButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.holo_green_dark
                ), PorterDuff.Mode.SRC_IN
            )
            TaskAdapter(
                sortTasksByOrder(allTasks).filter {
                    it.categoryId == category.id
                            && !it.isDone
                            && it.priority == "HIGH"
                            && (
                            DateUtils.isToday(it.dateTime ?: 0)
                                    || DateUtils
                                .isToday(it.dateTime?.minus(86_400_000) ?: 0)
                            )
                },
                updateTask
            )
        }
        !filterByImportance && !filterByUrgency -> {
            itemTouchHelper.attachToRecyclerView(binding.taskRecycler)
            binding.fragmentCategoryFilterByPriorityButton.colorFilter = null
            binding.fragmentCategoryFilterByUrgencyButton.colorFilter = null
            TaskAdapter(
                sortTasksByOrder(allTasks).filter { it.categoryId == category.id && !it.isDone },
                updateTask
            )
        }
        filterByImportance && !filterByUrgency -> {
            // выключить перетаскивание
            itemTouchHelper.attachToRecyclerView(null)
            binding.fragmentCategoryFilterByUrgencyButton.colorFilter = null
            binding.fragmentCategoryFilterByPriorityButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.holo_green_dark
                ), PorterDuff.Mode.SRC_IN
            )
            TaskAdapter(
                sortTasksByOrder(allTasks).filter {
                    it.categoryId == category.id && !it.isDone && it.priority == "HIGH"
                },
                updateTask
            )
        }
        !filterByImportance && filterByUrgency -> {
            itemTouchHelper.attachToRecyclerView(null)
            binding.fragmentCategoryFilterByPriorityButton.colorFilter = null
            binding.fragmentCategoryFilterByUrgencyButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.holo_green_dark
                ), PorterDuff.Mode.SRC_IN
            )
            // If the task time is today or tomorrow, show it as urgency task
            TaskAdapter(
                sortTasksByOrder(allTasks).filter {
                    it.categoryId == category.id
                            && !it.isDone
                            && (
                            DateUtils.isToday(it.dateTime ?: 0)
                                    || DateUtils
                                .isToday(it.dateTime?.minus(86_400_000) ?: 0)
                            )
                },
                updateTask
            )
        }
        else -> {
            itemTouchHelper.attachToRecyclerView(binding.taskRecycler)
            binding.fragmentCategoryFilterByPriorityButton.colorFilter = null
            binding.fragmentCategoryFilterByUrgencyButton.colorFilter = null
            TaskAdapter(
                sortTasksByOrder(allTasks).filter { it.categoryId == category.id && !it.isDone },
                updateTask
            )
        }
    }

    private fun changeActiveTaskRecyclerVisibility() {
        if (binding.taskRecycler.visibility == GONE) {
            binding.taskRecycler.visibility = VISIBLE
            binding.fragmentCategoryShowActiveTasks
                .setImageResource(R.drawable.ic_round_keyboard_arrow_down_24)
            binding.fragmentCategoryFilterByPriorityButton.visibility = VISIBLE
            binding.fragmentCategoryFilterByUrgencyButton.visibility = VISIBLE
        } else {
            binding.taskRecycler.visibility = GONE
            binding.fragmentCategoryShowActiveTasks
                .setImageResource(R.drawable.ic_round_keyboard_arrow_left_24)
            binding.fragmentCategoryFilterByPriorityButton.visibility = GONE
            binding.fragmentCategoryFilterByUrgencyButton.visibility = GONE
        }
    }

    private fun changeDoneTaskRecyclerVisibility() {
        if (binding.doneTaskRecycler.visibility == GONE) {
            binding.doneTaskRecycler.visibility = VISIBLE
            binding.fragmentCategoryShowDoneTasks
                .setImageResource(R.drawable.ic_round_keyboard_arrow_down_24)
            binding.fragmentCategoryDeleteDoneButton.visibility = VISIBLE
        } else {
            binding.doneTaskRecycler.visibility = GONE
            binding.fragmentCategoryShowDoneTasks
                .setImageResource(R.drawable.ic_round_keyboard_arrow_left_24)
            binding.fragmentCategoryDeleteDoneButton.visibility = GONE
        }
    }

    private fun sortTasksByOrder(tasks: List<Task>): List<Task> {
        return tasks.sortedBy { it.orderInCategory }
    }

    private fun moveItem(from: Int, to: Int, taskViewModel: TaskViewModel) {
        val sortedTasks = todoTasks.sortedBy { it.orderInCategory }
        val mutableSortedTasks = sortedTasks.toMutableList()
        val itemToMove = sortedTasks[from]
        mutableSortedTasks.removeAt(from)
        mutableSortedTasks.add(to, itemToMove)
        mutableSortedTasks.forEachIndexed { index, task ->
            task.orderInCategory = index
        }
        isUpdating = true
        mutableSortedTasks.forEach {
            isUpdating = true
            taskViewModel.update(it)
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