package net.longday.planner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task
import net.longday.planner.data.repository.CategoryRepository
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    val categories = categoryRepository.categories.filterNotNull().asLiveData()

    fun insert(category: Category) = CoroutineScope(Dispatchers.IO).launch {
        categoryRepository.insert(category)
    }
}