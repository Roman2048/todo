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

class SubtaskAdapter(
    private val openTaskDetails: (task: Task) -> Unit,
    private val updateTask: (task: Task) -> Unit,
) : ListAdapter<Task, SubtaskAdapter.SubtaskViewHolder>(TaskDiff()) {

    class SubtaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: MaterialTextView = view.findViewById(R.id.task_item_text)
        val time: MaterialTextView = view.findViewById(R.id.task_item_time)
        val checkBox: MaterialCheckBox = view.findViewById(R.id.task_item_checkbox)
    }

    class TaskDiff : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskViewHolder {
        return SubtaskViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.subtask_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.title.text = task.title
        holder.time.text = getTime(task)
        holder.checkBox.isChecked = task.isDone
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            task.isDone = isChecked
            task.completedTime = System.currentTimeMillis()
            updateTask(task)
        }
        holder.title.setOnClickListener {
            openTaskDetails.invoke(task)
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