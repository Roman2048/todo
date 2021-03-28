package net.longday.planner.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.longday.planner.CategoryFragment
import net.longday.planner.R
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task

class ViewPagerAdapter(
    fragment: Fragment,
    private val categories: List<Category>,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
//        val fragment = CategoryFragment()
//        fragment.arguments = Bundle().apply {
//            putInt("position", position)
//        }
//        return fragment
        return CategoryFragment()
    }
}


//var categories: List<Category> = listOf(Category("1", "1", 0))
//
//class ViewPagerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//    val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
//}
//
//override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
//    return ViewPagerViewHolder(
//        LayoutInflater.from(parent.context).inflate(R.layout.tab_content, parent, false)
//    )
//}
//
//override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
//    holder.recyclerView.adapter =
//        TaskAdapter(tasks.filter { it.categoryId == position.toString() })
//}
//
//override fun getItemCount(): Int {
//    return categories.size
//}