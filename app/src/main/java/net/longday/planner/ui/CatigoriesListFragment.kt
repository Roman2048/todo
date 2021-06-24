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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
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

    private val itemTouchHelper by lazy {
        // 1. Note that I am specifying all 4 directions.
        //    Specifying START and END also allows
        //    more organic dragging than just specifying UP and DOWN.
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(UP or
                    DOWN or
                    START or
                    END, 0) {

                override fun onMove(recyclerView: RecyclerView,
                                    viewHolder: RecyclerView.ViewHolder,
                                    target: RecyclerView.ViewHolder): Boolean {

                    val adapter = recyclerView.adapter as CategoryAdapter
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition
                    // 2. Update the backing model. Custom implementation in
                    //    MainRecyclerViewAdapter. You need to implement
                    //    reordering of the backing model inside the method.
//                    adapter.moveItem(from, to)
                    // 3. Tell adapter to render the model update.
                    adapter.notifyItemMoved(from, to)

                    return true
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                      direction: Int) {
                    // 4. Code block for horizontal swipe.
                    //    ItemTouchHelper handles horizontal swipe as well, but
                    //    it is not relevant with reordering. Ignoring here.
                }
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }

    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler: RecyclerView = view.findViewById(R.id.categories_recycler_view)
        itemTouchHelper.attachToRecyclerView(recycler)
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
            view.findNavController()
                .navigate(R.id.action_categoryEditorFragment_to_addCategoryFragment)
            it.showKeyboard()
        }
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }
}