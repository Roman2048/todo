package net.longday.planner.adapter

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import net.longday.planner.R

class TaskAdapter : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    var tabIndex = 0
 // tabIndex соответсвует категории (поле индекс у категории в БД)
    fun setTabIndex(tabIndex: Int): TaskAdapter {
        this.tabIndex = tabIndex
        return this
    }

    private val dataset = FakeDataset.tasks

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val listedTaskTextInput: EditText = view.findViewById(R.id.listed_task_text_input)
        val listedTaskSaveButton: Button = view.findViewById(R.id.listed_task_save_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.task, parent, false))
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        // фильтруем таски по tabIndex или берем категорим по индексу и оттуда берем таски
        holder.listedTaskTextInput.text = SpannableStringBuilder(dataset[position].title)
        holder.listedTaskSaveButton.setOnClickListener {
            FakeDataset.tasks[position].title = holder.listedTaskTextInput.text.toString()
            this.notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}