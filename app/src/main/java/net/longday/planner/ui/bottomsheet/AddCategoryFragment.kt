package net.longday.planner.ui.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.retrofit.RetrofitClient
import net.longday.planner.retrofit.RetrofitServices
import net.longday.planner.viewmodel.CategoryViewModel
import retrofit2.create
import java.util.*

@AndroidEntryPoint
class AddCategoryFragment : BottomSheetDialogFragment() {

    private val categoryViewModel: CategoryViewModel by viewModels()

    private val job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editText: TextInputLayout =
            view.findViewById(R.id.fragment_add_category_text_input)
        editText.requestFocus()
        val saveButton: Button = view.findViewById(R.id.fragment_add_category_save_button)
        val navController =
            activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment)
                ?.findNavController()
        saveButton.setOnClickListener {
            categoryViewModel.insert(
                Category(
                    UUID.randomUUID().toString(),
                    editText.editText?.text.toString(),
                )
            )
            val getCategoryListService = RetrofitClient.getClient("http://longday.net/test/")
                .create(RetrofitServices::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                runCatching{
                    getCategoryListService.getCategoryList().execute().body()
                }
            }.start()
            ioScope.launch {
                getCategoryListService.getCategoryList().execute().body()
            }.start()

            navController?.navigate(R.id.action_addCategoryFragment_to_categoryEditorFragment)
            it.hideKeyboard()
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