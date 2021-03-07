package net.longday.planner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import net.longday.planner.adapter.FakeDataset
import net.longday.planner.adapter.ViewPagerAdapter

class PlannerFragment : Fragment() {

    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_planner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPager = view.findViewById(R.id.view_pager)
        val viewPagerAdapter = ViewPagerAdapter()
        viewPager.adapter = viewPagerAdapter
        val addTabButton = view.findViewById<MaterialButton>(R.id.add_tab_button)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = FakeDataset.categories[position].title
        }.attach()
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                addTabButton.text = "super"
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })



        addTabButton.setOnClickListener {
//            viewPagerAdapter.tabs.add(13)
//            FakeDataset.tasks.add(Task(UUID.randomUUID().toString(),  view.findViewById<EditText>(R.id.new_task_text_input).text.toString(), "0"))
//            FakeDataset.categories.add(Category(UUID.randomUUID().toString(), "cat X", 0))
            viewPagerAdapter.notifyDataSetChanged()
        }


    }
}