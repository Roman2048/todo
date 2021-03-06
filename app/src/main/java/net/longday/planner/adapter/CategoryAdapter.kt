package net.longday.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import net.longday.planner.R
import net.longday.planner.data.entity.Category

class CategoryAdapter(
    var categories: List<Category>,
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.category_text_input)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.category_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.textView.text = categories[position].title
        holder.textView.setOnClickListener {
            try {
                it.findNavController().navigate(
                    R.id.action_listManagement_to_editCategory,
                    bundleOf("category" to category)
                )
            } catch (e: IllegalArgumentException) {
            }
        }
    }

    override fun getItemCount() = categories.size
}