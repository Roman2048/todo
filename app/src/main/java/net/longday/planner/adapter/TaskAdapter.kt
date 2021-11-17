package net.longday.planner.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import net.longday.planner.R
import net.longday.planner.data.entity.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    var tasks: List<Task>,
    private val updateTask: (task: Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: MaterialTextView = view.findViewById(R.id.task_item_text)
        val textTime: MaterialTextView = view.findViewById(R.id.task_item_time)
        val textCheckbox: MaterialCheckBox = view.findViewById(R.id.task_item_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.textView.text = task.title
        holder.textTime.text = getTime(task, holder.textView.context)
        holder.textCheckbox.isChecked = task.isDone
        holder.textCheckbox.setOnCheckedChangeListener { _, isChecked ->
            task.isDone = isChecked
            task.completedTime = System.currentTimeMillis()
            updateTask(task)
        }
        holder.textView.setOnClickListener {
            try {
                it.findNavController().navigate(
                    R.id.action_homeFragment_to_editTaskFragment,
                    bundleOf("task" to task)
                )
            } catch (e: IllegalArgumentException) {
            }
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    // TODO: if date is tomorrow, show "tomorrow"
    private fun getTime(task: Task, context: Context): String {
        // If date is null, show nothing
        return if (task.dateTime == null) {
            ""
        } else {
            // If date is today, show only time. Else show only date.
            if (DateUtils.isToday(task.dateTime!!)) {
                if (task.isAllDay) {
                    context.getString(R.string.home_fragment_list_item_today_text)
                } else {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(task.dateTime)
                }
            } else {
                SimpleDateFormat("MMM d", Locale.getDefault()).format(task.dateTime)
            }
        }
    }
}