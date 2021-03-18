package net.longday.planner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.adapter.ViewPagerAdapter
import net.longday.planner.data.entity.Task
import net.longday.planner.di.PersistenceModule
import net.longday.planner.viewmodel.TaskViewModel
import java.util.*

@AndroidEntryPoint
class PlannerFragment : Fragment() {

//    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_planner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

//        val taskViewModel = TaskViewModel(PersistenceModule.provideTaskDao(PersistenceModule.provideDatabase(PlannerApplication())))
        val tasks = taskViewModel.tasks.value

        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager)

        val myTasks = listOf(
            Task(UUID.randomUUID().toString(), "aaa", "0"),
            Task(UUID.randomUUID().toString(), "bbb", "0"),
            Task(UUID.randomUUID().toString(), "ccc", "0"),
            Task(UUID.randomUUID().toString(), "ddd", "1"),
            Task(UUID.randomUUID().toString(), "eee", "1"),
            Task(UUID.randomUUID().toString(), "fff", "1"),
        )
        val viewPagerAdapter = ViewPagerAdapter(myTasks.orEmpty())

        viewPager.adapter = viewPagerAdapter

        val addTabButton = view.findViewById<MaterialButton>(R.id.add_tab_button)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = "sup"
        }.attach()


        addTabButton.setOnClickListener {
            taskViewModel.insert(
                Task(UUID.randomUUID().toString(),
                view.findViewById<EditText>(R.id.new_task_text_input).text.toString(),
                "0"))
            viewPagerAdapter?.notifyDataSetChanged()
        }
    }

}

