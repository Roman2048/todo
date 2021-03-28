package net.longday.planner.adapter

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import net.longday.planner.R
import net.longday.planner.data.entity.Task

class TaskAdapter(
    var tasks: List<Task>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val listedTaskTextInput: MaterialTextView = view.findViewById(R.id.task_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false))
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        // фильтруем таски по tabIndex или берем категорим по индексу и оттуда берем таски
        holder.listedTaskTextInput.text = SpannableStringBuilder(tasks[position].title)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}