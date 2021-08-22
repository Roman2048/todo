package net.longday.planner.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
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
import androidx.navigation.findNavController
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

    private val categoryViewModel: CategoryViewModel by viewModels()

    private val tabViewModel: TabViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

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
        createNotificationChannel()
        syncNavigationBarColorWithUiNightMode()
        val pagerAdapter = ViewPagerAdapter(this, listOf())
        binding.categoryViewPager.adapter = pagerAdapter
        var category = Category("", "")
        var categories = listOf<Category>()

        categoryViewModel.categories.observe(viewLifecycleOwner) {
            val sortedCategories = it.sortedBy { cat -> cat.position }
            categories = sortedCategories
            pagerAdapter.categories = sortedCategories
            pagerAdapter.notifyDataSetChanged()
            TabLayoutMediator(binding.tabLayout, binding.categoryViewPager) { tab, position ->
                tab.text = sortedCategories[position].title
            }.attach()
        }

        // Handle the tabs
        var newSelect = true
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (!newSelect) {
                    tabViewModel.insert(Tab(1, binding.tabLayout.selectedTabPosition))
                }
                newSelect = false
                category = categories[tab!!.position]
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        tabViewModel.tab.observe(viewLifecycleOwner) {
            binding.categoryViewPager.postDelayed(
                {
                    binding.tabLayout.getTabAt(it.selected)?.select()
                    binding.categoryViewPager.currentItem = it.selected
                }, 0
            )
        }

        binding.fab.setOnClickListener {
            // Навигация
            view.findNavController().navigate(
                R.id.action_homeFragment_to_addTaskFragment,
                bundleOf("category" to category)
            )
        }

        binding.categoriesButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_categoryEditorFragment)
        }

        binding.bottomAppBar.setNavigationOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
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
//            notificationChannel.vibrationPattern = longArrayOf(700L)
            notificationChannel.importance = NotificationManager.IMPORTANCE_HIGH
            notificationChannel.enableVibration(true)
            val notificationManager =
                requireContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.deleteNotificationChannel("test_planner_reminders");
            notificationManager.createNotificationChannel(notificationChannel)
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