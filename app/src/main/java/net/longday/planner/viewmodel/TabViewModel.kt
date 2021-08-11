package net.longday.planner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import net.longday.planner.data.entity.Tab
import net.longday.planner.data.repository.TabRepository
import javax.inject.Inject

@HiltViewModel
class TabViewModel @Inject constructor(private val tabRepository: TabRepository) : ViewModel() {

    val tab = tabRepository.tab.filterNotNull().asLiveData()

    fun insert(tab: Tab) = CoroutineScope(Dispatchers.IO).launch {
        tabRepository.insert(tab)
    }

    fun update(tab: Tab) = CoroutineScope(Dispatchers.IO).launch {
        tabRepository.update(tab)
    }

    fun delete(tab: Tab) = CoroutineScope(Dispatchers.IO).launch {
        tabRepository.delete(tab)
    }

}