package net.longday.planner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.adapter.CategoryAdapter
import net.longday.planner.adapter.TaskAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.viewmodel.CategoryViewModel
import net.longday.planner.viewmodel.TaskViewModel
import java.util.*

/**
 * Фрагмент, содержащий список задач, входящих в данную категорию
 */
@AndroidEntryPoint
class CategoryFragment : Fragment() {

//    private val categoryViewModel: CategoryViewModel by viewModels()

    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView: RecyclerView = view.findViewById(R.id.task_recycler)
        val adapter = TaskAdapter(taskViewModel.tasks.value.orEmpty())
        arguments?.takeIf { it.containsKey("position") }?.apply {
            recyclerView.adapter = adapter
            TODO("отфильтровать таски по позиции")
        }
        taskViewModel.tasks.observe(viewLifecycleOwner) {
            recyclerView.adapter = TaskAdapter(it)
            adapter.notifyDataSetChanged()

        }


//        val newCategoryTextInput = view.findViewById<EditText>(R.id.new_category_text_input)
//        val addTabButton = view.findViewById<MaterialButton>(R.id.add_tab_button)
//        val categoriesRecyclerView = view.findViewById<RecyclerView>(R.id.categories_recycler_view)
//        val categoryAdapter = CategoryAdapter(categoryViewModel.categories.value.orEmpty(), categoryViewModel)
//        categoriesRecyclerView.adapter = categoryAdapter
//        categoryViewModel.categories.observe(viewLifecycleOwner) {
//            categoryAdapter.categories = it
//            categoryAdapter.notifyDataSetChanged()
//            newCategoryTextInput.setText("")
//        }
//        addTabButton.setOnClickListener {
//            categoryViewModel.insert(
//                Category(
//                    UUID.randomUUID().toString(),
//                    newCategoryTextInput.text.toString(),
//                    categoryViewModel.categories.value?.size ?: 0
//                )
//            )
//        }
    }
}