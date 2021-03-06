package net.longday.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.longday.planner.R
import net.longday.planner.data.entity.Task

class TaskAdapter : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    var dataset = listOf<Task>()

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val task: TextView = view.findViewById<TextView>(R.id.task_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.task, parent, false))
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.task.text = dataset[position].title
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}