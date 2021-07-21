package net.longday.planner.ui

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
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
                private var dragFromPosition = -1
                private var dragToPosition = -1
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val adapter = recyclerView.adapter as CategoryAdapter
                    val innerFrom = viewHolder.adapterPosition
                    val innerTo = target.adapterPosition
                    if (dragFromPosition == -1) {
                        dragFromPosition = viewHolder.adapterPosition
                    }
                    dragToPosition = target.adapterPosition
                    adapter.notifyItemMoved(innerFrom, innerTo)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    super.onSelectedChanged(viewHolder, actionState)
                    Log.d(
                        "DRAG",
                        "onSelectedChanged\nСостояние actionState = $actionState.\ndragFromPosition = $dragFromPosition\ndragToPosition = $dragToPosition"
                    )
                    when (actionState) {
                        ACTION_STATE_DRAG -> {
                            viewHolder?.also { dragToPosition = it.adapterPosition }
                        }
                        ACTION_STATE_IDLE -> {
                            if (dragFromPosition != -1 && dragToPosition != -1 && dragFromPosition != dragToPosition) {
                                // Item successfully dragged
                                moveItem(dragFromPosition, dragToPosition, categoryViewModel)
                                // Reset drag positions
                                dragFromPosition = -1
                                dragToPosition = -1
                            }
                        }
                    }
                }
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler: RecyclerView = view.findViewById(R.id.categories_recycler_view)
        itemTouchHelper.attachToRecyclerView(recycler)
        val addCategoryItem: MaterialTextView =
            view.findViewById(R.id.categories_add_new_category_item)
        val adapter = CategoryAdapter(mutableListOf())
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
        Log.d("DRAG", "moveItem: from($from), to($to)")
//        object : CountDownTimer(2000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {}
//            override fun onFinish() {
        val categories = categoryViewModel.categories.value ?: listOf<Category>()
        val sortedCategories = categories.sortedBy { it.position }
        val mutableSortedCategories = sortedCategories.toMutableList()
        val itemToMove = sortedCategories[from]
        mutableSortedCategories.removeAt(from)
        mutableSortedCategories.add(to, itemToMove)
        /* После выполнения кода выше должен получится лист с правильным порядоком, но неправильными position */
        mutableSortedCategories.forEachIndexed { index, category ->
            category.position = index
        }
        /* Получен лист с исправлеными position */
        mutableSortedCategories.forEach {
            categoryViewModel.update(
                Category(
                    it.id,
                    it.title,
                    it.position,
                )
            )
        }
//            }
//        }.start()
//        Toast.makeText(requireContext(), "try to move: from = $from, to = $to", Toast.LENGTH_SHORT).show()
    }
}