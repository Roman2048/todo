package net.longday.planner.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.CategoryAdapter
import net.longday.planner.viewmodel.CategoryViewModel

/**
 * Экран со списком категорий, для их редактирования.
 */
@AndroidEntryPoint
class CategoryEditorFragment : Fragment(R.layout.fragment_category_editor) {

    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler: RecyclerView = view.findViewById(R.id.categories_recycler_view)
        val adapter = CategoryAdapter(listOf(), categoryViewModel)
        recycler.adapter = adapter
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            adapter.categories = it
            adapter.notifyDataSetChanged()
        }
    }
}