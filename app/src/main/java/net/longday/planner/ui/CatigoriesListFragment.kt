package net.longday.planner.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.CategoryAdapter
import net.longday.planner.viewmodel.CategoryViewModel

/**
 * Экран со списком категорий, для их редактирования.
 */
@AndroidEntryPoint
class CatigoriesListFragment : Fragment(R.layout.fragment_categories_list) {

    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler: RecyclerView = view.findViewById(R.id.categories_recycler_view)
        val adapter = CategoryAdapter(listOf())
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
        val fab: FloatingActionButton = view.findViewById(R.id.category_editor_fab)
        fab.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_categoryEditorFragment_to_addCategoryFragment)
        }
    }
}