package net.longday.planner.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.adapter.ViewPagerAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.viewmodel.CategoryViewModel

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val categoryViewModel: CategoryViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set Navigation bar color to black when dark theme is active
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {requireActivity().window.navigationBarColor = Color.BLACK}
            Configuration.UI_MODE_NIGHT_NO -> {}
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
        val pref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        var category: Category = Category("", "")
        val viewPager: ViewPager2 = view.findViewById(R.id.category_view_pager)
        val pagerAdapter = ViewPagerAdapter(this, listOf())
        viewPager.adapter = pagerAdapter
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        var categories: List<Category> = listOf()
        val categoryId: String? = arguments?.get("categoryId") as String?
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            val sortedCategories = it.sortedBy { cat -> cat.position }
            categories = sortedCategories
            pagerAdapter.categories = sortedCategories
            pagerAdapter.notifyDataSetChanged()
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = sortedCategories[position].title

            }.attach()
            // Выбор вкладки при возврате с деталей таски
            if (categoryId != null) {
                Log.d("NAVIGATE", "categories.size = ${categories.size}")
//                val categoryToReturn = categories.filter { cat -> cat.id == categoryId }[0]
//                viewPager.postDelayed({ viewPager.currentItem = categoryToReturn.position }, 100)
                val pos = pref.getInt("categoryPositionToReturn", 1)
                Log.d("PREF", "i read shared pref and it is = $pos")
                viewPager.post { viewPager.currentItem = pos }
//                viewPager.postDelayed({ viewPager.currentItem = pos }, 100)
            }

        }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                category = categories[tab!!.position]
                if (categoryId == null) {
                    with (pref.edit()) {
                        putInt("categoryPositionToReturn", viewPager.currentItem)
                        Log.d("PREF", "save pref viewPager.currentItem = ${viewPager.currentItem}")
                        apply()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })



        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            view.findNavController().navigate(
                R.id.action_homeFragment_to_addTaskFragment,
                bundleOf("category" to category)
            )
        }

        val categoryEditorButton: AppCompatImageButton = view.findViewById(R.id.categories_button)
        categoryEditorButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_categoryEditorFragment)
        }

        val appBarButton: BottomAppBar = view.findViewById(R.id.bottom_app_bar)
        appBarButton.setNavigationOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

//        val reminderButton: ImageButton = view.findViewById(R.id.home_fragment_reminder_button)
//        reminderButton.setOnClickListener {
//            val message = "Делай красиво! " + elapsedRealtime()
//            val intent = Intent(context, ReminderActivity::class.java).apply {
//                putExtra(EXTRA_MESSAGE, message)
//            }
//            startActivity(intent)
//        }
    }
}