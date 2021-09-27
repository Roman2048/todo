package net.longday.planner.ui.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.R.style.AddTaskBottomSheetStyle
import net.longday.planner.data.entity.Category
import net.longday.planner.databinding.FragmentEditCategoryBinding
import net.longday.planner.viewmodel.CategoryViewModel

@AndroidEntryPoint
class EditCategoryFragment : DialogFragment() {

    private var _binding: FragmentEditCategoryBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()

    private var categories = listOf<Category>()

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
        fixLayoutDefaults()
        val editText = binding.fragmentEditCategoryTextInput
        editText.requestFocus()
        view.postDelayed({ view.showKeyboard() }, 100)
        val category: Category? = arguments?.get("category") as Category?
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
            findNavController().popBackStack()
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
                findNavController().popBackStack()
            }
            builder.setNegativeButton(
                getString(R.string.delete_done_task_dialog_cancel_button_text)
            ) { _, _ -> }
            builder.show()
        }
    }

    override fun getTheme() = AddTaskBottomSheetStyle

    /**
     * Show keyboard when the dialog is opened
     */
    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    private fun fixLayoutDefaults() {
        requireDialog().window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
        requireDialog().window?.setGravity(Gravity.BOTTOM)
    }
}