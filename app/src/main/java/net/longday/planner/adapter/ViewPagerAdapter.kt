package net.longday.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task

class ViewPagerAdapter(
    private val tasks: List<Task>,
) : RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder>() {

    var categories: List<Category> = listOf(Category("1", "1", 0))

    class ViewPagerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        return ViewPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.tab_content, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.recyclerView.adapter =
            TaskAdapter(tasks.filter { it.categoryId == position.toString() })
    }

    override fun getItemCount(): Int {
        return categories.size
    }

}