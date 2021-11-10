package net.longday.planner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.CategoryAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.databinding.FragmentListManagementBinding
import net.longday.planner.viewmodel.CategoryViewModel

/**
 * List management screen
 */
@AndroidEntryPoint
class ListManagementFragment : Fragment(R.layout.fragment_list_management) {

    private var _binding: FragmentListManagementBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()
    private var categoryList = listOf<Category>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var addListTextView: MaterialTextView
    private lateinit var backButton: AppCompatImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /* Handle lists reordering by drag & drop */
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
                    when (actionState) {
                        ACTION_STATE_DRAG -> {
                            viewHolder?.also { dragToPosition = it.adapterPosition }
                        }
                        ACTION_STATE_IDLE -> {
                            if (dragFromPosition != -1
                                && dragToPosition != -1
                                && dragFromPosition != dragToPosition
                            ) {
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
        recyclerView = binding.listManagementRecycler
        addListTextView = binding.listManagementAddList
        backButton = binding.listManagementBackButton
        itemTouchHelper.attachToRecyclerView(recyclerView)
        val adapter = CategoryAdapter(mutableListOf())
        recyclerView.adapter = adapter
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            categoryList = it
            adapter.categories = it.sortedBy { category -> category.position }
            adapter.notifyDataSetChanged()
        }
        backButton.setOnClickListener {
//            findNavController().popBackStack()
            try {
                findNavController().navigate(R.id.homeFragment)
            } catch (e: IllegalArgumentException) {
            }
        }
        addListTextView.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_listManagement_to_addCategory)
            } catch (e: IllegalArgumentException) {
            }
        }
    }

    /* Sort lists after reordering */
    private fun moveItem(from: Int, to: Int, categoryViewModel: CategoryViewModel) {
        val sortedCategories = categoryList.sortedBy { it.position }
        val itemToMove = sortedCategories[from]
        val mutableSortedCategories = sortedCategories.toMutableList()
        mutableSortedCategories.removeAt(from)
        mutableSortedCategories.add(to, itemToMove)
        mutableSortedCategories.forEachIndexed { index, category ->
            category.position = index
            categoryViewModel.update(category)
        }
    }
}