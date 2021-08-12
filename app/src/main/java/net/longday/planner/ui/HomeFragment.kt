package net.longday.planner.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.longday.planner.R
import net.longday.planner.adapter.ViewPagerAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Tab
import net.longday.planner.di.PersistenceModule
import net.longday.planner.viewmodel.CategoryViewModel
import net.longday.planner.viewmodel.TabViewModel

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val categoryViewModel: CategoryViewModel by viewModels()

    private val tabViewModel: TabViewModel by viewModels()

    /**
     * Close app if the back button what pressed on main screen
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity
            ?.onBackPressedDispatcher
            ?.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finish()
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        createNotificationChannel()
        syncNavigationBarColorWithUiNightMode()

        var category = Category("", "")
        val viewPager: ViewPager2 = view.findViewById(R.id.category_view_pager)
        val pagerAdapter = ViewPagerAdapter(this, listOf())
        viewPager.adapter = pagerAdapter
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        var categories: List<Category> = listOf()

        categoryViewModel.categories.observe(viewLifecycleOwner) {
            val sortedCategories = it.sortedBy { cat -> cat.position }
            categories = sortedCategories
            pagerAdapter.categories = sortedCategories
            pagerAdapter.notifyDataSetChanged()
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = sortedCategories[position].title
            }.attach()
        }

        // Handle the tabs
        var newSelect = true
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (!newSelect) {
                    tabViewModel.insert(Tab(1, tabLayout.selectedTabPosition))
                }
                newSelect = false
                category = categories[tab!!.position]
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        tabViewModel.tab.observe(viewLifecycleOwner) {
            viewPager.postDelayed(
                {
                tabLayout.getTabAt(it.selected)?.select()
                viewPager.currentItem = it.selected
                }, 0)
        }

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            // Навигация
            view.findNavController().navigate(
                R.id.action_homeFragment_to_addTaskFragment,
                bundleOf("category" to category)
            )
        }

        val categoryEditorButton: AppCompatImageButton = view.findViewById(R.id.categories_button)
        categoryEditorButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_categoryEditorFragment)
        }

        val settingsButton: BottomAppBar = view.findViewById(R.id.bottom_app_bar)
        settingsButton.setNavigationOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }
    }

    /**
     * Create the NotificationChannel
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                "test_planner_reminders",
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager: NotificationManager =
                requireContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    /**
     * Set Navigation bar color to black when dark theme is active
     */
    private fun syncNavigationBarColorWithUiNightMode() {
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                requireActivity().window.navigationBarColor = Color.BLACK
            }
            Configuration.UI_MODE_NIGHT_NO -> {
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }
    }
}