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
import java.util.*

@AndroidEntryPoint
class AddCategoryFragment : Fragment(R.layout.fragment_add_category) {

    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val textEdit: EditText = view.findViewById(R.id.add_category_text_edit)
        val button: Button = view.findViewById(R.id.add_category_button)
        button.setOnClickListener {
            categoryViewModel.insert(
                Category(
                    UUID.randomUUID().toString(),
                    textEdit.text.toString(),
                )
            )
            view.findNavController()
                .navigate(R.id.action_addCategoryFragment_to_categoryEditorFragment)
            it.hideKeyboard()
        }
        val backButton: Button = view.findViewById(R.id.add_category_back_button)
        backButton.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_addCategoryFragment_to_categoryEditorFragment)
            it.hideKeyboard()
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
}