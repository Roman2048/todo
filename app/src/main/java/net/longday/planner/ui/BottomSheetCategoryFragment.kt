package net.longday.planner.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.viewmodel.CategoryViewModel
import java.util.*

@AndroidEntryPoint
class BottomSheetCategoryFragment : BottomSheetDialogFragment() {

    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val category: Category? = arguments?.get("category") as Category?
        val type: String? = arguments?.get("type") as String?
        val editText: TextInputLayout =
            view.findViewById(R.id.fragment_bottom_sheet_category_edit_text)
        val saveButton: Button = view.findViewById(R.id.fragment_bottom_sheet_category_save_button)
        editText.editText?.setText(category?.title)
        val navController =
            activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment)
                ?.findNavController()
        saveButton.setOnClickListener {
            when (type) {
                "create" -> {
                    categoryViewModel.insert(
                        Category(
                            UUID.randomUUID().toString(),
                            editText.editText?.text.toString(),
                        )
                    )
                }
                "update" -> {
                    categoryViewModel.update(
                        Category(
                            category?.id ?: "",
                            editText.editText?.text.toString(),
                        )
                    )
                }
            }
            navController?.navigate(R.id.action_bottomSheetCategoryFragment_to_categoryEditorFragment)
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