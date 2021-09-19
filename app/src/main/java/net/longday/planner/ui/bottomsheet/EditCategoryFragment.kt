package net.longday.planner.ui.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.databinding.FragmentEditCategoryBinding
import net.longday.planner.viewmodel.CategoryViewModel

@AndroidEntryPoint
class EditCategoryFragment : BottomSheetDialogFragment() {

    private val categoryViewModel: CategoryViewModel by viewModels()

    private var categories = listOf<Category>()

    private var _binding: FragmentEditCategoryBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val category: Category? = arguments?.get("category") as Category?
        val editText = binding.fragmentEditCategoryTextInput
        editText.requestFocus()
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            categories = it
            if (it.size <= 1) binding.fragmentEditCategoryDeleteButton.isEnabled = false
        }
        editText.editText?.setText(category?.title)
        editText.setEndIconOnClickListener {
            categoryViewModel.update(
                Category(
                    category?.id ?: "",
                    editText.editText?.text.toString(),
                    category?.position ?: -1,
                )
            )
            try {
                findNavController()
                    .navigate(R.id.action_editCategoryFragment_to_categoryEditorFragment)
            } catch (e: IllegalArgumentException) {
            }
            it.hideKeyboard()
        }
        binding.fragmentEditCategoryDeleteButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.delete_done_task_dialog_title))
            builder.setMessage(getString(R.string.delete_category_dialog_message))
            builder.setPositiveButton(
                getString(R.string.fragment_edit_category_delete_button_text)
            ) { _, _ ->
                categoryViewModel.delete(
                    Category(
                        category?.id ?: "",
                        editText.editText?.text.toString(),
                        category?.position ?: -1,
                    )
                )
                try {
                    findNavController()
                        .navigate(R.id.action_editCategoryFragment_to_categoryEditorFragment)
                } catch (e: IllegalArgumentException) {
                }
                it.hideKeyboard()
            }
            builder.setNegativeButton(
                getString(R.string.delete_done_task_dialog_cancel_button_text)
            ) { _, _ -> }
            builder.show()
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