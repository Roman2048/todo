package net.longday.planner.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.longday.planner.R
import net.longday.planner.adapter.ViewPagerAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Statistic
import net.longday.planner.data.entity.Tab
import net.longday.planner.databinding.FragmentHomeBinding
import net.longday.planner.retrofit.RetrofitClient
import net.longday.planner.retrofit.StatisticService
import net.longday.planner.viewmodel.CategoryViewModel
import net.longday.planner.viewmodel.TabViewModel

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()

    private val tabViewModel: TabViewModel by viewModels()

    private lateinit var viewPager: ViewPager2

    private lateinit var tabLayout: TabLayout

    private var sortedCategories = listOf<Category>()

    private var chosenCategory: Category? = null

    /**
     * Close app if the back button what pressed on main screen
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPager = binding.categoryViewPager
        tabLayout = binding.tabLayout
        createNotificationChannel()
        syncNavigationBarColorWithUiNightMode()
        val pagerAdapter = ViewPagerAdapter(this, sortedCategories)
        viewPager.adapter = pagerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = sortedCategories[position].title
        }.attach()
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            sortedCategories = it.sortedBy { cat -> cat.position }
            pagerAdapter.categories = sortedCategories
            pagerAdapter.notifyDataSetChanged()
            /* Create new task if activity started by SEND intent */
            val intent = requireActivity().intent
            when (intent.action) {
                Intent.ACTION_SEND -> {
                    if (intent.type == "text/plain") {
                        try {
                            findNavController().navigate(
                                R.id.action_homeFragment_to_addTaskFragment,
                                bundleOf(
                                    Pair("category", chosenCategory ?: sortedCategories[0]),
                                    Pair("intent", intent)
                                )
                            )
                        } catch (e: IllegalArgumentException) {
                        }
                    }
                }
            }
        }

        var newSelect = true
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                /* Save last used Tab position */
                if (!newSelect) {
                    tabViewModel.insert(Tab(selected = tabLayout.selectedTabPosition))
                }
                newSelect = false
                /* Save current category. Used in bundle when navigate to AddTaskFragment */
                chosenCategory = sortedCategories[tab?.position ?: 0]
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        /* Workaround to return on last used Tab when navigate to HomeFragment from other places */
        // FIXME: Blink when changing tab to last used
        tabViewModel.tab.observe(viewLifecycleOwner) {
            viewPager.post { viewPager.currentItem = it.selected }
        }

        /* Create new task fab */ //IllegalArgumentException
        binding.fab.setOnClickListener {
            try {
                findNavController().navigate(
                    R.id.action_homeFragment_to_addTaskFragment,
                    bundleOf("category" to chosenCategory)
                )
            } catch (e: IllegalArgumentException) {
            }
        }

        binding.categoriesButton.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_homeFragment_to_categoryEditorFragment)
            } catch (e: IllegalArgumentException) {
            }
        }

        binding.bottomAppBar.setNavigationOnClickListener {
            try {
                findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
            } catch (e: IllegalArgumentException) {
            }
        }
    }

    /**
     * Create the NotificationChannel
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "planner_nc_1000",
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableVibration(true)
            val notificationManager =
                requireContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    /**
     * Set Navigation bar color to black when dark theme is active
     */
    private fun syncNavigationBarColorWithUiNightMode() {
        val uiMode = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)
        if (uiMode == Configuration.UI_MODE_NIGHT_YES) {
            requireActivity().window.navigationBarColor = Color.BLACK
        }
    }

    private fun sendStats() {
        CoroutineScope(Dispatchers.IO).launch {
//            val tasks = categoryViewModel.categories.value.size
            RetrofitClient
                .getClient("https://longday.net/")
                .create(StatisticService::class.java)
                .sendStatistic(
                    Statistic(
                        userId = "",
                        categories = 1,
                        tasks = 2,
                        reminders = 3,
                    )
                )
        }
    }
}