package net.longday.planner.viewmodel

import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.filterNotNull
import net.longday.planner.data.repository.CategoryRepository

class CategoryViewModel(private val categoryRepository: CategoryRepository) {

    val categories = categoryRepository.categories.filterNotNull().asLiveData()
}