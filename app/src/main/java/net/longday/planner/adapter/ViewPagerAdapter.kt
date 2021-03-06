package net.longday.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.longday.planner.R

class ViewPagerAdapter: RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder>() {

    var tabs = (0..5).toList()

    class ViewPagerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById<TextView>(R.id.pager_tab)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        return ViewPagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.tab, parent,false))
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.textView.text = tabs[position].toString()
    }

    override fun getItemCount(): Int {
        return tabs.size
    }

}