package net.longday.planner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.ViewPagerAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.viewmodel.CategoryViewModel

/**
 * Основной экран приложения.
 * Сверху отображается список категорий в виде вкладок, и кнопка для перехода на страницу
 * для их редактирования.
 * Основное содержимое - список задач для выбранной влкадки.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
            view.findNavController().navigate(
                R.id.action_homeFragment_to_addTaskFragment,
                bundleOf("category_id" to categories[viewPager.currentItem].id)
            )
        }
        val categoryEditorButton: Button = view.findViewById(R.id.categories_button)
        categoryEditorButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_categoryEditorFragment)
        }
    }
}

