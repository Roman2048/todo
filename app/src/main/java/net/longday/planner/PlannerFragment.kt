package net.longday.planner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.adapter.ViewPagerAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.CategoryViewModel
import net.longday.planner.viewmodel.TaskViewModel
import java.util.*

@AndroidEntryPoint
class PlannerFragment : Fragment() {

    private val taskViewModel: TaskViewModel by viewModels()

    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_planner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Instantiate a ViewPager2 and a PagerAdapter.
        val viewPager: ViewPager2 = view.findViewById(R.id.category_view_pager)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ViewPagerAdapter(this, listOf())
        viewPager.adapter = pagerAdapter

//        val categoriesButton = view.findViewById<Button>(R.id.categories_button)
//
//        categoriesButton.setOnClickListener {
//            view.findNavController().navigate(R.id.action_homeFragment_to_categoryFragment)
//        }
//
//        var viewPagerAdapter = ViewPagerAdapter(CategoryFragment(), listOf())
//
//        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager)
//
//        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
//
//        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
//            viewPagerAdapter = ViewPagerAdapter(CategoryFragment(), categories)
//            viewPagerAdapter.notifyDataSetChanged()
//            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
//                tab.text = categories[position].title
//            }.attach()
//
//        }

//        taskViewModel.tasks.observe(viewLifecycleOwner) {
//            viewPagerAdapter = ViewPagerAdapter(it)
//            viewPager.adapter = viewPagerAdapter
//            viewPagerAdapter.notifyDataSetChanged()
//        }
//
//        viewPager.adapter = viewPagerAdapter
    }
}

