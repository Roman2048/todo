package net.longday.planner.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.CategoryAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.viewmodel.CategoryViewModel

/**
 * Экран со списком категорий, для их редактирования.
 */
@AndroidEntryPoint
class CategoriesListFragment : Fragment(R.layout.fragment_categories_list) {

    private val categoryViewModel: CategoryViewModel by viewModels()

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, 0) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val adapter = recyclerView.adapter as CategoryAdapter
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition
                    moveItem(from, to, categoryViewModel)
                    adapter.notifyItemMoved(from, to)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler: RecyclerView = view.findViewById(R.id.categories_recycler_view)
        itemTouchHelper.attachToRecyclerView(recycler)
        val addCategoryItem: MaterialTextView =
            view.findViewById(R.id.categories_add_new_category_item)
        val adapter = CategoryAdapter(mutableListOf(), requireContext())
        recycler.adapter = adapter
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            adapter.categories = it.sortedBy { category -> category.position }
            adapter.notifyDataSetChanged()
        }
        val backButton: AppCompatImageButton = view.findViewById(R.id.category_editor_edit_button)
        backButton.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_categoryEditorFragment_to_homeFragment)
        }

        addCategoryItem.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_categoryEditorFragment_to_addCategoryFragment)
            it.showKeyboard()
        }
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    private fun moveItem(from: Int, to: Int, categoryViewModel: CategoryViewModel) {
        Toast.makeText(requireContext(), "try to move: from = $from, to = $to", Toast.LENGTH_SHORT).show()
        val categories =
            categoryViewModel.categories.value ?: listOf<Category>().sortedBy { it.position }
        val reorderedCategories = categories.toMutableList()
        val itemToMove = categories[from]
        reorderedCategories.removeAt(from)
        reorderedCategories.add(to, itemToMove)
        reorderedCategories.forEachIndexed { index, currentCategory ->
//            if (currentCategory.position != categories.first { filteredCategory: Category ->
//                    currentCategory.id == filteredCategory.id
//                }.position) {
                categoryViewModel.update(
                    Category(
                        currentCategory.id,
                        currentCategory.title,
                        index
                    )
                )
//                Toast.makeText(requireContext(), "updated", Toast.LENGTH_SHORT).show()
//            }
        }
    }
}