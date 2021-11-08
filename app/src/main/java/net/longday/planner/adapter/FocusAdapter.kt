package net.longday.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import net.longday.planner.R
import net.longday.planner.data.entity.Task
import java.text.SimpleDateFormat
import java.util.*

class FocusAdapter(
    private val openTaskDetails: (task: Task) -> Unit,
    private val updateTask: (task: Task) -> Unit,
) : ListAdapter<Task, FocusAdapter.FocusViewHolder>(TaskDiff()) {

    class FocusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: MaterialTextView = view.findViewById(R.id.task_item_text)
        val textTime: MaterialTextView = view.findViewById(R.id.task_item_time)
        val textCheckbox: MaterialCheckBox = view.findViewById(R.id.task_item_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FocusViewHolder {
        return FocusViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FocusViewHolder, position: Int) {
        val task = getItem(position)
        holder.textView.text = task.title
        holder.textTime.text = getTime(task)
        holder.textCheckbox.isChecked = task.isDone
        holder.textCheckbox.setOnCheckedChangeListener { _, isChecked ->
            task.isDone = isChecked
            task.completedTime = System.currentTimeMillis()
            updateTask(task)
        }
        holder.textView.setOnClickListener {
            openTaskDetails.invoke(task)
        }
    }

    class TaskDiff : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }
    }

    private fun getTime(task: Task): String {
        return if (task.dateTime == null) {
            ""
        } else {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(task.dateTime)
        }
    }
}