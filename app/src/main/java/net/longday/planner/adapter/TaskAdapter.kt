package net.longday.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import net.longday.planner.R
import net.longday.planner.data.entity.Task

class TaskAdapter(
    var tasks: List<Task>,
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: MaterialTextView = view.findViewById(R.id.task_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.textView.text = tasks[position].title
        val task = tasks[position]
        holder.textView.setOnClickListener {
            it.findNavController().navigate(
                R.id.action_homeFragment_to_editTaskFragment,
                bundleOf("task" to task)
            )
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}