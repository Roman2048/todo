package net.longday.planner.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.viewmodel.CategoryViewModel

@AndroidEntryPoint
class UpdateCategoryFragment : Fragment(R.layout.fragment_update_category) {

    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val editText: EditText = view.findViewById(R.id.edit_category_text_edit)
        val saveButton: Button = view.findViewById(R.id.edit_category_item_save_button)
        val deleteButton: Button = view.findViewById(R.id.edit_category_item_delete_button)
        val backButton: Button = view.findViewById(R.id.edit_category_item_back_button)
        val category: Category = arguments?.get("category") as Category
        editText.setText(category.title)
        saveButton.setOnClickListener {
            categoryViewModel.update(
                Category(
                    category.id,
                    editText.text.toString(),
                )
            )
            view.findNavController().navigate(R.id.categoryEditorFragment)
            it.hideKeyboard()
        }
        deleteButton.setOnClickListener {
            categoryViewModel.delete(category)
            view.findNavController().navigate(R.id.categoryEditorFragment)
            it.hideKeyboard()
        }
        backButton.setOnClickListener {
            view.findNavController().navigate(R.id.categoryEditorFragment)
            it.hideKeyboard()
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
}