package net.longday.planner.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.ui.CategoryContentFragment

class ViewPagerAdapter(
    fragment: Fragment,
    var categories: List<Category>,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        val fragment = CategoryContentFragment()
        fragment.arguments = bundleOf("category" to categories[position])
        return fragment
    }
}