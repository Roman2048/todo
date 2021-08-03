package net.longday.planner.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import net.longday.planner.R
import net.longday.planner.data.entity.Task
import java.text.SimpleDateFormat

class TaskAdapter(
    var tasks: List<Task>,
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: MaterialTextView = view.findViewById(R.id.task_item_text)
        val textTime: MaterialTextView = view.findViewById(R.id.task_item_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.textView.text = task.title
        holder.textTime.text = getTime(task)
//        if (task.isDone) {
//            holder.textView.paintFlags = holder.textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
//        }
        holder.textView.setOnClickListener {
            it.findNavController().navigate(
                R.id.action_homeFragment_to_editTaskFragment,
                bundleOf("task" to task)
            )
            it.showKeyboard()
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    // TODO: Надо так: если дата = сегодня, то пишем время вместо даты. Если дата не сегодня,
    // TODO: то пишем дату. Если завтра то пишем TOMORROW.
    private fun getTime(task: Task): String {
        // Если null ничего не показываем
        return if (task.dateTime == null) {
            ""
        } else {
            // Определяем дата это или дата + время
            if (task.dateTime.toString().endsWith("00000")) {
                // Если дата то показываем как есть
                SimpleDateFormat("MMM d").format(task.dateTime)
            } else {
                SimpleDateFormat("HH:mm").format(task.dateTime)
            }
        }
    }
}