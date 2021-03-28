package net.longday.planner.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.longday.planner.ui.CategoryContentFragment
import net.longday.planner.data.entity.Category

class ViewPagerAdapter(
    fragment: Fragment,
    var categories: List<Category>,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
//        val fragment = CategoryFragment()
//        fragment.arguments = Bundle().apply {
//            putInt("position", position)
//        }
//        return fragment
        return CategoryContentFragment()
    }
}