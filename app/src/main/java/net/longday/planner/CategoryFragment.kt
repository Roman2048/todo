package net.longday.planner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.adapter.CategoryAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.viewmodel.CategoryViewModel
import java.util.*

@AndroidEntryPoint
class CategoryFragment : Fragment() {

    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val newCategoryTextInput = view.findViewById<EditText>(R.id.new_category_text_input)
        val addTabButton = view.findViewById<MaterialButton>(R.id.add_tab_button)
        val categoriesRecyclerView = view.findViewById<RecyclerView>(R.id.categories_recycler_view)
        val categoryAdapter = CategoryAdapter(categoryViewModel.categories.value.orEmpty())
        categoriesRecyclerView.adapter = categoryAdapter
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            categoryAdapter.categories = it
            categoryAdapter.notifyDataSetChanged()
            newCategoryTextInput.setText("")
        }
        addTabButton.setOnClickListener {
            categoryViewModel.insert(
                Category(
                    UUID.randomUUID().toString(),
                    newCategoryTextInput.text.toString(),
                    categoryViewModel.categories.value?.size ?: 0
                )
            )
        }
    }
}