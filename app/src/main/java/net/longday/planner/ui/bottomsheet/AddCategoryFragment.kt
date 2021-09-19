package net.longday.planner.ui.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.viewmodel.CategoryViewModel
import java.util.*

@AndroidEntryPoint
class AddCategoryFragment : BottomSheetDialogFragment() {

    private val categoryViewModel: CategoryViewModel by viewModels()
    private val job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.showKeyboard()
        // Обновляем позиции если одна из них равна "-1" (то есть добавлена новая категория)
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            if (it.any { category -> category.position == -1 }) {
                updateOrder(it)
            }
        }
        val editText: TextInputLayout = view.findViewById(R.id.fragment_add_category_text_input)
        editText.requestFocus()
        /* Save new task */
        editText.setEndIconOnClickListener {
            val newCategory = Category(
                UUID.randomUUID().toString(),
                editText.editText?.text.toString(),
            )
            categoryViewModel.insert(newCategory)
            try {
                findNavController()
                    .navigate(R.id.action_addCategoryFragment_to_categoryEditorFragment)
            } catch (e: IllegalArgumentException) {
            }
            it.hideKeyboard()
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetStyle
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    private fun updateOrder(categories: List<Category>) {
        ioScope.launch {
            if (categories.isNotEmpty()) {
                val sortedCategories = categories.sortedBy { it.position }
                val mutableSortedCategories = sortedCategories.toMutableList()
                mutableSortedCategories.forEachIndexed { index, category ->
                    category.position = index
                }
                mutableSortedCategories.forEach {
                    categoryViewModel.update(
                        Category(
                            it.id,
                            it.title,
                            it.position,
                        )
                    )
                }
            }
        }.start()
    }
}