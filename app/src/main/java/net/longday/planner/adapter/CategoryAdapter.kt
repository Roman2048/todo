package net.longday.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.longday.planner.R
import net.longday.planner.data.entity.Category

class CategoryAdapter(
    var categories: List<Category>,
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryItem: TextView = view.findViewById(R.id.category_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.category, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.categoryItem.text = categories[position].title
//        holder.categoryItem.setOnFocusChangeListener { v, hasFocus -> if (hasFocus)  }
    }

    override fun getItemCount() = categories.size
}