package net.longday.planner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import net.longday.planner.adapter.FakeDataset
import net.longday.planner.adapter.ViewPagerAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPager = view.findViewById(R.id.pager)
        val viewPagerAdapter = ViewPagerAdapter()
        viewPager.adapter = viewPagerAdapter
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = FakeDataset.categories[position].title
        }.attach()
        val addTabButton = view.findViewById<MaterialButton>(R.id.add_tab_button)
        addTabButton.setOnClickListener {
//            viewPagerAdapter.tabs.add(13)
//            FakeDataset.tasks.add(Task(UUID.randomUUID().toString(), "new task title", "0"))
            FakeDataset.categories.add(Category(UUID.randomUUID().toString(), "cat X", 0))
            viewPagerAdapter.notifyDataSetChanged()
        }


    }
}