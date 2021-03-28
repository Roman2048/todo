package net.longday.planner.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.viewmodel.CategoryViewModel

class CategoryAdapter(
    var categories: List<Category>,
    private val categoryViewModel: CategoryViewModel,
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryTextInput: TextInputEditText = view.findViewById(R.id.category_text_input)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.category, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryTextInput.setText(category.title)
        var updated = false
        holder.categoryTextInput.doOnTextChanged { text, _, _, _ ->
            Log.d("planner", "Update category text input trigger")
            if (updated) return@doOnTextChanged
            updated = true
            if (text != category.title) {
                categoryViewModel.update(
                    Category(
                        category.id,
                        text.toString(),
                        category.tabIndex
                    )
                )
            }
        }
    }

    override fun getItemCount() = categories.size
}