package net.longday.planner.adapter

import android.content.Context
import android.graphics.Paint
import android.text.format.DateUtils
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
import java.util.*

class DoneTaskAdapter(
    var tasks: List<Task>,
) : RecyclerView.Adapter<DoneTaskAdapter.TaskViewHolder>() {

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
        holder.textView.paintFlags = holder.textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
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

    private fun getTime(task: Task): String {
        // If date is null, show nothing
        return if (task.dateTime == null) {
            ""
        } else {
            // If date is today, show only time. Else show only date.
            if (DateUtils.isToday(task.dateTime!!)) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(task.dateTime)
            } else {
                SimpleDateFormat("MMM d", Locale.getDefault()).format(task.dateTime)
            }
        }
    }
}