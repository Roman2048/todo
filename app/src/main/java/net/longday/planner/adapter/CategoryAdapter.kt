package net.longday.planner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.viewmodel.CategoryViewModel

class CategoryAdapter(
    var categories: List<Category>,
    val context: Context,
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
            it.findNavController().navigate(
                R.id.action_categoryEditorFragment_to_editCategoryFragment,
                bundleOf("category" to category)
            )
            it.showKeyboard()
        }
    }

    override fun getItemCount() = categories.size

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    // TODO: как менять местами придумать
//    fun moveItem(from: Int, to: Int, categoryViewModel: CategoryViewModel) {
//        val category = categories[from]
//        categories.removeAt(from)
//        categories.add(to, category)
//        categories.forEach {
//            if (it.position >= to) {
//                categoryViewModel.update(
//                    Category(
//                        it.id,
//                        it.title,
//                        it.position + 1,
//                    )
//                )
//            }
//        }
//    }
}