package net.longday.planner.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.CategoryAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.CategoryViewModel
import java.util.*

/**
 * Экран со списком категорий, для их редактирования.
 */
@AndroidEntryPoint
class CatigoriesListFragment : Fragment(R.layout.fragment_categories_list) {

    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler: RecyclerView = view.findViewById(R.id.categories_recycler_view)
        val addCategoryItem: MaterialTextView =
            view.findViewById(R.id.categories_add_new_category_item)
        val adapter = CategoryAdapter(listOf(), requireContext())
        recycler.adapter = adapter
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            adapter.categories = it
            adapter.notifyDataSetChanged()
        }
        val backButton: AppCompatImageButton = view.findViewById(R.id.category_editor_edit_button)
        backButton.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_categoryEditorFragment_to_homeFragment)
        }

        addCategoryItem.setOnClickListener {
            BottomSheetDialog(requireContext(), R.style.BottomSheetStyle).apply {
                it.showKeyboard()
                setContentView(layoutInflater.inflate(R.layout.bottom_sheet_category, null))
                val categoryEditText: TextInputLayout? =
                    this.findViewById(R.id.bottom_sheet_category_edit_text)
                val categorySaveButton: Button? =
                    this.findViewById(R.id.bottom_sheet_category_save_button)
                categoryEditText?.requestFocus()
                show()
                categorySaveButton?.setOnClickListener {
                    categoryViewModel.insert(
                        Category(
                            id = UUID.randomUUID().toString(),
                            title = categoryEditText?.editText?.text.toString(),
                        )
                    )
                    hide()
                }
            }
        }
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }
}