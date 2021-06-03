package net.longday.planner.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.ViewPagerAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.CategoryViewModel
import net.longday.planner.viewmodel.TaskViewModel
import java.util.*

/**
 * Основной рабочий экран приложения.
 * Сверху отображается список категорий в виде вкладок, и кнопка для перехода на страницу
 * для их редактирования.
 * Основное содержимое - список задач для выбранной влкадки.
 */
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val categoryViewModel: CategoryViewModel by viewModels()

    private val taskViewModel: TaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val categoryId: String = arguments?.get("category_id").toString()
        val viewPager: ViewPager2 = view.findViewById(R.id.category_view_pager)
        val pagerAdapter = ViewPagerAdapter(this, listOf())
        viewPager.adapter = pagerAdapter
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        var categories: List<Category> = listOf()
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            categories = it
            pagerAdapter.categories = it
            pagerAdapter.notifyDataSetChanged()
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = it[position].title
            }.attach()
        }

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            BottomSheetDialog(requireContext(), R.style.BottomSheetStyle).apply {
                setContentView(layoutInflater.inflate(R.layout.bottom_sheet, null))
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                show()
                this.findViewById<Button>(R.id.new_task_save_button)?.setOnClickListener {
                    if (categoryId != "") {
                        taskViewModel.insert(
                            Task(
                                UUID.randomUUID().toString(),
                                this.findViewById<EditText>(R.id.bottom_sheet_edit_text)?.text.toString(),
                                "0a8ffcdb-e233-4f57-b0f0-66b36e4163f5"
//                                categoryId
                            )
                        )
                    }
                    val text = "Hello toast!"
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(requireContext(), text, duration)
                    toast.show()
                }
            }
        }

        val categoryEditorButton: AppCompatImageButton = view.findViewById(R.id.categories_button)
        categoryEditorButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_categoryEditorFragment)
        }

        val mDrawerLayout = requireActivity().findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val appBarButton: BottomAppBar = view.findViewById(R.id.bottom_app_bar)
        appBarButton.setNavigationOnClickListener {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }
}