package net.longday.planner.ui.bottomsheet

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.viewmodel.CategoryViewModel
import java.util.*

@AndroidEntryPoint
class EditCategoryFragment : BottomSheetDialogFragment() {

    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val category: Category? = arguments?.get("category") as Category?
        val editText: TextInputLayout =
            view.findViewById(R.id.fragment_edit_category_text_input)
        editText.requestFocus()
        val saveButton: Button = view.findViewById(R.id.fragment_edit_category_save_button)
        val deleteButton: Button = view.findViewById(R.id.fragment_edit_category_delete_button)
        editText.editText?.setText(category?.title)
        val navController =
            activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment)
                ?.findNavController()
        saveButton.setOnClickListener {
            categoryViewModel.update(
                Category(
                    category?.id ?: "",
                    editText.editText?.text.toString(),
                    category?.position ?: -1,
                )
            )
            navController?.navigate(R.id.action_editCategoryFragment_to_categoryEditorFragment)
            it.hideKeyboard()
        }
        deleteButton.setOnClickListener {
            categoryViewModel.delete(
                Category(
                    category?.id ?: "",
                    editText.editText?.text.toString(),
                    category?.position ?: -1,
                )
            )
            navController?.navigate(R.id.action_editCategoryFragment_to_categoryEditorFragment)
            it.hideKeyboard()
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetStyle
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
}